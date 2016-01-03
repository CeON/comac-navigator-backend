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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.sf.ehcache.Element;
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
import pl.edu.icm.comac.vis.server.model.Graph;
import pl.edu.icm.comac.vis.server.model.Link;
import pl.edu.icm.comac.vis.server.model.Node;
import pl.edu.icm.comac.vis.server.model.NodeType;

/**
 * An implementation of the graph service which uses model of atomic nodes to
 * build the graph
 *
 * @author Aleksander Nowinski <a.nowinski@icm.edu.pl>
 */
@Service
public class AtomicGraphServiceImpl implements GraphService {

    public static final int MAX_CACHED_RELATIONS = 250;
    private static final int MAX_RETURNED_RELATIONS = 20;

    @Autowired
    Repository repository;
    @Autowired
    GraphToolkit graphToolkit;
    
    @Autowired
    AtomicNodeProvider nodeProvider; //necessary to fix problems with caching annotations.
//    @Autowired
//    Cache nodeCache;

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(AtomicGraphServiceImpl.class.getName());

    @Override
    public Graph constructGraphs(String[] ids) throws OpenRDFException {
        List<NodeCacheEntry> favCacheNodes = fetchNodes(ids);
        //build link map
        Map<String, Set<String>> links = favCacheNodes.parallelStream().
                filter(x->!x.isOverflow()).
                map(x -> x.getRelations()).
                flatMap(x -> x.stream()).
                flatMap(x -> Arrays.stream(new String[][]{
            {x.getSubject(), x.getObject()}, {x.getObject(), x.getSubject()}
        })).
                collect(Collectors.groupingBy(x -> x[0], Collectors.mapping(x -> x[1], Collectors.toSet())));
        Set<String> large = favCacheNodes.stream().filter(x -> x.isOverflow()).
                map(x -> x.getId()).collect(Collectors.toSet());
        Set<String> normal = favCacheNodes.stream().filter(x -> !x.isOverflow()).
                map(x -> x.getId()).collect(Collectors.toSet());
        Set<String> unfav = graphToolkit.calculateAdditions(normal, large, links, MAX_RETURNED_RELATIONS);
        //now fetch the unfavs:
        List<NodeCacheEntry> unfavCacheNodes = fetchNodes(unfav.toArray(new String[unfav.size()]));
        List<NodeCacheEntry> allNodes = new ArrayList<NodeCacheEntry>();
        allNodes.addAll(favCacheNodes);
        allNodes.addAll(unfavCacheNodes);
        List<NodeCacheEntry> largeNodes = allNodes.stream().filter(x -> x.isOverflow()).collect(Collectors.toList());
        List<RelationCacheEntry> largeRelations = calculateRelations(largeNodes);
        //now build the graph:

        List<Node> nodes = new ArrayList<>();

        List<Node> fnodes = favCacheNodes.stream().map(cached -> {
            Node res = new Node(cached.getId(), cached.getType(), cached.getName(), 1.0);
            res.setFavourite(true);
            return res;
        }).collect(Collectors.toList());
        nodes.addAll(fnodes);
        List<Node> ufnodes = unfavCacheNodes.stream().map(cached -> {
            Node res = new Node(cached.getId(), cached.getType(), cached.getName(), 1.0);
            res.setFavourite(false);
            return res;
        }).collect(Collectors.toList());
        nodes.addAll(ufnodes);
        //
        Set<String> nodeIdSet = nodes.stream().map(x -> x.getId()).collect(Collectors.toSet());

        Set<Link> graphRelations = allNodes.parallelStream().filter(x->!x.isOverflow()).
                flatMap(x -> x.getRelations().stream()).
                filter(x -> nodeIdSet.contains(x.subject) && nodeIdSet.contains(x.object)).
                map(x -> new Link(x.getPredicate(), x.getSubject(), x.getObject())).collect(Collectors.toSet());
        Graph res = new Graph();

        res.setNodes(nodes);
        res.setLinks(new ArrayList<Link>(graphRelations));
        return res;
    }

    private List<NodeCacheEntry> fetchNodes(String[] ids) throws OpenRDFException {
        List<NodeCacheEntry> res = new ArrayList<>();
List<String> missedIds = new ArrayList();
        for (String id : ids) {

//            Element el = nodeCache.get(id);
//            if (el != null) {
//                NodeCacheEntry t = (NodeCacheEntry) el.getObjectValue();
//                res.add(t);
//            } else {
                missedIds.add(id);
//            }
        }
        for (String missedId : missedIds) {
            //FIXME: separate handling for terms
            if (NodeTypeService.isTermType(missedId)) {
                continue;
            }
            NodeCacheEntry e = nodeProvider.fetchNodeCacheEntry(missedId);
            res.add(e);
        }
        return res;
    }

    

    private List<RelationCacheEntry> calculateRelations(List<NodeCacheEntry> overflown) {
        ///FIXME: really calculate relations
        return Collections.emptyList();
    }

}
