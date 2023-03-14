package it.unipd.dei.jpp.analyze;

import it.unipd.dei.jpp.filter.MultipleCharsFilter;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.en.*;
import org.apache.lucene.analysis.miscellaneous.LengthFilter;
import org.apache.lucene.analysis.standard.ClassicTokenizer;

import static it.unipd.dei.jpp.utils.AnalyzerUtil.*;

/**
 * @author Luca Martinelli (luca.martinelli.1@studenti.unipd.it)
 * @version 1.0
 * @since 1.0
 */
public class ToucheAnalyzerQuery extends Analyzer {

    /**
     * Creates a new instance of the analyzer for process queries.
     */
    public ToucheAnalyzerQuery() {
        super();
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer source;

        // The OpenNLPTokenizer was implemented initially, but we noticed that ClassicTokenizer performs better.
        /*try {
            source = new OpenNLPTokenizer(TokenStream.DEFAULT_TOKEN_ATTRIBUTE_FACTORY,
                    loadSentenceModel("en-sent.bin"), loadOpenNLPTokenizer("en-token.bin"));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }*/

        source = new ClassicTokenizer();

        TokenStream tokens = new LowerCaseFilter(source);

        tokens = new MultipleCharsFilter(tokens);

        tokens = new EnglishPossessiveFilter(tokens);

        tokens = new LengthFilter(tokens, 3, 20);

        tokens = new StopFilter(tokens, loadStopList("ebsco.txt"));

        return new TokenStreamComponents(source, tokens);
    }

}

