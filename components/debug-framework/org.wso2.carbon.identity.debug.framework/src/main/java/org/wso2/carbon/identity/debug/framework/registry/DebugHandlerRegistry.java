/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.debug.framework.registry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.debug.framework.extension.DebugResourceHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for dynamic registration and lookup of debug resource handlers.
 */
public class DebugHandlerRegistry {

    private static final Log LOG = LogFactory.getLog(DebugHandlerRegistry.class);
    private static final DebugHandlerRegistry INSTANCE = new DebugHandlerRegistry();
    private final Map<String, DebugResourceHandler> handlers = new ConcurrentHashMap<>();

    private DebugHandlerRegistry() {

    }

    public static DebugHandlerRegistry getInstance() {

        return INSTANCE;
    }

    public void register(String resourceType, DebugResourceHandler handler) {

        handlers.put(resourceType, handler);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Registered debug resource handler for type: " + resourceType);
        }
    }

    public void unregister(String resourceType) {

        handlers.remove(resourceType);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Unregistered debug resource handler for type: " + resourceType);
        }
    }

    public DebugResourceHandler getHandler(String resourceType) {

        return handlers.get(resourceType);
    }
}
