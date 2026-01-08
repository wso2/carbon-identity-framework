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

    /**
     * Handles debug flow by loading DebugRequestCoordinator via reflection.
     * This avoids hard dependency on debug-framework module while allowing
     * debug flow processing if the framework is available.
     * 
     * @param request HttpServletRequest.
     * @param response HttpServletResponse.
     * @return true if debug flow was handled, false otherwise.
     */
    private boolean handleDebugFlow(HttpServletRequest request, HttpServletResponse response) {        
        try {
            // Load DebugRequestCoordinator via reflection to avoid hard dependency
            Class<?> debugCoordinatorClass = Class.forName(
                    "org.wso2.carbon.identity.debug.framework.core.DebugRequestCoordinator");
            
            // Create instance of DebugRequestCoordinator
            Object debugCoordinator = debugCoordinatorClass.getDeclaredConstructor().newInstance();
            
            // Get the handleCommonAuthRequest method
            java.lang.reflect.Method handleMethod = debugCoordinatorClass.getMethod(
                    "handleCommonAuthRequest",
                    HttpServletRequest.class,
                    HttpServletResponse.class);
            
            // Invoke the method and get result
            Object result = handleMethod.invoke(debugCoordinator, request, response);
            
            if (result instanceof Boolean) {
                boolean handled = (Boolean) result;
                if (handled && log.isDebugEnabled()) {
                    log.debug("Debug flow callback processed by DebugRequestCoordinator");
                }
                return handled;
            }
            
            return false;
            
        } catch (ClassNotFoundException e) {
            // Debug framework not available - this is OK, debug flows won't be supported
            if (log.isDebugEnabled()) {
                log.debug("DebugRequestCoordinator not available (debug-framework not deployed)");
            }
            return false;
        } catch (java.lang.reflect.InvocationTargetException e) {
            // Exception in debug processing
            log.error("Error in debug flow processing: " + 
                (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()),
                e.getCause() != null ? e.getCause() : e);
            return false;
        } catch (Exception e) {
            log.error("Error discovering/invoking DebugRequestCoordinator: " + e.getMessage(), e);
            return false;
        }
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
