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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.listener;

import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.SessionContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Session Context Management listener to call the specific inbound components. The inbound components can use this
 * listener to insert any extra key-value to the session object.
 */
public interface SessionContextMgtListener {

    /**
     * Framework will select one listener based on the inbound type returned by this.
     *
     * @return Inbound Type.
     */
    String getInboundType();

    /**
     * Pre Creation Session in the framework.
     *
     * @param sessionContextKey session Context Key.
     * @param request           Http Servlet Request.
     * @param response          Http Servlet Response.
     * @param context           Authentication Context.
     * @return                  Map of key-values to be added to the session object.
     */
    Map<String, String> onPreCreateSession(String sessionContextKey, HttpServletRequest request,
                                           HttpServletResponse response, AuthenticationContext context);

    Map<String, String> onPreUpdateSession(String sessionContextKey, HttpServletRequest request,
                                           HttpServletResponse response, AuthenticationContext context);
}
