/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.edu.icm.comac.vis.server.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author Aleksander Nowinski <a.nowinski@icm.edu.pl>
 */
public class Graph {
    String graphId;
    List<Node> nodes=new ArrayList<>();
    List<Link> links = new ArrayList<>();

    public Graph() {
    }

    public Graph(List<Node> n, List<Link> links) {
        this.nodes = n;
        this.links = links;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public String getGraphId() {
        return graphId;
    }

    public void setGraphId(String graphId) {
        this.graphId = graphId;
    }

    public Map<String, Node> nodeMap() {
        return nodes.stream().collect(Collectors.toMap(n->n.getId(), n->n));
    }
    
    public List<String> nodeIds() {
        return nodes.parallelStream().map(n->n.getId()).collect(Collectors.toList());
    }
    
    public List<String> favNodeIds() {
        return nodes.stream().filter(x->x.isFavourite()).map(x->x.getId()).collect(Collectors.toList());
    }
}
