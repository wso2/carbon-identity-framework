/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.input.validation.mgt.model;

/**
 * Object for property model.
 */
public class Property {

    private String name;
    private String displayName;
    private String description;
    private String type;
    private int displayOrder;

    /**
     * Method to get name.
     *
     * @return  Property name.
     */
    public String getName() {

        return name;
    }

    /**
     * Method to get display name.
     *
     * @return  Display name.
     */
    public String getDisplayName() {

        return displayName;
    }

    /**
     * Method to get description.
     *
     * @return  Description.
     */
    public String getDescription() {

        return description;
    }

    /**
     * Method to get type.
     *
     * @return  Type.
     */
    public String getType() {

        return type;
    }

    /**
     * Method to get display order.
     *
     * @return  Display order.
     */
    public int getDisplayOrder() {

        return displayOrder;
    }

    /**
     * Method to set name.
     *
     * @param name  Name of the property.
     */
    public void setName(String name) {

        this.name = name;
    }

    /**
     * Method to set display name.
     *
     * @param displayName   Display name.
     */
    public void setDisplayName(String displayName) {

        this.displayName = displayName;
    }

    /**
     * Method to set description.
     *
     * @param description   Description.
     */
    public void setDescription(String description) {

        this.description = description;
    }

    /**
     * Method to set type.
     *
     * @param type  Type.
     */
    public void setType(String type) {

        this.type = type;
    }

    /**
     * Method to set display order.
     *
     * @param displayOrder  Display order.
     */
    public void setDisplayOrder(int displayOrder) {

        this.displayOrder = displayOrder;
    }
}
