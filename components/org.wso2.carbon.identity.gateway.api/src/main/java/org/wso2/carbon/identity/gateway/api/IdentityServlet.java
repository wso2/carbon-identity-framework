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

package org.wso2.carbon.identity.gateway.api;

import org.wso2.carbon.identity.gateway.api.internal.FrameworkServiceDataHolder;

import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class IdentityServlet {

    private IdentityProcessCoordinator manager = new IdentityProcessCoordinator();

    /**
     * Process request/response.
     *
     * @param request  HttpServletRequest
     */
    private HttpIdentityResponse process(Request request) {

        HttpIdentityRequestFactory factory = getIdentityRequestFactory(request);

        IdentityRequest identityRequest = null;
        HttpIdentityResponse.HttpIdentityResponseBuilder responseBuilder = null;

        try {
            identityRequest = factory.create(request).build();
            if(identityRequest == null) {
                throw FrameworkRuntimeException.error("IdentityRequest is Null. Cannot proceed!!");
            }
        } catch (FrameworkClientException e) {
            responseBuilder = factory.handleException(e);
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

    protected void service(Request request) throws IOException {

        HttpIdentityResponse httpIdentityResponse = process(request);
        processHttpResponse(httpIdentityResponse, request);
    }

    private void processHttpResponse(HttpIdentityResponse httpIdentityResponse, Request request) {
        Response.ResponseBuilder builder = Response.status(httpIdentityResponse.getStatusCode());
        //#TODO: want to get clear how transform identoty response to jaxrs response
        /*for (Map.Entry<String, String> entry : httpIdentityResponse.getHeaders().entrySet()) {
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
            } catch (IOException ex) {
                throw FrameworkRuntimeException.error("Error occurred while redirecting response", ex);
            }
        } else {
            response.setStatus(httpIdentityResponse.getStatusCode());
            try {
                PrintWriter out = response.getWriter();
                if (StringUtils.isNotBlank(httpIdentityResponse.getBody())) {
                    out.print(httpIdentityResponse.getBody());
                }
            } catch (IOException e) {
                throw FrameworkRuntimeException.error("Error occurred while getting Response writer object", e);
            }
        }*/
    }

    /**
     * Get the HttpIdentityRequestFactory.
     *
     * @param request  HttpServletRequest
     * @return HttpIdentityRequestFactory
     */
    private HttpIdentityRequestFactory getIdentityRequestFactory(Request request) {

        List<HttpIdentityRequestFactory> factories =
                FrameworkServiceDataHolder.getInstance().getHttpIdentityRequestFactories();

        for (HttpIdentityRequestFactory requestBuilder : factories) {
            if (requestBuilder.canHandle(request)) {
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

    private void sendRedirect(Response response, HttpIdentityResponse HttpIdentityResponse)
            throws IOException {

        String queryParams =  buildQueryString(HttpIdentityResponse.getParameters());
        //TODO: MSS4J how redirect
        //response.sendRedirect(HttpIdentityResponse.getRedirectURL() + "&"+ queryParams);
    }

    public static String buildQueryString(Map<String, String[]> parameterMap) throws UnsupportedEncodingException {
        StringBuilder queryString = new StringBuilder("?");
        boolean isFirst = true;
        Iterator i$ = parameterMap.entrySet().iterator();

        while(i$.hasNext()) {
            Map.Entry entry = (Map.Entry)i$.next();
            String[] arr$ = (String[])entry.getValue();
            int len$ = arr$.length;

            for(int i$1 = 0; i$1 < len$; ++i$1) {
                String paramValue = arr$[i$1];
                if(isFirst) {
                    isFirst = false;
                } else {
                    queryString.append("&");
                }

                queryString.append(URLEncoder.encode((String)entry.getKey(), StandardCharsets.UTF_8.name()));
                queryString.append("=");
                queryString.append(URLEncoder.encode(paramValue, StandardCharsets.UTF_8.name()));
            }
        }

        return queryString.toString();
    }
}
