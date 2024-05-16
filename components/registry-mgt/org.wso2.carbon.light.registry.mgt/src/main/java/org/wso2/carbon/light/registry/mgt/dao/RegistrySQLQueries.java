/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.light.registry.mgt.dao;

public class RegistrySQLQueries {

    private RegistrySQLQueries() {

    }

    public static final String GET_PATH_ID =
            "SELECT REG_PATH_ID FROM REG_PATH WHERE REG_PATH_VALUE = :PATH; AND REG_TENANT_ID = :TENANT_ID;";
    public static final String GET_COLLECTION_RESOURCE_PROPERTIES =
            "SELECT REG_NAME, REG_VALUE FROM REG_PROPERTY P, REG_RESOURCE_PROPERTY RP WHERE P.REG_ID=RP" +
                    ".REG_PROPERTY_ID AND RP.REG_PATH_ID = :REG_PATH_ID; AND RP.REG_RESOURCE_NAME IS NULL AND P" +
                    ".REG_TENANT_ID=RP.REG_TENANT_ID AND RP.REG_TENANT_ID = :TENANT_ID; ORDER BY P.REG_ID";
    public static final String GET_FILE_RESOURCE_PROPERTIES =
            "SELECT REG_NAME, REG_VALUE FROM REG_PROPERTY P, REG_RESOURCE_PROPERTY RP WHERE P.REG_ID=RP" +
                    ".REG_PROPERTY_ID AND RP.REG_PATH_ID = :REG_PATH_ID; AND RP.REG_RESOURCE_NAME = :REG_NAME; AND P" +
                    ".REG_TENANT_ID=RP.REG_TENANT_ID AND RP.REG_TENANT_ID = :TENANT_ID; ORDER BY P.REG_ID";
    public static final String GET_COLLECTION_CHILD_RESOURCES =
            "SELECT REG_NAME FROM REG_RESOURCE WHERE REG_PATH_ID = :REG_PATH_ID; AND REG_TENANT_ID = :TENANT_ID; AND" +
                    " REG_NAME IS NOT NULL;";
    public static final String GET_COLLECTION_CHILD_PATHS =
            "SELECT P.REG_PATH_ID, P.REG_PATH_VALUE FROM REG_PATH P, REG_RESOURCE R WHERE P.REG_PATH_PARENT_ID = " +
                    ":PATH_PARENT_ID; AND P.REG_TENANT_ID = :TENANT_ID; AND R.REG_PATH_ID=P.REG_PATH_ID AND R" +
                    ".REG_NAME IS NULL AND R.REG_TENANT_ID = :TENANT_ID;";
    public static final String IS_FILE_RESOURCE_EXISTS =
            "SELECT COUNT(*) FROM REG_RESOURCE WHERE REG_PATH_ID = :REG_PATH_ID; AND REG_TENANT_ID = :TENANT_ID; AND" +
                    " REG_NAME = :REG_NAME;";
    public static final String IS_COLLECTION_RESOURCE_EXISTS =
            "SELECT COUNT(*) FROM REG_RESOURCE WHERE REG_PATH_ID = :REG_PATH_ID; AND REG_TENANT_ID = :TENANT_ID; AND" +
                    " REG_NAME IS NULL;";
    public static final String ADD_PATH =
            "INSERT INTO REG_PATH (REG_PATH_VALUE, REG_PATH_PARENT_ID, REG_TENANT_ID) VALUES (:PATH;, " +
                    ":PATH_PARENT_ID;, :TENANT_ID;);";
    public static final String ADD_RESOURCE =
            "INSERT INTO REG_RESOURCE (REG_PATH_ID, REG_NAME, REG_MEDIA_TYPE, REG_CREATOR, REG_CREATED_TIME, " +
                    "REG_LAST_UPDATOR, REG_LAST_UPDATED_TIME, REG_DESCRIPTION, REG_TENANT_ID, REG_UUID) VALUES " +
                    "(:REG_PATH_ID;, :REG_NAME;, :REG_MEDIA_TYPE;, :REG_CREATOR;, :REG_CREATED_TIME;, " +
                    ":REG_LAST_UPDATOR;, :REG_LAST_UPDATED_TIME;, :REG_DESCRIPTION;, :TENANT_ID;, :REG_UUID;);";
    public static final String ADD_PROPERTY =
            "INSERT INTO REG_PROPERTY (REG_NAME, REG_VALUE, REG_TENANT_ID) VALUES (:REG_NAME;, :REG_VALUE;, " +
                    ":TENANT_ID;);";
    public static final String ADD_RESOURCE_PROPERTY =
            "INSERT INTO REG_RESOURCE_PROPERTY (REG_PROPERTY_ID, REG_PATH_ID, REG_RESOURCE_NAME, REG_TENANT_ID) " +
                    "VALUES (:REG_PROPERTY_ID;, :REG_PATH_ID;, :REG_NAME;, :TENANT_ID;);";
    public static final String ADD_CONTENT =
            "INSERT INTO REG_CONTENT (REG_CONTENT_DATA, REG_TENANT_ID) VALUES (:REG_CONTENT_DATA;, :TENANT_ID;);";
    public static final String UPDATE_RESOURCE =
            "UPDATE REG_RESOURCE SET REG_CONTENT_ID= :REG_CONTENT_ID; WHERE REG_PATH_ID = :REG_PATH_ID; AND " +
                    "REG_TENANT_ID= :TENANT_ID; AND REG_NAME= :REG_NAME;";
    public static final String DELETE_FILE_RESOURCE =
            "DELETE FROM REG_RESOURCE WHERE REG_PATH_ID= :REG_PATH_ID; AND REG_NAME= :REG_NAME; AND REG_TENANT_ID= " +
                    ":TENANT_ID;";
    public static final String DELETE_COLLECTION_RESOURCE =
            "DELETE FROM REG_RESOURCE WHERE REG_PATH_ID= :REG_PATH_ID; AND REG_NAME IS NULL AND REG_TENANT_ID= " +
                    ":TENANT_ID;";
    public static final String DELETE_CONTENT =
            "DELETE FROM REG_CONTENT WHERE REG_CONTENT_ID= :REG_CONTENT_ID; AND REG_TENANT_ID= :TENANT_ID;";
    public static final String GET_FILE_RESOURCE_PROPERTY_ID =
            "SELECT REG_PROPERTY_ID FROM REG_RESOURCE_PROPERTY WHERE REG_PATH_ID= :REG_PATH_ID; AND " +
                    "REG_RESOURCE_NAME=:REG_NAME; AND REG_TENANT_ID= :TENANT_ID;";
    public static final String GET_COLLECTION_RESOURCE_PROPERTY_ID =
            "SELECT REG_PROPERTY_ID FROM REG_RESOURCE_PROPERTY WHERE REG_PATH_ID= :REG_PATH_ID; AND " +
                    "REG_RESOURCE_NAME IS NULL AND REG_TENANT_ID= :TENANT_ID;";
    public static final String DELETE_RESOURCE_PROPERTY =
            "DELETE FROM REG_RESOURCE_PROPERTY WHERE REG_PROPERTY_ID=:REG_PROPERTY_ID; AND REG_TENANT_ID= :TENANT_ID;";
    public static final String DELETE_PROPERTY =
            "DELETE FROM REG_PROPERTY WHERE REG_ID=:REG_ID; AND REG_TENANT_ID= :TENANT_ID;";
    public static final String GET_COLLECTION_RESOURCE =
            "SELECT REG_MEDIA_TYPE, REG_CREATOR, REG_CREATED_TIME, REG_LAST_UPDATOR, REG_LAST_UPDATED_TIME," +
                    " REG_VERSION, REG_DESCRIPTION, REG_CONTENT_ID, REG_UUID FROM REG_RESOURCE WHERE" +
                    " REG_PATH_ID= :REG_PATH_ID; AND REG_NAME IS NULL AND REG_TENANT_ID= :TENANT_ID;";
    public static final String GET_FILE_RESOURCE =
            "SELECT REG_MEDIA_TYPE, REG_CREATOR, REG_CREATED_TIME, REG_LAST_UPDATOR, REG_LAST_UPDATED_TIME," +
                    " REG_VERSION, REG_DESCRIPTION, REG_CONTENT_ID, REG_UUID FROM REG_RESOURCE WHERE" +
                    " REG_PATH_ID= :REG_PATH_ID; AND REG_NAME = :REG_NAME; AND REG_TENANT_ID= :TENANT_ID;";
    public static final String GET_REG_CONTENT =
            "SELECT REG_CONTENT_DATA  FROM  REG_CONTENT WHERE REG_CONTENT_ID = :REG_CONTENT_ID; " +
                    "AND REG_TENANT_ID= :TENANT_ID;";
}
