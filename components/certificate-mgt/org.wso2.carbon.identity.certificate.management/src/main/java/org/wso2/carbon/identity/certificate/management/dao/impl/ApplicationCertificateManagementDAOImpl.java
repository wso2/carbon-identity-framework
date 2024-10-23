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

package org.wso2.carbon.identity.certificate.management.dao.impl;

import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.database.utils.jdbc.exceptions.TransactionException;
import org.wso2.carbon.identity.certificate.management.constant.ApplicationCertificateMgtSQLQueries;
import org.wso2.carbon.identity.certificate.management.constant.CertificateMgtErrors;
import org.wso2.carbon.identity.certificate.management.constant.CertificateMgtSQLConstants;
import org.wso2.carbon.identity.certificate.management.dao.ApplicationCertificateManagementDAO;
import org.wso2.carbon.identity.certificate.management.exception.CertificateMgtException;
import org.wso2.carbon.identity.certificate.management.model.Certificate;
import org.wso2.carbon.identity.certificate.management.util.CertificateMgtExceptionHandler;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is the implementation of the Application Certificate Management DAO.
 * This supports using auto-incremented IDs for certificate management operations in application-mgt.
 *
 * @deprecated It is recommended to use {@link CertificateManagementDAOImpl}, which supports operations with UUID.
 */
@Deprecated
public class ApplicationCertificateManagementDAOImpl implements ApplicationCertificateManagementDAO {

    @Override
    @Deprecated
    public int addCertificate(String certificateId, Certificate certificate, int tenantId)
            throws CertificateMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            InputStream certByteStream = getCertificateByteStream(certificate.getCertificate());
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
            CertificateMgtExceptionHandler.throwServerException(CertificateMgtErrors.ERROR_WHILE_ADDING_CERTIFICATE, e,
                    certificate.getName());
        }
        return 0;
    }

    @Override
    @Deprecated
    public Certificate getCertificate(int certificateId, int tenantId) throws CertificateMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        Map<String, Object> attributeMap = new HashMap<>();
        Certificate certificate = null;
        try {
            jdbcTemplate.fetchSingleRecord(ApplicationCertificateMgtSQLQueries.GET_CERTIFICATE_BY_ID,
                (resultSet, rowNumber) -> {
                    attributeMap.put(CertificateMgtSQLConstants.Column.NAME,
                            resultSet.getString(CertificateMgtSQLConstants.Column.NAME));
                    attributeMap.put(CertificateMgtSQLConstants.Column.CERTIFICATE_IN_PEM,
                            resultSet.getBinaryStream(CertificateMgtSQLConstants.Column.CERTIFICATE_IN_PEM));
                    return null;
                }, preparedStatement -> {
                    preparedStatement.setInt(1, certificateId);
                    preparedStatement.setInt(2, tenantId);
                }
            );
            if (!attributeMap.isEmpty()) {
                certificate = new Certificate.Builder()
                        .id(String.valueOf(certificateId))
                        .name((String) attributeMap.get(CertificateMgtSQLConstants.Column.NAME))
                        .certificate(getStringValueFromBlob((InputStream)
                                attributeMap.get(CertificateMgtSQLConstants.Column.CERTIFICATE_IN_PEM)))
                        .build();
            }
        } catch (DataAccessException | IOException e) {
            CertificateMgtExceptionHandler.throwServerException(CertificateMgtErrors.ERROR_WHILE_RETRIEVING_CERTIFICATE,
                    e, String.valueOf(certificateId));
        }
        return certificate;
    }

    @Override
    @Deprecated
    public Certificate getCertificateByName(String certificateName, int tenantId) throws CertificateMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        Map<String, Object> attributeMap = new HashMap<>();
        Certificate certificate = null;
        try {
            jdbcTemplate.fetchSingleRecord(ApplicationCertificateMgtSQLQueries.GET_CERTIFICATE_BY_NAME,
                (resultSet, rowNumber) -> {
                    attributeMap.put(CertificateMgtSQLConstants.Column.ID,
                            resultSet.getString(CertificateMgtSQLConstants.Column.ID));
                    attributeMap.put(CertificateMgtSQLConstants.Column.CERTIFICATE_IN_PEM,
                            resultSet.getBinaryStream(CertificateMgtSQLConstants.Column.CERTIFICATE_IN_PEM));
                    return null;
                }, preparedStatement -> {
                    preparedStatement.setString(1, certificateName);
                    preparedStatement.setInt(2, tenantId);
                }
            );
            if (!attributeMap.isEmpty()) {
                certificate = new Certificate.Builder()
                        .id(String.valueOf(attributeMap.get(CertificateMgtSQLConstants.Column.ID)))
                        .name(certificateName)
                        .certificate(getStringValueFromBlob((InputStream)
                                attributeMap.get(CertificateMgtSQLConstants.Column.CERTIFICATE_IN_PEM)))
                        .build();
            }
        } catch (DataAccessException | IOException e) {
            CertificateMgtExceptionHandler.throwServerException(
                    CertificateMgtErrors.ERROR_WHILE_RETRIEVING_CERTIFICATE_BY_NAME, e, certificateName);
        }
        return certificate;
    }

    @Override
    @Deprecated
    public void updateCertificateContent(int certificateId, String certificateContent, int tenantId)
            throws CertificateMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            InputStream certByteStream = getCertificateByteStream(certificateContent);
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
            CertificateMgtExceptionHandler.throwServerException(CertificateMgtErrors.ERROR_WHILE_UPDATING_CERTIFICATE,
                    e, String.valueOf(certificateId));
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
            CertificateMgtExceptionHandler.throwServerException(CertificateMgtErrors.ERROR_WHILE_DELETING_CERTIFICATE,
                    e, String.valueOf(certificateId));
        }
    }

    /**
     * Converts the given certificate content into a stream of bytes using UTF-8 encoding.
     *
     * @param certificateContent Certificate content to be converted to blob.
     * @return InputStream of the certificate content.
     */
    private InputStream getCertificateByteStream(String certificateContent) {

        return new ByteArrayInputStream(certificateContent.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Get the string representation of the content from the given InputStream of a blob.
     *
     * @param stream InputStream containing blob data.
     * @return String content of the blob, or null if the input stream is null.
     * @throws IOException If an I/O error occurs while reading the stream.
     */
    private String getStringValueFromBlob(InputStream stream) throws IOException {

        if (stream == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        }

        return sb.toString();
    }
}
