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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.framework.IdentityProcessCoordinator;
import org.wso2.carbon.identity.framework.exception.FrameworkException;
import org.wso2.carbon.identity.framework.message.IdentityResponse;
import org.wso2.carbon.identity.gateway.resource.MSF4JIdentityRequestFactory;
import org.wso2.carbon.identity.gateway.resource.MSF4JResponseFactory;
import org.wso2.carbon.identity.gateway.resource.internal.DataHolder;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.util.BufferUtil;

import java.nio.ByteBuffer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.ws.rs.core.HttpHeaders.CONTENT_LENGTH;

public class GatewayHelper {

    private DataHolder dataHolder = DataHolder.getInstance();
    private static GatewayHelper instance = new GatewayHelper();

    private Logger logger = LoggerFactory.getLogger(GatewayHelper.class);

    private GatewayHelper() {
    }

    public static GatewayHelper getInstance() {
        return instance;
    }

    public MSF4JIdentityRequestFactory pickRequestFactory(Request request) {

        return dataHolder.getRequestFactoryList().stream()
                .filter(x -> x.canHandle(request))
                .findFirst()
                .orElse(null);
    }


    public MSF4JResponseFactory pickIdentityResponseFactory(IdentityResponse identityResponse) {

        return dataHolder.getResponseFactoryList().stream()
                .filter(x -> x.canHandle(identityResponse))
                .findFirst()
                .orElse(null);
    }

    public MSF4JResponseFactory pickIdentityResponseFactory(FrameworkException ex) {

        return dataHolder.getResponseFactoryList().stream()
                .filter(x -> x.canHandle(ex))
                .findFirst()
                .orElse(null);
    }

    public IdentityProcessCoordinator getIdentityProcessCoordinator() {
        return dataHolder.getProcessCoordinator();
    }

    public String readRequestBody(Request request) {

//        String contentLengthValue = request.getHeader(CONTENT_LENGTH);
//
//        if (contentLengthValue != null) {
//            ByteBuffer byteBuffer = BufferUtil.merge(request.getFullMessageBody());
//            return new String(byteBuffer.array(), UTF_8);
//        }
        return String.valueOf(request.getProperty("body"));
//        return "";
    }
}
