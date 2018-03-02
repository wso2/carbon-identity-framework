/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.consent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the claim metadata required for a particular service provider
 */
public class ConsentClaimsData implements Serializable {

    List<ClaimMetaData> mandatoryClaims = new ArrayList<>();
    List<ClaimMetaData> requestedClaims = new ArrayList<>();
    List<ClaimMetaData> claimsWithConsent = new ArrayList<>();

    public List<ClaimMetaData> getMandatoryClaims() {
        return mandatoryClaims;
    }

    public void setMandatoryClaims(
            List<ClaimMetaData> mandatoryClaims) {
        this.mandatoryClaims = mandatoryClaims;
    }

    public List<ClaimMetaData> getRequestedClaims() {
        return requestedClaims;
    }

    public void setRequestedClaims(
            List<ClaimMetaData> requestedClaims) {
        this.requestedClaims = requestedClaims;
    }

    public List<ClaimMetaData> getClaimsWithConsent() {
        return claimsWithConsent;
    }

    public void setClaimsWithConsent(List<ClaimMetaData> claimsWithConsent) {
        this.claimsWithConsent = claimsWithConsent;
    }
}
