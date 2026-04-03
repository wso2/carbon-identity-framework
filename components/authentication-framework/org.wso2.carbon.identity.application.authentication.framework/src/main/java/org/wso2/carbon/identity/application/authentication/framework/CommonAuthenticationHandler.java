/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.exception.CookieValidationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.UserAssertionFailedException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkErrorConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Common authentication handler.
 */
public class CommonAuthenticationHandler {

    private static final Log log = LogFactory.getLog(CommonAuthenticationHandler.class);

    public CommonAuthenticationHandler() {
        ConfigurationFacade.getInstance();
    }

    private boolean handleDebugFlow(HttpServletRequest request, HttpServletResponse response) {

        for (DebugAuthenticationInterceptor interceptor : FrameworkServiceDataHolder.getInstance()
                .getDebugAuthenticationInterceptors()) {
            try {
                if (interceptor.handleCommonAuthRequest(request, response)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Common auth request handled by debug interceptor: "
                                + interceptor.getClass().getName());
                    }
                    return true;
                }
            } catch (Exception e) {
                log.warn("Error while executing debug authentication interceptor: "
                        + interceptor.getClass().getName() + ". Continuing with normal flow.", e);
            }
        }
        return false;
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (FrameworkUtils.getMaxInactiveInterval() == 0) {
            FrameworkUtils.setMaxInactiveInterval(request.getSession().getMaxInactiveInterval());
        }

        try {
            boolean debugHandled = handleDebugFlow(request, response);
            
            if (debugHandled) {
                // Debug flow handled, return without proceeding to regular authentication.
                log.info("Debug flow handled by DebugService.");
                return;
            }

            // If not a debug flow, proceed with regular WSO2 authentication.
            FrameworkUtils.getRequestCoordinator().handle(request, response);
        } catch (CookieValidationFailedException e) {

            if (!FrameworkErrorConstants.ErrorMessages.ERROR_MISMATCHING_TENANT_DOMAIN.getCode()
                    .equals(e.getErrorCode())) {
                log.warn("Session nonce cookie validation has failed for the sessionDataKey: "
                        + request.getParameter("sessionDataKey") + ". Hence, restarting the login flow.");
            }
            FrameworkUtils.getRequestCoordinator().handle(request, response);
        } catch (UserAssertionFailedException e) {

            FrameworkUtils.getRequestCoordinator().handle(request, response);
        } catch (Exception e) {
            // Handle any exceptions from debug coordinator.
            log.error("Exception in CommonAuthenticationHandler: " + e.getMessage(), e);
            // Fallback to regular authentication if debug processing fails.
            FrameworkUtils.getRequestCoordinator().handle(request, response);
        }
    }
}
