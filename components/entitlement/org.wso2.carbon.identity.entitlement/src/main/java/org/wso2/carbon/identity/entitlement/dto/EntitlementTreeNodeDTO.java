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
import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public class EntitlementTreeNodeDTO {

    /**
     * Node name
     */
    private String name;

    /**
     * children of the Node
     */
    private EntitlementTreeNodeDTO[] childNodes = new EntitlementTreeNodeDTO[]{};

    public EntitlementTreeNodeDTO(String name) {
        this.name = name;
    }

    public EntitlementTreeNodeDTO() {

    }

    public String getName() {
        return name;
    }

    public EntitlementTreeNodeDTO[] getChildNodes() {
        return Arrays.copyOf(childNodes, childNodes.length);
    }

    public void setChildNodes(EntitlementTreeNodeDTO[] childNodes) {
        this.childNodes = Arrays.copyOf(childNodes, childNodes.length);
    }

    public void addChildNode(EntitlementTreeNodeDTO node) {
        Set<EntitlementTreeNodeDTO> valueNodes = new HashSet<EntitlementTreeNodeDTO>(Arrays.asList(this.childNodes));
        valueNodes.add(node);
        this.childNodes = valueNodes.toArray(new EntitlementTreeNodeDTO[valueNodes.size()]);
    }
}
