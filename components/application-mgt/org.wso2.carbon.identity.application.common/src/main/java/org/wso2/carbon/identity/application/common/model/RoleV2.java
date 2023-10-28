/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

import java.io.Serializable;
import java.util.Iterator;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Role V2 object.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "AssociatedRole")
public class RoleV2 implements Serializable {

    private static final long serialVersionUID = 497647508006862448L;
    private static final String ID = "Id";
    private static final String NAME = "Name";

    @XmlElement(name = ID)
    private String id;

    @XmlElement(name = NAME)
    private String name;

    public RoleV2() {

    }

    public RoleV2(String id) {

        this.id = id;
    }

    public static RoleV2 build(OMElement roleOM) {

        if (roleOM == null) {
            return null;
        }
        RoleV2 roleV2 = new RoleV2();
        Iterator<?> iter = roleOM.getChildElements();

        while (iter.hasNext()) {
            OMElement element = (OMElement) (iter.next());
            String elementName = element.getLocalName();

            if (ID.equals(elementName)) {
                roleV2.setId(element.getText());
            } else if (NAME.equals(elementName)) {
                roleV2.setName(element.getText());
            }
        }
        return roleV2;
    }

    public RoleV2(String id, String name) {

        this.id = id;
        this.name = name;
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RoleV2 roleV2 = (RoleV2) o;
        return Objects.equals(id, roleV2.id) && Objects.equals(name, roleV2.name);
    }

    @Override
    public int hashCode() {

        int result = super.hashCode();
        result = 31 * result + id.hashCode();
        return result;
    }

    @Override
    public String toString() {

        return id;
    }
}
