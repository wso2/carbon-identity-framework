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
import org.wso2.carbon.identity.gateway.api.exception.GatewayRuntimeException;
import org.wso2.carbon.identity.gateway.context.SessionContext;

import java.util.concurrent.BlockingDeque;

/**
 * SessionPersistenceTask is the class that is use to do db operation for the Session.
 */
public class SessionPersistenceTask implements Runnable {

    private static Logger log = LoggerFactory.getLogger(SessionPersistenceTask.class);

    private BlockingDeque<SessionJob> sessionJobQueue;
    private SessionDAO persistentDAO;

    SessionPersistenceTask(BlockingDeque<SessionJob> sessionJobQueue, SessionDAO persistentDAO) {
        this.sessionJobQueue = sessionJobQueue;
        this.persistentDAO = persistentDAO;
    }

    @Override
    public void run() {

        if (log.isDebugEnabled()) {
            log.debug("SessionPersistenceTask is started");
        }

        while (true) {
            SessionJob job;
            try {
                job = sessionJobQueue.take();
                if (job != null) {
                    if (job.sessionContext != null) {
                        persistentDAO.put(job.key, job.sessionContext);
                    } else {
                        persistentDAO.remove(job.key);
                    }
                }
            } catch (InterruptedException | GatewayRuntimeException e) {
                log.error("Error occurred while running task for SessionJob", e);
            }
        }
    }

    static class SessionJob {

        String key;
        SessionContext sessionContext;

        SessionJob(String key) {
            this.key = key;
        }

        SessionJob(String key, SessionContext sessionContext) {
            this.key = key;
            this.sessionContext = sessionContext;
        }
    }
}
