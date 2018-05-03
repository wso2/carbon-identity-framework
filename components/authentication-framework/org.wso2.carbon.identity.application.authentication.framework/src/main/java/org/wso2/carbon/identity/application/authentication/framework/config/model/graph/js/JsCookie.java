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

/**
 * Javascript wrapper for Java level Cookie.
 * This provides controlled access to Cookie object via provided javascript native syntax.
 * e.g
 * var commonAuthIdDomain = context.request.cookies.commonAuthId.domain
 * Also it prevents writing an arbitrary values to the respective fields, keeping consistency on runtime Cookie.
 */
public class JsCookie extends AbstractJSObjectWrapper<Cookie> {

    public JsCookie(Cookie cookie) {
        super(cookie);
    }

    @Override
    public Object getMember(String name) {

        switch (name) {
            case FrameworkConstants.JSAttributes.JS_COOKIE_NAME:
                return getWrapped().getName();
            case FrameworkConstants.JSAttributes.JS_COOKIE_VALUE:
                return getWrapped().getValue();
            case FrameworkConstants.JSAttributes.JS_COOKIE_COMMENT:
                return getWrapped().getComment();
            case FrameworkConstants.JSAttributes.JS_COOKIE_DOMAIN:
                return getWrapped().getDomain();
            case FrameworkConstants.JSAttributes.JS_COOKIE_MAX_AGE:
                return getWrapped().getMaxAge();
            case FrameworkConstants.JSAttributes.JS_COOKIE_PATH:
                return getWrapped().getPath();
            case FrameworkConstants.JSAttributes.JS_COOKIE_SECURE:
                return getWrapped().getSecure();
            case FrameworkConstants.JSAttributes.JS_COOKIE_VERSION:
                return getWrapped().getVersion();
            case FrameworkConstants.JSAttributes.JS_COOKIE_HTTP_ONLY:
                return getWrapped().isHttpOnly();
            default:
                return super.getMember(name);
        }
    }

    @Override
    public boolean hasMember(String name) {

        switch (name) {
            case FrameworkConstants.JSAttributes.JS_COOKIE_NAME:
                return getWrapped().getName() != null;
            case FrameworkConstants.JSAttributes.JS_COOKIE_VALUE:
                return getWrapped().getValue() != null;
            case FrameworkConstants.JSAttributes.JS_COOKIE_COMMENT:
                return getWrapped().getComment() != null;
            case FrameworkConstants.JSAttributes.JS_COOKIE_DOMAIN:
                return getWrapped().getDomain() != null;
            case FrameworkConstants.JSAttributes.JS_COOKIE_MAX_AGE:
                return getWrapped().getMaxAge() != -1;
            case FrameworkConstants.JSAttributes.JS_COOKIE_PATH:
                return getWrapped().getPath() != null;
            case FrameworkConstants.JSAttributes.JS_COOKIE_VERSION:
                return getWrapped().getVersion() != 0;
            default:
                return super.hasMember(name);
        }
    }
}
