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

package org.wso2.carbon.identity.device.policy.api.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the structured result of evaluating a device policy against device data.
 */
public class DevicePolicyEvaluationResult {

    /**
     * Evaluation outcome status.
     */
    public enum Status {
        COMPLIANT,
        NON_COMPLIANT,
        INCOMPLETE_DEVICE_DATA
    }

    private final Status status;
    private final String policyName;
    private final List<String> failedFields;
    private final List<String> missingFields;

    private DevicePolicyEvaluationResult(Status status, String policyName,
            List<String> failedFields, List<String> missingFields) {

        this.status = status;
        this.policyName = policyName;
        this.failedFields = failedFields != null ? Collections.unmodifiableList(new ArrayList<>(failedFields))
                : Collections.emptyList();
        this.missingFields = missingFields != null ? Collections.unmodifiableList(new ArrayList<>(missingFields))
                : Collections.emptyList();
    }

    /**
     * Creates a {@link DevicePolicyEvaluationResult} representing a compliant evaluation result.
     *
     * @param policyName Name of the evaluated policy.
     * @return Compliant result.
     */
    public static DevicePolicyEvaluationResult compliant(String policyName) {

        return new DevicePolicyEvaluationResult(Status.COMPLIANT, policyName, null, null);
    }

    /**
     * Creates a {@link DevicePolicyEvaluationResult} representing a non-compliant evaluation result.
     *
     * @param policyName   Name of the evaluated policy.
     * @param failedFields List of field names that failed policy evaluation.
     * @return Non-compliant result.
     */
    public static DevicePolicyEvaluationResult nonCompliant(String policyName, List<String> failedFields) {

        return new DevicePolicyEvaluationResult(Status.NON_COMPLIANT, policyName, failedFields, null);
    }

    /**
     * Creates a {@link DevicePolicyEvaluationResult} representing an incomplete device data result.
     *
     * @param policyName    Name of the evaluated policy.
     * @param missingFields List of required field names absent from the device data.
     * @return Incomplete device data result.
     */
    public static DevicePolicyEvaluationResult incompleteDeviceData(String policyName, List<String> missingFields) {

        return new DevicePolicyEvaluationResult(Status.INCOMPLETE_DEVICE_DATA, policyName, null, missingFields);
    }

    /**
     * Returns the status of the policy evaluation.
     *
     * @return Evaluation {@link Status}.
     */
    public Status getStatus() {

        return status;
    }

    /**
     * Returns the name of the evaluated policy.
     *
     * @return Policy name string.
     */
    public String getPolicyName() {

        return policyName;
    }

    /**
     * Returns the list of fields that failed policy evaluation.
     *
     * @return Unmodifiable list of failed field names, empty if not NON_COMPLIANT.
     */
    public List<String> getFailedFields() {

        return failedFields;
    }

    /**
     * Returns the list of required fields absent from the device data.
     *
     * @return Unmodifiable list of missing field names, empty if not INCOMPLETE_DEVICE_DATA.
     */
    public List<String> getMissingFields() {

        return missingFields;
    }

    /**
     * Returns true if the device is compliant with the policy.
     *
     * @return {@code true} if status is COMPLIANT, {@code false} otherwise.
     */
    public boolean isCompliant() {

        return status == Status.COMPLIANT;
    }
}
