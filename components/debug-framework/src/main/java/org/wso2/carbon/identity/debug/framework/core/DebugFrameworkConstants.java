/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.debug.framework.core;

/**
 * Constants for the Debug Framework.
 * Provides centralized definition of all string constants used throughout the framework
 * to ensure consistency and reduce duplication.
 */
public final class DebugFrameworkConstants {

    private DebugFrameworkConstants() {
    }

    // Debug Flow Context Constants.
    public static final String DEBUG_REQUEST_TYPE = "DFDP_DEBUG";
    public static final String DEBUG_FLOW_PROPERTY = "isDebugFlow";
    public static final String DEBUG_SESSION_PROPERTY = "DEBUG_SESSION";
    public static final String DEBUG_SERVICE_PROVIDER_NAME = "DFDP_DEBUG_SP";

    // Debug Context Property Keys.
    public static final String DEBUG_SESSION_ID = "DEBUG_SESSION_ID";
    public static final String DEBUG_TIMESTAMP = "DEBUG_TIMESTAMP";
    public static final String DEBUG_AUTHENTICATOR_NAME = "DEBUG_AUTHENTICATOR_NAME";
    public static final String DEBUG_IDP_NAME = "DEBUG_IDP_NAME";
    public static final String DEBUG_IDP_RESOURCE_ID = "DEBUG_IDP_RESOURCE_ID";
    public static final String DEBUG_IDP_DESCRIPTION = "DEBUG_IDP_DESCRIPTION";
    public static final String DEBUG_REQUEST_URI = "DEBUG_REQUEST_URI";
    public static final String DEBUG_REMOTE_ADDR = "DEBUG_REMOTE_ADDR";
    public static final String DEBUG_USER_AGENT = "DEBUG_USER_AGENT";

    // OAuth2 Configuration Properties.
    public static final String DEBUG_CLIENT_ID = "DEBUG_CLIENT_ID";
    public static final String DEBUG_CLIENT_SECRET = "DEBUG_CLIENT_SECRET";
    public static final String DEBUG_AUTHZ_ENDPOINT = "DEBUG_AUTHZ_ENDPOINT";
    public static final String DEBUG_TOKEN_ENDPOINT = "DEBUG_TOKEN_ENDPOINT";
    public static final String DEBUG_USERINFO_ENDPOINT = "DEBUG_USERINFO_ENDPOINT";
    public static final String DEBUG_IDP_SCOPE = "DEBUG_IDP_SCOPE";

    // Custom OAuth2 Parameters.
    public static final String CUSTOM_REDIRECT_URI = "CUSTOM_REDIRECT_URI";
    public static final String CUSTOM_SCOPE = "CUSTOM_SCOPE";
    public static final String CUSTOM_ACCESS_TYPE = "DEBUG_CUSTOM_access_type";
    public static final String DEBUG_USERNAME = "DEBUG_USERNAME";
    public static final String ADDITIONAL_OAUTH_PARAMS = "ADDITIONAL_OAUTH_PARAMS";

    // PKCE Parameters.
    public static final String DEBUG_CODE_VERIFIER = "DEBUG_CODE_VERIFIER";
    public static final String DEBUG_STATE = "DEBUG_STATE";
    public static final String DEBUG_EXTERNAL_REDIRECT_URL = "DEBUG_EXTERNAL_REDIRECT_URL";

    // Step Status Properties.
    public static final String STEP_CONNECTION_STATUS = "step_connection_status";
    public static final String STEP_AUTHENTICATION_STATUS = "step_authentication_status";
    public static final String STEP_CLAIM_MAPPING_STATUS = "step_claim_mapping_status";
    public static final String DEBUG_STEP_AUTH_URL_GENERATED = "DEBUG_STEP_AUTH_URL_GENERATED";
    public static final String DEBUG_STEP_AUTH_URL = "DEBUG_STEP_AUTH_URL";
    public static final String DEBUG_STEP_AUTH_URL_TIMESTAMP = "DEBUG_STEP_AUTH_URL_TIMESTAMP";
    public static final String DEBUG_STEP_CONTEXT_CREATION_STARTED = "DEBUG_STEP_CONTEXT_CREATION_STARTED";
    public static final String DEBUG_STEP_CALLBACK_URL_BUILT = "DEBUG_STEP_CALLBACK_URL_BUILT";
    public static final String DEBUG_STEP_CALLBACK_URL = "DEBUG_STEP_CALLBACK_URL";
    public static final String DEBUG_STEP_CALLBACK_URL_FALLBACK_USED = "DEBUG_STEP_CALLBACK_URL_FALLBACK_USED";

    // Debug Flow Identification Constants.
    public static final String DEBUG_IDENTIFIER_PARAM = "isDebugFlow";
    public static final String SESSION_DATA_KEY_PARAM = "sessionDataKey";
    public static final String DEBUG_FLOW_CALLBACK_PATH = "/commonauth";
    public static final String DEBUG_CONTEXT_ID = "DEBUG_CONTEXT_ID";
    
    // Request Completion Flag - Signals to CommonAuthenticationHandler that debug flow handled the request.
    public static final String DEBUG_REQUEST_HANDLED = "debugRequestHandled";

    // IDP Configuration Property Keys.
    public static final String IDP_CONFIG = "IDP_CONFIG";

    // OAuth2 Callback Parameters.
    public static final String OAUTH2_CODE_PARAM = "code";
    public static final String OAUTH2_STATE_PARAM = "state";
    public static final String OAUTH2_ERROR_PARAM = "error";
    public static final String OAUTH2_ERROR_DESCRIPTION_PARAM = "error_description";

    // OAuth2 Request Parameters.
    public static final String OAUTH2_RESPONSE_TYPE = "response_type";
    public static final String OAUTH2_RESPONSE_TYPE_CODE = "code";
    public static final String OAUTH2_CLIENT_ID_PARAM = "client_id";
    public static final String OAUTH2_REDIRECT_URI_PARAM = "redirect_uri";
    public static final String OAUTH2_SCOPE_PARAM = "scope";
    public static final String OAUTH2_STATE_PARAM_NAME = "state";
    public static final String OAUTH2_CODE_CHALLENGE_PARAM = "code_challenge";
    public static final String OAUTH2_CODE_CHALLENGE_METHOD = "code_challenge_method";
    public static final String OAUTH2_CODE_CHALLENGE_METHOD_S256 = "S256";
    public static final String OAUTH2_GRANT_TYPE = "grant_type";
    public static final String OAUTH2_GRANT_TYPE_AUTH_CODE = "authorization_code";

    // Default Constants.
    public static final String DEFAULT_TENANT_DOMAIN = "carbon.super";
    public static final int CACHE_EXPIRY_MINUTES = 15;
    public static final int PKCE_CODE_VERIFIER_LENGTH = 32;

    // Authenticator Constants.
    public static final String OIDC_AUTHENTICATOR_NAME = "OpenIDConnectAuthenticator";
    public static final String OAUTH2_OIDC_AUTHENTICATOR_NAME = "OAuth2OpenIDConnectAuthenticator";
    public static final String GOOGLE_OIDC_AUTHENTICATOR_NAME = "GoogleOIDCAuthenticator";
    public static final String GITHUB_AUTHENTICATOR_NAME = "GithubAuthenticator";
    public static final String GITHUB_AUTHENTICATOR_NAME_ALT = "GitHubAuthenticator";

    // Authenticator Configuration Properties.
    public static final String CLIENT_ID_PROP = "ClientId";
    public static final String CLIENT_ID_ALT_PROP = "client_id";
    public static final String CLIENT_ID_ALT_PROP2 = "OAuth2ClientId";
    public static final String CLIENT_SECRET_PROP = "ClientSecret";
    public static final String CLIENT_SECRET_ALT_PROP = "client_secret";
    public static final String AUTHZ_ENDPOINT_PROP = "OAuth2AuthzEPUrl";
    public static final String AUTHZ_ENDPOINT_ALT_PROP = "AuthzEndpoint";
    public static final String AUTHZ_ENDPOINT_ALT_PROP2 = "authorization_endpoint";
    public static final String TOKEN_ENDPOINT_PROP = "OAuth2TokenEPUrl";
    public static final String TOKEN_ENDPOINT_ALT_PROP = "TokenEndpoint";
    public static final String TOKEN_ENDPOINT_ALT_PROP2 = "token_endpoint";
    public static final String USERINFO_ENDPOINT_PROP = "UserInfoEndpoint";
    public static final String USERINFO_ENDPOINT_ALT_PROP = "userinfo_endpoint";
    public static final String SCOPE_PROP = "Scope";
    public static final String SCOPES_PROP = "Scopes";
    public static final String SCOPE_ALT_PROP = "scope";
    public static final String SCOPE_ALT_PROP2 = "requestedScope";
    public static final String ADDITIONAL_QUERY_PARAMS_PROP = "AdditionalQueryParameters";

    // Status Constants.
    public static final String STATUS_STARTED = "started";
    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_FAILED = "failed";
    public static final String STATUS_PENDING = "pending";

    // Encoding Constants.
    public static final String UTF_8 = "UTF-8";
    public static final String SHA_256 = "SHA-256";

    // Reflection-based Executor Method Names.
    public static final String EXECUTOR_METHOD_GET_CLIENT_ID = "getClientId";
    public static final String EXECUTOR_METHOD_GET_AUTHZ_ENDPOINT = "getAuthorizationServerEndpoint";
    public static final String EXECUTOR_METHOD_GET_AUTHZ_ENDPOINT_ALT = "getAuthorizationEndpoint";
    public static final String EXECUTOR_METHOD_GET_TOKEN_ENDPOINT = "getTokenEndpoint";
    public static final String EXECUTOR_METHOD_GET_USERINFO_ENDPOINT = "getUserInfoEndpoint";

    // Error Messages.
    public static final String ERROR_IDP_NOT_FOUND = "Identity Provider not found for ID: %s";
    public static final String ERROR_IDP_DISABLED = "Identity Provider is disabled: %s";
    public static final String ERROR_NO_AUTHENTICATOR = "No suitable authenticator found for IdP: %s";
    public static final String ERROR_OAUTH2_CONFIG_MISSING_CLIENT_ID =
            "OAuth 2.0 Client ID is missing for IdP: %s";
    public static final String ERROR_OAUTH2_CONFIG_MISSING_AUTHZ_ENDPOINT =
            "OAuth 2.0 Authorization Endpoint is missing for IdP: %s";
    public static final String ERROR_OAUTH2_CONFIG_MISSING_TOKEN_ENDPOINT =
            "OAuth 2.0 Token Endpoint is missing for IdP: %s";
    public static final String ERROR_NO_SCOPE_CONFIGURED = "No scope configured for the IdP.";

    // Log Messages.
    public static final String LOG_OAUTH2_CONTEXT_CREATED =
            "OAuth2 debug context created successfully with context identifier: %s";
    public static final String LOG_OAUTH2_VALIDATION_FAILED =
            "OAuth 2.0 validation failed for IdP: %s";
    public static final String LOG_OAUTH2_AUTH_URL_GENERATED =
            "OAuth 2.0 Authorization URL Generated";

    // Debug Result Context Properties.
    public static final String DEBUG_AUTH_ERROR = "DEBUG_AUTH_ERROR";
    public static final String DEBUG_AUTH_SUCCESS = "DEBUG_AUTH_SUCCESS";
    public static final String DEBUG_OAUTH_CODE = "DEBUG_OAUTH_CODE";
    public static final String DEBUG_OAUTH_STATE = "DEBUG_OAUTH_STATE";
    public static final String DEBUG_SESSION_DATA_KEY = "DEBUG_SESSION_DATA_KEY";
    public static final String DEBUG_CALLBACK_TIMESTAMP = "DEBUG_CALLBACK_TIMESTAMP";
    public static final String DEBUG_CALLBACK_PROCESSED = "DEBUG_CALLBACK_PROCESSED";
    public static final String DEBUG_ACCESS_TOKEN = "DEBUG_ACCESS_TOKEN";
    public static final String DEBUG_ID_TOKEN = "DEBUG_ID_TOKEN";
    public static final String DEBUG_TOKEN_TYPE = "DEBUG_TOKEN_TYPE";
    public static final String DEBUG_INCOMING_CLAIMS = "DEBUG_INCOMING_CLAIMS";
    public static final String DEBUG_MAPPED_LOCAL_CLAIMS_MAP = "DEBUG_MAPPED_LOCAL_CLAIMS_MAP";
    public static final String DEBUG_IDP_CONFIGURED_MAPPINGS = "DEBUG_IDP_CONFIGURED_MAPPINGS";
    public static final String DEBUG_CLAIM_MAPPING_DIAGNOSTIC = "DEBUG_CLAIM_MAPPING_DIAGNOSTIC";
    public static final String DEBUG_STEP_CLAIM_MAPPING_AUTO_USED = "DEBUG_STEP_CLAIM_MAPPING_AUTO_USED";
    public static final String DEBUG_CALLBACK_URL = "DEBUG_CALLBACK_URL";
    public static final String DEBUG_CALLBACK_URL_USED = "DEBUG_CALLBACK_URL_USED";

    // Step Status Constants.
    public static final String STEP_CLAIM_EXTRACTION_STATUS = "step_claim_extraction_status";

    // Debug Result Map Property Keys.
    public static final String SESSION_ID_PARAM = "sessionId";
    public static final String ERROR_PARAM = "error";
    public static final String IDP_NAME_PARAM = "idpName";
    public static final String SUCCESS_RESULT_PARAM = "success";
    public static final String AUTHENTICATOR_PARAM = "authenticator";
    public static final String TIMESTAMP_PARAM = "timestamp";
    public static final String STEP_CONNECTION_STATUS_PARAM = "step_connection_status";
    public static final String STEP_AUTHENTICATION_STATUS_PARAM = "step_authentication_status";
    public static final String STEP_CLAIM_MAPPING_STATUS_PARAM = "step_claim_mapping_status";
    public static final String STATUS_NOT_STARTED = "not_started";

    // Common Parameter Values.
    public static final String DEBUG_PREFIX = "debug-";
    public static final String FAILURE_STATUS = "failed";
    public static final String TRUE = "true";
    public static final String FALSE = "false";
    
    // Error Details Keys.
    public static final String ERROR_CODE_KEY = "errorCode";
    public static final String ERROR_DESCRIPTION_KEY = "errorDescription";
    public static final String ERROR_MESSAGE_KEY = "errorMessage";
    public static final String ERROR_DETAILS_KEY = "errorDetails";
    public static final String TROUBLESHOOTING_HINT_KEY = "troubleshootingHint";
    public static final String STEP_CONNECTION_ERROR_KEY = "step_connection_error";
    public static final String STEP_CLAIM_EXTRACTION_ERROR_KEY = "step_claim_extraction_error";
    public static final String EXTERNAL_REDIRECT_URL_KEY = "externalRedirectUrl";
    public static final String CALLBACK_URL_KEY = "callbackUrl";
    public static final String ID_TOKEN_KEY = "idToken";
    public static final String ID_TOKEN_PRESENT_KEY = "idTokenPresent";
    public static final String ACCESS_TOKEN_PRESENT_KEY = "accessTokenPresent";
    public static final String CLAIM_MAPPING_DIAGNOSTIC_KEY = "claimMappingDiagnostic";
    public static final String CLAIM_MAPPING_AUTO_KEY = "claim_mapping_auto";
    public static final String IDPCLAIM_KEY = "idpClaim";
    public static final String IS_CLAIM_KEY = "isClaim";
    public static final String VALUE_KEY = "value";
    public static final String STATUS_KEY = "status";
    public static final String LOCAL_CLAIM_URI_KEY = "localClaimUri";
    public static final String SUCCESSFUL_STATUS = "Successful";
    
    // Error Codes.
    public static final String ERROR_CODE_CLAIMS_EXTRACTION_FAILED = "CLAIMS_EXTRACTION_FAILED";

}
