/*
*  Copyright (c)  WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.entitlement.ui;

import org.wso2.carbon.identity.entitlement.stub.dto.PublisherPropertyDTO;

import java.util.Comparator;

/**
 * Comparator implementation to sort the <code>ModulePropertyDTO</code> object array
 */

/**
 *  @deprecated  As this moved to org.wso2.carbon.identity.entitlement.common
 */
@Deprecated
public class PropertyDTOComparator implements Comparator {

    @Override
    public int compare(Object o1, Object o2) {

        PublisherPropertyDTO dto1 = (PublisherPropertyDTO) o1;
        PublisherPropertyDTO dto2 = (PublisherPropertyDTO) o2;
        if (dto1.getDisplayOrder() < dto2.getDisplayOrder()) {
            return -1;
        } else if (dto1.getDisplayOrder() == dto2.getDisplayOrder()) {
            return 0;
        } else {
            return 1;
        }
    }
}
