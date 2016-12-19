/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.entitlement.ui.dto;

/**
 *  @deprecated  As this moved to org.wso2.carbon.identity.entitlement.common
 */
@Deprecated
public class ElementCountDTO {

    private int subElementCount;

    private int attributeDesignatorsElementCount;

    private int attributeValueElementCount;

    private int attributeSelectorElementCount;

    public int getSubElementCount() {
        return subElementCount;
    }

    public void setSubElementCount(int subElementCount) {
        this.subElementCount = subElementCount;
    }

    public int getAttributeSelectorElementCount() {
        return attributeSelectorElementCount;
    }

    public void setAttributeSelectorElementCount(int attributeSelectorElementCount) {
        this.attributeSelectorElementCount = attributeSelectorElementCount;
    }

    public int getAttributeValueElementCount() {
        return attributeValueElementCount;
    }

    public void setAttributeValueElementCount(int attributeValueElementCount) {
        this.attributeValueElementCount = attributeValueElementCount;
    }

    public int getAttributeDesignatorsElementCount() {
        return attributeDesignatorsElementCount;
    }

    public void setAttributeDesignatorsElementCount(int attributeDesignatorsElementCount) {
        this.attributeDesignatorsElementCount = attributeDesignatorsElementCount;
    }
}
