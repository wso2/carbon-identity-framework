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
import org.wso2.carbon.identity.secret.mgt.core.model.SecretType;
import org.wso2.carbon.identity.secret.mgt.core.model.Secrets;

/**
 * Secret manager service interface.
 */
public interface SecretManager {

    /**
     * This API is used to create the given secret.
     *
     * @param secretTypeName Name of the {@link SecretType}.
     * @param secret         The {@link Secret}.
     * @return Returns {@link Secret} created.
     * @throws SecretManagementException Secret management exception.
     */
    Secret addSecret(String secretTypeName, Secret secret) throws SecretManagementException;

    /**
     * This API is used to retrieve the given secret.
     *
     * @param secretTypeName Name of the {@link SecretType}.
     * @param secretName     Name of the {@link Secret}.
     * @return Returns {@link Secret} requested.
     * @throws SecretManagementException Secret management exception.
     */
    Secret getSecret(String secretTypeName, String secretName) throws SecretManagementException;

    /**
     * Get all the secrets of the current tenant.
     *
     * @param secretTypeName Name of the {@link SecretType}.
     * @return {@link Secrets} object with all the tenant secrets.
     * @throws SecretManagementException Secret Management Exception.
     */
    Secrets getSecrets(String secretTypeName) throws SecretManagementException;

    /**
     * This API is used to delete the given secret.
     *
     * @param secretTypeName Name of the {@link SecretType}.
     * @param secretName     Name of the {@link Secret}
     * @throws SecretManagementException Secret management exception.
     */
    void deleteSecret(String secretTypeName, String secretName) throws SecretManagementException;

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
     * @param secretTypeName Name of the {@link SecretType}.
     * @param secret         Secret object.
     * @throws SecretManagementException Secret management exception.
     */
    Secret replaceSecret(String secretTypeName, Secret secret) throws SecretManagementException;

    /**
     * Add {@link SecretType}.
     *
     * @param secretType {@link SecretType} to be added.
     * @throws SecretManagementException Secret Management Exception.
     */
    SecretType addSecretType(SecretType secretType) throws SecretManagementException;

    /**
     * Replace {@link SecretType}.
     *
     * @param secretType {@link SecretType} to be replaced.
     * @throws SecretManagementException Secret Management Exception.
     */
    SecretType replaceSecretType(SecretType secretType) throws SecretManagementException;

    /**
     * Get {@link SecretType} by name.
     *
     * @param secretTypeName Name of the {@link SecretType}.
     * @return {@link SecretType} for the given name.
     * @throws SecretManagementException Secret Management Exception.
     */
    SecretType getSecretType(String secretTypeName) throws SecretManagementException;

    /**
     * Delete {@link SecretType} by name.
     *
     * @param secretTypeName Name of the {@link SecretType}.
     * @throws SecretManagementException Configuration Management Exception.
     */
    void deleteSecretType(String secretTypeName) throws SecretManagementException;

    /**
     * Update value of a secret by name.
     *
     * @param secretTypeName Name of the {@link SecretType}.
     * @param name  Name of the {@link Secret}.
     * @param value secret value to be updated.
     * @throws SecretManagementException Configuration Management Exception.
     */
    Secret updateSecretValue(String secretTypeName, String name, String value) throws SecretManagementException;

    /**
     * Delete description of a secret by name.
     *
     * @param secretTypeName Name of the {@link SecretType}.
     * @param name  Name of the {@link Secret}.
     * @param description secret description to be updated.
     * @throws SecretManagementException Configuration Management Exception.
     */
    Secret updateSecretDescription(String secretTypeName, String name, String description) throws SecretManagementException;

    /**
     * Checks for the existence of a given secret.
     *
     * @param secretTypeName Name of the {@link SecretType}.
     * @param secretName  Name of the {@link Secret}.
     * @throws SecretManagementException Configuration Management Exception.
     */
    boolean isSecretExist(String secretTypeName, String secretName) throws SecretManagementException;
}
