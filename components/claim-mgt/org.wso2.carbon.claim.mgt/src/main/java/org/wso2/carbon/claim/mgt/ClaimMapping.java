/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.claim.mgt;

public class ClaimMapping {

    private String nonCarbonDialect;

    private String nonCarbonClaimURI;

    private String carbonClaimURI;

    public ClaimMapping(String nonCarbonDialect, String nonCarbonClaimURI, String carbonClaimURI) {
        this.nonCarbonDialect = nonCarbonDialect;
        this.nonCarbonClaimURI = nonCarbonClaimURI;
        this.carbonClaimURI = carbonClaimURI;
    }

    public String getNonCarbonClaimURI() {
        return nonCarbonClaimURI;
    }

    public void setNonCarbonClaimURI(String nonCarbonClaimURI) {
        this.nonCarbonClaimURI = nonCarbonClaimURI;
    }

    public String getNonCarbonDialect() {
        return nonCarbonDialect;
    }

    public void setNonCarbonDialect(String nonCarbonDialect) {
        this.nonCarbonDialect = nonCarbonDialect;
    }

    public String getCarbonClaimURI() {
        return carbonClaimURI;
    }

    public void setCarbonClaimURI(String carbonClaimURI) {
        this.carbonClaimURI = carbonClaimURI;
    }

}