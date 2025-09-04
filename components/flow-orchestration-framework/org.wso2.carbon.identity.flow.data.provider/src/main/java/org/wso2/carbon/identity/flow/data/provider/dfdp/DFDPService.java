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

package org.wso2.carbon.identity.flow.data.provider.dfdp;

import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * DFDP Service Interface.
 * This interface defines the contract for DFDP (Debug Flow Data Provider) operations
 * that can be used by both authentication framework and flow orchestration framework.
 */
public interface DFDPService {

    /**
     * Processes a DFDP request and returns the results.
     * 
     * @param request HTTP servlet request containing DFDP parameters
     * @param response HTTP servlet response for sending results
     * @param context Authentication context for maintaining state
     * @throws FrameworkException if DFDP processing fails
     */
    void processDFDPRequest(HttpServletRequest request, HttpServletResponse response, 
                           AuthenticationContext context) throws FrameworkException;

    /**
     * Checks if the given request is a DFDP request.
     * 
     * @param request HTTP servlet request
     * @return true if this is a DFDP request, false otherwise
     */
    boolean isDFDPRequest(HttpServletRequest request);

    /**
     * Validates DFDP parameters in the request.
     * 
     * @param request HTTP servlet request
     * @throws FrameworkException if validation fails
     */
    void validateDFDPRequest(HttpServletRequest request) throws FrameworkException;
}
