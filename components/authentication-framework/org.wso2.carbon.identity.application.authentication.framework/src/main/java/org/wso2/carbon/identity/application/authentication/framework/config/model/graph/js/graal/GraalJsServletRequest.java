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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.graal;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.base.JsBaseServletRequest;
import org.wso2.carbon.identity.application.authentication.framework.context.TransientObjectWrapper;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * Javascript wrapper for Java level HTTPServletRequest for GraalJs Execution.
 * This provides controlled access to HTTPServletRequest object via provided javascript native syntax.
 * e.g
 * var redirect_uri = context.request.params.redirect_uri
 * <p>
 * instead of
 * var userName = context.getRequest().getParameter("redirect_uri)
 * <p>
 * Also it prevents writing an arbitrary values to the respective fields,
 * keeping consistency on runtime HTTPServletRequest.
 */
public class GraalJsServletRequest extends JsBaseServletRequest implements ProxyObject {

    public GraalJsServletRequest(TransientObjectWrapper<HttpServletRequest> wrapped) {

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
                return new GraalJsWritableParameters(headers);
            case FrameworkConstants.JSAttributes.JS_PARAMS:
                return new GraalJsParameters(getRequest().getParameterMap());
            case FrameworkConstants.JSAttributes.JS_COOKIES:
                Map cookies = new HashMap();
                Cookie[] cookieArr = getRequest().getCookies();
                if (cookieArr != null) {
                    for (Cookie cookie : cookieArr) {
                        cookies.put(cookie.getName(), new GraalJsCookie(cookie));
                    }
                }
                return new GraalJsWritableParameters(cookies);
            case FrameworkConstants.JSAttributes.JS_REQUEST_IP:
                return IdentityUtil.getClientIpAddress(getRequest());
            default:
                return super.getMember(name);
        }
    }

    @Override
    public Object getMemberKeys() {

        return null;
    }

    @Override
    public boolean hasMember(String name) {

        if (getRequest() == null) {
            //Transient Object is null, hence no member access is possible.
            return false;
        }

        switch (name) {
            case FrameworkConstants.JSAttributes.JS_HEADERS:
            case FrameworkConstants.JSAttributes.JS_COOKIES:
            case FrameworkConstants.JSAttributes.JS_REQUEST_IP:
                return true;
            case FrameworkConstants.JSAttributes.JS_PARAMS:
                return getRequest().getParameterMap() != null;
            default:
                return super.hasMember(name);
        }
    }

    @Override
    public void putMember(String key, Value value) {

        LOG.warn("Unsupported operation. Servlet Request is read only. Can't remove parameter " + key);
    }

    @Override
    public boolean removeMember(String key) {

        return false;
    }
}
