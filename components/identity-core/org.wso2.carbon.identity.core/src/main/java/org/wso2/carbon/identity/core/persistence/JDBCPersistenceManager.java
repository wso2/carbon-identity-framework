/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.identity.core.persistence;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.namespace.QName;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * This class is used for handling identity meta data persistence in the Identity JDBC Store. During
 * the server start-up, it checks whether the database is created, if not it creates one. It reads
 * the data source properties from the identity.xml.
 * This is implemented as a singleton. An instance of this class can be obtained through
 * JDBCPersistenceManager.getInstance() method.
 */
public class JDBCPersistenceManager {

    private static Log log = LogFactory.getLog(JDBCPersistenceManager.class);
    private static volatile JDBCPersistenceManager instance;
    private DataSource dataSource;

    private JDBCPersistenceManager() {
        initDataSource();
    }

    /**
     * Get an instance of the JDBCPersistenceManager. It implements a lazy
     * initialization with double
     * checked locking, because it is initialized first by identity.core module
     * during the start up.
     *
     * @return JDBCPersistenceManager instance
     * @throws IdentityException Error when reading the data source configurations
     */
    public static JDBCPersistenceManager getInstance() {
        if (instance == null) {
            synchronized (JDBCPersistenceManager.class) {
                if (instance == null) {
                    instance = new JDBCPersistenceManager();
                }
            }
        }
        return instance;
    }

    private void initDataSource() {

        OMElement persistenceManagerConfigElem = IdentityConfigParser.getInstance()
                .getConfigElement("JDBCPersistenceManager");
        try {
            if (persistenceManagerConfigElem == null) {
                String errorMsg = "Identity Persistence Manager configuration is not available in " +
                        "identity.xml file. Terminating the JDBC Persistence Manager " +
                        "initialization. This may affect certain functionality.";
                throw IdentityRuntimeException.error(errorMsg);
            }

            OMElement dataSourceElem = persistenceManagerConfigElem.getFirstChildWithName(
                    new QName(IdentityCoreConstants.IDENTITY_DEFAULT_NAMESPACE, "DataSource"));

            if (dataSourceElem == null) {
                String errorMsg = "DataSource Element is not available for JDBC Persistence " +
                        "Manager in identity.xml file. Terminating the JDBC Persistence Manager " +
                        "initialization. This might affect certain features.";
                throw IdentityRuntimeException.error(errorMsg);
            }

            OMElement dataSourceNameElem = dataSourceElem.getFirstChildWithName(
                    new QName(IdentityCoreConstants.IDENTITY_DEFAULT_NAMESPACE, "Name"));

            if (dataSourceNameElem != null) {
                String dataSourceName = dataSourceNameElem.getText();
                Context ctx = new InitialContext();
                dataSource = (DataSource) ctx.lookup(dataSourceName);
            }
        }  catch (NamingException e) {
            String errorMsg = "Error when looking up the Identity Data Source.";
            throw IdentityRuntimeException.error(errorMsg, e);
        }
    }

    public void initializeDatabase() {
        IdentityDBInitializer dbInitializer = new IdentityDBInitializer(dataSource);
        dbInitializer.createIdentityDatabase();
    }

    /**
     * Returns an database connection for Identity data source.
     *
     * @return Database connection
     * @throws IdentityException Exception occurred when getting the data source.
     */
    public Connection getDBConnection() throws IdentityRuntimeException {
        try {
            Connection dbConnection = dataSource.getConnection();
            dbConnection.setAutoCommit(false);
            dbConnection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            return dbConnection;
        } catch (SQLException e) {
            String errMsg = "Error when getting a database connection object from the Identity data source.";
            throw IdentityRuntimeException.error(errMsg, e);
        }
    }

}
