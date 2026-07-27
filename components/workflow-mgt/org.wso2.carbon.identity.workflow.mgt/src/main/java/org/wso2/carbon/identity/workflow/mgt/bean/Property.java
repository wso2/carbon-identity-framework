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

package org.wso2.carbon.identity.workflow.mgt.bean;

/**
 * This class represents a key-value pair property associated with a workflow or task.
 */
public class Property {

    private String key = null;

    private String value = null;

    /**
     * The key of the task property.
     **/
    public String getKey() {

        return key;
    }

    /**
     * Set Key of the task property.
     **/
    public void setKey(String key) {

        this.key = key;
    }

    /**
     * The value of the key of the task property.
     **/
    public String getValue() {

        return value;
    }

    /**
     * Set Value of the key of the task property.
     **/
    public void setValue(String value) {

        this.value = value;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("class PropertyDTO {\n");
        builder.append("  key: ").append(key).append("\n");
        builder.append("  value: ").append(value).append("\n");
        builder.append("}\n");
        return builder.toString();
    }
}
