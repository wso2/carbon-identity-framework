/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 * NOTE: The code/logic in this class is copied from https://bitbucket.org/thetransactioncompany/cors-filter.
 * All credits goes to the original authors of the project https://bitbucket.org/thetransactioncompany/cors-filter.
 */

package org.wso2.carbon.identity.cors.mgt.core.constant;

/**
 * SQL queries related to CORS operations.
 */
public class SQLQueries {

    public static final String GET_CORS_ORIGINS_BY_TENANT_ID =
            "SELECT ID, ORIGIN, UUID " +
            "FROM IDN_CORS_ORIGIN " +
            "WHERE TENANT_ID = ? " +
            "ORDER BY ID ASC";

    public static final String GET_CORS_ORIGINS_BY_APPLICATION_ID =
            "SELECT ID, ORIGIN, UUID " +
            "FROM IDN_CORS_ORIGIN " +
            "INNER JOIN IDN_CORS_ASSOCIATION ON IDN_CORS_ORIGIN.ID = IDN_CORS_ASSOCIATION.IDN_CORS_ORIGIN_ID " +
            "WHERE IDN_CORS_ORIGIN.TENANT_ID = ? AND IDN_CORS_ASSOCIATION.SP_APP_ID = ? " +
            "ORDER BY ID ASC";

    public static final String GET_CORS_ORIGIN_ID =
            "SELECT ID FROM IDN_CORS_ORIGIN " +
            "WHERE TENANT_ID = ? AND ORIGIN = ?";

    public static final String GET_CORS_ORIGIN_ID_BY_UUID =
            "SELECT ID " +
            "FROM IDN_CORS_ORIGIN " +
            "WHERE UUID = ?";

    public static final String INSERT_CORS_ORIGIN =
            "INSERT INTO IDN_CORS_ORIGIN (TENANT_ID, ORIGIN, UUID) " +
            "VALUES (?, ?, ?)";

    public static final String INSERT_CORS_ASSOCIATION =
            "INSERT INTO IDN_CORS_ASSOCIATION (IDN_CORS_ORIGIN_ID, SP_APP_ID) " +
            "VALUES (?, ?)";

    public static final String DELETE_ORIGIN =
            "DELETE " +
            "FROM IDN_CORS_ORIGIN " +
            "WHERE ID = ?";

    public static final String GET_CORS_APPLICATION_IDS_BY_CORS_ORIGIN_ID =
            "SELECT SP_APP_ID " +
            "FROM IDN_CORS_ASSOCIATION " +
            "WHERE IDN_CORS_ORIGIN_ID = ?";

    public static final String GET_CORS_APPLICATIONS_BY_CORS_ORIGIN_ID =
            "SELECT SP_APP.UUID, SP_APP.APP_NAME " +
            "FROM SP_APP " +
            "INNER JOIN IDN_CORS_ASSOCIATION ON IDN_CORS_ASSOCIATION.SP_APP_ID = SP_APP.ID " +
            "INNER JOIN IDN_CORS_ORIGIN ON IDN_CORS_ASSOCIATION.IDN_CORS_ORIGIN_ID = IDN_CORS_ORIGIN.ID " +
            "WHERE IDN_CORS_ORIGIN.UUID = ?";

    public static final String DELETE_CORS_APPLICATION_ASSOCIATION =
            "DELETE " +
            "FROM IDN_CORS_ASSOCIATION " +
            "WHERE IDN_CORS_ORIGIN_ID = ? AND SP_APP_ID = ?";
}
