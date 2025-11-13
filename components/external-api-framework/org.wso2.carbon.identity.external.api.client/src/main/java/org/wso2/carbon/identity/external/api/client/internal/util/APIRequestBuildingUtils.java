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

package org.wso2.carbon.identity.external.api.client.internal.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.wso2.carbon.identity.external.api.client.api.model.APIAuthentication;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Utility class for building API request components.
 */
public class APIRequestBuildingUtils {

    private static final Log LOG = LogFactory.getLog(APIRequestBuildingUtils.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * Builds the authentication header based on the authentication type.
     *
     * @param apiAuthentication The API authentication configuration.
     * @return Header object containing the authentication information.
     */
    public static Header buildAuthenticationHeader(APIAuthentication apiAuthentication) {

        if (apiAuthentication == null || apiAuthentication.getType() == null) {
            LOG.debug("API authentication or authentication type is null. Skipping header construction.");
            return null;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Building authentication header for type: " + apiAuthentication.getType());
         }
        switch (apiAuthentication.getType()) {
            case BASIC:
                String credentials = apiAuthentication.getProperty(APIAuthentication.Property.USERNAME).getValue()
                        + ":" + apiAuthentication.getProperty(APIAuthentication.Property.PASSWORD).getValue();
                byte[] encodedBytes = Base64.getEncoder().encode(credentials.getBytes(StandardCharsets.UTF_8));
                Header basicHeader = new BasicHeader(AUTHORIZATION_HEADER,
                        "Basic " + new String(encodedBytes, StandardCharsets.UTF_8));
                LOG.debug("Basic authentication header created successfully.");
                return basicHeader;
            case BEARER:
                Header bearerHeader = new BasicHeader(AUTHORIZATION_HEADER,
                        "Bearer " + apiAuthentication.getProperty(APIAuthentication.Property.ACCESS_TOKEN).getValue());
                LOG.debug("Bearer authentication header created successfully.");
                return bearerHeader;
            case API_KEY:
                Header apiKeyHeader = new BasicHeader(
                        apiAuthentication.getProperty(APIAuthentication.Property.HEADER).getValue(),
                        apiAuthentication.getProperty(APIAuthentication.Property.VALUE).getValue());
                LOG.debug("API Key authentication header created successfully.");
                return apiKeyHeader;
            default:
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Returning null for authentication type: " + apiAuthentication.getType());
                }
                return null;
        }
    }
}
