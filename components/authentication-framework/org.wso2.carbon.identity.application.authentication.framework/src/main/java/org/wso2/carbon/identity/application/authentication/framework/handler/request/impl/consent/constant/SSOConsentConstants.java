/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.consent.constant;

/**
 * Constants related to SSO consent handling.
 */
public class SSOConsentConstants {

    public static final String CONSENT_VALIDITY_SEPARATOR = ",";
    public static final String CONSENT_VALIDITY_TYPE_SEPARATOR = ":";
    public static final String CONSENT_VALIDITY_TYPE_VALID_UNTIL = "VALID_UNTIL";
    public static final String CONSENT_VALIDITY_TYPE_VALID_UNTIL_INDEFINITE = "INDEFINITE";
    public static final String CONFIG_ELEM_CONSENT = "Consent";
    public static final String CONFIG_ELEM_ENABLE_SSO_CONSENT_MANAGEMENT = "EnableSSOConsentManagement";
    public static final String USERNAME_CLAIM = "http://wso2.org/claims/username";
    public static final String FEDERATED_USER_DOMAIN_PREFIX = "FEDERATED";
    public static final String FEDERATED_USER_DOMAIN_SEPARATOR = ":";
}
