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

package org.wso2.carbon.identity.application.authentication.framework.store;

import org.wso2.carbon.identity.application.authentication.framework.dao.LongWaitStatusDAO;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.model.LongWaitStatus;

import java.sql.Timestamp;
import java.util.Date;

/**
 * The service holds long wait status.
 */
public class LongWaitStatusStoreService {

    private LongWaitStatusDAO statusDAO;
    private int connectionTimeout;

    public LongWaitStatusStoreService(LongWaitStatusDAO statusDAO, int connectionTimeout) {

        this.statusDAO = statusDAO;
        this.connectionTimeout = connectionTimeout;
    }

    public void addWait(int tenantId, String sessionId, LongWaitStatus longWaitStatus) throws FrameworkException {

        Date now = new Date();
        Timestamp createdTime = new Timestamp(now.getTime());
        Timestamp expireTime = new Timestamp(now.getTime() + connectionTimeout);
        statusDAO.addWaitStatus(tenantId, sessionId, longWaitStatus, createdTime, expireTime);
    }

    public LongWaitStatus getWait(String sessionId) throws FrameworkException {

        return statusDAO.getWaitStatus(sessionId);
    }

    public void removeWait(String sessionId) throws FrameworkException {

        statusDAO.removeWaitStatus(sessionId);
    }
}
