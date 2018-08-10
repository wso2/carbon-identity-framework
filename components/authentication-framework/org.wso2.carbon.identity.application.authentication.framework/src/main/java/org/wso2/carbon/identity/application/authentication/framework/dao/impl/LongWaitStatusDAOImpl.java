/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.dao.LongWaitStatusDAO;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.model.LongWaitStatus;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class LongWaitStatusDAOImpl implements LongWaitStatusDAO {
    private static final Log log = LogFactory.getLog(LongWaitStatusDAOImpl.class);

    public void addWaitStatus(int tenantId, String waitKey, LongWaitStatus status, Timestamp createdTime, Timestamp
            expireTime) throws FrameworkException {

        String query = "INSERT INTO IDN_AUTH_WAIT_STATUS (TENANT_ID, LONG_WAIT_KEY, WAIT_STATUS, TIME_CREATED, " +
                "EXPIRE_TIME) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = IdentityDatabaseUtil.getDBConnection()) {

            try (PreparedStatement addPrepStmt = connection.prepareStatement(query)) {
                addPrepStmt.setInt(1, tenantId);
                addPrepStmt.setString(2, waitKey);
                if (LongWaitStatus.Status.WAITING == status.getStatus()) {
                    addPrepStmt.setString(3, "1");
                } else {
                    addPrepStmt.setString(3, "0");
                }
                addPrepStmt.setTimestamp(4, createdTime);
                addPrepStmt.setTimestamp(5, expireTime);
                addPrepStmt.execute();
                if (log.isDebugEnabled()) {
                    log.debug("Added wait status for wait key: " + waitKey);
                }
                connection.commit();
            } catch (SQLException e) {
                throw new FrameworkException("Error while adding wait status for key:" + waitKey, e);
            }
        } catch (SQLException e) {
            throw new FrameworkException("Error while adding wait status for key:" + waitKey, e);
        }
    }

    public void removeWaitStatus(String waitKey) throws FrameworkException {

        String query = "DELETE FROM IDN_AUTH_WAIT_STATUS WHERE LONG_WAIT_KEY=?";

        try (Connection connection = IdentityDatabaseUtil.getDBConnection()) {

            try (PreparedStatement addPrepStmt = connection.prepareStatement(query)) {
                addPrepStmt.setString(1, waitKey);
                addPrepStmt.execute();
                if (log.isDebugEnabled()) {
                    log.debug("Removed wait status for wait key: " + waitKey);
                }
                connection.commit();
            } catch (SQLException e) {
                throw new FrameworkException("Error while removing wait status with key:" + waitKey, e);
            }
        } catch (SQLException e) {
            throw new FrameworkException("Error while removing wait status with key:" + waitKey, e);
        }
    }

    public LongWaitStatus getWaitStatus(String waitKey) throws FrameworkException {

        String query = "SELECT WAIT_STATUS FROM IDN_AUTH_WAIT_STATUS WHERE LONG_WAIT_KEY=?";

        LongWaitStatus longWaitStatus = new LongWaitStatus();

        try (Connection connection = IdentityDatabaseUtil.getDBConnection()) {

            try (PreparedStatement addPrepStmt = connection.prepareStatement(query)) {
                addPrepStmt.setString(1, waitKey);
                try (ResultSet resultSet = addPrepStmt.executeQuery()) {
                    if (resultSet.next()) {
                        String wait_status = resultSet.getString("WAIT_STATUS");
                        if (log.isDebugEnabled()) {
                            log.debug("Searched for wait status for wait key: " + waitKey + ". Result: "
                                    + ("1".equals(wait_status) ? "WAITING" : "COMPLETED"));
                        }
                        if(wait_status.equals("1")) {
                            longWaitStatus.setStatus(LongWaitStatus.Status.WAITING);
                        }else {
                            longWaitStatus.setStatus(LongWaitStatus.Status.COMPLETED);
                        }
                    } else {
                        longWaitStatus.setStatus(LongWaitStatus.Status.UNKNOWN);
                        if (log.isDebugEnabled()) {
                            log.debug("Searched for wait status for wait key: " + waitKey + ". Result: UNKNOWN");
                        }
                    }
                }
            } catch (SQLException e) {
                throw new FrameworkException("Error while searching for wait status with key:" + waitKey, e);
            }
        } catch (SQLException e) {
            throw new FrameworkException("Error while searching for wait status with key:" + waitKey, e);
        }
        if (longWaitStatus.getStatus() == null) {
            longWaitStatus.setStatus(LongWaitStatus.Status.UNKNOWN);
        }
        return longWaitStatus;
    }
}
