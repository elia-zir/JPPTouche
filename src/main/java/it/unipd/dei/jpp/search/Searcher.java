/*
 *  Copyright 2021 University of Padua, Italy
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package it.unipd.dei.jpp.search;

import it.unipd.dei.jpp.analyze.*;
import it.unipd.dei.jpp.parse.JPPQueryParser;
import it.unipd.dei.jpp.parse.ParsedDocument;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.benchmark.quality.QualityQuery;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * Searches a document collection.
 *
 * @author Luca Martinelli (luca.martinelli.1@studenti.unipd.it)
 * @version 1.00
 * @since 1.00
 */
public class Searcher {

    /**
     * The fields of the typical TREC topics.
     *
     * @author Luca Martinelli (luca.martinelli.1@studenti.unipd.it)
     * @version 1.00
     * @since 1.00
     */
    public static final class TOPIC_FIELDS {

        /**
         * The title of a topic.
         */
        public static final String TITLE = "title";

        /**
         * The description of a topic.
         */
        public static final String DESCRIPTION = "description";

        /**
         * The narrative of a topic.
         */
        public static final String NARRATIVE = "narrative";
    }

    /**
     * The identifier of the run
     */
    private final String runID;

    /**
     * The run to be written
     */
    private final PrintWriter run;

    /**
     * The index reader
     */
    private final IndexReader reader;

    /**
     * The index searcher.
     */
    private final IndexSearcher searcher;

    /**
     * The topics to be searched
     */
    private final QualityQuery[] topics;

    /**
     * The query parser
     */
    private final JPPQueryParser qp;

    /**
     * The maximum number of documents to retrieve
     */
    private final int maxDocsRetrieved;


    /**
     * Creates a new searcher.
     *
     * @param analyzer         the {@code Analyzer} to be used.
     * @param similarity       the {@code Similarity} to be used.
     * @param indexPath        the directory where containing the index to be searched.
     * @param topicsFile       the file containing the topics to search for.
     * @param expectedTopics   the total number of topics expected to be searched.
     * @param runID            the identifier of the run to be created.
     * @param runPath          the path where to store the run.
     * @param maxDocsRetrieved the maximum number of documents to be retrieved.
     * @param fieldWeights     the weights to assign at the three fields
     * @param runName          If present, the name of the run file, runID otherwise
     * @throws NullPointerException     if any of the parameters is {@code null}.
     * @throws IllegalArgumentException if any of the parameters assumes invalid values.
     */
    public Searcher(final Analyzer analyzer, final Similarity similarity, final String indexPath,
                    final String topicsFile, final int expectedTopics, final String runID, final String runPath,
                    final int maxDocsRetrieved, float[] fieldWeights, String runName) {

        if (analyzer == null) {
            throw new NullPointerException("Analyzer cannot be null.");
        }

        if (similarity == null) {
            throw new NullPointerException("Similarity cannot be null.");
        }

        if (indexPath == null) {
            throw new NullPointerException("Index path cannot be null.");
        }

        if (indexPath.isEmpty()) {
            throw new IllegalArgumentException("Index path cannot be empty.");
        }

        final Path indexDir = Paths.get(indexPath);
        if (!Files.isReadable(indexDir)) {
            throw new IllegalArgumentException(
                    String.format("Index directory %s cannot be read.", indexDir.toAbsolutePath()));
        }

        if (!Files.isDirectory(indexDir)) {
            throw new IllegalArgumentException(String.format("%s expected to be a directory where to search the index.",
                    indexDir.toAbsolutePath()));
        }

        try {
            reader = DirectoryReader.open(FSDirectory.open(indexDir));
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("Unable to create the index reader for directory %s: %s.",
                    indexDir.toAbsolutePath(), e.getMessage()), e);
        }

        // SETUP SIMILARITy

        searcher = new IndexSearcher(reader);
        searcher.setSimilarity(similarity);

        if (topicsFile == null) {
            throw new NullPointerException("Topics file cannot be null.");
        }

        if (topicsFile.isEmpty()) {
            throw new IllegalArgumentException("Topics file cannot be empty.");
        }

        try {
            BufferedReader in = Files.newBufferedReader(Paths.get(topicsFile), StandardCharsets.UTF_8);

            topics = new ToucheTopicsReader().readQueries(in);

            in.close();
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    String.format("Unable to process topic file %s: %s.", topicsFile, e.getMessage()), e);
        }

        if (expectedTopics <= 0) {
            throw new IllegalArgumentException(
                    "The expected number of topics to be searched cannot be less than or equal to zero.");
        }

        if (topics.length != expectedTopics) {
            System.out.printf("Expected to search for %s topics; %s topics found instead.", expectedTopics,
                    topics.length);
        }

        // Define different weights to different fields of the documents
        Map<String, Float> fieldsBoost = new HashMap<>();
        if (fieldWeights == null || fieldWeights.length < 3) {
            fieldWeights = new float[]{0.25f, 1f, 0f};
        }
        fieldsBoost.put(ParsedDocument.FIELDS.BODY, fieldWeights[0]);
        fieldsBoost.put(ParsedDocument.FIELDS.PREMISES, fieldWeights[1]);
        fieldsBoost.put(ParsedDocument.FIELDS.CONCLUSION, fieldWeights[2]);

        // Use a custom QueryParser to apply different weights in the query and use synonym map
        qp = new JPPQueryParser(fieldsBoost, analyzer, ParsedDocument.FIELDS.PREMISES);

        if (runID == null) {
            throw new NullPointerException("Run identifier cannot be null.");
        }

        if (runID.isEmpty()) {
            throw new IllegalArgumentException("Run identifier cannot be empty.");
        }

        this.runID = runID;


        if (runPath == null) {
            throw new NullPointerException("Run path cannot be null.");
        }

        if (runPath.isEmpty()) {
            throw new IllegalArgumentException("Run path cannot be empty.");
        }

        final Path runDir = Paths.get(runPath);
        if (!Files.isWritable(runDir)) {
            throw new IllegalArgumentException(
                    String.format("Run directory %s cannot be written.", runDir.toAbsolutePath()));
        }

        if (!Files.isDirectory(runDir)) {
            throw new IllegalArgumentException(String.format("%s expected to be a directory where to write the run.",
                    runDir.toAbsolutePath()));
        }

        // SETUP FILE WRITERS

        Path runFile = runDir.resolve((runName != null ? runName : runID) + ".txt");
        try {
            run = new PrintWriter(Files.newBufferedWriter(runFile, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE));
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    String.format("Unable to open run file %s: %s.", runFile.toAbsolutePath(), e.getMessage()), e);
        }

        if (maxDocsRetrieved <= 0) {
            throw new IllegalArgumentException(
                    "The maximum number of documents to be retrieved cannot be less than or equal to zero.");
        }

        this.maxDocsRetrieved = maxDocsRetrieved;
    }

    /**
     * /** Searches for the specified topics.
     *
     * @throws IOException if something goes wrong while searching.
     */
    public void search() throws IOException {
        System.out.printf("%n#### Start searching ####%n");

        // the start time of the searching
        final long start = System.currentTimeMillis();

        final Set<String> idField = new HashSet<>();
        idField.add(ParsedDocument.FIELDS.ID);

        BooleanQuery.Builder bq;
        Query q;
        TopDocs docs;
        ScoreDoc[] sd;
        String docID;

        try {
            for (QualityQuery t : topics) {
                System.out.printf("Searching for topic %s.%n", t.getQueryID());

                bq = new BooleanQuery.Builder();

                // Uncomment these lines to use query expansion with synonyms from WordNet

                // Load synonyms from the WordNet file
                // SynonymMap synonyms = loadSynonyms("wn_s.pl");
                // Crate a query passing the weight to assign to synonyms
                // Query currentQuery = qp.parse(t.getValue(TOPIC_FIELDS.TITLE), synonyms, 1f, 0.4f);

                // Query only on premises field without using synonyms
                // Query currentQuery = qp.parse(t.getValue(TOPIC_FIELDS.TITLE));

                // Query with multi fields and weights, without using synonyms
                Query currentQuery = qp.multiParse(t.getValue(TOPIC_FIELDS.TITLE));

                bq.add(currentQuery, BooleanClause.Occur.SHOULD);

                q = bq.build();

                docs = searcher.search(q, maxDocsRetrieved);
                sd = docs.scoreDocs;

                // Uncomment these lines to re-rank the first 30 documents using sentiment analysis
                /*
                docs = new JPPReScorer(reader, docs, 30).reScore();
                sd = docs.scoreDocs;
                */

                for (int i = 0, n = sd.length; i < n; i++) {
                    docID = reader.document(sd[i].doc, idField).get(ParsedDocument.FIELDS.ID);
                    run.printf(Locale.ENGLISH, "%s Q0 %s %d %.6f %s%n", t.getQueryID(), docID, i, sd[i].score, runID);
                }
                run.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            run.close();
            reader.close();
        }

        /*
         * The total elapsed time.
         */
        long elapsedTime = System.currentTimeMillis() - start;
        System.out.printf("%d topic(s) searched in %d seconds.", topics.length, elapsedTime / 1000);
        System.out.printf("%n#### Searching complete ####%n");
    }

    /**
     * Main Only for test Purpose
     */
    public static void main(String[] args) throws Exception {

        final String indexPath = "experiment/index";

        final Analyzer a = new ToucheAnalyzerQuery();

        final Similarity sim = new LMDirichletSimilarity(1800);
        // final Similarity sim = new BM25Similarity();

        final String topics = "documents/topics.xml";
        // final String topics = "documents/topics_2021.xml";

        final String runPath = "experiment";

        String runID = "seupd-jpp-dirichlet";
        // final String runID = "seupd-jpp-bm25";

        final int maxDocsRetrieved = 1000;

        final int expectedTopics = 50;

        // Get weights from CLI parameters
        float[] fieldWeights = null;
        if (args.length >= 3) {
            fieldWeights = new float[3];
            fieldWeights[0] = Float.parseFloat(args[0]);
            fieldWeights[1] = Float.parseFloat(args[1]);
            fieldWeights[2] = Float.parseFloat(args[2]);
        }

        if (args.length >= 4) {
            runID = args[3];
        }

        // searching
        final Searcher s = new Searcher(a, sim, indexPath, topics, expectedTopics, runID, runPath, maxDocsRetrieved, fieldWeights, null);
        s.search();

    }
}
