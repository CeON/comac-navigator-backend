/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.edu.icm.comac.vis.server.model;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import pl.edu.icm.comac.vis.server.RDFConstants;

/**
 * An enum to identify node types.
 *
 * @author Aleksander Nowinski <a.nowinski@icm.edu.pl>
 */
public enum NodeType {
    PAPER(new String[]{RDFConstants.CEON_PAPER_TYPE, RDFConstants.CEON_PAPER_TYPE_OLD}, "paper"),
    PERSON(new String[]{RDFConstants.CEON_PERSON_TYPE, RDFConstants.CEON_PERSON_TYPE_OLD}, "author"),
    JOURNAL(new String[]{RDFConstants.CEON_JOURNAL_TYPE, RDFConstants.CEON_JOURNAL_TYPE_OLD}, "journal"),
    BLOG(new String[]{RDFConstants.CEON_BLOG_TYPE, RDFConstants.CEON_BLOG_TYPE_OLD}, "blog"),
    BLOG_ENTRY(new String[]{RDFConstants.CEON_BLOG_ENTRY_TYPE, RDFConstants.CEON_BLOG_ENTRY_TYPE_OLD}, "blog_entry"),
    DATASET(new String[]{RDFConstants.CEON_DATASET_TYPE, RDFConstants.CEON_DATASET_TYPE_OLD}, "dataset"),
    ORGANIZATION(new String[]{RDFConstants.CEON_ORGANIZATION_TYPE, RDFConstants.CEON_ORGANIZATION_TYPE_OLD}, "organization"),
    PROJECT(new String[]{RDFConstants.CEON_PROJECT_TYPE, RDFConstants.CEON_PROJECT_TYPE_OLD}, "project"),
    TERM(new String[]{}, "topic");

    NodeType(String[] url, String jsonType) {
        this.urls = new HashSet<>(Arrays.asList(url));
        this.jsonName = jsonType;
    }

    final String jsonName;
    final Set<String> urls;

    @JsonValue
    public String getJsonName() {
        return jsonName;
    }

    public static NodeType byUrl(String url) {
        for (NodeType value : NodeType.values()) {
            if (value.urls.contains(url)) {
                return value;
            }
        }
        return null;
    }
}
