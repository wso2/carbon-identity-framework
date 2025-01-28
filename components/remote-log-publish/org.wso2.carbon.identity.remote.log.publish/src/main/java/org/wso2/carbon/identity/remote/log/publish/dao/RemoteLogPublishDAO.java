package org.wso2.carbon.identity.remote.log.publish.dao;

import java.util.List;
import org.wso2.carbon.identity.remote.log.publish.exception.RemoteLogPublishServerException;
import org.wso2.carbon.identity.remote.log.publish.model.RemoteLogPublishConfig;

/**
 * Perform CRUD operations for {@link RemoteLogPublishConfig}.
 */
public interface RemoteLogPublishDAO {


    void addRemoteLogPublishConfig(RemoteLogPublishConfig config, String tenantDomain)
            throws RemoteLogPublishServerException;
    void updateRemoteLogPublishConfig(RemoteLogPublishConfig config, String tenantDomain)
            throws RemoteLogPublishServerException;
    RemoteLogPublishConfig getRemoteLogPublishConfig(String logType, String tenantDomain)
            throws RemoteLogPublishServerException;
    List<RemoteLogPublishConfig> getAllRemoteLogPublishConfigs(String tenantDomain)
            throws RemoteLogPublishServerException;
    void deleteRemoteLogPublishConfig(String logType, String tenantDomain) throws RemoteLogPublishServerException;
    void deleteAllRemoteLogPublishConfigs(String tenantDomain) throws RemoteLogPublishServerException;
}
