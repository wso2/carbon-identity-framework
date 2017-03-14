/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.wso2.carbon.identity.gateway.common.model.idp;


import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
/**
 * IDPMetaData is a IDP model class.
 */
public class IDPMetaData {

    private List<IDPCertificate> certificates = new ArrayList<>();
    private String federationHub;
    private String homeRealm;
    private ClaimConfig claimConfig;
    private RoleConfig roleConfig;
    private Properties properties = new Properties();

    public List<IDPCertificate> getCertificates() {
        return certificates;
    }

    public void setCertificates(List<IDPCertificate> certificates) {
        this.certificates = certificates;
    }

    public ClaimConfig getClaimConfig() {
        return claimConfig;
    }

    public void setClaimConfig(ClaimConfig claimConfig) {
        this.claimConfig = claimConfig;
    }

    public String getFederationHub() {
        return federationHub;
    }

    public void setFederationHub(String federationHub) {
        this.federationHub = federationHub;
    }

    public String getHomeRealm() {
        return homeRealm;
    }

    public void setHomeRealm(String homeRealm) {
        this.homeRealm = homeRealm;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public RoleConfig getRoleConfig() {
        return roleConfig;
    }

    public void setRoleConfig(RoleConfig roleConfig) {
        this.roleConfig = roleConfig;
    }
}
