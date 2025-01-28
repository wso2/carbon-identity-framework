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

package org.wso2.carbon.identity.remote.log.publish;

import java.util.List;
import org.wso2.carbon.identity.remote.log.publish.exception.RemoteLogPublishServerException;
import org.wso2.carbon.identity.remote.log.publish.model.RemoteLogPublishConfig;

/**
 * This is the interface used for configuring tenant wise remote log publishing configurations.
 */
public interface RemoteLogPublishConfigService {

    /**
     * This method is used to add a remote log publish configuration for a tenant.
     *
     * @param config    Remote log publishing config for tenant to add.
     * @param tenantDomain  Tenant domain to add the log publishing configuration.
     */
    void addRemoteLogPublishConfig(RemoteLogPublishConfig config, String tenantDomain)
            throws RemoteLogPublishServerException;

    /**
     * This method is used to add a remote log publish configuration for a tenant.
     *
     * @param config    Remote log publishing config for tenant to update.
     * @param tenantDomain  Tenant domain to update the log publishing configuration.
     */
    void updateRemoteLogPublishConfig(RemoteLogPublishConfig config, String tenantDomain)
            throws RemoteLogPublishServerException;

    /**
     * This method is used to delete all the remote log publish configuration of a tenant.
     *
     * @param tenantDomain  Tenant domain to delete the log publishing configuration.
     */
    void deleteAllRemoteLogPublishConfigs(String tenantDomain);

    /**
     * This method is used to delete the remote log publish configuration of a tenant for a given log type.
     *
     * @param logType    Log type to delete remote log publishing config for tenant.
     * @param tenantDomain  Tenant domain to delete the log publishing configuration.
     */
    void deleteRemoteLogPublishConfig(String logType, String tenantDomain);

    /**
     * This method is used to get all the remote log publish configurations.
     *
     * @param tenantDomain  Tenant domain to get all log publish configurations.
     * @return  The list of remote log publish configurations for the tenant.
     */
    List<RemoteLogPublishConfig> getAllRemoteLogPublishConfigs(String tenantDomain);

    /**
     * This method is used to get the remote log publishing configuration for a given log type.
     *
     * @param tenantDomain  Tenant domain to get the log publish configuration.
     * @return  The remote log publish configuration for the tenant.
     */
    RemoteLogPublishConfig getRemoteLogPublishConfig(String logType, String tenantDomain);
}
