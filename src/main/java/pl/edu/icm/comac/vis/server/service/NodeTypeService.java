/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.edu.icm.comac.vis.server.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
import org.springframework.cache.annotation.Cacheable;
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
//
//    @Autowired
//    Cache typeCache;

    private static final String SINGLE_TYPE_QUERY = "SELECT ?type WHERE { ?id a ?type }";

    /**
     * An easy identification of the shingle node type.
     *
     * @param id
     * @return
     * @throws UnknownNodeException
     * @throws OpenRDFException
     */
    @Cacheable("typeCache")
    public NodeType identifyType(String id) throws UnknownNodeException, OpenRDFException {
        log.debug("Looking for types for {}.", id);

        RepositoryConnection conn = repo.getConnection();
        ValueFactory vf = conn.getValueFactory();

        TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, SINGLE_TYPE_QUERY);
        query.setBinding("id", vf.createURI(id));
        TupleQueryResult result = query.evaluate();
        NodeType res = null;
        while (result.hasNext()) {
            BindingSet s = result.next();
            final String typeString = s.getValue("type").stringValue();
            res = NodeType.byUrl(typeString);
            if (res != null) {
                break;
            }
        }
        result.close();
        conn.close();
        return res;
    }

    public static String termToTermId(String term) {
        return RDFConstants.TOPIC_ID_PREFIX + term;
    }

    public static String termIdToTerm(String id) {
        if (isTermType(id)) {
            return id.substring(RDFConstants.TOPIC_ID_PREFIX.length());
        } else {
            return id;
        }
    }

    protected static boolean isTermType(String identifier) {
        return identifier.startsWith(RDFConstants.TOPIC_ID_PREFIX);
    }

    private static final String TYPE_QUERY = "select ?fav ?type "
            + "WHERE { ?fav a ?type .  } "
            + "values ?fav {%s}";

    /**
     * Method which locatest types for the ids within the repository using RDF
     * queries. It is used if caching fails.
     *
     * @param ids identifiers to check types.
     * @return map containing type for each identifier.
     * @throws OpenRDFException
     * @throws UnknownNodeException if at least one identifier has not been
     * identified.
     */
    public Map<String, NodeType> identifyTypes(Set<String> ids) throws OpenRDFException, UnknownNodeException {
        Map<String, NodeType> res = new HashMap<>();
//        Set<String> missing = new HashSet<>();
//        for (String id : ids) {
//            Cache.ValueWrapper vw = typeCache.get(id);
//            if (vw == null) {
//                missing.add(id);
//            } else {
//                res.put(id, (NodeType) vw.get());
//            }
//        }

        log.debug("Looking for types for {} identifiers.", ids.size());
        StringBuilder b = new StringBuilder();
//        int i=0;
        for(String id: ids) {
            //fixme: looks that SESAME has some problem with bindings in values section, dirty hack for now...
            //b.append("?fav").append("" + i).append(" ");
            b.append("<").append(id).append("> ");
//            i++;
        }
        String sparql = String.format(TYPE_QUERY, b.toString());
        log.debug("Query is: {}", sparql);

        RepositoryConnection conn = repo.getConnection();
        ValueFactory vf = conn.getValueFactory();

        TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, sparql);
        //proper version which uses values
//        for (int n = 0; n < ids.size(); n++) {
//            query.setBinding("fav" + n, vf.createURI(ids.get(n)));
//        }
        TupleQueryResult result = query.evaluate();
        while (result.hasNext()) {
            BindingSet s = result.next();
            final String favId = s.getValue("fav").stringValue();
            final String typeId = s.getValue("type").stringValue();
            final NodeType type = NodeType.byUrl(typeId);
            if (type != null) {
                res.put(favId, type);
//                typeCache.put(favId, type);
            } else {
                log.warn("Unknown object type: "+typeId);
            }
        }
        result.close();
        conn.close();
        if (ids.size() != res.size()) {
            log.warn("Unexpected inconsistency, requested types for {} ids, but got only {} ids");
            for (String id : ids) {
                if (!res.containsKey(id)) {
                    log.debug("Could not find type for id {}", id);
                    throw new UnknownNodeException("Could not identify node " + id);
                }
            }
        }
        return res;
    }

}
