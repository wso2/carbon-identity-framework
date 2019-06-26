/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.user.store.configuration;

/**
 * This class contains database queries related to userstore CRUD operations.
 */
public class UserStoreMgtDBQueries {

    public static final String STORE_USERSTORE_PROPERTIES = "INSERT INTO IDN_ARTIFACT_STORE (ID,TENANT_ID,ARTIFACT,IDENTIFIER,CONTENT_TYPE,ARTIFACT_TYPE)" +
            " VALUES (?,?,?,?,?,?)";
    //To update the user store domain name.
    public static final String UPDATE_USERSTORE_PROPERTIES = "UPDATE IDN_ARTIFACT_STORE SET ARTIFACT= ?,IDENTIFIER= ? WHERE IDENTIFIER = ? AND TENANT_ID = ? AND ARTIFACT_TYPE = ?";
    public static final String GET_USERSTORE_PROPERTIES = "SELECT ARTIFACT FROM IDN_ARTIFACT_STORE WHERE IDENTIFIER=? AND TENANT_ID = ? AND ARTIFACT_TYPE= ?";
    public static final String GET_All_USERSTORE_PROPERTIES = "SELECT IDENTIFIER, ARTIFACT FROM IDN_ARTIFACT_STORE WHERE TENANT_ID = ? AND ARTIFACT_TYPE= ?";
    public static final String DELETE_USERSTORE_PROPERTIES = "DELETE FROM IDN_ARTIFACT_STORE WHERE IDENTIFIER = ? AND TENANT_ID = ? AND ARTIFACT_TYPE= ?";

}
