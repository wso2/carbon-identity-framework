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
 * The {@code SearchSubjectsRequest} class is a model class for a search subject request. This class is used to
 * represent a search request to search subjects which can perform the given action in given resource. This follows the
 * AuthZEN Resource Search API format.
 */
public class SearchSubjectsRequest {

    private AuthorizationSubject subject;
    private AuthorizationAction action;
    private AuthorizationResource resource;
    private Map<String, Object> context;
    // The token to retrieve the next page of results.
    private String page;

    /**
     * Constructs a SearchSubjectsRequest object with the subject type, action, and resource.
     *
     * @param subject The type of the subject objects that should be searched.
     * @param action The action that should be performed on the resource.
     * @param resource The resource on which the action should be performed.
     */
    public SearchSubjectsRequest(AuthorizationSubject subject, AuthorizationAction action,
                                 AuthorizationResource resource) {

        this.subject = subject;
        this.action = action;
        this.resource = resource;
    }

    /**
     * Sets the context for the search request.
     *
     * @param context The context for the search request.
     */
    public void setContext(Map<String, Object> context) {

        this.context = context;
    }

    /**
     * Sets the page token for the search request.
     *
     * @param page The page token for the search request.
     */
    public void setPage(String page) {

        this.page = page;
    }

    /**
     * Returns the subject type for the search request.
     *
     * @return The subject type for the search request.
     */
    public AuthorizationSubject getSubject() {

        return subject;
    }

    /**
     * Returns the action for the search request.
     *
     * @return The action for the search request.
     */
    public AuthorizationAction getAction() {

        return action;
    }

    /**
     * Returns the resource for the search request.
     *
     * @return The resource for the search request.
     */
    public AuthorizationResource getResource() {

        return resource;
    }

    /**
     * Returns the context for the search request.
     *
     * @return The context for the search request.
     */
    public Map<String, Object> getContext() {

        return context;
    }

    /**
     * Returns the page token for the search request.
     *
     * @return The page token for the search request.
     */
    public String getPage() {

        return page;
    }
}
