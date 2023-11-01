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

package org.wso2.carbon.identity.client.attestation.mgt.model;

/**
 * Represents metadata related to Apple app attestation.
 */
public class AppleAttestationMetaData {

    // The App ID associated with the Apple app.
    private String appId;

    /**
     * Get the App ID associated with the Apple app.
     *
     * @return The App ID.
     */
    public String getAppId() {

        return appId;
    }

    /**
     * Set the App ID associated with the Apple app.
     *
     * @param appId The App ID to set.
     */
    public void setAppId(String appId) {

        this.appId = appId;
    }
}
