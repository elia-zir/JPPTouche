package it.unipd.dei.jpp.filter;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;

/**
 * Token Filter used to remove multiple repetition of same character
 *
 * @author Luca Martinelli (luca.martinelli.1@studenti.unipd.it)
 * @version 1.00
 * @since 1.00
 */
public class MultipleCharsFilter extends TokenFilter {
    private final CharTermAttribute charTermAttr;

    /**
     * Create new instance of the MultipleCharsFilter
     *
     * @param ts the token stream
     */
    public MultipleCharsFilter(TokenStream ts) {
        super(ts);
        this.charTermAttr = addAttribute(CharTermAttribute.class);
    }

    @Override
    public boolean incrementToken() throws IOException {
        if (!input.incrementToken()) {
            return false;
        }

        int length = charTermAttr.length();
        char[] buffer = charTermAttr.buffer();
        char[] newBuffer = new char[length];

        int j = length;
        if (length > 2) {
            newBuffer[0] = buffer[0];
            newBuffer[1] = buffer[1];
            j = 2;

            for (int i = 2; i < length; i++) {
                if (!(buffer[i - 2] == buffer[i - 1] && buffer[i - 1] == buffer[i])) {
                    newBuffer[j] = buffer[i];
                    j++;
                }
            }
        } else {
            newBuffer = buffer.clone();
        }

        charTermAttr.setEmpty();
        charTermAttr.copyBuffer(newBuffer, 0, j);
        return true;
    }
}