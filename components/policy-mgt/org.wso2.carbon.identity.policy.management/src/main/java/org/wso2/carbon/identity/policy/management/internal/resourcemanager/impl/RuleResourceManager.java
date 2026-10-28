/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.policy.management.internal.resourcemanager.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.policy.management.api.constant.ErrorMessage;
import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementException;
import org.wso2.carbon.identity.policy.management.api.model.PolicyResource;
import org.wso2.carbon.identity.policy.management.api.model.PolicyResource.ResourceType;
import org.wso2.carbon.identity.policy.management.api.model.PolicyResourceMetadata;
import org.wso2.carbon.identity.policy.management.api.model.RulePolicyResource;
import org.wso2.carbon.identity.policy.management.api.model.RulePolicyResourceMetadata;
import org.wso2.carbon.identity.policy.management.api.type.PolicyTypeDefinition;
import org.wso2.carbon.identity.policy.management.api.type.RuleResourceTypeHandler;
import org.wso2.carbon.identity.policy.management.internal.component.PolicyMgtComponentServiceHolder;
import org.wso2.carbon.identity.policy.management.internal.resourcemanager.PolicyResourceManager;
import org.wso2.carbon.identity.policy.management.internal.util.PolicyManagementExceptionHandler;
import org.wso2.carbon.identity.rule.management.api.exception.RuleManagementException;
import org.wso2.carbon.identity.rule.management.api.model.Rule;
import org.wso2.carbon.identity.rule.management.api.service.RuleManagementService;
import org.wso2.carbon.identity.rule.metadata.api.exception.RuleMetadataException;
import org.wso2.carbon.identity.rule.metadata.api.model.FieldDefinition;
import org.wso2.carbon.identity.rule.metadata.api.model.FlowType;

import java.util.List;

/**
 * {@link PolicyResourceManager} for {@link ResourceType#RULE} resources, backed by rule-mgt.
 */
public class RuleResourceManager implements PolicyResourceManager {

    private static final Log LOG = LogFactory.getLog(RuleResourceManager.class);

    @Override
    public ResourceType getSupportedResourceType() {

        return ResourceType.RULE;
    }

    @Override
    public PolicyResource create(PolicyResource resource, String tenantDomain) throws PolicyManagementException {

        if (!(resource instanceof RulePolicyResource)) {
            throw PolicyManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_UNSUPPORTED_RESOURCE_TYPE_FOR_MANAGER,
                    resource == null ? "null" : resource.getClass().getName());
        }
        RulePolicyResource ruleResource = (RulePolicyResource) resource;
        try {
            Rule createdRule = getRuleManagementService().addRule(ruleResource.getRule(), tenantDomain);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Rule added for policy target '" + resource.getTarget()
                        + "' with ruleId: " + createdRule.getId());
            }
            return new RulePolicyResource.Builder()
                    .id(resource.getId())
                    .target(resource.getTarget())
                    .resourceId(createdRule.getId())
                    .build();
        } catch (RuleManagementException e) {
            throw PolicyManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_WHILE_ADDING_RULE_FOR_POLICY, e, resource.getTarget());
        }
    }

    @Override
    public PolicyResource enrich(PolicyResource resource, String tenantDomain) throws PolicyManagementException {

        try {
            Rule rule = getRuleManagementService().getRuleByRuleId(resource.getResourceId(), tenantDomain);
            return new RulePolicyResource.Builder()
                    .id(resource.getId())
                    .target(resource.getTarget())
                    .resourceId(resource.getResourceId())
                    .rule(rule)
                    .build();
        } catch (RuleManagementException e) {
            throw PolicyManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_WHILE_RETRIEVING_POLICY, e);
        }
    }

    @Override
    public void delete(PolicyResource resource, String tenantDomain) {

        try {
            getRuleManagementService().deleteRule(resource.getResourceId(), tenantDomain);
        } catch (RuleManagementException e) {
            LOG.error("Failed to delete rule " + resource.getResourceId()
                    + " from rule-mgt. Rule may be orphaned.", e);
        }
    }

    /**
     * {@inheritDoc}
     * Returns the fields, operators and values of the rule flow of the policy type that apply to the target.
     */
    @Override
    public PolicyResourceMetadata getMetadata(PolicyTypeDefinition definition, String target, String tenantDomain)
            throws PolicyManagementException {

        // The type definition guarantees that the rule handler is a RuleResourceTypeHandler.
        RuleResourceTypeHandler handler =
                (RuleResourceTypeHandler) definition.getResourceTypeHandler(ResourceType.RULE);
        if (handler == null || handler.getRuleFlowType() == null) {
            throw PolicyManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_RULE_FLOW_NOT_DEFINED, definition.getType());
        }
        // Rule management and rule metadata each have their own flow type enum with matching constants.
        FlowType metadataFlowType = FlowType.valueOf(handler.getRuleFlowType().name());
        try {
            List<FieldDefinition> fieldDefinitions = PolicyMgtComponentServiceHolder.getInstance()
                    .getRuleMetadataService().getExpressionMeta(metadataFlowType, tenantDomain);
            return new RulePolicyResourceMetadata(handler.getRuleFlowType(),
                    handler.filterMetadata(target, fieldDefinitions));
        } catch (RuleMetadataException e) {
            throw PolicyManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_WHILE_RETRIEVING_RULE_METADATA, e, definition.getType());
        }
    }

    private RuleManagementService getRuleManagementService() {

        return PolicyMgtComponentServiceHolder.getInstance().getRuleManagementService();
    }
}
