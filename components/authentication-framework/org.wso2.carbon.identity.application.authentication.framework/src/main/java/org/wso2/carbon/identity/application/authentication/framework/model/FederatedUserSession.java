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
 * FederatedUserSession is the class that represents a session object of a federated user
 *
 * IdPSessionId
 * SessionId
 * IdPName
 * AuthenticatorName
 * ProtocolType
 */
public class FederatedUserSession {

    public FederatedUserSession() {

    }

    private String IdPSessionId;
    private String SessionId;
    private String IdPName;
    private String AuthenticatorName;
    private String ProtocolType;

    public String getIdPSessionId() {

        return IdPSessionId;
    }

    public void setIdPSessionId(String idPSessionId) {

        this.IdPSessionId = idPSessionId;
    }

    public String getSessionId() {

        return SessionId;
    }

    public void setSessionId(String sessionId) {

        this.SessionId = sessionId;
    }

    public String getIdPName() {

        return IdPName;
    }

    public void setIdPName(String idPName) {

        this.IdPName = idPName;
    }

    public String getAuthenticatorName() {

        return AuthenticatorName;
    }

    public void setAuthenticatorName(String authenticatorName) {

        this.AuthenticatorName = authenticatorName;
    }

    public String getProtocolType() {

        return ProtocolType;
    }

    public void setProtocolType(String protocolType) {

        this.ProtocolType = protocolType;
    }
}
