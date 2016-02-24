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

package org.wso2.carbon.identity.application.authentication.framework.cache;

import org.wso2.carbon.identity.application.authentication.framework.context.SessionContext;
import org.wso2.carbon.identity.application.authentication.framework.store.SessionContextDO;
import org.wso2.carbon.identity.application.common.cache.CacheEntry;

import java.util.concurrent.TimeUnit;

public class SessionContextCacheEntry extends CacheEntry {

    private static final long serialVersionUID = 42165605438157753L;

    SessionContext context;
    String loggedInUser;
    private long accessedTime;

    public SessionContextCacheEntry() {
        setAccessedTime();
    }

    public SessionContextCacheEntry(SessionContextDO sessionContextDO) {
        SessionContextCacheEntry entry = (SessionContextCacheEntry) sessionContextDO.getEntry();
        this.context = entry.getContext();
        this.loggedInUser = entry.getLoggedInUser();
        this.setAccessedTime(sessionContextDO.getTimestamp().getTime());
    }

    public String getLoggedInUser() {
        return loggedInUser;
    }

    public void setLoggedInUser(String loggedInUser) {
        this.loggedInUser = loggedInUser;
    }

    public SessionContext getContext() {
        return context;
    }

    public void setContext(SessionContext context) {
        this.context = context;
    }

    public void setAccessedTime() {
        this.accessedTime = System.currentTimeMillis();
    }

    private void setAccessedTime(long accessedTime) {
        this.accessedTime = accessedTime;
    }

    public long getAccessedTime() {
        return this.accessedTime;
    }
}
