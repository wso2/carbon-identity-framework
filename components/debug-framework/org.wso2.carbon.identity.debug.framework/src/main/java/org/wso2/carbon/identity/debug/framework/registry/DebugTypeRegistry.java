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

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for debug callback handlers, keyed by protocol.
 */
public class DebugTypeRegistry {

    private static final DebugTypeRegistry INSTANCE = new DebugTypeRegistry();

    private final ConcurrentHashMap<String, DebugCallbackHandler> handlersByProtocol = new ConcurrentHashMap<>();

    private DebugTypeRegistry() {

    }

    public static DebugTypeRegistry getInstance() {

        return INSTANCE;
    }

    public void addDebugCallbackHandler(DebugCallbackHandler handler) {

        handlersByProtocol.put(handler.getSupportedProtocol().toLowerCase(Locale.ROOT), handler);
    }

    public void removeDebugCallbackHandler(DebugCallbackHandler handler) {

        handlersByProtocol.remove(handler.getSupportedProtocol().toLowerCase(Locale.ROOT));
    }

    public DebugCallbackHandler getHandler(String protocol) {

        return handlersByProtocol.get(protocol.toLowerCase(Locale.ROOT));
    }
}
