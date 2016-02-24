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

import org.wso2.carbon.identity.entitlement.dto.StatusHolder;

import java.io.Serializable;
import java.util.Comparator;

/**
 *
 */
public class StatusHolderComparator implements Serializable, Comparator {

    private static final long serialVersionUID = -6675867912216533133L;

    @Override
    public int compare(Object o1, Object o2) {

        StatusHolder dto1 = (StatusHolder) o1;
        StatusHolder dto2 = (StatusHolder) o2;
        long time1 = 0;
        long time2 = 0;
        try {
            time1 = Long.parseLong(dto1.getTimeInstance());
            time2 = Long.parseLong(dto2.getTimeInstance());
        } catch (Exception e) {
            // if time stamp is missing there can be null pointer
            // ignore
        }
        if (time1 > time2) {
            return -1;
        } else if (time1 == time2) {
            return 0;
        } else {
            return 1;
        }
    }
}
