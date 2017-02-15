/*
 *
 *  * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  *
 *  * WSO2 Inc. licenses this file to you under the Apache License,
 *  * Version 2.0 (the "License"); you may not use this file except
 *  * in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 */

package org.wso2.carbon.identity.gateway.api.response;

import org.wso2.carbon.identity.common.base.handler.AbstractHandler;
import org.wso2.carbon.identity.gateway.api.exception.FrameworkRuntimeException;
import org.wso2.carbon.identity.gateway.api.exception.FrameworkServerException;

import javax.ws.rs.core.NewCookie;

public abstract class HttpIdentityResponseFactory extends AbstractHandler {

    public boolean canHandle(IdentityResponse identityResponse) {
        return false;
    }

    public boolean canHandle(FrameworkServerException exception) {
        return false;
    }

    public boolean canHandle(FrameworkRuntimeException exception) {
        return false;
    }

    public HttpIdentityResponse.HttpIdentityResponseBuilder create(IdentityResponse identityResponse) {
        HttpIdentityResponse.HttpIdentityResponseBuilder builder = new HttpIdentityResponse
                .HttpIdentityResponseBuilder();
        create(builder, identityResponse);
        return builder;
    }

    public void create(HttpIdentityResponse.HttpIdentityResponseBuilder builder, IdentityResponse identityResponse) {
        builder.addHeader("SIOWTOSW", identityResponse.getSessionKey());
    }

    public HttpIdentityResponse.HttpIdentityResponseBuilder handleException(FrameworkServerException exception) {

        HttpIdentityResponse.HttpIdentityResponseBuilder builder =
                new HttpIdentityResponse.HttpIdentityResponseBuilder();
        builder.setStatusCode(500);
        builder.setBody(exception.getMessage());
        return builder;
    }

    public HttpIdentityResponse.HttpIdentityResponseBuilder handleException(RuntimeException exception) {

        HttpIdentityResponse.HttpIdentityResponseBuilder builder
                = new HttpIdentityResponse.HttpIdentityResponseBuilder();
        builder.setStatusCode(500);
        return builder;
    }
}
