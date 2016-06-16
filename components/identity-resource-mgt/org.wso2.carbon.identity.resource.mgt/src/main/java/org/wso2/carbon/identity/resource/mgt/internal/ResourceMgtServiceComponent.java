/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.resource.mgt.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.identity.resource.mgt.RegistryResourceMgtService;
import org.wso2.carbon.identity.resource.mgt.RegistryResourceMgtServiceImpl;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * @scr.component name="identity.resource.mgt"
 * immediate="true"
 * @scr.reference name="realm.service"
 * interface="org.wso2.carbon.user.core.service.RealmService"cardinality="1..1"
 * policy="dynamic" bind="setRealmService" unbind="unsetRealmService"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService" cardinality="1..1"
 * policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 */

public class ResourceMgtServiceComponent {

    private static final Log log = LogFactory.getLog(ResourceMgtServiceComponent.class);

    private static RealmService realmService;

    private static RegistryService registryService;

    protected void activate(ComponentContext context) {

        RegistryResourceMgtService resourceMgtService = new RegistryResourceMgtServiceImpl();
        // register the identity resource mgt service
        ServiceRegistration resourceMgtSR = context.getBundleContext().
                registerService(RegistryResourceMgtService.class.getName(), resourceMgtService, null);

        if (resourceMgtSR != null) {
            if (log.isDebugEnabled()) {
                log.debug("Identity Resource Management Service registered.");
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Identity Resource Management Service could not be registered.");
            }
        }

    }

    protected void setRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the Realm Service");
        }
        ResourceMgtServiceComponent.realmService = realmService;
    }

    protected void unsetRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("UnSetting the Realm Service");
        }
        ResourceMgtServiceComponent.realmService = null;
    }

    protected void setRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the Realm Service");
        }
        ResourceMgtServiceComponent.registryService = registryService;
    }

    protected void unsetRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("UnSetting the Realm Service");
        }
        ResourceMgtServiceComponent.registryService = null;
    }

    public static RealmService getRealmService() {
        return realmService;
    }

    public static RegistryService getRegistryService() {
        return registryService;
    }
}
