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

package org.wso2.carbon.identity.workflow.mgt.bean.metadata.type;

/**
 * DataType enum represents the data types used in workflow metadata.
 */
public enum DataType {

    STRING("String"),
    DOUBLE("Double"),
    INTEGER("Integer"),
    BOOLEAN("Boolean"),
    DATE_TIME("DateTime");
    private final String value;

    DataType(String v) {

        value = v;
    }

    public static DataType fromValue(String v) {

        for (DataType c : DataType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

    public String value() {

        return value;
    }
}
