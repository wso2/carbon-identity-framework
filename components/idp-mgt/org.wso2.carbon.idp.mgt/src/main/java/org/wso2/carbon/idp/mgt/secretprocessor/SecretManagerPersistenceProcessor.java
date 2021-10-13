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

import org.apache.commons.io.Charsets;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;
import org.wso2.carbon.identity.secret.mgt.core.model.Secret;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementServerException;
import org.wso2.carbon.idp.mgt.internal.IdpMgtServiceComponentHolder;

import static org.wso2.carbon.identity.secret.mgt.core.constant.SecretConstants.ErrorMessages.ERROR_CODE_INVALID_SECRET_ID;
import static org.wso2.carbon.identity.secret.mgt.core.constant.SecretConstants.ErrorMessages.ERROR_CODE_SECRET_ID_DOES_NOT_EXISTS;

/**
 * An implementation of SecretPersistenceProcessor which uses the SecretManager to store the identity provider
 * secrets in an encrypted format in the IDN_SECRET table.
 */
public class SecretManagerPersistenceProcessor implements SecretPersistenceProcessor {

    private static final Log log = LogFactory.getLog(SecretManagerPersistenceProcessor.class);
    private static final String secretTypeName = "FED_AUTH";

    @Override
    public String addSecret(int idpId, String name, String propertyName, String secretValue)
            throws IdentityProviderManagementServerException {

        String secretName = name + "_" + idpId + "_" + propertyName;
        String description = propertyName + " of " + name + " for IdP with id " + idpId;

        Secret secret = new Secret();
        secret.setSecretName(secretName);
        secret.setSecretValue(secretValue);
        secret.setDescription(description);

        try {
            Secret encryptedSecret = IdpMgtServiceComponentHolder.getInstance().getSecretManager()
                    .addSecret(secretTypeName, secret);
            return encryptedSecret.getSecretId();
        } catch (SecretManagementException e) {
            String errorMessage = String.format("Server encountered an error while persisting %s value of %s " +
                    "for IdP with id %s", propertyName, name, idpId);
            throw new IdentityProviderManagementServerException(errorMessage, e);
        }
    }

    @Override
    public String getPreprocessedSecret(String secretId) throws IdentityProviderManagementServerException {

        try {
            Secret secret = IdpMgtServiceComponentHolder.getInstance().getSecretManager().getSecretById(secretId);
            return decrypt(secret.getSecretValue());
        } catch (SecretManagementException e) {
            if (!(StringUtils.equals(e.getErrorCode(), ERROR_CODE_SECRET_ID_DOES_NOT_EXISTS.getCode()) ||
                    StringUtils.equals(e.getErrorCode(), ERROR_CODE_INVALID_SECRET_ID.getCode()))) {
                String errorMessage = String.format("Server encountered an error while retrieving the preprocessed " +
                        "secret for secret with id %s", secretId);
                throw new IdentityProviderManagementServerException(errorMessage, e);
            }
        } catch (CryptoException e) {
            String errorMessage = String.format("Server encountered an error while decrypting the secret with " +
                    "id %s", secretId);
            throw new IdentityProviderManagementServerException(errorMessage, e);
        }

        return secretId;
    }

    @Override
    public void deleteSecret(String secretId) throws IdentityProviderManagementServerException {

        try {
            IdpMgtServiceComponentHolder.getInstance().getSecretManager().deleteSecretById(secretId);
        } catch (SecretManagementException e) {
            if (!StringUtils.equals(e.getErrorCode(), ERROR_CODE_SECRET_ID_DOES_NOT_EXISTS.getCode())) {
                String errorMessage = String.format("Server encountered an error while deleting the secret with " +
                        "id %s", secretId);
                throw new IdentityProviderManagementServerException(errorMessage, e);
            }
        }
    }

    @Override
    public String updateSecret(String secretId, String secretValue) throws IdentityProviderManagementServerException {

        try {
            Secret secret = IdpMgtServiceComponentHolder.getInstance().getSecretManager()
                    .updateSecretValueById(secretId, secretValue);
            return secret.getSecretId();
        } catch (SecretManagementException e) {
            String errorMessage = String.format("Server encountered an error while updating the secret with " +
                    "id %s", secretId);
            throw new IdentityProviderManagementServerException(errorMessage, e);
        }
    }


    private String decrypt(String cipherText) throws CryptoException {

        return new String(CryptoUtil.getDefaultCryptoUtil().base64DecodeAndDecrypt(cipherText), Charsets.UTF_8);
    }

}
