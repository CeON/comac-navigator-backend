/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.edu.icm.comac.vis.server.model;

import com.fasterxml.jackson.annotation.JsonValue;
import pl.edu.icm.comac.vis.server.RDFConstants;

/**
 * An enum to identify node types.
 *
 * @author Aleksander Nowinski <a.nowinski@icm.edu.pl>
 */
public enum NodeType {
    PAPER(RDFConstants.CEON_PAPER_TYPE, "paper"),
    PERSON(RDFConstants.CEON_PERSON_TYPE, "author"),
    JOURNAL(RDFConstants.CEON_JOURNAL_TYPE, "journal"),
    TERM(null, "topic");

    NodeType(String url, String jsonType) {
        this.url = url;
        this.jsonName = jsonType;
    }

    final String jsonName;
    final String url;

    @JsonValue
    public String getJsonName() {
        return jsonName;
    }

    public String getUrl() {
        return url;
    }

    public static NodeType byUrl(String url) {
        for (NodeType value : NodeType.values()) {
            if (url.equals(value.getUrl())) {
                return value;
            }
        }
        return null;
    }
}
