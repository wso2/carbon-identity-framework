/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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
 * Represents a session of a federated user
 *
 * IdPSessionId
 * SessionId
 * IdPName
 * AuthenticatorName
 * ProtocolType
 */
public class FederatedUserSession {

    private String idPSessionId;
    private String sessionId;
    private String idPName;
    private String authenticatorName;
    private String protocolType;

    public FederatedUserSession() {

    }

    public FederatedUserSession(String idPSessionId, String sessionId, String idPName, String authenticatorName,
                                String protocolType) {

        this.idPSessionId = idPSessionId;
        this.sessionId = sessionId;
        this.idPName = idPName;
        this.authenticatorName = authenticatorName;
        this.protocolType = protocolType;
    }

    public String getIdPSessionId() {

        return idPSessionId;
    }

    public void setIdPSessionId(String idPSessionId) {

        this.idPSessionId = idPSessionId;
    }

    public String getSessionId() {

        return sessionId;
    }

    public void setSessionId(String sessionId) {

        this.sessionId = sessionId;
    }

    public String getIdPName() {

        return idPName;
    }

    public void setIdPName(String idPName) {

        this.idPName = idPName;
    }

    public String getAuthenticatorName() {

        return authenticatorName;
    }

    public void setAuthenticatorName(String authenticatorName) {

        this.authenticatorName = authenticatorName;
    }

    public String getProtocolType() {

        return protocolType;
    }

    public void setProtocolType(String protocolType) {

        this.protocolType = protocolType;
    }
}
