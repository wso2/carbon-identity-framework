/*
 * Copyright (c) 2010 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.user.mgt.common;

import java.util.Arrays;

public class UIPermissionNode {

    private String resourcePath;
    private String displayName;
    private boolean isSelected;
    private UIPermissionNode[] nodeList = new UIPermissionNode[0];

    public UIPermissionNode() {

    }

    public UIPermissionNode(String resourcePath, String displayName) {
        this.resourcePath = resourcePath;
        this.displayName = displayName;
    }

    public UIPermissionNode(String resourcePath, String displayName, boolean isSelected) {
        super();
        this.resourcePath = resourcePath;
        this.displayName = displayName;
        this.isSelected = isSelected;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public UIPermissionNode[] getNodeList() {
        if (nodeList != null) {
            return nodeList.clone();
        }
        return new UIPermissionNode[0];
    }

    public void setNodeList(UIPermissionNode[] nodeList) {
        this.nodeList = Arrays.copyOf(nodeList, nodeList.length);
    }
}
