/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.notification.mgt;

/**
 * Constants class for message management component
 */
@SuppressWarnings("unused")
public class NotificationMgtConstants {

    public static final String MODULE_CONFIG_FILE = "msg-mgt.properties";
    public static final int THREAD_POOL_DEFAULT_SIZE = 5;

    private NotificationMgtConstants() {
    }

    public static class Configs {
        public static final String MODULE_NAME = "module.name";
        public static final String SUBSCRIPTION = "subscription";
        public static final String THREAD_POOL_SIZE = "threadPool.size";

        private Configs() {
        }
    }
}
