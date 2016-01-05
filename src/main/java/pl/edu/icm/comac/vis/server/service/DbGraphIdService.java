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
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

/**
 *
 * @author Aleksander Nowinski <a.nowinski@icm.edu.pl>
 */
@Service
public class DbGraphIdService implements GraphIdService {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(DbGraphIdService.class.getName());
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public String getGraphId(List<String> nodes) {
        //create id:
        String[] findId = findId(nodes);
        String id = findId[0];
        if (findId[1] == null) {
            depositGraph(id, nodes);
        }
        return id;
    }

    @Override
    public List<String> getNodes(String graphId) throws UnknownGraphException {
        log.debug("Retrieving graph " + graphId);
        SqlRowSet set = jdbcTemplate.queryForRowSet("select element  from graph g join graph_entry ge on graph_id=id where id = ?", graphId);
        List<String> res = new ArrayList<>();

        while (set.next()) {
            res.add(set.getString(1));
        }
        log.debug("Retrieved for graph " + graphId + " found " + res.size() + " nodes.");
        if (res.size() == 0) {
            throw new UnknownGraphException("Graph with id " + graphId + " not found.");
        }
        return res;
    }

    /**
     * Just find a proper id and info if the graph exists.
     *
     * @return array, first element is valid id, second element not null means
     * that this graph already exists.
     */
    protected String[] findId(List<String> nodes) {
        String[] res = new String[2];
        List<String> arrayList = new ArrayList<>(new HashSet(nodes));
        Collections.sort(arrayList);
        int hash = arrayList.hashCode();
        boolean ok = false;

        do {
            String id = formatHashCodeAsId(hash);
            res[0] = id;
            try {
                List<String> existing = getNodes(id);
                List<String> exS = new ArrayList<>(existing);
                Collections.sort(exS);
                if (exS.equals(arrayList)) {
                    ok = true;
                    res[1] = "ok";

                }
            } catch (UnknownGraphException ue) {
                res[1] = null;
                ok = true;
            }

            hash++;

        } while (!ok);

        return res;
    }

    protected static String formatHashCodeAsId(int hash) {
        return String.format("%08x", hash);
    }

    protected void depositGraph(String id, List<String> nodes) {
        jdbcTemplate.update("insert into graph (id, created) VALUES (?,?)", id, new Date());
        for (String node : nodes) {
            jdbcTemplate.update("insert into GRAPH_ENTRY (graph_id, element) VALUES (?,?)", id, node);
        }
    }

}
