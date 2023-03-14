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
package it.unipd.dei.jpp.utils;

import it.unipd.dei.jpp.parse.ParsedDocument;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerModel;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WordlistLoader;
import org.apache.lucene.analysis.opennlp.tools.NLPSentenceDetectorOp;
import org.apache.lucene.analysis.opennlp.tools.NLPTokenizerOp;
import org.apache.lucene.analysis.tokenattributes.*;
import org.apache.lucene.wordnet.SynonymMap;

import java.io.*;
import java.util.Objects;

/**
 * Utils used in various classes
 *
 * @author Marco Alecci (marco.alecci@studenti.unipd.it)
 * @version 1.00
 * @since 1.00
 */
public class AnalyzerUtil {

    /**
     * The class loader of this class. Needed for reading files from the {@code resource} directory.
     */
    private static final ClassLoader CL = AnalyzerUtil.class.getClassLoader();

    /**
     * Loads the required stop list among those available in the {@code resources} folder.
     *
     * @param stopFile the name of the file containing the stop list.
     * @return the stop list
     * @throws IllegalStateException if there is any issue while loading the stop list.
     */
    public static CharArraySet loadStopList(final String stopFile) {

        if (stopFile == null)
            throw new NullPointerException("Stop list file name cannot be null.");
        if (stopFile.isEmpty())
            throw new IllegalArgumentException("Stop list file name cannot be empty.");

        CharArraySet stopList;

        try {
            Reader in = new BufferedReader(new InputStreamReader(Objects.requireNonNull(CL.getResourceAsStream(stopFile))));
            stopList = WordlistLoader.getWordSet(in);
            in.close();
        } catch (IOException e) {
            throw new IllegalStateException(
                    String.format("Unable to load the stop list %s: %s", stopFile, e.getMessage()), e);
        }

        return stopList;
    }

    /**
     * Loads the required synonym map among those available in the {@code resources} folder.
     *
     * @param fileName the name of the file containing the synonyms.
     * @return the stop list
     * @throws IllegalStateException if there is any issue while loading the synonym map.
     */
    public static SynonymMap loadSynonymMap(String fileName) throws IOException {
        return new SynonymMap(Objects.requireNonNull(CL.getResourceAsStream(fileName)));
    }

    /**
     * Loads the required {@code NLPTokenizerOp} among those available in the {@code resources} folder.
     *
     * @param fileName the name of the file of opennlp tokenizer.
     * @return the stop list
     * @throws IllegalStateException if there is any issue while loading the opennlp tokenizer.
     */
    static NLPTokenizerOp loadOpenNLPTokenizer(String fileName) throws IOException {
        return new NLPTokenizerOp(new TokenizerModel(Objects.requireNonNull(CL.getResourceAsStream(fileName))));
    }

    /**
     * Loads the required sentence model among those available in the {@code resources} folder.
     *
     * @param fileName the name of the file of the sentence model.
     * @return the stop list
     * @throws IllegalStateException if there is any issue while loading the sentence model.
     */
    static NLPSentenceDetectorOp loadSentenceModel(String fileName) throws IOException {
        return new NLPSentenceDetectorOp(new SentenceModel(Objects.requireNonNull(CL.getResourceAsStream(fileName))));
    }

    /**
     * Consumes a {@link TokenStream} for the given text by using the provided {@link Analyzer} and prints diagnostic
     * information about all the generated tokens and their {@link org.apache.lucene.util.Attribute}s.
     *
     * @param a the analyzer to use.
     * @param t the text to process.
     * @throws IOException if something goes wrong while processing the text.
     */
    public static void consumeTokenStream(final Analyzer a, final String t) throws IOException {

        // the start time of the processing
        final long start = System.currentTimeMillis();

        // Create a new TokenStream for a dummy field
        final TokenStream stream = a.tokenStream(ParsedDocument.FIELDS.PREMISES, new StringReader(t));

        // Lucene tokens are decorated with different attributes whose values contain information about the token,
        // e.g. the term represented by the token, the offset of the token, etc.

        try (stream) {
            final CharTermAttribute tokenTerm = stream.addAttribute(CharTermAttribute.class);
            final TypeAttribute tokenType = stream.addAttribute(TypeAttribute.class);
            final KeywordAttribute tokenKeyword = stream.addAttribute(KeywordAttribute.class);
            final PositionIncrementAttribute tokenPositionIncrement = stream.addAttribute(PositionIncrementAttribute.class);
            final PositionLengthAttribute tokenPositionLength = stream.addAttribute(PositionLengthAttribute.class);
            final OffsetAttribute tokenOffset = stream.addAttribute(OffsetAttribute.class);
            final FlagsAttribute tokenFlags = stream.addAttribute(FlagsAttribute.class);
            System.out.printf("####################################################################################%n");
            System.out.printf("Text to be processed%n");
            System.out.printf("+ %s%n%n", t);
            System.out.printf("Tokens%n");
            // Reset the stream before starting
            stream.reset();

            // Print all tokens until the stream is exhausted
            while (stream.incrementToken()) {
                System.out.printf("+ token: %s%n", tokenTerm.toString());
                System.out.printf("  - type: %s%n", tokenType.type());
                System.out.printf("  - keyword: %b%n", tokenKeyword.isKeyword());
                System.out.printf("  - position increment: %d%n", tokenPositionIncrement.getPositionIncrement());
                System.out.printf("  - position length: %d%n", tokenPositionLength.getPositionLength());
                System.out.printf("  - offset: [%d, %d]%n", tokenOffset.startOffset(), tokenOffset.endOffset());
                System.out.printf("  - flags: %d%n", tokenFlags.getFlags());
            }

            // Perform any end-of-stream operations
            stream.end();
        }

        // Close the stream and release all the resources
        System.out.printf("%nElapsed time%n");
        System.out.printf("+ %d milliseconds%n", System.currentTimeMillis() - start);
        System.out.printf("####################################################################################%n");
    }
}
