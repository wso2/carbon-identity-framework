/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.identity.configuration.mgt.core.search.constant;

public class ConditionType {

    public enum ComplexOperator {
        OR,
        AND,
        NOT;

        public String toSQL() {

            String op = null;
            switch (this) {
                case OR:
                    op = this.toString();
                    break;
                case AND:
                    op = this.toString();
                    break;
                case NOT:
                    op = this.toString();
                    break;
            }
            return op;
        }
    }

    public enum PrimitiveOperator {
        EQUALS,
        NOT_EQUALS,
        LESS_THAN,
        GREATER_THAN,
        LESS_OR_EQUALS,
        GREATER_OR_EQUALS;

        public String toSQL() {

            String op = null;
            switch (this) {
                case EQUALS:
                    op = "=";
                    break;
                case NOT_EQUALS:
                    op = "<>";
                    break;
                case GREATER_THAN:
                    op = ">";
                    break;
                case GREATER_OR_EQUALS:
                    op = ">=";
                    break;
                case LESS_THAN:
                    op = "<";
                    break;
                case LESS_OR_EQUALS:
                    op = "<=";
                    break;
            }
            return op;
        }
    }
}
