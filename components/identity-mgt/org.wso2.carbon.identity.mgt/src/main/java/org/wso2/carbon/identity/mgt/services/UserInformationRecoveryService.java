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

package org.wso2.carbon.identity.mgt.services;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.captcha.mgt.beans.CaptchaInfoBean;
import org.wso2.carbon.captcha.mgt.util.CaptchaUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.base.IdentityConstants;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.IdentityClaimManager;
import org.wso2.carbon.identity.core.model.IdentityEventListenerConfig;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.mgt.ChallengeQuestionProcessor;
import org.wso2.carbon.identity.mgt.IdentityMgtConfig;
import org.wso2.carbon.identity.mgt.IdentityMgtEventListener;
import org.wso2.carbon.identity.mgt.IdentityMgtServiceException;
import org.wso2.carbon.identity.mgt.RecoveryProcessor;
import org.wso2.carbon.identity.mgt.beans.VerificationBean;
import org.wso2.carbon.identity.mgt.constants.IdentityMgtConstants;
import org.wso2.carbon.identity.mgt.dto.ChallengeQuestionDTO;
import org.wso2.carbon.identity.mgt.dto.ChallengeQuestionIdsDTO;
import org.wso2.carbon.identity.mgt.dto.NotificationDataDTO;
import org.wso2.carbon.identity.mgt.dto.UserChallengesCollectionDTO;
import org.wso2.carbon.identity.mgt.dto.UserChallengesDTO;
import org.wso2.carbon.identity.mgt.dto.UserDTO;
import org.wso2.carbon.identity.mgt.dto.UserIdentityClaimDTO;
import org.wso2.carbon.identity.mgt.dto.UserRecoveryDTO;
import org.wso2.carbon.identity.mgt.internal.IdentityMgtServiceComponent;
import org.wso2.carbon.identity.mgt.util.UserIdentityManagementUtil;
import org.wso2.carbon.identity.mgt.util.Utils;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.Permission;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.claim.Claim;
import org.wso2.carbon.user.core.listener.UserOperationEventListener;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.user.mgt.UserMgtConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This service provides the services needed to recover user password and user
 * account information.
 * @deprecated use identity management REST API implementation available in
 * org.wso2.carbon.identity.rest.api.user.recovery.v1.impl.core.UsernameRecoveryService,
 * org.wso2.carbon.identity.rest.api.user.recovery.v1.impl.core.PasswordRecoveryService,
 * org.wso2.carbon.identity.user.endpoint.impl.MeApiServiceImpl instead.
 */
public class UserInformationRecoveryService {

    private static final Log log = LogFactory.getLog(UserInformationRecoveryService.class);

    public CaptchaInfoBean getCaptcha() throws IdentityMgtServiceException {

        if (log.isDebugEnabled()) {
            log.debug("User get captcha image request received");
        }

        try {
            CaptchaUtil.cleanOldCaptchas();
            CaptchaInfoBean bean = CaptchaUtil.generateCaptchaImage();

            if (log.isDebugEnabled()) {
                log.debug("Captcha stored: " + bean.getImagePath());
                log.debug("Captcha generated successfully");
            }

            return bean;

        } catch (Exception e) {
            String errorMessage = "Error while generating captcha";
            log.error(errorMessage, e);
            throw new IdentityMgtServiceException(errorMessage);
        }
    }

    public VerificationBean verifyUser(String username, CaptchaInfoBean captcha)
            throws IdentityMgtServiceException {

        UserDTO userDTO;
        VerificationBean bean;
        if (log.isDebugEnabled()) {
            log.debug("User verification request received with username : " + username);
        }

        if (IdentityMgtConfig.getInstance().isCaptchaVerificationInternallyManaged()) {
            try {
                CaptchaUtil.processCaptchaInfoBean(captcha);
            } catch (Exception e) {
                bean = handleError(VerificationBean.ERROR_CODE_INVALID_CAPTCHA + " Error while validating captcha", e);
                return bean;
            }
        }

        try {
            userDTO = Utils.processUserId(username);
        } catch (IdentityException e) {
            bean = handleError(VerificationBean.ERROR_CODE_INVALID_USER + " invalid user : "
                    + username, e);
            return bean;
        }

        try {
            if (IdentityMgtConfig.getInstance().isSaasEnabled()) {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                carbonContext.setTenantId(userDTO.getTenantId());
                carbonContext.setTenantDomain(userDTO.getTenantDomain());
            }
            RecoveryProcessor processor = IdentityMgtServiceComponent.getRecoveryProcessor();
            bean = processor.verifyUserForRecovery(1, userDTO);
            if (bean.getError() != null) {
                if (bean.getError().contains(VerificationBean.ERROR_CODE_INVALID_USER)) {
                    bean = handleError(VerificationBean.ERROR_CODE_INVALID_USER + " User does not exist : " + username,
                            null);
                } else if (bean.getError().contains(VerificationBean.ERROR_CODE_DISABLED_ACCOUNT)) {
                    bean = handleError(VerificationBean.ERROR_CODE_DISABLED_ACCOUNT +
                            " Account is disabled for user " + username + ". Can not allow to recover.", null);
                } else {
                    bean = handleError(VerificationBean.ERROR_CODE_UNEXPECTED + " Error verifying user : " + username,
                            null);
                }
            }
            return bean;
        } finally {
            if (IdentityMgtConfig.getInstance().isSaasEnabled()) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    public VerificationBean sendRecoveryNotification(String username, String key, String notificationType)
            throws IdentityMgtServiceException {

        return sendNotification(username, key, notificationType,
                IdentityMgtConstants.Notification.PASSWORD_RESET_RECOVERY);
    }

    public VerificationBean resendNotification(String username, String key, String notificationType)
            throws IdentityMgtServiceException {

        return sendNotification(username, key, notificationType, IdentityMgtConstants.Notification.RESEND_NOTIFICATION);
    }

    private VerificationBean sendNotification(String username, String key, String notificationType,
            String notification) {

        UserDTO userDTO = null;
        VerificationBean bean = null;

        if (log.isDebugEnabled()) {
            log.debug("User recovery notification sending request received with username : " + username + " notification" +
                    " type :" + notificationType);
        }
        try {
            userDTO = Utils.processUserId(username);
        } catch (IdentityException e) {
            bean = handleError(VerificationBean.ERROR_CODE_INVALID_USER + " invalid user : " + username, e);
            return bean;
        }

        RecoveryProcessor processor = IdentityMgtServiceComponent.getRecoveryProcessor();

        try {
            if (IdentityMgtConfig.getInstance().isSaasEnabled()) {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                carbonContext.setTenantId(userDTO.getTenantId());
                carbonContext.setTenantDomain(userDTO.getTenantDomain());
            }
            bean = processor.verifyConfirmationCode(1, userDTO.getUserId(), key);

            if (!bean.isVerified()) {
                log.error("Invalid user is trying to recover the password with username : " + username);
                bean = handleError(VerificationBean.ERROR_CODE_INVALID_USER +
                        " Invalid user is trying to recover the password with username : " + username, null);
                return bean;
            }
        } catch (IdentityException e1) {
            bean = UserIdentityManagementUtil.getCustomErrorMessagesToVerifyCode(e1, username);
            if (bean.getError() == null) {
                bean = handleError(VerificationBean.ERROR_CODE_INVALID_CODE + " Invalid confirmation code for user : "
                        + username, e1);
            }
            return bean;
        } finally {
            if (IdentityMgtConfig.getInstance().isSaasEnabled()) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }

        UserRecoveryDTO dto = new UserRecoveryDTO(userDTO);
        dto.setNotification(notification);
        dto.setNotificationType(notificationType);

        NotificationDataDTO dataDTO = null;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Initiating the notification sending process");
            }

            if (IdentityMgtConfig.getInstance().isSaasEnabled()) {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                carbonContext.setTenantId(userDTO.getTenantId());
                carbonContext.setTenantDomain(userDTO.getTenantDomain());
            }

            dataDTO = processor.recoverWithNotification(dto);

//			Send email data only if not internally managed.
            if (!(IdentityMgtConfig.getInstance().isNotificationInternallyManaged())) {
                bean.setNotificationData(dataDTO);
            }


        } catch (IdentityException e) {
            bean = UserIdentityManagementUtil.getCustomErrorMessagesToVerifyCode(e, username);
            if (bean.getError() == null) {
                bean = handleError(VerificationBean.ERROR_CODE_RECOVERY_NOTIFICATION_FAILURE + ": " + VerificationBean.
                        ERROR_CODE_UNEXPECTED + " Error when sending recovery message for " +
                        "user: " + username, e);
            }
            return bean;
        } finally {
            if (IdentityMgtConfig.getInstance().isSaasEnabled()) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return bean;
    }

    /**
     * This method is used to verify the confirmation code sent to user is
     * correct and validates. Before calling this method it needs to supply a
     * Captcha and should call getCaptcha().
     *
     * @param username - username of whom the password needs to be recovered.
     * @param code     - confirmation code sent to user by notification.
     * @param captcha  - generated captcha with answer for this communication.
     * @return - VerificationBean with new code to be used in updatePassword().
     * @throws IdentityMgtServiceException
     */
    public VerificationBean verifyConfirmationCode(String username, String code,
                                                   CaptchaInfoBean captcha) throws IdentityMgtServiceException {

        UserDTO userDTO;
        VerificationBean bean = new VerificationBean();

        if (log.isDebugEnabled()) {
            log.debug("User confirmation code verification request received with username :" + username);
        }
        if (IdentityMgtConfig.getInstance().isCaptchaVerificationInternallyManaged()) {
            try {
                CaptchaUtil.processCaptchaInfoBean(captcha);
            } catch (Exception e) {
                bean = handleError(VerificationBean.ERROR_CODE_INVALID_CODE
                        + " Error while validating captcha for user : " + username, e);
                return bean;
            }
        }

        try {
            userDTO = Utils.processUserId(username);
        } catch (IdentityException e) {
            bean = handleError(VerificationBean.ERROR_CODE_INVALID_USER + " invalid user : "
                    + username, e);
            return bean;
        }

        RecoveryProcessor processor = IdentityMgtServiceComponent.getRecoveryProcessor();

        if (IdentityMgtConfig.getInstance().isSaasEnabled()) {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantId(userDTO.getTenantId());
            carbonContext.setTenantDomain(userDTO.getTenantDomain());
        }

        try {
            bean = processor.verifyConfirmationCode(2, userDTO.getUserId(), code);
            if (bean.isVerified()) {
                bean = processor.updateConfirmationCode(3, userDTO.getUserId(), userDTO.getTenantId());
                if (log.isDebugEnabled()) {
                    log.debug("User confirmation code verification successful for user: " + username);
                }
            } else {
                bean.setVerified(false);
                bean.setKey("");
                log.error(bean.getError());
            }
        } catch (IdentityException e) {
            bean = UserIdentityManagementUtil.getCustomErrorMessagesToVerifyCode(e, username);
            if (bean.getError() == null) {
                bean = handleError(VerificationBean.ERROR_CODE_INVALID_CODE + " Error verifying confirmation code for " +
                        "user : " + username, e);
            }
            return bean;
        } finally {
            if (IdentityMgtConfig.getInstance().isSaasEnabled()) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }

        return bean;

    }

    /**
     * This method is used to update the password in the system for password
     * recovery process. Before calling this method caller needs to call
     * verifyConfirmationCode and get the newly generated confirmation code.
     *
     * @param username         - username
     * @param confirmationCode - newly generated confirmation code
     * @param newPassword      - new password
     * @return - VerificationBean with operation status true or false.
     * @throws IdentityMgtServiceException
     */
    public VerificationBean updatePassword(String username, String confirmationCode,
                                           String newPassword) throws IdentityMgtServiceException {

        RecoveryProcessor recoveryProcessor = IdentityMgtServiceComponent.getRecoveryProcessor();
        VerificationBean bean = null;

        if (log.isDebugEnabled()) {
            log.debug("User update password request received with username: " + username);
        }

        try {
            UserDTO userDTO = Utils.processUserId(username);

            if (IdentityMgtConfig.getInstance().isSaasEnabled()) {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                carbonContext.setTenantId(userDTO.getTenantId());
                carbonContext.setTenantDomain(userDTO.getTenantDomain());
             }

            TenantManager tenantManager = IdentityMgtServiceComponent.getRealmService().getTenantManager();
            int tenantId = 0;
            try {
                tenantId = tenantManager.getTenantId(userDTO.getTenantDomain());
            } catch (UserStoreException e) {
                log.warn("No Tenant id for tenant domain " + userDTO.getTenantDomain());
                return handleError(VerificationBean.ERROR_CODE_INVALID_TENANT + " No Tenant id for tenant domain : " +
                        userDTO.getTenantDomain(), e);
            }

            if (recoveryProcessor.verifyConfirmationCode(30, userDTO.getUserId(), confirmationCode).isVerified()) {
                Utils.updatePassword(userDTO.getUserId(), tenantId, newPassword);
                log.info("Credential is updated for user : " + userDTO.getUserId()
                        + " and tenant domain : " + userDTO.getTenantDomain());
                IdentityMgtConfig.getInstance().getRecoveryDataStore().invalidate(userDTO.getUserId(), tenantId);
                bean = new VerificationBean(true);
            } else if (recoveryProcessor.verifyConfirmationCode(3, userDTO.getUserId(), confirmationCode).isVerified()) {
                Utils.updatePassword(userDTO.getUserId(), tenantId, newPassword);
                log.info("Credential is updated for user : " + userDTO.getUserId()
                        + " and tenant domain : " + userDTO.getTenantDomain());
                IdentityMgtConfig.getInstance().getRecoveryDataStore().invalidate(userDTO.getUserId(), tenantId);
                bean = new VerificationBean(true);
            } else {
                String msg = "Invalid user tried to update credential with user Id : "
                        + userDTO.getUserId() + " and tenant domain : " + userDTO.getTenantDomain();
                bean = new VerificationBean(VerificationBean.ERROR_CODE_INVALID_USER + " " + msg);
                bean.setVerified(false);
                log.error(msg);
            }

        } catch (IdentityException e) {
            bean = UserIdentityManagementUtil.getCustomErrorMessagesToVerifyCode(e, username);
            if (bean.getError() == null) {
                bean = handleError(VerificationBean.ERROR_CODE_UNEXPECTED + " Error while updating credential " +
                        "for user: " + username, e);
            }
            return bean;
        } finally {
            if (IdentityMgtConfig.getInstance().isSaasEnabled()) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return bean;
    }

    public ChallengeQuestionIdsDTO getUserChallengeQuestionIds(String username, String confirmation)
            throws IdentityMgtServiceException {

        UserDTO userDTO = null;
        ChallengeQuestionIdsDTO idsDTO = new ChallengeQuestionIdsDTO();

        if (log.isDebugEnabled()) {
            log.debug("User challenge questions id request received with username: " + username);
        }
        try {
            userDTO = Utils.processUserId(username);
        } catch (IdentityException e) {
            idsDTO = handleChallengeIdError(VerificationBean.ERROR_CODE_INVALID_USER + " Error validating user : " +
                    username, e);
            return idsDTO;
        }

        try {
            if (IdentityMgtConfig.getInstance().isSaasEnabled()) {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                carbonContext.setTenantId(userDTO.getTenantId());
                carbonContext.setTenantDomain(userDTO.getTenantDomain());
            }
            RecoveryProcessor processor = IdentityMgtServiceComponent.getRecoveryProcessor();
            VerificationBean bean = null;
            try {
                bean = processor.verifyConfirmationCode(1, userDTO.getUserId(), confirmation);
                if (bean.isVerified()) {
                    bean = processor.updateConfirmationCode(20, userDTO.getUserId(), userDTO.getTenantId());
                } else {
                    bean.setVerified(false);
                }
            } catch (IdentityException e1) {
                idsDTO = UserIdentityManagementUtil.getCustomErrorMessagesForChallengeQuestionIds(e1, username);
                if (idsDTO == null) {
                    idsDTO = handleChallengeIdError(VerificationBean.ERROR_CODE_UNEXPECTED + " Error when validating " +
                            "code", e1);
                }
                return idsDTO;
            }
            if (bean.isVerified()) {
                try {
                    idsDTO = processor.getQuestionProcessor().getUserChallengeQuestionIds(userDTO.getUserId(), userDTO.getTenantId());
                    idsDTO.setKey(bean.getKey());
                    if (log.isDebugEnabled()) {
                        log.debug("User challenge question response successful for user: " + username);
                    }
                } catch (Exception e) {
                    idsDTO = handleChallengeIdError(VerificationBean.ERROR_CODE_CHALLENGE_QUESTION_NOT_FOUND +
                            " Error when getting user challenge questions for user : " + username, e);
                    return idsDTO;
                }
            } else {
                String msg = "Verification failed for user. Error : " + bean.getError();
                log.error(msg);
                idsDTO.setError(VerificationBean.ERROR_CODE_UNEXPECTED + " " + msg);
                idsDTO.setKey("");
            }
        }finally {
            if (IdentityMgtConfig.getInstance().isSaasEnabled()) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }

        return idsDTO;

    }

    /**
     * To get the challenge question for the user.
     *
     * @param userName
     * @param confirmation
     * @param questionId   - Question id returned from the getUserChanllegneQuestionIds
     *                     method.
     * @return Populated question bean with the question details and the key.
     * @throws IdentityMgtServiceException
     */
    public UserChallengesDTO getUserChallengeQuestion(String userName, String confirmation,
                                                      String questionId) throws IdentityMgtServiceException {

        UserDTO userDTO = null;
        UserChallengesDTO userChallengesDTO = new UserChallengesDTO();

        if (log.isDebugEnabled()) {
            log.debug("User challenge question request received with username :" + userName);
        }

        try {
            userDTO = Utils.processUserId(userName);
        } catch (IdentityException e) {
            return handleChallengesError(VerificationBean.ERROR_CODE_INVALID_USER + " Error validating user : " +
                    userName, null);
        }

        try {
            if (IdentityMgtConfig.getInstance().isSaasEnabled()) {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                carbonContext.setTenantId(userDTO.getTenantId());
                carbonContext.setTenantDomain(userDTO.getTenantDomain());
            }
            RecoveryProcessor processor = IdentityMgtServiceComponent.getRecoveryProcessor();

            VerificationBean bean;
            try {
                bean = processor.verifyConfirmationCode(20, userDTO.getUserId(), confirmation);
                if (bean.isVerified()) {
                    bean = processor.updateConfirmationCode(40, userDTO.getUserId(), userDTO.getTenantId());
                } else if (processor.verifyConfirmationCode(30, userDTO.getUserId(), confirmation).isVerified()) {
                    bean = processor.updateConfirmationCode(40, userDTO.getUserId(), userDTO.getTenantId());
                } else {
                    bean.setVerified(false);
                }
            } catch (IdentityException e) {
                userChallengesDTO = UserIdentityManagementUtil.getCustomErrorMessagesForChallengQuestions(e, userName);
                if (userChallengesDTO == null) {
                    userChallengesDTO = handleChallengesError(VerificationBean.ERROR_CODE_INVALID_CODE +
                            " Invalid confirmation code for user : " + userName, e);
                }
                return userChallengesDTO;
            }

            if (bean.isVerified()) {
                userChallengesDTO = processor.getQuestionProcessor().getUserChallengeQuestion(
                        userDTO.getUserId(), userDTO.getTenantId(), questionId);
                userChallengesDTO.setKey(bean.getKey());
                userChallengesDTO.setVerfied(true);
                if (log.isDebugEnabled()) {
                    log.debug("User challenge question retrieved successfully");
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Verification failed for user. Error : " + bean.getError());
                }
                userChallengesDTO.setError(VerificationBean.ERROR_CODE_INVALID_USER + " " + bean.getError());
            }
        } finally {
            if (IdentityMgtConfig.getInstance().isSaasEnabled()) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }

        return userChallengesDTO;
    }

    /**
     * Returns all the challenge questions configured for the user.
     *
     * @param userName     username of the user
     * @param confirmation confirmation code
     * @return an instance of UserChallengesCollectionDTO which holds the challenge questions and status
     * @throws IdentityMgtServiceException
     */
    public UserChallengesCollectionDTO getUserChallengeQuestions(String userName, String confirmation)
            throws IdentityMgtServiceException {

        UserDTO userDTO = null;
        UserChallengesCollectionDTO userChallengesCollectionDTO = new UserChallengesCollectionDTO();

        if (log.isDebugEnabled()) {
            log.debug("User challenge question request received with username :" + userName);
        }

        try {
            userDTO = Utils.processUserId(userName);
        } catch (IdentityException e) {
            log.error("Error while validating user " + userName, e);
            return UserIdentityManagementUtil.handleChallengeQuestionSetError(
                    VerificationBean.ERROR_CODE_INVALID_USER + " Error validating user : " + userName, null);
        }

        try {
            if (IdentityMgtConfig.getInstance().isSaasEnabled()) {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                carbonContext.setTenantId(userDTO.getTenantId());
                carbonContext.setTenantDomain(userDTO.getTenantDomain());
            }

            RecoveryProcessor processor = IdentityMgtServiceComponent.getRecoveryProcessor();

            VerificationBean bean;
            try {
                bean = processor.verifyConfirmationCode(1, userDTO.getUserId(), confirmation);
                if (bean.isVerified()) {
                    bean = processor.updateConfirmationCode(20, userDTO.getUserId(), userDTO.getTenantId());
                } else {
                    bean.setVerified(false);
                }
            } catch (IdentityException e) {
                log.error("Error while verifying confirmation code.", e);
                return UserIdentityManagementUtil.getCustomErrorMessagesForChallengeQuestionSet(e, userName);
            }

            if (bean.isVerified()) {
                UserChallengesDTO[] userChallengesDTOs = null;
                try {
                    userChallengesDTOs = processor.getQuestionProcessor().getUserChallengeQuestions(
                            userDTO.getUserId(), userDTO.getTenantId());
                    userChallengesCollectionDTO.setKey(bean.getKey());
                    userChallengesCollectionDTO.setUserChallengesDTOs(userChallengesDTOs);
                } catch (IdentityException e) {
                    log.error("Error while retrieving challenge questions of the user " + userName, e);
                    return UserIdentityManagementUtil.handleChallengeQuestionSetError(
                            VerificationBean.ERROR_CODE_CHALLENGE_QUESTION_NOT_FOUND + " No associated challenge " +
                            "questions found for the user : " + userName, null);
                }

                if (log.isDebugEnabled()) {
                    log.debug("User challenge questions retrieved successfully");
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Verification failed for user. Error : " + bean.getError());
                }
                userChallengesCollectionDTO.setError(VerificationBean.ERROR_CODE_INVALID_USER + " " + bean.getError());
            }
        } finally {
            if (IdentityMgtConfig.getInstance().isSaasEnabled()) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }

        return userChallengesCollectionDTO;
    }

    /**
     * This method is to verify the user supplied answer for the challenge
     * question.
     *
     * @param userName
     * @param confirmation
     * @param questionId
     * @param answer
     * @return status and key details about the operation status.
     * @throws IdentityMgtServiceException
     */
    public VerificationBean verifyUserChallengeAnswer(String userName, String confirmation,
                                                      String questionId, String answer) throws IdentityMgtServiceException {

        VerificationBean bean = new VerificationBean();
        bean.setVerified(false);

        if (log.isDebugEnabled()) {
            log.debug("User challenge answer request received with username :" + userName);
        }

        if (questionId == null || answer == null) {
            String error = "No challenge question id provided for verification";
            bean.setError(error);
            if (log.isDebugEnabled()) {
                log.debug(error);
            }

            return bean;
        }

        UserDTO userDTO = null;
        try {
            userDTO = Utils.processUserId(userName);
        } catch (IdentityException e) {
            bean = handleError(VerificationBean.ERROR_CODE_INVALID_USER + " Error verifying user: " + userName, e);
            return bean;
        }

        try {
            if (IdentityMgtConfig.getInstance().isSaasEnabled()) {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                carbonContext.setTenantId(userDTO.getTenantId());
                carbonContext.setTenantDomain(userDTO.getTenantDomain());
            }

            RecoveryProcessor recoveryProcessor = IdentityMgtServiceComponent.getRecoveryProcessor();

            try {
                bean = recoveryProcessor.verifyConfirmationCode(40, userDTO.getUserId(), confirmation);
                if (bean.isVerified()) {
                    bean = recoveryProcessor.updateConfirmationCode(30, userDTO.getUserId(), userDTO.getTenantId());
                } else {
                    bean.setVerified(false);
                }
            } catch (IdentityException e) {
                bean = UserIdentityManagementUtil.getCustomErrorMessagesToVerifyCode(e, userName);
                if (bean == null) {
                    bean = handleError(VerificationBean.ERROR_CODE_INVALID_CODE + " " +
                            " Error verifying confirmation code for user : " + userName, e);
                }
                return bean;
            }

            ChallengeQuestionProcessor processor = recoveryProcessor.getQuestionProcessor();

            UserChallengesDTO userChallengesDTO = new UserChallengesDTO();
            userChallengesDTO.setId(questionId);
            userChallengesDTO.setAnswer(answer);

            boolean verification = processor.verifyUserChallengeAnswer(userDTO.getUserId(), userDTO.getTenantId(),
                    userChallengesDTO);

            if (verification) {
                bean.setError("");
                bean.setUserId(userName);
                if (log.isDebugEnabled()) {
                    log.debug("User answer verification successful for user: " + userName);
                }
            } else {
                bean.setError("Challenge answer verification failed for user : " + userName);
                bean.setVerified(false);
                bean.setKey(""); // clear the key to avoid returning to caller.
                log.error(bean.getError());
            }
        } finally {
            if (IdentityMgtConfig.getInstance().isSaasEnabled()) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return bean;
    }

    /**
     * Verifies challenge question answers.
     *
     * @param userName username of the user
     * @param confirmation confirmation code UserChallengesDTO instances which holds the question id and answer
     * @param userChallengesDTOs an array of
     * @return an instance of VerificationBean which denote the status
     * @throws IdentityMgtServiceException
     */
    public VerificationBean verifyUserChallengeAnswers(String userName, String confirmation, UserChallengesDTO[] userChallengesDTOs) throws IdentityMgtServiceException {

        VerificationBean bean = new VerificationBean();
        bean.setVerified(false);

        if (log.isDebugEnabled()) {
            log.debug("User challenge answers request received with username :" + userName);
        }

        if (ArrayUtils.isEmpty(userChallengesDTOs)) {
            String errorMsg = "No challenge question id provided for verification";
            bean.setError(errorMsg);
            if (log.isDebugEnabled()) {
                log.debug(errorMsg);
            }

            return bean;
        }

        UserDTO userDTO;
        try {
            userDTO = Utils.processUserId(userName);
        } catch (IdentityException e) {
            bean = handleError(VerificationBean.ERROR_CODE_INVALID_USER + " Error verifying user: " + userName, e);
            return bean;
        }

        try {
            if (IdentityMgtConfig.getInstance().isSaasEnabled()) {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                carbonContext.setTenantId(userDTO.getTenantId());
                carbonContext.setTenantDomain(userDTO.getTenantDomain());
            }

            RecoveryProcessor recoveryProcessor = IdentityMgtServiceComponent.getRecoveryProcessor();

            try {
                bean = recoveryProcessor.verifyConfirmationCode(20, userDTO.getUserId(), confirmation);
                if (bean.isVerified()) {
                    bean = recoveryProcessor.updateConfirmationCode(30, userDTO.getUserId(), userDTO.getTenantId());
                } else {
                    bean.setVerified(false);
                }
            } catch (IdentityException e) {
                log.error("Error while verifying confirmation code.", e);
                bean = UserIdentityManagementUtil.getCustomErrorMessagesToVerifyCode(e, userName);
                if (bean == null) {
                    bean = handleError(VerificationBean.ERROR_CODE_INVALID_CODE + " " +
                                       " Error verifying confirmation code for user : " + userName, e);
                }
                return bean;
            }

            ChallengeQuestionProcessor processor = recoveryProcessor.getQuestionProcessor();
            boolean verification = processor.verifyUserChallengeAnswers(userDTO.getUserId(), userDTO.getTenantId(),
                                                                        userChallengesDTOs);

            if (verification) {
                bean.setError("");
                bean.setUserId(userName);
                if (log.isDebugEnabled()) {
                    log.debug("User answer verification successful for user: " + userName);
                }
            } else {
                bean.setError("Verification failed for one or more answers provided by user : " + userName);
                bean.setVerified(false);
                bean.setKey(""); // clear the key to avoid returning to caller.
                if (log.isDebugEnabled()) {
                    log.debug(bean.getError());
                }
            }
        } finally {
            if (IdentityMgtConfig.getInstance().isSaasEnabled()) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return bean;
    }

    /**
     * Get all challenge questions
     *
     * @return array of questions
     * @throws IdentityMgtServiceException if fails
     */
    public ChallengeQuestionDTO[] getAllChallengeQuestions() throws IdentityMgtServiceException {

        ChallengeQuestionProcessor processor = IdentityMgtServiceComponent.
                getRecoveryProcessor().getQuestionProcessor();
        List<ChallengeQuestionDTO> questionDTOs = null;
        try {
            questionDTOs = processor.getAllChallengeQuestions();
        } catch (IdentityException e) {
            log.error("Error while loading user challenges", e);
            throw new IdentityMgtServiceException("Error while loading user challenges");
        }
        return questionDTOs.toArray(new ChallengeQuestionDTO[questionDTOs.size()]);

    }

    /**
     * This returns the user supported claims.
     *
     * @param dialect
     * @return
     * @throws IdentityException
     */
    public UserIdentityClaimDTO[] getUserIdentitySupportedClaims(String dialect)
            throws IdentityException {
        IdentityClaimManager claimManager = null;
        Claim[] claims = null;
        UserRealm realm = null;

        claimManager = IdentityClaimManager.getInstance();
        realm = IdentityTenantUtil.getRealm(null, null);
        claims = claimManager.getAllSupportedClaims(dialect, realm);

        if (claims == null || claims.length == 0) {
            log.warn("Could not find any matching claims for requested dialect : " + dialect);
            return new UserIdentityClaimDTO[0];
        }

        List<UserIdentityClaimDTO> claimList = new ArrayList<UserIdentityClaimDTO>();

        for (int i = 0; i < claims.length; i++) {
            if (claims[i].getDisplayTag() != null
                    && !IdentityConstants.PPID_DISPLAY_VALUE.equals(claims[i].getDisplayTag())) {
                if (UserCoreConstants.ClaimTypeURIs.ACCOUNT_STATUS.equals(claims[i].getClaimUri())) {
                    continue;
                }
                if (claims[i].isSupportedByDefault() && (!claims[i].isReadOnly())) {

                    UserIdentityClaimDTO claimDto = new UserIdentityClaimDTO();
                    claimDto.setClaimUri(claims[i].getClaimUri());
                    claimDto.setClaimValue(claims[i].getValue());
                    claimDto.setRequired(claims[i].isRequired());
                    claimDto.setDisplayName(claims[i].getDisplayTag());
                    claimList.add(claimDto);
                }
            }
        }

        return claimList.toArray(new UserIdentityClaimDTO[claimList.size()]);
    }

    /**
     * Verifies the user against the provided claims and captcha information.
     *
     * @param claims
     * @param captcha
     * @param tenantDomain
     * @return
     * @throws IdentityMgtServiceException
     */
    public VerificationBean verifyAccount(UserIdentityClaimDTO[] claims, CaptchaInfoBean captcha,
                                          String tenantDomain) throws IdentityMgtServiceException {

        VerificationBean vBean = new VerificationBean();

        if (IdentityMgtConfig.getInstance().isCaptchaVerificationInternallyManaged()) {
            try {
                CaptchaUtil.processCaptchaInfoBean(captcha);
            } catch (Exception e) {
                vBean = handleError(VerificationBean.ERROR_CODE_INVALID_CAPTCHA
                        + " Error processing captcha", e);
                return vBean;
            }
        }

        if (!IdentityMgtConfig.getInstance().isSaasEnabled()) {
            String loggedInTenant = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            if (tenantDomain != null && !tenantDomain.isEmpty() && !loggedInTenant.equals(tenantDomain)) {
                String msg = "Trying to verify account unauthorized tenant space";
                log.error(msg);
                throw new IdentityMgtServiceException(msg);
            }
            if (tenantDomain == null || tenantDomain.isEmpty()) {
                tenantDomain = loggedInTenant;
            }
        }

        try {
            int tenantId = Utils.getTenantId(tenantDomain);
            String userName = UserIdentityManagementUtil.getUsernameByClaims(claims, tenantId);

            if (userName != null) {
                UserDTO userDTO = new UserDTO(UserCoreUtil.addTenantDomainToEntry(userName, tenantDomain));
                userDTO.setTenantId(tenantId);

                UserRecoveryDTO dto = new UserRecoveryDTO(userDTO);
                dto.setNotification(IdentityMgtConstants.Notification.ACCOUNT_ID_RECOVERY);
                dto.setNotificationType("EMAIL");

                RecoveryProcessor processor = IdentityMgtServiceComponent.getRecoveryProcessor();
                NotificationDataDTO notificationDto = processor.notifyWithEmail(dto);

                vBean.setVerified(notificationDto.isNotificationSent());

//				Send email data only if not internally managed.
                if (!(IdentityMgtConfig.getInstance().isNotificationInternallyManaged())) {
                    vBean.setNotificationData(notificationDto);
                }

            } else {
                vBean.setError("User not found");
                vBean.setVerified(false);
            }
        } catch (Exception e) {
            vBean = handleError(VerificationBean.ERROR_CODE_INVALID_USER
                    + " Error verifying user account", e);
            return vBean;
        }

        return vBean;
    }

    /**
     * This method is used to register an user in the system. The account will be locked if the
     * Authentication.Policy.Account.Lock.On.Creation is set to true. Else user will be able to
     * login after registration.
     *
     * @param userName
     * @param password
     * @param claims
     * @param profileName
     * @param tenantDomain
     * @return
     * @throws IdentityMgtServiceException
     */
    public VerificationBean registerUser(String userName, String password,
                                         UserIdentityClaimDTO[] claims, String profileName, String tenantDomain)
            throws IdentityMgtServiceException {

        VerificationBean vBean = new VerificationBean();

        org.wso2.carbon.user.core.UserStoreManager userStoreManager = null;
        Permission permission = null;

        if (!IdentityMgtConfig.getInstance().isSaasEnabled()) {
            String loggedInTenant = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            if (tenantDomain != null && !tenantDomain.isEmpty() && !loggedInTenant.equals(tenantDomain)) {
                String msg = "Trying to create users in unauthorized tenant space";
                log.error(msg);
                throw new IdentityMgtServiceException(msg);
            }
            if (tenantDomain == null || tenantDomain.isEmpty()) {
                tenantDomain = loggedInTenant;
            }
        }

        RealmService realmService = IdentityMgtServiceComponent.getRealmService();
        int tenantId;

        try {

            tenantId = Utils.getTenantId(tenantDomain);
            if (realmService.getTenantUserRealm(tenantId) != null) {
                userStoreManager = (org.wso2.carbon.user.core.UserStoreManager) realmService
                        .getTenantUserRealm(tenantId).getUserStoreManager();
            }

        } catch (Exception e) {
            vBean = handleError(VerificationBean.ERROR_CODE_UNEXPECTED
                    + " Error retrieving the user store manager for the tenant", e);
            return vBean;
        }

        try {

            if (IdentityMgtConfig.getInstance().isSaasEnabled()) {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                carbonContext.setTenantId(tenantId);
                carbonContext.setTenantDomain(tenantDomain);
            }

            if (userStoreManager == null) {
                vBean = new VerificationBean();
                vBean.setVerified(false);
                vBean.setError(VerificationBean.ERROR_CODE_UNEXPECTED
                        + " Error retrieving the user store manager for the tenant");
                return vBean;
            }

            Map<String, String> claimsMap = new HashMap<String, String>();
            for (UserIdentityClaimDTO userIdentityClaimDTO : claims) {
                claimsMap.put(userIdentityClaimDTO.getClaimUri(),
                        userIdentityClaimDTO.getClaimValue());
            }

            userStoreManager.addUser(userName, password, null, claimsMap, profileName);

            String identityRoleName = UserCoreConstants.INTERNAL_DOMAIN
                    + CarbonConstants.DOMAIN_SEPARATOR + IdentityConstants.IDENTITY_DEFAULT_ROLE;

            if (!userStoreManager.isExistingRole(identityRoleName, false)) {
                permission = new Permission("/permission/admin/login",
                        UserMgtConstants.EXECUTE_ACTION);
                userStoreManager.addRole(identityRoleName, new String[]{userName},
                        new Permission[]{permission}, false);
            } else {
                userStoreManager.updateUserListOfRole(identityRoleName, new String[]{},
                        new String[]{userName});
            }
            String listenerClassName = IdentityMgtConfig.getInstance().getProperty
                    (IdentityMgtConstants.PropertyConfig.IDENTITY_MGT_LISTENER_CLASS);
            if (StringUtils.isBlank(listenerClassName)) {
                listenerClassName = IdentityMgtEventListener.class.getName();
            }
            IdentityEventListenerConfig identityEventListenerConfig = IdentityUtil.readEventListenerProperty
                    (UserOperationEventListener.class.getName(), listenerClassName);

            boolean isListenerEnable = true;

            if (identityEventListenerConfig != null) {
                if (StringUtils.isNotBlank(identityEventListenerConfig.getEnable())) {
                    isListenerEnable = Boolean.parseBoolean(identityEventListenerConfig.getEnable());
                }
            }

            IdentityMgtConfig config = IdentityMgtConfig.getInstance();

            if (isListenerEnable && config.isAuthPolicyAccountLockOnCreation()) {
                UserDTO userDTO = new UserDTO(UserCoreUtil.addTenantDomainToEntry(userName, tenantDomain));
                userDTO.setTenantId(tenantId);

                UserRecoveryDTO dto = new UserRecoveryDTO(userDTO);
                dto.setNotification(IdentityMgtConstants.Notification.ACCOUNT_CONFORM);
                dto.setNotificationType("EMAIL");

                RecoveryProcessor processor = IdentityMgtServiceComponent.getRecoveryProcessor();
                vBean = processor.updateConfirmationCode(1, userName, tenantId);

                dto.setConfirmationCode(vBean.getKey());
                NotificationDataDTO notificationDto = processor.notifyWithEmail(dto);
                vBean.setVerified(notificationDto.isNotificationSent());

//				Send email data only if not internally managed.
                if (!(IdentityMgtConfig.getInstance().isNotificationInternallyManaged())) {
                    vBean.setNotificationData(notificationDto);
                }

            } else {
                vBean.setVerified(true);
            }
        } catch (UserStoreException | IdentityException e) {
            vBean = UserIdentityManagementUtil.getCustomErrorMessagesWhenRegistering(e, userName);
            //Rollback if user exists
            try {
                if (!e.getMessage().contains(IdentityCoreConstants.EXISTING_USER) && userStoreManager.isExistingUser(userName)) {
                    userStoreManager.deleteUser(userName);
                }
            } catch (UserStoreException e1) {
                vBean = UserIdentityManagementUtil.getCustomErrorMessagesWhenRegistering(e1, userName);
            }

            return vBean;
        } finally {
            if (IdentityMgtConfig.getInstance().isSaasEnabled()) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }

        return vBean;
    }


    /**
     * This method is used to resend selef sign up confiration code when user is not recieved email properly
     *
     * @param userName
     * @param code
     * @param profileName
     * @param tenantDomain
     * @return
     * @throws IdentityMgtServiceException
     */
    public VerificationBean resendSignUpConfiramtionCode(String userName, String code, String profileName, String
            tenantDomain)
            throws IdentityMgtServiceException {

        VerificationBean vBean = new VerificationBean();
        RecoveryProcessor processor = IdentityMgtServiceComponent.getRecoveryProcessor();

        if (!IdentityMgtConfig.getInstance().isSaasEnabled()) {
            String loggedInTenant = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            if (tenantDomain != null && !tenantDomain.isEmpty() && !loggedInTenant.equals(tenantDomain)) {
                String msg = "Trying to resend self sign up code  in unauthorized tenant space";
                log.error(msg);
                throw new IdentityMgtServiceException(msg);
            }
            if (tenantDomain == null || tenantDomain.isEmpty()) {
                tenantDomain = loggedInTenant;
            }
        }

        int tenantId;

        try {
            tenantId = Utils.getTenantId(tenantDomain);
        } catch (IdentityException e) {
            vBean = handleError(VerificationBean.ERROR_CODE_UNEXPECTED
                    + " Error while resending confirmation code", e);
            return vBean;
        }

        try {

            if (IdentityMgtConfig.getInstance().isSaasEnabled()) {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                carbonContext.setTenantId(tenantId);
                carbonContext.setTenantDomain(tenantDomain);
            }

            try {
                vBean = processor.verifyConfirmationCode(1, userName, code);
                if (!vBean.isVerified()) {
                    vBean.setError(VerificationBean.ERROR_CODE_INVALID_CODE);
                    return vBean;
                }
            } catch (IdentityException e) {
                vBean = handleError("Error while validating confirmation code for user : " + userName, e);
                return vBean;
            }

            try {

                String listenerClassName = IdentityMgtConfig.getInstance().getProperty
                        (IdentityMgtConstants.PropertyConfig.IDENTITY_MGT_LISTENER_CLASS);
                if (StringUtils.isBlank(listenerClassName)) {
                    listenerClassName = IdentityMgtEventListener.class.getName();
                }

                IdentityEventListenerConfig identityEventListenerConfig = IdentityUtil.readEventListenerProperty
                        (UserOperationEventListener.class.getName(), listenerClassName);

                boolean isListenerEnable = true;

                if (identityEventListenerConfig != null) {
                    if (StringUtils.isNotBlank(identityEventListenerConfig.getEnable())) {
                        isListenerEnable = Boolean.parseBoolean(identityEventListenerConfig.getEnable());
                    }
                }

                IdentityMgtConfig config = IdentityMgtConfig.getInstance();

                if (isListenerEnable && config.isAuthPolicyAccountLockOnCreation()) {
                    UserDTO userDTO = new UserDTO(UserCoreUtil.addTenantDomainToEntry(userName, tenantDomain));
                    userDTO.setTenantId(tenantId);

                    UserRecoveryDTO dto = new UserRecoveryDTO(userDTO);
                    dto.setNotification(IdentityMgtConstants.Notification.ACCOUNT_CONFORM);
                    dto.setNotificationType("EMAIL");

                    vBean = processor.updateConfirmationCode(1, userName, tenantId);

                    dto.setConfirmationCode(vBean.getKey());
                    NotificationDataDTO notificationDto = processor.notifyWithEmail(dto);
                    vBean.setVerified(notificationDto.isNotificationSent());

//				Send email data only if not internally managed.
                    if (!(IdentityMgtConfig.getInstance().isNotificationInternallyManaged())) {
                        vBean.setNotificationData(notificationDto);
                    }

                } else {
                    vBean.setVerified(true);
                }
            } catch (IdentityException e) {
                vBean = UserIdentityManagementUtil.getCustomErrorMessagesWhenRegistering(e, userName);
                return vBean;
            }
        } finally {
            if (IdentityMgtConfig.getInstance().isSaasEnabled()) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }

        return vBean;
    }

    /**
     * This method used to confirm the self registered user account and unlock it.
     *
     * @param username
     * @param code
     * @param captcha
     * @param tenantDomain
     * @return
     * @throws IdentityMgtServiceException
     */
    public VerificationBean confirmUserSelfRegistration(String username, String code,
                                                        CaptchaInfoBean captcha, String tenantDomain) throws IdentityMgtServiceException {

        VerificationBean bean = new VerificationBean();

        if (log.isDebugEnabled()) {
            log.debug("User registration verification request received with username :" + username);
        }
        if (IdentityMgtConfig.getInstance().isCaptchaVerificationInternallyManaged()) {
            try {
                CaptchaUtil.processCaptchaInfoBean(captcha);
            } catch (Exception e) {
                bean = handleError(VerificationBean.ERROR_CODE_INVALID_CAPTCHA
                        + " Error while validating captcha for user : " + username, e);
                return bean;
            }
        }

        if (!IdentityMgtConfig.getInstance().isSaasEnabled()) {
            String loggedInTenant = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            if (tenantDomain != null && !tenantDomain.isEmpty() && !loggedInTenant.equals(tenantDomain)) {
                String msg = "Trying to confirm users in unauthorized tenant space";
                log.error(msg);
                return handleError(VerificationBean.ERROR_CODE_INVALID_TENANT + " " + msg ,null);
            }
            if (tenantDomain == null || tenantDomain.isEmpty()) {
                tenantDomain = loggedInTenant;
            }
        }

        UserDTO userDTO = null;
        try {
            userDTO = Utils.processUserId(username + "@" + tenantDomain);

        } catch (IdentityException e) {
            bean = handleError(VerificationBean.ERROR_CODE_INVALID_USER
                    + " Error verifying user account for user : " + username, e);
            return bean;
        }

        try {
            if (IdentityMgtConfig.getInstance().isSaasEnabled()) {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                carbonContext.setTenantId(userDTO.getTenantId());
                carbonContext.setTenantDomain(userDTO.getTenantDomain());
            }

            RecoveryProcessor processor = IdentityMgtServiceComponent.getRecoveryProcessor();

            org.wso2.carbon.user.core.UserStoreManager userStoreManager = null;

            RealmService realmService = IdentityMgtServiceComponent.getRealmService();
            int tenantId;

            try {

                tenantId = Utils.getTenantId(tenantDomain);
                if (realmService.getTenantUserRealm(tenantId) != null) {
                    userStoreManager = (org.wso2.carbon.user.core.UserStoreManager) realmService
                            .getTenantUserRealm(tenantId).getUserStoreManager();

                    if (username != null && username.contains(UserCoreConstants.DOMAIN_SEPARATOR)) {
                        userStoreManager = userStoreManager.getSecondaryUserStoreManager(Utils.getUserStoreDomainName
                                (username));
                    }
                }

            } catch (Exception e) {
                bean = handleError(VerificationBean.ERROR_CODE_UNEXPECTED + "Error retrieving the user store manager" +
                        " for the tenant : " + tenantDomain, e);
                return bean;
            }

            try {
                bean = processor.verifyConfirmationCode(1, username, code);
                if (bean.isVerified()) {
                    UserIdentityManagementUtil.unlockUserAccount(username, userStoreManager);
                    bean.setVerified(true);

                } else {
                    bean.setVerified(false);
                    bean.setKey("");
                    log.error("User verification failed against the given confirmation code");
                }
            } catch (IdentityException e) {
                bean = UserIdentityManagementUtil.getCustomErrorMessagesToVerifyCode(e, username);
                if (bean.getError() == null) {
                    bean = handleError("Error while validating confirmation code for user : " + username, e);
                }
                return bean;
            }
        } finally {
            if (IdentityMgtConfig.getInstance().isSaasEnabled()) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return bean;
    }

    private VerificationBean handleError(String error, Exception e) {

        VerificationBean bean = new VerificationBean();
        bean.setVerified(false);

        if (error != null) {
            bean.setError(error);
            log.error(error, e);
        } else {
            bean.setError(e.getMessage());
            log.error(e.getMessage(), e);
        }

        return bean;
    }

    private UserChallengesDTO handleChallengesError(String error, Exception e) {

        UserChallengesDTO bean = new UserChallengesDTO();

        if (error != null) {
            bean.setError(error);
            log.error(error, e);
        } else {
            bean.setError(e.getMessage());
            log.error(e.getMessage(), e);
        }

        return bean;
    }

    private ChallengeQuestionIdsDTO handleChallengeIdError(String error, Exception e) {

        ChallengeQuestionIdsDTO bean = new ChallengeQuestionIdsDTO();

        if (error != null) {
            bean.setError(error);
            log.error(error, e);
        } else {
            bean.setError(e.getMessage());
            log.error(e.getMessage(), e);
        }

        return bean;
    }

}
