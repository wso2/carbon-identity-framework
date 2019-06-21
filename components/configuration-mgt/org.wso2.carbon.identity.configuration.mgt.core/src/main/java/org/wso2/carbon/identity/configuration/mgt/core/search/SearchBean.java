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

import org.wso2.carbon.identity.configuration.mgt.core.search.exception.PrimitiveConditionValidationException;

/**
 * This Interface is used to validate a {@link PrimitiveCondition} with {@link PrimitiveConditionValidator}. Any custom
 * modification required for the use case of the search bean is performed here.
 */
public interface SearchBean {

    /**
     * Generate the database qualified name for the given field name.
     *
     * @return Database qualified name for the given field name.
     */
    String getDBQualifiedFieldName(String fieldName);

    /**
     * Map the given {@link PrimitiveCondition} as per the need of the use case.
     *
     * @return Mapped {@link PrimitiveCondition}.
     */
    PrimitiveCondition mapPrimitiveCondition(PrimitiveCondition primitiveCondition)
            throws PrimitiveConditionValidationException;
}
