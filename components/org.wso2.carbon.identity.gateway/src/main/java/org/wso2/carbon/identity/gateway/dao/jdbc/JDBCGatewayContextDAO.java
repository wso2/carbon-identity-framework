/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.gateway.dao.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.common.jdbc.DataAccessException;
import org.wso2.carbon.identity.common.jdbc.JdbcTemplate;
import org.wso2.carbon.identity.gateway.api.context.GatewayMessageContext;
import org.wso2.carbon.identity.gateway.api.exception.GatewayRuntimeException;
import org.wso2.carbon.identity.gateway.dao.GatewayContextDAO;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;


/**
 *
 *
 */
public class JDBCGatewayContextDAO extends GatewayContextDAO {

    private static final Logger logger = LoggerFactory.getLogger(JDBCGatewayContextDAO.class);
    private static final String KEY = "KEY";
    private static final String OPERATION = "OPERATION";
    private static final String SESSION_OBJECT = "SESSION_OBJECT";
    private static final String TIME_CREATED = "TIME_CREATED";
    private static volatile JDBCGatewayContextDAO instance = new JDBCGatewayContextDAO();
    private JdbcTemplate jdbcTemplate;

    private JDBCGatewayContextDAO() {

    }

    public static JDBCGatewayContextDAO getInstance() {
        return instance;
    }

    @Override
    public GatewayMessageContext get(String key) {

        final String retrieveContext =
                "SELECT " + "OPERATION, TIME_CREATED, SESSION_OBJECT FROM IDN_CONTEXT WHERE KEY = :" + KEY + "; " +
                        "ORDER BY TIME_CREATED DESC LIMIT 1";

        AtomicReference<GatewayMessageContext> identityMessageContextAtomicReference = new AtomicReference<>();

        try {
            jdbcTemplate.fetchSingleRecord(retrieveContext, (resultSet, rowNumber) -> {
                String operation = resultSet.getString(OPERATION);
                if ("STORE".equals(operation)) {
                    InputStream is = resultSet.getBinaryStream(SESSION_OBJECT);
                    if (is != null) {
                        ObjectInput ois = null;
                        try {
                            ois = new ObjectInputStream(is);
                            identityMessageContextAtomicReference.set((GatewayMessageContext) ois.readObject());
                        } catch (IOException | ClassNotFoundException e) {
                            logger.error("Error while trying to close ObjectInputStream.", e);
                        } finally {
                            if (ois != null) {
                                try {
                                    ois.close();
                                } catch (IOException e) {
                                    logger.error("Error while trying to close ObjectInputStream.", e);
                                }
                            }
                        }
                    }
                }
                return identityMessageContextAtomicReference.get();
            }, namedPreparedStatement -> {
                namedPreparedStatement.setString(KEY, key);
            });
        } catch (DataAccessException e) {
            String errorMessage = "Error while retrieving session, " + e.getMessage();
            logger.error(errorMessage, e);
            throw new GatewayRuntimeException(errorMessage, e);
        }
        return identityMessageContextAtomicReference.get();
    }

    @Override
    public void put(String key, GatewayMessageContext gatewayMessageContext) {

        final String storeContext =
                "INSERT INTO IDN_CONTEXT " + "(KEY, OPERATION, SESSION_OBJECT, TIME_CREATED)"
                        + "VALUES (:" + KEY + ";, :" + OPERATION + ";, :" + SESSION_OBJECT + ";, :"
                        + TIME_CREATED + ";)";

        try {
            jdbcTemplate.executeInsert(storeContext, (namedPreparedStatement) -> {
                namedPreparedStatement.setString(KEY, key);
                namedPreparedStatement.setString(OPERATION, "STORE");
                namedPreparedStatement.setBlob(SESSION_OBJECT, gatewayMessageContext);
                namedPreparedStatement.setTimeStamp(TIME_CREATED, new Timestamp(new Date().getTime()));
            }, null, false);
        } catch (DataAccessException e) {
            String errorMessage = "Error while storing session, " + e.getMessage();
            logger.error(errorMessage, e);
            throw new GatewayRuntimeException(errorMessage, e);
        }
    }

    @Override
    public void remove(String key) {

        final String deleteContext =
                "INSERT INTO IDN_CONTEXT " + "(KEY, OPERATION, TIME_CREATED)"
                        + "VALUES (:" + KEY + ";, :" + OPERATION + ";, :"
                        + TIME_CREATED + ";)";

        try {
            jdbcTemplate.executeInsert(deleteContext, (namedPreparedStatement) -> {
                namedPreparedStatement.setString(KEY, key);
                namedPreparedStatement.setString(OPERATION, "DELETE");
                namedPreparedStatement.setTimeStamp(TIME_CREATED, new Timestamp(new Date().getTime()));
            }, null, false);
        } catch (DataAccessException e) {
            String errorMessage = "Error while storing session, " + e.getMessage();
            logger.error(errorMessage, e);
            throw new GatewayRuntimeException(errorMessage, e);
        }
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
