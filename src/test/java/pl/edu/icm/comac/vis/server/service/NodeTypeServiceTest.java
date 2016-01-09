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
     * Test of isTermType method, of class NodeTypeService.
     */
    @Test
    public void testIsTermType() {
        System.out.println("isTermType");
        for (String id : new String[]{
            "term::asdf", "term::ąś", "term:: + ", "term::", "term::--"
        }) {
            assertTrue(typeService.isTermType(id));
        }
        for (String id : new String[]{
            "nothing", "aterm::", "term++a", "--term::", "Term::"
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
            {"comac:pubmed%3A26106517", NodeType.PAPER},});
        checkTypeLoad(new Object[][]{
            {"comac:pubmed%3A26106517", NodeType.PAPER},
            {"comac:pubmed%3A26109181", NodeType.PAPER},
            {"comac:pubmed%3A26106472", NodeType.PAPER},
            {"comac:92e869ec-dc97-3f4e-bd51-a9ca547e25f2", NodeType.PERSON},});
        assertNull(typeService.identifyType("nonexistent:none"));

    }

    private void checkTypeLoad(Object[][] itl) throws OpenRDFException, UnknownNodeException {
        for (Object[] pair : itl) {
            NodeType t = typeService.identifyType((String) pair[0]);
            assertEquals(t, pair[1]);
        }
    }

}
