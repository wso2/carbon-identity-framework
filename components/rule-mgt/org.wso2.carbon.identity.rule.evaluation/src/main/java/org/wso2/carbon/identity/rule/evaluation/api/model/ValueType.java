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
 * Value type model.
 * This class represents the type of value of a field in a rule.
 * It can be a string, number, boolean or a reference.
 * This value type is used to communicate the data type of the field value to the rule evaluation data providers.
 */
public class ValueType {

    public static final ValueType STRING = new ValueType("STRING");
    public static final ValueType NUMBER = new ValueType("NUMBER");
    public static final ValueType BOOLEAN = new ValueType("BOOLEAN");
    public static final ReferenceValueType REFERENCE = new ReferenceValueType("REFERENCE");
    public static final ValueType LIST = new ValueType("LIST");

    public static ReferenceValueType createReferenceType(String referenceAttribute) {

        return new ReferenceValueType("REFERENCE", referenceAttribute);
    }

    private final String name;

    protected ValueType(String name) {

        this.name = name;
    }

    public String getName() {

        return name;
    }

    /**
     * Reference value type model.
     * This class represents the reference value type of a field in a rule.
     * It contains the reference attribute name that the field is referring to.
     */
    public static class ReferenceValueType extends ValueType {

        private String referenceAttribute;

        public ReferenceValueType(String name) {

            super(name);
        }

        private ReferenceValueType(String name, String referenceAttribute) {

            super(name);
            this.referenceAttribute = referenceAttribute;
        }

        public String getReferenceAttribute() {

            return referenceAttribute;
        }
    }
}
