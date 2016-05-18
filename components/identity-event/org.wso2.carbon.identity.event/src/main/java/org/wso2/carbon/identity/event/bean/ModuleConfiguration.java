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

package org.wso2.carbon.identity.event.bean;

import org.wso2.carbon.identity.core.handler.InitConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Configuration bean which is passed to Notification sending modules by the notification management component. This
 * includes all configuration information which are specific to a certain notification sending component.
 */
@SuppressWarnings("unused")
public class ModuleConfiguration extends InitConfig {
    /**
     * Module level properties. eg moduleName.propertyName
     */
    private Properties moduleProperties;
    /**
     * List of subscriptions by the particular module
     */
    private List<Subscription> subscriptions;

    /**
     * Overridden to add module properties and subscription list
     *
     * @param moduleProperties Set of properties which are configured to module level.
     * @param subscriptions    List of subscriptions for a module
     */
    public ModuleConfiguration(Properties moduleProperties, List<Subscription> subscriptions) {
        this.moduleProperties = moduleProperties;
        this.subscriptions = subscriptions;
    }

    /**
     * Overridden default constructor to make sure that configuration can be initiated without module params and list
     * of subscriptions. From this, those variables are initiated
     */
    public ModuleConfiguration() {
        moduleProperties = new Properties();
        subscriptions = new ArrayList<Subscription>();
    }

    public Properties getModuleProperties() {
        return moduleProperties;
    }

    /**
     * To get set of subscriptions configured for the module
     *
     * @return A set of subscriptions.
     */
    public List<Subscription> getSubscriptions() {
        return new ArrayList<Subscription>(subscriptions);
    }
}
