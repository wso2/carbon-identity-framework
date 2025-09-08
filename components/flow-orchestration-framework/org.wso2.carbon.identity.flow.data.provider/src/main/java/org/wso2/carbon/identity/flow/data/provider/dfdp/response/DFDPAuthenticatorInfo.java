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

package org.wso2.carbon.identity.flow.data.provider.dfdp.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * DFDP Authenticator Info DTO.
 * Part 7: Response Generation - Information about the authenticator used in the test.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DFDPAuthenticatorInfo {

    @JsonProperty("authenticatorName")
    private String authenticatorName;

    @JsonProperty("authenticatorType")
    private String authenticatorType;

    @JsonProperty("authenticatorClass")
    private String authenticatorClass;

    @JsonProperty("identityProviderName")
    private String identityProviderName;

    @JsonProperty("identityProviderType")
    private String identityProviderType;

    @JsonProperty("protocol")
    private String protocol;

    @JsonProperty("endpointUrl")
    private String endpointUrl;

    @JsonProperty("claimDialect")
    private String claimDialect;

    @JsonProperty("configurationProperties")
    private Map<String, String> configurationProperties;

    @JsonProperty("supportedClaims")
    private java.util.List<String> supportedClaims;

    @JsonProperty("authenticationStatus")
    private String authenticationStatus;

    @JsonProperty("authenticationTime")
    private long authenticationTimeMs;

    @JsonProperty("responseReceived")
    private boolean responseReceived;

    @JsonProperty("claimsExtracted")
    private boolean claimsExtracted;

    @JsonProperty("errorDetails")
    private String errorDetails;

    // Getters and setters

    public String getAuthenticatorName() {
        return authenticatorName;
    }

    public void setAuthenticatorName(String authenticatorName) {
        this.authenticatorName = authenticatorName;
    }

    public String getAuthenticatorType() {
        return authenticatorType;
    }

    public void setAuthenticatorType(String authenticatorType) {
        this.authenticatorType = authenticatorType;
    }

    public String getAuthenticatorClass() {
        return authenticatorClass;
    }

    public void setAuthenticatorClass(String authenticatorClass) {
        this.authenticatorClass = authenticatorClass;
    }

    public String getIdentityProviderName() {
        return identityProviderName;
    }

    public void setIdentityProviderName(String identityProviderName) {
        this.identityProviderName = identityProviderName;
    }

    public String getIdentityProviderType() {
        return identityProviderType;
    }

    public void setIdentityProviderType(String identityProviderType) {
        this.identityProviderType = identityProviderType;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    public String getClaimDialect() {
        return claimDialect;
    }

    public void setClaimDialect(String claimDialect) {
        this.claimDialect = claimDialect;
    }

    public Map<String, String> getConfigurationProperties() {
        return configurationProperties;
    }

    public void setConfigurationProperties(Map<String, String> configurationProperties) {
        this.configurationProperties = configurationProperties;
    }

    public java.util.List<String> getSupportedClaims() {
        return supportedClaims;
    }

    public void setSupportedClaims(java.util.List<String> supportedClaims) {
        this.supportedClaims = supportedClaims;
    }

    public String getAuthenticationStatus() {
        return authenticationStatus;
    }

    public void setAuthenticationStatus(String authenticationStatus) {
        this.authenticationStatus = authenticationStatus;
    }

    public long getAuthenticationTimeMs() {
        return authenticationTimeMs;
    }

    public void setAuthenticationTimeMs(long authenticationTimeMs) {
        this.authenticationTimeMs = authenticationTimeMs;
    }

    public boolean isResponseReceived() {
        return responseReceived;
    }

    public void setResponseReceived(boolean responseReceived) {
        this.responseReceived = responseReceived;
    }

    public boolean isClaimsExtracted() {
        return claimsExtracted;
    }

    public void setClaimsExtracted(boolean claimsExtracted) {
        this.claimsExtracted = claimsExtracted;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }

    // Utility methods

    public double getAuthenticationTimeSeconds() {
        return authenticationTimeMs / 1000.0;
    }

    public boolean isAuthenticationSuccessful() {
        return "SUCCESS".equals(authenticationStatus);
    }

    public boolean hasErrors() {
        return errorDetails != null && !errorDetails.trim().isEmpty();
    }
}
