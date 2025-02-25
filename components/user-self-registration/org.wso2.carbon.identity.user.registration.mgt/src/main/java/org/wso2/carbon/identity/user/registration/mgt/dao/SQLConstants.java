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

package org.wso2.carbon.identity.user.registration.mgt.dao;

/**
 * This class holds the database queries and constants for the flow management DAO layer.
 */
public class SQLConstants {

    public static final String DELETE_FLOW =
            "DELETE FROM IDN_FLOW WHERE TENANT_ID = ? AND IS_DEFAULT = TRUE AND TYPE = ?";

    public static final String INSERT_FLOW_INTO_IDN_FLOW =
            "INSERT INTO IDN_FLOW (ID, TENANT_ID, FLOW_NAME, TYPE, IS_DEFAULT) VALUES (?, ?, ?, ?, ?)";

    public static final String INSERT_FLOW_NODE_INFO =
            "INSERT INTO IDN_FLOW_NODE (NODE_ID, FLOW_ID, NODE_TYPE, IS_FIRST_NODE) VALUES (?, ?, ?, ?)";

    public static final String INSERT_NODE_EXECUTOR_INFO =
            "INSERT INTO IDN_FLOW_NODE_EXECUTOR (FLOW_NODE_ID, EXECUTOR_NAME, IDP_NAME) VALUES (?, ?, ?)";

    public static final String INSERT_NODE_EDGES =
            "INSERT INTO IDN_FLOW_NODE_MAPPING (FLOW_NODE_ID, NEXT_NODE_ID, TRIGGERING_ELEMENT) VALUES (?, ?, ?)";

    public static final String INSERT_FLOW_PAGE_INFO =
            "INSERT INTO IDN_FLOW_PAGE (FLOW_ID, FLOW_NODE_ID, STEP_ID, PAGE_CONTENT, TYPE) VALUES (?, ?, ?, ?, ?)";

    public static final String INSERT_FLOW_PAGE_META =
            "INSERT INTO IDN_FLOW_PAGE_META (PAGE_ID, COORDINATE_X, COORDINATE_Y, HEIGHT, WIDTH) VALUES (?, ?, ?, ?, " +
                    "?)";

    public static final String GET_FLOW =
            "SELECT P.ID AS PAGE_ID, P.STEP_ID, P.PAGE_CONTENT, P.TYPE AS PAGE_TYPE, M.COORDINATE_X, M" +
                    ".COORDINATE_Y, M.HEIGHT, M.WIDTH FROM IDN_FLOW F JOIN IDN_FLOW_PAGE P ON F.ID = P.FLOW_ID " +
                    "LEFT JOIN IDN_FLOW_PAGE_META M ON P.ID = M.PAGE_ID WHERE F.TENANT_ID = ? AND F.IS_DEFAULT = " +
                    "TRUE AND F.TYPE = 'REGISTRATION';";

    /**
     * SQL Placeholders.
     */
    public static final class SQLPlaceholders {

        public static final String REGISTRATION_FLOW = "REGISTRATION";
        public static final String STEP_ID = "STEP_ID";
        public static final String PAGE_CONTENT = "PAGE_CONTENT";
        public static final String PAGE_TYPE = "PAGE_TYPE";
        public static final String COORDINATE_X = "COORDINATE_X";
        public static final String COORDINATE_Y = "COORDINATE_Y";
        public static final String HEIGHT = "HEIGHT";
        public static final String WIDTH = "WIDTH";

        private SQLPlaceholders() {

        }
    }
}
