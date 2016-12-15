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

package org.wso2.carbon.identity.gateway.resource.util;

import org.wso2.carbon.identity.framework.IdentityProcessCoordinator;
import org.wso2.carbon.identity.framework.FrameworkException;
import org.wso2.carbon.identity.framework.FrameworkRuntimeException;
import org.wso2.carbon.identity.framework.message.IdentityResponse;
import org.wso2.carbon.identity.gateway.resource.MSF4JIdentityRequestBuilderFactory;
import org.wso2.carbon.identity.gateway.resource.MSF4JResponseBuilderFactory;
import org.wso2.carbon.identity.gateway.resource.internal.DataHolder;
import org.wso2.msf4j.Request;

/**
 * Utility class used by {@link org.wso2.carbon.identity.gateway.resource.IdentityGateway}
 */
public class GatewayUtil {

    private static DataHolder dataHolder = DataHolder.getInstance();

    /**
     * Pick a @{@link MSF4JIdentityRequestBuilderFactory} that can return a canonical
     * {@link org.wso2.carbon.identity.framework.builder.IdentityRequestBuilder} object based on the {@link Request}
     * received by the {@link org.wso2.carbon.identity.gateway.resource.IdentityGateway} micro service.
     *
     * @param request {@link Request} object.
     * @return MSF4JIdentityRequestBuilderFactory instance
     */
    public static MSF4JIdentityRequestBuilderFactory pickRequestFactory(Request request) {

        return dataHolder.getRequestFactoryList().stream()
                .filter(x -> x.canHandle(request))
                .findFirst()
                .orElseThrow(() -> new FrameworkRuntimeException("Cannot find a response factory to handle the " +
                        "MSF4J Identity Request."));
    }


    /**
     * Pick a @{@link MSF4JResponseBuilderFactory} that can return a @{@link javax.ws.rs.core.Response.ResponseBuilder}
     * based on the @{@link IdentityResponse} sent from the Framework.
     *
     * @param identityResponse {@link IdentityResponse} object sent from framework
     * @return MSF4JIdentityRequestBuilderFactory instance or null if none of the available services can handle the
     * identityResponse
     */
    public static MSF4JResponseBuilderFactory pickIdentityResponseFactory(IdentityResponse identityResponse) {

        return dataHolder.getResponseFactoryList().stream()
                .filter(x -> x.canHandle(identityResponse))
                .findFirst()
                .orElseThrow(() -> new FrameworkRuntimeException("Cannot find a response factory to handle the " +
                        "IdentityResponse."));
    }


    /**
     * Pick a {@link MSF4JResponseBuilderFactory} that can handle when a {@link FrameworkException}
     * is encountered while processing an {@link org.wso2.carbon.identity.framework.message.IdentityRequest}
     *
     * @param ex {@link FrameworkException} encountered.
     * @return MSF4JResponseBuilderFactoryImpl instance that can handle the exception, null if no registered response
     * factory can handle the exception.
     */
    public static MSF4JResponseBuilderFactory pickIdentityResponseFactory(FrameworkException ex) {

        return dataHolder.getResponseFactoryList().stream()
                .filter(x -> x.canHandle(ex))
                .findFirst()
                .orElseThrow(() -> new FrameworkRuntimeException("Cannot find a response factory to handle the " +
                        "FrameworkException."));
    }


    /**
     * Get the {@link IdentityProcessCoordinator} service that will co-ordinate the IdentityRequest flow.
     *
     * @return @{@link IdentityProcessCoordinator} OSGi service.
     */
    public static IdentityProcessCoordinator getIdentityProcessCoordinator() {

        return dataHolder.getProcessCoordinator();
    }


    /**
     * Read the request body of an {@link Request} as a String.
     *
     * @param request @{@link Request} to read the body from
     * @return the body of the request as a String.
     */
    public static String readRequestBody(Request request) {
        // TODO : we should read from the request, had a problem with thread getting stuck when reading the body so
        // used this workaround for now
        return String.valueOf(request.getProperty("body"));
    }
}
