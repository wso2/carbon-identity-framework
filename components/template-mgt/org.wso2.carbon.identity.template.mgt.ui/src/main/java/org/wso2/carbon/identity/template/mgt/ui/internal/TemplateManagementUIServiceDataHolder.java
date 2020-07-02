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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.template.mgt.ui.internal;

import org.wso2.carbon.identity.template.mgt.TemplateManager;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * This singleton data holder contains all the data required by the Template UI OSGi bundle
 */
public class TemplateManagementUIServiceDataHolder {

    private static TemplateManagementUIServiceDataHolder instance = new TemplateManagementUIServiceDataHolder();
    private TemplateManager templateManager;
    private RealmService realmService;

    private TemplateManagementUIServiceDataHolder() {

    }

    public static TemplateManagementUIServiceDataHolder getInstance() {

        return instance;
    }

    public TemplateManager getTemplateManager() {

        return templateManager;
    }

    public void setTemplateManager(TemplateManager templateManager) {

        this.templateManager = templateManager;
    }

    public RealmService getRealmService() {

        return realmService;
    }

    public void setRealmService(RealmService realmService) {

        this.realmService = realmService;
    }
}
