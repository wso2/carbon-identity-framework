/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.claim.mgt.bean;

/**
 * bean that encapsulate claim meta data
 * used when retrieving claims from database
 */
public class MetaDataBean {

    /**
     * claim identifier for the meta data
     */
    private int claimId;

    /**
     * name identifier for the meta data
     */
    private String metaDataKey;

    /**
     * value identifier for the meta data
     */
    private String metaDataValue;

    public int getClaimId() {
        return claimId;
    }

    public void setClaimId(int claimId) {
        this.claimId = claimId;
    }

    public String getMetaDataKey() {
        return metaDataKey;
    }

    public void setMetaDataKey(String metaDataKey) {
        this.metaDataKey = metaDataKey;
    }

    public String getMetaDataValue() {
        return metaDataValue;
    }

    public void setMetaDataValue(String metaDataValue) {
        this.metaDataValue = metaDataValue;
    }


}
