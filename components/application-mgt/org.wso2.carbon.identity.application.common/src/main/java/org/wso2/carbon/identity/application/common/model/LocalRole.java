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
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.io.Serializable;
import java.util.Iterator;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "LocalRole")
public class LocalRole implements Serializable {

    private static final long serialVersionUID = -1986741675509417413L;

    /**
     * The mapped role name of the IdP role at this local IdP end
     */
    @XmlElement(name = "LocalRoleName")
    private String localRoleName;

    /**
     * The user store domain ID of the mapped role name at this local IdP end
     */
    @XmlElement(name = "UserStoreId")
    private String userStoreId;

    public LocalRole() {

    }

    public LocalRole(String userStoreId, String localRoleName) {
        this.userStoreId = userStoreId;
        this.localRoleName = localRoleName;
    }

    public LocalRole(String combinedRoleName) {
        this.userStoreId = IdentityUtil.extractDomainFromName(combinedRoleName);
        this.localRoleName = UserCoreUtil.removeDomainFromName(combinedRoleName);
    }

    /*
     * <LocalRole> <LocalRoleName></LocalRoleName> <UserStoreId></UserStoreId> </LocalRole>
     */
    public static LocalRole build(OMElement localRoleOM) {
        LocalRole localRole = new LocalRole();

        Iterator<?> iter = localRoleOM.getChildElements();

        while (iter.hasNext()) {
            OMElement element = (OMElement) (iter.next());
            String elementName = element.getLocalName();

            if ("LocalRoleName".equals(elementName)) {
                localRole.setLocalRoleName(element.getText());
            } else if ("UserStoreId".equals(elementName)) {
                localRole.setUserStoreId(element.getText());
            }

        }

        return localRole;
    }

    public String getLocalRoleName() {
        return localRoleName;
    }

    public void setLocalRoleName(String localRoleName) {
        this.localRoleName = localRoleName;
    }

    public String getUserStoreId() {
        return userStoreId;
    }

    public void setUserStoreId(String userStoreId) {
        this.userStoreId = userStoreId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LocalRole localRole1 = (LocalRole) o;

        if (!localRoleName.equals(localRole1.localRoleName))
            return false;
        if (userStoreId != null ? !userStoreId.equals(localRole1.userStoreId)
                : localRole1.userStoreId != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = localRoleName.hashCode();
        result = 31 * result + (userStoreId != null ? userStoreId.hashCode() : 0);
        return result;
    }
}
