/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.edu.icm.comac.vis.server.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import pl.edu.icm.comac.vis.server.model.Graph;
import pl.edu.icm.comac.vis.server.model.Node;
import pl.edu.icm.comac.vis.server.model.NodeType;

/**
 *
 * @author Aleksander Nowinski <a.nowinski@icm.edu.pl>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public class GraphServiceTest {

    @Autowired
    GraphService graphService;

    public GraphServiceTest() {
    }

    /**
     * Test of constructGraphs method, of class GraphService.
     */
    @Test
    public void testConstructGraphs() throws Exception {
        System.out.println("constructGraphs");
        String[] ids = new String[]{};
        Graph empty = graphService.constructGraphs(ids);
        assertTrue(empty.getNodes().isEmpty());
        assertTrue(empty.getLinks().isEmpty());

        Graph first = graphService.constructGraphs(new String[]{
            "comac:415b4797-8ee8-3948-9c2b-1b47c97c0783"
        });
        System.out.println("Res is: " + first);
    }

    /**
     * Test of buildPersonPublicationGraph method, of class GraphService.
     */
    @Test
    public void testBuildPersonPublicationGraph() throws Exception {
        System.out.println("buildPersonPublicationGraph");
        final String pers_id_sample = "comac:415b4797-8ee8-3948-9c2b-1b47c97c0783";
        final String pub1_id = "comac:pubmed%3A26106517";
        String[] g1ids = new String[]{
            pers_id_sample};
        Set<String> ppids = new HashSet<>(Arrays.asList(g1ids));
        Graph g1 = graphService.buildPersonPublicationGraph(ppids);
        //we expect one more node: comac:pubmed%3A26106517
        assertEquals(2, g1.getNodes().size());
        Map<String, Node> nodeMap = g1.nodeMap();
        assertTrue(nodeMap.containsKey(pers_id_sample));
        final Node persNode = nodeMap.get(pers_id_sample);
        assertEquals("Sang D. Choi", persNode.getName());
        assertEquals(NodeType.PERSON, persNode.getType());
        assertTrue(nodeMap.containsKey(pub1_id));
        Node pnode = nodeMap.get(pub1_id);
        assertEquals("Aging Workers and Trade-Related Injuries in the US Construction Industry", pnode.getName());
        assertEquals(NodeType.PAPER, pnode.getType());
        assertEquals(1, g1.getLinks().size());
        //now more complex graph:
        String pap2 = "comac:pubmed%3A26109181";
        String pers2 = "comac:597eb8e6-2e8e-308b-9ad6-a6b9b7e30b09";
        String[] g2fava = new String[]{pap2, pers2};
        Set<String> g2favs = new HashSet<>(Arrays.asList(g2fava));
        Graph g2 = graphService.buildPersonPublicationGraph(g2favs);
        List<Node> expNodes2 = new ArrayList<>();
        Node n = new Node(pap2, NodeType.PAPER,
                "Tc-MYBPA an Arabidopsis TT2-like transcription factor and functions "
                + "in the regulation of proanthocyanidin synthesis in Theobroma cacao",
                1.0);
        n.setFavourite(true);
        expNodes2.add(n);
        n = new Node(pers2, NodeType.PERSON, "Yi Liu",
                1.0);
        n.setFavourite(true);
        expNodes2.add(n);
        for (String[] oa : new String[][]{
            {"comac:4a4cb2fa-e769-3f26-844a-6736c5240d49", "Zi Shi"},
            {"comac:b52564ed-fb24-3ed5-a3e4-0fa8832a5a7f", "Siela N. Maximova"},
            {"comac:dbfbc6a6-aa84-32f4-b7e6-9861db829f0b", "Mark J. Payne"},
            {"comac:ed3cbf99-2f33-342e-b7f3-27d04f8aa856", "Mark J. Guiltinan"},
        }) {
            n = new Node(oa[0], NodeType.PERSON, oa[1], 1.0);
            n.setFavourite(false);
            expNodes2.add(n);
        }
        n = new Node("comac:pubmed%3A26106517", NodeType.PAPER, "Aging Workers and Trade-Related Injuries in the US Construction Industry", 1.0);
        n.setFavourite(false);
        expNodes2.add(n);

        assertEquals(expNodes2.size(), g2.getNodes().size());
        for (Node nd : expNodes2) {
            assertTrue(g2.nodeIds().contains(nd.getId()));
            Node resNode = g2.nodeMap().get(nd.getId());
            assertEquals(nd.getType(), resNode.getType());
            assertEquals(nd.getName(), resNode.getName());
        }

    }

    /**
     * Test of updateGraphNodeTypes method, of class GraphService.
     */
    @Test
    public void testUpdateGraphNodeTypes() throws Exception {
        System.out.println("updateGraphNodeTypes");
        Graph g = new Graph();
        g.setNodes(new ArrayList<>());
        Node n = new Node();
        n.setId("comac:415b4797-8ee8-3948-9c2b-1b47c97c0783");
        g.getNodes().add(n);
        graphService.updateGraphNodeTypes(g);
        assertEquals(NodeType.PERSON, n.getType());
    }

    /**
     * Test of extractIdSetByTypes method, of class GraphService.
     */
    @Test
    @Ignore
    public void testExtractIdSetByTypes() {
        System.out.println("extractIdSetByTypes");
        Map<NodeType, Set<String>> idtypes = null;
        NodeType[] selectedTypes = null;
        Set<String> expResult = null;
        Set<String> result = GraphService.extractIdSetByTypes(idtypes, selectedTypes);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}
