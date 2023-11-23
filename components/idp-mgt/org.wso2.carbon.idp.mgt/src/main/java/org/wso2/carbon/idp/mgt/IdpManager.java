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
import org.wso2.carbon.identity.application.common.model.IdPGroup;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.LocalRole;
import org.wso2.carbon.identity.application.common.model.ProvisioningConnectorConfig;
import org.wso2.carbon.identity.application.common.model.RoleMapping;
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.idp.mgt.model.ConnectedAppsResult;
import org.wso2.carbon.idp.mgt.model.IdpSearchResult;

import java.util.Collections;
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
     * Get all basic identity provider information.
     *
     * @param limit        limit per page.
     * @param offset       offset value.
     * @param filter       filter value for IdP search.
     * @param sortOrder    order of IdP ASC/DESC.
     * @param sortBy       the column value need to sort.
     * @param tenantDomain tenantDomain of the user.
     * @return Identity Provider's Basic Information array {@link IdpSearchResult}.
     * @throws IdentityProviderManagementException Server/client related error when getting list of Identity Providers.
     * @deprecated use {@link #getIdPs(Integer, Integer, String, String, String, String, List)} instead.
     */
    IdpSearchResult getIdPs(Integer limit, Integer offset, String filter, String sortOrder, String sortBy,
                            String tenantDomain)
            throws IdentityProviderManagementException;

    /**
     * Get all identity provider's Basic information along with additionally requested information depends on the
     * requiredAttributes.
     *
     * @param limit              Limit per page.
     * @param offset             Offset value.
     * @param filter             Filter value for IdP search.
     * @param sortOrder          Order of IdP ASC/DESC.
     * @param sortBy             The column value need to sort.
     * @param tenantDomain       TenantDomain of the user.
     * @param requiredAttributes Required attributes which needs to be return.
     * @return Identity Provider's Basic Information array along with requested attribute
     * information{@link IdpSearchResult}.
     * @throws IdentityProviderManagementException Server/client related error when getting list of Identity Providers.
     */
    default IdpSearchResult getIdPs(Integer limit, Integer offset, String filter, String sortOrder, String sortBy,
                                    String tenantDomain, List<String> requiredAttributes)
            throws IdentityProviderManagementException {

        return null;
    }

    /**
     * Get basic information of identity providers along with additionally requested information.
     *
     * @param limit              The limit per page.
     * @param offset             The offset value.
     * @param sortOrder          The order of IdP ASC/DESC.
     * @param sortBy             The column value need to sort.
     * @param tenantDomain       The tenant domain of the user.
     * @param requiredAttributes The required attributes which needs to be returned.
     * @param expressionNodes    The list of filters.
     * @return The basic information of identity providers along with requested attributes.
     * @throws IdentityProviderManagementException Server/client related errors when getting list of identity providers.
     */
    default IdpSearchResult getIdPs(Integer limit, Integer offset, String sortOrder, String sortBy, String tenantDomain,
                                    List<String> requiredAttributes, List<ExpressionNode> expressionNodes)
            throws IdentityProviderManagementException {

        return null;
    }

    /**
     * Get all basic identity provider information.
     *
     * @param filter       filter value for IdP search.
     * @param tenantDomain tenant domain whose IdP names are requested.
     * @return filtered Idp Count.
     * @throws IdentityProviderManagementException Error while getting Identity  Providers count.
     */
    int getTotalIdPCount(String filter, String tenantDomain) throws IdentityProviderManagementException;

     /**
     * Retrieves registered Identity providers for a given tenant by Identity Provider name
     *
     * @param tenantDomain Tenant domain whose IdP names are requested
     * @return Set of <code>IdentityProvider</code>. IdP names, primary IdP and home realm
     * identifiers of each IdP
     * @throws IdentityProviderManagementException Error when getting list of Identity Providers
     */
    default List<IdentityProvider> getIdPsSearch(String tenantDomain, String filter)
            throws IdentityProviderManagementException {

	    return null;
    }

    /**
     * Retrieves registered Enabled Identity providers for a given tenant.
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
     * Returns extended IDP with resource ID.
     * @param resourceId            Resource ID of the IDP.
     * @param tenantDomain          Tenant domain of the IDP.
     * @param ignoreFileBasedIdps   Whether to ignore file based idps or not.
     * @return extended IDP.
     * @throws IdentityProviderManagementException IdentityProviderManagementException
     */
    default IdentityProvider getIdPByResourceId(String resourceId, String tenantDomain,
                                        boolean ignoreFileBasedIdps) throws IdentityProviderManagementException {
        return null;
    }

    /**
     * Returns IDP name by with resource ID.
     *
     * @param resourceId Resource ID of the IDP.
     * @return Name of IDP.
     * @throws IdentityProviderManagementException IdentityProviderManagementException.
     */
    default String getIdPNameByResourceId(String resourceId) throws IdentityProviderManagementException {

        return null;
    }

    /**
     * Retrieves registered Identity provider names for a given tenant by Identity Provider Ids.
     *
     * @param tenantDomain Tenant domain.
     * @param idpIds List of identity provider Ids.
     * @return A map of identity provider names keyed by idp id.
     */
    default Map<String, String> getIdPNamesById(String tenantDomain, Set<String> idpIds)
            throws IdentityProviderManagementException {

        return Collections.emptyMap();
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
    @Deprecated
    void addIdP(IdentityProvider identityProvider, String tenantDomain) throws IdentityProviderManagementException;

    /**
     * Adds an Identity Provider to the given tenant.
     *
     * @param identityProvider  New Identity Provider information.
     * @param tenantDomain      Tenant domain of the IDP.
     * @return extended IDP.
     * @throws IdentityProviderManagementException Error when adding Identity Provider information.
     */
    default IdentityProvider addIdPWithResourceId(IdentityProvider identityProvider, String
            tenantDomain) throws IdentityProviderManagementException {
        return null;
    }

    /**
     * Deletes an Identity Provider from a given tenant
     *
     * @param idPName Name of the IdP to be deleted
     * @throws IdentityProviderManagementException Error when deleting Identity Provider
     *                                                information
     */
    @Deprecated
    void deleteIdP(String idPName, String tenantDomain) throws IdentityProviderManagementException;

    /**
     * Delete all Identity Providers from a given tenant.
     *
     * @param tenantDomain Domain of the tenant
     * @throws IdentityProviderManagementException
     */
    default void deleteIdPs(String tenantDomain) throws IdentityProviderManagementException {

    };

    /**
     * Deletes an Identity Provider from a given tenant using its resource ID.
     *
     * @param resourceId    Resource ID of the IdP to be deleted
     * @throws IdentityProviderManagementException Error when deleting Identity Provider information.
     */
    default void deleteIdPByResourceId(String resourceId, String tenantDomain) throws
            IdentityProviderManagementException {}

    /**
     * Updates a given Identity Provider information
     *
     * @param oldIdPName          existing Identity Provider name
     * @param newIdentityProvider new IdP information
     * @throws IdentityProviderManagementException Error when updating Identity Provider
     *                                                information
     */
    @Deprecated
    void updateIdP(String oldIdPName, IdentityProvider newIdentityProvider, String tenantDomain) throws
            IdentityProviderManagementException;

    /**
     * Updates a given Identity Provider information using its resource ID.
     *
     * @param resourceId          IDP resource ID.
     * @param newIdentityProvider New IdP information.
     * @param tenantDomain        Tenant domain of the IDP.
     * @throws IdentityProviderManagementException Error when updating Identity Provider information.
     */
    default IdentityProvider updateIdPByResourceId(String resourceId, IdentityProvider newIdentityProvider, String
            tenantDomain) throws IdentityProviderManagementException {
        return null;
    }

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

    /**
     * Retrieve applications that are federating to the identity provider identified by resource ID.
     *
     * @param resourceId   Identity Provider's resource ID.
     * @param limit        Limit parameter for pagination.
     * @param offset       Offset parameter for pagination.
     * @param tenantDomain Tenant domain of Identity Provider.
     * @return
     * @throws IdentityProviderManagementException
     */
    default ConnectedAppsResult getConnectedApplications(String resourceId, Integer limit, Integer offset, String
            tenantDomain) throws IdentityProviderManagementException {

        return null;
    }

    default ConnectedAppsResult getConnectedAppsForLocalAuthenticator(String authenticatorId, int tenantId,
                                                                      Integer limit, Integer offset)
            throws IdentityProviderManagementException {

        return null;
    }
    /**
     * Retrieves the first matching IDP for the given metadata property.
     * Intended to ony be used to retrieve IDP based on a unique metadata property.
     *
     * @param property     IDP metadata property name.
     * @param value        Value associated with given Property.
     * @param tenantDomain Tenant domain whose information is requested.
     * @param ignoreFileBasedIdps Whether to ignore file based idps or not.
     * @return <code>IdentityProvider</code> Identity Provider information.
     * @throws IdentityProviderManagementException Error when getting Identity Provider
     *                                                information by IdP name.
     */
    default IdentityProvider getIdPByMetadataProperty(String property, String value, String tenantDomain,
                                                      boolean ignoreFileBasedIdps)
            throws IdentityProviderManagementException {

        return null;
    }

    /**
     * Get valid IDP groups by IDP group IDs.
     *
     * @param idpGroupIds  List of IDP group IDs.
     * @param tenantDomain Tenant domain.
     * @return List of valid IDP groups.
     * @throws IdentityProviderManagementException If an error occurred while getting IDP Group data.
     */
    default List<IdPGroup> getValidIdPGroupsByIdPGroupIds(List<String> idpGroupIds, String tenantDomain)
            throws IdentityProviderManagementException {

        return null;
    }
}
