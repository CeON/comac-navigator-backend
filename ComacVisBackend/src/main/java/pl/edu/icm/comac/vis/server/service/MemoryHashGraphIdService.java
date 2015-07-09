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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 *
 * @author Aleksander Nowinski <a.nowinski@icm.edu.pl>
 */
@Service
public class MemoryHashGraphIdService implements GraphIdService {

    Map<String, List<String>> hashStorage= new HashMap<>();

    @Override
    public String getGraphId(List<String> nodes) {
        List<String> workingTable = new ArrayList<String>(nodes);
        Collections.sort(workingTable);

        String res = String.format("%08x", workingTable.hashCode());
        hashStorage.putIfAbsent(res, workingTable);
        return res;
    }

    @Override
    public List<String> getNodes(String graphId) throws UnknownGraphException {
        List<String> res = hashStorage.get(graphId);
        if (res == null) {
            throw new UnknownGraphException("Graph with id: " + graphId + " not found.");
        }
        return res;
    }

}
