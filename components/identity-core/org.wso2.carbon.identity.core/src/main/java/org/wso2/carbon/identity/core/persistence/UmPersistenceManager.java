/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.core.persistence;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.util.DatabaseUtil;

import javax.sql.DataSource;

/**
 * This class is used as a data holder for user management datasource
 */
public class UmPersistenceManager {
    private static DataSource dataSource;
    private static UmPersistenceManager umPersistenceManager = new UmPersistenceManager();

    private static Log log = LogFactory.getLog(UmPersistenceManager.class);

    /**
     * Private constructor which will not allow to create objects of this class from outside
     */
    private UmPersistenceManager() {
        initDatasource();
    }

    /**
     * Singleton method
     *
     * @return UmPersistenceManager
     */
    public static UmPersistenceManager getInstance() {
        return umPersistenceManager;
    }

    /**
     * Get user management datasource.
     *
     * @return user management datasource
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    private void initDatasource() {
        try {
            dataSource = DatabaseUtil.getRealmDataSource(CarbonContext.getThreadLocalCarbonContext().getUserRealm().
                    getRealmConfiguration());
        } catch (UserStoreException e) {
            log.error("Error while retrieving user management data source", e);
        }
    }
}
