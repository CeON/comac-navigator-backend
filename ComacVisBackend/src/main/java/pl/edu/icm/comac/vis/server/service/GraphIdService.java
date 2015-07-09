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

import java.util.List;

/**
 * An interface for the service responsible for assigning IDs for the graph. The
 * interface is method agnostic.
 * @author Aleksander Nowinski <a.nowinski@icm.edu.pl>
 */
public interface GraphIdService {
    String getGraphId(List<String> nodes);
    List<String> getNodes(String graphId) throws UnknownGraphException;
}
