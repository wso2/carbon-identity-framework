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
import org.apache.commons.collections.CollectionUtils;

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
 * Associated v2 roles for the application.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "AssociatedRolesConfig")
public class AssociatedRolesConfig implements Serializable {

    private static final long serialVersionUID = 497647508006862448L;
    private static final String ALLOWED_AUDIENCE = "AllowedAudience";
    private static final String ASSOCIATED_ROLES = "AssociatedRoles";
    private static final String ASSOCIATED_ROLE = "AssociatedRole";

    @XmlElement(name = ALLOWED_AUDIENCE)
    private String allowedAudience;

    @XmlElementWrapper(name = ASSOCIATED_ROLES)
    @XmlElement(name = ASSOCIATED_ROLE)
    private RoleV2[] roles = new RoleV2[0];

    /*
    <AssociatedRolesConfig>
        <AllowedAudience>...</AllowedAudience>
        <AssociatedRoles>
            <AssociatedRole>...</AssociatedRole>
            <AssociatedRole>...</AssociatedRole>
            <!-- More AssociatedRole elements for each item in the list -->
        </AssociatedRoles>
    </AssociatedRolesConfig>
     */
    public static AssociatedRolesConfig build(OMElement associatedRolesConfigOM) {

        AssociatedRolesConfig associatedRolesConfig = new AssociatedRolesConfig();

        Iterator<?> iter = associatedRolesConfigOM.getChildElements();

        while (iter.hasNext()) {

            OMElement element = (OMElement) (iter.next());
            String elementName = element.getLocalName();

            if (ALLOWED_AUDIENCE.equals(elementName)) {
                associatedRolesConfig.setAllowedAudience(element.getText());
            } else if (ASSOCIATED_ROLES.equals(elementName)) {
                Iterator<?> roleIter = element.getChildElements();
                List<RoleV2> rolesArrList = new ArrayList<>();
                if (roleIter != null) {
                    while (roleIter.hasNext()) {
                        OMElement roleElement = (OMElement) (roleIter.next());
                        RoleV2 roleV2 = RoleV2.build(roleElement);
                        if (roleV2 != null) {
                            rolesArrList.add(roleV2);
                        }
                    }
                }
                if (CollectionUtils.isNotEmpty(rolesArrList)) {
                    associatedRolesConfig.setRoles(rolesArrList.toArray(new RoleV2[0]));
                }
            }
        }
        return associatedRolesConfig;
    }

    public String getAllowedAudience() {

        return allowedAudience;
    }

    public void setAllowedAudience(String allowedAudience) {

        this.allowedAudience = allowedAudience;
    }

    public RoleV2[] getRoles() {

        return roles;
    }

    public void setRoles(RoleV2[] roles) {

        this.roles = roles;
    }
}
