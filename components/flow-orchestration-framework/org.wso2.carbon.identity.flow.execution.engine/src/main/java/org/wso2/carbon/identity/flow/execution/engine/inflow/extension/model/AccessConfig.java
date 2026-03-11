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

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Access Configuration for In-Flow Extension actions.
 * <p>
 * Defines which parts of the flow context are exposed to the external service
 * and what operations the service is allowed to perform.
 * </p>
 *
 * <ul>
 *   <li>{@code expose} – structured list of {@link ExposePath} entries, each with a hierarchical
 *       path prefix and an {@code encrypted} flag controlling outbound JWE encryption.</li>
 *   <li>{@code allowedOperations} – list of {@link AllowedOperation} entries, each grouping an
 *       operation type with a list of {@link OperationPath} entries carrying per-path encryption
 *       flags for inbound JWE encryption.</li>
 * </ul>
 *
 * <p><b>Note:</b> The external service's certificate for outbound encryption is held in the
 * separate {@link Encryption} model, not in AccessConfig. This follows the same separation
 * pattern as {@code PasswordSharing} in the PreUpdatePassword action type.</p>
 */
public class AccessConfig {

    /**
     * Regex pattern to match path type annotations at the end of a path.
     * Strips trailing bracket expressions like {@code []}, {@code [field1, field2]}.
     * These annotations are used by the request builder for type coercion but are not
     * part of the logical path that the external service sees.
     */
    private static final Pattern PATH_ANNOTATION_PATTERN = Pattern.compile("\\[([^\\]]*)]$");

    private final List<ExposePath> expose;
    private final List<AllowedOperation> allowedOperations;

    /**
     * Constructs an AccessConfig with the given expose paths and allowed operations.
     *
     * @param expose            List of expose path entries. May be {@code null}.
     * @param allowedOperations List of allowed operation entries. May be {@code null}.
     */
    public AccessConfig(List<ExposePath> expose, List<AllowedOperation> allowedOperations) {

        this.expose = expose != null ? Collections.unmodifiableList(expose) : null;
        this.allowedOperations = allowedOperations != null
                ? Collections.unmodifiableList(allowedOperations) : null;
    }

    /**
     * Returns the list of expose path entries.
     *
     * @return Unmodifiable list of {@link ExposePath} entries, or {@code null} if not configured.
     */
    public List<ExposePath> getExpose() {

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
        return expose.stream().map(ExposePath::getPath).collect(Collectors.toList());
    }

    /**
     * Returns the list of allowed operation entries.
     *
     * @return Unmodifiable list of {@link AllowedOperation} entries, or {@code null} if not configured.
     */
    public List<AllowedOperation> getAllowedOperations() {

        return allowedOperations;
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
                .map(ExposePath::isEncrypted)
                .orElse(false);
    }

    /**
     * Check if a given operation path has inbound encryption enabled.
     * Iterates the nested {@link OperationPath} entries within each {@link AllowedOperation}.
     * Path type annotations (e.g. {@code []}, {@code [schema]}) are stripped from stored paths
     * before comparison, since operation paths from the external service do not include annotations.
     *
     * @param op   The operation type (e.g. "REPLACE").
     * @param path The operation path (clean, without annotations).
     * @return {@code true} if the matching operation path has {@code encrypted = true}.
     */
    public boolean isOperationPathEncrypted(String op, String path) {

        if (allowedOperations == null) {
            return false;
        }
        return allowedOperations.stream()
                .filter(ao -> ao.getOp().equalsIgnoreCase(op))
                .flatMap(ao -> ao.getPaths().stream())
                .filter(opPath -> path.startsWith(stripAnnotation(opPath.getPath())))
                .reduce((a, b) -> stripAnnotation(a.getPath()).length()
                        >= stripAnnotation(b.getPath()).length() ? a : b)
                .map(OperationPath::isEncrypted)
                .orElse(false);
    }

    /**
     * Returns whether any expose path or allowed operation path has encryption enabled.
     *
     * @return {@code true} if at least one path has {@code encrypted = true}.
     */
    public boolean hasAnyEncryptedPath() {

        boolean hasEncryptedExpose = expose != null
                && expose.stream().anyMatch(ExposePath::isEncrypted);
        boolean hasEncryptedOps = allowedOperations != null
                && allowedOperations.stream().anyMatch(AllowedOperation::hasAnyEncryptedPath);
        return hasEncryptedExpose || hasEncryptedOps;
    }

    /**
     * Strip trailing path type annotations from a path string.
     * For example, {@code "/properties/riskFactors[]"} becomes {@code "/properties/riskFactors"}.
     *
     * @param path The path that may contain trailing annotations.
     * @return The path without annotations.
     */
    private static String stripAnnotation(String path) {

        Matcher m = PATH_ANNOTATION_PATTERN.matcher(path);
        return m.find() ? path.substring(0, m.start()) : path;
    }
}
