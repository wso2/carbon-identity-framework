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
import org.wso2.carbon.identity.application.authentication.framework.AuthenticationInterceptor;
import org.wso2.carbon.identity.debug.framework.DebugFrameworkConstants;
import org.wso2.carbon.identity.debug.framework.extension.DebugInterceptor;
import org.wso2.carbon.identity.debug.framework.internal.DebugFrameworkServiceDataHolder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Intercepts /commonauth requests and delegates debug callbacks to the coordinator.
 */
public class DebugCommonAuthInterceptor implements DebugInterceptor, AuthenticationInterceptor {

    private static final Log LOG = LogFactory.getLog(DebugCommonAuthInterceptor.class);

    @Override
    public boolean canHandle(HttpServletRequest request) {

        String state = request.getParameter(DebugFrameworkConstants.CALLBACK_STATE_PARAM);
        return StringUtils.isNotBlank(state) && state.startsWith(DebugFrameworkConstants.DEBUG_PREFIX);
    }

    @Override
    public boolean handle(HttpServletRequest request, HttpServletResponse response) {

        DebugRequestCoordinator coordinator =
                DebugFrameworkServiceDataHolder.getInstance().getDebugRequestCoordinator();
                
        return coordinator.handleCallbackRequest(request, response);
    }
}
