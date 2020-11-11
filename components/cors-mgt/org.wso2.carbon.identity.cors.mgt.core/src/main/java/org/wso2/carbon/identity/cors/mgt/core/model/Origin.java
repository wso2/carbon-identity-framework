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
import org.wso2.carbon.identity.cors.mgt.core.exception.CORSManagementServiceClientException;

import java.net.IDN;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import static org.wso2.carbon.identity.cors.mgt.core.constant.ErrorMessages.ERROR_CODE_INVALID_URI;
import static org.wso2.carbon.identity.cors.mgt.core.constant.ErrorMessages.ERROR_CODE_MISSING_HOST;
import static org.wso2.carbon.identity.cors.mgt.core.constant.ErrorMessages.ERROR_CODE_MISSING_SCHEME;
import static org.wso2.carbon.identity.cors.mgt.core.constant.ErrorMessages.ERROR_CODE_NULL_ORIGIN;

/**
 * A class which can represent an instance of an origin. 
 */
public class Origin {

    /**
     * The original origin value, used in hash code generation and equality checking.
     */
    private final String value;

    /**
     * The origin scheme.
     */
    private String scheme;

    /**
     * The origin host.
     */
    private String host;

    /**
     * The parsed origin port, -1 for default port.
     */
    private int port = -1;

    /**
     * Creates a new origin from the specified URI string. Note that the syntax is not validated.
     *
     * @param origin The URI string for the origin. Must not be {@code null}.
     */
    public Origin(final String origin) throws CORSManagementServiceClientException {

        this.value = origin;

        // Check whether the origin is null.
        if (StringUtils.isBlank(origin)) {
            throw new CORSManagementServiceClientException(ERROR_CODE_NULL_ORIGIN.getMessage(),
                    ERROR_CODE_NULL_ORIGIN.getCode());
        }

        // Parse URI value.
        URI uri;
        try {
            uri = new URI(origin);
        } catch (URISyntaxException e) {
            throw new CORSManagementServiceClientException(
                    String.format(ERROR_CODE_INVALID_URI.getMessage(), origin),
                    ERROR_CODE_INVALID_URI.getCode());
        }

        scheme = uri.getScheme();
        host = uri.getHost();
        port = uri.getPort();

        if (scheme == null) {
            throw new CORSManagementServiceClientException(
                    String.format(ERROR_CODE_MISSING_SCHEME.getMessage(), origin),
                    ERROR_CODE_MISSING_SCHEME.getCode());
        }

        // Canonicalise scheme and host.
        scheme = scheme.toLowerCase(Locale.ENGLISH);

        if (host == null) {
            throw new CORSManagementServiceClientException(
                    String.format(ERROR_CODE_MISSING_HOST.getMessage(), origin),
                    ERROR_CODE_MISSING_HOST.getCode());
        }

        // Apply the IDNA to ASCII algorithm [RFC3490] to /host/.
        host = IDN.toASCII(host, IDN.ALLOW_UNASSIGNED | IDN.USE_STD3_ASCII_RULES);

        // Convert to lower case.
        host = host.toLowerCase(Locale.ENGLISH);
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

    /**
     * Returns the scheme.
     *
     * @return The scheme.
     */
    public String getScheme() {

        return scheme;
    }

    /**
     * Returns the host (name or IP address).
     *
     * @return The host name or IP address.
     */
    public String getHost() {

        return host;
    }

    /**
     * Returns the port number.
     *
     * @return The port number, -1 for default port.
     */
    public int getPort() {

        return port;
    }

    /**
     * Returns the suffix which is made up of the host name / IP address
     * and port (if a non-default port is specified).
     *
     * @return The suffix.
     */
    public String getSuffix() {

        String suffix = host;
        if (port != -1) {
            suffix = suffix + ":" + port;
        }
        return suffix;
    }
}
