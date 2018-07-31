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

package org.wso2.carbon.identity.remotefetch.core.constants;

/**
 * Constant Queries for RemoteFetch Core
 */
public class SQLConstants {
    public static final String CREATE_REVISION = "INSERT IDN_REMOTE_FETCH_REVISIONS (CONFIG_ID, FILE_PATH, FILE_HASH," +
            " ITEM_NAME) VALUES(?,?,?,?);";

    public static final String UPDATE_REVISION = "UPDATE IDN_REMOTE_FETCH_REVISIONS SET CONFIG_ID = ?, FILE_PATH = ?," +
            " FILE_HASH = ?, DEPLOYED_DATE = ?, DEPLOYMENT_STATUS = ?, ITEM_NAME = ? WHERE ID = ?";

    public static final String DELETE_REVISION = "DELETE FROM IDN_REMOTE_FETCH_REVISIONS WHERE ID = ?";

    public static final String GET_REVISIONS_BY_CONFIG = "SELECT ID, CONFIG_ID, FILE_PATH, FILE_HASH, DEPLOYED_DATE," +
            " DEPLOYMENT_STATUS, ITEM_NAME FROM IDN_REMOTE_FETCH_REVISIONS WHERE CONFIG_ID = ?";

    public static final String GET_REVISION_BY_UNIQUE = "SELECT ID, CONFIG_ID, FILE_PATH, FILE_HASH, DEPLOYED_DATE," +
            " DEPLOYMENT_STATUS, ITEM_NAME FROM IDN_REMOTE_FETCH_REVISIONS WHERE CONFIG_ID = ? AND ITEM_NAME = ?";

    public static final String CREATE_CONFIG = "INSERT IDN_REMOTE_FETCH_CONFIG (TENANT_ID, USER_NAME, REPO_MANAGER_TYPE, " +
            "ACTION_LISTENER_TYPE, CONFIG_DEPLOYER_TYPE, ATTRIBUTES_JSON) VALUES (?,?,?,?,?,?)";

    public static final String LIST_CONFIGS = "SELECT ID, TENANT_ID, USER_NAME, REPO_MANAGER_TYPE, " +
            "ACTION_LISTENER_TYPE, CONFIG_DEPLOYER_TYPE, ATTRIBUTES_JSON FROM `IDN_REMOTE_FETCH_CONFIG`";

    public static final String GET_CONFIG = "SELECT ID, TENANT_ID, USER_NAME, REPO_MANAGER_TYPE," +
            " ACTION_LISTENER_TYPE, CONFIG_DEPLOYER_TYPE, ATTRIBUTES_JSON FROM `IDN_REMOTE_FETCH_CONFIG` WHERE ID = ?";

    public static final String GET_CONFIG_BY_UNIQUE = "SELECT ID, TENANT_ID, USER_NAME, REPO_MANAGER_TYPE," +
            " ACTION_LISTENER_TYPE, CONFIG_DEPLOYER_TYPE, ATTRIBUTES_JSON FROM `IDN_REMOTE_FETCH_CONFIG` WHERE " +
            "TENANT_ID = ? AND REPO_MANAGER_TYPE = ? AND CONFIG_DEPLOYER_TYPE = ?";

    public static final String UPDATE_CONFIG = "UPDATE IDN_REMOTE_FETCH_CONFIG SET TENANT_ID = ?, USER_NAME = ?," +
            " REPO_MANAGER_TYPE = ?, ACTION_LISTENER_TYPE = ?, CONFIG_DEPLOYER_TYPE = ?, ATTRIBUTES_JSON = ? " +
            "WHERE ID = ?";

    public static final String DELETE_CONFIG = "DELETE FROM IDN_REMOTE_FETCH_CONFIG WHERE ID = ?";
}
