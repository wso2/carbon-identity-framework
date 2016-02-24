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
import org.wso2.carbon.identity.application.common.model.xsd.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.xsd.RequestPathAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
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
    public void createApplication(ServiceProvider serviceProvider) throws AxisFault {
        try {
            if (debugEnabled) {
                log.debug("Registering Service Provider " + serviceProvider.getApplicationName());
            }
            stub.createApplication(serviceProvider);
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
