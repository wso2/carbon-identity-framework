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

package org.wso2.carbon.identity.debug.idp.util;

import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.debug.framework.model.DiagnosticEvent;
import org.wso2.carbon.identity.debug.framework.util.DebugDiagnosticsUtil;

import java.util.List;
import java.util.Map;

/**
 * Utility for recording diagnostic events specifically for WSO2 AuthenticationContext.
 * This class resides in the IdP-specific layer to keep the core debug framework 
 * protocol-agnostic and decoupled from the internal authentication framework.
 */
public final class IdpDebugDiagnosticsUtil {

    private IdpDebugDiagnosticsUtil() {
    }

    /**
     * Records a diagnostic event for an authentication context.
     *
     * @param context The authentication context.
     * @param stage The diagnostic stage.
     * @param status The diagnostic status.
     * @param message The diagnostic message.
     */
    public static void recordEvent(AuthenticationContext context, String stage, String status, String message) {
        DebugDiagnosticsUtil.recordEvent(context.getProperties(), stage, status, message);
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
        DebugDiagnosticsUtil.recordEvent(context.getProperties(), stage, status, message, details);
    }

    /**
     * Records a typed diagnostic event for an authentication context.
     *
     * @param context The authentication context.
     * @param event Diagnostic event.
     */
    public static void recordEvent(AuthenticationContext context, DiagnosticEvent event) {
        DebugDiagnosticsUtil.recordEvent(context.getProperties(), event);
    }

    /**
     * Returns diagnostics recorded in the authentication context.
     *
     * @param context The authentication context.
     * @return List of diagnostic event maps.
     */
    public static List<Map<String, Object>> getDiagnostics(AuthenticationContext context) {
        return DebugDiagnosticsUtil.getDiagnostics(context.getProperties());
    }

    /**
     * Returns typed diagnostic events recorded in the authentication context.
     *
     * @param context The authentication context.
     * @return List of DiagnosticEvent instances.
     */
    public static List<DiagnosticEvent> getDiagnosticEvents(AuthenticationContext context) {
        return DebugDiagnosticsUtil.getDiagnosticEvents(context.getProperties());
    }
}
