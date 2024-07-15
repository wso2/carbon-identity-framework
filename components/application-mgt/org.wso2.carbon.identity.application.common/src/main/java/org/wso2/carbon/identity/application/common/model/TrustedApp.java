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

import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.PlatformType;

import java.io.Serializable;

/**
 * Platform specific trusted app configuration of an application.
 */
public class TrustedApp implements Serializable {

    private static final long serialVersionUID = -753653473009612027L;

    private PlatformType platformType;
    private String appIdentifier;
    private String[] thumbprints;

    /**
     * This attribute specifies whether this application is configured to share passkey credentials.
     */
    private boolean isFIDOTrusted;

    /**
     * Get the platform type of the trusted app.
     *
     * @return Platform type.
     */
    public PlatformType getPlatformType() {

        return platformType;
    }

    /**
     * Set the platform type of the trusted app.
     *
     * @param platformType Platform type.
     */
    public void setPlatformType(PlatformType platformType) {

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
    public String[] getThumbprints() {

        return thumbprints;
    }

    /**
     * Set the thumbprints of the trusted app.
     *
     * @param thumbprints Thumbprints.
     */
    public void setThumbprints(String[] thumbprints) {

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
}
