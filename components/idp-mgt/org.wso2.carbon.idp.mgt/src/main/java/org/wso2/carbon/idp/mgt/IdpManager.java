/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.idp.mgt;

import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.LocalRole;
import org.wso2.carbon.identity.application.common.model.ProvisioningConnectorConfig;
import org.wso2.carbon.identity.application.common.model.RoleMapping;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IdpManager {

    /**
     * Retrieves resident Identity provider for a given tenant
     *
     * @param tenantDomain Tenant domain whose resident IdP is requested
     * @return <code>LocalIdentityProvider</code>
     * @throws IdentityProviderManagementException Error when getting Resident Identity Providers
     */
    IdentityProvider getResidentIdP(String tenantDomain) throws IdentityProviderManagementException;


    /**
     * Add Resident Identity provider for a given tenant
     *
     * @param identityProvider <code>IdentityProvider</code>
     * @param tenantDomain     Tenant domain whose resident IdP is requested
     * @throws IdentityProviderManagementException Error when adding Resident Identity Provider
     */
    void addResidentIdP(IdentityProvider identityProvider, String tenantDomain) throws
            IdentityProviderManagementException;

    /**
     * Update Resident Identity provider for a given tenant
     *
     * @param identityProvider <code>IdentityProvider</code>
     * @param tenantDomain     Tenant domain whose resident IdP is requested
     * @throws IdentityProviderManagementException Error when updating Resident Identity Provider
     */
    void updateResidentIdP(IdentityProvider identityProvider, String tenantDomain) throws
            IdentityProviderManagementException;

    /**
     * Retrieves registered Identity providers for a given tenant
     *
     * @param tenantDomain Tenant domain whose IdP names are requested
     * @return Set of <code>IdentityProvider</code>. IdP names, primary IdP and home realm
     * identifiers of each IdP
     * @throws IdentityProviderManagementException Error when getting list of Identity Providers
     */
    List<IdentityProvider> getIdPs(String tenantDomain) throws IdentityProviderManagementException;

    /**
     * Retrieves registered Enabled Identity providers for a given tenant
     *
     * @param tenantDomain Tenant domain whose IdP names are requested
     * @return Set of <code>IdentityProvider</code>. IdP names, primary IdP and home realm
     * identifiers of each IdP
     * @throws IdentityProviderManagementException Error when getting list of Identity Providers
     */
    List<IdentityProvider> getEnabledIdPs(String tenantDomain) throws IdentityProviderManagementException;

    /**
     * @param idPName
     * @param tenantDomain
     * @param ignoreFileBasedIdps
     * @return
     * @throws IdentityProviderManagementException
     */
    IdentityProvider getIdPByName(String idPName, String tenantDomain,
                                  boolean ignoreFileBasedIdps) throws IdentityProviderManagementException;

    /**
     * Returns IDP with passed ID.
     * @param id ID of the IDP.
     * @param tenantDomain Tenant domain of the IDP.
     * @param ignoreFileBasedIdps Whether to ignore file based idps or not.
     * @return IDP.
     * @throws IdentityProviderManagementException IdentityProviderManagementException
     */
    default IdentityProvider getIdPById(String id, String tenantDomain,
                                boolean ignoreFileBasedIdps) throws IdentityProviderManagementException {
        return null;
    }

    /**
     * @param idPName
     * @param tenantDomain
     * @param ignoreFileBasedIdps
     * @return
     * @throws IdentityProviderManagementException
     */
    IdentityProvider getEnabledIdPByName(String idPName, String tenantDomain,
                                         boolean ignoreFileBasedIdps) throws IdentityProviderManagementException;

    /**
     * Retrieves Identity provider information about a given tenant by Identity Provider name
     *
     * @param idPName      Unique name of the Identity provider of whose information is requested
     * @param tenantDomain Tenant domain whose information is requested
     * @return <code>IdentityProvider</code> Identity Provider information
     * @throws IdentityProviderManagementException Error when getting Identity Provider
     *                                                information by IdP name
     */
    IdentityProvider getIdPByName(String idPName, String tenantDomain) throws IdentityProviderManagementException;

    /**
     * Returns IDP with given IDP.
     *
     * @param id           ID of the IDP.
     * @param tenantDomain Tenant domain of the IDP.
     * @return Identity provider with given ID.
     * @throws IdentityProviderManagementException IdentityProviderManagementException.
     */
    default IdentityProvider getIdPById(String id, String tenantDomain) throws IdentityProviderManagementException {

        return null;
    }

    /**
     * @param property     IDP authenticator property (E.g.: IdPEntityId)
     * @param value        Value associated with given Property
     * @param tenantDomain
     * @return <code>IdentityProvider</code> Identity Provider information
     * @throws IdentityProviderManagementException Error when getting Identity Provider
     *                                                information by authenticator property value
     */
    IdentityProvider getIdPByAuthenticatorPropertyValue(String property, String value, String tenantDomain, boolean
            ignoreFileBasedIdps) throws IdentityProviderManagementException;

    /**
     * Retrieves Enabled Identity provider information about a given tenant by Identity Provider name
     *
     * @param idPName      Unique name of the Identity provider of whose information is requested
     * @param tenantDomain Tenant domain whose information is requested
     * @return <code>IdentityProvider</code> Identity Provider information
     * @throws IdentityProviderManagementException Error when getting Identity Provider
     *                                                information by IdP name
     */
    IdentityProvider getEnabledIdPByName(String idPName, String tenantDomain) throws
            IdentityProviderManagementException;

    /**
     * Retrieves Identity provider information about a given tenant by realm identifier
     *
     * @param realmId      Unique realm identifier of the Identity provider of whose information is
     *                     requested
     * @param tenantDomain Tenant domain whose information is requested
     * @throws IdentityProviderManagementException Error when getting Identity Provider
     *                                                information by IdP home realm identifier
     */
    IdentityProvider getIdPByRealmId(String realmId, String tenantDomain) throws IdentityProviderManagementException;

    /**
     * Retrieves Enabled Identity provider information about a given tenant by realm identifier
     *
     * @param realmId      Unique realm identifier of the Identity provider of whose information is
     *                     requested
     * @param tenantDomain Tenant domain whose information is requested
     * @throws IdentityProviderManagementException Error when getting Identity Provider
     *                                                information by IdP home realm identifier
     */
    IdentityProvider getEnabledIdPByRealmId(String realmId, String tenantDomain) throws
            IdentityProviderManagementException;

    /**
     * Retrieves Identity provider information about a given tenant
     *
     * @param idPName      Unique Name of the IdP to which the given IdP claim URIs need to be mapped
     * @param tenantDomain The tenant domain of whose local claim URIs to be mapped
     * @param idPClaimURIs IdP claim URIs which need to be mapped to tenant's local claim URIs
     * @throws IdentityProviderManagementException Error when getting claim mappings
     */
    Set<ClaimMapping> getMappedLocalClaims(String idPName, String tenantDomain, List<String> idPClaimURIs) throws
            IdentityProviderManagementException;

    /**
     * Retrieves Identity provider information about a given tenant
     *
     * @param idPName      Unique Name of the IdP to which the given IdP claim URIs need to be mapped
     * @param tenantDomain The tenant domain of whose local claim URIs to be mapped
     * @param idPClaimURIs IdP claim URIs which need to be mapped to tenant's local claim URIs
     * @throws IdentityProviderManagementException Error when getting claim mappings
     */
    Map<String, String> getMappedLocalClaimsMap(String idPName, String tenantDomain, List<String> idPClaimURIs) throws
            IdentityProviderManagementException;

    /**
     * Retrieves Identity provider information about a given tenant
     *
     * @param idPName        Unique Name of the IdP to which the given local claim URIs need to be mapped
     * @param tenantDomain   The tenant domain of whose local claim URIs to be mapped
     * @param localClaimURIs Local claim URIs which need to be mapped to IdP's claim URIs
     * @throws IdentityProviderManagementException Error when getting claim mappings
     */
    Set<ClaimMapping> getMappedIdPClaims(String idPName, String tenantDomain, List<String> localClaimURIs) throws
            IdentityProviderManagementException;

    /**
     * Retrieves Identity provider information about a given tenant
     *
     * @param idPName        Unique Name of the IdP to which the given local claim URIs need to be mapped
     * @param tenantDomain   The tenant domain of whose local claim URIs to be mapped
     * @param localClaimURIs Local claim URIs which need to be mapped to IdP's claim URIs
     * @throws IdentityProviderManagementException Error when getting claim mappings
     */
    Map<String, String> getMappedIdPClaimsMap(String idPName, String tenantDomain, List<String> localClaimURIs) throws
            IdentityProviderManagementException;

    /**
     * Retrieves Identity provider information about a given tenant
     *
     * @param idPName      Unique name of the IdP to which the given IdP roles need to be mapped
     * @param tenantDomain The tenant domain of whose local roles to be mapped
     * @param idPRoles     IdP roles which need to be mapped to local roles
     * @throws IdentityProviderManagementException Error when getting role mappings
     */
    Set<RoleMapping> getMappedLocalRoles(String idPName, String tenantDomain, String[] idPRoles) throws
            IdentityProviderManagementException;

    /**
     * Retrieves Identity provider information about a given tenant
     *
     * @param idPName      Unique name of the IdP to which the given IdP roles need to be mapped
     * @param tenantDomain The tenant domain of whose local roles to be mapped
     * @param idPRoles     IdP roles which need to be mapped to local roles
     * @throws IdentityProviderManagementException Error when getting role mappings
     */
    Map<String, LocalRole> getMappedLocalRolesMap(String idPName, String tenantDomain, String[] idPRoles) throws
            IdentityProviderManagementException;

    /**
     * Retrieves Identity provider information about a given tenant
     *
     * @param idPName      Unique name of the IdP to which the given local roles need to be mapped
     * @param tenantDomain The tenant domain of whose local roles need to be mapped
     * @param localRoles   Local roles which need to be mapped to IdP roles
     * @throws IdentityProviderManagementException Error when getting role mappings
     */
    Set<RoleMapping> getMappedIdPRoles(String idPName, String tenantDomain, LocalRole[] localRoles) throws
            IdentityProviderManagementException;

    /**
     * Retrieves Identity provider information about a given tenant
     *
     * @param idPName      Unique name of the IdP to which the given local roles need to be mapped
     * @param tenantDomain The tenant domain of whose local roles need to be mapped
     * @param localRoles   Local roles which need to be mapped to IdP roles
     * @throws IdentityProviderManagementException Error when getting role mappings
     */
    Map<LocalRole, String> getMappedIdPRolesMap(String idPName, String tenantDomain, LocalRole[] localRoles) throws
            IdentityProviderManagementException;

    /**
     * Adds an Identity Provider to the given tenant
     *
     * @param identityProvider new Identity Provider information
     * @throws IdentityProviderManagementException Error when adding Identity Provider
     *                                                information
     */
    void addIdP(IdentityProvider identityProvider, String tenantDomain) throws IdentityProviderManagementException;

    /**
     * Deletes an Identity Provider from a given tenant
     *
     * @param idPName Name of the IdP to be deleted
     * @throws IdentityProviderManagementException Error when deleting Identity Provider
     *                                                information
     */
    void deleteIdP(String idPName, String tenantDomain) throws IdentityProviderManagementException;

    /**
     * Updates a given Identity Provider information
     *
     * @param oldIdPName          existing Identity Provider name
     * @param newIdentityProvider new IdP information
     * @throws IdentityProviderManagementException Error when updating Identity Provider
     *                                                information
     */
    void updateIdP(String oldIdPName, IdentityProvider newIdentityProvider, String tenantDomain) throws
            IdentityProviderManagementException;

    /**
     * Get the authenticators registered in the system.
     *
     * @return <code>FederatedAuthenticatorConfig</code> array.
     * @throws IdentityProviderManagementException Error when getting authenticators registered
     *                                                in the system
     */
    FederatedAuthenticatorConfig[] getAllFederatedAuthenticators() throws IdentityProviderManagementException;

    /**
     * Get the Provisioning Connectors registered in the system.
     *
     * @return <code>ProvisioningConnectorConfig</code> array.
     * @throws IdentityProviderManagementException
     */
    ProvisioningConnectorConfig[] getAllProvisioningConnectors() throws IdentityProviderManagementException;

}
