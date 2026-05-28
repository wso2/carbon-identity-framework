/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.debug.idp.core;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.debug.framework.DebugFrameworkConstants;
import org.wso2.carbon.identity.debug.framework.core.DebugProcessor;
import org.wso2.carbon.identity.debug.framework.exception.DebugFrameworkServerException;
import org.wso2.carbon.identity.debug.framework.model.DebugContext;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Abstract base for IdP-specific debug processors.
 * Owns the redirect-based callback template and the connectionId concept.
 */
public abstract class IdpDebugProcessor extends DebugProcessor {

    private static final Log LOG = LogFactory.getLog(IdpDebugProcessor.class);

    /**
     * Processes the debug flow callback from an external IdP.
     * Drives the redirect-based callback lifecycle: authenticate, extract data, build result, send response.
     *
     * @param request      Incoming HTTP request containing callback parameters.
     * @param response     HTTP response for sending results.
     * @param debugContext Debug context for storing and retrieving debug state.
     * @throws DebugFrameworkServerException If a server-side error occurs while processing the callback.
     */
    public void processCallback(HttpServletRequest request, HttpServletResponse response,
            DebugContext debugContext) throws DebugFrameworkServerException {

        String state = resolveState(request, debugContext);
        String connectionId = extractConnectionId(debugContext);

        if (!processAuthentication(request, debugContext, response, state, connectionId)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Authentication processing failed for state: " + state + ". Sending debug response.");
            }
            sendDebugResponse(response, state, connectionId);
            return;
        }

        Map<String, Object> claims = extractClaims(debugContext, state);
        if (claims == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Claim extraction failed for state: " + state + ". Sending debug response.");
            }
            sendDebugResponse(response, state, connectionId);
            return;
        }

        buildAndCacheDebugResult(debugContext, state, claims);
        sendDebugResponse(response, state, connectionId);
    }

    private String resolveState(HttpServletRequest request, DebugContext debugContext) {

        String state = request.getParameter(DebugFrameworkConstants.CALLBACK_STATE_PARAM);
        if (StringUtils.isBlank(state)) {
            state = (String) debugContext.getProperty(DebugFrameworkConstants.DEBUG_PROTOCOL_STATE);
        }
        if (StringUtils.isBlank(state)) {
            state = DebugFrameworkConstants.UNKNOWN_DEBUG_STATE;
        }
        return state;
    }

    /**
     * Extracts the connection ID from the debug context as the resource identifier.
     */
    protected String extractConnectionId(DebugContext debugContext) {

        String connectionId = (String) debugContext.getProperty(IdpDebugConstants.CONNECTION_ID);
        return connectionId != null ? connectionId : "";
    }

    protected abstract boolean processAuthentication(HttpServletRequest request, DebugContext debugContext,
            HttpServletResponse response, String state, String resourceIdentifier) throws DebugFrameworkServerException;

    protected abstract Map<String, Object> extractClaims(DebugContext debugContext, String state);

    protected abstract void buildAndCacheDebugResult(DebugContext debugContext, String state,
            Map<String, Object> claims) throws DebugFrameworkServerException;

    protected abstract void sendDebugResponse(HttpServletResponse response, String state,
            String resourceIdentifier) throws DebugFrameworkServerException;
}
