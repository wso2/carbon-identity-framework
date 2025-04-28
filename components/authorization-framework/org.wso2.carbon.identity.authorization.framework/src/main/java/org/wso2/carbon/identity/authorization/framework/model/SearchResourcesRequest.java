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
 * The {@code SearchResourcesRequest} class is a model class for a search resource request. This class is used to
 * represent a search request to search resources which the given subject can perform the given action. This follows the
 * AuthZEN Resource Search API format.
 */
public class SearchResourcesRequest {

    private AuthorizationResource resource;
    private AuthorizationAction action;
    private AuthorizationSubject subject;
    private Map<String, Object> context;
    // The token to retrieve the next page of results.
    private String page;

    /**
     * Constructs a {@code SearchResourcesRequest} object with the resource type, action, and subject.
     *
     * @param resource The type of the resource object that the search result should return.
     * @param action The action that needs to be performed.
     * @param subject The subject that needs to perform the action.
     */
    public SearchResourcesRequest(AuthorizationResource resource, AuthorizationAction action,
                                  AuthorizationSubject subject) {

        this.resource = resource;
        this.action = action;
        this.subject = subject;
    }

    /**
     * Sets the context of the search objects request.
     *
     * @param context The context of the search objects request.
     */
    public void setContext(Map<String, Object> context) {

        this.context = context;
    }

    /**
     * Sets the token to retrieve the next page of results.
     *
     * @param page The token to retrieve the next page of results.
     */
    public void setPage(String page) {

        this.page = page;
    }

    /**
     * Returns the type of the object that the search result should be.
     *
     * @return The type of the object that the search result should be.
     */
    public AuthorizationResource getResource() {

        return resource;
    }

    /**
     * Returns the action that needs to be performed.
     *
     * @return The action that needs to be performed.
     */
    public AuthorizationAction getAction() {

        return action;
    }

    /**
     * Returns the type of the object to be searched.
     *
     * @return The type of the object to be searched.
     */
    public AuthorizationSubject getSubject() {

        return subject;
    }

    /**
     * Returns the context of the search objects request.
     *
     * @return The context of the search objects request.
     */
    public Map<String, Object> getContext() {

        return context;
    }

    /**
     * Returns the token to retrieve the next page of results.
     *
     * @return The token to retrieve the next page of results.
     */
    public String getPage() {

        return page;
    }
}
