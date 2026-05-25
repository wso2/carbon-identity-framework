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

package org.wso2.carbon.identity.flow.extension.model;

import org.wso2.carbon.identity.flow.extension.executor.PathTypeAnnotationUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Configures flow context access for external services.
 * * - expose: Paths sent to the service; 'encrypted' flag toggles outbound JWE.
 * - modify: Paths the service can REPLACE; 'encrypted' flag toggles inbound JWE.
 * * Note: External certificates are managed on {@link FlowExtensionAction#getCertificate()}, not here.
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

        this.expose = expose != null ? Collections.unmodifiableList(new ArrayList<>(expose)) : null;
        this.modify = modify != null ? Collections.unmodifiableList(new ArrayList<>(modify)) : null;
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
     * @return List of path strings, or an empty list if expose is not configured.
     */
    public List<String> getExposePaths() {

        if (expose == null) {
            return Collections.emptyList();
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
     * @return List of path strings, or an empty list if modify is not configured.
     */
    public List<String> getModifyPaths() {

        if (modify == null) {
            return Collections.emptyList();
        }
        return modify.stream().map(ContextPath::getPath).collect(Collectors.toList());
    }

    /**
     * Check if a given expose path has outbound encryption enabled.
     * With leaf-only expose paths, this performs an exact match lookup.
     *
     * @param path The expose path to check.
     * @return {@code true} if the matching expose entry has {@code encrypted = true}.
     */
    public boolean isExposePathEncrypted(String path) {

        if (expose == null) {
            return false;
        }
        return expose.stream()
                .filter(ep -> ep.getPath().equals(path))
                .findFirst()
                .map(ContextPath::isEncrypted)
                .orElse(false);
    }

    /**
     * Check if a given modify path has inbound encryption enabled.
     * Matches the modify entry whose path (after stripping type annotations) exactly equals the given path.
     *
     * @param path The terminal path to check (clean, without annotations).
     * @return {@code true} if the matching modify entry has {@code encrypted = true}.
     */
    public boolean isModifyPathEncrypted(String path) {

        if (modify == null) {
            return false;
        }
        return modify.stream()
                .filter(mp -> path.equals(PathTypeAnnotationUtil.stripAnnotation(mp.getPath())[0]))
                .findFirst()
                .map(ContextPath::isEncrypted)
                .orElse(false);
    }

}
