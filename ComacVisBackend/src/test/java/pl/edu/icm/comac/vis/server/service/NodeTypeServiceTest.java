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
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.openrdf.OpenRDFException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import pl.edu.icm.comac.vis.server.RDFConstants;
import pl.edu.icm.comac.vis.server.model.NodeType;

/**
 *
 * @author Aleksander Nowinski <a.nowinski@icm.edu.pl>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public class NodeTypeServiceTest {

    public NodeTypeServiceTest() {
    }

    @Autowired
    NodeTypeService typeService;

    /**
     * Test of classifyIdentifiers method, of class NodeTypeService.
     */
    @Test
    public void testIdentifyTypes() throws Exception {
        System.out.println("identifyTypes");
        Set<String> identifiers = new HashSet<String>();
        identifiers.add(RDFConstants.TOPIC_ID_PREFIX + "test_term");
        NodeType[] types = new NodeType[]{
            NodeType.JOURNAL, NodeType.PAPER, NodeType.PERSON, NodeType.TERM};
        String[][] ids = new String[][]{
            {},
            {},
            {},
            {RDFConstants.TOPIC_ID_PREFIX + "test_term"}
        };
        testTypeDetector(types, ids);ids = new String[][]{
            {"http://comac.ceon.pl/source-issn-2093-7911",},
            {"comac:pubmed%3A26106517","comac:pubmed%3A26109181","comac:pubmed%3A26106472",},
            {"comac:92e869ec-dc97-3f4e-bd51-a9ca547e25f2","comac:415b4797-8ee8-3948-9c2b-1b47c97c0783"},
            {RDFConstants.TOPIC_ID_PREFIX + "test_term"}
        };
        testTypeDetector(types, ids);
        //to check caching
        testTypeDetector(types, ids);
    }

    private void testTypeDetector(NodeType[] types, String[][] ids) throws UnknownNodeException, OpenRDFException {
        HashSet<String> in = new HashSet<>();
        for (String[] id : ids) {
            for (String i : id) {
                in.add(i);
            }
        }
        Map<NodeType, Set<String>> res = typeService.classifyIdentifiers(in);
        for (int i = 0; i < types.length; i++) {
            NodeType t = types[i];
            if (ids[i].length > 0) {
                assertEquals(ids[i].length, res.get(t).size());
                for (String id : ids[i]) {
                    assertTrue(res.get(t).contains(id));
                }
            } else {
                assertFalse(res.containsKey(t));
            }
            //now assert that there are no more keys:
            List<NodeType> tl = Arrays.asList(types);
            for (NodeType type : res.keySet()) {
                assertTrue(tl.contains(type));
            }
        }

    }

    

    /**
     * Test of isTermType method, of class NodeTypeService.
     */
    @Test
    public void testIsTermType() {
        System.out.println("isTermType");
        for (String id : new String[]{
            "term::asdf","term::ąś","term:: + ","term::","term::--"
        }) {
            assertTrue(typeService.isTermType(id));
        }
        for (String id : new String[]{
            "nothing", "aterm::","term++a","--term::","Term::"
        }) {
            assertFalse(typeService.isTermType(id));
        }

    }

    /**
     * Test of locateIdentifierTypes method, of class NodeTypeService.
     */
    @Test
    public void testLocateIdentifierTypes() throws Exception {
        System.out.println("locateIdentifierTypes");
        checkTypeLoad(new Object[][]{
            {"http://comac.ceon.pl/source-issn-2093-7911", NodeType.JOURNAL},
            {"comac:415b4797-8ee8-3948-9c2b-1b47c97c0783", NodeType.PERSON},
            {"comac:pubmed%3A26106517", NodeType.PAPER},
        });
        checkTypeLoad(new Object[][]{
            {"comac:pubmed%3A26106517", NodeType.PAPER},
            {"comac:pubmed%3A26109181", NodeType.PAPER},
            {"comac:pubmed%3A26106472", NodeType.PAPER},
            {"comac:92e869ec-dc97-3f4e-bd51-a9ca547e25f2", NodeType.PERSON},
        });
        try {
            typeService.locateIdentifierTypes(Arrays.asList(new String[] {"nonexistent:none"}));
            fail("Passed nonexistent node.");
        } catch(UnknownNodeException une) {
            //ok
        }
        
    }
    
    private void checkTypeLoad(Object[][] itl) throws OpenRDFException, UnknownNodeException {
        List<String> in = new ArrayList<>();
        for (Object[] it : itl) {
            in.add((String)it[0]);
        }
        Map<String, NodeType> r = typeService.locateIdentifierTypes(in);
        assertEquals(itl.length, r.size());
        for (Object[] it : itl) {
            assertEquals(it[1], r.get(it[0]));
        }
    }

}
