/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.handler.authz;

import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The interface to handle the authorization for applications. Custom authorization handlers should be written
 * implementing this interface.
 */
public interface AuthorizationHandler {

    /**
     * Checks whether the user is authorized.
     *
     * @param request  The HttpServletRequest from the user-agent
     * @param response The HttpServletResponse to the user-agent
     * @param context  The authorization context
     * @return <code>true</code> if the user is authorized, <code>false</code> otherwise
     */
    boolean isAuthorized(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationContext context);
}
