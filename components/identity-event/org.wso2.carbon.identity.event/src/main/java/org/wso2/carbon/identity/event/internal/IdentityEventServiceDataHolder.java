/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.event.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.event.services.IdentityEventService;
import org.wso2.carbon.idp.mgt.IdpManager;

import java.util.concurrent.ExecutorService;

public class IdentityEventServiceDataHolder {

    private static IdentityEventServiceDataHolder instance = new IdentityEventServiceDataHolder();
    private static Log log = LogFactory.getLog(IdentityEventServiceDataHolder.class);
    private IdpManager idpManager;
    private IdentityEventService eventMgtService;
    private ExecutorService threadPool;

    public ExecutorService getThreadPool() {
        return threadPool;
    }

    public void setThreadPool(ExecutorService threadPool) {
        this.threadPool = threadPool;
    }

    public IdpManager getIdpManager() {
        return idpManager;
    }

    public void setIdpManager(IdpManager idpManager) {
        this.idpManager = idpManager;
    }

    private IdentityEventServiceDataHolder() {

    }

    public static IdentityEventServiceDataHolder getInstance() {

        return instance;
    }

    public IdentityEventService getEventMgtService() {
        return eventMgtService;
    }

    public void setEventMgtService(IdentityEventService eventMgtService) {
        this.eventMgtService = eventMgtService;
    }
}
