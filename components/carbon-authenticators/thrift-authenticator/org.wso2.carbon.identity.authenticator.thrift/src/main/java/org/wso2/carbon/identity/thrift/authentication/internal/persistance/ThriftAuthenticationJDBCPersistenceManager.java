/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.identity.thrift.authentication.internal.persistance;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.thrift.authentication.internal.generatedCode.AuthenticationException;
import org.wso2.carbon.identity.thrift.authentication.internal.util.ThriftAuthenticationConfigParser;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.namespace.QName;
import java.sql.Connection;
import java.sql.SQLException;
/**
 * This class is used for handling authentication meta data persistence in the Thrift Authentication JDBC Store. During
 * the server start-up, it checks whether the database is created, if not it creates one. It reads
 * the data source properties from the thrift-authentication.xml.
 * This is implemented as a singleton. An instance of this class can be obtained through
 * ThriftAuthenticationJDBCPersistenceManager.getInstance() method.
 */
public class ThriftAuthenticationJDBCPersistenceManager {

    private static Log log = LogFactory.getLog(ThriftAuthenticationJDBCPersistenceManager.class);
    private static ThriftAuthenticationJDBCPersistenceManager instance;
    private DataSource dataSource;

    private ThriftAuthenticationJDBCPersistenceManager() throws AuthenticationException {
        initDataSource();
    }

    /**
     * Get an instance of the ThriftAuthenticationJDBCPersistenceManager. It implements a lazy
     * initialization with double
     * checked locking, because it is initialized first by identity.core module
     * during the start up.
     *
     * @return ThriftAuthenticationJDBCPersistenceManager instance
     * @throws AuthenticationException Error when reading the data source configurations
     */
    public static ThriftAuthenticationJDBCPersistenceManager getInstance() throws AuthenticationException {
        if (instance == null) {
            synchronized (ThriftAuthenticationJDBCPersistenceManager.class) {
                if (instance == null) {
                    instance = new ThriftAuthenticationJDBCPersistenceManager();
                }
            }
        }
        return instance;
    }

    private void initDataSource() throws AuthenticationException {

        OMElement persistenceManagerConfigElem = ThriftAuthenticationConfigParser.getInstance()
                .getConfigElement("JDBCPersistenceManager");
        try {
            if (persistenceManagerConfigElem == null) {
                String errorMsg = "Thrift Authentication Persistence Manager configuration is not available in " +
                        "thrift-authentication.xml file. Terminating the JDBC Persistence Manager " +
                        "initialization. This may affect certain functionality.";
                log.error(errorMsg);
                throw new AuthenticationException(errorMsg);
            }

            OMElement dataSourceElem = persistenceManagerConfigElem.getFirstChildWithName(
                    new QName(ThriftAuthenticationConfigParser.IDENTITY_DEFAULT_NAMESPACE, "DataSource"));

            if (dataSourceElem == null) {
                String errorMsg = "DataSource Element is not available for JDBC Persistence " +
                        "Manager in identity.xml file. Terminating the JDBC Persistence Manager " +
                        "initialization. This might affect certain features.";
                log.error(errorMsg);
                throw new AuthenticationException(errorMsg);
            }

            OMElement dataSourceNameElem = dataSourceElem.getFirstChildWithName(
                    new QName(ThriftAuthenticationConfigParser.IDENTITY_DEFAULT_NAMESPACE, "Name"));

            if (dataSourceNameElem != null) {
                String dataSourceName = dataSourceNameElem.getText();
                Context ctx = new InitialContext();
                dataSource = (DataSource) ctx.lookup(dataSourceName);
            }
        } catch (NamingException e) {
            String errorMsg = "Error when looking up the Thrift Authentication Data Source.";
            log.error(errorMsg, e);
            throw new AuthenticationException(errorMsg);
        }
    }

    /**
     * Returns an database connection for Identity data source.
     *
     * @return Database connection
     * @throws AuthenticationException Exception occurred when getting the data source.
     */
    public Connection getDBConnection() throws AuthenticationException {
        try {
            Connection dbConnection = dataSource.getConnection();
            dbConnection.setAutoCommit(false);
            dbConnection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            return dbConnection;
        } catch (SQLException e) {
            String errMsg = "Error when getting a database connection object from the Thrift Authentication data source.";
            log.error(errMsg, e);
            throw new AuthenticationException(errMsg);
        }
    }

}
