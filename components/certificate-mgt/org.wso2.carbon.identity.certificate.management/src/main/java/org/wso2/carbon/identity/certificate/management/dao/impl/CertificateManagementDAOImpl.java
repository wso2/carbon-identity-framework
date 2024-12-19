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
import org.wso2.carbon.identity.certificate.management.constant.CertificateMgtErrors;
import org.wso2.carbon.identity.certificate.management.constant.CertificateMgtSQLConstants;
import org.wso2.carbon.identity.certificate.management.dao.CertificateManagementDAO;
import org.wso2.carbon.identity.certificate.management.exception.CertificateMgtException;
import org.wso2.carbon.identity.certificate.management.exception.CertificateMgtRuntimeException;
import org.wso2.carbon.identity.certificate.management.model.Certificate;
import org.wso2.carbon.identity.certificate.management.util.CertificateMgtExceptionHandler;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * This class implements the Certificate Management DAO.
 * And this is responsible for handling the CRUD operations with the database.
 */
public class CertificateManagementDAOImpl implements CertificateManagementDAO {

    @Override
    public void addCertificate(String certificateId, Certificate certificate, int tenantId)
            throws CertificateMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            InputStream certByteStream = getCertificateByteStream(certificate.getCertificateContent());
            int certificateLength = certByteStream.available();
            jdbcTemplate.withTransaction(template -> {
                template.executeInsert(CertificateMgtSQLConstants.Query.ADD_CERTIFICATE,
                    preparedStatement -> {
                        int index = 1;
                        preparedStatement.setString(index++, certificateId);
                        preparedStatement.setString(index++, certificate.getName());
                        preparedStatement.setBinaryStream(index++, certByteStream, certificateLength);
                            preparedStatement.setInt(index, tenantId);
                        }, certificate, false);
                return null;
            });
        } catch (TransactionException | IOException e) {
            CertificateMgtExceptionHandler.throwServerException(CertificateMgtErrors.ERROR_WHILE_ADDING_CERTIFICATE, e,
                    certificate.getName());
        }
    }

    @Override
    public Certificate getCertificate(String certificateId, int tenantId) throws CertificateMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        Certificate certificate = null;
        try {
            certificate = jdbcTemplate.fetchSingleRecord(CertificateMgtSQLConstants.Query.GET_CERTIFICATE_BY_ID,
                (resultSet, rowNumber) -> new Certificate.Builder()
                        .id(String.valueOf(certificateId))
                        .name(resultSet.getString(CertificateMgtSQLConstants.Column.NAME))
                        .certificateContent(getStringValueFromBlob(resultSet.getBinaryStream(
                                CertificateMgtSQLConstants.Column.CERTIFICATE_IN_PEM)))
                        .build(),
                preparedStatement -> {
                    preparedStatement.setString(1, certificateId);
                    preparedStatement.setInt(2, tenantId);
                }
            );
        } catch (CertificateMgtRuntimeException | DataAccessException e) {
            /**
             * Handling CertificateMgtRuntimeException, which is intentionally thrown to represent an underlying
             * IOException from the {@link #getStringValueFromBlob(InputStream)} method.
             */
            CertificateMgtExceptionHandler.throwServerException(CertificateMgtErrors.ERROR_WHILE_RETRIEVING_CERTIFICATE,
                    e, certificateId);
        }
        return certificate;
    }

    @Override
    public void updateCertificateContent(String certificateId, String certificateContent, int tenantId)
            throws CertificateMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            InputStream certByteStream = getCertificateByteStream(certificateContent);
            int certificateLength = certByteStream.available();
            jdbcTemplate.withTransaction(template -> {
                template.executeUpdate(CertificateMgtSQLConstants.Query.UPDATE_CERTIFICATE_CONTENT,
                    preparedStatement -> {
                        int index = 1;
                        preparedStatement.setBinaryStream(index++, certByteStream, certificateLength);
                        preparedStatement.setString(index++, certificateId);
                        preparedStatement.setInt(index, tenantId);
                    });
                return null;
            });
        } catch (TransactionException | IOException e) {
            CertificateMgtExceptionHandler.throwServerException(CertificateMgtErrors.ERROR_WHILE_UPDATING_CERTIFICATE,
                    e, certificateId);
        }
    }

    @Override
    public void deleteCertificate(String certificateId, int tenantId) throws CertificateMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            jdbcTemplate.withTransaction(template -> {
                template.executeUpdate(CertificateMgtSQLConstants.Query.DELETE_CERTIFICATE,
                    preparedStatement -> {
                        preparedStatement.setString(1, certificateId);
                        preparedStatement.setInt(2, tenantId);
                    });
                return null;
            });
        } catch (TransactionException e) {
            CertificateMgtExceptionHandler.throwServerException(CertificateMgtErrors.ERROR_WHILE_DELETING_CERTIFICATE,
                    e, certificateId);
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
     */
    private String getStringValueFromBlob(InputStream stream) {

        if (stream == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            // Throwing a runtime exception because NamedQueryFilter does not handle IOExceptions.
            CertificateMgtExceptionHandler.throwRuntimeException("Error while reading the InputStream", e);
        }

        return sb.toString();
    }
}
