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
package pl.edu.icm.comac.vis.server.model;

/**
 *
 * @author Aleksander Nowinski <a.nowinski@icm.edu.pl>
 */
public class PropertyTranslator {
    String URL;
    String JSONPropertyName;
    boolean singular;
    boolean objectValue = false;

    public PropertyTranslator(String URL, String JSONPropertyName, boolean singular, boolean objectValue) {
        this.URL = URL;
        this.JSONPropertyName = JSONPropertyName;
        this.singular = singular;
        this.objectValue = objectValue;
    }

    
    
    public PropertyTranslator(String URL, String JSONPropertyName, boolean singular) {
        this.URL = URL;
        this.JSONPropertyName = JSONPropertyName;
        this.singular = singular;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getJSONPropertyName() {
        return JSONPropertyName;
    }

    public void setJSONPropertyName(String JSONPropertyName) {
        this.JSONPropertyName = JSONPropertyName;
    }

    public boolean isSingular() {
        return singular;
    }

    public void setSingular(boolean singular) {
        this.singular = singular;
    }

    public boolean isObjectValue() {
        return objectValue;
    }

    public void setObjectValue(boolean objectValue) {
        this.objectValue = objectValue;
    }

    
    
    
    
}
