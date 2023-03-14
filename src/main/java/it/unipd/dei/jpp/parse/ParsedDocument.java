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

package it.unipd.dei.jpp.parse;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.lucene.document.Field;

/**
 * Represents a parsed document to be indexed.
 *
 * @author Luca Martinelli (luca.martinelli.1@studenti.unipd.it)
 * @version 1.00
 * @since 1.00
 */
public class ParsedDocument {

    /**
     * The names of the {@link Field}s within the index.
     *
     * @author Luca Martinelli (luca.martinelli.1@studenti.unipd.it)
     * @version 1.00
     * @since 1.00
     */
    public final static class FIELDS {
        public static final String ID = "id";
        public static final String ACQUISITION_TIME = "acquisitionTime";
        public static final String SOURCE_URL = "sourceUrl";
        public static final String PREMISES = "premises";
        public static final String DISCUSSION_TITLE = "discussionTitle";
        public static final String MODE = "mode";
        public static final String SOURCE_DOMAIN = "sourceDomain";
        public static final String SOURCE_TEXT = "sourceText";
        public static final String BODY = "body";
        public static final String CONCLUSION = "conclusion";
        public static final String STANCE = "stance";
        public static final String TOPIC = "topic";
        public static final String AUTHOR = "author";
        public static final String AUTHOR_ORGANIZATION = "authorOrganization";
        public static final String AUTHOR_ROLE = "authorRole";
    }

    /**
     * The id of the document
     */
    private String id;

    /**
     * The time when the document was acquired
     */
    private String acquisitionTime;

    /**
     * The source url of the document
     */
    private String sourceUrl;

    /**
     * The premises of the document
     */
    private String premises;

    /**
     * The title of the document
     */
    private String discussionTitle;

    /**
     * The mode of the document (e.g. discussion)
     */
    private String mode;

    /**
     * The domain of the source
     */
    private String sourceDomain;

    /**
     * The full text of the document
     */
    private String sourceText;

    /**
     * The conclusion of the discussion
     */
    private String conclusion;

    /**
     * The stance of the argument (PRO or CON)
     */
    private String stance;

    /**
     * The topic of the discussion
     */
    private String topic;

    /**
     * The author of the premises
     */
    private String author;

    /**
     * The organization of the author
     */
    private String authorOrganization;

    /**
     * The role of the author in his organization
     */
    private String authorRole;

    /**
     * Creates a new parsed document
     *
     * @param id the unique document identifier.
     * @throws NullPointerException  if {@code id} and/or {@code body} are {@code null}.
     * @throws IllegalStateException if {@code id} and/or {@code body} are empty.
     */
    public ParsedDocument(final String id) {
        this.id = id;
    }

    /**
     * Returns the unique document identifier.
     *
     * @return the unique document identifier.
     */
    public String getIdentifier() {
        return id;
    }

    /**
     * Sets the unique document identifier.
     *
     * @param id the unique document identifier
     */
    public void setIdentifier(String id) {
        this.id = id;
    }

    /**
     * Returns the time when the document was acquired
     *
     * @return the time when the document was acquired
     */
    public String getAcquisitionTime() {
        return acquisitionTime;
    }

    /**
     * Sets the time when the document was acquired
     *
     * @param acquisitionTime the time when the document was acquired
     */
    public void setAcquisitionTime(String acquisitionTime) {
        this.acquisitionTime = acquisitionTime;
    }

    /**
     * Returns the source url of the document
     *
     * @return the source url of the document
     */
    public String getSourceUrl() {
        return sourceUrl;
    }

    /**
     * Sets the source url of the document
     *
     * @param sourceUrl the source url of the document
     */
    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    /**
     * Returns the premises of the document
     *
     * @return the premises of the document
     */
    public String getPremises() {
        return premises;
    }

    /**
     * Sets the premises of the document
     *
     * @param premises the premises of the document
     */
    public void setPremises(String premises) {
        this.premises = premises;
    }

    /**
     * Returns the title of the document
     *
     * @return the title of the document
     */
    public String getDiscussionTitle() {
        return discussionTitle;
    }

    /**
     * Sets the title of the document
     *
     * @param discussionTitle the title of the document
     */
    public void setDiscussionTitle(String discussionTitle) {
        this.discussionTitle = discussionTitle;
    }

    /**
     * Returns the mode of the document (e.g. discussion)
     *
     * @return the mode of the document (e.g. discussion)
     */
    public String getMode() {
        return mode;
    }

    /**
     * Sets the mode of the document (e.g. discussion)
     *
     * @param mode the mode of the document (e.g. discussion)
     */
    public void setMode(String mode) {
        this.mode = mode;
    }

    /**
     * Returns the domain of the source
     *
     * @return the domain of the source
     */
    public String getSourceDomain() {
        return sourceDomain;
    }

    /**
     * Sets the domain of the source
     *
     * @param sourceDomain the domain of the source
     */
    public void setSourceDomain(String sourceDomain) {
        this.sourceDomain = sourceDomain;
    }

    /**
     * Returns the full text of the document
     *
     * @return the full text of the document
     */
    public String getSourceText() {
        return sourceText;
    }

    /**
     * Sets the full text of the document
     *
     * @param sourceText the full text of the document
     */
    public void setSourceText(String sourceText) {
        this.sourceText = sourceText;
    }

    /**
     * Returns the conclusion of the discussion
     *
     * @return the conclusion of the discussion
     */
    public String getConclusion() {
        return conclusion;
    }

    /**
     * Sets the conclusion of the discussion
     *
     * @param conclusion the conclusion of the discussion
     */
    public void setConclusion(String conclusion) {
        this.conclusion = conclusion;
    }

    /**
     * Returns the stance of the argument (PRO or CON)
     *
     * @return the stance of the argument (PRO or CON)
     */
    public String getStance() {
        return stance;
    }

    /**
     * Sets the stance of the argument (PRO or CON)
     *
     * @param stance the stance of the argument (PRO or CON)
     */
    public void setStance(String stance) {
        this.stance = stance;
    }

    /**
     * Returns the topic of the discussion
     *
     * @return the topic of the discussion
     */
    public String getTopic() {
        return topic;
    }

    /**
     * Sets the topic of the discussion
     *
     * @param topic the topic of the discussion
     */
    public void setTopic(String topic) {
        this.topic = topic;
    }

    /**
     * Returns the author of the premises
     *
     * @return the author of the premises
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Sets the author of the premises
     *
     * @param author the author of the premises
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Returns the organization of the author
     *
     * @return the organization of the author
     */
    public String getAuthorOrganization() {
        return authorOrganization;
    }

    /**
     * Sets the organization of the author
     *
     * @param authorOrganization the organization of the author
     */
    public void setAuthorOrganization(String authorOrganization) {
        this.authorOrganization = authorOrganization;
    }

    /**
     * Returns the role of the author in his organization
     *
     * @return the role of the author in his organization
     */
    public String getAuthorRole() {
        return authorRole;
    }

    /**
     * Sets the role of the author in his organization
     *
     * @param authorRole the role of the author in his organization
     */
    public void setAuthorRole(String authorRole) {
        this.authorRole = authorRole;
    }


    @Override
    public final String toString() {
        ToStringBuilder tsb = new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append(FIELDS.ID, id)
                .append(FIELDS.ACQUISITION_TIME, acquisitionTime)
                .append(FIELDS.SOURCE_URL, sourceUrl)
                .append(FIELDS.PREMISES, premises)
                .append(FIELDS.DISCUSSION_TITLE, discussionTitle)
                .append(FIELDS.MODE, mode)
                .append(FIELDS.SOURCE_DOMAIN, sourceDomain)
                .append(FIELDS.SOURCE_TEXT, sourceText)
                .append(FIELDS.CONCLUSION, conclusion)
                .append(FIELDS.STANCE, stance)
                .append(FIELDS.TOPIC, topic)
                .append(FIELDS.AUTHOR, author)
                .append(FIELDS.AUTHOR_ORGANIZATION, authorOrganization)
                .append(FIELDS.AUTHOR_ROLE, authorRole);

        return tsb.toString();
    }

    @Override
    public final boolean equals(Object o) {
        return (this == o) || ((o instanceof ParsedDocument) && id.equals(((ParsedDocument) o).id));
    }

    @Override
    public final int hashCode() {
        return 37 * id.hashCode();
    }

    /**
     * Returns the body of the document
     *
     * @return the body of the document
     */
    public final String getFullBody() {
        return acquisitionTime + " " +
                sourceUrl + " " +
                topic + " " +
                author + " " +
                authorRole + " " +
                authorOrganization + " " +
                sourceDomain + " " +
                discussionTitle + " " +
                premises + " " +
                conclusion;
    }

}
