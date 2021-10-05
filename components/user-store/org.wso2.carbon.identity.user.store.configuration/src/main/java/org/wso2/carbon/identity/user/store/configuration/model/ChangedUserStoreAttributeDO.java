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

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant.UserStoreOperation;

/**
 * To keep attribute mapping details which should be changed.
 */
public class ChangedUserStoreAttributeDO {

    private UserStoreOperation operation;
    private UserStoreAttributeDO userStoreAttributeDO;

    /**
     * Get operation which should be performed on the attribute {@literal userStoreAttributeDO}.
     *
     * @return UserStoreOperation enum {@link UserStoreOperation}.
     */
    public UserStoreOperation getOperation() {

        return operation;
    }

    /**
     * Set operation which should be performed on attribute {@literal userStoreAttributeDO}.
     *
     * @param operation UserStoreOperation.
     */
    public void setOperation(String operation) {

        if (StringUtils.equalsIgnoreCase(UserStoreOperation.UPDATE.toString(), operation)) {
            this.operation = UserStoreOperation.UPDATE;
        } else if (StringUtils.equalsIgnoreCase(UserStoreOperation.DELETE.toString(), operation)) {
            this.operation = UserStoreOperation.DELETE;
        }
    }

    /**
     * Get attribute mappings.
     *
     * @return UserStoreAttributeDO.
     */
    public UserStoreAttributeDO getUsAttributeDO() {

        return userStoreAttributeDO;
    }

    /**
     * Set attribute details need to be changed.
     *
     * @param userStoreAttributeDO Attribute details.
     */
    public void setUsAttributeDO(UserStoreAttributeDO userStoreAttributeDO) {

        this.userStoreAttributeDO = userStoreAttributeDO;
    }
}

