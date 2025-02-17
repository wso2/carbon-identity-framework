/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.system.config.mgt.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;
import org.wso2.carbon.identity.secret.mgt.core.SecretManager;
import org.wso2.carbon.identity.secret.mgt.core.SecretResolveManager;

/**
 * Service component Holder for the System Config management.
 */
public class SystemConfigMgtServiceHolder {

    private static final Log log = LogFactory.getLog(SystemConfigMgtServiceHolder.class);

    private ConfigurationManager configurationManager;
    private SecretManager secretManager;
    private SecretResolveManager secretResolveManager;

    private SystemConfigMgtServiceHolder() {

    }

    public static SystemConfigMgtServiceHolder getInstance() {

        return SingletonHelper.INSTANCE;
    }

    public ConfigurationManager getConfigurationManager() {

        return configurationManager;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager) {

        this.configurationManager = configurationManager;
    }

    /**
     * SingletonHelper for the singleton instance of CORSServiceHolder.
     */
    private static class SingletonHelper {

        private static final SystemConfigMgtServiceHolder INSTANCE = new SystemConfigMgtServiceHolder();
    }

    /**
     * Get the SecretManager.
     *
     * @return SecretManager instance.
     */
    public SecretManager getSecretManager() {

        return secretManager;
    }

    /**
     * Set the SecretManager.
     *
     * @param secretManager SecretManager instance.
     */
    public void setSecretManager(SecretManager secretManager) {

        this.secretManager = secretManager;
    }

    /**
     * Get the SecretResolveManager.
     *
     * @return SecretResolveManager instance.
     */
    public SecretResolveManager getSecretResolveManager() {

        return secretResolveManager;
    }

    /**
     * Set the SecretResolveManager.
     *
     * @param secretResolveManager SecretResolveManager instance.
     */
    public void setSecretResolveManager(SecretResolveManager secretResolveManager) {

        this.secretResolveManager = secretResolveManager;
    }
}
