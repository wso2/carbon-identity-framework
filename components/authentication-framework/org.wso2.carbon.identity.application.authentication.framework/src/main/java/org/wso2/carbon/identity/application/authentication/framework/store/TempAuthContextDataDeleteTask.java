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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.BlockingDeque;

public class TempAuthContextDataDeleteTask implements Runnable {

    private static final Log log = LogFactory.getLog(TempAuthContextDataDeleteTask.class);
    private BlockingDeque<SessionContextDO> sessionContextDeleteTempQueue;
    private static volatile boolean running;

    public TempAuthContextDataDeleteTask(BlockingDeque<SessionContextDO> sessionContextDeleteTempQueue) {
        this.sessionContextDeleteTempQueue = sessionContextDeleteTempQueue;
    }

    @Override
    public void run() {

        if (log.isDebugEnabled()) {
            log.debug("Starting temporary authentication context data delete task");
        }

        running = true;
        while (running) {

            try {
                SessionContextDO sessionContextDO = sessionContextDeleteTempQueue.take();
                if (sessionContextDO != null) {
                    SessionDataStore.getInstance().removeTempAuthnContextData(sessionContextDO.getKey(),
                            sessionContextDO.getType());
                }
            } catch (InterruptedException e) {
                //ignore
                log.error("Error while running temporary data delete task: ", e);
            }
        }
    }

    public static void shutdown() {
        running = false;
    }

}
