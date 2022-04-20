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

package org.wso2.carbon.identity.application.authentication.endpoint.util;

public class Constants {

    public static final String SESSION_DATA_KEY = "sessionDataKey";
    public static final String SESSION_DATA_KEY_CONSENT = "sessionDataKeyConsent";
    public static final String AUTH_FAILURE = "authFailure";
    public static final String ERROR_CODE = "errorCode";
    public static final String ERROR_MSG = "errorMsg";
    public static final String AUTH_FAILURE_MSG = "authFailureMsg";
    public static final String STATUS = "status";
    public static final String STATUS_MSG = "statusMsg";
    public static final String IDP_AUTHENTICATOR_MAP = "idpAuthenticatorMap";
    public static final String RESIDENT_IDP_RESERVED_NAME = "LOCAL";
    public static final String MISSING_CLAIMS = "missingClaims";
    public static final String REQUESTED_CLAIMS = "requestedClaims";
    public static final String MANDATORY_CLAIMS = "mandatoryClaims";
    public static final String USER_CLAIMS_CONSENT_ONLY = "userClaimsConsentOnly";
    public static final String CLAIM_SEPARATOR = ",";
    public static final String REQUEST_PARAM_SP = "sp";
    // Response Messages
    public static final String ACCOUNT_RESEND_SUCCESS_RESOURCE = "account.resend.email.success";
    public static final String ACCOUNT_RESEND_FAIL_RESOURCE = "account.resend.email.fail";
    public static final String CONFIGURATION_ERROR = "configuration.error";
    public static final String AUTHENTICATION_MECHANISM_NOT_CONFIGURED = "authentication.mechanism.not.configured";
    public static final String ERROR_WHILE_BUILDING_THE_ACCOUNT_RECOVERY_ENDPOINT_URL =
            "Error.while.building.the.account.recovery.endpoint.url";
    // WebAPP Configurations
    public static final String ACCOUNT_RECOVERY_REST_ENDPOINT_URL = "AccountRecoveryRESTEndpointURL";
    public static final String ENABLE_AUTHENTICATION_WITH_REST_API = "EnableAuthenticationWithAuthenticationRESTAPI";
    public static final String AUTHENTICATION_REST_ENDPOINT_URL = "AuthenticationRESTEndpointURL";


    public static final String HTTPS_URL = "https://";
    public static final String HOST = "identity.server.host";
    public static final String COLON = ":";
    public static final String PORT = "identity.server.port";
    public static final String SERVICES_URL = "identity.server.serviceURL";
    public static final String DASHBOARD_RELYING_PARTY = "wso2.my.dashboard";
    public static final String CONFIG_APP_NAME = "app.name";
    public static final String CONFIG_APP_PASSWORD = "app.password";
    public static final String CONFIG_SERVER_ORIGIN = "identity.server.origin";

    private Constants() {

    }

    public static class SAML2SSO {
        public static final String ASSERTION_CONSUMER_URL = "assertnConsumerURL";
        public static final String RELAY_STATE = "RelayState";
        public static final String SAML_RESP = "SAMLResponse";

        private SAML2SSO() {

        }
    }

    public static class TenantConstants {

        // Tenant list dropdown related properties

        public static final String USERNAME = "mutual.ssl.username";
        public static final String USERNAME_HEADER = "username.header";
        public static final String CLIENT_KEY_STORE = "client.keyStore";
        public static final String CLIENT_TRUST_STORE = "client.trustStore";
        public static final String CLIENT_KEY_STORE_PASSWORD = "Carbon.Security.KeyStore.Password";
        public static final String CLIENT_TRUST_STORE_PASSWORD = "Carbon.Security.TrustStore.Password";
        public static final String HOSTNAME_VERIFICATION_ENABLED = "hostname.verification.enabled";
        public static final String KEY_MANAGER_TYPE = "key.manager.type";
        public static final String TRUST_MANAGER_TYPE = "trust.manager.type";
        public static final String TENANT_LIST_ENABLED = "tenantListEnabled";
        public static final String MUTUAL_SSL_MANAGER_ENABLED = "mutualSSLManagerEnabled";
        public static final String TLS_PROTOCOL = "tls.protocol";

        // Service URL constants
        public static final String TENANT_MGT_ADMIN_SERVICE_URL = "/TenantMgtAdminService/retrieveTenants";

        // String constants for SOAP response processing
        public static final String RETURN = "return";
        public static final String RETRIEVE_TENANTS_RESPONSE = "retrieveTenantsResponse";
        public static final String TENANT_DOMAIN = "tenantDomain";
        public static final String ACTIVE = "active";
        public static final String TENANT_DATA_SEPARATOR = ",";
        public static final String RELATIVE_PATH_START_CHAR = ".";
        public static final String CHARACTER_ENCODING = "UTF-8";
        public static final String CONFIG_RELATIVE_PATH = "./repository/conf/identity/EndpointConfig.properties";
        public static final String IDENTITY_XML_RELATIVE_PATH = "./repository/conf/identity/identity.xml";
        public static final String CONFIG_FILE_NAME = "EndpointConfig.properties";

        private TenantConstants() {

        }
    }

    public static class UserRegistrationConstants {

        public static final String WSO2_DIALECT = "http://wso2.org/claims";
        public static final String FIRST_NAME = "First Name";
        public static final String LAST_NAME = "Last Name";
        public static final String EMAIL_ADDRESS = "Email";
        public static final String USER_REGISTRATION_SERVICE = "/UserRegistrationAdminService" +
                ".UserRegistrationAdminServiceHttpsSoap11Endpoint/";

        private UserRegistrationConstants() {

        }
    }

    public static class ErrorToi18nMappingConstants {

        public static final String INVALID_CALLBACK_CALLBACK_NOT_MATCH = "invalid_callback_callback.not.match";
        public static final String INVALID_CALLBACK_CALLBACK_NOT_MATCH_I18N_KEY = "callback.not.match";
        public static final String INVALID_CLIENT_APP_NOT_FOUND = "invalid_client_application.not.found";
        public static final String INVALID_CLIENT_APP_NOT_FOUND_I18N_KEY = "application.not.found";
        public static final String INVALID_REQUEST_INVALID_REDIRECT_URI = "invalid_request_invalid.redirect.uri";
        public static final String INVALID_REQUEST_INVALID_REDIRECT_URI_I18N_KEY = "invalid.redirect.uri";
        public static final String AUTHENTICATION_ATTEMPT_FAILED_AUTHORIZATION_FAILED =
                "authentication.attempt.failed_authorization.failed";
        public static final String AUTHENTICATION_ATTEMPT_FAILED_CLAIM_REQUEST_MISSING =
                "authentication.attempt.failed_claim.request.missing";
        public static final String AUTHENTICATION_ATTEMPT_FAILED_JIT_PROVISIONING_VERIFY_USERNAME_FAILED =
                "authentication.attempt.failed_jit.provisioning.verify.username.existence.failed";
        public static final String AUTHENTICATION_ATTEMPT_FAILED_AUTHORIZATION_FAILED_I18N_KEY =
                "authentication.attempt.failed";
        public static final String MISCONFIGURATION_ERROR_SOMETHING_WENT_WRONG_CONTACT_ADMIN =
                "misconfiguration.error_something.went.wrong.contact.admin";
        public static final String MISCONFIGURATION_ERROR_SOMETHING_WENT_WRONG_CONTACT_ADMIN_I18N_KEY =
                "misconfiguration.error";
        public static final String AUTHENTICATION_ATTEMPT_FAILED_USERNAME_EXISTS =
                "authentication.attempt.failed_username.already.exists.error";
        public static final String USERNAME_EXISTS_ERROR_I19N_KEY = "username.already.exists.error";
        public static final String AUTHENTICATION_ATTEMPT_FAILED_USER_STORE_DOMAIN_ERROR =
                "authentication.attempt.failed_user.store.domain.error";
        public static final String USER_STORE_DOMAIN_ERROR_I18N_KEY = "user.store.domain.error";
        public static final String AUTHENTICATION_ATTEMPT_FAILED_ERROR_INVALID_USER_STORE_DOMAIN =
                "authentication.attempt.failed_invalid.user.store.domain";
        public static final String ERROR_INVALID_USER_STORE_DOMAIN_I18N_KEY = "invalid.user.store.domain";
        public static final String AUTHENTICATION_ATTEMPT_FAILED_USER_STORE_MAN_ERROR =
                "authentication.attempt.failed_user.store.manager.error";
        public static final String USER_STORE_MAN_ERROR_I18N_KEY = "user.store.manager.error";
        public static final String AUTHENTICATION_ATTEMPT_FAILED_INVALID_USER_STORE =
                "authentication.attempt.failed_invalid.user.store";
        public static final String INVALID_USER_STORE_I18N_KEY = "invalid.user.store";
        public static final String AUTHENTICATION_ATTEMPT_FAILED_USERNAME_EXISTENCE_ERROR =
                "authentication.attempt.failed_username.existence.error";
        public static final String USERNAME_EXISTENCE_ERROR_I18N_KEY = "username.existence.error";
        public static final String AUTHENTICATION_ATTEMPT_FAILED_CLAIM_MAP_HANDLING_ERROR =
                "authentication.attempt.failed_claim.mapping.handling.error";
        public static final String CLAIM_MAP_HANDLING_ERROR_I18N_KEY = "claim.mapping.handling.error";
        public static final String AUTHENTICATION_ATTEMPT_FAILED_RESIDENT_IDP_NULL_ERROR =
                "authentication.attempt.failed_resident.idp.null.for.tenant.error";
        public static final String RESIDENT_IDP_NULL_ERROR_I18N_KEY = "resident.idp.null.for.tenant.error";
        public static final String AUTHENTICATION_ATTEMPT_FAILED_CLAIM_MAP_GET_ERROR =
                "authentication.attempt.failed_claim.mapping.getting.error";
        public static final String CLAIM_MAP_GET_ERROR_I18N_KEY = "claim.mapping.getting.error";
        public static final String AUTHENTICATION_ATTEMPT_FAILED_ASSOCIATED_LOCAL_USER_ID_ERROR =
                "authentication.attempt.failed_associated.local.user.id.error";
        public static final String ASSOCIATED_LOCAL_USER_ID_ERROR_I18N_KEY = "associated.local.user.id.error";
        public static final String AUTHENTICATION_ATTEMPT_FAILED_IDP_ERROR_FOR_TENANT =
                "authentication.attempt.failed_idp.for.tenant.in.post.auth.error";
        public static final String IDP_ERROR_FOR_TENANT_I18N_KEY = "idp.for.tenant.in.post.auth.error";
        public static final String AUTHENTICATION_ATTEMPT_FAILED_REALM_ERROR_FOR_TENANT =
                "authentication.attempt.failed_realm.for.tenant.in.post.auth.error";
        public static final String REALM_ERROR_FOR_TENANT_I18N_KEY = "realm.for.tenant.in.post.auth.error";
        public static final String AUTHENTICATION_ATTEMPT_FAILED_CLAIM_ERROR_PASSWORD_PROVISION =
                "authentication.attempt.failed_claim.error.while.password.provision";
        public static final String CLAIM_ERROR_PASSWORD_PROVISION_I18N_KEY = "claim.error.while.password.provision";
        public static final String AUTHENTICATION_ATTEMPT_FAILED_USERNAME_FOR_ASSOCIATED_IDP_ERROR =
                "authentication.attempt.failed_username.associated.with.idp.error";
        public static final String USERNAME_FOR_ASSOCIATED_IDP_ERROR_I18N_KEY = "username.associated.with.idp.error";
        public static final String AUTHENTICATION_ATTEMPT_FAILED_SIGNUP_EP_ERROR_FOR_PROVISION =
                "authentication.attempt.failed_sign.up.end.point.for.provision.error";
        public static final String SIGNUP_EP_ERROR_FOR_PROVISION_I18N_KEY = "sign.up.end.point.for.provision.error";
        public static final String AUTHENTICATION_ATTEMPT_FAILED_CONSENT_ADD_FOR_TENANT_ERROR =
                "authentication.attempt.failed_add.consent.for.tenant.error";
        public static final String CONSENT_ADD_FOR_TENANT_ERROR_I18N_KEY = "add.consent.for.tenant.error";
        public static final String AUTHENTICATION_ATTEMPT_FAILED_SET_IDP_FOR_TENANT_ERROR =
                "authentication.attempt.failed_set.idp.data.for.tenant.error";
        public static final String SET_IDP_FOR_TENANT_ERROR_I18N_KEY = "set.idp.data.for.tenant.error";
        public static final String AUTHENTICATION_ATTEMPT_FAILED_GET_CONSENT_FOR_USER_ERROR =
                "authentication.attempt.failed_consent.retrieve.for.user.error";
        public static final String GET_CONSENT_FOR_USER_ERROR_I18N_KEY = "consent.retrieve.for.user.error";
        public static final String AUTHENTICATION_ATTEMPT_FAILED_CONSENT_DISABLED_FOR_SSO_ERROR =
                "authentication.attempt.failed_consent.disabled.for.sso.error";
        public static final String CONSENT_DISABLED_FOR_SSO_ERROR_I18N_KEY = "consent.disabled.for.sso.error";
        public static final String AUTHENTICATION_ATTEMPT_FAILED_INPUT_CONSENT_FOR_USER_ERROR =
                "authentication.attempt.failed_consent.input.for.user.error";
        public static final String INPUT_CONSENT_FOR_USER_ERROR_I18N_KEY = "consent.input.for.user.error";
        public static final String AUTHENTICATION_ATTEMPT_FAILED_USER_DENIED_CONSENT_ERROR =
                "authentication.attempt.failed_user.denied.consent.error";
        public static final String USER_DENIED_CONSENT_ERROR_I18N_KEY = "user.denied.consent.error";
        public static final String AUTHENTICATION_ATTEMPT_FAILED_USER_DENIED_MANDATORY_CONSENT_ERROR =
                "authentication.attempt.failed_user.denied.mandatory.consent.error";
        public static final String USER_DENIED_MANDATORY_CONSENT_ERROR_I18N_KEY = "user.denied.mandatory.consent.error";
        public static final String AUTHENTICATION_ATTEMPT_FAILED_CONSENT_PAGE_ERROR =
                "authentication.attempt.failed_consent.page.error";
        public static final String CONSENT_PAGE_ERROR_I18N_KEY = "consent.page.error";
        public static final String AUTHENTICATION_ATTEMPT_FAILED_APPLICATION_CONFIG_NULL_ERROR =
                "authentication.attempt.failed_application.configs.null.error";
        public static final String APPLICATION_CONFIG_NULL_ERROR_I18N_KEY = "application.configs.null.error";
        public static final String AUTHENTICATION_ATTEMPT_FAILED_REQUEST_CLAIMS_PAGE_ERROR =
                "authentication.attempt.failed_request.claims.page.error";
        public static final String REQUEST_CLAIMS_PAGE_ERROR_I18N_KEY = "request.claims.page.error";
        public static final String AUTHENTICATION_ATTEMPT_FAILED_REQUEST_CLAIMS_PAGE_URI_ERROR =
                "authentication.attempt.failed_request.claims.page.uri.build.error";
        public static final String REQUEST_CLAIMS_PAGE_URI_ERROR_I18N_KEY = "request.claims.page.uri.build.error";
        public static final String AUTHENTICATION_ATTEMPT_FAILED_RETRIEVE_CLAIM_ERROR =
                "authentication.attempt.failed_retrieve.claim.error";
        public static final String RETRIEVE_CLAIM_ERROR_I18N_KEY = "retrieve.claim.error";
        public static final String AUTHENTICATION_ATTEMPT_FAILED_GET_USER_ASSOCIATION_ERROR =
                "authentication.attempt.failed_get.association.for.user.error";
        public static final String GET_USER_ASSOCIATION_ERROR_I18N_KEY = "get.association.for.user.error";
        public static final String AUTHENTICATION_ATTEMPT_FAILED_UPDATE_LOCAL_USER_CLAIMS_ERROR =
                "authentication.attempt.failed_update.local.user.claims.error";
        public static final String UPDATE_LOCAL_USER_CLAIMS_ERROR_I18N_KEY = "update.local.user.claims.error";
        public static final String AUTHENTICATION_ATTEMPT_FAILED_RETRIEVING_REALM_TO_HANDLE_CLAIMS_ERROR =
                "authentication.attempt.failed_retrieving.realm.to.handle.claims.error";
        public static final String RETRIEVING_REALM_TO_HANDLE_CLAIMS_ERROR_I18N_KEY =
                "retrieving.realm.to.handle.claims.error";
        public static final String AUTHENTICATION_ATTEMPT_FAILED_POST_AUTH_COOKIE_NOT_FOUND =
                "authentication.attempt.failed_post.auth.cookie.not.found";
        public static final String POST_AUTH_COOKIE_NOT_FOUND_I18N_KEY = "post.auth.cookie.not.found";
        public static final String INCORRECT_ERROR_MAPPING_KEY = "incorrect.error.mapping";
        public static final String SUSPICIOUS_AUTHENTICATION_ATTEMPTS_SUSPICIOUS_AUTHENTICATION_ATTEMPTS_DESCRIPTION = "suspicious.authentication.attempts_suspicious.authentication.attempts.description";
        public static final String SUSPICIOUS_AUTHENTICATION_ATTEMPTS_SUSPICIOUS_AUTHENTICATION_ATTEMPTS_DESCRIPTION_I18N_KEY = "suspicious.authentication.attempts";

        private ErrorToi18nMappingConstants() {

        }
    }
}
