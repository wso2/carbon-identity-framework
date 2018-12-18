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

package org.wso2.carbon.identity.configuration.mgt.core.search;

import org.wso2.carbon.identity.configuration.mgt.core.search.constant.ConditionType;
import org.wso2.carbon.identity.configuration.mgt.core.search.exception.PrimitiveConditionValidationException;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represent a complex condition with a {@link ConditionType}. A complex condition can contain a list of
 * another complex conditions or a primitive condition. Ex: A sample complex condition with two complex conditions
 * as a list can represent the form, {@link ComplexCondition}[1] {@link ConditionType}[2] {@link ComplexCondition}[2].
 */
public class ComplexCondition implements Condition {

    private List<Condition> conditions;
    private ConditionType.ComplexOperator operator;

    public ComplexCondition(ConditionType.ComplexOperator operator, List<Condition> conditions) {

        this.operator = operator;
        this.conditions = conditions;
    }

    public List<Condition> getConditions() {

        return conditions;
    }

    public PlaceholderSQL buildQuery(PrimitiveConditionValidator primitiveConditionValidator)
            throws PrimitiveConditionValidationException {

        PlaceholderSQL placeholderSQL = new PlaceholderSQL();
        ArrayList<Object> data = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        boolean first = true;
        for (Condition condition : conditions) {
            if (!first) {
                sb.append(" ").append(operator.toSQL()).append(" ");
            } else {
                first = false;
            }
            sb.append("(");
            PlaceholderSQL eachPlaceholderSQL = condition.buildQuery(primitiveConditionValidator);
            sb.append(eachPlaceholderSQL.getQuery());
            data.addAll(eachPlaceholderSQL.getData());
            sb.append(")");
        }

        placeholderSQL.setQuery(sb.toString());
        placeholderSQL.setData(data);
        return placeholderSQL;
    }
}
