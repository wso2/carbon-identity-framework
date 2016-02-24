/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.BlockingDeque;

/**
 * Task to persist and remove session data
 */
public class SessionDataPersistTask implements Runnable {

    private static final Log log = LogFactory.getLog(SessionDataPersistTask.class);
    private BlockingDeque<SessionContextDO> sessionContextQueue;

    public SessionDataPersistTask(BlockingDeque<SessionContextDO> sessionContextQueue) {
        this.sessionContextQueue = sessionContextQueue;
    }

    @Override
    public void run() {

        log.debug("Session Context persist consumer is started");

        while (true) {

            try {
                SessionContextDO sessionContextDO = sessionContextQueue.take();
                if (sessionContextDO != null) {
                    if (sessionContextDO.getEntry() == null) {
                        log.debug("Session Data removing Task is started to run");
                        SessionDataStore.getInstance().removeSessionData(
                                sessionContextDO.getKey(), sessionContextDO.getType(), sessionContextDO.getTimestamp());
                    } else {
                        log.debug("Session Data persisting Task is started to run");
                        SessionDataStore.getInstance().persistSessionData(
                                sessionContextDO.getKey(),sessionContextDO.getType(),
                                sessionContextDO.getEntry(), sessionContextDO.getTimestamp(),
                                sessionContextDO.getTenantId());
                    }
                }
            } catch (InterruptedException e) {
                //ignore
                log.error(e);
            }

        }
    }
}
