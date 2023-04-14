/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.extension.mgt;

import org.json.JSONObject;
import org.wso2.carbon.identity.extension.mgt.exception.ExtensionManagementException;
import org.wso2.carbon.identity.extension.mgt.model.ExtensionInfo;

import java.util.List;

/**
 * Extension store interface.
 */
public interface ExtensionStore {

    /**
     * Get all the extensions.
     *
     * @return List of extensions.
     */
    List<ExtensionInfo> getExtensions();

    /**
     * Get all the extensions of a given type.
     *
     * @param extensionType Type of the extension.
     * @return List of extensions.
     */
    List<ExtensionInfo> getExtensionsByType(String extensionType) throws ExtensionManagementException;

    /**
     * Get a specific extension by type and id.
     *
     * @param extensionType Type of the extension.
     * @param extensionId   Id of the extension.
     * @return ExtensionInfo object.
     */
    ExtensionInfo getExtensionByTypeAndId(String extensionType, String extensionId) throws ExtensionManagementException;

    /**
     * Add a new extension.
     *
     * @param extensionType Type of the extension.
     * @param extensionId Id of the extension.
     * @param extensionInfo ExtensionInfo object.
     */
    void addExtension(String extensionType, String extensionId, ExtensionInfo extensionInfo) throws ExtensionManagementException;

    /**
     * Get template of a specific extension by type and id.
     *
     * @param extensionType Type of the extension.
     * @param extensionId Id of the extension.
     * @return Template of the extension.
     */
    JSONObject getTemplate(String extensionType, String extensionId) throws ExtensionManagementException;

    /**
     * Add template of a specific extension by type and id.
     *
     * @param extensionType Type of the extension.
     * @param extensionId Id of the extension.
     * @param extensionTemplate Template of the extension.
     */
    void addTemplate(String extensionType, String extensionId, JSONObject extensionTemplate) throws ExtensionManagementException;

    /**
     * Get metadata of a specific extension by type and id.
     *
     * @param extensionType Type of the extension.
     * @param extensionId Id of the extension.
     * @return Metadata of the extension.
     */
    JSONObject getMetadata(String extensionType, String extensionId) throws ExtensionManagementException;

    /**
     * Add metadata of a specific extension by type and id.
     *
     * @param extensionType Type of the extension.
     * @param extensionId Id of the extension.
     * @param extensionMetadata Metadata of the extension.
     */
    void addMetadata(String extensionType, String extensionId, JSONObject extensionMetadata) throws ExtensionManagementException;

}
