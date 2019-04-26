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

/**
 * Represents a primitive search expression. Ex: 'a > 5' where property is 'a', operator is '>' and value is '5'.
 */
public class PrimitiveCondition implements Condition {

    private String property;
    private Object value;
    private ConditionType.PrimitiveOperator operator;

    public PrimitiveCondition(String property, ConditionType.PrimitiveOperator operator, Object value) {

        this.property = property;
        this.value = value;
        this.operator = operator;
    }

    public ConditionType.PrimitiveOperator getOperator() {

        return operator;
    }

    public void setOperator(ConditionType.PrimitiveOperator operator) {

        this.operator = operator;
    }

    public String getProperty() {

        return property;
    }

    public void setProperty(String property) {

        this.property = property;
    }

    public Object getValue() {

        return value;
    }

    public void setValue(Object value) {

        this.value = value;
    }

    public PlaceholderSQL buildQuery(PrimitiveConditionValidator primitiveConditionValidator)
            throws PrimitiveConditionValidationException {

        PlaceholderSQL placeholderSQL = new PlaceholderSQL();
        PrimitiveCondition dbQualifiedPrimitiveCondition =
                primitiveConditionValidator.validate(this);
        placeholderSQL.setQuery(
                dbQualifiedPrimitiveCondition.getProperty() + " " + dbQualifiedPrimitiveCondition.getOperator().toSQL()
                        + " ?"
        );
        ArrayList<Object> data = new ArrayList<>();
        data.add(dbQualifiedPrimitiveCondition.getValue());
        placeholderSQL.setData(data);
        return placeholderSQL;
    }
}
