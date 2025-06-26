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

import org.wso2.carbon.identity.organization.management.service.model.BasicOrganization;

/**
 * This class represents the result of an organization discovery operation.
 */
public class OrganizationDiscoveryResult {

    private boolean successful;
    private BasicOrganization discoveredOrganization;
    private String failureReason;

    private OrganizationDiscoveryResult(Builder builder) {

        this.successful = builder.successful;
        this.discoveredOrganization = builder.discoveredOrganization;
        this.failureReason = builder.failureReason;
    }

    public boolean isSuccessful() {

        return successful;
    }

    public void setSuccessful(boolean successful) {

        this.successful = successful;
    }

    public BasicOrganization getDiscoveredOrganization() {

        return discoveredOrganization;
    }

    public void setDiscoveredOrganization(BasicOrganization discoveredOrganization) {

        this.discoveredOrganization = discoveredOrganization;
    }

    public String getFailureReason() {

        return failureReason;
    }

    public void setFailureReason(String failureReason) {

        this.failureReason = failureReason;
    }

    /**
     * Builder class for constructing OrganizationDiscoveryResult instances.
     */
    public static class Builder {

        private boolean successful;
        private BasicOrganization discoveredOrganization;
        private String failureReason;

        public Builder successful(boolean successful) {

            this.successful = successful;
            return this;
        }

        public Builder discoveredOrganization(BasicOrganization discoveredOrganization) {

            this.discoveredOrganization = discoveredOrganization;
            return this;
        }

        public Builder failureReason(String failureReason) {

            this.failureReason = failureReason;
            return this;
        }

        public OrganizationDiscoveryResult build() {

            return new OrganizationDiscoveryResult(this);
        }
    }
}
