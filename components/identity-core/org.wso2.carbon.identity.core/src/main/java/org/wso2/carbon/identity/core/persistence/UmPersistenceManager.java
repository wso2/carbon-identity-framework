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
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.util.DatabaseUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * This class is used as a data holder for user management datasource
 */
public class UmPersistenceManager {
    private static DataSource dataSource;
    private static UmPersistenceManager umPersistenceManager = new UmPersistenceManager();
    // This property refers to Active transaction state of postgresql db
    private static final String PG_ACTIVE_SQL_TRANSACTION_STATE = "25001";
    private static final String POSTGRESQL_DATABASE = "PostgreSQL";

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

    /**
     * Returns an database connection for UM data source.
     *
     * @param shouldApplyTransaction apply transaction or not
     * @return Database connection.
     * @throws IdentityRuntimeException Exception occurred when getting the data source.
     */
    public Connection getDBConnection(boolean shouldApplyTransaction) throws IdentityRuntimeException {

        try {
            Connection dbConnection = dataSource.getConnection();
            if (shouldApplyTransaction) {
                dbConnection.setAutoCommit(false);
                try {
                    dbConnection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                } catch (SQLException e) {
                    /* Handling startup error for postgresql Ref: https://github.com/wso2/product-is/issues/3545
                       Active SQL Transaction means that connection is not committed.
                       Need to commit before setting isolation property. */
                    if (dbConnection.getMetaData().getDriverName().contains(POSTGRESQL_DATABASE)
                            && PG_ACTIVE_SQL_TRANSACTION_STATE.equals(e.getSQLState())) {
                        dbConnection.commit();
                        dbConnection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                    }
                }
            }
            return dbConnection;
        } catch (SQLException e) {
            String errMsg = "Error when getting a database connection object from the UM data source.";
            throw IdentityRuntimeException.error(errMsg, e);
        }
    }

    /**
     * Revoke the transaction when catch then sql transaction errors.
     *
     * @param dbConnection database connection.
     */
    public void rollbackTransaction(Connection dbConnection) {

        try {
            if (dbConnection != null) {
                dbConnection.rollback();
            }
        } catch (SQLException e1) {
            log.error("An error occurred while rolling back transactions.", e1);
        }
    }

    /**
     * Commit the transaction.
     *
     * @param dbConnection database connection.
     */
    public void commitTransaction(Connection dbConnection) {

        try {
            if (dbConnection != null) {
                dbConnection.commit();
            }
        } catch (SQLException e1) {
            log.error("An error occurred while commit transactions.", e1);
        }
    }
}
