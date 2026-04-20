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

package org.wso2.carbon.identity.debug.framework.exception;

import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Exception thrown when context resolution or creation fails in the debug framework.
 * Used when Identity Provider configurations or context setup encounters errors.
 */
public class ContextResolutionException extends DebugFrameworkException {

    private static final long serialVersionUID = 1L;
    private static final String DEBUG_ERROR_PREFIX = "DEBUG-";
    private static final Pattern DIGIT_SUFFIX_PATTERN = Pattern.compile(".*?(\\d{5})$");

    /**
     * Constructs a ContextResolutionException with message.
     *
     * @param message Error message.
     */
    public ContextResolutionException(String message) {

        this(null, message, null, null);
    }

    /**
     * Constructs a ContextResolutionException with error code, message, and description.
     *
     * @param errorCode   Error code for categorization.
     * @param message     Error message.
     * @param description Detailed error description.
     */
    public ContextResolutionException(String errorCode, String message, String description) {

        this(errorCode, message, description, null);
    }

    /**
     * Constructs a ContextResolutionException with error code, message, description, and cause.
     *
     * @param errorCode   Error code for categorization.
     * @param message     Error message.
     * @param description Detailed error description.
     * @param cause       Root cause exception.
     */
    public ContextResolutionException(String errorCode, String message, String description, Throwable cause) {

        super(normalizeErrorCode(errorCode), message, description, cause);
    }

    /**
     * Normalizes external error code formats to framework standard format.
     *
     * @param errorCode Error code to normalize.
     * @return Normalized error code in DEBUG-xxxxx format where possible.
     */
    private static String normalizeErrorCode(String errorCode) {

        if (StringUtils.isBlank(errorCode)) {
            return errorCode;
        }

        if (errorCode.startsWith(DEBUG_ERROR_PREFIX)) {
            return errorCode;
        }

        Matcher matcher = DIGIT_SUFFIX_PATTERN.matcher(errorCode);
        if (matcher.matches()) {
            return DEBUG_ERROR_PREFIX + matcher.group(1);
        }

        return errorCode;
    }
}
