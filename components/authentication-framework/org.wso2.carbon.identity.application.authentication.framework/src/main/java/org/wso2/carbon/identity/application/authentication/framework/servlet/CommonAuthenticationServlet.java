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

package org.wso2.carbon.identity.application.authentication.framework.servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationRequestCacheEntry;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.CookieValidationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.model.CommonAuthRequestWrapper;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to handle common authentication requests.
 */
public class CommonAuthenticationServlet extends HttpServlet {

    private static final long serialVersionUID = -7182121722709941646L;
    private static final Log log = LogFactory.getLog(CommonAuthenticationServlet.class);

    @Override
    public void init() {
        // TODO move ConfigurationFacade initialization
        ConfigurationFacade.getInstance();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            if (FrameworkUtils.getMaxInactiveInterval() == 0) {
                FrameworkUtils.setMaxInactiveInterval(request.getSession().getMaxInactiveInterval());
            }
            FrameworkUtils.getRequestCoordinator().handle(request, response);
        } catch (CookieValidationFailedException e) {

            log.warn("Session nonce cookie validation has failed. Hence, restarting the login flow.");

            AuthenticationContext context = FrameworkUtils.getContextData(request);
            String callerSessionDataKey = context.getCallerSessionKey();
            AuthenticationRequestCacheEntry authRequest = FrameworkUtils.getAuthenticationRequestFromCache
                    (callerSessionDataKey);

            CommonAuthRequestWrapper requestWrapper = new CommonAuthRequestWrapper(request);
            requestWrapper.setParameter(FrameworkConstants.RequestParams.TYPE, context.getRequestType());
            requestWrapper.setParameter(FrameworkConstants.CALLER_SESSION_DATA_KEY, callerSessionDataKey);
            requestWrapper.setAttribute(FrameworkConstants.RequestAttribute.AUTH_REQUEST, authRequest);

            //set sessionDataKey to the request if it is not set
            if (request.getParameter(FrameworkConstants.SESSION_DATA_KEY) == null) {
                requestWrapper.setParameter(FrameworkConstants.SESSION_DATA_KEY, callerSessionDataKey);
            }

            doPost(requestWrapper, response);
        }
    }
}
