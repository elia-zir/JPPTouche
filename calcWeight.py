import csv
import subprocess
from os import listdir
from os.path import join

# Author: Luca Martinelli (luca.martinelli.1@studenti.unipd.it)
# Version: 1.0.0
#
# Get the best configuration of weights for body, premises and conclusion fields


javaFile = None

# Search for jar files in current directory
jarFiles = [f for f in listdir(
    './') if (join('./', f).endswith(".jar"))]

if len(jarFiles) > 0:
    javaFile = jarFiles[0]

# Sets experiment folder and position of run file

experimentFolder = './experiment'
defaultRunFile = experimentFolder + '/run.txt'

qrelsFile = experimentFolder + '/touche2020-corrected.qrels'

trec_results = []

i = 1

# Sets min, max and step of values to pass as weights

min_val = 0
max_val = 1
step = 0.25

# Multiplier of weights for using range
step_mul = 100

# Multiply vals and include max value
min_val = int(min_val * step_mul)
max_val = int((max_val + step) * step_mul)
step = int(step * step_mul)

# Append range to other custom values (not used)
values = []
values.extend(range(min_val, max_val, step))

# Print information about the run
total_m = int(pow(len(values), 3))
eta_m = str(int(total_m / 100)) + ":" + str(int((total_m % 100) * 0.6)) + ":00"
print("#measures: {total_m}\tETA: {eta_m}\n".format(
    total_m=total_m,
    eta_m=eta_m))

# Save max nDCG@5 and its configuration
max_ndcg = 0
max_config = 0

# Calculates all run
if javaFile != None:
    for bodyWeight in values:
        bodyWeight = bodyWeight / step_mul
        for premisesWeight in values:
            premisesWeight = premisesWeight / step_mul
            for conclusionWeight in values:
                conclusionWeight = conclusionWeight / step_mul

                runID = "bm25-{bw}-{pw}-{cw}".format(
                    bw=bodyWeight, pw=premisesWeight, cw=conclusionWeight
                )

                # Setup java file to execute
                configuration = ['java', '-cp', javaFile,
                                 'it.unipd.dei.jpp.search.Searcher', str(bodyWeight), str(premisesWeight), str(conclusionWeight), runID]

                print("RUN #{index:d}\n\tbody: {bw}\tpremises: {pw}\tconclusion: {cw}".format(
                    index=i, bw=bodyWeight, pw=premisesWeight, cw=conclusionWeight))

                # Execute run
                result = subprocess.run(configuration, stdout=subprocess.PIPE)

                # Get trec_eval evaluation
                runFile = experimentFolder + "/" + runID + ".txt"
                trec_eval = subprocess.run(
                    [experimentFolder + '/trec_eval', '-q', '-m', 'ndcg_cut.5', qrelsFile, runFile], stdout=subprocess.PIPE).stdout.decode('UTF-8')
                ndcg_5_all_measure = float(
                    trec_eval.split("\n")[-2].split("\t")[-1])

                print("\tnDCG@5: {ndcg_5}\n".format(ndcg_5=ndcg_5_all_measure))

                # Update max
                if ndcg_5_all_measure > max_ndcg:
                    max_ndcg = ndcg_5_all_measure
                    max_config = [bodyWeight,
                                  premisesWeight,
                                  conclusionWeight]

                # Save results
                trec_results.append({
                    'body': bodyWeight,
                    'premises': premisesWeight,
                    'conclusion': conclusionWeight,
                    'ndcg_5': ndcg_5_all_measure
                })

                # Increment run
                i += 1

# Get max
print("\nMaximum nDCG@5 is {max_ndcg} with configuration\n\tBody: {bodyW}\tPremises: {premW}\tConclusion: {concW}".format(
    max_ndcg=max_ndcg,
    bodyW=max_config[0],
    premW=max_config[1],
    concW=max_config[2],
))

print("\nSaving results in CSV file...")

# Save results in csv table
with open('trec_eval_results_weights.csv', mode='w', encoding='UTF-8') as trec_eval_res:
    csv_writer = csv.writer(
        trec_eval_res, delimiter=',', quotechar='"', quoting=csv.QUOTE_MINIMAL)

    csv_writer.writerow(
        ['Body', 'Premises', 'Conclusions', 'nDCG@5'])
    for trec_result in trec_results:
        csv_writer.writerow(
            [trec_result['body'], trec_result['premises'], trec_result['conclusion'], trec_result['ndcg_5']])

print("[FINISHED]")
