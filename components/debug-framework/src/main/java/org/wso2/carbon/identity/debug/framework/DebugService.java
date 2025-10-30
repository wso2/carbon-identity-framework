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

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/**
 * Service interface for the Debug Framework.
 * This interface defines the contract for handling debug authentication requests.
 */
public interface DebugService {

    /**
     * Handles requests to the /commonauth endpoint and determines if they are debug flows.
     * 
     * @param request the HTTP servlet request
     * @param response the HTTP servlet response
     * @return true if the request was handled as a debug flow, false otherwise
     * @throws IOException if an I/O error occurs
     */
    boolean handleCommonAuthRequest(HttpServletRequest request, HttpServletResponse response) throws IOException;

    /**
     * Checks if the given request represents a debug flow.
     * 
     * @param request the HTTP servlet request
     * @return true if this is a debug flow, false otherwise
     */
    boolean isDebugFlow(HttpServletRequest request);
}
