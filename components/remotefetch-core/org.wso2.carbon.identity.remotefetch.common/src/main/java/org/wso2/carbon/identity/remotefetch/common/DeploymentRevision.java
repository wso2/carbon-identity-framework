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

import java.io.File;
import java.util.Date;

/**
 * Deployment Revision bean
 */
public class DeploymentRevision {

    private int fileRevisionId;
    private int configId;
    private File file;
    private String fileHash;
    private Date deployedDate;
    private DEPLOYMENT_STATUS deploymentStatus;
    private String itemName;

    public enum DEPLOYMENT_STATUS {
        DEPLOYED, ERROR_DEPLOYING, FILE_MISSING
    }

    /**
     * @param configId
     * @param file
     */
    public DeploymentRevision(int configId, File file) {

        this.configId = configId;
        this.file = file;
    }

    /**
     * @return
     */
    public int getDeploymentRevisionId() {

        return fileRevisionId;
    }

    /**
     * @param fileRevisionId
     */
    public void setDeploymentRevisionId(int fileRevisionId) {

        this.fileRevisionId = fileRevisionId;
    }

    /**
     * @return
     */
    public int getConfigId() {

        return configId;
    }

    /**
     * @param configId
     */
    public void setConfigId(int configId) {

        this.configId = configId;
    }

    /**
     * @return
     */
    public File getFile() {

        return file;
    }

    /**
     * @param file
     */
    public void setFile(File file) {

        this.file = file;
    }

    /**
     * @return
     */
    public String getFileHash() {

        return fileHash;
    }

    /**
     * @param fileHash
     */
    public void setFileHash(String fileHash) {

        this.fileHash = fileHash;
    }

    /**
     * @return
     */
    public Date getDeployedDate() {

        return deployedDate;
    }

    /**
     * @param deployedDate
     */
    public void setDeployedDate(Date deployedDate) {

        this.deployedDate = deployedDate;
    }

    /**
     * @return
     */
    public DEPLOYMENT_STATUS getDeploymentStatus() {

        return deploymentStatus;
    }

    /**
     * @param deploymentStatus
     */
    public void setDeploymentStatus(DeploymentRevision.DEPLOYMENT_STATUS deploymentStatus) {

        this.deploymentStatus = deploymentStatus;
    }

    /**
     * @return
     */
    public String getItemName() {

        return itemName;
    }

    /**
     * @param itemName
     */
    public void setItemName(String itemName) {

        this.itemName = itemName;
    }
}
