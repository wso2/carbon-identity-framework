/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * This class represents the ConsentConfig model.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "ConsentConfig")
public class ConsentConfig implements Serializable {

    private static final long serialVersionUID = -8949172355745509861L;
    private static final String ENABLED_ELEM = "Enabled";
    private static final String CONSENT_PURPOSE_CONFIGS_ELEM = "ConsentPurposeConfigs";

    @XmlElement(name = "ConsentPurposeConfigs")
    private ConsentPurposeConfigs consentPurposeConfigs;

    @XmlElement(name = "Enabled")
    private boolean enabled;

    public boolean isEnabled() {

        return enabled;
    }

    public void setEnabled(boolean enabled) {

        this.enabled = enabled;
    }

    public ConsentPurposeConfigs getConsentPurposeConfigs() {

        return consentPurposeConfigs;
    }

    public void setConsentPurposeConfigs(ConsentPurposeConfigs consentPurposeConfigs) {

        this.consentPurposeConfigs = consentPurposeConfigs;
    }

    /**
     * Build ConsentConfig from ConsentConfig OM element.
     *
     * @param consentConfigsOM ConsentConfig OM element.
     * @return ConsentConfig object.
     */
    public static ConsentConfig build(OMElement consentConfigsOM) {

        ConsentConfig consentConfig = new ConsentConfig();
        Iterator<?> iterator = consentConfigsOM.getChildElements();
        while (iterator.hasNext()) {
            OMElement element = (OMElement) (iterator.next());
            String elementName = element.getLocalName();
            if (ENABLED_ELEM.equals(elementName)) {
                if (element.getText() != null) {
                    consentConfig.setEnabled(Boolean.getBoolean(element.getText()));
                }
            } else {
                if (CONSENT_PURPOSE_CONFIGS_ELEM.equals(elementName)) {
                    consentConfig.setConsentPurposeConfigs(ConsentPurposeConfigs.build(element));
                }
            }
        }
        return consentConfig;
    }
}
