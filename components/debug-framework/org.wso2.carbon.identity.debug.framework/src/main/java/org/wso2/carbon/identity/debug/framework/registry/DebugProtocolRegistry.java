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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Registry for debug callback handlers.
 */
public class DebugProtocolRegistry {

    private static final DebugProtocolRegistry INSTANCE = new DebugProtocolRegistry();

    private final List<DebugCallbackHandler> debugCallbackHandlers = new CopyOnWriteArrayList<>();

    private DebugProtocolRegistry() {

    }

    public static DebugProtocolRegistry getInstance() {

        return INSTANCE;
    }

    public void addDebugCallbackHandler(DebugCallbackHandler handler) {

        if (handler == null) {
            return;
        }
        if (!debugCallbackHandlers.contains(handler)) {
            debugCallbackHandlers.add(handler);
        }
    }

    public void removeDebugCallbackHandler(DebugCallbackHandler handler) {

        if (handler == null) {
            return;
        }
        debugCallbackHandlers.remove(handler);
    }

    public List<DebugCallbackHandler> getDebugCallbackHandlers() {

        return new ArrayList<>(debugCallbackHandlers);
    }
}
