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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * IdP Group Configuration.
 * This will contain a set of IdP groups that can be provided by the federated IdP for a user.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "IdPGroupConfig")
public class IdPGroupConfig implements Serializable {

    private static final long serialVersionUID = -2580928206017374295L;
    private static final String IDP_GROUPS = "IdpGroups";
    private static final String IDP_GROUP = "IdpGroup";

    @XmlElementWrapper(name = IDP_GROUPS)
    @XmlElement(name = IDP_GROUP)
    private String[] idpGroups = new String[0];

    /*
     * <IdPGroupConfig> <IdpGroups> <IdpGroup>group1</IdpGroup> <IdpGroup>group2</IdpGroup>
     * </IdpGroups> </IdPGroupConfig>
     */
    public static IdPGroupConfig build(OMElement idPGroupConfigOM) {

        IdPGroupConfig idPGroupConfig = new IdPGroupConfig();

        if (idPGroupConfigOM == null) {
            return idPGroupConfig;
        }

        Iterator<?> iter = idPGroupConfigOM.getChildElements();

        while (iter.hasNext()) {

            OMElement element = (OMElement) (iter.next());
            String elementName = element.getLocalName();

            if (IDP_GROUPS.equals(elementName)) {
                Iterator<?> idpGroupsIter = element.getChildElements();
                List<String> idpGroups = new ArrayList<>();
                while (idpGroupsIter.hasNext()) {
                    OMElement idpGroupElement = (OMElement) (idpGroupsIter.next());
                    idpGroups.add(idpGroupElement.getText());
                }
                idPGroupConfig.setIdpGroups(idpGroups.toArray(new String[0]));
            }
        }
        return idPGroupConfig;
    }

    /**
     * Get available IdP Groups of a federated IdP.
     *
     * @return available IdP Groups of a federated IdP.
     */
    public String[] getIdpGroups() {

        return idpGroups;
    }

    /**
     * Set available IdP Groups of a federated IdP.
     *
     * @param idpGroups IdP Groups.
     */
    public void setIdpGroups(String[] idpGroups) {

        this.idpGroups = idpGroups;
    }
}
