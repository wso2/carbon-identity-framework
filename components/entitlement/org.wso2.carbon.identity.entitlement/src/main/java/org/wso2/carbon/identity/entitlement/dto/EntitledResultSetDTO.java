/*
*  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.identity.entitlement.dto;

import java.util.Arrays;

/**
 * Encapsulates the entitle result set for given search result
 */
public class EntitledResultSetDTO {

    private EntitledAttributesDTO[] entitledAttributesDTOs = new EntitledAttributesDTO[0];

    private boolean advanceResult;

    private String message;

    private String messageType;

    public EntitledAttributesDTO[] getEntitledAttributesDTOs() {
        return Arrays.copyOf(entitledAttributesDTOs, entitledAttributesDTOs.length);
    }

    public void setEntitledAttributesDTOs(EntitledAttributesDTO[] entitledAttributesDTOs) {
        this.entitledAttributesDTOs = Arrays.copyOf(entitledAttributesDTOs, entitledAttributesDTOs.length);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public boolean isAdvanceResult() {
        return advanceResult;
    }

    public void setAdvanceResult(boolean advanceResult) {
        this.advanceResult = advanceResult;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EntitledResultSetDTO)) return false;

        EntitledResultSetDTO that = (EntitledResultSetDTO) o;

        if (advanceResult != that.advanceResult) return false;
        if (!Arrays.equals(entitledAttributesDTOs, that.entitledAttributesDTOs)) return false;
        if (message != null ? !message.equals(that.message) : that.message != null) return false;
        if (messageType != null ? !messageType.equals(that.messageType) : that.messageType != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = entitledAttributesDTOs != null ? Arrays.hashCode(entitledAttributesDTOs) : 0;
        result = 31 * result + (advanceResult ? 1 : 0);
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (messageType != null ? messageType.hashCode() : 0);
        return result;
    }
}
