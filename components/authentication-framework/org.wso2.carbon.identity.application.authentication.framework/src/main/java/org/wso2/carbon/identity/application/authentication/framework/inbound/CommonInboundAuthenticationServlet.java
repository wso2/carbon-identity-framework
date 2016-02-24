/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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
package org.wso2.carbon.identity.application.authentication.framework.inbound;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class CommonInboundAuthenticationServlet extends HttpServlet {

    private InboundAuthenticationManager inboundAuthenticationManager = new InboundAuthenticationManager();

    /**
     * Get Inbound authentication request builder
     *
     * @param req   Http request
     * @param resp  Http response
     * @return Inbound authentication request builder
     * @throws AuthenticationFrameworkRuntimeException
     */
    private InboundAuthenticationRequestBuilder getInboundRequestBuilder(HttpServletRequest req,
            HttpServletResponse resp) throws AuthenticationFrameworkRuntimeException {
        List<InboundAuthenticationRequestBuilder> requestBuilders = FrameworkServiceDataHolder.getInstance()
                .getInboundAuthenticationRequestBuilders();

        for (InboundAuthenticationRequestBuilder requestBuilder : requestBuilders) {
            if (requestBuilder.canHandle(req, resp)) {
                return requestBuilder;
            }
        }
        return null;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {

            doProcess(request, response);

        } catch (AuthenticationFrameworkRuntimeException ex) {
            throw new ServletException(ex);
        }
    }

    /**
     * Process authentication request/response
     *
     * @param request   Http request
     * @param response  Http response
     * @throws AuthenticationFrameworkRuntimeException
     */
    private void doProcess(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationFrameworkRuntimeException {

        InboundAuthenticationRequestBuilder requestBuilder = getInboundRequestBuilder(request, response);
        if (requestBuilder == null) {
            throw new AuthenticationFrameworkRuntimeException(
                    "No authentication request builder found to build the request");
        }

        InboundAuthenticationRequest authenticationRequest = requestBuilder.buildRequest(request, response);

        if (request.getPathInfo().contains(InboundAuthenticationConstants.HTTP_PATH_PARAM_REQUEST)) {
            InboundAuthenticationResponse inboundAuthenticationResponse = doProcessRequest(request, response,
                    authenticationRequest);
            if (inboundAuthenticationResponse.getStatusCode() == InboundAuthenticationConstants.StatusCode.REDIRECT) {
                try {
                    sendRedirect(response, inboundAuthenticationResponse);
                } catch (IOException ex) {
                    throw new AuthenticationFrameworkRuntimeException("Error occurred while redirecting response", ex);
                }
            }
        } else if (request.getPathInfo().contains(InboundAuthenticationConstants.HTTP_PATH_PARAM_RESPONSE)) {
            InboundAuthenticationResponse result = doProcessResponse(authenticationRequest);
            if (result.getRedirectURL() != null) {
                try {
                    response.sendRedirect(result.getRedirectURL());
                } catch (IOException ex) {
                    throw new AuthenticationFrameworkRuntimeException(
                            "Error occurred while redirecting response " + result.getRedirectURL(), ex);
                }
            }
        }

    }

    private void sendRedirect(HttpServletResponse response, InboundAuthenticationResponse inboundAuthenticationResponse)
            throws IOException {

        StringBuilder queryParams = new StringBuilder("?");
        boolean isFirst = true;
        for (Map.Entry<String, String> entry : inboundAuthenticationResponse.getParameters().entrySet()) {

            if (isFirst) {
                queryParams.append(entry.getKey());
                queryParams.append("=");
                queryParams.append(entry.getValue());
                isFirst = false;
            }
            queryParams.append("&");
            queryParams.append(entry.getKey());
            queryParams.append("=");
            queryParams.append(entry.getValue());
        }

        response.sendRedirect(inboundAuthenticationResponse.getRedirectURL() + queryParams);
    }

    /**
     * Process inbound request
     *
     * @param request   Http request
     * @param response  Http response
     * @param authenticationRequest Authentication request
     * @return Inbound authentication response
     * @throws AuthenticationFrameworkRuntimeException
     */
    protected InboundAuthenticationResponse doProcessRequest(HttpServletRequest request, HttpServletResponse response,
            InboundAuthenticationRequest authenticationRequest)
            throws AuthenticationFrameworkRuntimeException {

        try {
            InboundAuthenticationResponse result = inboundAuthenticationManager.processRequest(authenticationRequest);
            return result;
        } catch (FrameworkException ex) {
            throw new AuthenticationFrameworkRuntimeException("Error occurred while processing authentication request",
                    ex);
        }
    }

    /**
     * Process inbound authentication response
     *
     * @param authenticationRequest Authentication request
     * @return Inbound authentication response
     * @throws AuthenticationFrameworkRuntimeException
     */
    protected InboundAuthenticationResponse doProcessResponse(InboundAuthenticationRequest authenticationRequest)
            throws AuthenticationFrameworkRuntimeException {

        try {
            String[] sessionDataKey = authenticationRequest.getParameters().get(FrameworkConstants.SESSION_DATA_KEY);
            if (!ArrayUtils.isEmpty(sessionDataKey) && !StringUtils.isEmpty(sessionDataKey[0])) {
                InboundAuthenticationContextCacheEntry cacheEntry = InboundAuthenticationUtil
                        .getInboundAuthenticationContextToCache(sessionDataKey[0]);

                InboundAuthenticationResponse result = inboundAuthenticationManager.processResponse(
                        cacheEntry.getInboundAuthenticationContext(), authenticationRequest);
                return result;
            }
        } catch (FrameworkException ex) {
            throw new AuthenticationFrameworkRuntimeException("Error occurred while processing authentication response",
                    ex);
        }
        throw new AuthenticationFrameworkRuntimeException("No session found to process the response.");
    }

}
