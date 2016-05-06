/*
 *
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
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

package org.wso2.carbon.identity.notification.mgt.internal;

import java.util.concurrent.ExecutorService;

public class NotificationManagementServiceDataHolder {

    private static NotificationManagementServiceDataHolder instance = new NotificationManagementServiceDataHolder();

    /**
     * Thread pool for executing event distribution
     */
    private ExecutorService threadPool;

    private NotificationManagementServiceDataHolder() {
    }

    public static NotificationManagementServiceDataHolder getInstance() {
        return instance;
    }

    public ExecutorService getThreadPool() {
        return threadPool;
    }

    public NotificationManagementServiceDataHolder setThreadPool(ExecutorService threadPool) {
        this.threadPool = threadPool;
        return this;
    }

}
