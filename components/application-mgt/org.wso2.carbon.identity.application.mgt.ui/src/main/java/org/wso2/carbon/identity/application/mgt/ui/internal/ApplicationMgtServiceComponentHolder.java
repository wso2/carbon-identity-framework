/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.mgt.ui.internal;

import org.wso2.carbon.consent.mgt.core.ConsentManager;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * A singleton to hold the OSGi services received during the component activation.
 */
public class ApplicationMgtServiceComponentHolder {

    private static ApplicationMgtServiceComponentHolder instance = new ApplicationMgtServiceComponentHolder();

    private ConfigurationContextService configurationContextService;
    private ConsentManager consentManager;

    public static ApplicationMgtServiceComponentHolder getInstance() {
        return instance;
    }

    public ConfigurationContextService getConfigurationContextService() {
        return configurationContextService;
    }

    public void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        this.configurationContextService = configurationContextService;
    }

    public ConsentManager getConsentManager() {

        return consentManager;
    }

    public void setConsentManager(ConsentManager consentManager) {

        this.consentManager = consentManager;
    }
}
