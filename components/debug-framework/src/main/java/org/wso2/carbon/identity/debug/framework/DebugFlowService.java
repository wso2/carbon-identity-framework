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
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Main service for executing debug flows following the architecture:
 * API Layer -> ContextProvider -> Executer -> FederatedIdP -> /commonauth -> RequestCoordinator -> Processor.
 * 
 * This service orchestrates the entire debug flow as specified in the architecture diagram.
 */
public class DebugFlowService {

    private static final Log LOG = LogFactory.getLog(DebugFlowService.class);

    private final ContextProvider contextProvider;
    private final Executer executer;
    private final RequestCoordinator requestCoordinator;
    private final Processor processor;

    /**
     * Constructor initializes all debug framework components.
     */
    public DebugFlowService() {
        this.contextProvider = new ContextProvider();
        this.executer = new Executer();
        this.requestCoordinator = new RequestCoordinator();
        this.processor = new Processor();
    }

    /**
     * Executes the complete debug flow as per the architecture:
     * 1. ContextProvider - Create context with IdP id and other relevant data
     * 2. Executer - Invoke authenticator, send to FederatedIdP and callback to /commonauth
     * 3. RequestCoordinator - Process callback from /commonauth with debug identifier
     * 4. Processor - Process the result and send to client
     *
     * @param idp Identity Provider object from idp-mgt.
     * @param authenticatorName Authenticator name to use.
     * @param username Username for authentication.
     * @param password Password for authentication.
     * @param sessionDataKey Session data key.
     * @param request HttpServletRequest.
     * @param response HttpServletResponse.
     * @return Map containing debug flow results.
     */
    public Map<String, Object> executeDebugFlow(IdentityProvider idp, String authenticatorName, 
                                               String username, String password, String sessionDataKey,
                                               HttpServletRequest request, HttpServletResponse response) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting debug flow execution for IdP: " + 
                     (idp != null ? idp.getIdentityProviderName() : "null"));
        }

        Map<String, Object> debugResults = new HashMap<>();
        String debugSessionId = generateDebugSessionId();
        
        debugResults.put("debugSessionId", debugSessionId);
        debugResults.put("sessionDataKey", sessionDataKey);
        debugResults.put("timestamp", System.currentTimeMillis());

        try {
            // STEP 1: Use ContextProvider to create context with IdP id and other relevant data
            if (LOG.isDebugEnabled()) {
                LOG.debug("Step 1: Creating authentication context using ContextProvider");
            }
            
            String idpResourceId = idp != null ? idp.getResourceId() : null;
            AuthenticationContext context = contextProvider.provideContext(request, idpResourceId, authenticatorName);
            
            // Set additional context for debug flow
            context.setProperty("DEBUG_SESSION_ID", debugSessionId);
            context.setProperty("DEBUG_USERNAME", username);
            context.setProperty("DEBUG_PASSWORD", password);
            context.setProperty("DEBUG_HTTP_REQUEST", request);
            context.setProperty("DEBUG_HTTP_RESPONSE", response);

            debugResults.put("contextCreated", true);
            debugResults.put("contextIdentifier", context.getContextIdentifier());

            // STEP 2: Use Executer to invoke authenticator and send to FederatedIdP
            if (LOG.isDebugEnabled()) {
                LOG.debug("Step 2: Executing authentication using Executer");
            }

            boolean executionStarted = executer.execute(idp, context);
            debugResults.put("executionStarted", executionStarted);

            if (executionStarted) {
                // STEP 3: Simulate callback processing (RequestCoordinator handles /commonauth callbacks)
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Step 3: Processing authentication callback using RequestCoordinator");
                }

                // In real implementation, callback happens asynchronously via /commonauth
                // For debug flow, we simulate successful callback processing
                requestCoordinator.coordinate(context, request, response);
                debugResults.put("callbackProcessed", true);

                // STEP 4: Use Processor to process the result and prepare response
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Step 4: Processing results using Processor");
                }

                Object processedResult = processor.process(context);
                debugResults.put("processedResult", processedResult);
                debugResults.put("status", "SUCCESS");

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Debug flow completed successfully");
                }

            } else {
                debugResults.put("status", "EXECUTION_FAILED");
                debugResults.put("error", "Failed to start authentication execution");
                LOG.error("Debug flow execution failed to start");
            }

        } catch (Exception e) {
            LOG.error("Error in debug flow execution: " + e.getMessage(), e);
            debugResults.put("status", "ERROR");
            debugResults.put("error", e.getMessage());
            debugResults.put("executionStarted", false);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Debug flow execution completed with status: " + debugResults.get("status"));
        }

        return debugResults;
    }

    /**
     * Executes a simplified debug flow for connection testing.
     *
     * @param idp Identity Provider object.
     * @param authenticatorName Authenticator name.
     * @return Map containing connection test results.
     */
    public Map<String, Object> testConnection(IdentityProvider idp, String authenticatorName) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting connection test for IdP: " + 
                     (idp != null ? idp.getIdentityProviderName() : "null"));
        }

        Map<String, Object> testResults = new HashMap<>();
        String debugSessionId = generateDebugSessionId();
        
        testResults.put("debugSessionId", debugSessionId);
        testResults.put("testType", "CONNECTION_TEST");
        testResults.put("timestamp", System.currentTimeMillis());

        try {
            // Basic IdP configuration validation
            if (idp == null) {
                testResults.put("status", "FAILURE");
                testResults.put("error", "Identity Provider is null");
                return testResults;
            }

            // Check if IdP has authenticator configurations
            if (idp.getFederatedAuthenticatorConfigs() == null || 
                idp.getFederatedAuthenticatorConfigs().length == 0) {
                testResults.put("status", "FAILURE");
                testResults.put("error", "No authenticator configurations found for IdP");
                return testResults;
            }

            // If specific authenticator is specified, verify it exists
            if (authenticatorName != null) {
                boolean authenticatorFound = false;
                for (org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig config : 
                     idp.getFederatedAuthenticatorConfigs()) {
                    if (authenticatorName.equals(config.getName())) {
                        authenticatorFound = true;
                        break;
                    }
                }
                
                if (!authenticatorFound) {
                    testResults.put("status", "FAILURE");
                    testResults.put("error", "Specified authenticator not found: " + authenticatorName);
                    return testResults;
                }
            }

            testResults.put("status", "SUCCESS");
            testResults.put("idpName", idp.getIdentityProviderName());
            testResults.put("authenticatorsAvailable", idp.getFederatedAuthenticatorConfigs().length);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Connection test completed successfully for IdP: " + idp.getIdentityProviderName());
            }

        } catch (Exception e) {
            LOG.error("Error in connection test: " + e.getMessage(), e);
            testResults.put("status", "ERROR");
            testResults.put("error", e.getMessage());
        }

        return testResults;
    }

    /**
     * Generates a unique debug session ID.
     *
     * @return Debug session ID.
     */
    private String generateDebugSessionId() {
        return "debug-session-" + System.currentTimeMillis() + "-" + 
               java.util.UUID.randomUUID().toString().substring(0, 8);
    }
}
