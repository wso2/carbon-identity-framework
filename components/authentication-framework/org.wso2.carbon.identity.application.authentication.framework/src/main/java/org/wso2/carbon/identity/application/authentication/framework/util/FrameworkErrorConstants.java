/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.util;

/**
 * This class is holds the constants related with UserCore Error Management.
 */
public class FrameworkErrorConstants {

    /**
     * Relevant error messages and error codes.
     */
    public enum ErrorMessages {

        // Generic error messages
        ERROR_WHILE_GETTING_IDP_BY_NAME("80001", "Error while getting IdP with name %s for the tenant domain, %s in "
                + "post authentication handler"),
        ERROR_WHILE_GETTING_REALM_IN_POST_AUTHENTICATION("80002", "Error while getting realm for the tenant domain, %s "
                + "in post authentication handler"),
        ERROR_WHILE_GETTING_USER_STORE_DOMAIN_WHILE_PROVISIONING("80003", "Error while getting user domain for "
                + "tenant domain, %s for provisioning for IDP, %s"),
        ERROR_WHILE_REMOVING_DOMAIN_FROM_USERNAME_WHILE_PROVISIONING("80004", "Error while getting user domain name "
                + "from username, %s for provisioning IDP, %s"),
        ERROR_WHILE_GETTING_USER_STORE_MANAGER_WHILE_PROVISIONING("80005", "Error while getting user username, %s for"
                + " provisioning for IDP, %s"),
        ERROR_WHILE_TRYING_CALL_SIGN_UP_ENDPOINT_FOR_PASSWORD_PROVISIONING("80006", "Error while calling the sign-up "
                + "endpoint for provisioning user %s from IDP, %s"),
        ERROR_WHILE_TRYING_TO_PROVISION_USER_WITHOUT_PASSWORD_PROVISIONING("80007", "Error while trying to provision "
                + "user %s from IDP, %s"),
        ERROR_WHILE_TRYING_TO_GET_CLAIMS_WHILE_TRYING_TO_PASSWORD_PROVISION("80008", "Error while trying to get the "
                + "claims while trying to provision the user for IDP, %s"),
        ERROR_WHILE_TRYING_TO_PROVISION_USER_WITH_PASSWORD_PROVISIONING("80009", "Error while trying to provision "
                + "user %s from IDP, %s"),
        ERROR_WHILE_GETTING_USERNAME_ASSOCIATED_WITH_IDP("80010", "Error while getting user name associated with from "
                + "sIDP, %s"),
        ERROR_WHILE_UPDATING_CLAIM_MAPPINGS("80011", "Error while updating claim mapping for the user , %s"),
        ERROR_WHILE_GETTING_LOCAL_USER_ID("80012", "Error while getting associated local user ID for , %s"),
        ERROR_WHILE_GETTING_CLAIM_MAPPINGS("80013", "Error while getting claim mappings for user, %s");

        private final String code;
        private final String message;

        ErrorMessages(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return code + " - " + message;
        }

    }

}