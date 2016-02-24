/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.carbon.identity.notification.mgt.internal;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.identity.notification.mgt.NotificationManagementException;
import org.wso2.carbon.identity.notification.mgt.NotificationMgtConfigBuilder;
import org.wso2.carbon.identity.notification.mgt.NotificationMgtConstants;
import org.wso2.carbon.identity.notification.mgt.NotificationSender;
import org.wso2.carbon.identity.notification.mgt.NotificationSendingModule;
import org.wso2.carbon.identity.notification.mgt.bean.ModuleConfiguration;

import javax.mail.MessageRemovedException;
import java.util.ArrayList;
import java.util.List;

/**
 * @scr.reference name="ldap.tenant.manager.listener.service"
 * interface="org.wso2.carbon.identity.notification.mgt.NotificationSendingModule"
 * cardinality="0..n" policy="dynamic"
 * bind="addNotificationSendingModule"
 * unbind="removeNotificationSendingModule"
 * @scr.component name="carbon.identity.notification.mgt" immediate="true"
 */

@SuppressWarnings("unused")
public class NotificationManagementServiceComponent {

    private static final Log log = LogFactory.getLog(NotificationManagementServiceComponent.class);
    /**
     * Size of the thread pool for distributing events to subscribed modules
     */
    int threadPoolSize = 0;
    /**
     * NotificationSender instance which is exposed as the service.
     */
    private NotificationSender notificationSender;
    /**
     * Notification management configurations
     */
    private NotificationMgtConfigBuilder configBuilder;
    /**
     * Since Message Sending modules are dynamically registered a List is used
     */
    private List<NotificationSendingModule> notificationSendingModules = new ArrayList<NotificationSendingModule>();

    protected void activate(ComponentContext context) {
        // Register Notification sender as an OSGI service. Other components can consume the service for sending
        // messages on a registered event
        try {
            // Pass the bundle context to read property file in a case it is not found in default location.
            try {
                configBuilder = new NotificationMgtConfigBuilder(context.getBundleContext());
            } catch (NotificationManagementException e) {
                log.error("Error while building Notification Mgt configuration", e);
            }

            // Read the thread pool size from configurations. If not present in configurations use default value.
            if (configBuilder != null && configBuilder.getThreadPoolSize() != null) {
                try {
                    threadPoolSize = Integer.parseInt(configBuilder.getThreadPoolSize());
                } catch (NumberFormatException e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Error while parsing thread pool size configuration, " +
                                "setting default size :" + NotificationMgtConstants.THREAD_POOL_DEFAULT_SIZE);
                    }
                    threadPoolSize = NotificationMgtConstants.THREAD_POOL_DEFAULT_SIZE;
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("No configuration found for thread pool size, " +
                            "setting default size :" + NotificationMgtConstants.THREAD_POOL_DEFAULT_SIZE);
                }
                threadPoolSize = NotificationMgtConstants.THREAD_POOL_DEFAULT_SIZE;
            }
            if (log.isDebugEnabled()) {
                log.debug("Notification mgt thread pool size " + threadPoolSize);
            }
            // Register Notification sender as the service class
            notificationSender = new NotificationSender(notificationSendingModules, threadPoolSize);
            context.getBundleContext().registerService(NotificationSender.class.getName(),
                    notificationSender, null);
            if (log.isDebugEnabled()) {
                log.debug("Notification Management bundle is activated");
            }
            // Catch throwable since there may be run time exceptions.
        } catch (Throwable e) {
            log.error("Error while initiating Notification Management component", e);
        }
    }

    protected void deactivate(ComponentContext context) {
        if (log.isDebugEnabled()) {
            log.debug("Notification Management bundle is deactivated");
        }
        notificationSender.stopService();
    }

    /**
     * Will register message sending modules dynamically. This method is used to bind the notification sending
     * modules in to msg mgt component
     *
     * @param module MessageSendingModule
     */
    protected void addNotificationSendingModule(NotificationSendingModule module) throws MessageRemovedException {

        ModuleConfiguration moduleConfiguration;
        if (StringUtils.isEmpty(module.getModuleName())) {
            if (log.isDebugEnabled()) {
                log.debug("Cannot register module without a valid module name");
            }
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Registering a message sending module " + module.getModuleName());
        }

        if (configBuilder != null) {
            moduleConfiguration = configBuilder.getModuleConfigurations(module.getModuleName());
        } else {
            moduleConfiguration = new ModuleConfiguration();
        }
        try {
            module.init(moduleConfiguration);
            notificationSendingModules.add(module);
        } catch (NotificationManagementException e) {
            log.error("Error while initializing Notification sending module " + module.getModuleName(), e);
        }
    }

    /**
     * This method is used to unbind notification sending modules. Whenever a module is dynamically stops,
     * this method will unregister them.
     *
     * @param module MessageSendingModule
     */
    protected void removeNotificationSendingModule(NotificationSendingModule module) {
        if (log.isDebugEnabled()) {
            log.debug("Removing a message module " + module.getModuleName());
        }
        notificationSendingModules.remove(module);
    }
}