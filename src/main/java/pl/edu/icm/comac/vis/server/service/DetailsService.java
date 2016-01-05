/*
 * Copyright 2015 ICM Warsaw University.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pl.edu.icm.comac.vis.server.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.TextUtils;
import org.openrdf.OpenRDFException;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.edu.icm.comac.vis.Utilities;
import pl.edu.icm.comac.vis.server.RDFConstants;
import pl.edu.icm.comac.vis.server.model.ComacPropertyConstants;
import pl.edu.icm.comac.vis.server.model.NodeType;
import pl.edu.icm.comac.vis.server.model.PropertyTranslator;

/**
 *
 * @author Aleksander Nowinski <a.nowinski@icm.edu.pl>
 */
@Service
public class DetailsService {

    @Autowired
    Repository repo;

    @Autowired
    NodeTypeService nodeTypeService;

    static Map<String, PropertyTranslator> translators;
    protected static final String FOAF_NAME_URI = "http://xmlns.com/foaf/0.1/name";
    protected static final String FOAF_GIVENNAME_URI = "http://xmlns.com/foaf/0.1/givenname";
    protected static final String FOAF_FAMILYNAME_URI = "http://xmlns.com/foaf/0.1/family_name";

    static {
        translators = new HashMap<>();
        registerTranslator(new PropertyTranslator("http://purl.org/dc/elements/1.1/date", "Date", true));
        registerTranslator(new PropertyTranslator("http://purl.org/dc/elements/1.1/date", "Date", true));
        registerTranslator(new PropertyTranslator("http://purl.org/dc/elements/1.1/title", "Title", true));
        registerTranslator(new PropertyTranslator("http://purl.org/dc/elements/1.1/subject", "Subject", false));
        registerTranslator(new PropertyTranslator("http://purl.org/ontology/bibo/issn", "ISSN", true));//FIXME: check if we have multiple issn in data
        registerTranslator(new PropertyTranslator("http://purl.org/ontology/bibo/doi", "DOI", true));
        registerTranslator(new PropertyTranslator("http://purl.org/dc/terms/abstract", "Abstract", true));
        registerTranslator(new PropertyTranslator(FOAF_GIVENNAME_URI, "GivenName", false));
        registerTranslator(new PropertyTranslator(FOAF_FAMILYNAME_URI, "FamilyName", false));
        registerTranslator(new PropertyTranslator(FOAF_NAME_URI, "name", true));
        registerTranslator(new PropertyTranslator("http://xmlns.com/foaf/0.1/mbox", "Email", false));
        registerTranslator(new PropertyTranslator("http://purl.org/dc/elements/1.1/source", "Source", true, true));
        registerTranslator(new PropertyTranslator("http://purl.org/dc/terms/references", "References", false, true));
//        registerTranslator(new PropertyTranslator("", "", true, NodeType.));
//        registerTranslator(new PropertyTranslator("", "", true, NodeType.));
//        registerTranslator(new PropertyTranslator("", "", true, NodeType.));
    }

    private static void registerTranslator(PropertyTranslator t) {
        translators.put(t.getURL(), t);
    }

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(DetailsService.class.getName());

    public Map<String, Object> getObjectInfo(String id) throws UnknownNodeException, OpenRDFException {
        Map<String, Object> res = null;
        NodeType type = nodeTypeService.identifyType(id);

        if (type == NodeType.TERM) {
            res = termProperties(id);
        } else {
            res = basicObjectProperties(id, true);
        }
        appendCoreProperties(res, id);
        return res;
    }

    protected void appendCoreProperties(Map<String, Object> pmap, String id) throws OpenRDFException {
        pmap.put(ComacPropertyConstants.IDENTIFIER, id);
        try {
            NodeType type = nodeTypeService.identifyType(id);
            pmap.put(ComacPropertyConstants.TYPE, type);

        } catch (UnknownNodeException une) {
            log.info("Could not identify type of node {}", id);
            log.info("Exception while identifying.", une);
        }

    }

    private Map<String, Object> termProperties(String id) {
        Map<String, Object> res = new HashMap<>();
        res.put(ComacPropertyConstants.TERM_VALUE, NodeTypeService.termIdToTerm(id));
        //FIXME: other term properties, inc. referring item count.
        return res;
    }

    protected Map<String, Object> basicObjectProperties(String id, boolean expandProperties) throws OpenRDFException {
        Map<String, Object> res = new HashMap<String, Object>();
        String sparqlQuery = "select ?property ?value ?value_class where { ?id ?property ?value .  OPTIONAL { ?value a ?value_class }  }";
        log.debug("Preparing query for details...");
        RepositoryConnection conn = repo.getConnection();
        TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery);
        ValueFactory valueFactory = conn.getValueFactory();
        query.setBinding("id", valueFactory.createURI(id));
        TupleQueryResult qres = query.evaluate();
        log.debug("Evaluated sparql query...");
        while (qres.hasNext()) {
            BindingSet bs = qres.next();
            String prop = bs.getValue("property").stringValue();

            if (RDFConstants.TYPE_PROPERTY.equals(prop)) {
                continue;
            }
            String val = bs.getValue("value").stringValue();
            if (hasTranslator(prop)) {
                final PropertyTranslator translator = getTranslator(prop);
                Object propertyValue = val;
                if (bs.hasBinding("value_class")) { //object value
                    if (expandProperties) {
                        if (!translator.isObjectValue()) {
                            log.warn("Unexpected property with obj value for non-object translator, skipping.");
                            continue;
                        }
                        Map<String, Object> opmap = basicObjectProperties(val, false);
                        appendCoreProperties(opmap, val);
                        propertyValue = opmap;
                    }
                }
                prop = translator.getJSONPropertyName();
                if (translator.isSingular()) {
                    res.put(prop, propertyValue);
                } else {
                    ((List) res.computeIfAbsent(prop, k -> new ArrayList())).add(propertyValue);
                }
            } else if ("http://purl.org/ontology/bibo/authorList".equals(prop)) {
                res.put("Authors", authorList(id));
            } else if (!bs.hasBinding("value_class")) {
                res.put(prop, val);
            } else {
                log.debug("skipping prop: {} val: {}", prop, val);
            }

        }
        return res;
    }

    private List<Object> authorList(String id) throws OpenRDFException {
        log.debug("Preparing author list for {}", id);
        String sparqlQuery = "select ?property ?value WHERE "
                + "{ ?id <http://purl.org/ontology/bibo/authorList> ?l ."
                + " ?l ?property ?value }";
        RepositoryConnection conn = repo.getConnection();
        TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery);
        ValueFactory valueFactory = conn.getValueFactory();
        query.setBinding("id", valueFactory.createURI(id));
        TupleQueryResult qres = query.evaluate();
        Map<String, String> orderMap = new HashMap<>();
        while (qres.hasNext()) {
            BindingSet bs = qres.next();
            String prop = bs.getValue("property").stringValue();
            if (!prop.matches("http://www.w3.org/1999/02/22-rdf-syntax-ns#_\\d+")) {
                continue;
            }
            String val = bs.getValue("value").stringValue();
            String p = prop.substring("http://www.w3.org/1999/02/22-rdf-syntax-ns#_".length());
            p = StringUtils.leftPad(p, 5, '0');
            orderMap.put(p, val);
        }
        log.debug("Got {} authors", orderMap.size());
        List<Object> res = new ArrayList<>();
        for (String key : orderMap.keySet().stream().sorted().collect(Collectors.toList())) {
            final Map<String, Object> aprops = basicObjectProperties(orderMap.get(key), false);
            appendCoreProperties(aprops, orderMap.get(key));
            updateNameProperty(aprops);
            res.add(aprops);
        }
        return res;
    }

    public static boolean hasTranslator(String url) {
        return translators.containsKey(url);
    }

    public static PropertyTranslator getTranslator(String url) {
        return translators.get(url);
    }

    /**
     * A hook to prepare name of the person if there is no name defined in RDF.
     *
     * @param properties already prepared for the item
     */
    private void updateNameProperty(Map<String, Object> aprops) {
        PropertyTranslator nt = getTranslator(FOAF_NAME_URI);
        if (!aprops.containsKey(nt.getJSONPropertyName())) {
            List<String> gnval = (List) aprops.get(getTranslator(FOAF_GIVENNAME_URI).getJSONPropertyName());
            String gn = null;
            if (gnval != null && !gnval.isEmpty()) {
                gn = gnval.get(0);
            }
            List<String> fnval = (List) aprops.get(getTranslator(FOAF_FAMILYNAME_URI).getJSONPropertyName());

            String fn = null;
            if (fnval != null && !fnval.isEmpty()) {
                fn = fnval.get(0);
            }
            aprops.put(nt.getJSONPropertyName(), Utilities.buildNameString(gn, fn));
        }
    }
}
