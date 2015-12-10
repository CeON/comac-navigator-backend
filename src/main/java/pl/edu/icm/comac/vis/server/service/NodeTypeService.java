/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.edu.icm.comac.vis.server.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
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
import pl.edu.icm.comac.vis.server.RDFConstants;
import pl.edu.icm.comac.vis.server.model.NodeType;

/**
 * A class to easily identify node types. Includes node caching.
 *
 * @author Aleksander Nowinski <a.nowinski@icm.edu.pl>
 */
@Service
public class NodeTypeService {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(NodeTypeService.class.getName());
    @Autowired
    Repository repo;

    @Autowired
    Cache idCache;

    /** Identify types of the nodes for given identifiers.
     * 
     * @param identifiers - set of strings to identify types for
     * @return A map, with keys as types from RDFConstants and values are sets of identifiers.
     * @throws UnknownNodeException if there is at leas one 
     * @throws OpenRDFException if underlying sesame repo fails.
     */
    public Map<NodeType, Set<String>> classifyIdentifiers(Set<String> identifiers) throws UnknownNodeException, OpenRDFException, OpenRDFException, OpenRDFException, UnknownNodeException {
        Map<NodeType, Set<String>> res = new HashMap<>();
        Map<String, NodeType> tmap = identifyTypes(identifiers);
        for (Map.Entry<String, NodeType> e : tmap.entrySet()) {
            res.putIfAbsent(e.getValue(), new HashSet<>());
            res.get(e.getValue()).add(e.getKey());
        }
        return res;
    }
    
    /** An easy identification of the shingle node type.
     * 
     * @param id
     * @return
     * @throws UnknownNodeException
     * @throws OpenRDFException 
     */
    public NodeType identifyType(String id) throws UnknownNodeException, OpenRDFException {
        Set<String> s = new HashSet<>();
        s.add(id);
        Map<NodeType, Set<String>> res = classifyIdentifiers(s);
        //there is single key and single map, with single elemen
        return res.keySet().stream().findAny().get();
    }
    
    public Map<String, NodeType> identifyTypes(Set<String> identifiers) throws OpenRDFException, UnknownNodeException {
        List<String> missing = new ArrayList<>();
        Map<String, NodeType> res = new HashMap<String, NodeType>();
        for (String identifier : identifiers) {
            if (isTermType(identifier)) {
                res.put(identifier, NodeType.TERM);
            } else {
                Element el = idCache.get(identifier);
                if (el != null) {
                    NodeType t = (NodeType) el.getObjectValue();
                    res.put(identifier, t);
                } else {
                    missing.add(identifier);
                }
            }
        }
        if (!missing.isEmpty()) {
            Map<String, NodeType> types = locateIdentifierTypes(missing);
            
            for (Map.Entry<String, NodeType> e : types.entrySet()) {
                Element el = new Element(e.getKey(), e.getValue());
                idCache.put(el);
                res.put(e.getKey(), e.getValue());
            }
        }
        return res;
    }


    protected boolean isTermType(String identifier) {
        return identifier.startsWith(RDFConstants.TOPIC_ID_PREFIX);
    }

    private static final String TYPE_QUERY = "select ?fav ?type "
            + "WHERE { ?fav a ?type .  } "
            + "values ?fav {%s}";

    /** Method which locatest types for the ids within the repository using RDF 
     * queries. It is used if caching fails.
     * @param ids identifiers to check types.
     * @return map containing type for each identifier.
     * @throws OpenRDFException
     * @throws UnknownNodeException if at least one identifier has not been 
     * identified.
     */
    protected Map<String, NodeType> locateIdentifierTypes(List<String> ids) throws OpenRDFException, UnknownNodeException {
        log.debug("Looking for types for {} identifiers.", ids.size());
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            //fixme: looks that SESAME has some problem with bindings in values section, dirty hack for now...
            //b.append("?fav").append("" + i).append(" ");
            b.append("<").append(ids.get(i)).append("> ");
        }
        String sparql = String.format(TYPE_QUERY, b.toString());
        log.debug("Query is: {}", sparql);

        RepositoryConnection conn = repo.getConnection();
        ValueFactory vf = conn.getValueFactory();

        TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, sparql);
//        for (int n = 0; n < ids.size(); n++) {
//            query.setBinding("fav" + n, vf.createURI(ids.get(n)));
//        }
        TupleQueryResult result = query.evaluate();
        Map<String, NodeType> res = new HashMap<>();
        while(result.hasNext()) {
            BindingSet s = result.next();
            res.put(s.getValue("fav").stringValue(), NodeType.byUrl(s.getValue("type").stringValue()));
        }
        result.close();
        conn.close();
        if(ids.size()!=res.size()) {
            log.warn("Unexpected inconsistency, requested types for {} ids, but got only {} ids");
            for (String id : ids) {
                if(!res.containsKey(id)){
                    log.debug("Could not find type for id {}", id);
                    throw new UnknownNodeException("Could not identify node "+id);
                }
            }
        }
        return res;
    }
}
