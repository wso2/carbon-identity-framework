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
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.user.profile.mgt.AssociatedAccountDTO;
import org.wso2.carbon.identity.user.profile.mgt.UserProfileException;
import org.wso2.carbon.identity.user.profile.mgt.association.federation.constant.FederatedAssociationConstants;
import org.wso2.carbon.identity.user.profile.mgt.association.federation.exception.FederatedAssociationManagerClientException;
import org.wso2.carbon.identity.user.profile.mgt.association.federation.exception.FederatedAssociationManagerException;
import org.wso2.carbon.identity.user.profile.mgt.association.federation.exception.FederatedAssociationManagerServerException;
import org.wso2.carbon.identity.user.profile.mgt.association.federation.model.AssociatedIdentityProvider;
import org.wso2.carbon.identity.user.profile.mgt.association.federation.model.FederatedAssociation;
import org.wso2.carbon.identity.user.profile.mgt.dao.UserProfileMgtDAO;
import org.wso2.carbon.identity.user.profile.mgt.internal.IdentityUserProfileServiceDataHolder;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdpManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.List;

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
import static org.wso2.carbon.identity.user.profile.mgt.association.federation.constant.FederatedAssociationConstants.ErrorMessages.INVALID_IDP_PROVIDED;
import static org.wso2.carbon.identity.user.profile.mgt.association.federation.constant.FederatedAssociationConstants.ErrorMessages.INVALID_TENANT_DOMAIN_PROVIDED;
import static org.wso2.carbon.identity.user.profile.mgt.association.federation.constant.FederatedAssociationConstants.ErrorMessages.INVALID_TENANT_ID_PROVIDED;
import static org.wso2.carbon.identity.user.profile.mgt.association.federation.constant.FederatedAssociationConstants.ErrorMessages.INVALID_USER_IDENTIFIER_PROVIDED;
import static org.wso2.carbon.identity.user.profile.mgt.association.federation.constant.FederatedAssociationConstants.ErrorMessages.INVALID_USER_STORE_DOMAIN_PROVIDED;

public class FederatedAssociationManagerImpl implements FederatedAssociationManager {

    private static final Log log = LogFactory.getLog(FederatedAssociationManagerImpl.class);

    @Override
    public void createFederatedAssociation(User user, String idpName, String federatedUserId)
            throws FederatedAssociationManagerException {

        validateUserObject(user);
        int tenantId = getValidatedTenantId(user);
        validateUserExistence(user, tenantId);
        validateIfFederatedUserAccountAlreadyAssociated(user.getTenantDomain(), idpName, federatedUserId);
        try {
            UserProfileMgtDAO.getInstance().createAssociation(tenantId, user.getUserStoreDomain(), user.getUserName(),
                    idpName, federatedUserId);
        } catch (UserProfileException e) {
            throw handleFederatedAssociationManagerServerException(ERROR_WHILE_CREATING_FEDERATED_ASSOCIATION_OF_USER
                    , e, false);
        }
    }

    @Override
    public void createFederatedAssociationWithIdpResourceId(User user, String idpId, String federatedUserId)
            throws FederatedAssociationManagerException {

        // Resolve idp name from idp uuid.
        String idpName = getIdentityProviderNameByResourceId(idpId);
        if (StringUtils.isEmpty(idpName)) {
            throw handleFederatedAssociationManagerClientException(INVALID_IDP_PROVIDED, null, true);
        }
        createFederatedAssociation(user, idpName, federatedUserId);
    }

    @Override
    public User getAssociatedLocalUser(String tenantDomain, String idpId, String federatedUserId)
            throws FederatedAssociationManagerException {

        // Resolve idp name from IDP UUID.
        String idpName = getIdentityProviderNameByResourceId(idpId);
        if (StringUtils.isEmpty(idpName)) {
            throw handleFederatedAssociationManagerClientException(INVALID_IDP_PROVIDED, null, true);
        }
        String usernameWithDomain = getUserForFederatedAssociation(tenantDomain, idpName, federatedUserId);
        if (StringUtils.isNotBlank(usernameWithDomain)) {
            User user = new User();
            user.setUserStoreDomain(UserCoreUtil.extractDomainFromName(usernameWithDomain));
            user.setUserName(UserCoreUtil.removeDomainFromName(usernameWithDomain));
            user.setTenantDomain(tenantDomain);
            return user;
        }
        return null;
    }

    @Override
    public String getUserForFederatedAssociation(String tenantDomain, String idpName, String federatedUserId)
            throws FederatedAssociationManagerException {

        int tenantId = getValidatedTenantIdFromDomain(tenantDomain);
        try {
            return UserProfileMgtDAO.getInstance().getUserAssociatedFor(tenantId, idpName, federatedUserId);
        } catch (UserProfileException e) {
            if (log.isDebugEnabled()) {
                String msg = "Error while retrieving user associated for federated IdP: " + idpName + ", with " +
                        "federation identifier: " + federatedUserId + ", in tenant: "
                        + CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
                log.debug(msg);
            }
            throw handleFederatedAssociationManagerServerException(ERROR_WHILE_GETTING_USER_FOR_FEDERATED_ASSOCIATION,
                    e, true);
        }
    }

    @Override
    public FederatedAssociation[] getFederatedAssociationsOfUser(User user)
            throws FederatedAssociationManagerException {

        validateUserObject(user);
        int tenantId = getValidatedTenantId(user);
        validateUserExistence(user, tenantId);
        try {
            List<FederatedAssociation> federatedAssociations = new ArrayList<>();
            List<AssociatedAccountDTO> associatedAccountDTOS = UserProfileMgtDAO.getInstance()
                    .getAssociatedFederatedAccountsForUser(tenantId, user.getUserStoreDomain(), user.getUserName());
            for (AssociatedAccountDTO associatedAccount : associatedAccountDTOS) {
                AssociatedIdentityProvider idp = getAssociatedIdentityProvider(user.getTenantDomain(),
                        associatedAccount.getIdentityProviderName());
                federatedAssociations.add(
                        new FederatedAssociation(
                                associatedAccount.getId(),
                                idp,
                                associatedAccount.getUsername()
                        )
                );
            }
            return federatedAssociations.toArray(new FederatedAssociation[0]);
        } catch (UserProfileException e) {
            if (log.isDebugEnabled()) {
                String msg = "Error while retrieving federated account associations of user: "
                        + user.toFullQualifiedUsername();
                log.debug(msg);
            }
            throw handleFederatedAssociationManagerServerException(ERROR_WHILE_RETRIEVING_FEDERATED_ASSOCIATION_OF_USER,
                    e, true);
        }
    }

    @Override
    public List<AssociatedAccountDTO> getFederatedAssociationsOfUser(
            int tenantId, String userStoreDomain, String domainFreeUsername)
            throws FederatedAssociationManagerException {

        if (MultitenantConstants.INVALID_TENANT_ID == tenantId) {
            if (log.isDebugEnabled()) {
                log.debug("Invalid tenant id is received to retrieve federated associations.");
            }
            throw handleFederatedAssociationManagerClientException(INVALID_TENANT_ID_PROVIDED, null, true);
        }
        if (StringUtils.isEmpty(userStoreDomain)) {
            throw handleFederatedAssociationManagerClientException(INVALID_USER_STORE_DOMAIN_PROVIDED, null, true);
        }
        if (StringUtils.isEmpty(domainFreeUsername)) {
            throw handleFederatedAssociationManagerClientException(INVALID_USER_IDENTIFIER_PROVIDED, null, true);
        }

        try {
            return UserProfileMgtDAO.getInstance()
                    .getAssociatedFederatedAccountsForUser(tenantId, userStoreDomain, domainFreeUsername);
        } catch (UserProfileException e) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Error while retrieving federated account associations for user: %s of " +
                        "tenantId: %s", domainFreeUsername, tenantId));
            }
            throw handleFederatedAssociationManagerServerException(
                    ERROR_WHILE_RETRIEVING_FEDERATED_ASSOCIATION_OF_USER, e, true);
        }
    }

    @Override
    public void deleteFederatedAssociation(User user, String idpName, String federatedUserId)
            throws FederatedAssociationManagerException {

        validateUserObject(user);
        int tenantId = getValidatedTenantId(user);
        validateFederatedAssociation(user, idpName, federatedUserId);
        try {
            UserProfileMgtDAO.getInstance().deleteAssociation(tenantId, user.getUserStoreDomain(), user.getUserName(),
                    idpName, federatedUserId);
        } catch (UserProfileException e) {
            if (log.isDebugEnabled()) {
                String msg = "Error while removing the federated association with idpId: " + idpName + ", and " +
                        "federatedUserId: " + federatedUserId + ", for user: " + user.toFullQualifiedUsername();
                log.debug(msg);
            }
            throw handleFederatedAssociationManagerServerException(ERROR_WHILE_DELETING_FEDERATED_ASSOCIATION_OF_USER
                    , e, true);
        }
    }

    @Override
    public void deleteFederatedAssociation(User user, String federatedAssociationId)
            throws FederatedAssociationManagerException {

        validateUserObject(user);
        validateFederatedAssociation(user, federatedAssociationId);
        try {
            UserProfileMgtDAO.getInstance().deleteFederatedAssociation(user.getUserStoreDomain(), user.getUserName(),
                    federatedAssociationId);
        } catch (UserProfileException e) {
            if (log.isDebugEnabled()) {
                String msg = "Error while removing the federated association: " + federatedAssociationId
                        + ", for user: " + user.toFullQualifiedUsername();
                log.debug(msg, e);
            }
            throw handleFederatedAssociationManagerServerException(ERROR_WHILE_DELETING_FEDERATED_ASSOCIATION_OF_USER
                    , e, true);
        }
    }

    @Override
    public void deleteFederatedAssociation(User user) throws FederatedAssociationManagerException {

        validateUserObject(user);
        int tenantId = getValidatedTenantId(user);
        validateExistenceOfFederatedAssociations(user);
        try {
            UserProfileMgtDAO.getInstance().deleteFederatedAssociation(tenantId, user.getUserStoreDomain(),
                    user.getUserName());
        } catch (UserProfileException e) {
            if (log.isDebugEnabled()) {
                String msg = "Error while removing the federated associations of user: "
                        + user.toFullQualifiedUsername();
                log.debug(msg, e);
            }
            throw handleFederatedAssociationManagerServerException(ERROR_WHILE_DELETING_FEDERATED_ASSOCIATION_OF_USER
                    , e, true);
        }
    }

    private void validateUserObject(User user) throws FederatedAssociationManagerException {

        boolean isValidUserObject = (user != null && isRequiredUserParametersPresent(user));
        if (!isValidUserObject) {
            if (log.isDebugEnabled()) {
                log.debug("Either provided user is null or missing user parameters.");
            }
            throw handleFederatedAssociationManagerClientException(INVALID_USER_IDENTIFIER_PROVIDED, null, true);
        }
    }

    private boolean isRequiredUserParametersPresent(User user) {

        return !StringUtils.isEmpty(user.getTenantDomain()) && !StringUtils.isEmpty(user.getUserStoreDomain())
                && !StringUtils.isEmpty(user.getUserName());
    }

    private void validateExistenceOfFederatedAssociations(User user) throws FederatedAssociationManagerException {

        if (!isValidFederatedAssociationsExist(user)) {
            if (log.isDebugEnabled()) {
                log.debug("Valid federated associations does not exist for the user: "
                        + user.toFullQualifiedUsername());
            }
            throw handleFederatedAssociationManagerClientException(FEDERATED_ASSOCIATION_DOES_NOT_EXISTS, null, true);
        }
    }

    private boolean isValidFederatedAssociationsExist(User user) throws FederatedAssociationManagerException {

        FederatedAssociation[] federatedUserAccountAssociationDTOS = getFederatedAssociationsOfUser(user);
        return !ArrayUtils.isEmpty(federatedUserAccountAssociationDTOS);
    }

    private void validateFederatedAssociation(User user, String federatedAssociationId)
            throws FederatedAssociationManagerException {

        if (StringUtils.isEmpty(federatedAssociationId)
                || !isValidFederatedAssociation(user, federatedAssociationId)) {
            if (log.isDebugEnabled()) {
                log.debug("A valid federated association does not exist for the Id: " + federatedAssociationId
                        + ", of the user: " + user.toFullQualifiedUsername());
            }
            throw handleFederatedAssociationManagerClientException(INVALID_FEDERATED_ASSOCIATION, null, true);
        }
    }

    private boolean isValidFederatedAssociation(User user, String federatedAssociationId)
            throws FederatedAssociationManagerException {

        FederatedAssociation[] federatedUserAccountAssociationDTOS
                = getFederatedAssociationsOfUser(user);
        if (federatedUserAccountAssociationDTOS != null) {
            for (FederatedAssociation federatedUserAccountAssociationDTO : federatedUserAccountAssociationDTOS) {
                if (federatedAssociationId.equals(federatedUserAccountAssociationDTO.getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    private int getValidatedTenantId(User user) throws FederatedAssociationManagerException {

        int tenantId;
        RealmService realmService;
        try {
            realmService = IdentityUserProfileServiceDataHolder.getInstance().getRealmService();
            tenantId = realmService.getTenantManager().getTenantId(user.getTenantDomain());
        } catch (UserStoreException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while getting the tenant Id for the tenant domain: "
                        + user.getTenantDomain());
            }
            throw handleFederatedAssociationManagerServerException(ERROR_WHILE_WORKING_WITH_FEDERATED_ASSOCIATIONS, e,
                    false);
        }
        if (MultitenantConstants.INVALID_TENANT_ID == tenantId) {
            if (log.isDebugEnabled()) {
                log.debug("Invalid tenant id is resolved for the tenant domain: " + user.getTenantDomain());
            }
            throw handleFederatedAssociationManagerClientException(INVALID_TENANT_DOMAIN_PROVIDED, null, true);
        }
        return tenantId;
    }

    private int getValidatedTenantIdFromDomain(String tenantDomain) throws FederatedAssociationManagerException {

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
        if (MultitenantConstants.INVALID_TENANT_ID == tenantId) {
            if (log.isDebugEnabled()) {
                log.debug("Invalid tenant id is resolved for the tenant domain: " + tenantDomain);
            }
            throw handleFederatedAssociationManagerClientException(INVALID_TENANT_DOMAIN_PROVIDED, null, true);
        }
        return tenantId;
    }

    private void validateIfFederatedUserAccountAlreadyAssociated(String tenantDomain, String idpId,
                                                                 String federatedUserId)
            throws FederatedAssociationManagerException {

        String userAssociated = getUserForFederatedAssociation(tenantDomain, idpId, federatedUserId);
        if (userAssociated != null) {
            if (log.isDebugEnabled()) {
                log.debug("Federated ID: " + federatedUserId + ", for IdP: " + idpId + ", is already associated " +
                        "with the local user account: " + userAssociated + UserCoreConstants
                        .TENANT_DOMAIN_COMBINER + CarbonContext.getThreadLocalCarbonContext().getTenantDomain() + ".");
            }
            throw handleFederatedAssociationManagerClientException(FEDERATED_ASSOCIATION_ALREADY_EXISTS, null, true);
        }
    }

    private void validateFederatedAssociation(User user, String idpName, String federatedUserId)
            throws FederatedAssociationManagerException {

        if (StringUtils.isEmpty(idpName) || StringUtils.isEmpty(federatedUserId)
                || !isValidFederatedAssociation(user, idpName, federatedUserId)) {
            if (log.isDebugEnabled()) {
                log.debug("A valid federated association does not exist for the idpName: " + idpName + ", and " +
                        "federatedUserId: " + federatedUserId + ", of the user: " + user.toFullQualifiedUsername());
            }
            throw handleFederatedAssociationManagerClientException(INVALID_FEDERATED_ASSOCIATION, null, true);
        }
    }

    private boolean isValidFederatedAssociation(User user, String idpName, String federatedUserId)
            throws FederatedAssociationManagerException {

        FederatedAssociation[] federatedUserAccountAssociationDTOS = getFederatedAssociationsOfUser(user);
        if (federatedUserAccountAssociationDTOS != null) {
            for (FederatedAssociation eachFederatedAssociation : federatedUserAccountAssociationDTOS) {
                if (idpName.equals(getResolvedIdPName(user, eachFederatedAssociation.getIdp().getId()))
                        && federatedUserId.equals(eachFederatedAssociation.getFederatedUserId())) {
                    return true;
                }
            }
        }
        return false;
    }

    private String getResolvedIdPName(User user, String idpId) throws FederatedAssociationManagerException {

        return getIdentityProviderName(user.getTenantDomain(), idpId);
    }

    private void validateUserExistence(User user, int tenantId) throws FederatedAssociationManagerException {

        try {
            UserStoreManager userStoreManager = IdentityUserProfileServiceDataHolder.getInstance().getRealmService()
                    .getTenantUserRealm(tenantId).getUserStoreManager();
            if (!userStoreManager.isExistingUser(
                    UserCoreUtil.addDomainToName(user.getUserName(), user.getUserStoreDomain()))) {
                if (log.isDebugEnabled()) {
                    log.error("UserNotFound: userName: " + user.getUserName() + ", in the domain: "
                            + user.getUserStoreDomain() + ", and in the tenant: " + user.getTenantDomain());
                }
                throw handleFederatedAssociationManagerClientException(INVALID_USER_IDENTIFIER_PROVIDED, null, true);
            }
        } catch (UserStoreException e) {
            if (log.isDebugEnabled()) {
                String msg = "Error occurred while verifying the existence of the userName: " + user.getUserName()
                        + ", in the domain: " + user.getUserStoreDomain() + ", and in the tenant: "
                        + user.getTenantDomain();
                log.debug(msg);
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

    private AssociatedIdentityProvider getAssociatedIdentityProvider(String tenantDomain, String identityProviderName)
            throws FederatedAssociationManagerServerException {

        try {
            IdpManager idpManager = IdentityUserProfileServiceDataHolder.getInstance().getIdpManager();
            if (idpManager != null) {
                IdentityProvider idp = idpManager.getIdPByName(identityProviderName, tenantDomain);
                return new AssociatedIdentityProvider(
                        idp.getResourceId(),
                        idp.getIdentityProviderName(),
                        idp.getDisplayName(),
                        idp.getImageUrl());
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
                        + identityProviderName + ", in the tenant domain: " + tenantDomain);
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
                IdentityProvider identityProvider = idpManager.getIdPByResourceId(idpId, tenantDomain, false);
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

    private String getIdentityProviderNameByResourceId(String idpResourceId)
            throws FederatedAssociationManagerException {

        try {
            IdpManager idpManager = IdentityUserProfileServiceDataHolder.getInstance().getIdpManager();
            if (idpManager == null) {
                if (log.isDebugEnabled()) {
                    log.debug("The IdpManager service is not available in the runtime");
                }
                String msg = "Error while retrieving identity provider for the federated association";
                throw new FederatedAssociationManagerException(msg);
            }
            return idpManager.getIdPNameByResourceId(idpResourceId);
        } catch (IdentityProviderManagementException e) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Could not resolve the identity provider name for the id: %s:", idpResourceId));
            }
            String msg = "Error while resolving identity provider name";
            throw new FederatedAssociationManagerException(msg);
        }
    }
}
