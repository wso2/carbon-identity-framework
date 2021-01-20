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

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityResponse;

import static org.wso2.carbon.identity.application.authentication.framework.session.extender.SessionExtenderConstants.CODE;
import static org.wso2.carbon.identity.application.authentication.framework.session.extender.SessionExtenderConstants.DESCRIPTION;
import static org.wso2.carbon.identity.application.authentication.framework.session.extender.SessionExtenderConstants.MESSAGE;
import static org.wso2.carbon.identity.application.authentication.framework.session.extender.SessionExtenderConstants.TRACE_ID;

/**
 * Session Extender specific response object.
 */
public class SessionExtenderErrorResponse extends IdentityResponse {

    private final String response;

    protected SessionExtenderErrorResponse(SessionExtenderErrorResponseBuilder builder) {

        super(builder);
        this.response = builder.response;
    }

    public String getResponse() {
        return response;
    }

    public static class SessionExtenderErrorResponseBuilder extends IdentityResponseBuilder {

        private String response = null;
        private String errorCode = null;
        private String errorMessage = null;
        private String errorDescription = null;
        private String traceId = null;

        public SessionExtenderErrorResponseBuilder setErrorMessage(String errorMessage) {

            this.errorMessage = errorMessage;
            return this;
        }

        public SessionExtenderErrorResponseBuilder setErrorCode(String errorCode) {

            this.errorCode = errorCode;
            return this;
        }

        public SessionExtenderErrorResponseBuilder setErrorDescription(String errorDescription) {

            this.errorDescription = errorDescription;
            return this;
        }

        public SessionExtenderErrorResponseBuilder setTraceId(String traceId) {

            this.traceId = traceId;
            return this;
        }

        @Override
        public SessionExtenderErrorResponse build() {

            JSONObject responseJSON = new JSONObject();
            if (StringUtils.isNotBlank(errorCode)) {
                responseJSON.put(CODE, errorCode);
            }
            if (StringUtils.isNotBlank(errorMessage)) {
                responseJSON.put(MESSAGE, errorMessage);
            }
            if (StringUtils.isNotBlank(errorDescription)) {
                responseJSON.put(DESCRIPTION, errorDescription);
            }
            if (StringUtils.isNotBlank(traceId)) {
                responseJSON.put(TRACE_ID, traceId);
            }
            response = responseJSON.length() > 0 ? responseJSON.toString() : null;

            return new SessionExtenderErrorResponse(this);
        }
    }
}
