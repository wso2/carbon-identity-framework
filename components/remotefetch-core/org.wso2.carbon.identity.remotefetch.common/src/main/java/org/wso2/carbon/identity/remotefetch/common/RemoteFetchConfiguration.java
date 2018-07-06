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

package org.wso2.carbon.identity.remotefetch.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds configuration data to instantiate a RemoteFetch
 */
public class RemoteFetchConfiguration implements Serializable {
    private int remoteFetchConfigurationId = -1;
    private int tenantId = 0;
    private String repositoryConnectorType = "";
    private String actionListenerType = "";
    private String confgiurationDeployerType = "";
    private Map<String,String> repositoryConnectorAttributes = new HashMap<>();
    private Map<String,String> actionListenerAttributes = new HashMap<>();
    private Map<String,String> confgiurationDeployerAttributes = new HashMap<>();
    private Map<String,String> deploymentDetails = new HashMap<>();

    public RemoteFetchConfiguration() { // default implementation ignored
    }

    public RemoteFetchConfiguration(int remoteFetchConfigurationId, int tenantId, String repositoryConnectorType,
                                    String actionListenerType, String confgiurationDeployerType,
                                    Map<String, String> repositoryConnectorAttributes, Map<String,
            String> actionListenerAttributes, Map<String, String> confgiurationDeployerAttributes,
                                    Map<String, String> deploymentDetails) {

        this.remoteFetchConfigurationId = remoteFetchConfigurationId;
        this.tenantId = tenantId;
        this.repositoryConnectorType = repositoryConnectorType;
        this.actionListenerType = actionListenerType;
        this.confgiurationDeployerType = confgiurationDeployerType;
        this.repositoryConnectorAttributes = repositoryConnectorAttributes;
        this.actionListenerAttributes = actionListenerAttributes;
        this.confgiurationDeployerAttributes = confgiurationDeployerAttributes;
        this.deploymentDetails = deploymentDetails;
    }

    /**
     *
     * @return
     */
    public int getTenantId() {

        return tenantId;
    }

    /**
     *
     * @param tenantId
     */
    public void setTenantId(int tenantId) {

        this.tenantId = tenantId;
    }

    /**
     *
     * @return
     */
    public Map<String, String> getDeploymentDetails() {

        return deploymentDetails;
    }

    /**
     *
     * @param deploymentDetails
     */
    public void setDeploymentDetails(Map<String, String> deploymentDetails) {

        this.deploymentDetails = deploymentDetails;
    }

    /**
     *
     * @return
     */
    public int getRemoteFetchConfigurationId() {

        return remoteFetchConfigurationId;
    }

    /**
     *
     * @return
     */
    public String getRepositoryConnectorType() {

        return repositoryConnectorType;
    }

    /**
     *
     * @param repositoryConnectorType
     */
    public void setRepositoryConnectorType(String repositoryConnectorType) {

        this.repositoryConnectorType = repositoryConnectorType;
    }

    /**
     *
     * @return
     */
    public String getActionListenerType() {

        return actionListenerType;
    }

    /**
     *
     * @param actionListenerType
     */
    public void setActionListenerType(String actionListenerType) {

        this.actionListenerType = actionListenerType;
    }

    /**
     *
     * @return
     */
    public String getConfgiurationDeployerType() {

        return confgiurationDeployerType;
    }

    /**
     *
     * @param confgiurationDeployerType
     */
    public void setConfgiurationDeployerType(String confgiurationDeployerType) {

        this.confgiurationDeployerType = confgiurationDeployerType;
    }

    /**
     *
     * @return
     */
    public Map<String, String> getRepositoryConnectorAttributes() {

        return repositoryConnectorAttributes;
    }

    /**
     *
     * @param repositoryConnectorAttributes
     */
    public void setRepositoryConnectorAttributes(Map<String, String> repositoryConnectorAttributes) {

        this.repositoryConnectorAttributes = repositoryConnectorAttributes;
    }

    /**
     *
     * @return
     */
    public Map<String, String> getActionListenerAttributes() {

        return actionListenerAttributes;
    }

    /**
     *
     * @param actionListenerAttributes
     */
    public void setActionListenerAttributes(Map<String, String> actionListenerAttributes) {

        this.actionListenerAttributes = actionListenerAttributes;
    }

    /**
     *
     * @return
     */
    public Map<String, String> getConfgiurationDeployerAttributes() {

        return confgiurationDeployerAttributes;
    }

    /**
     *
     * @param confgiurationDeployerAttributes
     */
    public void setConfgiurationDeployerAttributes(Map<String, String> confgiurationDeployerAttributes) {

        this.confgiurationDeployerAttributes = confgiurationDeployerAttributes;
    }

    @Override
    public String toString() {

        return "RemoteFetchConfiguration{" +
                "remoteFetchConfigurationId=" + remoteFetchConfigurationId +
                ", repositoryConnectorType='" + repositoryConnectorType + '\'' +
                ", actionListenerType='" + actionListenerType + '\'' +
                ", confgiurationDeployerType='" + confgiurationDeployerType + '\'' +
                ", repositoryConnectorAttributes=" + repositoryConnectorAttributes +
                ", actionListenerAttributes=" + actionListenerAttributes +
                ", confgiurationDeployerAttributes=" + confgiurationDeployerAttributes +
                ", deploymentDetails=" + deploymentDetails +
                '}';
    }
}
