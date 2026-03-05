/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.flow.execution.engine.inflow.extension.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Access Configuration for In-Flow Extension actions.
 * <p>
 * Defines which parts of the flow context are exposed to the external service
 * and what operations the service is allowed to perform.
 * </p>
 *
 * <ul>
 *   <li>{@code expose} – hierarchical path prefixes that control which context data is sent
 *       in the action execution request (e.g. {@code "/user/claims/"}, {@code "/flow/properties/"}).</li>
 *   <li>{@code allowedOperations} – a structured list of operation descriptors, where each entry
 *       specifies an operation type ({@code "op"}) and the paths it applies to ({@code "paths"}).</li>
 * </ul>
 */
public class AccessConfig {

    private final List<String> expose;
    private final List<Map<String, Object>> allowedOperations;

    /**
     * Constructs an AccessConfig with the given expose paths and allowed operations.
     *
     * @param expose            List of hierarchical path prefixes to expose. May be {@code null}.
     * @param allowedOperations List of operation descriptors. May be {@code null}.
     */
    public AccessConfig(List<String> expose, List<Map<String, Object>> allowedOperations) {

        this.expose = expose != null ? Collections.unmodifiableList(expose) : null;
        this.allowedOperations = allowedOperations != null ? Collections.unmodifiableList(allowedOperations) : null;
    }

    /**
     * Returns the list of hierarchical path prefixes that define what context data is exposed.
     *
     * @return Unmodifiable list of expose path prefixes, or {@code null} if not configured.
     */
    public List<String> getExpose() {

        return expose;
    }

    /**
     * Returns the list of allowed operation descriptors.
     * Each entry is a map with at least {@code "op"} (operation type) and {@code "paths"} keys.
     *
     * @return Unmodifiable list of operation descriptors, or {@code null} if not configured.
     */
    public List<Map<String, Object>> getAllowedOperations() {

        return allowedOperations;
    }
}
