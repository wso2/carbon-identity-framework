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
 * Model representing a VC Credential Configuration.
 * Aligns with the updated Admin API schema.
 */
public class VCCredentialConfiguration {

    // Server-generated identifier (resource id)
    private String id;

    // Stable identifier used in OID4VCI issuer metadata
    private String identifier;

    // Credential configuration identifier published via issuer metadata.
    private String displayName;

    private String scope;
    private String format;

    // Single signing algorithm supported for this configuration.
    private String signingAlgorithm;

    private String type;

    private List<String> claims = new ArrayList<>();

    private Integer expiresIn;

    // Backend-generated random UUID for credential offer. Null if no offer has been generated.
    private String offerId;

    // Cursor key for pagination support.
    private Integer cursorKey;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getSigningAlgorithm() {
        return signingAlgorithm;
    }

    public void setSigningAlgorithm(String signingAlgorithm) {
        this.signingAlgorithm = signingAlgorithm;
    }

    public List<String> getClaims() {
        return claims;
    }

    public void setClaims(List<String> claims) {
        this.claims = claims;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Integer expiresIn) {
        this.expiresIn = expiresIn;
    }

    /**
     * Get the offer ID.
     *
     * @return Offer ID or null if no offer exists.
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
     * Get the cursor key for pagination.
     *
     * @return Cursor key.
     */
    public Integer getCursorKey() {
        return cursorKey;
    }

    /**
     * Set the cursor key for pagination.
     *
     * @param cursorKey Cursor key.
     */
    public void setCursorKey(Integer cursorKey) {
        this.cursorKey = cursorKey;
    }
}
