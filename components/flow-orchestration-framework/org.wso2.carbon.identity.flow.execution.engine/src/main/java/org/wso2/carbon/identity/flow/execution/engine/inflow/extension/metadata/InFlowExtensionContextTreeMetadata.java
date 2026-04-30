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

package org.wso2.carbon.identity.flow.execution.engine.inflow.extension.metadata;

import java.util.Collections;
import java.util.List;

/**
 * Metadata response served by the {@code GET /flow/in-flow-extension/context-tree} endpoint.
 * Carries the controlled context tree (filtered by {@code FlowContextHandoverPolicy}) plus
 * per-flow-type policy flags that the Console UI uses to gate the access-config editor.
 */
public class InFlowExtensionContextTreeMetadata {

    private final String flowType;
    private final List<InFlowExtensionContextTreeNode> contextTree;
    private final boolean redirectionEnabled;
    private final boolean allowReadOnlyClaimsModification;

    public InFlowExtensionContextTreeMetadata(String flowType,
                                              List<InFlowExtensionContextTreeNode> contextTree,
                                              boolean redirectionEnabled,
                                              boolean allowReadOnlyClaimsModification) {

        this.flowType = flowType;
        this.contextTree = contextTree != null ? Collections.unmodifiableList(contextTree)
                : Collections.emptyList();
        this.redirectionEnabled = redirectionEnabled;
        this.allowReadOnlyClaimsModification = allowReadOnlyClaimsModification;
    }

    /**
     * @return The flow type this metadata applies to. {@code null} indicates the default tree.
     */
    public String getFlowType() {

        return flowType;
    }

    public List<InFlowExtensionContextTreeNode> getContextTree() {

        return contextTree;
    }

    public boolean isRedirectionEnabled() {

        return redirectionEnabled;
    }

    public boolean isAllowReadOnlyClaimsModification() {

        return allowReadOnlyClaimsModification;
    }
}
