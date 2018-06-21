/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.endpoint.client.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Object model that represents the error response returned from /api/identity/auth/v1.0 API.
 */
public class AuthenticationErrorResponse extends AuthenticationResponse {

    private String code = null;
    private String message = null;
    private String description = null;
    private Map<String, String> properties = new HashMap<String, String>();

    /**
     * Returns the error code.
     *
     * @return error code
     */
    public String getCode() {
        return code;
    }

    /**
     * Sets error code.
     *
     * @param code error code
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Returns error message.
     *
     * @return error message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets error message.
     *
     * @param message error message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Returns error description.
     *
     * @return error description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets error description.
     *
     * @param description error description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns any additional properties in error response.
     *
     * @return key value pair map
     */
    public Map<String, String> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    /**
     * Sets additional properties.
     *
     * @param properties key value pair map
     */
    public void setProperties(Map<String, String> properties) {
        if (properties != null || !properties.isEmpty()) {
            this.properties.putAll(properties);
        }
    }
}

