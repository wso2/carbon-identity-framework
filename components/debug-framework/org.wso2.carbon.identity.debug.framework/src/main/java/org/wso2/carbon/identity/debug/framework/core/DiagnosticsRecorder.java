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

package org.wso2.carbon.identity.debug.framework.core;

import org.wso2.carbon.identity.debug.framework.DebugFrameworkConstants;
import org.wso2.carbon.identity.debug.framework.model.DebugContext;
import org.wso2.carbon.identity.debug.framework.model.DiagnosticEvent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Records and retrieves diagnostic events for debug flows.
 */
public class DiagnosticsRecorder {

    /**
     * Record a diagnostic event in the given debug context.
     *
     * @param context Debug context holding flow properties.
     * @param event   Diagnostic event to record.
     */
    public void record(DebugContext context, DiagnosticEvent event) {

        if (context == null) {
            return;
        }
        getOrCreateDiagnostics(context).addFirst(normalize(event));
    }

    /**
     * Retrieve diagnostics recorded in the given debug context.
     *
     * @param context Debug context holding flow properties.
     * @return Snapshot list of diagnostics in latest-first order.
     */
    public List<DiagnosticEvent> getDiagnostics(DebugContext context) {

        if (context == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(getOrCreateDiagnostics(context));
    }

    @SuppressWarnings("unchecked")
    private Deque<DiagnosticEvent> getOrCreateDiagnostics(DebugContext context) {

        Object existing = context.getProperty(DebugFrameworkConstants.DEBUG_DIAGNOSTICS);

        if (existing instanceof Deque) {
            return (Deque<DiagnosticEvent>) existing;
        }

        // Post-deserialization the timeline may arrive as a generic Collection of Maps.
        if (existing instanceof Collection) {
            Deque<DiagnosticEvent> timeline = coerceCollection((Collection<Object>) existing);
            context.setProperty(DebugFrameworkConstants.DEBUG_DIAGNOSTICS, timeline);
            return timeline;
        }

        Deque<DiagnosticEvent> timeline = new ArrayDeque<>();
        context.setProperty(DebugFrameworkConstants.DEBUG_DIAGNOSTICS, timeline);
        return timeline;
    }

    private Deque<DiagnosticEvent> coerceCollection(Collection<Object> diagnostics) {

        Deque<DiagnosticEvent> timeline = new ArrayDeque<>();
        for (Object event : diagnostics) {
            DiagnosticEvent diagnosticEvent = convertToEvent(event);
            if (diagnosticEvent != null) {
                timeline.addLast(normalize(diagnosticEvent));
            }
        }
        return timeline;
    }

    @SuppressWarnings("unchecked")
    private DiagnosticEvent convertToEvent(Object event) {

        if (event instanceof DiagnosticEvent) {
            return (DiagnosticEvent) event;
        }
        if (event instanceof Map) {
            return DiagnosticEvent.fromMap((Map<String, Object>) event);
        }
        return null;
    }

    private DiagnosticEvent normalize(DiagnosticEvent event) {

        if (event == null) {
            return DiagnosticEvent.builder().timestamp(System.currentTimeMillis()).build();
        }

        DiagnosticEvent normalizedEvent = DiagnosticEvent.builder()
                .stage(event.getStage())
                .status(event.getStatus())
                .message(event.getMessage())
                .timestamp(event.getTimestamp() > 0 ? event.getTimestamp() : System.currentTimeMillis())
                .errorCode(event.getErrorCode())
                .errorDescription(event.getErrorDescription())
                .build();

        if (event.getDetails() != null && !event.getDetails().isEmpty()) {
            normalizedEvent.setDetails(new LinkedHashMap<>(event.getDetails()));
        }
        return normalizedEvent;
    }
}
