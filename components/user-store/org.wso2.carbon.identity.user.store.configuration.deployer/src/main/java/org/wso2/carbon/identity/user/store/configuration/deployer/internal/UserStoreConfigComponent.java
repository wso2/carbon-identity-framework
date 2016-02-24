/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.user.store.configuration.deployer.internal;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.identity.user.store.configuration.deployer.util.UserStoreConfigurationConstants;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.io.File;
import java.io.IOException;

/**
 * @scr.component name="identity.user.store.org.wso2.carbon.identity.user.store.configuration.component"
 * immediate="true"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService" cardinality="1..1"
 * policy="dynamic" bind="setRealmService" unbind="unsetRealmService"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 * @scr.reference name="server.configuration.service"
 * interface="org.wso2.carbon.base.api.ServerConfigurationService" cardinality="1..1"
 * policy="dynamic"  bind="setServerConfigurationService"
 * unbind="unsetServerConfigurationService"
 */
public class UserStoreConfigComponent {
    private static Log log = LogFactory.getLog(UserStoreConfigComponent.class);
    private static RealmService realmService = null;
    private static ServerConfigurationService serverConfigurationService = null;

    public static RealmService getRealmService() {
        return realmService;
    }

    protected void setRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the Realm Service");
        }
        UserStoreConfigComponent.realmService = realmService;
    }

    public static ServerConfigurationService getServerConfigurationService() {
        return UserStoreConfigComponent.serverConfigurationService;
    }

    protected void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the serverConfigurationService");
        }
        UserStoreConfigComponent.serverConfigurationService = serverConfigurationService;
    }

    /**
     * @param ctxt
     */
    protected void activate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.debug("Identity userstore bundle is activated.");
        }
//        BundleContext bundleCtx = ctxt.getBundleContext();
//        Dictionary properties = new Hashtable();
//        properties.put(CarbonConstants.AXIS2_CONFIG_SERVICE,
//                Axis2ConfigurationContextObserver.class.getName());
//        bundleCtx.registerService(Axis2ConfigurationContextObserver.class.getName(),
//                new UserStoreConfgurationContextObserver(), properties);
        triggerDeployerForSuperTenantSecondaryUserStores();
    }

    /**
     * @param ctxt
     */
    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.debug("Identity Userstore-Config bundle is deactivated");
        }
    }

    protected void unsetRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting the Realm Service");
        }
        UserStoreConfigComponent.realmService = null;
    }

    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        if (log.isDebugEnabled()) {
            log.info("Setting the ConfigurationContextService");
        }
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        if (log.isDebugEnabled()) {
            log.debug("Unset the ConfigurationContextService");
        }
    }

    protected void unsetServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting the ServerConfigurationService");
        }
        UserStoreConfigComponent.serverConfigurationService = null;
    }

    /**
     * This method invoked when the bundle get activated, it touches the super-tenants user store
     * configuration with latest time stamp. This invokes undeploy and deploy method
     */
    private void triggerDeployerForSuperTenantSecondaryUserStores() {

        String repositoryPath = CarbonUtils.getCarbonRepository();
        int repoLength = repositoryPath.length();

        /**
         * This operation is done to make sure if the getCarbonRepository method doesn't return a
         * file path with File.Separator at the end, this will add it
         * If repositoryPath is ,<CARBON_HOME>/repository/deployment this method will add
         * File.separator at the end. If not this will exit
         */
        String fSeperator = repositoryPath.substring(repoLength - 1, repoLength);
        if (!fSeperator.equals(File.separator)) {
            repositoryPath += File.separator;
        }
        String superTenantUserStorePath = repositoryPath + "userstores" + File.separator;

        File folder = new File(superTenantUserStorePath);
        File[] listOfFiles = folder.listFiles();

        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file != null) {
                    String ext = FilenameUtils.getExtension(file.getAbsolutePath());
                    if (isValidExtension(ext)) {
                        try {
                            FileUtils.touch(new File(file.getAbsolutePath()));
                        } catch (IOException e) {
                            String errMsg = "Error occurred while trying to touch " + file.getName() +
                                    ". Passwords will continue to remain in plaintext";
                            log.error(errMsg, e);
                            // continuing here since user stores are still functional
                            // except the passwords are in plain text
                        }
                    }
                }
            }
        }
    }

    private boolean isValidExtension(String ext) {

        if (ext != null) {
            if (UserStoreConfigurationConstants.XML_EXTENSION.equalsIgnoreCase(ext)) {
                return true;
            } else if (UserStoreConfigurationConstants.ENC_EXTENSION.equalsIgnoreCase(ext)) {
                return true;
            }
        }
        return false;
    }
}
