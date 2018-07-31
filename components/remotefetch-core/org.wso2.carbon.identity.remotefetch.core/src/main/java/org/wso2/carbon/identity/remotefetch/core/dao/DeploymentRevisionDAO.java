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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.remotefetch.core.dao;

import org.wso2.carbon.identity.remotefetch.common.DeploymentRevision;
import org.wso2.carbon.identity.remotefetch.common.exceptions.RemoteFetchCoreException;

import java.util.List;

/**
 * Interface used to access the data layer to store/update
 */
public interface DeploymentRevisionDAO {

    /**
     * @param deploymentRevision
     * @return
     * @throws RemoteFetchCoreException
     */
    int createDeploymentRevision(DeploymentRevision deploymentRevision) throws RemoteFetchCoreException;

    /**
     * @param remoteFetchConfigurationId
     * @param itemName
     * @return
     * @throws RemoteFetchCoreException
     */
    DeploymentRevision getDeploymentRevision(int remoteFetchConfigurationId, String itemName)
            throws RemoteFetchCoreException;

    /**
     * @param deploymentRevision
     * @throws RemoteFetchCoreException
     */
    void updateDeploymentRevision(DeploymentRevision deploymentRevision) throws RemoteFetchCoreException;

    /**
     * @param deploymentRevisionId
     * @throws RemoteFetchCoreException
     */
    void deleteDeploymentRevision(int deploymentRevisionId) throws RemoteFetchCoreException;

    /**
     * @param remoteFetchConfigurationId
     * @return
     * @throws RemoteFetchCoreException
     */
    List<DeploymentRevision> getDeploymentRevisionsByConfigurationId(int remoteFetchConfigurationId)
            throws RemoteFetchCoreException;

}
