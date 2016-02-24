/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;

import java.io.Serializable;

public class AuthenticatedIdPData implements Serializable {

    private static final long serialVersionUID = 5576595024956777804L;

    private String idpName;
    private AuthenticatorConfig authenticator;
    private AuthenticatedUser user;

    public String getIdpName() {
        return idpName;
    }

    public void setIdpName(String idpName) {
        this.idpName = idpName;
    }

    public AuthenticatedUser getUser() {
        return user;
    }

    public void setUser(AuthenticatedUser user) {
        this.user = user;
    }

    public AuthenticatorConfig getAuthenticator() {
        return authenticator;
    }

    public void setAuthenticator(AuthenticatorConfig authenticator) {
        this.authenticator = authenticator;
    }
}
