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

import org.wso2.carbon.identity.framework.FrameworkException;
import org.wso2.carbon.identity.framework.message.Response;

import javax.ws.rs.core.Response.ResponseBuilder;

/**
 * Default factory implementation of {@link GatewayResponseBuilderFactory}
 */
public class GatewayResponseBuilderFactory {

    public String getName() {

        return getClass().getSimpleName();
    }

    public int getPriority() {

        return 100;
    }

    public boolean canHandle(Response response) {

        return true;
    }

    public boolean canHandle(FrameworkException exception) {

        return true;
    }

    public ResponseBuilder create(Response response) {

        ResponseBuilder builder = javax.ws.rs.core.Response.status(response.getStatusCode());
        return create(builder, response);
    }

    public ResponseBuilder create(ResponseBuilder builder, Response response) {

        response.getHeaderMap().forEach(builder::header);
        builder.status(response.getStatusCode());
        builder.entity(response.getBody());
        return builder;
    }

    public ResponseBuilder handleException(FrameworkException exception) {

        return javax.ws.rs.core.Response.status(500).entity(exception.getMessage());
    }


}
