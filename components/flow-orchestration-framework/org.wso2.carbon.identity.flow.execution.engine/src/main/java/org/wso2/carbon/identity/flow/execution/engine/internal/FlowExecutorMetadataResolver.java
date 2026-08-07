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

package org.wso2.carbon.identity.flow.execution.engine.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.flow.execution.engine.graph.AuthenticationExecutor;
import org.wso2.carbon.identity.flow.execution.engine.graph.Executor;
import org.wso2.carbon.identity.flow.execution.engine.metadata.FlowExecutorInfo;
import org.wso2.carbon.identity.flow.execution.engine.metadata.FlowExecutorMetadata;
import org.wso2.carbon.identity.flow.mgt.Constants.FlowTypes;

import java.util.Collections;
import java.util.Set;

/**
 * Resolves a dynamically registered {@link Executor} into a {@link FlowExecutorInfo}.
 */
public class FlowExecutorMetadataResolver {

    private static final Log LOG = LogFactory.getLog(FlowExecutorMetadataResolver.class);

    private FlowExecutorMetadataResolver() {

    }

    /**
     * Resolve the given executor into its fully populated metadata view.
     *
     * @param executor Executor to resolve. Must not be null.
     * @return Resolved executor info.
     */
    public static FlowExecutorInfo resolve(Executor executor) {

        return FlowExecutorInfo.builder()
                .name(executor.getName())
                .metadata(getMetadata(executor))
                .supportedFlowTypes(getSupportedFlowTypes(executor))
                .idpRequired(executor instanceof AuthenticationExecutor)
                .build();
    }

    /**
     * Reads the metadata declared by dynamically registered executors.
     */
    private static FlowExecutorMetadata getMetadata(Executor executor) {

        try {
            return executor.getExecutorMetadata();
        } catch (RuntimeException | LinkageError e) {
            LOG.error("Failed to read metadata from executor: " + executor.getName()
                    + ". Falling back to derived defaults.");
            return null;
        }
    }

    /**
     * Reads the supported flow types declared by dynamically registered executors.
     */
    private static Set<FlowTypes> getSupportedFlowTypes(Executor executor) {

        try {
            Set<FlowTypes> flowTypes = executor.getSupportedFlowTypes();
            return flowTypes == null ? Collections.emptySet() : flowTypes;
        } catch (RuntimeException | LinkageError e) {
            LOG.error("Failed to read supported flow types from executor: " + executor.getName()
                    + ". Treating it as supporting no flow type.");
            return Collections.emptySet();
        }
    }
}
