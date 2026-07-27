/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.flow.mgt.dao;

/**
 * This class holds the database queries and constants for the flow management DAO layer.
 */
public class SQLConstants {

    public static final String DELETE_FLOW =
            "DELETE FROM IDN_FLOW WHERE TENANT_ID = ? AND IS_DEFAULT = ? AND TYPE = ?";
    public static final String INSERT_FLOW_INTO_IDN_FLOW =
            "INSERT INTO IDN_FLOW (ID, TENANT_ID, FLOW_NAME, TYPE, IS_DEFAULT, LAST_MODIFIED) VALUES (?, ?, ?, ?, ?, ?)";
    public static final String INSERT_FLOW_NODE_INFO =
            "INSERT INTO IDN_FLOW_NODE (NODE_ID, FLOW_ID, NODE_TYPE, IS_FIRST_NODE) VALUES (?, ?, ?, ?)";
    public static final String INSERT_NODE_EXECUTOR_INFO =
            "INSERT INTO IDN_FLOW_NODE_EXECUTOR (FLOW_NODE_ID, EXECUTOR_NAME) VALUES (?, ?)";
    public static final String INSERT_NODE_EXECUTOR_META =
            "INSERT INTO IDN_FLOW_NODE_EXECUTOR_META (EXECUTOR_ID, METADATA_NAME, METADATA_VALUE) VALUES (?, ?, ?)";
    public static final String INSERT_NODE_EDGES =
            "INSERT INTO IDN_FLOW_NODE_MAPPING (FLOW_NODE_ID, NEXT_NODE_ID, TRIGGERING_ELEMENT) VALUES (?, ?, ?)";
    public static final String INSERT_FLOW_PAGE_INFO =
            "INSERT INTO IDN_FLOW_PAGE (FLOW_ID, FLOW_NODE_ID, STEP_ID, PAGE_CONTENT, TYPE) VALUES (?, ?, ?, ?, ?)";
    public static final String INSERT_FLOW_PAGE_META =
            "INSERT INTO IDN_FLOW_PAGE_META (PAGE_ID, COORDINATE_X, COORDINATE_Y, HEIGHT, WIDTH) VALUES (?, ?, ?, ?, " +
                    "?)";
    public static final String GET_FLOW =
            "SELECT P.ID AS PAGE_ID, P.STEP_ID, P.PAGE_CONTENT, P.TYPE AS PAGE_TYPE, M.COORDINATE_X, M" +
                    ".COORDINATE_Y, M.HEIGHT, M.WIDTH " +
                    "FROM IDN_FLOW F JOIN IDN_FLOW_PAGE P ON F.ID = P.FLOW_ID " +
                    "LEFT JOIN IDN_FLOW_PAGE_META M ON P.ID = M.PAGE_ID " +
                    "WHERE F.TENANT_ID = ? AND F.IS_DEFAULT = ? AND F.TYPE = ?";

    public static final String GET_NODES_WITH_MAPPINGS_QUERY =
            "SELECT f.ID AS FLOW_ID, n.NODE_ID, n.NODE_TYPE, n.IS_FIRST_NODE, " +
                    "ne.EXECUTOR_NAME, ne.ID as EXECUTOR_ID, " +
                    "nextNode.NODE_ID AS NEXT_NODE_ACTUAL_ID, nm.TRIGGERING_ELEMENT " +
                    "FROM IDN_FLOW f " +
                    "JOIN IDN_FLOW_NODE n ON f.ID = n.FLOW_ID " +
                    "LEFT JOIN IDN_FLOW_NODE_EXECUTOR ne ON n.ID = ne.FLOW_NODE_ID " +
                    "LEFT JOIN IDN_FLOW_NODE_MAPPING nm ON n.ID = nm.FLOW_NODE_ID " +
                    "LEFT JOIN IDN_FLOW_NODE nextNode ON nm.NEXT_NODE_ID = nextNode.ID " +
                    "WHERE f.TENANT_ID = ? AND f.IS_DEFAULT = ? AND f.TYPE = ? " +
                    "ORDER BY n.NODE_ID";

    public static final String GET_VIEW_PAGES_IN_FLOW =
            "SELECT n.NODE_ID, p.STEP_ID, p.PAGE_CONTENT FROM IDN_FLOW_PAGE p " +
                    "JOIN IDN_FLOW_NODE n ON p.FLOW_NODE_ID = n.ID WHERE p.FLOW_ID = ? AND p.TYPE = ?";

    public static final String GET_FIRST_STEP_ID = "SELECT fp.STEP_ID FROM IDN_FLOW_PAGE fp JOIN IDN_FLOW_NODE fn" +
            " ON fp.FLOW_NODE_ID = fn.ID JOIN IDN_FLOW f ON fn.FLOW_ID = f.ID WHERE fn.IS_FIRST_NODE = ? AND" +
            " f.TENANT_ID = ? AND f.TYPE = ?";

    public static final String GET_NODE_EXECUTOR_META =
            "SELECT METADATA_NAME, METADATA_VALUE FROM IDN_FLOW_NODE_EXECUTOR_META WHERE EXECUTOR_ID = ?";

    private SQLConstants() {

    }

    /**
     * SQL Placeholders.
     */
    public static final class SQLPlaceholders {
        
        public static final String DB_SCHEMA_COLUMN_NAME_STEP_ID = "STEP_ID";
        public static final String DB_SCHEMA_COLUMN_NAME_PAGE_CONTENT = "PAGE_CONTENT";
        public static final String DB_SCHEMA_COLUMN_NAME_PAGE_TYPE = "PAGE_TYPE";
        public static final String DB_SCHEMA_COLUMN_NAME_COORDINATE_X = "COORDINATE_X";
        public static final String DB_SCHEMA_COLUMN_NAME_COORDINATE_Y = "COORDINATE_Y";
        public static final String DB_SCHEMA_COLUMN_NAME_HEIGHT = "HEIGHT";
        public static final String DB_SCHEMA_COLUMN_NAME_WIDTH = "WIDTH";
        public static final String DB_SCHEMA_COLUMN_NAME_NODE_ID = "NODE_ID";
        public static final String DB_SCHEMA_COLUMN_NAME_NODE_TYPE = "NODE_TYPE";
        public static final String DB_SCHEMA_COLUMN_NAME_IS_FIRST_NODE = "IS_FIRST_NODE";
        public static final String DB_SCHEMA_COLUMN_NAME_TRIGGERING_ELEMENT = "TRIGGERING_ELEMENT";
        public static final String DB_SCHEMA_COLUMN_NAME_EXECUTOR_NAME = "EXECUTOR_NAME";
        public static final String DB_SCHEMA_COLUMN_NAME_EXECUTOR_ID = "EXECUTOR_ID";
        public static final String DB_SCHEMA_COLUMN_NAME_METADATA_NAME = "METADATA_NAME";
        public static final String DB_SCHEMA_COLUMN_NAME_METADATA_VALUE = "METADATA_VALUE";

        public static final String DB_SCHEMA_ALIAS_FLOW_ID = "FLOW_ID";
        public static final String DB_SCHEMA_ALIAS_NEXT_NODE_ID = "NEXT_NODE_ACTUAL_ID";

        private SQLPlaceholders() {

        }
    }
}
