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

package org.wso2.carbon.identity.device.policy.internal.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.device.policy.api.constant.DevicePolicyErrorMessage;
import org.wso2.carbon.identity.device.policy.api.exception.DevicePolicyException;
import org.wso2.carbon.identity.device.policy.api.exception.DevicePolicyServerException;
import org.wso2.carbon.identity.device.policy.api.model.DevicePolicyEvaluationResult;
import org.wso2.carbon.identity.device.policy.api.service.DevicePolicyEvaluator;
import org.wso2.carbon.identity.device.policy.internal.component.DevicePolicyComponentServiceHolder;
import org.wso2.carbon.identity.device.policy.internal.util.DevicePolicyExceptionHandler;
import org.wso2.carbon.identity.policy.evaluation.api.exception.PolicyEvaluationException;
import org.wso2.carbon.identity.policy.evaluation.api.model.PolicyEvaluationContext;
import org.wso2.carbon.identity.policy.evaluation.api.model.PolicyEvaluationResult;
import org.wso2.carbon.identity.policy.evaluation.api.model.RuleResourceEvaluationResult;
import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementException;
import org.wso2.carbon.identity.policy.management.api.model.Policy;
import org.wso2.carbon.identity.policy.management.api.model.PolicyResource;
import org.wso2.carbon.identity.policy.management.api.model.PolicyResource.ResourceType;
import org.wso2.carbon.identity.policy.management.api.model.RulePolicyResource;
import org.wso2.carbon.identity.rule.management.api.model.Expression;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link DevicePolicyEvaluator}.
 */
public class DevicePolicyEvaluatorImpl implements DevicePolicyEvaluator {

    private static final Log LOG = LogFactory.getLog(DevicePolicyEvaluatorImpl.class);
    private static final String DEVICE_PLATFORM_FIELD = "platform";
    private static final String FLOW_TYPE_DEVICE_POLICY = "DEVICE_POLICY";

    @Override
    public DevicePolicyEvaluationResult evaluate(String policyName, Map<String, Object> deviceData,
                                                 String appId, String tenantDomain)
            throws DevicePolicyException {

        DevicePolicyComponentServiceHolder.getInstance()
                .getIntegrityDataEnricher()
                .enrich(deviceData, appId, tenantDomain);

        String platform = (String) deviceData.get(DEVICE_PLATFORM_FIELD);

        List<String> missingFields = findMissingRequiredFields(policyName, platform, deviceData, tenantDomain);
        if (!missingFields.isEmpty()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Device data incomplete for policy '" + policyName + "': " + missingFields);
            }
            return DevicePolicyEvaluationResult.incompleteDeviceData(policyName, missingFields);
        }

        String policyId;
        try {
            policyId = DevicePolicyComponentServiceHolder.getInstance()
                    .getPolicyManagementService()
                    .getPolicyIdByName(policyName, tenantDomain);
        } catch (PolicyManagementException e) {
            throw DevicePolicyExceptionHandler.handleServerException(
                    DevicePolicyErrorMessage.ERROR_DEVICE_POLICY_EVALUATION_FAILED, e, policyName);
        }

        if (policyId == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Policy not found: " + policyName + " for tenant: " + tenantDomain);
            }
            return DevicePolicyEvaluationResult.policyNotFound(policyName);
        }

        PolicyEvaluationContext context = new PolicyEvaluationContext(FLOW_TYPE_DEVICE_POLICY);
        deviceData.forEach(context::add);
        PolicyEvaluationResult result;
        try {
            result = DevicePolicyComponentServiceHolder.getInstance()
                    .getPolicyEvaluationService()
                    .evaluate(policyId, platform != null ? platform : "", context, tenantDomain);
        } catch (PolicyEvaluationException e) {
            throw DevicePolicyExceptionHandler.handleServerException(
                    DevicePolicyErrorMessage.ERROR_DEVICE_POLICY_EVALUATION_FAILED, e, policyName);
        }

        if (result == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Policy not found: " + policyName + " for tenant: " + tenantDomain);
            }
            return DevicePolicyEvaluationResult.policyNotFound(policyName);
        }

        if (!result.isSatisfied()) {
            List<String> failedFields = result.getResults().stream()
                    .filter(resourceResult -> !resourceResult.isSatisfied())
                    .filter(resourceResult -> resourceResult instanceof RuleResourceEvaluationResult)
                    .flatMap(resourceResult ->
                            ((RuleResourceEvaluationResult) resourceResult).getFailedFields().stream())
                    .collect(Collectors.toList());
            return DevicePolicyEvaluationResult.nonCompliant(policyName, failedFields);
        }
        return DevicePolicyEvaluationResult.compliant(policyName);
    }

    private List<String> findMissingRequiredFields(String policyName, String platform,
            Map<String, Object> deviceData, String tenantDomain) throws DevicePolicyServerException {

        if (platform == null || platform.trim().isEmpty()) {
            return Collections.singletonList(DEVICE_PLATFORM_FIELD);
        }
        Policy policy;
        try {
            policy = DevicePolicyComponentServiceHolder.getInstance()
                    .getPolicyManagementService()
                    .getPolicyByName(policyName, tenantDomain);
        } catch (PolicyManagementException e) {
            throw DevicePolicyExceptionHandler.handleServerException(
                    DevicePolicyErrorMessage.ERROR_DEVICE_POLICY_EVALUATION_FAILED, e, policyName);
        }
        if (policy == null) {
            return Collections.emptyList();
        }
        PolicyResource resource = policy.getResources().stream()
                .filter(r -> r.getResourceType() == ResourceType.RULE
                        && platform.equalsIgnoreCase(r.getTarget()))
                .findFirst()
                .orElse(null);
        if (!(resource instanceof RulePolicyResource)) {
            return Collections.emptyList();
        }
        RulePolicyResource ruleResource = (RulePolicyResource) resource;
        if (ruleResource.getRule() == null) {
            return Collections.emptyList();
        }
        return ruleResource.getRule().getExpressions().stream()
                .map(Expression::getField)
                .distinct()
                .filter(field -> {
                    Object value = deviceData.get(field);
                    return value == null || String.valueOf(value).trim().isEmpty();
                })
                .collect(Collectors.toList());
    }
}
