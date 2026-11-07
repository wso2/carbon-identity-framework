/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.flow.execution.engine.metadata;

/**
 * A phase of an executor that spans several nodes. Each node names the phase it runs in its executor metadata.
 */
public class FlowExecutorPhase {

    private final String name;
    private final String description;

    /**
     * Create a phase.
     *
     * @param name        Name of the phase, as set in the executor metadata of a node.
     * @param description Short description of the phase.
     */
    public FlowExecutorPhase(String name, String description) {

        this.name = name;
        this.description = description;
    }

    public String getName() {

        return name;
    }

    public String getDescription() {

        return description;
    }
}
