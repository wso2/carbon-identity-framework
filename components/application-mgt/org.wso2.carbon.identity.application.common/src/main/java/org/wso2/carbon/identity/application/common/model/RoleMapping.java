/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Role mapping of the Identity Provider.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "RoleMapping")
public class RoleMapping implements Serializable {

    private static final long serialVersionUID = 4838992846277456900L;

    @XmlElement(name = "localRole")
    private LocalRole localRole = null;

    @XmlElement(name = "remoteRole")
    private String remoteRole = null;

    public RoleMapping() {

    }

    /**
     * @param localRole
     * @param remoteRole
     */
    public RoleMapping(LocalRole localRole, String remoteRole) {
        this.localRole = localRole;
        this.remoteRole = remoteRole;
    }

    /*
     * <RoleMapping> <localRole></localRole> <remoteRole></remoteRole> </RoleMapping>
     */
    public static RoleMapping build(OMElement roleMappingOM) {
        RoleMapping roleMapping = new RoleMapping();

        Iterator<?> iter = roleMappingOM.getChildElements();

        while (iter.hasNext()) {
            OMElement element = (OMElement) (iter.next());
            String elementName = element.getLocalName();

            if ("localRole".equals(elementName)) {
                roleMapping.setLocalRole(LocalRole.build(element));
            }
            if ("remoteRole".equals(elementName)) {
                roleMapping.setRemoteRole(element.getText());
            }
        }

        return roleMapping;
    }

    public String getRemoteRole() {
        return remoteRole;
    }

    public void setRemoteRole(String remoteRole) {
        this.remoteRole = remoteRole;
    }

    public LocalRole getLocalRole() {
        return localRole;
    }

    public void setLocalRole(LocalRole localRole) {
        this.localRole = localRole;
    }

}
