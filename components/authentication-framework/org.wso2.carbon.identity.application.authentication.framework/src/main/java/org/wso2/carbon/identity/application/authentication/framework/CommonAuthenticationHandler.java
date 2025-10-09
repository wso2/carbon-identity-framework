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
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

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
     * Handles debug flow using OSGi service lookup.
     * 
     * @param request HttpServletRequest.
     * @param response HttpServletResponse.
     * @return true if debug flow was handled, false otherwise.
     */
    private boolean handleDebugFlow(HttpServletRequest request, HttpServletResponse response) {
        log.info("=== HANDLE DEBUG FLOW: Starting ===");
        
        try {
            // Get OSGi bundle context
            BundleContext bundleContext = IdentityTenantUtil.getBundleContext();
            if (bundleContext == null) {
                log.info("=== HANDLE DEBUG FLOW: Bundle context is null ===");
                return false;
            }
            
            // Look up debug service using the exact class name
            ServiceReference<?> serviceRef = bundleContext.getServiceReference(
                "org.wso2.carbon.identity.debug.framework.DebugService");
            
            if (serviceRef == null) {
                // List all available services for debugging
                try {
                    ServiceReference<?>[] allServices = bundleContext.getAllServiceReferences(null, null);
                    log.info("=== DEBUG FLOW: Available OSGi services count: " + 
                            (allServices != null ? allServices.length : 0) + " ===");
                    
                    if (allServices != null) {
                        int debugServiceCount = 0;
                        for (ServiceReference<?> sr : allServices) {
                            String[] objectClasses = (String[]) sr.getProperty("objectClass");
                            if (objectClasses != null) {
                                for (String className : objectClasses) {
                                    if (className.contains("debug") || className.contains("Debug")) {
                                        log.info("=== DEBUG FLOW: Found debug-related service: " + className + " ===");
                                        debugServiceCount++;
                                    }
                                }
                            }
                        }
                        log.info("=== DEBUG FLOW: Total debug-related services found: " + debugServiceCount + " ===");
                    }
                } catch (Exception e) {
                    log.info("=== DEBUG FLOW: Error listing services: " + e.getMessage() + " ===");
                }
                
                log.info("=== HANDLE DEBUG FLOW: Debug service not available ===");
                return false;
            }
            
            Object debugService = bundleContext.getService(serviceRef);
            if (debugService == null) {
                log.info("=== HANDLE DEBUG FLOW: Debug service instance is null ===");
                return false;
            }
            
            log.info("=== HANDLE DEBUG FLOW: Debug service found: " + debugService.getClass().getName() + " ===");
            
            // Use reflection to call the method (since we can't import the interface)
            java.lang.reflect.Method handleMethod = debugService.getClass().getMethod(
                "handleCommonAuthRequest", HttpServletRequest.class, HttpServletResponse.class);
            
            log.info("=== HANDLE DEBUG FLOW: Invoking handleCommonAuthRequest method ===");
            
            Object result = handleMethod.invoke(debugService, request, response);
            boolean handled = result instanceof Boolean && (Boolean) result;
            
            log.info("=== HANDLE DEBUG FLOW: Result = " + handled + " ===");
            
            // Release the service reference
            bundleContext.ungetService(serviceRef);
            
            return handled;
            
        } catch (Exception e) {
            log.error("Error handling debug flow via OSGi service lookup: " + e.getMessage(), e);
            return false;
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Enhanced logging to track all requests to CommonAuthenticationHandler
        String requestURI = request.getRequestURI();
        String queryString = request.getQueryString();
        log.info("=== COMMON AUTH HANDLER: Processing request ===");
        log.info("Request URI: " + requestURI);
        log.info("Query String: " + queryString);

        if (FrameworkUtils.getMaxInactiveInterval() == 0) {
            FrameworkUtils.setMaxInactiveInterval(request.getSession().getMaxInactiveInterval());
        }

        try {
            // First, check if this is a debug flow and handle it accordingly.
            log.info("=== CHECKING FOR DEBUG FLOW ===");
            boolean debugHandled = handleDebugFlow(request, response);
            log.info("Debug Flow Handled: " + debugHandled);
            
            if (debugHandled) {
                // Debug flow handled, return without proceeding to regular authentication.
                log.info("=== DEBUG FLOW PROCESSED - SKIPPING REGULAR AUTH ===");
                return;
            }

            // If not a debug flow, proceed with regular WSO2 authentication.
            log.info("=== PROCEEDING WITH REGULAR WSO2 AUTHENTICATION ===");
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
