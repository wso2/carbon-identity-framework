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

package org.wso2.carbon.identity.mgt.endpoint.util.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.wso2.carbon.identity.mgt.endpoint.util.IdentityManagementEndpointUtil;
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.LocalClaim;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Client which retrieves claim data required by endpoints.
 */
public class ClaimRetrievalClient {

    private static final Log LOG = LogFactory.getLog(ClaimRetrievalClient.class);
    private static final String LOCAL_CLAIM_API_RELATIVE_PATH = "/api/server/v1/claim-dialects/local/claims";

    private final ObjectMapper objectMapper;

    /**
     * Constructor that initializes the ObjectMapper with proper configuration.
     */
    public ClaimRetrievalClient() {

        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    /**
     * Retrieves local claims for the given list of claim URIs.
     *
     * @param tenantDomain The tenant domain.
     * @param claimURIs    The list of claim URIs to retrieve.
     * @return A map of claim URIs to LocalClaim objects.
     * @throws ClaimRetrievalClientException If an error occurs during claim retrieval.
     */
    public Map<String, LocalClaim> getLocalClaimsByURIs(String tenantDomain, List<String> claimURIs)
            throws ClaimRetrievalClientException {

        if (claimURIs == null || claimURIs.isEmpty()) {
            LOG.debug("No claim URIs provided for retrieval.");
            return new HashMap<>();
        }

        try {
            // TODO: Update this endpoint URL to include claim URI filters once the Claim API adds support for
            // filtering by claim URIs.
            HttpGet request = new HttpGet(getClaimsEndpoint(tenantDomain));
            ClientUtils.setAuthorizationHeader(request);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Retrieving local claims for tenant: " + tenantDomain);
            }

            String responseString = IdentityManagementEndpointUtil.getHttpClientResponseString(request);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Successfully retrieved local claims for tenant: " + tenantDomain);
            }
            if (StringUtils.isEmpty(responseString)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Empty response received from claims API for tenant: " + tenantDomain);
                }
                return new HashMap<>();
            }

            return parseClaimsResponse(responseString, claimURIs);
        } catch (IOException e) {
            String msg = "Error while retrieving claims for tenant: " + tenantDomain;
            LOG.debug(msg, e);
            throw new ClaimRetrievalClientException(msg, e);
        }
    }

    /**
     * Parses the claims API response.
     *
     * @param responseString The JSON response from the claims API.
     * @param claimURIs      The list of claim URIs to filter by.
     * @return A map of claim URIs to LocalClaim objects.
     * @throws ClaimRetrievalClientException If JSON parsing fails.
     */
    private Map<String, LocalClaim> parseClaimsResponse(String responseString, List<String> claimURIs)
            throws ClaimRetrievalClientException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Parsing claims response.");
        }
        try {
            List<LocalClaim> allClaims = objectMapper.readValue(responseString, new TypeReference<List<LocalClaim>>() {});
            Map<String, LocalClaim> claimsMap = new HashMap<>();
            for (LocalClaim claim : allClaims) {
                if (claimURIs.contains(claim.getClaimURI())) {
                    claimsMap.put(claim.getClaimURI(), claim);
                }
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Retrieved " + claimsMap.size() + " claims out of " + claimURIs.size() + " requested.");
            }
            return claimsMap;
        } catch (Exception e) {
            String msg = "Error while parsing claims response.";
            LOG.debug(msg, e);
            throw new ClaimRetrievalClientException(msg, e);
        }
    }

    /**
     * Builds the claims API endpoint URL for the given tenant.
     *
     * @param tenantDomain The tenant domain.
     * @return The complete claims API endpoint URL.
     * @throws ClaimRetrievalClientException If URL building fails.
     */
    private String getClaimsEndpoint(String tenantDomain) throws ClaimRetrievalClientException {

        try {
            return IdentityManagementEndpointUtil.getBasePath(tenantDomain, LOCAL_CLAIM_API_RELATIVE_PATH);
        } catch (ApiException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error while building claims endpoint for tenant: " + tenantDomain);
            }
            throw new ClaimRetrievalClientException("Error while building url for: " + LOCAL_CLAIM_API_RELATIVE_PATH,
                    e);
        }
    }
}
