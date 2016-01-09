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
    private static final String CEON_ONTOLOGY = "http://data.ceon.pl/ontology/";
    public static final String CEON_PAPER_TYPE=CEON_ONTOLOGY+"text";
    public static final String CEON_PERSON_TYPE=CEON_ONTOLOGY+"person";
    public static final String CEON_JOURNAL_TYPE=CEON_ONTOLOGY+"journal";
    public static final String CEON_BLOG_TYPE=CEON_ONTOLOGY+"blog";
    
    public static final String CEON_BLOG_ENTRY_TYPE=CEON_ONTOLOGY+"blog_entry";
    public static final String CEON_DATASET_TYPE=CEON_ONTOLOGY+"dataset";
    public static final String CEON_ORGANIZATION_TYPE=CEON_ONTOLOGY+"organization";
    public static final String CEON_PROJECT_TYPE=CEON_ONTOLOGY+"project";
    
    private static final String CEON_ONTOLOGY_OLD = "http://data.ceon.pl/ontology/1.0/";
    public static final String CEON_PAPER_TYPE_OLD=CEON_ONTOLOGY_OLD+"text";
    public static final String CEON_PERSON_TYPE_OLD=CEON_ONTOLOGY_OLD+"person";
    public static final String CEON_JOURNAL_TYPE_OLD=CEON_ONTOLOGY_OLD+"journal";
    public static final String CEON_BLOG_TYPE_OLD=CEON_ONTOLOGY_OLD+"blog";
    
    public static final String CEON_BLOG_ENTRY_TYPE_OLD=CEON_ONTOLOGY_OLD+"blog_entry";
    public static final String CEON_DATASET_TYPE_OLD=CEON_ONTOLOGY_OLD+"dataset";
    public static final String CEON_ORGANIZATION_TYPE_OLD=CEON_ONTOLOGY_OLD+"organization";
    public static final String CEON_PROJECT_TYPE_OLD=CEON_ONTOLOGY_OLD+"project";
    
    public static final String TOPIC_TYPE="type::topic";
    public static final String TOPIC_ID_PREFIX = "term::";
    
    public static final String TYPE_PROPERTY="http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
    
//    public static final String RDF_TYPE_DATETIME="http://www.w3.org/2001/XMLSchema#dateTime";
    
    
    public static final String[][] PREDEFINED_NAMESPACES= new String[][] {
        {"dc", "http://purl.org/dc/elements/1.1/"}, 
        {"dcterms", "http://purl.org/dc/terms/"}, 
        {"bibo", "http://purl.org/ontology/bibo/"}, 
        {"foaf", "http://xmlns.com/foaf/0.1/"},
        {"ceon", CEON_ONTOLOGY}
    };
}
