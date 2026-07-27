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
package org.wso2.carbon.identity.flow.mgt.model;

import java.io.Serializable;
import java.util.List;

/**
 * Data Transfer Object for validation information.
 */
public class ValidationDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    private String type;
    private String name;
    private String message;
    private String label;
    private List<Condition> conditions;

    public String getType() {

        return type;
    }

    public void setType(String type) {

        this.type = type;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getMessage() {

        return message;
    }

    public void setMessage(String message) {

        this.message = message;
    }

    public String getLabel() {

        return label;
    }

    public void setLabel(String label) {

        this.label = label;
    }

    public List<Condition> getConditions() {

        return conditions;
    }

    public void setConditions(List<Condition> conditions) {

        this.conditions = conditions;
    }

    /**
     * Represents a single validation condition with a key and value.
     */
    public static class Condition implements Serializable {

        private static final long serialVersionUID = 1L;
        private String key;
        private String value;

        public Condition(String key, String value) {

            this.key = key;
            this.value = value;
        }

        public String getKey() {

            return key;
        }

        public String getValue() {

            return value;
        }
    }
}
