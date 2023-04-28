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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Application role mapping type of application for an IdP.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "ApplicationRoleMappingConfig")
public class AppRoleMappingConfig implements Serializable {

    private static final long serialVersionUID = -1870576785207238676L;
    private static final String IDP_NAME = "IdPName";
    private static final String USE_APP_ROLE_MAPPINGS = "UseAppRoleMappings";

    @XmlElement(name = IDP_NAME)
    private String idPName;

    @XmlElement(name = USE_APP_ROLE_MAPPINGS)
    private boolean useAppRoleMappings;

    /**
     * Build ApplicationRoleMappingConfig from OMElement which indicates whether the IdP will use mappings from
     * idp-roles to application roles for the application.
     *
     * @param applicationRoleMappingConfigOM OMElement.
     * @return the application role mapping config.
     */
    public static AppRoleMappingConfig build(OMElement applicationRoleMappingConfigOM) {

        AppRoleMappingConfig applicationRoleMappingType = new AppRoleMappingConfig();
        Iterator<?> iterator = applicationRoleMappingConfigOM.getChildElements();

        while (iterator.hasNext()) {
            OMElement omElement = (OMElement) iterator.next();
            if (IDP_NAME.equals(omElement.getLocalName())) {
                applicationRoleMappingType.setIdPName(omElement.getText());
            } else if (USE_APP_ROLE_MAPPINGS.equals(omElement.getLocalName())) {
                applicationRoleMappingType.setUseAppRoleMappings(Boolean.parseBoolean(omElement.getText()));
            }
        }
        return applicationRoleMappingType;
    }

    /**
     * Return the name of the federated IdP used for application role mapping.
     *
     * @return IdP Name.
     */
    public String getIdPName() {

        return idPName;
    }

    /**
     * Set the name of the federated IdP used for application role mapping.
     *
     * @param idPName IdP Name.
     */
    public void setIdPName(String idPName) {

        this.idPName = idPName;
    }

    /**
     * Return whether the IdP will use mappings from idp-roles to application roles for the application.
     *
     * @return use app role mapping or not for idp.
     */
    public boolean isUseAppRoleMappings() {

        return useAppRoleMappings;
    }

    /**
     * Return whether the IdP will use mappings from idp-roles to application roles for the application.
     *
     * @param useAppRoleMappings use app role mapping or not for idp.
     */
    public void setUseAppRoleMappings(boolean useAppRoleMappings) {

        this.useAppRoleMappings = useAppRoleMappings;
    }
}
