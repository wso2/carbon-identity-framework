/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com).
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
 */

package org.wso2.carbon.identity.secret.mgt.core;

import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;
import org.wso2.carbon.identity.secret.mgt.core.model.Secret;
import org.wso2.carbon.identity.secret.mgt.core.model.Secrets;

/**
 * Secret manager service interface.
 */
public interface SecretManager {

    /**
     * This API is used to create the given secret.
     *
     * @param secret The {@link Secret}.
     * @return Returns {@link Secret} created.
     * @throws SecretManagementException Secret management exception.
     */
    Secret addSecret(Secret secret) throws SecretManagementException;

    /**
     * This API is used to retrieve the given secret.
     *
     * @param secretName Name of the {@link Secret}.
     * @return Returns {@link Secret} requested.
     * @throws SecretManagementException Secret management exception.
     */
    Secret getSecret(String secretName) throws SecretManagementException;

    /**
     * Get all the secrets of the current tenant.
     *
     * @return {@link Secrets} object with all the tenant secrets.
     * @throws SecretManagementException Secret Management Exception.
     */
    Secrets getSecrets() throws SecretManagementException;

    /**
     * This API is used to delete the given secret.
     *
     * @param secretName Request to delete the {@link Secret}.
     * @throws SecretManagementException Secret management exception.
     */
    void deleteSecret(String secretName) throws SecretManagementException;

    /**
     * This function is used to get a secret by the secret id.
     *
     * @param secretId Id representing the secret.
     * @return the secret object corresponding to the secret id.
     * @throws SecretManagementException Secret management exception.
     */
    Secret getSecretById(String secretId) throws SecretManagementException;

    /**
     * This function is used to delete the given secret id.
     *
     * @param secretId Request to delete the {@link Secret}.
     * @throws SecretManagementException Secret management exception.
     */
    void deleteSecretById(String secretId) throws SecretManagementException;

    /**
     * This function is used to replace a given secret.
     *
     * @param secret secret object.
     * @throws SecretManagementException Secret management exception.
     */
    Secret replaceSecret(Secret secret) throws SecretManagementException;
}
