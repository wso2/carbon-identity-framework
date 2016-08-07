/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.mgt;

public class ApplicationConstants {

    private ApplicationConstants() {

    }

    public static final int LOCAL_IDP_ID = 1;
    public static final int LOCAL_IDP_AUTHENTICATOR_ID = 1;
    public static final String LOCAL_IDP = "wso2carbon-local-idp";
    public static final String LOCAL_IDP_NAME = "LOCAL";
    public static final String LOCAL_SP = "wso2carbon-local-sp";
    public static final String LOCAL_IDP_DEFAULT_CLAIM_DIALECT = "http://wso2.org/claims";
    public static final String STANDARD_APPLICATION = "standardAPP";
    public static final String WELLKNOWN_APPLICATION_TYPE = "appType";

    public static final String AUTH_TYPE_DEFAULT = "default";
    public static final String AUTH_TYPE_LOCAL = "local";
    public static final String AUTH_TYPE_FEDERATED = "federated";
    public static final String AUTH_TYPE_FLOW = "flow";

    public static final String IDP_NAME = "idpName";
    public static final String IDP_AUTHENTICATOR_NAME = "authenticatorName";
    public static final String IDP_AUTHENTICATOR_DISPLAY_NAME = "authenticatorDisplayName";
    public static final String APPLICATION_DOMAIN = "Application";
}
