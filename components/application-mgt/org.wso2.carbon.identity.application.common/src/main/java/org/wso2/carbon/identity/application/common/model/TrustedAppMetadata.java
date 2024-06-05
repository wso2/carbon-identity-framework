/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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
import org.apache.axis2.databinding.annotation.IgnoreNullElement;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Iterator;

/**
 * Trusted app metadata of an application.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "TrustedAppMetadata")
public class TrustedAppMetadata implements Serializable {

    //TODO why is serialVersionUID needed?
    private static final long serialVersionUID = -753653473009612026L;
    private static final String ANDROID_PACKAGE_NAME = "AndroidPackageName";
    private static final String ANDROID_THUMBPRINTS = "AndroidThumbprints";
    private static final String APPLE_APP_ID = "AppleAppId";
    private static final String IS_FIDO_TRUSTED = "IsFidoTrusted";
    private static final String IS_TWA_ENABLED = "IsTWAEnabled";

    @IgnoreNullElement
    @XmlElement(name = ANDROID_PACKAGE_NAME)
    private String androidPackageName;

    //TODO handle thumbprints differently
    @IgnoreNullElement
    @XmlElement(name = ANDROID_THUMBPRINTS)
    private String androidThumbprints;

    @IgnoreNullElement
    @XmlElement(name = APPLE_APP_ID)
    private String appleAppId;

    @IgnoreNullElement
    @XmlElement(name = IS_FIDO_TRUSTED)
    private boolean isFidoTrusted;

    @IgnoreNullElement
    @XmlElement(name = IS_TWA_ENABLED)
    private boolean isTWAEnabled;

    /**
     * Creates an instance of the TrustedAppMetadata class by parsing an OMElement.
     *
     * @param trustedAppMetadataOM The OMElement to parse and build the TrustedAppMetadata object from.
     * @return A new TrustedAppMetadata object populated with data from the OMElement.
     */
    public static TrustedAppMetadata build(OMElement trustedAppMetadataOM) {
        TrustedAppMetadata trustedAppMetadata = new TrustedAppMetadata();

        Iterator<?> iter = trustedAppMetadataOM.getChildElements();

        while (iter.hasNext()) {
            OMElement element = (OMElement) (iter.next());
            String elementName = element.getLocalName();

            if (ANDROID_PACKAGE_NAME.equals(elementName)) {
                trustedAppMetadata.setAndroidPackageName(element.getText());
            }
            if (ANDROID_THUMBPRINTS.equals(elementName)) {
                trustedAppMetadata.setAndroidThumbprints(element.getText());
            }
            if (APPLE_APP_ID.equals(elementName)) {
                trustedAppMetadata.setAppleAppId(element.getText());
            }
            if (IS_FIDO_TRUSTED.equals(elementName)) {
                trustedAppMetadata.setIsFidoTrusted(Boolean.parseBoolean(element.getText()));
            }
            if (IS_TWA_ENABLED.equals(elementName)) {
                trustedAppMetadata.setIsTWAEnabled(Boolean.parseBoolean(element.getText()));
            }
        }
        return trustedAppMetadata;
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
     * Get the Android thumbprints.
     *
     * @return The Android thumbprints.
     */
    public String getAndroidThumbprints() {

        return androidThumbprints;
    }

    /**
     * Set the Android thumbprints.
     *
     * @param androidThumbprints The Android thumbprints to set.
     */
    public void setAndroidThumbprints(String androidThumbprints) {

        this.androidThumbprints = androidThumbprints;
    }

    /**
     * Get the Apple App ID.
     *
     * @return The Apple App ID.
     */
    public String getAppleAppId() {

        return appleAppId;
    }

    /**
     * Set the Apple App ID.
     *
     * @param appleAppId The Apple App ID to set.
     */
    public void setAppleAppId(String appleAppId) {

        this.appleAppId = appleAppId;
    }

    /**
     * Get the FIDO trusted status.
     *
     * @return The FIDO trusted status.
     */
    public boolean getIsFidoTrusted() {

        return isFidoTrusted;
    }

    /**
     * Set the FIDO trusted status.
     *
     * @param isFidoTrusted The FIDO trusted status to set.
     */
    public void setIsFidoTrusted(boolean isFidoTrusted) {

        this.isFidoTrusted = isFidoTrusted;
    }

    /**
     * Get the TWA enabled status.
     *
     * @return The TWA enabled status.
     */
    public boolean getIsTWAEnabled() {

        return isTWAEnabled;
    }

    /**
     * Set the TWA enabled status.
     *
     * @param isTWAEnabled The TWA enabled status to set.
     */
    public void setIsTWAEnabled(boolean isTWAEnabled) {

        this.isTWAEnabled = isTWAEnabled;
    }
}
