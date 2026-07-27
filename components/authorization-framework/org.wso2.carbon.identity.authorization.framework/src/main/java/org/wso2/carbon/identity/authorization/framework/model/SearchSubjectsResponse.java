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
 * The {@code SearchSubjectsResponse} class is a model class for a search subjects response returned in a search
 * subjects request from an authorization engine. This follows the Subject Search API format from AuthZEN working
 * group. The returned results are stored as a list of {@link AuthorizationSubject} objects.
 */
public class SearchSubjectsResponse {

    private ArrayList<AuthorizationSubject> results;
    // The token to retrieve the next page of results.
    private String page;

    public SearchSubjectsResponse(ArrayList<AuthorizationSubject> results) {

        this.results = results;
    }

    public ArrayList<AuthorizationSubject> getResults() {

        return results;
    }

    public String getPage() {

        return page;
    }

    public void setPage(String page) {

        this.page = page;
    }
}
