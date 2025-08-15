/*
 * Copyright (c) 2023-2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.event;

/**
 * Identity management related constants
 */
public class IdentityEventConstants {

    private IdentityEventConstants(){}

    public class PropertyConfig {

        private PropertyConfig(){}

        public static final String CONFIG_FILE_NAME = "identity-event.properties";
        public static final String ACCOUNT_LOCK_ENABLE = "Account.Lock.Enable";
        public static final String AUTH_POLICY_ENABLE = "Authentication.Policy.Enable";
        public static final String AUTH_POLICY_ACCOUNT_EXIST = "Authentication.Policy.Check.Account.Exist";
        public static final String AUTH_POLICY_ACCOUNT_LOCKING_FAIL_ATTEMPTS = "Authentication.Policy.Account.Lock.On" +
                ".Failure.Max.Attempts";
        public static final String PASSWORD_POLICY_EXTENSIONS = "Password.policy.extensions";
        public static final String EXTENSION_USER_DATA_STORE = "Identity.Mgt.User.Data.Store";
        public static final String EXTENSION_USER_RECOVERY_DATA_STORE = "Identity.Mgt.User.Recovery.Data.Store";
        public static final String NOTIFICATION_LINK_EXPIRE_TIME = "Notification.Expire.Time";
        public static final String ALREADY_WRITTEN_PROPERTY_KEY = "AlreadyWritten";
        public static final String ALREADY_WRITTEN_PROPERTY_VALUE = "true";

    }

    public class Event {

        private Event(){}

        public static final String PRE_AUTHENTICATION = "PRE_AUTHENTICATION";
        public static final String POST_AUTHENTICATION = "POST_AUTHENTICATION";
        public static final String AUTHENTICATION_STEP_SUCCESS = "AUTHENTICATION_STEP_SUCCESS";
        public static final String AUTHENTICATION_STEP_FAILURE = "AUTHENTICATION_STEP_FAILURE";
        public static final String AUTHENTICATION_SUCCESS = "AUTHENTICATION_SUCCESS";
        public static final String AUTHENTICATION_FAILURE = "AUTHENTICATION_FAILURE";
        public static final String SESSION_CREATE = "SESSION_CREATE";
        public static final String SESSION_UPDATE = "SESSION_UPDATE";
        public static final String SESSION_TERMINATE = "SESSION_TERMINATE";
        /**
         * Session termination event version 2.
         * This event is used to publish both single session and bulk session termination events,
         *  consolidating multiple session terminations into a single event instead of emitting an event,
         *  for each terminated session individually.
         */
        public static final String SESSION_TERMINATE_V2 = "SESSION_TERMINATE_V2";
        public static final String SESSION_EXPIRE = "SESSION_EXPIRE";
        public static final String SESSION_EXTEND = "SESSION_EXTEND";
        public static final String VERIFICATION = "VERIFICATION";
        public static final String PRE_SET_USER_CLAIMS = "PRE_SET_USER_CLAIMS";
        public static final String POST_SET_USER_CLAIMS = "POST_SET_USER_CLAIMS";
        public static final String PRE_ADD_USER= "PRE_ADD_USER";
        public static final String POST_ADD_USER= "POST_ADD_USER";
        public static final String PRE_UPDATE_CREDENTIAL= "PRE_UPDATE_CREDENTIAL";
        public static final String POST_UPDATE_CREDENTIAL= "POST_UPDATE_CREDENTIAL";
        public static final String PRE_UPDATE_CREDENTIAL_BY_ADMIN= "PRE_UPDATE_CREDENTIAL_BY_ADMIN";
        public static final String POST_UPDATE_CREDENTIAL_BY_ADMIN= "POST_UPDATE_CREDENTIAL_BY_ADMIN";
        public static final String POST_UPDATE_CREDENTIAL_BY_SCIM= "POST_UPDATE_CREDENTIAL_BY_SCIM";
        public static final String PRE_DELETE_USER= "PRE_DELETE_USER";
        public static final String POST_DELETE_USER= "POST_DELETE_USER";
        public static final String PRE_SET_USER_CLAIM= "PRE_SET_USER_CLAIM";
        public static final String PRE_GET_USER_CLAIM= "PRE_GET_USER_CLAIM";
        public static final String PRE_GET_USER_CLAIMS= "PRE_GET_USER_CLAIMS";
        public static final String POST_GET_USER_CLAIMS= "POST_GET_USER_CLAIMS";
        public static final String POST_GET_USER_CLAIM= "POST_GET_USER_CLAIM";
        public static final String POST_SET_USER_CLAIM= "POST_SET_USER_CLAIM";
        public static final String PRE_DELETE_USER_CLAIMS= "PRE_DELETE_USER_CLAIMS";
        public static final String POST_DELETE_USER_CLAIMS= "POST_DELETE_USER_CLAIMS";
        public static final String PRE_DELETE_USER_CLAIM= "PRE_DELETE_USER_CLAIM";
        public static final String POST_DELETE_USER_CLAIM= "POST_DELETE_USER_CLAIM";
        public static final String PRE_ADD_ROLE= "PRE_ADD_ROLE";
        public static final String POST_ADD_ROLE= "POST_ADD_ROLE";
        public static final String PRE_DELETE_ROLE= "PRE_DELETE_ROLE";
        public static final String POST_DELETE_ROLE= "POST_DELETE_ROLE";
        public static final String PRE_UPDATE_ROLE= "PRE_UPDATE_ROLE";
        public static final String POST_UPDATE_ROLE= "POST_UPDATE_ROLE";
        public static final String PRE_UPDATE_USER_LIST_OF_ROLE= "PRE_UPDATE_USER_LIST_OF_ROLE";
        public static final String POST_UPDATE_USER_LIST_OF_ROLE= "POST_UPDATE_USER_LIST_OF_ROLE";
        public static final String POST_UPDATE_USER_LIST_OF_HYBRID_ROLE= "POST_UPDATE_USER_LIST_OF_HYBRID_ROLE";
        public static final String PRE_UPDATE_ROLE_LIST_OF_USER= "PRE_UPDATE_ROLE_LIST_OF_USER";
        public static final String POST_UPDATE_ROLE_LIST_OF_USER= "POST_UPDATE_ROLE_LIST_OF_USER";

        public static final String PRE_ADD_ROLE_EVENT = "PRE_ADD_ROLE_EVENT";
        public static final String POST_ADD_ROLE_EVENT = "POST_ADD_ROLE_EVENT";
        public static final String PRE_GET_ROLES_EVENT = "PRE_GET_ROLES_EVENT";
        public static final String POST_GET_ROLES_EVENT = "POST_GET_ROLES_EVENT";
        public static final String PRE_GET_ROLES_COUNT_EVENT = "PRE_GET_ROLES_COUNT_EVENT";
        public static final String POST_GET_ROLES_COUNT_EVENT = "POST_GET_ROLES_COUNT_EVENT";
        public static final String PRE_GET_ROLE_EVENT = "PRE_GET_ROLE_EVENT";
        public static final String POST_GET_ROLE_EVENT = "POST_GET_ROLE_EVENT";
        public static final String PRE_UPDATE_ROLE_NAME_EVENT = "PRE_UPDATE_ROLE_NAME_EVENT";
        public static final String POST_UPDATE_ROLE_NAME_EVENT = "POST_UPDATE_ROLE_NAME_EVENT";
        public static final String PRE_DELETE_ROLE_EVENT = "PRE_DELETE_ROLE_EVENT";
        public static final String POST_DELETE_ROLE_EVENT = "POST_DELETE_ROLE_EVENT";
        public static final String PRE_GET_USER_LIST_OF_ROLE_EVENT = "PRE_GET_USER_LIST_OF_ROLE_EVENT";
        public static final String POST_GET_USER_LIST_OF_ROLE_EVENT = "POST_GET_USER_LIST_OF_ROLE_EVENT";
        public static final String PRE_UPDATE_USER_LIST_OF_ROLE_EVENT = "PRE_UPDATE_USER_LIST_OF_ROLE_EVENT";
        public static final String POST_UPDATE_USER_LIST_OF_ROLE_EVENT = "POST_UPDATE_USER_LIST_OF_ROLE_EVENT";
        public static final String PRE_GET_GROUP_LIST_OF_ROLES_EVENT = "PRE_GET_GROUP_LIST_OF_ROLES_EVENT";
        public static final String POST_GET_GROUP_LIST_OF_ROLES_EVENT = "POST_GET_GROUP_LIST_OF_ROLES_EVENT";
        public static final String PRE_UPDATE_GROUP_LIST_OF_ROLE_EVENT = "PRE_UPDATE_GROUP_LIST_OF_ROLE_EVENT";
        public static final String POST_UPDATE_GROUP_LIST_OF_ROLE_EVENT = "POST_UPDATE_GROUP_LIST_OF_ROLE_EVENT";
        public static final String PRE_GET_PERMISSION_LIST_OF_ROLE_EVENT = "PRE_GET_PERMISSION_LIST_OF_ROLE_EVENT";
        public static final String POST_GET_PERMISSION_LIST_OF_ROLE_EVENT = "POST_GET_PERMISSION_LIST_OF_ROLE_EVENT";
        public static final String PRE_SET_PERMISSIONS_FOR_ROLE_EVENT = "PRE_SET_PERMISSIONS_FOR_ROLE_EVENT";
        public static final String POST_SET_PERMISSIONS_FOR_ROLE_EVENT = "POST_SET_PERMISSIONS_FOR_ROLE_EVENT";

        public static final String UPDATE_GOVERNANCE_CONFIGURATION= "UPDATE_GOVERNANCE_CONFIGURATION";
        public static final String PRE_ADD_NEW_PASSWORD = "PRE_ADD_NEW_PASSWORD";
        public static final String POST_ADD_NEW_PASSWORD = "POST_ADD_NEW_PASSWORD";
        public static final String PRE_SEND_RECOVERY_NOTIFICATION = "PRE_SEND_RECOVERY_NOTIFICATION";
        public static final String POST_SEND_RECOVERY_NOTIFICATION = "POST_SEND_RECOVERY_NOTIFICATION";
        public static final String POST_ACCOUNT_SUSPENSION = "POST_ACCOUNT_SUSPENSION";      
        public static final String TRIGGER_NOTIFICATION = "TRIGGER_NOTIFICATION";
        public static final String TRIGGER_SMS_NOTIFICATION = "TRIGGER_SMS_NOTIFICATION";
        public static final String PRE_ACCOUNT_RECOVERY = "PRE_ACCOUNT_RECOVERY";
        public static final String POST_ACCOUNT_RECOVERY = "POST_ACCOUNT_RECOVERY";
        public static final String PRE_GET_USER_RECOVERY_DATA = "PRE_GET_USER_RECOVERY_DATA";
        public static final String POST_GET_USER_RECOVERY_DATA = "POST_GET_USER_RECOVERY_DATA";
        public static final String VALIDATE_PASSWORD = "VALIDATE_PASSWORD";

        public static final String PRE_ADD_INTERNAL_ROLE_WITH_ID = "PRE_ADD_INTERNAL_ROLE_WITH_ID";
        public static final String POST_ADD_INTERNAL_ROLE_WITH_ID = "POST_ADD_INTERNAL_ROLE_WITH_ID";
        public static final String PRE_GET_USER_CLAIM_VALUE_WITH_ID = "PRE_GET_USER_CLAIM_VALUE_WITH_ID";
        public static final String PRE_GET_USER_CLAIM_VALUES_WITH_ID = "PRE_GET_USER_CLAIM_VALUES_WITH_ID";
        public static final String POST_GET_USER_CLAIM_VALUE_WITH_ID = "POST_GET_USER_CLAIM_VALUE_WITH_ID";
        public static final String POST_GET_USER_CLAIM_VALUES_WITH_ID = "POST_GET_USER_CLAIM_VALUES_WITH_ID";
        public static final String PRE_GET_USER_LIST_WITH_ID = "PRE_GET_USER_LIST_WITH_ID";
        public static final String PRE_GET_USER_LIST_PAGINATION_WITH_ID = "PRE_GET_USER_LIST_PAGINATION_WITH_ID";
        public static final String PRE_GET_USER_LIST_CONDITION_WITH_ID = "PRE_GET_USER_LIST_CONDITION_WITH_ID";
        public static final String POST_GET_USER_LIST_WITH_ID = "POST_GET_USER_LIST_WITH_ID";
        public static final String POST_GET_USER_LIST_PAGINATION_WITH_ID = "POST_GET_USER_LIST_PAGINATION_WITH_ID";
        public static final String POST_GET_USER_LIST_CONDITION_WITH_ID = "POST_GET_USER_LIST_CONDITION_WITH_ID";
        public static final String PRE_GET_USER_WITH_ID = "PRE_GET_USER_WITH_ID";
        public static final String POST_GET_USER_WITH_ID = "POST_GET_USER_WITH_ID";
        public static final String POST_GET_PAGINATED_USER_LIST_WITH_ID = "POST_GET_PAGINATED_USER_LIST_WITH_ID";
        public static final String POST_LIST_USERS_WITH_ID = "POST_LIST_USERS_WITH_ID";
        public static final String POST_GET_ROLE_LIST_OF_USER_WITH_ID = "POST_GET_ROLE_LIST_OF_USER_WITH_ID";
        public static final String POST_GET_USER_LIST_OF_ROLE_WITH_ID = "POST_GET_USER_LIST_OF_ROLE_WITH_ID";
        public static final String POST_GET_USERS_CLAIM_VALUES_WITH_ID = "POST_GET_USERS_CLAIM_VALUES_WITH_ID";
        public static final String PRE_AUTHENTICATE_WITH_ID = "PRE_AUTHENTICATE_WITH_ID";
        public static final String POST_AUTHENTICATE_WITH_ID = "POST_AUTHENTICATE_WITH_ID";
        public static final String PRE_AUTHENTICATE_CLAIM_WITH_ID = "PRE_AUTHENTICATE_CLAIM_WITH_ID";
        public static final String POST_AUTHENTICATE_CLAIM_WITH_ID = "POST_AUTHENTICATE_CLAIM_WITH_ID";
        public static final String PRE_AUTHENTICATE_LOGIN_IDENTIFIER_WITH_ID = "PRE_AUTHENTICATE_LOGIN_IDENTIFIER_WITH_ID";
        public static final String POST_AUTHENTICATE_LOGIN_IDENTIFIER_WITH_ID = "POST_AUTHENTICATE_LOGIN_IDENTIFIER_WITH_ID";
        public static final String PRE_ADD_USER_WITH_ID = "PRE_ADD_USER_WITH_ID";
        public static final String POST_ADD_USER_WITH_ID = "POST_ADD_USER_WITH_ID";
        public static final String PRE_UPDATE_CREDENTIAL_WITH_ID = "PRE_UPDATE_CREDENTIAL_WITH_ID";
        public static final String POST_UPDATE_CREDENTIAL_WITH_ID = "POST_UPDATE_CREDENTIAL_WITH_ID";
        public static final String PRE_UPDATE_CREDENTIAL_BY_ADMIN_WITH_ID = "PRE_UPDATE_CREDENTIAL_BY_ADMIN_WITH_ID";
        public static final String POST_UPDATE_CREDENTIAL_BY_ADMIN_WITH_ID = "POST_UPDATE_CREDENTIAL_BY_ADMIN_WITH_ID";
        public static final String PRE_DELETE_USER_WITH_ID = "PRE_DELETE_USER_WITH_ID";
        public static final String POST_DELETE_USER_WITH_ID = "POST_DELETE_USER_WITH_ID";
        public static final String PRE_SET_USER_CLAIM_VALUE_WITH_ID = "PRE_SET_USER_CLAIM_VALUE_WITH_ID";
        public static final String POST_SET_USER_CLAIM_VALUE_WITH_ID = "POST_SET_USER_CLAIM_VALUE_WITH_ID";
        public static final String PRE_SET_USER_CLAIM_VALUES_WITH_ID = "PRE_SET_USER_CLAIM_VALUES_WITH_ID";
        public static final String POST_SET_USER_CLAIM_VALUES_WITH_ID = "POST_SET_USER_CLAIM_VALUES_WITH_ID";
        public static final String PRE_DELETE_USER_CLAIM_VALUES_WITH_ID = "PRE_DELETE_USER_CLAIM_VALUES_WITH_ID";
        public static final String POST_DELETE_USER_CLAIM_VALUES_WITH_ID = "POST_DELETE_USER_CLAIM_VALUES_WITH_ID";
        public static final String PRE_DELETE_USER_CLAIM_VALUE_WITH_ID = "PRE_DELETE_USER_CLAIM_VALUE_WITH_ID";
        public static final String POST_DELETE_USER_CLAIM_VALUE_WITH_ID = "POST_DELETE_USER_CLAIM_VALUE_WITH_ID";
        public static final String PRE_ADD_ROLE_WITH_ID = "PRE_ADD_ROLE_WITH_ID";
        public static final String POST_ADD_ROLE_WITH_ID = "POST_ADD_ROLE_WITH_ID";
        public static final String PRE_UPDATE_USER_LIST_OF_ROLE_WITH_ID = "PRE_UPDATE_USER_LIST_OF_ROLE_WITH_ID";
        public static final String POST_UPDATE_USER_LIST_OF_ROLE_WITH_ID = "POST_UPDATE_USER_LIST_OF_ROLE_WITH_ID";
        public static final String POST_UPDATE_USER_LIST_OF_HYBRID_ROLE_WITH_ID=
                "POST_UPDATE_USER_LIST_OF_HYBRID_ROLE_WITH_ID";
        public static final String PRE_UPDATE_ROLE_LIST_OF_USER_WITH_ID = "PRE_UPDATE_ROLE_LIST_OF_USER_WITH_ID";
        public static final String POST_UPDATE_ROLE_LIST_OF_USER_WITH_ID = "POST_UPDATE_ROLE_LIST_OF_USER_WITH_ID";
        public static final String POST_GET_ROLE_LIST_OF_USERS_WITH_ID = "POST_GET_ROLE_LIST_OF_USERS_WITH_ID";
        public static final String PRE_SET_CHALLENGE_QUESTION_ANSWERS = "PRE_SET_CHALLENGE_QUESTION_ANSWERS";
        public static final String POST_SET_CHALLENGE_QUESTION_ANSWERS = "POST_SET_CHALLENGE_QUESTION_ANSWERS";
        public static final String PRE_USER_ACCOUNT_CONFIRMATION = "PRE_USER_ACCOUNT_CONFIRMATION";
        public static final String POST_USER_ACCOUNT_CONFIRMATION = "POST_USER_ACCOUNT_CONFIRMATION";
        public static final String PRE_SELF_SIGNUP_REGISTER = "PRE_SELF_SIGNUP_REGISTER";
        public static final String POST_SELF_SIGNUP_REGISTER = "POST_SELF_SIGNUP_REGISTER";
        public static final String PRE_SELF_SIGNUP_CONFIRM = "PRE_SELF_SIGNUP_CONFIRM";
        public static final String POST_SELF_SIGNUP_CONFIRM = "POST_SELF_SIGNUP_CONFIRM";
        public static final String PRE_EMAIL_CHANGE_VERIFICATION = "PRE_EMAIL_CHANGE_VERIFICATION";
        public static final String POST_EMAIL_CHANGE_VERIFICATION = "POST_EMAIL_CHANGE_VERIFICATION";
        public static final String PRE_LOCK_ACCOUNT = "PRE_LOCK_ACCOUNT";
        public static final String POST_LOCK_ACCOUNT = "POST_LOCK_ACCOUNT";
        public static final String PRE_UNLOCK_ACCOUNT = "PRE_UNLOCK_ACCOUNT";
        public static final String POST_UNLOCK_ACCOUNT = "POST_UNLOCK_ACCOUNT";
        public static final String PRE_DISABLE_ACCOUNT = "PRE_DISABLE_ACCOUNT";
        public static final String POST_DISABLE_ACCOUNT = "POST_DISABLE_ACCOUNT";
        public static final String PRE_ENABLE_ACCOUNT = "PRE_ENABLE_ACCOUNT";
        public static final String POST_ENABLE_ACCOUNT = "POST_ENABLE_ACCOUNT";
        public static final String PRE_ADD_USER_WITH_ASK_PASSWORD = "PRE_ADD_USER_WITH_ASK_PASSWORD";
        public static final String POST_ADD_USER_WITH_ASK_PASSWORD = "POST_ADD_USER_WITH_ASK_PASSWORD";
        public static final String PRE_FORCE_PASSWORD_RESET_BY_ADMIN = "PRE_FORCE_PASSWORD_RESET_BY_ADMIN";
        public static final String POST_FORCE_PASSWORD_RESET_BY_ADMIN = "POST_FORCE_PASSWORD_RESET_BY_ADMIN";
        public static final String PRE_VERIFY_EMAIL_CLAIM = "PRE_VERIFY_EMAIL_CLAIM";
        public static final String POST_VERIFY_EMAIL_CLAIM = "POST_VERIFY_EMAIL_CLAIM";
        public static final String POST_GENERATE_EMAIL_OTP = "POST_GENERATE_EMAIL_OTP";
        public static final String POST_VALIDATE_EMAIL_OTP = "POST_VALIDATE_EMAIL_OTP";
        public static final String POST_GENERATE_SMS_OTP = "POST_GENERATE_SMS_OTP";
        public static final String POST_VALIDATE_SMS_OTP = "POST_VALIDATE_SMS_OTP";
        public static final String SESSION_EXTENSION = "SESSION_EXTENSION";

        // Claim metadata operation related events.
        public static final String PRE_ADD_CLAIM_DIALECT = "PRE_ADD_CLAIM_DIALECT";
        public static final String PRE_UPDATE_CLAIM_DIALECT = "PRE_UPDATE_CLAIM_DIALECT";
        public static final String PRE_DELETE_CLAIM_DIALECT = "PRE_DELETE_CLAIM_DIALECT";
        public static final String PRE_ADD_LOCAL_CLAIM = "PRE_LOCAL_CLAIM_ADD";
        public static final String PRE_UPDATE_LOCAL_CLAIM = "PRE_UPDATE_LOCAL_CLAIM";
        public static final String PRE_DELETE_LOCAL_CLAIM = "POST_DELETE_LOCAL_CLAIM";
        public static final String PRE_ADD_EXTERNAL_CLAIM = "PRE_ADD_EXTERNAL_CLAIM";
        public static final String PRE_UPDATE_EXTERNAL_CLAIM = "PRE_UPDATE_EXTERNAL_CLAIM";
        public static final String PRE_DELETE_EXTERNAL_CLAIM = "PRE_DELETE_EXTERNAL_CLAIM";
        public static final String POST_ADD_CLAIM_DIALECT = "POST_ADD_CLAIM_DIALECT";
        public static final String POST_UPDATE_CLAIM_DIALECT = "POST_RENAME_CLAIM_DIALECT";
        public static final String POST_DELETE_CLAIM_DIALECT = "POST_DELETE_CLAIM_DIALECT";
        public static final String POST_ADD_LOCAL_CLAIM = "POST_LOCAL_CLAIM_ADD";
        public static final String POST_UPDATE_LOCAL_CLAIM = "POST_UPDATE_LOCAL_CLAIM";
        public static final String POST_DELETE_LOCAL_CLAIM = "POST_DELETE_LOCAL_CLAIM";
        public static final String POST_ADD_EXTERNAL_CLAIM = "POST_ADD_EXTERNAL_CLAIM";
        public static final String POST_UPDATE_EXTERNAL_CLAIM = "POST_UPDATE_EXTERNAL_CLAIM";
        public static final String POST_DELETE_EXTERNAL_CLAIM = "POST_DELETE_EXTERNAL_CLAIM";
        public static final String POST_NON_BASIC_AUTHENTICATION = "POST_NON_BASIC_AUTHENTICATION";

        // Loggers related events.
        public static final String PUBLISH_AUDIT_LOG = "PUBLISH_AUDIT_LOG";
        public static final String PUBLISH_DIAGNOSTIC_LOG = "PUBLISH_DIAGNOSTIC_LOG";

        // Role-Mgt V2 events
        public static final String PRE_ADD_ROLE_V2_EVENT = "PRE_ADD_ROLE_V2_EVENT";
        public static final String POST_ADD_ROLE_V2_EVENT = "POST_ADD_ROLE_V2_EVENT";
        public static final String PRE_GET_ROLES_V2_EVENT = "PRE_GET_ROLES_V2_EVENT";
        public static final String POST_GET_ROLES_V2_EVENT = "POST_GET_ROLES_V2_EVENT";
        public static final String PRE_GET_ROLES_V2_COUNT_EVENT = "PRE_GET_ROLES_V2_COUNT_EVENT";
        public static final String POST_GET_ROLES_V2_COUNT_EVENT = "POST_GET_ROLES_V2_COUNT_EVENT";
        public static final String PRE_GET_ROLES_V2_FILTERED_COUNT_EVENT = "PRE_GET_ROLES_V2_COUNT_EVENT";
        public static final String POST_GET_ROLES_V2_FILTERED_COUNT_EVENT = "POST_GET_ROLES_V2_COUNT_EVENT";
        public static final String PRE_GET_ROLE_V2_EVENT = "PRE_GET_ROLE_V2_EVENT";
        public static final String POST_GET_ROLE_V2_EVENT = "POST_GET_ROLE_V2_EVENT";
        public static final String PRE_UPDATE_ROLE_V2_NAME_EVENT = "PRE_UPDATE_ROLE_V2_NAME_EVENT";
        public static final String POST_UPDATE_ROLE_V2_NAME_EVENT = "POST_UPDATE_ROLE_V2_NAME_EVENT";
        public static final String PRE_DELETE_ROLE_V2_EVENT = "PRE_DELETE_ROLE_V2_EVENT";
        public static final String POST_DELETE_ROLE_V2_EVENT = "POST_DELETE_ROLE_V2_EVENT";
        public static final String PRE_GET_USER_LIST_OF_ROLE_V2_EVENT = "PRE_GET_USER_LIST_OF_ROLE_V2_EVENT";
        public static final String POST_GET_USER_LIST_OF_ROLE_V2_EVENT = "POST_GET_USER_LIST_OF_ROLE_V2_EVENT";
        public static final String PRE_UPDATE_USER_LIST_OF_ROLE_V2_EVENT = "PRE_UPDATE_USER_LIST_OF_ROLE_V2_EVENT";
        public static final String POST_UPDATE_USER_LIST_OF_ROLE_V2_EVENT = "POST_UPDATE_USER_LIST_OF_ROLE_V2_EVENT";
        public static final String PRE_GET_GROUP_LIST_OF_ROLES_V2_EVENT = "PRE_GET_GROUP_LIST_OF_ROLES_V2_EVENT";
        public static final String POST_GET_GROUP_LIST_OF_ROLES_V2_EVENT = "POST_GET_GROUP_LIST_OF_ROLES_V2_EVENT";
        public static final String PRE_UPDATE_GROUP_LIST_OF_ROLE_V2_EVENT = "PRE_UPDATE_GROUP_LIST_OF_ROLE_V2_EVENT";
        public static final String PRE_UPDATE_IDP_GROUP_LIST_OF_ROLE_V2_EVENT =
                "PRE_UPDATE_IDP_GROUP_LIST_OF_ROLE_V2_EVENT";
        public static final String POST_UPDATE_GROUP_LIST_OF_ROLE_V2_EVENT =
                "POST_UPDATE_GROUP_LIST_OF_ROLE_V2_EVENT";
        public static final String POST_UPDATE_IDP_GROUP_LIST_OF_ROLE_V2_EVENT =
                "POST_UPDATE_IDP_GROUP_LIST_OF_ROLE_V2_EVENT";
        public static final String PRE_GET_PERMISSION_LIST_OF_ROLE_V2_EVENT =
                "PRE_GET_PERMISSION_LIST_OF_ROLE_V2_EVENT";
        public static final String POST_GET_PERMISSION_LIST_OF_ROLE_V2_EVENT =
                "POST_GET_PERMISSION_LIST_OF_ROLE_V2_EVENT";
        public static final String PRE_UPDATE_PERMISSIONS_FOR_ROLE_V2_EVENT =
                "PRE_UPDATE_PERMISSIONS_FOR_ROLE_V2_EVENT";
        public static final String POST_UPDATE_PERMISSIONS_FOR_ROLE_V2_EVENT =
                "POST_UPDATE_PERMISSIONS_FOR_ROLE_V2_EVENT";
        public static final String PRE_ADD_MAIN_ROLE_TO_SHARED_ROLE_RELATIONSHIP_V2_EVENT =
                "PRE_ADD_MAIN_ROLE_TO_SHARED_ROLE_RELATIONSHIP_V2_EVENT";
        public static final String POST_ADD_MAIN_ROLE_TO_SHARED_ROLE_RELATIONSHIP_V2_EVENT =
                "POST_ADD_MAIN_ROLE_TO_SHARED_ROLE_RELATIONSHIP_V2_EVENT";

        public static final String POST_APP_USER_ATTRIBUTE_UPDATE = "POST_APP_USER_ATTRIBUTE_UPDATE";

        // API Resource Management events.
        public static final String PRE_ADD_API_RESOURCE = "PRE_ADD_API_RESOURCE";

        public static final String POST_ADD_API_RESOURCE = "POST_ADD_API_RESOURCE";

        public static final String PRE_DELETE_API_RESOURCE = "PRE_DELETE_API_RESOURCE";

        public static final String POST_DELETE_API_RESOURCE = "POST_DELETE_API_RESOURCE";

        public static final String PRE_UPDATE_API_RESOURCE = "PRE_UPDATE_API_RESOURCE";

        public static final String POST_UPDATE_API_RESOURCE = "POST_UPDATE_API_RESOURCE";

        public static final String PRE_UPDATE_SCOPE_METADATA = "PRE_UPDATE_SCOPE_METADATA";

        public static final String POST_UPDATE_SCOPE_METADATA = "POST_UPDATE_SCOPE_METADATA";

        public static final String PRE_DELETE_API_RESOURCE_SCOPES = "PRE_DELETE_API_RESOURCE_SCOPES";

        public static final String POST_DELETE_API_RESOURCE_SCOPES = "POST_DELETE_API_RESOURCE_SCOPES";

        public static final String PRE_DELETE_SCOPE = "PRE_DELETE_SCOPE";

        public static final String POST_DELETE_SCOPE = "POST_DELETE_SCOPE";

        public static final String PRE_PUT_API_RESOURCE_SCOPES = "PRE_PUT_API_RESOURCE_SCOPES";

        public static final String POST_PUT_API_RESOURCE_SCOPES = "POST_PUT_API_RESOURCE_SCOPES";

        // Application Authorized API
        public static final String PRE_ADD_AUTHORIZED_API_FOR_APPLICATION_EVENT =
                "PRE_ADD_AUTHORIZED_API_FOR_APPLICATION_EVENT";
        public static final String POST_ADD_AUTHORIZED_API_FOR_APPLICATION_EVENT =
                "POST_ADD_AUTHORIZED_API_FOR_APPLICATION_EVENT";
        public static final String PRE_UPDATE_AUTHORIZED_API_FOR_APPLICATION_EVENT =
                "PRE_UPDATE_AUTHORIZED_API_FOR_APPLICATION_EVENT";
        public static final String POST_UPDATE_AUTHORIZED_API_FOR_APPLICATION_EVENT =
                "POST_UPDATE_AUTHORIZED_API_FOR_APPLICATION_EVENT";
        public static final String PRE_DELETE_AUTHORIZED_API_FOR_APPLICATION_EVENT =
                "PRE_DELETE_AUTHORIZED_API_FOR_APPLICATION_EVENT";
        public static final String POST_DELETE_AUTHORIZED_API_FOR_APPLICATION_EVENT =
                "POST_DELETE_AUTHORIZED_API_FOR_APPLICATION_EVENT";

        public static final String USER_REGISTRATION_FAILED = "USER_REGISTRATION_FAILED";
        public static final String USER_REGISTRATION_SUCCESS = "USER_REGISTRATION_SUCCESS";

        // Deprecated. Use POST_USER_PROFILE_UPDATE instead
        public static final String USER_PROFILE_UPDATE = "USER_PROFILE_UPDATE";
        public static final String POST_USER_PROFILE_UPDATE = "POST_USER_PROFILE_UPDATE";
        public static final String TOKEN_ISSUED = "TOKEN_ISSUED";
        public static final String TOKEN_REVOKED = "TOKEN_REVOKED";

        public static final String POST_ISSUE_ACCESS_TOKEN_V2 = "POST_ISSUE_ACCESS_TOKEN_V2";
    }

    /**
     * Data Publishing Event Handler related constants triggered when a session is changed.
     */
    public enum EventName {

        AUTHENTICATION_STEP_SUCCESS,
        AUTHENTICATION_STEP_FAILURE,
        AUTHENTICATION_SUCCESS,
        AUTHENTICATION_FAILURE,
        SESSION_CREATE,
        SESSION_UPDATE,
        SESSION_TERMINATE,
        SESSION_EXPIRE,
        SESSION_EXTEND,
    }

    public class EventProperty {

        private EventProperty(){}

        public static final String NOTIFICATION_CHANNEL = "notification-channel";
        public static final String MODULE = "module";
        public static final String USER_NAME = "user-name";
        public static final String ROLE_NAME = "role-name";
        public static final String ROLE_ID = "role-id";
        public static final String SHARED_ROLE_ID = "shared-role-id";
        public static final String SHARED_ROLE_TENANT_DOMAIN = "shared-role-tenant-domain";
        public static final String USER_STORE_DOMAIN = "userstore-domain";
        public static final String CONFIRMATION_CODE = "confirmation-code";
        public static final String TEMPORARY_PASSWORD = "temporary-password";
        public static final String URL_USER_NAME = "url:user-name";
        public static final String IDENTITY_MGT_CONFIG = "identityMgtConfig";
        public static final String TENANT_ID = "tenantId";
        public static final String TENANT_DOMAIN = "tenant-domain";
        public static final String APPLICATION_DOMAIN = "application-domain";
        public static final String OPERATION_STATUS = "OPERATION_STATUS";
        public static final String OPERATION_DESCRIPTION = "OPERATION_DESCRIPTION";
        public static final String USER_STORE_MANAGER = "userStoreManager";
        public static final String USER_CLAIMS = "USER_CLAIMS";
        public static final String ROLE_LIST = "ROLE_LIST";
        public static final String NEW_USER_ID_LIST = "NEW_USER_ID_LIST";
        public static final String DELETE_USER_ID_LIST = "DELETE_USER_ID_LIST";
        public static final String GROUP_LIST = "GROUP_LIST";
        public static final String NEW_GROUP_ID_LIST = "NEW_GROUP_ID_LIST";
        public static final String DELETE_GROUP_ID_LIST = "DELETE_GROUP_ID_LIST";
        public static final String CREDENTIAL = "CREDENTIAL";
        public static final String CLAIM_URI = "CLAIM_URI";
        public static final String CLAIM_VALUE = "CLAIM_VALUE";
        public static final String USER_LIST = "USER_LIST";
        public static final String PERMISSIONS = "PERMISSIONS";
        public static final String ADDED_PERMISSIONS = "ADDED_PERMISSIONS";
        public static final String DELETED_PERMISSIONS = "DELETED_PERMISSIONS";
        public static final String OLD_CREDENTIAL = "OLD_CREDENTIAL";
        public static final String NEW_ROLE_NAME = "NEW_ROLE_NAME";
        public static final String DELETED_USERS = "DELETED_USERS";
        public static final String NEW_USERS = "NEW_USERS";
        public static final String DELETED_ROLES = "DELETED_ROLES";
        public static final String NEW_ROLES = "NEW_ROLES";
        public static final String PROFILE_NAME = "PROFILE_NAME";
        public static final String VERIFIED_CHANNEL = "VERIFIED_CHANNEL";
        public static final String VERIFIED_EMAIL = "VERIFIED_EMAIL";
        public static final String RECOVERY_SCENARIO = "RECOVERY_SCENARIO";
        public static final String USER_RECOVERY_DATA = "USER_RECOVERY_DATA";
        public static final String GET_USER_RECOVERY_DATA_SCENARIO = "GET_USER_RECOVERY_DATA_SCENARIO";
        public static final String GET_USER_RECOVERY_DATA_SCENARIO_WITH_CODE_EXPIRY_VALIDATION =
                "GET_USER_RECOVERY_DATA_SCENARIO_WITH_CODE_EXPIRY_VALIDATION";
        public static final String GET_USER_RECOVERY_DATA_SCENARIO_WITHOUT_CODE_EXPIRY_VALIDATION =
                "GET_USER_RECOVERY_DATA_SCENARIO_WITHOUT_CODE_EXPIRY_VALIDATION";
        public static final String INITIATOR_TYPE = "INITIATOR_TYPE";

        public static final String REQUEST = "request";
        public static final String USER_ID = "USER_ID";
        public static final String USER_IDS = "USER_IDS";
        public static final String CONDITION = "CONDITION";
        public static final String LIMIT = "LIMIT";
        public static final String AUTHENTICATION_RESULT = "AUTHENTICATION_RESULT";
        public static final String OFFSET = "OFFSET";
        public static final String SORT_BY = "SORT_BY";
        public static final String SORT_ORDER = "SORT_ORDER";
        public static final String USER = "USER";
        public static final String FILTER = "FILTER";
        public static final String SEARCH_FILTER = "SEARCH_FILTER";
        public static final String USER_CLAIM_SEARCH_ENTRIES = "USER_CLAIM_SEARCH_ENTRIES";
        public static final String LOGIN_IDENTIFIERS = "LOGIN_IDENTIFIERS";
        public static final String CONTEXT = "context";
        public static final String SESSION_CONTEXT = "sessionContext";
        public static final String PARAMS = "params";
        public static final String AUTHENTICATION_STATUS = "authenticationStatus";
        public static final String USER_CHALLENGE_ANSWERS = "userChallengeAnswers";
        public static final String USER_OLD_CHALLENGE_ANSWERS = "userOldChallengeAnswers";
        public static final String SELF_REGISTRATION_CODE = "selfsignup-code";
        public static final String SELF_REGISTRATION_VERIFIED_CHANNEL = "selfsignup-verify-channel";
        public static final String SELF_REGISTRATION_VERIFIED_CHANNEL_CLAIM = "selfsignup-verify-channel-claim";
        public static final String SELF_SIGNUP_CONFIRM_TIME = "selfsignup-confirm-time";
        public static final String LAST_PASSWORD_UPDATE_TIME = "lastPasswordUpdateTime";
        public static final String LAST_LOGIN_TIME = "lastLoginTime";
        public static final String UPDATED_LOCKED_STATUS = "updatedLockStatus";
        public static final String UPDATED_DISABLED_STATUS = "updatedDisableStatus";
        public static final String CORRELATION_ID = "correlation-id";
        public static final String APPLICATION_NAME = "application-name";
        public static final String SERVICE_PROVIDER_UUID = "serviceProviderUUID";
        public static final String APPLICATION_ID = "application-id";
        public static final String USER_AGENT = "user-agent";
        public static final String RESEND_CODE = "resend-code";
        public static final String GENERATED_OTP = "generated-otp";
        public static final String USER_INPUT_OTP = "user-input-otp";
        public static final String CLIENT_IP = "client-ip";
        public static final String OTP_GENERATED_TIME = "otp-generated-time";
        public static final String OTP_EXPIRY_TIME = "otp-expiry-time";
        public static final String OTP_USED_TIME = "otp-used-time";
        public static final String OTP_STATUS = "otp-status";
        public static final String TRACE_ID = "traceId";
        public static final String SESSION_CONTEXT_ID = "sessionContextId";
        public static final String LOCAL_CLAIM_URI = "localClaimUri";
        public static final String LOCAL_CLAIM_PROPERTIES = "localClaimProperties";
        public static final String EXTERNAL_CLAIM_URI = "externalClaimUri";
        public static final String CLAIM_DIALECT_URI = "claimDialectUri";
        public static final String OLD_CLAIM_DIALECT_URI = "oldClaimDialectUri";
        public static final String NEW_CLAIM_DIALECT_URI = "newClaimDialectUri";
        public static final String EXTERNAL_CLAIM_PROPERTIES = "externalClaimProperties";
        public static final String PROPERTY_FAILED_LOGIN_ATTEMPTS_CLAIM = "PropertyFailedLoginAttemptsClaim";
        public static final String AUTHENTICATOR_NAME = "authenticatorName";
        public static final String MAPPED_ATTRIBUTES = "mappedAttributes";
        public static final String MAPPED_LOCAL_CLAIM_URI = "mappedLocalClaimUri";
        public static final String AUDIENCE = "audience";
        public static final String AUDIENCE_ID = "audienceId";

        public static final String UPDATED_CLAIM_MAPPINGS = "updatedClaimMappings";
        public static final String REQUIRED_ATTRIBUTES = "requiredAttributes";
        public static final String SKIP_LOCAL_USER_CLAIM_UPDATE = "skipLocalUserClaimUpdate";

        // API Resource and Application Authorized API related event properties.
        public static final String API_RESOURCE = "API_RESOURCE";
        public static final String API_ID = "API_ID";
        public static final String SCOPE_NAME = "SCOPE_NAME";
        public static final String ADDED_SCOPES = "ADDED_SCOPES";
        public static final String SCOPE = "SCOPE";
        public static final String DELETED_SCOPES = "DELETED_SCOPES";
        public static final String OLD_SCOPES = "OLD_SCOPES";
        public static final String NEW_SCOPES = "NEW_SCOPES";
        public static final String AUTHORIZED_API = "AUTHORIZED_API";
        public static final String ADDED_AUTHORIZATION_DETAILS_TYPES = "ADDED_AUTHORIZATION_DETAILS_TYPES";
        public static final String DELETED_AUTHORIZATION_DETAILS_TYPES = "DELETED_AUTHORIZATION_DETAILS_TYPES";

        public static final String ERROR_CODE = "ERROR_CODE";
        public static final String ERROR_MESSAGE = "ERROR_MESSAGE";

        public static final String SCENARIO = "SCENARIO";

        public static final String STEP_ID = "STEP_ID";
        public static final String CURRENT_AUTHENTICATOR = "CURRENT_AUTHENTICATOR";
        public static final String IDP = "IDP";

        public static final String USER_CLAIMS_ADDED = "USER_CLAIMS_ADDED";
        public static final String USER_CLAIMS_MODIFIED = "USER_CLAIMS_MODIFIED";
        public static final String USER_CLAIMS_DELETED = "USER_CLAIMS_DELETED";
        public static final String SESSION_IDS = "SESSION_IDS";

        public static final String CONSUMER_KEY = "CONSUMER_KEY";
        public static final String IAT = "IAT";
        public static final String JTI = "JTI";
        public static final String TOKEN_TYPE = "TOKEN_TYPE";
        public static final String GRANT_TYPE = "GRANT_TYPE";

        public static final String CONSUMER_KEYS = "CONSUMER_KEYS";

        public static final String IS_ORGANIZATION_USER = "IS_ORGANIZATION_USER";
        public static final String USER_RESIDENT_ORGANIZATION_ID = "USER_RESIDENT_ORGANIZATION_ID";

        public class Scenario {

            public class ScenarioTypes {

                public static final String POST_CREDENTIAL_UPDATE_BY_ADMIN = "POST_CREDENTIAL_UPDATE_BY_ADMIN";
                public static final String POST_CREDENTIAL_UPDATE_BY_USER = "POST_CREDENTIAL_UPDATE_BY_USER";
            }
        }
    }

    public class ErrorMessage {

        private ErrorMessage(){}

        public static final String FAILURE = "Failure";
        public static final String FAILED_AUTHENTICATION = "Authentication Failed.";
        public static final String FAILED_ENCRYPTION = "Encryption Failed";

    }


    public class Claim {

        private Claim(){}

        public static final String FAIL_LOGIN_ATTEMPTS = "http://wso2.org/claims/identity/failedLoginAttempts";
        public static final String UNLOCKING_TIME = "http://wso2.org/claims/identity/unlockTime";
        public static final String ACCOUNT_LOCK = "http://wso2.org/claims/identity/accountLocked";

    }
}
