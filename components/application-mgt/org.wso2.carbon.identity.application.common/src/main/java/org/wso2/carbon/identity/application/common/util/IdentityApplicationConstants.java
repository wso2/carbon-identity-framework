/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.common.util;

/**
 * Application management constants.
 */
public class IdentityApplicationConstants {

    private IdentityApplicationConstants() {

    }

    public static final String REF = "ref";
    public static final String APPLICATION_AUTHENTICATION_CONGIG = "application-authentication.xml";
    public static final String APPLICATION_AUTHENTICATION_DEFAULT_NAMESPACE =
            "http://wso2.org/projects/carbon/application-authentication.xml";
    public static final String RESIDENT_IDP_RESERVED_NAME = "LOCAL";
    public static final String DEFAULT_SP_CONFIG = "default";
    public static final String DEFAULT_IDP_CONFIG = "default";

    public static final String CERTIFICATE_TYPE = "X.509";
    public static final String DATE_FORMAT = "dd/MM/yyyy";

    public static final String CERTIFICATE_VAL = "certificateVal";
    public static final String OLD_CERT_FILE = "oldCertFile";

    public static final String WSO2CARBON_CLAIM_DIALECT = "http://wso2.org/claims";
    public static final String SF_OAUTH2_TOKEN_ENDPOINT = "https://login.salesforce.com/services/oauth2/token";

    public static final String FB_AUTHZ_URL = "http://www.facebook.com/dialog/oauth";
    public static final String FB_TOKEN_URL = "https://graph.facebook.com/oauth/access_token";
    public static final String FB_USER_INFO_URL = "https://graph.facebook.com/me";

    public static final String GOOGLE_OAUTH_URL = "https://accounts.google.com/o/oauth2/auth";
    public static final String GOOGLE_TOKEN_URL = "https://accounts.google.com/o/oauth2/token";
    public static final String GOOGLE_USERINFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

    public static final String WINDOWS_LIVE_OAUTH_URL = "https://login.live.com/oauth20_authorize.srf";
    public static final String WINDOWS_LIVE_TOKEN_URL = "https://login.live.com/oauth20_token.srf";
    public static final String WINDOWS_LIVE_USERINFO_URL = "https://apis.live.net/v5.0/me?access_token=";

    public static final String YAHOO_AUTHZ_URL = "https://me.yahoo.com/";

    public static final String YAHOO_OAUTH2_URL = "https://api.login.yahoo.com/oauth2/request_auth";
    public static final String YAHOO_TOKEN_URL = "https://api.login.yahoo.com/oauth2/get_token";
    @Deprecated
    /** @deprecated Please use {@link #YAHOO_OIDC_USERINFO_URL instead. */
    public static final String YAHOO_USERINFO_URL = "https://social.yahooapis.com/v1/user/";
    public static final String YAHOO_OIDC_USERINFO_URL = "https://api.login.yahoo.com/openid/v1/userinfo";

    public static final String SESSION_IDLE_TIME_OUT = "SessionIdleTimeout";
    public static final String REMEMBER_ME_TIME_OUT = "RememberMeTimeout";
    public static final String SESSION_IDLE_TIME_OUT_DEFAULT = "15";
    public static final String REMEMBER_ME_TIME_OUT_DEFAULT = "20160";

    public static final String NAME = "IDPProperties";
    public static final String CLEAN_UP_TIMEOUT = "CleanUpTimeout";
    public static final String CLEAN_UP_TIMEOUT_DEFAULT = "20160";
    public static final String CLEAN_UP_PERIOD = "CleanUpPeriod";
    public static final String CLEAN_UP_PERIOD_DEFAULT = "1140";
    public static final String TIME_CONFIG = "TimeConfig";
    public static final String COMMONAUTH = "commonauth";
    public static final String MULTIVALUED_PROPERTY_CHARACTER = ".";
    public static final String UNIQUE_ID_CONSTANT = "UniqueID";
    public static final String PASSWORD = "password";
    public static final String RANDOM_PHRASE_PREFIX = "random-password-generated!@#$%^&*(0)+_";
    public static final String AS_IN_USERNAME_USERSTORE_FOR_JIT = "As in username";

    public static final String REDIRECT_TO_MULTI_OPTION_PAGE_ON_FAILURE = "redirectToMultiOptionPageOnFailure";

    // JustInTimeProviderConfigElements
    public static final String IS_PASSWORD_PROVISIONING_ENABLED_ELEMENT = "IsPasswordProvisioningEnabled";
    public static final String ALLOW_MODIFY_USERNAME_ELEMENT = "AllowModifyUserName";
    public static final String PROMPT_CONSENT_ELEMENT = "PromptConsent";

    // Service Provider configuration options that are stored as SP properites.
    public static final String JWKS_URI_SP_PROPERTY_NAME = "jwksURI";
    public static final String TEMPLATE_ID_SP_PROPERTY_NAME = "templateId";
    public static final String TEMPLATE_ID_SP_PROPERTY_DISPLAY_NAME = "Template Id";
    public static final String IS_MANAGEMENT_APP_SP_PROPERTY_NAME = "isManagementApp";
    public static final String IS_MANAGEMENT_APP_SP_PROPERTY_DISPLAY_NAME = "Is Management Application";
    public static final String ADVANCED_CONFIG = "advancedConfigurations";

    public static final String IDP_ISSUER_NAME = "idpIssuerName";

    public static final String USE_USER_ID_FOR_DEFAULT_SUBJECT = "useUserIdForDefaultSubject";

    /**
     * Config elements.
     */
    public static class ConfigElements {

        public static final String PROPERTIES = "Properties";
        public static final String PROPERTY = "Property";
        public static final String ATTR_NAME = "name";
        public static final String ATTR_ENABLED = "enabled";
        public static final String PROPERTY_TYPE_STRING = "STRING";
        public static final String PROPERTY_TYPE_BLOB = "BLOB";
        public static final String PROPERTY_SP_DIALECT = "SPClaimDialects";

        private ConfigElements() {

            throw new AssertionError("Must not initiate an object of ConfigElements class");
        }

    }

    /**
     * Authenticator constants.
     */
    public static class Authenticator {

        /**
         * OpenId authenticator constants.
         */
        public static class OpenID {

            public static final String NAME = "openid";
            public static final String REALM_ID = "RealmId";
            public static final String OPEN_ID_URL = "OpenIdUrl";
            public static final String IS_USER_ID_IN_CLAIMS = "IsUserIdInClaims";

            private OpenID() {

                throw new AssertionError("Must not initiate an object of OpenID class");
            }
        }

        /**
         * SAML 2 authenticator constants.
         */
        public static class SAML2SSO {

            public static final String NAME = "samlsso";
            public static final String FED_AUTH_NAME = "SAMLSSOAuthenticator";
            public static final String IDP_ENTITY_ID = "IdPEntityId";
            public static final String SP_ENTITY_ID = "SPEntityId";
            public static final String SSO_URL = "SSOUrl";
            public static final String ACS_URL = "ACSUrl";
            public static final String ECP_URL = "ECPUrl";
            public static final String IS_AUTHN_REQ_SIGNED = "ISAuthnReqSigned";
            public static final String IS_ENABLE_ASSERTION_ENCRYPTION = "IsAssertionEncrypted";
            public static final String IS_ENABLE_ASSERTION_SIGNING = "isAssertionSigned";
            public static final String IS_LOGOUT_ENABLED = "IsLogoutEnabled";
            public static final String LOGOUT_REQ_URL = "LogoutReqUrl";
            public static final String IS_SLO_REQUEST_ACCEPTED = "IsSLORequestAccepted";
            public static final String IS_LOGOUT_REQ_SIGNED = "IsLogoutReqSigned";
            public static final String IS_AUTHN_RESP_SIGNED = "IsAuthnRespSigned";
            public static final String IS_USER_ID_IN_CLAIMS = "IsUserIdInClaims";
            public static final String REQUEST_METHOD = "RequestMethod";
            public static final String SIGNATURE_ALGORITHM = "SignatureAlgorithm";
            public static final String SIGNATURE_ALGORITHM_POST = "SignatureAlgorithmPost";
            public static final String DIGEST_ALGORITHM = "DigestAlgorithm";
            public static final String INCLUDE_NAME_ID_POLICY = "IncludeNameIDPolicy";
            public static final String NAME_ID_TYPE = "NameIDType";
            public static final String INCLUDE_AUTHN_CONTEXT = "IncludeAuthnContext";
            public static final String INCLUDE_CERT = "IncludeCert";
            public static final String INCLUDE_PROTOCOL_BINDING = "IncludeProtocolBinding";
            public static final String FORCE_AUTHENTICATION = "ForceAuthentication";
            public static final String AUTHENTICATION_CONTEXT_CLASS = "AuthnContextClassRef";
            public static final String ATTRIBUTE_CUSTOM_AUTHENTICATION_CONTEXT_CLASS = "CustomAuthnContextClassRef";
            public static final String CUSTOM_AUTHENTICATION_CONTEXT_CLASS_OPTION =
                    "Custom Authentication Context Class";
            public static final String AUTHENTICATION_CONTEXT_COMPARISON_LEVEL = "AuthnContextComparisonLevel";
            public static final String ATTRIBUTE_CONSUMING_SERVICE_INDEX = "AttributeConsumingServiceIndex";
            public static final String DESTINATION_URL_PREFIX = "DestinationURI";
            public static final String RESPONSE_AUTHN_CONTEXT_CLASS_REF = "ResponseAuthnContextClassRef";
            public static final String SAML_METADATA_VALIDITY_PERIOD = "SAMLMetadataValidityPeriod";
            public static final String SAML_METADATA_VALIDITY_PERIOD_DEFAULT = "60";
            public static final String SAML_METADATA_SIGNING_ENABLED = "SAMLMetadataSigningEnabled";
            public static final String SAML_METADATA_SIGNING_ENABLED_DEFAULT = "false";
            public static final String SAML_METADATA_AUTHN_REQUESTS_SIGNING_ENABLED = "samlAuthnRequestsSigningEnabled";
            public static final String SAML_METADATA_AUTHN_REQUESTS_SIGNING_DEFAULT = "false";
            public static final String UNSPECIFIED_NAME_ID_FORMAT =
                    "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified";
            public static final String IS_ARTIFACT_BINDING_ENABLED = "ISArtifactBindingEnabled";
            public static final String ARTIFACT_RESOLVE_URL = "ArtifactResolveUrl";
            public static final String IS_ARTIFACT_RESOLVE_REQ_SIGNED = "ISArtifactResolveReqSigned";
            public static final String IS_ARTIFACT_RESPONSE_SIGNED = "ISArtifactResponseSigned";

            private SAML2SSO() {

                throw new AssertionError("Must not initiate an object of SAMLSSO class");
            }

        }

        /**
         * OpenId connect authenticator constants.
         */
        public static class OIDC extends OAuth2 {

            public static final String NAME = "openidconnect";
            public static final String USER_INFO_URL = "UserInfoUrl";
            public static final String OIDC_CHECK_SESSION_URL = "OIDCCheckSessionEPUrl";
            public static final String OIDC_LOGOUT_URL = "OIDCLogoutEPUrl";
            public static final String IS_USER_ID_IN_CLAIMS = "IsUserIdInClaims";
            public static final String IS_BASIC_AUTH_ENABLED = "IsBasicAuthEnabled";
        }

        /**
         * Passive STS authenticator constants.
         */
        public static class PassiveSTS {

            public static final String NAME = "passivests";
            public static final String REALM_ID = "RealmId";
            public static final String IDENTITY_PROVIDER_URL = "IdentityProviderUrl";
            public static final String IDENTITY_PROVIDER_ENTITY_ID = "IdPEntityId";
            public static final String IS_USER_ID_IN_CLAIMS = "IsUserIdInClaims";
            public static final String IS_ENABLE_ASSERTION_SIGNATURE_VALIDATION =
                    "IsEnablePassiveSTSAssertionSignatureValidation";
            public static final String IS_ENABLE_ASSERTION_AUDIENCE_VALIDATION =
                    "IsEnablePassiveSTSAssertionAudienceValidation";

            private PassiveSTS() {

                throw new AssertionError("Must not initiate an object of PassiveSTS class");
            }
        }

        /**
         * Facebook authenticator constants.
         */
        public static class Facebook {

            public static final String NAME = "facebook";
            public static final String CLIENT_ID = "ClientId";
            public static final String CLIENT_SECRET = "ClientSecret";
            public static final String SCOPE = "Scope";
            public static final String USER_INFO_FIELDS = "UserInfoFields";
            public static final String AUTH_ENDPOINT = "AuthnEndpoint";
            public static final String AUTH_TOKEN_ENDPOINT = "AuthTokenEndpoint";
            public static final String USER_INFO_ENDPOINT = "UserInfoEndpoint";
            public static final String CALLBACK_URL = "callBackUrl";

            private Facebook() {

                throw new AssertionError("Must not initiate an object of Facebook class");
            }
        }

        /**
         * WS Trust authenticator constants.
         */
        public static class WSTrust {

            public static final String NAME = "wstrust";
            public static final String IDENTITY_PROVIDER_URL = "IDENTITY_PROVIDER_URL";

            private WSTrust() {

                throw new AssertionError("Must not initiate an object of WSTrust class");
            }
        }

        /**
         * Fido authenticator constants.
         */
        public static class FIDO {

            public static final String FIDO_AUTH = "FidoAuth";
        }

    }

    /**
     * OAuth10A constants.
     */
    public static class OAuth10A {

        public static final String NAME = "oauth10a";
        public static final String CONSUMER_KEY = "ConsumerKey";
        public static final String CONSUMER_SECRET = "ConsumerSecret";
        public static final String OAUTH1_REQUEST_TOKEN_URL = "OAuth1RequestTokenUrl";
        public static final String OAUTH1_AUTHORIZE_URL = "OAuth1AuthorizeUrl";
        public static final String OAUTH1_ACCESS_TOKEN_URL = "OAuth1AccessTokenUrl";

        private OAuth10A() {

            throw new AssertionError("Must not initiate an object of OAuth10A class");
        }
    }

    /**
     * OAuth 2 constants.
     */
    public static class OAuth2 {

        public static final String NAME = "oauth2";
        public static final String CLIENT_ID = "ClientId";
        public static final String CLIENT_SECRET = "ClientSecret";
        public static final String OAUTH2_AUTHZ_URL = "OAuth2AuthzEPUrl";
        public static final String OAUTH2_TOKEN_URL = "OAuth2TokenEPUrl";
        public static final String OAUTH2_REVOKE_URL = "OAuth2RevokeEPUrl";
        public static final String OAUTH2_INTROSPECT_URL = "OAuth2IntrospectEPUrl";
        public static final String OAUTH2_USER_INFO_EP_URL = "OAuth2UserInfoEPUrl";
        public static final String CALLBACK_URL = "callbackUrl";
        public static final String OAUTH_CONSUMER_SECRET = "oauthConsumerSecret";

        public static final String OIDC_WEB_FINGER_EP_URL = "OIDCWebFingerEPUrl";
        public static final String OAUTH2_DCR_EP_URL = "OAuth2DCREPUrl";
        public static final String OAUTH2_JWKS_EP_URL = "OAuth2JWKSPage";
        public static final String OIDC_DISCOVERY_EP_URL = "OIDCDiscoveryEPUrl";

        private OAuth2() {

            throw new AssertionError("Must not initiate an object of OAuth2 class");
        }
    }

    /**
     * SAML 2 constants.
     */
    public static class SAML2 {

        /**
         * SAML 2 authentication context class constants.
         */
        public static class AuthnContextClass {

            public static final String IP = "Internet Protocol";
            public static final String IP_PASSWORD = "Internet Protocol Password";
            public static final String KERBEROS = "Kerberos";
            public static final String MOBILE_ONE_FACTOR_UNREGISTERED = "Mobile One Factor Unregistered";
            public static final String MOBILE_TWO_FACTOR_UNREGISTERED = "Mobile Two Factor Unregistered";
            public static final String MOBILE_ONE_FACTOR_CONTRACT = "Mobile One Factor Contract";
            public static final String MOBILE_TWO_FACTOR_CONTRACT = "Mobile Two Factor Contract";
            public static final String PASSWORD = "Password";
            public static final String PASSWORD_PROTECTED_TRANSPORT = "Password Protected Transport";
            public static final String PREVIOUS_SESSION = "Previous Session";
            public static final String X509 = "Public Key - X.509";
            public static final String PGP = "Public Key - PGP";
            public static final String SPKI = "Public Key - SPKI";
            public static final String XML_DSIG = "Public Key - XML Digital Signature";
            public static final String SMARTCARD = "Smartcard";
            public static final String SMARTCARD_PKI = "Smartcard PKI";
            public static final String SOFTWARE_PKI = "Software PKI";
            public static final String TELEPHONY = "Telephony";
            public static final String NOMAD_TELEPHONY = "Telephony (Nomadic)";
            public static final String PERSONAL_TELEPHONY = "Telephony (Personalized)";
            public static final String AUTHENTICATED_TELEPHONY = "Telephony (Authenticated)";
            public static final String SECURE_REMOTE_PASSWORD = "Secure Remote Password";
            public static final String TLS_CLIENT = "SSL/TLS Certificate-Based Client Authentication";
            public static final String TIME_SYNC_TOKEN = "Time Sync Token";
            public static final String UNSPECIFIED = "Unspecified";
        }

        /**
         * SAML 2 authentication context class URIs
         */
        public static class AuthnContextClassURI {

            public static final String IP = "urn:oasis:names:tc:SAML:2.0:ac:classes:InternetProtocol";
            public static final String IP_PASSWORD = "urn:oasis:names:tc:SAML:2.0:ac:classes:InternetProtocolPassword";
            public static final String KERBEROS = "urn:oasis:names:tc:SAML:2.0:ac:classes:Kerberos";
            public static final String MOBILE_ONE_FACTOR_UNREGISTERED =
                    "urn:oasis:names:tc:SAML:2.0:ac:classes:MobileOneFactorUnregistered";
            public static final String MOBILE_TWO_FACTOR_UNREGISTERED =
                    "urn:oasis:names:tc:SAML:2.0:ac:classes:MobileTwoFactorUnregistered";
            public static final String MOBILE_ONE_FACTOR_CONTRACT =
                    "urn:oasis:names:tc:SAML:2.0:ac:classes:MobileOneFactorContract";
            public static final String MOBILE_TWO_FACTOR_CONTRACT =
                    "urn:oasis:names:tc:SAML:2.0:ac:classes:MobileTwoFactorContract";
            public static final String PASSWORD = "urn:oasis:names:tc:SAML:2.0:ac:classes:Password";
            public static final String PASSWORD_PROTECTED_TRANSPORT =
                    "urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport";
            public static final String PREVIOUS_SESSION = "urn:oasis:names:tc:SAML:2.0:ac:classes:PreviousSession";
            public static final String X509 = "urn:oasis:names:tc:SAML:2.0:ac:classes:X509";
            public static final String PGP = "urn:oasis:names:tc:SAML:2.0:ac:classes:PGP";
            public static final String SPKI = "urn:oasis:names:tc:SAML:2.0:ac:classes:SPKI";
            public static final String XML_DSIG = "urn:oasis:names:tc:SAML:2.0:ac:classes:XMLDSig";
            public static final String SMARTCARD = "urn:oasis:names:tc:SAML:2.0:ac:classes:Smartcard";
            public static final String SMARTCARD_PKI = "urn:oasis:names:tc:SAML:2.0:ac:classes:SmartcardPKI";
            public static final String SOFTWARE_PKI = "urn:oasis:names:tc:SAML:2.0:ac:classes:SoftwarePKI";
            public static final String TELEPHONY = "urn:oasis:names:tc:SAML:2.0:ac:classes:Telephony";
            public static final String NOMAD_TELEPHONY = "urn:oasis:names:tc:SAML:2.0:ac:classes:NomadTelephony";
            public static final String PERSONAL_TELEPHONY = "urn:oasis:names:tc:SAML:2.0:ac:classes:PersonalTelephony";
            public static final String AUTHENTICATED_TELEPHONY =
                    "urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony";
            public static final String SECURE_REMOTE_PASSWORD =
                    "urn:oasis:names:tc:SAML:2.0:ac:classes:SecureRemotePassword";
            public static final String TLS_CLIENT = "urn:oasis:names:tc:SAML:2.0:ac:classes:TLSClient";
            public static final String TIME_SYNC_TOKEN = "urn:oasis:names:tc:SAML:2.0:ac:classes:TimeSyncToken";
            public static final String UNSPECIFIED = "urn:oasis:names:tc:SAML:2.0:ac:classes:unspecified";
        }

        /**
         * SAML 2 authentication context comparison methods.
         */
        public static class AuthnContextComparison {

            public static final String EXACT = "Exact";
            public static final String MINIMUM = "Minimum";
            public static final String MAXIMUM = "Maximum";
            public static final String BETTER = "Better";
        }
    }

    /**
     * Passive STS constants.
     */
    public static class PassiveSTS {

        public static final String NAME = "passivests";
        public static final String PASSIVE_STS_REALM = "passiveSTSRealm";
        public static final String PASSIVE_STS_REPLY_URL = "passiveSTSWReply";
    }

    /**
     * XML constants.
     */
    public static class XML {

        /**
         * Signature algorithms.
         */
        public static class SignatureAlgorithm {

            public static final String DSA_SHA1 = "DSA with SHA1";
            public static final String ECDSA_SHA1 = "ECDSA with SHA1";
            public static final String ECDSA_SHA256 = "ECDSA with SHA256";
            public static final String ECDSA_SHA384 = "ECDSA with SHA384";
            public static final String ECDSA_SHA512 = "ECDSA with SHA512";
            public static final String RSA_MD5 = "RSA with MD5";
            public static final String RSA_RIPEMD160 = "RSA with RIPEMD160";
            public static final String RSA_SHA1 = "RSA with SHA1";
            public static final String RSA_SHA256 = "RSA with SHA256";
            public static final String RSA_SHA384 = "RSA with SHA384";
            public static final String RSA_SHA512 = "RSA with SHA512";
        }

        /**
         * Signature algorithm URIs.
         */
        public static class SignatureAlgorithmURI {

            public static final String DSA_SHA1 = "http://www.w3.org/2000/09/xmldsig#dsa-sha1";
            public static final String ECDSA_SHA1 = "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha1";
            public static final String ECDSA_SHA256 = "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256";
            public static final String ECDSA_SHA384 = "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha384";
            public static final String ECDSA_SHA512 = "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha512";
            public static final String RSA_MD5 = "http://www.w3.org/2001/04/xmldsig-more#rsa-md5";
            public static final String RSA_RIPEMD160 = "http://www.w3.org/2001/04/xmldsig-more#rsa-ripemd160";
            public static final String RSA_SHA1 = "http://www.w3.org/2000/09/xmldsig#rsa-sha1";
            public static final String RSA_SHA256 = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256";
            public static final String RSA_SHA384 = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha384";
            public static final String RSA_SHA512 = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha512";
        }

        /**
         * Digest algorithms.
         */
        public static class DigestAlgorithm {

            public static final String MD5 = "MD5";
            public static final String RIPEMD160 = "RIPEMD160";
            public static final String SHA1 = "SHA1";
            public static final String SHA256 = "SHA256";
            public static final String SHA384 = "SHA384";
            public static final String SHA512 = "SHA512";
        }

        /**
         * Digest algorithm URIs.
         */
        public static class DigestAlgorithmURI {

            public static final String MD5 = "http://www.w3.org/2001/04/xmldsig-more#md5";
            public static final String RIPEMD160 = "http://www.w3.org/2001/04/xmlenc#ripemd160";
            public static final String SHA1 = "http://www.w3.org/2000/09/xmldsig#sha1";
            public static final String SHA256 = "http://www.w3.org/2001/04/xmlenc#sha256";
            public static final String SHA384 = "http://www.w3.org/2001/04/xmldsig-more#sha384";
            public static final String SHA512 = "http://www.w3.org/2001/04/xmlenc#sha512";
        }

        /**
         * Assertion encryption algorithms.
         */
        public static class AssertionEncryptionAlgorithm {

            public static final String AES256 = "AES256";
            public static final String AES192 = "AES192";
            public static final String AES128 = "AES128";
            public static final String TRIPLEDES = "TRIPLEDES";
            public static final String AES128_GCM = "AES128_GCM";
            public static final String AES192_GCM = "AES192_GCM";
            public static final String AES256_GCM = "AES256_GCM";
        }

        /**
         * Assertion encryption algorithm URIs.
         */
        public static class AssertionEncryptionAlgorithmURI {

            public static final String AES256 = "http://www.w3.org/2001/04/xmlenc#aes256-cbc";
            public static final String AES192 = "http://www.w3.org/2001/04/xmlenc#aes192-cbc";
            public static final String AES128 = "http://www.w3.org/2001/04/xmlenc#aes128-cbc";
            public static final String TRIPLEDES = "http://www.w3.org/2001/04/xmlenc#tripledes-cbc";
            public static final String AES128_GCM = "http://www.w3.org/2009/xmlenc11#aes128-gcm";
            public static final String AES192_GCM = "http://www.w3.org/2009/xmlenc11#aes192-gcm";
            public static final String AES256_GCM = "http://www.w3.org/2009/xmlenc11#aes256-gcm";
        }

        /**
         * Key encryption algorithms.
         */
        public static class KeyEncryptionAlgorithm {

            public static final String RSAOAEP = "RSAOAEP";
            public static final String RSA15 = "RSA15";
            public static final String RSAOAEP11 = "RSAOAEP11";
        }

        /**
         * Key encryption algorithm URIs.
         */
        public static class KeyEncryptionAlgorithmURI {

            public static final String RSAOAEP = "http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p";
            public static final String RSA15 = "http://www.w3.org/2001/04/xmlenc#rsa-1_5";
            public static final String RSAOAEP11 = "http://www.w3.org/2009/xmlenc11#rsa-oaep";
        }

    }

    /**
     * SCIM constants.
     */
    public static class SCIM {

        public static final String USERS_EP_URL = "scimUserEndpoint";
        public static final String GROUPS_EP_URL = "scimGroupEndpoint";

        private SCIM() {

            throw new AssertionError("Must not initiate an object of SCIM class");
        }
    }

    /**
     * SCIM 2 constants.
     */
    public static class SCIM2 {

        public static final String USERS_EP_URL = "scim2UserEndpoint";
        public static final String GROUPS_EP_URL = "scim2GroupEndpoint";

        private SCIM2() {

            throw new AssertionError("Must not initiate an object of SCIM2 class");
        }
    }

    /**
     * Error message enums.
     * <p>
     * Error codes for server errors start with 65 and client errors start with 60.
     */
    public enum Error {

        UNEXPECTED_SERVER_ERROR("65001"),
        SORTING_NOT_IMPLEMENTED("65002"),
        ATTRIBUTE_FILTERING_NOT_IMPLEMENTED("65003"),

        INVALID_REQUEST("60001"),
        INVALID_OFFSET("60002"),
        INVALID_LIMIT("60003"),
        INVALID_FILTER("60004"),
        APPLICATION_NOT_DISCOVERABLE("60005"),
        APPLICATION_NOT_FOUND("60006"),
        APPLICATION_ALREADY_EXISTS("60007"),
        OPERATION_FORBIDDEN("60008"),
        INBOUND_KEY_ALREADY_EXISTS("60009");

        private final String code;
        private static final String APPLICATION_MANAGEMENT_ERROR_CODE_PREFIX = "APP-";

        Error(String code) {

            this.code = code;
        }

        public String getCode() {

            return APPLICATION_MANAGEMENT_ERROR_CODE_PREFIX + code;
        }
    }
}
