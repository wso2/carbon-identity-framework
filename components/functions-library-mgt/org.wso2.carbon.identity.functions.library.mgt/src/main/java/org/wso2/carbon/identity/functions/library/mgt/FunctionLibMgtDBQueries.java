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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.functions.library.mgt;

/**
 * This class contains default SQL queries.
 */
public class FunctionLibMgtDBQueries {

    public static final String STORE_FUNCTIONLIB_INFO =
            "INSERT INTO IDN_FUNCTION_LIBRARY (NAME, DESCRIPTION, TYPE, TENANT_ID, DATA) VALUES (?,?,?,?,?)";

    public static final String UPDATE_FUNCTIONLIB_INFO =
            "UPDATE IDN_FUNCTION_LIBRARY SET NAME = ?, DESCRIPTION = ?, DATA = ? WHERE TENANT_ID = ? AND NAME = ?";

    public static final String LOAD_FUNCTIONLIB_FROM_TENANTID =
            "SELECT NAME,DESCRIPTION,TYPE,DATA FROM IDN_FUNCTION_LIBRARY WHERE TENANT_ID = ?";

    public static final String LOAD_FUNCTIONLIB_FROM_TENANTID_AND_NAME =
            "SELECT NAME,DESCRIPTION,TYPE,DATA FROM IDN_FUNCTION_LIBRARY WHERE TENANT_ID = ? AND NAME = ?";

    public static final String REMOVE_FUNCTIONLIB =
            "DELETE FROM IDN_FUNCTION_LIBRARY WHERE TENANT_ID = ? AND NAME = ?";
}
