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

package org.wso2.carbon.identity.remotefetch.core.implementations.actionHandlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.remotefetch.common.ConfigFileContent;
import org.wso2.carbon.identity.remotefetch.common.DeploymentRevision;
import org.wso2.carbon.identity.remotefetch.common.actionlistener.ActionListener;
import org.wso2.carbon.identity.remotefetch.common.configdeployer.ConfigDeployer;
import org.wso2.carbon.identity.remotefetch.common.exceptions.RemoteFetchCoreException;
import org.wso2.carbon.identity.remotefetch.common.repomanager.RepositoryManager;
import org.wso2.carbon.identity.remotefetch.core.dao.DeploymentRevisionDAO;
import org.wso2.carbon.identity.remotefetch.core.dao.impl.DeploymentRevisionDAOImpl;
import org.wso2.carbon.identity.remotefetch.core.implementations.repositoryHandlers.GitRepositoryManager;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PollingActionListener implements ActionListener {

    private static final Log log = LogFactory.getLog(GitRepositoryManager.class);

    private RepositoryManager repo;
    private Integer frequency = 60;
    private Date lastIteration;
    private File path;
    private ConfigDeployer configDeployer;
    private DeploymentRevisionDAO deploymentRevisionDAO;
    private Map<String, DeploymentRevision> deploymentRevisionMap = new HashMap<>();
    private int remoteFetchConfigurationId;

    public PollingActionListener(RepositoryManager repo, File path, ConfigDeployer configDeployer,
                                 int frequency, int remoteFetchConfigurationId) {

        this.repo = repo;
        this.path = path;
        this.configDeployer = configDeployer;
        this.frequency = frequency;
        this.remoteFetchConfigurationId = remoteFetchConfigurationId;
        this.deploymentRevisionDAO = new DeploymentRevisionDAOImpl();
        this.seedRevisions();
    }

    /**
     * seed local map with existing DeploymentRevisions from database
     */
    private void seedRevisions() {

        try {
            List<DeploymentRevision> deploymentRevisions = this.deploymentRevisionDAO
                    .getDeploymentRevisionsByConfigurationId(this.remoteFetchConfigurationId);

            for (DeploymentRevision deploymentRevision : deploymentRevisions) {
                this.deploymentRevisionMap.put(deploymentRevision.getItemName(), deploymentRevision);
            }

        } catch (RemoteFetchCoreException e) {
            log.info("Unable to seed DeploymentRevisions", e);
        }
    }

    /**
     * resolve and create / update list of revisions
     *
     * @param configPaths
     */
    private void manageRevisions(List<File> configPaths) {

        configPaths.forEach((File configPath) -> {
            String resolvedName = "";

            try {
                resolvedName = this.configDeployer.resolveConfigName(this.repo.getFile(configPath));
            } catch (Exception e) {
                log.info("Unable to resolve configuration", e);
            }

            if (!(resolvedName.isEmpty())) {
                if (this.deploymentRevisionMap.containsKey(resolvedName)) {
                    updateRevision(resolvedName, configPath);
                } else {
                    createRevision(resolvedName, configPath);
                }
            }
        });
    }

    /**
     * Update DeploymentRevision if file was moved/renamed and store
     *
     * @param resolvedName
     * @param configPath
     */
    private void updateRevision(String resolvedName, File configPath) {

        DeploymentRevision currentdDeploymentRevision = this.deploymentRevisionMap.get(resolvedName);
        if (!currentdDeploymentRevision.getFile().equals(configPath)) {
            try {
                currentdDeploymentRevision.setFile(configPath);
                this.deploymentRevisionDAO.updateDeploymentRevision(currentdDeploymentRevision);
            } catch (RemoteFetchCoreException e) {
                log.info("Unable to update DeploymentRevision for configuration", e);
            }
        }
    }

    /**
     * Create DeploymentRevision and store for resolved name and path
     *
     * @param resolvedName
     * @param configPath
     */
    private void createRevision(String resolvedName, File configPath) {

        DeploymentRevision deploymentRevision = new DeploymentRevision(this.remoteFetchConfigurationId, configPath);
        deploymentRevision.setFileHash("");
        deploymentRevision.setItemName(resolvedName);
        try {
            int id = this.deploymentRevisionDAO.createDeploymentRevision(deploymentRevision);
            deploymentRevision.setDeploymentRevisionId(id);
            this.deploymentRevisionMap.put(deploymentRevision.getItemName(), deploymentRevision);
        } catch (RemoteFetchCoreException e) {
            log.info("Unable to add a new DeploymentRevision for configuration", e);
        }
    }

    @Override
    public void iteration() {

        Calendar nextIteration = Calendar.getInstance();
        nextIteration.add(Calendar.SECOND, this.frequency);
        if ((lastIteration == null) || (lastIteration.before(nextIteration.getTime()))) {
            try {
                this.repo.fetchRepository();
            } catch (Exception e) {
                log.info("Error pulling repository");
            }

            this.pollDirectory(this.path, this.configDeployer);
            this.lastIteration = new Date();
        }
    }

    /**
     * Poll directory for new files for given root
     *
     * @param path
     * @param deployer
     */
    private void pollDirectory(File path, ConfigDeployer deployer) {

        log.info("Polling Directory " + path.getPath() + " for changes");
        List<File> configFiles = null;

        try {
            configFiles = this.repo.listFiles(path);
        } catch (Exception e) {
            log.info("Error listing files in root");
            return;
        }

        this.manageRevisions(configFiles);

        for (DeploymentRevision deploymentRevision : this.deploymentRevisionMap.values()) {
            String newHash = "";
            String currentHash = deploymentRevision.getFileHash();

            try {
                newHash = this.repo.getRevisionHash(deploymentRevision.getFile());
            } catch (Exception e) {
                log.info("Unable to get new hash", e);
            }

            // Deploy if new file or updated file
            if (!newHash.isEmpty() && (currentHash.isEmpty() || !(currentHash.equals(newHash)))) {

                deploymentRevision.setFileHash(newHash);
                try {
                    ConfigFileContent configFileContent = repo.getFile(deploymentRevision.getFile());
                    deployer.deploy(configFileContent);
                    deploymentRevision.setDeploymentStatus(DeploymentRevision.DEPLOYMENT_STATUS.DEPLOYED);
                    log.info("Deployed " + deploymentRevision.getFile().getPath());
                } catch (Exception e) {
                    log.info("Error Deploying " + deploymentRevision.getFile().getName(), e);
                    deploymentRevision.setDeploymentStatus(DeploymentRevision.DEPLOYMENT_STATUS.ERROR_DEPLOYING);
                }

                // Set new deployment Date
                deploymentRevision.setDeployedDate(new Date());

                try {
                    this.deploymentRevisionDAO.updateDeploymentRevision(deploymentRevision);
                } catch (Exception e) {
                    log.info("Error updating DeploymentRevision", e);
                }
            }
        }
    }
}
