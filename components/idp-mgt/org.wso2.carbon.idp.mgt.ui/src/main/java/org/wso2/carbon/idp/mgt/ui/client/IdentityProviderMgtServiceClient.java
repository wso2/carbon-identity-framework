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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.idp.xsd.ProvisioningConnectorConfig;
import org.wso2.carbon.idp.mgt.stub.IdentityProviderMgtServiceIdentityProviderManagementExceptionException;
import org.wso2.carbon.idp.mgt.stub.IdentityProviderMgtServiceStub;
import org.wso2.carbon.user.mgt.stub.UserAdminStub;
import org.wso2.carbon.user.mgt.stub.types.carbon.UserStoreInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * @return <code>FederatedIdentityProvider</code>
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
     * @throws Exception Error when getting list of Identity Providers
     */
    public List<IdentityProvider> getIdPs() throws Exception {
        try {
            IdentityProvider[] identityProviders = idPMgtStub.getAllIdPs();
            if (identityProviders != null && identityProviders.length > 0) {
                return Arrays.asList(identityProviders);
            } else {
                return new ArrayList<IdentityProvider>();
            }
        } catch (Exception e) {
            log.error("Error in retrieving the list of Identity Providers for a given tenant", e);
            throw new Exception("Error occurred while retrieving list of Identity Providers");
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
                    "Error occurred while retrieving list of Enabled Identity Providers");
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
            throw new Exception("Error occurred while retrieving information about " + idPName);
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
            throw new Exception(e.getMessage());
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
            throw new Exception("Error in deleting the Identity Provider");
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
            throw new Exception(e.getMessage());
        }
    }

    /**
     * @return
     * @throws Exception
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
            throw new Exception("Error occurred while retrieving all Federated Authenticators");
        }

        return configMap;

    }

    /**
     * @return
     * @throws Exception
     */
    public Map<String, ProvisioningConnectorConfig> getCustomProvisioningConnectors() throws Exception {
        Map<String, ProvisioningConnectorConfig> provisioningConnectors = new HashMap<String, ProvisioningConnectorConfig>();
        try {
            ProvisioningConnectorConfig[] provisioningConnectorConfigs = idPMgtStub
                    .getAllProvisioningConnectors();
            if (provisioningConnectorConfigs != null && provisioningConnectorConfigs.length > 0
                    && provisioningConnectorConfigs[0] != null) {
                for (ProvisioningConnectorConfig config : provisioningConnectorConfigs) {
                    if (!(("spml").equals(config.getName()) || ("scim").equals(config.getName())
                            || ("salesforce").equals(config.getName()) ||
                            ("googleapps").equals(config.getName())))
                        provisioningConnectors.put(config.getName(), config);

                }
            }
        } catch (Exception e) {
            log.error("Error in retrieving Provisioning Connector", e);
            throw new Exception("Error occurred while retrieving all Provisioning Connectors");
        }
        return provisioningConnectors;
    }

    /**
     * @return
     * @throws Exception
     */
    public String[] getAllLocalClaimUris() throws Exception {

        try {
            return idPMgtStub.getAllLocalClaimUris();
        } catch (Exception e) {
            log.error("Error in retrieving localClaim Uris", e);
            throw new Exception("Error occurred while retrieving all local claim URIs");
        }
    }

    /**
     * @return
     * @throws Exception
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
                    "Error occurred while retrieving Read-Write User Store Domain IDs for logged-in user's tenant realm");
        }
    }

    public String getResidentIDPMetadata() throws java.rmi.RemoteException, IdentityProviderMgtServiceIdentityProviderManagementExceptionException {
        return idPMgtStub.getResidentIDPMetadata();
    }

}
