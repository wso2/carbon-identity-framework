/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.flow.execution.engine.inflow.extension.model;

import org.wso2.carbon.identity.flow.execution.engine.inflow.extension.executor.PathTypeAnnotationUtil;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Access Configuration for In-Flow Extension actions.
 * <p>
 * Defines which parts of the flow context are exposed to the external service
 * and which paths the service is allowed to modify.
 * </p>
 *
 * <ul>
 *   <li>{@code expose} – structured list of {@link ContextPath} entries, each with a hierarchical
 *       path prefix and an {@code encrypted} flag controlling outbound JWE encryption.</li>
 *   <li>{@code modify} – structured list of {@link ContextPath} entries defining which paths the
 *       external service can change. All modifications map to REPLACE operations internally.
 *       The {@code encrypted} flag on modify paths controls inbound JWE encryption (the external
 *       service encrypts values, IS decrypts with its private key).</li>
 * </ul>
 *
 * <p>Expose and modify are independent: expose controls what data is sent to the external service,
 * while modify controls what data the external service is allowed to change. A path can appear
 * in both lists with independent encryption flags.</p>
 *
 * <p><b>Note:</b> The external service's certificate for outbound encryption is held in the
 * separate {@link Encryption} model, not in AccessConfig.</p>
 */
public class AccessConfig {

    private final List<ContextPath> expose;
    private final List<ContextPath> modify;

    /**
     * Constructs an AccessConfig with the given expose and modify paths.
     *
     * @param expose List of expose path entries. May be {@code null}.
     * @param modify List of modify path entries. May be {@code null}.
     */
    public AccessConfig(List<ContextPath> expose, List<ContextPath> modify) {

        this.expose = expose != null ? Collections.unmodifiableList(expose) : null;
        this.modify = modify != null ? Collections.unmodifiableList(modify) : null;
    }

    /**
     * Returns the list of expose path entries.
     *
     * @return Unmodifiable list of {@link ContextPath} entries, or {@code null} if not configured.
     */
    public List<ContextPath> getExpose() {

        return expose;
    }

    /**
     * Returns the flat list of expose path strings (without encryption metadata).
     * Convenience method for components that only need path prefixes.
     *
     * @return List of path strings, or {@code null} if expose is not configured.
     */
    public List<String> getExposePaths() {

        if (expose == null) {
            return null;
        }
        return expose.stream().map(ContextPath::getPath).collect(Collectors.toList());
    }

    /**
     * Returns the list of modify path entries.
     *
     * @return Unmodifiable list of {@link ContextPath} entries, or {@code null} if not configured.
     */
    public List<ContextPath> getModify() {

        return modify;
    }

    /**
     * Returns the flat list of modify path strings (without encryption metadata).
     * Convenience method for components that only need path strings.
     *
     * @return List of path strings, or {@code null} if modify is not configured.
     */
    public List<String> getModifyPaths() {

        if (modify == null) {
            return null;
        }
        return modify.stream().map(ContextPath::getPath).collect(Collectors.toList());
    }

    /**
     * Check if a given path prefix has outbound encryption enabled.
     * Matches the most specific (longest) expose path that is a prefix of the given path.
     *
     * @param pathPrefix The path to check.
     * @return {@code true} if the matching expose entry has {@code encrypted = true}.
     */
    public boolean isExposePathEncrypted(String pathPrefix) {

        if (expose == null) {
            return false;
        }
        return expose.stream()
                .filter(ep -> pathPrefix.startsWith(ep.getPath()))
                .reduce((a, b) -> a.getPath().length() >= b.getPath().length() ? a : b)
                .map(ContextPath::isEncrypted)
                .orElse(false);
    }

    /**
     * Check if a given modify path has inbound encryption enabled.
     * Matches the most specific (longest) modify path that is a prefix of the given path.
     *
     * @param pathPrefix The path to check (clean, without annotations).
     * @return {@code true} if the matching modify entry has {@code encrypted = true}.
     */
    public boolean isModifyPathEncrypted(String pathPrefix) {

        if (modify == null) {
            return false;
        }
        return modify.stream()
                .filter(mp -> pathPrefix.startsWith(PathTypeAnnotationUtil.stripAnnotation(mp.getPath())[0]))
                .reduce((a, b) -> PathTypeAnnotationUtil.stripAnnotation(a.getPath())[0].length()
                        >= PathTypeAnnotationUtil.stripAnnotation(b.getPath())[0].length() ? a : b)
                .map(ContextPath::isEncrypted)
                .orElse(false);
    }

}
