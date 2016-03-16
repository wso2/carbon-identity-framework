/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.user.store.count.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.identity.user.store.count.UserStoreCountRetriever;
import org.wso2.carbon.identity.user.store.count.exception.UserStoreCounterException;
import org.wso2.carbon.identity.user.store.count.jdbc.JDBCUserStoreCountRetriever;
import org.wso2.carbon.user.core.listener.UserOperationEventListener;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * @scr.component name="identity.user.store.count.component" immediate="true"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService"
 * unbind="unsetRealmService"
 */

public class UserStoreCountDSComponent {

    private static final Log log = LogFactory.getLog(UserStoreCountDSComponent.class);

    public static RealmService getRealmService() {
        return UserStoreCountDataHolder.getInstance().getRealmService();
    }

    protected void setRealmService(RealmService realmService) {

        UserStoreCountDataHolder.getInstance().setRealmService(realmService);
        if (log.isDebugEnabled()) {
            log.debug("RealmService is set in the User Store Count bundle");
        }
    }

    protected void unsetRealmService(RealmService realmService) {

        UserStoreCountDataHolder.getInstance().setRealmService(null);
        if (log.isDebugEnabled()) {
            log.debug("RealmService is unset in the Application Authentication Framework bundle");
        }
    }

    public static BundleContext getBundleContext() throws UserStoreCounterException {
        BundleContext bundleContext = UserStoreCountDataHolder.getInstance().getBundleContext();
        if (bundleContext == null) {
            String msg = "System has not been started properly. Bundle Context is null.";
            log.error(msg);
            throw new UserStoreCounterException(msg);
        }

        return bundleContext;
    }


    @SuppressWarnings("unchecked")
    protected void activate(ComponentContext ctxt) {
        BundleContext bundleContext = ctxt.getBundleContext();
        UserStoreCountDataHolder.getInstance().setBundleContext(bundleContext);

        UserStoreCountRetriever userStoreCountRetriever = new JDBCUserStoreCountRetriever();
        ServiceRegistration serviceRegistration = ctxt.getBundleContext().registerService(
                UserOperationEventListener.class.getName(), userStoreCountRetriever, null);
        UserStoreCountDataHolder.getInstance().getUserStoreCountRetrievers().put(
                JDBCUserStoreCountRetriever.class.getName(), userStoreCountRetriever);

        if (serviceRegistration != null) {
            if (log.isDebugEnabled()) {
                log.debug("Identity User Store Count -  JDBCUserStoreCountRetriever registered.");
            }
        } else {
            log.error("Identity User Store Count -  JDBCUserStoreCountRetriever could not be registered.");
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.debug("User store count bundle is deactivated");
        }

        UserStoreCountDataHolder.getInstance().setBundleContext(null);
    }

    protected void setUserStoreCountRetriever(UserStoreCountRetriever userStoreCountRetriever) {

        UserStoreCountDataHolder.getInstance().getUserStoreCountRetrievers().put(
                userStoreCountRetriever.getClass().getName(), userStoreCountRetriever);

        if (log.isDebugEnabled()) {
            log.debug("Added user store count retriever : " + userStoreCountRetriever.getClass().getName());
        }
    }

    protected void unsetUserStoreCountRetriever(UserStoreCountRetriever userStoreCountRetriever) {

        UserStoreCountDataHolder.getInstance().getUserStoreCountRetrievers().remove(
                userStoreCountRetriever.getClass().getName());

        if (log.isDebugEnabled()) {
            log.debug("Removed user store count retriever : " + userStoreCountRetriever.getClass().getName());
        }
    }


}
