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
    
    /** An easy identification of the shingle node type.
     * 
     * @param id
     * @return
     * @throws UnknownNodeException
     * @throws OpenRDFException 
     */
    @Cacheable("idCache")
    public NodeType identifyType(String id) throws UnknownNodeException, OpenRDFException {
        log.debug("Looking for types for {}.", id);
        
        RepositoryConnection conn = repo.getConnection();
        ValueFactory vf = conn.getValueFactory();

        TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, SINGLE_TYPE_QUERY);
        query.setBinding("id", vf.createURI(id));
        TupleQueryResult result = query.evaluate();
        NodeType res = null;
        while(result.hasNext()) {
            BindingSet s = result.next();
            final String typeString = s.getValue("type").stringValue();
            res = NodeType.byUrl(typeString);
            if(res!=null) {
                break;
            }
        }
        result.close();
        conn.close();
        return res;
    }

    
    
    public static String termToTermId(String term) {
        return RDFConstants.TOPIC_ID_PREFIX+term;
    }
    
    public static String termIdToTerm(String id) {
        if(isTermType(id)) {
            return id.substring(RDFConstants.TOPIC_ID_PREFIX.length());
        } else {
            return id;
        }
    }

    protected static boolean isTermType(String identifier) {
        return identifier.startsWith(RDFConstants.TOPIC_ID_PREFIX);
    }

    
    private static final String SINGLE_TYPE_QUERY = "SELECT ?type WHERE { ?id a ?type }";

}
