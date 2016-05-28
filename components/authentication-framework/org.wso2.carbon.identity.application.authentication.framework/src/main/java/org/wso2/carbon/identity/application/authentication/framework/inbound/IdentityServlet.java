/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

public class IdentityServlet extends HttpServlet {

    private IdentityProcessCoordinator manager = new IdentityProcessCoordinator();

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {

        HttpIdentityResponse httpIdentityResponse = process(request, response);
        processHttpResponse(httpIdentityResponse, request, response);
    }

    /**
     * Process request/response.
     *
     * @param request   HttpServletRequest
     * @param response  HttpServletResponse
     */
    private HttpIdentityResponse process(HttpServletRequest request, HttpServletResponse response) {

        HttpIdentityRequestFactory factory = getIdentityRequestFactory(request, response);

        IdentityRequest identityRequest = null;
        HttpIdentityResponse.HttpIdentityResponseBuilder responseBuilder = null;

        try {
            identityRequest = factory.create(request, response).build();
            if(identityRequest == null) {
                throw FrameworkRuntimeException.error("IdentityRequest is Null. Cannot proceed!!");
            }
        } catch (FrameworkClientException e) {
            responseBuilder = factory.handleException(e, request, response);
            if(responseBuilder == null) {
                throw FrameworkRuntimeException.error("HttpIdentityResponseBuilder is Null. Cannot proceed!!");
            }
            return responseBuilder.build();
        }

        IdentityResponse identityResponse = null;
        HttpIdentityResponseFactory responseFactory = null;

        try {
            identityResponse = manager.process(identityRequest);
            if(identityResponse == null) {
                throw FrameworkRuntimeException.error("IdentityResponse is Null. Cannot proceed!!");
            }
            responseFactory = getIdentityResponseFactory(identityResponse);
            responseBuilder = responseFactory.create(identityResponse);
            if(responseBuilder == null) {
                throw FrameworkRuntimeException.error("HttpIdentityResponseBuilder is Null. Cannot proceed!!");
            }
            return responseBuilder.build();
        } catch (FrameworkException e) {
            responseFactory = getIdentityResponseFactory(e);
            responseBuilder = responseFactory.handleException(e);
            if(responseBuilder == null) {
                throw FrameworkRuntimeException.error("HttpIdentityResponseBuilder is Null. Cannot proceed!!");
            }
            return responseBuilder.build();
        }
    }

    private void processHttpResponse(HttpIdentityResponse httpIdentityResponse, HttpServletRequest request,
                                     HttpServletResponse response) {

        for(Map.Entry<String,String> entry: httpIdentityResponse.getHeaders().entrySet()) {
            response.addHeader(entry.getKey(), entry.getValue());
        }
        for(Map.Entry<String,Cookie> entry: httpIdentityResponse.getCookies().entrySet()) {
            response.addCookie(entry.getValue());
        }
        if(StringUtils.isNotBlank(httpIdentityResponse.getContentType())) {
            response.setContentType(httpIdentityResponse.getContentType());
        }
        if (httpIdentityResponse.getStatusCode() == HttpServletResponse.SC_MOVED_TEMPORARILY) {
            try {
                sendRedirect(response, httpIdentityResponse);
            } catch (IOException ex) {
                throw FrameworkRuntimeException.error("Error occurred while redirecting response", ex);
            }
        } else {
            response.setStatus(httpIdentityResponse.getStatusCode());
            try {
                PrintWriter out = response.getWriter();
                if(StringUtils.isNotBlank(httpIdentityResponse.getBody())) {
                    out.print(httpIdentityResponse.getBody());
                }
            } catch (IOException e) {
                throw FrameworkRuntimeException.error("Error occurred while getting Response writer object", e);
            }
        }
    }

    /**
     * Get the HttpIdentityRequestFactory.
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @return HttpIdentityRequestFactory
     */
    private HttpIdentityRequestFactory getIdentityRequestFactory(HttpServletRequest request, HttpServletResponse response) {

        List<HttpIdentityRequestFactory> factories = FrameworkServiceDataHolder.getInstance().getHttpIdentityRequestFactories();

        for (HttpIdentityRequestFactory requestBuilder : factories) {
            if (requestBuilder.canHandle(request, response)) {
                return requestBuilder;
            }
        }
        throw FrameworkRuntimeException.error("No HttpIdentityRequestFactory found to create the request");
    }

    /**
     * Get the HttpIdentityResponseFactory.
     *
     * @param identityResponse IdentityResponse
     * @return HttpIdentityResponseFactory
     */
    private HttpIdentityResponseFactory getIdentityResponseFactory(IdentityResponse identityResponse) {

        List<HttpIdentityResponseFactory> factories = FrameworkServiceDataHolder.getInstance()
                .getHttpIdentityResponseFactories();

        for (HttpIdentityResponseFactory responseFactory : factories) {
            if (responseFactory.canHandle(identityResponse)) {
                return responseFactory;
            }
        }
        throw FrameworkRuntimeException.error("No HttpIdentityResponseFactory found to create the request");
    }

    /**
     * Get the HttpIdentityResponseFactory.
     *
     * @param exception FrameworkException
     * @return HttpIdentityResponseFactory
     */
    private HttpIdentityResponseFactory getIdentityResponseFactory(FrameworkException exception) {

        List<HttpIdentityResponseFactory> factories = FrameworkServiceDataHolder.getInstance()
                .getHttpIdentityResponseFactories();

        for (HttpIdentityResponseFactory responseFactory : factories) {
            if (responseFactory.canHandle(exception)) {
                return responseFactory;
            }
        }
        throw FrameworkRuntimeException.error("No HttpIdentityResponseFactory found to create the request");
    }

    private void sendRedirect(HttpServletResponse response, HttpIdentityResponse HttpIdentityResponse) throws IOException {

        String queryParams = IdentityUtil.buildQueryString(HttpIdentityResponse.getParameters());
        response.sendRedirect(HttpIdentityResponse.getRedirectURL() + queryParams);
    }
}
