/*
 * Copyright (c) 2013-2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.util;

/**
 * Constants used in Application Authenticators Framework
 */
public abstract class FrameworkConstants {

    public static final String SESSION_DATA_KEY = "sessionDataKey";
    public static final String SECRET_TYPE = "ADAPTIVE_AUTH_CALL_CHOREO";
    public static final String QUERY_PARAMS = "commonAuthQueryParams";
    public static final String SUBJECT = "subject";
    public static final String DEFAULT_SEQUENCE = "default";
    public static final String AUTHENTICATED_AUTHENTICATORS = "authenticatedAuthenticators";
    public static final String REDIRECT_URL = "REDIRECT_URL";
    public static final String IS_API_BASED = "IS_API_BASED";
    public static final String COMMONAUTH_COOKIE = "commonAuthId";
    public static final String ALLOW_SESSION_CREATION = "allowSessionCreation";
    public static final String CONTEXT_PROP_INVALID_EMAIL_USERNAME = "InvalidEmailUsername";
    // Cookie used for post authenticaion sequence tracking
    public static final String PASTR_COOKIE = "pastr";
    public static final String CLAIM_URI_WSO2_EXT_IDP = "http://wso2.org/claims/externalIDP";
    public static final String LOCAL_ROLE_CLAIM_URI = "http://wso2.org/claims/role";
    public static final String ACCOUNT_LOCKED_CLAIM_URI = "http://wso2.org/claims/identity/accountLocked";
    public static final String ACCOUNT_DISABLED_CLAIM_URI = "http://wso2.org/claims/identity/accountDisabled";
    public static final String ACCOUNT_UNLOCK_TIME_CLAIM = "http://wso2.org/claims/identity/unlockTime";
    public static final String USERNAME_CLAIM = "http://wso2.org/claims/username";
    public static final String USER_ID_CLAIM = "http://wso2.org/claims/userid";
    public static final String EMAIL_ADDRESS_CLAIM = "http://wso2.org/claims/emailaddress";
    public static final String APP_ROLES_CLAIM = "http://wso2.org/claims/applicationRoles";
    public static final String ROLES_CLAIM = "http://wso2.org/claims/roles";

    public static final String GROUPS_CLAIM = "http://wso2.org/claims/groups";
    public static final String PROVISIONED_SOURCE_ID_CLAIM = "http://wso2.org/claims/identity/userSourceId";
    public static final String IDP_TYPE_CLAIM = "http://wso2.org/claims/identity/idpType";
    public static final String UNFILTERED_LOCAL_CLAIM_VALUES = "UNFILTERED_LOCAL_CLAIM_VALUES";
    public static final String UNFILTERED_LOCAL_CLAIMS_FOR_NULL_VALUES = "UNFILTERED_LOCAL_CLAIMS_FOR_NULL_VALUES";
    public static final String UNFILTERED_IDP_CLAIM_VALUES = "UNFILTERED_IDP_CLAIM_VALUES";
    public static final String UNFILTERED_SP_CLAIM_VALUES = "UNFILTERED_SP_CLAIM_VALUES";
    public static final String SP_TO_CARBON_CLAIM_MAPPING = "SP_TO_CARBON_CLAIM_MAPPING";
    public static final String SP_REQUESTED_CLAIMS_IN_REQUEST = "SP_REQUESTED_CLAIMS_IN_REQUEST";
    public static final String LOCAL_IDP_NAME = "LOCAL";
    public static final String FEDERATED_IDP_NAME = "FEDERATED";
    public static final String REQ_ATTR_HANDLED = "commonAuthHandled";
    public static final String LOGOUT = "commonAuthLogout";
    public static final String IDP = "idp";
    public static final String AUTHENTICATOR = "authenticator";
    public static final String SIGNATURE_ALGORITHM = "SigAlg";
    public static final String SAML_REQUEST = "SAMLRequest";
    public static final String SIGNATURE = "Signature";
    public static final String COMMONAUTH = "commonauth";
    public static final String PASSIVE_STS = "passivests";
    public static final String OPENID_SERVER = "openidserver";
    public static final String OAUTH2 = "oauth2";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String SIGN_UP_ENDPOINT = "/accountrecoveryendpoint/signup.do";
    public static final String REGISTRATION_ENDPOINT = "/accountrecoveryendpoint/register.do";
    public static final String ERROR_CODE = "errorCode";
    public static final String ERROR_MESSAGE = "errorMessage";
    public static final String ERROR_URI = "errorURI";
    public static final String REMAINING_ATTEMPTS = "remainingAttempts";
    public static final String FAILED_USERNAME = "failedUsername";
    public static final String LOCK_REASON = "lockedReason";
    public static final String NOT_SATISFY_PREREQUISITES_REASON = "NotSatisfyAuthenticatorPrerequisitesReason";
    public static final String SKIP_NONCE_COOKIE_VALIDATION = "SkipNonceCookieValidation";
    public static final String IS_MULTI_OPS_RESPONSE = "isMultiOptionsResponse";
    public static final String IS_AUTH_FLOW_CONCLUDED = "isAuthFlowConcluded";
    public static final String IS_API_BASED_AUTH_FLOW = "isAPIBasedAuthFlow";

    // This is to support sign-up form to be displayed in the provisioning flow, as when trying to displaying the
    // sign-up form, we validate whether self-sign up is enabled.
    public static final String SKIP_SIGN_UP_ENABLE_CHECK = "skipsignupenablecheck";
    public static final String SERVICE_PROVIDER = "serviceProvider";
    public static final String PASSWORD_PROVISION_ENABLED = "passwordProvisionEnabled";
    public static final String ALLOW_CHANGE_USER_NAME = "allowchangeusername";
    public static final String OPENID_IDENTITY = "openid.identity";
    public static final String OIDC = "oidc";
    public static final String AUTH_ENDPOINT_QUERY_PARAMS_ACTION_INCLUDE = "include";
    public static final String AUTH_ENDPOINT_QUERY_PARAMS_ACTION_EXCLUDE = "exclude";

    public static final String AUDIT_MESSAGE
            = "Initiator : %s | Action : %s | Target : %s | Data : { %s } | Result : %s ";
    public static final String AUDIT_SUCCESS = "Success";
    public static final String AUDIT_FAILED = "Failed";
    public static final String BASIC_AUTHENTICATOR_CLASS = "BasicAuthenticator";
    public static final String LOCAL = "LOCAL";
    public static final String SHOW_AUTHFAILURE_RESON_CONFIG = "showAuthFailureReason";
    public static final String MASK_USER_NOT_EXISTS_ERROR_CODE_CONFIG = "maskUserNotExistsErrorCode";
    public static final String SHOW_AUTH_FAILURE_REASON_ON_LOGIN_PAGE_CONF = "showAuthFailureReasonOnLoginPage";
    public static final String REDIRECT_TO_RETRY_PAGE_ON_ACCOUNT_LOCK_CONF = "redirectToRetryPageOnAccountLock";
    public static final String AUTHENTICATED_USER = "AuthenticatedUser";
    public static final String CREATED_TIMESTAMP = "CreatedTimestamp";
    public static final String UPDATED_TIMESTAMP = "UpdatedTimestamp";

    public static final String POST_AUTHENTICATION_EXTENSION_COMPLETED = "postAuthenticationExtensionCompleted";
    public static final String CURRENT_POST_AUTHENTICATION_HANDLER = "currentPostAuthHandler";
    public static final String POST_AUTHENTICATION_REDIRECTION_TRIGGERED = "postAuthenticationRedirectionTriggered";
    public static final String STEP_BASED_SEQUENCE_HANDLER_TRIGGERED = "stepBasedSequenceHandlerTriggered";
    public static final String IS_USER_CREATION_NEEDED = "isUserCreationNeeded";

    // This property is to keep track whether the post authentication handler for jit provisioning is executing
    // request flow or response flow.
    public static final String PASSWORD_PROVISION_REDIRECTION_TRIGGERED = "passwordProvisioningRedirectionTriggered";
    public static final String CHANGING_USERNAME_ALLOWED = "changingUserNameAllowed";
    public static final String MISSING_CLAIMS = "missingClaims";
    public static final String DISPLAY_NAMES = "displayNames";
    public static final String MISSING_CLAIMS_DISPLAY_NAME = "missingClaimsDisplayName";
    public static final String POST_AUTH_MISSING_CLAIMS_ERROR = "postAuthMissingClaimsError";
    public static final String POST_AUTH_MISSING_CLAIMS_ERROR_CODE = "postAuthMissingClaimsErrorCode";

    public static final String REQUEST_PARAM_SP = "sp";
    public static final String REQUEST_PARAM_SP_UUID = "spId";
    public static final String REQUEST_PARAM_ERROR_KEY = "errorKey";
    public static final String REQUEST_PARAM_AUTH_FLOW_ID = "authFlowId";
    public static final String MAPPED_ATTRIBUTES = "MappedAttributes";
    public static final String IDP_ID = "idpId";
    public static final String FED_IDP_ID = "fedIdpId";
    public static final String ASSOCIATED_ID = "associatedID";

    public static final String JIT_PROVISIONING_FLOW = "JITProvisioningFlow";
    public static final String ALLOW_LOGIN_TO_IDP = "JITProvisioning.AllowLoginToIDP";
    public static final String SECRET_KEY_CLAIM_URL = "http://wso2.org/claims/identity/secretkey";
    public static final String ENABLE_ENCRYPTION = "EnableEncryption";
    public static final String TOTP_KEY = "CryptoService.TotpSecret";
    public static final String IDP_RESOURCE_ID = "IDPResourceID";
    public static final String ENABLE_JIT_PROVISION_ENHANCE_FEATURE = "JITProvisioning.EnableEnhancedFeature";
    public static final String ERROR_CODE_INVALID_ATTRIBUTE_UPDATE = "SUO-10000";

    // Error details sent from authenticators
    public static final String AUTH_ERROR_CODE = "AuthErrorCode";
    public static final String AUTH_ERROR_MSG = "AuthErrorMessage";
    public static final String AUTH_ERROR_URI = "AuthErrorURI";

    public static final String AUTHENTICATION_CONTEXT_PROPERTIES = "AUTHENTICATION_CONTEXT_PROPERTIES";
    public static final String ORGANIZATION_USER_PROPERTIES = "ORGANIZATION_USER_PROPERTIES";
    public static final String ORGANIZATION_AUTHENTICATOR = "OrganizationAuthenticator";
    public static final String ORGANIZATION_LOGIN_HOME_REALM_IDENTIFIER = "OrganizationSSO";
    public static final String ORGANIZATION_LOGIN_IDP_NAME = "SSO";
    public static final String ORG_ID_PARAMETER = "orgId";
    public static final String USER_ORGANIZATION_CLAIM = "user_organization";
    public static final String SESSION_AUTH_HISTORY = "SESSION_AUTH_HISTORY";

    public static final String SERVICE_PROVIDER_SUBJECT_CLAIM_VALUE = "ServiceProviderSubjectClaimValue";
    public static final String CONFIG_ENABLE_SCOPE_BASED_CLAIM_FILTERING = "EnableScopeBasedClaimFiltering";
    public static final String CONFIG_ALLOW_SP_REQUESTED_FED_CLAIMS_ONLY = "AllowSPRequestedFedClaimsOnly";

    public static final String REMEMBER_ME_OPT_ON = "on";
    public static final String LAST_FAILED_AUTHENTICATOR = "LastFailedAuthenticator";
    public static final String RUNTIME_PARAMS = "RUNTIME_PARAMS";
    public static final String SP_STANDARD_DIALECT = "SP_STANDARD_DIALECT";
    public static final String RUNTIME_CLAIMS = "RUNTIME_CLAIMS";

    public static final String INPUT_TYPE_IDENTIFIER_FIRST = "idf";
    public static final String INPUT_TYPE_LOGIN_HINT = "login_hint";

    public static final String STATUS = "&status=";
    public static final String STATUS_MSG = "&statusMsg=";
    public static final String STATUS_PARAM = "status";
    public static final String STATUS_MSG_PARAM = "statusMsg";
    public static final String ACCOUNT_LOCKED_MSG = "ACCOUNT IS LOCKED";
    public static final String ERROR_MSG = "This account is locked due to exceeding maximum number of failed attempts.";
    public static final String USER_TENANT_DOMAIN_MISMATCH = "UserTenantDomainMismatch";
    public static final String BACK_TO_FIRST_STEP = "BACK_TO_FIRST_STEP";

    public static final String AUTH_MECHANISM = "AuthMechanism";
    public static final String PASSWORD_PROPERTY = "PASSWORD_PROPERTY";

    public static final String FEDERATED_IDP_ROLE_CLAIM_VALUE_SEPARATOR =
            "FederatedIDPRoleClaimValueAttributeSeparator";
    public static final String FEDERATED_IDP_GROUP_CLAIM_VALUE_SEPARATOR =
            "FederatedIDPGroupClaimValueAttributeSeparator";
    public static final String USE_IDP_ROLE_CLAIM_AS_IDP_GROUP_CLAIM =
            "UseIDPRoleClaimAsIDPGroupClaim";

    // Current session thread local identifier.
    public static final String CURRENT_SESSION_IDENTIFIER = "currentSessionIdentifier";
    public static final String CURRENT_TOKEN_IDENTIFIER = "currentTokenIdentifier";

    // Idp to local role mapping thread local identifier.
    public static final String IDP_TO_LOCAL_ROLE_MAPPING = "idpToLocalRoleMapping";

    // Authentication flow thread local identifier.
    public static final String AUTHENTICATION_FRAMEWORK_FLOW = "authenticationFrameworkFlow";

    // Maximum retry times for session data store.
    public static final int MAX_RETRY_TIME = 3;

    public static final String TENANT_CONTEXT_PREFIX = "/t/";
    public static final String ORGANIZATION_CONTEXT_PREFIX = "/o/";

    public static final String USER_TENANT_DOMAIN = "user-tenant-domain";

    public static final String BASIC_AUTH_MECHANISM = "basic";
    public static final String RECAPTCHA_PARAM = "reCaptcha";
    public static final String RECAPTCHA_RESEND_CONFIRMATION_PARAM = "reCaptchaResend";
    public static final String RECAPTCHA_KEY_PARAM = "reCaptchaKey";
    public static final String RECAPTCHA_API_PARAM = "reCaptchaAPI";
    public static final String CAPTCHA_PARAM_STRING = "captchaParams";

    // DB product names.
    public static final String MY_SQL = "MySQL";
    public static final String MARIA_DB = "MariaDB";
    public static final String POSTGRE_SQL = "PostgreSQL";
    public static final String DB2 = "DB2";
    public static final String MICROSOFT = "Microsoft";
    public static final String S_MICROSOFT = "microsoft";
    public static final String INFORMIX = "Informix";
    public static final String H2 = "H2";
    public static final String ORACLE = "Oracle";

    public static final String SCRIPT_ENGINE_CONFIG = "AdaptiveAuth.ScriptEngine";
    public static final String THREAD_LOCAL_SCRIPT_ENGINE_CONFIG = "AdaptiveAuth.LimitScriptEngineCreation";
    public static final String OPENJDK_NASHORN = "openjdkNashorn";
    public static final String NASHORN = "nashorn";
    public static final String GRAAL_JS = "graaljs";
    public static final String POLYGLOT_CLASS = "org.graalvm.polyglot";

    // Attribute sync related constants.
    public static final String ATTRIBUTE_SYNC_METHOD = "attributeSyncMethod";
    public static final String OVERRIDE_ALL = "OVERRIDE_ALL";
    public static final String SYNC_NONE = "NONE";
    public static final String PRESERVE_LOCAL = "PRESERVE_LOCAL";
    public static final String RESTART_LOGIN_FLOW = "restartLoginFlow";
    public static final String INITIAL_CONTEXT = "initialContext";
    public static final String RESTART_LOGIN_FLOW_QUERY_PARAMS =
            "&authFailure=true&authFailureMsg=login.reinitiate.message";
    public static final String NONCE_COOKIE_WHITELISTED_AUTHENTICATORS_CONFIG =
            "SessionNonceCookie.WhitelistedAuthenticator";

    public static final String BLOCKED_USERSTORE_DOMAINS_LIST = "BlockedUserStoreDomains";
    public static final String BLOCKED_USERSTORE_DOMAINS_SEPARATOR = ",";

    public static final String IS_USER_RESOLVED = "isUserResolved";
    public static final String ERROR_STATUS_AUTH_CONTEXT_NULL = "authentication.context.null";
    public static final String ERROR_DESCRIPTION_AUTH_CONTEXT_NULL = "authentication.context.null.description";
    public static final String ERROR_STATUS_AUTH_FLOW_TIMEOUT = "authentication.flow.timeout";
    public static final String ERROR_DESCRIPTION_AUTH_FLOW_TIMEOUT = "authentication.flow.timeout.description";
    public static final String ERROR_STATUS_APP_DISABLED = "authentication.flow.app.disabled";
    public static final String ERROR_DESCRIPTION_APP_DISABLED = "authentication.flow.app.disabled.description";
    public static final String IS_SENT_TO_RETRY = "isSentToRetry";
    public static final String CONTEXT_IDENTIFIER = "contextIdentifier";
    public static final String REQ_ATTR_RETRY_STATUS = "retryStatus";
    public static final String IDP_MAPPED_USER_ROLES = "identityProviderMappedUserRoles";
    public static final String ALLOW_ASSOCIATING_TO_EXISTING_USER = "JITProvisioning.AllowAssociatingToExistingUser";

    // The constant to used as the attribute key or the property key of the federated tokens.
    public static final String FEDERATED_TOKENS = "federated_tokens";

    private FrameworkConstants() {

    }

    /**
     * Authentication framework configurations.
     */
    public static class Config {

        // Constant definitions for Elements
        public static final String ELEM_SEQUENCE = "Sequence";
        public static final String ELEM_STEP = "Step";
        public static final String ELEM_AUTHENTICATION_CHAIN = "AuthenticationChain";
        public static final String ELEM_AUTHENTICATOR = "Authenticator";
        public static final String ELEM_AUTHENTICATOR_CONFIG = "AuthenticatorConfig";
        public static final String ELEM_AUTH_ENDPOINT_QUERY_PARAM = "AuthenticationEndpointQueryParam";
        public static final String ELEM_HOST_NAME = "HostName";
        public static final String ELEM_AUTHENTICATOR_NAME_MAPPING = "AuthenticatorNameMapping";
        public static final String ELEM_IDP_CONFIG = "IdPConfig";
        public static final String ELEM_PARAMETER = "Parameter";
        public static final String ELEM_REQ_PATH_AUTHENTICATOR = "RequestPathAuthenticators";
        public static final String ELEM_AUTH_ENDPOINT_REDIRECT_PARAM = "AuthenticationEndpointRedirectParam";
        public static final String ATTR_AUTH_ENDPOINT_QUERY_PARAM_NAME = "name";
        public static final String ATTR_AUTH_ENDPOINT_QUERY_PARAM_ACTION = "action";
        public static final String REMOVE_PARAM_ON_CONSUME = "removeOnConsumeFromAPI";
        public static final String SEND_ONLY_LOCALLY_MAPPED_ROLES_OF_IDP = "FederatedRoleManagement"
                + ".ReturnOnlyMappedLocalRoles";
        public static final String SEND_MANUALLY_ADDED_LOCAL_ROLES_OF_IDP = "FederatedRoleManagement"
                + ".ReturnManuallyAddedLocalRoles";

        /**
         * Configuration name for setting the url for receiving tenant list upon any modification to a tenant
         */
        public static final String ELEM_TENANT_DATA_LISTENER_URL = "TenantDataListenerURL";
        // Constant definitions for attributes
        public static final String ATTR_AUTHENTICATOR_NAME = "name";
        public static final String ATTR_AUTHENTICATOR_IDPS = "idpList";
        public static final String ATTR_AUTHENTICATOR_ENABLED = "enabled";
        public static final String ATTR_PARAMETER_NAME = "name";
        public static final String ATTR_STEP_LOGIN_PAGE = "loginPage";
        public static final String ATTR_STEP_ORDER = "order";
        public static final String ATTR_APPLICATION_NAME = "name";
        public static final String ATTR_AUTHENTICATOR_CONFIG_NAME = "name";
        public static final String ATTR_FORCE_AUTHENTICATE = "forceAuthn";
        public static final String ATTR_CHECK_AUTHENTICATE = "checkAuthn";
        public static final String ATTR_APPLICATION_ID = "appId";
        public static final String ATTR_AUTHENTICATOR_NAME_MAPPING_NAME = "name";
        public static final String ATTR_AUTHENTICATOR_NAME_MAPPING_ALIAS = "alias";
        public static final String ATTR_ACR_LIST = "acrList";
        // Constant definitions for other QNames
        public static final String QNAME_AUTHENTICATION_ENDPOINT_URL = "AuthenticationEndpointURL";
        public static final String QNAME_AUTHENTICATION_ENDPOINT_RETRY_URL = "AuthenticationEndpointRetryURL";
        public static final String QNAME_AUTHENTICATION_ENDPOINT_ERROR_URL = "AuthenticationEndpointErrorURL";
        public static final String QNAME_AUTHENTICATION_ENDPOINT_WAIT_URL = "AuthenticationEndpointWaitURL";
        public static final String QNAME_AUTHENTICATION_ENDPOINT_IDF_CONFIRM_URL = "IdentifierFirstConfirmationURL";
        public static final String QNAME_AUTHENTICATION_ENDPOINT_PROMPT_URL = "AuthenticationEndpointPromptURL";
        public static final String QNAME_AUTHENTICATION_ENDPOINT_MISSING_CLAIMS_URL =
                "AuthenticationEndpointMissingClaimsURL";
        public static final String QNAME_PROXY_MODE = "ProxyMode";
        public static final String QNAME_MAX_LOGIN_ATTEMPT_COUNT = "MaxLoginAttemptCount";
        public static final String QNAME_EXTENSIONS = "Extensions";
        public static final String QNAME_CACHE_TIMEOUTS = "CacheTimeouts";
        public static final String QNAME_AUTHENTICATOR_CONFIGS = "AuthenticatorConfigs";
        public static final String QNAME_AUTHENTICATOR_NAME_MAPPINGS = "AuthenticatorNameMappings";
        public static final String QNAME_IDP_CONFIGS = "IdPConfigs";
        public static final String QNAME_SEQUENCES = "Sequences";
        public static final String QNAME_AUTH_ENDPOINT_QUERY_PARAMS = "AuthenticationEndpointQueryParams";
        public static final String QNAME_AUTH_ENDPOINT_REDIRECT_PARAMS = "AuthenticationEndpointRedirectParams";
        public static final String QNAME_FILTERING_ENABLED_HOST_NAMES = "FilteringEnabledHostNames";
        public static final String QNAME_ALLOW_AUTHENTICATOR_CUSTOM_CLAIM_MAPPINGS =
                "AllowCustomClaimMappingsForAuthenticators";
        public static final String QNAME_MERGE_AUTHENTICATOR_CUSTOM_CLAIM_MAPPINGS_WITH_DEFAULT =
                "AllowMergingCustomClaimMappingsWithDefaultClaimMappings";
        public static final String QNAME_ALLOW_CONSENT_PAGE_REDIRECT_PARAMS =
                "AllowConsentPageRedirectParams";

        /**
         * Configuration name for the collection of urls for receiving tenant list
         */
        public static final String QNAME_TENANT_DATA_LISTENER_URLS = "TenantDataListenerURLs";
        /**
         * Configuration name for enabling or disabling the tenant list dropdown
         */
        public static final String QNAME_TENANT_DOMAIN_DROPDOWN_ENABLED = "TenantDomainDropDownEnabled";
        public static final String QNAME_EXT_REQ_COORDINATOR = "RequestCoordinator";
        public static final String QNAME_EXT_AUTH_REQ_HANDLER = "AuthenticationRequestHandler";
        public static final String QNAME_EXT_LOGOUT_REQ_HANDLER = "LogoutRequestHandler";
        public static final String QNAME_EXT_STEP_BASED_SEQ_HANDLER = "StepBasedSequenceHandler";
        public static final String QNAME_EXT_REQ_PATH_BASED_SEQ_HANDLER = "RequestPathBasedSequenceHandler";
        public static final String QNAME_EXT_STEP_HANDLER = "StepHandler";
        public static final String QNAME_EXT_HRD = "HomeRealmDiscoverer";
        public static final String QNAME_EXT_AUTH_CONTEXT_HANDLER = "AuthenticationContextHandler";
        public static final String QNAME_EXT_CLAIM_HANDLER = "ClaimHandler";
        public static final String QNAME_EXT_ROLE_HANDLER = "ClaimHandler";
        public static final String QNAME_EXT_PROVISIONING_HANDLER = "ProvisioningHandler";
        public static final String QNAME_EXT_AUTHORIZATION_HANDLER = "AuthorizationHandler";
        public static final String QNAME_EXT_USER_STORE_ORDER_CALLBACK_HANDLER = "CallbackFactory";
        public static final String QNAME_EXT_POST_AUTHENTICATION_HANDLER = "PostAuthenticationHandler";

        /**
         * Configuration used for user session mapping.
         */
        public static final String USER_SESSION_MAPPING_ENABLED =
                "JDBCPersistenceManager.SessionDataPersist.UserSessionMapping.Enable";
        public static final String SKIP_LOCAL_USER_SEARCH_FOR_AUTHENTICATION_FLOW_HANDLERS =
                "SkipLocalUserSearchForAuthenticationFlowHandlers";

        /**
         * Configuration used for session data storage optimization.
         */
        public static final String SESSION_DATA_STORAGE_OPTIMIZATION_ENABLED =
                "JDBCPersistenceManager.SessionDataPersist.SessionDataStorageOptimization.Enable";

        /**
         * Configuration to enable publishing the active session count in analytics event.
         */
        public static final String PUBLISH_ACTIVE_SESSION_COUNT = "Analytics.PublishActiveSessionCount";

        /**
         * Configuration to enable preserving user from being logged out at password update by skipping current
         * session and token from being terminated.
         */
        public static final String PRESERVE_LOGGED_IN_SESSION_AT_PASSWORD_UPDATE =
                "PasswordUpdate.PreserveLoggedInSession";

        /**
         * Configuration to avoid deleting claim mapping during existing claim mapping syncing process with idp claims.
         */
        public static final String JIT_PROVISIONING_CONFIG = "JITProvisioning";
        public static final String INCREDIBLE_CLAIMS_CONFIG_ELEMENT = "IndelibleClaims";
        public static final String CLAIM_URI_CONFIG_ELEMENT = "ClaimURI";

        /**
         * Configuration to enable validating session expiry for authentication context cache entry.
         */
        public static final String AUTHENTICATION_CONTEXT_EXPIRY_VALIDATION =
                "EnableAuthenticationContextExpiryValidation";

        private Config() {
        }

    }

    /**
     * Parameters used in authentication requests.
     */
    public static class RequestParams {

        public static final String TYPE = "type";
        public static final String DENY = "deny";
        public static final String FORCE_AUTHENTICATE = "forceAuth";
        public static final String RE_AUTHENTICATE = "reAuth";
        public static final String PASSIVE_AUTHENTICATION = "passiveAuth";
        public static final String CALLER_PATH = "commonAuthCallerPath";
        public static final String FEDERATED_IDP = "fidp";
        public static final String ISSUER = "relyingParty";
        public static final String TENANT_DOMAIN = "tenantDomain";
        public static final String TENANT_ID = "tenantId";
        public static final String LOGOUT = "commonAuthLogout";
        public static final String IDP = "idp";
        public static final String AUTHENTICATOR = "authenticator";
        public static final String USER_ABORT = "userAbort";
        public static final String FLOW_STATUS = "authenticatorFlowStatus";
        public static final String TO_COMMONAUTH = "tocommonauth";
        public static final String MAX_AGE = "max_age";
        public static final String MANDOTARY_CLAIM_PREFIX = "claim_mand_";
        public static final String REMEMBER_ME = "chkRemember";
        public static final String INPUT_TYPE = "inputType";
        public static final String AUTH_TYPE = "authType";
        public static final String IDF = "idf";
        public static final String IDENTIFIER_CONSENT = "identifier_consent";
        public static final String RESTART_FLOW = "restart_flow";
        public static final String LOGIN_TENANT_DOMAIN = "t";
        public static final String USER_TENANT_DOMAIN_HINT = "ut";
        public static final String CORRELATION_ID = "crId";
        public static final String IS_IDF_INITIATED_FROM_AUTHENTICATOR = "isIdfInitiatedFromAuthenticator";
        public static final String SESSION_ID = "sessionId";

        private RequestParams() {
        }
    }

    /**
     * Parameters used in authentication responses.
     */
    public static class ResponseParams {

        public static final String AUTHENTICATED = "commonAuthAuthenticated";
        public static final String AUTHENTICATED_USER = "authenticatedUser";
        public static final String AUTHENTICATED_IDPS = "authenticatedIdPs";
        public static final String LOGGED_OUT = "commonAuthLoggedOut";
        public static final String USER_ATTRIBUTES = "userAttributes";

        private ResponseParams() {
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
     * Inbound authentication request types.
     */
    public static class RequestType {

        public static final String CLAIM_TYPE_OPENID = "openid";
        public static final String CLAIM_TYPE_STS = "sts";
        public static final String CLAIM_TYPE_WSO2 = "wso2";
        public static final String CLAIM_TYPE_SAML_SSO = "samlsso";
        public static final String CLAIM_TYPE_SCIM = "scim";
        public static final String CLAIM_TYPE_OIDC = "oidc";

        private RequestType() {
        }
    }

    /**
     * Request attributes.
     */
    public static class RequestAttribute {

        public static final String SESSION_DATA = "sessionData";
        public static final String AUTH_RESULT = "authResult";
        public static final String AUTH_REQUEST = "authRequest";
        public static final String HTTP_REQUEST = "HttpServletRequest";
        public static final String HTTP_RESPONSE = "HttpServletResponse";
        public static final String IDENTIFIER_FIRST_AUTHENTICATOR = "IdentifierExecutor";

        private RequestAttribute() {
        }
    }

    /**
     * Default context paths in authentication endpoint.
     */
    public static class DefaultUrlContexts {

        public static final String AUTHENTICATION_ENDPOINT = "/authenticationendpoint/login.do";
        public static final String AUTHENTICATION_ENDPOINT_RETRY = "/authenticationendpoint/retry.do";
        public static final String AUTHENTICATION_ENDPOINT_ERROR = "/authenticationendpoint/error.do";
        public static final String AUTHENTICATION_ENDPOINT_WAIT = "/authenticationendpoint/wait.do";
        public static final String IDENTIFIER_FIRST_CONFIRMATION = "/authenticationendpoint/idf-confirm.do";
        public static final String AUTHENTICATION_ENDPOINT_DYNAMIC_PROMPT = "/authenticationendpoint/dynamic_prompt.do";
        public static final String AUTHENTICATION_ENDPOINT_MISSING_CLAIMS_PROMPT = "/authenticationendpoint/claims.do";
        public static final String ACCOUNT_RECOVERY_ENDPOINT_PATH = "/accountrecoveryendpoint";

    }

    /**
     * Analytics related attributes.
     */
    public static class AnalyticsAttributes {

        public static final String USER = "user";
        public static final String SESSION_ID = "sessionId";
        public static final String IS_FEDERATED = "isFederated";
        public static final String IS_INITIAL_LOGIN = "isInitialLogin";
        public static final String HAS_FEDERATED_STEP = "hasFederatedStep";
        public static final String HAS_LOCAL_STEP = "hasLocalStep";
        public static final String AUTHN_DATA_PUBLISHER_PROXY = "AuthnDataPublisherProxy";
        public static final String SESSION_CREATE = "sessionCreated";
        public static final String SESSION_UPDATE = "sessionUpdated";
        public static final String SESSION_TERMINATE = "sessionTerminated";
        public static final String ACTIVE_SESSION_COUNT = "activeSessionCount";
    }

    /**
     * Attribute names exposed for adaptive authentication script.
     */
    public static class JSAttributes {

        public static final String JS_AUTHENTICATED_SUBJECT_IDENTIFIER = "identifier";
        public static final String JS_USERNAME = "username";
        public static final String JS_UNIQUE_ID = "uniqueId";
        public static final String JS_USER_STORE_DOMAIN = "userStoreDomain";
        public static final String JS_TENANT_DOMAIN = "tenantDomain";
        public static final String JS_SERVICE_PROVIDER_NAME = "serviceProviderName";
        public static final String JS_REQUESTED_ACR = "requestedAcr";
        public static final String JS_LAST_AUTHENTICATED_USER = "lastAuthenticatedUser";
        public static final String JS_LAST_LOGIN_FAILED_USER = "lastLoginFailedUser";
        public static final String JS_AUTHENTICATED_SUBJECT = "subject";
        public static final String JS_CURRENT_KNOWN_SUBJECT = "currentKnownSubject";
        public static final String JS_LOCAL_CLAIMS = "localClaims";
        public static final String JS_REMOTE_CLAIMS = "remoteClaims";
        public static final String JS_SELECTED_ACR = "selectedAcr";
        public static final String JS_STEPS = "steps";
        public static final String JS_CURRENT_STEP = "currentStep";
        public static final String JS_REQUEST = "request";
        public static final String JS_RESPONSE = "response";
        public static final String JS_HEADERS = "headers";
        public static final String JS_PARAMS = "params";
        public static final String JS_REQUEST_IP = "ip";
        public static final String JS_COOKIES = "cookies";
        public static final String JS_COOKIE_NAME = "name";
        public static final String JS_COOKIE_VALUE = "value";
        public static final String JS_COOKIE_COMMENT = "comment";
        public static final String JS_COOKIE_DOMAIN = "domain";
        public static final String JS_COOKIE_MAX_AGE = "max-age";
        public static final String JS_COOKIE_PATH = "path";
        public static final String JS_COOKIE_SECURE = "secure";
        public static final String JS_COOKIE_VERSION = "version";
        public static final String JS_COOKIE_HTTP_ONLY = "httpOnly";
        public static final String JS_COOKIE_SAMESITE = "sameSite";
        public static final String JS_LOCAL_ROLES = "roles";
        public static final String JS_CLAIMS = "claims";
        public static final String JS_AUTHENTICATED_IDP = "idp";
        public static final String JS_AUTHENTICATOR = "authenticator";
        public static final String JS_AUTHENTICATION_OPTIONS = "options";
        public static final String JS_LOCAL_IDP = "local";
        public static final String JS_FEDERATED_IDP = "federated";
        public static final String JS_COMMON_OPTIONS = "common";
        public static final String JS_OPTIONS_USERNAME = "username";

        public static final String PROP_CURRENT_NODE = "Adaptive.Auth.Current.Graph.Node";

        public static final String JS_FUNC_ON_LOGIN_REQUEST = "onLoginRequest";
        public static final String JS_FUNC_EXECUTE_STEP = "executeStep";
        public static final String JS_FUNC_SHOW_PROMPT = "prompt";
        public static final String JS_FUNC_CALL_AND_WAIT = "callAndWait";
        public static final String JS_CALL_AND_WAIT_STATUS = "callAndWaitReturnStatus";
        public static final String JS_CALL_AND_WAIT_DATA = "callAndWaitReturnData";
        public static final String JS_FUNC_SELECT_ACR_FROM = "selectAcrFrom";
        public static final String JS_LOG = "Log";
        public static final String JS_FUNC_SEND_ERROR = "sendError";
        public static final String JS_RETRY_STEP = "retry";
        public static final String JS_FUNC_LOAD_FUNC_LIB = "loadLocalLibrary";
        public static final String JS_FUNC_GET_SECRET_BY_NAME = "getSecretByName";
        public static final String JS_AUTH_FAILURE = "fail";
        public static final String JS_ENDPOINT_PARAMS = "endpointParams";

        public static final String IDP = "idp";
        public static final String AUTHENTICATOR = "authenticator";
        public static final String AUTHENTICATION_OPTIONS = "authenticationOptions";
        public static final String STEP_OPTIONS = "stepOptions";
        public static final String AUTHENTICATOR_PARAMS = "authenticatorParams";
        public static final String FORCE_AUTH_PARAM = "forceAuth";
        public static final String SUBJECT_IDENTIFIER_PARAM = "markAsSubjectIdentifierStep";
        public static final String SUBJECT_ATTRIBUTE_PARAM = "markAsSubjectAttributeStep";
        public static final String SKIP_PROMPT = "skipPrompt";

        public static final String POLYGLOT_SOURCE = "src.js";
        public static final String POLYGLOT_LANGUAGE = "js";
        public static final String GRAALJS = "graaljs";
        public static final String NASHORN = "nashorn";
    }

    /**
     * Domain names of internal roles.
     */
    public static class InternalRoleDomains {

        public static final String APPLICATION_DOMAIN = "Application";
        public static final String WORKFLOW_DOMAIN = "Workflow";
        private InternalRoleDomains() {
        }
    }

    /**
     * Content types.
     */
    public static class ContentTypes {

        public static final String TYPE_APPLICATION_JSON = "application/json";

        private ContentTypes() {
        }
    }

    /**
     * Constants related with Consent management.
     */
    public static class Consent {

        public static final String COLLECTION_METHOD_JIT = "Web Form - Just In Time Provisioning";
        public static final String LANGUAGE_ENGLISH = "en";
        public static final String SERVICES = "services";
        public static final String PURPOSES = "purposes";
        public static final String PII_CATEGORY = "piiCategory";
        public static final String EXPLICIT_CONSENT_TYPE = "EXPLICIT";
        public static final String INFINITE_TERMINATION = "DATE_UNTIL:INDEFINITE";
    }

    /**
     * Adaptive authentication related constants.
     */
    public static class AdaptiveAuthentication {

        public static final String ADAPTIVE_AUTH_LONG_WAIT_TIMEOUT = "AdaptiveAuth.LongWaitTimeout";
        public static final String CONF_EXECUTION_SUPERVISOR_ENABLE =
                "AdaptiveAuth.ExecutionSupervisor.Enable";
        public static final String CONF_EXECUTION_SUPERVISOR_THREAD_COUNT =
                "AdaptiveAuth.ExecutionSupervisor.ThreadCount";
        public static final String CONF_EXECUTION_SUPERVISOR_TIMEOUT =
                "AdaptiveAuth.ExecutionSupervisor.Timeout";
        public static final String CONF_EXECUTION_SUPERVISOR_MEMORY_LIMIT =
                "AdaptiveAuth.ExecutionSupervisor.MemoryLimit";
        public static final int DEFAULT_EXECUTION_SUPERVISOR_THREAD_COUNT = 1;
        public static final long DEFAULT_EXECUTION_SUPERVISOR_TIMEOUT = 500L;
        public static final long DEFAULT_EXECUTION_SUPERVISOR_MEMORY_LIMIT = -1;
        public static final String PROP_EXECUTION_SUPERVISOR_RESULT
                = "AdaptiveAuthExecutionSupervisorResult";
        public static final String AUTHENTICATOR_NAME_IN_AUTH_CONFIG
                = "AdaptiveAuth.AuthenticatorNameInAuthConfig.Enable";
        public static final String GRAALJS_SCRIPT_STATEMENTS_LIMIT
                = "AdaptiveAuth.GraalJS.ScriptStatementsLimit";
        public static final int DEFAULT_GRAALJS_SCRIPT_STATEMENTS_LIMIT = 0;
    }

    /**
     * Resident IDP related properties.
     */
    public static class ResidentIdpPropertyName {

       public static final String ACCOUNT_DISABLE_HANDLER_ENABLE_PROPERTY = "account.disable.handler.enable";
    }


    /**
     * Constants related with Analytics parameters.
     */
    public static class AnalyticsData {

        public static final String AUTHENTICATION_START_TIME = "authenticationStartTime";
        public static final String AUTHENTICATION_DURATION = "authenticationDuration";
        public static final String DATA_MAP = "dataMap";
        public static final String AUTHENTICATION_ERROR_CODE = "authenticationErrorCode";
        public static final String CURRENT_AUTHENTICATOR_START_TIME = "currentAuthenticatorStartTime";
        public static final String CURRENT_AUTHENTICATOR_DURATION = "currentAuthenticatorDuration";
        public static final String CURRENT_AUTHENTICATOR_ERROR_CODE = "currentAuthenticatorErrorCode";
        public static final String CUSTOM_PARAM_PREFIX = "customParam";
        public static final int CUSTOM_PARAM_LENGTH = 5;
        public static final String CUSTOM_PARAM_1 = CUSTOM_PARAM_PREFIX + "1";
        public static final String CUSTOM_PARAM_2 = CUSTOM_PARAM_PREFIX + "2";
        public static final String CUSTOM_PARAM_3 = CUSTOM_PARAM_PREFIX + "3";
        public static final String CUSTOM_PARAM_4 = CUSTOM_PARAM_PREFIX + "4";
        public static final String CUSTOM_PARAM_5 = CUSTOM_PARAM_PREFIX + "5";
    }

    /**
     * Default application related constants.
     */
    public static class Application {

        public static final String MY_ACCOUNT_APP = "My Account";
        public static final String MY_ACCOUNT_APP_PATH = "/myaccount";
        public static final String CONSOLE_APP = "Console";
        public static final String CONSOLE_APP_PATH = "/console";
    }

    /**
     * Auto login related constants.
     */
    public static class AutoLoginConstant {

        public static final String COOKIE_NAME = "ALOR";
        public static final String CONTENT = "content";
        public static final String DOMAIN = "domain";
    }

    /**
     * Define logging constants for framework.
     */
    public static class LogConstants {

        public static final String AUTHENTICATION_FRAMEWORK = "authentication-framework";
        public static final String AUTH_SCRIPT_LOGGING = "auth-script-logging";
        public static final String SERVICE_PROVIDER = "service provider";
        public static final String TENANT_DOMAIN = "tenant domain";
        public static final String REQUESTED_CLAIMS = "requested claims";
        public static final String MANDATORY_CLAIMS = "mandatory claims";
        public static final String USE_EXISTING_CONSENT = "using existing consent";
        public static final String CLAIMS_WITH_CONSENT = "claims with consent";
        public static final String DENIED_CLAIMS = "denied claims";
        public static final String MISSING_CLAIMS = "missing claims";
        public static final String READ_ONLY_CLAIMS = "read only claims";
        public static final String THREAD_ID = "thread id";
        public static final String CONTEXT_ID = "context id";
        public static final String ORIGINATING_ADDRESS = "originating address";
        public static final String REQUEST_HEADERS = "request headers";
        public static final String USER = "user";
        public static final String USER_STORE_DOMAIN = "user store domain";
        public static final String AUTHENTICATORS = "authenticators";
        public static final String AUTHENTICATOR_NAME = "authenticator name";
        public static final String IDP_NAMES = "idp names";
        public static final String STEP = "step";
        public static final String STEPS = "steps";
        public static final String COUNT = "count";
        public static final String AUTHENTICATED_IDPS = "authenticated idps";
        public static final String IDP = "idp";
        public static final String SESSION_CONTEXT_KEY = "session context key";

        /**
         * Define action IDs for diagnostic logs in the framework component.
         */
        public static class ActionIDs {

            public static final String INIT_AUTH_FLOW = "init-authentication-flow";
            public static final String INIT_LOGOUT_FLOW = "init-logout-flow";
            public static final String PROCESS_LOGOUT_REQUEST = "process-logout-request";
            public static final String HANDLE_CLAIM_MAPPING = "handle-claim-mappings";
            public static final String HANDLE_AUTH_REQUEST = "handle-authentication-request";
            public static final String HANDLE_AUTH_STEP = "handle-authentication-step";
            public static final String PROCESS_ACR_VALUES = "process-acr-values";
            public static final String HANDLE_MISSING_CLAIMS = "handle-missing-claims";
            public static final String PROCESS_CLAIM_CONSENT = "process-claim-consent";
            public static final String AUTHENTICATION_STEP_EXECUTION = "authentication-step-execution";
            public static final String EXECUTE_ADAPTIVE_SCRIPT = "execute-adaptive-script";
        }
    }

    /**
     * Enum for authenticator param type.
     */
    public enum AuthenticatorParamType {

        STRING,
        INTEGER,
    }

    /**
     * Enum for authenticator prompt type.
     * USER_PROMPT - Obtain data from user input.
     * INTERNAL_PROMPT - Generate required data internally.
     * REDIRECTION_PROMPT - Requires redirection.
     */
    public enum AuthenticatorPromptType {

        USER_PROMPT,
        INTERNAL_PROMPT,
        REDIRECTION_PROMPT
    }

    /**
     * Enum for authenticator message type.
     * INFO - Info messages.
     * ERROR - Error messages.
     */
    public enum AuthenticatorMessageType {

        INFO,
        ERROR
    }
}
