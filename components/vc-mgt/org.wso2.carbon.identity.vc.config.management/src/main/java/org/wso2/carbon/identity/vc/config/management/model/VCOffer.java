/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.vc.config.management.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Model representing a VC Offer configuration.
 */
public class VCOffer {

    // Server-generated identifier (resource id).
    private String offerId;

    // Display name of the offer.
    private String displayName;

    // List of credential configuration IDs associated with this offer.
    private List<String> credentialConfigurationIds = new ArrayList<>();

    /**
     * Get the offer ID.
     *
     * @return Offer ID.
     */
    public String getOfferId() {

        return offerId;
    }

    /**
     * Set the offer ID.
     *
     * @param offerId Offer ID.
     */
    public void setOfferId(String offerId) {

        this.offerId = offerId;
    }

    /**
     * Get the display name.
     *
     * @return Display name.
     */
    public String getDisplayName() {

        return displayName;
    }

    /**
     * Set the display name.
     *
     * @param displayName Display name.
     */
    public void setDisplayName(String displayName) {

        this.displayName = displayName;
    }

    /**
     * Get the credential configuration IDs.
     *
     * @return List of credential configuration IDs.
     */
    public List<String> getCredentialConfigurationIds() {

        return credentialConfigurationIds;
    }

    /**
     * Set the credential configuration IDs.
     *
     * @param credentialConfigurationIds List of credential configuration IDs.
     */
    public void setCredentialConfigurationIds(List<String> credentialConfigurationIds) {

        this.credentialConfigurationIds = credentialConfigurationIds;
    }
}

