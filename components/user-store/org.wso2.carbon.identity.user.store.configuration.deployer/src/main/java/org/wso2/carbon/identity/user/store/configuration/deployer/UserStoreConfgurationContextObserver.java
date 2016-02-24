package org.wso2.carbon.identity.user.store.configuration.deployer;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;

public class UserStoreConfgurationContextObserver implements Axis2ConfigurationContextObserver {

    private static Log log = LogFactory.getLog(Axis2ConfigurationContextObserver.class);

    public void createdConfigurationContext(ConfigurationContext context) {

    }

    public void creatingConfigurationContext(int context) {

    }

    public void terminatedConfigurationContext(ConfigurationContext context) {

    }

    public void terminatingConfigurationContext(ConfigurationContext context) {
        try {
            org.wso2.carbon.user.api.UserRealm tenantRealm = CarbonContext
                    .getThreadLocalCarbonContext().getUserRealm();
            RealmConfiguration realmConfig = tenantRealm.getRealmConfiguration();
            AbstractUserStoreManager userStoreManager = (AbstractUserStoreManager) tenantRealm
                    .getUserStoreManager();
            userStoreManager.clearAllSecondaryUserStores();
            realmConfig.setSecondaryRealmConfig(null);
            userStoreManager.setSecondaryUserStoreManager(null);
            log.info("Unloaded all secondary user stores for tenant "
                    + CarbonContext.getThreadLocalCarbonContext().getTenantId());
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

}
