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

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.identity.secret.mgt.core.constant.SecretConstants;
import org.wso2.carbon.identity.secret.mgt.core.dao.SecretDAO;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementClientException;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementServerException;
import org.wso2.carbon.identity.secret.mgt.core.internal.SecretManagerComponentDataHolder;
import org.wso2.carbon.identity.secret.mgt.core.model.Secret;
import org.wso2.carbon.identity.secret.mgt.core.model.Secrets;

import static org.wso2.carbon.identity.secret.mgt.core.constant.SecretConstants.DB_TABLE_NAME;
import static org.wso2.carbon.identity.secret.mgt.core.constant.SecretConstants.ErrorMessages.ERROR_CODE_GET_DAO;
import static org.wso2.carbon.identity.secret.mgt.core.constant.SecretConstants.ErrorMessages.ERROR_CODE_GET_SECRET;
import static org.wso2.carbon.identity.secret.mgt.core.constant.SecretConstants.ErrorMessages.ERROR_CODE_INVALID_SECRET_ID;
import static org.wso2.carbon.identity.secret.mgt.core.constant.SecretConstants.ErrorMessages.ERROR_CODE_SECRET_ADD_REQUEST_INVALID;
import static org.wso2.carbon.identity.secret.mgt.core.constant.SecretConstants.ErrorMessages.ERROR_CODE_SECRET_ALREADY_EXISTS;
import static org.wso2.carbon.identity.secret.mgt.core.constant.SecretConstants.ErrorMessages.ERROR_CODE_SECRET_DELETE_REQUEST_REQUIRED;
import static org.wso2.carbon.identity.secret.mgt.core.constant.SecretConstants.ErrorMessages.ERROR_CODE_SECRET_DOES_NOT_EXISTS;
import static org.wso2.carbon.identity.secret.mgt.core.constant.SecretConstants.ErrorMessages.ERROR_CODE_SECRET_GET_REQUEST_INVALID;
import static org.wso2.carbon.identity.secret.mgt.core.constant.SecretConstants.ErrorMessages.ERROR_CODE_SECRET_MANAGER_NOT_ENABLED;
import static org.wso2.carbon.identity.secret.mgt.core.constant.SecretConstants.ErrorMessages.ERROR_CODE_SECRET_REPLACE_REQUEST_INVALID;
import static org.wso2.carbon.identity.secret.mgt.core.util.SecretUtils.generateUniqueID;
import static org.wso2.carbon.identity.secret.mgt.core.util.SecretUtils.handleClientException;
import static org.wso2.carbon.identity.secret.mgt.core.util.SecretUtils.handleServerException;

/**
 * Secret Manager service implementation.
 */
public class SecretManagerImpl implements SecretManager {

    private static final Log log = LogFactory.getLog(SecretManagerImpl.class);
    private final List<SecretDAO> secretDAOS;

    public SecretManagerImpl() {

        this.secretDAOS = SecretManagerComponentDataHolder.getInstance().getSecretDAOS();
    }

    @Override
    public Secret addSecret(Secret secret) throws SecretManagementException {

        validateSecretManagerEnabled();
        validateSecretCreateRequest(secret);
        String secretId = generateUniqueID();
        secret.setSecretId(secretId);
        secret.setSecretValue(getEncryptedSecret(secret.getSecretValue(), secret.getSecretName()));
        this.getSecretDAO().addSecret(secret);
        if (log.isDebugEnabled()) {
            log.debug("Secret: " + secret.getSecretName() + " added successfully");
        }
        return secret;
    }

    @Override
    public Secret getSecret(String secretName) throws SecretManagementException {

        validateSecretManagerEnabled();
        validateSecretRetrieveRequest(secretName);
        Secret secret = this.getSecretDAO().getSecretByName(secretName, getTenantId());
        if (secret == null) {
            if (log.isDebugEnabled()) {
                log.debug("No secret found for the secretName: " + secretName);
            }
            throw handleClientException(ERROR_CODE_SECRET_DOES_NOT_EXISTS, secretName, null);
        }
        if (log.isDebugEnabled()) {
            log.debug("Secret: " + secretName + " is retrieved successfully.");
        }
        return secret;
    }

    @Override
    public Secrets getSecrets() throws SecretManagementException {

        validateSecretManagerEnabled();
        List secretList = this.getSecretDAO().getSecrets(getTenantId());
        if (secretList == null) {
            if (log.isDebugEnabled()) {
                log.debug("No secret found for the tenant: " + getTenantDomain());
            }
            throw handleClientException(
                    SecretConstants.ErrorMessages.ERROR_CODE_SECRETS_DOES_NOT_EXISTS, null);
        }
        if (log.isDebugEnabled()) {
            log.debug("All secrets of tenant: " + getTenantDomain() + " are retrieved successfully.");
        }
        return new Secrets(secretList);
    }

    @Override
    public Secret getSecretById(String secretId) throws SecretManagementException {

        validateSecretManagerEnabled();
        if (StringUtils.isBlank(secretId)) {
            throw handleClientException(ERROR_CODE_INVALID_SECRET_ID, secretId);
        }
        Secret secret = this.getSecretDAO().getSecretById(secretId, getTenantId());
        if (secret == null) {
            if (log.isDebugEnabled()) {
                log.debug("No secret found for the secretId: " + secretId);
            }
            throw handleClientException(SecretConstants.ErrorMessages.ERROR_CODE_SECRET_ID_DOES_NOT_EXISTS, secretId);
        }
        if (log.isDebugEnabled()) {
            log.debug("Secret: " + secret.getSecretName() + " is retrieved successfully.");
        }
        return secret;
    }

    @Override
    public void deleteSecret(String secretName) throws SecretManagementException {

        validateSecretManagerEnabled();
        validateSecretDeleteRequest(secretName);
        if (isSecretExist(secretName)) {
            this.getSecretDAO().deleteSecretByName(secretName, getTenantId());
            if (log.isDebugEnabled()) {
                log.debug("Secret: " + secretName + " is deleted successfully.");
            }
        } else {
            throw handleClientException(SecretConstants.ErrorMessages.ERROR_CODE_SECRET_DOES_NOT_EXISTS, secretName);
        }
    }

    @Override
    public void deleteSecretById(String secretId) throws SecretManagementException {

        validateSecretManagerEnabled();
        if (StringUtils.isBlank(secretId)) {
            throw handleClientException(ERROR_CODE_INVALID_SECRET_ID, secretId);
        }
        if (isSecretExistsById(secretId)) {
            this.getSecretDAO().deleteSecretById(secretId, getTenantId());
            if (log.isDebugEnabled()) {
                log.debug("Secret id: " + secretId + " in tenant: " + getTenantDomain() + " deleted successfully.");
            }
        } else {
            throw handleClientException(SecretConstants.ErrorMessages.ERROR_CODE_SECRET_ID_DOES_NOT_EXISTS, secretId);
        }
    }

    @Override
    public Secret replaceSecret(Secret secret) throws SecretManagementException {

        validateSecretManagerEnabled();
        validateSecretReplaceRequest(secret);
        String secretId = generateSecretId(secret.getSecretName());
        secret.setSecretId(secretId);
        secret.setSecretValue(getEncryptedSecret(secret.getSecretValue(), secret.getSecretName()));
        this.getSecretDAO().replaceSecret(secret);
        if (log.isDebugEnabled()) {
            log.debug(secret.getSecretName() + " secret replaced successfully.");
        }
        return secret;
    }

    /**
     * Validate that secret name is non-empty.
     *
     * @param secretName The secret name.
     * @throws SecretManagementException If secret validation fails.
     */
    private void validateSecretRetrieveRequest(String secretName) throws SecretManagementException {

        if (StringUtils.isEmpty(secretName)) {
            if (log.isDebugEnabled()) {
                log.debug("Invalid secret identifier with secretName: " + secretName + ".");
            }
            throw handleClientException(ERROR_CODE_SECRET_GET_REQUEST_INVALID, null);
        }
    }

    /**
     * Validate that secret name is non-empty.
     * Set tenant domain if they are not set to the secret object.
     *
     * @param secretName The secret name.
     * @throws SecretManagementException If secret validation fails.
     */
    private void validateSecretDeleteRequest(String secretName)
            throws SecretManagementException {

        if (StringUtils.isEmpty(secretName)) {
            if (log.isDebugEnabled()) {
                log.debug("Error identifying the secret with secret name: " + secretName + ".");
            }
            throw handleClientException(ERROR_CODE_SECRET_DELETE_REQUEST_REQUIRED, null);
        }

        if (!isSecretExist(secretName)) {
            if (log.isDebugEnabled()) {
                log.debug("A secret with the name: " + secretName + " does not exists.");
            }
            throw handleClientException(ERROR_CODE_SECRET_DOES_NOT_EXISTS, secretName);
        }
    }

    /**
     * Validate that secret name and value are non-empty.
     * Set tenant domain if they are not set to the secret object.
     *
     * @param secret The secret to be added.
     * @throws SecretManagementException If secret validation fails.
     */
    private void validateSecretCreateRequest(Secret secret) throws SecretManagementException {

        if (StringUtils.isEmpty(secret.getSecretName()) || StringUtils.isEmpty(secret.getSecretValue())) {
            throw handleClientException(ERROR_CODE_SECRET_ADD_REQUEST_INVALID, null);
        }
        if (isSecretExist(secret.getSecretName())) {
            if (log.isDebugEnabled()) {
                log.debug("A secret with the name: " + secret.getSecretName() + " does not exists.");
            }
            throw handleClientException(ERROR_CODE_SECRET_ALREADY_EXISTS, secret.getSecretName());
        }
        if (StringUtils.isEmpty(secret.getTenantDomain())) {
            secret.setTenantDomain(getTenantDomain());
        }
    }

    /**
     * Validate that secret name. Validate the secret existence.
     * Set tenant domain if it is not set to the secret object.
     *
     * @param secret The secret to be replaced.
     * @throws SecretManagementException If secret validation fails.
     */
    private void validateSecretReplaceRequest(Secret secret)
            throws SecretManagementException {

        if (StringUtils.isEmpty(secret.getSecretName()) || StringUtils.isEmpty(secret.getSecretValue())) {
            throw handleClientException(ERROR_CODE_SECRET_REPLACE_REQUEST_INVALID, null);
        }

        if (!isSecretExist(secret.getSecretName())) {
            if (log.isDebugEnabled()) {
                log.debug("A secret with the name: " + secret.getSecretName() + " does not exists.");
            }
            throw handleClientException(ERROR_CODE_SECRET_DOES_NOT_EXISTS, secret.getSecretName());
        }

        if (StringUtils.isEmpty(secret.getTenantDomain())) {
            secret.setTenantDomain(getTenantDomain());
        }
    }

    /**
     * Select highest priority Secret DAO from an already sorted list of Secret DAOs.
     *
     * @return Highest priority Secret DAO.
     */
    private SecretDAO getSecretDAO() throws SecretManagementException {

        if (!this.secretDAOS.isEmpty()) {
            return secretDAOS.get(secretDAOS.size() - 1);
        } else {
            throw handleServerException(ERROR_CODE_GET_DAO, "secretDAOs");
        }
    }

    private void validateSecretManagerEnabled() throws SecretManagementServerException {

        if (!SecretManagerComponentDataHolder.getInstance().isSecretManagementEnabled()) {
            throw handleServerException(ERROR_CODE_SECRET_MANAGER_NOT_ENABLED, DB_TABLE_NAME);
        }
    }

    private int getTenantId() {

        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
    }

    private String getTenantDomain() {

        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
    }

    private boolean isSecretExist(String secretName) throws SecretManagementException {

        try {
            getSecret(secretName);
        } catch (SecretManagementClientException e) {
            if (ERROR_CODE_SECRET_DOES_NOT_EXISTS.getCode().equals(e.getErrorCode())) {
                return false;
            }
            throw e;
        }
        return true;
    }

    private boolean isSecretExistsById(String secretId) throws SecretManagementException {

        if (StringUtils.isBlank(secretId)) {
            if (log.isDebugEnabled()) {
                log.debug("A secret with the id: " + secretId + " does not exists.");
            }
            throw handleClientException(ERROR_CODE_INVALID_SECRET_ID, secretId);
        }
        return this.getSecretDAO().isExistingSecret(secretId, getTenantId());
    }

    private String generateSecretId(String secretName) throws SecretManagementException {

        String secretId;
        if (isSecretExist(secretName)) {
            secretId = getSecret(secretName).getSecretId();
        } else {
            secretId = generateUniqueID();
            if (log.isDebugEnabled()) {
                log.debug("Secret id generated: " + secretId);
            }
        }
        return secretId;
    }

    private String getEncryptedSecret(String secretValue, String name) throws SecretManagementServerException {

        try {
            return encrypt(secretValue);
        } catch (CryptoException e) {
            throw handleServerException(ERROR_CODE_GET_SECRET, name, e);
        }
    }

    /**
     * Encrypt secret.
     *
     * @param plainText plain text secret.
     * @return encrypted secret.
     */
    private String encrypt(String plainText) throws CryptoException {

        return CryptoUtil.getDefaultCryptoUtil().encryptAndBase64Encode(
                plainText.getBytes());
    }
}
