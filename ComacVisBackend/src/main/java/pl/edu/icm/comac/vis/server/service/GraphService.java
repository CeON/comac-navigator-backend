/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.edu.icm.comac.vis.server.service;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.edu.icm.comac.vis.server.model.Graph;
import pl.edu.icm.comac.vis.server.model.Link;
import pl.edu.icm.comac.vis.server.model.Node;
import pl.edu.icm.comac.vis.server.model.NodeType;

/**
 *
 * @author Aleksander Nowinski <a.nowinski@icm.edu.pl>
 */
@Service
public class GraphService {

    @Autowired
    Repository repo;

    @Autowired
    GraphIdService graphIdService;

    @Autowired
    NodeTypeService nodeTypeService;
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(GraphService.class.getName());

    public Graph constructGraphs(String[] ids) throws OpenRDFException {
        try {
            Map<NodeType, Set<String>> types = nodeTypeService.classifyIdentifiers(new HashSet<String>(Arrays.asList(ids)));
            Set<String> ppids = extractIdSetByTypes(types, NodeType.PAPER, NodeType.PERSON);
            Graph ppg = buildPersonPublicationGraph(ppids);
            Set<String> jids = extractIdSetByTypes(types, NodeType.JOURNAL);
            Graph ppgPlusJournTopic = applyJournals(ppg, jids);
            Graph ppjgPlusTopic = applyTopics(ppgPlusJournTopic, extractIdSetByTypes(types, NodeType.TERM));
            Graph finalReduced = reduceGraph(ppjgPlusTopic);
            Graph finalWeighted = weightGraph(finalReduced);
            return finalWeighted;
        } catch (UnknownNodeException ex) {
            log.warn("Unknown node in grapht: ", ex);
            return new Graph();
        }
    }

    /**
     * Prepare base graph of papers and persons.
     * @param ppids list of the identifiers to be included in the graph.
     * @return
     * @throws OpenRDFException 
     */
    protected Graph buildPersonPublicationGraph(Set<String> ppids) 
            throws OpenRDFException, UnknownNodeException {
        String GRAPH_QUERY_INIT = "PREFIX x: <http://example.org/>"
                + " PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
                + " PREFIX dc: <http://purl.org/dc/elements/1.1/>"
                + " CONSTRUCT {"
                + "   ?fav x:name ?favname . ?fav x:fav true ."
                + "   ?in ?inrel ?fav . ?in x:name ?inname ."
                + "   ?fav ?outrel ?out . ?out x:name ?outname ."
                + " }"
                + " WHERE {"
                + "   { ?fav foaf:name ?favname } UNION { ?fav dc:title ?favname }"
                + "   OPTIONAL { ?in ?inrel ?fav .  { ?in foaf:name ?inname } UNION { ?in dc:title ?inname } }"
                + "   OPTIONAL { "
                + "      ?fav ?outrel ?out"
                + "      . { ?out a  <http://data.ceon.pl/ontology/1.0/person> } UNION { ?out a  <http://data.ceon.pl/ontology/1.0/text>}"
                + "      . { ?out foaf:name ?outname } UNION { ?out dc:title ?outname } }"
                + "   VALUES ?fav { %s }"
                + " }";

        RepositoryConnection conn = repo.getConnection();
        ValueFactory vf = conn.getValueFactory();
        URI name = vf.createURI("http://example.org/name");
        URI fav = vf.createURI("http://example.org/fav");

        Graph result;
        //special case - empty graph:
        if (ppids.isEmpty()) {
            result = new Graph();
        } else {
            GraphQueryResult graph = null;
            try {
//                String vars = "";
//                for (int n = 0; n < ppids.size(); n++) {
//                    vars += " ?fav" + n;
//                }
                StringBuilder vals = new StringBuilder();
                for (String ppid : ppids) {
                    vals.append("<").append(ppid).append("> ");
                }
                String sparql = String.format(GRAPH_QUERY_INIT, vals);
                log.debug("Graph query: {}", sparql);

                GraphQuery query = conn.prepareGraphQuery(QueryLanguage.SPARQL, sparql);
//                int n = 0;
//                for (String ppid : ppids) {
//                    query.setBinding("fav" + n, vf.createURI(ppid));
//                    n++;
//                }
                graph = query.evaluate();
                //List<Map<String, Object>> links = new ArrayList<>();
                Set<Link> links = new HashSet<>();
                Map<String, Node> nodeMap = new HashMap<>();
//                Map<URI, Map<String, Object>> nodes = new HashMap<>();

                while (graph.hasNext()) {
                    Statement spo = graph.next();
                    log.debug("Result triple: {}", spo);
                    Resource s = spo.getSubject();
                    URI p = spo.getPredicate();
                    Value o = spo.getObject();
                    String id = s.stringValue();

                    if (p.equals(name)) {
                        nodeMap.computeIfAbsent(id, k -> new Node()).setName(o.stringValue());
                    } else if (p.equals(fav)) {
                        nodeMap.computeIfAbsent(id, k -> new Node()).setFavourite(true);
                    } else {
                        links.add(new Link(p.stringValue(), s.stringValue(), o.stringValue()));
                    }
                }
                List<Node> nodes = nodeMap.entrySet().stream().map(e -> {
                    e.getValue().setId(e.getKey());
                    return e.getValue();
                }).collect(Collectors.toList());
                result = new Graph(nodes, new ArrayList<>(links));

            } finally {
                if (graph != null) {
                    graph.close();
                }
                conn.close();
            }
        }
        updateGraphNodeTypes(result);
        return result;
    }
    
    /**
     * Update node types of the graph using the service
     * @param g 
     */
    protected  void updateGraphNodeTypes(Graph g) throws OpenRDFException, UnknownNodeException {
        Set<String> ids = g.getNodes().stream()
                .filter(n->n.getType()==null)
                .map(n->n.getId())
                .collect(Collectors.toSet());
        Map<String, NodeType> tmap = nodeTypeService.identifyTypes(ids);
        
        g.getNodes().stream().filter(n->n.getType()==null).forEach(n-> n.setType(tmap.get(n.getId())));
        return;
    }

    /** Simple method to get list of ids by the 
     * 
     * @param idtypes
     * @param selectedTypes
     * @return 
     */
    protected static Set<String> extractIdSetByTypes(Map<NodeType, Set<String>> idtypes,
            NodeType... selectedTypes) {
        Set<String> res = idtypes.entrySet().stream()
                .filter(e -> {
                    for (NodeType st : selectedTypes) {
                        if (st.equals(e.getKey())) {
                            return true;
                        }
                    }
                    return false;
                })
                .flatMap(e -> e.getValue().stream())
                .collect(Collectors.toSet());
        return res;
    }

    private Graph applyJournals(Graph in, Set<String> jids) {
        return in;
    }

    private Graph reduceGraph(Graph in) {
        return in;
    }

    private Graph weightGraph(Graph in) {
        return in;
    }

    private Graph applyTopics(Graph in, Set<String> extractIdSetByTypes) {
        return in;
    }

}
