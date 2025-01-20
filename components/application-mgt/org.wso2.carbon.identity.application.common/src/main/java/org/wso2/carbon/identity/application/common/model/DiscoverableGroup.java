/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
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

package org.wso2.carbon.identity.application.common.model;

import org.apache.axiom.om.OMElement;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The list of groups through which the application can be discovered in my account.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "DiscoverableGroup")
public class DiscoverableGroup implements Serializable {

    private static final long serialVersionUID = -3030000000000000000L;
    private static final String USER_STORE = "UserStore";
    private static final String GROUPS = "Groups";
    private static final String GROUP = "Group";

    @XmlElement(name = USER_STORE)
    private String userStore;

    @XmlElementWrapper(name = GROUPS)
    @XmlElement(name = GROUP)
    private GroupBasicInfo[] groups;

    /**
     * Creates an instance of the DiscoverableGroup class by parsing an OMElement.
     *
     * @param discoverableGroupOM The OMElement used to parse and build the DiscoverableGroup object.
     * @return A new DiscoverableGroup object populated with data from the OMElement.
     */
    public static DiscoverableGroup build(OMElement discoverableGroupOM) {

        DiscoverableGroup discoverableGroup = new DiscoverableGroup();
        Iterator<?> iter = discoverableGroupOM.getChildElements();
        List<GroupBasicInfo> groupList = new ArrayList<>();

        while (iter.hasNext()) {
            OMElement element = (OMElement) iter.next();
            String elementName = element.getLocalName();

            if (USER_STORE.equals(elementName) && StringUtils.isNotBlank(element.getText())) {
                discoverableGroup.setUserStore(element.getText());
            } else if (GROUPS.equals(elementName)) {
                Iterator<?> groupIter = element.getChildElements();
                while (groupIter.hasNext()) {
                    OMElement groupElement = (OMElement) groupIter.next();
                    GroupBasicInfo groupBasicInfo = GroupBasicInfo.build(groupElement);
                    if (groupBasicInfo != null) {
                        groupList.add(groupBasicInfo);
                    }
                }
            }
        }

        if (discoverableGroup.getUserStore() == null || groupList.isEmpty()) {
            return null;
        }

        discoverableGroup.setGroups(groupList.toArray(new GroupBasicInfo[0]));
        return discoverableGroup;
    }

    /**
     * Get the list of discoverable group basic info for the current user store.
     *
     * @return The list of group basic info.
     */
    public GroupBasicInfo[] getGroups() {

        return groups;
    }

    /**
     * Set the list of discoverable group basic info for the current user store.
     *
     * @param groups The list of group basic info.
     */
    public void setGroups(GroupBasicInfo[] groups) {

        this.groups = groups;
    }

    /**
     * Get the user store name.
     *
     * @return The user store name.
     */
    public String getUserStore() {

        return userStore;
    }

    /**
     * Set the user store name.
     *
     * @param userStore The user store name.
     */
    public void setUserStore(String userStore) {

        this.userStore = userStore;
    }
}
