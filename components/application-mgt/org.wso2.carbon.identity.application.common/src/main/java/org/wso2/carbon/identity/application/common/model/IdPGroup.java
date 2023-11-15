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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.databinding.annotation.IgnoreNullElement;

import java.io.Serializable;
import java.util.Iterator;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * IdP Group model class.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "IdPGroup")
public class IdPGroup implements Serializable {

    private static final long serialVersionUID = -2580928206017374295L;
    private static final String IDP_GROUP = "IdpGroup";
    private static final String IDP_GROUP_NAME = "IdpGroupName";

    @XmlElement(name = IDP_GROUP)
    private String idpGroupName = null;
    @IgnoreNullElement
    @XmlTransient
    @JsonIgnore
    private String idpGroupId = null;
    @IgnoreNullElement
    @XmlTransient
    @JsonIgnore
    private String idpId = null;

    public static IdPGroup build(OMElement idpGroupOM) {

        IdPGroup idpGroup = new IdPGroup();
        Iterator<?> iterator = idpGroupOM.getChildElements();
        while (iterator.hasNext()) {
            OMElement omElement = (OMElement) iterator.next();
            if (omElement.getLocalName().equals(IDP_GROUP_NAME)) {
                idpGroup.setIdpGroupName(omElement.getText());
            }
        }
        return idpGroup;
    }

    /**
     * Get idp group name.
     *
     * @return idp Group Name.
     */
    public String getIdpGroupName() {

        return idpGroupName;
    }

    /**
     * Set idp group name.
     *
     * @param idpGroupName idp group name.
     */
    public void setIdpGroupName(String idpGroupName) {

        this.idpGroupName = idpGroupName;
    }

    /**
     * Get idp group id.
     *
     * @return idp Group Id.
     */
    public String getIdpGroupId() {

        return idpGroupId;
    }

    /**
     * Set idp group id.
     *
     * @param idpGroupId idp group id.
     */
    public void setIdpGroupId(String idpGroupId) {

        this.idpGroupId = idpGroupId;
    }

    /**
     * Get idp id.
     *
     * @return idp Id.
     */
    public String getIdpId() {

        return idpId;
    }

    /**
     * Set idp id.
     *
     * @param idpId idp id.
     */
    public void setIdpId(String idpId) {

        this.idpId = idpId;
    }
}
