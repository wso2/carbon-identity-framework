/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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
 *
 * NOTE: The code/logic in this class is copied from https://bitbucket.org/thetransactioncompany/cors-filter.
 * All credits goes to the original authors of the project https://bitbucket.org/thetransactioncompany/cors-filter.
 */

package org.wso2.carbon.identity.cors.mgt.core.model;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.cors.mgt.core.constant.ErrorMessages;

/**
 * Resource request origin (not validated), as defined in The Web Origin Concept (RFC 6454).
 */
public class Origin {

    /**
     * The original origin value, used in hash code generation and equality checking.
     */
    private final String value;

    /**
     * Creates a new origin from the specified URI string. Note that the syntax is not validated.
     *
     * @param value The URI string for the origin. Must not be {@code null}.
     */
    public Origin(final String value) {

        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException(ErrorMessages.ERROR_CODE_NULL_ORIGIN.getMessage());
        }
        this.value = value.trim();
    }

    /**
     * Returns the original string value of this origin.
     *
     * @return The origin as a URI string.
     */
    @Override
    public String toString() {

        return value;
    }

    /**
     * Overrides {@code Object.hashCode}.
     *
     * @return The object hash code.
     */
    @Override
    public int hashCode() {

        return value.hashCode();
    }

    /**
     * Overrides {@code Object.equals()}.
     *
     * @param object The object to compare to.
     * @return {@code true} if the objects are both origins with the same value, else {@code false}.
     */
    @Override
    public boolean equals(Object object) {

        return object != null && object.getClass() == this.getClass() && this.toString().equals(object.toString());
    }

    /**
     * Get the {@code value}.
     *
     * @return Returns the {@code value}.
     */
    public String getValue() {

        return value;
    }
}
