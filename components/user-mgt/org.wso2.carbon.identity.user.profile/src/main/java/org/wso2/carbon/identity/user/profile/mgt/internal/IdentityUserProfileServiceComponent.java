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
import org.wso2.carbon.identity.user.profile.mgt.listener.ProfileMgtEventListener;
import org.wso2.carbon.identity.user.profile.mgt.util.ServiceHodler;
import org.wso2.carbon.identity.user.store.configuration.listener.UserStoreConfigListener;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.listener.UserOperationEventListener;

/**
 * @scr.component name="identity.user.profile.mgt.component" immediate="true"
 * @scr.reference name="user.realm.default"
 * interface="org.wso2.carbon.user.core.UserRealm"
 * cardinality="1..1" policy="dynamic"
 * bind="setUserRealmDefault" unbind="unsetUserRealmDefault"
 */
public class IdentityUserProfileServiceComponent {

    private static final Log log = LogFactory.getLog(IdentityUserProfileServiceComponent.class);

    protected void activate(ComponentContext ctxt) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("User Profile Mgt bundle is activated ");
            }

            ServiceRegistration userStoreConfigEventSR = ctxt.getBundleContext().registerService(
                    UserStoreConfigListener.class.getName(), new UserStoreConfigListenerImpl(), null);
            if (userStoreConfigEventSR != null) {
                if (log.isDebugEnabled()) {
                    log.debug("User profile management - UserStoreConfigListener registered.");
                }
            } else {
                log.error("User profile management - UserStoreConfigListener could not be registered.");
            }
            ServiceRegistration profileMgtEventSR = ctxt.getBundleContext().registerService(
                    UserOperationEventListener.class.getName(), new ProfileMgtEventListener(), null);
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

}
