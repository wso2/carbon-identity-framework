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

import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceSearchBean;
import org.wso2.carbon.identity.configuration.mgt.core.search.constant.ConditionType;
import org.wso2.carbon.identity.configuration.mgt.core.search.exception.PrimitiveConditionValidationException;

import java.lang.reflect.Field;

public class PrimitiveConditionValidator {

    private SearchBean searchBean;

    public PrimitiveConditionValidator(SearchBean searchBean) {

        this.searchBean = searchBean;
    }

    /**
     * Validate parameters in a {@link PrimitiveCondition} with the given Search bean
     *
     * @param primitiveCondition
     * @return A db qualified {@link PrimitiveCondition}.
     * @throws PrimitiveConditionValidationException
     */
    public PrimitiveCondition validate(PrimitiveCondition primitiveCondition) throws PrimitiveConditionValidationException {

        if (searchBean == null) {
            throw new NullPointerException("Invalid search bean: null in the PrimitiveCondition validate.");
        }
        String property = primitiveCondition.getProperty();
        ConditionType.PrimitiveOperator operator = primitiveCondition.getOperator();
        Object value = primitiveCondition.getValue();

        if (property == null || operator == null || value == null) {
            throw new PrimitiveConditionValidationException(
                    "Invalid primitive condition parameters found in: property = " + property
                            + (operator == null ? ", condition = null" : "")
                            + (value == null ? ", value = null" : "")
                            + "."
            );
        }
        try {
            Field field = this.searchBean.getClass().getDeclaredField(property);
            if (!field.getType().getName().equals(value.getClass().getName())) {
                throw new PrimitiveConditionValidationException(
                        "Value for the property: " + property + " is expected to be: " + field.getType().getName() +
                                " but found: " + value.getClass().getName()
                );
            }
        } catch (NoSuchFieldException e) {
            throw new PrimitiveConditionValidationException(
                    "Property: " + property + " is not found in the allowed search properties present in the bean " +
                            "class: " + ResourceSearchBean.class.getName()
            );
        }

        // If parameter validation are a success then build a database qualified primitive condition.
        PrimitiveCondition dbQualifiedPrimitiveCondition;
        dbQualifiedPrimitiveCondition = this.searchBean.mapPrimitiveCondition(primitiveCondition);
        dbQualifiedPrimitiveCondition.setProperty(this.searchBean.getDBQualifiedFieldName(
                dbQualifiedPrimitiveCondition.getProperty()
        ));
        return dbQualifiedPrimitiveCondition;
    }
}
