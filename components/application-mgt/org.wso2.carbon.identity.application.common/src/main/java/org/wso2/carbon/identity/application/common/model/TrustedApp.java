/*
 * Copyright (c) 2024, WSO2 Inc. (http://www.wso2.com).
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

import org.apache.axis2.databinding.annotation.IgnoreNullElement;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Platform specific trusted app configuration of an application.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "TrustedApp")
public class TrustedApp implements Serializable {

    private static final long serialVersionUID = -753653473009612027L;
    private static final String PLATFORM_TYPE = "PlatformType";
    private static final String APP_IDENTIFIER = "AppIdentifier";
    private static final String THUMBPRINTS = "Thumbprints";
    private static final String IS_FIDO_TRUSTED = "IsFidoTrusted";
    private static final String IS_TWA_ENABLED = "IsTWAEnabled";

    @IgnoreNullElement
    @XmlElement(name = PLATFORM_TYPE)
    private String platformType;

    @IgnoreNullElement
    @XmlElement(name = APP_IDENTIFIER)
    private String appIdentifier;

    @IgnoreNullElement
    @XmlElement(name = THUMBPRINTS)
    private String thumbprints;

    @IgnoreNullElement
    @XmlElement(name = IS_FIDO_TRUSTED)
    private boolean isFIDOTrusted;

    @IgnoreNullElement
    @XmlElement(name = IS_TWA_ENABLED)
    private boolean isTWAEnabled;

    /**
     * Get the platform type of the trusted app.
     *
     * @return Platform type.
     */
    public String getPlatformType() {

        return platformType;
    }

    /**
     * Set the platform type of the trusted app.
     *
     * @param platformType Platform type.
     */
    public void setPlatformType(String platformType) {

        this.platformType = platformType;
    }

    /**
     * Get the app identifier of the trusted app.
     *
     * @return App identifier.
     */
    public String getAppIdentifier() {

        return appIdentifier;
    }

    /**
     * Set the app identifier of the trusted app.
     *
     * @param appIdentifier App identifier.
     */
    public void setAppIdentifier(String appIdentifier) {

        this.appIdentifier = appIdentifier;
    }

    /**
     * Get the thumbprints of the trusted app.
     *
     * @return Thumbprints.
     */
    public String getThumbprints() {

        return thumbprints;
    }

    /**
     * Set the thumbprints of the trusted app.
     *
     * @param thumbprints Thumbprints.
     */
    public void setThumbprints(String thumbprints) {

        this.thumbprints = thumbprints;
    }

    /**
     * Check whether the trusted app is FIDO trusted.
     *
     * @return Is FIDO trusted.
     */
    public boolean getIsFIDOTrusted() {

        return isFIDOTrusted;
    }

    /**
     * Set whether the trusted app is FIDO trusted.
     *
     * @param isFIDOTrusted Is FIDO trusted.
     */
    public void setIsFIDOTrusted(boolean isFIDOTrusted) {

        this.isFIDOTrusted = isFIDOTrusted;
    }

    /**
     * Check whether TWA is enabled for the trusted app.
     *
     * @return Is TWA enabled.
     */
    public boolean getIsTWAEnabled() {

        return isTWAEnabled;
    }

    /**
     * Set whether TWA is enabled for the trusted app.
     *
     * @param isTWAEnabled Is TWA enabled.
     */
    public void setIsTWAEnabled(boolean isTWAEnabled) {

        this.isTWAEnabled = isTWAEnabled;
    }
}
