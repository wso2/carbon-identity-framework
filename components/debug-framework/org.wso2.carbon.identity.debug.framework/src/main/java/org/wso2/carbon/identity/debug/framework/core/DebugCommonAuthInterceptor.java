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

import org.wso2.carbon.identity.application.authentication.framework.DebugAuthenticationInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Intercepts common auth requests and delegates debug callback handling to the coordinator.
 */
public class DebugCommonAuthInterceptor implements DebugAuthenticationInterceptor {

    private final DebugRequestCoordinator debugRequestCoordinator;

    public DebugCommonAuthInterceptor(DebugRequestCoordinator debugRequestCoordinator) {

        this.debugRequestCoordinator = debugRequestCoordinator;
    }

    @Override
    public boolean handleCommonAuthRequest(HttpServletRequest request, HttpServletResponse response) {

        return debugRequestCoordinator.handleCallbackRequest(request, response);
    }
}
