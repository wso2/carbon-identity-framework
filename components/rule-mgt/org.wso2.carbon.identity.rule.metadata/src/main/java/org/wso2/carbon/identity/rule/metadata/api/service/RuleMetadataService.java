/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.rule.metadata.api.service;

import org.wso2.carbon.identity.rule.metadata.api.exception.RuleMetadataException;
import org.wso2.carbon.identity.rule.metadata.api.model.FieldDefinition;
import org.wso2.carbon.identity.rule.metadata.api.model.FlowType;
import org.wso2.carbon.identity.rule.metadata.api.model.Operator;

import java.util.List;

/**
 * Rule metadata service interface.
 * This class is responsible for providing the rule metadata for the given flow type.
 */
public interface RuleMetadataService {

    /**
     * Get the expression metadata for the given flow type.
     *
     * @param flowType     Flow type
     * @param tenantDomain Tenant domain
     * @return List of field definitions
     * @throws RuleMetadataException If an error occurred while getting the metadata
     */
    public List<FieldDefinition> getExpressionMeta(FlowType flowType, String tenantDomain)
            throws RuleMetadataException;

    /**
     * Get all operators applicable for rule expressions.
     *
     * @return List of operators
     */
    public List<Operator> getApplicableOperatorsInExpressions();
}
