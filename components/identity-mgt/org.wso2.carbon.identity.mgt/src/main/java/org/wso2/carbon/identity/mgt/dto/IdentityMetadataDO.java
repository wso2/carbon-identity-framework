/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.mgt.dto;

/**
 * This object represents an entry of the identity metadata database.
 */
public class IdentityMetadataDO {

    public static final String METADATA_TEMPORARY_CREDENTIAL = "TEMPORARY_CREDENTIAL";
    public static final String METADATA_CONFIRMATION_CODE = "CONFIRMATION_CODE";
    public static final String METADATA_PRIMARAY_SECURITY_QUESTION = "PRIMARAY_SEC_QUESTION";

    private String userName;
    private int tenantId;
    private String metadataType;
    private String metadata;
    private boolean isValid = true;

    public IdentityMetadataDO() {

    }

    /**
     * @param userName
     * @param tenantId
     * @param metadataType
     * @param metadata
     * @param isValid
     */
    public IdentityMetadataDO(String userName, int tenantId, String metadataType,
                              String metadata, boolean isValid) {
        this.setUserName(userName);
        this.setTenantId(tenantId);
        this.setMetadataType(metadataType);
        this.setMetadata(metadata);
        this.setValid(isValid);
    }

    /**
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param userName the userName to set
     * @return
     */
    public IdentityMetadataDO setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    /**
     * @return the tenantId
     */
    public int getTenantId() {
        return tenantId;
    }

    /**
     * @param tenantId the tenantId to set
     * @return
     */
    public IdentityMetadataDO setTenantId(int tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    /**
     * @return the metadataType
     */
    public String getMetadataType() {
        return metadataType;
    }

    /**
     * @param metadataType the metadataType to set
     * @return
     */
    public IdentityMetadataDO setMetadataType(String metadataType) {
        this.metadataType = metadataType;
        return this;
    }

    /**
     * @return the metadata
     */
    public String getMetadata() {
        return metadata;
    }

    /**
     * @param metadata the metadata to set
     * @return
     */
    public IdentityMetadataDO setMetadata(String metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * @return the isValid
     */
    public boolean isValid() {
        return isValid;
    }

    /**
     * @param isValid the isValid to set
     */
    public void setValid(boolean isValid) {
        this.isValid = isValid;
    }

}
