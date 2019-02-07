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

import org.wso2.carbon.identity.application.authentication.framework.context.TransientObjectWrapper;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

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
public class JsServletRequest extends AbstractJSObjectWrapper<TransientObjectWrapper<HttpServletRequest>> {

    public JsServletRequest(TransientObjectWrapper<HttpServletRequest> wrapped) {

        super(wrapped);
    }

    @Override
    public Object getMember(String name) {

        switch (name) {
            case FrameworkConstants.JSAttributes.JS_HEADERS:
                Map headers = new HashMap();
                Enumeration<String> headerNames = getRequest().getHeaderNames();
                if (headerNames != null) {
                    while (headerNames.hasMoreElements()) {
                        String headerName = headerNames.nextElement();
                        headers.put(headerName, getRequest().getHeader(headerName));
                    }
                }
                return new JsWritableParameters(headers);
            case FrameworkConstants.JSAttributes.JS_PARAMS:
                return new JsParameters(getRequest().getParameterMap());
            case FrameworkConstants.JSAttributes.JS_COOKIES:
                Map cookies = new HashMap();
                Cookie[] cookieArr = getRequest().getCookies();
                if (cookieArr != null) {
                    for (Cookie cookie : cookieArr) {
                        cookies.put(cookie.getName(), new JsCookie(cookie));
                    }
                }
                return new JsWritableParameters(cookies);
            case FrameworkConstants.JSAttributes.JS_REQUEST_IP:
                return IdentityUtil.getClientIpAddress(getRequest());
            default:
                return super.getMember(name);
        }
    }

    @Override
    public boolean hasMember(String name) {

        if (getRequest() == null) {
            //Transient Object is null, hence no member access is possible.
            return false;
        }

        switch (name) {
            case FrameworkConstants.JSAttributes.JS_HEADERS:
                return getRequest().getHeaderNames() != null;
            case FrameworkConstants.JSAttributes.JS_PARAMS:
                return getRequest().getParameterMap() != null;
            case FrameworkConstants.JSAttributes.JS_COOKIES:
                return getRequest().getCookies() != null;
            default:
                return super.hasMember(name);
        }
    }

    private HttpServletRequest getRequest() {

        TransientObjectWrapper<HttpServletRequest> transientObjectWrapper = getWrapped();
        return transientObjectWrapper.getWrapped();
    }
}

