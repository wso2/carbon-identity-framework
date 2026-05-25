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

package org.wso2.carbon.identity.flow.extension.metadata;

import org.wso2.carbon.identity.flow.extension.model.FlowContextHandoverConfig;

/**
 * Public-API entry point for retrieving the controlled In-Flow Extension context tree.
 * Lives in the {@code metadata} package (which is exported by the engine OSGi bundle), so
 * external bundles such as the flow-management API server can call it without depending on
 * the engine's {@code internal} package.
 *
 * <p>Delegates config lookup to {@link FlowExecutionEngineDataHolder}, which owns the
 * engine-level {@link FlowContextHandoverConfig} singleton.</p>
 */
public final class FlowExtensionContextTreeService {

    private static final FlowExtensionContextTreeService INSTANCE = new FlowExtensionContextTreeService();

    private FlowExtensionContextTreeService() {

    }

    public static FlowExtensionContextTreeService getInstance() {

        return INSTANCE;
    }

    /**
     * Build the controlled context tree for the given flow type.
     *
     * @param flowType the flow type, or null for the default tree.
     * @return the metadata DTO carrying the pruned tree + per-flow-type policy flags.
     */
    public FlowExtensionContextTreeMetadata buildContextTree(String flowType) {

        return new FlowExtensionContextTreeBuilder(
                FlowContextHandoverConfig.defaultPolicy()).build(flowType);
    }
}
