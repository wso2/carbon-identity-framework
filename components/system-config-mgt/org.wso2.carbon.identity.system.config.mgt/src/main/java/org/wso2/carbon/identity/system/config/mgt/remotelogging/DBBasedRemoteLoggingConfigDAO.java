/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.system.config.mgt.remotelogging;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementException;
import org.wso2.carbon.identity.configuration.mgt.core.model.Attribute;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resource;
import org.wso2.carbon.identity.secret.mgt.core.SecretManager;
import org.wso2.carbon.identity.secret.mgt.core.SecretResolveManager;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;
import org.wso2.carbon.identity.secret.mgt.core.model.ResolvedSecret;
import org.wso2.carbon.identity.secret.mgt.core.model.Secret;
import org.wso2.carbon.logging.service.LoggingConstants.LogType;
import org.wso2.carbon.logging.service.RemoteLoggingServerException;
import org.wso2.carbon.logging.service.dao.RemoteLoggingConfigDAO;
import org.wso2.carbon.logging.service.data.RemoteServerLoggerData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This class is used to update the remote server logging configurations in the database.
 */
public class DBBasedRemoteLoggingConfigDAO implements RemoteLoggingConfigDAO {

    public static final String REMOTE_LOGGING_RESOURCE_TYPE = "REMOTE_LOGGING_CONFIG";
    public static final String URL = "url";
    public static final String CONNECT_TIMEOUT_MILLIS = "connectTimeoutMillis";
    public static final String VERIFY_HOSTNAME = "verifyHostname";
    public static final String LOG_TYPE = "logType";
    public static final String USERNAME = "username";
    public static final String KEYSTORE_LOCATION = "keystoreLocation";
    public static final String TRUSTSTORE_LOCATION = "truststoreLocation";
    public static final String RESOURCE_NOT_EXISTS_ERROR_CODE = "CONFIGM_00017";

    public static final String IDN_SECRET_TYPE_REMOTE_LOGGING_SECRETS = "REMOTE_LOGGING_SECRETS";
    public static final String SECRET_NAME_PASSWORD = "PASSWORD";
    public static final String SECRET_NAME_KEYSTORE_PASSWORD = "KEYSTORE_PASSWORD";
    public static final String SECRET_NAME_TRUSTSTORE_PASSWORD = "TRUSTSTORE_PASSWORD";

    private static final Log LOG = LogFactory.getLog(DBBasedRemoteLoggingConfigDAO.class);
    private final ConfigurationManager configurationManager;
    private final SecretManager secretManager;
    private final SecretResolveManager secretResolveManager;

    public DBBasedRemoteLoggingConfigDAO(ConfigurationManager configurationManager, SecretManager secretManager,
                                         SecretResolveManager secretResolveManager) {

        this.configurationManager = configurationManager;
        this.secretManager = secretManager;
        this.secretResolveManager = secretResolveManager;
    }

    @Override
    public void saveRemoteServerConfig(RemoteServerLoggerData data, LogType logType)
            throws RemoteLoggingServerException {

        Resource resource = buildResourceFromRemoteServerLoggerData(data, String.valueOf(logType));
        try {
            if (isRemoteServerConfigResourceExists(REMOTE_LOGGING_RESOURCE_TYPE, String.valueOf(logType))) {
                this.configurationManager.replaceResource(REMOTE_LOGGING_RESOURCE_TYPE, resource);
            } else {
                this.configurationManager.addResource(REMOTE_LOGGING_RESOURCE_TYPE, resource);
            }
            storeRemoteLoggingSecrets(String.valueOf(logType), data);
        } catch (ConfigurationManagementException e) {
            throw new RemoteLoggingServerException("Error occurred while saving Remote Logging configuration.", e);
        } catch (SecretManagementException e) {
            throw new RemoteLoggingServerException("Error occurred while storing secrets for Remote Logging " +
                    "configuration.", e);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Remote Logging configuration are saved successfully in configuration-store");
        }
    }

    @Override
    public Optional<RemoteServerLoggerData> getRemoteServerConfig(LogType logType) throws RemoteLoggingServerException {

        try {
            Resource resource = this.configurationManager.getResource(REMOTE_LOGGING_RESOURCE_TYPE,
                    String.valueOf(logType));
            if (resource == null) {
                return Optional.empty();
            }
            RemoteServerLoggerData remoteServerLoggerData = buildRemoteServerLoggerDataFromResource(resource);
            resolveRemoteLoggerSecrets(String.valueOf(logType), remoteServerLoggerData);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Remote Logger configuration loaded successfully from configuration-store");
            }
            return Optional.of(remoteServerLoggerData);
        } catch (ConfigurationManagementException e) {
            if (RESOURCE_NOT_EXISTS_ERROR_CODE.equals(e.getErrorCode())) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Can not find a remote logger configurations", e);
                }
                return Optional.empty();
            }
            throw new RemoteLoggingServerException("Error occurred while loading Remote Logger configuration.", e);
        } catch (SecretManagementException e) {
            throw new RemoteLoggingServerException("Error occurred while loading Remote Logger secrets.", e);
        }
    }

    @Override
    public void resetRemoteServerConfig(LogType logType) throws RemoteLoggingServerException {

        try {
            this.configurationManager.deleteResource(REMOTE_LOGGING_RESOURCE_TYPE, String.valueOf(logType));
            deleteRemoteLoggingSecrets(String.valueOf(logType));
        } catch (ConfigurationManagementException e) {
            throw new RemoteLoggingServerException("Error occurred while resetting Remote Logger configuration.", e);
        } catch (SecretManagementException e) {
            throw new RemoteLoggingServerException("Error occurred while deleting Remote Logger secrets.", e);
        }
    }

    /**
     * Check whether the Remote Server Config resource exists for the current tenant.
     *
     * @param resourceType Remote Server Config resource type.
     * @param resourceName Remote Server Config resource name.
     * @return Return true if the resource already exists. If not return false.
     */
    private boolean isRemoteServerConfigResourceExists(String resourceType, String resourceName)
            throws RemoteLoggingServerException {

        Resource resource;
        try {
            resource = this.configurationManager.getResource(resourceType, resourceName);
        } catch (ConfigurationManagementException e) {
            if (RESOURCE_NOT_EXISTS_ERROR_CODE.equals(e.getErrorCode())) {
                return false;
            }
            throw new RemoteLoggingServerException("Error occurred while checking the existence of the Remote Server " +
                    "Config resource.", e);
        }
        return resource != null;
    }

    private Resource buildResourceFromRemoteServerLoggerData(RemoteServerLoggerData remoteServerLoggerData,
                                                             String logType) {

        Resource resource = new Resource(logType, REMOTE_LOGGING_RESOURCE_TYPE);
        List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute(URL, remoteServerLoggerData.getUrl()));
        attributes.add(new Attribute(CONNECT_TIMEOUT_MILLIS, remoteServerLoggerData.getConnectTimeoutMillis()));
        attributes.add(new Attribute(LOG_TYPE, remoteServerLoggerData.getLogType()));
        attributes.add(new Attribute(USERNAME, remoteServerLoggerData.getUsername()));
        attributes.add(new Attribute(TRUSTSTORE_LOCATION, remoteServerLoggerData.getTruststoreLocation()));
        attributes.add(new Attribute(KEYSTORE_LOCATION, remoteServerLoggerData.getKeystoreLocation()));
        attributes.add(new Attribute(VERIFY_HOSTNAME, String.valueOf(remoteServerLoggerData.isVerifyHostname())));
        resource.setAttributes(attributes);
        return resource;
    }

    private RemoteServerLoggerData buildRemoteServerLoggerDataFromResource(Resource resource) {

        RemoteServerLoggerData remoteServerLoggerData = new RemoteServerLoggerData();

        List<Attribute> attributes = resource.getAttributes();
        if (attributes != null) {
            for (Attribute attribute : attributes) {
                String key = attribute.getKey();
                String value = attribute.getValue();

                switch (key) {
                    case URL:
                        remoteServerLoggerData.setUrl(value);
                        break;
                    case CONNECT_TIMEOUT_MILLIS:
                        remoteServerLoggerData.setConnectTimeoutMillis(value);
                        break;
                    case LOG_TYPE:
                        remoteServerLoggerData.setLogType(value);
                        break;
                    case USERNAME:
                        remoteServerLoggerData.setUsername(value);
                        break;
                    case TRUSTSTORE_LOCATION:
                        remoteServerLoggerData.setTruststoreLocation(value);
                        break;
                    case KEYSTORE_LOCATION:
                        remoteServerLoggerData.setKeystoreLocation(value);
                        break;
                    case VERIFY_HOSTNAME:
                        remoteServerLoggerData.setVerifyHostname(Boolean.parseBoolean(value));
                        break;
                    default:
                        // Ignore unknown attributes.
                        break;
                }
            }
        }
        return remoteServerLoggerData;
    }

    /**
     * Store the credentials as secrets in the secret manager.
     *
     * @param logType                Log Type.
     * @param remoteServerLoggerData Remote Server Logger Data.
     * @throws SecretManagementException If an error occurs while storing the secrets.
     */
    private void storeRemoteLoggingSecrets(String logType, RemoteServerLoggerData remoteServerLoggerData)
            throws SecretManagementException {

        storeSecretProperty(logType, SECRET_NAME_PASSWORD, remoteServerLoggerData.getPassword());
        storeSecretProperty(logType, SECRET_NAME_KEYSTORE_PASSWORD, remoteServerLoggerData.getKeystorePassword());
        storeSecretProperty(logType, SECRET_NAME_TRUSTSTORE_PASSWORD, remoteServerLoggerData.getTruststorePassword());
    }

    /**
     * Resolve the remote logger secrets.
     *
     * @param remoteServerLoggerData Remote Server Logger Data.
     * @throws SecretManagementException If an error occurs while resolving the secrets.
     */
    private void resolveRemoteLoggerSecrets(String logType, RemoteServerLoggerData remoteServerLoggerData)
            throws SecretManagementException {

        String password = resolveSecretProperty(logType, SECRET_NAME_PASSWORD);
        if (StringUtils.isNoneBlank(password)) {
            remoteServerLoggerData.setPassword(password);
        }

        String keystorePassword = resolveSecretProperty(logType, SECRET_NAME_KEYSTORE_PASSWORD);
        if (StringUtils.isNoneBlank(keystorePassword)) {
            remoteServerLoggerData.setKeystorePassword(keystorePassword);
        }

        String truststorePassword = resolveSecretProperty(logType, SECRET_NAME_TRUSTSTORE_PASSWORD);
        if (StringUtils.isNoneBlank(truststorePassword)) {
            remoteServerLoggerData.setTruststorePassword(truststorePassword);
        }
    }

    /**
     * Delete the Remote Logging secrets.
     *
     * @throws SecretManagementException If an error occurs while deleting the secrets.
     */
    private void deleteRemoteLoggingSecrets(String logType) throws SecretManagementException {

        deleteSecretProperty(logType, SECRET_NAME_PASSWORD);
        deleteSecretProperty(logType, SECRET_NAME_KEYSTORE_PASSWORD);
        deleteSecretProperty(logType, SECRET_NAME_TRUSTSTORE_PASSWORD);
    }

    /**
     * Add new Secret for Remote Logging secret type.
     *
     * @param logType        Log Type.
     * @param secretProperty Name of the Secret Property.
     * @param secretValue    Secret Value.
     * @throws SecretManagementException If an error occurs while adding the secret.
     */
    private void storeSecretProperty(String logType, String secretProperty, String secretValue)
            throws SecretManagementException {

        if (StringUtils.isEmpty(secretValue)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Secret value for '" + secretProperty + "' is empty. Deleting the secret property.");
            }
            deleteSecretProperty(logType, secretProperty);
            return;
        }
        String secretName = buildSecretName(logType, secretProperty);
        if (secretManager.isSecretExist(IDN_SECRET_TYPE_REMOTE_LOGGING_SECRETS, secretName)) {
            updateExistingSecretProperty(secretName, secretValue);
        } else {
            addNewSecretProperty(secretName, secretValue);
        }
    }

    /**
     * Add new Secret for Remote Logging secret type.
     *
     * @param secretName  Name of the secret.
     * @param secretValue Secret Value.
     * @throws SecretManagementException If an error occurs while adding the secret.
     */
    private void addNewSecretProperty(String secretName, String secretValue) throws SecretManagementException {

        Secret secret = new Secret();
        secret.setSecretName(secretName);
        secret.setSecretValue(secretValue);
        secretManager.addSecret(IDN_SECRET_TYPE_REMOTE_LOGGING_SECRETS, secret);
    }

    /**
     * Update an existing secret of Remote Logging secret type.
     *
     * @param secretName  Name of the secret.
     * @param secretValue Secret property.
     * @throws SecretManagementException If an error occurs while adding the secret.
     */
    private void updateExistingSecretProperty(String secretName, String secretValue)
            throws SecretManagementException {

        ResolvedSecret resolvedSecret =
                secretResolveManager.getResolvedSecret(IDN_SECRET_TYPE_REMOTE_LOGGING_SECRETS, secretName);
        if (!resolvedSecret.getResolvedSecretValue().equals(secretValue)) {
            secretManager.updateSecretValue(IDN_SECRET_TYPE_REMOTE_LOGGING_SECRETS, secretName, secretValue);
        }
    }

    /**
     * Resolve the secret.
     *
     * @param logType        Log Type.
     * @param secretProperty Name of the Secret Property.
     * @return Resolved secret value.
     * @throws SecretManagementException If an error occurs while resolving the secret.
     */
    private String resolveSecretProperty(String logType, String secretProperty) throws SecretManagementException {

        String secretName = buildSecretName(logType, secretProperty);
        if (secretManager.isSecretExist(IDN_SECRET_TYPE_REMOTE_LOGGING_SECRETS, secretName)) {
            ResolvedSecret resolvedSecret =
                    secretResolveManager.getResolvedSecret(IDN_SECRET_TYPE_REMOTE_LOGGING_SECRETS, secretName);
            return resolvedSecret.getResolvedSecretValue();
        }
        return null;
    }

    /**
     * Delete the secret.
     *
     * @param logType        Log Type.
     * @param secretProperty Name of the secret property.
     * @throws SecretManagementException If an error occurs while deleting the secret.
     */
    private void deleteSecretProperty(String logType, String secretProperty) throws SecretManagementException {

        String secretName = buildSecretName(logType, secretProperty);
        if (secretManager.isSecretExist(IDN_SECRET_TYPE_REMOTE_LOGGING_SECRETS, secretName)) {
            secretManager.deleteSecret(IDN_SECRET_TYPE_REMOTE_LOGGING_SECRETS, secretName);
        }
    }

    /**
     * Build the secret name.
     *
     * @param logType        Log Type.
     * @param secretProperty Secret Property.
     * @return Secret Name.
     */
    private String buildSecretName(String logType, String secretProperty) {

        return StringUtils.upperCase(logType + ":" + secretProperty);
    }
}
