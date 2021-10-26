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

package org.wso2.carbon.idp.mgt.secretprocessor;

import org.wso2.carbon.idp.mgt.IdentityProviderManagementServerException;

/**
 * SecretPersistenceProcessor implementations are used to process identity provider related secrets just before
 * storing them in the database.
 * E.g. to encrypt secrets before storing them in the database.
 */
public interface SecretPersistenceProcessor {

    String addSecret(int idpId, String name, String propertyName, String secretValue, String secretType) throws
            IdentityProviderManagementServerException;

    String getPreprocessedSecret(String secretId) throws IdentityProviderManagementServerException;

    void deleteSecret(String secretId) throws IdentityProviderManagementServerException;

    String updateSecret(String secretId, String secretValue) throws IdentityProviderManagementServerException;
}
