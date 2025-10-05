/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.debug.framework;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.RequestCoordinator;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Debug-aware request coordinator that intercepts /commonauth requests to identify debug flows.
 * When a debug flow is detected (via isDebugFlow parameter), it routes the request to the debug framework.
 * Otherwise, it delegates to the default request coordinator.
 */
public class DebugAwareRequestCoordinator implements RequestCoordinator {

    private static final Log log = LogFactory.getLog(DebugAwareRequestCoordinator.class);
    private static final String DEBUG_FLOW_PARAM = "isDebugFlow";

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String isDebugFlow = request.getParameter(DEBUG_FLOW_PARAM);
        
        if ("true".equals(isDebugFlow)) {
            if (log.isDebugEnabled()) {
                log.debug("Debug flow detected in /commonauth request. Routing to debug RequestCoordinator.");
            }
            
            handleDebugFlow(request, response);
        } else {
            // Delegate to default request coordinator for normal flows
            RequestCoordinator defaultCoordinator = FrameworkUtils.getRequestCoordinator();
            defaultCoordinator.handle(request, response);
        }
    }

    /**
     * Handles debug authentication flows by routing to the debug framework's RequestCoordinator.
     *
     * @param request  The HTTP servlet request.
     * @param response The HTTP servlet response.
     * @throws IOException If an error occurs during debug flow processing.
     */
    private void handleDebugFlow(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {

        try {
            // Get the debug framework's RequestCoordinator
            org.wso2.carbon.identity.debug.framework.RequestCoordinator debugRequestCoordinator = 
                    getDebugRequestCoordinator();
            
            if (debugRequestCoordinator != null) {
                debugRequestCoordinator.handle(request, response);
            } else {
                log.error("Debug RequestCoordinator not available. Falling back to default flow.");
                RequestCoordinator defaultCoordinator = FrameworkUtils.getRequestCoordinator();
                defaultCoordinator.handle(request, response);
            }
        } catch (Exception e) {
            log.error("Error occurred while handling debug flow.", e);
            throw new IOException("Error in debug flow handling", e);
        }
    }

    /**
     * Gets the debug framework's RequestCoordinator instance.
     *
     * @return The debug RequestCoordinator instance.
     */
    private org.wso2.carbon.identity.debug.framework.RequestCoordinator getDebugRequestCoordinator() {

        // For now, return a new instance of the debug RequestCoordinator
        // This could be enhanced to use a cached instance or service lookup
        return new org.wso2.carbon.identity.debug.framework.RequestCoordinator();
    }
}
