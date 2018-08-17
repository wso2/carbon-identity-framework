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
package org.wso2.carbon.identity.api.idpmgt;

public class IdPConstants {

    public static final String IDP_MANAGEMENT_CONFIG_XML = "idp-mgt-config.xml";
    public static String IDP_MGT_CONTEXT_PATH = "api/identity/idp-mgt/v1.0/";
    public static final String IDP_SEARCH_LIMIT_PATH = "SearchLimits.IdP";
    public static final String APPLICATION_JSON = "application/json";
    public static final String DEFAULT_RESPONSE_CONTENT_TYPE = APPLICATION_JSON;
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String STATUS_INTERNAL_SERVER_ERROR_MESSAGE_DEFAULT = "Internal server error";
    public static final String STATUS_BAD_REQUEST_MESSAGE_DEFAULT = "Bad Request";

    public enum ErrorMessages {

        ERROR_CODE_RESOURCE_NOT_FOUND("IDP_00001", "No existing IDP found with given ID"),
        ERROR_CODE_IDP_ALREADY_EXIST("IDP_00002", "An IDP with given name already exists"),
        ERROR_CODE_INVALID_IDP("IDP_00003", "Identity Provider name is missing"),
        ERROR_CODE_INVALID_ARGS_FOR_LIMIT_OFFSET("IDP_00004", "Identity Provider name is missing"),
        ERROR_CODE_INVALID_TYPE_RECIEVED("IDP_00006", "Invalid type received"),
        ERROR_CODE_INVALID_FEDERATED_CONFIG("IDP_00007", "Invalid Federated Configuration received"),
        ERROR_CODE_INVALID_ROLE_CONFIG("IDP_00008", "Invalid Role Configuration received"),
        ERROR_CODE_INVALID_PROVISIONING_CONFIG("IDP_00009", "Invalid Provisioning Configuration received"),
        ERROR_CODE_BUILDING_CONFIG("IDP_00010", "Error occurred while building configuration from idp-mgt-config" +
                ".xml."),
        ERROR_CODE_INTERNAL_ERROR("IDP_00011", "Internal error"),
        ERROR_CODE_INVALID_AUTHENTICATOR("IDP_00012", "Invalid authenticator requested to update"),
        ERROR_CODE_EXIST_AUTHENTICATOR("IDP_00013", "Authenticator exists"),
        ERROR_CODE_NULL_AUTHENTICATOR("IDP_00014", "Null authenticator name received"),
        ERROR_CODE_INVALID_CONNECTOR("IDP_00015", "Invalid connector requested to update"),
        ERROR_CODE_INVALID_CONNECTOR_DELETE("IDP_00018", "Invalid connector requested to delete"),
        ERROR_CODE_NULL_CONNECTOR_LIST("IDP_00019", "Connector list is empty"),
        ERROR_CODE_NULL_CONNECTOR("IDP_00016", "Null connector name received"),
        ERROR_CODE_EXIST_CONNECTOR("IDP_00017", "Authenticator exists"),
        ERROR_CODE_FAIL_RESIDENT_IDP_EDIT("IDP_00020", "Resident IDP name cannot be modified"),
        ERROR_CODE_FAIL_RESIDENT_IDP_ADD("IDP_00021", "Resident IDP cannot be added"),
        ERROR_CODE_FAIL_RESIDENT_IDP_DELETE("IDP_00022", "Resident IDP cannot be deleted");

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

            return code + " : " + message;
        }
    }
}
