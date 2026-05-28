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

import org.wso2.carbon.identity.debug.framework.extension.DebugCallbackHandler;
import org.wso2.carbon.identity.debug.framework.extension.DebugResourceHandler;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for debug handlers.
 */
public class DebugTypeRegistry {

    private static final DebugTypeRegistry INSTANCE = new DebugTypeRegistry();

    private final ConcurrentHashMap<String, DebugCallbackHandler> callbackHandlersByType = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, DebugResourceHandler> handlersByResourceType = new ConcurrentHashMap<>();

    private DebugTypeRegistry() {

    }

    public static DebugTypeRegistry getInstance() {

        return INSTANCE;
    }

    public void addCallbackHandler(String type, DebugCallbackHandler handler) {

        callbackHandlersByType.put(type.toLowerCase(Locale.ROOT), handler);
    }

    public void removeCallbackHandler(String type) {

        callbackHandlersByType.remove(type.toLowerCase(Locale.ROOT));
    }

    public DebugCallbackHandler getCallbackHandler(String type) {

        return callbackHandlersByType.get(type.toLowerCase(Locale.ROOT));
    }

    public void addDebugResourceHandler(String resourceType, DebugResourceHandler handler) {

        handlersByResourceType.put(resourceType.toLowerCase(Locale.ROOT), handler);
    }

    public void removeDebugResourceHandler(String resourceType) {

        handlersByResourceType.remove(resourceType.toLowerCase(Locale.ROOT));
    }

    public DebugResourceHandler getResourceHandler(String resourceType) {

        return handlersByResourceType.get(resourceType.toLowerCase(Locale.ROOT));
    }
}
