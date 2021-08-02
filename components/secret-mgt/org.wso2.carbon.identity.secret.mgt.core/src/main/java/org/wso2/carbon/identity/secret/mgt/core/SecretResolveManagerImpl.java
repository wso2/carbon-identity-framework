package org.wso2.carbon.identity.secret.mgt.core;

import org.apache.commons.codec.Charsets;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementServerException;
import org.wso2.carbon.identity.secret.mgt.core.model.ResolvedSecret;
import org.wso2.carbon.identity.secret.mgt.core.model.Secret;

import static org.wso2.carbon.identity.secret.mgt.core.constant.SecretConstants.ErrorMessages.ERROR_CODE_GET_SECRET;
import static org.wso2.carbon.identity.secret.mgt.core.util.SecretUtils.handleServerException;

public class SecretResolveManagerImpl implements SecretResolveManager {

    private final SecretManager secretManager;

    public SecretResolveManagerImpl() {

        this.secretManager = new SecretManagerImpl();
    }

    @Override
    public ResolvedSecret getResolvedSecret(String secretName) throws SecretManagementException {

        Secret secret = secretManager.getSecret(secretName);
        return getResolvedSecret(secret);
    }

    private ResolvedSecret getResolvedSecret(Secret secret) throws SecretManagementServerException {

        ResolvedSecret resolvedSecret = (ResolvedSecret) secret;
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
     * Encrypt secret.
     *
     * @param cipherText cipher text secret.
     * @return decrypted secret.
     */
    private String decrypt(String cipherText) throws CryptoException {

        return new String(CryptoUtil.getDefaultCryptoUtil().base64DecodeAndDecrypt(
                cipherText), Charsets.UTF_8);
    }
}
