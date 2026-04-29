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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.graalvm.polyglot.Value;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.JsGraalGraphEngineModeRouter;

import org.graalvm.polyglot.proxy.ProxyObject;
import org.graalvm.polyglot.proxy.ProxyArray;
import java.util.Map;

/**
 * Consolidated property path navigator for the remote JS engine.
 * Handles navigation through nested objects using "::" separated path segments.
 *
 * Supports traversal of:
 * - ProxyObject (getMember)
 * - ProxyArray (get by numeric index)
 *
 */
final class PropertyPathNavigator {

    private static final Log log = LogFactory.getLog(PropertyPathNavigator.class);

    private PropertyPathNavigator() {
        // Utility class
    }

    /**
     * Check if a string consists of only ASCII digit characters.
     * Avoids compiling a new regex on every call (unlike String.matches("\\d+")).
     *
     * @param s The string to check
     * @return true if non-empty and all characters are digits
     */
    static boolean isNumeric(String s) {
        if (s == null || s.isEmpty()) return false;

        for (char c : s.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }

    /**
     * Navigate a property path starting from a root object.
     *
     * @param pathParts  The path segments (already split by "::")
     * @param startIndex Index to start navigating from (0 for full path, 1 to skip refId)
     * @param root       The root object to navigate from
     * @return The resolved value, or null if the path cannot be resolved
     */
    public static Object navigatePath(String[] pathParts, int startIndex, Object root) {
        Object current = root;

        for (int i = startIndex; i < pathParts.length; i++) {
            String part = pathParts[i];
            if (current == null) {
                throw new IllegalStateException("Null encountered while navigating path at segment: " + part);
            }

            // __keys__ triggers member key enumeration
            if (RemoteEngineConstants.KEYS_PROPERTY.equals(part)) {
                return getMemberKeys(current);
            }

            current = navigateSingleStep(current, part);
        }

        return current;
    }

    /**
     * Navigate to the parent object and set the final property value.
     *
     * @param pathParts  The path segments (already split by "::")
     * @param startIndex Index to start navigating from
     * @param root       The root object to navigate from
     * @param value      The value to set at the end of the path
     * @return true if the property was successfully set, false otherwise
     */
    public static boolean setProperty(String[] pathParts, int startIndex, Object root, Object value) {
        if (pathParts.length == 0) {
            return false;
        }

        // Navigate to the parent (all segments except the last)
        Object parent = root;
        for (int i = startIndex; i < pathParts.length - 1; i++) {
            String part = pathParts[i];
            if (parent == null) {
                throw new IllegalStateException("Null encountered while navigating path at segment: " + part );
            }
            parent = navigateSingleStep(parent, part);
        }

        if (parent == null) {
            log.warn("[PropertyPathNavigator] Parent is null, cannot set final property");
            return false;
        }

        // Set the final property
        String finalPart = pathParts[pathParts.length - 1];
        return setFinalProperty(parent, finalPart, value);
    }

    /**
     * Navigate a single step in the property path (for GET operations).
     * Handles ProxyArray, ProxyObject, Map, and reflection getter.
     */
    private static Object navigateSingleStep(Object current, String part) {
        // Numeric segments on ProxyArray use get(index), not getMember()
        if (isNumeric(part) && current instanceof ProxyArray) {
            int index = Integer.parseInt(part);
            Object result = ((ProxyArray) current).get(index);
            if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                log.debug("[PropertyPathNavigator] Accessed array index " + index + " -> " +
                        (result != null ? result.getClass().getSimpleName() : "null"));
            }
            return result;
        }

        // Standard member access on ProxyObject
        if (current instanceof ProxyObject) {
            Object result = ((ProxyObject) current).getMember(part);
            if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                log.debug("[PropertyPathNavigator] Accessed member '" + part + "' on proxy -> " +
                        (result != null ? result.getClass().getSimpleName() : "null"));
            }
            return result;
        }

        throw new IllegalStateException(
                "Cannot navigate property '" + part + "' on type: " +
                        current.getClass().getName()
        );

    }
    /**
     * Set the final property value on the parent object.
     * Tries ProxyObject.putMember, reflection putMember, Map.put, and reflection setter.
     */
    private static boolean setFinalProperty(Object parent, String finalPart, Object value) {
        // Try ProxyObject.putMember with GraalVM Value wrapping
        if (parent instanceof ProxyObject) {
            try {
                Value wrappedValue = Value.asValue(value);
                ((ProxyObject) parent).putMember(finalPart, wrappedValue);
                if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                    log.debug("[PropertyPathNavigator] Set property via putMember: " + finalPart);
                }
                return true;
            } catch (Exception e) {
                throw new IllegalStateException(
                        "Failed to set property '" + finalPart + "' on ProxyObject: " +
                                parent.getClass().getName(), e
                );
            }
        }

        throw new IllegalStateException(
                "Unsupported type for setting property: " + parent.getClass().getName() +
                        ", property: " + finalPart
        );
    }

    /**
     * Get member keys from an object.
     * Handles ProxyObject (getMemberKeys)
     */
    public static Object getMemberKeys(Object current) {
        if (current instanceof ProxyObject) {
            Object keys = ((ProxyObject) current).getMemberKeys();
            if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                log.debug("[PropertyPathNavigator] __keys__ on proxy -> " +
                        (keys != null ? keys.getClass().getSimpleName() : "null"));
            }
            return keys;
        }
        throw new IllegalStateException(
                "Cannot extract keys from type: " + current.getClass().getName()
        );
    }
}
