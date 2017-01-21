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

import java.util.Properties;

public abstract class HttpIdentityResponseFactory {

    protected Properties properties;

    public void init(Properties properties) throws FrameworkRuntimeException {
        this.properties = properties;
    }

    public abstract String getName();

    public int getPriority() {
        return 0;
    }

    public boolean canHandle(IdentityResponse identityResponse) {
        return false;
    }

    public boolean canHandle(FrameworkException exception) {
        return false;
    }

    public abstract HttpIdentityResponse.HttpIdentityResponseBuilder create(IdentityResponse identityResponse);

    public abstract HttpIdentityResponse.HttpIdentityResponseBuilder create(HttpIdentityResponse.HttpIdentityResponseBuilder builder, IdentityResponse identityResponse);

    public HttpIdentityResponse.HttpIdentityResponseBuilder handleException(FrameworkException exception) {

        HttpIdentityResponse.HttpIdentityResponseBuilder builder =
                new HttpIdentityResponse.HttpIdentityResponseBuilder();
        builder.setStatusCode(500);
        builder.setBody(exception.getMessage());
        return builder;
    }
}
