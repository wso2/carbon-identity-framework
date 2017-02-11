/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.gateway.resource;

import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.identity.gateway.api.FrameworkClientException;
import org.wso2.carbon.identity.gateway.api.FrameworkServerException;
import org.wso2.carbon.identity.gateway.api.FrameworkRuntimeException;
import org.wso2.carbon.identity.gateway.api.HttpIdentityRequestFactory;
import org.wso2.carbon.identity.gateway.api.HttpIdentityResponse;
import org.wso2.carbon.identity.gateway.api.HttpIdentityResponseFactory;
import org.wso2.carbon.identity.gateway.api.IdentityRequest;
import org.wso2.carbon.identity.gateway.api.IdentityResponse;
import org.wso2.carbon.identity.gateway.resource.internal.GatewayResourceDataHolder;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.util.BufferUtil;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Identity GatewayResource MicroService. This serves as the endpoint for all requests that come into the Identity GatewayResource.
 */
@Component(
        name = "org.wso2.carbon.identity.framework.resource.GatewayResource",
        service = Microservice.class,
        immediate = true
)
@Path("/gateway")
public class GatewayResource implements Microservice {


    private static final String ERROR_PROCESSING_REQUEST = "Error Processing Request.";
    private static final String INVALID_REQUEST = "Invalid or Malformed Request.";

    /**
     * Entry point for all initial Identity Request coming into the GatewayResource.
     *
     * @param request
     * @return Response
     */
    @POST
    @Path("/")
    public Response processPost(@Context Request request) {
        addParameters(request);
        process(request);
        return processHttpResponse(process(request), request);
    }


    @GET
    @Path("/")
    public Response processGet(@Context Request request) {
        return processPost(request);
    }


    private ProcessCoordinator manager = new ProcessCoordinator();

    /**
     * Process request/response.
     *
     * @param request HttpServletRequest
     */
    private HttpIdentityResponse process(Request request) {

        HttpIdentityRequestFactory factory = getIdentityRequestFactory(request);

        IdentityRequest identityRequest = null;
        HttpIdentityResponse.HttpIdentityResponseBuilder responseBuilder = null;

        try {
            identityRequest = factory.create(request).build();
            if (identityRequest == null) {
                throw FrameworkRuntimeException.error("IdentityRequest is Null. Cannot proceed!!");
            }
        } catch (FrameworkClientException e) {
            responseBuilder = factory.handleException(e);
            if (responseBuilder == null) {
                throw FrameworkRuntimeException.error("HttpIdentityResponseBuilder is Null. Cannot proceed!!");
            }
            return responseBuilder.build();
        }

        IdentityResponse identityResponse = null;
        HttpIdentityResponseFactory responseFactory = null;

        try {
            identityResponse = manager.process(identityRequest);
            if (identityResponse == null) {
                throw FrameworkRuntimeException.error("IdentityResponse is Null. Cannot proceed!!");
            }
            responseFactory = getIdentityResponseFactory(identityResponse);
            responseBuilder = responseFactory.create(identityResponse);
            if (responseBuilder == null) {
                throw FrameworkRuntimeException.error("HttpIdentityResponseBuilder is Null. Cannot proceed!!");
            }
            return responseBuilder.build();
        } catch (FrameworkServerException e) {
            responseFactory = getIdentityResponseFactory(e);
            responseBuilder = responseFactory.handleException(e);
            if (responseBuilder == null) {
                throw FrameworkRuntimeException.error("HttpIdentityResponseBuilder is Null. Cannot proceed!!");
            }
            return responseBuilder.build();
        }
    }

    protected void service(Request request) throws IOException {

        HttpIdentityResponse httpIdentityResponse = process(request);
        processHttpResponse(httpIdentityResponse, request);
    }

    private Response processHttpResponse(HttpIdentityResponse httpIdentityResponse, Request request) {

        Response.ResponseBuilder builder = Response.status(httpIdentityResponse.getStatusCode());
        builder.entity(httpIdentityResponse.getBody());
        httpIdentityResponse.getHeaders().forEach(builder::header);
        return builder.build();
//        Response.ResponseBuilder builder = Response.status(httpIdentityResponse.getStatusCode());
//        //#TODO: want to get clear how transform identoty response to jaxrs response
//        for (Map.Entry<String, String> entry : httpIdentityResponse.getHeaders().entrySet()) {
//            response.addHeader(entry.getKey(), entry.getValue());
//        }
//        for (Map.Entry<String, Cookie> entry : httpIdentityResponse.getCookies().entrySet()) {
//            response.addCookie(entry.getValue());
//        }
//        if (StringUtils.isNotBlank(httpIdentityResponse.getContentType())) {
//            response.setContentType(httpIdentityResponse.getContentType());
//        }
//        if (httpIdentityResponse.getStatusCode() == HttpServletResponse.SC_MOVED_TEMPORARILY) {
//            try {
//                sendRedirect(response, httpIdentityResponse);
//            } catch (IOException ex) {
//                throw FrameworkRuntimeException.error("Error occurred while redirecting response", ex);
//            }
//        } else {
//            response.setStatus(httpIdentityResponse.getStatusCode());
//            try {
//                PrintWriter out = response.getWriter();
//                if (StringUtils.isNotBlank(httpIdentityResponse.getBody())) {
//                    out.print(httpIdentityResponse.getBody());
//                }
//            } catch (IOException e) {
//                throw FrameworkRuntimeException.error("Error occurred while getting Response writer object", e);
//            }
//        }
    }

    /**
     * Get the HttpIdentityRequestFactory.
     *
     * @param request HttpServletRequest
     * @return HttpIdentityRequestFactory
     */
    private HttpIdentityRequestFactory getIdentityRequestFactory(Request request) {

        List<HttpIdentityRequestFactory> factories =
                GatewayResourceDataHolder.getInstance().getHttpIdentityRequestFactories();

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

        List<HttpIdentityResponseFactory> factories = GatewayResourceDataHolder.getInstance()
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
    private HttpIdentityResponseFactory getIdentityResponseFactory(FrameworkServerException exception) {

        List<HttpIdentityResponseFactory> factories = GatewayResourceDataHolder.getInstance()
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

        String queryParams = buildQueryString(HttpIdentityResponse.getParameters());
        //TODO: MSS4J how redirect
        //response.sendRedirect(HttpIdentityResponse.getRedirectURL() + "&"+ queryParams);
    }

    public static String buildQueryString(Map<String, String[]> parameterMap) throws UnsupportedEncodingException {
        StringBuilder queryString = new StringBuilder("?");
        boolean isFirst = true;
        Iterator i$ = parameterMap.entrySet().iterator();

        while (i$.hasNext()) {
            Map.Entry entry = (Map.Entry) i$.next();
            String[] arr$ = (String[]) entry.getValue();
            int len$ = arr$.length;

            for (int i$1 = 0; i$1 < len$; ++i$1) {
                String paramValue = arr$[i$1];
                if (isFirst) {
                    isFirst = false;
                } else {
                    queryString.append("&");
                }

                queryString.append(URLEncoder.encode((String) entry.getKey(), StandardCharsets.UTF_8.name()));
                queryString.append("=");
                queryString.append(URLEncoder.encode(paramValue, StandardCharsets.UTF_8.name()));
            }
        }

        return queryString.toString();
    }

    private void addParameters(Request request) {
        Map parameters = getRequestParameters(request);
        parameters.forEach((k, v) -> {
            request.setProperty(String.valueOf(k), v);
        });
        String body = readRequestBody(request);
        request.setProperty("requestBody", body);

       if ( isFormParamRequest(request.getContentType())) {
           try {
               handleFormParams(body, request);
           } catch (UnsupportedEncodingException e) {
               throw FrameworkRuntimeException.error("Error while building request body");

           }
       }
    }

    public static String readRequestBody(Request msf4jRequest) {

        ByteBuffer merge = BufferUtil.merge(msf4jRequest.getFullMessageBody());
        return Charset.defaultCharset().decode(merge).toString();
    }

    public static Map getRequestParameters (Request request) {
        return getQueryParamMap(request.getUri());
    }

    public static Map<String, String> getQueryParamMap(String requestUri) {

        if (Optional.ofNullable(requestUri).isPresent()) {
            Map<String, String> queryMap = new HashMap<>();
            return splitQuery(
                    Arrays.stream(requestUri.split("\\?"))
                            .skip(1)
                            .findFirst()
                            .orElse(null)
            );
        }

        return Collections.emptyMap();
    }

    private void handleFormParams(String requestBody, Request request)
            throws UnsupportedEncodingException {

        splitQuery(requestBody).forEach(request::setProperty);
    }

    public static Map<String, String> splitQuery(String queryString) {

        if (Optional.ofNullable(queryString).isPresent()) {
            Map<String, String> queryMap = new HashMap<>();
            Arrays.stream(queryString.split("&"))
                    .map(GatewayResource::splitQueryParameter)
                    .filter(x -> x.getKey() != null && x.getValue() != null)
                    .forEach(x -> queryMap.put(x.getKey(), x.getValue()));

            return queryMap;
        }

        return Collections.emptyMap();
    }


    private static AbstractMap.SimpleEntry<String, String> splitQueryParameter(String queryPairString) {

        int idx = queryPairString.indexOf("=");
        String key = idx > 0 ? queryPairString.substring(0, idx) : queryPairString;
        String value = idx > 0 && queryPairString.length() > idx + 1 ? queryPairString.substring(idx + 1) : null;
        return new AbstractMap.SimpleEntry<String, String>(urlDecode(key), urlDecode(value));
    }

    public static String urlDecode(final String encoded) {

        try {
            return encoded == null ? null : URLDecoder.decode(encoded, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException("Impossible: UTF-8 is a required encoding", e);
        }
    }

    private boolean isFormParamRequest(String contentType) {
        return MediaType.APPLICATION_FORM_URLENCODED.equalsIgnoreCase(contentType);
    }
}

