package org.wso2.carbon.identity.remote.log.publish.dao.impl;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.wso2.carbon.identity.remote.log.publish.dao.RemoteLogPublishDAO;
import org.wso2.carbon.identity.remote.log.publish.exception.RemoteLogPublishServerException;
import org.wso2.carbon.identity.remote.log.publish.model.RemoteLogPublishConfig;

/**
 * Remote Log publish config DAO Impl with cache.
 */
public class CacheBackedRemoteLogPublishDAO implements RemoteLogPublishDAO {

    private final RemoteLogPublishDAO remoteLogPublishDAO;
    private final ConcurrentMap<String, RemoteLogPublishConfig> cache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, List<RemoteLogPublishConfig>> allConfigsCache = new ConcurrentHashMap<>();

    public CacheBackedRemoteLogPublishDAO(RemoteLogPublishDAO remoteLogPublishDAO) {

        this.remoteLogPublishDAO = remoteLogPublishDAO;
    }

    @Override
    public void addRemoteLogPublishConfig(RemoteLogPublishConfig config, String tenantDomain)
            throws RemoteLogPublishServerException {

        remoteLogPublishDAO.addRemoteLogPublishConfig(config, tenantDomain);
        cache.put(generateCacheKey(config.getLogType(), tenantDomain), config);
    }

    @Override
    public void updateRemoteLogPublishConfig(RemoteLogPublishConfig config, String tenantDomain)
            throws RemoteLogPublishServerException {

        remoteLogPublishDAO.updateRemoteLogPublishConfig(config, tenantDomain);
        cache.put(generateCacheKey(config.getLogType(), tenantDomain), config);
    }

    @Override
    public RemoteLogPublishConfig getRemoteLogPublishConfig(String logType, String tenantDomain)
            throws RemoteLogPublishServerException {

        String cacheKey = generateCacheKey(logType, tenantDomain);
        RemoteLogPublishConfig cachedConfig = cache.get(cacheKey);
        if (cachedConfig != null) {
            return cachedConfig;
        }

        RemoteLogPublishConfig config = remoteLogPublishDAO.getRemoteLogPublishConfig(logType, tenantDomain);
        cache.put(cacheKey, config);
        return config;
    }

    @Override
    public List<RemoteLogPublishConfig> getAllRemoteLogPublishConfigs(String tenantDomain)
            throws RemoteLogPublishServerException {

        List<RemoteLogPublishConfig> cachedConfigs = allConfigsCache.get(tenantDomain);
        if (cachedConfigs != null) {
            return cachedConfigs;
        }

        List<RemoteLogPublishConfig> configs = remoteLogPublishDAO.getAllRemoteLogPublishConfigs(tenantDomain);
        allConfigsCache.put(tenantDomain, configs);
        return configs;
    }

    @Override
    public void deleteRemoteLogPublishConfig(String logType, String tenantDomain)
            throws RemoteLogPublishServerException {

        remoteLogPublishDAO.deleteRemoteLogPublishConfig(logType, tenantDomain);
        cache.remove(generateCacheKey(logType, tenantDomain));
    }

    @Override
    public void deleteAllRemoteLogPublishConfigs(String tenantDomain) throws RemoteLogPublishServerException {

        remoteLogPublishDAO.deleteAllRemoteLogPublishConfigs(tenantDomain);
        allConfigsCache.remove(tenantDomain);
    }

    private String generateCacheKey(String logType, String tenantDomain) {
        return tenantDomain + "_" + logType;
    }
}
