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
import org.wso2.carbon.identity.gateway.api.context.GatewayMessageContext;
import org.wso2.carbon.identity.gateway.api.exception.GatewayRuntimeException;

import java.util.concurrent.BlockingDeque;

/**
 * GatewayContextPersistenceTask is the place that persist the GatewayContext.
 */
public class GatewayContextPersistenceTask implements Runnable {

    private static Logger log = LoggerFactory.getLogger(GatewayContextPersistenceTask.class);

    private BlockingDeque<IdentityContextJob> identityContextJobQueue;
    private GatewayContextDAO persistentDAO;

    GatewayContextPersistenceTask(BlockingDeque<IdentityContextJob> identityContextJobQueue,
                                  GatewayContextDAO persistentDAO) {
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
                    if (job.gatewayMessageContext != null) {
                        persistentDAO.put(job.key, job.gatewayMessageContext);
                    } else {
                        persistentDAO.remove(job.key);
                    }
                }
            } catch (InterruptedException | GatewayRuntimeException e) {
                log.error("Error occurred while running task for SessionJob", e);
            }
        }
    }

    static class IdentityContextJob {

        String key;
        GatewayMessageContext gatewayMessageContext;

        IdentityContextJob(String key) {
            this.key = key;
        }

        IdentityContextJob(String key, GatewayMessageContext sessionContext) {
            this.key = key;
            this.gatewayMessageContext = sessionContext;
        }
    }
}
