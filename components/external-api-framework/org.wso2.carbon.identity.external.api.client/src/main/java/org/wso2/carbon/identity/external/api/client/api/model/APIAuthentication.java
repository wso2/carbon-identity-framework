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

package org.wso2.carbon.identity.external.api.client.api.model;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.external.api.client.api.constant.ErrorMessageConstant.ErrorMessage;
import org.wso2.carbon.identity.external.api.client.api.exception.APIClientRequestException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Model class for supported authentication types and their properties.
 */
public class APIAuthentication {

    private static final Log LOG = LogFactory.getLog(APIAuthentication.class);

    private final AuthType authType;
    private final List<APIAuthProperty> properties;

    public APIAuthentication(Builder builder) {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Creating APIAuthentication with auth type: %s", builder.authType));
        }

        this.authType = builder.authType;
        this.properties = Collections.unmodifiableList(new ArrayList<>(builder.resolvedAuthProperties));
    }

    /**
     * Get the authentication type.
     *
     * @return AuthType
     */
    public AuthType getType() {

        return authType;
    }

    /**
     * Get the list of authentication properties.
     *
     * @return List of APIAuthProperty
     */
    public List<APIAuthProperty> getProperties() {

        return Collections.unmodifiableList(properties);
    }

    /**
     * Get the authentication property by property name.
     *
     * @param propertyName Property enum
     * @return APIAuthProperty
     */
    public APIAuthProperty getProperty(Property propertyName) {

        return this.properties.stream()
                .filter(property -> propertyName.getName().equals(property.getName()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Builder class for APIAuthentication.
     */
    public static class Builder {

        private AuthType authType;
        private Map<String, String> authPropertiesMap = new HashMap<>();
        private List<APIAuthProperty> resolvedAuthProperties;

        public Builder authType(AuthType authType) {

            this.authType = authType;
            return this;
        }

        public Builder properties(Map<String, String> authPropertiesMap) {

            this.authPropertiesMap = authPropertiesMap != null ? new HashMap<>(authPropertiesMap) : new HashMap<>();
            return this;
        }

        /**
         * Build the APIAuthentication instance.
         *
         * @return APIAuthentication
         * @throws APIClientRequestException If required properties are missing or invalid
         */
        public APIAuthentication build() throws APIClientRequestException {

            if (authType == null) {
                throw new APIClientRequestException(ErrorMessage.ERROR_CODE_MISSING_AUTH_TYPE, null);
            }

            resolvedAuthProperties = new ArrayList<>();
            switch (authType) {
                case BASIC:
                    resolvedAuthProperties.add(buildAuthProperty(Property.USERNAME.getName()));
                    resolvedAuthProperties.add(buildAuthProperty(Property.PASSWORD.getName()));
                    break;
                case BEARER:
                    resolvedAuthProperties.add(buildAuthProperty(Property.ACCESS_TOKEN.getName()));
                    break;
                case API_KEY:
                    resolvedAuthProperties.add(buildAuthProperty(Property.HEADER.getName()));
                    resolvedAuthProperties.add(buildAuthProperty(Property.VALUE.getName()));
                    break;
                case NONE:
                    break;
            }
            authPropertiesMap.clear();
            return new APIAuthentication(this);
        }

        private APIAuthProperty buildAuthProperty(String propertyName) throws APIClientRequestException {

            if (authPropertiesMap.containsKey(propertyName)) {
                String propValue = authPropertiesMap.get(propertyName);
                if (StringUtils.isNotBlank(propValue)) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Authentication property " + propertyName + " built successfully.");
                    }
                    return new APIAuthProperty.Builder(propertyName, propValue).build();
                }
                throw new APIClientRequestException(ErrorMessage.ERROR_CODE_BLANK_AUTH_PROPERTY, propertyName);
            }
            throw new APIClientRequestException(ErrorMessage.ERROR_CODE_MISSING_AUTH_PROPERTY, propertyName);
        }
    }

    /**
     * Enum for supported Authentication Types.
     */
    public enum AuthType {

        NONE("NONE"),
        BEARER("BEARER"),
        BASIC("BASIC"),
        API_KEY("API_KEY");

        private final String name;

        AuthType(String name) {

            this.name = name;
        }

        public String getName() {

            return name;
        }
    }

    /**
     * Enum for supported Authentication Properties.
     */
    public enum Property {

        USERNAME("username"),
        PASSWORD("password"),
        HEADER("header"),
        VALUE("value"),
        ACCESS_TOKEN("accessToken");

        private final String name;

        Property(String name) {

            this.name = name;
        }

        public String getName() {

            return name;
        }
    }
}
