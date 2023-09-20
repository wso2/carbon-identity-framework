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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.role.mgt.core.v2.dao;

import org.wso2.carbon.identity.role.mgt.core.dao.RoleDAOImpl;

/**
 * SQL Queries used in {@link RoleDAOImpl}.
 */
public class SQLQueries {

    public static final String ADD_ROLE_WITH_AUDIENCE_SQL = "INSERT INTO UM_HYBRID_ROLE (UM_ROLE_NAME, APP_ID, " +
            "UM_TENANT_ID) VALUES (:UM_ROLE_NAME;, :APP_ID;, :UM_TENANT_ID;)";

    public static final String ADD_ROLE_AUDIENCE_SQL = "INSERT INTO UM_HYBRID_ROLE_AUDIENCE (AUDIENCE,AUDIENCE_ID) " +
            "VALUES (:AUDIENCE;, :AUDIENCE_ID;)";
    public static final String GET_ROLE_AUDIENCE_SQL = "SELECT ID FROM UM_HYBRID_ROLE_AUDIENCE WHERE AUDIENCE " +
            "=:AUDIENCE; AND AUDIENCE_ID=:AUDIENCE_ID;";
    public static final String GET_ROLE_AUDIENCE_BY_ID_SQL = "SELECT AUDIENCE, AUDIENCE_ID FROM UM_HYBRID_ROLE_AUDIENCE " +
            "WHERE ID =:AUDIENCE_ID;";

    public static final String GET_ROLE_AUDIENCE_BY_ROLE_NAME_SQL = "SELECT AUDIENCE, AUDIENCE_ID FROM " +
            "UM_HYBRID_ROLE as r INNER JOIN UM_HYBRID_ROLE_AUDIENCE as ra ON r.AUDIENCE_ID = ra.ID  " +
            "WHERE r.ROLE_NAME =:ROLE_NAME AND r.TENANT_ID;";
    public static final String ADD_ROLE_SCOPE_SQL =
            "INSERT INTO ROLE_SCOPE (ROLE_ID, SCOPE_NAME, TENANT_ID) VALUES (:ROLE_ID;, "
                    + ":SCOPE_NAME;, :TENANT_ID;)";

    public static final String GET_APP_NAME_BY_APP_ID = "SELECT APP_NAME FROM SP_APP WHERE UUID = :APP_ID;";


    // DB queries to list roles.
    public static final String GET_ROLES_BY_TENANT_MYSQL = "SELECT UM_ROLE_NAME, AUDIENCE_ID FROM UM_HYBRID_ROLE WHERE "
            + "UM_TENANT_ID=:UM_TENANT_ID; ORDER BY UM_ID DESC LIMIT :OFFSET;, :LIMIT;";

    public static final String GET_ROLES_BY_TENANT_ORACLE = "SELECT UM_ROLE_NAME, AUDIENCE_ID FROM " +
            "(SELECT UM_ROLE_NAME, AUDIENCE_ID, rownum AS rnum FROM (SELECT UM_ROLE_NAME, AUDIENCE_ID FROM " +
            "UM_HYBRID_ROLE WHERE UM_TENANT_ID=:UM_TENANT_ID; ORDER BY UM_ID DESC) WHERE rownum <= :END_INDEX;) " +
            "WHERE rnum > :ZERO_BASED_START_INDEX;";

    public static final String GET_ROLES_BY_TENANT_MSSQL = "SELECT UM_ROLE_NAME, AUDIENCE_ID FROM UM_HYBRID_ROLE WHERE "
            + "UM_TENANT_ID=:UM_TENANT_ID; ORDER BY UM_ID DESC OFFSET :OFFSET; ROWS FETCH NEXT :LIMIT; ROWS ONLY";

    public static final String GET_ROLES_BY_TENANT_POSTGRESQL = "SELECT UM_ROLE_NAME, AUDIENCE_ID FROM " +
            "UM_HYBRID_ROLE WHERE UM_TENANT_ID=:UM_TENANT_ID; ORDER BY UM_ID DESC LIMIT :LIMIT; OFFSET :OFFSET;";

    public static final String GET_ROLES_BY_TENANT_DB2 = "SELECT UM_ROLE_NAME, AUDIENCE_ID FROM " +
            "(SELECT ROW_NUMBER() OVER(ORDER BY UM_ID DESC) AS rn,UM_HYBRID_ROLE.* FROM UM_HYBRID_ROLE " +
            "WHERE UM_TENANT_ID=:UM_TENANT_ID;)WHERE rn BETWEEN :ONE_BASED_START_INDEX; AND :END_INDEX;";

    public static final String GET_ROLES_BY_TENANT_INFORMIX = "SELECT SKIP :OFFSET; FIRST "
            + ":LIMIT; UM_ROLE_NAME, AUDIENCE_ID FROM UM_HYBRID_ROLE WHERE UM_TENANT_ID=:UM_TENANT_ID; " +
            "ORDER BY UM_ID DESC";

    // DB queries to filter roles.
    public static final String GET_ROLES_BY_TENANT_AND_ROLE_NAME_MYSQL = "SELECT UM_ROLE_NAME, AUDIENCE_ID FROM " +
            "UM_HYBRID_ROLE WHERE UM_TENANT_ID=:UM_TENANT_ID; AND UM_ROLE_NAME LIKE :UM_ROLE_NAME; ORDER BY UM_ID " +
            "DESC LIMIT :OFFSET;, :LIMIT;";

    public static final String GET_ROLES_BY_TENANT_AND_ROLE_NAME_ORACLE =
            "SELECT UM_ROLE_NAME, AUDIENCE_ID FROM (SELECT UM_ROLE_NAME, rownum AS "
                    + "rnum FROM (SELECT UM_ROLE_NAME, AUDIENCE_ID FROM UM_HYBRID_ROLE WHERE" +
                    " UM_TENANT_ID=:UM_TENANT_ID; ORDER BY UM_ID DESC) WHERE UM_ROLE_NAME LIKE :UM_ROLE_NAME; " +
                    "AND rownum <= :END_INDEX;) WHERE rnum > :ZERO_BASED_START_INDEX;";

    public static final String GET_ROLES_BY_TENANT_AND_ROLE_NAME_MSSQL =
            "SELECT UM_ROLE_NAME, AUDIENCE_ID FROM UM_HYBRID_ROLE WHERE "
                    + "UM_TENANT_ID=:UM_TENANT_ID; AND UM_ROLE_NAME LIKE :UM_ROLE_NAME; ORDER BY UM_ID DESC OFFSET "
                    + ":OFFSET; ROWS FETCH NEXT :LIMIT; ROWS ONLY";

    public static final String GET_ROLES_BY_TENANT_AND_ROLE_NAME_POSTGRESQL =
            "SELECT UM_ROLE_NAME, AUDIENCE_ID FROM UM_HYBRID_ROLE WHERE "
                    + "UM_TENANT_ID=:UM_TENANT_ID; AND UM_ROLE_NAME LIKE :UM_ROLE_NAME; ORDER BY UM_ID DESC LIMIT "
                    + ":LIMIT; OFFSET :OFFSET;";

    public static final String GET_ROLES_BY_TENANT_AND_ROLE_NAME_DB2 =
            "SELECT UM_ROLE_NAME, AUDIENCE_ID FROM (SELECT ROW_NUMBER() OVER(ORDER"
                    + " BY UM_ID DESC) AS rn,UM_HYBRID_ROLE.* FROM UM_HYBRID_ROLE WHERE UM_TENANT_ID=:UM_TENANT_ID; "
                    + "AND UM_ROLE_NAME LIKE :UM_ROLE_NAME;)WHERE rn BETWEEN :ONE_BASED_START_INDEX; AND :END_INDEX;";

    public static final String GET_ROLES_BY_TENANT_AND_ROLE_NAME_INFORMIX = "SELECT SKIP :OFFSET; FIRST "
            + ":LIMIT; UM_ROLE_NAME, AUDIENCE_ID FROM UM_HYBRID_ROLE WHERE UM_TENANT_ID=:UM_TENANT_ID; AND UM_ROLE_NAME " +
            "LIKE :UM_ROLE_NAME; ORDER BY UM_ID DESC";

}
