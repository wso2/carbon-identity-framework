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

import org.apache.commons.codec.Charsets;
import org.osgi.annotation.bundle.Capability;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementServerException;
import org.wso2.carbon.identity.secret.mgt.core.model.ResolvedSecret;
import org.wso2.carbon.identity.secret.mgt.core.model.Secret;

import static org.wso2.carbon.identity.secret.mgt.core.constant.SecretConstants.ErrorMessages.ERROR_CODE_GET_SECRET;
import static org.wso2.carbon.identity.secret.mgt.core.util.SecretUtils.handleServerException;

@Capability(
        namespace = "osgi.service",
        attribute = {
                "objectClass=org.wso2.carbon.identity.secret.mgt.core.SecretResolveManager",
                "service.scope=singleton"
        }
)
public class SecretResolveManagerImpl implements SecretResolveManager {

    private final SecretManager secretManager;

    public SecretResolveManagerImpl() {

        this.secretManager = new SecretManagerImpl();
    }

    @Override
    public ResolvedSecret getResolvedSecret(String secretTypeName, String secretName) throws SecretManagementException {

        Secret secret = secretManager.getSecret(secretTypeName, secretName);
        return getResolvedSecret(secret);
    }

    private ResolvedSecret getResolvedSecret(Secret secret) throws SecretManagementServerException {

        ResolvedSecret resolvedSecret = new ResolvedSecret();
        resolvedSecret.setSecretId(secret.getSecretId());
        resolvedSecret.setSecretName(secret.getSecretName());
        resolvedSecret.setCreatedTime(secret.getCreatedTime());
        resolvedSecret.setLastModified(secret.getLastModified());
        resolvedSecret.setTenantDomain(secret.getTenantDomain());
        resolvedSecret.setResolvedSecretValue(getDecryptedSecretValue(secret.getSecretValue(), secret.getSecretName()));
        return resolvedSecret;
    }

    private String getDecryptedSecretValue(String secretValue, String name) throws SecretManagementServerException {

        try {
            return decrypt(secretValue);
        } catch (CryptoException e) {
            throw handleServerException(ERROR_CODE_GET_SECRET, name, e);
        }
    }

    /**
     * Decrypt secret.
     *
     * @param cipherText cipher text secret.
     * @return decrypted secret.
     */
    private String decrypt(String cipherText) throws CryptoException {

        return new String(CryptoUtil.getDefaultCryptoUtil().base64DecodeAndDecrypt(
                cipherText), Charsets.UTF_8);
    }
}
