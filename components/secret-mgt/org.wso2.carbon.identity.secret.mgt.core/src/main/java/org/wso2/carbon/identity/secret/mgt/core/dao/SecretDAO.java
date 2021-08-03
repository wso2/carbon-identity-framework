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

package org.wso2.carbon.identity.secret.mgt.core.dao;

import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;
import org.wso2.carbon.identity.secret.mgt.core.model.Secret;

import java.util.List;

/**
 * Perform CRUD operations for {@link org.wso2.carbon.identity.secret.mgt.core.model.Secret}.
 */
public interface SecretDAO {

    /**
     * Get priority value for the {@link SecretDAO}.
     *
     * @return Priority value for the DAO.
     */
    int getPriority();

    /**
     * Add {@link Secret}.
     *
     * @param secret {@link Secret} to be added.
     * @throws SecretManagementException Secret Management Exception.
     */
    void addSecret(Secret secret) throws SecretManagementException;

    /**
     * Returns {@link Secret} by name.
     *
     * @param name     Name of the {@link Secret}.
     * @param tenantId Tenant id of the {@link Secret}.
     * @return {@link Secret} for the given name.
     * @throws SecretManagementException Secret Management Exception.
     */
    Secret getSecretByName(String name, int tenantId) throws SecretManagementException;

    /**
     * Returns {@link Secret} by id.
     *
     * @return {@link Secret} for the given name.
     * @throws SecretManagementException Secret Management Exception.
     */
    Secret getSecretById(String secretId, int tenantId) throws SecretManagementException;

    /**
     * Get secrets for the tenant.
     *
     * @param tenantId Id of the tenant.
     * @return A list of {@link Secret} for the tenant
     */
    List getSecrets(int tenantId) throws SecretManagementException;

    /**
     * Delete {@link Secret} by the given secretName.
     *
     * @param secretId Id of the {@link Secret}.
     * @param tenantId Tenant id of the {@link Secret}.
     * @throws SecretManagementException Secret Management Exception.
     */
    void deleteSecretById(String secretId, int tenantId) throws SecretManagementException;

    /**
     * Delete {@link Secret} by the given secretName.
     *
     * @param name     Name of the {@link Secret}.
     * @param tenantId Tenant id of the {@link Secret}.
     * @throws SecretManagementException Secret Management Exception.
     */
    void deleteSecretByName(String name, int tenantId) throws SecretManagementException;

    /**
     * Replace {@link Secret} or create not exists.
     *
     * @param secret {@link Secret} to be added.
     * @throws SecretManagementException Secret Management Exception.
     */
    void replaceSecret(Secret secret) throws SecretManagementException;

    /**
     * Validates whether a secrets exists with the given secret id in the tenant domain.
     *
     * @param secretId id of the secret.
     * @param tenantId id of the considered tenant domain.
     * @return whether the secret exists or not.
     * @throws SecretManagementException if an error occurs while validating the secretId.
     */
    boolean isExistingSecret(String secretId, int tenantId) throws SecretManagementException;

}
