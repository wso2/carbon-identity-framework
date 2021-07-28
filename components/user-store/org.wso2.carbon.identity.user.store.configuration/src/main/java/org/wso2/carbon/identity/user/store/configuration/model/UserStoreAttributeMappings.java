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

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant.UserStoreOperation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Data object to hold attribute mappings for all user store types.
 */
public class UserStoreAttributeMappings {

    private Map<String, UserStoreAttributeDO> ldapUserStoreAttrMappings;
    private Map<String, UserStoreAttributeDO> adUserStoreAttrMappings;
    private Map<String, UserStoreAttributeDO> defaultUserStoreAttrMapping;

    public UserStoreAttributeMappings() {

    }

    /**
     * To get user store attribute mappings of LDAP user store types.
     *
     * @return Map of attribute mappings.
     */
    public Map<String, UserStoreAttributeDO> getLdapUserStoreAttrMappings() {

        return Collections.unmodifiableMap(ldapUserStoreAttrMappings);
    }

    /**
     * Set attribute mappings of LDAP user store.
     *
     * @param changedLDAPAttrMap Map of attribute mappings which needs to be changed against default values.
     */
    public void setLdapUserStoreAttrMappings(Map<String, ChangedUserStoreAttributeDO> changedLDAPAttrMap) {

        this.ldapUserStoreAttrMappings = getModifiedAttrMap(changedLDAPAttrMap);
    }

    /**
     * To get user store attribute mappings of JDBC user store types.
     *
     * @return Map of attribute mappings.
     */
    public Map<String, UserStoreAttributeDO> getAdUserStoreAttrMappings() {

        return Collections.unmodifiableMap(adUserStoreAttrMappings);
    }

    /**
     * Set attribute mappings of AD user store.
     *
     * @param changedADAttrMap Map of attribute mappings which needs to be changed against default values.
     */
    public void setAdUserStoreAttrMappings(Map<String, ChangedUserStoreAttributeDO> changedADAttrMap) {

        this.adUserStoreAttrMappings = getModifiedAttrMap(changedADAttrMap);
    }

    /**
     * To get default user store attribute mappings.
     *
     * @return Map of attribute mappings.
     */
    public Map<String, UserStoreAttributeDO> getDefaultUserStoreAttrMapping() {

        return Collections.unmodifiableMap(defaultUserStoreAttrMapping);
    }

    /**
     * Set default attribute mappings.
     *
     * @param defaultUserStoreAttrMapping Map of default attribute mappings.
     */
    public void setDefaultUserStoreAttrMapping(Map<String, UserStoreAttributeDO> defaultUserStoreAttrMapping) {

        this.defaultUserStoreAttrMapping = defaultUserStoreAttrMapping;
        this.ldapUserStoreAttrMappings = defaultUserStoreAttrMapping;
        this.adUserStoreAttrMappings = defaultUserStoreAttrMapping;
    }

    /**
     * To merge default attribute mappings and mappings changes of other user stores.
     *
     * @param changedUserStoreAttrMap Attribute mapping changes which should change default values.
     * @return Map of attribute mappings.
     */
    private Map<String, UserStoreAttributeDO> getModifiedAttrMap(Map<String, ChangedUserStoreAttributeDO>
                                                                         changedUserStoreAttrMap) {

        if (defaultUserStoreAttrMapping == null) {
            return null;
        }
        Map<String, UserStoreAttributeDO> clonedAttrMap = SerializationUtils
                .clone(new HashMap<>(defaultUserStoreAttrMapping));
        for (String claimId : changedUserStoreAttrMap.keySet()) {
            if (clonedAttrMap.containsKey(claimId)) {
                if (changedUserStoreAttrMap.get(claimId).getOperation() == UserStoreOperation.UPDATE) {
                    if (clonedAttrMap.containsKey(claimId)) {
                        UserStoreAttributeDO defaultUserStoreAttributeDO = clonedAttrMap.get(claimId);
                        UserStoreAttributeDO newUserStoreAttributeDO = changedUserStoreAttrMap.get(claimId)
                                .getUsAttributeDO();
                        if (StringUtils.isNotBlank(newUserStoreAttributeDO.getMappedAttribute())) {
                            defaultUserStoreAttributeDO.setMappedAttribute(newUserStoreAttributeDO
                                    .getMappedAttribute());
                        }
                        if (StringUtils.isNotBlank(newUserStoreAttributeDO.getDisplayName())) {
                            defaultUserStoreAttributeDO.setDisplayName(newUserStoreAttributeDO.getDisplayName());
                        }
                        clonedAttrMap.put(claimId, defaultUserStoreAttributeDO);
                    }
                } else if (changedUserStoreAttrMap.get(claimId).getOperation() == UserStoreOperation.DELETE) {
                    clonedAttrMap.remove(claimId);
                }
            }
        }
        return clonedAttrMap;
    }
}

