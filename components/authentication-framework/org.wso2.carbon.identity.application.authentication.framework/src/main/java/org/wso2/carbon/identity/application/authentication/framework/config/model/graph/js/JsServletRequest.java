/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js;

import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Javascript wrapper for Java level HTTPServletRequest.
 * This provides controlled access to HTTPServletRequest object via provided javascript native syntax.
 * e.g
 * var redirect_uri = context.request.params.redirect_uri
 * <p>
 * instead of
 * var userName = context.getRequest().getParameter("redirect_uri)
 * <p>
 * Also it prevents writing an arbitrary values to the respective fields, keeping consistency on runtime HTTPServletRequest.
 */
public class JsServletRequest extends AbstractJSObjectWrapper<HttpServletRequest> {

    public JsServletRequest(HttpServletRequest wrapped) {
        super(wrapped);
    }

    @Override
    public Object getMember(String name) {

        switch (name) {
            case FrameworkConstants.JSAttributes.JS_HEADERS:
                Map headers = new HashMap();
                Enumeration<String> headerNames = getWrapped().getHeaderNames();
                if (headerNames != null) {
                    while (headerNames.hasMoreElements()) {
                        String headerName = headerNames.nextElement();
                        headers.put(headerName, getWrapped().getHeader(headerName));
                    }
                }
                return new JsParameters(headers);
            case FrameworkConstants.JSAttributes.JS_PARAMS:
                return new JsParameters(getWrapped().getParameterMap());
            case FrameworkConstants.JSAttributes.JS_COOKIES:
                Map cookies = new HashMap();
                Cookie[] cookieArr = getWrapped().getCookies();
                if(cookieArr != null) {
                    for (Cookie cookie : cookieArr) {
                        cookies.put(cookie.getName(), new JsCookie(cookie));
                    }
                }
                return new JsParameters(cookies);
            default:
                return super.getMember(name);
        }
    }

    @Override
    public boolean hasMember(String name) {
        switch (name) {
            case FrameworkConstants.JSAttributes.JS_HEADERS:
                return getWrapped().getHeaderNames() != null;
            case FrameworkConstants.JSAttributes.JS_PARAMS:
                return getWrapped().getParameterMap() != null;
            case FrameworkConstants.JSAttributes.JS_COOKIES:
                return getWrapped().getCookies() != null;
            default:
                return super.hasMember(name);
        }
    }
}

