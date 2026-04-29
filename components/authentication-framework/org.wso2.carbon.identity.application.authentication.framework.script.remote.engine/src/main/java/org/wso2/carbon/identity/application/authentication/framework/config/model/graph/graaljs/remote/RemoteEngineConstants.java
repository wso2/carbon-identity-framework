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

/**
 * Centralized constants for the remote JS engine protocol.
 * These string values form the wire protocol between IS and the GraalJS External.
 * Any change here MUST be mirrored in the External's ExternalConstants.java.
 *
 * Categories:
 * 1. Type markers   - Non-serializable object placeholders
 * 2. Reference prefixes - Lazy proxy path prefixes (__proxyref__, __hostref__)
 * 3. Proxy markers  - Serialized proxy metadata fields
 * 4. Binding keys   - Reserved binding names
 * 5. Path/transport - Path separator, transport indicators
 * 6. Proxy types    - Proxy type identifiers
 */
public final class RemoteEngineConstants {

    private RemoteEngineConstants() {
        // Utility class — no instances
    }

    // ============ Type Markers ============

    /** Placeholder for JsGraalAuthenticationContext which cannot be serialized via protobuf. */
    public static final String CONTEXT_PLACEHOLDER =
            "__JsGraalAuthenticationContext_placeholder__";

    // ============ Reference Prefixes (lazy proxy path prefixes) ============

    /** Prefix for proxy object references: "__proxyref__::<uuid>::<property>" */
    public static final String PROXY_REF_PREFIX = "__proxyref__::";

    /** Prefix for host function return references: "__hostref__::<uuid>::<property>" */
    public static final String HOST_REF_PREFIX = "__hostref__::";

    // ============ Proxy Marker Fields (serialized proxy metadata) ============

    /** Marker field indicating a serialized context proxy. */
    public static final String IS_CONTEXT_PROXY = "__isContextProxy";

    /** Field holding the proxy type (e.g., "context", "authenticateduser"). */
    public static final String PROXY_TYPE_FIELD = "__proxyType";

    /** Field holding the proxy base path for navigation. */
    public static final String BASE_PATH_FIELD = "__basePath";

    /** Marker field indicating a host function return reference. */
    public static final String IS_HOST_REF = "__isHostRef";

    /** Field holding the reference ID for proxy/host-ref objects. */
    public static final String REFERENCE_ID_FIELD = "__referenceId";

    // ============ Special Property Names ============

    /** Special property triggering member key enumeration (Object.keys()). */
    public static final String KEYS_PROPERTY = "__keys__";

    // ============ Binding Keys ============

    /** The binding key for the authentication context object. */
    public static final String CONTEXT_BINDING_KEY = "context";

    /** The binding key for the callback context in External. */
    public static final String CALLBACK_CONTEXT_KEY = "__callbackContext";

    // ============ Path ============

    /** Separator used in property paths (e.g., "steps::1::subject"). */
    public static final String PATH_SEPARATOR = "::";

    // ============ Proxy Types ============

    /** Proxy type for generic POJO objects. */
    public static final String PROXY_TYPE_POJO = "pojo";

    // ============ mTLS Configuration ============
    // mTLS is mandatory on the IS→External gRPC channel. The wire carries the
    // full JsAuthenticationContext (username, tenant, userstore domain, claims,
    // session id) plus host-function payloads, so plaintext on a non-loopback
    // target is a confidentiality defect. Channel credentials are sourced from
    // the Carbon primary keystore + truststore via ServerConfiguration; if that
    // material cannot be loaded, GrpcConnectionManager#createChannel refuses to
    // bring the channel up. The previous feature-local PEM toggle, constants,
    // and bundle have been removed — operators rotate keys via the standard
    // Security.KeyStore.* / Security.TrustStore.* configuration.
}
