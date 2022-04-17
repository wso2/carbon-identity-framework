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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.mgt.ChallengeQuestionProcessor;
import org.wso2.carbon.identity.mgt.IdentityMgtConfig;
import org.wso2.carbon.identity.mgt.IdentityMgtServiceException;
import org.wso2.carbon.identity.mgt.constants.IdentityMgtConstants;
import org.wso2.carbon.identity.mgt.dto.ChallengeQuestionDTO;
import org.wso2.carbon.identity.mgt.dto.UserChallengesDTO;
import org.wso2.carbon.identity.mgt.dto.UserChallengesSetDTO;
import org.wso2.carbon.identity.mgt.dto.UserDTO;
import org.wso2.carbon.identity.mgt.dto.UserIdentityClaimDTO;
import org.wso2.carbon.identity.mgt.dto.UserRecoveryDTO;
import org.wso2.carbon.identity.mgt.internal.IdentityMgtServiceComponent;
import org.wso2.carbon.identity.mgt.util.UserIdentityManagementUtil;
import org.wso2.carbon.identity.mgt.util.Utils;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is the admin service for the identity management. Some of these
 * operations are can only be carried out by admins. The other operations are
 * allowed to all logged in users.
 *
 * @author sga
 * @deprecated use REST API implementation available in org.wso2.carbon.identity.scim2.provider.resources.UserResource
 * and org.wso2.carbon.identity.rest.api.user.challenge.v1.core.UserChallengeService instead.
 */
public class UserIdentityManagementAdminService {

    private static final Log log = LogFactory.getLog(UserIdentityManagementAdminService.class);

    private static final Log audit = CarbonConstants.AUDIT_LOG;
    private static String AUDIT_MESSAGE = "Initiator : %s | Action : %s | Target : %s | Data : { %s } | Result : %s ";
    private final String SUCCESS = "Success";

    // --------Operations require Admin permissions ---------//

    /**
     * Admin deletes a user from the system. This is an irreversible operation.
     *
     * @param userName
     * @throws IdentityMgtServiceException
     */
    public void deleteUser(String userName) throws IdentityMgtServiceException {

        try {
            UserStoreManager userStoreManager = IdentityMgtServiceComponent.getRealmService().
                    getTenantUserRealm(CarbonContext.getThreadLocalCarbonContext().getTenantId()).getUserStoreManager();
            userStoreManager.deleteUser(userName);
            log.info("Deleted user: " + userName);
        } catch (UserStoreException e) {
            String errorMessage = "Error occured while deleting user : " + userName;
            log.error(errorMessage, e);
            throw new IdentityMgtServiceException(errorMessage);
        }
    }

    /**
     * Admin locks the user account. Only the admin can unlock the account using
     * the {@literal unlockUserAccount} method.
     *
     * @param userName
     * @throws IdentityMgtServiceException
     */
    public void lockUserAccount(String userName) throws IdentityMgtServiceException {

        try {
            UserStoreManager userStoreManager = getUserStore(userName);
            String userNameWithoutDomain = UserCoreUtil.removeDomainFromName(userName);
            UserIdentityManagementUtil.lockUserAccount(userNameWithoutDomain, userStoreManager);
            log.info("User account locked: " + userName);
        } catch (UserStoreException|IdentityException e) {
            log.error("Error occurred while trying to lock the account " + userName, e);
            throw new IdentityMgtServiceException("Error occurred while trying to lock the account " + userName, e);
        }
    }

    /**
     * Admin unlocks the user account.
     *
     * @param userName
     * @throws IdentityMgtServiceException
     */
    public void unlockUserAccount(String userName, String notificationType) throws IdentityMgtServiceException {
        try {
            UserStoreManager userStoreManager = getUserStore(userName);
            String userNameWithoutDomain = UserCoreUtil.removeDomainFromName(userName);
            UserIdentityManagementUtil.unlockUserAccount(userNameWithoutDomain, userStoreManager);
            int tenantID = userStoreManager.getTenantId();
            String tenantDomain = IdentityMgtServiceComponent.getRealmService().getTenantManager().getDomain(tenantID);
            boolean isNotificationSending = IdentityMgtConfig.getInstance().isNotificationSending();
            if (notificationType != null && isNotificationSending) {
                UserRecoveryDTO dto;
                if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                    dto = new UserRecoveryDTO(userName);
                } else {
                    UserDTO userDTO = new UserDTO(UserCoreUtil.addTenantDomainToEntry(userName, tenantDomain));
                    userDTO.setTenantId(tenantID);
                    dto = new UserRecoveryDTO(userDTO);
                }
                dto.setNotification(IdentityMgtConstants.Notification.ACCOUNT_UNLOCK);
                dto.setNotificationType(notificationType);
                IdentityMgtServiceComponent.getRecoveryProcessor().recoverWithNotification(dto);
            }
            log.info("Account unlocked for: " + userName);
        } catch (UserStoreException|IdentityException e) {
            String message = "Error occurred while unlocking account for: " + userName;
            log.error(message, e);
            throw new IdentityMgtServiceException(message, e);
        }
    }

    /**
     * Admin disables the user account. Only the admin can enable the account using
     * the {@literal enableUserAccount} method.
     *
     * @param userName
     * @throws IdentityMgtServiceException
     */
    public void disableUserAccount(String userName, String notificationType) throws IdentityMgtServiceException {

        try {
            UserStoreManager userStoreManager = getUserStore(userName);
            String userNameWithoutDomain = UserCoreUtil.removeDomainFromName(userName);
            UserIdentityManagementUtil.disableUserAccount(userNameWithoutDomain, userStoreManager);

            audit.info(String.format(AUDIT_MESSAGE, getUser(), "Disable user account", userName,
                    "Notification type :" + notificationType, SUCCESS));

            int tenantID = userStoreManager.getTenantId();
            String tenantDomain = IdentityMgtServiceComponent.getRealmService().getTenantManager().getDomain(tenantID);
            boolean isNotificationSending = IdentityMgtConfig.getInstance().isAccountDisableNotificationSending();
            if (notificationType != null && isNotificationSending) {
                UserRecoveryDTO dto;
                if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                    dto = new UserRecoveryDTO(userName);
                } else {
                    UserDTO userDTO = new UserDTO(UserCoreUtil.addTenantDomainToEntry(userName, tenantDomain));
                    userDTO.setTenantId(tenantID);
                    dto = new UserRecoveryDTO(userDTO);
                }
                dto.setNotification(IdentityMgtConstants.Notification.ACCOUNT_DISABLE);
                dto.setNotificationType(notificationType);
                IdentityMgtServiceComponent.getRecoveryProcessor().recoverWithNotification(dto);

                if(log.isDebugEnabled()){
                    log.debug("Account enabled notification is sent in " + notificationType);
                }
            }
        } catch (UserStoreException | IdentityException e) {
            log.error("Error occurred while trying to disable the account " + userName, e);
            throw new IdentityMgtServiceException("Error occurred while trying to disable the account " + userName, e);
        }
    }

    private String getUser() {
        String user = CarbonContext.getThreadLocalCarbonContext().getUsername();
        if (user != null) {
            user = user + "@" + CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        } else {
            user = CarbonConstants.REGISTRY_SYSTEM_USERNAME;
        }
        return user;
    }

    /**
     * Admin enables the user account.
     *
     * @param userName
     * @throws IdentityMgtServiceException
     */
    public void enableUserAccount(String userName, String notificationType) throws IdentityMgtServiceException {
        try {
            UserStoreManager userStoreManager = getUserStore(userName);
            String userNameWithoutDomain = UserCoreUtil.removeDomainFromName(userName);
            UserIdentityManagementUtil.enableUserAccount(userNameWithoutDomain, userStoreManager);

            audit.info(String.format(AUDIT_MESSAGE, getUser(), "Enable user account", userName,
                    "Notification type :" + notificationType, SUCCESS));

            int tenantID = userStoreManager.getTenantId();
            String tenantDomain = IdentityMgtServiceComponent.getRealmService().getTenantManager().getDomain(tenantID);
            boolean isNotificationSending = IdentityMgtConfig.getInstance().isAccountEnableNotificationSending();
            if (notificationType != null && isNotificationSending) {
                UserRecoveryDTO dto;
                if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                    dto = new UserRecoveryDTO(userName);
                } else {
                    UserDTO userDTO = new UserDTO(UserCoreUtil.addTenantDomainToEntry(userName, tenantDomain));
                    userDTO.setTenantId(tenantID);
                    dto = new UserRecoveryDTO(userDTO);
                }
                dto.setNotification(IdentityMgtConstants.Notification.ACCOUNT_ENABLE);
                dto.setNotificationType(notificationType);
                IdentityMgtServiceComponent.getRecoveryProcessor().recoverWithNotification(dto);

                if(log.isDebugEnabled()){
                    log.debug("Account enabled notification is sent in " + notificationType);
                }
            }

        } catch (UserStoreException | IdentityException e) {
            String message = "Error occurred while enabling account for: " + userName;
            log.error(message, e);
            throw new IdentityMgtServiceException(message, e);
        }
    }

    /**
     * Admin resets the password of the user.
     *
     * @param userName
     * @param newPassword
     * @throws IdentityMgtServiceException
     */
    public void resetUserPassword(String userName, String newPassword)
            throws IdentityMgtServiceException {
        try {
            UserStoreManager userStoreManager = getUserStore(userName);
            String userNameWithoutDomain = UserCoreUtil.removeDomainFromName(userName);
            userStoreManager.updateCredentialByAdmin(userNameWithoutDomain, newPassword);
            log.info("User password reset for: " + userName);
        } catch (UserStoreException e) {
            String message = "Error occurred while resetting password for: " + userName;
            log.error(message, e);
            throw new IdentityMgtServiceException(message, e);
        }
    }

    /**
     * get challenges of user
     *
     * @param userName bean class that contains user and tenant Information
     * @return array of challenges  if null, return empty array
     * @throws org.wso2.carbon.identity.mgt.IdentityMgtServiceException if fails
     */
    public UserChallengesDTO[] getChallengeQuestionsOfUser(String userName)
            throws IdentityMgtServiceException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String loggedInName = CarbonContext.getThreadLocalCarbonContext().getUsername();

        if(userName != null && !userName.equals(loggedInName)){
            AuthorizationManager authzManager = null;
            try {
                authzManager = IdentityMgtServiceComponent.getRealmService().getTenantUserRealm(tenantId).
                        getAuthorizationManager();
            } catch (UserStoreException e) {
                throw new IdentityMgtServiceException("Error occurred while retrieving AuthorizationManager for tenant " +
                        tenantDomain, e);
            }
            boolean isAuthorized = false;
            try {
                isAuthorized = authzManager.isUserAuthorized(loggedInName, "/permission/admin/manage/identity/identitymgt/view",
                        CarbonConstants.UI_PERMISSION_ACTION);
            } catch (UserStoreException e) {
                    throw new IdentityMgtServiceException("Error occurred while checking access level for " +
                            "user " + userName + " in tenant " + tenantDomain, e);
            }
            if(!isAuthorized){
                throw new IdentityMgtServiceException("Unauthorized access!! Possible violation of confidentiality. " +
                        "User " + loggedInName + " trying to get challenge questions for user " + userName);
            }
        } else if (userName == null){
            userName = loggedInName;
        }

        ChallengeQuestionProcessor processor = IdentityMgtServiceComponent.
                getRecoveryProcessor().getQuestionProcessor();

        return processor.getChallengeQuestionsOfUser(userName, tenantId, true);
    }

    /**
     * get all promoted user challenges
     *
     * @return array of user challenges
     * @throws IdentityMgtServiceException if fails
     */
    public UserChallengesSetDTO[] getAllPromotedUserChallenge() throws IdentityMgtServiceException {

        ChallengeQuestionProcessor processor = IdentityMgtServiceComponent.
                getRecoveryProcessor().getQuestionProcessor();
        List<UserChallengesSetDTO> challengeQuestionSetDTOs = new ArrayList<UserChallengesSetDTO>();
        List<ChallengeQuestionDTO> questionDTOs = null;
        try {
            questionDTOs = processor.getAllChallengeQuestions();
        } catch (IdentityException e) {
            log.error("Error while loading user challenges", e);
            throw new IdentityMgtServiceException("Error while loading user challenges");
        }
        Map<String, List<UserChallengesDTO>> listMap = new HashMap<String, List<UserChallengesDTO>>();
        for (ChallengeQuestionDTO dto : questionDTOs) {

            List<UserChallengesDTO> dtoList = listMap.get(dto.getQuestionSetId());
            if (dtoList == null) {
                dtoList = new ArrayList<UserChallengesDTO>();
            }

            UserChallengesDTO userChallengesDTO = new UserChallengesDTO();
            userChallengesDTO.setId(dto.getQuestionSetId());
            userChallengesDTO.setQuestion(dto.getQuestion());
            userChallengesDTO.setOrder(dto.getOrder());

            dtoList.add(userChallengesDTO);
            listMap.put(dto.getQuestionSetId(), dtoList);
        }

        for (Map.Entry<String, List<UserChallengesDTO>> listEntry : listMap.entrySet()) {
            UserChallengesSetDTO dto = new UserChallengesSetDTO();
            dto.setId(listEntry.getKey());
            List<UserChallengesDTO> dtoList = listEntry.getValue();
            dto.setChallengesDTOs(dtoList.toArray(new UserChallengesDTO[dtoList.size()]));
            challengeQuestionSetDTOs.add(dto);
        }

        return challengeQuestionSetDTOs.toArray(new UserChallengesSetDTO[challengeQuestionSetDTOs.size()]);
    }

    /**
     * get all challenge questions
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
            String errorMessage = "Error while loading user challenge questions";
            log.error(errorMessage, e);
            throw new IdentityMgtServiceException(errorMessage);
        }
        return questionDTOs.toArray(new ChallengeQuestionDTO[questionDTOs.size()]);

    }

    /**
     * set all challenge questions
     *
     * @param challengeQuestionDTOs array of questions
     * @throws IdentityMgtServiceException if fails
     */
    public void setChallengeQuestions(ChallengeQuestionDTO[] challengeQuestionDTOs)
            throws IdentityMgtServiceException {

        ChallengeQuestionProcessor processor = IdentityMgtServiceComponent.
                getRecoveryProcessor().getQuestionProcessor();
        try {
            processor.setChallengeQuestions(challengeQuestionDTOs);
        } catch (IdentityException e) {
            log.error("Error while persisting user challenges", e);
            throw new IdentityMgtServiceException("Error while persisting user challenges");
        }
    }

    /**
     * set challenges of user
     *
     * @param userName bean class that contains user and tenant Information
     * @throws IdentityMgtServiceException if fails
     */
    public void setChallengeQuestionsOfUser(String userName, UserChallengesDTO[] challengesDTOs) throws IdentityMgtServiceException {

        if (challengesDTOs == null || challengesDTOs.length < 1) {
            log.error("no challenges provided by user");
            throw new IdentityMgtServiceException("no challenges provided by user");
        }

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String loggedInName = CarbonContext.getThreadLocalCarbonContext().getUsername();

        if(userName != null && !userName.equals(loggedInName)){
            AuthorizationManager authzManager = null;
            try {
                authzManager = IdentityMgtServiceComponent.getRealmService().getTenantUserRealm(tenantId).
                        getAuthorizationManager();
            } catch (UserStoreException e) {
                throw new IdentityMgtServiceException("Error occurred while retrieving AuthorizationManager for tenant " +
                        tenantDomain, e);
            }
            boolean isAuthorized = false;
            try {
                isAuthorized = authzManager.isUserAuthorized(loggedInName, "/permission/admin/manage/identity/identitymgt/update",
                        CarbonConstants.UI_PERMISSION_ACTION);
            } catch (UserStoreException e) {
                throw new IdentityMgtServiceException("Error occurred while checking access level for " +
                        "user " + userName + " in tenant " + tenantDomain, e);
            }
            if(!isAuthorized){
                throw new IdentityMgtServiceException("Unauthorized access!! Possible elevation of privilege attack. " +
                        "User " + loggedInName + " trying to change challenge questions for user " + userName);
            }
        } else if (userName == null){
            userName = loggedInName;
        }

        validateSecurityQuestionDuplicate(challengesDTOs);

        ChallengeQuestionProcessor processor = IdentityMgtServiceComponent.
                getRecoveryProcessor().getQuestionProcessor();

        try {
            List<ChallengeQuestionDTO> challengeQuestionDTOs = processor.getAllChallengeQuestions();
            for (UserChallengesDTO userChallengesDTO : challengesDTOs){
                boolean found = false ;
                for (ChallengeQuestionDTO challengeQuestionDTO :challengeQuestionDTOs ){
                    if(challengeQuestionDTO.getQuestion().equals(userChallengesDTO.getQuestion()) &&
                            challengeQuestionDTO.getQuestionSetId().equals(userChallengesDTO.getId())){
                        found = true ;
                        break ;
                    }
                }
                if(!found){
                    String errMsg = "Error while persisting user challenges for user : " + userName + ", because these user challengers are not registered with the tenant" ;
                    log.error(errMsg);
                    throw new IdentityMgtServiceException(errMsg);
                }
            }
            processor.setChallengesOfUser(userName, tenantId, challengesDTOs);
        } catch (IdentityException e) {
            String errorMessage = "Error while persisting user challenges for user : " + userName;
            log.error(errorMessage, e);
            throw new IdentityMgtServiceException(errorMessage);
        }
    }


    /**
     * User updates/add account recovery data such as the email address or the
     * phone number etc.
     *
     * @param userIdentityClaims
     * @throws IdentityMgtServiceException
     */
    public void updateUserIdentityClaims(UserIdentityClaimDTO[] userIdentityClaims)
            throws IdentityMgtServiceException {
        String userName = CarbonContext.getThreadLocalCarbonContext().getUsername();

        try {
            UserStoreManager userStoreManager = IdentityMgtServiceComponent.getRealmService()
                    .getTenantUserRealm(CarbonContext.getThreadLocalCarbonContext().getTenantId())
                    .getUserStoreManager();

            Map<String, String> claims = new HashMap<String, String>();
            for (UserIdentityClaimDTO dto : userIdentityClaims) {
                if (dto.getClaimUri().contains(UserCoreConstants.ClaimTypeURIs.IDENTITY_CLAIM_URI)) {
                    log.warn("WARNING! User " + userName + " tried to alter " + dto.getClaimUri());
                    throw IdentityException.error("Updates to the claim " + dto.getClaimUri() +
                            " are not allowed");
                }
                claims.put(dto.getClaimUri(), dto.getClaimValue());
            }
            userStoreManager.setUserClaimValues(userName, claims, null);

        } catch (UserStoreException|IdentityException e) {
            String errorMessage = "Error while updating identity recovery data for : " + userName;
            log.error(errorMessage, e);
            throw new IdentityMgtServiceException(errorMessage, e);
        }
    }

    /**
     * Returns all user claims which can be used in the identity recovery
     * process
     * such as the email address, telephone number etc
     *
     * @return
     * @throws IdentityMgtServiceException
     */
    public UserIdentityClaimDTO[] getAllUserIdentityClaims() throws IdentityMgtServiceException {
        String userName = CarbonContext.getThreadLocalCarbonContext().getUsername();
        return UserIdentityManagementUtil.getAllUserIdentityClaims(userName);
    }

    /**
     * User change the password of the user.
     *
     * @param newPassword
     * @throws IdentityMgtServiceException
     */
    public void changeUserPassword(String newPassword, String oldPassword) throws IdentityMgtServiceException {

        String userName = CarbonContext.getThreadLocalCarbonContext().getUsername();

        try {
            UserStoreManager userStoreManager = getUserStore(userName);
            userName = UserCoreUtil.removeDomainFromName(userName);
            userStoreManager.updateCredential(userName, newPassword, oldPassword);
            log.info("Password changed for: " + userName);
        } catch (UserStoreException e) {
            String message = "Error while resetting the password for: " + userName;
            log.error(message, e);
            throw new IdentityMgtServiceException(message, e);
        }
    }

    /**
     * This method is used to check the user's user store is read only.
     *
     * @param userName
     * @param tenantDomain
     * @return
     * @throws IdentityMgtServiceException
     */
    public boolean isReadOnlyUserStore(String userName, String tenantDomain)
            throws IdentityMgtServiceException {

        boolean isReadOnly = false;

        org.wso2.carbon.user.core.UserStoreManager userStoreManager = null;

        if (StringUtils.isEmpty(tenantDomain)) {
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }

        RealmService realmService = IdentityMgtServiceComponent.getRealmService();
        int tenantId;

        try {
            tenantId = Utils.getTenantId(tenantDomain);

            if (realmService.getTenantUserRealm(tenantId) != null) {
                userStoreManager = (org.wso2.carbon.user.core.UserStoreManager) getUserStore(userName);
            }

        } catch (Exception e) {
            String msg = "Error retrieving the user store manager for the tenant";
            log.error(msg, e);
            throw new IdentityMgtServiceException(msg);
        }

        try {
            if (userStoreManager != null && userStoreManager.isReadOnly()) {
                isReadOnly = true;
            } else
                isReadOnly = false;

        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            String errorMessage = "Error while retrieving user store manager";
            log.error(errorMessage, e);
            throw new IdentityMgtServiceException(errorMessage);
        }

        return isReadOnly;
    }

    private UserStoreManager getUserStore(String userName) throws UserStoreException {
        UserStoreManager userStoreManager = IdentityMgtServiceComponent.getRealmService().
                getTenantUserRealm(CarbonContext.getThreadLocalCarbonContext().getTenantId()).getUserStoreManager();
        if (userName != null && userName.contains(UserCoreConstants.DOMAIN_SEPARATOR)) {
            String userStoreDomain = getUserStoreDomainName(userName);
            return ((AbstractUserStoreManager) userStoreManager)
                    .getSecondaryUserStoreManager(userStoreDomain);
        } else {
            return userStoreManager;
        }
    }

    private String getUserStoreDomainName(String userName) {
        String userNameWithoutDomain = userName;
        int index;
        if ((index = userName.indexOf(CarbonConstants.DOMAIN_SEPARATOR)) >= 0) {
            // remove domain name if exist
            userNameWithoutDomain = userName.substring(0, index);
        }
        return userNameWithoutDomain;
    }

    private void validateSecurityQuestionDuplicate(UserChallengesDTO[] challengesDTOs) throws IdentityMgtServiceException {

        Set<String> tmpMap = new HashSet<String>();
        for(int i = 0; i < challengesDTOs.length ; i++) {
            UserChallengesDTO userChallengesDTO = challengesDTOs[i];
            if(tmpMap.contains(userChallengesDTO.getId())){
                String errMsg = "Error while validating user challenges, because these can't be more than one security challenges for one claim uri" ;
                log.error(errMsg);
                throw new IdentityMgtServiceException(errMsg);
            }
            tmpMap.add(userChallengesDTO.getId());
        }
    }

}
