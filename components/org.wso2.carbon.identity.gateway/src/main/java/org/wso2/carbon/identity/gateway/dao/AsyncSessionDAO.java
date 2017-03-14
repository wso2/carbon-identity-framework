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

import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.gateway.context.SessionContext;
import org.wso2.carbon.identity.gateway.dao.jdbc.JDBCSessionDAO;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * AsyncSessionDAO is the DAO class for Session persistence in async manner.
 */
public class AsyncSessionDAO extends SessionDAO {

    private static final Logger log = LoggerFactory.getLogger(AsyncSessionDAO.class);

    private static SessionDAO instance = new AsyncSessionDAO();
    private SessionDAO persistentDAO = JDBCSessionDAO.getInstance();
    private int poolSize = 0;

    private BlockingDeque<SessionPersistenceTask.SessionJob> sessionJobs = new LinkedBlockingDeque();

    private AsyncSessionDAO() {

        //
        String poolSizeConfig = "2";
        if (NumberUtils.isNumber(poolSizeConfig)) {
            poolSize = Integer.parseInt(poolSizeConfig);
            if (log.isDebugEnabled()) {
                log.debug("Thread pool size for Session Async DAO: " + poolSizeConfig);
            }
        }
        if (poolSize > 0) {
            ExecutorService threadPool = Executors.newFixedThreadPool(poolSize);
            for (int i = 0; i < poolSize; i++) {
                threadPool.execute(new SessionPersistenceTask(sessionJobs, persistentDAO));
            }
            threadPool = Executors.newFixedThreadPool(poolSize);
            for (int i = 0; i < poolSize; i++) {
                threadPool.execute(new SessionPersistenceTask(sessionJobs, persistentDAO));
            }
        }
    }

    public static SessionDAO getInstance() {
        return instance;
    }

    @Override
    public SessionContext get(String key) {
        return persistentDAO.get(key);
    }

    @Override
    public void put(String key, SessionContext context) {

        if (poolSize > 0) {
            SessionPersistenceTask.SessionJob job = new SessionPersistenceTask.SessionJob(key, context);
            sessionJobs.add(job);
        } else {
            persistentDAO.put(key, context);
        }
    }

    @Override
    public void remove(String key) {

        if (poolSize > 0) {
            SessionPersistenceTask.SessionJob job = new SessionPersistenceTask.SessionJob(key);
            sessionJobs.add(job);
        } else {
            persistentDAO.remove(key);
        }
    }
}
