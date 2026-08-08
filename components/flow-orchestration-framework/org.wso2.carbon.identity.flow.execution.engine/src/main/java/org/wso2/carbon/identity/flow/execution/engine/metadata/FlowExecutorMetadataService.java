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
 * engine, together with the metadata they declare.
 *
 * <p>Lives in the {@code metadata} package, which the engine OSGi bundle exports, so external bundles
 * such as the flow management API server can call it without reaching into the engine's
 * {@code internal} package. This mirrors
 * {@code org.wso2.carbon.identity.flow.extension.metadata.FlowExtensionContextTreeService}.</p>
 *
 * <p><b>Late binding: do not cache these results.</b> Executors are bound through an OSGi reference
 * with {@code MULTIPLE} cardinality and {@code DYNAMIC} policy, so a connector bundle placed in
 * {@code repository/components/dropins} can activate long after the server - and any consuming web
 * application - has started. Every method here reads the live executor registry at call time and
 * snapshots nothing, which is precisely what makes a late activating connector visible on the very
 * next call. A caller that caches the result defeats this.</p>
 */
public final class FlowExecutorMetadataService {

    private static final FlowExecutorMetadataService INSTANCE = new FlowExecutorMetadataService();

    private FlowExecutorMetadataService() {

    }

    /**
     * The shared instance. Stateless: it reads the live executor registry on every call, so the same
     * instance is safe to use from any thread and across bundle lifecycles.
     *
     * @return Singleton instance of this service.
     */
    public static FlowExecutorMetadataService getInstance() {

        return INSTANCE;
    }

    /**
     * Every registered executor, resolved and sorted by display name. Includes executors that are not
     * composer visible; use {@link #getComposerExecutors(String)} to build a step palette.
     *
     * @return Immutable list of resolved executor metadata. Never null.
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
     * Executors that are selectable as a step in the given flow type, i.e. those that are composer
     * visible and declared support for it.
     *
     * <p>An executor that declares no supported flow types is never returned. Declaring a flow type
     * is the opt in: it keeps executors that exist only as engine plumbing out of the palette.</p>
     *
     * @param flowType Flow type name, matched against {@link FlowTypes}. Unknown or blank values yield
     *                 an empty list.
     * @return Immutable list of resolved executor metadata. Never null.
     */
    public List<FlowExecutorInfo> getComposerExecutors(String flowType) {

        FlowTypes resolvedFlowType = toFlowType(flowType);
        if (resolvedFlowType == null) {
            return Collections.emptyList();
        }
        List<FlowExecutorInfo> filtered = new ArrayList<>();
        for (FlowExecutorInfo info : getExecutors()) {
            if (info.isVisibleInComposer() && info.supportsFlowType(resolvedFlowType)) {
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

        if (executorName == null || executorName.trim().isEmpty()) {
            return Optional.empty();
        }
        Executor executor = FlowExecutionEngineDataHolder.getInstance().getExecutors().get(executorName);
        return executor == null ? Optional.empty() : Optional.of(FlowExecutorMetadataResolver.resolve(executor));
    }

    /**
     * Whether an executor is currently registered under the given name. Intended for design time
     * validation, so that an unusable executor is reported when a flow is saved rather than only when
     * it is executed.
     *
     * @param executorName Executor name.
     * @return True if registered.
     */
    public boolean isExecutorRegistered(String executorName) {

        return executorName != null && !executorName.trim().isEmpty()
                && FlowExecutionEngineDataHolder.getInstance().getExecutors().containsKey(executorName);
    }

    /**
     * Names of all currently registered executors, sorted. Cheap: performs no metadata resolution.
     *
     * @return Immutable sorted set of executor names. Never null.
     */
    public Set<String> getRegisteredExecutorNames() {

        return Collections.unmodifiableSet(
                new TreeSet<>(FlowExecutionEngineDataHolder.getInstance().getExecutors().keySet()));
    }

    private FlowTypes toFlowType(String flowType) {

        if (flowType == null || flowType.trim().isEmpty()) {
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
