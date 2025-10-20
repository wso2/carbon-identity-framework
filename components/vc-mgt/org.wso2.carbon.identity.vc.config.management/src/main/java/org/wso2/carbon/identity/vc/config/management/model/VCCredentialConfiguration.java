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
import java.util.Objects;

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
    private String configurationId;

    private String scope;
    private String format = "jwt_vc_json";

    // Single signing algorithm supported for this configuration.
    private String credentialSigningAlgValuesSupported;

    private String credentialType;

    private CredentialMetadata credentialMetadata = new CredentialMetadata();

    private List<ClaimMapping> claimMappings = new ArrayList<>();

    private Integer expiryInSeconds;

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

    public String getCredentialSigningAlgValuesSupported() {
        return credentialSigningAlgValuesSupported;
    }

    public void setCredentialSigningAlgValuesSupported(String credentialSigningAlgValuesSupported) {
        this.credentialSigningAlgValuesSupported = credentialSigningAlgValuesSupported;
    }

    public List<ClaimMapping> getClaimMappings() {
        return claimMappings;
    }

    public void setClaimMappings(List<ClaimMapping> claimMappings) {
        this.claimMappings = claimMappings;
    }

    public String getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(String configurationId) {
        this.configurationId = configurationId;
    }

    public String getCredentialType() {
        return credentialType;
    }

    public void setCredentialType(String credentialType) {
        this.credentialType = credentialType;
    }

    public CredentialMetadata getCredentialMetadata() {
        return credentialMetadata;
    }

    public void setCredentialMetadata(CredentialMetadata credentialMetadata) {
        this.credentialMetadata = credentialMetadata;
    }

    public Integer getExpiryInSeconds() {
        return expiryInSeconds;
    }

    public void setExpiryInSeconds(Integer expiryInSeconds) {
        this.expiryInSeconds = expiryInSeconds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        VCCredentialConfiguration that = (VCCredentialConfiguration) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Credential metadata payload.
     */
    public static class CredentialMetadata {

        private String display;

        public String getDisplay() {
            return display;
        }

        public void setDisplay(String display) {
            this.display = display;
        }
    }
}
