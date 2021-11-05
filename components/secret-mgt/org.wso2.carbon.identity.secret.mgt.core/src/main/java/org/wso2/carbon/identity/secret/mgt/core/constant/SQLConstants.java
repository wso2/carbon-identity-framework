/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com).
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

package org.wso2.carbon.identity.secret.mgt.core.constant;

/**
 * This class contains database queries related to secret management CRUD operations.
 */
public class SQLConstants {

    public static final String INSERT_SECRET = "INSERT INTO IDN_SECRET( ID, TENANT_ID, SECRET_NAME, SECRET_VALUE, " +
            "CREATED_TIME, LAST_MODIFIED, TYPE_ID, DESCRIPTION ) VALUES(:ID; ,:TENANT_ID; ,:SECRET_NAME; , " +
            ":SECRET_VALUE; , :CREATED_TIME; , :LAST_MODIFIED; , :TYPE_ID; , :DESCRIPTION;)";

    public static final String UPDATE_SECRET = "UPDATE IDN_SECRET SET SECRET_VALUE = :SECRET_VALUE; , LAST_MODIFIED " +
            "= :LAST_MODIFIED; , TYPE_ID = :TYPE_ID; , DESCRIPTION = :DESCRIPTION; WHERE ID = :ID;";

    public static final String GET_SECRET_BY_NAME = "SELECT ID,TENANT_ID,SECRET_NAME,SECRET_VALUE,CREATED_TIME," +
            "LAST_MODIFIED,TYPE_ID,DESCRIPTION FROM IDN_SECRET WHERE SECRET_NAME = :SECRET_NAME; AND TYPE_ID = " +
            ":TYPE_ID; AND TENANT_ID = :TENANT_ID;";

    public static final String GET_SECRET_NAME_BY_ID = "SELECT SECRET_NAME FROM IDN_SECRET WHERE ID = :ID; " +
            "AND TENANT_ID = :TENANT_ID;";

    public static final String GET_SECRET_BY_ID = "SELECT ID,TENANT_ID,SECRET_NAME,SECRET_VALUE,CREATED_TIME," +
            "LAST_MODIFIED,TYPE_ID,DESCRIPTION FROM IDN_SECRET WHERE ID = :ID; AND TENANT_ID = :TENANT_ID;";

    public static final String DELETE_SECRET_BY_ID = "DELETE FROM IDN_SECRET WHERE ID = :ID; AND TENANT_ID = " +
            ":TENANT_ID;";

    public static final String GET_SECRETS = "SELECT ID, TENANT_ID, SECRET_NAME, LAST_MODIFIED, CREATED_TIME, " +
            "DESCRIPTION FROM IDN_SECRET WHERE TYPE_ID = :TYPE_ID; AND TENANT_ID = :TENANT_ID;";

    public static final String UPDATE_SECRET_VALUE = "UPDATE IDN_SECRET SET SECRET_VALUE = :SECRET_VALUE; , " +
            "LAST_MODIFIED = :LAST_MODIFIED; WHERE ID = :ID;";
}
