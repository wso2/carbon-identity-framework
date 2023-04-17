/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.OptimizedSessionContext;
import org.wso2.carbon.identity.application.authentication.framework.context.SessionContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.session.storage.SessionDataStorageOptimizationException;

/**
 * This class is used to optimize the Session Context before storing it and again loaded it with objects.
 */
public class SessionContextLoader {

    private static final SessionContextLoader instance = new SessionContextLoader();
    private static final Log log = LogFactory.getLog(SessionContextLoader.class);

    private SessionContextLoader() { }

    /**
     * Singleton method of the Session Context Loader class.
     *
     * @return Session Context Loader object
     */
    public static SessionContextLoader getInstance() {

        return instance;
    }


    public SessionContextCacheEntry optimizeSessionContextCacheEntry(SessionContextCacheEntry entry)
            throws SessionDataStorageOptimizationException {

        if (log.isDebugEnabled()) {
            log.debug(String.format("Optimization process for the session context with context id: %s has started.",
                    entry.getContextIdentifier()));
        }
        OptimizedSessionContext optContext = new OptimizedSessionContext(entry.getContext());
        return new SessionContextCacheEntry(entry, optContext);
    }

    public SessionContextCacheEntry loadSessionContextCacheEntry(SessionContextCacheEntry entry)
            throws SessionDataStorageOptimizationException {

        if (log.isDebugEnabled()) {
            log.debug(String.format("Loading process for the session context with context id: %s has started.",
                    entry.getContextIdentifier()));
        }
        SessionContext context = entry.getOptimizedSessionContext().getSessionContext();
        entry.setContext(context);
        entry.resetOptimizedSessionContext();
        return entry;
    }
}
