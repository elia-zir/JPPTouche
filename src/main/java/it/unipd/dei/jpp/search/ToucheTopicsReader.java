package it.unipd.dei.jpp.search;

import org.apache.lucene.benchmark.quality.QualityQuery;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

/**
 * Represents a topic parser
 *
 * @author Luca Martinelli (luca.martinelli.1@studenti.unipd.it)
 * @version 1.00
 * @since 1.00
 */
public class ToucheTopicsReader {

    /**
     * Returns a list of {@code QualityQuery} representing queries
     *
     * @param in the {@code BufferedReader}
     * @return a list of {@code QualityQuery}
     * @throws IOException if something goes wrong
     */
    public QualityQuery[] readQueries(BufferedReader in) throws IOException {
        List<QualityQuery> queriesList = new ArrayList<>();
        XmlMapper mapper = new XmlMapper();

        Topic[] topics = mapper.readValue(in, Topic[].class);

        for (Topic t : topics) {
            String queryID = t.getNumber();
            HashMap<String, String> values = new HashMap<>();
            values.put(Searcher.TOPIC_FIELDS.TITLE, t.getTitle());
            values.put(Searcher.TOPIC_FIELDS.DESCRIPTION, t.getDescription());
            values.put(Searcher.TOPIC_FIELDS.NARRATIVE, t.getNarrative());

            queriesList.add(new QualityQuery(queryID, values));
        }

        QualityQuery[] queries = new QualityQuery[queriesList.size()];
        queriesList.toArray(queries);
        return queries;
    }

    /**
     * An object that represent a topic
     */
    static class Topic {
        /**
         * The topic number
         */
        private String number;

        /**
         * The title of the topic
         */
        private String title;

        /**
         * The description of the topic
         */
        private String description;

        /**
         * The narrative of the topic
         */
        private String narrative;

        /**
         * Returns the topic number
         *
         * @return the topic number
         */
        public String getNumber() {
            return number;
        }

        /**
         * Sets the topic number
         *
         * @param number the topic number
         */
        public void setNumber(String number) {
            this.number = number;
        }

        /**
         * Returns the topic title
         *
         * @return the topic title
         */
        public String getTitle() {
            return title;
        }

        /**
         * Sets the topic title
         *
         * @param title the topic title
         */
        public void setTitle(String title) {
            this.title = title;
        }


        /**
         * Returns the topic description
         *
         * @return the topic description
         */
        public String getDescription() {
            return description;
        }

        /**
         * Sets the topic description
         *
         * @param description the topic description
         */
        public void setDescription(String description) {
            this.description = description;
        }

        /**
         * Returns the topic narrative
         *
         * @return the topic narrative
         */
        public String getNarrative() {
            return narrative;
        }

        /**
         * Sets the topic narrative
         *
         * @param narrative the topic narrative
         */
        public void setNarrative(String narrative) {
            this.narrative = narrative;
        }
    }
}

