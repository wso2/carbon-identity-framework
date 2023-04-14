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
import org.wso2.carbon.identity.extension.mgt.model.ExtensionDataKey;
import org.wso2.carbon.identity.extension.mgt.model.ExtensionInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Extension store implementation.
 */
public class ExtensionStoreImpl implements ExtensionStore {

    private final Map<String, Map<String, ExtensionInfo>> extensionInfoList = new HashMap<>();

    private final Map<ExtensionDataKey, JSONObject> extensionTemplates = new HashMap<>();

    private final Map<ExtensionDataKey, JSONObject> extensionMetadata = new HashMap<>();

    /**
     * Get all the extensions.
     *
     * @return List of extensions.
     */
    @Override
    public List<ExtensionInfo> getExtensions() {

        List<ExtensionInfo> extensionInfoList = new ArrayList<>();
        this.extensionInfoList.forEach((key, extensionListByType) -> {
            extensionInfoList.addAll(extensionListByType.values());
        });
        return extensionInfoList;
    }

    /**
     * Get all the extensions of a given type.
     *
     * @param extensionType Type of the extension.
     * @return List of extensions.
     */
    @Override
    public List<ExtensionInfo> getExtensionsByType(String extensionType) throws ExtensionManagementException {

        if (!extensionInfoList.containsKey(extensionType)) {
            throw new ExtensionManagementException("Invalid extension type: " + extensionType);
        }
        return new ArrayList<>(extensionInfoList.get(extensionType).values());
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

        if (!extensionInfoList.containsKey(extensionType)) {
            throw new ExtensionManagementException("Invalid extension type: " + extensionType);
        }
        return extensionInfoList.get(extensionType).get(extensionId);
    }

    /**
     * Add a new extension.
     *
     * @param extensionType Type of the extension.
     * @param extensionId Id of the extension.
     * @param extensionInfo ExtensionInfo object.
     */
    @Override
    public void addExtension(String extensionType, String extensionId, ExtensionInfo extensionInfo) {

        if (!extensionInfoList.containsKey(extensionType)) {
            extensionInfoList.put(extensionType, new HashMap<>());
        }
        extensionInfoList.get(extensionType).put(extensionId, extensionInfo);
    }

    /**
     * Get the template of a specific extension.
     *
     * @param extensionType Type of the extension.
     * @param extensionId   Id of the extension.
     * @return Template of the extension.
     */
    @Override
    public JSONObject getTemplate(String extensionType, String extensionId) throws ExtensionManagementException {

        if (!extensionInfoList.containsKey(extensionType)) {
            throw new ExtensionManagementException("Invalid extension type: " + extensionType);
        }
        return extensionTemplates.get(new ExtensionDataKey(extensionType, extensionId));
    }

    /**
     * Add a template to a specific extension.
     *
     * @param extensionType Type of the extension.
     * @param extensionId   Id of the extension.
     * @param extensionTemplate Template of the extension.
     */
    @Override
    public void addTemplate(String extensionType, String extensionId, JSONObject extensionTemplate) {

        this.extensionTemplates.put(new ExtensionDataKey(extensionType, extensionId), extensionTemplate);
    }

    /**
     * Get the metadata of a specific extension.
     *
     * @param extensionType Type of the extension.
     * @param extensionId   Id of the extension.
     * @return Metadata of the extension.
     */
    @Override
    public JSONObject getMetadata(String extensionType, String extensionId) throws ExtensionManagementException {

        if (!extensionInfoList.containsKey(extensionType)) {
            throw new ExtensionManagementException("Invalid extension type: " + extensionType);
        }
        return extensionMetadata.get(new ExtensionDataKey(extensionType, extensionId));
    }

    /**
     * Add metadata to a specific extension.
     *
     * @param extensionType Type of the extension.
     * @param extensionId   Id of the extension.
     * @param extensionMetadata Metadata of the extension.
     */
    @Override
    public void addMetadata(String extensionType, String extensionId, JSONObject extensionMetadata){

        this.extensionMetadata.put(new ExtensionDataKey(extensionType, extensionId), extensionMetadata);
    }
}
