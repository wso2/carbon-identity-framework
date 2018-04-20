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

import java.io.Serializable;

/**
 * Represents the simplest form of the metadata of a claim dialect.
 */
public class ClaimDialect implements Serializable {

    private static final long serialVersionUID = -3907634389860801704L;

    private String claimDialectURI;

    public ClaimDialect(String claimDialectURI) {
        this.claimDialectURI = claimDialectURI;
    }

    public String getClaimDialectURI() {
        return claimDialectURI;
    }

}
