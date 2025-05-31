/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.flow.engine.graph;

/**
 * Executor to load a user into the FlowContext based on the provided user ID.
 */
public class PasswordSetupExecutor {

//    private static final Log LOG = LogFactory.getLog(PasswordSetupExecutor.class);
//
//    @Override
//    public String getName() {
//        return "PasswordSetupExecutor";
//    }
//
//    @Override
//    public List<String> getInitiationData() {
//
//        LOG.debug("Initiation data is not required for the executor: " + getName());
//        return null;
//    }
//
//    @Override
//    public ExecutorResponse execute(FlowContext context) throws FlowEngineException {
//
//        String userId = context.getUserId();
//        //String userName = claims.get(USERNAME_CLAIM_URI);
//        context.getTenantDomain()
//        String confirmationCode = claims.get("http://wso2.org/claims/confimrationCode");
//
//
////        if (userName == null || userName.isEmpty()) {
////            throw handleClientException(ERROR_CODE_USERSTORE_MANAGER_FAILURE, context.getContextIdentifier());
////        }
//
//        try {
//            String userStoreDomainName = resolveUserStoreDomain(userName);
//            UserStoreManager userStoreManager = getUserStoreManager(context.getTenantDomain(), userStoreDomainName,
//                    context.getContextIdentifier());
//            Claim[] userClaims = userStoreManager.getUserClaimValues(userName, null);
//            List<Claim> userClaimsList = new ArrayList<>(
//                    Arrays.asList(userClaims));
//
//            if (userClaimsList == null) {
//                throw handleClientException(ERROR_CODE_USERSTORE_MANAGER_FAILURE, userName);
//            }
//
//            FlowUser user = new FlowUser();
//            user.setUsername(userName);
//            for (Claim claim : userClaimsList) {
//                user.addClaim(claim.getClaimUri(), claim.getValue());
//            }
//
//            context.setFlowUser(user);
//            return new ExecutorResponse(STATUS_USER_INPUT_REQUIRED);
//        } catch (UserStoreException e) {
//            throw handleServerException(ERROR_CODE_USERSTORE_MANAGER_FAILURE, e, context.getTenantDomain());
//        }
//        return null;
//    }
//
//    public void updateNewPassword(User user, String password, String domainQualifiedName,
//                                  String recoveryScenario,
//                                  boolean isNotificationInternallyManaged, FlowContext context)
//            throws IdentityEventException, IdentityRecoveryException {
//
//        try {
//            int tenantId = IdentityTenantUtil.getTenantId(context.getTenantDomain());
//            org.wso2.carbon.user.api.UserStoreManager userStoreManager = getUserStoreManager(context.getTenantDomain(),
//                    domainQualifiedName, context.getContextIdentifier() );
//            userStoreManager.updateCredentialByAdmin(domainQualifiedName, password);
//
//            // Get the claims that related to a password reset.
//            HashMap<String, String> userClaims = getAccountStateClaims(userRecoveryData,
//                    isNotificationInternallyManaged);
//            if (MapUtils.isNotEmpty(userClaims)) {
//                // Update the retrieved claims set.
//                userStoreManager.setUserClaimValues(domainQualifiedName, userClaims, null);
//            }
//        } catch (UserStoreException e) {
//            checkPasswordValidity(e, user);
//            if (log.isDebugEnabled()) {
//                log.debug("NotificationPasswordRecoveryManager: Unexpected Error occurred while updating password "
//                        + "for the user: " + domainQualifiedName, e);
//            }
//            throw Utils.handleServerException(IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_UNEXPECTED, null, e);
//        } catch (FlowEngineException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    /**
//     * Get the claims that needs to be updated when a user attempts a password reset.
//     *
//     * @param userRecoveryData                User recovery data
//     * @param isNotificationInternallyManaged Whether the notifications are internally managed
//     * @return Claims that are related to account state
//     * @throws IdentityEventException Error while checking for account state claim
//     */
//    private HashMap<String, String> getAccountStateClaims(UserRecoveryData userRecoveryData,
//                                                          boolean isNotificationInternallyManaged)
//            throws IdentityEventException {
//
//        HashMap<String, String> userClaims = new HashMap<>();
//        Enum<RecoveryScenarios> recoveryScenario = userRecoveryData.getRecoveryScenario();
//        // If notifications are internally managed we try to set the verified claims since this is an opportunity
//        // to verify a user channel.
//        if (isNotificationInternallyManaged && !isNotificationLessRecoveryMethod(recoveryScenario)) {
//            if (NotificationChannels.EMAIL_CHANNEL.getChannelType().equals(userRecoveryData.getRemainingSetIds())) {
//                userClaims.put(NotificationChannels.EMAIL_CHANNEL.getVerifiedClaimUrl(), Boolean.TRUE.toString());
//            } else if (NotificationChannels.SMS_CHANNEL.getChannelType().equals(userRecoveryData.getRemainingSetIds())) {
//                userClaims.put(NotificationChannels.SMS_CHANNEL.getVerifiedClaimUrl(), Boolean.TRUE.toString());
//            } else {
//                if (log.isDebugEnabled()) {
//                    String error = String
//                            .format("No notification channels for the user : %s in tenant domain : " + "%s",
//                                    userRecoveryData.getUser().getUserStoreDomain() + userRecoveryData.getUser()
//                                            .getUserName(), userRecoveryData.getUser().getTenantDomain());
//                    log.debug(error);
//                }
//                userClaims.put(NotificationChannels.EMAIL_CHANNEL.getVerifiedClaimUrl(), Boolean.TRUE.toString());
//            }
//        }
//        // We don't need to change any states during user initiated password recovery.
//        if (RecoveryScenarios.NOTIFICATION_BASED_PW_RECOVERY.equals(recoveryScenario)
//                || RecoveryScenarios.QUESTION_BASED_PWD_RECOVERY.equals(recoveryScenario)
//                || RecoveryScenarios.ADMIN_FORCED_PASSWORD_RESET_VIA_EMAIL_LINK.equals(recoveryScenario)
//                || RecoveryScenarios.ADMIN_FORCED_PASSWORD_RESET_VIA_OTP.equals(recoveryScenario)
//                || RecoveryScenarios.ADMIN_FORCED_PASSWORD_RESET_VIA_SMS_OTP.equals(recoveryScenario)
//                || RecoveryScenarios.ASK_PASSWORD.equals(recoveryScenario)) {
//            IdentityUtil.threadLocalProperties.get().put(AccountConstants.ADMIN_INITIATED, false);
//        }
//
//        if (Utils.isAccountStateClaimExisting(userRecoveryData.getUser().getTenantDomain())) {
//            userClaims.put(IdentityRecoveryConstants.ACCOUNT_STATE_CLAIM_URI,
//                    IdentityRecoveryConstants.ACCOUNT_STATE_UNLOCKED);
//            userClaims.put(IdentityRecoveryConstants.ACCOUNT_LOCKED_REASON_CLAIM, StringUtils.EMPTY);
//            userClaims.put(IdentityRecoveryConstants.ACCOUNT_LOCKED_CLAIM, Boolean.FALSE.toString());
//        }
//
//        // If the scenario is initiated by the admin, set the account locked claim to FALSE.
//        if (RecoveryScenarios.ADMIN_FORCED_PASSWORD_RESET_VIA_EMAIL_LINK.equals(recoveryScenario)
//                || RecoveryScenarios.ADMIN_FORCED_PASSWORD_RESET_VIA_OTP.equals(recoveryScenario)
//                || RecoveryScenarios.ADMIN_FORCED_PASSWORD_RESET_VIA_SMS_OTP.equals(recoveryScenario)) {
//            userClaims.put(IdentityRecoveryConstants.ACCOUNT_LOCKED_CLAIM, Boolean.FALSE.toString());
//            userClaims.remove(IdentityRecoveryConstants.ACCOUNT_LOCKED_REASON_CLAIM);
//        }
//        return userClaims;
//    }
//
//    private String resolveUserStoreDomain(String username) {
//
//        int separatorIndex = username.indexOf(UserCoreConstants.DOMAIN_SEPARATOR);
//        if (separatorIndex >= 0) {
//            String domain = username.substring(0, separatorIndex);
//            if (INTERNAL_DOMAIN.equalsIgnoreCase(domain) || WORKFLOW_DOMAIN.equalsIgnoreCase(domain)
//                    || APPLICATION_DOMAIN.equalsIgnoreCase(domain)) {
//                return domain.substring(0, 1).toUpperCase(ENGLISH) + domain.substring(1).toLowerCase(ENGLISH);
//            }
//            return domain.toUpperCase(ENGLISH);
//        }
//
//        String domainName = IdentityUtil.getProperty(SELF_REGISTRATION_DEFAULT_USERSTORE_CONFIG);
//        return domainName != null ? domainName.toUpperCase(ENGLISH) :
//                IdentityUtil.getPrimaryDomainName().toUpperCase(ENGLISH);
//    }
//
//    private UserStoreManager getUserStoreManager(String tenantDomain, String userdomain, String flowId)
//            throws FlowEngineException {
//
//        RealmService realmService = FlowEngineDataHolder.getInstance().getRealmService();
//        UserStoreManager userStoreManager;
//        try {
//            UserRealm tenantUserRealm = realmService.getTenantUserRealm(IdentityTenantUtil.getTenantId(tenantDomain));
//            if (IdentityUtil.getPrimaryDomainName().equals(userdomain)) {
//                userStoreManager = (UserStoreManager) tenantUserRealm.getUserStoreManager();
//            } else {
//                userStoreManager =
//                        ((UserStoreManager) tenantUserRealm.getUserStoreManager()).getSecondaryUserStoreManager(userdomain);
//            }
//            if (userStoreManager == null) {
//                throw handleServerException(ERROR_CODE_USERSTORE_MANAGER_FAILURE, tenantDomain, flowId);
//            }
//            return userStoreManager;
//        } catch (UserStoreException e) {
//            throw handleServerException(ERROR_CODE_USERSTORE_MANAGER_FAILURE, e, tenantDomain, flowId);
//        }
//    }
}