/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.identity.core;

import org.wso2.carbon.core.RegistryResources;

public class IdentityRegistryResources {

    public final static String IDENTITY_ROOT = "identity/";
    public final static String IDENTITY_PATH = RegistryResources.ROOT + IDENTITY_ROOT;

    public final static String USER_CERT = IDENTITY_PATH + "UserPersonalCert/";
    public final static String USER_TRUSTED_RP = IDENTITY_PATH + "UserTrustedRP/";
    public final static String GLOABALLY_TRUSTED_RP = IDENTITY_PATH + "GloballyTrustedRP/";
    public final static String RELYING_PARTY = IDENTITY_PATH + "RelyingParty/";
    public final static String ISSUED_TOKENS = IDENTITY_PATH + "IssuedTokens/";
    public final static String REGISTERED_IC = IDENTITY_PATH + "RegisteredInfoCard/";
    public final static String REMOVED_REGISTERED_IC = IDENTITY_PATH + "RemovedRegisteredInfoCard/";
    public final static String REPORT_ACTIONS = IDENTITY_PATH + "ReportActions/";
    public final static String DIALECT = IDENTITY_PATH + "Dialect/";
    public final static String CLAIM = IDENTITY_PATH + "Claim/";
    public final static String PPID_INFO = IDENTITY_PATH + "PPIDInfo/";
    public final static String REVOKED_IC = IDENTITY_PATH + "RevokedInfoCard/";
    public final static String OPENID_USER_RP = IDENTITY_PATH + "OpenIdUserRP/";
    public final static String NAME_VALUE_PAIR = IDENTITY_PATH + "NameValuePair/";
    public final static String CARD_ISSUER = IDENTITY_PATH + "CardIssuer";
    public final static String ENTITLEMENT = IDENTITY_PATH + "Entitlement/Policies/";
    public final static String ENTITLEMENT_POLICY_RESOURCES = IDENTITY_PATH + "Entitlement/resources/";

    public final static String OPENID_USER_RP_ROOT = IDENTITY_PATH + "OpenIdUserRP/";
    public final static String USER_TRUSTED_RP_ROOT = IDENTITY_PATH + "UserTrustedRP/";
    public final static String PPID_ROOT = IDENTITY_PATH + "PPIDValue/";
    public final static String INFOCARD_ROOT = IDENTITY_PATH + "InfoCard/";
    public final static String XMPP_SETTINGS_ROOT = IDENTITY_PATH + "XmppSettings/";
    public final static String OPENID_SIGN_UP = IDENTITY_PATH + "OpenIDSignUp/";
    public final static String SAML_SSO_SERVICE_PROVIDERS = IDENTITY_PATH + "SAMLSSO/";
    public final static String OPEN_ID_ADMIN_SETTINGS = IDENTITY_PATH + "OpenIDSettings/";
    public final static String OAUTH_CONSUMER_PATH = IDENTITY_PATH + "OAuthConsumer/";
    public final static String OAUTH_APP_PATH = IDENTITY_PATH + "OAuthApp/";
    public final static String OAUTH_APP_CALLBACK = "CallbackUrl";
    public final static String OAUTH_APP_CONSUMER_KEY = "ConsumerKey";
    public final static String OAUTH_APP_CONSUMER_SECRET = "ConsumerSecret";
    public final static String OAUTH_APP_NAME = "OAuthAppName";
    public final static String SAML_SSO_GEN_KEY = IDENTITY_PATH + "generated-key";

    // common
    public final static String PROP_USER_ID = "UserID";

    // claim
    public final static String PROP_CLAIM_URI = "ClaimURI";
    public final static String PROP_CLAIM_DESCRIPTION = "Description";
    public final static String PROP_CLAIM_SIMPLE = "Simple";
    public final static String PROP_CLAIM_USER_EDITABLE = "UserEditable";
    public final static String PROP_CLAIM_ATTR_ID = "AttrId";
    public final static String PROP_CLAIM_SUPPORTED = "Supported";
    public final static String PROP_CLAIM_REQUIRED = "Required";
    public final static String PROP_CLAIM_OPENID_TAG = "OpenIdTag";
    public final static String PROP_CLAIM_DISPLAY_TAG = "DisplayTag";

    // dialect
    public final static String PROP_DIALECT_URI = "DialectURI";
    public final static String PROP_DIALECT_INFO = "DialectInfo";

    // information card
    public final static String PROP_IC_CARD_ID = "InfoCardID";
    public final static String PROP_IC_USER_ID = "UserID";
    public final static String PROP_IC_DATE_EXPIRES = "DateExpires";
    public final static String PROP_IC_DATE_ISSUED = "DateIssued";
    public final static String PROP_IC_IS_OPENID = "IsOpenID";
    public final static String PROP_IC_TOKENS = "Tokens";

    // user trusted relying party
    public final static String PROP_HOST_NAME = "HostName";
    public final static String PROP_PPID = "PPID";

    // relying party
    public final static String PROP_ALIAS = "Alias";

    public final static String PROP_RP_URL = "RPUrl";
    public final static String PROP_IS_TRUSTED_ALWAYS = "IsTrustedAlways";
    public final static String PROP_VISIT_COUNT = "VisitCount";
    public final static String PROP_LAST_VISIT = "LastVisit";
    public final static String PROP_DEFAULT_PROFILE_NAME = "DefaultProfileName";

    // associations
    public final static String ASSOCIATION_CLAIM_DIALECT = "identity.claim.dialect";
    public final static String ASSOCIATION_USER_TRUSTED_RP = "identity.user.usertrustedrp";
    public final static String ASSOCIATION_PPID_RP = "identity.ppid.rp";
    public final static String ASSOCIATION_USER_INFOCARD = "identity.user.infocard";
    public final static String ASSOCIATION_USER_OPENID_RP = "identity.user.openid.rp";
    public final static String ASSOCIATION_USER_PPID = "identity.user.ppid";
    public final static String ASSOCIATION_USER_XMPP_SETTINGS = "identity.user.xmppSettings";
    public final static String ASSOCIATION_USER_OPENID = "identity.user.openidAssociation";
    public final static String ASSOCIATION_USER_OAUTH_APP = "identity.user.oauth.app";

    // XMPP Settings
    public final static String XMPP_SERVER = "XmppServer";
    public final static String XMPP_USERNAME = "XmppUserName";
    public final static String XMPP_USERCODE = "XmppUserCode";
    public final static String XMPP_ENABLED = "Enabled";
    public final static String XMPP_PIN_ENABLED = "PINEnabled";

    // OpenId Sign-Up

    public final static String PROP_OPENID_SIGN_UP_USERID = "UserID";
    public final static String PROP_OPENID = "OpenID";

    //SAML SSO
    public final static String PROP_SAML_SSO_ISSUER = "Issuer";
    public final static String PROP_SAML_SSO_ASSERTION_CONS_URL = "SAMLSSOAssertionConsumerURL";
    public final static String PROP_SAML_SSO_ASSERTION_CONS_URLS = "SAMLSSOAssertionConsumerURLs";
    public final static String PROP_DEFAULT_SAML_SSO_ASSERTION_CONS_URL = "DefaultSAMLSSOAssertionConsumerURL";
    public final static String PROP_SAML_SSO_ISSUER_CERT_ALIAS = "IssuerCertAlias";
    public final static String PROP_SAML_SSO_USE_FULLY_QUALIFIED_USERNAME_AS_SUBJECT = "useFullyQualifiedUsername";
    public final static String PROP_SAML_SSO_DO_SINGLE_LOGOUT = "doSingleLogout";
    public final static String PROP_SAML_SLO_RESPONSE_URL = "sloResponseURL";
    public final static String PROP_SAML_SLO_REQUEST_URL = "sloRequestURL";
    public final static String PROP_SAML_SSO_LOGIN_PAGE_URL = "loginPageURL";
    public final static String PROP_SAML_SSO_DO_SIGN_RESPONSE = "doSignResponse";
    public final static String PROP_SAML_SSO_DO_SIGN_ASSERTIONS = "doSignAssertions";
    public static final String PROP_SAML_SSO_PUB_KEY_FILE_PATH = "pub-Key-file-path";
    public final static String PROP_SAML_SSO_GEN_KEY_PASS = "private-key-password";
    public static final String PROP_SAML_SSO_ATTRIB_CONSUMING_SERVICE_INDEX = "AttributeConsumingServiceIndex";
    public static final String PROP_SAML_SSO_REQUESTED_CLAIMS = "RequestedClaims";
    public static final String PROP_SAML_SSO_REQUESTED_AUDIENCES = "RequestedAudiences";
    public static final String PROP_SAML_SSO_REQUESTED_RECIPIENTS = "RequestedRecipients";
    public static final String PROP_SAML_SSO_ENABLE_ATTRIBUTES_BY_DEFAULT = "EnableAttributesByDefault";
    public static final String PROP_SAML_SSO_ENABLE_NAMEID_CLAIMURI = "EnableNameIDClaimUri";
    public static final String PROP_SAML_SSO_NAMEID_CLAIMURI = "NameIDClaimUri";
    public static final String PROP_SAML_SSO_NAMEID_FORMAT = "NameIDFormat";
    public static final String PROP_SAML_SSO_IDP_INIT_SSO_ENABLED = "IdPInitSSOEnabled";
    public static final String PROP_SAML_SLO_IDP_INIT_SLO_ENABLED = "IdPInitSLOEnabled";
    public static final String PROP_SAML_IDP_INIT_SLO_RETURN_URLS = "IdPInitiatedSLOReturnToURLs";
    public static final String PROP_SAML_SSO_ENABLE_ENCRYPTED_ASSERTION = "doEnableEncryptedAssertion";
    public static final String PROP_SAML_SSO_VALIDATE_SIGNATURE_IN_REQUESTS = "doValidateSignatureInRequests";
    public static final String PROP_SAML_SSO_SIGNING_ALGORITHM = "signingAlgorithm";
    public static final String PROP_SAML_SSO_DIGEST_ALGORITHM = "digestAlgorithm";

    // OpenID Admin
    public final static String SUB_DOMAIN = "SubDomain";
    public final static String OPENID_PATTERN = "OpenIDPattern";

    // registry identifiers
    public static final String CONFIG_REGISTRY_IDENTIFIER = "conf";
    public static final String GOVERNANCE_REGISTRY_IDENTIFIER = "gov";

    //IDP metadata
    public static final String IDENTITY = "repository/identity/";
    public static final String IDENTITYPROVIDER = IDENTITY+ "provider/";
    public static final String SAMLIDP = IDENTITYPROVIDER + "saml/";

}
