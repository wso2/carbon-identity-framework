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

package org.wso2.carbon.identity.application.authentication.framework.store.impl.redis;

public class RedisConstants {

    public static final String GET_REDIS_POOL_MAX_TOTAL =
            "JDBCPersistenceManager.SessionDataPersist.RedisPoolConfig.MaxTotal";
    public static final String GET_REDIS_POOL_MAX_IDLE =
            "JDBCPersistenceManager.SessionDataPersist.RedisPoolConfig.MaxIdle";
    public static final String GET_REDIS_POOL_MIN_IDLE =
            "JDBCPersistenceManager.SessionDataPersist.RedisPoolConfig.MinIdle";
    public static final String GET_REDIS_POOL_MAX_WAIT =
            "JDBCPersistenceManager.SessionDataPersist.RedisPoolConfig.MaxWaitMills";
    public static final String GET_TEMP_DATA_CLEANUP_ENABLE =
            "JDBCPersistenceManager.SessionDataPersist.TempDataCleanup.Enable";
    public static final String GET_POOL_SIZE =
            "JDBCPersistenceManager.SessionDataPersist.PoolSize";
    // Redis Constants.
    public static final String NANO_TIME = "nanoTime";
    public static final String TYPE = "type";
    public static final String EXPIRY_TIME = "expireTime";
    public static final String TENANT_ID = "tenantId";
    public static final String OBJECT = "object";
    public static final String DIVIDER = ":";
    public static final String TEMPSTORE = "temp";
    // General Persist layer constants.
    public static boolean DEFAULT_TEMPDATA_CLEANUP_ENABLED = false;
    public static int DEFAULT_MAX_SESSION_DATA_POOLSIZE = 100;
    public static boolean DEFAULT_ENABLE_PERSIST = true;
    // Redis pool configuration constants.
    public static int DEFAULT_MAX_TOTAL = 1000;
    public static int DEFAULT_MAX_IDLE = 1000;
    public static int DEFAULT_MIN_IDLE = 100;
    public static int DEFAULT_MAX_WAIT_MILLIS = 20000;

}
