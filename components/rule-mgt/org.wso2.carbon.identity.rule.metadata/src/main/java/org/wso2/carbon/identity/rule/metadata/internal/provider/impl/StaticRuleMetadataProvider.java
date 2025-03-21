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

package org.wso2.carbon.identity.rule.metadata.internal.provider.impl;

import org.wso2.carbon.identity.rule.metadata.api.exception.RuleMetadataConfigException;
import org.wso2.carbon.identity.rule.metadata.api.exception.RuleMetadataException;
import org.wso2.carbon.identity.rule.metadata.api.exception.RuleMetadataServerException;
import org.wso2.carbon.identity.rule.metadata.api.model.FieldDefinition;
import org.wso2.carbon.identity.rule.metadata.api.model.FlowType;
import org.wso2.carbon.identity.rule.metadata.api.provider.RuleMetadataProvider;
import org.wso2.carbon.identity.rule.metadata.internal.config.RuleMetadataConfigFactory;
import org.wso2.carbon.identity.rule.metadata.internal.util.RuleMetadataExceptionBuilder;

import java.util.List;

/**
 * Static rule metadata provider.
 * This class is responsible for providing the static rule metadata defined in the system via config files.
 */
public class StaticRuleMetadataProvider implements RuleMetadataProvider {

    private StaticRuleMetadataProvider() {

    }

    public static StaticRuleMetadataProvider loadStaticMetadata() throws RuleMetadataServerException {

        try {
            RuleMetadataConfigFactory.load();
        } catch (RuleMetadataConfigException e) {
            throw RuleMetadataExceptionBuilder.buildServerException(RuleMetadataExceptionBuilder.RuleMetadataError
                    .ERROR_WHILE_LOADING_STATIC_RULE_METADATA, e);
        }

        return new StaticRuleMetadataProvider();
    }

    @Override
    public List<FieldDefinition> getExpressionMeta(FlowType flowType, String tenantDomain)
            throws RuleMetadataException {

        return RuleMetadataConfigFactory.getFlowConfig().getFieldDefinitionsForFlow(flowType);
    }
}
