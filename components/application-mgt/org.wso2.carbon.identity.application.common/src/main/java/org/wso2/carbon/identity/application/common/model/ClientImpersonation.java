/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.identity.application.common.model;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.databinding.annotation.IgnoreNullElement;

import java.io.Serializable;
import java.util.Iterator;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class represents the metadata related to client impersonation. It is used for
 * serializing and deserializing data to/from XML format.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "ClientImpersonationMetaData")
public class ClientImpersonation implements Serializable {

    private static final long serialVersionUID = 1995041000019950518L;
    private static final String IS_IMPERSONATION_EMAIL_NOTIFICATION_ENABLED = "IsImpersonationEmailNotificationEnabled";
    private static final String IS_IMPERSONATION_ENABLED = "IsImpersonationEnabled";

    @IgnoreNullElement
    @XmlElement(name = IS_IMPERSONATION_ENABLED)
    private boolean isImpersonationEnabled;

    // Field to store whether email notification for impersonation is enabled.
    @IgnoreNullElement
    @XmlElement(name = IS_IMPERSONATION_EMAIL_NOTIFICATION_ENABLED)
    private boolean isImpersonationEmailNotificationEnabled;

    /**
     * Creates an instance of the ClientImpersonationMetaData class by parsing an OMElement.
     *
     * @param metaDataOM The OMElement to parse and build the ClientImpersonationMetaData object from.
     * @return A new ClientImpersonationMetaData object populated with data from the OMElement.
     */
    public static ClientImpersonation build(OMElement metaDataOM) {
        ClientImpersonation metaData = new ClientImpersonation();

        Iterator<?> iter = metaDataOM.getChildElements();

        while (iter.hasNext()) {
            OMElement element = (OMElement) (iter.next());
            String elementName = element.getLocalName();

            if (IS_IMPERSONATION_ENABLED.equals(elementName)) {
                boolean isImpersonationEnabled = element.getText() != null && Boolean.parseBoolean(element.getText());
                metaData.setImpersonationEnabled(isImpersonationEnabled);
            } else if (IS_IMPERSONATION_EMAIL_NOTIFICATION_ENABLED.equals(elementName)) {
                boolean isImpersonationEmailNotificationEnabled = element.getText() != null
                        && Boolean.parseBoolean(element.getText());
                metaData.setImpersonationEmailNotificationEnabled(isImpersonationEmailNotificationEnabled);
            }
        }
        return metaData;
    }


    /**
     * Get the value indicating whether email notification for impersonation is enabled.
     *
     * @return True if attestation is enabled, otherwise false.
     */
    public boolean isImpersonationEmailNotificationEnabled() {

        return isImpersonationEmailNotificationEnabled;
    }

    /**
     * Set the value indicating whether email notification for impersonation is enabled.
     *
     * @param impersonationEmailNotificationEnabled True to enable attestation, false to disable it.
     */
    public void setImpersonationEmailNotificationEnabled(boolean impersonationEmailNotificationEnabled) {

        isImpersonationEmailNotificationEnabled = impersonationEmailNotificationEnabled;
    }


    public boolean isImpersonationEnabled() {

        return isImpersonationEnabled;
    }

    public void setImpersonationEnabled(boolean impersonationEnabled) {

        isImpersonationEnabled = impersonationEnabled;
    }
}
