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

import java.util.Map;

/**
 * The {@code SearchActionsRequest} class is a model class for a search action request. This class is used to
 * represent a search request to search actions which the given subject can perform on given resource. This follows the
 * AuthZEN Action Search API format.
 */
public class SearchActionsRequest {

    private AuthorizationSubject subject;
    private AuthorizationResource resource;
    private Map<String, Object> context;
    // The token to retrieve the next page of results.
    private String page;

    /**
     * Constructor for SearchActionsRequest.
     *
     * @param subject  The subject to search actions for.
     * @param resource The resource to search actions for.
     */
    public SearchActionsRequest(AuthorizationSubject subject, AuthorizationResource resource) {

        this.subject = subject;
        this.resource = resource;
    }

    public AuthorizationSubject getSubject() {

        return subject;
    }

    public AuthorizationResource getResource() {

        return resource;
    }

    public Map<String, Object> getContext() {

        return context;
    }

    public void setContext(Map<String, Object> context) {

        this.context = context;
    }

    public String getPage() {

        return page;
    }

    public void setPage(String page) {

        this.page = page;
    }
}
