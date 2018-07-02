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
import org.wso2.carbon.identity.remotefetch.common.ActionHandler;
import org.wso2.carbon.identity.remotefetch.common.ConfigDeployer;
import org.wso2.carbon.identity.remotefetch.common.RepositoryConnector;
import org.wso2.carbon.identity.remotefetch.core.implementations.repositoryHandlers.GitRepositoryConnector;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PollingActionHandler implements ActionHandler, Runnable {

    private static Log log = LogFactory.getLog(GitRepositoryConnector.class);

    private RepositoryConnector repo;
    private Map<File, ConfigDeployer> directoryMap;
    private Integer frequency =  60;

    // Keeps last deployed revision date of each file
    private Map<File,Date> revisionDates = new HashMap<>();

    public PollingActionHandler(RepositoryConnector repo, Map<File, ConfigDeployer> directoryMap) {
        this.repo = repo;
        this.directoryMap = directoryMap;
    }

    public void main(){
        Thread t = new Thread(new PollingActionHandler(this.repo,this.directoryMap));
        t.start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                this.repo.fetchRepository();
            } catch (Exception e){
                log.info("Error pulling repository");
            }

            if(this.revisionDates.size() == 0) this.seedExistingFiles(directoryMap.keySet());
            this.directoryMap.forEach(this::pollDirectory);

            try{
                Integer seconds = this.frequency;
                log.info("Sleeping for " + seconds +" seconds");
                Thread.sleep(1000 * (long) seconds);
            }catch (InterruptedException e){
                log.info("Polling loop interrupted");
                Thread.currentThread().interrupt();
            }

            if (Thread.currentThread().isInterrupted()) break;
        }
    }

    private void seedExistingFiles(Set<File> paths){
        log.info("Seeding Existing file revisions");
        paths.forEach((File path) -> {
            List<File> configFiles = null;
            try {
                configFiles = this.repo.listFiles(path);
            }catch (Exception e){
                log.info("Error listing files in path for seeding");
            }
            if (configFiles != null){
                configFiles.forEach((File configFile) -> {
                    this.revisionDates.put(configFile,null);
                });
            }
        });
    }

    private void pollDirectory(File path, ConfigDeployer deployer){
        log.info("Polling Directory " + path.getPath() + " for changes");
        List<File> configFiles = null;

        try {
            configFiles = this.repo.listFiles(path);
        }catch (Exception e){
            log.info("Error listing files in root");
        }

        if (configFiles != null){
            for (File file: configFiles) {
                Date currentRevision = null;
                try {
                    currentRevision = this.repo.getLastModified(file);
                }catch (Exception e){
                    log.info("Unable to read modify date of " + path.getPath());
                }

                // Is this file a new addition, if so deploy now or else check revisions
                if (this.revisionDates.containsKey(file)){
                    Date previousRevision = this.revisionDates.get(file);
                    if (currentRevision != null && previousRevision != null
                            && previousRevision.before(currentRevision)){
                        log.info("Deploying " + file.getPath());
                        try {
                            deployer.deploy(repo.getFile(file));
                        } catch (Exception e){
                            log.info("Error Deploying "+ file.getName());
                        }
                    }
                }else{
                    log.info("Deploying new file " + file.getPath());
                    try {
                        deployer.deploy(repo.getFile(file));
                    } catch (Exception e){
                        log.info("Error Deploying "+ file.getName());
                    }
                }

                this.revisionDates.put(file,currentRevision);
            }
        }
    }
}
