/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.user.store.configuration.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.user.store.configuration.dao.AbstractUserStoreDAOFactory;
import org.wso2.carbon.identity.user.store.configuration.dao.UserStoreDAO;

/**
 * This is the implementation of {@link AbstractUserStoreDAOFactory} to create DatabaseBasedUserStoreDAOFactory.
 */
public class DatabaseBasedUserStoreDAOFactory extends AbstractUserStoreDAOFactory {

    private static final String DATABASE_BASED = DatabaseBasedUserStoreDAOFactory.class.getName();
    private static final Log log = LogFactory.getLog(DatabaseBasedUserStoreDAOFactory.class);

    @Override
    public UserStoreDAO getInstance() {

        UserStoreDAO userStoreDAOImpl = new DatabaseBasedUserStoreDAOImpl();

        if (log.isDebugEnabled()) {
            log.debug("Created new database based userStore DAO : " + DATABASE_BASED);
        }
        return userStoreDAOImpl;
    }

    @Override
    public String getRepository() {

        return DATABASE_BASED;
    }
}
