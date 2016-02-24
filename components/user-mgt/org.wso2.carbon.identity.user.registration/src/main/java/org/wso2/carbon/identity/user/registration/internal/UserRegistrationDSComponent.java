/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.user.registration.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;


/**
 * @scr.component name="usermgt.component"" immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService"
 * unbind="unsetRegistryService"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService"
 * unbind="unsetRealmService"
 */
public class UserRegistrationDSComponent {

    private static final Log log = LogFactory.getLog(UserRegistrationDSComponent.class);

    private static RegistryService registryService = null;
    private static RealmService realmService = null;

    public static RegistryService getRegistryService() {
        return registryService;
    }

    protected void setRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.info("Setting the Registry Service");
        }
        UserRegistrationDSComponent.registryService = registryService;
    }

    public static RealmService getRealmService() {
        return realmService;
    }

    protected void setRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.info("Setting the Realm Service");
        }
        UserRegistrationDSComponent.realmService = realmService;
    }

    protected void activate(ComponentContext ctxt) {
        log.debug("UserRegistration bundle is activated ");
    }

    protected void deactivate(ComponentContext ctxt) {
        log.debug("UserRegistration bundle is deactivated ");
    }

    protected void unsetRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.info("Unsetting the Registry Service");
        }
        UserRegistrationDSComponent.registryService = null;
    }

    protected void unsetRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.info("Unsetting the Realm Service");
        }
        UserRegistrationDSComponent.realmService = null;
    }

}
