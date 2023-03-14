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

package it.unipd.dei.jpp.fields;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;

import java.io.Reader;

/**
 * Represents a {@link Field} for containing the body of a document.
 * <p>
 * It is a tokenized field, stored, keeping document ids, term frequencies, positions and offsets (see {@link
 * IndexOptions#DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS}.
 *
 * @author Luca Martinelli (luca.martinelli.1@studenti.unipd.it)
 * @version 1.00
 * @since 1.00
 */
public class BodyMetaField extends Field {

    /**
     * The type of the document body field
     */
    private static final FieldType META_TYPE = new FieldType();

    static {
        META_TYPE.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
        META_TYPE.setTokenized(true);
        META_TYPE.setStored(true);
    }

    /**
     * Create a new field of type body-meta for the document.
     *
     * @param field the field where to store the value.
     * @param value the contents to be stored in the field of the document.
     */
    public BodyMetaField(final String field, final Reader value) {
        super(field, value, META_TYPE);
    }

    /**
     * Create a new field of type body-meta for the document.
     *
     * @param field the field where to store the value.
     * @param value the contents to be stored in the field of the document.
     */
    public BodyMetaField(final String field, final String value) {
        super(field, value, META_TYPE);
    }

}
