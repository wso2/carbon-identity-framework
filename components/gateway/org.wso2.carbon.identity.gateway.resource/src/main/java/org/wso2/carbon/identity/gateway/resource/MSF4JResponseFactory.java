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

import org.wso2.carbon.identity.framework.exception.FrameworkException;
import org.wso2.carbon.identity.framework.message.IdentityResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

public class MSF4JResponseFactory {

    public String getName() {
        return null;
    }

    public int getPriority() {
        return 0;
    }

    public boolean canHandle(IdentityResponse identityResponse) {
        return true;
    }

    public boolean canHandle(FrameworkException exception) {
        return true;
    }

    public ResponseBuilder create(IdentityResponse identityResponse) {

        ResponseBuilder builder = Response.status(identityResponse.getStatusCode());
        return create(builder, identityResponse);
    }

    public ResponseBuilder create(ResponseBuilder builder, IdentityResponse identityResponse) {

        identityResponse.getHeaderMap().forEach(builder::header);
        builder.status(identityResponse.getStatusCode());
        builder.entity(identityResponse.getBody());
        return builder;
    }

    public ResponseBuilder handleException(FrameworkException exception) {
        return Response.status(500).entity(exception.getMessage());
    }


}
