/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.edu.icm.comac.vis.server;

/**
 *
 * @author Aleksander Nowinski <a.nowinski@icm.edu.pl>
 */
public class RDFConstants {
    private static final String CEON_ONTOLOGY = "http://data.ceon.pl/ontology/1.0/";
    public static final String CEON_PAPER_TYPE=CEON_ONTOLOGY+"text";
    public static final String CEON_PERSON_TYPE=CEON_ONTOLOGY+"person";
    public static final String CEON_JOURNAL_TYPE=CEON_ONTOLOGY+"journal";
    
    public static final String TOPIC_TYPE="type::topic";
    public static final String TOPIC_ID_PREFIX = "term::";
    
    
    public static final String[][] PREDEFINED_NAMESPACES= new String[][] {
        {"dc", "http://purl.org/dc/elements/1.1/"}, 
        {"dcterms", "http://purl.org/dc/terms/"}, 
        {"bibo", "http://purl.org/ontology/bibo/"}, 
        {"foaf", "http://xmlns.com/foaf/0.1/"},
        {"ceon", CEON_ONTOLOGY}
    };
}
