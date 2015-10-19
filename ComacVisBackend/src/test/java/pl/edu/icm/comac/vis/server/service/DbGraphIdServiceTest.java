/*
 * Copyright 2015 Pivotal Software, Inc..
 *
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
import java.util.HashSet;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.jdbc.JdbcTestUtils;

/**
 *
 * @author Aleksander Nowinski <a.nowinski@icm.edu.pl>
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public class DbGraphIdServiceTest {

    @Autowired 
    JdbcTemplate jdbcTemplate;
    
    @Autowired
    DbGraphIdService service;
    
    public DbGraphIdServiceTest() {
    }
    
    

    /**
     * Test of getGraphId method, of class DbGraphIdService.
     */
    @org.junit.Test
    public void testGetGraphId() {
        System.out.println("getGraphId");
        List<String> nodes = Arrays.asList(new String[] {"a", "b"});
        String id = service.getGraphId(nodes);
        assertNotNull(id);
        assertEquals(String.format("%08x",nodes.hashCode()), id);
//        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "graph"));
        assertEquals(2, JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, "graph_entry", "graph_id='"+id+"'"));
    }
    
    
    /**
     * Test of getNodes method, of class DbGraphIdService.
     */
    @Test
    public void testGetNodes() throws Exception {
        System.out.println("getNodes");
        try {
            service.getNodes("nonexistent");
            fail("Passed nonexistent id.");
        } catch (UnknownGraphException ue) {
            //ok
        }
        List<String> nodes = Arrays.asList(new String[] {"node2", "node1"});
        String id = service.getGraphId(nodes);
        System.out.println("Newly stored array id is: "+id);
        List<String> n = service.getNodes(id);
        
        assertEquals(new HashSet(nodes), new HashSet(n));

    }

    /**
     * Test of findId method, of class DbGraphIdService.
     */
    @Test
    public void testFindId() {
        System.out.println("findId");
        List<String> nodes = Arrays.asList(new String[] {"xnode2", "xnode1"});
        List<String> sorted = new ArrayList<>(nodes);
        Collections.sort(sorted);
        String expectedId = DbGraphIdService.formatHashCodeAsId(sorted.hashCode());
        String[] findId = service.findId(nodes);
        assertEquals(2, findId.length);
        assertEquals(expectedId, findId[0]);
        assertNull(findId[1]);
        service.getGraphId(nodes);
        findId = service.findId(nodes);
        assertEquals(2, findId.length);
        assertEquals(expectedId, findId[0]);
        assertNotNull(findId[1]);
        
    }
    
}
