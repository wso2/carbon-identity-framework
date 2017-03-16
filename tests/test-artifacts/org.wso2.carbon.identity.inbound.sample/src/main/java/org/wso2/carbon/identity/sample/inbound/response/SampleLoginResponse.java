/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.sample.inbound.response;

import org.wso2.carbon.identity.gateway.api.context.GatewayMessageContext;
import org.wso2.carbon.identity.gateway.api.response.GatewayResponse;

import java.util.Map;

public class SampleLoginResponse extends GatewayResponse {

    private String subject;
    private String claims;

    public String getSubject() {
        return subject;
    }

    public String getClaims() {
        return claims;
    }

    protected SampleLoginResponse(GatewayResponseBuilder builder) {
        super(builder);
        this.subject = ((SampleLoginResponseBuilder) builder).subject;
        this.claims = ((SampleLoginResponseBuilder) builder).claims;
    }

    public static class SampleLoginResponseBuilder extends GatewayResponseBuilder {

        private String subject;
        private String claims;

        public SampleLoginResponseBuilder setSubject(String subject) {
            this.subject = subject;
            return this;
        }

        public SampleLoginResponseBuilder setClaims(String claims) {
            this.claims = claims;
            return this;
        }

        public SampleLoginResponseBuilder(GatewayMessageContext context) {
            super(context);
        }

        @Override
        public GatewayResponse build() {
            return new SampleLoginResponse(this);
        }

    }

}
