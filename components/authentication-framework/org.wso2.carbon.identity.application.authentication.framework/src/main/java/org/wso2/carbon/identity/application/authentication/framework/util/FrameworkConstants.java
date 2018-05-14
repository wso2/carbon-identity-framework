/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
    public static final String QUERY_PARAMS = "commonAuthQueryParams";
    public static final String SUBJECT = "subject";
    public static final String DEFAULT_SEQUENCE = "default";
    public static final String AUTHENTICATED_AUTHENTICATORS = "authenticatedAuthenticators";
    public static final String COMMONAUTH_COOKIE = "commonAuthId";
    // Cookie used for post authenticaion sequence tracking
    public static final String PASTR_COOKIE = "pastr";
    public static final String CLAIM_URI_WSO2_EXT_IDP = "http://wso2.org/claims/externalIDP";
    public static final String LOCAL_ROLE_CLAIM_URI = "http://wso2.org/claims/role";
    public static final String UNFILTERED_LOCAL_CLAIM_VALUES = "UNFILTERED_LOCAL_CLAIM_VALUES";
    public static final String UNFILTERED_IDP_CLAIM_VALUES = "UNFILTERED_IDP_CLAIM_VALUES";
    public static final String UNFILTERED_SP_CLAIM_VALUES = "UNFILTERED_SP_CLAIM_VALUES";
    public static final String SP_TO_CARBON_CLAIM_MAPPING = "SP_TO_CARBON_CLAIM_MAPPING";
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

    // This is to support sign-up form to be displayed in the provisioning flow, as when trying to displaying the
    // sign-up form, we validate whether self-sign up is enabled.
    public static final String SKIP_SIGN_UP_ENABLE_CHECK = "skipsignupenablecheck";
    public static final String PASSWORD_PROVISION_ENABLED = "passwordProvisionEnabled";
    public static final String ALLOW_CHANGE_USER_NAME = "allowchangeusername";
    public static final String OPENID_IDENTITY = "openid.identity";
    public static final String OIDC = "oidc";
    public static final String AUTH_ENDPOINT_QUERY_PARAMS_ACTION_INCLUDE = "include";
    public static final String AUTH_ENDPOINT_QUERY_PARAMS_ACTION_EXCLUDE = "exclude";

    public static final String AUDIT_MESSAGE = "Initiator : %s | Action : %s | Target : %s | Data : { %s } | Result : %s ";
    public static final String AUDIT_SUCCESS = "Success";
    public static final String AUDIT_FAILED = "Failed";
    public static final String BASIC_AUTHENTICATOR_CLASS = "BasicAuthenticator";
    public static final String LOCAL = "LOCAL";
    public static final String SHOW_AUTHFAILURE_RESON_CONFIG = "showAuthFailureReason";
    public static final String AUTHENTICATED_USER = "AuthenticatedUser";
    public static final String CREATED_TIMESTAMP = "CreatedTimestamp";
    public static final String UPDATED_TIMESTAMP = "UpdatedTimestamp";

    public static final String POST_AUTHENTICATION_EXTENSION_COMPLETED = "postAuthenticationExtensionCompleted";
    public static final String POST_AUTHENTICATION_REDIRECTION_TRIGGERED = "postAuthenticationRedirectionTriggered";

    // This property is to keep track whether the post authentication handler for jit provisioning is executing
    // request flow or response flow.
    public static final String PASSWORD_PROVISION_REDIRECTION_TRIGGERED = "passwordProvisioningRedirectionTriggered";
    public static final String CHANGING_USERNAME_ALLOWED = "changingUserNameAllowed";
    public static final String MISSING_CLAIMS = "missingClaims";
    public static final String MISSING_CLAIMS_DISPLAY_NAME = "missingClaimsDisplayName";

    public static final String REQUEST_PARAM_SP = "sp";
    public static final String MAPPED_ATTRIBUTES = "MappedAttributes";
    public static final String IDP_ID = "idpId";
    public static final String ASSOCIATED_ID = "associatedID";

    // Error details sent from authenticators
    public static final String AUTH_ERROR_CODE = "AuthErrorCode";
    public static final String AUTH_ERROR_MSG = "AuthErrorMessage";
    public static final String AUTH_ERROR_URI = "AuthErrorURI";

    public static final String AUTHENTICATION_CONTEXT_PROPERTIES = "AUTHENTICATION_CONTEXT_PROPERTIES";

    public static final String SERVICE_PROVIDER_SUBJECT_CLAIM_VALUE = "ServiceProviderSubjectClaimValue";

    private FrameworkConstants() {

    }

    public static class Config {

        // Constant definitions for Elements
        public static final String ELEM_SEQUENCE = "Sequence";
        public static final String ELEM_STEP = "Step";
        public static final String ELEM_AUTHENTICATION_CHAIN = "AuthenticationChain";
        public static final String ELEM_AUTHENTICATOR = "Authenticator";
        public static final String ELEM_AUTHENTICATOR_CONFIG = "AuthenticatorConfig";
        public static final String ELEM_AUTH_ENDPOINT_QUERY_PARAM = "AuthenticationEndpointQueryParam";
        public static final String ELEM_AUTHENTICATOR_NAME_MAPPING = "AuthenticatorNameMapping";
        public static final String ELEM_IDP_CONFIG = "IdPConfig";
        public static final String ELEM_PARAMETER = "Parameter";
        public static final String ELEM_REQ_PATH_AUTHENTICATOR = "RequestPathAuthenticators";
        public static final String ATTR_AUTH_ENDPOINT_QUERY_PARAM_NAME = "name";
        public static final String ATTR_AUTH_ENDPOINT_QUERY_PARAM_ACTION = "action";
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
        public static final String QNAME_PROXY_MODE = "ProxyMode";
        public static final String QNAME_MAX_LOGIN_ATTEMPT_COUNT = "MaxLoginAttemptCount";
        public static final String QNAME_EXTENSIONS = "Extensions";
        public static final String QNAME_CACHE_TIMEOUTS = "CacheTimeouts";
        public static final String QNAME_AUTHENTICATOR_CONFIGS = "AuthenticatorConfigs";
        public static final String QNAME_AUTHENTICATOR_NAME_MAPPINGS = "AuthenticatorNameMappings";
        public static final String QNAME_IDP_CONFIGS = "IdPConfigs";
        public static final String QNAME_SEQUENCES = "Sequences";
        public static final String QNAME_AUTH_ENDPOINT_QUERY_PARAMS = "AuthenticationEndpointQueryParams";
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
        public static final String QNAME_EXT_POST_AUTHENTICATION_HANDLER = "PostAuthenticationHandler";

        private Config() {
        }

    }

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
        public static final String FLOW_STATUS = "authenticatorFlowStatus";
        public static final String TO_COMMONAUTH = "tocommonauth";
        public static final String MAX_AGE = "max_age";
        public static final String MANDOTARY_CLAIM_PREFIX = "claim_mand_";
        public static final String REMEMBER_ME = "chkRemember";

        private RequestParams() {
        }
    }

    public static class ResponseParams {

        public static final String AUTHENTICATED = "commonAuthAuthenticated";
        public static final String AUTHENTICATED_USER = "authenticatedUser";
        public static final String AUTHENTICATED_IDPS = "authenticatedIdPs";
        public static final String LOGGED_OUT = "commonAuthLoggedOut";
        public static final String USER_ATTRIBUTES = "userAttributes";

        private ResponseParams() {
        }
    }

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

    public static class RequestAttribute {

        public static final String SESSION_DATA = "sessionData";
        public static final String AUTH_RESULT = "authResult";
        public static final String AUTH_REQUEST = "authRequest";
        public static final String HTTP_REQUEST = "HttpServletRequest";
        public static final String HTTP_RESPONSE = "HttpServletResponse";

        private RequestAttribute() {
        }
    }

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
    }

    public static class JSAttributes {

        public static final String JS_AUTHENTICATED_SUBJECT_IDENTIFIER = "authenticatedSubjectIdentifier";
        public static final String JS_USERNAME = "username";
        public static final String JS_USER_STORE_DOMAIN = "userStoreDomain";
        public static final String JS_TENANT_DOMAIN = "tenantDomain";
        public static final String JS_SERVICE_PROVIDER_NAME = "serviceProviderName";
        public static final String JS_REQUESTED_ACR = "requestedAcr";
        public static final String JS_LAST_AUTHENTICATED_USER = "lastAuthenticatedUser";
        public static final String JS_LAST_LOGIN_FAILED_USER = "lastLoginFailedUser";
        public static final String JS_AUTHENTICATED_SUBJECT = "subject";
        public static final String JS_LOCAL_CLAIMS = "localClaims";
        public static final String JS_REMOTE_CLAIMS = "remoteClaims";
        public static final String JS_SELECTED_ACR = "selectedAcr";
        public static final String JS_STEPS = "steps";
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
        public static final String JS_LOCAL_ROLES = "roles";
        public static final String JS_AUTHENTICATED_IDP = "idp";

        public static final String PROP_CURRENT_NODE = "Adaptive.Auth.Current.Graph.Node";

        public static final String JS_FUNC_INITIATE_REQUEST = "onInitialRequest";
        public static final String JS_FUNC_EXECUTE_STEP = "executeStep";
        public static final String JS_FUNC_SELECT_ACR_FROM = "selectAcrFrom";
        public static final String JS_LOG = "Log";
        public static final String JS_FUNC_SEND_ERROR = "sendError";
        public static final String JS_SHOW_ERROR_PAGE = "showErrorPage";
        public static final String JS_PAGE_URI = "pageUri";
    }

    public static class InternalRoleDomains {

        public static final String APPLICATION_DOMAIN = "Application";
        public static final String WORKFLOW_DOMAIN = "Workflow";

        private InternalRoleDomains() {
        }
    }
}
