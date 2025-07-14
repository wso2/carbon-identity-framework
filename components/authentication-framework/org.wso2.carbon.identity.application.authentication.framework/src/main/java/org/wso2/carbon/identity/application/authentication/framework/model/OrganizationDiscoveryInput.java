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

/**
 * This class represents the input for organization discovery.
 */
public class OrganizationDiscoveryInput {

    private String orgId;
    private String orgHandle;
    private String orgName;
    private String loginHint;
    private String orgDiscoveryType;

    private OrganizationDiscoveryInput(Builder builder) {

        this.orgId = builder.orgId;
        this.orgHandle = builder.orgHandle;
        this.orgName = builder.orgName;
        this.loginHint = builder.loginHint;
        this.orgDiscoveryType = builder.orgDiscoveryType;
    }

    public String getOrgId() {

        return orgId;
    }

    public void setOrgId(String orgId) {

        this.orgId = orgId;
    }

    public String getOrgHandle() {

        return orgHandle;
    }

    public void setOrgHandle(String orgHandle) {

        this.orgHandle = orgHandle;
    }

    public String getOrgName() {

        return orgName;
    }

    public void setOrgName(String orgName) {

        this.orgName = orgName;
    }

    public String getLoginHint() {

        return loginHint;
    }

    public void setLoginHint(String loginHint) {

        this.loginHint = loginHint;
    }

    public String getOrgDiscoveryType() {

        return orgDiscoveryType;
    }

    public void setOrgDiscoveryType(String orgDiscoveryType) {

        this.orgDiscoveryType = orgDiscoveryType;
    }

    /**
     * Builder class for constructing OrganizationDiscoveryInput instances.
     */
    public static class Builder {

        private String orgId;
        private String orgHandle;
        private String orgName;
        private String loginHint;
        private String orgDiscoveryType;

        public Builder orgId(String orgId) {

            this.orgId = orgId;
            return this;
        }

        public Builder orgHandle(String orgHandle) {

            this.orgHandle = orgHandle;
            return this;
        }

        public Builder orgName(String orgName) {

            this.orgName = orgName;
            return this;
        }

        public Builder loginHint(String loginHint) {

            this.loginHint = loginHint;
            return this;
        }

        public Builder orgDiscoveryType(String orgDiscoveryType) {

            this.orgDiscoveryType = orgDiscoveryType;
            return this;
        }

        public OrganizationDiscoveryInput build() {

            return new OrganizationDiscoveryInput(this);
        }
    }
}
