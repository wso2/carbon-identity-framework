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
 * The {@code AuthorizationAction} class represents an Action object in an Evaluation request. This action refers to a
 * specific action that needs to be evaluated by the Authorization Engine.
 */
public class AuthorizationAction {

    private String action;
    private Map<String, Object> properties;

    /**
     * Constructs an {@code AuthorizationAction} object with the action.
     *
     * @param action The action to be evaluated.
     */
    public AuthorizationAction(String action) {
        this.action = action;
    }

    /**
     * Returns the action to be evaluated.
     * @return The action to be evaluated.
     */
    public String getAction() {
        return action;
    }

    /**
     * Sets the properties of the action.
     * @return The properties of the action.
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * Sets the properties of the action.
     * @param properties The properties of the action.
     */
    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AuthorizationAction that = (AuthorizationAction) obj;
        if (properties == null && that.properties == null) {
            return true;
        }
        if (properties == null || that.properties == null) {
            return false;
        }
        return action.equals(that.action) &&
                properties.equals(that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(action, properties);
    }
}
