/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.gateway.resource;

import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.identity.gateway.api.exception.GatewayClientException;
import org.wso2.carbon.identity.gateway.api.exception.GatewayRuntimeException;
import org.wso2.carbon.identity.gateway.api.exception.GatewayServerException;
import org.wso2.carbon.identity.gateway.api.request.GatewayRequestBuilderFactory;
import org.wso2.carbon.identity.gateway.api.request.GatewayRequest;
import org.wso2.carbon.identity.gateway.api.response.GatewayResponse;
import org.wso2.carbon.identity.gateway.api.response.HttpGatewayResponse;
import org.wso2.carbon.identity.gateway.api.response.GatewayResponseBuilderFactory;
import org.wso2.carbon.identity.gateway.common.util.Constants;
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
 * Identity GatewayResource MicroService. This serves as the endpoint for all requests that come into the Identity
 * GatewayResource.
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
        return process(request);
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
     * @param request
     *         HttpServletRequest
     */
    private Response process(Request request) {

        GatewayRequestBuilderFactory factory = getIdentityRequestFactory(request);

        GatewayRequest gatewayRequest = null;
        Response.ResponseBuilder responseBuilder = null;

        try {
            gatewayRequest = factory.create(request).build();
            if (gatewayRequest == null) {
                throw new GatewayRuntimeException("GatewayRequest is Null. Cannot proceed!!");
            }
        } catch (GatewayClientException e) {
            responseBuilder = factory.handleException(e);
            if (responseBuilder == null) {
                throw new GatewayRuntimeException("HttpIdentityResponseBuilder is Null. Cannot proceed!!");
            }
            //#TODO Enable this to new response
            return responseBuilder.build();
        }

        GatewayResponse gatewayResponse = null;
        GatewayResponseBuilderFactory responseFactory = null;

        try {
            gatewayResponse = manager.process(gatewayRequest);
            if (gatewayResponse == null) {
                throw new GatewayRuntimeException("GatewayResponse is Null. Cannot proceed!!");
            }
            responseFactory = getIdentityResponseFactory(gatewayResponse);
            Response.ResponseBuilder builder = responseFactory.createBuilder(gatewayResponse);
            if (builder == null) {
                throw new GatewayRuntimeException("HttpIdentityResponseBuilder is Null. Cannot proceed!!");
            }
            return builder.build();
        } catch (GatewayServerException e) {
            responseFactory = getIdentityResponseFactory(e);
            responseBuilder = responseFactory.handleException(e);
            if (responseBuilder == null) {
                throw new GatewayRuntimeException("HttpIdentityResponseBuilder is Null. Cannot proceed!!");
            }
            //#TODO Enable this to new response
            //return responseBuilder.build();
        }
        return null;
    }

    private Response processHttpResponse(HttpGatewayResponse httpGatewayResponse, Request request) {

        Response.ResponseBuilder builder = Response.status(httpGatewayResponse.getStatusCode());
        builder.entity(httpGatewayResponse.getBody());
        httpGatewayResponse.getHeaders().forEach(builder::header);
        httpGatewayResponse.getCookies().forEach(builder::cookie);
        return builder.build();
        //        Response.ResponseBuilder builder = Response.status(httpGatewayResponse.getStatusCode());
        //        //#TODO: want to get clear how transform identoty response to jaxrs response
        //        for (Map.Entry<String, String> entry : httpGatewayResponse.getHeaders().entrySet()) {
        //            response.addHeader(entry.getKey(), entry.getValue());
        //        }
        //        for (Map.Entry<String, Cookie> entry : httpGatewayResponse.getCookies().entrySet()) {
        //            response.addCookie(entry.getValue());
        //        }
        //        if (StringUtils.isNotBlank(httpGatewayResponse.getContentType())) {
        //            response.setContentType(httpGatewayResponse.getContentType());
        //        }
        //        if (httpGatewayResponse.getStatusCode() == HttpServletResponse.SC_MOVED_TEMPORARILY) {
        //            try {
        //                sendRedirect(response, httpGatewayResponse);
        //            } catch (IOException ex) {
        //                throw GatewayRuntimeException.error("Error occurred while redirecting response", ex);
        //            }
        //        } else {
        //            response.setStatus(httpGatewayResponse.getStatusCode());
        //            try {
        //                PrintWriter out = response.getWriter();
        //                if (StringUtils.isNotBlank(httpGatewayResponse.getBody())) {
        //                    out.print(httpGatewayResponse.getBody());
        //                }
        //            } catch (IOException e) {
        //                throw GatewayRuntimeException.error("Error occurred while getting Response writer
        // object", e);
        //            }
        //        }
    }

    /**
     * Get the GatewayRequestBuilderFactory.
     *
     * @param request
     *         HttpServletRequest
     * @return GatewayRequestBuilderFactory
     */
    private GatewayRequestBuilderFactory getIdentityRequestFactory(Request request) {

        List<GatewayRequestBuilderFactory> factories =
                GatewayResourceDataHolder.getInstance().getHttpIdentityRequestFactories();

        for (GatewayRequestBuilderFactory requestBuilder : factories) {
            if (requestBuilder.canHandle(request)) {
                return requestBuilder;
            }
        }
        throw new GatewayRuntimeException("No GatewayRequestBuilderFactory found to create the request");
    }

    /**
     * Get the GatewayResponseBuilderFactory.
     *
     * @param gatewayResponse
     *         GatewayResponse
     * @return GatewayResponseBuilderFactory
     */
    private GatewayResponseBuilderFactory getIdentityResponseFactory(GatewayResponse gatewayResponse) {

        List<GatewayResponseBuilderFactory> factories = GatewayResourceDataHolder.getInstance()
                .getHttpIdentityResponseFactories();

        for (GatewayResponseBuilderFactory responseFactory : factories) {
            if (responseFactory.canHandle(gatewayResponse)) {
                return responseFactory;
            }
        }
        throw new GatewayRuntimeException("No GatewayResponseBuilderFactory found to create the request");
    }

    /**
     * Get the GatewayResponseBuilderFactory.
     *
     * @param exception
     *         FrameworkException
     * @return GatewayResponseBuilderFactory
     */
    private GatewayResponseBuilderFactory getIdentityResponseFactory(GatewayServerException exception) {

        List<GatewayResponseBuilderFactory> factories = GatewayResourceDataHolder.getInstance()
                .getHttpIdentityResponseFactories();

        for (GatewayResponseBuilderFactory responseFactory : factories) {
            if (responseFactory.canHandle(exception)) {
                return responseFactory;
            }
        }
        throw new GatewayRuntimeException("No GatewayResponseBuilderFactory found to create the request");
    }

    private void sendRedirect(Response response, HttpGatewayResponse HttpGatewayResponse)
            throws IOException {

        String queryParams = buildQueryString(HttpGatewayResponse.getParameters());
        //TODO: MSS4J how redirect
        //response.sendRedirect(HttpGatewayResponse.getRedirectURL() + "&"+ queryParams);
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
        Map<String, String> queryParams = getRequestParameters(request);
        request.setProperty(Constants.QUERY_PARAMETERS, queryParams);
        String body = readRequestBody(request);
        request.setProperty(Constants.REQUEST_BODY, body);

        if (isFormParamRequest(request.getContentType())) {
            try {
                handleFormParams(body, request);
            } catch (UnsupportedEncodingException e) {
                throw new GatewayRuntimeException("Error while building request body");
            }
        } else {
            request.setProperty(Constants.BODY_PARAMETERS, new HashMap<String, String>());
        }
    }

    public static String readRequestBody(Request msf4jRequest) {

        ByteBuffer merge = BufferUtil.merge(msf4jRequest.getFullMessageBody());
        return Charset.defaultCharset().decode(merge).toString();
    }

    public static Map<String, String> getRequestParameters(Request request) {
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

        Map<String, String> bodyParams = new HashMap<String, String>();
        splitQuery(requestBody).forEach(bodyParams::put);
        request.setProperty(Constants.BODY_PARAMETERS, bodyParams);
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

