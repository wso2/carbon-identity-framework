/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.core.model;

import javax.servlet.http.Cookie;


public class CookieBuilder {

    private String name;
    private String value;
    private String comment;
    private String domain;
    private int maxAge = -1;
    private String path = "/";
    private boolean secure = true;
    private int version = 0;
    private boolean isHttpOnly = true;

    public CookieBuilder(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public CookieBuilder setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public CookieBuilder setDomain(String domain) {
        this.domain = domain;
        return this;
    }

    public CookieBuilder setMaxAge(int maxAge) {
        this.maxAge = maxAge;
        return this;
    }

    public CookieBuilder setPath(String path) {
        this.path = path;
        return this;
    }

    public CookieBuilder setSecure(boolean secure) {
        this.secure = secure;
        return this;
    }

    public CookieBuilder setVersion(int version) {
        this.version = version;
        return this;
    }

    public CookieBuilder setHttpOnly(boolean isHttpOnly) {
        this.isHttpOnly = isHttpOnly;
        return this;
    }

    public Cookie build() {
        return new IdentityCookie(this);
    }

    private static class IdentityCookie extends Cookie  {
        private IdentityCookie(CookieBuilder builder)   {
            super(builder.name, builder.value);
            this.setComment(builder.comment);
            if (builder.domain != null) {
                this.setDomain(builder.domain);
            }
            this.setHttpOnly(builder.isHttpOnly);
            this.setPath(builder.path);
            this.setMaxAge(builder.maxAge);
            this.setSecure(builder.secure);
            this.setVersion(builder.version);
        }
    }
}
