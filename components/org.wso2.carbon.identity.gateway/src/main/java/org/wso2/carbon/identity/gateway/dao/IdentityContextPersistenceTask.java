/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.gateway.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.gateway.api.context.IdentityMessageContext;
import org.wso2.carbon.identity.gateway.api.exception.FrameworkRuntimeException;

import java.util.concurrent.BlockingDeque;

public class IdentityContextPersistenceTask implements Runnable {

    private static Logger log = LoggerFactory.getLogger(IdentityContextPersistenceTask.class);

    private BlockingDeque<IdentityContextJob> identityContextJobQueue;
    private IdentityContextDAO persistentDAO;

    IdentityContextPersistenceTask(BlockingDeque<IdentityContextJob> identityContextJobQueue, IdentityContextDAO persistentDAO) {
        this.identityContextJobQueue = identityContextJobQueue;
        this.persistentDAO = persistentDAO;
    }

    @Override
    public void run() {

        if (log.isDebugEnabled()) {
            log.debug("SessionPersistenceTask is started");
        }

        while (true) {
            IdentityContextJob job;
            try {
                job = identityContextJobQueue.take();
                if (job != null) {
                    if (job.identityMessageContext != null) {
                        persistentDAO.put(job.key, job.identityMessageContext);
                    } else {
                        persistentDAO.remove(job.key);
                    }

                }
            } catch (InterruptedException | FrameworkRuntimeException e) {
                log.error("Error occurred while running task for SessionJob", e);
            }
        }
    }

    static class IdentityContextJob {

        String key;
        IdentityMessageContext identityMessageContext;

        IdentityContextJob(String key) {
            this.key = key;
        }

        IdentityContextJob(String key, IdentityMessageContext sessionContext) {
            this.key = key;
            this.identityMessageContext = sessionContext;
        }
    }

}
