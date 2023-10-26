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
import org.wso2.carbon.identity.extension.mgt.internal.ExtensionManagerDataHolder;
import org.wso2.carbon.identity.extension.mgt.model.ExtensionInfo;
import org.wso2.carbon.identity.extension.mgt.utils.ExtensionMgtUtils;

import java.util.List;

/**
 * Implementation of the extension manager.
 */
public class ExtensionManagerImpl implements ExtensionManager {

    /**
     * Get all the extensions.
     *
     * @return List of extensions.
     */
    @Override
    public List<ExtensionInfo> getExtensions() {

        return ExtensionManagerDataHolder.getInstance().getExtensionStore().getExtensions();
    }

    /**
     * Get all the extensions of a given type.
     *
     * @param extensionType Type of the extension.
     * @return List of extensions.
     */
    @Override
    public List<ExtensionInfo> getExtensionsByType(String extensionType) throws ExtensionManagementException {

        ExtensionMgtUtils.validateExtensionType(extensionType);
        return ExtensionManagerDataHolder.getInstance().getExtensionStore().getExtensionsByType(extensionType);
    }

    /**
     * Get a specific extension by type and id.
     *
     * @param extensionType Type of the extension.
     * @param extensionId   Id of the extension.
     * @return ExtensionInfo object.
     */
    @Override
    public ExtensionInfo getExtensionByTypeAndId(String extensionType, String extensionId) throws ExtensionManagementException {

        ExtensionMgtUtils.validateExtensionType(extensionType);
        return ExtensionManagerDataHolder.getInstance().getExtensionStore().getExtensionByTypeAndId(extensionType,
                extensionId);
    }

    /**
     * Get all the extension types.
     *
     * @return Array of extension types.
     */
    @Override
    public String[] getExtensionTypes() {

        return ExtensionMgtUtils.getExtensionTypes();
    }

    /**
     * Get the extension template.
     *
     * @param extensionType Type of the extension.
     * @param extensionId   Id of the extension.
     * @return Extension template.
     */
    @Override
    public JSONObject getExtensionTemplate(String extensionType, String extensionId) throws ExtensionManagementException {

        ExtensionMgtUtils.validateExtensionType(extensionType);
        return ExtensionManagerDataHolder.getInstance().getExtensionStore().getTemplate(extensionType,
                extensionId);
    }

    /**
     * Get the extension metadata.
     *
     * @param extensionType Type of the extension.
     * @param extensionId   Id of the extension.
     * @return Extension metadata.
     */
    @Override
    public JSONObject getExtensionMetadata(String extensionType, String extensionId) throws ExtensionManagementException {

        ExtensionMgtUtils.validateExtensionType(extensionType);
        return ExtensionManagerDataHolder.getInstance().getExtensionStore().getMetadata(extensionType,
                extensionId);
    }
}
