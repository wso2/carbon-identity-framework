package org.wso2.carbon.identity.user.store.configuration.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.user.store.configuration.dao.AbstractUserStoreDAOFactory;
import org.wso2.carbon.identity.user.store.configuration.dao.impl.DatabaseBasedUserStoreDAOFactory;
import org.wso2.carbon.identity.user.store.configuration.utils.IdentityUserStoreMgtException;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.listener.UserStoreConfigurationListener;

/**
 * Database based implementation for user store configuration listener.
 */
public class DatabaseBasedUserStoreConfigurationListener implements UserStoreConfigurationListener {

    private static final Log LOG = LogFactory.getLog(DatabaseBasedUserStoreConfigurationListener.class);

    @Override
    public int getExecutionOrderId() {

        return 2;
    }

    @Override
    public RealmConfiguration[] getSecondaryUserStoreRealmConfigurations(int tenantId) {

        AbstractUserStoreDAOFactory userStoreDAOFactory = new DatabaseBasedUserStoreDAOFactory();
        RealmConfiguration[] realmConfigurations = new RealmConfiguration[0];
        try {
            realmConfigurations = userStoreDAOFactory.getInstance().getUserStoreRealmsForTenant(tenantId);
        } catch (IdentityUserStoreMgtException e) {
            LOG.error("Error in user store configuration deployment.");
        }
        return realmConfigurations;
    }
}
