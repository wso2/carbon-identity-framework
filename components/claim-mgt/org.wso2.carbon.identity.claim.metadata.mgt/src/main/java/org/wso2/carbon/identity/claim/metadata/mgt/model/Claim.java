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

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the simplest form of the metadata of a claim.
 */
public class Claim {
    private String claimDialectURI;
    private String claimURI;
    private Map<String, String> claimProperties;

    public Claim(String claimDialectURI, String claimURI) {
        this.claimDialectURI = claimDialectURI;
        this.claimURI = claimURI;
        this.claimProperties = new HashMap<>();
    }

    public Claim(String claimDialectURI, String claimURI, Map<String, String> claimProperties) {
        this.claimDialectURI = claimDialectURI;
        this.claimURI = claimURI;

        if (claimProperties == null) {
            claimProperties = new HashMap<>();
        }

        this.claimProperties = claimProperties;
    }

    public String getClaimDialectURI() {
        return claimDialectURI;
    }

    public String getClaimURI() {
        return claimURI;
    }

    public Map<String, String> getClaimProperties() {
        return claimProperties;
    }

    public String getClaimProperty(String propertyName) {
        if (this.getClaimProperties().containsKey(propertyName)) {
            this.getClaimProperties().get(propertyName);
        }
        return null;
    }

    public void setClaimProperties(Map<String, String> claimProperties) {
        if (claimProperties == null) {
            claimProperties = new HashMap<>();
        }
        this.claimProperties = claimProperties;
    }

    public void setClaimProperty(String propertyName, String propertyValue) {
        this.getClaimProperties().put(propertyName, propertyValue);
    }
}
