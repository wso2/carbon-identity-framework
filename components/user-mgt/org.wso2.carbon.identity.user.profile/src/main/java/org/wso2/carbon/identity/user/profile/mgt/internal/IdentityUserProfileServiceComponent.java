/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.user.profile.mgt.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.identity.core.util.IdentityCoreInitializedEvent;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.user.profile.mgt.listener.ProfileMgtEventListener;
import org.wso2.carbon.identity.user.profile.mgt.util.ServiceHodler;
import org.wso2.carbon.identity.user.store.configuration.listener.UserStoreConfigListener;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.listener.UserOperationEventListener;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component(
         name = "identity.user.profile.mgt.component", 
         immediate = true)
public class IdentityUserProfileServiceComponent {

    private static final Log log = LogFactory.getLog(IdentityUserProfileServiceComponent.class);

    @Activate
    protected void activate(ComponentContext ctxt) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("User Profile Mgt bundle is activated ");
            }
            ServiceRegistration userStoreConfigEventSR = ctxt.getBundleContext().registerService(UserStoreConfigListener.class.getName(), new UserStoreConfigListenerImpl(), null);
            if (userStoreConfigEventSR != null) {
                if (log.isDebugEnabled()) {
                    log.debug("User profile management - UserStoreConfigListener registered.");
                }
            } else {
                log.error("User profile management - UserStoreConfigListener could not be registered.");
            }
            ServiceRegistration profileMgtEventSR = ctxt.getBundleContext().registerService(UserOperationEventListener.class.getName(), new ProfileMgtEventListener(), null);
            // Check whether the IDN tables exist at the beginning.
            ServiceHodler.setIsIDNTableExist(isIDNTablesExist());
            if (log.isDebugEnabled()) {
                log.debug("Is IDN_ASSOCIATED_ID table exist: " + ServiceHodler.isIDNTableExist());
            }
            if (profileMgtEventSR != null) {
                if (log.isDebugEnabled()) {
                    log.debug("User profile management - ProfileMgtEventListener registered.");
                }
            } else {
                log.error("User profile management - ProfileMgtEventListener could not be registered.");
            }
        } catch (Throwable e) {
            log.error("Failed to activate ProfileMgt bundle ", e);
        }
    }

    // the below two methods are kept just to be sure the realm is available.
    @Reference(
             name = "user.realm.default", 
             service = org.wso2.carbon.user.core.UserRealm.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetUserRealmDefault")
    protected void setUserRealmDefault(UserRealm userRealmDefault) {
        if (log.isDebugEnabled()) {
            log.debug("Setting DefaultRealm in User Profile Management");
        }
        ServiceHodler.setInternalUserStore(userRealmDefault);
    }

    protected void unsetUserRealmDefault(UserRealm userRealmDefault) {
        if (log.isDebugEnabled()) {
            log.info("Un-setting DefaultRealm in User Profile Management");
        }
        ServiceHodler.setInternalUserStore(null);
    }

    @Reference(
             name = "identityCoreInitializedEventService", 
             service = org.wso2.carbon.identity.core.util.IdentityCoreInitializedEvent.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetIdentityCoreInitializedEventService")
    protected void setIdentityCoreInitializedEventService(IdentityCoreInitializedEvent identityCoreInitializedEvent) {
    /* reference IdentityCoreInitializedEvent service to guarantee that this component will wait until identity core
         is started */
    }

    protected void unsetIdentityCoreInitializedEventService(IdentityCoreInitializedEvent identityCoreInitializedEvent) {
    /* reference IdentityCoreInitializedEvent service to guarantee that this component will wait until identity core
         is started */
    }

    /**
     * Checks whether the IDN_ASSOCIATED_ID table is present.
     * @return True if table exist.
     */
    private boolean isIDNTablesExist() {
        try (Connection connection = IdentityDatabaseUtil.getDBConnection()) {
            // Try to find whether there is IDN_ASSOCIATED_ID table.
            DatabaseMetaData metaData = connection.getMetaData();
            String tableName = "IDN_ASSOCIATED_ID";
            if (metaData.storesLowerCaseIdentifiers()) {
                tableName = tableName.toLowerCase();
            }
            try (ResultSet resultSet = metaData.getTables(null, null, tableName, new String[] { "TABLE" })) {
                if (resultSet.next()) {
                    connection.commit();
                    return true;
                }
            }
            connection.commit();
        } catch (SQLException e) {
            return false;
        }
        return false;
    }
}

