/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.identity.client.attestation.mgt.model;

import org.wso2.carbon.identity.client.attestation.mgt.utils.Constants;
import org.wso2.carbon.identity.core.bean.context.MessageContext;


/**
 * The object which will contain context information which are passed through API based Authentication process.
 * All information related to client attestation will be available in this context including the attestation
 * status, attested client information, type and errors.
 */
public class ClientAttestationContext extends MessageContext {

    private static final long serialVersionUID = 1995051819950410L;

    private String clientId;
    private String tenantDomain;
    private boolean apiBasedAuthenticationEnabled;
    private boolean attestationEnabled;
    private boolean isAttested;
    private Constants.ClientTypes clientType;
    private String errorMessage;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    public boolean isApiBasedAuthenticationEnabled() {
        return apiBasedAuthenticationEnabled;
    }

    public void setApiBasedAuthenticationEnabled(boolean apiBasedAuthenticationEnabled) {
        this.apiBasedAuthenticationEnabled = apiBasedAuthenticationEnabled;
    }

    public boolean isAttestationEnabled() {
        return attestationEnabled;
    }

    public void setAttestationEnabled(boolean attestationEnabled) {
        this.attestationEnabled = attestationEnabled;
    }

    public boolean isAttested() {
        return isAttested;
    }

    public void setAttested(boolean attested) {
        isAttested = attested;
    }

    public Constants.ClientTypes getClientType() {
        return clientType;
    }

    public void setClientType(Constants.ClientTypes clientType) {
        this.clientType = clientType;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "ClientAttestationContext{" +
                "clientId='" + clientId + '\'' +
                ", tenantDomain='" + tenantDomain + '\'' +
                ", apiBasedAuthenticationEnabled=" + apiBasedAuthenticationEnabled +
                ", attestationEnabled=" + attestationEnabled +
                ", isAttested=" + isAttested +
                ", clientType=" + clientType +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
