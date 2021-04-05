/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.store.impl.rdbms;

public class rdbmsConstants {

    // Identity file configuration constants.
    public static final String PERSIST_ENABLE =
            "JDBCPersistenceManager.SessionDataPersist.Enable";
    public static final String SESSION_DATA_CLEANUP_ENABLE =
            "JDBCPersistenceManager.SessionDataPersist.SessionDataCleanUp.Enable";

    // RDBMS implementation constants.
    public static final String GET_TEMP_POOL_SIZE = "JDBCPersistenceManager.SessionDataPersist.TempDataCleanup.PoolSize";
    public static final String GET_TEMP_CLEANUP_ENABLE = "JDBCPersistenceManager.SessionDataPersist.TempDataCleanup.Enable";
    public static final String GET_DELETE_CHUNK_SIZE =
            "JDBCPersistenceManager.SessionDataPersist.SessionDataCleanUp.DeleteChunkSize";
    public static final String GET_POOL_SIZE =
            "JDBCPersistenceManager.SessionDataPersist.PoolSize";
    public static final int DEFAULT_MAX_TEMP_DATA_POOLSIZE = 50;
    public static final boolean DEFAULT_TEMP_DATA_CLEANUP_ENABLED = false;
    public static final int DEFAULT_DETELE_CHUNK_SIZE = 50000;
    public static int DEFAULT_MAX_SESSION_DATA_POOLSIZE = 100;
    public static boolean DEFAULT_SESSION_DATA_CLEANUP_ENABLED = true;
    public static boolean DEFAULT_ENABLE_PERSIST = true;

}
