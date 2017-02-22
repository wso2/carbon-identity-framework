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

import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import org.wso2.carbon.identity.common.base.handler.AbstractHandler;
import org.wso2.carbon.identity.gateway.api.exception.GatewayRuntimeException;
import org.wso2.carbon.identity.gateway.api.exception.GatewayServerException;
import org.wso2.carbon.identity.gateway.common.util.Constants;

import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public abstract class GatewayResponseBuilderFactory extends AbstractHandler {

    public boolean canHandle(GatewayResponse gatewayResponse) {
        return false;
    }

    public boolean canHandle(GatewayServerException exception) {
        return false;
    }

    public boolean canHandle(GatewayRuntimeException exception) {
        return false;
    }

    public HttpGatewayResponse.HttpIdentityResponseBuilder create(GatewayResponse gatewayResponse) {
        HttpGatewayResponse.HttpIdentityResponseBuilder builder = new HttpGatewayResponse
                .HttpIdentityResponseBuilder();
        create(builder, gatewayResponse);
        return builder;
    }

    public void create(HttpGatewayResponse.HttpIdentityResponseBuilder builder, GatewayResponse gatewayResponse) {
        Cookie cookie = new DefaultCookie(Constants.GATEWAY_COOKIE, gatewayResponse.getSessionKey());
        cookie.setPath("/");
        builder.addHeader("Set-Cookie", cookie);
    }

    public Response.ResponseBuilder createBuilder(GatewayResponse gatewayResponse) {
        Response.ResponseBuilder builder = Response.noContent();
        createBuilder(builder, gatewayResponse);
        return builder;
    }

    public void createBuilder(Response.ResponseBuilder builder, GatewayResponse gatewayResponse) {
        javax.ws.rs.core.Cookie cookie1 = new javax.ws.rs.core.Cookie(Constants.GATEWAY_COOKIE, gatewayResponse
                .getSessionKey(),"/","");
        builder.cookie(new NewCookie(cookie1));
    }

    public Response.ResponseBuilder handleException(GatewayServerException exception) {

        Response.ResponseBuilder builder = Response.noContent();
        builder.status(500);
        builder.entity(exception.getMessage());
        return builder;
    }

    public HttpGatewayResponse.HttpIdentityResponseBuilder handleException(RuntimeException exception) {

        HttpGatewayResponse.HttpIdentityResponseBuilder builder
                = new HttpGatewayResponse.HttpIdentityResponseBuilder();
        builder.setStatusCode(500);
        return builder;
    }


}
