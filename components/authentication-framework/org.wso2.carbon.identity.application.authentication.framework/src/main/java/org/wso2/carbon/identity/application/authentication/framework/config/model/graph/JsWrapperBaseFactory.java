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

import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.base.JsBaseAuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.base.JsBaseAuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.base.JsBaseCookie;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.base.JsBaseParameters;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.base.JsBaseServletRequest;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.base.JsBaseServletResponse;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.TransientObjectWrapper;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;

import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Interface to create Js Wrapper objects.
 */
public interface JsWrapperBaseFactory {

    /**
     * Creates a JavaScript Proxy for authenticated User.
     * @param authenticatedUser - Represent Authenticated Subject
     * @return Proxy for authenticated User
     */
    JsBaseAuthenticatedUser createJsAuthenticatedUser(AuthenticatedUser authenticatedUser);

    /**
     * Creates a JavaScript Proxy for authenticated User.
     * @param authenticatedUser - Represent Authenticated Subject
     * @param  authenticationContext = Represent Authentication Context
     * @return Proxy for authenticated User
     */
    JsBaseAuthenticatedUser createJsAuthenticatedUser(AuthenticationContext authenticationContext,
                                                      AuthenticatedUser authenticatedUser);


    /**
     * Creates a JavaScript Proxy for authentication Context.
     * @param authenticationContext - Represent Authentication Request data from servlet
     * @return Proxy for authentication Context
     */
    JsBaseAuthenticationContext createJsAuthenticationContext(AuthenticationContext authenticationContext);

    /**
     * Creates a JavaScript Proxy for Java Level Cookie.
     * @param cookie - Java Level Cookie
     * @return Proxy for Cookie
     */
    JsBaseCookie createJsCookie(Cookie cookie);

    /**
     * Creates a JavaScript Proxy for Servlet Request HTTP Headers.
     * @param parameters -HTTP Headers in Servlet Request
     * @return Proxy for HTTP headers/Cookies in Servlet Request
     */
    JsBaseParameters createJsParameters(Map parameters);

    /**
     * Creates a JavaScript Proxy for Writable Servlet Request HTTP Headers.
     * @param data - HTTP Headers in Servlet Request
     * @return Proxy for HTTP headers/Cookies in Servlet Request
     */
    JsBaseParameters createJsWritableParameters(Map data);

    /**
     * Creates a JavaScript Proxy for Servlet Request.
     * @param wrapped - Wrapped Servlet Request
     * @return Proxy for wrapped Servlet Request
     */
    JsBaseServletRequest createJsServletRequest(TransientObjectWrapper<HttpServletRequest> wrapped);

    /**
     * Creates a JavaScript Proxy for Servlet Response.
     * @param wrapped Wrapped Servlet Response
     * @return Proxy for wrapped Servlet Response
     */
    JsBaseServletResponse createJsServletResponse(TransientObjectWrapper<HttpServletResponse> wrapped);
}
