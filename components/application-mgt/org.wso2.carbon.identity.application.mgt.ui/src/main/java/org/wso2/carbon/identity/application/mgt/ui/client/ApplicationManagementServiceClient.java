/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.mgt.ui.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.xsd.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.xsd.ImportResponse;
import org.wso2.carbon.identity.application.common.model.xsd.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.xsd.RequestPathAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.xsd.SpFileContent;
import org.wso2.carbon.identity.application.common.model.xsd.SpTemplate;
import org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceIdentityApplicationManagementException;
import org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceStub;
import org.wso2.carbon.user.mgt.stub.UserAdminStub;
import org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;
import org.wso2.carbon.user.mgt.stub.types.carbon.UserStoreInfo;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class ApplicationManagementServiceClient {

    IdentityApplicationManagementServiceStub stub;
    Log log = LogFactory.getLog(ApplicationManagementServiceClient.class);
    boolean debugEnabled = log.isErrorEnabled();
    private UserAdminStub userAdminStub;

    /**
     * @param cookie
     * @param backendServerURL
     * @param configCtx
     * @throws AxisFault
     */
    public ApplicationManagementServiceClient(String cookie, String backendServerURL,
                                              ConfigurationContext configCtx) throws AxisFault {

        String serviceURL = backendServerURL + "IdentityApplicationManagementService";
        String userAdminServiceURL = backendServerURL + "UserAdmin";
        stub = new IdentityApplicationManagementServiceStub(configCtx, serviceURL);
        userAdminStub = new UserAdminStub(configCtx, userAdminServiceURL);

        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        ServiceClient userAdminClient = userAdminStub._getServiceClient();
        Options userAdminOptions = userAdminClient.getOptions();
        userAdminOptions.setManageSession(true);
        userAdminOptions.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        if (debugEnabled) {
            log.debug("Invoking service " + serviceURL);
        }

    }

    /**
     * @param serviceProvider
     * @throws AxisFault
     */
    public void createApplication(ServiceProvider serviceProvider, String templateName) throws AxisFault {

        try {
            if (debugEnabled) {
                log.debug("Registering Service Provider " + serviceProvider.getApplicationName());
            }
            stub.addApplication(serviceProvider, templateName);
        } catch (RemoteException | IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
            handleException(e);
        }

    }

    /**
     * @param applicationName
     * @return
     * @throws AxisFault
     */
    public ServiceProvider getApplication(String applicationName) throws AxisFault {
        try {
            if (debugEnabled) {
                log.debug("Loading Service Provider " + applicationName);
            }
            return stub.getApplication(applicationName);
        } catch (RemoteException | IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
            handleException(e);
        }
        return null;
    }

    /**
     * @return
     * @throws AxisFault
     */
    public ApplicationBasicInfo[] getAllApplicationBasicInfo() throws Exception {
        try {
            return stub.getAllApplicationBasicInfo();
        } catch (RemoteException | IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
            handleException(e);
        }
        return new ApplicationBasicInfo[0];
    }

    /**
     * @param serviceProvider
     * @throws AxisFault
     */
    public void updateApplicationData(ServiceProvider serviceProvider) throws Exception {
        try {
            stub.updateApplication(serviceProvider);
        } catch (RemoteException | IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
            handleException(e);
        }
    }

    /**
     * @param applicationID
     * @throws AxisFault
     */
    public void deleteApplication(String applicationID) throws Exception {
        try {
            stub.deleteApplication(applicationID);
        } catch (RemoteException | IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
            handleException(e);
        }
    }

    /**
     * @param identityProviderName
     * @throws AxisFault
     */
    public IdentityProvider getFederatedIdentityProvider(String identityProviderName) throws AxisFault {
        try {
            return stub.getIdentityProvider(identityProviderName);
        } catch (RemoteException | IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
            handleException(e);
        }
        return null;
    }

    /**
     * @return
     * @throws AxisFault
     */
    public RequestPathAuthenticatorConfig[] getAllRequestPathAuthenticators() throws AxisFault {
        try {
            return stub.getAllRequestPathAuthenticators();
        } catch (RemoteException | IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
            handleException(e);
        }
        return new RequestPathAuthenticatorConfig[0];
    }

    /**
     * @return
     * @throws AxisFault
     */
    public LocalAuthenticatorConfig[] getAllLocalAuthenticators() throws AxisFault {
        try {
            return stub.getAllLocalAuthenticators();
        } catch (RemoteException | IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
            handleException(e);
        }
        return new LocalAuthenticatorConfig[0];
    }

    /**
     * @return
     * @throws AxisFault
     */
    public IdentityProvider[] getAllFederatedIdentityProvider() throws AxisFault {
        try {
            return stub.getAllIdentityProviders();
        } catch (RemoteException | IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
            handleException(e);
        }
        return new IdentityProvider[0];
    }

    /**
     * @return
     * @throws AxisFault
     */
    public String[] getAllClaimUris() throws AxisFault {
        try {
            return stub.getAllLocalClaimUris();
        } catch (RemoteException | IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
            handleException(e);
        }
        return new String[0];
    }

    /**
     * Get User Store Domains
     *
     * @return
     * @throws AxisFault
     */
    public String[] getUserStoreDomains() throws AxisFault {
        try {
            List<String> readWriteDomainNames = new ArrayList<String>();
            UserStoreInfo[] storesInfo = userAdminStub.getUserRealmInfo().getUserStoresInfo();
            for (UserStoreInfo storeInfo : storesInfo) {
                if (!storeInfo.getReadOnly()) {
                    readWriteDomainNames.add(storeInfo.getDomainName());
                }
            }
            return readWriteDomainNames.toArray(new String[readWriteDomainNames.size()]);
        } catch (RemoteException | UserAdminUserAdminException e) {
            throw new AxisFault("Error occurred while retrieving Read-Write User Store Domain IDs for logged-in" +
                                " user's tenant realm");
        }
    }

    /**
     * Retrieve the configured authentication templates as a JSON String.
     *
     * @return Authentication template configuration
     * @throws AxisFault
     */
    public String getAuthenticationTemplatesJson() throws AxisFault {

        try {
            return stub.getAuthenticationTemplatesJSON();
        } catch (RemoteException e) {
            throw new AxisFault("Error occurred while retrieving authentication flow templates", e);
        }
    }

    public ImportResponse importApplication(SpFileContent spFileContent) throws AxisFault {
        try {
            if (debugEnabled) {
                log.debug("Importing Service Provider from file : " + spFileContent.getFileName());
            }
            return stub.importApplication(spFileContent);
        } catch (RemoteException | IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
            handleException(e);
        }
        return new ImportResponse();
    }

    public String exportApplication(String appid, boolean exportSecrets) throws AxisFault {
        try {
            if (debugEnabled) {
                log.debug("Exporting Service Provider to file" );
            }
            return stub.exportApplication(appid, exportSecrets);
        } catch (RemoteException | IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
            handleException(e);
        }
        return null;
    }

    /**
     * Create an application template.
     *
     * @param spTemplate service provider template info
     * @throws AxisFault
     */
    public void createApplicationTemplate(SpTemplate spTemplate) throws AxisFault {

        try {
            if (debugEnabled) {
                log.debug(String.format("Registering Service Provider template: %s", spTemplate.getName()));
            }
            stub.createApplicationTemplate(spTemplate);
        } catch (RemoteException | IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
            handleException(e);
        }
    }

    /**
     * Add configured service provider as a template.
     *
     * @param serviceProvider Service provider to be configured as a template
     * @param spTemplate service provider template basic info
     * @throws AxisFault
     */
    public void createApplicationTemplateFromSP(ServiceProvider serviceProvider, SpTemplate spTemplate)
            throws AxisFault {

        try {
            if (debugEnabled) {
                log.debug(String.format("Adding Service Provider as a template with name: %s", spTemplate.getName()));
            }
            stub.createApplicationTemplateFromSP(serviceProvider, spTemplate);
        } catch (RemoteException | IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
            handleException(e);
        }
    }

    /**
     * Get Service provider template.
     *
     * @param templateName template name
     * @return service provider template info
     * @throws AxisFault
     */
    public SpTemplate getApplicationTemplate(String templateName) throws AxisFault {

        try {
            if (debugEnabled) {
                log.debug(String.format("Retrieving Service Provider template: %s", templateName));
            }
            return stub.getApplicationTemplate(templateName);
        } catch (RemoteException | IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
            handleException(e);
        }
        return null;
    }

    /**
     * Delete an application template.
     *
     * @param templateName name of the template
     * @throws AxisFault
     */
    public void deleteApplicationTemplate(String templateName) throws AxisFault {

        try {
            if (debugEnabled) {
                log.debug(String.format("Deleting Service Provider template: %s", templateName));
            }
            stub.deleteApplicationTemplate(templateName);
        } catch (RemoteException | IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
            handleException(e);
        }
    }

    /**
     * Update an application template.
     *
     * @param spTemplate SP template info to be updated
     * @throws AxisFault
     */
    public void updateApplicationTemplate(String templateName, SpTemplate spTemplate) throws AxisFault {

        try {
            if (debugEnabled) {
                log.debug(String.format("Updating Service Provider template: %s", templateName));
            }
            stub.updateApplicationTemplate(templateName, spTemplate);
        } catch (RemoteException | IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
            handleException(e);
        }
    }

    /**
     * Check existence of a application template.
     *
     * @param templateName template name
     * @return true if a template with the specified name exists
     * @throws AxisFault
     */
    public boolean isExistingApplicationTemplate(String templateName) throws AxisFault {

        try {
            if (debugEnabled) {
                log.debug(String.format("Checking existence of application template: %s.", templateName));
            }
            return stub.isExistingApplicationTemplate(templateName);
        } catch (RemoteException | IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
            handleException(e);
        }
        return false;
    }

    /**
     * Get basic info of all the service provider templates.
     *
     * @return Array of all application templates
     * @throws AxisFault
     */
    public SpTemplate[] getAllApplicationTemplateInfo() throws AxisFault {

        SpTemplate[] templates = null;
        try {
            if (debugEnabled) {
                log.debug("Get all service provider template basic info.");
            }
            templates = stub.getAllApplicationTemplateInfo();
        } catch (RemoteException | IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
            handleException(e);
        }
        return templates;
    }

    private void handleException(Exception e) throws AxisFault {
        String errorMessage = "Unknown error occurred.";

        if (e instanceof IdentityApplicationManagementServiceIdentityApplicationManagementException) {
            IdentityApplicationManagementServiceIdentityApplicationManagementException exception =
                    (IdentityApplicationManagementServiceIdentityApplicationManagementException) e;
            if (exception.getFaultMessage().getIdentityApplicationManagementException() != null) {
                errorMessage = exception.getFaultMessage().getIdentityApplicationManagementException().getMessage();
            }
        } else {
            errorMessage = e.getMessage();
        }

        throw new AxisFault(errorMessage, e);
    }

}
