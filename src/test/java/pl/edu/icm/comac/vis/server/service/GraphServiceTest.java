/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.edu.icm.comac.vis.server.service;

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import pl.edu.icm.comac.vis.server.model.Graph;

/**
 *
 * @author Aleksander Nowinski <a.nowinski@icm.edu.pl>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public class GraphServiceTest {

    @Autowired
    AtomicGraphServiceImpl graphService;

    public GraphServiceTest() {
    }

    /**
     * Test of constructGraphs method, of class GraphServiceTypeBasedImpl.
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

}
