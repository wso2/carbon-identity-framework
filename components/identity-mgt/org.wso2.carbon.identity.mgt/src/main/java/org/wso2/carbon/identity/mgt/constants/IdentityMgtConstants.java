/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
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

package org.wso2.carbon.identity.mgt.constants;

/**
 * Identity management related constants
 */
public class IdentityMgtConstants {

    public static final String IDENTITY_MANAGEMENT_PATH =
            "/repository/components/org.wso2.carbon.identity.mgt";

    public static final String IDENTITY_MANAGEMENT_KEYS = IDENTITY_MANAGEMENT_PATH + "/keys";

    public static final String IDENTITY_MANAGEMENT_DATA = IDENTITY_MANAGEMENT_PATH + "/data";

    public static final String IDENTITY_MANAGEMENT_QUESTIONS = IDENTITY_MANAGEMENT_PATH + "/questionCollection";

    public static final String IDENTITY_MANAGEMENT_CHALLENGES = IDENTITY_MANAGEMENT_PATH + "/challenges";

    public static final String CONFIRMATION_KEY_NOT_MACHING = "The credential update failed. Secret key is not matching.";

    public static final String EMAIL_CONF_DIRECTORY = "email";

    public static final String EMAIL_ADMIN_CONF_FILE = "email-admin-config.xml";

    public static final String DEFAULT_CHALLENGE_QUESTION_URI01 =
            "http://wso2.org/claims/challengeQuestion1";

    public static final String DEFAULT_CHALLENGE_ANSWER_URI01 =
            "http://wso2.org/claims/challengeAnswer1";

    public static final String DEFAULT_CHALLENGE_QUESTION_URI02 =
            "http://wso2.org/claims/challengeQuestion2";

    public static final String DEFAULT_CHALLENGE_ANSWER_URI02 =
            "http://wso2.org/claims/challengeAnswer2";

    // TODO remove this
    private static final String[] SECRET_QUESTIONS_SET01 = new String[]{"City where you were born ?",
            "Father's middle name ?", "Favorite food ?", "Favorite vacation location ?"};

    // TODO remove this
    private static final String[] SECRET_QUESTIONS_SET02 = new String[]{"Model of your first car ?",
            "Name of the hospital where you were born ?", "Name of your first pet ?", "Favorite sport ?"};

    public static final String EMAIL_ADDRESS = "email";

    public static final String FIRST_NAME = "firstName";

    public static final String USER_NAME = "admin";

    public static final String TENANT_DOMAIN = "tenantDomain";

    public static final String SECRET_KEY = "secretKey";

    public static final String USER_KEY = "userKey";

    public static final String VERIFIED_CHALLENGES = "verifiedChallenges";

    public static final String EMAIL_CONFIG_TYPE = "emailConfigType";

    public static final String REDIRECT_PATH = "redirectPath";

    public static final String EXPIRE_TIME = "expireTime";

    public static final String EMAIL_MANAGE_INTERNALLY = "emailSendingInternallyManaged";

    public static final String MAX_FAILED_ATTEMPT = "maxFailedLoginAttempt";

    public static final String CAPTCHA_VERIFICATION_INTERNALLY = "captchaVerificationInternallyManaged";

    public static final String LINE_SEPARATOR = "!";

    public static final String DEFAULT_MAX_FAIL_LOGIN_ATTEMPTS = "10";

    public static final String LAST_LOGIN_TIME = "http://wso2.org/claims/identity/lastLogonTime";

    public static final String EMAIL_TEMPLATE_PATH = "identity/config/emailTemplate";

    public static final String LAST_PASSWORD_UPDATE_TIME = "http://wso2.org/claims/identity/lastPasswordUpdateTime";

    private IdentityMgtConstants() {
    }

    public static String[] getSecretQuestionsSet01() {
        if(SECRET_QUESTIONS_SET01 != null){
            return SECRET_QUESTIONS_SET01.clone();
        }
        return new String[0];
    }

    public static String[] getSecretQuestionsSet02() {
        if(SECRET_QUESTIONS_SET02 != null){
            return SECRET_QUESTIONS_SET02.clone();
        }
        return new String[0];
    }

    public static class PropertyConfig {

        public static final String CONFIG_FILE_NAME = "identity-mgt.properties";

        public static final String USER_INFO_RECOVERY_SAA_SENABLE = "UserInfoRecovery.SaaSEnable";

        public static final String ARTIFACT_DELETE_THREAD_POOL_SIZE = "UserInfoRecovery.Artifact.Delete.ThreadPoolSize";

        public static final String NOTIFICATION_SEND_ENABLE = "Notification.Sending.Enable";

        public static final String NOTIFICATION_SEND_ACCOUNT_DISABLE = "Notification.Sending.Enable.Account.Disable";

        public static final String NOTIFICATION_SEND_ACCOUNT_ENABLE = "Notification.Sending.Enable.Account.Enable";

        public static final String RECOVERY_CLAIM = "Recovery.Claim";

        public static final String IDENTITY_LISTENER_ENABLE = "Identity.Listener.Enable";

        public static final String IDENTITY_MGT_LISTENER_CLASS = "Identity.Listener.Class";

        public static final String NOTIFICATION_SEND_INTERNALLY = "Notification.Sending.Internally.Managed";

        public static final String ACCOUNT_VERIFICATION_ENABLE = "UserAccount.Verification.Enable";

        public static final String ACCOUNT_VERIFICATION_ROLE = "UserAccount.Verification.Role";

        public static final String CAPTCHA_VERIFICATION_INTERNALLY = "Captcha.Verification.Internally.Managed";

        public static final String TEMPORARY_PASSWORD_ENABLE = "Temporary.Password.Enable";

        public static final String TEMPORARY_PASSWORD_ONETIME = "Temporary.Password.OneTime";

        public static final String TEMPORARY_PASSWORD_EXPIRE_TIME = "Temporary.Password.Expire.Time";

        public static final String AUTH_POLICY_ENABLE = "Authentication.Policy.Enable";

        public static final String AUTH_POLICY_PASSWORD_ONE_TIME = "Authentication.Policy.Check.OneTime.Password";

        public static final String AUTH_POLICY_PASSWORD_EXPIRE = "Authentication.Policy.Check.Password.Expire";

        public static final String AUTH_POLICY_ACCOUNT_LOCK = "Authentication.Policy.Check.Account.Lock";

        public static final String AUTH_POLICY_ACCOUNT_DISABLE = "Authentication.Policy.Check.Account.Disable";

        public static final String AUTH_POLICY_ACCOUNT_EXIST = "Authentication.Policy.Check.Account.Exist";

        public static final String AUTH_POLICY_ACCOUNT_LOCKING_TIME = "Authentication.Policy.Account.Lock.Time";

        public static final String AUTH_POLICY_PASSWORD_EXPIRE_TIME = "Authentication.Policy.Password.Expire.Time";

        public static final String AUTH_POLICY_LOCK_ON_FAILURE = "Authentication.Policy.Account.Lock.On.Failure";

        public static final String AUTH_POLICY_ACCOUNT_LOCKING_FAIL_ATTEMPTS = "Authentication.Policy.Account.Lock.On.Failure.Max.Attempts";

        public static final String AUTH_POLICY_ACCOUNT_LOCK_ON_CREATION = "Authentication.Policy.Account.Lock.On.Creation";

        public static final String CHALLENGE_QUESTION_SEPARATOR = "Challenge.Question.Separator";

        public static final String EXTENSION_PASSWORD_GENERATOR = "Identity.Mgt.Random.Password.Generator";

        public static final String EXTENSION_USER_DATA_STORE = "Identity.Mgt.User.Data.Store";

        public static final String EXTENSION_USER_RECOVERY_DATA_STORE = "Identity.Mgt.User.Recovery.Data.Store";

        public static final String EXTENSION_NOTIFICATION_SENDING_MODULE = "Identity.Mgt.Notification.Sending.Module";

        public static final String NOTIFICATION_LINK_EXPIRE_TIME = "Notification.Expire.Time";

        public static final String  NOTIFICATION_SENDING_THREAD_POOL_SIZE = "Notification.Sending.Thread.Pool.Size";

        public static final String  NOTIFICATION_SENDING_TIMEOUT = "Notification.Sending.Timeout";

        public static final String PASSWORD_POLICY_EXTENSIONS = "Password.policy.extensions";

        public static final String PASSWORD_LENGTH_MIN = "Password.policy.min.length";

        public static final String PASSWORD_LENGTH_MAX = "Password.policy.max.length";

        public static final String REGISTRY_CLEANUP_PERIOD = "Identity.Mgt.Registry.CleanUpPeriod";

        private PropertyConfig(){
        }
    }


    public static class Notification {

        public static final String PASSWORD_RESET_RECOVERY = "passwordReset";

        public static final String ACCOUNT_ID_RECOVERY = "accountIdRecovery";

        public static final String TEMPORARY_PASSWORD = "temporaryPassword";

        public static final String ACCOUNT_CONFORM = "accountConfirmation";

        public static final String ACCOUNT_UNLOCK = "accountUnLock";

        public static final String OTP_PASSWORD = "otp";

        public static final String ASK_PASSWORD = "askPassword";

        public static final String PASSWORD_EXPIRES = "passwordExpires";

        public static final String PASSWORD_EXPIRED = "passwordExpired";

        public static final String ACCOUNT_ENABLE = "accountEnable";

        public static final String ACCOUNT_DISABLE = "accountDisable";

        private Notification(){
        }

    }

    public class ErrorHandling {

        public static final String NOTIFICATION_FAILURE = "Notification sending failure. Notification address is not " +
                "defined for user:";

        public static final String ERROR_LOADING_EMAIL_TEMP = "Error occurred while loading email templates for user : ";

        public static final String EXTERNAL_CODE = "Error occurred while getting external code for user : ";

        public static final String CREATING_NOTIFICATION_ERROR = "Error occurred while creating notification for user : ";

        public static final String INVALID_CONFIRMATION_CODE = " Invalid confirmation code ";

        public static final String USER_ACCOUNT = " No user account found for user ";

    }

    public enum LockedReason {
        ADMIN_INITIATED,
        MAX_ATTEMT_EXCEEDED,
        UNVERIFIED
    }
}
