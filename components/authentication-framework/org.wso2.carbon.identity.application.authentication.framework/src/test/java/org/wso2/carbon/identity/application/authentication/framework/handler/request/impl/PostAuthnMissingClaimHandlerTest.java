/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.wso2.carbon.identity.application.authentication.framework.handler.request.impl;

import org.testng.annotations.Test;
import org.wso2.carbon.identity.claim.metadata.mgt.model.AttributeMapping;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;

/**
 * This is a test class for {@link PostAuthnMissingClaimHandler}.
 */
public class PostAuthnMissingClaimHandlerTest extends PostAuthnMissingClaimHandler {

    @SuppressWarnings("checkstyle:LocalVariableName")
    @Test(description = "This test case tests the related display names for mandatory missing claims are derived")
    public void testCorrectDisplayNamesDeriveForMissingClaims()
            throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {

        List<AttributeMapping> mappedAttributes = new ArrayList<>();

        Map<String, String> localClaimProperties = new HashMap<>();
        localClaimProperties.put("Description", "Local");
        localClaimProperties.put("DisplayName", "Local");

        Map<String, String> localityClaimProperties = new HashMap<>();
        localityClaimProperties.put("Description", "Locality");
        localityClaimProperties.put("DisplayName", "Locality");

        Map<String, String> secretKeyClaimProperties = new HashMap<>();
        secretKeyClaimProperties.put("Description", "Claim to store the secret key");
        secretKeyClaimProperties.put("DisplayName", "Secret Key");

        Map<String, String> countryClaimProperties = new HashMap<>();
        countryClaimProperties.put("Description", "Country");
        countryClaimProperties.put("DisplayName", "Country");

        Map<String, String> verifyEmailClaimProperties = new HashMap<>();
        verifyEmailClaimProperties.put("Description", "Temporary claim to invoke email verified feature");
        verifyEmailClaimProperties.put("DisplayName", "Verify Email");

        List<LocalClaim> localClaims = new ArrayList<>();
        LocalClaim localClaim = new LocalClaim("http://wso2.org/claims/local", mappedAttributes, localClaimProperties);
        LocalClaim localClaim2 = new
                LocalClaim("http://wso2.org/claims/locality", mappedAttributes, localityClaimProperties);
        LocalClaim localClaim3 = new
                LocalClaim("http://wso2.org/claims/identity/secretkey", mappedAttributes, secretKeyClaimProperties);
        LocalClaim localClaim4 = new
                LocalClaim("http://wso2.org/claims/country", mappedAttributes, countryClaimProperties);
        LocalClaim localClaim5 = new
                LocalClaim("http://wso2.org/claims/identity/verifyEmail", mappedAttributes, verifyEmailClaimProperties);
        localClaims.add(localClaim);
        localClaims.add(localClaim2);
        localClaims.add(localClaim3);
        localClaims.add(localClaim4);
        localClaims.add(localClaim5);

        Map<String, String> missingClaimMap = new HashMap<>();
        missingClaimMap.put("http://wso2.org/claims/local", "http://wso2.org/claims/local");
        missingClaimMap.put("http://wso2.org/claims/country", "http://wso2.org/claims/country");
        missingClaimMap.put("http://wso2.org/claims/locality", "http://wso2.org/claims/locality");

        String relatedDisplayNames = "http://wso2.org/claims/local|Local,http://wso2.org/claims/country|Country," +
                "http://wso2.org/claims/locality|Locality";

        Class<PostAuthnMissingClaimHandler> claimDisplay = PostAuthnMissingClaimHandler.class;
        Object obj = claimDisplay.newInstance();
        Method displayName = claimDisplay.
                getDeclaredMethod("getMissingClaimsDisplayNames", Map.class, List.class);
        displayName.setAccessible(true);
        String returnedDisplayNames = (String) displayName.invoke(obj, missingClaimMap, localClaims);
        assertEquals(returnedDisplayNames, relatedDisplayNames);
    }
}
