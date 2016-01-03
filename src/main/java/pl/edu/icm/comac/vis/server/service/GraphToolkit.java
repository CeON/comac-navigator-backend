/*
 * Copyright 2016 Pivotal Software, Inc..
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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Utility class to provide calculations on the graphs
 *
 * @author Aleksander Nowinski <a.nowinski@icm.edu.pl>
 */
@Service
public class GraphToolkit {

    /**
     * Method to choose appropriate not-favourite nodes of the graph. It takes
     * all nodes outside the normal and large sets, and chooses only those, who
     * are not overflowing the normal nodes over 'itemLinkLimit'. Note, that if 
     * at most one normal node is not overflown by item, others may be.
     *
     * @param normal list of normal favaourite nodes.
     * @param large list of favourite nodes which are overflown, i.e. we have limited
     * knowledge of their links
     * @param links map of all links for all items, normal and external
     * @param itemLinkLimit recommended size od the links
     * @return set of the items choosen for the graphs as not favourite nodes.
     */
    public Set<String> calculateAdditions(Set<String> normal, Set<String> large,
            Map<String, Set<String>> links, long itemLinkLimit) {
        Map<String, Long> outgoingLinks = new HashMap<>();
        normal.stream().forEach(x -> outgoingLinks.put(x, 0l));
        List<String> leftovers = links.keySet().stream().
                filter(x -> !(normal.contains(x) || large.contains(x))).
                collect(Collectors.toList());

        //now order list by the link count in each of each element:
        Collections.sort(leftovers, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                int res = -((Integer) links.get(o1).size()).compareTo(((Integer) links.get(o2).size()));
                if (res == 0) {
                    return o1.compareToIgnoreCase(o2);
                } else {
                    return res;
                }
            }
        });
        Set<String> approved = new HashSet<String>();
        for (String item : leftovers) {
            final Set<String> itemLinks = links.get(item);
            if (itemLinks.stream().
                    anyMatch(x -> {
                        return outgoingLinks.containsKey(x) && outgoingLinks.get(x) < itemLinkLimit;
                    })) {
                approved.add(item);
                itemLinks.stream().
                        filter(x -> outgoingLinks.containsKey(x)).
                        forEach(x -> outgoingLinks.put(x, outgoingLinks.get(x) + 1));
            }
        }
        return approved;
    }

}
