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
import org.wso2.carbon.identity.external.api.client.api.exception.APIClientRequestException;

import static org.wso2.carbon.identity.external.api.client.api.constant.ErrorMessageConstant.ErrorMessage.ERROR_CODE_INVALID_AUTH_PROPERTY;

/**
 * Model class for API Authentication Property.
 */
public class APIAuthProperty {

    private static final Log LOG = LogFactory.getLog(APIAuthProperty.class);

    private final String name;
    private final String value;

    /**
     * Constructor for APIAuthProperty.
     *
     * @param authPropertyBuilder Builder instance.
     */
    public APIAuthProperty(Builder authPropertyBuilder) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating APIAuthProperty with name: " + authPropertyBuilder.name);
        }
        this.name = authPropertyBuilder.name;
        this.value = authPropertyBuilder.value;
    }

    /**
     * Get the name of the authentication property.
     *
     * @return name
     */
    public String getName() {

        return name;
    }

    /**
     * Get the value of the authentication property.
     *
     * @return value
     */
    public String getValue() {

        return value;
    }

    /**
     * Authentication Property Builder.
     */
    public static class Builder {

        private final String name;
        private final String value;

        public Builder(String name, String value) {

            this.name = name;
            this.value = value;
        }

        /**
         * Build the APIAuthProperty instance.
         *
         * @return APIAuthProperty instance.
         * @throws APIClientRequestException If validation fails.
         */
        public APIAuthProperty build() throws APIClientRequestException {

            if (StringUtils.isBlank(name) || value == null) {
                LOG.error("Invalid auth property: name is blank or value is null");
                throw new APIClientRequestException(ERROR_CODE_INVALID_AUTH_PROPERTY, null);
            }

            return new APIAuthProperty(this);
        }
    }
}
