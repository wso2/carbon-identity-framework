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
 *
 */
package org.wso2.carbon.identity.gateway.authentication.response;


import org.wso2.carbon.identity.gateway.api.response.GatewayResponse;

/**
 * Each handlers that is execute under the AuthenticationHandler will execute and return this AuthenticationResponse.
 * This has different status,
 * <p>
 * INCOMPLETE - Refer this as the authentication is not completed yet because of some reason.
 * AUTHENTICATED - Refer this to the authentication is completed and can be either continue the steps or return back.
 * <p>
 * This will hold the GatewayResponseBuilder to return response through the API to AuthenticationHandler.
 */
public class AuthenticationResponse {

    //Default Status is INCOMPLETE.
    public Status status = Status.INCOMPLETE;

    private GatewayResponse.GatewayResponseBuilder gatewayResponseBuilder = null;

    /**
     * Default Constructor. By using this, status will assign to INCOMPLETE.
     */
    public AuthenticationResponse() {

    }

    /**
     * By using this, status will assign to INCOMPLETE.
     *
     * @param gatewayResponseBuilder
     */
    public AuthenticationResponse(GatewayResponse.GatewayResponseBuilder gatewayResponseBuilder) {
        this.gatewayResponseBuilder = gatewayResponseBuilder;
    }

    /**
     * @param status                 Status
     * @param gatewayResponseBuilder GatewayResponse.GatewayResponseBuilder
     */
    public AuthenticationResponse(Status status, GatewayResponse.GatewayResponseBuilder gatewayResponseBuilder) {
        this.status = status;
        this.gatewayResponseBuilder = gatewayResponseBuilder;
    }

    /**
     * @param status
     */
    public AuthenticationResponse(Status status) {
        this.status = status;
    }


    /**
     * Retrieve the GatewayResponse.GatewayResponseBuilder
     *
     * @return GatewayResponse.GatewayResponseBuilder
     */
    public GatewayResponse.GatewayResponseBuilder getGatewayResponseBuilder() {
        return gatewayResponseBuilder;
    }


    /**
     * Status for AuthenticationResponse.
     */
    public static enum Status {
        AUTHENTICATED,
        INCOMPLETE;
    }
}
