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

package org.wso2.carbon.identity.debug.framework.core;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.DebugAuthenticationInterceptor;
import org.wso2.carbon.identity.debug.framework.DebugFrameworkConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Intercepts common auth requests and delegates debug callback handling to the coordinator.
 */
public class DebugCommonAuthInterceptor implements DebugAuthenticationInterceptor {

    private static final Log LOG = LogFactory.getLog(DebugCommonAuthInterceptor.class);
    private final DebugRequestCoordinator debugRequestCoordinator;

    public DebugCommonAuthInterceptor(DebugRequestCoordinator debugRequestCoordinator) {

        this.debugRequestCoordinator = debugRequestCoordinator;
    }

    @Override
    public boolean isDebugRequest(HttpServletRequest request) {

        if (Boolean.parseBoolean(request.getParameter(DebugFrameworkConstants.DEBUG_IDENTIFIER_PARAM))) {
            return true;
        }

        String sessionDataKey = request.getParameter(DebugFrameworkConstants.SESSION_DATA_KEY_PARAM);
        if (StringUtils.isNotBlank(sessionDataKey) && sessionDataKey.startsWith(DebugFrameworkConstants.DEBUG_PREFIX)) {
            return true;
        }

        String state = request.getParameter(DebugFrameworkConstants.CALLBACK_STATE_PARAM);
        return StringUtils.isNotBlank(state) && state.startsWith(DebugFrameworkConstants.DEBUG_PREFIX);
    }

    @Override
    public boolean handleCommonAuthRequest(HttpServletRequest request, HttpServletResponse response) {

        return debugRequestCoordinator.handleCallbackRequest(request, response);
    }
}
