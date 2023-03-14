# Hyper Group / Jean Pierre Polnareff
We participate in the **CLEF Touché Lab 2021**, and in particular we submitted our runs for the Task 1 that regards conversational argument retrieval

The organizers of the Touché Lab asked us to pick for our team a name from a list of fictional or real fencers.

So we decided for **[Jean Pierre Polnareff](https://jojo.fandom.com/wiki/Jean_Pierre_Polnareff)**.


## Members

* Marco Alecci

* Tommaso Baldo

* Gabriele Del Fiume

* Francisca Chidubem Ezeobi

* Luca Martinelli

* Elia Ziroldo

## Structure of the repository
```
┌── runs/
│    └── Best runs for both 2020 and 2021 topics
├── src/main/
│    ├── java/it/unipd/dei/jpp/
│    │    ├── analyze/
│    │    ├── fields/
│    │    ├── filter/
│    │    ├── index/
│    │    ├── parse/
│    │    ├── search/
│    │    ├── utils/
│    │    └── ToucheIR.java
│    └── resources/
│        └── Stoplists, WordNet database file and OpenNLP files
├── pom.xml
├── JPPTouche.sh
├── calcWeight.py
└── Report.pdf
```

## Usage 
1. First you have to download the data collection, and the topics provided by Touché Lab.
    * The corpus is downloaded from here https://zenodo.org/record/3734893
    
    * The topics can be downloaded from the Touché website https://webis.de/events/touche-21/shared-task-1.html. 
    
    You can also download the topics of the 2020 edition that we used for testing our system.
    
2. Then you have to package the code and produce the jar file

3. After that you can use the bash script **JPPTouche.sh** in the following way:

```
./JPPTouche.sh -i $inputDataset -o $outputDir -d $indexDir -r $runName
```

The **$inputDataset** must contain all the files of the corpus, and the file containing the topics (it must be called topics.xml).

The **$outputDir** will contain the run file containing the retrieved documents.

The **$indexDir** will contain the index folder.

The **$runName** is the name of the run that will be produced (without file extension). This is an optional parameter.

**N.B.** If you don't specify anything, the program will search for a input directory called **input** and will create a new folder called **output**.

**N.B.** Note that the jar file of the program must be in the same folder of the bash file.
