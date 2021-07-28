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

package org.wso2.carbon.identity.user.store.configuration.utils;

import org.wso2.carbon.identity.user.store.configuration.internal.UserStoreConfigListenersHolder;
import org.wso2.carbon.identity.user.store.configuration.model.UserStoreAttributeMappings;

/**
 * Build attribute mappings.
 */
public class AttributeMappingBuilder {

    private AttributeMappingBuilder() {

    }

    private static final class BuilderHolder {

        static final AttributeMappingBuilder BUILDER = new AttributeMappingBuilder();
    }

    public static AttributeMappingBuilder getInstance() {

        return BuilderHolder.BUILDER;
    }

    /**
     * Build attribute mappings for user store types and store in {@link UserStoreConfigListenersHolder}.
     */
    public void build() {

        UserStoreAttributeMappings userStoreAttributeMappings = new UserStoreAttributeMappings();
        userStoreAttributeMappings.setDefaultUserStoreAttrMapping(DefaultUserStoreAttributeMappingParser.getInstance().
                getDefaultAttributes());
        userStoreAttributeMappings.setAdUserStoreAttrMappings(UserStoreAttributeMappingParser.getInstance().
                getADUserStoreAttrChangeDOMap());
        userStoreAttributeMappings.setLdapUserStoreAttrMappings(UserStoreAttributeMappingParser.getInstance().
                getLDAPUserStoreAttrChangeDOMap());
        UserStoreConfigListenersHolder.getInstance().setUserStoreAttributeMappings(userStoreAttributeMappings);
    }
}
