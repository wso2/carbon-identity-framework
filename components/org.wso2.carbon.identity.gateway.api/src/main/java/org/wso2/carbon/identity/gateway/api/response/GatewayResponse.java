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
package org.wso2.carbon.identity.gateway.api.response;

import org.wso2.carbon.identity.gateway.api.context.GatewayMessageContext;

import java.io.Serializable;

/**
 * Return type of the gateway framework. Processor return this type of instance and each protocol can have its own
 * return sub type of this.
 */
public class GatewayResponse implements Serializable {

    private static final long serialVersionUID = 1348704275109461974L;

    protected GatewayMessageContext context;
    protected String sessionKey;

    /**
     * Default Cosntructor.
     *
     * @param builder
     */
    protected GatewayResponse(GatewayResponseBuilder builder) {
        this.context = builder.context;
        this.sessionKey = builder.sessionKey;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    /**
     * Default builder - GatewayResponseBuilder.
     */
    public static class GatewayResponseBuilder {

        protected GatewayMessageContext context;
        protected String sessionKey;

        public GatewayResponseBuilder(GatewayMessageContext context) {
            this.context = context;
        }

        public GatewayResponseBuilder() {

        }

        public GatewayResponse build() {
            return new GatewayResponse(this);
        }

        public GatewayResponseBuilder setSessionKey(String sessionKey) {
            this.sessionKey = sessionKey;
            return this;
        }
    }
}
