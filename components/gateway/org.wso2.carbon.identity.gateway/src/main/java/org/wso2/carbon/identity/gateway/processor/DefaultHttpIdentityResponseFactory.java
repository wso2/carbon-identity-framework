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

package org.wso2.carbon.identity.gateway.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.framework.exception.FrameworkException;
import org.wso2.carbon.identity.framework.response.IdentityResponse;
import org.wso2.carbon.identity.framework.response.factory.HttpIdentityResponseFactory;

import static org.wso2.carbon.identity.framework.response.HttpIdentityResponse.HttpIdentityResponseBuilder;

/*
    Dummy Implementation of HttpIdentityResponseFactory for demo purpose.
 */
public class DefaultHttpIdentityResponseFactory extends HttpIdentityResponseFactory {

    private static final String DEFAULT_HTTP_RESPONSE_FACTORY = "DefaultHttpIdentityResponseFactory";
    private static final Logger log = LoggerFactory.getLogger(DefaultHttpIdentityResponseFactory.class);

    @Override
    public String getName() {
        return DEFAULT_HTTP_RESPONSE_FACTORY;
    }

    @Override
    public HttpIdentityResponseBuilder create(IdentityResponse identityResponse) {
        HttpIdentityResponseBuilder responseBuilder = new HttpIdentityResponseBuilder();
        responseBuilder.setStatusCode(200);
        responseBuilder.setBody("Hello from Identity Framework");

        if (log.isDebugEnabled()) {
            log.debug(getName() + " built the HTTPResponse from the IdentityResponse.");
        }

        return responseBuilder;
    }

    @Override
    public HttpIdentityResponseBuilder create(HttpIdentityResponseBuilder builder, IdentityResponse identityResponse) {

        builder.setBody("Hello there!!!!");
        builder.setStatusCode(200);
        return builder;
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public boolean canHandle(IdentityResponse identityResponse) {
        return true;
    }

    @Override
    public boolean canHandle(FrameworkException exception) {
        return true;
    }
}
