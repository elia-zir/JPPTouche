package it.unipd.dei.jpp.parse;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.apache.lucene.search.Query;
import org.apache.lucene.wordnet.SynonymMap;

import java.util.*;

import static it.unipd.dei.jpp.utils.AnalyzerUtil.loadStopList;

public class JPPQueryParser {

    /**
     * The query parsed used to create the query
     */
    private final QueryParser qp;

    /**
     * A map with fields used to generate the query and their weights
     */
    private final Map<String, Float> fieldAndBoosts;

    /**
     * The default field used for parsing with single field
     */
    private final String defaultField;

    /**
     * The analyzer used
     */
    private final Analyzer analyzer;

    /**
     * A stop list used to set a lower weight to some terms.
     */
    private final CharArraySet stopList;

    /**
     * Setup the custom query parser.
     *
     * @param fieldAndBoosts A map of field with weights
     * @param analyzer       The analyzer used to process the query
     * @param defaultField   The default field used for single field parser
     */
    public JPPQueryParser(Map<String, Float> fieldAndBoosts, Analyzer analyzer, String defaultField) {
        if (fieldAndBoosts == null) {
            throw new NullPointerException("Fields hashmap cannot be null.");
        }
        if (fieldAndBoosts.size() <= 0) {
            throw new NullPointerException("Fields hashmap cannot be empty.");
        }
        if (analyzer == null) {
            throw new NullPointerException("Analyzer cannot be null.");
        }
        if (defaultField == null) {
            throw new NullPointerException("Default field cannot be null.");
        }
        if (defaultField.isEmpty()) {
            throw new NullPointerException("Default field cannot be empty.");
        }
        if (!fieldAndBoosts.containsKey(defaultField)) {
            throw new NullPointerException("Default field must be in field and boosts hashmap.");
        }

        // load ebsco stop list
        this.stopList = loadStopList("ebsco.txt");

        this.fieldAndBoosts = fieldAndBoosts;
        this.analyzer = analyzer;
        this.defaultField = defaultField;

        qp = new QueryParser(defaultField, analyzer);
    }

    /**
     * Parse a single field using {@code QueryParserBase}.
     *
     * @param query The query to be parsed
     * @return a {@code Query}
     * @throws Exception if something goes wrong
     */
    public Query parse(String query) throws Exception {
        String queryEscaped = QueryParserBase.escape(query);
        return qp.parse(queryEscaped);
    }

    /**
     * Parse multiple fields with weights using {@code MultiFieldQueryParser}.
     *
     * @param query The query to be parsed
     * @return a {@code Query}
     * @throws Exception if something goes wrong
     */
    public Query multiParse(String query) throws Exception {
        String queryEscaped = QueryParserBase.escape(query);

        String[] fields = new String[fieldAndBoosts.size()];
        fieldAndBoosts.keySet().toArray(fields);

        MultiFieldQueryParser mqp = new MultiFieldQueryParser(fields, analyzer, fieldAndBoosts);
        return mqp.parse(queryEscaped);
    }

    /**
     * Parse query with synonyms with default weight of 1 to synonyms.
     *
     * @param query    The query to be parsed
     * @param synonyms A {@code SynonymMap}
     * @return a {@code Query}
     * @throws Exception if something goes wrong
     */
    public Query parse(String query, SynonymMap synonyms) throws Exception {
        return parse(query, synonyms, 1f);
    }

    /**
     * Parse query with synonyms with custom weight.
     *
     * @param query        The query to be parsed
     * @param synonyms     A {@code SynonymMap}
     * @param synonymBoost The weight to assign to synonyms
     * @return a {@code Query}
     * @throws Exception if something goes wrong
     */
    public Query parse(String query, SynonymMap synonyms, float synonymBoost) throws Exception {
        StringBuilder sb = new StringBuilder();

        String queryParsed = qp.parse(query).toString(defaultField).replaceAll("[^\\w\\s]", "");
        String[] terms = queryParsed.split(" ");

        for (String term : terms) {
            String[] synonymsTerms = null;
            if (synonyms != null) {
                synonymsTerms = synonyms.getSynonyms(term);
            }

            sb.append("(");
            for (String field : fieldAndBoosts.keySet()) {
                double boost = fieldAndBoosts.get(field);

                sb.append(field).append(":(").append(term);
                float termWeight = termWeight(term);
                if (termWeight != 1f) {
                    sb.append("^").append(termWeight);
                }

                if (synonymsTerms != null && synonymsTerms.length != 0) {
                    for (String synonymsTerm : synonymsTerms) {
                        sb.append(" OR ").append(synonymsTerm).append("^").append(synonymBoost);
                    }
                }

                sb.append(")^").append(boost).append(" ");
            }
            sb.append(") ");
        }

        sb.deleteCharAt(sb.length() - 1);
        return qp.parse(sb.toString());
    }

    /**
     * Get term weight. Used with stop list, to assign a lower weight to more frequent terms.
     *
     * @param term The term to calculate weight on
     * @return The weight to assign to the term
     */
    private float termWeight(String term) {
        // Actually not used because removing stop words produces better results.
        /*float weight = 1f;
        if (stopList.contains(term)) {
            weight = 0.5f;
        }
        return weight;
        */
        return 1f;
    }

}
