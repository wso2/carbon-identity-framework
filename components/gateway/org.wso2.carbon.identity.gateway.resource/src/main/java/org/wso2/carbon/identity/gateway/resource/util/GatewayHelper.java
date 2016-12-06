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
import org.wso2.carbon.identity.framework.exception.FrameworkException;
import org.wso2.carbon.identity.framework.message.IdentityResponse;
import org.wso2.carbon.identity.gateway.resource.MSF4JIdentityRequestFactory;
import org.wso2.carbon.identity.gateway.resource.MSF4JResponseFactory;
import org.wso2.carbon.identity.gateway.resource.internal.DataHolder;
import org.wso2.msf4j.Request;

public class GatewayHelper {

    private DataHolder dataHolder = DataHolder.getInstance();
    private static GatewayHelper instance = new GatewayHelper();

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
}
