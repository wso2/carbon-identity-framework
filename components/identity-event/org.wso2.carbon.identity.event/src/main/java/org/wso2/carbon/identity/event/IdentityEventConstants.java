/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
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
        public static final String PRE_SET_USER_CLAIMS = "PRE_SET_USER_CLAIMS";
        public static final String POST_SET_USER_CLAIMS = "POST_SET_USER_CLAIMS";
        public static final String PRE_ADD_USER= "PRE_ADD_USER";
        public static final String POST_ADD_USER= "POST_ADD_USER";
        public static final String PRE_UPDATE_CREDENTIAL= "PRE_UPDATE_CREDENTIAL";
        public static final String POST_UPDATE_CREDENTIAL= "POST_UPDATE_CREDENTIAL";
        public static final String PRE_UPDATE_CREDENTIAL_BY_ADMIN= "PRE_UPDATE_CREDENTIAL_BY_ADMIN";
        public static final String POST_UPDATE_CREDENTIAL_BY_ADMIN= "POST_UPDATE_CREDENTIAL_BY_ADMIN";
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
        public static final String PRE_UPDATE_ROLE_LIST_OF_USER= "PRE_UPDATE_ROLE_LIST_OF_USER";
        public static final String POST_UPDATE_ROLE_LIST_OF_USER= "POST_UPDATE_ROLE_LIST_OF_USER";
        public static final String UPDATE_GOVERNANCE_CONFIGURATION= "UPDATE_GOVERNANCE_CONFIGURATION";

        public static final String TRIGGER_NOTIFICATION = "TRIGGER_NOTIFICATION";

    }

    public class EventProperty {

        private EventProperty(){}

        public static final String MODULE = "module";
        public static final String USER_NAME = "user-name";
        public static final String ROLE_NAME = "role-name";
        public static final String USER_STORE_DOMAIN = "userstore-domain";
        public static final String CONFIRMATION_CODE = "confirmation-code";
        public static final String TEMPORARY_PASSWORD = "temporary-password";
        public static final String URL_USER_NAME = "url:user-name";
        public static final String IDENTITY_MGT_CONFIG = "identityMgtConfig";
        public static final String TENANT_ID = "tenantId";
        public static final String TENANT_DOMAIN = "tenant-domain";
        public static final String OPERATION_STATUS = "OPERATION_STATUS";
        public static final String USER_STORE_MANAGER = "userStoreManager";
        public static final String USER_CLAIMS = "USER_CLAIMS";
        public static final String ROLE_LIST = "ROLE_LIST";
        public static final String CREDENTIAL = "CREDENTIAL";
        public static final String CLAIM_URI = "CLAIM_URI";
        public static final String CLAIM_VALUE = "CLAIM_VALUE";
        public static final String USER_LIST = "USER_LIST";
        public static final String PERMISSIONS = "PERMISSIONS";
        public static final String OLD_CREDENTIAL = "OLD_CREDENTIAL";
        public static final String NEW_ROLE_NAME = "NEW_ROLE_NAME";
        public static final String DELETED_USERS = "DELETED_USERS";
        public static final String NEW_USERS = "NEW_USERS";
        public static final String DELETED_ROLES = "DELETED_ROLES";
        public static final String NEW_ROLES = "NEW_ROLES";
        public static final String PROFILE_NAME = "PROFILE_NAME";



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
