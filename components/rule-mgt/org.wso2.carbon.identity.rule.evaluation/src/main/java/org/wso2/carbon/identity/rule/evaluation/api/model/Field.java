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

package org.wso2.carbon.identity.rule.evaluation.api.model;

/**
 * Field model.
 * This class represents a field in a rule.
 */
public class Field {

    private final String name;
    private final ValueType valueType;

    public Field(String name, ValueType valueType) {

        this.name = name;
        this.valueType = valueType;
    }

    public String getName() {

        return name;
    }

    public ValueType getValueType() {

        return valueType;
    }
}
