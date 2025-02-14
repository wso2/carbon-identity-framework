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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementException;
import org.wso2.carbon.identity.configuration.mgt.core.model.Attribute;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resource;
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
    public static final String PASSWORD = "password";
    public static final String KEYSTORE_LOCATION = "keystoreLocation";
    public static final String KEYSTORE_PASSWORD = "keystorePassword";
    public static final String TRUSTSTORE_LOCATION = "truststoreLocation";
    public static final String TRUSTSTORE_PASSWORD = "truststorePassword";
    public static final String RESOURCE_NOT_EXISTS_ERROR_CODE = "CONFIGM_00017";

    private static final Log LOG = LogFactory.getLog(DBBasedRemoteLoggingConfigDAO.class);
    private ConfigurationManager configurationManager;

    public DBBasedRemoteLoggingConfigDAO(ConfigurationManager configurationManager) {

        this.configurationManager = configurationManager;
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
        } catch (ConfigurationManagementException e) {
            throw new RemoteLoggingServerException("Error occurred while saving Remote Logging configuration.", e);
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
        }
    }

    @Override
    public void resetRemoteServerConfig(LogType logType) throws RemoteLoggingServerException {

        try {
            this.configurationManager.deleteResource(REMOTE_LOGGING_RESOURCE_TYPE, String.valueOf(logType));
        } catch (ConfigurationManagementException e) {
            throw new RemoteLoggingServerException("Error occurred while resetting Remote Logger configuration.", e);
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
        attributes.add(new Attribute(PASSWORD, remoteServerLoggerData.getPassword()));
        attributes.add(new Attribute(TRUSTSTORE_LOCATION, remoteServerLoggerData.getTruststoreLocation()));
        attributes.add(new Attribute(TRUSTSTORE_PASSWORD, remoteServerLoggerData.getTruststorePassword()));
        attributes.add(new Attribute(KEYSTORE_LOCATION, remoteServerLoggerData.getKeystoreLocation()));
        attributes.add(new Attribute(KEYSTORE_PASSWORD, remoteServerLoggerData.getKeystorePassword()));
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
                    case PASSWORD:
                        remoteServerLoggerData.setPassword(value);
                        break;
                    case TRUSTSTORE_LOCATION:
                        remoteServerLoggerData.setTruststoreLocation(value);
                        break;
                    case TRUSTSTORE_PASSWORD:
                        remoteServerLoggerData.setTruststorePassword(value);
                        break;
                    case KEYSTORE_LOCATION:
                        remoteServerLoggerData.setKeystoreLocation(value);
                        break;
                    case KEYSTORE_PASSWORD:
                        remoteServerLoggerData.setKeystorePassword(value);
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
}
