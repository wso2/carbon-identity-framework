/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.handler.hrd.impl;

import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.handler.hrd.HomeRealmDiscoverer;

/**
 * Default home realm discoverer implementation.
 */
public class DefaultHomeRealmDiscoverer implements HomeRealmDiscoverer {

    private static volatile DefaultHomeRealmDiscoverer instance;

    public static DefaultHomeRealmDiscoverer getInstance() {
        if (instance == null) {
            synchronized (DefaultHomeRealmDiscoverer.class) {
                if (instance == null) {
                    instance = new DefaultHomeRealmDiscoverer();
                }
            }
        }
        return instance;
    }

    @Override
    public String discover(String value) throws FrameworkException {
        return value;
    }
}
