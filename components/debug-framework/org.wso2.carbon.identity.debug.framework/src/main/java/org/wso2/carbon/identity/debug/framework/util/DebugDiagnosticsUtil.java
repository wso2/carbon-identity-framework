/*
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

package org.wso2.carbon.identity.debug.framework.util;

import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.debug.framework.core.DiagnosticsRecorder;
import org.wso2.carbon.identity.debug.framework.model.DebugContext;
import org.wso2.carbon.identity.debug.framework.model.DiagnosticEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility for recording and retrieving protocol-agnostic debug diagnostic events.
 * Keeps a simple static API while diagnostics are internally modeled as
 * first-class {@link DiagnosticEvent} instances.
 * Map conversion is used only at integration boundaries where map payloads are required.
 */
public final class DebugDiagnosticsUtil {

    private static final DiagnosticsRecorder DIAGNOSTICS_RECORDER = new DiagnosticsRecorder();

    private DebugDiagnosticsUtil() {

    }

    /**
     * Records a diagnostic event with stage, status, and message for a debug context.
     *
     * @param context The debug context.
     * @param stage The diagnostic stage.
     * @param status The diagnostic status.
     * @param message The diagnostic message.
     */
    public static void recordEvent(DebugContext context, String stage, String status, String message) {

        recordEvent(context, stage, status, message, null);
    }

    /**
     * Records a diagnostic event with details for a debug context.
     *
     * @param context The debug context.
     * @param stage The diagnostic stage.
     * @param status The diagnostic status.
     * @param message The diagnostic message.
     * @param details Additional event details.
     */
    public static void recordEvent(DebugContext context, String stage, String status, String message,
            Map<String, Object> details) {

        DIAGNOSTICS_RECORDER.record(context, buildEvent(stage, status, message, details));
    }

    /**
     * Records a typed diagnostic event for a debug context.
     *
     * @param context The debug context.
     * @param event Diagnostic event.
     */
    public static void recordEvent(DebugContext context, DiagnosticEvent event) {

        DIAGNOSTICS_RECORDER.record(context, event);
    }

    /**
     * Records a diagnostic event with stage, status, and message for an authentication context.
     *
     * @param context The authentication context.
     * @param stage The diagnostic stage.
     * @param status The diagnostic status.
     * @param message The diagnostic message.
     */
    public static void recordEvent(AuthenticationContext context, String stage, String status, String message) {

        recordEvent(context, stage, status, message, null);
    }

    /**
     * Records a diagnostic event with details for an authentication context.
     *
     * @param context The authentication context.
     * @param stage The diagnostic stage.
     * @param status The diagnostic status.
     * @param message The diagnostic message.
     * @param details Additional event details.
     */
    public static void recordEvent(AuthenticationContext context, String stage, String status, String message,
            Map<String, Object> details) {

        DIAGNOSTICS_RECORDER.record(context, buildEvent(stage, status, message, details));
    }

    /**
     * Records a typed diagnostic event for an authentication context.
     *
     * @param context The authentication context.
     * @param event Diagnostic event.
     */
    public static void recordEvent(AuthenticationContext context, DiagnosticEvent event) {

        DIAGNOSTICS_RECORDER.record(context, event);
    }

    /**
     * Returns diagnostics recorded in the debug context.
     *
     * @param context The debug context.
     * @return Diagnostic events.
     */
    public static List<Map<String, Object>> getDiagnostics(DebugContext context) {

        return DIAGNOSTICS_RECORDER.getDiagnostics(context).stream()
                .map(DiagnosticEvent::toMap)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Returns typed diagnostics recorded in the debug context.
     *
     * @param context The debug context.
     * @return Diagnostic events.
     */
    public static List<DiagnosticEvent> getDiagnosticEvents(DebugContext context) {

        return DIAGNOSTICS_RECORDER.getDiagnostics(context);
    }

    /**
     * Returns diagnostics recorded in the authentication context.
     *
     * @param context The authentication context.
     * @return Diagnostic events.
     */
    public static List<Map<String, Object>> getDiagnostics(AuthenticationContext context) {

        return DIAGNOSTICS_RECORDER.getDiagnostics(context).stream()
                .map(DiagnosticEvent::toMap)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Returns typed diagnostics recorded in the authentication context.
     *
     * @param context The authentication context.
     * @return Diagnostic events.
     */
    public static List<DiagnosticEvent> getDiagnosticEvents(AuthenticationContext context) {

        return DIAGNOSTICS_RECORDER.getDiagnostics(context);
    }

    private static DiagnosticEvent buildEvent(String stage, String status, String message,
            Map<String, Object> details) {

        DiagnosticEvent event = DiagnosticEvent.builder()
                .stage(stage)
                .status(status)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
        if (details != null && !details.isEmpty()) {
            event.setDetails(details);
        }
        return event;
    }
}
