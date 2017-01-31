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

import io.swagger.annotations.Api;
import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.msf4j.Request;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

@Api(value = "Identity Endpoint")
@SwaggerDefinition(
        info = @Info(
                title = "Identity Endpoint Swagger Definition", version = "1.0",
                description = "Identity Endpoint",
                license = @License(name = "Apache 2.0", url = "http://www.apache.org/licenses/LICENSE-2.0"))
)
@Path("/identity")
public class IdentityServlet {

    private IdentityProcessCoordinator manager = new IdentityProcessCoordinator();

    public void service(@Context Request request) {

        HttpIdentityResponse httpIdentityResponse = process(request);
        processHttpResponse(httpIdentityResponse, request);
    }

    /**
     * Process the {@link Request}.
     *
     * @param request
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
                throw FrameworkRuntimeException.error("HttpIdentityResponseBuilder is Null. Cannot proceed!!", e);
            }
            return responseBuilder.build();
        } catch (RuntimeException e) {
            responseBuilder = factory.handleException(e);
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
                throw FrameworkRuntimeException.error("IdentityResponse is Null. Cannot proceed!!");
            }
            responseFactory = getHttpIdentityResponseFactory(identityResponse);
            responseBuilder = responseFactory.create(identityResponse);
            if (responseBuilder == null) {
                throw FrameworkRuntimeException.error("HttpIdentityResponseBuilder is Null. Cannot proceed!!");
            }
            return responseBuilder.build();
        } catch (FrameworkException e) {
            responseFactory = getIdentityResponseFactory(e);
            responseBuilder = responseFactory.handleException(e);
            if (responseBuilder == null) {
                throw FrameworkRuntimeException.error("HttpIdentityResponseBuilder is Null. Cannot proceed!!", e);
            }
            return responseBuilder.build();
        } catch (RuntimeException e) {
            responseFactory = getIdentityResponseFactory(e);
            responseBuilder = responseFactory.handleException(e);
            if (responseBuilder == null) {
                throw FrameworkRuntimeException.error("HttpIdentityResponseBuilder is Null. Cannot proceed!!", e);
            }
            return responseBuilder.build();
        }
    }

    /**
     * Process the {@link HttpIdentityResponse} and {@link Request}.
     * @param httpIdentityResponse {@link HttpIdentityResponse}
     * @param request {@link Request}
     */
    private Response processHttpResponse(HttpIdentityResponse httpIdentityResponse, Request request) {

        Response.ResponseBuilder responseBuilder = Response.created(URI.create(request.getUri()));

        httpIdentityResponse.getHeaders().forEach((key, value) -> responseBuilder.header(key, value));
        httpIdentityResponse.getCookies().forEach(
                (key, value) -> responseBuilder.cookie(new NewCookie(key, value.getValue())));
        if (StringUtils.isNotBlank(httpIdentityResponse.getContentType())) {
            responseBuilder.type(httpIdentityResponse.getContentType());
        }
        if (httpIdentityResponse.getStatusCode() == HttpServletResponse.SC_MOVED_TEMPORARILY) {
            try {
                sendRedirect(httpIdentityResponse);
            } catch (IOException e) {
                throw FrameworkRuntimeException.error("Error occurred while redirecting response", e);
            }
        } else {
            responseBuilder.status(httpIdentityResponse.getStatusCode());
            return responseBuilder.build();
        }
        return null;
    }

    /**
     * Get the HttpIdentityRequestFactory.
     *
     * @param request  {@link Request}
     * @return {@link HttpIdentityRequestFactory}
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
     * @param httpIdentityResponse {@link HttpIdentityResponse}
     */
    private void sendRedirect(HttpIdentityResponse httpIdentityResponse) throws IOException {

//        String redirectUrl;
//        if (httpIdentityResponse.isFragmentUrl()) {
//            redirectUrl = IdentityUtil.buildFragmentUrl(httpIdentityResponse.getRedirectURL(),
//                    httpIdentityResponse.getParameters());
//        } else {
//            redirectUrl = IdentityUtil.buildQueryUrl(httpIdentityResponse.getRedirectURL(),
//                    httpIdentityResponse.getParameters());
//        }
//        response.sendRedirect(redirectUrl);
//    }
    }
}
