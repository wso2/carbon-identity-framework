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

package org.wso2.carbon.identity.gateway.test.module.util;

public class GatewayTestConstants {

    public static String HOST_NAME = "localhost";
    public static int PORT = 8080;
    public static String GATEWAY_ENDPOINT = "http://" + HOST_NAME + ":" + PORT + "/gateway";
    public static String SAMPLE_PROTOCOL = "sampleProtocol";
    public static String NON_EXISTING_PROTOCOL = "NonExistingProtocol";
    public static String RELAY_STATE = "RelayState";
    public static String EXTERNAL_IDP = "externalIDP";
    public static String ASSERTION = "Assertion";
    public static String QUERY_PARAM_SEPARATOR = "&";
    public static String AUTHENTICATED_USER = "authenticatedUser";
    public static String AUTHENTICATED_USER_NAME = "ExternalAuthenticatedUser";
    public static String RESPONSE_CONTEXT = "/response";
    public static String SAMPLE_SP_NAME = "sample";
    public static String SAMPLE_ISSUER_NAME = "travelocity.com";
    public static String SAMPLE_IDP_NAME = "myidp";
}

