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

package org.wso2.carbon.identity.rule.metadata.service.impl;

import org.wso2.carbon.identity.rule.metadata.exception.RuleMetadataException;
import org.wso2.carbon.identity.rule.metadata.exception.RuleMetadataServerException;
import org.wso2.carbon.identity.rule.metadata.model.FieldDefinition;
import org.wso2.carbon.identity.rule.metadata.model.FlowType;
import org.wso2.carbon.identity.rule.metadata.provider.RuleMetadataProvider;
import org.wso2.carbon.identity.rule.metadata.util.RuleMetadataExceptionBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class is responsible for managing the rule metadata providers and providing the metadata
 * for the given flow type.
 */
public class RuleMetadataManager {

    private static final RuleMetadataManager INSTANCE = new RuleMetadataManager();

    private final List<RuleMetadataProvider> metadataProviders = new ArrayList<>();

    private RuleMetadataManager() {

    }

    public static RuleMetadataManager getInstance() {

        return INSTANCE;
    }

    public void registerMetadataProvider(RuleMetadataProvider metadataProvider) {

        metadataProviders.add(metadataProvider);
    }

    public void unregisterMetadataProvider(RuleMetadataProvider metadataProvider) {

        metadataProviders.remove(metadataProvider);
    }

    public List<FieldDefinition> getExpressionMetaForFlow(FlowType flowType, String tenantDomain)
            throws RuleMetadataException {

        Set<String> addedFieldNames = new HashSet<>();
        List<FieldDefinition> addedFieldDefinitions = new ArrayList<>();
        for (RuleMetadataProvider metadataProvider : metadataProviders) {
            List<FieldDefinition> fieldDefinitionsFromProvider =
                    metadataProvider.getExpressionMeta(flowType, tenantDomain);
            addFieldDefinitions(metadataProvider, fieldDefinitionsFromProvider, addedFieldNames, addedFieldDefinitions);
        }
        return addedFieldDefinitions;
    }

    private void addFieldDefinitions(RuleMetadataProvider metadataProvider,
                                     List<FieldDefinition> fieldDefinitionsFromProvider, Set<String> addedFieldNames,
                                     List<FieldDefinition> addedFieldDefinitions) throws RuleMetadataServerException {

        for (FieldDefinition fieldDefinition : fieldDefinitionsFromProvider) {
            if (addedFieldNames.contains(fieldDefinition.getField().getName())) {
                throw RuleMetadataExceptionBuilder.buildServerException(
                        RuleMetadataExceptionBuilder.RuleMetadataError.ERROR_DUPLICATE_FIELD,
                        fieldDefinition.getField().getName(), metadataProvider.getClass().getName());
            }
            addedFieldNames.add(fieldDefinition.getField().getName());
        }

        addedFieldDefinitions.addAll(fieldDefinitionsFromProvider);
    }
}

