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

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.flow.execution.engine.graph.Executor;
import org.wso2.carbon.identity.flow.execution.engine.internal.FlowExecutionEngineDataHolder;
import org.wso2.carbon.identity.flow.execution.engine.internal.FlowExecutorMetadataResolver;
import org.wso2.carbon.identity.flow.mgt.Constants.FlowTypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * Public API entry point for discovering the {@link Executor}s currently registered with the flow
 * engine and the metadata they declare.
 */
public final class FlowExecutorMetadataService {

    private static final FlowExecutorMetadataService INSTANCE = new FlowExecutorMetadataService();

    private FlowExecutorMetadataService() {

    }

    /**
     * The shared instance that reads the live executor registry on every call, so the same
     * instance is safe to use from any thread and across bundle lifecycles.
     *
     * @return Singleton instance of this service.
     */
    public static FlowExecutorMetadataService getInstance() {

        return INSTANCE;
    }

    /**
     * Every registered executor, resolved and sorted by display name.
     *
     * @return Immutable list of resolved executor metadata.
     */
    public List<FlowExecutorInfo> getExecutors() {

        List<FlowExecutorInfo> resolved = new ArrayList<>();
        for (Executor executor : FlowExecutionEngineDataHolder.getInstance().getExecutors().values()) {
            resolved.add(FlowExecutorMetadataResolver.resolve(executor));
        }
        resolved.sort(Comparator.comparing(info -> info.getDisplayName() == null
                ? "" : info.getDisplayName().toLowerCase(Locale.ENGLISH)));
        return Collections.unmodifiableList(resolved);
    }

    /**
     * Executors that are selectable as a step in the given flow type.
     *
     * @param flowType Flow type name, matched against {@link FlowTypes}.
     * @return Immutable list of resolved executor metadata.
     */
    public List<FlowExecutorInfo> getSupportedExtensionExecutors(String flowType) {

        FlowTypes resolvedFlowType = toFlowType(flowType);
        if (resolvedFlowType == null) {
            return Collections.emptyList();
        }
        List<FlowExecutorInfo> filtered = new ArrayList<>();
        for (FlowExecutorInfo info : getExecutors()) {
            if (info.supportsFlowType(resolvedFlowType)) {
                filtered.add(info);
            }
        }
        return Collections.unmodifiableList(filtered);
    }

    /**
     * Resolved metadata for a single executor.
     *
     * @param executorName Executor name as returned by {@link Executor#getName()}.
     * @return The metadata, or empty if no executor is registered under that name.
     */
    public Optional<FlowExecutorInfo> getExecutor(String executorName) {

        if (StringUtils.isBlank(executorName)) {
            return Optional.empty();
        }
        Executor executor = FlowExecutionEngineDataHolder.getInstance().getExecutors().get(executorName);
        return executor == null ? Optional.empty() : Optional.of(FlowExecutorMetadataResolver.resolve(executor));
    }

    /**
     * Whether an executor is currently registered under the given name.
     *
     * @param executorName Executor name.
     * @return True if registered.
     */
    public boolean isExecutorRegistered(String executorName) {

        return StringUtils.isNotBlank(executorName)
                && FlowExecutionEngineDataHolder.getInstance().getExecutors().containsKey(executorName);
    }

    /**
     * Names of all currently registered executors, sorted.
     *
     * @return Immutable sorted set of executor names.
     */
    public Set<String> getRegisteredExecutorNames() {

        return Collections.unmodifiableSet(
                new TreeSet<>(FlowExecutionEngineDataHolder.getInstance().getExecutors().keySet()));
    }

    private FlowTypes toFlowType(String flowType) {

        if (StringUtils.isBlank(flowType)) {
            return null;
        }
        for (FlowTypes type : FlowTypes.values()) {
            if (type.name().equalsIgnoreCase(flowType) || type.getType().equalsIgnoreCase(flowType)) {
                return type;
            }
        }
        return null;
    }
}
