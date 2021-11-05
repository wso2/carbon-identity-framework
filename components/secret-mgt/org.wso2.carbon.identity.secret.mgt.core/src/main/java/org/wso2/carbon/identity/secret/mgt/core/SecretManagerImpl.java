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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.identity.secret.mgt.core.dao.SecretDAO;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementClientException;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementServerException;
import org.wso2.carbon.identity.secret.mgt.core.internal.SecretManagerComponentDataHolder;
import org.wso2.carbon.identity.secret.mgt.core.model.Secret;
import org.wso2.carbon.identity.secret.mgt.core.model.Secrets;

import java.util.List;
import java.util.Set;

import static org.wso2.carbon.identity.secret.mgt.core.constant.SecretConstants.ErrorMessages;
import static org.wso2.carbon.identity.secret.mgt.core.util.SecretUtils.generateUniqueID;
import static org.wso2.carbon.identity.secret.mgt.core.util.SecretUtils.getSecretDescriptionRegex;
import static org.wso2.carbon.identity.secret.mgt.core.util.SecretUtils.getSecretNameRegex;
import static org.wso2.carbon.identity.secret.mgt.core.util.SecretUtils.getSecretValueRegex;
import static org.wso2.carbon.identity.secret.mgt.core.util.SecretUtils.handleClientException;
import static org.wso2.carbon.identity.secret.mgt.core.util.SecretUtils.handleServerException;
import static org.wso2.carbon.identity.secret.mgt.core.util.SecretUtils.isSecretDescriptionRegexValid;
import static org.wso2.carbon.identity.secret.mgt.core.util.SecretUtils.isSecretNameRegexValid;
import static org.wso2.carbon.identity.secret.mgt.core.util.SecretUtils.isSecretValueRegexValid;

/**
 * Secret Manager service implementation.
 */
public class SecretManagerImpl implements SecretManager {

    private static final Log log = LogFactory.getLog(SecretManagerImpl.class);
    private final List<SecretDAO> secretDAOS;
    private final Set<String> secretTypes;

    public SecretManagerImpl() {

        this.secretDAOS = SecretManagerComponentDataHolder.getInstance().getSecretDAOS();
        this.secretTypes = SecretManagerComponentDataHolder.getInstance().getSecretTypes();
    }

    @Override
    public Secret addSecret(String secretType, Secret secret) throws SecretManagementException {

        validateSecretManagerEnabled();
        validateSecretCreateRequest(secretType, secret);
        String secretId = generateUniqueID();
        secret.setSecretId(secretId);
        secret.setSecretType(secretType);
        secret.setSecretValue(getEncryptedSecret(secret.getSecretValue(), secret.getSecretName()));
        this.getSecretDAO().addSecret(secret);
        if (log.isDebugEnabled()) {
            log.debug("Secret: " + secret.getSecretName() + " added successfully");
        }
        return secret;
    }

    @Override
    public Secrets listSecrets(String secretType) throws SecretManagementException {

        validateSecretManagerEnabled();
        validateSecretType(secretType);

        List<Secret> secretList = this.getSecretDAO().listSecrets(secretType, getTenantId());
        if (secretList == null) {
            if (log.isDebugEnabled()) {
                log.debug("No secret found for the secretType: "
                        + secretType + "for the tenant: " + getTenantDomain());
            }
            throw handleClientException(
                    ErrorMessages.ERROR_CODE_SECRETS_DOES_NOT_EXISTS, null);
        }
        if (log.isDebugEnabled()) {
            log.debug("All secrets of tenant: " + getTenantDomain()
                    + " are retrieved successfully.");
        }
        return new Secrets(secretList);
    }

    @Override
    public Secret getSecretByName(String secretType, String secretName)
            throws SecretManagementException {

        validateSecretManagerEnabled();
        validateSecretRetrieveRequestByName(secretType, secretName);
        Secret secret = this.getSecretDAO().getSecretByName(secretName, secretType, getTenantId());
        if (secret == null) {
            if (log.isDebugEnabled()) {
                log.debug("No secret found for the secretName: " + secretName);
            }
            throw handleClientException(ErrorMessages.ERROR_CODE_SECRET_DOES_NOT_EXISTS, secretName, null);
        }

        if (!secret.getSecretType().equals(secretType)) {
            if (log.isDebugEnabled()) {
                log.debug("Invalid secret type for the secret: " + secretName);
            }
            throw handleClientException(ErrorMessages.ERROR_CODE_INVALID_SECRET_TYPE_FOR_THE_SECRET_NAME, secretName);
        }

        if (log.isDebugEnabled()) {
            log.debug("Secret: " + secretName + " is retrieved successfully.");
        }
        return secret;
    }

    @Override
    public Secret getSecret(String secretType, String secretId)
            throws SecretManagementException {

        validateSecretManagerEnabled();
        if (StringUtils.isBlank(secretId)) {
            throw handleClientException(ErrorMessages.ERROR_CODE_INVALID_SECRET_ID, secretId);
        }
        validateSecretType(secretType);

        Secret secret = this.getSecretDAO().getSecretById(secretId, getTenantId());
        if (secret == null) {
            if (log.isDebugEnabled()) {
                log.debug("No secret found for the secretId: " + secretId);
            }
            throw handleClientException(ErrorMessages.ERROR_CODE_SECRET_ID_DOES_NOT_EXISTS, secretId);
        }

        if (!secret.getSecretType().equals(secretType)) {
            if (log.isDebugEnabled()) {
                log.debug("Invalid secret type for the secretId: " + secretId);
            }
            throw handleClientException(ErrorMessages.ERROR_CODE_INVALID_SECRET_TYPE_FOR_THE_SECRET_ID, secretId);
        }

        if (log.isDebugEnabled()) {
            log.debug("Secret: " + secret.getSecretId() + " is retrieved successfully.");
        }
        return secret;
    }

    @Override
    public void deleteSecret(String secretType, String secretId)
            throws SecretManagementException {

        validateSecretManagerEnabled();
        if (StringUtils.isBlank(secretId)) {
            throw handleClientException(ErrorMessages.ERROR_CODE_INVALID_SECRET_ID, secretId);
        }
        validateSecretType(secretType);
        this.getSecretDAO().deleteSecret(secretId, getTenantId());
        if (log.isDebugEnabled()) {
            log.debug("Secret id: " + secretId + " in tenant: " + getTenantDomain() + " deleted successfully.");
        }
    }

    public Secret updateSecret(String secretType, Secret secret) throws SecretManagementException {

        validateSecretManagerEnabled();
        validateSecretType(secretType);

        if (StringUtils.isEmpty(secret.getSecretId()) || StringUtils.isEmpty(secret.getSecretValue())) {
            throw handleClientException(ErrorMessages.ERROR_CODE_SECRET_UPDATE_REQUEST_INVALID, null);
        }

        if (secret.getSecretValue() != null) {
            validateSecretValue(secret.getSecretValue());
        }
        if (secret.getDescription() != null) {
            validateSecretDescription(secret.getDescription());
        }
        if (StringUtils.isEmpty(secret.getTenantDomain())) {
            secret.setTenantDomain(getTenantDomain());
        }
        this.getSecretDAO().updateSecret(secret, getTenantId());
        if (log.isDebugEnabled()) {
            log.debug(secret.getSecretId() + " secret updated successfully.");
        }
        return secret;
    }

    @Override
    public Secret updateSecretValue(String secretType, String secretId, String value)
            throws SecretManagementException {

        validateSecretManagerEnabled();
        validateSecretType(secretType);
        validateSecretValue(value);

        Secret secret, updatedSecret;
        secret = getSecret(secretType, secretId);
        try {
            updatedSecret = this.getSecretDAO().updateSecretValue(secret, encrypt(value));
        } catch (CryptoException e) {
            throw handleServerException(ErrorMessages.ERROR_CODE_UPDATE_SECRET, value, e);
        }
        if (log.isDebugEnabled()) {
            log.debug(secret.getSecretName() + " secret value updated successfully.");
        }
        return updatedSecret;
    }

    @Override
    public void addSecretType(String secretType) throws SecretManagementException {
        if (StringUtils.isEmpty(secretType)) {
            if (log.isDebugEnabled()) {
                log.debug("Invalid secret type name: " + secretType + ".");
            }
            throw handleClientException(ErrorMessages.ERROR_CODE_SECRET_TYPE_NAME_REQUIRED, null);
        }

        if (secretTypes.add(secretType)) {
            if (log.isDebugEnabled()) {
                log.debug("Secret type: " + secretType + " Added successfully.");
            }
        } else {
            log.error("Secret type: " + secretType + " already exists.");
        }
    }

    /**
     * Validate that secret type and secret name is non-empty.
     *
     * @param secretType Name of the secret type.
     * @param secretName The secret name.
     * @throws SecretManagementException If secret validation fails.
     */
    private void validateSecretRetrieveRequestByName(String secretType, String secretName)
            throws SecretManagementException {

        validateSecretType(secretType);

        if (StringUtils.isEmpty(secretName)) {
            if (log.isDebugEnabled()) {
                log.debug("Invalid secret identifier with secretName: " + secretName
                        + " and secretType: " + secretName + ".");
            }
            throw handleClientException(ErrorMessages.ERROR_CODE_SECRET_GET_REQUEST_INVALID, null);
        }
    }

    /**
     * Validate that secret type and secret name and value are non-empty.
     * Set tenant domain if they are not set to the secret object.
     *
     * @param secret The secret to be added.
     * @throws SecretManagementException If secret validation fails.
     */
    private void validateSecretCreateRequest(String secretType, Secret secret)
            throws SecretManagementException {

        validateSecretType(secretType);
        validateSecretName(secret.getSecretName());
        validateSecretValue(secret.getSecretValue());
        validateSecretDescription(secret.getDescription());

        if (StringUtils.isEmpty(secret.getSecretName()) ||
                StringUtils.isEmpty(secret.getSecretValue())) {
            throw handleClientException(ErrorMessages.ERROR_CODE_SECRET_ADD_REQUEST_INVALID, null);
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
            throw handleServerException(ErrorMessages.ERROR_CODE_GET_DAO, "secretDAOs");
        }
    }

    private void validateSecretManagerEnabled() throws SecretManagementServerException {

        if (!SecretManagerComponentDataHolder.getInstance().isSecretManagementEnabled()) {
            throw handleServerException(ErrorMessages.ERROR_CODE_SECRET_MANAGER_NOT_ENABLED);
        }
    }

    private int getTenantId() {

        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
    }

    private String getTenantDomain() {

        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
    }

    private String getEncryptedSecret(String secretValue, String name)
            throws SecretManagementServerException {

        try {
            return encrypt(secretValue);
        } catch (CryptoException e) {
            throw handleServerException(ErrorMessages.ERROR_CODE_ADD_SECRET, name, e);
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
                plainText.getBytes(Charsets.UTF_8));
    }

    /**
     * Validate the secret type.
     *
     * @param secretType The secret type name to be retrieved.
     * @throws SecretManagementException If secret validation fails.
     */
    private void validateSecretType(String secretType) throws SecretManagementException {

        if (StringUtils.isEmpty(secretType)) {
            if (log.isDebugEnabled()) {
                log.debug("Invalid secret type name: " + secretType + ".");
            }
            throw handleClientException(ErrorMessages.ERROR_CODE_SECRET_TYPE_NAME_REQUIRED, null);
        } else if (!secretTypes.contains(secretType)) {
            if (log.isDebugEnabled()) {
                log.debug("Invalid secret type: " + secretType + ".");
            }
            throw handleClientException(ErrorMessages.ERROR_CODE_SECRET_TYPE_DOES_NOT_EXISTS, secretType);
        }
    }

    /**
     * Validate secret name against a regex pattern.
     * @param secretName Name of the secret.
     * @throws SecretManagementClientException
     */
    private void validateSecretName(String secretName) throws SecretManagementClientException {
        if (!isSecretNameRegexValid(secretName)) {
            if (log.isDebugEnabled()) {
                log.debug("Secret name does not conform to "
                        + getSecretNameRegex() + " pattern");
            }
            throw handleClientException(ErrorMessages.ERROR_CODE_INVALID_SECRET_NAME, getSecretNameRegex());
        }
    }

    /**
     * Validate secret value against a regex pattern.
     * @param secretValue Value of the secret.
     * @throws SecretManagementClientException
     */
    private void validateSecretValue(String secretValue) throws SecretManagementClientException {
        if (!isSecretValueRegexValid(secretValue)) {
            if (log.isDebugEnabled()) {
                log.debug("Secret value does not conform to "
                        + getSecretValueRegex() + " pattern");
            }
            throw handleClientException(ErrorMessages.ERROR_CODE_INVALID_SECRET_VALUE, getSecretValueRegex());
        }
    }

    /**
     * Validate secret description against a regex pattern.
     * @param description Description of the secret.
     * @throws SecretManagementClientException
     */
    private void validateSecretDescription(String description)
            throws SecretManagementClientException {
        if (description != null && !isSecretDescriptionRegexValid(description)) {
            if (log.isDebugEnabled()) {
                log.debug("Secret description does not conform to "
                        + getSecretDescriptionRegex() + " pattern");
            }
            throw handleClientException(ErrorMessages.ERROR_CODE_INVALID_SECRET_DESCRIPTION,
                    getSecretDescriptionRegex());
        }
    }
}
