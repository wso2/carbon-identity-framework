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

package org.wso2.carbon.identity.extension.mgt.function;

import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.identity.extension.mgt.model.ExtensionInfo;
import org.wso2.carbon.identity.extension.mgt.utils.ExtensionMgtConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Function to convert a JSONObject to an ExtensionInfo object.
 */
public class JSONObjectToExtensionInfo implements Function<JSONObject, ExtensionInfo> {

    @Override
    public ExtensionInfo apply(JSONObject jsonObject) {

        ExtensionInfo extensionInfo = new ExtensionInfo();
        extensionInfo.setId(jsonObject.getString(ExtensionMgtConstants.ID));
        extensionInfo.setName(jsonObject.getString(ExtensionMgtConstants.NAME));
        extensionInfo.setDescription(jsonObject.getString(ExtensionMgtConstants.DESCRIPTION));
        extensionInfo.setImage(jsonObject.getString(ExtensionMgtConstants.IMAGE));
        extensionInfo.setDisplayOrder(jsonObject.getInt(ExtensionMgtConstants.DISPLAY_ORDER));
        extensionInfo.setTags(getTags(jsonObject.getJSONArray(ExtensionMgtConstants.TAGS)));
        extensionInfo.setCategory(jsonObject.getString(ExtensionMgtConstants.CATEGORY));
        return extensionInfo;
    }

    /**
     * Get the tags from the JSONArray.
     *
     * @param tags JSONArray of tags.
     * @return List of tags.
     */
    private List<String> getTags(JSONArray tags) {

        List<String> tagList = new ArrayList<>();
        for (int i = 0; i < tags.length(); i++) {
            tagList.add(tags.getString(i));
        }
        return tagList;
    }
}
