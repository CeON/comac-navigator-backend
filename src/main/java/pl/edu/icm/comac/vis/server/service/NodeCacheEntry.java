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

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import pl.edu.icm.comac.vis.server.model.NodeType;

/**
 *
 * @author Aleksander Nowinski <a.nowinski@icm.edu.pl>
 */
public class NodeCacheEntry implements Serializable {
    String name;
    String id;
    NodeType type;
    List<RelationCacheEntry> relations; //bot in and out relations
    boolean overflow;

    public NodeCacheEntry(String name, String id, NodeType type, List<RelationCacheEntry> relations) {
        this.name = name;
        this.id = id;
        this.type = type;
        this.relations = relations;
        this.overflow = false;
    }

    public NodeCacheEntry(String name, String id, NodeType type, boolean overflow) {
        this.name = name;
        this.id = id;
        this.type = type;
        this.overflow = overflow;
        this.relations = null;
    }
    
    

    
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public NodeType getType() {
        return type;
    }

    public void setType(NodeType type) {
        this.type = type;
    }

    

    public List<RelationCacheEntry> getRelations() {
        return relations;
    }

    public void setRelations(List<RelationCacheEntry> relations) {
        this.relations = relations;
    }

    public boolean isOverflow() {
        return overflow;
    }

    public void setOverflow(boolean overflow) {
        this.overflow = overflow;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.name);
        hash = 97 * hash + Objects.hashCode(this.id);
        hash = 97 * hash + Objects.hashCode(this.type);
        hash = 97 * hash + Objects.hashCode(this.relations);
        hash = 97 * hash + (this.overflow ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NodeCacheEntry other = (NodeCacheEntry) obj;
        if (this.overflow != other.overflow) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        if (!Objects.equals(this.relations, other.relations)) {
            return false;
        }
        return true;
    }
    
}
