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
import org.wso2.carbon.identity.gateway.api.context.IdentityMessageContext;
import org.wso2.carbon.identity.gateway.dao.jdbc.JDBCIdentityContextDAO;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

public class AsyncIdentityContextDAO extends IdentityContextDAO {

    private static final Logger log = LoggerFactory.getLogger(AsyncIdentityContextDAO.class);

    private static IdentityContextDAO instance = new AsyncIdentityContextDAO();
    private IdentityContextDAO persistentDAO = JDBCIdentityContextDAO.getInstance();
    private int poolSize = 0;

    private BlockingDeque<IdentityContextPersistenceTask.IdentityContextJob> identityContextJobs = new
            LinkedBlockingDeque();

    public static IdentityContextDAO getInstance() {
        return instance;
    }

    private AsyncIdentityContextDAO() {

        //
        String poolSizeConfig = "0";
        if (NumberUtils.isNumber(poolSizeConfig)) {
            poolSize = Integer.parseInt(poolSizeConfig);
            if (log.isDebugEnabled()) {
                log.debug("Thread pool size for Session Async DAO: " + poolSizeConfig);
            }
        }
        if (poolSize > 0) {
            ExecutorService threadPool = Executors.newFixedThreadPool(poolSize);
            for (int i = 0; i < poolSize; i++) {
                threadPool.execute(new IdentityContextPersistenceTask(identityContextJobs, persistentDAO));
            }
            threadPool = Executors.newFixedThreadPool(poolSize);
            for (int i = 0; i < poolSize; i++) {
                threadPool.execute(new IdentityContextPersistenceTask(identityContextJobs, persistentDAO));
            }
        }
    }

    @Override
    public void put(String key, IdentityMessageContext context) {

        if (poolSize > 0) {
            IdentityContextPersistenceTask.IdentityContextJob job = new IdentityContextPersistenceTask
                    .IdentityContextJob(key, context);
            identityContextJobs.add(job);
        } else {
            persistentDAO.put(key, context);
        }
    }

    @Override
    public IdentityMessageContext get(String key) {
        return persistentDAO.get(key);
    }

    @Override
    public void remove(String key) {

        if (poolSize > 0) {
            IdentityContextPersistenceTask.IdentityContextJob job = new IdentityContextPersistenceTask
                    .IdentityContextJob(key);
            identityContextJobs.add(job);
        } else {
            persistentDAO.remove(key);
        }
    }
}
