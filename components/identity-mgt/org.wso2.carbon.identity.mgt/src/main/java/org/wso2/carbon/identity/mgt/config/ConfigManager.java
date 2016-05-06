/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
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

package org.wso2.carbon.identity.mgt.config;

import org.wso2.carbon.identity.mgt.IdentityMgtConfigException;

/**
 * This is used to manage the tenant specific server configurations.
 */
public interface ConfigManager {

    /**
     * This loads a tenant specific configuration.
     *
     * @param tenantId - tenant Id which the configuration belongs.
     * @return the populated configuration object.
     */
    Config loadConfig(int tenantId) throws IdentityMgtConfigException;

    /**
     * This saves the given configuration in specific tenant space.
     *
     * @param config   - Configuration that needs to be saved.
     * @param tenantId - The tenant Id of the tenant that this configuration needs to be saved.
     * @throws IdentityMgtConfigException 
     */
    void saveConfig(Config config, int tenantId) throws IdentityMgtConfigException;

    /**
     * This is used to set the specific configuration reader impl.
     *
     * @param reader - Configuration reader implementation.
     */
    void setReader(ConfigReader reader);

    /*
     * TODO - is this method needed?
     */
    void setConfig(Config config);

    /**
     * This is used to set the resource path in which the configuration going to be saved.
     *
     * @param path - path to be saved.
     */
    void setResourcePath(String path);

    /**
     * This is used to set the specific configuration writer impl.
     *
     * @param writer - Configuration writer implementation.
     */
    void setWriter(ConfigWriter writer);
}
