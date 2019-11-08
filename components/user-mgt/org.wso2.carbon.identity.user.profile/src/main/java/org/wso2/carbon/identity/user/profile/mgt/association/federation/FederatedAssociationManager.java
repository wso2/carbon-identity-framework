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

import org.wso2.carbon.identity.user.profile.mgt.UserProfileException;
import org.wso2.carbon.identity.user.profile.mgt.association.federation.exception.FederatedAssociationManagerException;
import org.wso2.carbon.identity.user.profile.mgt.association.federation.model.FederatedAssociation;

/**
 * The service which exposes federated account association management APIs.
 */
public interface FederatedAssociationManager {

    /**
     * Associate the given user with the given IdP and the federated user id.
     *
     * @param userId          Fully qualified user name of the user.
     * @param idpName         Identity Provider Name.
     * @param federatedUserId Federated Identity ID.
     * @throws UserProfileException
     */
    void createFederatedAssociation(String userId, String idpName, String federatedUserId)
            throws FederatedAssociationManagerException;

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
     * @param userId Fully qualified user name of the user.
     * @return An array of federated associations which contains the federated identifier info.
     * @throws FederatedAssociationManagerException
     */
    FederatedAssociation[] getFederatedAssociationsOfUser(String userId) throws FederatedAssociationManagerException;

    /**
     * Remove the federated association with the given federated association for the given user.
     *
     * @param userId          Fully qualified user name of the user.
     * @param idpName         Name of the IdP.
     * @param federatedUserId Federated user id of the federated association.
     * @throws FederatedAssociationManagerException
     */
    void deleteFederatedAssociation(String userId, String idpName, String federatedUserId)
            throws FederatedAssociationManagerException;

    /**
     * Remove the federated association with the given federated association for the given user.
     *
     * @param userId                 Fully qualified user name of the user.
     * @param federatedAssociationId The unique identifier value for a given federated association.
     * @throws FederatedAssociationManagerException
     */
    void deleteFederatedAssociation(String userId, String federatedAssociationId)
            throws FederatedAssociationManagerException;

    /**
     * Remove the federated associations of the given user.
     *
     * @param userId Fully qualified user name of the user.
     * @throws FederatedAssociationManagerException
     */
    void deleteFederatedAssociation(String userId) throws FederatedAssociationManagerException;
}
