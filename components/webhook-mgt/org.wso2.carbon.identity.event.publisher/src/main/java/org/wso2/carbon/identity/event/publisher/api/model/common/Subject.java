/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.event.publisher.api.model.common;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract Model class for Subject.
 */
public abstract class Subject {

    private String format;
    protected Map<String, Object> properties = new HashMap<>();

    public String getFormat() {

        return format;
    }

    protected void setFormat(String format) {

        this.format = format;
    }

    @JsonAnyGetter
    public Map<String, Object> getProperties() {

        return properties;
    }

    protected void addProperty(String key, Object value) {

        properties.put(key, value);
    }

    @JsonAnySetter
    protected void setProperties(Map<String, Object> properties) {

        this.properties = properties;
    }

    public Object getProperty(String key) {

        return properties.get(key);
    }

}
