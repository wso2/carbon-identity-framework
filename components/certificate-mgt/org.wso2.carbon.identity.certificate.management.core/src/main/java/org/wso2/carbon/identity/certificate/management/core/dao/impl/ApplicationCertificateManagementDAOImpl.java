/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.certificate.management.core.dao.impl;

import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.database.utils.jdbc.exceptions.TransactionException;
import org.wso2.carbon.identity.certificate.management.core.constant.ApplicationCertificateMgtSQLQueries;
import org.wso2.carbon.identity.certificate.management.core.constant.CertificateMgtErrors;
import org.wso2.carbon.identity.certificate.management.core.constant.CertificateMgtSQLConstants;
import org.wso2.carbon.identity.certificate.management.core.dao.ApplicationCertificateManagementDAO;
import org.wso2.carbon.identity.certificate.management.core.exception.CertificateMgtException;
import org.wso2.carbon.identity.certificate.management.core.model.Certificate;
import org.wso2.carbon.identity.certificate.management.core.util.CertificateMgtUtil;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is the implementation of the Application Certificate Management DAO.
 * @deprecated Use {@link CertificateManagementDAOImpl} instead.
 */
public class ApplicationCertificateManagementDAOImpl implements ApplicationCertificateManagementDAO {

    @Override
    @Deprecated
    public int addCertificate(String certificateId, Certificate certificate, int tenantId)
            throws CertificateMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            InputStream certByteStream = CertificateMgtUtil.getCertificateByteStream(certificate.getCertificate());
            int certificateLength = certByteStream.available();
            return jdbcTemplate.withTransaction(template ->
                    template.executeInsert(CertificateMgtSQLConstants.Query.ADD_CERTIFICATE,
                            preparedStatement -> {
                                int index = 1;
                                preparedStatement.setString(index++, certificateId);
                                preparedStatement.setString(index++, certificate.getName());
                                preparedStatement.setBlob(index++, certByteStream, certificateLength);
                                preparedStatement.setInt(index, tenantId);
                            }, certificate, true));
        } catch (TransactionException | IOException e) {
            throw CertificateMgtUtil.raiseServerException(CertificateMgtErrors.ERROR_WHILE_ADDING_CERTIFICATE, e,
                    certificate.getName());
        }
    }

    @Override
    @Deprecated
    public Certificate getCertificate(int certificateId, int tenantId) throws CertificateMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        Map<String, Object> certificateMap = new HashMap<>();
        try {
            jdbcTemplate.fetchSingleRecord(ApplicationCertificateMgtSQLQueries.GET_CERTIFICATE_BY_ID,
                    (resultSet, rowNumber) -> {
                        certificateMap.put(CertificateMgtSQLConstants.Column.NAME,
                                resultSet.getString(CertificateMgtSQLConstants.Column.NAME));
                        certificateMap.put(CertificateMgtSQLConstants.Column.CERTIFICATE_IN_PEM,
                                resultSet.getBinaryStream(CertificateMgtSQLConstants.Column.CERTIFICATE_IN_PEM));
                        return null;
                    }, preparedStatement -> {
                        preparedStatement.setInt(1, certificateId);
                        preparedStatement.setInt(2, tenantId);
                    }
                                          );
            Certificate certificate = null;
            if (!certificateMap.isEmpty()) {
                certificate = new Certificate.Builder()
                        .id(String.valueOf(certificateId))
                        .name((String) certificateMap.get(CertificateMgtSQLConstants.Column.NAME))
                        .certificate(CertificateMgtUtil.getStringValueFromBlob((InputStream)
                                certificateMap.get(CertificateMgtSQLConstants.Column.CERTIFICATE_IN_PEM)))
                        .build();
            }

            return certificate;
        } catch (DataAccessException | IOException e) {
            throw CertificateMgtUtil.raiseServerException(CertificateMgtErrors.ERROR_WHILE_RETRIEVING_CERTIFICATE, e,
                    String.valueOf(certificateId));
        }
    }

    @Override
    @Deprecated
    public Certificate getCertificateByName(String certificateName, int tenantId) throws CertificateMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        Map<String, Object> certificateMap = new HashMap<>();
        try {
            jdbcTemplate.fetchSingleRecord(ApplicationCertificateMgtSQLQueries.GET_CERTIFICATE_BY_NAME,
                    (resultSet, rowNumber) -> {
                        certificateMap.put(CertificateMgtSQLConstants.Column.ID,
                                resultSet.getString(CertificateMgtSQLConstants.Column.ID));
                        certificateMap.put(CertificateMgtSQLConstants.Column.CERTIFICATE_IN_PEM,
                                resultSet.getBinaryStream(CertificateMgtSQLConstants.Column.CERTIFICATE_IN_PEM));
                        return null;
                    }, preparedStatement -> {
                        preparedStatement.setString(1, certificateName);
                        preparedStatement.setInt(2, tenantId);
                    }
                                          );
            Certificate certificate = null;
            if (!certificateMap.isEmpty()) {
                certificate = new Certificate.Builder()
                        .id(String.valueOf(certificateMap.get(CertificateMgtSQLConstants.Column.ID)))
                        .name(certificateName)
                        .certificate(CertificateMgtUtil.getStringValueFromBlob((InputStream)
                                certificateMap.get(CertificateMgtSQLConstants.Column.CERTIFICATE_IN_PEM)))
                        .build();
            }

            return certificate;
        } catch (DataAccessException | IOException e) {
            throw CertificateMgtUtil.raiseServerException(
                    CertificateMgtErrors.ERROR_WHILE_RETRIEVING_CERTIFICATE_BY_NAME, e,
                    certificateName);
        }
    }

    @Override
    @Deprecated
    public void updateCertificateContent(int certificateId, String certificateContent, int tenantId)
            throws CertificateMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            InputStream certByteStream = CertificateMgtUtil.getCertificateByteStream(certificateContent);
            int certificateLength = certByteStream.available();
            jdbcTemplate.withTransaction(template -> {
                template.executeUpdate(ApplicationCertificateMgtSQLQueries.UPDATE_CERTIFICATE_CONTENT,
                        preparedStatement -> {
                            int index = 1;
                            preparedStatement.setBlob(index++, certByteStream, certificateLength);
                            preparedStatement.setInt(index++, certificateId);
                            preparedStatement.setInt(index, tenantId);
                        });
                return null;
            });
        } catch (TransactionException | IOException e) {
            throw CertificateMgtUtil.raiseServerException(CertificateMgtErrors.ERROR_WHILE_UPDATING_CERTIFICATE, e,
                    String.valueOf(certificateId));
        }
    }

    @Override
    @Deprecated
    public void deleteCertificate(int certificateId, int tenantId) throws CertificateMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            jdbcTemplate.withTransaction(template -> {
                template.executeUpdate(ApplicationCertificateMgtSQLQueries.DELETE_CERTIFICATE,
                        preparedStatement -> {
                            preparedStatement.setInt(1, certificateId);
                            preparedStatement.setInt(2, tenantId);
                        });
                return null;
            });
        } catch (TransactionException e) {
            throw CertificateMgtUtil.raiseServerException(CertificateMgtErrors.ERROR_WHILE_DELETING_CERTIFICATE, e,
                    String.valueOf(certificateId));
        }
    }
}
