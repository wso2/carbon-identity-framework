/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.user.registration;

/**
 * Constants values
 */
public class SelfRegistrationConstants {


    public static final String ROLE_NAME_PROPERTY = "SelfSignUp.SignUpRole.Name";

    public static final String ROLE_EXTERNAL_PROPERTY = "SelfSignUp.SignUpRole.External";
    public static final String SIGN_UP_CONFIG_REG_PATH = "/repository/identity/sign-up-config";
    public static final String SELF_SIGN_UP_ELEMENT = "SelfSignUp";
    public static final String SIGN_UP_DOMAIN_ELEMENT = "SignUpDomain";
    public static final String IS_EXTERNAL_ELEMENT = "IsExternalRole";
    public static final String ROLE_NAME_ELEMENT = "RoleName";
    public static final String SIGN_UP_ROLE_ELEMENT = "SignUpRole";
    public static final String SIGN_UP_ROLE_CLAIM_URI = "http://wso2.org/claims/signuprole";

    private SelfRegistrationConstants(){

    }

}