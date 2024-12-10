/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.security.keystore.service;

import org.wso2.carbon.security.keystore.KeyStoreManagementException;

/**
 * Interface for generating and managing context-specific tenant key stores.
 */
public interface IdentityKeyStoreGenerator {

    /**
     * Generates a context-specific KeyStore for a given tenant domain.
     * <p>
     * This method creates a new KeyStore for the specified tenant domain and context if it does not already exist.
     * </p>
     *
     * @param tenantDomain the tenant domain for which the KeyStore is to be created.
     * @param context      the context for which the KeyStore is to be generated.
     * @throws KeyStoreManagementException if an error occurs during KeyStore creation or initialization.
     */
    void generateKeyStore(String tenantDomain, String context) throws KeyStoreManagementException;
}
