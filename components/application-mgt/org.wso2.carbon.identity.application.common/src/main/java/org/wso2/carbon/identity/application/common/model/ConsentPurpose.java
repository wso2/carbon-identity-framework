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
import java.util.Iterator;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class represents the ConsentPurpose model.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "ConsentPurpose")
public class ConsentPurpose implements Serializable {


    private static final long serialVersionUID = 1421773871658784071L;
    private static final Log log = LogFactory.getLog(ConsentPurpose.class);
    private static final String PURPOSE_ID_ELEM = "PurposeID";
    private static final String DISPLAY_ORDER_ELEM = "DisplayOrder";
    private static final int DEFAULT_DISPLAY_ORDER = 0;

    @XmlElement(name = "PurposeID")
    private int purposeId;

    @XmlElement(name = "DisplayOrder")
    private int displayOrder;

    public int getPurposeId() {

        return purposeId;
    }

    public void setPurposeId(int purposeId) {

        this.purposeId = purposeId;
    }

    public int getDisplayOrder() {

        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {

        this.displayOrder = displayOrder;
    }

    /**
     * Build ConsentPurpose from ConsentPurpose OM element.
     *
     * @param consentPurposeOM ConsentPurpose OM element.
     * @return ConsentPurpose object.
     */
    public static ConsentPurpose build(OMElement consentPurposeOM) throws IdentityApplicationManagementException {

        ConsentPurpose consentPurpose = new ConsentPurpose();
        if (consentPurposeOM == null) {
            return consentPurpose;
        }

        Iterator<?> children = consentPurposeOM.getChildElements();
        while (children.hasNext()) {
            OMElement member = (OMElement) children.next();
            if (PURPOSE_ID_ELEM.equals(member.getLocalName())) {
                try {
                    consentPurpose.setPurposeId(Integer.parseInt(member.getText()));
                } catch (NumberFormatException e) {
                    log.warn("PurposeID should be an Integer. Found: " + member.getText() + " instead.");
                    throw new IdentityApplicationManagementException("Invalid purpose ID: " + member.getText(), e);
                }
            } else {
                if (DISPLAY_ORDER_ELEM.equals(member.getLocalName())) {
                    try {
                        consentPurpose.setDisplayOrder(Integer.parseInt(member.getText()));
                    } catch (NumberFormatException e) {
                        log.warn("DisplayOrder should be an Integer. Found: " + member.getText() + " instead. Setting " +
                                 "default display order: " + DEFAULT_DISPLAY_ORDER);
                        consentPurpose.setDisplayOrder(DEFAULT_DISPLAY_ORDER);
                    }
                }
            }
        }
        return consentPurpose;
    }
}
