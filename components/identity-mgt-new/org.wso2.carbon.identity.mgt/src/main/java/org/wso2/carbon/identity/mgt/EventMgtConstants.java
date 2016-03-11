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

package org.wso2.carbon.identity.mgt;

/**
 * Identity management related constants
 */
public class EventMgtConstants {

    private EventMgtConstants(){}

    public class PropertyConfig {

        private PropertyConfig(){}

        public static final String CONFIG_FILE_NAME = "identity-mgt.properties";
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

    }

    public class EventProperty {

        private EventProperty(){}

        public static final String MODULE = "module";
        public static final String USER_NAME = "userName";
        public static final String USER_STORE_MANAGER = "userStoreManager";
        public static final String IDENTITY_MGT_CONFIG = "identityMgtConfig";

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
