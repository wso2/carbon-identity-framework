/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.model;

import org.wso2.carbon.identity.organization.management.service.model.Organization;

/**
 * This class represents the result of an organization discovery operation.
 */
public class OrganizationDiscoveryResult {

    private boolean successful;
    private Organization discoveredOrganization;
    private String sharedApplicationId;
    private FailureDetails failureDetails;

    /**
     * Factory method to create a successful organization discovery result.
     *
     * @param organization        The discovered organization.
     * @param sharedApplicationId The shared application id.
     * @return A successful OrganizationDiscoveryResult instance.
     */
    public static OrganizationDiscoveryResult success(Organization organization, String sharedApplicationId) {

        OrganizationDiscoveryResult result = new OrganizationDiscoveryResult();
        result.setSuccessful(true);
        result.setDiscoveredOrganization(organization);
        result.setSharedApplicationId(sharedApplicationId);
        return result;
    }

    /**
     * Factory method to create a failed organization discovery result.
     *
     * @param code    The failure code.
     * @param message The failure message.
     * @return A failed OrganizationDiscoveryResult instance.
     */
    public static OrganizationDiscoveryResult failure(String code, String message) {

        OrganizationDiscoveryResult result = new OrganizationDiscoveryResult();
        result.setSuccessful(false);
        result.setFailureDetails(new FailureDetails(code, message));
        return result;
    }

    public boolean isSuccessful() {

        return successful;
    }

    public void setSuccessful(boolean successful) {

        this.successful = successful;
    }

    public Organization getDiscoveredOrganization() {

        return discoveredOrganization;
    }

    public void setDiscoveredOrganization(Organization discoveredOrganization) {

        this.discoveredOrganization = discoveredOrganization;
    }

    public String getSharedApplicationId() {

        return sharedApplicationId;
    }

    public void setSharedApplicationId(String sharedApplicationId) {

        this.sharedApplicationId = sharedApplicationId;
    }

    public FailureDetails getFailureDetails() {

        return failureDetails;
    }

    public void setFailureDetails(FailureDetails failureDetails) {

        this.failureDetails = failureDetails;
    }

    /**
     * Represents the details of a failure in organization discovery.
     */
    public static class FailureDetails {

        private String code;
        private String message;

        public FailureDetails(String code, String message) {

            this.code = code;
            this.message = message;
        }

        public String getCode() {

            return code;
        }

        public void setCode(String code) {

            this.code = code;
        }

        public String getMessage() {

            return message;
        }

        public void setMessage(String message) {

            this.message = message;
        }
    }
}
