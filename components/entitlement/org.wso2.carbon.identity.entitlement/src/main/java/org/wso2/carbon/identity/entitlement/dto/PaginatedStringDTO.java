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
package org.wso2.carbon.identity.entitlement.dto;

import java.util.Arrays;

/**
 *
 */
public class PaginatedStringDTO {

    private String[] statusHolders = new String[0];

    private int numberOfPages;

    public String[] getStatusHolders() {
        return Arrays.copyOf(statusHolders, statusHolders.length);
    }

    public void setStatusHolders(String[] statusHolders) {
        this.statusHolders = Arrays.copyOf(statusHolders, statusHolders.length);
    }

    public int getNumberOfPages() {
        return numberOfPages;
    }

    public void setNumberOfPages(int numberOfPages) {
        this.numberOfPages = numberOfPages;
    }
}
