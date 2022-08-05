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

import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.user.profile.mgt.AssociatedAccountDTO;
import org.wso2.carbon.identity.user.profile.mgt.UserProfileException;
import org.wso2.carbon.identity.user.profile.mgt.association.federation.exception.FederatedAssociationManagerException;
import org.wso2.carbon.identity.user.profile.mgt.association.federation.model.FederatedAssociation;

import java.util.List;

/**
 * The service which exposes federated account association management APIs.
 */
public interface FederatedAssociationManager {

    /**
     * Associate the given user with the given IdP and the federated user id.
     *
     * @param user            User.
     * @param idpName         Identity Provider Name.
     * @param federatedUserId Federated Identity ID.
     * @throws UserProfileException
     */
    void createFederatedAssociation(User user, String idpName, String federatedUserId)
            throws FederatedAssociationManagerException;

    /**
     * Associate the given user with the given IdP and the federated user id.
     *
     * @param user            Local user.
     * @param idpId           Identity provider uuid.
     * @param federatedUserId Federated user id.
     * @throws FederatedAssociationManagerException while creating the federated association.
     */
    default void createFederatedAssociationWithIdpResourceId(User user, String idpId, String federatedUserId)
            throws FederatedAssociationManagerException {

    }

    /**
     * Get associated local user for the given federated user.
     *
     * @param tenantDomain      Tenant domain.
     * @param idpId             UUID of the IDP.
     * @param federatedUserId   UUID of the federated user.
     * @return  Associated local user name.
     * @throws FederatedAssociationManagerException
     */
    default User getAssociatedLocalUser(String tenantDomain, String idpId, String federatedUserId)
            throws FederatedAssociationManagerException {

        return null;
    }

    /**
     * Return the username of the local user associated with the given federated identifier.
     *
     * @param tenantDomain    Tenant domain.
     * @param idpName         Identity Provider Name.
     * @param federatedUserId Federated Identity ID.
     * @return the username of the user associated with.
     * @throws FederatedAssociationManagerException
     */
    String getUserForFederatedAssociation(String tenantDomain, String idpName, String federatedUserId)
            throws FederatedAssociationManagerException;

    /**
     * Return an array of federated associations associated with the given user.
     *
     * @param user User.
     * @return An array of federated associations which contains the federated identifier info.
     * @throws FederatedAssociationManagerException
     */
    FederatedAssociation[] getFederatedAssociationsOfUser(User user) throws FederatedAssociationManagerException;

    /**
     * Returns a list of federated associations associated with the given user.
     *
     * @param tenantId Id of the tenant.
     * @param domainFreeUsername username without domain.
     * @return An array of federated associations which contains the federated identifier info.
     * @throws FederatedAssociationManagerException
     */
    default List<AssociatedAccountDTO> getFederatedAssociationsOfUser(
            int tenantId, String userStoreDomain, String domainFreeUsername) throws
            FederatedAssociationManagerException {

        return null;
    }

    /**
     * Remove the federated association with the given federated association for the given user.
     *
     * @param user            User.
     * @param idpName         Name of the IdP.
     * @param federatedUserId Federated user id of the federated association.
     * @throws FederatedAssociationManagerException
     */
    void deleteFederatedAssociation(User user, String idpName, String federatedUserId)
            throws FederatedAssociationManagerException;

    /**
     * Remove the federated association with the given federated association for the given user.
     *
     * @param user                   User.
     * @param federatedAssociationId The unique identifier value for a given federated association.
     * @throws FederatedAssociationManagerException
     */
    void deleteFederatedAssociation(User user, String federatedAssociationId)
            throws FederatedAssociationManagerException;

    /**
     * Remove the federated associations of the given user.
     *
     * @param user User.
     * @throws FederatedAssociationManagerException
     */
    void deleteFederatedAssociation(User user) throws FederatedAssociationManagerException;
}
