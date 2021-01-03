/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.session.extender.request;

import org.wso2.carbon.identity.application.authentication.framework.inbound.FrameworkClientException;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityRequest;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Session Extender specific request object.
 */
public class SessionExtenderRequest extends IdentityRequest {

    private final String sessionKey;
    private final Cookie sessionCookie;

    protected SessionExtenderRequest(SessionExtenderRequestBuilder builder) throws FrameworkClientException {

        super(builder);
        this.sessionKey = builder.sessionKey;
        this.sessionCookie = builder.sessionCookie;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public Cookie getSessionCookie() {
        return sessionCookie;
    }

    public static class SessionExtenderRequestBuilder extends IdentityRequestBuilder {

        private String sessionKey = null;
        private Cookie sessionCookie = null;

        public SessionExtenderRequestBuilder(HttpServletRequest request, HttpServletResponse response) {

            super(request, response);
        }

        public SessionExtenderRequestBuilder setSessionKey(String sessionKey) {

            this.sessionKey = sessionKey;
            return this;
        }

        public SessionExtenderRequestBuilder setSessionCookie(Cookie sessionCookie) {

            this.sessionCookie = sessionCookie;
            return this;
        }

        @Override
        public SessionExtenderRequest build() throws FrameworkClientException {

            return new SessionExtenderRequest(this);
        }
    }
}
