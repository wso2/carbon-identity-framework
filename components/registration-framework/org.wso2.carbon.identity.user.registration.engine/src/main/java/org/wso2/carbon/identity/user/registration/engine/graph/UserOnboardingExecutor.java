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

package org.wso2.carbon.identity.user.registration.engine.graph;

import static org.wso2.carbon.identity.user.registration.engine.Constants.ErrorMessages.ERROR_CODE_USERNAME_NOT_PROVIDED;
import static org.wso2.carbon.identity.user.registration.engine.Constants.ErrorMessages.ERROR_CODE_USERSTORE_MANAGER_FAILURE;
import static org.wso2.carbon.identity.user.registration.engine.Constants.ErrorMessages.ERROR_CODE_USER_ONBOARD_FAILURE;
import static org.wso2.carbon.identity.user.registration.engine.Constants.ExecutorStatus.STATUS_USER_CREATED;
import static org.wso2.carbon.identity.user.registration.engine.Constants.PASSWORD;
import static org.wso2.carbon.identity.user.registration.engine.Constants.USERNAME_CLAIM_URI;
import static org.wso2.carbon.identity.user.registration.engine.util.RegistrationFlowEngineUtils.handleClientException;
import static org.wso2.carbon.identity.user.registration.engine.util.RegistrationFlowEngineUtils.handleServerException;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.ExecutorTypes.USER_ONBOARDING;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.user.registration.engine.internal.RegistrationFlowEngineDataHolder;
import org.wso2.carbon.identity.user.registration.engine.model.ExecutorResponse;
import org.wso2.carbon.identity.user.registration.engine.model.RegisteringUser;
import org.wso2.carbon.identity.user.registration.engine.model.RegistrationContext;
import org.wso2.carbon.identity.user.registration.engine.exception.RegistrationEngineException;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.mgt.common.DefaultPasswordGenerator;

/**
 * Implementation of the executor which handles onboarding the user to the system.
 */
public class UserOnboardingExecutor implements Executor {

    private static final Log LOG = LogFactory.getLog(UserOnboardingExecutor.class);

    @Override
    public String getName() {

        return USER_ONBOARDING;
    }

    @Override
    public List<String> getInitiationData() {

        LOG.debug("Initiation data is not required for the executor: " + getName());
        return null;
    }

    @Override
    public ExecutorResponse execute(RegistrationContext context) throws RegistrationEngineException {

        updateUserProfile(context);
        RegisteringUser user = context.getRegisteringUser();
        Map<String, char[]> credentials = user.getUserCredentials();

        char[] password =
                credentials.getOrDefault(PASSWORD, new DefaultPasswordGenerator().generatePassword());

        Map<String, Object> claims = user.getClaims();
        Map<String, String> userClaims = new HashMap<>();
        for (Map.Entry<String, Object> entry : claims.entrySet()) {
            userClaims.put(entry.getKey(), String.valueOf(entry.getValue()));
        }

        try {
            String userStoreDomainName = IdentityUtil.extractDomainFromName(user.getUsername());
            UserStoreManager userStoreManager = getUserStoreManager(context.getTenantDomain(),
                                                                    userStoreDomainName,
                                                                    context.getContextIdentifier());
            String domainName = userStoreManager.getRealmConfiguration()
                    .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);

            userStoreManager.addUser(IdentityUtil.addDomainToName(user.getUsername(), domainName),
                                     String.valueOf(password), null, userClaims, null);
            String userid = ((AbstractUserStoreManager) userStoreManager).getUserIDFromUserName(user.getUsername());
            context.setUserId(userid);
            return new ExecutorResponse(STATUS_USER_CREATED);
        } catch (UserStoreException e) {
            throw handleServerException(ERROR_CODE_USER_ONBOARD_FAILURE, e, user.getUsername(),
                                        context.getContextIdentifier());
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    private void updateUserProfile(RegistrationContext context) throws RegistrationEngineException {

        RegisteringUser user = context.getRegisteringUser();
        context.getUserInputData().forEach((key, value) -> {
            if (key.startsWith("http://wso2.org/claims/")) {
                if (!user.getClaims().containsKey(key)) {
                    user.addClaim(key, value);
                }
            }
        });
        String username = Optional.ofNullable(user.getUsername())
                .orElseGet(() -> user.getClaims().get(USERNAME_CLAIM_URI).toString());
        if (username == null) {
            throw handleClientException(ERROR_CODE_USERNAME_NOT_PROVIDED, context.getContextIdentifier());
        }
        user.setUsername(username);
    }

    private UserStoreManager getUserStoreManager(String tenantDomain, String userdomain, String flowId)
            throws RegistrationEngineException {

        RealmService realmService = RegistrationFlowEngineDataHolder.getInstance().getRealmService();
        UserStoreManager userStoreManager;
        try {
            UserRealm tenantUserRealm = realmService.getTenantUserRealm(IdentityTenantUtil.getTenantId(tenantDomain));
            if (IdentityUtil.getPrimaryDomainName().equals(userdomain)) {
                userStoreManager = (UserStoreManager) tenantUserRealm.getUserStoreManager();
            } else {
                userStoreManager =
                        ((UserStoreManager) tenantUserRealm.getUserStoreManager()).getSecondaryUserStoreManager(userdomain);
            }
        } catch (UserStoreException e) {
            throw handleServerException(ERROR_CODE_USERSTORE_MANAGER_FAILURE, e, tenantDomain, flowId);
        }
        if (userStoreManager == null) {
            throw handleServerException(ERROR_CODE_USERSTORE_MANAGER_FAILURE, tenantDomain, flowId);
        }
        return userStoreManager;
    }
}
