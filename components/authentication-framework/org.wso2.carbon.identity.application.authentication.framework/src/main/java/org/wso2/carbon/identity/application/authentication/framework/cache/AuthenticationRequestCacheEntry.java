/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.application.authentication.framework.cache;

import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticationRequest;
import org.wso2.carbon.identity.application.common.cache.CacheEntry;

/**
 * keeps an instance of AuthenticationRequest which has all data
 * added from calling servlets. (query params are removed from request and added to this
 * context object when calling to authentication endpoint)
 */
public class AuthenticationRequestCacheEntry extends CacheEntry {

    private static final long serialVersionUID = 358933465378244386L;
    //AuthenticationRequest keep all the data related to
    private AuthenticationRequest authenticationRequest;

    public AuthenticationRequestCacheEntry(AuthenticationRequest requestContext) {
        this.authenticationRequest = requestContext;
    }

    public AuthenticationRequest getAuthenticationRequest() {
        return authenticationRequest;
    }

    public void setAuthenticationRequest(AuthenticationRequest
                                                 authenticationRequest) {
        this.authenticationRequest = authenticationRequest;
    }

}
