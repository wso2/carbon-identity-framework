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

package org.wso2.carbon.identity.extension.mgt.model;

import java.util.List;
import java.util.Map;

/**
 * Extension information model.
 */
public class ExtensionInfo {

    private String id;
    private String name;
    private String description;
    private String image;
    private Integer displayOrder;
    private List<String> tags = null;
    private String category;

    private String type;
    private List<Map<String, Object>> additionalProperties;

    /**
     **/
    public ExtensionInfo() {

    }

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    public String getImage() {

        return image;
    }

    public void setImage(String image) {

        this.image = image;
    }

    public Integer getDisplayOrder() {

        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {

        this.displayOrder = displayOrder;
    }

    public List<String> getTags() {

        return tags;
    }

    public void setTags(List<String> tags) {

        this.tags = tags;
    }

    public String getCategory() {

        return category;
    }

    public void setCategory(String category) {

        this.category = category;
    }

    public String getType() {

        return type;
    }

    public void setType(String type) {

        this.type = type;
    }

    public List<Map<String, Object>> getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(List<Map<String, Object>> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }
}
