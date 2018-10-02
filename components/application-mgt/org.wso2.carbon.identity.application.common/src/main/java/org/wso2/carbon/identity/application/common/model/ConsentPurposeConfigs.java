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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;

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
 * This class represents the ConsentPurposeConfigs model.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "ConsentPurposeConfigs")
public class ConsentPurposeConfigs implements Serializable {


    private static final long serialVersionUID = -7182787626304846628L;
    private static final Log log = LogFactory.getLog(ConsentPurposeConfigs.class);
    private static final String CONSENT_PURPOSE_ELEM = "ConsentPurpose";

    @XmlElementWrapper(name="ConsentPurposeConfigs")
    @XmlElement(name = "ConsentPurpose")
    private ConsentPurpose[] consentPurpose;

    public ConsentPurpose[] getConsentPurpose() {

        return consentPurpose;
    }

    public void setConsentPurpose(ConsentPurpose[] consentPurpose) {

        this.consentPurpose = consentPurpose;
    }

    /**
     * Build ConsentPurposeConfigs from ConsentPurposeConfigs OM element.
     *
     * @param consentPurposeConfigsOM ConsentPurposeConfigs OM element.
     * @return ConsentPurposeConfigs object.
     */
    public static ConsentPurposeConfigs build(OMElement consentPurposeConfigsOM) {

        ConsentPurposeConfigs consentPurposeConfigs = new ConsentPurposeConfigs();

        if (consentPurposeConfigsOM == null) {
            return consentPurposeConfigs;
        }

        List<ConsentPurpose> consentPurposes = new ArrayList<>();
        Iterator<?> iterator = consentPurposeConfigsOM.getChildElements();
        while (iterator.hasNext()) {
            OMElement consentPurposeOM = (OMElement) iterator.next();
            if (CONSENT_PURPOSE_ELEM.equals(consentPurposeOM.getLocalName())) {
                ConsentPurpose consentPurpose;
                try {
                    consentPurpose = ConsentPurpose.build(consentPurposeOM);
                    if (consentPurpose != null) {
                        consentPurposes.add(consentPurpose);
                    }
                } catch (IdentityApplicationManagementException e) {
                    log.error("Error while parsing the ConsentPurpose config.", e);
                }
            }
        }
        consentPurposeConfigs.setConsentPurpose(consentPurposes.toArray(new ConsentPurpose[0]));
        return consentPurposeConfigs;
    }
}
