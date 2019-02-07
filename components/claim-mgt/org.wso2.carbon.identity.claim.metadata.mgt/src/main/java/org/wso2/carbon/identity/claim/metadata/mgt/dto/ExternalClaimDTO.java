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

package org.wso2.carbon.identity.claim.metadata.mgt.dto;

/**
 * Data transfer representation for metadata of a external claim.
 */
public class ExternalClaimDTO {
    private String externalClaimDialectURI;
    private String externalClaimURI;
    private String mappedLocalClaimURI;
    private ClaimPropertyDTO[] claimProperties;

    public String getExternalClaimDialectURI() {
        return externalClaimDialectURI;
    }

    public void setExternalClaimDialectURI(String externalClaimDialectURI) {
        this.externalClaimDialectURI = externalClaimDialectURI;
    }

    public String getExternalClaimURI() {
        return externalClaimURI;
    }

    public void setExternalClaimURI(String externalClaimURI) {
        this.externalClaimURI = externalClaimURI;
    }

    public String getMappedLocalClaimURI() {
        return mappedLocalClaimURI;
    }

    public void setMappedLocalClaimURI(String mappedLocalClaimURI) {
        this.mappedLocalClaimURI = mappedLocalClaimURI;
    }

    public ClaimPropertyDTO[] getClaimProperties() {
        return claimProperties;
    }

    public void setClaimProperties(ClaimPropertyDTO[] claimProperties) {
        this.claimProperties = claimProperties;
    }
}
