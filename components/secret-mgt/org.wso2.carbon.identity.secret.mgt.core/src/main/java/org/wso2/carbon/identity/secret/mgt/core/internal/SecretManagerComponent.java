/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com).
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

package org.wso2.carbon.identity.secret.mgt.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.secret.mgt.core.SecretManager;
import org.wso2.carbon.identity.secret.mgt.core.SecretManagerImpl;
import org.wso2.carbon.identity.secret.mgt.core.SecretResolveManager;
import org.wso2.carbon.identity.secret.mgt.core.SecretResolveManagerImpl;
import org.wso2.carbon.identity.secret.mgt.core.dao.SecretDAO;
import org.wso2.carbon.identity.secret.mgt.core.dao.impl.CachedBackedSecretDAO;
import org.wso2.carbon.identity.secret.mgt.core.dao.impl.SecretDAOImpl;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.wso2.carbon.identity.secret.mgt.core.constant.SecretConstants.DB_TABLE_NAME;

/**
 * OSGi declarative services component which handles registration and un-registration of configuration management
 * service.
 */
@Component(
        name = "carbon.secret.mgt.component",
        immediate = true
)
public class SecretManagerComponent {

    private static final Log log = LogFactory.getLog(SecretManagerComponent.class);

    /**
     * Register SecretManager as an OSGI service.
     *
     * @param componentContext OSGI service component context.
     */
    @Activate
    protected void activate(ComponentContext componentContext) {

        BundleContext bundleContext = componentContext.getBundleContext();
        SecretDAO secretDAO = new SecretDAOImpl();
        SecretManagerComponentDataHolder.getInstance().setSecretDAOS(new ArrayList<>());
        bundleContext.registerService(SecretDAO.class.getName(),
                new CachedBackedSecretDAO(secretDAO), null);
        bundleContext.registerService(SecretManager.class.getName(),
                new SecretManagerImpl(), null);
        bundleContext.registerService(SecretResolveManager.class.getName(),
                new SecretResolveManagerImpl(), null);
        SecretManagerComponentDataHolder.getInstance().setSecretManagementEnabled
                (isSecretManagementEnabled());
    }

    @Reference(
            name = "secret.dao",
            service = org.wso2.carbon.identity.secret.mgt.core.dao.SecretDAO.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetSecret"
    )
    protected void setSecret(SecretDAO secretDAO) {

        if (secretDAO != null) {
            if (log.isDebugEnabled()) {
                log.debug("Secret DAO is registered in SecretManager service.");
            }

            SecretManagerComponentDataHolder.getInstance().getSecretDAOS().add(secretDAO);
            SecretManagerComponentDataHolder.getInstance().getSecretDAOS().sort(Comparator.comparingInt(SecretDAO::getPriority));
        }
    }

    protected void unsetSecret(SecretDAO secretDAO) {

        if (log.isDebugEnabled()) {
            log.debug("Purpose DAO is unregistered in SecretManager service.");
        }
        SecretManagerComponentDataHolder.getInstance().getSecretDAOS().remove(secretDAO);
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
        SecretManagerComponentDataHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {

        if (log.isDebugEnabled()) {
            log.debug("Unsetting the Realm Service");
        }
        SecretManagerComponentDataHolder.getInstance().setRealmService(null);
    }

    private boolean isSecretManagementEnabled() {

        return FrameworkUtils.isTableExists(DB_TABLE_NAME);
    }
}
