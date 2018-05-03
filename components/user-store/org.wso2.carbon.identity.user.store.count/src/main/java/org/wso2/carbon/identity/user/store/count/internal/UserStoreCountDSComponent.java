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
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.identity.core.util.IdentityCoreInitializedEvent;
import org.wso2.carbon.identity.user.store.count.AbstractCountRetrieverFactory;
import org.wso2.carbon.identity.user.store.count.UserStoreCountRetriever;
import org.wso2.carbon.identity.user.store.count.exception.UserStoreCounterException;
import org.wso2.carbon.identity.user.store.count.jdbc.JDBCCountRetrieverFactory;
import org.wso2.carbon.identity.user.store.count.jdbc.JDBCUserStoreCountRetriever;
import org.wso2.carbon.identity.user.store.count.jdbc.internal.InternalCountRetriever;
import org.wso2.carbon.identity.user.store.count.jdbc.internal.InternalCountRetrieverFactory;
import org.wso2.carbon.user.core.service.RealmService;

@Component(
        name = "identity.user.store.count.component",
        immediate = true
)
public class UserStoreCountDSComponent {

    private static final Log log = LogFactory.getLog(UserStoreCountDSComponent.class);

    public static RealmService getRealmService() {
        return UserStoreCountDataHolder.getInstance().getRealmService();
    }

    @Reference(
            name = "user.realmservice.default",
            service = RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService"
    )
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

        AbstractCountRetrieverFactory jdbcCountRetrieverFactory = new JDBCCountRetrieverFactory();
        AbstractCountRetrieverFactory internalCountRetrieverFactory = new InternalCountRetrieverFactory();
        ServiceRegistration serviceRegistration = bundleContext
                .registerService(AbstractCountRetrieverFactory.class.getName(), jdbcCountRetrieverFactory, null);
        bundleContext
                .registerService(AbstractCountRetrieverFactory.class.getName(), internalCountRetrieverFactory, null);

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

    @Reference(
            name = "user.store.count",
            service = AbstractCountRetrieverFactory.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetCountRetrieverFactory"
    )
    protected void setCountRetrieverFactory(AbstractCountRetrieverFactory countRetrieverFactory) {

        UserStoreCountDataHolder.getInstance().getCountRetrieverFactories()
                .put(countRetrieverFactory.getCounterType(), countRetrieverFactory);
        if (log.isDebugEnabled()) {
            log.debug("Added count retriever : " + countRetrieverFactory.getCounterType());
        }

    }

    protected void unsetCountRetrieverFactory(AbstractCountRetrieverFactory countRetrieverFactory) {

        UserStoreCountDataHolder.getInstance().getCountRetrieverFactories()
                .remove(countRetrieverFactory.getCounterType());

        if (log.isDebugEnabled()) {
            log.debug("Removed count retriever : " + countRetrieverFactory.getCounterType());
        }
    }

    @Reference(
            name = "identityCoreInitializedEventService",
            service = IdentityCoreInitializedEvent.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetIdentityCoreInitializedEventService"
    )
    protected void setIdentityCoreInitializedEventService(IdentityCoreInitializedEvent identityCoreInitializedEvent) {
        /* reference IdentityCoreInitializedEvent service to guarantee that this component will wait until identity core
         is started */
    }

    protected void unsetIdentityCoreInitializedEventService(IdentityCoreInitializedEvent identityCoreInitializedEvent) {
        /* reference IdentityCoreInitializedEvent service to guarantee that this component will wait until identity core
         is started */
    }

}
