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

package org.wso2.carbon.identity.fraud.detection.core.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.identity.fraud.detection.core.IdentityFraudDetector;
import org.wso2.carbon.identity.fraud.detection.core.constant.FraudDetectionConstants;
import org.wso2.carbon.identity.fraud.detection.core.exception.FraudDetectionConfigServerException;
import org.wso2.carbon.identity.fraud.detection.core.exception.UnsupportedFraudDetectionEventException;
import org.wso2.carbon.identity.fraud.detection.core.internal.IdentityFraudDetectionDataHolder;
import org.wso2.carbon.identity.fraud.detection.core.model.FraudDetectorRequestDTO;
import org.wso2.carbon.identity.fraud.detection.core.model.FraudDetectorResponseDTO;
import org.wso2.carbon.identity.fraud.detection.core.service.FraudDetectionConfigsService;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.DiagnosticLog;

import java.util.Map;

import static org.wso2.carbon.identity.event.IdentityEventConstants.Event.POST_ADD_NEW_PASSWORD;
import static org.wso2.carbon.identity.event.IdentityEventConstants.Event.POST_ADD_USER;
import static org.wso2.carbon.identity.event.IdentityEventConstants.Event.POST_ADD_USER_WITH_ASK_PASSWORD;
import static org.wso2.carbon.identity.event.IdentityEventConstants.Event.POST_EMAIL_CHANGE_VERIFICATION;
import static org.wso2.carbon.identity.event.IdentityEventConstants.Event.POST_FORCE_PASSWORD_RESET_BY_ADMIN;
import static org.wso2.carbon.identity.event.IdentityEventConstants.Event.POST_GENERATE_EMAIL_OTP;
import static org.wso2.carbon.identity.event.IdentityEventConstants.Event.POST_GENERATE_SMS_OTP;
import static org.wso2.carbon.identity.event.IdentityEventConstants.Event.POST_MOBILE_CHANGE_VERIFICATION;
import static org.wso2.carbon.identity.event.IdentityEventConstants.Event.POST_SEND_RECOVERY_NOTIFICATION;
import static org.wso2.carbon.identity.event.IdentityEventConstants.Event.POST_UPDATE_CREDENTIAL_BY_SCIM;
import static org.wso2.carbon.identity.event.IdentityEventConstants.Event.POST_USER_ACCOUNT_CONFIRMATION;
import static org.wso2.carbon.identity.event.IdentityEventConstants.Event.POST_USER_PROFILE_UPDATE;
import static org.wso2.carbon.identity.event.IdentityEventConstants.Event.POST_VALIDATE_EMAIL_OTP;
import static org.wso2.carbon.identity.event.IdentityEventConstants.Event.POST_VALIDATE_SMS_OTP;
import static org.wso2.carbon.identity.event.IdentityEventConstants.Event.TRIGGER_NOTIFICATION;
import static org.wso2.carbon.identity.event.IdentityEventConstants.Event.TRIGGER_SMS_NOTIFICATION;
import static org.wso2.carbon.identity.event.IdentityEventConstants.EventName.AUTHENTICATION_FAILURE;
import static org.wso2.carbon.identity.event.IdentityEventConstants.EventName.AUTHENTICATION_STEP_FAILURE;
import static org.wso2.carbon.identity.event.IdentityEventConstants.EventName.AUTHENTICATION_SUCCESS;
import static org.wso2.carbon.identity.event.IdentityEventConstants.EventName.SESSION_TERMINATE;
import static org.wso2.carbon.identity.event.IdentityEventConstants.EventProperty.CONTEXT;
import static org.wso2.carbon.identity.event.IdentityEventConstants.EventProperty.IS_PASSWORD_UPDATE_ACTION;
import static org.wso2.carbon.identity.event.IdentityEventConstants.EventProperty.RECOVERY_SCENARIO;
import static org.wso2.carbon.identity.event.IdentityEventConstants.EventProperty.SCENARIO;
import static org.wso2.carbon.identity.event.IdentityEventConstants.EventProperty.Scenario.ScenarioTypes.POST_CREDENTIAL_UPDATE_BY_ADMIN;
import static org.wso2.carbon.identity.event.IdentityEventConstants.EventProperty.Scenario.ScenarioTypes.POST_CREDENTIAL_UPDATE_BY_USER;
import static org.wso2.carbon.identity.event.IdentityEventConstants.EventProperty.Scenario.ScenarioTypes.POST_USER_PROFILE_UPDATE_BY_ADMIN;
import static org.wso2.carbon.identity.event.IdentityEventConstants.EventProperty.Scenario.ScenarioTypes.POST_USER_PROFILE_UPDATE_BY_USER;
import static org.wso2.carbon.identity.fraud.detection.core.constant.FraudDetectionConstants.ACCOUNT_CONFIRMATION_TEMPLATE_TYPE;
import static org.wso2.carbon.identity.fraud.detection.core.constant.FraudDetectionConstants.ExecutionStatus.ERROR;
import static org.wso2.carbon.identity.fraud.detection.core.constant.FraudDetectionConstants.ExecutionStatus.FAILURE;
import static org.wso2.carbon.identity.fraud.detection.core.constant.FraudDetectionConstants.ExecutionStatus.SKIPPED;
import static org.wso2.carbon.identity.fraud.detection.core.constant.FraudDetectionConstants.ExecutionStatus.SUCCESS;
import static org.wso2.carbon.identity.fraud.detection.core.constant.FraudDetectionConstants.INTERNAL_EVENT_NAME;
import static org.wso2.carbon.identity.fraud.detection.core.constant.FraudDetectionConstants.LogConstants.ActionIDs.FRAUD_DETECTION_EVENT_RESPONSE;
import static org.wso2.carbon.identity.fraud.detection.core.constant.FraudDetectionConstants.LogConstants.FRAUD_DETECTION_SERVICE;
import static org.wso2.carbon.identity.fraud.detection.core.constant.FraudDetectionConstants.RecoveryScenarios.ADMIN_FORCED_PASSWORD_RESET_VIA_EMAIL_LINK;
import static org.wso2.carbon.identity.fraud.detection.core.constant.FraudDetectionConstants.RecoveryScenarios.ADMIN_FORCED_PASSWORD_RESET_VIA_OTP;
import static org.wso2.carbon.identity.fraud.detection.core.constant.FraudDetectionConstants.RecoveryScenarios.ASK_PASSWORD;
import static org.wso2.carbon.identity.fraud.detection.core.constant.FraudDetectionConstants.RecoveryScenarios.EMAIL_VERIFICATION_ON_UPDATE;
import static org.wso2.carbon.identity.fraud.detection.core.constant.FraudDetectionConstants.RecoveryScenarios.MOBILE_VERIFICATION_ON_UPDATE;
import static org.wso2.carbon.identity.fraud.detection.core.constant.FraudDetectionConstants.RecoveryScenarios.NOTIFICATION_BASED_PW_RECOVERY;
import static org.wso2.carbon.identity.fraud.detection.core.constant.FraudDetectionConstants.RecoveryScenarios.NOTIFICATION_BASED_PW_RECOVERY_OFFLINE_INVITE;
import static org.wso2.carbon.identity.fraud.detection.core.constant.FraudDetectionConstants.TEMPLATE_TYPE;
import static org.wso2.carbon.user.core.UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME;

/**
 * Utility class for handling identity events and fraud detection events.
 */
public class EventUtil {

    private static final Log LOG = LogFactory.getLog(EventUtil.class);

    /**
     * Resolve the fraud detection event from the identity event.
     *
     * @param event Identity event.
     * @return Resolved fraud detection event.
     * @throws UnsupportedFraudDetectionEventException If the event is not supported for fraud detection.
     */
    public static FraudDetectionConstants.FraudDetectionEvents resolveFraudDetectionEvent(Event event)
            throws UnsupportedFraudDetectionEventException {

        if (isLoginEvent(event)) {
            return FraudDetectionConstants.FraudDetectionEvents.LOGIN;
        } else if (isLogoutEvent(event)) {
            return FraudDetectionConstants.FraudDetectionEvents.LOGOUT;
        } else if (isPostUserCreationEvent(event)) {
            return FraudDetectionConstants.FraudDetectionEvents.POST_USER_CREATION;
        } else if (isPostPasswordUpdateEvent(event)) {
            return FraudDetectionConstants.FraudDetectionEvents.POST_UPDATE_PASSWORD;
        } else if (isPasswordUpdateNotificationEvent(event)) {
            return FraudDetectionConstants.FraudDetectionEvents.PRE_UPDATE_PASSWORD_NOTIFICATION;
        } else if (isSelfRegistrationVerificationNotificationEvent(event)) {
            return FraudDetectionConstants.FraudDetectionEvents.SELF_REGISTRATION_VERIFICATION_NOTIFICATION;
        } else if (isPostSelfRegistrationVerificationEvent(event)) {
            return FraudDetectionConstants.FraudDetectionEvents.POST_SELF_REGISTRATION_VERIFICATION;
        } else if (isUserAttributeUpdateVerificationNotificationEvent(event)) {
            return FraudDetectionConstants.FraudDetectionEvents.USER_ATTRIBUTE_UPDATE_VERIFICATION_NOTIFICATION;
        } else if (isPostUserAttributeUpdateVerificationEvent(event)) {
            return FraudDetectionConstants.FraudDetectionEvents.POST_USER_ATTRIBUTE_UPDATE_VERIFICATION;
        } else if (isAuthenticationStepVerificationEvent(event)) {
            return FraudDetectionConstants.FraudDetectionEvents.AUTHENTICATION_STEP_NOTIFICATION_VERIFICATION;
        } else if (isPostUserProfileUpdateEvent(event)) {
            return FraudDetectionConstants.FraudDetectionEvents.POST_UPDATE_USER_PROFILE;
        } else {
            throw new UnsupportedFraudDetectionEventException("Unsupported fraud detection event: "
                    + event.getEventName());
        }
    }

    /**
     * Handle the identity event by publishing to all configured fraud detectors.
     *
     * @param event                 Identity event.
     * @param fraudDetectionEvent   Resolved fraud detection event.
     * @param isLoggingEnabled      Whether logging is enabled for the request payload.
     */
    public static void handleEvent(Event event, FraudDetectionConstants.FraudDetectionEvents fraudDetectionEvent,
                                   boolean isLoggingEnabled) {

        event.getEventProperties().put(INTERNAL_EVENT_NAME, event.getEventName());
        FraudDetectorRequestDTO fraudDetectorRequestDTO
                = new FraudDetectorRequestDTO(fraudDetectionEvent, event.getEventProperties());
        fraudDetectorRequestDTO.setLogRequestPayload(isLoggingEnabled);
        Map<String, IdentityFraudDetector> fraudDetectors =
                IdentityFraudDetectionDataHolder.getInstance().getIdentityFraudDetectors();
        fraudDetectors.values().forEach(identityFraudDetector -> {
            handleResponse(identityFraudDetector, identityFraudDetector.publishRequest(fraudDetectorRequestDTO));
        });
    }

    /**
     * Handle the fraud detector response and log accordingly.
     *
     * @param fraudDetector Fraud detector.
     * @param responseDTO   Fraud detector response DTO.
     */
    public static void handleResponse(IdentityFraudDetector fraudDetector, FraudDetectorResponseDTO responseDTO) {

        String fraudDetectorName = fraudDetector.getName();
        String eventName = responseDTO.getEventName().name();
        DiagnosticLog.ResultStatus resultStatus = DiagnosticLog.ResultStatus.FAILED;
        String resultMessage = StringUtils.EMPTY;

        if (SKIPPED.equals(responseDTO.getStatus())) {
            resultMessage = "Request to the fraud detector: " + fraudDetectorName + " was skipped as it cannot "
                    + "handle the request. Event name: " + eventName;
            LOG.debug(resultMessage);
        } else if (SUCCESS.equals(responseDTO.getStatus())) {
            resultMessage = "Successfully sent request to the fraud detector: " + fraudDetectorName +
                    ". Event name: " + eventName;
            LOG.debug(resultMessage);
            resultStatus = DiagnosticLog.ResultStatus.SUCCESS;
        } else if (ERROR.equals(responseDTO.getStatus())) {
            String errorType = responseDTO.getErrorType() != null ?
                    responseDTO.getErrorType().getValue() : "Unknown";
            String errorReason = responseDTO.getErrorReason() != null ?
                    responseDTO.getErrorReason() : "Unknown";
            resultMessage = "Error occurred while processing the request to the fraud detector: " + fraudDetectorName
                    + ". Event name: " + eventName + ". Error Type: " + errorType + ". Error Reason: " + errorReason;
            LOG.error(resultMessage);
        } else if (FAILURE.equals(responseDTO.getStatus())) {
            resultMessage = "Request to the fraud detector: " + fraudDetectorName + " failed."
                    + " Event name: " + eventName;
            LOG.error(resultMessage);
        }

        publishDiagnosticLogOnResponse(resultStatus, resultMessage, fraudDetectorName, eventName);
    }

    /**
     * Publish diagnostic log on fraud detector response.
     *
     * @param resultStatus      Result status.
     * @param resultMessage     Result message.
     * @param fraudDetectorName Fraud detector name.
     * @param eventName         Event name.
     */
    private static void publishDiagnosticLogOnResponse(DiagnosticLog.ResultStatus resultStatus, String resultMessage,
                                                       String fraudDetectorName, String eventName) {

        if (!LoggerUtils.isDiagnosticLogsEnabled()) {
            return;
        }

        DiagnosticLog.DiagnosticLogBuilder diagnosticLogBuilder = new DiagnosticLog.DiagnosticLogBuilder(
                FRAUD_DETECTION_SERVICE, FRAUD_DETECTION_EVENT_RESPONSE
        );
        diagnosticLogBuilder
                .logDetailLevel(DiagnosticLog.LogDetailLevel.INTERNAL_SYSTEM)
                .resultStatus(resultStatus)
                .resultMessage(resultMessage)
                .inputParam("Fraud Detector Name", fraudDetectorName)
                .inputParam("Fraud Detection Event", eventName);
        LoggerUtils.triggerDiagnosticLogEvent(diagnosticLogBuilder);
    }

    /**
     * Get user claim values from the user store.
     *
     * @param username        Username.
     * @param tenantDomain    Tenant domain.
     * @param userStoreDomain User store domain.
     * @param claims          Claims to be retrieved.
     * @return Map of claim URIs and their values.
     * @throws UserStoreException If an error occurs while retrieving the claim values.
     */
    public static Map<String, String> getUserClaimValues(String username, String tenantDomain, String userStoreDomain,
                                           String[] claims) throws UserStoreException {

        UserRealm userRealm = getUserRealm(tenantDomain);
        UserStoreManager userStoreManager = getUserStoreManager(userRealm, userStoreDomain);
        String tenantAwareUsername = UserCoreUtil.addDomainToName(username, userStoreDomain);
        return userStoreManager.getUserClaimValues(tenantAwareUsername, claims, null);
    }

    /**
     * Check if user info publishing is allowed in the payload.
     *
     * @param tenantDomain Tenant domain.
     * @return true if allowed, false otherwise.
     * @throws FraudDetectionConfigServerException If an error occurs while retrieving the configuration.
     */
    public static boolean isAllowUserInfoInPayload(String tenantDomain) throws FraudDetectionConfigServerException {

        return FraudDetectionConfigsService.getInstance().getFraudDetectionConfigs(tenantDomain).isPublishUserInfo();
    }

    /**
     * Check if device metadata publishing is allowed in the payload.
     *
     * @param tenantDomain Tenant domain.
     * @return true if allowed, false otherwise.
     * @throws FraudDetectionConfigServerException If an error occurs while retrieving the configuration.
     */
    public static boolean isAllowDeviceMetadataInPayload(String tenantDomain) throws
            FraudDetectionConfigServerException {

        return FraudDetectionConfigsService.getInstance().getFraudDetectionConfigs(tenantDomain)
                .isPublishDeviceMetadata();
    }

    /**
     * Check if request payload logging is enabled.
     *
     * @param tenantDomain Tenant domain.
     * @return true if enabled, false otherwise.
     * @throws FraudDetectionConfigServerException If an error occurs while retrieving the configuration.
     */
    public static boolean isRequestPayloadLoggingEnabled(String tenantDomain) throws
            FraudDetectionConfigServerException {

        return FraudDetectionConfigsService.getInstance().getFraudDetectionConfigs(tenantDomain).isLogRequestPayload();
    }

    /**
     * Get the UserStoreManager for the given user store domain.
     *
     * @param userRealm      User realm.
     * @param userStoreDomain User store domain.
     * @return UserStoreManager instance.
     */
    private static UserStoreManager getUserStoreManager(UserRealm userRealm, String userStoreDomain)
            throws org.wso2.carbon.user.core.UserStoreException {

        UserStoreManager userStoreManager = userRealm.getUserStoreManager();
        if (StringUtils.isBlank(userStoreDomain) || PRIMARY_DEFAULT_DOMAIN_NAME.equals(userStoreDomain)) {
            return userStoreManager;
        }
        return ((AbstractUserStoreManager) userStoreManager).getSecondaryUserStoreManager(userStoreDomain);
    }

    /**
     * Get the UserRealm for the given tenant domain.
     *
     * @param tenantDomain Tenant domain.
     * @return UserRealm instance.
     * @throws UserStoreException If an error occurs while retrieving the UserRealm.
     */
    private static UserRealm getUserRealm(String tenantDomain) throws UserStoreException {

        RealmService realmService = IdentityFraudDetectionDataHolder.getInstance().getRealmService();
        int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
        return (UserRealm) realmService.getTenantUserRealm(tenantId);
    }

    /**
     * Check if the event is a login event.
     *
     * @param event Identity event.
     * @return true if it is a login event, false otherwise.
     */
    private static boolean isLoginEvent(Event event) {

        String eventName = event.getEventName();
        if (AUTHENTICATION_SUCCESS.name().equals(eventName) || AUTHENTICATION_FAILURE.name().equals(eventName)
                || AUTHENTICATION_STEP_FAILURE.name().equals(eventName)) {
            if (event.getEventProperties().containsKey(CONTEXT)) {
                AuthenticationContext context = (AuthenticationContext) event.getEventProperties().get(CONTEXT);
                if (context.isPassiveAuthenticate()) {
                    LOG.debug("Ignoring login event as it is a passive authentication.");
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the event is a logout event.
     *
     * @param event Identity event.
     * @return true if it is a logout event, false otherwise.
     */
    private static boolean isLogoutEvent(Event event) {

        return SESSION_TERMINATE.name().equals(event.getEventName());
    }

    /**
     * Check if the event is a post user creation event.
     *
     * @param event Identity event.
     * @return true if it is a post user creation event, false otherwise.
     */
    private static boolean isPostUserCreationEvent(Event event) {

        return POST_ADD_USER.equals(event.getEventName());
    }

    /**
     * Check if the event is a post password update event.
     *
     * @param event Identity event.
     * @return true if it is a post password update event, false otherwise.
     */
    private static boolean isPostPasswordUpdateEvent(Event event) {

        String eventName = event.getEventName();
        if (POST_ADD_NEW_PASSWORD.equals(eventName)) {
            String recoveryScenario = (String) event.getEventProperties().get(RECOVERY_SCENARIO);
            return ASK_PASSWORD.equals(recoveryScenario) || NOTIFICATION_BASED_PW_RECOVERY.equals(recoveryScenario)
                    || ADMIN_FORCED_PASSWORD_RESET_VIA_EMAIL_LINK.equals(recoveryScenario)
                    || ADMIN_FORCED_PASSWORD_RESET_VIA_OTP.equals(recoveryScenario)
                    || NOTIFICATION_BASED_PW_RECOVERY_OFFLINE_INVITE.equals(recoveryScenario);
        } else if (POST_UPDATE_CREDENTIAL_BY_SCIM.equals(eventName)) {
            String scenario = (String) event.getEventProperties().get(SCENARIO);
            return POST_CREDENTIAL_UPDATE_BY_ADMIN.equals(scenario) || POST_CREDENTIAL_UPDATE_BY_USER.equals(scenario);
        }
        return false;
    }

    /**
     * Check if the event is a password update notification event.
     *
     * @param event Identity event.
     * @return true if it is a password update notification event, false otherwise.
     */
    private static boolean isPasswordUpdateNotificationEvent(Event event) {

        String eventName = event.getEventName();
        return POST_ADD_USER_WITH_ASK_PASSWORD.equals(eventName)
                || POST_FORCE_PASSWORD_RESET_BY_ADMIN.equals(eventName)
                || (POST_SEND_RECOVERY_NOTIFICATION.equals(eventName)
                && (NOTIFICATION_BASED_PW_RECOVERY.equals(event.getEventProperties().get(RECOVERY_SCENARIO))));
    }

    /**
     * Check if the event is a self registration verification notification event.
     *
     * @param event Identity event.
     * @return true if it is a self registration verification notification event, false otherwise.
     */
    private static boolean isSelfRegistrationVerificationNotificationEvent(Event event) {

        String eventName = event.getEventName();
        if (TRIGGER_NOTIFICATION.equals(eventName)) {
            String templateType = (String) event.getEventProperties().get(TEMPLATE_TYPE);
            // This is a self registration account verification notification scenario.
            return (ACCOUNT_CONFIRMATION_TEMPLATE_TYPE.equals(templateType));
        }
        return false;
    }

    /**
     * Check if the event is a post self registration verification event.
     *
     * @param event Identity event.
     * @return true if it is a post self registration verification event, false otherwise.
     */
    private static boolean isPostSelfRegistrationVerificationEvent(Event event) {

        String eventName = event.getEventName();
        return POST_USER_ACCOUNT_CONFIRMATION.equals(eventName);
    }

    /**
     * Check if the event is a user attribute update verification notification event.
     *
     * @param event Identity event.
     * @return true if it is a user attribute update verification notification event, false otherwise.
     */
    private static boolean isUserAttributeUpdateVerificationNotificationEvent(Event event) {

        String eventName = event.getEventName();
        if (TRIGGER_NOTIFICATION.equals(eventName) || TRIGGER_SMS_NOTIFICATION.equals(eventName)) {
            String scenario = (String) event.getEventProperties().get(RECOVERY_SCENARIO);
            return EMAIL_VERIFICATION_ON_UPDATE.equals(scenario) || MOBILE_VERIFICATION_ON_UPDATE.equals(scenario);
        }
        return false;
    }

    /**
     * Check if the event is a post user attribute update verification event.
     *
     * @param event Identity event.
     * @return true if it is a post user attribute update verification event, false otherwise.
     */
    private static boolean isPostUserAttributeUpdateVerificationEvent(Event event) {

        String eventName = event.getEventName();
        return POST_EMAIL_CHANGE_VERIFICATION.equals(eventName) || POST_MOBILE_CHANGE_VERIFICATION.equals(eventName);
    }

    /**
     * Check if the event is a post user profile update event.
     *
     * @param event Identity event.
     * @return true if it is a post user profile update event, false otherwise.
     */
    private static boolean isPostUserProfileUpdateEvent(Event event) {

        if (POST_USER_PROFILE_UPDATE.equals(event.getEventName())) {

            if (Boolean.parseBoolean((String) event.getEventProperties().get(IS_PASSWORD_UPDATE_ACTION))) {
                LOG.debug("Ignoring user profile update event as it is a password update action.");
                return false;
            }

            String scenario = (String) event.getEventProperties().get(SCENARIO);
            return POST_USER_PROFILE_UPDATE_BY_ADMIN.equals(scenario)
                    || POST_USER_PROFILE_UPDATE_BY_USER.equals(scenario);
        }
        return false;
    }

    /**
     * Check if the event is an authentication step verification event.
     *
     * @param event Identity event.
     * @return true if it is an authentication step verification event, false otherwise.
     */
    private static boolean isAuthenticationStepVerificationEvent(Event event) {

        String eventName = event.getEventName();
        return POST_GENERATE_EMAIL_OTP.equals(eventName) || POST_VALIDATE_EMAIL_OTP.equals(eventName)
                || POST_GENERATE_SMS_OTP.equals(eventName) || POST_VALIDATE_SMS_OTP.equals(eventName);
    }
}
