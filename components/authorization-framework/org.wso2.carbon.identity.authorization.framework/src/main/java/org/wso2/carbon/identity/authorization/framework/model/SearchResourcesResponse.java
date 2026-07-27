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

package org.wso2.carbon.identity.authorization.framework.model;

import java.util.ArrayList;

/**
 * The {@code SearchResourcesResponse} class is a model class for a search resources response returned in a search
 * resources request from an authorization engine This follows the Resource Search API format from AuthZEN working
 * group. The returned results are stored as a list of {@link AuthorizationResource} objects.
 */
public class SearchResourcesResponse {

    private ArrayList<AuthorizationResource> results;
    // The page token for the next page of results.
    private String page;

    /**
     * Constructs a {@code SearchResourcesResponse} object with the search results.
     *
     * @param results The search results.
     */
    public SearchResourcesResponse(ArrayList<AuthorizationResource> results) {

        this.results = results;
    }

    /**
     * Returns the search results.
     *
     * @return The search results.
     */
    public ArrayList<AuthorizationResource> getResults() {

        return this.results;
    }
}
