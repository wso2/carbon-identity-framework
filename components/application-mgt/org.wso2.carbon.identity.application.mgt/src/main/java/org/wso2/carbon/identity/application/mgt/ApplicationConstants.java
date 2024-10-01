/*
 * Copyright (c) 2014-2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;

/**
 * Definitions of few constants shared across with other components from this component.
 * You may not instantiate this class directly. However you can access the constants declared as static.
 */
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
    public static final String SERVICE_PROVIDERS = "ServiceProviders";

    public static final String AUTH_TYPE_DEFAULT = "default";
    public static final String AUTH_TYPE_LOCAL = "local";
    public static final String AUTH_TYPE_FEDERATED = "federated";
    public static final String AUTH_TYPE_FLOW = "flow";

    public static final String IDP_NAME = "idpName";
    public static final String IDP_AUTHENTICATOR_NAME = "authenticatorName";
    public static final String IDP_AUTHENTICATOR_DISPLAY_NAME = "authenticatorDisplayName";
    public static final String APPLICATION_DOMAIN = "Application";
    // Regex for validating application name.
    public static final String APP_NAME_VALIDATING_REGEX = "^[a-zA-Z0-9 ._-]*$";

    public static final String TEMPLATE_CATEGORY = "category";
    public static final String CATEGORY_DISPLAY_NAME = "displayName";
    public static final String CATEGORY_ORDER = "order";
    public static final String RUN_TIME = "runtime";
    public static final String RUN_TIME_ANY = "any";
    public static final String RUN_TIME_NEW = "new";
    public static final String RUN_TIME_LEGACY = "legacy";
    public static final String CATEGORY_TEMPLATES = "templates";
    public static final String UNCATEGORIZED = "uncategorized";
    public static final String DISPLAY_NAME_FOR_UNCATEGORIZED = "Uncategorized";
    public static final int ORDER_FOR_UNCATEGORIZED = 10000;
    public static final String FILE_EXT_JSON = ".json";
    public static final String CATEGORIES_METADATA_FILE = "categories.json";
    public static final String TEMPLATES_DIR_PATH = CarbonUtils.getCarbonHome() + File.separator + "repository"
            + File.separator + "resources" + File.separator + "identity" + File.separator + "authntemplates" + File
            .separator;
    public static final String PURPOSE_GROUP_TYPE_SP = "SP";
    public static final String PURPOSE_GROUP_TYPE_SYSTEM = "SYSTEM";
    public static final String PURPOSE_GROUP_SHARED = "SHARED";

    public static final String TENANT_DEFAULT_SP_TEMPLATE_NAME = "default";
    public static final String MY_SQL = "MySQL";
    public static final String MARIADB = "MariaDB";
    public static final String POSTGRE_SQL = "PostgreSQL";
    public static final String DB2 = "DB2";
    public static final String MICROSOFT = "Microsoft";
    public static final String S_MICROSOFT = "microsoft";
    public static final String INFORMIX = "Informix";
    public static final String H2 = "H2";
    public static final String ORACLE = "Oracle";
    public static final String UNION_SEPARATOR = " UNION ALL ";
    public static final String DEFAULT_AUTH_SEQ = "default_sequence";
    public static final int DEFAULT_RESULTS_PER_PAGE = 10;
    public static final String ITEMS_PER_PAGE_PROPERTY = "ItemsPerPage";
    public static final int DEFAULT_FETCH_CHUNK_SIZE = 50;
    public static final String FETCH_CHUNK_SIZE = "FetchChunkSize";

    // Named query fields
    public static final String OFFSET = "OFFSET";
    public static final String LIMIT = "LIMIT";
    public static final String ZERO_BASED_START_INDEX = "ZERO_BASED_START_INDEX";
    public static final String ONE_BASED_START_INDEX = "ONE_BASED_START_INDEX";
    public static final String END_INDEX = "END_INDEX";

    // System application config elements
    public static final String SYSTEM_APPLICATIONS_CONFIG_ELEMENT = "SystemApplications";
    public static final String DEFAULT_APPLICATIONS_CONFIG_ELEMENT = "DefaultApplications";
    public static final String APPLICATION_NAME_CONFIG_ELEMENT = "ApplicationName";

    // Application Management Service Configurations.
    public static final String ENABLE_APPLICATION_ROLE_VALIDATION_PROPERTY = "ApplicationMgt.EnableRoleValidation";
    public static final String TRUSTED_APP_CONSENT_REQUIRED_PROPERTY = "ApplicationMgt.TrustedAppConsentRequired";
    public static final String TRUSTED_APP_MAX_THUMBPRINT_COUNT_PROPERTY =
            "ApplicationMgt.TrustedAppMaxThumbprintCount";

    public static final String NON_EXISTING_USER_CODE = "30007 - ";

    // Console and My Account application names.
    public static final String CONSOLE_APPLICATION_NAME = "Console";
    public static final String MY_ACCOUNT_APPLICATION_NAME = "My Account";
    public static final String CONSOLE_ACCESS_URL_FROM_SERVER_CONFIGS = "Console.AccessURL";
    public static final String MY_ACCOUNT_ACCESS_URL_FROM_SERVER_CONFIGS = "MyAccount.AccessURL";
    public static final String CONSOLE_APPLICATION_CLIENT_ID = "CONSOLE";
    public static final String CONSOLE_APPLICATION_INBOUND_TYPE = "oauth2";
    public static final String MY_ACCOUNT_APPLICATION_CLIENT_ID = "MY_ACCOUNT";
    public static final String TENANT_DOMAIN_PLACEHOLDER = "{TENANT_DOMAIN}";
    public static final String CONSOLE_ACCESS_ORIGIN = "Console.Origin";
    public static final String MYACCOUNT_ACCESS_ORIGIN = "MyAccount.Origin";
    public static final String CONSOLE_PORTAL_PATH = "Console.AppBaseName";
    public static final String MYACCOUNT_PORTAL_PATH = "MyAccount.AppBaseName";
    public static final String AUTHORIZE_ALL_SCOPES = "OAuth.AuthorizeAllScopes";
    public static final String RBAC = "RBAC";

    /**
     * Group the constants related to logs.
     */
    public static class LogConstants {

        public static final String TARGET_APPLICATION = "APPLICATION";
        public static final String USER = "USER";
        public static final String INBOUND_AUTHENTICATION_CONFIG = "inboundAuthenticationConfig";
        public static final String APP_OWNER = "owner";
        public static final String DISABLE_LEGACY_AUDIT_LOGS_IN_APP_MGT_CONFIG = "disableLegacyAuditLogsInAppMgt";
        public static final String ENABLE_V2_AUDIT_LOGS = "enableV2AuditLogs";
        public static final String CREATE_APPLICATION = "create-application";
        public static final String UPDATE_APPLICATION = "update-application";
        public static final String DELETE_APPLICATION = "delete-application";
    }

    /**
     * Group the constants related to application versioning.
     */
    public static class ApplicationVersion {

        public static final String APP_VERSION_V0 = "v0.0.0";
        public static final String APP_VERSION_V1 = "v1.0.0";

        // Change the latest version when a new version is introduced.
        public static final String LATEST_APP_VERSION = APP_VERSION_V1;
        public static final String BASE_APP_VERSION = APP_VERSION_V0;

        /**
         * Application version enum.
         */
        public enum ApplicationVersions {

            V0(APP_VERSION_V0),
            V1(APP_VERSION_V1);

            private final String value;

            ApplicationVersions(String value) {
                this.value = value;
            }

            public String getValue() {
                return value;
            }
        }
    }

    /**
     * Grouping of constants related to database SP_APP table.
     */
    public static class ApplicationTableColumns {

        public static final String ID = "ID";
        public static final String APP_NAME = "APP_NAME";
        public static final String APP_VERSION = "VERSION";
        public static final String DESCRIPTION = "DESCRIPTION";

        public static final String USERNAME = "USERNAME";
        public static final String USER_STORE = "USER_STORE";
        public static final String TENANT_ID = "TENANT_ID";

        public static final String ROLE_CLAIM = "ROLE_CLAIM";
        public static final String AUTH_TYPE = "AUTH_TYPE";
        public static final String PROVISIONING_USERSTORE_DOMAIN = "PROVISIONING_USERSTORE_DOMAIN";

        public static final String IS_LOCAL_CLAIM_DIALECT = "IS_LOCAL_CLAIM_DIALECT";
        public static final String IS_SEND_LOCAL_SUBJECT_ID = "IS_SEND_LOCAL_SUBJECT_ID";
        public static final String IS_SEND_AUTH_LIST_OF_IDPS = "IS_SEND_AUTH_LIST_OF_IDPS";
        public static final String IS_USE_TENANT_DOMAIN_SUBJECT = "IS_USE_TENANT_DOMAIN_SUBJECT";
        public static final String IS_USE_USER_DOMAIN_SUBJECT = "IS_USE_USER_DOMAIN_SUBJECT";
        public static final String ENABLE_AUTHORIZATION = "ENABLE_AUTHORIZATION";

        public static final String SUBJECT_CLAIM_URI = "SUBJECT_CLAIM_URI";
        public static final String IS_SAAS_APP = "IS_SAAS_APP";
        public static final String IS_DUMB_MODE = "IS_DUMB_MODE";
        public static final String IS_DISCOVERABLE = "IS_DISCOVERABLE";

        public static final String UUID = "UUID";
        public static final String IMAGE_URL = "IMAGE_URL";
        public static final String ACCESS_URL = "ACCESS_URL";
        public static final String APP_ID = "APP_ID";
        public static final String API_ID = "API_ID";
        public static final String POLICY_ID = "POLICY_ID";
        public static final String SCOPE_NAME = "SCOPE_NAME";
        public static final String MAIN_APP_ID = "MAIN_APP_ID";

        private ApplicationTableColumns() {

        }
    }
    
    /**
     * Standard inbound authentication protocols.
     */
    public static class StandardInboundProtocols {
        
        public static final String OAUTH2 = "oauth2";
        public static final String WS_TRUST = "wstrust";
        public static final String SAML2 = "samlsso";
        public static final String PASSIVE_STS = "passivests";
        
        private StandardInboundProtocols() {
        
        }
    }

    /**
     * Grouping of constants related to database SP_INBOUND_AUTH table.
     */
    public static class ApplicationInboundTableColumns {

        public static final String INBOUND_AUTH_KEY = "INBOUND_AUTH_KEY";
        public static final String INBOUND_AUTH_TYPE = "INBOUND_AUTH_TYPE";

        private ApplicationInboundTableColumns() {

        }
    }

    /**
     * Enums for error messages.
     */
    public enum ErrorMessage {

        ERROR_RETRIEVING_USER_BY_ID("65503", "Error occurred while retrieving user",
                "Error occurred while retrieving user by userid: %s."),
        NON_EXISTING_USER_ID("60504", "User not found",
                "No user found for the given user-id: %s."),
        ERROR_RETRIEVING_USERSTORE_MANAGER("65504", "Error retrieving userstore manager.",
                "Error occurred while retrieving userstore manager."),
        UNEXPECTED_ERROR("65006", "Unexpected processing error.",
                "Server encountered an unexpected error when creating the application.");

        private final String code;

        private final String message;
        private final String description;

        ErrorMessage(String code, String message, String description) {

            this.code = code;
            this.message = message;
            this.description = description;
        }

        public String getCode() {

            return code;
        }

        public String getMessage() {

            return message;
        }

        public String getDescription() {

            return description;
        }

        @Override
        public String toString() {

            return code + " | " + message;
        }
    }
}
