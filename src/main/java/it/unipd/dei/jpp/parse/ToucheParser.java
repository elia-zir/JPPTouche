/*
 *  Copyright 2017-2021 University of Padua, Italy
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

package it.unipd.dei.jpp.parse;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Json Parser using Jackson Library
 *
 * @author Marco Alecci (marco.alecci@studenti.unipd.it)
 * @version 1.00
 * @since 1.00
 */
public class ToucheParser extends DocumentParser {

    /**
     * The currently parsed document
     */
    private ParsedDocument document = null;

    /**
     * The {@code JsonParser} used to parse the documents
     */
    private final JsonParser parser;

    /**
     * Creates a new document parser.
     *
     * @param in the reader to the document(s) to be parsed.
     * @throws NullPointerException     if {@code in} is {@code null}.
     * @throws IllegalArgumentException if any error occurs while creating the parser.
     */
    public ToucheParser(final Reader in) throws IOException {
        super(new BufferedReader(in));
        parser = new JsonFactory().createParser(in);
        while (true) {
            if (parser.nextToken().equals(JsonToken.START_ARRAY))
                break;
        }
    }

    @Override
    public boolean hasNext() {
        try {
            JsonToken token = parser.nextToken();
            if (token.equals(JsonToken.END_ARRAY)) {
                return false;
            }

            if (!token.equals(JsonToken.START_OBJECT)) {
                throw new IllegalStateException();
            }

            final JsonNode root = new ObjectMapper().readTree(parser);
            final JsonNode context = root.get("context");
            final JsonNode premise = root.get("premises").get(0);

            final String id = root.get("id").asText();
            document = new ParsedDocument(id);

            final String acquisitionTime = context.has("acquisitionTime") ? context.get("acquisitionTime").asText() : null;
            document.setAcquisitionTime(acquisitionTime);

            final String sourceUrl = root.has("sourceUrl") ? root.get("sourceUrl").asText() : null;
            document.setSourceUrl(sourceUrl);

            final String stance = premise.has("stance") ? premise.get("stance").asText() : null;
            document.setStance(stance);

            final String mode = context.has("mode") ? context.get("mode").asText() : null;
            document.setMode(mode);

            final String premises = premise.has("text") ? premise.get("text").asText() : null;
            document.setPremises(premises);

            final String sourceText = context.has("sourceText") ? context.get("sourceText").asText() : null;
            document.setSourceText(sourceText);

            final String discussionTitle = context.has("discussionTitle") ? context.get("discussionTitle").asText() : null;
            document.setDiscussionTitle(discussionTitle);

            final String sourceDomain = context.has("sourceDomain") ? context.get("sourceDomain").asText() : null;
            document.setSourceDomain(sourceDomain);

            final String conclusion = root.has("conclusion") ? root.get("conclusion").asText() : null;
            document.setConclusion(conclusion);

            final String topic = context.has("topic") ? context.get("topic").asText() : null;
            document.setTopic(topic);

            final String author = root.has("author") ? root.get("author").asText() : null;
            document.setAuthor(author);

            final String authorOrganization = root.has("authorOrganization") ? root.get("authorOrganization").asText() : null;
            document.setAuthorOrganization(authorOrganization);

            final String authorRole = root.has("authorRole") ? root.get("authorRole").asText() : null;
            document.setAuthorRole(authorRole);

            return true;
        } catch (IOException e) {
            throw new IllegalArgumentException("Read failed", e);
        }
    }

    @Override
    protected final ParsedDocument parse() {
        return document;
    }

}
