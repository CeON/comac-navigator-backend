/*
 * Copyright 2014 Pivotal Software, Inc..
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
package pl.edu.icm.comac.vis.server;

import static java.util.Collections.emptyMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.Query;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.icm.comac.vis.server.model.Link;
import pl.edu.icm.comac.vis.server.service.GraphIdService;
import pl.edu.icm.comac.vis.server.service.UnknownGraphException;

/**
 *
 * @author Aleksander Nowinski <a.nowinski@icm.edu.pl>
 */
@RestController
@EnableAutoConfiguration
public class DataController {

    protected static final String JSON_FAVOURITE = "favourite";
    protected static final String JSON_IMPORTANCE = "importance";
    private static final int MAX_RESPONSE = 500;
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(DataController.class);

    @Autowired
    Repository repo;

    @Autowired
    GraphIdService graphIdService;

    private static final int MAX_SEARCH_RESULTS = 500;

    @RequestMapping("/data/graph")
    Map<String, Object> graph(@RequestParam String query) {
        try {
            final String[] idArray = query.split("\\|");
            String graphId = graphIdService.getGraphId(Arrays.asList(idArray));
            Map<String, Object> res = personPublicationGraph(idArray);
            res.put("graphId", graphId);
            return res;
        } catch (OpenRDFException e) {
            log.error("query failed", e);
            return emptyMap();
        }
    }

    @RequestMapping("/data/graphById")
    Map<String, Object> graphById(@RequestParam String query) throws UnknownGraphException {
        try {
            List<String> nodes = graphIdService.getNodes(query);
            Map<String, Object> graph = personPublicationGraph(nodes.toArray(new String[nodes.size()]));
            return graph;
        } catch (OpenRDFException e) {
            log.error("query failed", e);
            return emptyMap();
        }
    }

    private static final String GRAPH_QUERY_INIT = "PREFIX x: <http://example.org/>"
            + " PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
            + " PREFIX dc: <http://purl.org/dc/elements/1.1/>"
            + " CONSTRUCT { ?fav a ?favtype . ?fav x:name ?favname . ?fav x:fav true ."
            + " ?in ?inrel ?fav . ?in a ?intype . ?in x:name ?inname ."
            + " ?fav ?outrel ?out . ?out a ?outtype . ?out x:name ?outname . }"
            + " WHERE { ?fav a ?favtype . { ?fav foaf:name ?favname } UNION { ?fav dc:title ?favname }"
            + " OPTIONAL { ?in ?inrel ?fav . ?in a ?intype . { ?in foaf:name ?inname } UNION { ?in dc:title ?inname } }"
            + " OPTIONAL { ?fav ?outrel ?out . ?out a ?outtype . { ?out foaf:name ?outname } UNION { ?out dc:title ?outname } }"
            + " VALUES ?fav {";

    private Map<String, Object> personPublicationGraph(String... uris) throws OpenRDFException {
        RepositoryConnection conn = repo.getConnection();
        ValueFactory vf = conn.getValueFactory();
        URI type = vf.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        URI name = vf.createURI("http://example.org/name");
        URI fav = vf.createURI("http://example.org/fav");

        Map<String, Object> result = new HashMap<>();
        //special case - empty graph:

        if (uris.length == 1 && uris[0].isEmpty()) {
            result.put("links", Collections.EMPTY_SET);
            result.put("nodes", Collections.EMPTY_SET);
        } else {
            try {
                String sparql = GRAPH_QUERY_INIT;
                for (int n = 0; n < uris.length; n++) {
                    sparql += " ?fav" + n;
                }
                sparql += " }}";
                log.debug("Graph query: {}", sparql);

                GraphQuery query = conn.prepareGraphQuery(QueryLanguage.SPARQL, sparql);
                for (int n = 0; n < uris.length; n++) {
                    query.setBinding("fav" + n, vf.createURI(uris[n]));
                }
                GraphQueryResult graph = query.evaluate();
                try {
                    //List<Map<String, Object>> links = new ArrayList<>();
                    Set<Link> links = new HashSet<>();
                    Map<URI, Map<String, Object>> nodes = new HashMap<>();

                    while (graph.hasNext()) {
                        Statement spo = graph.next();
                        log.debug("Result triple: {}", spo);
                        Resource s = spo.getSubject();
                        URI p = spo.getPredicate();
                        Value o = spo.getObject();

                        if (p.equals(type)) {
                            nodes.computeIfAbsent((URI) s, k -> new HashMap<>()).put("type", mapTypeValue(o.stringValue()));
                        } else if (p.equals(name)) {
                            nodes.computeIfAbsent((URI) s, k -> new HashMap<>()).put("name", o.stringValue());
                        } else if (p.equals(fav)) {
                            nodes.computeIfAbsent((URI) s, k -> new HashMap<>()).put(JSON_FAVOURITE, true);
                        } else {
                            links.add(new Link(p.stringValue(), s.stringValue(), o.stringValue()));
                        }
                    }
                    result.put("links", links);

                    Map<String, Double> importance = calculateImportance(nodes, links);
                    applyImportance(nodes, importance);

                    result.put("nodes",
                            nodes.entrySet().stream().map(
                                    e -> {
                                        Map<String, Object> node = e.getValue();
                                        node.put("id", e.getKey().stringValue());
                                        return node;
                                    }).toArray());
                } finally {
                    graph.close();
                }
            } finally {
                conn.close();
            }
        }
        return result;
    }

    private void buildJournalGraph(List<String> favWorks, List<String> otherWorks, List<String> favJournals) {
        String f = "";
        return;
    }

    @RequestMapping("/data/sparql_construct")
    Map<String, Object> sparqlConstruct(@RequestParam("query") String query) {
        log.debug("Invoking construct query: {}", query);
//        List<String> variables = new ArrayList<String>();
        List<String[]> resultArray = new ArrayList<>();

        Map<String, Object> res = new HashMap<String, Object>();
        if (query.trim().isEmpty()) {
            res.put("error", "Query is empty");
        }
        try {
            RepositoryConnection con = repo.getConnection();
            try {
                GraphQuery graphQuery = con.prepareGraphQuery(QueryLanguage.SPARQL, query);
                GraphQueryResult qres = graphQuery.evaluate();
                try {
                    while (qres.hasNext() && resultArray.size() < MAX_RESPONSE) {  // iterate over the result
                        Statement st = qres.next();
                        resultArray.add(new String[]{st.getSubject().stringValue(), st.getPredicate().stringValue(), st.getObject().stringValue()});
                    }
                } finally {
                    qres.close();
                }
            } finally {
                con.close();
            }
            res.put("header", new String[]{"Subject", "Predicate", "Object"});
            res.put("values", resultArray);
            log.debug("Finished query got {} results", resultArray.size());
        } catch (OpenRDFException e) {
            res.put("error", e.getMessage());
            log.debug("Exception parsing query: {}", e.getMessage());
        }

        return res;
    }

    @RequestMapping("/data/sparql_select")
    Map sparql(@RequestParam("query") String query) {
        log.debug("Invoking tuple query: {}", query);
        List<String> variables = new ArrayList<String>();
        List<String[]> resultArray = new ArrayList<>();

        Map<String, Object> res = new HashMap<String, Object>();
        if (query.trim().isEmpty()) {
            res.put("error", "Query is empty");
        }
        try {
            RepositoryConnection con = repo.getConnection();
            String queryString = query;//"SELECT (COUNT(*) AS ?no) { ?s ?p ?o  }";
            //now add predefined connections:
            if (!query.trim().toUpperCase().startsWith("PREFIX")) {
                StringBuilder b = new StringBuilder();
                for (String[] ns : RDFConstants.PREDEFINED_NAMESPACES) {
                    b.append("PREFIX " + ns[0] + ": <" + ns[1] + "> ");
                }
                queryString = b.toString()+" "+query;
            }
            try {
                TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
                TupleQueryResult result = tupleQuery.evaluate();
                try {
                    variables.addAll(result.getBindingNames());
                    while (result.hasNext() && resultArray.size() < MAX_RESPONSE) {  // iterate over the result
                        String[] arr = new String[variables.size()];
                        BindingSet bindingSet = result.next();
                        for (int i = 0; i < arr.length; i++) {
                            String var = variables.get(i);

                            String val = null;
                            if (var != null) {
                                final Value v = bindingSet.getValue(var);
                                if (v != null) {
                                    val = v.stringValue();
                                } else {
                                    val = null;
                                }
                            }
                            log.debug("Result var {}={}, table size={}", var, val, resultArray.size());
                            arr[i] = val;
                        }
                        resultArray.add(arr);
                    }
                } finally {
                    result.close();
                }
            } finally {
                con.close();
            }
            res.put("header", variables);
            res.put("values", resultArray);
            log.debug("Finished query got {} results", resultArray.size());
        } catch (Exception e) {
            res.put("error", e.getMessage());
            log.debug("Exception parsing query: {}", e);
        }
        return res;
    }

    /*
     PREFIX foaf: <http://xmlns.com/foaf/0.1/> PREFIX dc: <http://purl.org/dc/elements/1.1/>  select ?fav ?favname ?type where { { ?fav foaf:name ?favname } UNION { ?fav dc:title ?favname } . filter(CONTAINS(lcase(?favname), "nowiń")). ?fav <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?type} order by ?favname
    
     */
    @RequestMapping("/data/search")
    Map search(@RequestParam("query") String query) {
        Map<String, Object> res = new HashMap<String, Object>();
        Map<String, Object> response = new HashMap<String, Object>();
        String baseQuery = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
                + "PREFIX dc: <http://purl.org/dc/elements/1.1/>  "
                + "select ?fav ?favname ?type "
                + "where "
                + "{ "
                + "{ ?fav foaf:name ?favname } UNION { ?fav dc:title ?favname } . "
                + " filter(CONTAINS(lcase(?favname),?query)). "
                + "?fav a ?type"
                + "} "
                //                + "order by ?favname "
                + "LIMIT " + (MAX_SEARCH_RESULTS + 1);

//        String countQuery="PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
//                + "PREFIX dc: <http://purl.org/dc/elements/1.1/>  "
//                + "select (count(*) as ?count) "
//                + "where "
//                + "{ "
//                + "{ ?fav foaf:name ?favname } UNION { ?fav dc:title ?favname } . "
//                + "filter(CONTAINS(lcase(?favname), \"%s\")). } ";
        query = query.toLowerCase();
        log.debug("Starting count query:");
        //now count the query:
//        String q = String.format(countQuery, query);
        try {
            RepositoryConnection con = repo.getConnection();
            ValueFactory vf = con.getValueFactory();
            List<SearchResult> searchResultList = new ArrayList<SearchResult>();
            boolean hasMore = false;
            try {
                TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, baseQuery);
                tupleQuery.setBinding("query", vf.createLiteral(query));
                TupleQueryResult result = tupleQuery.evaluate();
                while (result.hasNext()) {
                    BindingSet set = result.next();
                    String id = set.getValue("fav").stringValue();
                    String type = set.getValue("type").stringValue();
                    type = mapTypeValue(type);
                    String name = set.getValue("favname").stringValue();
                    if (searchResultList.size() < MAX_SEARCH_RESULTS) {
                        searchResultList.add(new SearchResult(id, type, name));
                    } else {
                        hasMore = true;
                    }
                }

//                //we expect only one result:
//                int rexCount = Integer.parseInt(result.next().getValue("count").stringValue());
//                log.debug("Query gives {} results", rexCount);
//                response.put("numFound", rexCount);
//                //now true query:
                response.put("docs", searchResultList);
                response.put("hasMoreResults", hasMore);
            } finally {
                if (con != null) {
                    con.close();
                }
            }
            res.put("response", response);
        } catch (Exception e) {
            res.put("error", e.getMessage());
        }
        return res;
    }

    /**
     * Method which converts fully qualified type value from RDF into proper
     * types expected by the graph.
     *
     * @param fullType
     * @return
     */
    protected String mapTypeValue(String fullType) {
        String res = fullType;
        switch (fullType) {
            case "http://data.ceon.pl/ontology/1.0/text":
                res = "paper";
                break;
            case "http://data.ceon.pl/ontology/1.0/person":
                res = "author";
                break;
        }
        return res;
    }

    private Map<String, Double> calculateImportance(Map<URI, Map<String, Object>> nodes, Set<Link> links) {
        Set<String> others = nodes.entrySet().stream()
                .filter(e -> !e.getValue().containsKey(JSON_FAVOURITE))
                .map(e -> e.getKey().stringValue())
                .collect(Collectors.toSet());
        Set<String> favs = nodes.entrySet().stream()
                .filter(e -> e.getValue().containsKey(JSON_FAVOURITE))
                .map(e -> e.getKey().stringValue())
                .collect(Collectors.toSet());
        Map<String, int[]> linkCount = new HashMap<>(); //0-fav, 1-other
        for (Link link : links) {
            int idx = favs.contains(link.getTargetId()) ? 0 : 1;
            linkCount.computeIfAbsent(link.getSourceId(), x -> new int[2])[idx]++;
            idx = favs.contains(link.getSourceId()) ? 0 : 1;
            linkCount.computeIfAbsent(link.getTargetId(), x -> new int[2])[idx]++;
        }
        //now recalculate importance:
        Map<String, Double> res = new HashMap<>();
        //favs: 
        for (String f : favs) {
            int[] lc = linkCount.computeIfAbsent(f, x -> new int[2]);
            int l = lc[0] + lc[1] - 2;
            if (l < 1) {
                l = 1;
            }
            res.put(f, 1. + 0.7 * (1. - 1. / l));
        }
        for (String other : others) {
            int[] lc = linkCount.computeIfAbsent(other, x -> new int[2]);
            int l = 2 * lc[0] + lc[1] - 1;
            if (l < 1) {
                l = 1;
            }
            res.put(other, 0.7 + 0.7 * (1. - 1. / l));
        }
        return res;
    }

    private void applyImportance(Map<URI, Map<String, Object>> nodes, Map<String, Double> importance) {
        Map<String, Map<String, Object>> snodes = nodes.entrySet().stream().
                collect(Collectors.toMap(
                        e -> e.getKey().stringValue(), e -> e.getValue()
                ));//necessary to change key type.
        for (Map.Entry<String, Double> entry : importance.entrySet()) {
            Map<String, Object> node = snodes.get(entry.getKey());
            node.put(JSON_IMPORTANCE, "" + entry.getValue());
        }
    }

    private static class SearchResult {

        String id;
        String type;
        String name;

        public SearchResult(String id, String type, String name) {
            this.id = id;
            this.type = type;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }
}
