/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.core;

import java.util.Map;
import java.util.Properties;

/**
 * Identity connector configuration.
 */
public interface ConnectorConfig {

    /**
     * Get the connector name.
     *
     * @return connector name
     */
    String getName();

    /**
     * Get the connector friendly name.
     *
     * @return connector friendly name
     */
    String getFriendlyName();

    /**
     * Get the connector category.
     *
     * @return connector category
     */
    String getCategory();

    /**
     * Get the connector sub category.
     *
     * @return connector sub category
     */
    String getSubCategory();

    /**
     * Get the connector order.
     *
     * @return connector order
     */
    int getOrder();

    /**
     * Get the mapping between properties and property display names.
     *
     * @return property to display names mapping
     */
    Map<String, String> getPropertyNameMapping();

    /**
     * Get the mapping between connector properties and property descriptions.
     *
     * @return property to description mapping
     */
    Map<String, String> getPropertyDescriptionMapping();

    /**
     * Get the connector property names.
     *
     * @return connector property names
     */
    String[] getPropertyNames();

    /**
     * Get the connector property default values.
     *
     * @param tenantDomain tenant domain of the config
     * @return default property values
     * @throws ConnectorException
     */
    Properties getDefaultPropertyValues(String tenantDomain) throws ConnectorException;

    /**
     * Get the connector property default values for the given property names.
     *
     * @param propertyNames property names
     * @param tenantDomain tenant domain of the config
     * @return default property values
     * @throws ConnectorException
     */
    Map<String, String> getDefaultPropertyValues(String[] propertyNames, String tenantDomain) throws ConnectorException;

}
