/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.remotefetch.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.remotefetch.common.RemoteFetchComponentRegistry;
import org.wso2.carbon.identity.remotefetch.core.RemoteFetchComponentRegistryImpl;
import org.wso2.carbon.identity.remotefetch.core.RemoteFetchCore;
import org.wso2.carbon.identity.remotefetch.core.implementations.actionHandlers.PollingActionListenerComponent;
import org.wso2.carbon.identity.remotefetch.core.implementations.configDeployers.ServiceProviderConfigDeployerComponent;
import org.wso2.carbon.identity.remotefetch.core.implementations.repositoryHandlers.GitRepositoryManagerComponent;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;

@Component(
        name = "identity.application.remotefetch.component",
        immediate = true
)
public class RemoteFetchServiceComponent {

    private static final Log log = LogFactory.getLog(RemoteFetchServiceComponent.class);

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Activate
    protected void activate(ComponentContext context) {

        RemoteFetchComponentRegistry remoteFetchComponentRegistry = new RemoteFetchComponentRegistryImpl();

        remoteFetchComponentRegistry.registerRepositoryManager(new GitRepositoryManagerComponent());
        remoteFetchComponentRegistry.registerConfigDeployer(new ServiceProviderConfigDeployerComponent());
        remoteFetchComponentRegistry.registerActionListener(new PollingActionListenerComponent());

        RemoteFetchServiceComponentHolder.getInstance().setRemoteFetchComponentRegistry(remoteFetchComponentRegistry);
        RemoteFetchServiceComponentHolder.getInstance().setDataSource(this.getDataSource());

        BundleContext bundleContext = context.getBundleContext();
        bundleContext.registerService(RemoteFetchComponentRegistry.class.getName(),
                RemoteFetchServiceComponentHolder.getInstance().getRemoteFetchComponentRegistry(), null);

        RemoteFetchCore core = new RemoteFetchCore();
        try {
            scheduler.scheduleAtFixedRate(core, 0, 60, TimeUnit.SECONDS);
            log.info("Identity RemoteFetchServiceComponent bundle is activated");
        } catch (Exception e) {
            log.error("Error while activating RemoteFetchServiceComponent bundle", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        scheduler.shutdownNow();

        if (log.isDebugEnabled()) {
            log.debug("Identity RemoteFetchServiceComponent bundle is deactivated");
        }
    }

    @Reference(
            name = "user.applicationmanagementservice.default",
            service = ApplicationManagementService.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetApplicationManagerService"
    )
    protected void setApplicationManagerService(ApplicationManagementService applicationManagerService) {

        RemoteFetchServiceComponentHolder.getInstance().setApplicationManagementService(applicationManagerService);
    }

    protected void unsetApplicationManagerService(ApplicationManagementService applicationManagerService) {

        RemoteFetchServiceComponentHolder.getInstance().setApplicationManagementService(null);
    }

    @Reference(
            name = "user.realmservice.default",
            service = RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService"
    )
    protected void setRealmService(RealmService realmService) {

        RemoteFetchServiceComponentHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {

        RemoteFetchServiceComponentHolder.getInstance().setRealmService(null);
    }

    private DataSource getDataSource(){
        return IdentityDatabaseUtil.getDataSource();
    }
}
