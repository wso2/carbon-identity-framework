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
import org.osgi.annotation.bundle.Capability;
import org.wso2.carbon.identity.device.policy.api.constant.DevicePolicyErrorMessage;
import org.wso2.carbon.identity.device.policy.api.exception.DevicePolicyException;
import org.wso2.carbon.identity.device.policy.api.exception.DevicePolicyServerException;
import org.wso2.carbon.identity.device.policy.api.model.DevicePolicyEvaluationResult;
import org.wso2.carbon.identity.device.policy.api.service.DevicePolicyEvaluator;
import org.wso2.carbon.identity.device.policy.internal.component.DevicePolicyComponentServiceHolder;
import org.wso2.carbon.identity.device.policy.internal.type.DevicePolicyTypeDefinition;
import org.wso2.carbon.identity.device.policy.internal.util.DevicePolicyExceptionHandler;
import org.wso2.carbon.identity.device.policy.internal.util.DeviceTokenExtractor;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.util.OrganizationManagementUtil;
import org.wso2.carbon.identity.policy.evaluation.api.exception.PolicyEvaluationException;
import org.wso2.carbon.identity.policy.evaluation.api.model.PolicyEvaluationContext;
import org.wso2.carbon.identity.policy.evaluation.api.model.PolicyEvaluationResult;
import org.wso2.carbon.identity.policy.evaluation.api.model.RuleResourceEvaluationResult;
import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Default implementation of {@link DevicePolicyEvaluator}.
 */
@Capability(
        namespace = "osgi.service",
        attribute = {
                "objectClass=org.wso2.carbon.identity.device.policy.api.service.DevicePolicyEvaluator",
                "service.scope=singleton"
        }
)
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

        ResolvedPolicy policy = resolvePolicy(ReferenceType.ID, policyId, tenantDomain);
        return evaluatePolicy(policy.getPolicyId(), deviceData, policy.getTenantDomain());
    }

    /**
     * Evaluates enriched device data against a policy in the tenant that owns it.
     *
     * @param policyId           Policy ID.
     * @param deviceData         Device data.
     * @param policyTenantDomain Tenant domain that owns the policy.
     * @return Evaluation result.
     * @throws DevicePolicyException If the evaluation fails.
     */
    private DevicePolicyEvaluationResult evaluatePolicy(String policyId, Map<String, Object> deviceData,
                                                        String policyTenantDomain)
            throws DevicePolicyException {

        // The platform is the only field required up front, since it selects the rule to evaluate.
        // Every other field is left to the rule engine: a rule may be satisfied by only part of its
        // expressions, so absent data does not necessarily mean the policy cannot pass.
        String platform = (String) deviceData.get(DEVICE_PLATFORM_FIELD);
        if (StringUtils.isBlank(platform)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Device data incomplete for policy '" + policyId + "': " + DEVICE_PLATFORM_FIELD);
            }
            return DevicePolicyEvaluationResult.incompleteDeviceData(policyId,
                    Collections.singletonList(DEVICE_PLATFORM_FIELD));
        }

        PolicyEvaluationContext context = new PolicyEvaluationContext(FLOW_TYPE_DEVICE_POLICY);
        deviceData.forEach(context::add);
        PolicyEvaluationResult result;
        try {
            result = DevicePolicyComponentServiceHolder.getInstance()
                    .getPolicyEvaluationService()
                    .evaluate(policyId, platform, context, policyTenantDomain);
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
            // A field the device never sent failed because the data was absent, not because the
            // device breached the policy. Report those as incomplete data instead.
            List<String> missingFields = failedFields.stream()
                    .filter(field -> isBlankValue(deviceData.get(field)))
                    .toList();
            if (!missingFields.isEmpty()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Device data incomplete for policy '" + policyId + "': " + missingFields);
                }
                return DevicePolicyEvaluationResult.incompleteDeviceData(policyId, missingFields);
            }
            return DevicePolicyEvaluationResult.nonCompliant(policyId, failedFields);
        }
        return DevicePolicyEvaluationResult.compliant(policyId);
    }

    @Override
    public DevicePolicyEvaluationResult evaluateByPolicyName(String policyName, Map<String, Object> deviceData,
                                                             String appId, String tenantDomain)
            throws DevicePolicyException {

        ResolvedPolicy policy = resolvePolicy(ReferenceType.NAME, policyName, tenantDomain);
        DevicePolicyComponentServiceHolder.getInstance()
                .getIntegrityDataEnricher()
                .enrich(deviceData, appId, tenantDomain);
        return evaluatePolicy(policy.getPolicyId(), deviceData, policy.getTenantDomain());
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

        ResolvedPolicy policy = resolvePolicy(ReferenceType.NAME, policyName, tenantDomain);
        Map<String, Object> deviceData = new DeviceTokenExtractor()
                .extractWithPublicKey(token, base64PublicKey, correlationId, tenantDomain);
        DevicePolicyComponentServiceHolder.getInstance()
                .getIntegrityDataEnricher()
                .enrich(deviceData, appId, tenantDomain);
        return evaluatePolicy(policy.getPolicyId(), deviceData, policy.getTenantDomain());
    }

    /**
     * Finds a policy ID in a single tenant.
     *
     * @param referenceType   ID or name reference.
     * @param policyReference Policy ID or name.
     * @param tenantDomain    Tenant domain.
     * @return Policy ID, or empty if not found.
     * @throws DevicePolicyServerException If the lookup fails.
     */
    private Optional<String> findPolicyId(ReferenceType referenceType, String policyReference, String tenantDomain)
            throws DevicePolicyServerException {

        try {
            if (referenceType == ReferenceType.ID) {
                return DevicePolicyComponentServiceHolder.getInstance().getPolicyManagementService()
                        .getPolicyById(policyReference, tenantDomain) != null
                        ? Optional.of(policyReference) : Optional.empty();
            }
            return Optional.ofNullable(DevicePolicyComponentServiceHolder.getInstance()
                    .getPolicyManagementService()
                    .getPolicyIdByName(DevicePolicyTypeDefinition.TYPE, policyReference, tenantDomain));
        } catch (PolicyManagementException e) {
            throw DevicePolicyExceptionHandler.handleServerException(
                    DevicePolicyErrorMessage.ERROR_DEVICE_POLICY_EVALUATION_FAILED, e, policyReference);
        }
    }

    /**
     * Resolves a policy by ID or name, using the root organization's policy for sub-organizations.
     *
     * @param referenceType   ID or name reference.
     * @param policyReference Policy ID or name.
     * @param tenantDomain    Tenant domain of the request.
     * @return Policy ID and its owner tenant domain.
     * @throws DevicePolicyException If the policy is not found or the lookup fails.
     */
    private ResolvedPolicy resolvePolicy(ReferenceType referenceType, String policyReference, String tenantDomain)
            throws DevicePolicyException {

        String policyTenantDomain = tenantDomain;
        try {
            if (OrganizationManagementUtil.isOrganization(tenantDomain)) {
                // Sub-organizations can't own device policies, so they use the root organization's policy.
                OrganizationManager organizationManager =
                        DevicePolicyComponentServiceHolder.getInstance().getOrganizationManager();
                policyTenantDomain = organizationManager.resolveTenantDomain(organizationManager
                        .getPrimaryOrganizationId(organizationManager.resolveOrganizationId(tenantDomain)));
            }
        } catch (OrganizationManagementException e) {
            throw DevicePolicyExceptionHandler.handleServerException(
                    DevicePolicyErrorMessage.ERROR_DEVICE_POLICY_ORGANIZATION_RESOLUTION_FAILED, e, policyReference,
                    tenantDomain);
        }
        String resolvedTenantDomain = policyTenantDomain;
        return findPolicyId(referenceType, policyReference, resolvedTenantDomain)
                .map(policyId -> new ResolvedPolicy(policyId, resolvedTenantDomain))
                .orElseThrow(() -> DevicePolicyExceptionHandler.handleClientException(
                        DevicePolicyErrorMessage.ERROR_DEVICE_POLICY_NOT_FOUND, policyReference, tenantDomain));
    }

    private boolean isBlankValue(Object value) {

        return value == null || StringUtils.isBlank(String.valueOf(value));
    }

    /**
     * Policy ID with the tenant domain that owns it.
     */
    private static final class ResolvedPolicy {

        private final String policyId;
        private final String tenantDomain;

        private ResolvedPolicy(String policyId, String tenantDomain) {

            this.policyId = policyId;
            this.tenantDomain = tenantDomain;
        }

        private String getPolicyId() {

            return policyId;
        }

        private String getTenantDomain() {

            return tenantDomain;
        }
    }

    /**
     * How a policy is referenced.
     */
    private enum ReferenceType {
        ID,
        NAME
    }
}
