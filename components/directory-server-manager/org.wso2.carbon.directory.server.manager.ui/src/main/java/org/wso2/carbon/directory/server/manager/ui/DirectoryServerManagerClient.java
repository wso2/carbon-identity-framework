/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.directory.server.manager.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.directory.common.stub.types.ServerPrinciple;
import org.wso2.carbon.directory.server.manager.stub.DirectoryServerManagerExceptionException;
import org.wso2.carbon.directory.server.manager.stub.DirectoryServerManagerStub;

import java.rmi.RemoteException;

/**
 * A client which communicates with back-end DirectoryServerManager to create/query and delete
 * server principles.
 */
@SuppressWarnings({"UnusedDeclaration"})
public class DirectoryServerManagerClient {

    public static final String SERVER_MANAGER_CLIENT = "org.wso2.carbon.directory.server.manager";
    private static final Log log = LogFactory.getLog(DirectoryServerManagerClient.class);
    private static final String SERVER_MANAGER_SERVICE = "DirectoryServerManager";
    protected DirectoryServerManagerStub stub = null;
    private String passwordRegularExpression;
    private String serviceNameRegularExpression;

    public DirectoryServerManagerClient(String cookie, String url,
                                        ConfigurationContext configContext) throws ServerManagerClientException {
        try {
            stub = new DirectoryServerManagerStub(configContext, url + SERVER_MANAGER_SERVICE);
            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        } catch (AxisFault e) {
            log.error("Unable to instantiate DirectoryServerManagerClient.", e);
            throw new ServerManagerClientException(ServerManagerClientException.INIT_SERVICE_PRINCIPLE_ERROR, e);
        }
    }

    public void addServicePrinciple(String serverName, String serverDescription, String password)
            throws ServerManagerClientException {
        try {

            if (stub.isExistingServicePrinciple(serverName)) {
                log.error("Error adding service principle. Service name already exists.");
                throw new ServerManagerClientException(ServerManagerClientException.SERVICE_PRINCIPLE_ALREADY_EXISTS);
            }

            stub.addServer(serverName, serverDescription, password);
        } catch (RemoteException e) {
            StringBuilder stb = new StringBuilder();
            stb.append("Server Name - ").append(serverName).append(" Description - ").append(serverDescription);
            log.error("Error adding service principle. Could not reach back-end service. " + stb.toString(), e);
            throw new ServerManagerClientException(ServerManagerClientException.ADD_SERVICE_PRINCIPLES_ERROR, e);
        } catch (DirectoryServerManagerExceptionException e) {
            StringBuilder stb = new StringBuilder();
            stb.append("Server Name - ").append(serverName).append(" Description - ").append(serverDescription);
            log.error("An error occurred while adding a service principle. " + stb.toString(), e);
            throw new ServerManagerClientException(ServerManagerClientException.ADD_SERVICE_PRINCIPLES_ERROR, e);
        }
    }

    public ServerPrinciple[] listServicePrinciples(String filter) throws ServerManagerClientException {
        try {
            ServerPrinciple[] principles = stub.listServicePrinciples(filter);
            if (principles == null) {
                return new ServerPrinciple[0];
            } else {
                return principles;
            }
        } catch (RemoteException e) {
            log.error("Error listing service principles. Could not reach back-end service. Filter - " + filter, e);
            throw new ServerManagerClientException(ServerManagerClientException.LIST_SERVICE_PRINCIPLES_ERROR, e);
        } catch (DirectoryServerManagerExceptionException e) {
            log.error("An error occurred while listing service principles. Filter - " + filter, e);
            throw new ServerManagerClientException(ServerManagerClientException.LIST_SERVICE_PRINCIPLES_ERROR, e);
        }
    }

    public void updatePassword(String servicePrinciple, String oldPassword, String newPassword)
            throws ServerManagerClientException {

        try {
            stub.changePassword(servicePrinciple, oldPassword, newPassword);
        } catch (RemoteException e) {
            log.error("Could not reach back-end service. Error updating password for service principle - "
                    + servicePrinciple, e);
            throw new ServerManagerClientException(ServerManagerClientException.CHANGE_SERVICE_PRINCIPLES_ERROR, e);
        } catch (DirectoryServerManagerExceptionException e) {
            log.error("An error occurred while updating service principle's password. Service principle - "
                    + servicePrinciple, e);
            throw new ServerManagerClientException(ServerManagerClientException.CHANGE_SERVICE_PRINCIPLES_ERROR, e);
        }
    }

    public void removeServicePrinciple(String servicePrinciple) throws ServerManagerClientException {
        try {
            stub.removeServer(servicePrinciple);
        } catch (RemoteException e) {
            log.error("Could not reach back-end service. Error deleting service principle - " + servicePrinciple, e);
            throw new ServerManagerClientException(ServerManagerClientException.REMOVE_SERVICE_PRINCIPLES_ERROR, e);
        } catch (DirectoryServerManagerExceptionException e) {
            log.error("An error occurred while deleting service principle - "
                    + servicePrinciple, e);
            throw new ServerManagerClientException(ServerManagerClientException.REMOVE_SERVICE_PRINCIPLES_ERROR, e);
        }
    }

    public String getPasswordConformanceRegularExpression() throws ServerManagerClientException {

        if (this.passwordRegularExpression != null) {
            return this.passwordRegularExpression;
        }

        try {
            this.passwordRegularExpression = stub.getPasswordConformanceRegularExpression();
            return this.passwordRegularExpression;
        } catch (RemoteException e) {
            log.error("Could not reach back-end service. Error retrieving password format. ", e);
            throw new ServerManagerClientException(ServerManagerClientException.PASSWORD_FORMAT_RETRIEVING_ERROR, e);
        } catch (DirectoryServerManagerExceptionException e) {
            log.error("An error occurred while retrieving password format.", e);
            throw new ServerManagerClientException(ServerManagerClientException.PASSWORD_FORMAT_RETRIEVING_ERROR, e);
        }
    }

    public String getServiceNameConformanceRegularExpression() throws ServerManagerClientException {

        if (this.serviceNameRegularExpression != null) {
            return this.serviceNameRegularExpression;
        }

        try {
            this.serviceNameRegularExpression = stub.getServiceNameConformanceRegularExpression();
            return this.serviceNameRegularExpression;
        } catch (RemoteException e) {
            log.error("Could not reach back-end service. Error retrieving service name format. ", e);
            throw new ServerManagerClientException(ServerManagerClientException.NAME_FORMAT_RETRIEVING_ERROR, e);
        } catch (DirectoryServerManagerExceptionException e) {
            log.error("An error occurred while retrieving service name format.", e);
            throw new ServerManagerClientException(ServerManagerClientException.NAME_FORMAT_RETRIEVING_ERROR, e);
        }
    }

    public boolean isKDCEnabled() {

        try {
            return stub.isKDCEnabled();
        } catch (RemoteException e) {
            log.error("Could not reach back-end service. Error checking whether KDC is enabled. ", e);
        } catch (DirectoryServerManagerExceptionException e) {
            log.error("An error occurred while checking whether KDC is enabled.", e);
        }

        return false;
    }
}
