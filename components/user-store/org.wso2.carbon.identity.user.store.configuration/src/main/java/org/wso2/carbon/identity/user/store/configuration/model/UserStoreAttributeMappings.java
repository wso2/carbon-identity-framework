/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com).
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.user.store.configuration.model;

import java.util.Collections;
import java.util.Map;

/**
 * Data object to hold attribute mappings for all user store types.
 */
public class UserStoreAttributeMappings {

    private Map<String, UserStoreAttribute> defaultUserStoreAttributeMappings;
    private Map<String, Map<String, UserStoreAttribute>> userStoreAttributeMappings;

    /**
     * To get default user store attribute mappings.
     *
     * @return Map of default attribute mappings.
     */
    public Map<String, UserStoreAttribute> getDefaultUserStoreAttributeMappings() {

        return Collections.unmodifiableMap(defaultUserStoreAttributeMappings);
    }

    /**
     * Set default attribute mappings.
     *
     * @param defaultUserStoreAttrMapping Map of default attribute mappings.
     */
    public void setDefaultUserStoreAttributeMappings(Map<String, UserStoreAttribute> defaultUserStoreAttrMapping) {

        this.defaultUserStoreAttributeMappings = defaultUserStoreAttrMapping;
    }

    /**
     * Retrieve userstore attribute mappings.
     *
     * @return Map of userstores attribute mappings.
     */
    public Map<String, Map<String, UserStoreAttribute>> getUserStoreAttributeMappings() {

        return Collections.unmodifiableMap(userStoreAttributeMappings);
    }

    /**
     * Set all available user store attribute mappings.
     *
     * @param availableUserStoreAttrMappings Map of user store type and their attribute mappings.
     */
    public void setUserStoreAttributeMappings(
            Map<String, Map<String, UserStoreAttribute>> availableUserStoreAttrMappings) {

        userStoreAttributeMappings = availableUserStoreAttrMappings;
    }
}

