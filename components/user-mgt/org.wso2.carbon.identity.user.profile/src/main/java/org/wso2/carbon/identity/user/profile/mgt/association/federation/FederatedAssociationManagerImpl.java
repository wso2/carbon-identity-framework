/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.user.profile.mgt.association.federation;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.user.profile.mgt.AssociatedAccountDTO;
import org.wso2.carbon.identity.user.profile.mgt.UserProfileException;
import org.wso2.carbon.identity.user.profile.mgt.association.federation.constant.FederatedAssociationConstants;
import org.wso2.carbon.identity.user.profile.mgt.association.federation.exception.FederatedAssociationManagerClientException;
import org.wso2.carbon.identity.user.profile.mgt.association.federation.exception.FederatedAssociationManagerException;
import org.wso2.carbon.identity.user.profile.mgt.association.federation.exception.FederatedAssociationManagerServerException;
import org.wso2.carbon.identity.user.profile.mgt.association.federation.model.FederatedAssociation;
import org.wso2.carbon.identity.user.profile.mgt.dao.UserProfileMgtDAO;
import org.wso2.carbon.identity.user.profile.mgt.internal.IdentityUserProfileServiceDataHolder;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdpManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.wso2.carbon.identity.user.profile.mgt.association.federation.constant.FederatedAssociationConstants.ErrorMessages.ERROR_WHILE_CREATING_FEDERATED_ASSOCIATION_OF_USER;
import static org.wso2.carbon.identity.user.profile.mgt.association.federation.constant.FederatedAssociationConstants.ErrorMessages.ERROR_WHILE_DELETING_FEDERATED_ASSOCIATION_OF_USER;
import static org.wso2.carbon.identity.user.profile.mgt.association.federation.constant.FederatedAssociationConstants.ErrorMessages.ERROR_WHILE_GETTING_THE_USER;
import static org.wso2.carbon.identity.user.profile.mgt.association.federation.constant.FederatedAssociationConstants.ErrorMessages.ERROR_WHILE_GETTING_USER_FOR_FEDERATED_ASSOCIATION;
import static org.wso2.carbon.identity.user.profile.mgt.association.federation.constant.FederatedAssociationConstants.ErrorMessages.ERROR_WHILE_RESOLVING_IDENTITY_PROVIDERS;
import static org.wso2.carbon.identity.user.profile.mgt.association.federation.constant.FederatedAssociationConstants.ErrorMessages.ERROR_WHILE_RETRIEVING_FEDERATED_ASSOCIATION_OF_USER;
import static org.wso2.carbon.identity.user.profile.mgt.association.federation.constant.FederatedAssociationConstants.ErrorMessages.ERROR_WHILE_WORKING_WITH_FEDERATED_ASSOCIATIONS;
import static org.wso2.carbon.identity.user.profile.mgt.association.federation.constant.FederatedAssociationConstants.ErrorMessages.FEDERATED_ASSOCIATION_ALREADY_EXISTS;
import static org.wso2.carbon.identity.user.profile.mgt.association.federation.constant.FederatedAssociationConstants.ErrorMessages.FEDERATED_ASSOCIATION_DOES_NOT_EXISTS;
import static org.wso2.carbon.identity.user.profile.mgt.association.federation.constant.FederatedAssociationConstants.ErrorMessages.INVALID_FEDERATED_ASSOCIATION;
import static org.wso2.carbon.identity.user.profile.mgt.association.federation.constant.FederatedAssociationConstants.ErrorMessages.INVALID_TENANT_DOMAIN_PROVIDED;
import static org.wso2.carbon.identity.user.profile.mgt.association.federation.constant.FederatedAssociationConstants.ErrorMessages.INVALID_USER_IDENTIFIER_PROVIDED;
import static org.wso2.carbon.user.core.UserCoreConstants.TENANT_DOMAIN_COMBINER;

public class FederatedAssociationManagerImpl implements FederatedAssociationManager {

    private static final Log log = LogFactory.getLog(FederatedAssociationManagerImpl.class);
    private static final Function<AssociatedAccountDTO, FederatedAssociation>
            convertToFederatedUserAccountAssociationDTO = AssociatedAccountDTO -> new FederatedAssociation(
            AssociatedAccountDTO.getId(),
            AssociatedAccountDTO.getIdentityProviderName(),
            AssociatedAccountDTO.getUsername()
    );

    @Override
    public void createFederatedAssociation(String userId, String idpName, String federatedUserId)
            throws FederatedAssociationManagerException {

        int tenantId = getTenantId(userId);
        validateIfFederatedUserAccountAlreadyAssociated(MultitenantUtils.getTenantDomain(userId), idpName,
                federatedUserId);
        validateUser(userId, tenantId);

        User user = getUser(userId);
        try {
            UserProfileMgtDAO.getInstance().createAssociation(tenantId, user.getUserStoreDomain(), user.getUserName(),
                    idpName, federatedUserId);
        } catch (UserProfileException e) {
            throw handleFederatedAssociationManagerServerException(ERROR_WHILE_CREATING_FEDERATED_ASSOCIATION_OF_USER
                    , e, false);
        }
    }

    @Override
    public String getUserForTheFederatedAssociation(String tenantDomain, String idpName, String federatedUserId)
            throws FederatedAssociationManagerException {

        try {
            int tenantId = getIdFromTenantDomain(tenantDomain);
            return UserProfileMgtDAO.getInstance().getUserAssociatedFor(tenantId, idpName, federatedUserId);
        } catch (UserProfileException | IdentityRuntimeException e) {
            if (log.isDebugEnabled()) {
                String msg = "Error while retrieving user associated for federation IdP: " + idpName + ", with " +
                        "federation identifier: " + federatedUserId + ", in tenant: "
                        + CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
                log.debug(msg);
            }
            throw handleFederatedAssociationManagerServerException(ERROR_WHILE_GETTING_USER_FOR_FEDERATED_ASSOCIATION,
                    e, true);
        }
    }

    @Override
    public FederatedAssociation[] getFederatedAssociationsOfUser(String userId)
            throws FederatedAssociationManagerException {

        int tenantId = getTenantId(userId);
        validateUser(userId, tenantId);
        User user = getUser(userId);
        try {
            List<FederatedAssociation> federatedAssociations = new ArrayList<>();
            List<AssociatedAccountDTO> associatedAccountDTOS = UserProfileMgtDAO.getInstance()
                    .getAssociatedFederatedAccountsForUser(tenantId, user.getUserStoreDomain(), user.getUserName());
            for (AssociatedAccountDTO associatedAccount : associatedAccountDTOS) {
                String identityProviderId = getIdentityProviderId(MultitenantUtils.getTenantDomain(userId),
                        associatedAccount);
                federatedAssociations.add(
                        new FederatedAssociation(
                                identityProviderId,
                                associatedAccount.getIdentityProviderName(),
                                associatedAccount.getUsername()
                        )
                );
            }
            return federatedAssociations.toArray(new FederatedAssociation[0]);
        } catch (UserProfileException e) {
            if (log.isDebugEnabled()) {
                String msg = "Error while retrieving federation account associations of user: " + userId;
                log.debug(msg);
            }
            throw handleFederatedAssociationManagerServerException(
                    ERROR_WHILE_RETRIEVING_FEDERATED_ASSOCIATION_OF_USER, e, true);
        }
    }

    @Override
    public void deleteFederatedAssociation(String userId, String idpName, String federatedUserId)
            throws FederatedAssociationManagerException {

        int tenantId = getTenantId(userId);
        validateFederatedAssociation(userId, idpName, federatedUserId);
        User user = getUser(userId);
        try {
            UserProfileMgtDAO.getInstance().deleteAssociation(tenantId, user.getUserStoreDomain(), user.getUserName(),
                    idpName, federatedUserId);
        } catch (UserProfileException e) {
            if (log.isDebugEnabled()) {
                String msg = "Error while removing the federation association with idpId: " + idpName + ", and " +
                        "federatedUserId: " + federatedUserId + ", for user: " + userId + ", in tenant: "
                        + MultitenantUtils.getTenantDomain(userId);
                log.debug(msg);
            }
            // TODO: 10/31/19 We cannot guarantee why UserProfileException Occurred since we dont have a client/server
            //  child exceptions. Hence always a server error is thrown.
            throw handleFederatedAssociationManagerServerException(ERROR_WHILE_DELETING_FEDERATED_ASSOCIATION_OF_USER
                    , e, true);
        }
    }

    @Override
    public void deleteFederatedAssociation(String userName, String federatedAssociationId)
            throws FederatedAssociationManagerException {

        validateFederatedAssociation(userName, federatedAssociationId);
        User user = getUser(userName);
        try {
            UserProfileMgtDAO.getInstance().deleteFederatedAssociation(user.getUserStoreDomain(), user.getUserName(),
                    federatedAssociationId);
        } catch (UserProfileException e) {
            if (log.isDebugEnabled()) {
                String msg = "Error while removing the federated association: " + federatedAssociationId
                        + ", for user: " + userName + ", in tenant: " + MultitenantUtils.getTenantDomain(userName);
                log.debug(msg, e);
            }
            // TODO: 10/31/19 We cannot guarantee why UserProfileException Occurred since we dont have a client/server
            //  child exceptions. Hence always a server error is thrown.
            throw handleFederatedAssociationManagerServerException(ERROR_WHILE_DELETING_FEDERATED_ASSOCIATION_OF_USER, e,
                    true);
        }
    }

    @Override
    public void deleteFederatedAssociation(String userName) throws FederatedAssociationManagerException {

        int tenantId = getTenantId(userName);
        validateExistenceOfFederatedAssociations(userName);
        User user = getUser(userName);
        try {
            UserProfileMgtDAO.getInstance().deleteFederatedAssociation(tenantId, user.getUserStoreDomain(),
                    user.getUserName());
        } catch (UserProfileException e) {
            if (log.isDebugEnabled()) {
                String msg = "Error while removing the federated associations of user: " + userName + ", in tenant: "
                        + MultitenantUtils.getTenantDomain(userName);
                log.debug(msg, e);
            }
            // TODO: 10/31/19 We cannot guarantee why UserProfileException Occurred since we dont have a client/server
            //  child exceptions. Hence always a server error is thrown.
            throw handleFederatedAssociationManagerServerException(ERROR_WHILE_DELETING_FEDERATED_ASSOCIATION_OF_USER, e,
                    true);
        }
    }

    private void validateExistenceOfFederatedAssociations(String userName) throws FederatedAssociationManagerException {

        if (!isValidFederatedAssociationsExist(userName)) {
            if (log.isDebugEnabled()) {
                log.debug("Valid federated associations does not exist for the user: " + userName);
            }
            throw handleFederatedAssociationManagerClientException(FEDERATED_ASSOCIATION_DOES_NOT_EXISTS, null, true);
        }
    }

    private boolean isValidFederatedAssociationsExist(String userName) throws FederatedAssociationManagerException {

        FederatedAssociation[] federatedUserAccountAssociationDTOS
                = getFederatedAssociationsOfUser(userName);
        return !ArrayUtils.isEmpty(federatedUserAccountAssociationDTOS);
    }

    private void validateFederatedAssociation(String userName, String federatedAssociationId)
            throws FederatedAssociationManagerException {

        if (StringUtils.isEmpty(federatedAssociationId)
                || !isValidFederatedAssociation(userName, federatedAssociationId)) {
            if (log.isDebugEnabled()) {
                log.debug("A valid federated association does not exist for the Id: " + federatedAssociationId
                        + ", of the user: " + userName);
            }
            throw handleFederatedAssociationManagerClientException(INVALID_FEDERATED_ASSOCIATION, null, true);
        }
    }

    private boolean isValidFederatedAssociation(String userName, String federatedAssociationId)
            throws FederatedAssociationManagerException {

        FederatedAssociation[] federatedUserAccountAssociationDTOS
                = getFederatedAssociationsOfUser(userName);
        if (federatedUserAccountAssociationDTOS != null) {
            for (FederatedAssociation federatedUserAccountAssociationDTO
                    : federatedUserAccountAssociationDTOS) {
                if (federatedAssociationId.equals(federatedUserAccountAssociationDTO.getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    private String getPlainUserName(String userName) {

        String userNameWithoutDomain = getUsernameWithoutDomain(userName);
        return removeTenantDomain(userNameWithoutDomain);
    }

    private String getUsernameWithoutDomain(String username) {

        int index = username.indexOf(CarbonConstants.DOMAIN_SEPARATOR);
        if (index < 0) {
            return username;
        }
        return username.substring(index + 1);
    }

    private String removeTenantDomain(String username) {

        String tenantDomain = MultitenantUtils.getTenantDomain(username);
        if (username.endsWith(tenantDomain)) {
            return username.substring(0, username.lastIndexOf(TENANT_DOMAIN_COMBINER));
        }
        return username;
    }

    private User getUser(String fullyQualifiedUserName) {

        String tenantDomain = MultitenantUtils.getTenantDomain(fullyQualifiedUserName);
        String userStoreDomain = IdentityUtil.extractDomainFromName(fullyQualifiedUserName);
        String userName = getPlainUserName(fullyQualifiedUserName);

        User user = new User();
        user.setTenantDomain(tenantDomain);
        user.setUserStoreDomain(userStoreDomain);
        user.setUserName(userName);
        return user;
    }

    private int getTenantId(String userId) throws FederatedAssociationManagerException {

        int tenantId;
        RealmService realmService;
        try {
            realmService = IdentityUserProfileServiceDataHolder.getInstance().getRealmService();
            tenantId = realmService.getTenantManager().getTenantId(MultitenantUtils.getTenantDomain(userId));
        } catch (UserStoreException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while getting the tenant Id for the tenant domain: "
                        + MultitenantUtils.getTenantDomain(userId));
            }
            throw handleFederatedAssociationManagerServerException(ERROR_WHILE_WORKING_WITH_FEDERATED_ASSOCIATIONS, e,
                    false);
        }
        validateTenantId(MultitenantUtils.getTenantDomain(userId), tenantId);
        return tenantId;
    }

    private int getIdFromTenantDomain(String tenantDomain) throws FederatedAssociationManagerException {

        int tenantId;
        RealmService realmService;
        try {
            realmService = IdentityUserProfileServiceDataHolder.getInstance().getRealmService();
            tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while getting the tenant Id for the tenant domain: " + tenantDomain);
            }
            throw handleFederatedAssociationManagerServerException(ERROR_WHILE_WORKING_WITH_FEDERATED_ASSOCIATIONS, e,
                    false);
        }
        validateTenantId(tenantDomain, tenantId);
        return tenantId;
    }

    private void validateTenantId(String tenantDomain, int tenantId) throws FederatedAssociationManagerException {

        if (MultitenantConstants.INVALID_TENANT_ID == tenantId) {
            if (log.isDebugEnabled()) {
                log.debug("Invalid tenant id is resolved for the tenant domain: " + tenantDomain);
            }
            throw handleFederatedAssociationManagerClientException(INVALID_TENANT_DOMAIN_PROVIDED, null, true);
        }
    }

    private void validateIfFederatedUserAccountAlreadyAssociated(String tenantDomain, String idpId,
                                                                 String federatedUserId)
            throws FederatedAssociationManagerException {

        String userAssociated = getUserForTheFederatedAssociation(tenantDomain, idpId, federatedUserId);
        if (userAssociated != null) {
            if (log.isDebugEnabled()) {
                log.debug("Federated ID: " + federatedUserId + ", for IdP: " + idpId + ", is already associated " +
                        "with the local user account: " + userAssociated + UserCoreConstants
                        .TENANT_DOMAIN_COMBINER + CarbonContext.getThreadLocalCarbonContext().getTenantDomain() + ".");
            }
            throw handleFederatedAssociationManagerClientException(FEDERATED_ASSOCIATION_ALREADY_EXISTS, null, true);
        }
    }

    private void validateFederatedAssociation(String userName, String idpName, String federatedUserId)
            throws FederatedAssociationManagerException {

        if (StringUtils.isEmpty(idpName) || StringUtils.isEmpty(federatedUserId)
                || !isValidFederatedAssociation(userName, idpName, federatedUserId)) {
            if (log.isDebugEnabled()) {
                log.debug("A valid federation association does not exist for the idpName: " + idpName + ", and " +
                        "federatedUserId: " + federatedUserId + ", of the user: " + userName);
            }
            throw handleFederatedAssociationManagerClientException(INVALID_FEDERATED_ASSOCIATION, null, true);
        }
    }

    private boolean isValidFederatedAssociation(String userName, String idpName, String federatedUserId)
            throws FederatedAssociationManagerException {

        FederatedAssociation[] federatedUserAccountAssociationDTOS = getFederatedAssociationsOfUser(userName);
        if (federatedUserAccountAssociationDTOS != null) {
            for (FederatedAssociation eachFederatedAssociation : federatedUserAccountAssociationDTOS) {
                if (idpName.equals(getResolvedIdPName(userName, eachFederatedAssociation.getIdpId()))
                        && federatedUserId.equals(eachFederatedAssociation.getFederatedUserId())) {
                    return true;
                }
            }
        }
        return false;
    }

    private String getResolvedIdPName(String userName, String idpId) throws FederatedAssociationManagerException {

        return getIdentityProviderName(MultitenantUtils.getTenantDomain(userName), idpId);
    }

    private void validateUser(String username, int tenantId) throws FederatedAssociationManagerException {

        if (StringUtils.isBlank(username)) {
            if (log.isDebugEnabled()) {
                log.error("Username cannot be empty.");
            }
            throw handleFederatedAssociationManagerClientException(INVALID_USER_IDENTIFIER_PROVIDED, null, true);
        }
        try {
            UserStoreManager userStoreManager = IdentityUserProfileServiceDataHolder.getInstance().getRealmService()
                    .getTenantUserRealm(tenantId).getUserStoreManager();
            if (!userStoreManager.isExistingUser(removeTenantDomain(username))) {
                if (log.isDebugEnabled()) {
                    log.error("UserNotFound: User: " + username + ", does not exist in tenant: " +
                            MultitenantUtils.getTenantDomain(username));
                }
                throw handleFederatedAssociationManagerClientException(INVALID_USER_IDENTIFIER_PROVIDED, null, true);
            }
        } catch (UserStoreException e) {
            if (log.isDebugEnabled()) {
                String msg = "Error occurred while verifying the existence of the user: " + username + ", in tenant: "
                        + MultitenantUtils.getTenantDomain(username);
                log.error(msg);
            }
            throw handleFederatedAssociationManagerServerException(ERROR_WHILE_GETTING_THE_USER, e, true);
        }
    }

    private FederatedAssociationManagerClientException handleFederatedAssociationManagerClientException
            (FederatedAssociationConstants.ErrorMessages errorMessages, Throwable throwable, boolean
                    messageWithCode) {

        String message;
        if (messageWithCode) {
            message = errorMessages.toString();
        } else {
            message = errorMessages.getDescription();
        }

        if (throwable == null) {
            return new FederatedAssociationManagerClientException(String.valueOf(errorMessages.getCode()), message);
        } else {
            return new FederatedAssociationManagerClientException(String.valueOf(errorMessages.getCode()), message,
                    throwable);
        }
    }

    private FederatedAssociationManagerServerException handleFederatedAssociationManagerServerException
            (FederatedAssociationConstants.ErrorMessages errorMessages, Throwable throwable, boolean
                    messageWithCode) {

        String message;
        if (messageWithCode) {
            message = errorMessages.toString();
        } else {
            message = errorMessages.getDescription();
        }

        if (throwable == null) {
            return new FederatedAssociationManagerServerException(String.valueOf(errorMessages.getCode()), message);
        } else {
            return new FederatedAssociationManagerServerException(String.valueOf(errorMessages.getCode()), message,
                    throwable);
        }
    }

    private String getIdentityProviderId(String tenantDomain, AssociatedAccountDTO associatedAccount)
            throws FederatedAssociationManagerServerException {

        try {
            IdpManager idpManager = IdentityUserProfileServiceDataHolder.getInstance().getIdpManager();
            if (idpManager != null) {
                IdentityProvider identityProvider = idpManager.getIdPByName(associatedAccount.getIdentityProviderName(),
                        tenantDomain);
                return identityProvider.getResourceId();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("The IdpManager service is not available in the runtime");
                }
                throw handleFederatedAssociationManagerServerException(ERROR_WHILE_RESOLVING_IDENTITY_PROVIDERS,
                        null, true);
            }
        } catch (IdentityProviderManagementException e) {
            if (log.isDebugEnabled()) {
                log.debug("Could not resolve the identity provider for the name: "
                        + associatedAccount.getIdentityProviderName() + ", in the tenant domain: " + tenantDomain);
            }
            throw handleFederatedAssociationManagerServerException(ERROR_WHILE_RESOLVING_IDENTITY_PROVIDERS,
                    null, true);
        }
    }

    private String getIdentityProviderName(String tenantDomain, String idpId)
            throws FederatedAssociationManagerException {

        try {
            IdpManager idpManager = IdentityUserProfileServiceDataHolder.getInstance().getIdpManager();
            if (idpManager != null) {
                IdentityProvider identityProvider = idpManager.getIdPById(idpId, tenantDomain);
                return identityProvider.getIdentityProviderName();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("The IdpManager service is not available in the runtime");
                }
                throw handleFederatedAssociationManagerServerException(ERROR_WHILE_RESOLVING_IDENTITY_PROVIDERS,
                        null, true);
            }
        } catch (IdentityProviderManagementException e) {
            if (log.isDebugEnabled()) {
                log.debug("Could not resolve the identity provider for the id: " + idpId
                        + ", in the tenant domain: " + tenantDomain);
            }
            throw handleFederatedAssociationManagerServerException(ERROR_WHILE_RESOLVING_IDENTITY_PROVIDERS,
                    null, true);
        }
    }
}
