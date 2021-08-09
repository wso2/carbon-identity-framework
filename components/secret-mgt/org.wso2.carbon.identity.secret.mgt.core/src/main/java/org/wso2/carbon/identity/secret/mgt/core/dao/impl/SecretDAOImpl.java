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

package org.wso2.carbon.identity.secret.mgt.core.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.NamedTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.database.utils.jdbc.exceptions.TransactionException;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.LambdaExceptionUtils;
import org.wso2.carbon.identity.secret.mgt.core.constant.SQLConstants;
import org.wso2.carbon.identity.secret.mgt.core.dao.SecretDAO;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;
import org.wso2.carbon.identity.secret.mgt.core.model.Secret;

import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static java.time.ZoneOffset.UTC;
import static org.wso2.carbon.identity.secret.mgt.core.constant.SQLConstants.GET_SECRETS;
import static org.wso2.carbon.identity.secret.mgt.core.constant.SQLConstants.GET_SECRET_BY_ID;
import static org.wso2.carbon.identity.secret.mgt.core.constant.SQLConstants.GET_SECRET_BY_NAME;
import static org.wso2.carbon.identity.secret.mgt.core.constant.SQLConstants.GET_SECRET_CREATED_TIME_BY_NAME;
import static org.wso2.carbon.identity.secret.mgt.core.constant.SQLConstants.GET_SECRET_NAME_BY_ID;
import static org.wso2.carbon.identity.secret.mgt.core.constant.SQLConstants.INSERT_SECRET;
import static org.wso2.carbon.identity.secret.mgt.core.constant.SQLConstants.UPDATE_SECRET;
import static org.wso2.carbon.identity.secret.mgt.core.constant.SecretConstants.DB_SCHEMA_COLUMN_NAME_CREATED_TIME;
import static org.wso2.carbon.identity.secret.mgt.core.constant.SecretConstants.DB_SCHEMA_COLUMN_NAME_ID;
import static org.wso2.carbon.identity.secret.mgt.core.constant.SecretConstants.DB_SCHEMA_COLUMN_NAME_LAST_MODIFIED;
import static org.wso2.carbon.identity.secret.mgt.core.constant.SecretConstants.DB_SCHEMA_COLUMN_NAME_NAME;
import static org.wso2.carbon.identity.secret.mgt.core.constant.SecretConstants.DB_SCHEMA_COLUMN_NAME_TENANT_ID;
import static org.wso2.carbon.identity.secret.mgt.core.constant.SecretConstants.DB_SCHEMA_COLUMN_NAME_VALUE;
import static org.wso2.carbon.identity.secret.mgt.core.constant.SecretConstants.ErrorMessages.ERROR_CODE_ADD_SECRET;
import static org.wso2.carbon.identity.secret.mgt.core.constant.SecretConstants.ErrorMessages.ERROR_CODE_DELETE_SECRET;
import static org.wso2.carbon.identity.secret.mgt.core.constant.SecretConstants.ErrorMessages.ERROR_CODE_GET_SECRET;
import static org.wso2.carbon.identity.secret.mgt.core.constant.SecretConstants.ErrorMessages.ERROR_CODE_REPLACE_SECRET;
import static org.wso2.carbon.identity.secret.mgt.core.constant.SecretConstants.ErrorMessages.ERROR_CODE_SECRETS_DOES_NOT_EXISTS;
import static org.wso2.carbon.identity.secret.mgt.core.constant.SecretConstants.ErrorMessages.ERROR_CODE_SECRET_ALREADY_EXISTS;
import static org.wso2.carbon.identity.secret.mgt.core.util.SecretUtils.handleClientException;
import static org.wso2.carbon.identity.secret.mgt.core.util.SecretUtils.handleServerException;

/**
 * {@link SecretDAO} implementation.
 */
public class SecretDAOImpl implements SecretDAO {

    private static final Log log = LogFactory.getLog(SecretDAOImpl.class);
    private static final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(UTC));

    @Override
    public int getPriority() {

        return 1;
    }

    @Override
    public Secret getSecretByName(String name, int tenantId) throws SecretManagementException {

        NamedJdbcTemplate jdbcTemplate = getNewTemplate();
        List<SecretRawDataCollector> secretRawDataCollectors;
        try {
            String query = GET_SECRET_BY_NAME;
            secretRawDataCollectors = jdbcTemplate.executeQuery(query,
                    (resultSet, rowNumber) -> {
                        SecretRawDataCollector.SecretRawDataCollectorBuilder
                                secretRawDataCollectorBuilder =
                                new SecretRawDataCollector.SecretRawDataCollectorBuilder()
                                        .setSecretId(resultSet.getString(DB_SCHEMA_COLUMN_NAME_ID))
                                        .setTenantId(resultSet.getInt(DB_SCHEMA_COLUMN_NAME_TENANT_ID))
                                        .setSecretName(resultSet.getString(DB_SCHEMA_COLUMN_NAME_NAME))
                                        .setSecretValue(resultSet.getString(DB_SCHEMA_COLUMN_NAME_VALUE))
                                        .setLastModified(resultSet.getTimestamp(DB_SCHEMA_COLUMN_NAME_LAST_MODIFIED, calendar))
                                        .setCreatedTime(resultSet.getTimestamp(DB_SCHEMA_COLUMN_NAME_CREATED_TIME,
                                                calendar));
                        return secretRawDataCollectorBuilder.build();
                    }, preparedStatement -> {
                        preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_NAME, name);
                        preparedStatement.setInt(DB_SCHEMA_COLUMN_NAME_TENANT_ID, tenantId);

                    });

            return secretRawDataCollectors == null || secretRawDataCollectors.size() == 0 ?
                    null : buildSecretFromRawData(secretRawDataCollectors);
        } catch (DataAccessException | CryptoException e) {
            throw handleServerException(ERROR_CODE_GET_SECRET, name, e);
        }
    }

    @Override
    public Secret getSecretById(String secretId, int tenantId) throws SecretManagementException {

        NamedJdbcTemplate jdbcTemplate = getNewTemplate();
        List<SecretRawDataCollector> secretRawDataCollectors;
        try {
            String query = GET_SECRET_BY_ID;
            secretRawDataCollectors = jdbcTemplate.executeQuery(query, (resultSet, rowNumber) -> {
                        SecretRawDataCollector.SecretRawDataCollectorBuilder
                                secretRawDataCollectorBuilder =
                                new SecretRawDataCollector.SecretRawDataCollectorBuilder()
                                        .setSecretId(resultSet.getString(DB_SCHEMA_COLUMN_NAME_ID))
                                        .setTenantId(resultSet.getInt(DB_SCHEMA_COLUMN_NAME_TENANT_ID))
                                        .setSecretName(resultSet.getString(DB_SCHEMA_COLUMN_NAME_NAME))
                                        .setSecretValue(resultSet.getString(DB_SCHEMA_COLUMN_NAME_VALUE))
                                        .setLastModified(resultSet.getTimestamp(DB_SCHEMA_COLUMN_NAME_LAST_MODIFIED, calendar))
                                        .setCreatedTime(resultSet.getTimestamp(DB_SCHEMA_COLUMN_NAME_CREATED_TIME,
                                                calendar));
                        return secretRawDataCollectorBuilder.build();
                    },
                    preparedStatement -> {
                        preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ID, secretId);
                        preparedStatement.setInt(DB_SCHEMA_COLUMN_NAME_TENANT_ID, tenantId);
                    });

            return secretRawDataCollectors == null || secretRawDataCollectors.size() == 0 ?
                    null : buildSecretFromRawData(secretRawDataCollectors);
        } catch (DataAccessException | CryptoException e) {
            throw handleServerException(ERROR_CODE_GET_SECRET, "id = " + secretId, e);
        }
    }

    @Override
    public List getSecrets(int tenantId) throws SecretManagementException {

        NamedJdbcTemplate jdbcTemplate = getNewTemplate();
        try {
            return jdbcTemplate.executeQuery(GET_SECRETS,
                    (LambdaExceptionUtils.rethrowRowMapper((resultSet, rowNumber) -> {
                        String secretId = resultSet.getString(DB_SCHEMA_COLUMN_NAME_ID);
                        String secretName = resultSet.getString(DB_SCHEMA_COLUMN_NAME_NAME);
                        String secretLastModified = resultSet.getString(DB_SCHEMA_COLUMN_NAME_LAST_MODIFIED);
                        String secretCreatedTime = resultSet.getString(DB_SCHEMA_COLUMN_NAME_CREATED_TIME);
                        Secret secret = new Secret();
                        secret.setCreatedTime(secretCreatedTime);
                        secret.setSecretId(secretId);
                        secret.setSecretName(secretName);
                        secret.setLastModified(secretLastModified);
                        secret.setTenantDomain(IdentityTenantUtil.getTenantDomain(tenantId));
                        return secret;
                    })),
                    preparedStatement -> {
                        preparedStatement.setInt(DB_SCHEMA_COLUMN_NAME_TENANT_ID, tenantId);
                    });
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_SECRETS_DOES_NOT_EXISTS, e);
        }
    }

    @Override
    public void deleteSecretById(String secretId, int tenantId) throws SecretManagementException {

        NamedJdbcTemplate jdbcTemplate = getNewTemplate();
        try {
            jdbcTemplate.executeUpdate(SQLConstants.DELETE_SECRET_BY_ID, preparedStatement -> {
                preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ID, secretId);
                preparedStatement.setInt(DB_SCHEMA_COLUMN_NAME_TENANT_ID, tenantId);
            });
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_DELETE_SECRET, secretId, e);
        }
    }

    @Override
    public void deleteSecretByName(String name, int tenantId) throws SecretManagementException {

        NamedJdbcTemplate jdbcTemplate = getNewTemplate();
        try {
            jdbcTemplate.executeUpdate(SQLConstants.DELETE_SECRET, preparedStatement -> {
                preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_NAME, name);
                preparedStatement.setInt(DB_SCHEMA_COLUMN_NAME_TENANT_ID, tenantId);
            });
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_DELETE_SECRET, e);
        }
    }

    @Override
    public void addSecret(Secret secret) throws SecretManagementException {

        Timestamp currentTime = new java.sql.Timestamp(new Date().getTime());

        NamedJdbcTemplate jdbcTemplate = getNewTemplate();
        try {
            jdbcTemplate.withTransaction(template -> {

                // Insert secret metadata.
                template.executeInsert(INSERT_SECRET,
                        preparedStatement -> {
                            int initialParameterIndex = 1;
                            preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ID, secret.getSecretId());
                            preparedStatement.setInt(DB_SCHEMA_COLUMN_NAME_TENANT_ID,
                                    PrivilegedCarbonContext.getThreadLocalCarbonContext()
                                            .getTenantId());
                            preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_NAME, secret.getSecretName());
                            preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_VALUE, secret.getSecretValue());
                            preparedStatement.setTimeStamp(DB_SCHEMA_COLUMN_NAME_CREATED_TIME, currentTime, calendar);

                            preparedStatement.setTimeStamp(DB_SCHEMA_COLUMN_NAME_LAST_MODIFIED, currentTime, calendar);
                        }, secret, false);
                return null;
            });
            secret.setLastModified(currentTime.toInstant().toString());
            secret.setCreatedTime(currentTime.toInstant().toString());

        } catch (TransactionException e) {
            throw handleServerException(ERROR_CODE_ADD_SECRET, secret.getSecretName(), e);
        }
    }

    @Override
    public boolean isExistingSecret(String secretId, int tenantId) throws SecretManagementException {

        NamedJdbcTemplate jdbcTemplate = getNewTemplate();
        String secretName;
        try {
            secretName = jdbcTemplate.fetchSingleRecord(GET_SECRET_NAME_BY_ID, (resultSet, rowNumber) ->
                            resultSet.getString(DB_SCHEMA_COLUMN_NAME_NAME),
                    preparedStatement -> {
                        preparedStatement.setString(1, secretId);
                        preparedStatement.setInt(2, tenantId);
                    });
            return StringUtils.isNotEmpty(secretName);
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_GET_SECRET, "id = " + secretId, e);
        }
    }

    private Secret buildSecretFromRawData(List<SecretRawDataCollector> secretRawDataCollectors) throws CryptoException {

        Secret secret = new Secret();
        secretRawDataCollectors.forEach(secretRawDataCollector -> {
            if (secret.getSecretId() == null) {
                secret.setSecretId(secretRawDataCollector.getSecretId());
                secret.setSecretName(secretRawDataCollector.getSecretName());
                secret.setSecretValue(secretRawDataCollector.getValue());
                if (secretRawDataCollector.getCreatedTime() != null) {
                    secret.setCreatedTime(secretRawDataCollector.getCreatedTime().toInstant().toString());
                }
                secret.setLastModified(secretRawDataCollector.getLastModified().toInstant().toString());
                secret.setTenantDomain(
                        IdentityTenantUtil.getTenantDomain(secretRawDataCollector.getTenantId()));
            }
        });
        return secret;
    }

    private Timestamp getCreatedTimeInResponse(Secret secret) throws TransactionException {

        NamedJdbcTemplate jdbcTemplate = getNewTemplate();
        return jdbcTemplate.withTransaction(template ->
                template.fetchSingleRecord(GET_SECRET_CREATED_TIME_BY_NAME,
                        (resultSet, rowNumber) -> resultSet.getTimestamp(DB_SCHEMA_COLUMN_NAME_CREATED_TIME, calendar),
                        preparedStatement -> {
                            int initialParameterIndex = 1;
                            preparedStatement.setString(initialParameterIndex, secret.getSecretName());
                            preparedStatement.setInt(++initialParameterIndex,
                                    PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
                        }
                )
        );
    }

    @Override
    public void replaceSecret(Secret secret) throws SecretManagementException {

        Timestamp currentTime = new java.sql.Timestamp(new Date().getTime());

        NamedJdbcTemplate jdbcTemplate = getNewTemplate();
        try {
            Timestamp createdTime = jdbcTemplate.withTransaction(template -> {

                updateSecretMetadata(template, secret, currentTime);
                return getCreatedTimeInResponse(secret);

            });
            secret.setLastModified(currentTime.toInstant().toString());
            if (createdTime != null) {
                secret.setCreatedTime(createdTime.toInstant().toString());
            }
        } catch (TransactionException e) {
            if (e.getCause() instanceof SecretManagementException) {
                throw (SecretManagementException) e.getCause();
            }
            throw handleServerException(ERROR_CODE_REPLACE_SECRET, secret.getSecretName(), e);
        }
    }

    private void updateSecretMetadata(NamedTemplate<Timestamp> template, Secret secret, Timestamp currentTime)
            throws SecretManagementException, DataAccessException {

        try {
            template.executeUpdate(UPDATE_SECRET, preparedStatement -> {
                preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_NAME, secret.getSecretName());
                preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_VALUE, secret.getSecretValue());
                preparedStatement.setTimeStamp(DB_SCHEMA_COLUMN_NAME_LAST_MODIFIED, currentTime, calendar);
                preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ID, secret.getSecretId());
            });
        } catch (DataAccessException e) {
            if (e.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw handleClientException(ERROR_CODE_SECRET_ALREADY_EXISTS, secret.getSecretName(), e);
            } else {
                throw e;
            }
        }
    }

    /**
     * Get a new Jdbc Template.
     *
     * @return a new Jdbc Template.
     */
    private NamedJdbcTemplate getNewTemplate() {

        return new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
    }
}
