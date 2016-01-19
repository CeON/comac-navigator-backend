/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.edu.icm.comac.vis.server.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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
import pl.edu.icm.comac.vis.server.model.NodeType;

/**
 * A simple service to provide search functionality.
 *
 * @author Aleksander Nowinski <a.nowinski@icm.edu.pl>
 */
//@Service
public class SearchService {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(SearchService.class.getName());
    @Autowired
    Repository repo;

    @Autowired
    NodeTypeService typeService;

    boolean enableBlazegraphSearch = false;

    private static final String BLAZEGRAPH_SEARCH_QUERY
            = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
            + "PREFIX dc: <http://purl.org/dc/elements/1.1/>  "
            + "PREFIX bds: <http://www.bigdata.com/rdf/search#> "
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
            + "select ?fav ?favname "
            + "where "
            + "{ "
            + "?favname bds:search ?query . ?favname bds:matchAllTerms \"true\" . "
            + "{ ?fav foaf:name ?favname } UNION { ?fav dc:title ?favname } UNION { ?fav rdfs:label ?favname } . "
            + "}  order by ?favname ";

    private static final String GENERIC_RDF_QUERY
            = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
            + "PREFIX dc: <http://purl.org/dc/elements/1.1/>  "
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
            + "select ?fav ?favname "
            + "where "
            + "{ "
            + "{ ?fav foaf:name ?favname } UNION { ?fav dc:title ?favname } UNION { ?fav rdfs:label ?favname }  . "
            + " filter(CONTAINS(lcase(?favname),?query)). "
            + "}  order by ?favname ";

    //                + "order by ?favname "
//                + "LIMIT " + (MAX_SEARCH_RESULTS + 1);
    /**
     * Search for a text query. The query may be interpreted in terms of filter,
     * so it should be escaped to avoid spqrql injection attack.
     *
     * @param searchQuery string to be found within name of the element
     * @param maxResult maximum number of result entries (0 means unlimited)
     * @return list of the elements found.
     */
    public List<SearchResult> search(String searchQuery, int maxResult) throws OpenRDFException {
        RepositoryConnection con = null;
        List<SearchResult> res = new ArrayList<>();
        try {
            con = repo.getConnection();
            ValueFactory vf = con.getValueFactory();
            String sparqlQuery = enableBlazegraphSearch ? BLAZEGRAPH_SEARCH_QUERY : GENERIC_RDF_QUERY;
            if (maxResult > 0) {
                sparqlQuery += " LIMIT " + maxResult;
            }
            TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery);
            tupleQuery.setBinding("query", vf.createLiteral(searchQuery));
            TupleQueryResult result = tupleQuery.evaluate();
            while (result.hasNext()) {
                BindingSet set = result.next();
                String id = set.getValue("fav").stringValue();
//                NodeType type = NodeType.byUrl(set.getValue("type").stringValue());
                String name = set.getValue("favname").stringValue();
                res.add(new SearchResult(id, name));
            }
            result.close();
            log.debug("search success, got {} results. Going to update them with types", res.size());
            updateResultsWithTypes(res);
            log.debug("Types updated successfully");
        } finally {
            if (con != null) {
                con.close();
            }
        }
        return res;
    }

    public Repository getRepo() {
        return repo;
    }

    public void setRepo(Repository repo) {
        this.repo = repo;
    }

    public boolean isEnableBlazegraphSearch() {
        return enableBlazegraphSearch;
    }

    public void setEnableBlazegraphSearch(boolean enableBlazegraphSearch) {
        this.enableBlazegraphSearch = enableBlazegraphSearch;
    }

    protected  void updateResultsWithTypes(List<SearchResult> searchResults) throws OpenRDFException {
        Set<String> ids = searchResults.stream().map(x -> x.getId()).collect(Collectors.toSet());
        try {
            Map<String, NodeType> types = typeService.identifyTypes(ids);
            searchResults.parallelStream().forEachOrdered(x->x.setType(types.get(x.getId())));
        } catch (UnknownNodeException ex) {
            log.error("Unexpected inconsistency in data, no type defined for node",ex);
        }
        return;

    }

}
