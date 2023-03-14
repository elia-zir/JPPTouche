package it.unipd.dei.jpp.search;

import com.vader.sentiment.analyzer.SentimentAnalyzer;
import it.unipd.dei.jpp.parse.ParsedDocument;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;
import java.util.*;

/**
 * Re-score the documents using sentiment analysis
 *
 * @author Luca Martinelli (luca.martinelli.1@studenti.unipd.it)
 * @version 1.00
 * @since 1.00
 */
public class JPPReScorer {

    /**
     * The index reader used to retrieve document information
     */
    private final IndexReader reader;

    /**
     * The documents returned by previous search
     */
    private final TopDocs topDocs;

    /**
     * The number of first N documents to be re-scored
     */
    private final int topN;

    /**
     * @param reader  The reader for the index
     * @param topDocs The documents to be re-scored
     * @param topN    The number of first N documents to be re-scored
     */
    public JPPReScorer(IndexReader reader, TopDocs topDocs, int topN) {
        this.reader = reader;
        this.topDocs = topDocs;
        this.topN = topN;
    }

    /**
     * Returns a list of documents with new scores
     *
     * @return a list of documents with new scores
     * @throws IOException if something goes wrong
     */
    public TopDocs reScore() throws IOException {
        ScoreDoc[] newScoreDocs = topDocs.scoreDocs.clone();

        for (int i = 0; i < newScoreDocs.length && i < topN; i++) {
            ScoreDoc scoreDoc = newScoreDocs[i];
            Document doc = reader.document(scoreDoc.doc);
            float newScore = getNewScore(doc);
            scoreDoc.score = combine(scoreDoc.score, newScore);
        }

        Comparator<ScoreDoc> sortDocComparator = (a, b) -> {
            if (a.score > b.score) {
                return -1;
            } else if (a.score < b.score) {
                return 1;
            } else {
                return a.doc - b.doc;
            }
        };

        Arrays.sort(newScoreDocs, sortDocComparator);

        return new TopDocs(topDocs.totalHits, newScoreDocs);
    }

    /**
     * Calculates the sentiment analysis on the current document.
     *
     * @param doc The document to calculate sentiment analysis
     * @return the sentiment analysis
     * @throws IOException if something goes wrong
     */
    private float getNewScore(Document doc) throws IOException {
        String docPremises = doc.get(ParsedDocument.FIELDS.PREMISES);
        SentimentAnalyzer sentimentAnalyzer = new SentimentAnalyzer(docPremises);
        sentimentAnalyzer.analyze();
        Map<String, Float> scoreMap = sentimentAnalyzer.getPolarity();
        return scoreMap.get("compound");
    }

    /**
     * Combine the old score to the new score, calculated by sentiment analysis.
     *
     * @param firstScore  The old score
     * @param secondScore The score calculated by sentiment analysis
     * @return The new score of the document
     */
    private float combine(float firstScore, float secondScore) {
        float newScore;

        //Emotional is better
        newScore = 0.33f * firstScore + 0.66f * Math.abs(secondScore) * firstScore;

        //Neutral is better
        //newScore = 0.33f * firstScore - 0.66f * Math.abs(secondScore) * firstScore;

        return newScore;
    }

}
