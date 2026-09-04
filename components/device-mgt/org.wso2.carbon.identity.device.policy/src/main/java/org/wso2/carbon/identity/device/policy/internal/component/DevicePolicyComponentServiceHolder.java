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

package org.wso2.carbon.identity.device.policy.internal.component;

import org.wso2.carbon.identity.application.authentication.framework.JsFunctionRegistry;
import org.wso2.carbon.identity.client.attestation.mgt.services.ClientAttestationService;
import org.wso2.carbon.identity.device.mgt.api.service.DeviceManagementService;
import org.wso2.carbon.identity.device.policy.api.service.DevicePolicyEvaluator;
import org.wso2.carbon.identity.device.policy.internal.service.IntegrityDataEnricher;
import org.wso2.carbon.identity.policy.evaluation.api.service.PolicyEvaluationService;
import org.wso2.carbon.identity.policy.management.api.service.PolicyManagementService;

/**
 * Service holder for the device policy component.
 * Provides access to all OSGi services consumed by this bundle.
 */
public class DevicePolicyComponentServiceHolder {

    private static final DevicePolicyComponentServiceHolder INSTANCE = new DevicePolicyComponentServiceHolder();

    private PolicyEvaluationService policyEvaluationService;
    private PolicyManagementService policyManagementService;
    private DeviceManagementService deviceManagementService;
    private JsFunctionRegistry jsFunctionRegistry;
    private ClientAttestationService clientAttestationService;
    private DevicePolicyEvaluator devicePolicyEvaluator;
    private IntegrityDataEnricher integrityDataEnricher;

    private DevicePolicyComponentServiceHolder() {

    }

    /**
     * Returns the singleton service holder instance.
     *
     * @return Service holder.
     */
    public static DevicePolicyComponentServiceHolder getInstance() {

        return INSTANCE;
    }

    public PolicyEvaluationService getPolicyEvaluationService() {

        return policyEvaluationService;
    }

    public void setPolicyEvaluationService(PolicyEvaluationService policyEvaluationService) {

        this.policyEvaluationService = policyEvaluationService;
    }

    public PolicyManagementService getPolicyManagementService() {

        return policyManagementService;
    }

    public void setPolicyManagementService(PolicyManagementService policyManagementService) {

        this.policyManagementService = policyManagementService;
    }

    public DeviceManagementService getDeviceManagementService() {

        return deviceManagementService;
    }

    public void setDeviceManagementService(DeviceManagementService deviceManagementService) {

        this.deviceManagementService = deviceManagementService;
    }

    public JsFunctionRegistry getJsFunctionRegistry() {

        return jsFunctionRegistry;
    }

    public void setJsFunctionRegistry(JsFunctionRegistry jsFunctionRegistry) {

        this.jsFunctionRegistry = jsFunctionRegistry;
    }

    /**
     * Returns the ClientAttestationService used to verify Google Play Integrity tokens.
     * May be null when the client-attestation-mgt bundle is not present (non-Android deployments).
     *
     * @return ClientAttestationService instance, or null if not registered.
     */
    public ClientAttestationService getClientAttestationService() {

        return clientAttestationService;
    }

    public void setClientAttestationService(ClientAttestationService clientAttestationService) {

        this.clientAttestationService = clientAttestationService;
    }

    public DevicePolicyEvaluator getDevicePolicyEvaluator() {

        return devicePolicyEvaluator;
    }

    public void setDevicePolicyEvaluator(DevicePolicyEvaluator devicePolicyEvaluator) {

        this.devicePolicyEvaluator = devicePolicyEvaluator;
    }

    public IntegrityDataEnricher getIntegrityDataEnricher() {

        return integrityDataEnricher;
    }

    public void setIntegrityDataEnricher(IntegrityDataEnricher integrityDataEnricher) {

        this.integrityDataEnricher = integrityDataEnricher;
    }
}
