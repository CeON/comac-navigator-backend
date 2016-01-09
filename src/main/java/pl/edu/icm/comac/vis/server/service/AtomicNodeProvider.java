/*
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
import java.util.List;
import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
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
import pl.edu.icm.comac.vis.Utilities;
import pl.edu.icm.comac.vis.server.model.NodeType;
import static pl.edu.icm.comac.vis.server.service.AtomicGraphServiceImpl.MAX_CACHED_RELATIONS;

/**
 * Simple class to provide cache node entries for AtomicGraphServiceImpl. It
 * does internal caching, using spring cache infrastructure. It is responsible
 * solely for extracting the data from sparql endpoint
 *
 * @author Aleksander Nowinski <a.nowinski@icm.edu.pl>
 */
@Service
public class AtomicNodeProvider {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(AtomicNodeProvider.class.getName());

    @Autowired
    Repository repository;

    @Cacheable("nodeCache")
    public NodeCacheEntry fetchNodeCacheEntry(String nodeId) throws OpenRDFException {
        String detailsSparqlQuery = "PREFIX  dc:<http://purl.org/dc/elements/1.1/> "
                + "PREFIX  foaf:<http://xmlns.com/foaf/0.1/> "
                + "select  ?type ?title ?name ?family_name ?givenname \n"
                + "WHERE { \n"
                + "?id a ?type . \n"
                + "optional  { ?id dc:title ?title } \n"
                + "optional { ?id foaf:name ?name  }  \n"
                + "optional { ?id foaf:family_name ?family_name  } \n"
                + "optional { ?id foaf:givenname ?givenname }\n"
                + "}  limit 500";

        log.debug("Fetching node {} with SPARQL", nodeId);
        RepositoryConnection conn = repository.getConnection();
        ValueFactory vf = conn.getValueFactory();
        TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, detailsSparqlQuery);
        query.setBinding("id", vf.createURI(nodeId));
        TupleQueryResult result = query.evaluate();
        //now get type and name:
        if (!result.hasNext()) {
            log.warn("No information found for Id={}", nodeId);
            return null;
        }
        BindingSet bset = result.next();
        String t = bset.getValue("type").stringValue();
        NodeType type = NodeType.byUrl(t);
        String name = null;
        if (bset.hasBinding("title")) {
            name = bset.getValue("title").stringValue();
        } else if (bset.hasBinding("name")) {
            name = bset.getValue("name").stringValue();
        } else if (bset.hasBinding("family_name") && bset.hasBinding("givenname")) {
            name = Utilities.buildNameString(bset.getValue("givenname").stringValue(),
                    bset.getValue("family_name").stringValue());
        } else if (bset.hasBinding("family_name")) {
            name = bset.getValue("family_name").stringValue();
        } else {
            name = "???";
        }
        result.close();

        String inRelsQuery = "select ?src_id ?type ?relation "
                + "where { ?src_id ?relation ?id . ?src_id a ?type } "
                + "LIMIT " + (MAX_CACHED_RELATIONS + 1);
        List<RelationCacheEntry> relations = new ArrayList<RelationCacheEntry>();
        boolean hugeRelations = false;
        query = conn.prepareTupleQuery(QueryLanguage.SPARQL, inRelsQuery);
        query.setBinding("id", vf.createURI(nodeId));
        result = query.evaluate();

        int cin = 0;
        while (result.hasNext()) {
            BindingSet next = result.next();
            cin++;
            final Value value = next.getValue("src_id");
            if (!(value instanceof URI)) { //to skip possibly anonymous nodes.
                continue;
            }
            relations.add(new RelationCacheEntry(value.stringValue(),
                    next.getValue("relation").stringValue(),
                    nodeId));
        }
        result.close();
        if (cin > MAX_CACHED_RELATIONS) {
            hugeRelations = true;
        } else {
            String outRelsQuery = "select ?dst_id ?type ?relation "
                    + "where { ?id ?relation ?dst_id . ?dst_id a ?type } "
                    + "LIMIT " + (MAX_CACHED_RELATIONS + 1);
            query = conn.prepareTupleQuery(QueryLanguage.SPARQL, outRelsQuery);
            query.setBinding("id", vf.createURI(nodeId));
            result = query.evaluate();
            int cout = 0;
            while (result.hasNext()) {
                BindingSet next = result.next();
                cout++;
                final Value value = next.getValue("dst_id");
                if (!(value instanceof URI)) {
//                    log.debug("Not an URI value, skipping.");
                    continue;
                }
                relations.add(new RelationCacheEntry(nodeId,
                        next.getValue("relation").stringValue(),
                        value.stringValue()));
            }
            result.close();
            if (cout > MAX_CACHED_RELATIONS) {
                hugeRelations = true;
            }
        }
        conn.close();
        NodeCacheEntry res = null;
        if (hugeRelations) {
            res = new NodeCacheEntry(name, nodeId, type, true);
        } else {
            res = new NodeCacheEntry(name, nodeId, type, relations);
        }
        return res;
    }
}
