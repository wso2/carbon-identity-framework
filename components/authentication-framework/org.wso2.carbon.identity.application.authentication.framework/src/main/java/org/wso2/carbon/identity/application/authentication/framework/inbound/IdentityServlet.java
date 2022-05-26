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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Identity Servlet.
 */
public class IdentityServlet extends HttpServlet {

    private static final Log log = LogFactory.getLog(IdentityServlet.class);
    private IdentityProcessCoordinator manager = new IdentityProcessCoordinator();

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpIdentityResponse httpIdentityResponse = process(request, response);
        processHttpResponse(httpIdentityResponse, response);
    }

    /**
     * Process the {@link HttpServletRequest} and {@link HttpServletResponse}.
     *
     * @param request
     * @param response
     */
    private HttpIdentityResponse process(HttpServletRequest request, HttpServletResponse response) {

        HttpIdentityRequestFactory factory = getIdentityRequestFactory(request, response);

        IdentityRequest identityRequest = null;
        HttpIdentityResponse.HttpIdentityResponseBuilder responseBuilder = null;

        try {
            identityRequest = factory.create(request, response).build();
            if (identityRequest == null) {
                String message = "IdentityRequest is Null. Cannot proceed!!";
                log.error(message);
                throw FrameworkRuntimeException.error(message);
            }
        } catch (FrameworkClientException e) {
            log.error("Failed to create IdentityRequest", e);
            responseBuilder = factory.handleException(e, request, response);
            if (responseBuilder == null) {
                throw FrameworkRuntimeException.error("HttpIdentityResponseBuilder is Null. Cannot proceed!!", e);
            }
            return responseBuilder.build();
        } catch (RuntimeException e) {
            log.error("Failed to create IdentityRequest", e);
            responseBuilder = factory.handleException(e, request, response);
            if (responseBuilder == null) {
                throw FrameworkRuntimeException.error("HttpIdentityResponseBuilder is Null. Cannot proceed!!", e);
            }
            return responseBuilder.build();
        }

        IdentityResponse identityResponse = null;
        HttpIdentityResponseFactory responseFactory = null;

        try {
            identityResponse = manager.process(identityRequest);
            if (identityResponse == null) {
                String message = "IdentityResponse is Null. Cannot proceed!!";
                log.error(message);
                throw FrameworkRuntimeException.error(message);
            }
            responseFactory = getHttpIdentityResponseFactory(identityResponse);
            responseBuilder = responseFactory.create(identityResponse);
            if (responseBuilder == null) {
                String message = "HttpIdentityResponseBuilder is Null. Cannot proceed!!";
                log.error(message);
                throw FrameworkRuntimeException.error(message);
            }
            return responseBuilder.build();
        } catch (FrameworkException e) {
            if (e instanceof FrameworkClientException) {
                if (log.isDebugEnabled()) {
                    log.debug("Failed to process IdentityRequest", e);
                }
            } else {
                log.error("Failed to process IdentityRequest", e);
            }
            responseFactory = getIdentityResponseFactory(e);
            responseBuilder = responseFactory.handleException(e);
            if (responseBuilder == null) {
                throw FrameworkRuntimeException.error("HttpIdentityResponseBuilder is Null. Cannot proceed!!", e);
            }
            return responseBuilder.build();
        } catch (RuntimeException e) {
            log.error("Failed to process IdentityRequest", e);
            responseFactory = getIdentityResponseFactory(e);
            responseBuilder = responseFactory.handleException(e);
            if (responseBuilder == null) {
                throw FrameworkRuntimeException.error("HttpIdentityResponseBuilder is Null. Cannot proceed!!", e);
            }
            return responseBuilder.build();
        }
    }

    /**
     * Process the {@link HttpIdentityResponse} and {@link HttpServletResponse}.
     *
     * @param httpIdentityResponse {@link HttpIdentityResponse}
     * @param response             {@link HttpServletResponse}
     */
    private void processHttpResponse(HttpIdentityResponse httpIdentityResponse, HttpServletResponse response) {

        for (Map.Entry<String, String> entry : httpIdentityResponse.getHeaders().entrySet()) {
            response.addHeader(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, Cookie> entry : httpIdentityResponse.getCookies().entrySet()) {
            response.addCookie(entry.getValue());
        }
        if (StringUtils.isNotBlank(httpIdentityResponse.getContentType())) {
            response.setContentType(httpIdentityResponse.getContentType());
        }
        if (httpIdentityResponse.getStatusCode() == HttpServletResponse.SC_MOVED_TEMPORARILY) {
            try {
                sendRedirect(response, httpIdentityResponse);
            } catch (IOException e) {
                String message = "Error occurred while redirecting response";
                log.error(message, e);
                throw FrameworkRuntimeException.error(message, e);
            }
        } else {
            response.setStatus(httpIdentityResponse.getStatusCode());
            try {
                PrintWriter out = response.getWriter();
                if (StringUtils.isNotBlank(httpIdentityResponse.getBody())) {
                    out.print(httpIdentityResponse.getBody());
                }
            } catch (IOException e) {
                String message = "Error occurred while getting Response writer object";
                log.error(message, e);
                throw FrameworkRuntimeException.error(message, e);
            }
        }
    }

    /**
     * Get the HttpIdentityRequestFactory.
     *
     * @param request  {@link HttpServletRequest}
     * @param response {@link HttpServletResponse}
     * @return {@link HttpIdentityRequestFactory}
     */
    private HttpIdentityRequestFactory getIdentityRequestFactory(HttpServletRequest request,
            HttpServletResponse response) {

        List<HttpIdentityRequestFactory> factories = FrameworkServiceDataHolder.getInstance()
                .getHttpIdentityRequestFactories();
        for (HttpIdentityRequestFactory requestBuilder : factories) {
            if (requestBuilder.canHandle(request, response)) {
                return requestBuilder;
            }
        }
        String message = "No HttpIdentityRequestFactory found to create the request";
        log.error(message);
        throw FrameworkRuntimeException.error(message);
    }

    /**
     * Get the {@link HttpIdentityResponseFactory} to handle this {@link IdentityResponse}.
     *
     * @param identityResponse IdentityResponse
     * @return HttpIdentityResponseFactory
     */
    private HttpIdentityResponseFactory getHttpIdentityResponseFactory(IdentityResponse identityResponse) {

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
     * Get the {@link HttpIdentityResponseFactory} to handle this {@link FrameworkException}.
     *
     * @param exception {@link FrameworkException}
     * @return {@link HttpIdentityResponseFactory}
     */
    private HttpIdentityResponseFactory getIdentityResponseFactory(FrameworkException exception) {

        List<HttpIdentityResponseFactory> factories = FrameworkServiceDataHolder.getInstance()
                .getHttpIdentityResponseFactories();
        for (HttpIdentityResponseFactory responseFactory : factories) {
            if (responseFactory.canHandle(exception)) {
                return responseFactory;
            }
        }
        throw FrameworkRuntimeException.error("No HttpIdentityResponseFactory found to create the response", exception);
    }

    /**
     * Get the {@link HttpIdentityResponseFactory} to handle this {@link RuntimeException}.
     *
     * @param exception {@link RuntimeException}
     * @return {@link HttpIdentityResponseFactory}
     */
    private HttpIdentityResponseFactory getIdentityResponseFactory(RuntimeException exception) {

        List<HttpIdentityResponseFactory> factories = FrameworkServiceDataHolder.getInstance()
                .getHttpIdentityResponseFactories();
        for (HttpIdentityResponseFactory responseFactory : factories) {
            if (responseFactory.canHandle(exception)) {
                return responseFactory;
            }
        }
        throw FrameworkRuntimeException.error("No HttpIdentityResponseFactory found to create the response", exception);
    }

    /**
     * Sends a 302 redirect response to client.
     *
     * @param response             {@link HttpServletResponse}
     * @param httpIdentityResponse {@link HttpIdentityResponse}
     */
    private void sendRedirect(HttpServletResponse response, HttpIdentityResponse httpIdentityResponse)
            throws IOException {

        String redirectUrl;
        if (httpIdentityResponse.isFragmentUrl()) {
            redirectUrl = IdentityUtil
                    .buildFragmentUrl(httpIdentityResponse.getRedirectURL(), httpIdentityResponse.getParameters());
        } else {
            redirectUrl = IdentityUtil
                    .buildQueryUrl(httpIdentityResponse.getRedirectURL(), httpIdentityResponse.getParameters());
        }
        response.sendRedirect(redirectUrl);
    }
}
