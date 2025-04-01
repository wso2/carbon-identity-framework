/*
 * Copyright (c) 2013-2023, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

import org.wso2.carbon.identity.application.authentication.framework.context.OptimizedSessionContext;
import org.wso2.carbon.identity.application.authentication.framework.context.SessionContext;
import org.wso2.carbon.identity.application.authentication.framework.store.SessionContextDO;
import org.wso2.carbon.identity.core.cache.CacheEntry;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Wrapper object to cache {@link SessionContext}
 */
public class SessionContextCacheEntry extends CacheEntry {

    private static final long serialVersionUID = 42165605438157753L;

    private String contextIdentifier;
    SessionContext context;
    private OptimizedSessionContext optimizedSessionContext;
    String loggedInUser;
    private long accessedTime;
    private final Properties properties = new Properties();

    public void addProperty(Object propName, Object propValue) {

        properties.put(propName, propValue);
    }

    public Object getProperty(Object propName) {

        return properties.get(propName);
    }

    public SessionContextCacheEntry() {
        setAccessedTime();
    }

    public SessionContextCacheEntry(SessionContextDO sessionContextDO) {
        SessionContextCacheEntry entry = (SessionContextCacheEntry) sessionContextDO.getEntry();
        this.contextIdentifier = entry.getContextIdentifier();
        this.context = entry.getContext();
        this.optimizedSessionContext = entry.getOptimizedSessionContext();
        this.loggedInUser = entry.getLoggedInUser();
        this.setAccessedTime(TimeUnit.NANOSECONDS.toMillis(sessionContextDO.getNanoTime()));
    }

    public SessionContextCacheEntry(SessionContextCacheEntry entry, OptimizedSessionContext optimizedSessionContext) {

        this.contextIdentifier = entry.getContextIdentifier();
        this.context = null;
        this.optimizedSessionContext = optimizedSessionContext;
        this.loggedInUser = entry.getLoggedInUser();
        this.accessedTime = entry.getAccessedTime();
        this.setValidityPeriod(entry.getValidityPeriod());
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

    OptimizedSessionContext getOptimizedSessionContext() {

        return optimizedSessionContext;
    }

    void resetOptimizedSessionContext() {

        this.optimizedSessionContext = null;
    }

    public String getContextIdentifier() {

        return this.contextIdentifier;
    }

    public void setContextIdentifier(String key) {

        this.contextIdentifier = key;
    }
}
