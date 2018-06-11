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

import org.wso2.carbon.identity.application.authentication.framework.model.LongWaitStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * The service holds long wait status.
 */
public class LongWaitStatusStoreService {

    private Map<String, LongWaitStatus> longWaitStatusMap = new HashMap<>();

    public void addWait(String sessionId, LongWaitStatus longWaitStatus) {

        if (sessionId != null) {
            longWaitStatusMap.put(sessionId, longWaitStatus);
        }
    }

    public LongWaitStatus getWait(String sessionId) {
        return longWaitStatusMap.get(sessionId);
    }

    public LongWaitStatus removeWait(String sessionId) {
        return longWaitStatusMap.remove(sessionId);
    }
}
