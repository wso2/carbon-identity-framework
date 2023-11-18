/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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
 * This class represents the metadata related to client attestation. It is used for
 * serializing and deserializing data to/from XML format.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "ClientAttestationMetaData")
public class ClientAttestationMetaData implements Serializable {

    private static final long serialVersionUID = 1995051800019950410L;
    private static final String IS_ATTESTATION_ENABLED = "IsAttestationEnabled";
    private static final String ANDROID_PACKAGE_NAME = "AndroidPackageName";
    private static final String ANDROID_ATTESTATION_SERVICE_CREDENTIALS = "AndroidAttestationServiceCredentials";
    private static final String APPLE_APP_ID = "AppleAppId";

    // Field to store whether attestation is enabled.
    @IgnoreNullElement
    @XmlElement(name = IS_ATTESTATION_ENABLED)
    private boolean isAttestationEnabled;

    // Field to store the Android package name.
    @IgnoreNullElement
    @XmlElement(name = ANDROID_PACKAGE_NAME)
    private String androidPackageName;

    // Field to store Android attestation service credentials.
    @IgnoreNullElement
    @XmlElement(name = ANDROID_ATTESTATION_SERVICE_CREDENTIALS)
    private String androidAttestationServiceCredentials;

    // Field to store Apple app ID.
    @IgnoreNullElement
    @XmlElement(name = APPLE_APP_ID)
    private String appleAppId;

    /**
     * Creates an instance of the ClientAttestationMetaData class by parsing an OMElement.
     *
     * @param metaDataOM The OMElement to parse and build the ClientAttestationMetaData object from.
     * @return A new ClientAttestationMetaData object populated with data from the OMElement.
     */
    public static ClientAttestationMetaData build(OMElement metaDataOM) {
        ClientAttestationMetaData metaData = new ClientAttestationMetaData();

        Iterator<?> iter = metaDataOM.getChildElements();

        while (iter.hasNext()) {
            OMElement element = (OMElement) (iter.next());
            String elementName = element.getLocalName();

            if (IS_ATTESTATION_ENABLED.equals(elementName)) {
                boolean isAttestationEnabled = Boolean.parseBoolean(element.getText());
                metaData.setAttestationEnabled(isAttestationEnabled);
            }
            if (ANDROID_PACKAGE_NAME.equals(elementName)) {
                metaData.setAndroidPackageName(element.getText());
            }
            if (ANDROID_ATTESTATION_SERVICE_CREDENTIALS.equals(elementName)) {
                metaData.setAndroidAttestationServiceCredentials(element.getText());
            }
            if (APPLE_APP_ID.equals(elementName)) {
                metaData.setAppleAppId(element.getText());
            }
        }
        return metaData;
    }


    /**
     * Get the value indicating whether attestation is enabled.
     *
     * @return True if attestation is enabled, otherwise false.
     */
    public boolean isAttestationEnabled() {

        return isAttestationEnabled;
    }

    /**
     * Set the value indicating whether attestation is enabled.
     *
     * @param attestationEnabled True to enable attestation, false to disable it.
     */
    public void setAttestationEnabled(boolean attestationEnabled) {

        isAttestationEnabled = attestationEnabled;
    }

    /**
     * Get the Android package name.
     *
     * @return The Android package name.
     */
    public String getAndroidPackageName() {

        return androidPackageName;
    }

    /**
     * Set the Android package name.
     *
     * @param androidPackageName The Android package name to set.
     */
    public void setAndroidPackageName(String androidPackageName) {

        this.androidPackageName = androidPackageName;
    }

    /**
     * Get the Android attestation service credentials.
     *
     * @return The Android attestation service credentials.
     */
    public String getAndroidAttestationServiceCredentials() {

        return androidAttestationServiceCredentials;
    }

    /**
     * Set the Android attestation service credentials.
     *
     * @param androidAttestationServiceCredentials The credentials to set.
     */
    public void setAndroidAttestationServiceCredentials(String androidAttestationServiceCredentials) {

        this.androidAttestationServiceCredentials = androidAttestationServiceCredentials;
    }

    /**
     * Gets the Apple App ID.
     *
     * @return The Apple App ID.
     */
    public String getAppleAppId() {

        return appleAppId;
    }

    /**
     * Sets the Apple App ID.
     *
     * @param appleAppId The Apple App ID to set.
     */
    public void setAppleAppId(String appleAppId) {

        this.appleAppId = appleAppId;
    }
}
