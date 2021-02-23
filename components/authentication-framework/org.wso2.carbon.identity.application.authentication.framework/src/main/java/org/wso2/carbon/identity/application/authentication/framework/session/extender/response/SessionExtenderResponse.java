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

package org.wso2.carbon.identity.application.authentication.framework.session.extender.response;

import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityResponse;

/**
 * Session Extender specific response object.
 */
public class SessionExtenderResponse extends IdentityResponse {

    private final String traceId;

    protected SessionExtenderResponse(SessionExtenderResponseBuilder builder) {

        super(builder);
        this.traceId = builder.traceId;
    }

    public String getTraceId() {
        return traceId;
    }

    public static class SessionExtenderResponseBuilder extends IdentityResponseBuilder {

        private String traceId = null;

        public SessionExtenderResponseBuilder setTraceId(String traceId) {

            this.traceId = traceId;
            return this;
        }

        @Override
        public SessionExtenderResponse build() {

            return new SessionExtenderResponse(this);
        }
    }
}
