/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.user.store.configuration.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.user.store.configuration.dao.AbstractUserStoreDAOFactory;
import org.wso2.carbon.identity.user.store.configuration.dao.UserStoreDAO;

/**
 * This is the implementation of {@link AbstractUserStoreDAOFactory} to create FileBasedUserStoreDAOs.
 */
public class FileBasedUserStoreDAOFactory extends AbstractUserStoreDAOFactory {

    private static final String FILE_BASED = FileBasedUserStoreDAOImpl.class.getName();
    private static final Log log = LogFactory.getLog(AbstractUserStoreDAOFactory.class);

    @Override
    public UserStoreDAO getInstance() {

        UserStoreDAO userStoreDAOImpl = new FileBasedUserStoreDAOImpl();

        if (log.isDebugEnabled()) {
            log.debug("Created new file based userStore DAO : " + FILE_BASED);
        }
        return userStoreDAOImpl;
    }

    @Override
    public String getRepository() {

        return FileBasedUserStoreDAOFactory.class.getName();
    }
}
