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

package org.wso2.carbon.identity.core.model;

/**
 * Represents a service provider property with a key-value pair storing ASSERTION_CONSUMER_URLS, AUDIENCES, RECIPIENTS,
 * SLO_RETURN_TO_URLS properties for a service provider.
 */
public class ServiceProviderProperty {

    private String key;
    private String value;

    public ServiceProviderProperty(String key, String value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Get the key of the tuple.
     *
     * @return Key of the tuple.
     */
    public String getKey() {
        return this.key;
    }

    /**
     * Get the value of the tuple.
     *
     * @return Value of the tuple.
     */
    public String getValue() {
        return this.value;
    }

}
