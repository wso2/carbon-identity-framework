///*
// * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package org.wso2.carbon.identity.common.util.jdbc;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.wso2.carbon.identity.common.base.exception.IdentityRuntimeException;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//
///**
// * Utility class for database operations.
// */
//public class JDBCUtils {
//
//    private static final Logger logger = LoggerFactory.getLogger(JDBCUtils.class);
//
//    private static volatile JDBCUtils instance = new JDBCUtils();
//
//    private JDBCUtils() {
//
//    }
//
//    public static JDBCUtils getInstance() {
//        return instance;
//    }
//
//    /**
//     * Get a database connection instance from the Identity Persistence Manager.
//     *
//     * @return Database Connection
//     * @throws IdentityRuntimeException Error when getting a database connection to Identity database
//     */
//    public static Connection getDBConnection() throws IdentityRuntimeException {
//        return null;
//    }
//
//    public static void closeAllConnections(Connection dbConnection, ResultSet rs, PreparedStatement prepStmt) {
//
//        closeResultSet(rs);
//        closeStatement(prepStmt);
//        closeConnection(dbConnection);
//    }
//
//    public static void closeConnection(Connection dbConnection) {
//        if (dbConnection != null) {
//            try {
//                dbConnection.close();
//            } catch (SQLException e) {
//                logger.error("Database error. Could not close statement. Continuing with others. - " + e.getMessage()
//                        , e);
//            }
//        }
//    }
//
//    public static void closeResultSet(ResultSet rs) {
//        if (rs != null) {
//            try {
//                rs.close();
//            } catch (SQLException e) {
//                logger.error("Database error. Could not close result set  - " + e.getMessage(), e);
//            }
//        }
//
//    }
//
//    public static void closeStatement(PreparedStatement preparedStatement) {
//        if (preparedStatement != null) {
//            try {
//                preparedStatement.close();
//            } catch (SQLException e) {
//                logger.error("Database error. Could not close statement. Continuing with others. - " + e.getMessage()
//                        , e);
//            }
//        }
//
//    }
//
//    public static void rollBack(Connection dbConnection) {
//        try {
//            if (dbConnection != null) {
//                dbConnection.rollback();
//            }
//        } catch (SQLException e1) {
//            logger.error("An error occurred while rolling back transactions. ", e1);
//        }
//    }
//}
