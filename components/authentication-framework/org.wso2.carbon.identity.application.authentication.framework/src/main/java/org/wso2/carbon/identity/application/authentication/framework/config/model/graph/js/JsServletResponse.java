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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * Javascript wrapper for Java level HttpServletResponse.
 * This provides controlled access to HttpServletResponse object via provided javascript native syntax.
 * e.g
 * response.headers.["Set-Cookie"] = ['crsftoken=xxxxxssometokenxxxxx']
 * <p>
 * instead of
 * context.getResponse().addCookie(cookie);
 * <p>
 * Also it prevents writing an arbitrary values to the respective fields, keeping consistency on runtime HttpServletResponse.
 */
public class JsServletResponse extends AbstractJSObjectWrapper<TransientObjectWrapper<HttpServletResponse>> {

    public JsServletResponse(TransientObjectWrapper<HttpServletResponse> wrapped) {

        super(wrapped);
    }

    @Override
    public Object getMember(String name) {

        switch (name) {
        case FrameworkConstants.JSAttributes.JS_HEADERS:
            Map headers = new HashMap();
            Collection<String> headerNames = getResponse().getHeaderNames();
            if (headerNames != null) {
                for (String element : headerNames) {
                    headers.put(element, getResponse().getHeader(element));
                }
            }
            return new JsHeaders(headers, getResponse());
        default:
            return super.getMember(name);
        }
    }

    @Override
    public boolean hasMember(String name) {

        if (getResponse() == null) {
            //Transient Object is null, hence no member access is possible.
            return false;
        }

        switch (name) {
        case FrameworkConstants.JSAttributes.JS_HEADERS:
            return getResponse().getHeaderNames() != null;
        default:
            return super.hasMember(name);
        }
    }

    private HttpServletResponse getResponse() {

        TransientObjectWrapper<HttpServletResponse> transientObjectWrapper = getWrapped();
        return transientObjectWrapper.getWrapped();
    }

    public void addCookie(Cookie cookie) {
        getResponse().addCookie(cookie);
    }
}
