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

package org.wso2.carbon.identity.debug.framework.internal;

import org.wso2.carbon.identity.debug.framework.listener.DebugExecutionListener;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Data holder for Debug Framework Service Component.
 * This holds references to OSGi services that are required by the debug framework.
 */
public class DebugFrameworkServiceDataHolder {

    /**
     * List of registered debug execution listeners.
     */
    private final List<DebugExecutionListener> debugExecutionListeners = new CopyOnWriteArrayList<>();

    /**
     * Private constructor to prevent instantiation.
     */
    private DebugFrameworkServiceDataHolder() {

    }

    /**
     * Returns the singleton instance of the data holder.
     *
     * @return the singleton instance.
     */
    public static DebugFrameworkServiceDataHolder getInstance() {

        return InstanceHolder.INSTANCE;
    }

    private static class InstanceHolder {

        private static final DebugFrameworkServiceDataHolder INSTANCE =
                new DebugFrameworkServiceDataHolder();
    }

    /**
     * Gets the list of debug execution listeners.
     *
     * @return List of debug execution listeners.
     */
    public List<DebugExecutionListener> getDebugExecutionListeners() {

        return debugExecutionListeners;
    }

    /**
     * Adds a debug execution listener.
     *
     * @param listener The debug execution listener to add.
     */
    public void addDebugExecutionListener(DebugExecutionListener listener) {

        if (listener != null) {
            this.debugExecutionListeners.add(listener);
            this.debugExecutionListeners.sort(Comparator.comparingInt(DebugExecutionListener::getExecutionOrderId));
        }
    }

    /**
     * Removes a debug execution listener.
     *
     * @param listener The debug execution listener to remove.
     */
    public void removeDebugExecutionListener(DebugExecutionListener listener) {

        if (listener != null) {
            this.debugExecutionListeners.remove(listener);
        }
    }

}
