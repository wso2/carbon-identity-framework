/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Intercepts incoming common auth requests before the regular framework flow.
 * Implementations can short-circuit handling by returning {@code true}.
 */
public interface DebugAuthenticationInterceptor {

    /**
     * Checks whether this interceptor can handle the given debug request.
     * Implementations must inspect the request (e.g. a specific parameter or session key) to
     * determine whether they are the correct handler, avoiding unintended interception of
     * requests meant for other interceptors and disruption of the regular authentication flow.
     *
     * @param request HttpServletRequest.
     * @return {@code true} if this interceptor can handle the given debug request.
     */
    boolean canHandle(HttpServletRequest request);

    /**
     * Handles an incoming common auth request.
     *
     * @param request HttpServletRequest.
     * @param response HttpServletResponse.
     * @return {@code true} if the request was handled and the regular flow should be skipped.
     */
    boolean handleCommonAuthRequest(HttpServletRequest request, HttpServletResponse response);
}
