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
 * Resolves an {@link Executor} into a {@link FlowExecutorInfo}: reads the two SPI hooks defensively
 * and derives the values the executor cannot declare about itself. Defaulting for anything left
 * undeclared is {@link FlowExecutorInfo}'s job, not this class's.
 *
 * <p><b>Backward compatibility contract.</b> An executor that declares nothing gets: an empty set of
 * supported flow types (so it is not offered as a composer step), a display name falling back to
 * {@link Executor#getName()}, and
 * {@code idpRequired} inferred from {@code instanceof AuthenticationExecutor}. Executors written
 * before this SPI existed therefore keep working unchanged at execution time; they simply do not
 * advertise themselves, which is what lets consumers keep their own curated lists as a baseline.</p>
 *
 * <p>Deliberately in the {@code internal} package: this is implementation detail, not API.</p>
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
                .declaredMetadata(declaredMetadata(executor))
                .supportedFlowTypes(declaredFlowTypes(executor))
                .idpRequired(executor instanceof AuthenticationExecutor)
                .build();
    }

    /**
     * An executor is third party code that may be contributed by a bundle dropped into
     * repository/components/dropins, so a faulty implementation must not break metadata resolution
     * for every other executor. {@link LinkageError} is caught alongside runtime exceptions because
     * that is how a dropin built against a different framework version fails.
     */
    private static FlowExecutorMetadata declaredMetadata(Executor executor) {

        try {
            return executor.getExecutorMetadata();
        } catch (RuntimeException | LinkageError e) {
            LOG.error("Failed to read declared metadata from executor: " + executor.getName()
                    + ". Falling back to derived defaults.");
            return null;
        }
    }

    private static Set<FlowTypes> declaredFlowTypes(Executor executor) {

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
