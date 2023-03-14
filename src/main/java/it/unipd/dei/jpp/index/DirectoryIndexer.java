/*
 *  Copyright 2017-2021 University of Padua, Italy
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  yo u may not use this file except in compliance with the License.
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

package it.unipd.dei.jpp.index;

import it.unipd.dei.jpp.fields.BodyField;
import it.unipd.dei.jpp.fields.BodyMetaField;
import it.unipd.dei.jpp.fields.MetaField;
import it.unipd.dei.jpp.parse.DocumentParser;
import it.unipd.dei.jpp.parse.ParsedDocument;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

/**
 * Indexes documents processing a whole directory tree.
 *
 * @author Luca Martinelli (luca.martinelli.1@studenti.unipd.it)
 * @version 1.00
 * @since 1.00
 */
public class DirectoryIndexer {

    /**
     * One megabyte
     */
    private static final int MEGABYTE = 1024 * 1024;

    /**
     * Max documents to index
     */
    private static final int MAX_DOCUMENTS = -1;

    /**
     * The index writer.
     */
    private final IndexWriter writer;

    /**
     * The class of the {@code DocumentParser} to be used.
     */
    private final Class<? extends DocumentParser> dpCls;

    /**
     * The directory (and sub-directories) where documents are stored.
     */
    private final Path docsDir;

    /**
     * The extension of the files to be indexed.
     */
    private final String extension;

    /**
     * The charset used for encoding documents.
     */
    private final Charset cs;

    /**
     * The total number of documents expected to be indexed.
     */
    private final long expectedDocs;

    /**
     * The start instant of the indexing.
     */
    private final long start;

    /**
     * The total number of indexed files.
     */
    private long filesCount;

    /**
     * The total number of indexed documents.
     */
    private long docsCount;

    /**
     * The total number of indexed bytes
     */
    private long bytesCount;

    /**
     * Creates a new indexer.
     *
     * @param analyzer        the {@code Analyzer} to be used.
     * @param similarity      the {@code Similarity} to be used.
     * @param ramBufferSizeMB the size in megabytes of the RAM buffer for indexing documents.
     * @param indexPath       the directory where to store the index.
     * @param docsPath        the directory from which documents have to be read.
     * @param extension       the extension of the files to be indexed.
     * @param charsetName     the name of the charset used for encoding documents.
     * @param expectedDocs    the total number of documents expected to be indexed
     * @param dpCls           the class of the {@code DocumentParser} to be used.
     * @throws NullPointerException     if any of the parameters is {@code null}.
     * @throws IllegalArgumentException if any of the parameters assumes invalid values.
     */
    public DirectoryIndexer(final Analyzer analyzer, final Similarity similarity, final int ramBufferSizeMB,
                            final String indexPath, final String docsPath, final String extension,
                            final String charsetName, final long expectedDocs,
                            final Class<? extends DocumentParser> dpCls) {

        if (dpCls == null) {
            throw new NullPointerException("Document parser class cannot be null.");
        }

        this.dpCls = dpCls;

        if (analyzer == null) {
            throw new NullPointerException("Analyzer cannot be null.");
        }

        if (similarity == null) {
            throw new NullPointerException("Similarity cannot be null.");
        }

        if (ramBufferSizeMB <= 0) {
            throw new IllegalArgumentException("RAM buffer size cannot be less than or equal to zero.");
        }

        /*
         * SETUP SIMILARITY AND CONFIGS
         */

        final IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setSimilarity(similarity);
        iwc.setRAMBufferSizeMB(ramBufferSizeMB);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        iwc.setCommitOnClose(true);

        if (indexPath == null) {
            throw new NullPointerException("Index path cannot be null.");
        }

        if (indexPath.isEmpty()) {
            throw new IllegalArgumentException("Index path cannot be empty.");
        }

        final Path indexDir = Paths.get(indexPath);

        // if the directory does not already exist, create it
        if (Files.notExists(indexDir)) {
            try {
                Files.createDirectory(indexDir);
            } catch (Exception e) {
                throw new IllegalArgumentException(
                        String.format("Unable to create directory %s:.", indexDir.toAbsolutePath()));
            }
        }

        if (!Files.isWritable(indexDir)) {
            throw new IllegalArgumentException(
                    String.format("Index directory %s cannot be written.", indexDir.toAbsolutePath()));
        }

        if (!Files.isDirectory(indexDir)) {
            throw new IllegalArgumentException(String.format("%s expected to be a directory where to write the index.",
                    indexDir.toAbsolutePath()));
        }

        if (docsPath == null) {
            throw new NullPointerException("Documents path cannot be null.");
        }

        if (docsPath.isEmpty()) {
            throw new IllegalArgumentException("Documents path cannot be empty.");
        }

        final Path docsDir = Paths.get(docsPath);
        if (!Files.isReadable(docsDir)) {
            throw new IllegalArgumentException(
                    String.format("Documents directory %s cannot be read.", docsDir.toAbsolutePath()));
        }

        if (!Files.isDirectory(docsDir)) {
            throw new IllegalArgumentException(
                    String.format("%s expected to be a directory of documents.", docsDir.toAbsolutePath()));
        }

        this.docsDir = docsDir;

        if (extension == null) {
            throw new NullPointerException("File extension cannot be null.");
        }

        if (extension.isEmpty()) {
            throw new IllegalArgumentException("File extension cannot be empty.");
        }
        this.extension = extension;

        if (charsetName == null) {
            throw new NullPointerException("Charset name cannot be null.");
        }

        if (charsetName.isEmpty()) {
            throw new IllegalArgumentException("Charset name cannot be empty.");
        }

        try {
            cs = Charset.forName(charsetName);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    String.format("Unable to create the charset %s: %s.", charsetName, e.getMessage()), e);
        }

        if (expectedDocs <= 0) {
            throw new IllegalArgumentException(
                    "The expected number of documents to be indexed cannot be less than or equal to zero.");
        }
        this.expectedDocs = expectedDocs;

        this.docsCount = 0;

        this.bytesCount = 0;

        this.filesCount = 0;

        try {
            writer = new IndexWriter(FSDirectory.open(indexDir), iwc);
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("Unable to create the index writer in directory %s: %s.",
                    indexDir.toAbsolutePath(), e.getMessage()), e);
        }

        this.start = System.currentTimeMillis();

    }

    /**
     * Indexes the documents.
     *
     * @throws IOException if something goes wrong while indexing.
     */
    public void index() throws IOException {

        System.out.printf("%n#### Start indexing ####%n");

        Map<String, Integer> parsedIDS = new HashMap<>();

        Files.walkFileTree(docsDir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.getFileName().toString().endsWith(extension)) {
                    DocumentParser dp = DocumentParser.create(dpCls, Files.newBufferedReader(file, cs));

                    bytesCount += Files.size(file);
                    filesCount += 1;

                    Document doc;
                    for (ParsedDocument pd : dp) {
                        String id = pd.getIdentifier();

                        if (!parsedIDS.containsKey(id)) {
                            parsedIDS.put(id, 1);


                            // Remove premises with less than 25 characters (removed)
                            /*
                            String premises = pd.getPremises();
                            String [] words = premises.split("\\s+");
                            if(words.length<25){
                                continue;
                            }
                            */

                            doc = new Document();

                            // add the document identifier
                            doc.add(new MetaField(ParsedDocument.FIELDS.ID, pd.getIdentifier()));

                            // add the document body
                            BodyField bodyField = new BodyField(ParsedDocument.FIELDS.BODY, pd.getFullBody());
                            doc.add(bodyField);

                            // add (and store) document premises
                            BodyMetaField premisesField = new BodyMetaField(ParsedDocument.FIELDS.PREMISES, pd.getPremises());
                            doc.add(premisesField);

                            // add (and store) document conclusion
                            BodyMetaField conclusionsField = new BodyMetaField(ParsedDocument.FIELDS.CONCLUSION, pd.getConclusion());
                            doc.add(conclusionsField);

                            // add document stance
                            doc.add(new MetaField(ParsedDocument.FIELDS.STANCE, pd.getStance()));

                            writer.addDocument(doc);

                            docsCount++;

                            // print progress every 10000 indexed documents
                            if (docsCount % 10000 == 0) {
                                System.out.printf("%d document(s) (%d files, %d MB) indexed in %d seconds.%n",
                                        docsCount, filesCount, bytesCount / MEGABYTE,
                                        (System.currentTimeMillis() - start) / 1000);
                            }

                            if (MAX_DOCUMENTS > 0 && docsCount >= MAX_DOCUMENTS) {
                                break;
                            }
                        } else {
                            int parsedNum = parsedIDS.get(id);
                            parsedIDS.put(id, parsedNum + 1);
                        }

                    }

                }
                return FileVisitResult.CONTINUE;
            }
        });

        writer.commit();

        writer.close();

        if (docsCount != expectedDocs) {
            System.out.printf("Expected to index %d documents; %d indexed instead.%n", expectedDocs, docsCount);
        }

        System.out.printf("%d document(s) (%d files, %d MB) indexed in %d seconds.%n", docsCount, filesCount,
                bytesCount / MEGABYTE, (System.currentTimeMillis() - start) / 1000);

        System.out.println("Duplicate documents found:");
        for (String id : parsedIDS.keySet()) {
            int parsedNum = parsedIDS.get(id);
            if (parsedNum > 1) {
                System.out.printf("Document with ID %s appears %d times%n", id, parsedNum);
            }
        }

        System.out.printf("#### Indexing complete ####%n");
    }

}
