/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Service interface for handling debug authentication flows.
 * Implementations should handle /commonauth requests and route debug flows appropriately.
 */
public interface DebugService {

    /**
     * Checks if the given request represents a debug authentication flow.
     *
     * @param request HttpServletRequest to inspect.
     * @return true if this is a debug flow, false otherwise.
     */
    boolean isDebugFlow(HttpServletRequest request);

    /**
     * Handles a /commonauth request, routing debug flows to protocol-specific processors.
     * This method should be called from the main WSO2 /commonauth handler to intercept debug requests.
     *
     * @param request HttpServletRequest from /commonauth endpoint.
     * @param response HttpServletResponse.
     * @return true if request was handled by debug processor, false if it should be handled by regular flow.
     * @throws IOException If processing fails.
     */
    boolean handleCommonAuthRequest(HttpServletRequest request, HttpServletResponse response) throws IOException;
}
