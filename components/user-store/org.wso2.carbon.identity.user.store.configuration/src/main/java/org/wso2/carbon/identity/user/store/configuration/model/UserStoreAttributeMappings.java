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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant.UserStoreOperation;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Data object to hold attribute mappings for all user store types.
 */
public class UserStoreAttributeMappings {

    private Map<String, UserStoreAttributeDO> defaultUserStoreAttrMapping;
    private static final Log LOG = LogFactory.getLog(UserStoreAttributeMappings.class);
    private final Map<String, Map<String, UserStoreAttributeDO>> userStoreAttrMappings = new HashMap<>();

    /**
     * To get default user store attribute mappings.
     *
     * @return Map of attribute mappings.
     */
    public Map<String, UserStoreAttributeDO> getDefaultUserStoreAttributeMapping() {

        return Collections.unmodifiableMap(defaultUserStoreAttrMapping);
    }

    /**
     * Set default attribute mappings.
     *
     * @param defaultUserStoreAttrMapping Map of default attribute mappings.
     */
    public void setDefaultUserStoreAttributeMapping(Map<String, UserStoreAttributeDO> defaultUserStoreAttrMapping) {

        this.defaultUserStoreAttrMapping = defaultUserStoreAttrMapping;
    }

    /**
     * Get attribute mappings changes for the given user store type.
     *
     * @param userStoretype User store type.
     * @return Map of attribute uri and attribute mappings of the given user store type.
     */
    public Map<String, UserStoreAttributeDO> getUserStoreAttributeMappings(String userStoretype) {

        if (userStoreAttrMappings.containsKey(userStoretype)) {
            return Collections.unmodifiableMap(userStoreAttrMappings.get(userStoretype));
        }
        /*
         * If details are not available for the given user store type, return default values.
         * Add a new userstore attribute mappings file to conf/attributes/userstore directory to
         * configure correct attribute mappings for the userstore.
         */
        LOG.warn(String.format("No record found for the given userstore: %s. Default attribute mappings are " +
                        "returning. Please add userstore attribute mappings file for the userstore %s " +
                        "to conf/attributes/userstore directory to configure correct attribute mappings.",
                userStoretype, userStoretype));
        return defaultUserStoreAttrMapping;
    }

    /**
     * Set all available user store attribute mappings.
     *
     * @param availableUserStoreAttrMappings Map of user store type and their attribute mappings changes.
     */
    public void setUserStoreAttributeMappings(Map<String, Map<String, ChangedUserStoreAttributeDO>>
                                                      availableUserStoreAttrMappings) {

        for (Map.Entry<String, Map<String, ChangedUserStoreAttributeDO>> entry :
                availableUserStoreAttrMappings.entrySet()) {
            Map<String, UserStoreAttributeDO> tempMap = getModifiedAttributeMap(entry.getValue());
            this.userStoreAttrMappings.put(entry.getKey(), tempMap);
        }
    }

    /**
     * To merge default attribute mappings and mappings changes of other user stores.
     *
     * @param changedUserStoreAttrMap Attribute mapping changes which should change default values.
     * @return Map of attribute mappings.
     */
    private Map<String, UserStoreAttributeDO> getModifiedAttributeMap(Map<String, ChangedUserStoreAttributeDO>
                                                                              changedUserStoreAttrMap) {

        if (defaultUserStoreAttrMapping == null) {
            return null;
        }
        Gson gson = new Gson();
        String serializedDefaultAttrMappings = gson.toJson(defaultUserStoreAttrMapping);
        // To deserialize a hashmap using Gson, need a type object of the hashmap.
        Type type = new TypeToken<HashMap<String, UserStoreAttributeDO>>() {
        }.getType();
        Map<String, UserStoreAttributeDO> clonedAttrMap = gson.fromJson(serializedDefaultAttrMappings, type);
        for (Map.Entry<String, ChangedUserStoreAttributeDO> entry : changedUserStoreAttrMap.entrySet()) {
            if (!clonedAttrMap.containsKey(entry.getKey())) {
                continue;
            }
            if (entry.getValue().getOperation() == UserStoreOperation.UPDATE) {
                UserStoreAttributeDO defaultUserStoreAttributeDO = clonedAttrMap.get(entry.getKey());
                UserStoreAttributeDO newUserStoreAttributeDO = entry.getValue().getUsAttributeDO();
                if (StringUtils.isNotBlank(newUserStoreAttributeDO.getMappedAttribute())) {
                    defaultUserStoreAttributeDO.setMappedAttribute(newUserStoreAttributeDO
                            .getMappedAttribute());
                }
                if (StringUtils.isNotBlank(newUserStoreAttributeDO.getDisplayName())) {
                    defaultUserStoreAttributeDO.setDisplayName(newUserStoreAttributeDO.getDisplayName());
                }
                clonedAttrMap.put(entry.getKey(), defaultUserStoreAttributeDO);
            } else if (entry.getValue().getOperation() == UserStoreOperation.DELETE) {
                clonedAttrMap.remove(entry.getKey());
            }
        }
        return clonedAttrMap;
    }
}

