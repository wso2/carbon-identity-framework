package org.wso2.carbon.identity.remote.log.publish;

import java.util.List;
import java.util.UUID;
import org.wso2.carbon.identity.remote.log.publish.dao.RemoteLogPublishDAO;
import org.wso2.carbon.identity.remote.log.publish.dao.impl.CacheBackedRemoteLogPublishDAO;
import org.wso2.carbon.identity.remote.log.publish.dao.impl.RemoteLogPublishDAOImpl;
import org.wso2.carbon.identity.remote.log.publish.exception.RemoteLogPublishServerException;
import org.wso2.carbon.identity.remote.log.publish.model.RemoteLogPublishConfig;

/**
 * RemoteLogPublishConfigService implementation.
 */
public class RemoteLogPublishConfigServiceImpl implements RemoteLogPublishConfigService {

    private final RemoteLogPublishDAO cacheBackedRemoteLogPublishDAO =
            new CacheBackedRemoteLogPublishDAO(new RemoteLogPublishDAOImpl());

    @Override
    public void addRemoteLogPublishConfig(RemoteLogPublishConfig config, String tenantDomain)
            throws RemoteLogPublishServerException {

        config.setUuid(UUID.randomUUID().toString());
        cacheBackedRemoteLogPublishDAO.addRemoteLogPublishConfig(config, tenantDomain);
    }

    @Override
    public void updateRemoteLogPublishConfig(RemoteLogPublishConfig config, String tenantDomain)
            throws RemoteLogPublishServerException {

        cacheBackedRemoteLogPublishDAO.updateRemoteLogPublishConfig(config, tenantDomain);
    }

    @Override
    public void deleteAllRemoteLogPublishConfigs(String tenantDomain) {

    }

    @Override
    public void deleteRemoteLogPublishConfig(String logType, String tenantDomain) {

    }

    @Override
    public List<RemoteLogPublishConfig> getAllRemoteLogPublishConfigs(String tenantDomain) {
        return null;
    }

    @Override
    public RemoteLogPublishConfig getRemoteLogPublishConfig(String logType, String tenantDomain) {
        return null;
    }
}
