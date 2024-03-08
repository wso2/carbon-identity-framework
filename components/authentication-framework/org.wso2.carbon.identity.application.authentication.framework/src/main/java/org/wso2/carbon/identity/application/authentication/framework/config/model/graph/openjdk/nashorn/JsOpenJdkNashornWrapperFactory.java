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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.openjdk.nashorn;

import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JsWrapperBaseFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.openjdk.nashorn.JsOpenJdkNashornAuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.openjdk.nashorn.JsOpenJdkNashornAuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.openjdk.nashorn.JsOpenJdkNashornClaims;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.openjdk.nashorn.JsOpenJdkNashornCookie;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.openjdk.nashorn.JsOpenJdkNashornHeaders;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.openjdk.nashorn.JsOpenJdkNashornParameters;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.openjdk.nashorn.JsOpenJdkNashornRuntimeClaims;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.openjdk.nashorn.JsOpenJdkNashornServletRequest;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.openjdk.nashorn.JsOpenJdkNashornServletResponse;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.openjdk.nashorn.JsOpenJdkNashornStep;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.openjdk.nashorn.JsOpenJdkNashornSteps;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.openjdk.nashorn.JsOpenJdkNashornWritableParameters;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.TransientObjectWrapper;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;

import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Factory to create a Javascript Object Wrappers for OpenJDk.Nashorn execution.
 * Since Nashorn is deprecated in JDK 11 and onwards. We are introducing OpenJDK Nashorn engine.
 */
public class JsOpenJdkNashornWrapperFactory implements JsWrapperBaseFactory {

    @Override
    public JsOpenJdkNashornAuthenticatedUser createJsAuthenticatedUser(AuthenticatedUser authenticatedUser) {

        return new JsOpenJdkNashornAuthenticatedUser(authenticatedUser);
    }

    @Override
    public JsOpenJdkNashornAuthenticatedUser createJsAuthenticatedUser(AuthenticationContext authenticationContext,
                                                             AuthenticatedUser authenticatedUser) {

        return new JsOpenJdkNashornAuthenticatedUser(authenticationContext, authenticatedUser);
    }

    @Override
    public JsOpenJdkNashornAuthenticatedUser createJsAuthenticatedUser(AuthenticationContext context,
                                                             AuthenticatedUser wrappedUser, int step, String idp) {

        return new JsOpenJdkNashornAuthenticatedUser(context, wrappedUser, step, idp);
    }

    @Override
    public JsOpenJdkNashornAuthenticationContext createJsAuthenticationContext
            (AuthenticationContext authenticationContext) {

        return new JsOpenJdkNashornAuthenticationContext(authenticationContext);
    }

    @Override
    public JsOpenJdkNashornCookie createJsCookie(Cookie cookie) {

        return new JsOpenJdkNashornCookie(cookie);
    }

    @Override
    public JsOpenJdkNashornParameters createJsParameters(Map parameters) {

        return new JsOpenJdkNashornParameters(parameters);
    }

    @Override
    public JsOpenJdkNashornWritableParameters createJsWritableParameters(Map data) {

        return new JsOpenJdkNashornWritableParameters(data);
    }

    @Override
    public JsOpenJdkNashornServletRequest createJsServletRequest(TransientObjectWrapper<HttpServletRequest> wrapped) {

        return new JsOpenJdkNashornServletRequest(wrapped);
    }

    @Override
    public JsOpenJdkNashornServletResponse createJsServletResponse
            (TransientObjectWrapper<HttpServletResponse> wrapped) {

        return new JsOpenJdkNashornServletResponse(wrapped);
    }

    @Override
    public JsOpenJdkNashornClaims createJsClaims(AuthenticationContext context, int step, String idp,
                                       boolean isRemoteClaimRequest) {

        return new JsOpenJdkNashornClaims(context, step, idp, isRemoteClaimRequest);
    }

    @Override
    public JsOpenJdkNashornClaims createJsClaims(AuthenticationContext context, AuthenticatedUser user,
                                       boolean isRemoteClaimRequest) {

        return new JsOpenJdkNashornClaims(context, user, isRemoteClaimRequest);
    }

    @Override
    public JsOpenJdkNashornRuntimeClaims createJsRuntimeClaims(AuthenticationContext context, int step, String idp) {

        return new JsOpenJdkNashornRuntimeClaims(context, step, idp);
    }

    @Override
    public JsOpenJdkNashornRuntimeClaims createJsRuntimeClaims(AuthenticationContext context, AuthenticatedUser user) {

        return new JsOpenJdkNashornRuntimeClaims(context, user);
    }

    @Override
    public JsOpenJdkNashornStep createJsStep(AuthenticationContext context, int step, String authenticatedIdp,
                                   String authenticatedAuthenticator) {

        return new JsOpenJdkNashornStep(context, step, authenticatedIdp, authenticatedAuthenticator);
    }

    @Override
    public JsOpenJdkNashornHeaders createJsHeaders(Map wrapped, HttpServletResponse response) {

        return new JsOpenJdkNashornHeaders(wrapped, response);
    }

    @Override
    public JsOpenJdkNashornSteps createJsSteps(AuthenticationContext context) {

        return new JsOpenJdkNashornSteps(context);
    }
}
