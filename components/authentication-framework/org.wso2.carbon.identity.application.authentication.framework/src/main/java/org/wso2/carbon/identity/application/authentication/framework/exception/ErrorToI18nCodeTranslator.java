/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.exception;

import static org.wso2.carbon.identity.application.authentication.framework.exception.ErrorToI18nCodeTranslator.I18NErrorMessages.ERROR_CODE_DEFAULT;
import static org.wso2.carbon.identity.application.authentication.framework.exception.ErrorToI18nCodeTranslator.I18NErrorMessages.ERROR_CODE_JIT_PROVISIONING_USERNAME_EXISTENCE;
import static org.wso2.carbon.identity.application.authentication.framework.exception.ErrorToI18nCodeTranslator.I18NErrorMessages.ERROR_CODE_MISSING_CLAIM_REQUEST;
import static org.wso2.carbon.identity.application.authentication.framework.exception.ErrorToI18nCodeTranslator.I18NErrorMessages.ERROR_CONSENT_DISABLED_FOR_SSO;
import static org.wso2.carbon.identity.application.authentication.framework.exception.ErrorToI18nCodeTranslator.I18NErrorMessages.ERROR_GETTING_ASSOCIATION_FOR_USER;
import static org.wso2.carbon.identity.application.authentication.framework.exception.ErrorToI18nCodeTranslator.I18NErrorMessages.ERROR_INVALID_USER_STORE;
import static org.wso2.carbon.identity.application.authentication.framework.exception.ErrorToI18nCodeTranslator.I18NErrorMessages.ERROR_INVALID_USER_STORE_DOMAIN;
import static org.wso2.carbon.identity.application.authentication.framework.exception.ErrorToI18nCodeTranslator.I18NErrorMessages.ERROR_POST_AUTH_COOKIE_NOT_FOUND;
import static org.wso2.carbon.identity.application.authentication.framework.exception.ErrorToI18nCodeTranslator.I18NErrorMessages.ERROR_PROCESSING_APPLICATION_CLAIM_CONFIGS;
import static org.wso2.carbon.identity.application.authentication.framework.exception.ErrorToI18nCodeTranslator.I18NErrorMessages.ERROR_RETRIEVING_CLAIM;
import static org.wso2.carbon.identity.application.authentication.framework.exception.ErrorToI18nCodeTranslator.I18NErrorMessages.ERROR_UPDATING_CLAIMS_FOR_LOCAL_USER;
import static org.wso2.carbon.identity.application.authentication.framework.exception.ErrorToI18nCodeTranslator.I18NErrorMessages.ERROR_USER_DENIED_CONSENT;
import static org.wso2.carbon.identity.application.authentication.framework.exception.ErrorToI18nCodeTranslator.I18NErrorMessages.ERROR_USER_DENIED_CONSENT_FOR_MANDATORY;
import static org.wso2.carbon.identity.application.authentication.framework.exception.ErrorToI18nCodeTranslator.I18NErrorMessages.ERROR_WHILE_ADDING_CONSENT;
import static org.wso2.carbon.identity.application.authentication.framework.exception.ErrorToI18nCodeTranslator.I18NErrorMessages.ERROR_WHILE_BUILDING_REDIRECT_URI;
import static org.wso2.carbon.identity.application.authentication.framework.exception.ErrorToI18nCodeTranslator.I18NErrorMessages.ERROR_WHILE_CONSENT_INPUT_FOR_USER;
import static org.wso2.carbon.identity.application.authentication.framework.exception.ErrorToI18nCodeTranslator.I18NErrorMessages.ERROR_WHILE_GETTING_CLAIM_MAPPINGS;
import static org.wso2.carbon.identity.application.authentication.framework.exception.ErrorToI18nCodeTranslator.I18NErrorMessages.ERROR_WHILE_GETTING_IDP_BY_NAME;
import static org.wso2.carbon.identity.application.authentication.framework.exception.ErrorToI18nCodeTranslator.I18NErrorMessages.ERROR_WHILE_GETTING_LOCAL_USER_ID;
import static org.wso2.carbon.identity.application.authentication.framework.exception.ErrorToI18nCodeTranslator.I18NErrorMessages.ERROR_WHILE_GETTING_REALM_IN_POST_AUTHENTICATION;
import static org.wso2.carbon.identity.application.authentication.framework.exception.ErrorToI18nCodeTranslator.I18NErrorMessages.ERROR_WHILE_GETTING_REALM_TO_HANDLE_CLAIMS;
import static org.wso2.carbon.identity.application.authentication.framework.exception.ErrorToI18nCodeTranslator.I18NErrorMessages.ERROR_WHILE_GETTING_USERNAME_ASSOCIATED_WITH_IDP;
import static org.wso2.carbon.identity.application.authentication.framework.exception.ErrorToI18nCodeTranslator.I18NErrorMessages.ERROR_WHILE_GETTING_USER_STORE_DOMAIN;
import static org.wso2.carbon.identity.application.authentication.framework.exception.ErrorToI18nCodeTranslator.I18NErrorMessages.ERROR_WHILE_GETTING_USER_STORE_MANAGER;
import static org.wso2.carbon.identity.application.authentication.framework.exception.ErrorToI18nCodeTranslator.I18NErrorMessages.ERROR_WHILE_HANDLING_CLAIM_MAPPINGS;
import static org.wso2.carbon.identity.application.authentication.framework.exception.ErrorToI18nCodeTranslator.I18NErrorMessages.ERROR_WHILE_REDIRECTING_TO_CONSENT_PAGE;
import static org.wso2.carbon.identity.application.authentication.framework.exception.ErrorToI18nCodeTranslator.I18NErrorMessages.ERROR_WHILE_REDIRECTING_TO_REQUEST_CLAIMS_PAGE;
import static org.wso2.carbon.identity.application.authentication.framework.exception.ErrorToI18nCodeTranslator.I18NErrorMessages.ERROR_WHILE_RETRIEVING_CONSENT_DATA;
import static org.wso2.carbon.identity.application.authentication.framework.exception.ErrorToI18nCodeTranslator.I18NErrorMessages.ERROR_WHILE_SETTING_IDP_DATA;
import static org.wso2.carbon.identity.application.authentication.framework.exception.ErrorToI18nCodeTranslator.I18NErrorMessages.ERROR_WHILE_SETTING_IDP_DATA_IDP_IS_NULL;
import static org.wso2.carbon.identity.application.authentication.framework.exception.ErrorToI18nCodeTranslator.I18NErrorMessages.ERROR_WHILE_TRYING_CALL_SIGN_UP_ENDPOINT_FOR_PASSWORD_PROVISIONING;
import static org.wso2.carbon.identity.application.authentication.framework.exception.ErrorToI18nCodeTranslator.I18NErrorMessages.ERROR_WHILE_TRYING_TO_GET_CLAIMS_WHILE_TRYING_TO_PASSWORD_PROVISION;
import static org.wso2.carbon.identity.application.authentication.framework.exception.ErrorToI18nCodeTranslator.I18NErrorMessages.USER_ALREADY_EXISTS_ERROR;

/**
 * Responsible for translating the error codes into respective status and the status message and keeping the mapping
 * details of the status and status messages against the error codes.
 */
public class ErrorToI18nCodeTranslator {

    /**
     * Enum for I18N Messages.
     */
    public enum I18NErrorMessages {

        // Generic error messages.
        ERROR_CODE_MISSING_CLAIM_REQUEST("One or more read-only claim is missing in the requested claim set. " +
                "Please contact your administrator for more information about this issue.",
                "authentication.attempt.failed", "claim.request.missing"),
        ERROR_CODE_JIT_PROVISIONING_USERNAME_EXISTENCE("80019", "authentication.attempt.failed",
                "username.existence.error"),
        USER_ALREADY_EXISTS_ERROR("80018", "authentication.attempt.failed",
                "username.already.exists.error"),
        ERROR_WHILE_GETTING_USER_STORE_DOMAIN("80020", "authentication.attempt.failed",
                "user.store.domain.error"),
        ERROR_INVALID_USER_STORE_DOMAIN("80021", "authentication.attempt.failed",
                "invalid.user.store.domain"),
        ERROR_WHILE_GETTING_USER_STORE_MANAGER("80022", "authentication.attempt.failed",
                "user.store.manager.error"),
        ERROR_INVALID_USER_STORE("80023", "authentication.attempt.failed",
                "invalid.user.store"),
        ERROR_WHILE_GETTING_IDP_BY_NAME("80001", "authentication.attempt.failed",
                "idp.for.tenant.in.post.auth.error"),
        ERROR_WHILE_GETTING_REALM_IN_POST_AUTHENTICATION("80002", "authentication.attempt.failed",
                "realm.for.tenant.in.post.auth.error"),
        ERROR_WHILE_TRYING_TO_GET_CLAIMS_WHILE_TRYING_TO_PASSWORD_PROVISION("8008",
                "authentication.attempt.failed", "claim.error.while.password.provision"),
        ERROR_WHILE_GETTING_USERNAME_ASSOCIATED_WITH_IDP("80010", "authentication.attempt.failed",
                "username.associated.with.idp.error"),
        ERROR_WHILE_TRYING_CALL_SIGN_UP_ENDPOINT_FOR_PASSWORD_PROVISIONING("80006",
                "authentication.attempt.failed", "sign.up.end.point.for.provision.error"),
        ERROR_WHILE_ADDING_CONSENT("80014", "authentication.attempt.failed",
                "add.consent.for.tenant.error"),
        ERROR_WHILE_SETTING_IDP_DATA("80015", "authentication.attempt.failed",
                "set.idp.data.for.tenant.error"),
        ERROR_WHILE_HANDLING_CLAIM_MAPPINGS("80017", "authentication.attempt.failed",
                "claim.mapping.handling.error"),
        ERROR_WHILE_SETTING_IDP_DATA_IDP_IS_NULL("80016", "authentication.attempt.failed",
                "resident.idp.null.for.tenant.error"),
        ERROR_WHILE_GETTING_CLAIM_MAPPINGS("80013", "authentication.attempt.failed",
                "claim.mapping.getting.error"),
        ERROR_WHILE_GETTING_LOCAL_USER_ID("80012", "authentication.attempt.failed",
                "associated.local.user.id.error"),
        ERROR_WHILE_RETRIEVING_CONSENT_DATA("Authentication failed. Error occurred while processing user consent.",
                "authentication.attempt.failed", "consent.retrieve.for.user.error"),
        ERROR_CONSENT_DISABLED_FOR_SSO("Authentication Failure: Consent management is disabled for SSO.",
                "authentication.attempt.failed", "consent.disabled.for.sso.error"),
        ERROR_WHILE_CONSENT_INPUT_FOR_USER("Authentication failed. Error while processing user consent input.",
                "authentication.attempt.failed", "consent.input.for.user.error"),
        ERROR_USER_DENIED_CONSENT("Authentication failed. User denied consent to share information with",
                "authentication.attempt.failed", "user.denied.consent.error"),
        ERROR_USER_DENIED_CONSENT_FOR_MANDATORY("Authentication failed. Consent denied for mandatory attributes.",
                "authentication.attempt.failed", "user.denied.mandatory.consent.error"),
        ERROR_WHILE_REDIRECTING_TO_CONSENT_PAGE("Authentication failed. Error while processing consent requirements.",
                "authentication.attempt.failed", "consent.page.error"),
        ERROR_PROCESSING_APPLICATION_CLAIM_CONFIGS("Authentication failed. Error while processing application " +
                "claim configurations.", "authentication.attempt.failed", "application.configs.null.error"),
        ERROR_XACML_EVAL_FAILED("Authorization Failed.XACML policy evaluation failed for user.",
                "authentication.attempt.failed", "xacml.policy.evaluation.failed"),
        ERROR_WHILE_EVAL_AUTHZ("Authorization Failed.Error while trying to evaluate authorization",
                "authentication.attempt.failed", "xacml.authz.eval.failed"),
        ERROR_WHILE_REDIRECTING_TO_REQUEST_CLAIMS_PAGE("Error while handling missing mandatory claims. Error in " +
                "request claims page.", "authentication.attempt.failed", "request.claims.page.error"),
        ERROR_WHILE_BUILDING_REDIRECT_URI("Error while handling missing mandatory claims. Error in redirect URI.",
                "authentication.attempt.failed", "request.claims.page.uri.build.error"),
        ERROR_RETRIEVING_CLAIM("Error while handling missing mandatory claims. Error in retrieving claim.",
                "authentication.attempt.failed", "retrieve.claim.error"),
        ERROR_GETTING_ASSOCIATION_FOR_USER("Error while handling missing mandatory claims. Error in association.",
                "authentication.attempt.failed", "get.association.for.user.error"),
        ERROR_UPDATING_CLAIMS_FOR_LOCAL_USER("Error while handling missing mandatory claims. Error in updating claims.",
                "authentication.attempt.failed", "update.local.user.claims.error"),
        ERROR_WHILE_GETTING_REALM_TO_HANDLE_CLAIMS("Error while handling missing mandatory claims. Error in realm.",
                "authentication.attempt.failed", "retrieving.realm.to.handle.claims.error"),
        ERROR_POST_AUTH_COOKIE_NOT_FOUND("Invalid Request: Your authentication flow is ended or invalid. " +
                "Please initiate again.", "authentication.attempt.failed", "post.auth.cookie.not.found"),
        ERROR_CODE_DEFAULT("Default", "authentication.attempt.failed", "authorization.failed");

        private final String errorCode;
        private final String status;
        private final String statusMsg;

        I18NErrorMessages(String errorCode, String status, String statusMsg) {

            this.errorCode = errorCode;
            this.status = status;
            this.statusMsg = statusMsg;
        }

        public String getErrorCode() {

            return errorCode;
        }

        public String getStatus() {

            return status;
        }

        public String getStatusMsg() {

            return statusMsg;
        }

        @Override
        public String toString() {

            return status + "_" + statusMsg;
        }
    }

    public static I18nErrorCodeWrapper translate(String errorCode) {

        if (ERROR_CODE_MISSING_CLAIM_REQUEST.getErrorCode().equals(errorCode)) {
            return new I18nErrorCodeWrapper(ERROR_CODE_MISSING_CLAIM_REQUEST.getStatus(),
                    ERROR_CODE_MISSING_CLAIM_REQUEST.getStatusMsg());
        } else if (USER_ALREADY_EXISTS_ERROR.getErrorCode().equals(errorCode)) {
            return new I18nErrorCodeWrapper(USER_ALREADY_EXISTS_ERROR.getStatus(),
                    USER_ALREADY_EXISTS_ERROR.getStatusMsg());
        } else if (ERROR_CODE_JIT_PROVISIONING_USERNAME_EXISTENCE.getErrorCode().equals(errorCode)) {
            return new I18nErrorCodeWrapper(ERROR_CODE_JIT_PROVISIONING_USERNAME_EXISTENCE.getStatus(),
                    ERROR_CODE_JIT_PROVISIONING_USERNAME_EXISTENCE.getStatusMsg());
        } else if (ERROR_WHILE_GETTING_USER_STORE_DOMAIN.getErrorCode().equals(errorCode)) {
            return new I18nErrorCodeWrapper(ERROR_WHILE_GETTING_USER_STORE_DOMAIN.getStatus(),
                    ERROR_WHILE_GETTING_USER_STORE_DOMAIN.getStatusMsg());
        } else if (ERROR_INVALID_USER_STORE_DOMAIN.getErrorCode().equals(errorCode)) {
            return new I18nErrorCodeWrapper(ERROR_INVALID_USER_STORE_DOMAIN.getStatus(),
                    ERROR_INVALID_USER_STORE_DOMAIN.getStatusMsg());
        } else if (ERROR_WHILE_GETTING_USER_STORE_MANAGER.getErrorCode().equals(errorCode)) {
            return new I18nErrorCodeWrapper(ERROR_WHILE_GETTING_USER_STORE_MANAGER.getStatus(),
                    ERROR_WHILE_GETTING_USER_STORE_MANAGER.getStatusMsg());
        } else if (ERROR_INVALID_USER_STORE.getErrorCode().equals(errorCode)) {
            return new I18nErrorCodeWrapper(ERROR_INVALID_USER_STORE.getStatus(),
                    ERROR_INVALID_USER_STORE.getStatusMsg());
        } else if (ERROR_WHILE_GETTING_IDP_BY_NAME.getErrorCode().equals(errorCode)) {
            return new I18nErrorCodeWrapper(ERROR_WHILE_GETTING_IDP_BY_NAME.getStatus(),
                    ERROR_WHILE_GETTING_IDP_BY_NAME.getStatusMsg());
        } else if (ERROR_WHILE_GETTING_REALM_IN_POST_AUTHENTICATION.getErrorCode().equals(errorCode)) {
            return new I18nErrorCodeWrapper(ERROR_WHILE_GETTING_REALM_IN_POST_AUTHENTICATION.getStatus(),
                    ERROR_WHILE_GETTING_REALM_IN_POST_AUTHENTICATION.getStatusMsg());
        } else if (ERROR_WHILE_TRYING_TO_GET_CLAIMS_WHILE_TRYING_TO_PASSWORD_PROVISION
                .getErrorCode().equals(errorCode)) {
            return new I18nErrorCodeWrapper(ERROR_WHILE_TRYING_TO_GET_CLAIMS_WHILE_TRYING_TO_PASSWORD_PROVISION
                    .getStatus(),
                    ERROR_WHILE_TRYING_TO_GET_CLAIMS_WHILE_TRYING_TO_PASSWORD_PROVISION.getStatusMsg());
        } else if (ERROR_WHILE_GETTING_USERNAME_ASSOCIATED_WITH_IDP.getErrorCode().equals(errorCode)) {
            return new I18nErrorCodeWrapper(ERROR_WHILE_GETTING_USERNAME_ASSOCIATED_WITH_IDP.getStatus(),
                    ERROR_WHILE_GETTING_USERNAME_ASSOCIATED_WITH_IDP.getStatusMsg());
        } else if (ERROR_WHILE_TRYING_CALL_SIGN_UP_ENDPOINT_FOR_PASSWORD_PROVISIONING.getErrorCode()
                .equals(errorCode)) {
            return new I18nErrorCodeWrapper(ERROR_WHILE_TRYING_CALL_SIGN_UP_ENDPOINT_FOR_PASSWORD_PROVISIONING
                    .getStatus(),
                    ERROR_WHILE_TRYING_CALL_SIGN_UP_ENDPOINT_FOR_PASSWORD_PROVISIONING.getStatusMsg());
        } else if (ERROR_WHILE_ADDING_CONSENT.getErrorCode().equals(errorCode)) {
            return new I18nErrorCodeWrapper(ERROR_WHILE_ADDING_CONSENT.getStatus(),
                    ERROR_WHILE_ADDING_CONSENT.getStatusMsg());
        } else if (ERROR_WHILE_SETTING_IDP_DATA.getErrorCode().equals(errorCode)) {
            return new I18nErrorCodeWrapper(ERROR_WHILE_SETTING_IDP_DATA.getStatus(),
                    ERROR_WHILE_SETTING_IDP_DATA.getStatusMsg());
        } else if (ERROR_WHILE_HANDLING_CLAIM_MAPPINGS.getErrorCode().equals(errorCode)) {
            return new I18nErrorCodeWrapper(ERROR_WHILE_HANDLING_CLAIM_MAPPINGS.getStatus(),
                    ERROR_WHILE_HANDLING_CLAIM_MAPPINGS.getStatusMsg());
        } else if (ERROR_WHILE_SETTING_IDP_DATA_IDP_IS_NULL.getErrorCode().equals(errorCode)) {
            return new I18nErrorCodeWrapper(ERROR_WHILE_SETTING_IDP_DATA_IDP_IS_NULL.getStatus(),
                    ERROR_WHILE_SETTING_IDP_DATA_IDP_IS_NULL.getStatusMsg());
        } else if (ERROR_WHILE_GETTING_CLAIM_MAPPINGS.getErrorCode().equals(errorCode)) {
            return new I18nErrorCodeWrapper(ERROR_WHILE_GETTING_CLAIM_MAPPINGS.getStatus(),
                    ERROR_WHILE_GETTING_CLAIM_MAPPINGS.getStatusMsg());
        } else if (ERROR_WHILE_GETTING_LOCAL_USER_ID.getErrorCode().equals(errorCode)) {
            return new I18nErrorCodeWrapper(ERROR_WHILE_GETTING_LOCAL_USER_ID.getStatus(),
                    ERROR_WHILE_GETTING_LOCAL_USER_ID.getStatusMsg());
        } else if (ERROR_WHILE_RETRIEVING_CONSENT_DATA.getErrorCode().equals(errorCode)) {
            return new I18nErrorCodeWrapper(ERROR_WHILE_RETRIEVING_CONSENT_DATA.getStatus(),
                    ERROR_WHILE_RETRIEVING_CONSENT_DATA.getStatusMsg());
        } else if (ERROR_CONSENT_DISABLED_FOR_SSO.getErrorCode().equals(errorCode)) {
            return new I18nErrorCodeWrapper(ERROR_CONSENT_DISABLED_FOR_SSO.getStatus(),
                    ERROR_CONSENT_DISABLED_FOR_SSO.getStatusMsg());
        } else if (ERROR_WHILE_CONSENT_INPUT_FOR_USER.getErrorCode().equals(errorCode)) {
            return new I18nErrorCodeWrapper(ERROR_WHILE_CONSENT_INPUT_FOR_USER.getStatus(),
                    ERROR_WHILE_CONSENT_INPUT_FOR_USER.getStatusMsg());
        } else if (ERROR_USER_DENIED_CONSENT.getErrorCode().contains(errorCode)) {
            return new I18nErrorCodeWrapper(ERROR_USER_DENIED_CONSENT.getStatus(),
                    ERROR_USER_DENIED_CONSENT.getStatusMsg());
        } else if (ERROR_USER_DENIED_CONSENT_FOR_MANDATORY.getErrorCode().equals(errorCode)) {
            return new I18nErrorCodeWrapper(ERROR_USER_DENIED_CONSENT_FOR_MANDATORY.getStatus(),
                    ERROR_USER_DENIED_CONSENT_FOR_MANDATORY.getStatusMsg());
        } else if (ERROR_WHILE_REDIRECTING_TO_CONSENT_PAGE.getErrorCode().equals(errorCode)) {
            return new I18nErrorCodeWrapper(ERROR_WHILE_REDIRECTING_TO_CONSENT_PAGE.getStatus(),
                    ERROR_WHILE_REDIRECTING_TO_CONSENT_PAGE.getStatusMsg());
        } else if (ERROR_PROCESSING_APPLICATION_CLAIM_CONFIGS.getErrorCode().equals(errorCode)) {
            return new I18nErrorCodeWrapper(ERROR_PROCESSING_APPLICATION_CLAIM_CONFIGS.getStatus(),
                    ERROR_PROCESSING_APPLICATION_CLAIM_CONFIGS.getStatusMsg());
        } else if (ERROR_WHILE_REDIRECTING_TO_REQUEST_CLAIMS_PAGE.getErrorCode().equals(errorCode)) {
            return new I18nErrorCodeWrapper(ERROR_WHILE_REDIRECTING_TO_REQUEST_CLAIMS_PAGE.getStatus(),
                    ERROR_WHILE_REDIRECTING_TO_REQUEST_CLAIMS_PAGE.getStatusMsg());
        } else if (ERROR_WHILE_BUILDING_REDIRECT_URI.getErrorCode().equals(errorCode)) {
            return new I18nErrorCodeWrapper(ERROR_WHILE_BUILDING_REDIRECT_URI.getStatus(),
                    ERROR_WHILE_BUILDING_REDIRECT_URI.getStatusMsg());
        } else if (ERROR_RETRIEVING_CLAIM.getErrorCode().equals(errorCode)) {
            return new I18nErrorCodeWrapper(ERROR_RETRIEVING_CLAIM.getStatus(),
                    ERROR_RETRIEVING_CLAIM.getStatusMsg());
        } else if (ERROR_GETTING_ASSOCIATION_FOR_USER.getErrorCode().equals(errorCode)) {
            return new I18nErrorCodeWrapper(ERROR_GETTING_ASSOCIATION_FOR_USER.getStatus(),
                    ERROR_GETTING_ASSOCIATION_FOR_USER.getStatusMsg());
        } else if (ERROR_UPDATING_CLAIMS_FOR_LOCAL_USER.getErrorCode().equals(errorCode)) {
            return new I18nErrorCodeWrapper(ERROR_UPDATING_CLAIMS_FOR_LOCAL_USER.getStatus(),
                    ERROR_UPDATING_CLAIMS_FOR_LOCAL_USER.getStatusMsg());
        } else if (ERROR_WHILE_GETTING_REALM_TO_HANDLE_CLAIMS.getErrorCode().equals(errorCode)) {
            return new I18nErrorCodeWrapper(ERROR_WHILE_GETTING_REALM_TO_HANDLE_CLAIMS.getStatus(),
                    ERROR_WHILE_GETTING_REALM_TO_HANDLE_CLAIMS.getStatusMsg());
        } else if (ERROR_POST_AUTH_COOKIE_NOT_FOUND.getErrorCode().equals(errorCode)) {
            return new I18nErrorCodeWrapper(ERROR_POST_AUTH_COOKIE_NOT_FOUND.getStatus(),
                    ERROR_POST_AUTH_COOKIE_NOT_FOUND.getStatusMsg());
        } else {
            return new I18nErrorCodeWrapper(ERROR_CODE_DEFAULT.getStatus(), ERROR_CODE_DEFAULT.getStatusMsg());
        }
    }
}
