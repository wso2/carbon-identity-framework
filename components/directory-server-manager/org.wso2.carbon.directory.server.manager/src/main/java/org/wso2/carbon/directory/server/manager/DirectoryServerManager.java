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

package org.wso2.carbon.directory.server.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.directory.server.manager.common.ServerPrinciple;
import org.wso2.carbon.directory.server.manager.internal.LDAPServerManagerConstants;
import org.wso2.carbon.directory.server.manager.internal.LDAPServerStoreManager;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;

/**
 * DirectoryServerManager is responsible for adding, removing and listing server principles
 * from a LDAP directory server.
 */
@SuppressWarnings({"UnusedDeclaration"})
public class DirectoryServerManager extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(DirectoryServerManager.class);

    private LDAPServerStoreManager getServerStoreManager() throws DirectoryServerManagerException {
        UserRealm realm = this.getUserRealm();
        RealmConfiguration configuration;
        try {
            configuration = realm.getRealmConfiguration();
        } catch (UserStoreException e) {
            throw new DirectoryServerManagerException("Unable to retrieve realm configuration.", e);
        }

        return new LDAPServerStoreManager(configuration);
    }

    /**
     * Adds a server principle to directory server.
     *
     * @param serverName        Name of the server to be added.
     * @param serverDescription Description of the server principle.
     * @param serverPassword    Server principle password.
     * @throws DirectoryServerManagerException If an error occurred while adding a new server principle.
     */
    public void addServer(String serverName, String serverDescription, String serverPassword)
            throws DirectoryServerManagerException {
        LDAPServerStoreManager ldapServerStoreManager = getServerStoreManager();
        ldapServerStoreManager.addServicePrinciple(serverName, serverDescription, serverPassword);
    }

    /**
     * Removes given server principle.
     *
     * @param serverName Name of the server principle to remove.
     * @throws DirectoryServerManagerException If an error occurred while adding a new server principle.
     */
    public void removeServer(String serverName) throws DirectoryServerManagerException {

        LDAPServerStoreManager ldapServerStoreManager = getServerStoreManager();
        ldapServerStoreManager.deleteServicePrinciple(serverName);
    }

    /**
     * Changes the password of a given server principle.
     *
     * @param serverPrinciple  Name of the server principle.
     * @param existingPassword Existing password of the server principle.
     * @param newPassword      New password of the principle.
     * @throws DirectoryServerManagerException If an error occurred while adding a new server principle.
     */
    public void changePassword(String serverPrinciple, String existingPassword, String newPassword)
            throws DirectoryServerManagerException {
        LDAPServerStoreManager ldapServerStoreManager = getServerStoreManager();
        ldapServerStoreManager.updateServicePrinciplePassword(serverPrinciple, existingPassword, newPassword);
    }

    /**
     * List service principles, current available in the directory server.
     *
     * @param filter Filter service principles based on this parameter.
     * @return Returns the service principles.
     * @throws DirectoryServerManagerException If an error occurred while listing service principles.
     */
    public ServerPrinciple[] listServicePrinciples(String filter) throws DirectoryServerManagerException {
        LDAPServerStoreManager ldapServerStoreManager = getServerStoreManager();
        return ldapServerStoreManager.listServicePrinciples(filter);
    }

    /**
     * get service principle matching the name.
     *
     * @param serverName server name.
     * @return Returns the service principle.
     * @throws DirectoryServerManagerException If an error occurred while getting service principle.
     */
    public ServerPrinciple getServicePrinciple(String serverName) throws DirectoryServerManagerException {

        LDAPServerStoreManager ldapServerStoreManager = getServerStoreManager();
        return ldapServerStoreManager.getServicePrinciple(serverName);
    }

    /**
     * This method checks whether give service principle already exists in the LDAP KDC.
     *
     * @param servicePrinciple Name of the service principle.
     * @return <code>true</code> if given service principle already exists, else <code>false</code>.
     * @throws DirectoryServerManagerException In case if there is an error while check is being done.
     */
    public boolean isExistingServicePrinciple(String servicePrinciple) throws DirectoryServerManagerException {
        LDAPServerStoreManager ldapServerStoreManager = getServerStoreManager();
        return ldapServerStoreManager.isExistingServicePrinciple(servicePrinciple);
    }

    /**
     * Gets the regular expression which defines the format of the service principle, password.
     *
     * @return Regular expression.
     * @throws DirectoryServerManagerException If unable to get RealmConfiguration.
     */
    public String getPasswordConformanceRegularExpression() throws DirectoryServerManagerException {

        try {
            RealmConfiguration userStoreConfigurations = this.getUserRealm().getRealmConfiguration();
            if (userStoreConfigurations != null) {
                String passwordRegEx = userStoreConfigurations.getUserStoreProperty(
                        LDAPServerManagerConstants.SERVICE_PASSWORD_REGEX_PROPERTY);
                if (passwordRegEx == null) {
                    return LDAPServerManagerConstants.DEFAULT_PASSWORD_REGULAR_EXPRESSION;
                } else {
                    log.info("Service password format is " + passwordRegEx);
                    return passwordRegEx;
                }
            }
        } catch (UserStoreException e) {
            log.error("Unable to retrieve service password format.", e);
            throw new DirectoryServerManagerException("Unable to retrieve service password format.", e);
        }

        return LDAPServerManagerConstants.DEFAULT_PASSWORD_REGULAR_EXPRESSION;
    }

    /**
     * Gets the regular expression which defines the format of the service principle.
     * Current we use following like format,
     * ftp/localhost
     *
     * @return Service principle name format as a regular expression.
     * @throws DirectoryServerManagerException If unable to retrieve RealmConfiguration.
     */
    public String getServiceNameConformanceRegularExpression() throws DirectoryServerManagerException {

        try {
            RealmConfiguration userStoreConfigurations = this.getUserRealm().getRealmConfiguration();
            if (userStoreConfigurations != null) {
                String serviceNameRegEx = userStoreConfigurations.getUserStoreProperty(
                        LDAPServerManagerConstants.SERVICE_PRINCIPLE_NAME_REGEX_PROPERTY);
                if (serviceNameRegEx == null) {
                    return LDAPServerManagerConstants.DEFAULT_SERVICE_NAME_REGULAR_EXPRESSION;
                } else {
                    log.info("Service name format is " + serviceNameRegEx);
                    return serviceNameRegEx;
                }
            }
        } catch (UserStoreException e) {
            log.error("Unable to retrieve service name format.", e);
            throw new DirectoryServerManagerException("Unable to retrieve service name format.", e);
        }

        return LDAPServerManagerConstants.DEFAULT_SERVICE_NAME_REGULAR_EXPRESSION;
    }

    /**
     * This method checks whether KDC is enabled.
     *
     * @return true if KDC is enabled, else false.
     * @throws DirectoryServerManagerException If an error occurred while querying user realm.
     */
    public boolean isKDCEnabled() throws DirectoryServerManagerException {
        try {
            RealmConfiguration userStoreConfigurations = this.getUserRealm().getRealmConfiguration();
            if (userStoreConfigurations != null) {
                String isKDCEnabled = userStoreConfigurations.getUserStoreProperty
                        (UserCoreConstants.RealmConfig.PROPERTY_KDC_ENABLED);

                return isKDCEnabled != null && Boolean.parseBoolean(isKDCEnabled);

            }
        } catch (UserStoreException e) {
            log.error("Could not retrieve KDC Enabled parameter.", e);
            throw new DirectoryServerManagerException("Could not retrieve KDC Enabled parameter.", e);
        }

        return false;
    }


}
