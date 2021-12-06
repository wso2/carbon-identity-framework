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
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.user.store.configuration.dao.AbstractUserStoreDAOFactory;
import org.wso2.carbon.identity.user.store.configuration.dao.impl.DatabaseBasedUserStoreDAOFactory;
import org.wso2.carbon.identity.user.store.configuration.deployer.util.UserStoreConfigurationConstants;
import org.wso2.carbon.identity.user.store.configuration.utils.IdentityUserStoreMgtException;
import org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreConfigConstants;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.io.File;
import java.io.IOException;

/**
 * UserStore config component for deployer.
 */
@Component(
        name = "identity.user.store.org.wso2.carbon.identity.user.store.configuration.component",
        immediate = true
)
public class UserStoreConfigComponent {
    private static final Log log = LogFactory.getLog(UserStoreConfigComponent.class);
    private static RealmService realmService = null;
    private static ServerConfigurationService serverConfigurationService = null;
    private static AbstractUserStoreDAOFactory dbBasedUserStoreDAOFactory = null;

    public static RealmService getRealmService() {
        return realmService;
    }

    @Reference(
            name = "user.realmservice.default",
            service = RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService"
    )
    protected void setRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the Realm Service");
        }
        UserStoreConfigComponent.realmService = realmService;
    }

    public static ServerConfigurationService getServerConfigurationService() {
        return UserStoreConfigComponent.serverConfigurationService;
    }

    @Reference(
            name = "server.configuration.service",
            service = ServerConfigurationService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetServerConfigurationService"
    )
    protected void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the serverConfigurationService");
        }
        UserStoreConfigComponent.serverConfigurationService = serverConfigurationService;
    }

    /**
     * @param ctxt
     */
    @Activate
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
    @Deactivate
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

    @Reference(
            name = "config.context.service",
            service = ConfigurationContextService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigurationContextService"
    )
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

    @Reference(
            name = "database.based.user.store.config.service",
            service = AbstractUserStoreDAOFactory.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetDatabaseBasedUserStoreDAOFactory"
    )
    protected void setDatabaseBasedUserStoreDAOFactory(AbstractUserStoreDAOFactory abstractUserStoreDAOFactory) {

        if (abstractUserStoreDAOFactory.getClass().getSimpleName()
                .equalsIgnoreCase(DatabaseBasedUserStoreDAOFactory.class.toString())) {
            UserStoreConfigComponent.dbBasedUserStoreDAOFactory = abstractUserStoreDAOFactory;
            if (log.isDebugEnabled()) {
                log.debug("DatabaseBasedUserStoreDAOFactory is set to User Store Deployer bundle.");
            }
        }
    }

    protected void unsetDatabaseBasedUserStoreDAOFactory(AbstractUserStoreDAOFactory abstractUserStoreDAOFactory) {

        UserStoreConfigComponent.dbBasedUserStoreDAOFactory = null;
        if (log.isDebugEnabled()) {
            log.debug("DatabaseBasedUserStoreDAOFactory is unset from User Store Deployer bundle.");
        }
    }

    /**
     * This method invoked when the bundle get activated, it touches the super-tenants user store
     * configuration with latest time stamp. This invokes undeploy and deploy method
     */
    private void triggerDeployerForSuperTenantSecondaryUserStores() {

        triggerDeployerForSuperTenantFileBasedUserStores();
        if (SecondaryUserStoreConfigurationUtil.isUserStoreRepositorySeparationEnabled()) {
            deployDatabaseBasedUserStoresForSuperTenant();
        }
    }

    private void triggerDeployerForSuperTenantFileBasedUserStores() {

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

    private void deployDatabaseBasedUserStoresForSuperTenant() {

        RealmConfiguration[] userStores = new RealmConfiguration[0];
        try {
            userStores = UserStoreConfigComponent.dbBasedUserStoreDAOFactory.getInstance().getUserStoreRealms();
        } catch (IdentityUserStoreMgtException e) {
            log.error("Error occurred while getting the user store realms", e);
        }
        for (RealmConfiguration realmConfiguration : userStores) {
            UserRealm userRealm = (UserRealm) CarbonContext.getThreadLocalCarbonContext().getUserRealm();
            AbstractUserStoreManager primaryUSM;
            try {
                primaryUSM = (AbstractUserStoreManager) userRealm.getUserStoreManager();
                primaryUSM.addSecondaryUserStoreManager(realmConfiguration, userRealm);
                setSecondaryUserStoreToChain(userRealm.getRealmConfiguration(), realmConfiguration);
            } catch (UserStoreException e) {
                log.error("Error occurred while trying to add set the secondary user stores", e);
            }
        }
    }

    /**
     * Set secondary user store at the very end of chain.
     *
     * @param parent : primary user store
     * @param child  : secondary user store
     */
    private void setSecondaryUserStoreToChain(RealmConfiguration parent, RealmConfiguration child) {

        String parentDomain = parent.getUserStoreProperty(UserStoreConfigConstants.DOMAIN_NAME);
        String addingDomain = child.getUserStoreProperty(UserStoreConfigConstants.DOMAIN_NAME);

        if (parentDomain == null) {
            return;
        }

        while (parent.getSecondaryRealmConfig() != null) {
            if (parentDomain.equals(addingDomain)) {
                return;
            }
            parent = parent.getSecondaryRealmConfig();
            parentDomain = parent.getUserStoreProperty(UserStoreConfigConstants.DOMAIN_NAME);
        }

        if (parentDomain.equals(addingDomain)) {
            return;
        }
        parent.setSecondaryRealmConfig(child);
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
