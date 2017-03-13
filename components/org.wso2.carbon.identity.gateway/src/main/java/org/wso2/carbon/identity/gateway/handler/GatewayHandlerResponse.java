/*
 *
 *  * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  *
 *  * WSO2 Inc. licenses this file to you under the Apache License,
 *  * Version 2.0 (the "License"); you may not use this file except
 *  * in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 */

package org.wso2.carbon.identity.gateway.handler;

import org.wso2.carbon.identity.gateway.api.response.GatewayResponse;

/**
 * Generic Handler Response
 */
public class GatewayHandlerResponse {

    public Status status = Status.CONTINUE;

    private GatewayResponse.GatewayResponseBuilder gatewayResponseBuilder;

    public GatewayHandlerResponse(Status status, GatewayResponse.GatewayResponseBuilder gatewayResponseBuilder) {
        this.status = status;
        this.gatewayResponseBuilder = gatewayResponseBuilder;
    }

    public GatewayHandlerResponse(Status status) {
        this.status = status;
    }

    public GatewayHandlerResponse(GatewayResponse.GatewayResponseBuilder gatewayResponseBuilder) {
        this.gatewayResponseBuilder = gatewayResponseBuilder;
    }

    public GatewayHandlerResponse() {
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public GatewayResponse.GatewayResponseBuilder getGatewayResponseBuilder() {
        return gatewayResponseBuilder;
    }

    public void setGatewayResponseBuilder(GatewayResponse.GatewayResponseBuilder gatewayResponseBuilder) {
        this.gatewayResponseBuilder = gatewayResponseBuilder;
    }

    public static enum Status {
        REDIRECT, CONTINUE;
    }
}
