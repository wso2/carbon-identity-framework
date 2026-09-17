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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.device.policy.api.constant.DevicePolicyErrorMessage;
import org.wso2.carbon.identity.device.policy.api.exception.DevicePolicyException;
import org.wso2.carbon.identity.device.policy.api.exception.DevicePolicyServerException;
import org.wso2.carbon.identity.device.policy.api.model.DevicePolicyEvaluationResult;
import org.wso2.carbon.identity.device.policy.api.service.DevicePolicyEvaluator;
import org.wso2.carbon.identity.device.policy.internal.component.DevicePolicyComponentServiceHolder;
import org.wso2.carbon.identity.device.policy.internal.util.DevicePolicyExceptionHandler;
import org.wso2.carbon.identity.device.policy.internal.util.DeviceTokenExtractor;
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
import java.util.Optional;

/**
 * Default implementation of {@link DevicePolicyEvaluator}.
 */
public class DevicePolicyEvaluatorImpl implements DevicePolicyEvaluator {

    private static final Log LOG = LogFactory.getLog(DevicePolicyEvaluatorImpl.class);
    private static final String DEVICE_PLATFORM_FIELD = "platform";
    private static final String FLOW_TYPE_DEVICE_POLICY = "DEVICE_POLICY";

    @Override
    public DevicePolicyEvaluationResult evaluate(String policyId, Map<String, Object> deviceData,
                                                 String appId, String tenantDomain)
            throws DevicePolicyException {

        DevicePolicyComponentServiceHolder.getInstance()
                .getIntegrityDataEnricher()
                .enrich(deviceData, appId, tenantDomain);

        Policy policy = getPolicy(policyId, tenantDomain);
        if (policy == null) {
            throw DevicePolicyExceptionHandler.handleClientException(
                    DevicePolicyErrorMessage.ERROR_DEVICE_POLICY_NOT_FOUND, policyId, tenantDomain);
        }

        Optional<DevicePolicyEvaluationResult> incompleteResult =
                checkDeviceDataCompleteness(policyId, policy, deviceData);
        if (incompleteResult.isPresent()) {
            return incompleteResult.get();
        }
        // The completeness check above guarantees the platform is present.
        String platform = (String) deviceData.get(DEVICE_PLATFORM_FIELD);

        PolicyEvaluationContext context = new PolicyEvaluationContext(FLOW_TYPE_DEVICE_POLICY);
        deviceData.forEach(context::add);
        PolicyEvaluationResult result;
        try {
            result = DevicePolicyComponentServiceHolder.getInstance()
                    .getPolicyEvaluationService()
                    .evaluate(policyId, platform, context, tenantDomain);
        } catch (PolicyEvaluationException e) {
            throw DevicePolicyExceptionHandler.handleServerException(
                    DevicePolicyErrorMessage.ERROR_DEVICE_POLICY_EVALUATION_FAILED, e, policyId);
        }

        if (!result.isSatisfied()) {
            List<String> failedFields = result.getResults().stream()
                    .filter(resourceResult -> !resourceResult.isSatisfied())
                    .filter(resourceResult -> resourceResult instanceof RuleResourceEvaluationResult)
                    .flatMap(resourceResult ->
                            ((RuleResourceEvaluationResult) resourceResult).getFailedFields().stream())
                    .toList();
            return DevicePolicyEvaluationResult.nonCompliant(policyId, failedFields);
        }
        return DevicePolicyEvaluationResult.compliant(policyId);
    }

    @Override
    public DevicePolicyEvaluationResult evaluateByPolicyName(String policyName, Map<String, Object> deviceData,
                                                             String appId, String tenantDomain)
            throws DevicePolicyException {

        String policyId = getPolicyId(policyName, tenantDomain);
        if (policyId == null) {
            throw DevicePolicyExceptionHandler.handleClientException(
                    DevicePolicyErrorMessage.ERROR_DEVICE_POLICY_NOT_FOUND, policyName, tenantDomain);
        }
        return evaluate(policyId, deviceData, appId, tenantDomain);
    }

    @Override
    public DevicePolicyEvaluationResult evaluateFromToken(String policyId, String token, String base64PublicKey,
                                                          String correlationId, String appId, String tenantDomain)
            throws DevicePolicyException {

        Map<String, Object> deviceData = new DeviceTokenExtractor()
                .extractWithPublicKey(token, base64PublicKey, correlationId, tenantDomain);
        return evaluate(policyId, deviceData, appId, tenantDomain);
    }

    @Override
    public DevicePolicyEvaluationResult evaluateFromTokenByPolicyName(String policyName, String token,
                                                                      String base64PublicKey, String correlationId,
                                                                      String appId, String tenantDomain)
            throws DevicePolicyException {

        String policyId = getPolicyId(policyName, tenantDomain);
        if (policyId == null) {
            throw DevicePolicyExceptionHandler.handleClientException(
                    DevicePolicyErrorMessage.ERROR_DEVICE_POLICY_NOT_FOUND, policyName, tenantDomain);
        }
        return evaluateFromToken(policyId, token, base64PublicKey, correlationId, appId, tenantDomain);
    }

    private Policy getPolicy(String policyId, String tenantDomain) throws DevicePolicyServerException {

        try {
            return DevicePolicyComponentServiceHolder.getInstance()
                    .getPolicyManagementService()
                    .getPolicyById(policyId, tenantDomain);
        } catch (PolicyManagementException e) {
            throw DevicePolicyExceptionHandler.handleServerException(
                    DevicePolicyErrorMessage.ERROR_DEVICE_POLICY_EVALUATION_FAILED, e, policyId);
        }
    }

    private String getPolicyId(String policyName, String tenantDomain) throws DevicePolicyServerException {

        try {
            return DevicePolicyComponentServiceHolder.getInstance()
                    .getPolicyManagementService()
                    .getPolicyIdByName(policyName, tenantDomain);
        } catch (PolicyManagementException e) {
            throw DevicePolicyExceptionHandler.handleServerException(
                    DevicePolicyErrorMessage.ERROR_DEVICE_POLICY_EVALUATION_FAILED, e, policyName);
        }
    }

    private Optional<DevicePolicyEvaluationResult> checkDeviceDataCompleteness(String policyId,
            Policy policy, Map<String, Object> deviceData) {

        String platform = (String) deviceData.get(DEVICE_PLATFORM_FIELD);
        List<String> missingFields;
        if (StringUtils.isBlank(platform)) {
            missingFields = Collections.singletonList(DEVICE_PLATFORM_FIELD);
        } else {
            missingFields = findMissingRequiredFields(platform, policy, deviceData);
        }

        if (!missingFields.isEmpty()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Device data incomplete for policy '" + policyId + "': " + missingFields);
            }
            return Optional.of(DevicePolicyEvaluationResult.incompleteDeviceData(policyId, missingFields));
        }
        return Optional.empty();
    }

    private List<String> findMissingRequiredFields(String platform, Policy policy, Map<String, Object> deviceData) {

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
                .toList();
    }
}
