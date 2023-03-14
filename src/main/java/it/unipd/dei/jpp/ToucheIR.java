/*
 * Copyright 2021 University of Padua, Italy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.unipd.dei.jpp;

import it.unipd.dei.jpp.analyze.*;
import it.unipd.dei.jpp.index.DirectoryIndexer;
import it.unipd.dei.jpp.parse.ToucheParser;
import it.unipd.dei.jpp.search.Searcher;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.Similarity;

/**
 * @author Luca Martinelli (luca.martinelli.1@studenti.unipd.it)
 * @version 1.0
 * @since 1.0
 */
public class ToucheIR {

    /**
     * Main method of the class.
     *
     * @param args command line arguments. If provided, {@code args[0]} contains the path the the index directory;
     *             {@code args[1]} contains the path to the run file.
     * @throws Exception if something goes wrong while indexing and searching.
     */
    public static void main(String[] args) throws Exception {

        String inputDir = "documents/";
        String outputDir = "experiment/";
        String outputIndexDir = "experiment/";
        String runName = null;

        if (args.length > 0) {
            inputDir = args[0].endsWith("/") ? args[0] : args[0] + "/";
        }
        if (args.length > 1) {
            outputDir = args[1].endsWith("/") ? args[1] : args[1] + "/";
        }
        if (args.length > 2) {
            outputIndexDir = args[2].endsWith("/") ? args[2] : args[2] + "/";
        }
        if (args.length > 3) {
            runName = args[3];
        }

        final int ramBuffer = 512;
        final String docsPath = inputDir;
        final String indexPath = outputIndexDir + "index";

        final String extension = "json";
        final int expectedDocs = 387740;
        final String charsetName = "UTF-8";

        final Analyzer a = new ToucheAnalyzerIndex();

        final Similarity sim = new LMDirichletSimilarity(1800);
        //final Similarity sim = new BM25Similarity();

        final String topics = inputDir + "topics.xml";
        // final String topics = inputDir + "topics_2021.xml";

        final String runPath = outputDir;

        final String runID = "seupd-jpp-dirichlet";
        // final String runID = "seupd-jpp-bm25";

        final int maxDocsRetrieved = 1000;

        final int expectedTopics = 50;

        // indexing
        final DirectoryIndexer i = new DirectoryIndexer(a, sim, ramBuffer, indexPath, docsPath, extension, charsetName,
                expectedDocs, ToucheParser.class);
        i.index();

        Analyzer queryAnalyzer = new ToucheAnalyzerQuery();
        // searching
        final Searcher s = new Searcher(queryAnalyzer, sim, indexPath, topics, expectedTopics, runID, runPath, maxDocsRetrieved, null, runName);
        s.search();

    }

}
