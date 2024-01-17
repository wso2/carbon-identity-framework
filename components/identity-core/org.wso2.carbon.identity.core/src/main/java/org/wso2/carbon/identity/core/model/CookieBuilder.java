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

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.core.SameSiteCookie;
import org.wso2.carbon.core.ServletCookie;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.servlet.http.Cookie;

import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
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
    private SameSiteCookie sameSite = SameSiteCookie.STRICT;

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

        if (path.startsWith("/t/"+ MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            StringUtils.replace(path, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME,
                    IdentityTenantUtil.getSuperTenantAliasInPublicUrl());
        }
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

    public CookieBuilder setSameSite(SameSiteCookie sameSite) {
        this.sameSite = sameSite;
        return this;
    }

    public Cookie build() {
        return new IdentityCookie(this);
    }

    private static class IdentityCookie extends ServletCookie {
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
            this.setSameSite(builder.sameSite);
        }
    }
}
