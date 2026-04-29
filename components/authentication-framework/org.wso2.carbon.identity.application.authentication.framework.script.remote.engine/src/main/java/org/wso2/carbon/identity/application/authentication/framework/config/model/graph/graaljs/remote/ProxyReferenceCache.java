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
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.JsGraalGraphEngineModeRouter;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Session-scoped cache for proxy object references and host function return references.
 * <p>
 * Manages two types of references:
 * <ul>
 *   <li><b>Proxy objects</b>: Complex objects cached for lazy property loading
 *       (e.g., User arrays from getUsersWithClaimValues). Accessed via "__proxyref__" prefix.</li>
 *   <li><b>Host function return refs</b>: Objects returned by host functions that need
 *       property access from the External. Accessed via "__hostref__" prefix.</li>
 * </ul>
 */
class ProxyReferenceCache {

    private static final Log log = LogFactory.getLog(ProxyReferenceCache.class);

    // Session-scoped cache for proxied objects (e.g., User objects from getUsersWithClaimValues)
    // Key: reference_id (UUID), Value: actual object
    private final Map<String, Object> proxyObjectCache = new ConcurrentHashMap<>();

    // Host function return references
    // Key: reference_id (UUID), Value: object returned by host function
    private final Map<String, Object> hostFunctionRefs = new ConcurrentHashMap<>();

    /**
     * Store a complex object reference for later property access.
     * Used when host functions return complex objects that need to be accessed
     * via dynamic proxy on the External.
     *
     * @param obj The object to store.
     * @return A unique reference ID.
     */
    String storeHostReturnReference(Object obj) {
        String refId = UUID.randomUUID().toString();
        hostFunctionRefs.put(refId, obj);
        if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
            log.debug("[RemoteJsEngine] Stored object reference: " + refId +
                    " -> " + (obj != null ? obj.getClass().getSimpleName() : "null"));
        }
        return refId;
    }

    /**
     * Get a property from a cached proxy object.
     * Path format: "&lt;referenceId&gt;::&lt;property&gt;" or
     * "&lt;referenceId&gt;::&lt;property&gt;::&lt;nestedProperty&gt;..."
     * <p>
     * This enables lazy loading of complex objects. Instead of eagerly serializing all
     * properties (which causes timeouts for large result sets like getUsersWithClaimValues),
     * objects are cached and properties are fetched on-demand when accessed.
     */
    Object getProxyObjectProperty(String path) {
        String[] parts = path.split(RemoteEngineConstants.PATH_SEPARATOR);
        String refId = parts[0];
        Object root = proxyObjectCache.get(refId);

        if (root == null) {
            log.warn("[RemoteJsEngine] No proxy object found for reference ID: " + refId);
            return null;
        }

        if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
            log.debug("[RemoteJsEngine] Retrieved proxy object for refId: " + refId +
                    ", type: " + root.getClass().getName());
        }

        Object result = PropertyPathNavigator.navigatePath(parts, 1, root);

        if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
            log.debug("[RemoteJsEngine] getProxyObjectProperty '" + path + "' = " +
                    (result != null ? result.getClass().getSimpleName() : "null"));
        }
        return result;
    }

    /**
     * Navigate a property path on a stored host function return reference.
     * Path format: "&lt;refId&gt;" or "&lt;refId&gt;::&lt;property&gt;::&lt;subprop&gt;..."
     */
    Object getHostRefProperty(String path) {
        String[] parts = path.split(RemoteEngineConstants.PATH_SEPARATOR);
        String refId = parts[0];
        Object root = hostFunctionRefs.get(refId);

        if (root == null) {
            log.warn("[RemoteJsEngine] No host function ref found for ID: " + refId);
            return null;
        }

        Object result = PropertyPathNavigator.navigatePath(parts, 1, root);

        if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
            log.debug("[RemoteJsEngine] getHostRefProperty '" + path + "' = " +
                    (result != null ? result.getClass().getSimpleName() : "null"));
        }
        return result;
    }

    /**
     * Set a property on a stored host function return reference.
     * Path format: "&lt;refId&gt;::&lt;property&gt;::&lt;subprop&gt;..."
     */
    boolean setHostRefProperty(String path, Object value) {
        String[] parts = path.split(RemoteEngineConstants.PATH_SEPARATOR);
        if (parts.length < 2) {
            log.warn("[RemoteJsEngine] setHostRefProperty requires at least refId and property: " + path);
            return false;
        }

        String refId = parts[0];
        Object root = hostFunctionRefs.get(refId);
        if (root == null) {
            log.warn("[RemoteJsEngine] No host function ref found for ID: " + refId);
            return false;
        }

        return PropertyPathNavigator.setProperty(parts, 1, root, value);
    }

    /**
     * Set a property on a cached proxy object.
     * Path format: "&lt;referenceId&gt;::&lt;property&gt;" or
     * "&lt;referenceId&gt;::&lt;property&gt;::&lt;nestedProperty&gt;..."
     *
     * @param path  The path containing referenceId and property segments.
     * @param value The value to set.
     * @return true if the property was successfully set, false otherwise.
     */
    boolean setProxyObjectProperty(String path, Object value) {

        String[] parts = path.split(RemoteEngineConstants.PATH_SEPARATOR);
        if (parts.length < 2) {
            log.warn("[RemoteJsEngine] setProxyObjectProperty requires at least refId and property: " + path);
            return false;
        }

        String refId = parts[0];
        Object root = proxyObjectCache.get(refId);
        if (root == null) {
            log.warn("[RemoteJsEngine] No proxy object found for reference ID: " + refId);
            return false;
        }

        return PropertyPathNavigator.setProperty(parts, 1, root, value);
    }

    /**
     * Get the proxy object cache map.
     * Used by transport layer to set ThreadLocal before serialization.
     *
     * @return The proxy object cache.
     */
    Map<String, Object> getCache() {
        return proxyObjectCache;
    }

    /**
     * Resolve a context-data property path with path-prefix caching.
     * <p>
     * Walks {@code proxyObjectCache} backwards from the full path to find the longest
     * previously cached ancestor. Navigation resumes from that ancestor (skipping
     * already-traversed steps) or falls back to {@code root} when no ancestor is cached.
     * When the resolved value is a navigable Graal {@code Proxy} (ProxyObject/ProxyArray),
     * it is stored under the full path so future requests for the same path or any
     * descendant can resume from it.
     * <p>
     * Only navigable intermediates are cached. Leaf primitives, {@code null}, and
     * {@link RemoteEngineConstants#KEYS_PROPERTY} results are deliberately re-fetched
     * each call so out-of-band mutations to the underlying authentication context
     * cannot produce stale reads. Cached intermediates are live proxy wrappers around
     * the live context — their {@code getMember} calls always reflect current state.
     * <p>
     * Context-path keys share this map with UUID-keyed host-function and proxy-reference
     * entries. The two key spaces do not collide: UUIDs contain no {@code "::"} separator
     * and single-segment context names (e.g. {@code "user"}, {@code "request"}) do not
     * match the UUID textual format.
     *
     * @param contextPath The full property path separated by {@code "::"}.
     * @param root        The root object to navigate from if no cached ancestor is found.
     * @return The resolved value, or {@code null} if the path cannot be resolved.
     */
    Object getContextPathProperty(String contextPath, Object root) {

        if (contextPath == null || contextPath.isEmpty()) {
            return root;
        }

        // Fast path: exact full-path hit on a previously cached navigable intermediate.
        Object cachedFull = proxyObjectCache.get(contextPath);
        if (cachedFull != null) {
            if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                log.debug("[ProxyReferenceCache] Context path cache HIT (full): " + contextPath);
            }
            return cachedFull;
        }

        String[] parts = contextPath.split(RemoteEngineConstants.PATH_SEPARATOR);

        // Walk from the longest possible ancestor down to the shortest, stopping
        // at the first cached entry. No prefix cached -> resume from root.
        int resumeIndex = 0;
        Object resumeFrom = root;
        for (int prefixLen = parts.length - 1; prefixLen >= 1; prefixLen--) {
            String prefix = joinPathPrefix(parts, prefixLen);
            Object cached = proxyObjectCache.get(prefix);
            if (cached != null) {
                resumeFrom = cached;
                resumeIndex = prefixLen;
                if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                    log.debug("[ProxyReferenceCache] Context path cache HIT (prefix '" + prefix +
                            "', " + (parts.length - prefixLen) + " step(s) remaining): " + contextPath);
                }
                break;
            }
        }

        // Resolve remaining segments via the shared navigator — existing navigation
        // semantics (ProxyObject / ProxyArray / __keys__) are preserved untouched.
        Object value = PropertyPathNavigator.navigatePath(parts, resumeIndex, resumeFrom);

        // Cache navigable intermediates only. Skip __keys__ terminals, nulls, and primitives.
        String terminalSegment = parts[parts.length - 1];
        boolean terminalIsKeys = RemoteEngineConstants.KEYS_PROPERTY.equals(terminalSegment);
        if (!terminalIsKeys && ProxyTypeResolver.isJsWrapperProxy(value)) {
            proxyObjectCache.putIfAbsent(contextPath, value);
            if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                log.debug("[ProxyReferenceCache] Cached context path '" + contextPath + "' -> " +
                        value.getClass().getSimpleName());
            }
        }

        return value;
    }

    /**
     * Invalidate cached context-path entries that may be stale after a write.
     * <p>
     * Removes the exact written path and every descendant path from
     * {@code proxyObjectCache}. Ancestor entries stay: they are live proxy references
     * whose {@code getMember} calls already reflect the new value. UUID-keyed host
     * function / proxy-reference entries are untouched — UUIDs contain no
     * {@code "::"} separator and cannot be matched by either clause of the predicate.
     *
     * @param contextPath The write-target property path in the same form used for reads.
     */
    void invalidateContextPath(String contextPath) {

        if (contextPath == null || contextPath.isEmpty()) {
            return;
        }
        String descendantPrefix = contextPath + RemoteEngineConstants.PATH_SEPARATOR;
        boolean removed = proxyObjectCache.keySet().removeIf(
                k -> k.equals(contextPath) || k.startsWith(descendantPrefix));
        if (removed && JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
            log.debug("[ProxyReferenceCache] Invalidated context path and descendants: " + contextPath);
        }
    }

    /**
     * Join the first {@code len} segments of {@code parts} with the path separator.
     * Mirrors what {@code String.split(PATH_SEPARATOR)} would have produced as input.
     */
    private static String joinPathPrefix(String[] parts, int len) {

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            if (i > 0) {
                sb.append(RemoteEngineConstants.PATH_SEPARATOR);
            }
            sb.append(parts[i]);
        }
        return sb.toString();
    }
}
