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

package org.wso2.carbon.identity.external.api.token.handler.api.model;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.identity.external.api.token.handler.api.exception.TokenRequestException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.wso2.carbon.identity.external.api.token.handler.api.constant.ErrorMessageConstant.ErrorMessage;

/**
 * Model class for Grant Type Context.
 */
public class GrantContext {

    private final GrantType grantType;
    private final Map<String, String> properties;

    private GrantContext(GrantContext.Builder builder) {

        this.grantType = builder.grantType;
        this.properties = Collections.unmodifiableMap(new HashMap<>(builder.resolvedGrantProperties));
    }

    /**
     * Get Grant Type.
     *
     * @return Grant Type.
     */
    public GrantType getGrantType() {

        return grantType;
    }

    /**
     * Get Property value for the given property name.
     *
     * @param propertyName Property name.
     * @return Property value.
     */
    public String getProperty(String propertyName) {

        return properties.get(propertyName);
    }

    /**
     * Builder class for GrantContext.
     */
    public static class Builder {

        private GrantType grantType;
        private Map<String, String> propertyMap = new HashMap<>();
        private final Map<String, String> resolvedGrantProperties = new HashMap<>();

        /**
         * Set Grant Type.
         *
         * @param grantType Grant Type.
         * @return Builder.
         */
        public Builder grantType(GrantType grantType) {

            this.grantType = grantType;
            return this;
        }

        /**
         * Set Properties.
         *
         * @param properties Properties.
         * @return Builder.
         */
        public Builder properties(Map<String, String> properties) {

            this.propertyMap = properties != null ? new HashMap<>(properties) : new HashMap<>();
            return this;
        }

        /**
         * Build GrantContext.
         *
         * @return GrantContext.
         * @throws TokenRequestException TokenHandlerException.
         */
        public GrantContext build() throws TokenRequestException {

            if (grantType == null) {
                throw new TokenRequestException(ErrorMessage.ERROR_CODE_MISSING_GRANT_TYPE, null);
            }

            switch (grantType) {
                case CLIENT_CREDENTIAL:
                    resolvedGrantProperties.put(
                            Property.CLIENT_ID.getName(), getProperty(Property.CLIENT_ID.getName()));
                    resolvedGrantProperties.put(
                            Property.CLIENT_SECRET.getName(), getProperty(Property.CLIENT_SECRET.getName()));
                    resolvedGrantProperties.put(Property.SCOPE.getName(), getProperty(Property.SCOPE.getName()));
                    break;
            }
            return new GrantContext(this);
        }

        private String getProperty(String propertyName) throws TokenRequestException {

            if (propertyMap != null && propertyMap.containsKey(propertyName)) {
                String propValue = propertyMap.get(propertyName);
                if (StringUtils.isNotBlank(propValue)) {
                    return propValue;
                }
                throw new TokenRequestException(ErrorMessage.ERROR_CODE_BLANK_AUTH_PROPERTY, propertyName);
            }

            throw new TokenRequestException(ErrorMessage.ERROR_CODE_MISSING_AUTH_PROPERTY, propertyName);
        }
    }

    /**
     * Authentication Grant Type.
     */
    public enum GrantType {

        CLIENT_CREDENTIAL
    }

    /**
     * Authentication Property Enum.
     */
    public enum Property {

        CLIENT_ID("client_id"),
        CLIENT_SECRET("client_secret"),
        SCOPE("scope");

        private final String name;

        Property(String name) {

            this.name = name;
        }

        public String getName() {

            return name;
        }
    }
}
