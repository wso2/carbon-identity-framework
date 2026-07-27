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
import java.util.Objects;

/**
 * The {@code AuthorizationResource} class is a model class for a resource object in an Authorization request.
 * The resource object is used to represent the resource that the user is trying to access.
 */
public class AuthorizationResource {

    private String resourceType;
    private String resourceId;
    private Map<String, Object> properties;

    /**
     * Constructs an {@code AuthorizationResource} object with the resource object type and the resource object ID.
     *
     * @param resourceType The type of the resource object.
     * @param resourceId   The ID of the resource object.
     */
    public AuthorizationResource(String resourceType, String resourceId) {
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    /**
     * Constructs an {@code AuthorizationResource} object with the resource object type for search requests.
     *
     * @param resourceType The type of the resource object.
     */
    public AuthorizationResource(String resourceType) {

        this.resourceType = resourceType;
    }

    /**
     * Sets the properties of the resource object.
     *
     * @param properties The properties of the resource object.
     */
    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    /**
     * Returns the type of the resource object.
     *
     * @return The type of the resource object.
     */
    public String getResourceType() {
        return resourceType;
    }

    /**
     * Returns the ID of the resource object.
     *
     * @return The ID of the resource object.
     */
    public String getResourceId() {
        return resourceId;
    }

    /**
     * Returns the properties of the resource object.
     *
     * @return The properties of the resource object.
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AuthorizationResource that = (AuthorizationResource) obj;
        if (properties == null && that.properties == null) {
            return true;
        }
        if (properties == null || that.properties == null) {
            return false;
        }
        return resourceType.equals(that.resourceType) &&
                resourceId.equals(that.resourceId) &&
                properties.equals(that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceType, resourceId, properties);
    }
}
