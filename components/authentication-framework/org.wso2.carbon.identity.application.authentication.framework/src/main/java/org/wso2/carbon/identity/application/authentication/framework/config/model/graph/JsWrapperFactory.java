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

import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsAuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsAuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsCookie;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsParameters;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsServletRequest;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsServletResponse;
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
public interface JsWrapperFactory {

    /**
     * Creates a JavaScript Proxy for authenticated User.
     * @param authenticatedUser - Represent Authenticated Subject
     * @return Proxy for authenticated User
     */
    JsAuthenticatedUser createJsAuthenticatedUser(AuthenticatedUser authenticatedUser);

    /**
     * Creates a JavaScript Proxy for authentication Context.
     * @param authenticationContext - Represent Authentication Request data from servlet
     * @return Proxy for authentication Context
     */
    JsAuthenticationContext createJsAuthenticationContext(AuthenticationContext authenticationContext);

    /**
     * Creates a JavaScript Proxy for Java Level Cookie.
     * @param cookie - Java Level Cookie
     * @return Proxy for Cookie
     */
    JsCookie createJsCookie(Cookie cookie);

    /**
     * Creates a JavaScript Proxy for Servlet Request HTTP Headers.
     * @param parameters -HTTP Headers in Servlet Request
     * @return Proxy for HTTP headers/Cookies in Servlet Request
     */
    JsParameters createJsParameters(Map parameters);

    /**
     * Creates a JavaScript Proxy for Writable Servlet Request HTTP Headers.
     * @param data - HTTP Headers in Servlet Request
     * @return Proxy for HTTP headers/Cookies in Servlet Request
     */
    JsParameters createJsWritableParameters(Map data);

    /**
     * Creates a JavaScript Proxy for Servlet Request.
     * @param wrapped - Wrapped Servlet Request
     * @return Proxy for wrapped Servlet Request
     */
    JsServletRequest createJsServletRequest(TransientObjectWrapper<HttpServletRequest> wrapped);

    /**
     * Creates a JavaScript Proxy for Servlet Response.
     * @param wrapped Wrapped Servlet Response
     * @return Proxy for wrapped Servlet Response
     */
    JsServletResponse createJsServletResponse(TransientObjectWrapper<HttpServletResponse> wrapped);

    /**
     * Creates JavaScript Function Wrapper.
     * @param source - JavaScript Raw String Source
     * @param isFunction - Executability of source
     * @return JavaScript Function Wrapper
     */
    SerializableJsFunction createSerializableFunction(String source, boolean isFunction);

}
