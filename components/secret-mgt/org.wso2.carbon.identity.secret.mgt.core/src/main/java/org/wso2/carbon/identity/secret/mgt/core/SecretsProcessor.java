/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.secret.mgt.core;

import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;

/**
 * Secrets processor service interface for Identity Provider, ServiceProvider etc.
 */
public interface SecretsProcessor<T> {

    /**
     * This method is used to encrypt and store the secret values in the DB
     * and replace the secret value in the object with a reference.
     *
     * @param object Name of the {@link T}.
     * @throws SecretManagementException Secret management exception.
     */
    T encryptAssociatedSecrets(T object) throws SecretManagementException;

    /**
     * This method is used to retrieve encrypted secret values from the DB, decrypt them
     * and replace secret references in the object.
     *
     * @param object Name of the {@link T}.
     * @throws SecretManagementException Secret management exception.
     */
    T decryptAssociatedSecrets(T object) throws SecretManagementException;

    /**
     * This API is used to remove secrets from the DB which belongs to the object that is being deleted.
     *
     * @param object Name of the {@link T}.
     * @throws SecretManagementException Secret management exception.
     */
    void deleteAssociatedSecrets(T object) throws SecretManagementException;

}
