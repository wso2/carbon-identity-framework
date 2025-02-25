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
import java.util.Iterator;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Basic details of a group for discoverable group list.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Group")
public class GroupBasicInfo implements Serializable {

    private static final long serialVersionUID = -3030000000000000001L;
    private static final String ID = "ID";
    private static final String NAME = "Name";

    @XmlElement(name = ID)
    private String id;

    @XmlElement(name = NAME)
    private String name;

    /**
     * Creates an instance of the GroupBasicInfo class by parsing an OMElement.
     *
     * @param groupOM The OMElement used to parse and build the GroupBasicInfo object.
     * @return A new GroupBasicInfo object populated with data from the OMElement.
     */
    public static GroupBasicInfo build(OMElement groupOM) {

        GroupBasicInfo groupBasicInfo = new GroupBasicInfo();
        Iterator<?> iter = groupOM.getChildElements();

        while (iter.hasNext()) {
            OMElement element = (OMElement) iter.next();
            String elementName = element.getLocalName();

            if (ID.equals(elementName) && StringUtils.isNotBlank(element.getText())) {
                groupBasicInfo.setId(element.getText());
            } else if (NAME.equals(elementName) && StringUtils.isNotBlank(element.getText())) {
                groupBasicInfo.setName(element.getText());
            }
        }

        if (groupBasicInfo.getId() == null) {
            return null;
        }

        return groupBasicInfo;
    }

    /**
     * Get the group ID.
     *
     * @return Group ID.
     */
    public String getId() {

        return id;
    }

    /**
     * Set the group ID.
     *
     * @param id Group ID.
     */
    public void setId(String id) {

        this.id = id;
    }

    /**
     * Get the group name.
     *
     * @return Group name.
     */
    public String getName() {

        return name;
    }

    /**
     * Set the group name.
     *
     * @param name Group name.
     */
    public void setName(String name) {

        this.name = name;
    }
}
