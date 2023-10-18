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
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.Iterator;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class represents the federated association configuration.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "FederatedAssociationConfig")
public class FederatedAssociationConfig implements Serializable {

    private static final long serialVersionUID = 3689722862659524700L;
    private static final String IS_ENABLED = "IsEnabled";
    private static final String LOOKUP_ATTRIBUTES = "LookupAttributes";

    @XmlElement(name = "IsEnabled")
    private boolean isEnabled = false;

    @XmlElement(name = "LookupAttributes")
    private String[] lookupAttributes = new String[]{};

    public static FederatedAssociationConfig build(OMElement federatedAssociationConfigOM) {

        FederatedAssociationConfig federatedAssociationConfig = new FederatedAssociationConfig();

        if (federatedAssociationConfigOM == null) {
            return federatedAssociationConfig;
        }

        Iterator<?> iter = federatedAssociationConfigOM.getChildElements();

        while (iter.hasNext()) {
            OMElement element = (OMElement) (iter.next());
            String elementName = element.getLocalName();

            if (IS_ENABLED.equals(elementName)) {
                if (StringUtils.isNotBlank(element.getText())) {
                    federatedAssociationConfig.setEnabled(Boolean.parseBoolean(element.getText()));
                }
            } else if (LOOKUP_ATTRIBUTES.equals(elementName)) {
                String[] attributeList = StringUtils.split(element.getText(), ",");
                federatedAssociationConfig.setLookupAttributes(attributeList);
            }
        }

        return federatedAssociationConfig;
    }

    public void setEnabled(boolean enabled) {

        isEnabled = enabled;
    }

    public String[] getLookupAttributes() {

        return lookupAttributes;
    }

    public boolean isEnabled() {

        return isEnabled;
    }

    public void setLookupAttributes(String[] lookupAttributes) {

        this.lookupAttributes = lookupAttributes;
    }

}
