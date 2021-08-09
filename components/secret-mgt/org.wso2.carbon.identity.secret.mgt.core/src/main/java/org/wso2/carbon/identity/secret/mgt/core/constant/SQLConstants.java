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

    public static final String INSERT_SECRET = "INSERT INTO IDN_SECRET( ID, TENANT_ID, NAME, VALUE, CREATED_TIME, " +
            "LAST_MODIFIED ) VALUES(:ID; ,:TENANT_ID; ,:NAME; , :VALUE; , :CREATED_TIME; , :LAST_MODIFIED;)";

    public static final String UPDATE_SECRET = "UPDATE IDN_SECRET SET NAME = :NAME; ,VALUE = :VALUE; , LAST_MODIFIED " +
            "= :LAST_MODIFIED; " +
            " WHERE ID = :ID;";

    public static final String GET_SECRET_BY_NAME = "SELECT ID,TENANT_ID,NAME,VALUE,CREATED_TIME, LAST_MODIFIED FROM " +
            "IDN_SECRET WHERE NAME = :NAME; AND TENANT_ID = :TENANT_ID;";

    public static final String GET_SECRET_NAME_BY_ID = "SELECT NAME FROM IDN_SECRET WHERE ID = :ID; AND TENANT_ID =" +
            " :TENANT_ID;";

    public static final String GET_SECRET_BY_ID = "SELECT ID,TENANT_ID,NAME,VALUE,CREATED_TIME, LAST_MODIFIED FROM" +
            " IDN_SECRET WHERE ID = :ID; AND TENANT_ID = :TENANT_ID;";

    public static final String GET_SECRET_CREATED_TIME_BY_NAME = "SELECT CREATED_TIME FROM IDN_SECRET " +
            "WHERE NAME = :NAME; AND TENANT_ID = :TENANT_ID;";

    public static final String DELETE_SECRET = "DELETE FROM IDN_SECRET WHERE NAME = :NAME; AND TENANT_ID = :TENANT_ID;";

    public static final String DELETE_SECRET_BY_ID = "DELETE FROM IDN_SECRET WHERE ID = :ID; AND TENANT_ID = " +
            ":TENANT_ID;";

    public static final String GET_SECRETS =
            "SELECT ID, TENANT_ID, NAME, LAST_MODIFIED, CREATED_TIME FROM IDN_SECRET WHERE TENANT_ID = :TENANT_ID;";
}
