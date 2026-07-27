/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.configuration.mgt.core.model;

import java.util.Objects;

/**
 * A model class to Identify a Configuration Resource uniquely by its type and name.
 */
public class ResourceIdentifier {

    private String resourceType;
    private String resourceName;

    public ResourceIdentifier(String resourceType, String resourceName) {

        this.resourceType = resourceType;
        this.resourceName = resourceName;
    }

    public String getResourceType() {

        return resourceType;
    }

    public String getResourceName() {

        return resourceName;
    }

    @Override
    public boolean equals(Object o) {

        if (!(o instanceof ResourceIdentifier that)) {
            return false;
        }
        return Objects.equals(resourceType, that.resourceType) &&
                Objects.equals(resourceName, that.resourceName);
    }

    @Override
    public int hashCode() {

        return Objects.hash(resourceType, resourceName);
    }
}
