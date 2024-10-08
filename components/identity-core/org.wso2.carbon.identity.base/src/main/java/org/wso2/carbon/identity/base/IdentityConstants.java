/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.identity.base;

/**
 * Common constants of the identity solution.
 */
public class IdentityConstants {

    public static final String DEFULT_RESOURCES = "org.wso2.carbon.identity.core.resources";
    public static final String SELF_ISSUED_ISSUER = "http://schemas.xmlsoap.org/ws/2005/05/identity/issuer/self";
    public static final String PREFIX = "ic";
    public static final String NS = "http://schemas.xmlsoap.org/ws/2005/05/identity";
    public static final String OPENID_NS = "http://schema.openid.net/2007/05";
    public final static String NS_MSFT_ADDR = "http://schemas.microsoft.com/ws/2005/05/addressing/none";
    public static final String IDENTITY_ADDRESSING_NS = "http://schemas.xmlsoap.org/ws/2006/02/addressingidentity";
    public final static String CLAIM_TENANT_DOMAIN = "http://wso2.org/claims/tenant";
    public final static String CLAIM_PPID = NS
            + "/claims/privatepersonalidentifier";
    public final static String CLAIM_OPENID = OPENID_NS + "/claims/identifier";
    public final static String PARAM_SUPPORTED_TOKEN_TYPES = "SupportedTokenTypes";
    public final static String PARAM_NOT_SUPPORTED_TOKEN_TYPES = "NotSupportedTokenTypes";
    public final static String PARAM_CARD_NAME = "CardName";
    public final static String PARAM_VALUE_CARD_NAME = "WSO2 Managed Card";
    public final static String PARAM_VALID_PERIOD = "ValidPeriod";
    public final static String PARAM_VALUE_VALID_PERIOD = "365";
    public final static String SAML10_URL = "urn:oasis:names:tc:SAML:1.0:assertion";
    public final static String SAML11_URL = "http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV1.1";
    public final static String SAML20_URL = "urn:oasis:names:tc:SAML:2.0:assertion";
    public final static String CARD_IMAGE_PATH = "/card.jpg";
    public final static String PARAM_USE_SYMM_BINDING = "useSymmBinding";
    public final static String USER_VERIFICATION_PAGE = "/UserVerification.action";
    public final static String USER_VERIFICATION_PARAM = "confString";
    public final static String XML_TOKEN = "xmlToken";
    public final static String PROFILE_NAME = "profileName";
    public final static String PASSWORD = "oppassword";
    public final static String INFOCARD_LOGIN = "opinfocardlogin";
    public static final String USER_APPROVED = "userApproved";
    public final static String WSO2_IS_NS = "http://www.wso2.org/solutions/identity";
    public final static String RESOURCES = "org.wso2.solutions.identity.resources";
    public final static String INITIAL_CLAIMS_FILE_PATH = "conf/initial-claims.xml";
    public static final String PROPERTY_USER = "IdentityProvier.User";
    public static final String HTTPS = "https://";
    public static final String HTTPS_PORT = "Ports.HTTPS";
    public static final String HOST_NAME = "HostName";
    public static final String TRUE = "true";
    public static final String PHISHING_RESISTANCE = "phishingResistanceAuthentication";
    public static final String MULTI_FACTOR_AUTH = "multifactorlogin";
    public static final String PARAM_MAP = "parameterMap";
    public static final String DESTINATION_URL = "destinationUrl";
    public static final String FORM_REDIRECTION = "jsp/redirect.jsp";
    public final static String ISSUER_SELF = "Self";
    public final static String CARD_ISSUSER_LOG = "org.wso2.solutions.identity.card";
    public final static String TOKEN_ISSUSER_LOG = "org.wso2.solutions.identity.token";
    public static final String SERVICE_NAME_STS_UT = "sts-ut";
    public static final String SERVICE_NAME_STS_UT_SYMM = "sts-ut-symm";
    public static final String SERVICE_NAME_STS_IC = "sts-ic";
    public static final String SERVICE_NAME_STS_IC_SYMM = "sts-ic-symm";
    public static final String SERVICE_NAME_MEX_UT = "mex-ut";
    public static final String SERVICE_NAME_MEX_UT_SYMM = "mex-ut-symm";
    public static final String SERVICE_NAME_MEX_IC = "mex-ic";
    public static final String SERVICE_NAME_MEX_IC_SYMM = "mex-ic-symm";
    public static final String INFOCARD_DIALECT = "http://schemas.xmlsoap.org/ws/2005/05/identity";
    public static final String OPENID_SREG_DIALECT = "http://schema.openid.net/2007/05/claims";
    public static final String OPENID_AX_DIALECT = "http://axschema.org";
    // Authentication mechanism
    public static final int AUTH_TYPE_USERNAME_TOKEN = 1;
    public static final int AUTH_TYPE_KEBEROS_TICKET = 2;
    public static final int AUTH_TYPE_X509_CERTIFICATE = 3;
    public static final int AUTH_TYPE_SELF_ISSUED = 4;
    public static final String RP_USER_ROLE = "Rp_User_Role";
    public final static String PARAM_NAME_ALLOW_USER_REGISTRATION = "allowUserReg";
    public final static String PARAM_NAME_ENABLE_OPENID_LOGIN = "enableOpenIDLogin";
    public final static String IDENTITY_DEFAULT_ROLE = "identity";
    public final static String DEFAULT_SUPER_TENAT = "identity.cloud.wso2.com";
    public static String PPID_DISPLAY_VALUE = "Private personal identifier";
    public static final String FEDERATED_IDP_SESSION_ID = "FederatedIdPSessionIndex_";

    //Event Listeners attributes
    public final static String EVENT_LISTENER_TYPE = "type";
    public final static String EVENT_LISTENER_NAME = "name";
    public final static String EVENT_LISTENER_ORDER = "orderId";
    public final static String EVENT_LISTENER_ENABLE = "enable";
    public final static String EVENT_LISTENERS = "EventListeners";
    public final static String EVENT_LISTENER = "EventListener";
    public final static String EVENT_LISTENER_PROPERTY = "Property";
    public final static String EVENT_LISTENER_PROPERTY_NAME = "name";


    // Cache Config constants
    public final static String CACHE_CONFIG = "CacheConfig";
    public final static String CACHE_MANAGER = "CacheManager";
    public final static String CACHE_MANAGER_NAME = "name";
    public final static String CACHE = "Cache";
    public final static String CACHE_NAME = "name";
    public final static String CACHE_ENABLE = "enable";
    public final static String CACHE_TIMEOUT = "timeout";
    public final static String CACHE_CAPACITY = "capacity";
    
    // Cookie Config constants
    public final static String COOKIES_CONFIG = "Cookies";
    public final static String COOKIE = "Cookie";
    public final static String COOKIE_NAME = "name";
    public final static String COOKIE_DOMAIN = "domain";
    public final static String COOKIE_COMMENT = "comment";
    public final static String COOKIE_VERSION = "version";
    public final static String COOKIE_PATH = "path";
    public final static String COOKIE_MAX_AGE = "maxAge";
    public final static String COOKIE_SECURE = "secure";
    public final static String COOKIE_HTTP_ONLY = "httpOnly";
    public final static String COOKIE_SAME_SITE = "sameSite";
    public final static String COOKIES_TO_INVALIDATE_CONFIG = "CookiesToInvalidate";

    // Store Procedure Based DAO Configuration Constants
    public final static String STORED_PROCEDURE_DAO_CONFIG = "StoredProcedureDAO";
    public final static String DAO_CONFIG = "DAO";
    public final static String DAO_NAME = "name";
    public final static String DAO_ENABLE = "enable";

    // HTTP headers which may contain IP address of the client in the order of priority
    public static final String[] HEADERS_WITH_IP = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"};
    public static final String UNKNOWN = "unknown";
    public static final String USER_IP = "user-ip";

    // Service provider constants
    public static final String SKIP_CONSENT_DISPLAY_NAME="Skip Consent";
    public static final String SKIP_CONSENT="skipConsent";
    public static final String SKIP_LOGOUT_CONSENT_DISPLAY_NAME = "Skip Logout Consent";
    public static final String SKIP_LOGOUT_CONSENT = "skipLogoutConsent";
    public static final String USE_EXTERNAL_CONSENT_PAGE_DISPLAY_NAME = "Use External Consent Page";
    public static final String USE_EXTERNAL_CONSENT_PAGE = "useExternalConsentPage";

    // Use display name of a user when filtering users.
    public static final String SHOW_DISPLAY_NAME = "UserFiltering.ShowDisplayName";

    // Configuration constants of authentication authenticator in identity.xml file.
    public static final String TAGS = "Tags";

    // User account association constants
    public static final String USER_ACCOUNT_ASSOCIATION_ENABLE_SHA256_KEY = "UserAccountAssociation.EnableSHA256Key";

    public static final String IDENTITY_UTIL_ENABLE_SHA256 = "IdentityUtil.EnableSHA256";
    public static final String CERT_THUMBPRINT_ENABLE_SHA256 = "CertThumbprint.EnableSHA256";
    public static final String ALLOW_LEGACY_ROLE_CLAIM_BEHAVIOUR = "AllowLegacyRoleClaimBehaviour";

    private IdentityConstants() {
    }

    /**
     * Server Configuration data retrieval Strings.
     */
    public static class ServerConfig {

        public final static String USER_TRUSTED_RP_STORE_LOCATION = "Security.UserTrustedRPStore.Location";
        public final static String USER_TRUSTED_RP_STORE_PASSWORD = "Security.UserTrustedRPStore.Password";
        public final static String USER_TRUSTED_RP_STORE_TYPE = "Security.UserTrustedRPStore.Type";
        public final static String USER_TRUSTED_RP_KEY_PASSWORD = "Security.UserTrustedRPStore.KeyPassword";

        public final static String USER_SSO_STORE_LOCATION = "Security.UserSSOStore.Location";
        public final static String USER_SSO_STORE_PASSWORD = "Security.UserSSOStore.Password";
        public final static String USER_SSO_STORE_TYPE = "Security.UserSSOStore.Type";
        public final static String USER_SSO_KEY_PASSWORD = "Security.UserSSOStore.KeyPassword";

        public final static String OPENID_SERVER_URL = "OpenID.OpenIDServerUrl";
        public final static String OPENID_USER_PATTERN = "OpenID.OpenIDUserPattern";
        public final static String OPENID_LOGIN_PAGE_URL = "OpenID.OpenIDLoginUrl";
        public final static String OPENID_SKIP_USER_CONSENT = "OpenID.OpenIDSkipUserConsent";
        public final static String OPENID_REMEMBER_ME_EXPIRY = "OpenID.OpenIDRememberMeExpiry";
        public final static String OPENID_USE_MULTIFACTOR_AUTHENTICATION = "OpenID.UseMultifactorAuthentication";
        public final static String OPENID_DISABLE_DUMB_MODE = "OpenID.DisableOpenIDDumbMode";
        public final static String OPENID_SESSION_TIMEOUT = "OpenID.SessionTimeout";
        public static final String ACCEPT_SAMLSSO_LOGIN = "OpenID.AcceptSAMLSSOLogin";

        public static final String OPENID_PRIVATE_ASSOCIATION_STORE_CLASS = "OpenID.OpenIDPrivateAssociationStoreClass";
        public static final String OPENID_ASSOCIATION_EXPIRY_TIME = "OpenID.OpenIDAssociationExpiryTime";

        public static final String ENABLE_OPENID_ASSOCIATION_CLEANUP_TASK = "OpenID.EnableOpenIDAssociationCleanupTask";
        public static final String OPENID_ASSOCIATION_CLEANUP_PERIOD = "OpenID.OpenIDAssociationCleanupPeriod";
        public static final String OPENID_PRIVATE_ASSOCIATION_SERVER_KEY = "OpenID.OpenIDPrivateAssociationServerKey";

        public static final String ISSUER_POLICY = "Identity.IssuerPolicy";
        public static final String TOKEN_VALIDATE_POLICY = "Identity.TokenValidationPolicy";
        public static final String BLACK_LIST = "Identity.BlackList";
        public static final String WHITE_LIST = "Identity.WhiteList";
        public static final String SYSTEM_KEY_STORE_PASS = "Identity.System.StorePass";
        public static final String SYSTEM_KEY_STORE = "Identity.System.KeyStore";

        // Location of the identity provider main key store
        public final static String IDP_STORE_LOCATION = "Security.KeyStore.Location";

        // Password of the identity provider main key store
        public final static String IDP_STORE_PASSWORD = "Security.KeyStore.Password";

        // Store type of the identity provider main key store
        public final static String IDP_STORE_TYPE = "Security.KeyStore.Type";

        // Location of the key store used to store users' personal certificates
        public final static String USER_PERSONAL_STORE_LOCATION = "Security.UserPersonalCeritificateStore.Location";

        // Password of the key store used to store users' personal certificates
        public final static String USER_PERSONAL_STORE_PASSWORD = "Security.UserPersonalCeritificateStore.Password";

        // Type of the key store used to store users' personal certificates
        public final static String USER_PERSONAL_STORE_TYPE = "Security.UserPersonalCeritificateStore.Type";

        public final static String USER_PERSONAL_KEY_PASSWORD = "Security.UserPersonalCeritificateStore.KeyPassword";

        //XMPP Settings for multifactor authentication

        public final static String XMPP_SETTINGS_PROVIDER = "MultifactorAuthentication.XMPPSettings.XMPPConfig.XMPPProvider";

        public final static String XMPP_SETTINGS_SERVER = "MultifactorAuthentication.XMPPSettings.XMPPConfig.XMPPServer";

        public final static String XMPP_SETTINGS_PORT = "MultifactorAuthentication.XMPPSettings.XMPPConfig.XMPPPort";

        public final static String XMPP_SETTINGS_EXT = "MultifactorAuthentication.XMPPSettings.XMPPConfig.XMPPExt";

        public final static String XMPP_SETTINGS_USERNAME = "MultifactorAuthentication.XMPPSettings.XMPPConfig.XMPPUserName";

        public final static String XMPP_SETTINGS_PASSWORD = "MultifactorAuthentication.XMPPSettings.XMPPConfig.XMPPPassword";

        //SAML SSO Service config
        public final static String SAMLSSO = "samlsso";
        public final static String ENTITY_ID = "SSOService.EntityID";
        public final static String SSO_IDP_URL = "SSOService.IdentityProviderURL";
        public final static String SSO_ARTIFACT_URL = "SSOService.ArtifactResolutionEndpoint";
        public final static String DEFAULT_LOGOUT_ENDPOINT = "SSOService.DefaultLogoutEndpoint";
        public final static String NOTIFICATION_ENDPOINT = "SSOService.NotificationEndpoint";
        public final static String SSO_ATTRIB_CLAIM_DIALECT = "SSOService.AttributesClaimDialect";
        public static final String SINGLE_LOGOUT_RETRY_COUNT = "SSOService.SingleLogoutRetryCount";
        public static final String SINGLE_LOGOUT_RETRY_INTERVAL = "SSOService.SingleLogoutRetryInterval";
        public static final String SSO_TENANT_PARTITIONING_ENABLED = "SSOService.TenantPartitioningEnabled";
        public static final String ACCEPT_OPENID_LOGIN = "SSOService.AcceptOpenIDLogin";
        public static final String SAML_RESPONSE_VALIDITY_PERIOD = "SSOService.SAMLResponseValidityPeriod";
        public static final String SAML2_ARTIFACT_VALIDITY_PERIOD = "SSOService.SAML2ArtifactValidityPeriodInMinutes";
        public static final String SSO_DEFAULT_SIGNING_ALGORITHM = "SSOService.SAMLDefaultSigningAlgorithmURI";
        public static final String SSO_DEFAULT_DIGEST_ALGORITHM = "SSOService.SAMLDefaultDigestAlgorithmURI";
        public static final String SSO_DEFAULT_ASSERTION_ENCRYPTION_ALGORITHM = "SSOService" +
                ".SAMLDefaultAssertionEncryptionAlgorithmURI";
        public static final String SSO_DEFAULT_KEY_ENCRYPTION_ALGORITHM = "SSOService" +
                ".SAMLDefaultKeyEncryptionAlgorithmURI";
        public static final String SLO_HOST_NAME_VERIFICATION_ENABLED = "SSOService.SLOHostNameVerificationEnabled";
        public static final String SAML_METADATA_VALIDITY_PERIOD = "SSOService.SAMLMetadataValidityPeriod";
        public static final String SAML_SESSION_NOT_ON_OR_AFTER_PERIOD = "SSOService.SAMLSessionNotOnOrAfterPeriod";
        public static final String SAML_METADATA_SIGNING_ENABLED = "SSOService.SAMLMetadataSigningEnabled";
        public static final String SAML_METADATA_IDP_ENABLE_SHA256_ALGO = "SSOService.SAMLIDPMetadataEnableSHA256Alg";
        public static final String SAML_METADATA_SP_ENABLE_SHA256_ALGO = "SSOService.SAMLSPMetadataParsingEnableSHA256Alg";
        public static final String SAML_ECP_URL = "SSOService.SAMLECPEndpoint";
        public static final String SAML_METADATA_AUTHN_REQUESTS_SIGNING_ENABLED = "SSOService" +
                ".SAML2AuthnRequestsSigningEnabled";
        public static final String ADD_NAME_ID_POLICY_IF_UNSPECIFIED = "SSOService" +
                ".SAML2AuthnRequestNameIdPolicyDefinedIfUnspecified";

        //Identity Persistence Manager
        public static final String SKIP_DB_SCHEMA_CREATION = "JDBCPersistenceManager.SkipDBSchemaCreation";

        //Timeout Configurations
        public static final String SESSION_IDLE_TIMEOUT = "TimeConfig.SessionIdleTimeout";
        public static final String REMEMBER_ME_TIME_OUT = "TimeConfig.RememberMeTimeout";
        public static final String EXTEND_REMEMBER_ME_SESSION_ON_AUTH =
                "TimeConfig.ExtendRememberMeSessionTimeoutOnAuth";

        public static final String CLEAN_UP_PERIOD = "JDBCPersistenceManager.SessionDataPersist.SessionDataCleanUp.CleanUpPeriod";
        public static final String CLEAN_UP_TIMEOUT = "JDBCPersistenceManager.SessionDataPersist.SessionDataCleanUp.CleanUpTimeout";

        public static final String OPERATION_CLEAN_UP_PERIOD = "JDBCPersistenceManager.SessionDataPersist" +
                                                         ".OperationDataCleanUp.CleanUpPeriod";
        public static final String OPERATION_CLEAN_UP_TIMEOUT = "JDBCPersistenceManager.SessionDataPersist" +
                                                                ".OperationDataCleanUp.CleanUpTimeout";
        public static final String TEMP_DATA_CLEAN_UP_TIMEOUT = "JDBCPersistenceManager.SessionDataPersist" +
                                                                ".TempDataCleanup.CleanUpTimeout";

        public static final String CLEAN_UP_TIMEOUT_DEFAULT = "20160";
        public static final String CLEAN_UP_PERIOD_DEFAULT = "1140";

        public static final String OPERATION_CLEAN_UP_TIMEOUT_DEFAULT = "1";
        public static final String OPERATION_CLEAN_UP_PERIOD_DEFAULT = "720";

        public static final String TEMP_DATA_CLEAN_UP_TIMEOUT_DEFAULT = "1";

        //PassiveSTS
        public static final String PASSIVE_STS_RETRY = "PassiveSTS.RetryURL";

        // Server Synchronization Tolerance Configurations
        public static final String CLOCK_SKEW = "ClockSkew";
        public static final String CLOCK_SKEW_DEFAULT = "300";

        //Enabling federated user association through dashboard
        public static final String ENABLE_FEDERATED_USER_ASSOCIATION = "EnableFederatedUserAssociation";
        public static final String ENABLE_FEDERATED_USER_ASSOCIATION_DEFAULT = "false";

        public static final String ADAPTIVE_AUTH_ALLOW_LOOPS = "AdaptiveAuth.AllowLoops";
    }

    /**
     * Local names of the identity provider constants
     */
    public static class LocalNames {
        public static final String REQUESTED_DISPLAY_TOKEN = "RequestedDisplayToken";
        public static final String REQUEST_DISPLAY_TOKEN = "RequestDisplayToken";
        public static final String DISPLAY_TOKEN = "DisplayToken";
        public static final String DISPLAY_CLAIM = "DisplayClaim";
        public static final String DISPLAY_TAG = "DisplayTag";
        public static final String DISPLAY_VALUE = "DisplayValue";
        public static final String IDENTITY_CLAIM = "Claim";
        public static final String IDENTITY_CLAIM_TYPE = "ClaimType";
        public static final String INFO_CARD_REFERENCE = "InformationCardReference";
        public static final String CARD_ID = "CardId";
        public final static String SELFISSUED_AUTHENTICATE = "SelfIssuedAuthenticate";
        public final static String USERNAME_PASSWORD_AUTHENTICATE = "UserNamePasswordAuthenticate";
        public final static String KEBEROSV5_AUTHENTICATE = "KerberosV5Authenticate";
        public final static String X509V3_AUTNENTICATE = "X509V3Authenticate";
        public final static String IDENTITY = "Identity";
        public final static String OPEN_ID_TOKEN = "OpenIDToken";
    }

    public static class IdentityTokens {

        public static final String FILE_NAME = "identity_log_tokens.properties";
        public static final String READ_LOG_TOKEN_PROPERTIES = "Read_Log_Token_Properties";

        public static final String USER_CLAIMS = "UserClaims";
        public static final String USER_ID_TOKEN = "UserIdToken";
        public static final String XACML_REQUEST = "XACML_Request";
        public static final String XACML_RESPONSE = "XACML_Response";
        public static final String NTLM_TOKEN = "NTLM_Token";
        public static final String SAML_ASSERTION = "SAML_Assertion";
        public static final String SAML_REQUEST = "SAML_Request";
        public static final String ACCESS_TOKEN = "AccessToken";
        public static final String REFRESH_TOKEN = "RefreshToken";
        public static final String AUTHORIZATION_CODE = "AuthorizationCode";
    }

    /**
     * Common constants related to OAuth/OpenID Connect.
     */
    public static class OAuth {

        public static final String OAUTH1_REQUEST_TOKEN_URL = "OAuth.OAuth1RequestTokenUrl";
        public static final String OAUTH1_AUTHORIZE_URL = "OAuth.OAuth1AuthorizeUrl";
        public static final String OAUTH1_ACCESSTOKEN_URL = "OAuth.OAuth1AccessTokenUrl";
        public static final String OAUTH2_AUTHZ_EP_URL = "OAuth.OAuth2AuthzEPUrl";
        public static final String OAUTH2_PAR_EP_URL = "OAuth.OAuth2ParEPUrl";
        public static final String OAUTH2_TOKEN_EP_URL = "OAuth.OAuth2TokenEPUrl";
        public static final String OAUTH2_USERINFO_EP_URL = "OAuth.OAuth2UserInfoEPUrl";
        public static final String OAUTH2_REVOKE_EP_URL = "OAuth.OAuth2RevokeEPUrl";
        public static final String OAUTH2_INTROSPECT_EP_URL = "OAuth.OAuth2IntrospectEPUrl";
        public static final String OIDC_CHECK_SESSION_EP_URL = "OAuth.OIDCCheckSessionEPUrl";
        public static final String OIDC_LOGOUT_EP_URL = "OAuth.OIDCLogoutEPUrl";
        public static final String OIDC_WEB_FINGER_EP_URL = "OAuth.OIDCWebFingerEPUrl";
        public static final String OAUTH2_DCR_EP_URL = "OAuth.OAuth2DCREPUrl";
        public static final String OAUTH2_JWKS_EP_URL = "OAuth.OAuth2JWKSPage";
        public static final String OIDC_DISCOVERY_EP_URL = "OAuth.OIDCDiscoveryEPUrl";
        public static final String OAUTH1_REQUEST_TOKEN_URL_V2 = "OAuth.V2.OAuth1RequestTokenUrl";
        public static final String OAUTH1_AUTHORIZE_URL_V2 = "OAuth.V2.OAuth1AuthorizeUrl";
        public static final String OAUTH1_ACCESSTOKEN_URL_V2 = "OAuth.V2.OAuth1AccessTokenUrl";
        public static final String OAUTH2_AUTHZ_EP_URL_V2 = "OAuth.V2.OAuth2AuthzEPUrl";
        public static final String OAUTH2_PAR_EP_URL_V2 = "OAuth.V2.OAuth2ParEPUrl";
        public static final String OAUTH2_TOKEN_EP_URL_V2 = "OAuth.V2.OAuth2TokenEPUrl";
        public static final String OAUTH2_USERINFO_EP_URL_V2 = "OAuth.V2.OAuth2UserInfoEPUrl";
        public static final String OAUTH2_REVOKE_EP_URL_V2 = "OAuth.V2.OAuth2RevokeEPUrl";
        public static final String OAUTH2_INTROSPECT_EP_URL_V2 = "OAuth.V2.OAuth2IntrospectEPUrl";
        public static final String OIDC_CHECK_SESSION_EP_URL_V2 = "OAuth.V2.OIDCCheckSessionEPUrl";
        public static final String OIDC_LOGOUT_EP_URL_V2 = "OAuth.V2.OIDCLogoutEPUrl";
        public static final String OIDC_WEB_FINGER_EP_URL_V2 = "OAuth.V2.OIDCWebFingerEPUrl";
        public static final String OAUTH2_DCR_EP_URL_V2 = "OAuth.V2.OAuth2DCREPUrl";
        public static final String OAUTH2_JWKS_EP_URL_V2 = "OAuth.V2.OAuth2JWKSPage";
        public static final String OIDC_DISCOVERY_EP_URL_V2 = "OAuth.V2.OIDCDiscoveryEPUrl";

        public static final String REQUEST_TOKEN = "oauth/request-token";
        public static final String AUTHORIZE_URL = "oauth/authorize-url";
        public static final String ACCESS_TOKEN = "oauth/access-token";
        public static final String AUTHORIZE = "oauth2/authorize";
        public static final String PAR = "oauth2/par";
        public static final String TOKEN = "oauth2/token";
        public static final String REVOKE = "oauth2/revoke";
        public static final String INTROSPECT = "oauth2/introspect";
        public static final String USERINFO = "oauth2/userinfo";
        public static final String CHECK_SESSION = "oidc/checksession";
        public static final String LOGOUT = "oidc/logout";

        public static final String WEBFINGER = ".well-known/webfinger";
        public static final String DCR = "api/identity/oauth2/dcr/v1.1/register";
        public static final String JWKS = "oauth2/jwks";
        public static final String DISCOVERY = "oauth2/oidcdiscovery";
        public static final String ENABLE_SHA256_JWK_THUMBPRINT = "OAuth.EnableSHA256OAuth2JWKThumbprint";
        public static final String ENABLE_SHA256_PARAMS = "OAuth.EnableSHA256Params";
    }

    /**
     * Common constants related to STS
     */
    public static class STS {

        public static final String PASSIVE_STS = "passivests";
        public static final String WSO2_CARBON_STS = "wso2carbon-sts";
        public static final String PSTS_IDENTITY_PROVIDER_URL = "PassiveSTS.IdentityProviderURL";
        public static final String STS_IDENTITY_PROVIDER_URL = "SecurityTokenService.IdentityProviderURL";
        public static final String PASSIVE_STS_SLO_HOST_NAME_VERIFICATION_ENABLED =
                "PassiveSTS.SLOHostNameVerificationEnabled";
        public static final String PASSIVE_STS_DISABLE_APPLIES_TO_IN_RESPONSE =
                "PassiveSTS.DisableAppliesToInPassiveSTSResponse";
        public static final String PASSIVE_STS_ENABLE_DEFAULT_SIGNATURE_AND_DIGEST_ALG =
                "PassiveSTS.EnableDefaultSignatureAndDigestAlgorithm";
        public static final String PASSIVE_STS_LOGOUT_WREPLY_VALIDATION =
                "PassiveSTS.EnableLogoutWreplyValidation";
    }

    /**
     * Common constants realted to SCIM
     */
    public static class SCIM {

        public static final String USER_EP_URL = "SCIM.UserEPUrl";
        public static final String GROUP_EP_URL = "SCIM.GroupEPUrl";
        public static final String USER_EP = "wso2/scim/Users";
        public static final String GROUP_EP = "wso2/scim/Groups";
    }

    /**
     * Common constants related to SCIM 2.0
     */
    public static class SCIM2 {

        public static final String USER_EP_URL = "SCIM2.UserEPUrl";
        public static final String GROUP_EP_URL = "SCIM2.GroupEPUrl";
        public static final String USER_EP = "scim2/Users";
        public static final String GROUP_EP = "scim2/Groups";
    }

    /**
     * Common constants related to Recovery.
     */
    public static class Recovery {

        public static final String RECOVERY_V1_API_ENABLE = "Recovery.EnableV1API";
    }

    /**
     * Common constants related to OpenID.
     */
    public static class OpenId {

        public final static String OPENID = "openid";
        public final static String NS = "http://schema.openid.net";
        public final static String OPENID_URL = "http://specs.openid.net/auth/2.0";
        public final static String ATTR_MODE = "openid.mode";
        public final static String ATTR_IDENTITY = "openid.identity";
        public final static String ATTR_RESPONSE_NONCE = "openid.response_nonce";
        public final static String ATTR_OP_ENDPOINT = "openid.op_endpoint";
        public final static String ATTR_NS = "openid.ns";
        public final static String ATTR_CLAIM_ID = "openid.claimed_id";
        public final static String ATTR_RETURN_TO = "openid.return_to";
        public final static String ATTR_ASSOC_HANDLE = "openid.assoc_handle";
        public final static String ATTR_SIGNED = "openid.signed";
        public final static String ATTR_SIG = "openid.sig";
        public final static String OPENID_IDENTIFIER = "openid_identifier";
        public final static String ASSOCIATE = "associate";
        public final static String CHECKID_SETUP = "checkid_setup";
        public final static String CHECKID_IMMEDIATE = "checkid_immediate";
        public final static String CHECK_AUTHENTICATION = "check_authentication";
        public final static String DISC = "openid-disc";
        public static final String PREFIX = "openid";
        public final static String ASSERTION = "openidAssertion";
        public final static String AUTHENTICATED = "authenticated";
        public final static String ONLY_ONCE = "Only Once";
        public final static String ONCE = "once";
        public final static String ALWAYS = "always";
        public final static String DENY = "Deny";
        public final static String ACTION = "_action";
        public final static String OPENID_RESPONSE = "id_res";
        public static final String AUTHENTICATED_AND_APPROVED = "authenticatedAndApproved";
        public final static String CANCEL = "cancel";
        public final static String FALSE = "false";
        public final static String PARAM_LIST = "parameterlist";
        public final static String PASSWORD = "password";
        public static final String SERVICE_NAME_STS_OPENID = "sts-openid-ut";
        public static final String SERVICE_NAME_MEX_OPENID = "mex-openid-ut";
        public static final String SERVICE_NAME_MEX_IC_OPENID = "mex-openid-ic";
        public static final String SERVICE_NAME_STS_IC_OPENID = "sts-openid-ic";

        public static final String SIMPLE_REGISTRATION = "sreg";
        public static final String ATTRIBUTE_EXCHANGE = "ax";
        public static final String PAPE = "pape";

        public static class PapeAttributes {

            public final static String AUTH_POLICIES = "auth_policies";
            public final static String NIST_AUTH_LEVEL = "nist_auth_level";
            public final static String AUTH_AGE = "auth_age";
            public final static String PHISHING_RESISTANCE = "http://schemas.openid.net/pape/policies/2007/06/phishing-resistant";
            public final static String MULTI_FACTOR = "http://schemas.openid.net/pape/policies/2007/06/multi-factor";
            public final static String MULTI_FACTOR_PHYSICAL = "http://schemas.openid.net/pape/policies/2007/06/multi-factor-physical";
            public final static String XMPP_BASED_MULTIFACTOR_AUTH = "xmpp_based_multifactor_auth";
            public final static String INFOCARD_BASED_MULTIFACTOR_AUTH = "infocard_based_multifactor_auth";
        }

        public static class SimpleRegAttributes {

            // As per the OpenID Simple Registration Extension 1.0 specification
            // fields below should
            // be included in the Identity Provider's response when
            // "openid.mode" is "id_res"

            public final static String NS_SREG = "http://openid.net/sreg/1.0";
            public final static String NS_SREG_1 = "http://openid.net/extensions/sreg/1.1";
            public final static String SREG = "openid.sreg.";
            public final static String OP_SREG = "openid.ns.sreg";
        }

        public static class ExchangeAttributes extends SimpleRegAttributes {

            public final static String NS = "http://axschema.org";
            public final static String NS_AX = "http://openid.net/srv/ax/1.0";
            public final static String EXT = "openid.ns.ext1";
            public final static String MODE = "openid.ext1.mode";
            public final static String TYPE = "openid.ext1.type.";
            public final static String VALUE = "openid.ext1.value.";
            public final static String FETCH_RESPONSE = "fetch_response";
        }
    }

    public static class CarbonPlaceholders {

        public static final String CARBON_HOST = "${carbon.host}";
        public static final String CARBON_PORT = "${carbon.management.port}";
        public static final String CARBON_PORT_HTTP = "${mgt.transport.http.port}";
        public static final String CARBON_PORT_HTTPS = "${mgt.transport.https.port}";
        public static final String CARBON_PROXY_CONTEXT_PATH = "${carbon.proxycontextpath}";
        public static final String CARBON_WEB_CONTEXT_ROOT = "${carbon.webcontextroot}";
        public static final String CARBON_PROTOCOL = "${carbon.protocol}";
        public static final String CARBON_CONTEXT = "${carbon.context}";

        public static final String CARBON_PORT_HTTP_PROPERTY = "mgt.transport.http.port";
        public static final String CARBON_PORT_HTTPS_PROPERTY = "mgt.transport.https.port";
    }

    public static class CORS {

        public static final String ALLOW_GENERIC_HTTP_REQUESTS = "CORS.AllowGenericHttpRequests";
        public static final String ALLOW_ANY_ORIGIN = "CORS.AllowAnyOrigin";
        public static final String ALLOWED_ORIGINS = "CORS.AllowedOrigins.Origin";
        public static final String ALLOW_SUBDOMAINS = "CORS.AllowSubdomains";
        public static final String SUPPORTED_METHODS = "CORS.SupportedMethods.Method";
        public static final String SUPPORT_ANY_HEADER = "CORS.SupportAnyHeader";
        public static final String SUPPORTED_HEADERS = "CORS.SupportedHeaders.Header";
        public static final String EXPOSED_HEADERS = "CORS.ExposedHeaders.Header";
        public static final String SUPPORTS_CREDENTIALS = "CORS.SupportsCredentials";
        public static final String MAX_AGE = "CORS.MaxAge";
        public static final String TAG_REQUESTS = "CORS.TagRequests";
    }

    /**
     * Contains the constants related to Legacy Feature config elements.
     */
    public static class LegacyFeatureConfigElements {

        public final static String LEGACY_FEATURE_CONFIG = "LegacyFeatures";
        public final static String LEGACY_FEATURE = "LegacyFeature";
        public final static String LEGACY_FEATURE_ID = "Id";
        public final static String LEGACY_FEATURE_VERSION = "Version";
        public final static String LEGACY_FEATURE_ENABLE = "Enable";
    }

    /**
     * Contains the constants related to Reverse Proxy configs elements.
     */
    public static class ReverseProxyConfigElements {

        public final static String REVERSE_PROXY_CONFIG = "ReverseProxyConfig";
        public final static String REVERSE_PROXY = "ReverseProxy";
        public final static String PROXY_CONTEXT = "ProxyContext";
        public final static String DEFAULT_CONTEXT = "DefaultContext";
    }

    /**
     * Contains the constants related to System roles configs elements.
     */
    public static class SystemRoles {

        // System roles config element.
        public static final String SYSTEM_ROLES_CONFIG_ELEMENT = "SystemRoles";
        public static final String SYSTEM_ROLES_ENABLED_CONFIG_ELEMENT = "SystemRoles.Enabled";
        public static final String ROLE_CONFIG_ELEMENT = "Role";
        public static final String ROLE_NAME_CONFIG_ELEMENT = "Name";
        public static final String ROLE_MANDATORY_SCOPES_CONFIG_ELEMENT = "MandatoryScopes";
        public static final String ROLE_SCOPE_CONFIG_ELEMENT = "Scope";
        public static final String API_RESOURCE_CONFIG_ELEMENT = "APIResource";
        public static final String ROLE_MANDATORY_API_RESOURCES_CONFIG_ELEMENT = "MandatoryAPIResources";

    }

    /**
     * Common constants related to API Response.
     */
    public static class APIResponse {

        public static final String SET_ACCOUNT_LOCK_AUTH_FAILURE_REASON = "APIResponse.SetAccountLockAuthFailureReason";
    }
}
