/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.nashorn.JsNashornAuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.nashorn.JsNashornAuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.nashorn.JsNashornCookie;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.nashorn.JsNashornParameters;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.nashorn.JsNashornServletRequest;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.nashorn.JsNashornServletResponse;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.nashorn.JsNashornWritableParameters;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.TransientObjectWrapper;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;

import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Factory to create a Javascript Object Wrappers for Nashorn execution.
 */
public class JsWrapperFactory implements JsWrapperBaseFactory {

    @Override
    public JsNashornAuthenticatedUser createJsAuthenticatedUser(AuthenticatedUser authenticatedUser) {

        return new JsNashornAuthenticatedUser(authenticatedUser);
    }

    @Override
    public JsNashornAuthenticatedUser createJsAuthenticatedUser(AuthenticationContext authenticationContext,
                                                             AuthenticatedUser authenticatedUser) {

        return new JsNashornAuthenticatedUser(authenticationContext, authenticatedUser);
    }
    @Override
    public JsNashornAuthenticationContext createJsAuthenticationContext(AuthenticationContext authenticationContext) {

        return new JsNashornAuthenticationContext(authenticationContext);
    }

    @Override
    public JsNashornCookie createJsCookie(Cookie cookie) {

        return new JsNashornCookie(cookie);
    }

    @Override
    public JsNashornParameters createJsParameters(Map parameters) {

        return new JsNashornParameters(parameters);
    }

    @Override
    public JsNashornWritableParameters createJsWritableParameters(Map data) {

        return new JsNashornWritableParameters(data);
    }

    @Override
    public JsNashornServletRequest createJsServletRequest(TransientObjectWrapper<HttpServletRequest> wrapped) {

        return new JsNashornServletRequest(wrapped);
    }

    @Override
    public JsNashornServletResponse createJsServletResponse(TransientObjectWrapper<HttpServletResponse> wrapped) {

        return new JsNashornServletResponse(wrapped);
    }
}
