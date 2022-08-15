/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.user.profile.mgt.association.federation.constant;

public class FederatedAssociationConstants {

    private FederatedAssociationConstants() {

    }

    public enum ErrorMessages {

        // Server errors.
        ERROR_RETRIEVING_TENANT_ID_OF_USER(15001, "Error while retrieving tenant ID of user"),
        ERROR_WHILE_DELETING_FEDERATED_ASSOCIATION_OF_USER(15002, "Error while deleting the federated association of" +
                " " +
                "user"),
        ERROR_WHILE_RETRIEVING_FEDERATED_ASSOCIATION_OF_USER(15003, "Database error occurred while retrieving the " +
                "federation association of user"),
        ERROR_WHILE_CREATING_FEDERATED_ASSOCIATION_OF_USER(15004, "Error while creating the federated association " +
                "of user"),
        ERROR_WHILE_RETRIEVING_ASSOCIATION_OF_USER(15005, "Database error occurred while retrieving the association " +
                "of user"),
        ERROR_WHILE_GETTING_THE_USER(15006, "Error occurred while retrieving the user from the database"),
        ERROR_WHILE_GETTING_TENANT_ID(15007, "Error occurred while retrieving the tenant id from the database"),
        ERROR_WHILE_GETTING_USER_FOR_FEDERATED_ASSOCIATION(15008, "Error occurred while retrieving the user for the " +
                "federation association"),
        ERROR_WHILE_WORKING_WITH_FEDERATED_ASSOCIATIONS(15009, "Error occurred while working with the federated " +
                "associations"),
        ERROR_WHILE_RESOLVING_IDENTITY_PROVIDERS(15009, "Error occurred while resolving identity providers"),

        // Client errors.
        INVALID_ATTRIBUTE_IDS_PROVIDED(10001, "Provided attribute IDs must be valid local claim URIs"),
        INVALID_FEDERATED_ASSOCIATION(10002, "Invalid federated association"),
        FEDERATED_ASSOCIATION_DOES_NOT_EXISTS(10003, "Valid federated associations do not exists"),
        FEDERATED_ASSOCIATION_ALREADY_EXISTS(10004, "The federated association is already associated to a local user"),
        INVALID_USER_IDENTIFIER_PROVIDED(10005, "The provided user identifier is invalid"),
        INVALID_TENANT_DOMAIN_PROVIDED(10006, "Provided tenant domain is invalid"),
        INVALID_IDP_PROVIDED(10007, "Provided federated identity provider is invalid"),
        INVALID_TENANT_ID_PROVIDED(10008, "Provided tenant id is invalid"),
        INVALID_USER_STORE_DOMAIN_PROVIDED(10009, "Provided user store domain is invalid");

        private final int code;
        private final String description;

        ErrorMessages(int code, String description) {

            this.code = code;
            this.description = description;
        }

        public int getCode() {

            return code;
        }

        public String getDescription() {

            return description;
        }

        @Override
        public String toString() {

            return code + " - " + description;
        }

    }
}
