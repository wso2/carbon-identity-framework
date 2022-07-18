/*
 * Copyright (c) 2014 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.idp.mgt.ui.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.idp.xsd.ProvisioningConnectorConfig;
import org.wso2.carbon.idp.mgt.stub.IdentityProviderMgtServiceIdentityProviderManagementExceptionException;
import org.wso2.carbon.idp.mgt.stub.IdentityProviderMgtServiceStub;
import org.wso2.carbon.idp.mgt.ui.util.IdentityException;
import org.wso2.carbon.user.mgt.stub.UserAdminStub;
import org.wso2.carbon.user.mgt.stub.types.carbon.UserStoreInfo;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Client class for IdentityProviderMgtServiceStub.
 */
public class IdentityProviderMgtServiceClient {

    private static final Log log = LogFactory.getLog(IdentityProviderMgtServiceClient.class);

    private IdentityProviderMgtServiceStub idPMgtStub;

    private UserAdminStub userAdminStub;

    /**
     * @param cookie           HttpSession cookie
     * @param backendServerURL Backend Carbon server URL
     * @param configCtx        Axis2 Configuration Context
     */
    public IdentityProviderMgtServiceClient(String cookie, String backendServerURL,
                                            ConfigurationContext configCtx) {

        String idPMgtServiceURL = backendServerURL + "IdentityProviderMgtService";
        String userAdminServiceURL = backendServerURL + "UserAdmin";
        try {
            idPMgtStub = new IdentityProviderMgtServiceStub(configCtx, idPMgtServiceURL);
        } catch (AxisFault axisFault) {
            log.error("Error while instantiating IdentityProviderMgtServiceStub", axisFault);
        }
        try {
            userAdminStub = new UserAdminStub(configCtx, userAdminServiceURL);
        } catch (AxisFault axisFault) {
            log.error("Error while instantiating UserAdminServiceStub", axisFault);
        }
        ServiceClient idPMgtClient = idPMgtStub._getServiceClient();
        ServiceClient userAdminClient = userAdminStub._getServiceClient();
        Options idPMgtOptions = idPMgtClient.getOptions();
        idPMgtOptions.setManageSession(true);
        idPMgtOptions.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                cookie);
        Options userAdminOptions = userAdminClient.getOptions();
        userAdminOptions.setManageSession(true);
        userAdminOptions.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                cookie);
    }

    /**
     * Retrieves Resident Identity provider for a given tenant
     *
     * @return <code>FederatedIdentityProvider</code>
     * @throws Exception Error when getting Resident Identity Providers
     */
    public IdentityProvider getResidentIdP() throws Exception {

        try {
            return idPMgtStub.getResidentIdP();
        } catch (Exception e) {
            log.error("Error in retrieving list of Identity Providers ", e);
            throw new Exception("Error occurred while retrieving list of Identity Providers");
        }
    }

    /**
     * Updated Resident Identity provider for a given tenant
     *
     * @param identityProvider <code>FederatedIdentityProvider</code>
     * @throws Exception Error when getting Resident Identity Providers
     */
    public void updateResidentIdP(IdentityProvider identityProvider) throws Exception {

        try {
            idPMgtStub.updateResidentIdP(identityProvider);
        } catch (Exception e) {
            log.error("Error in retrieving the list of Resident Identity Providers", e);
            throw new Exception("Error occurred while retrieving list of Identity Providers");
        }
    }

    /**
     * Retrieves registered Identity providers for a given tenant
     *
     * @return List of <code>FederatedIdentityProvider</code>. IdP names, primary IdP and home realm
     * identifiers of each IdP
     * @throws IdentityException Error when getting list of Identity Providers
     */
    public IdentityProvider[] getIdPs() throws IdentityException {

        try {
            return idPMgtStub.getAllIdPs();
        } catch (RemoteException | IdentityProviderMgtServiceIdentityProviderManagementExceptionException e) {
            String message = "Error in retrieving the list of Identity Providers for a given tenant ";
            log.error(message, e);
            throw new IdentityException(message, e);
        }
    }

    /**
     * Get all list of Idp information with pagination.
     *
     * @param pageNumber pageNumber need to filter.
     * @return list of paginated idp array.
     * @throws IdentityException Error when getting list of Identity Providers.
     */
    public IdentityProvider[] getAllPaginatedIdpInfo(int pageNumber) throws IdentityException {

        try {
            return idPMgtStub.getAllPaginatedIdpInfo(pageNumber);
        } catch (RemoteException | IdentityProviderMgtServiceIdentityProviderManagementExceptionException e) {
            String message = "Error in retrieving the list of Identity Providers for a given tenant ";
            log.error(message, e);
            throw new IdentityException(message, e);
        }
    }

    /**
     * Get all list of Idp information for a matching filter with pagination.
     *
     * @param pageNumber pageNumber need to filter.
     * @param filter     Application name filter.
     * @return list of paginated idp array.
     * @throws IdentityException Error when getting list of Identity Providers for a matching filter with pagination.
     */
    public IdentityProvider[] getPaginatedIdpInfo(int pageNumber, String filter) throws IdentityException {

        try {
            return idPMgtStub.getPaginatedIdpInfo(filter, pageNumber);
        } catch (RemoteException | IdentityProviderMgtServiceIdentityProviderManagementExceptionException e) {
            String message = "Error in retrieving the list of filtered Identity Providers for a given tenant ";
            log.error(message, e);
            throw new IdentityException(message, e);
        }
    }

    /**
     * Get count of all Identity providers.
     *
     * @return total number of Idps.
     * @throws IdentityException Error when getting count of total Identity Providers.
     */
    public int getIdpCount() throws IdentityException {

        try {
            return idPMgtStub.getAllIdpCount();
        } catch (RemoteException | IdentityProviderMgtServiceIdentityProviderManagementExceptionException e) {
            String message = "Error in retrieving the count of Identity Providers for a given tenant ";
            log.error(message, e);
            throw new IdentityException(message, e);
        }
    }

    /**
     * Get count of all basic applications for a matching filter.
     *
     * @param filter Idp filter
     * @return Count of applications match the filter
     * @throws IdentityException Error when getting count of filtered total Identity Providers.
     */
    public int getFilteredIdpCount(String filter) throws IdentityException {

        try {
            return idPMgtStub.getFilteredIdpCount(filter);
        } catch (RemoteException | IdentityProviderMgtServiceIdentityProviderManagementExceptionException e) {
            String message = "Error in retrieving the count of registered Identity Providers for a given tenant ";
            log.error(message, e);
            throw new IdentityException(message, e);
        }
    }

    /**
     * Retrieves registered Identity providers for a given tenant by Identity Provider name
     *
     * @param filter
     * @return List of <code>FederatedIdentityProvider</code>. IdP names, primary IdP and home realm
     * identifiers of each IdP
     * @throws Exception Error when getting list of Identity Providers
     */
    public List<IdentityProvider> getIdPsSearch(String filter) throws Exception {

        try {
            IdentityProvider[] identityProviders = idPMgtStub.getAllIdPsSearch(filter);
            if (identityProviders != null && identityProviders.length > 0) {
                return Arrays.asList(identityProviders);
            } else {
                return new ArrayList<IdentityProvider>();
            }
        } catch (Exception e) {
            log.error("Error in retrieving the Identity Provider", e);
            throw new Exception("Error occurred while retrieving Identity Providers", e);
        }
    }

    /**
     * Retrieves Enabled registered Identity providers for a given tenant
     *
     * @return List of <code>FederatedIdentityProvider</code>. IdP names, primary IdP and home realm
     * identifiers of each IdP
     * @throws Exception Error when getting list of Identity Providers
     */
    public List<IdentityProvider> getEnabledIdPs() throws Exception {

        try {
            IdentityProvider[] identityProviders = idPMgtStub.getEnabledAllIdPs();
            if (identityProviders != null && identityProviders.length > 0) {
                return Arrays.asList(identityProviders);
            } else {
                return new ArrayList<IdentityProvider>();
            }
        } catch (Exception e) {
            log.error("Error in retrieving the list of enabled registered Identity Providers for a given tenant", e);
            throw new Exception(
                    "Error occurred while retrieving list of Enabled Identity Providers", e);
        }
    }

    /**
     * Retrieves Identity provider information about a given tenant by Identity Provider name
     *
     * @param idPName Unique name of the Identity provider of whose information is requested
     * @return <code>FederatedIdentityProvider</code> Identity Provider information
     * @throws Exception Error when getting Identity Provider information by IdP name
     */
    public IdentityProvider getIdPByName(String idPName) throws Exception {

        try {
            return idPMgtStub.getIdPByName(idPName);
        } catch (Exception e) {
            log.error("Error in retrieving the information about Identity provider for a given tenant", e);
            throw new Exception("Error occurred while retrieving information about " + idPName, e);
        }
    }

    /**
     * Adds an Identity Provider to the given tenant
     *
     * @param identityProvider <code><FederatedIdentityProvider/code></code> federated Identity
     *                         Provider information
     * @throws Exception Error when adding Identity Provider information
     */
    public void addIdP(IdentityProvider identityProvider) throws Exception {

        try {
            idPMgtStub.addIdP(identityProvider);
        } catch (Exception e) {
            log.error("Error in adding a Identity Provider for a given tenant", e);
            throw new Exception(e.getMessage(), e);
        }
    }

    /**
     * Deletes an Identity Provider from a given tenant
     *
     * @param idPName Name of the IdP to be deleted
     * @throws Exception Error when deleting Identity Provider information
     */
    public void deleteIdP(String idPName) throws Exception {

        try {
            idPMgtStub.deleteIdP(idPName);
        } catch (Exception e) {
            log.error("Error in deleting the Identity Provider for a given tenant", e);
            throw new Exception("Error in deleting the Identity Provider", e);
        }
    }

    /**
     * Updates a given Identity Provider information
     *
     * @param oldIdPName       existing IdP name
     * @param identityProvider <code>FederatedIdentityProvider</code> new IdP information
     * @throws Exception Error when updating Identity Provider information
     */
    public void updateIdP(String oldIdPName, IdentityProvider identityProvider) throws Exception {

        try {
            idPMgtStub.updateIdP(oldIdPName, identityProvider);
        } catch (Exception e) {
            log.error("Error in updating the Identity Provider for a given tenant", e);
            throw new Exception(e.getMessage(), e);
        }
    }

    /**
     * Retrieve all the federated authenticators.
     *
     * @return Map containing the federated authenticators.
     * @throws Exception if an error occurs while retrieving the federated authenticators.
     */
    public Map<String, FederatedAuthenticatorConfig> getAllFederatedAuthenticators()
            throws Exception {

        Map<String, FederatedAuthenticatorConfig> configMap = new HashMap<String, FederatedAuthenticatorConfig>();

        try {
            FederatedAuthenticatorConfig[] fedAuthConfigs = idPMgtStub
                    .getAllFederatedAuthenticators();

            if (fedAuthConfigs != null && fedAuthConfigs.length > 0) {
                for (FederatedAuthenticatorConfig config : fedAuthConfigs) {
                    configMap.put(config.getName(), config);
                }
            }
        } catch (Exception e) {
            log.error("Error in retrieving Federated Authenticators", e);
            throw new Exception("Error occurred while retrieving all Federated Authenticators", e);
        }

        return configMap;

    }

    /**
     * Retrieve the custom provisioning connectors.
     *
     * @return Map containing the custom provisioning connectors.
     * @throws Exception if an error occurs while retrieving the custom provisioning connectors.
     */
    public Map<String, ProvisioningConnectorConfig> getCustomProvisioningConnectors() throws Exception {

        Map<String, ProvisioningConnectorConfig> provisioningConnectors = new HashMap<>();
        try {
            ProvisioningConnectorConfig[] provisioningConnectorConfigs = idPMgtStub
                    .getAllProvisioningConnectors();
            if (provisioningConnectorConfigs != null && provisioningConnectorConfigs.length > 0
                    && provisioningConnectorConfigs[0] != null) {
                for (ProvisioningConnectorConfig config : provisioningConnectorConfigs) {
                    if (!(StringUtils.equals("scim", config.getName()) ||
                            StringUtils.equals("salesforce", config.getName()) ||
                            StringUtils.equals("googleapps" , config.getName()) ||
                            StringUtils.equals("SCIM2", config.getName()))) {
                        provisioningConnectors.put(config.getName(), config);
                    }

                }
            }
        } catch (Exception e) {
            log.error("Error in retrieving Provisioning Connector", e);
            throw new Exception("Error occurred while retrieving all Provisioning Connectors", e);
        }
        return provisioningConnectors;
    }

    /**
     * Retrieve all the local claim URIs.
     *
     * @return All the local claim URIs.
     * @throws Exception if an error occurs while retrieving the local claim URIs.
     */
    public String[] getAllLocalClaimUris() throws Exception {

        try {
            return idPMgtStub.getAllLocalClaimUris();
        } catch (Exception e) {
            log.error("Error in retrieving localClaim Uris", e);
            throw new Exception("Error occurred while retrieving all local claim URIs", e);
        }
    }

    /**
     * Retrieve the user store domains.
     *
     * @return Array of user store domains.
     * @throws Exception if an error occurs while retrieving user store domains.
     */
    public String[] getUserStoreDomains() throws Exception {

        try {
            List<String> readWriteDomainNames = new ArrayList<String>();
            UserStoreInfo[] storesInfo = userAdminStub.getUserRealmInfo().getUserStoresInfo();
            for (UserStoreInfo storeInfo : storesInfo) {
                if (!storeInfo.getReadOnly()) {
                    readWriteDomainNames.add(storeInfo.getDomainName());
                }
            }
            return readWriteDomainNames.toArray(new String[readWriteDomainNames.size()]);
        } catch (Exception e) {
            log.error("Error in retrieving User Store Domain IDs", e);
            throw new Exception(
                    "Error occurred while retrieving Read-Write User Store Domain IDs " +
                            "for logged-in user's tenant realm", e);
        }
    }

    public String getResidentIDPMetadata() throws java.rmi.RemoteException,
            IdentityProviderMgtServiceIdentityProviderManagementExceptionException {

        return idPMgtStub.getResidentIDPMetadata();
    }

}
