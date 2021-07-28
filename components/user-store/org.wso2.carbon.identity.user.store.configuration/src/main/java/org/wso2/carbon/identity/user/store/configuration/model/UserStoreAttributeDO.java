/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com).
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

package org.wso2.carbon.identity.user.store.configuration.model;

import java.io.Serializable;

/**
 * Data object to hold user store attribute.
 */
public class UserStoreAttributeDO implements Serializable {

    private String claimUri;
    private String claimId;
    private String mappedAttribute;
    private String displayName;

    public UserStoreAttributeDO() {

    }

    /**
     * Get claim uri of the attribute.
     *
     * @return String claimUri.
     */
    public String getClaimUri() {

        return claimUri;
    }

    /**
     * Set claim uri of the attribute.
     *
     * @param claimUri String claimUri.
     */
    public void setClaimUri(String claimUri) {

        this.claimUri = claimUri;
    }

    /**
     * Get claim id of the attribute.
     *
     * @return String claimId.
     */
    public String getClaimId() {

        return claimId;
    }

    /**
     * Set claim id of the attribute.
     *
     * @param claimId String claimId.
     */
    public void setClaimId(String claimId) {

        this.claimId = claimId;
    }

    /**
     * Get mapped attribute of the attribute.
     *
     * @return String mappedAttribute.
     */
    public String getMappedAttribute() {

        return mappedAttribute;
    }

    /**
     * Set mapped attribute of the attribute.
     *
     * @param mappedAttribute String mappedAttribute.
     */
    public void setMappedAttribute(String mappedAttribute) {

        this.mappedAttribute = mappedAttribute;
    }

    /**
     * Get display name of the attribute.
     *
     * @return String displayName.
     */
    public String getDisplayName() {

        return displayName;
    }

    /**
     * Set display  name of the attribute.
     *
     * @param displayName String displayName.
     */
    public void setDisplayName(String displayName) {

        this.displayName = displayName;
    }
}
