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

package org.wso2.carbon.identity.remotefetch.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Holds configuration data to instantiate a RemoteFetch
 */
public class RemoteFetchConfiguration implements Serializable {

    private int remoteFetchConfigurationId = -1;
    private int tenantId = 0;
    private String repositoryManagerType = "";
    private String actionListenerType = "";
    private String confgiurationDeployerType = "";
    private Map<String, String> repositoryManagerAttributes = new HashMap<>();
    private Map<String, String> actionListenerAttributes = new HashMap<>();
    private Map<String, String> confgiurationDeployerAttributes = new HashMap<>();

    public RemoteFetchConfiguration() { // default implementation ignored
    }

    public RemoteFetchConfiguration(int remoteFetchConfigurationId, int tenantId, String repositoryManagerType,
                                    String actionListenerType, String confgiurationDeployerType) {

        this.remoteFetchConfigurationId = remoteFetchConfigurationId;
        this.tenantId = tenantId;
        this.repositoryManagerType = repositoryManagerType;
        this.actionListenerType = actionListenerType;
        this.confgiurationDeployerType = confgiurationDeployerType;
    }

    /**
     * @return
     */
    public int getTenantId() {

        return tenantId;
    }

    /**
     * @param tenantId
     */
    public void setTenantId(int tenantId) {

        this.tenantId = tenantId;
    }

    /**
     * @return
     */
    public int getRemoteFetchConfigurationId() {

        return remoteFetchConfigurationId;
    }

    /**
     * @return
     */
    public String getRepositoryManagerType() {

        return repositoryManagerType;
    }

    /**
     * @param repositoryManagerType
     */
    public void setRepositoryManagerType(String repositoryManagerType) {

        this.repositoryManagerType = repositoryManagerType;
    }

    /**
     * @return
     */
    public String getActionListenerType() {

        return actionListenerType;
    }

    /**
     * @param actionListenerType
     */
    public void setActionListenerType(String actionListenerType) {

        this.actionListenerType = actionListenerType;
    }

    /**
     * @return
     */
    public String getConfgiurationDeployerType() {

        return confgiurationDeployerType;
    }

    /**
     * @param confgiurationDeployerType
     */
    public void setConfgiurationDeployerType(String confgiurationDeployerType) {

        this.confgiurationDeployerType = confgiurationDeployerType;
    }

    /**
     * @return
     */
    public Map<String, String> getRepositoryManagerAttributes() {

        return repositoryManagerAttributes;
    }

    /**
     * @param repositoryManagerAttributes
     */
    public void setRepositoryManagerAttributes(Map<String, String> repositoryManagerAttributes) {

        this.repositoryManagerAttributes = repositoryManagerAttributes;
    }

    /**
     * @return
     */
    public Map<String, String> getActionListenerAttributes() {

        return actionListenerAttributes;
    }

    /**
     * @param actionListenerAttributes
     */
    public void setActionListenerAttributes(Map<String, String> actionListenerAttributes) {

        this.actionListenerAttributes = actionListenerAttributes;
    }

    /**
     * @return
     */
    public Map<String, String> getConfgiurationDeployerAttributes() {

        return confgiurationDeployerAttributes;
    }

    /**
     * @param confgiurationDeployerAttributes
     */
    public void setConfgiurationDeployerAttributes(Map<String, String> confgiurationDeployerAttributes) {

        this.confgiurationDeployerAttributes = confgiurationDeployerAttributes;
    }

    @Override
    public String toString() {

        return "RemoteFetchConfiguration{" +
                "remoteFetchConfigurationId=" + remoteFetchConfigurationId +
                ", tenantId=" + tenantId +
                ", repositoryManagerType='" + repositoryManagerType + '\'' +
                ", actionListenerType='" + actionListenerType + '\'' +
                ", confgiurationDeployerType='" + confgiurationDeployerType + '\'' +
                ", repositoryManagerAttributes=" + repositoryManagerAttributes +
                ", actionListenerAttributes=" + actionListenerAttributes +
                ", confgiurationDeployerAttributes=" + confgiurationDeployerAttributes +
                '}';
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (!(o instanceof RemoteFetchConfiguration)) return false;
        RemoteFetchConfiguration that = (RemoteFetchConfiguration) o;
        return remoteFetchConfigurationId == that.remoteFetchConfigurationId &&
                tenantId == that.tenantId &&
                Objects.equals(repositoryManagerType, that.repositoryManagerType) &&
                Objects.equals(actionListenerType, that.actionListenerType) &&
                Objects.equals(confgiurationDeployerType, that.confgiurationDeployerType) &&
                Objects.equals(repositoryManagerAttributes, that.repositoryManagerAttributes) &&
                Objects.equals(actionListenerAttributes, that.actionListenerAttributes) &&
                Objects.equals(confgiurationDeployerAttributes, that.confgiurationDeployerAttributes);
    }

    @Override
    public int hashCode() {

        return Objects.hash(remoteFetchConfigurationId, tenantId, repositoryManagerType, actionListenerType, confgiurationDeployerType, repositoryManagerAttributes, actionListenerAttributes, confgiurationDeployerAttributes);
    }
}
