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

import javax.ws.rs.core.Response.ResponseBuilder;

/**
 * This Factory produces a {@link ResponseBuilder} based on the {@link IdentityResponse} returned by the Framework.
 */
public interface MSF4JResponseBuilderFactory {

    String getName();

    int getPriority();

    boolean canHandle(IdentityResponse identityResponse);

    boolean canHandle(FrameworkException exception);

    ResponseBuilder create(IdentityResponse identityResponse);

    ResponseBuilder create(ResponseBuilder builder, IdentityResponse identityResponse);

    ResponseBuilder handleException(FrameworkException exception);
}
