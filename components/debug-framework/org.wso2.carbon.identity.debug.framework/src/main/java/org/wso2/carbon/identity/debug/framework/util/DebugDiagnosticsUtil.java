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
import org.wso2.carbon.identity.debug.framework.DebugFrameworkConstants;
import org.wso2.carbon.identity.debug.framework.model.DebugContext;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility for recording and retrieving protocol-agnostic debug diagnostic events.
 */
public final class DebugDiagnosticsUtil {

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

        Deque<Map<String, Object>> diagnostics = getOrCreateDiagnostics(context);
        diagnostics.addFirst(buildEvent(stage, status, message, details));
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

        Deque<Map<String, Object>> diagnostics = getOrCreateDiagnostics(context);
        diagnostics.addFirst(buildEvent(stage, status, message, details));
    }

    /**
     * Returns diagnostics recorded in the debug context.
     *
     * @param context The debug context.
     * @return Diagnostic events.
     */
    public static List<Map<String, Object>> getDiagnostics(DebugContext context) {

        return new ArrayList<>(getOrCreateDiagnostics(context));
    }

    /**
     * Returns diagnostics recorded in the authentication context.
     *
     * @param context The authentication context.
     * @return Diagnostic events.
     */
    public static List<Map<String, Object>> getDiagnostics(AuthenticationContext context) {

        return new ArrayList<>(getOrCreateDiagnostics(context));
    }

    @SuppressWarnings("unchecked")
    private static Deque<Map<String, Object>> getOrCreateDiagnostics(DebugContext context) {

        Object diagnostics = context.getProperty(DebugFrameworkConstants.DEBUG_DIAGNOSTICS);
        if (diagnostics instanceof Deque) {
            return (Deque<Map<String, Object>>) diagnostics;
        }
        Deque<Map<String, Object>> timeline = new ArrayDeque<>();
        context.setProperty(DebugFrameworkConstants.DEBUG_DIAGNOSTICS, timeline);
        return timeline;
    }

    @SuppressWarnings("unchecked")
    private static Deque<Map<String, Object>> getOrCreateDiagnostics(AuthenticationContext context) {

        Object diagnostics = context.getProperty(DebugFrameworkConstants.DEBUG_DIAGNOSTICS);
        if (diagnostics instanceof Deque) {
            return (Deque<Map<String, Object>>) diagnostics;
        }
        Deque<Map<String, Object>> timeline = new ArrayDeque<>();
        context.setProperty(DebugFrameworkConstants.DEBUG_DIAGNOSTICS, timeline);
        return timeline;
    }

    private static Map<String, Object> buildEvent(String stage, String status, String message,
            Map<String, Object> details) {

        Map<String, Object> event = new LinkedHashMap<>();
        event.put(DebugFrameworkConstants.DIAGNOSTIC_STAGE, stage);
        event.put(DebugFrameworkConstants.DIAGNOSTIC_STATUS, status);
        event.put(DebugFrameworkConstants.DIAGNOSTIC_MESSAGE, message);
        event.put(DebugFrameworkConstants.DIAGNOSTIC_TIMESTAMP, System.currentTimeMillis());
        if (details != null && !details.isEmpty()) {
            event.put(DebugFrameworkConstants.DIAGNOSTIC_DETAILS, details);
        }
        return event;
    }
}
