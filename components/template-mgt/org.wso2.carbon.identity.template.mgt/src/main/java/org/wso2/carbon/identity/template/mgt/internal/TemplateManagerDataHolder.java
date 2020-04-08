/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.template.mgt.internal;

import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;
import org.wso2.carbon.identity.template.mgt.handler.ReadOnlyTemplateHandler;
import org.wso2.carbon.identity.template.mgt.handler.TemplateHandler;
import org.wso2.carbon.identity.template.mgt.handler.impl.ConfigStoreBasedTemplateHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TemplateManagerDataHolder {

    private static TemplateManagerDataHolder instance = new TemplateManagerDataHolder();

    private ConfigurationManager configurationManager;

    private TemplateHandler readWriteTemplateHandler = new ConfigStoreBasedTemplateHandler();

    private List<ReadOnlyTemplateHandler> readOnlyTemplateHandlers = new ArrayList<>();

    public static TemplateManagerDataHolder getInstance() {

        return instance;
    }

    /**
     * Get the ConfigurationManager object held at the data holder.
     *
     * @return configuration manger object.
     */
    public ConfigurationManager getConfigurationManager() {

        return this.configurationManager;
    }

    /**
     * Set the ConfigurationManager.
     *
     * @param configurationManager configuration manger object.
     */
    public void setConfigurationManager(ConfigurationManager configurationManager) {

        this.configurationManager = configurationManager;
    }

    /**
     * Get read write template handler.
     *
     * @return template handler.
     */
    public TemplateHandler getReadWriteTemplateHandler() {

        return readWriteTemplateHandler;
    }

    /**
     * Set read write template handler.
     *
     * @param readWriteTemplateHandler template handler.
     */
    public void setReadWriteTemplateHandler(TemplateHandler readWriteTemplateHandler) {

        this.readWriteTemplateHandler = readWriteTemplateHandler;
    }

    /**
     * Get template handlers.
     *
     * @return list of template handlers.
     */
    public List<ReadOnlyTemplateHandler> getReadOnlyTemplateHandlers() {

        return Collections.unmodifiableList(readOnlyTemplateHandlers);
    }

    /**
     * Add template handler.
     *
     * @param templateHandler template handler.
     */
    public void addReadOnlyTemplateHandler(ReadOnlyTemplateHandler templateHandler) {

        this.readOnlyTemplateHandlers.add(templateHandler);
    }

    /**
     * Remove template handler.
     *
     * @param templateHandler template handler.
     */
    public void removeReadOnlyTemplateHandler(ReadOnlyTemplateHandler templateHandler) {

        this.readOnlyTemplateHandlers.remove(templateHandler);
    }
}
