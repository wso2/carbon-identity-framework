/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.claim.metadata.mgt.model;

import java.util.Map;

/**
 * Represents the metadata of a external claim.
 */
public class ExternalClaim extends Claim {
    private String mappedLocalClaim;

    public ExternalClaim(String claimDialectURI, String claimURI, String mappedLocalClaimURI) {
        super(claimDialectURI, claimURI);
        this.mappedLocalClaim = mappedLocalClaimURI;
    }

    public ExternalClaim(String claimDialectURI, String claimURI, String mappedLocalClaimURI,
            Map<String, String> claimProperties) {
        super(claimDialectURI, claimURI, claimProperties);
        this.mappedLocalClaim = mappedLocalClaimURI;
    }

    public String getMappedLocalClaim() {
        return mappedLocalClaim;
    }

    public void setMappedLocalClaim(String mappedLocalClaim) {
        this.mappedLocalClaim = mappedLocalClaim;
    }
}
