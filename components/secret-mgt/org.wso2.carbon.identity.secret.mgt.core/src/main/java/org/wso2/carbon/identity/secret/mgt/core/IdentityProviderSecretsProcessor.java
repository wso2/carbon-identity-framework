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

import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;

/**
 * IDP related secrets processor service interface.
 */
public interface IdentityProviderSecretsProcessor {

    /**
     * This API is used to store encrypted IDP property secrets while adding new IDPs or updating existing IDPs.
     *
     * @param identityProvider Name of the {@link IdentityProvider}.
     * @return Returns {@link IdentityProvider} created.
     * @throws SecretManagementException Secret management exception.
     */
    IdentityProvider addOrUpdateIdpSecrets(IdentityProvider identityProvider) throws
            SecretManagementException;

    /**
     * This API is used to get decrypted IDP property secrets while getting existing IDP data.
     *
     * @param identityProvider Name of the {@link IdentityProvider}.
     * @return Returns {@link IdentityProvider} created.
     * @throws SecretManagementException Secret management exception.
     */
    IdentityProvider getIdpSecrets(IdentityProvider identityProvider) throws
            SecretManagementException;

    /**
     * This API is used to remove secrets belongs to the IDP that is being deleted
     *
     * @param identityProvider Name of the {@link IdentityProvider}.
     * @throws SecretManagementException Secret management exception.
     */
    void deleteIdpSecrets(IdentityProvider identityProvider) throws
            SecretManagementException;
}
