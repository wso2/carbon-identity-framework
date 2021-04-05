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

import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.wso2.carbon.identity.application.authentication.framework.store.SessionContextDO;
import org.wso2.carbon.identity.application.authentication.framework.store.SessionDataPersistTask;
import org.wso2.carbon.identity.application.authentication.framework.store.SessionDataStore;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.common.cache.CacheEntry;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;

import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RedisSessionDataStore extends SessionDataStore {

    private static final Log log = LogFactory.getLog(SessionDataStore.class);

    private static int maxSessionDataPoolSize;
    private static JedisPool pool;
    private int maxTotal;
    private int maxIdle;
    private int minIdle;
    private int maxWaitMillis;
    private boolean enablePersist;
    private boolean tempDataCleanupEnabled;

    {
        try {
            maxSessionDataPoolSize = getIntegerPropertyFromIdentityUtil(redisConstants.GET_POOL_SIZE,
                    redisConstants.DEFAULT_MAX_SESSION_DATA_POOLSIZE);
            tempDataCleanupEnabled = getBooleanPropertyFromIdentityUtil(redisConstants.GET_TEMP_DATA_CLEANUP_ENABLE,
                    redisConstants.DEFAULT_TEMPDATA_CLEANUP_ENABLED);
            maxTotal = getIntegerPropertyFromIdentityUtil(redisConstants.GET_REDIS_POOL_MAX_TOTAL,
                    redisConstants.DEFAULT_MAX_TOTAL);
            maxIdle = getIntegerPropertyFromIdentityUtil(redisConstants.GET_REDIS_POOL_MAX_IDLE,
                    redisConstants.DEFAULT_MAX_IDLE);
            minIdle = getIntegerPropertyFromIdentityUtil(redisConstants.GET_REDIS_POOL_MIN_IDLE,
                    redisConstants.DEFAULT_MIN_IDLE);
            maxWaitMillis = getIntegerPropertyFromIdentityUtil(redisConstants.GET_REDIS_POOL_MAX_WAIT,
                    redisConstants.DEFAULT_MAX_WAIT_MILLIS);

        } catch (NumberFormatException e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception ignored : ", e);
            }
            log.warn("One or more pool size configurations cause NumberFormatException. Default values would be used.");
        }

        if (maxSessionDataPoolSize > 0) {
            log.info("Thread pool size for session persistent consumer : " + maxSessionDataPoolSize);
            ExecutorService threadPool = Executors.newFixedThreadPool(maxSessionDataPoolSize);
            for (int i = 0; i < maxSessionDataPoolSize; i++) {
                threadPool.execute(new SessionDataPersistTask(super.getSessionContextQueue()));
            }
        }
    }

    public RedisSessionDataStore() {

        enablePersist = getBooleanPropertyFromIdentityUtil(FrameworkConstants.SessionDataStoreConstants.PERSIST_ENABLE,
                FrameworkConstants.SessionDataStoreConstants.DEFAULT_ENABLE_PERSIST);
        if (!enablePersist) {
            log.info("Session Data Persistence of Authentication framework is not enabled.");
        }
    }

    // Serializes an Object to byte array.
    private static byte[] serialize(Serializable obj) {

        return SerializationUtils.serialize(obj);
    }

    // Deserializes a byte array back to Object.
    private static Object deserialize(byte[] bytes) {

        return SerializationUtils.deserialize(bytes);
    }

    private JedisPool getJedisInstance() throws JedisConnectionException {

        try {
            if (pool == null) {
                synchronized (RedisSessionDataStore.class) {
                    if (pool == null) {
                        pool = new JedisPool(getPoolConfig(), redisConstants.HOST,
                                redisConstants.PORT);
                    }
                }
            }
            return pool;
        } catch (JedisConnectionException e) {
            log.error(e);
        }
        return null;
    }

    private GenericObjectPoolConfig getPoolConfig() {

        GenericObjectPoolConfig jedisPoolConfig = new GenericObjectPoolConfig();
        jedisPoolConfig.setMaxTotal(maxTotal);
        jedisPoolConfig.setMaxIdle(maxIdle);
        jedisPoolConfig.setMinIdle(minIdle);
        jedisPoolConfig.setMaxWaitMillis(maxWaitMillis);
        jedisPoolConfig.setBlockWhenExhausted(redisConstants.BLOCK_WHEN_EXHAUSTED);
        jedisPoolConfig.setTestOnBorrow(redisConstants.TEST_ON_BORROW);
        jedisPoolConfig.setTestOnReturn(redisConstants.TEST_ON_RETURN);

        return jedisPoolConfig;
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void removeTempAuthnContextData(String key, String type) {
        // Empty method.
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void removeExpiredSessionData() {
        // Empty method.
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void removeSessionData(String key, String type, long nanoTime) {

        Jedis jedis;
        if (!enablePersist) {
            return;
        }
        String redisKey = key + redisConstants.DIVIDER + type;
        boolean tempStore = getSessionStoreType(type);
        if (tempStore) {
            redisKey = redisKey + redisConstants.DIVIDER +
                    redisConstants.TEMPSTORE;
        }

        try {
            jedis = getJedisInstance().getResource();
            if (jedis.exists(redisKey)) {
                jedis.del(redisKey);
            }
            String objectKey = redisKey + redisConstants.DIVIDER +
                    redisConstants.OBJECT;
            if (jedis.exists(objectKey)) {
                jedis.del(objectKey);
            }
            jedis.close();

        } catch (JedisException e) {
            log.error("Error while storing DELETE operation session data.", e);
        }
    }

    @Override
    /**
     * @deprecated This is now run as a part of the {@link #removeExpiredSessionData()} due to a possible deadlock as
     * mentioned in IDENTITY-5131.
     */
    public void removeExpiredOperationData() {
        // Empty method.
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public SessionContextDO getSessionContextData(String key, String type) {

        Jedis jedis;
        String redisKey = key + redisConstants.DIVIDER + type;
        if (!enablePersist) {
            return null;
        }
        boolean tempStore = getSessionStoreType(type);
        if (tempStore) {
            redisKey = redisKey + redisConstants.DIVIDER +
                    redisConstants.TEMPSTORE;
        }
        try {
            jedis = getJedisInstance().getResource();
            if (jedis.exists(redisKey)) {
                long nanoTime = Long.parseLong(jedis.hget(redisKey, redisConstants.NANO_TIME));
                String objectKey = redisKey + redisConstants.DIVIDER +
                        redisConstants.OBJECT;
                Object blobObject = deserialize(jedis.get(objectKey.getBytes()));
                jedis.close();
                return new SessionContextDO(key, type, blobObject, nanoTime);
            }
            return null;
        } catch (JedisException e) {
            log.error("Error while retrieving session data.", e);
        }
        return null;
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void persistSessionData(String key, String type, Object entry, long nanoTime, int tenantId) {

        long validityPeriodNano = 0L;
        String objectKey;
        Long expireTime;
        String redisKey;
        boolean tempStore = getSessionStoreType(type);
        redisKey = key + redisConstants.DIVIDER + type;
        if (!enablePersist) {
            return;
        }
        if (entry instanceof CacheEntry) {
            validityPeriodNano = ((CacheEntry) entry).getValidityPeriod();
        }
        if (validityPeriodNano == 0L) {
            validityPeriodNano = getCleanupTimeout(type, tenantId);
        }
        if (tempStore) {
            redisKey = redisKey + redisConstants.DIVIDER +
                    redisConstants.TEMPSTORE;
        }
        try {
            Jedis jedis;
            jedis = getJedisInstance().getResource();
            addRedisHash(jedis, redisKey, redisConstants.NANO_TIME, String.valueOf(nanoTime));
            addRedisHash(jedis, redisKey, redisConstants.EXPIRY_TIME,
                    String.valueOf(nanoTime + validityPeriodNano));
            addRedisHash(jedis, redisKey, redisConstants.TENANT_ID, String.valueOf(tenantId));

            objectKey = redisKey + redisConstants.DIVIDER +
                    redisConstants.OBJECT;
            byte[] serializedSessionObject = serialize((Serializable) entry);
            jedis.set(objectKey.getBytes(), serializedSessionObject);

            expireTime = TimeUnit.SECONDS.convert(validityPeriodNano, TimeUnit.NANOSECONDS);
            jedis.expire(redisKey, expireTime.intValue());
            jedis.expire(objectKey, expireTime.intValue());
            jedis.close();

        } catch (JedisException e) {
            log.error("Error while storing session data.", e);
        }
    }

    private boolean getSessionStoreType(String type) {

        if (tempDataCleanupEnabled && super.isTempCache(type)) {
            return true;
        }
        return false;
    }

    private void addRedisHash(Jedis jedis, String key, String field, String value) {

        jedis.hset(key, field, value);
    }
}
