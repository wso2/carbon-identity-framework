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

package org.wso2.carbon.identity.entitlement;

import org.wso2.carbon.identity.entitlement.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.dto.PolicyStoreDTO;

import java.io.Serializable;
import java.util.Comparator;

/**
 *
 */
public class PolicyOrderComparator implements Serializable, Comparator {

    private static final long serialVersionUID = -4125227115004608650L;

    @Override
    public int compare(Object o1, Object o2) {

        if (o1 instanceof PolicyStoreDTO && o2 instanceof PolicyStoreDTO) {
            PolicyStoreDTO dto1 = (PolicyStoreDTO) o1;
            PolicyStoreDTO dto2 = (PolicyStoreDTO) o2;
            if (dto1.getPolicyOrder() > dto2.getPolicyOrder()) {
                return -1;
            } else if (dto1.getPolicyOrder() == dto2.getPolicyOrder()) {
                return 0;
            } else {
                return 1;
            }
        } else if (o1 instanceof PolicyDTO && o2 instanceof PolicyDTO) {
            PolicyDTO dto1 = (PolicyDTO) o1;
            PolicyDTO dto2 = (PolicyDTO) o2;
            if (dto1.getPolicyOrder() > dto2.getPolicyOrder()) {
                return -1;
            } else if (dto1.getPolicyOrder() == dto2.getPolicyOrder()) {
                return 0;
            } else {
                return 1;
            }
        } else {
            throw new ClassCastException("PolicyOrderComparator only works for PolicyDTO and PolicyStoreDTO types");
        }
    }
}
