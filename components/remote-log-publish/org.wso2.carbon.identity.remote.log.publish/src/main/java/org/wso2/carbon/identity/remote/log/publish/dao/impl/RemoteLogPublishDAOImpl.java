package org.wso2.carbon.identity.remote.log.publish.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.core.util.JdbcUtils;
import org.wso2.carbon.identity.remote.log.publish.constants.RemoteLogPublishConstants;
import org.wso2.carbon.identity.remote.log.publish.dao.RemoteLogPublishDAO;
import org.wso2.carbon.identity.remote.log.publish.exception.RemoteLogPublishServerException;
import org.wso2.carbon.identity.remote.log.publish.model.RemoteLogPublishConfig;
import org.wso2.carbon.identity.remote.log.publish.util.RemoteLogPublishUtil;

import static org.wso2.carbon.identity.remote.log.publish.constants.SQLConstants.Column.CONNECTION_TIMEOUT;
import static org.wso2.carbon.identity.remote.log.publish.constants.SQLConstants.Column.KEYSTORE_LOCATION;
import static org.wso2.carbon.identity.remote.log.publish.constants.SQLConstants.Column.KEYSTORE_PASSWORD;
import static org.wso2.carbon.identity.remote.log.publish.constants.SQLConstants.Column.LOG_TYPE;
import static org.wso2.carbon.identity.remote.log.publish.constants.SQLConstants.Column.PASSWORD;
import static org.wso2.carbon.identity.remote.log.publish.constants.SQLConstants.Column.TENANT_DOMAIN;
import static org.wso2.carbon.identity.remote.log.publish.constants.SQLConstants.Column.TRUSTSTORE_LOCATION;
import static org.wso2.carbon.identity.remote.log.publish.constants.SQLConstants.Column.TRUSTSTORE_PASSWORD;
import static org.wso2.carbon.identity.remote.log.publish.constants.SQLConstants.Column.URL;
import static org.wso2.carbon.identity.remote.log.publish.constants.SQLConstants.Column.USERNAME;
import static org.wso2.carbon.identity.remote.log.publish.constants.SQLConstants.Column.UUID;
import static org.wso2.carbon.identity.remote.log.publish.constants.SQLConstants.Column.VERIFY_HOSTNAME;
import static org.wso2.carbon.identity.remote.log.publish.constants.SQLConstants.Query.DELETE_ALL_REMOTE_LOG_PUBLISH_CONFIGS_SQL;
import static org.wso2.carbon.identity.remote.log.publish.constants.SQLConstants.Query.DELETE_REMOTE_LOG_PUBLISH_CONFIGS_SQL;
import static org.wso2.carbon.identity.remote.log.publish.constants.SQLConstants.Query.GET_REMOTE_LOG_PUBLISH_CONFIG_SQL;
import static org.wso2.carbon.identity.remote.log.publish.constants.SQLConstants.Query.INSERT_REMOTE_LOG_PUBLISH_CONFIG_SQL;
import static org.wso2.carbon.identity.remote.log.publish.constants.SQLConstants.Query.LIST_REMOTE_LOG_PUBLISH_CONFIGS_SQL;
import static org.wso2.carbon.identity.remote.log.publish.constants.SQLConstants.Query.UPDATE_REMOTE_LOG_PUBLISH_CONFIG_SQL;

/**
 * Remote Log publish config DAO Impl.
 */
public class RemoteLogPublishDAOImpl implements RemoteLogPublishDAO {

    @Override
    public void addRemoteLogPublishConfig(RemoteLogPublishConfig config, String tenantDomain)
            throws RemoteLogPublishServerException {

        NamedJdbcTemplate namedJdbcTemplate = JdbcUtils.getNewNamedJdbcTemplate();
        try {
            namedJdbcTemplate.executeInsert(INSERT_REMOTE_LOG_PUBLISH_CONFIG_SQL, (preparedStatement -> {
                preparedStatement.setString(UUID, config.getUuid());
                preparedStatement.setString(TENANT_DOMAIN, tenantDomain);
                preparedStatement.setString(URL, config.getUrl());
                preparedStatement.setInt(CONNECTION_TIMEOUT, config.getConnectTimeoutMillis());
                preparedStatement.setBoolean(VERIFY_HOSTNAME, config.isVerifyHostname());
                preparedStatement.setString(LOG_TYPE, config.getLogType());
                preparedStatement.setString(USERNAME, config.getUsername());
                preparedStatement.setString(PASSWORD, config.getPassword());
                preparedStatement.setString(KEYSTORE_LOCATION, config.getKeystoreLocation());
                preparedStatement.setString(KEYSTORE_PASSWORD, config.getKeystorePassword());
                preparedStatement.setString(TRUSTSTORE_LOCATION, config.getTruststoreLocation());
                preparedStatement.setString(TRUSTSTORE_PASSWORD, config.getTruststorePassword());
            }), config, false);
        } catch (DataAccessException e) {
            throw RemoteLogPublishUtil.handleServerException(
                    RemoteLogPublishConstants.ErrorMessages.ERROR_WHILE_ADDING_CONFIG, e);
        }
    }

    @Override
    public void updateRemoteLogPublishConfig(RemoteLogPublishConfig config, String tenantDomain)
            throws RemoteLogPublishServerException {

        NamedJdbcTemplate namedJdbcTemplate = JdbcUtils.getNewNamedJdbcTemplate();
        try {
            namedJdbcTemplate.executeUpdate(UPDATE_REMOTE_LOG_PUBLISH_CONFIG_SQL, (preparedStatement -> {
                preparedStatement.setString(URL, config.getUrl());
                preparedStatement.setInt(CONNECTION_TIMEOUT, config.getConnectTimeoutMillis());
                preparedStatement.setBoolean(VERIFY_HOSTNAME, config.isVerifyHostname());
                preparedStatement.setString(USERNAME, config.getUsername());
                preparedStatement.setString(PASSWORD, config.getPassword());
                preparedStatement.setString(KEYSTORE_LOCATION, config.getKeystoreLocation());
                preparedStatement.setString(KEYSTORE_PASSWORD, config.getKeystorePassword());
                preparedStatement.setString(TRUSTSTORE_LOCATION, config.getTruststoreLocation());
                preparedStatement.setString(TRUSTSTORE_PASSWORD, config.getTruststorePassword());
                preparedStatement.setString(UUID, config.getUuid());
                preparedStatement.setString(TENANT_DOMAIN, tenantDomain);
                preparedStatement.setString(LOG_TYPE, config.getLogType());
            }));
        } catch (DataAccessException e) {
            throw RemoteLogPublishUtil.handleServerException(
                    RemoteLogPublishConstants.ErrorMessages.ERROR_WHILE_UPDATING_CONFIG, e);
        }
    }

    @Override
    public RemoteLogPublishConfig getRemoteLogPublishConfig(String logType, String tenantDomain)
            throws RemoteLogPublishServerException {

        NamedJdbcTemplate namedJdbcTemplate = JdbcUtils.getNewNamedJdbcTemplate();
        RemoteLogPublishConfig remoteLogPublishConfig;

        try {
            remoteLogPublishConfig = namedJdbcTemplate.fetchSingleRecord(GET_REMOTE_LOG_PUBLISH_CONFIG_SQL,
                    (resultSet, rowNumber) -> {
                        RemoteLogPublishConfig remoteLogPublishConfigResult = new RemoteLogPublishConfig();
                        remoteLogPublishConfigResult.setUuid(resultSet.getString(UUID));
                        remoteLogPublishConfigResult.setUrl(resultSet.getString(URL));
                        remoteLogPublishConfigResult.setConnectTimeoutMillis(resultSet.getInt(CONNECTION_TIMEOUT));
                        remoteLogPublishConfigResult.setLogType(resultSet.getString(LOG_TYPE));
                        remoteLogPublishConfigResult.setVerifyHostname(resultSet.getBoolean(VERIFY_HOSTNAME));
                        remoteLogPublishConfigResult.setUsername(resultSet.getString(USERNAME));
                        remoteLogPublishConfigResult.setPassword(resultSet.getString(PASSWORD));
                        remoteLogPublishConfigResult.setKeystoreLocation(resultSet.getString(KEYSTORE_LOCATION));
                        remoteLogPublishConfigResult.setKeystorePassword(resultSet.getString(KEYSTORE_PASSWORD));
                        remoteLogPublishConfigResult.setTruststoreLocation(resultSet.getString(TRUSTSTORE_LOCATION));
                        remoteLogPublishConfigResult.setTruststorePassword(resultSet.getString(TRUSTSTORE_PASSWORD));
                        return remoteLogPublishConfigResult;
                    },
                    preparedStatement -> {
                        preparedStatement.setString(TENANT_DOMAIN, tenantDomain);
                        preparedStatement.setString(LOG_TYPE, logType);
                    });
        } catch (DataAccessException e) {
            throw RemoteLogPublishUtil.handleServerException(
                    RemoteLogPublishConstants.ErrorMessages.ERROR_WHILE_RETRIEVING_CONFIG, e);
        }

        return remoteLogPublishConfig;
    }

    @Override
    public List<RemoteLogPublishConfig> getAllRemoteLogPublishConfigs(String tenantDomain)
            throws RemoteLogPublishServerException {

        NamedJdbcTemplate namedJdbcTemplate = JdbcUtils.getNewNamedJdbcTemplate();
        List<RemoteLogPublishConfig> remoteLogPublishConfigList;

        try {
            remoteLogPublishConfigList = namedJdbcTemplate.executeQuery(LIST_REMOTE_LOG_PUBLISH_CONFIGS_SQL,
                    (resultSet, rowNumber) -> {
                        RemoteLogPublishConfig remoteLogPublishConfig = new RemoteLogPublishConfig();
                        remoteLogPublishConfig.setUuid(resultSet.getString(UUID));
                        remoteLogPublishConfig.setUrl(resultSet.getString(URL));
                        remoteLogPublishConfig.setConnectTimeoutMillis(resultSet.getInt(CONNECTION_TIMEOUT));
                        remoteLogPublishConfig.setLogType(resultSet.getString(LOG_TYPE));
                        remoteLogPublishConfig.setVerifyHostname(resultSet.getBoolean(VERIFY_HOSTNAME));
                        remoteLogPublishConfig.setUsername(resultSet.getString(USERNAME));
                        remoteLogPublishConfig.setPassword(resultSet.getString(PASSWORD));
                        remoteLogPublishConfig.setKeystoreLocation(resultSet.getString(KEYSTORE_LOCATION));
                        remoteLogPublishConfig.setKeystorePassword(resultSet.getString(KEYSTORE_PASSWORD));
                        remoteLogPublishConfig.setTruststoreLocation(resultSet.getString(TRUSTSTORE_LOCATION));
                        remoteLogPublishConfig.setTruststorePassword(resultSet.getString(TRUSTSTORE_PASSWORD));
                        return remoteLogPublishConfig;
                    },
                    preparedStatement -> preparedStatement.setString(TENANT_DOMAIN, tenantDomain));
        } catch (DataAccessException e) {
            throw RemoteLogPublishUtil.handleServerException(
                    RemoteLogPublishConstants.ErrorMessages.ERROR_WHILE_RETRIEVING_CONFIG, e);
        }

        return remoteLogPublishConfigList;
    }

    @Override
    public void deleteRemoteLogPublishConfig(String logType, String tenantDomain)
            throws RemoteLogPublishServerException {

        NamedJdbcTemplate namedJdbcTemplate = JdbcUtils.getNewNamedJdbcTemplate();

        try {
            namedJdbcTemplate.executeUpdate(DELETE_REMOTE_LOG_PUBLISH_CONFIGS_SQL,
                    preparedStatement -> {
                        preparedStatement.setString(TENANT_DOMAIN, tenantDomain);
                        preparedStatement.setString(LOG_TYPE, logType);
                    });
        } catch (DataAccessException e) {
            throw RemoteLogPublishUtil.handleServerException(
                    RemoteLogPublishConstants.ErrorMessages.ERROR_WHILE_DELETING_CONFIG, e);
        }
    }

    @Override
    public void deleteAllRemoteLogPublishConfigs(String tenantDomain)
            throws RemoteLogPublishServerException {

        NamedJdbcTemplate namedJdbcTemplate = JdbcUtils.getNewNamedJdbcTemplate();

        try {
            namedJdbcTemplate.executeUpdate(DELETE_ALL_REMOTE_LOG_PUBLISH_CONFIGS_SQL,
                    preparedStatement -> preparedStatement.setString(TENANT_DOMAIN, tenantDomain));
        } catch (DataAccessException e) {
            throw RemoteLogPublishUtil.handleServerException(
                    RemoteLogPublishConstants.ErrorMessages.ERROR_WHILE_DELETING_CONFIG, e);
        }
    }
}
