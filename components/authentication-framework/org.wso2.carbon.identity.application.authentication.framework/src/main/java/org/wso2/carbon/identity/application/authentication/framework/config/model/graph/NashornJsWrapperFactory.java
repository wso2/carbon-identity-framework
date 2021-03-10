/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph;

import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.graal.GraalSerializableJsFunction;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.nashorn.NashornJsAuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.nashorn.NashornJsAuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.nashorn.NashornJsCookie;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.nashorn.NashornJsParameters;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.nashorn.NashornJsServletRequest;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.nashorn.NashornJsServletResponse;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.nashorn.NashornJsWritableParameters;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.TransientObjectWrapper;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;

import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Factory to create a Javascript Object Wrappers for GraalJs execution.
 */
public class NashornJsWrapperFactory implements JsWrapperFactory {

    @Override
    public NashornJsAuthenticatedUser createJsAuthenticatedUser(AuthenticatedUser authenticatedUser) {

        return new NashornJsAuthenticatedUser(authenticatedUser);
    }

    @Override
    public NashornJsAuthenticationContext createJsAuthenticationContext(AuthenticationContext authenticationContext) {

        return new NashornJsAuthenticationContext(authenticationContext);
    }

    @Override
    public NashornJsCookie createJsCookie(Cookie cookie) {

        return new NashornJsCookie(cookie);
    }

    @Override
    public NashornJsParameters createJsParameters(Map parameters) {

        return new NashornJsParameters(parameters);
    }

    @Override
    public NashornJsWritableParameters createJsWritableParameters(Map data) {

        return new NashornJsWritableParameters(data);
    }

    @Override
    public NashornJsServletRequest createJsServletRequest(TransientObjectWrapper<HttpServletRequest> wrapped) {

        return new NashornJsServletRequest(wrapped);
    }

    @Override
    public NashornJsServletResponse createJsServletResponse(TransientObjectWrapper<HttpServletResponse> wrapped) {

        return new NashornJsServletResponse(wrapped);
    }

    @Override
    public GraalSerializableJsFunction createSerializableFunction(String source, boolean isFunction) {

        return new GraalSerializableJsFunction(source, isFunction);
    }
}
