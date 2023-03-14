import csv
import subprocess
from os import listdir
from os.path import join
import json

# Author: Luca Martinelli (luca.martinelli.1@studenti.unipd.it)
# Version: 1.0.0
#
# Get the scores of the runs


def extract_id(json):
    try:
        return int(json["topic"])
    except KeyError:
        return 0

# Sets experiment folder and position of run file
experimentFolder = '../experiment'
runsFolder = experimentFolder + '/all_runs'
trecEvalFile = experimentFolder + '/trec_eval'
qrelsFile = experimentFolder + '/touche2020-corrected.qrels'

trec_results = []

# Save max nDCG@5 and its run
max_ndcg = 0
max_runID = None

# Get runIDs
runIDs = [f[0:-4] for f in listdir(runsFolder) if (
    join(runsFolder, f).endswith(".txt"))]

# Setup JSON object to store rows
csv_json_data = {"runNames": [], "topics": [], "ndcg_5s": {}}

for runID, i in zip(runIDs, range(len(runIDs))):
    runID_name = runID.replace("-", " ").title()
    csv_json_data["runNames"].append(runID_name)

# Calculates runs score
for runID, runName in zip(runIDs, csv_json_data["runNames"]):
    print("RUN #{runID}".format(runID=runID))

    # Get trec_eval evaluation
    runFile = join(runsFolder, runID + ".txt")
    trec_eval = subprocess.run(
        [trecEvalFile, '-q', '-m', 'ndcg_cut.5', qrelsFile, runFile], stdout=subprocess.PIPE).stdout.decode('UTF-8')

    # Get all ndcg_5 of all topics
    topicsRes = []
    topics = []
    for i in range(len(trec_eval.split("\n")) - 2):
        trec_topic_res = trec_eval.split("\n")[i]
        topicID = int(trec_topic_res.split("\t")[-2])
        topic_ndcg_5 = float(trec_topic_res.split("\t")[-1])

        if topicID in csv_json_data["ndcg_5s"]:
            csv_json_data["ndcg_5s"][topicID].append(topic_ndcg_5)
        else:
            csv_json_data["ndcg_5s"][topicID] = [topic_ndcg_5]

        topics.append(topicID)
        topicsRes.append({'topic': topicID, 'ndcg_5': topic_ndcg_5})

    # Get average measure of ndcg_5
    ndcg_5_all_measure = float(trec_eval.split("\n")[-2].split("\t")[-1])

    if "All" in csv_json_data["ndcg_5s"]:
        csv_json_data["ndcg_5s"]["All"].append(ndcg_5_all_measure)
    else:
        csv_json_data["ndcg_5s"]["All"] = [ndcg_5_all_measure]

    # Print average ndcg_5
    print("\tnDCG@5: {ndcg_5}\n".format(ndcg_5=ndcg_5_all_measure))

    if len(csv_json_data["topics"]) <= 0:
        topics.sort()
        topics.append("All")
        csv_json_data["topics"] = topics

    topicsRes.sort(key=extract_id)

    # Update max
    if ndcg_5_all_measure >= max_ndcg:
        max_ndcg = ndcg_5_all_measure
        max_runID = runFile

    # Save results
    trec_results.append({
        "runID": runID,
        "runName": runName,
        "runFile": runFile,
        "ndcg_5_avg": ndcg_5_all_measure,
        "ndcg_5_topics": topicsRes
    })

# Get max
print("\nMaximum nDCG@5 is {max_ndcg} of #{runID}".format(
    max_ndcg=max_ndcg,
    runID=max_runID
))

print("\nSaving results...")

# Save results in csv table
with open('trec_eval_results_runs.csv', mode='w', encoding='UTF-8') as trec_eval_res:
    csv_writer = csv.writer(
        trec_eval_res, delimiter=',', quotechar='"', quoting=csv.QUOTE_MINIMAL)

    csv_writer.writerow(["Topic"] + csv_json_data["runNames"])

    for topic in csv_json_data["topics"]:
        csv_writer.writerow([topic] + csv_json_data["ndcg_5s"][topic])

# Save results in JSON file
with open("trec_res.json", mode='w', encoding='UTF-8') as fp:
    json.dump(trec_results, fp, indent=2)

print("\n[FINISHED]")
