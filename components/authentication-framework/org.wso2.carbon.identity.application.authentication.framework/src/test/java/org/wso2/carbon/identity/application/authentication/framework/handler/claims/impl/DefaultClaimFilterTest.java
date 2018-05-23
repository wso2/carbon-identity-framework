/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.handler.claims.impl;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class DefaultClaimFilterTest {

    private String personIDLocalClaimUri = "http://wso2.org/claims/personIdentifier";
    private String personIDRemoteClaimUri = "http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier";
    private String legalPersonIdRemoteClaimUri = "http://eidas.europa.eu/attributes/legalperson/LegalPersonIdentifier";
    private String legalPersonIdLocalClaimUri = "http://wso2.org/claims/legalPersonIdentifier";
    List<ClaimMapping> spClaimMappings = new ArrayList<>();
    List<ClaimMapping> requestedClaimsInRequest = new ArrayList<>();

    @BeforeMethod
    public void setRequestedClaimsInRequest() throws Exception {

        ClaimMapping claimMapping = ClaimMapping.build(personIDLocalClaimUri, personIDRemoteClaimUri, null,
                true, true);
        requestedClaimsInRequest.add(claimMapping);
    }

    @BeforeMethod
    public void setSpClaimMappings() throws Exception {

        ClaimMapping claimMapping = ClaimMapping.build(legalPersonIdLocalClaimUri, legalPersonIdRemoteClaimUri, null,
                true, true);
        spClaimMappings.add(claimMapping);
    }

    @Test
    public void testFilterRequestedClaimsInRequest() throws Exception {

        DefaultClaimFilter defaultClaimFilter = new DefaultClaimFilter();
        List<ClaimMapping> filteredClaims = defaultClaimFilter.filterRequestedClaims(null,
                requestedClaimsInRequest);
        assertEquals(filteredClaims.size(), 1, "Error in filtering requested claims in request.");
        assertEquals(filteredClaims.get(0).getLocalClaim().getClaimUri(), personIDLocalClaimUri,
                "Error in filtering requested claims in request.");
        assertEquals(filteredClaims.get(0).getRemoteClaim().getClaimUri(), personIDRemoteClaimUri,
                "Error in filtering requested claims in request.");
    }

    @Test
    public void testFilterSpClaimMappings() throws Exception {

        DefaultClaimFilter defaultClaimFilter = new DefaultClaimFilter();
        List<ClaimMapping> filteredClaims = defaultClaimFilter.filterRequestedClaims(spClaimMappings,
                null);
        assertEquals(filteredClaims.size(), 1, "Error in filtering requested claims in sp config.");
        assertEquals(filteredClaims.get(0).getLocalClaim().getClaimUri(), legalPersonIdLocalClaimUri,
                "Error in filtering requested claims in sp config.");
        assertEquals(filteredClaims.get(0).getRemoteClaim().getClaimUri(), legalPersonIdRemoteClaimUri,
                "Error in filtering requested claims in sp config.");
    }

    @Test
    public void testFilterRequestedClaimsFromSpConfigAndRequest() throws Exception {

        DefaultClaimFilter defaultClaimFilter = new DefaultClaimFilter();
        List<ClaimMapping> filteredClaims;

        filteredClaims = defaultClaimFilter.filterRequestedClaims(spClaimMappings,
                requestedClaimsInRequest);
        assertEquals(filteredClaims.size(), 0, "Error in filtering requested claims in sp config and " +
                "request.");

        ClaimMapping claimMapping = ClaimMapping.build(personIDLocalClaimUri, personIDRemoteClaimUri, null,
                true, true);
        spClaimMappings.add(claimMapping);

        filteredClaims = defaultClaimFilter.filterRequestedClaims(spClaimMappings,
                requestedClaimsInRequest);
        assertEquals(filteredClaims.size(), 1, "Error in filtering requested claims in sp config and " +
                "request.");
        assertEquals(filteredClaims.get(0).getLocalClaim().getClaimUri(), personIDLocalClaimUri,
                "Error in filtering requested claims in sp config and request.");
        assertEquals(filteredClaims.get(0).getRemoteClaim().getClaimUri(), personIDRemoteClaimUri,
                "Error in filtering requested claims in sp config and request.");
    }
}
