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

import javax.xml.bind.annotation.XmlElement;

/**
 * Representation of an ExternalizedConsentPage Config.
 */
public class ExternalizedConsentPageConfig implements Serializable {

    private static final long serialVersionUID = 928301275168169633L;

    private static final String ENABLED_ELEM = "Enabled";
    private static final String URL_ELEM = "ConsentPageUrl";

    @XmlElement(name = ENABLED_ELEM)
    private boolean enabled;

    @XmlElement(name = URL_ELEM)
    private String consentPageUrl;

    public boolean isEnabled() {

        return enabled;
    }

    public void setEnabled(boolean enabled) {

        this.enabled = enabled;
    }

    public String getConsentPageUrl() {

        return consentPageUrl;
    }

    public void setConsentPageUrl(String consentPageUrl) {

        this.consentPageUrl = consentPageUrl;
    }

    /**
     * Returns a ExternalizedConsentPageConfig instance populated from the given OMElement
     * The OMElement is of the form below
     * <ExternalizedConsentPageConfiguration>
     * <Enabled></Enabled>
     * <ConsentPageUrl></ConsentPageUrl>
     * </ExternalizedConsentPageConfiguration>
     *
     * @param externalizedConsentPageConfigOM OMElement to populate externalizedConsentPageConfig
     * @return populated ExternalizedConsentPageConfig instance
     */
    public static ExternalizedConsentPageConfig build(OMElement externalizedConsentPageConfigOM) {

        ExternalizedConsentPageConfig externalizedConsentPageConfig = new ExternalizedConsentPageConfig();

        if (externalizedConsentPageConfigOM == null) {
            return externalizedConsentPageConfig;
        }

        Iterator<?> iterator = externalizedConsentPageConfigOM.getChildElements();
        while (iterator.hasNext()) {
            OMElement omElement = (OMElement) iterator.next();
            if (ENABLED_ELEM.equals(omElement.getLocalName())) {
                externalizedConsentPageConfig.setEnabled(Boolean.parseBoolean(omElement.getText()));
            } else if (URL_ELEM.equals(omElement.getLocalName())) {
                externalizedConsentPageConfig.setConsentPageUrl(omElement.getText());
            }
        }

        return externalizedConsentPageConfig;
    }

}
