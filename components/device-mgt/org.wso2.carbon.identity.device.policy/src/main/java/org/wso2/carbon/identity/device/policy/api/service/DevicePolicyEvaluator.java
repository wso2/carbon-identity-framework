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

package org.wso2.carbon.identity.device.policy.api.service;

import org.wso2.carbon.identity.device.policy.api.exception.DevicePolicyException;
import org.wso2.carbon.identity.device.policy.api.model.DevicePolicyEvaluationResult;

import java.util.Map;

/**
 * Evaluates device policy compliance against a policy using device attribute
 * data. Enriches the device data with platform-verified integrity values
 * before finding the rule for the device's platform and evaluating it.
 * If no rule is configured for the platform, the policy does not restrict
 * that platform and the device is treated as compliant.
 */
public interface DevicePolicyEvaluator {

    /**
     * Evaluates device policy compliance for the given policy and device data.
     * The device data is enriched with platform-verified integrity values
     * internally before evaluation, so callers do not need to enrich it
     * themselves.
     *
     * @param policyId     ID of the policy to evaluate against.
     * @param deviceData   Mutable map of device field names to their values.
     * @param appId        App resource ID for loading credentials.
     * @param tenantDomain Tenant domain for policy lookup and evaluation.
     * @return A {@link DevicePolicyEvaluationResult} representing the evaluation status and field details.
     * @throws DevicePolicyException If an error occurs during policy evaluation.
     */
    DevicePolicyEvaluationResult evaluate(String policyId, Map<String, Object> deviceData,
                                           String appId, String tenantDomain)
            throws DevicePolicyException;

    /**
     * Resolves the policy name to its ID and evaluates it through
     * {@link #evaluate(String, Map, String, String)}. For callers that reference a policy by name,
     * such as adaptive authentication scripts and flow executors.
     *
     * @param policyName   Name of the policy to evaluate against.
     * @param deviceData   Mutable map of device field names to their values.
     * @param appId        App resource ID for loading credentials.
     * @param tenantDomain Tenant domain for policy lookup and evaluation.
     * @return A {@link DevicePolicyEvaluationResult} representing the evaluation status and field details.
     * @throws DevicePolicyException If an error occurs during policy evaluation.
     */
    DevicePolicyEvaluationResult evaluateByPolicyName(String policyName, Map<String, Object> deviceData,
                                                      String appId, String tenantDomain)
            throws DevicePolicyException;

    /**
     * Verifies a device-data JWT signed with the given public key and evaluates the resolved device
     * data through {@link #evaluate(String, Map, String, String)}. For callers that hold a token
     * rather than resolved device data, and whose trust in the key was established out of band, such
     * as the device registration flow verifying the key through the challenge signature. Signature,
     * {@code iat} freshness and {@code jti} single-use are enforced before the claims are trusted.
     *
     * @param policyId        ID of the policy to evaluate against.
     * @param token           Raw JWT string carrying the device attributes.
     * @param base64PublicKey Base64-encoded X.509 EC public key to verify the signature against.
     * @param correlationId   Identifier used only for diagnostic correlation.
     * @param appId           App resource ID for loading credentials.
     * @param tenantDomain    Tenant domain for policy lookup and evaluation.
     * @return A {@link DevicePolicyEvaluationResult} representing the evaluation status and field details.
     * @throws DevicePolicyException If the token is invalid or stale, or evaluation fails.
     */
    DevicePolicyEvaluationResult evaluateFromToken(String policyId, String token, String base64PublicKey,
                                                   String correlationId, String appId, String tenantDomain)
            throws DevicePolicyException;

    /**
     * Resolves the policy name to its ID and evaluates the device-data JWT through
     * {@link #evaluateFromToken(String, String, String, String, String, String)}.
     *
     * @param policyName      Name of the policy to evaluate against.
     * @param token           Raw JWT string carrying the device attributes.
     * @param base64PublicKey Base64-encoded X.509 EC public key to verify the signature against.
     * @param correlationId   Identifier used only for diagnostic correlation.
     * @param appId           App resource ID for loading credentials.
     * @param tenantDomain    Tenant domain for policy lookup and evaluation.
     * @return A {@link DevicePolicyEvaluationResult} representing the evaluation status and field details.
     * @throws DevicePolicyException If the token is invalid or stale, or evaluation fails.
     */
    DevicePolicyEvaluationResult evaluateFromTokenByPolicyName(String policyName, String token,
                                                               String base64PublicKey, String correlationId,
                                                               String appId, String tenantDomain)
            throws DevicePolicyException;
}
