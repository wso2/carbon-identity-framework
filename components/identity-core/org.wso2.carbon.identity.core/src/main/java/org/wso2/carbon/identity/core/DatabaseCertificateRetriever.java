/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.core;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.api.Tenant;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * The database backed implementation of @{@link CertificateRetriever}
 */
public class DatabaseCertificateRetriever implements CertificateRetriever {

    private static final Log log = LogFactory.getLog(DatabaseCertificateRetriever.class);

    private static String QUERY_TO_GET_APPLICATION_CERTIFICATE = "SELECT CERTIFICATE_IN_PEM FROM IDN_CERTIFICATE " +
            "WHERE ID = ?";

    /**
     * @param certificateId Database identifier of the certificate.
     * @param tenant        Tenant where the certificate belongs to. But in this implementation the passed tenant is
     *                      not considered since the database id is already there.
     * @return The certificate for the given database identifier.
     * @throws CertificateRetrievingException
     */
    @Override
    public X509Certificate getCertificate(String certificateId, Tenant tenant) throws CertificateRetrievingException {

        Connection connection;
        try {
             connection = IdentityDatabaseUtil.getDBConnection();
        } catch (IdentityRuntimeException e) {
            throw new CertificateRetrievingException("Couldn't get a database connection.", e);
        }

        PreparedStatement statementToGetApplicationCertificate = null;
        ResultSet queryResults = null;

        try {
            statementToGetApplicationCertificate = connection.prepareStatement(QUERY_TO_GET_APPLICATION_CERTIFICATE);
            statementToGetApplicationCertificate.setInt(1, Integer.parseInt(certificateId));

            queryResults = statementToGetApplicationCertificate.executeQuery();


            String certificateContent = null;
            while (queryResults.next()) {
                certificateContent = getBlobValue(queryResults.getBinaryStream(1));
            }

            if (StringUtils.isNotBlank(certificateContent)) {
                return (X509Certificate) IdentityUtil.convertPEMEncodedContentToCertificate(certificateContent);
            }
        } catch (SQLException e) {
            String errorMessage = String.format("An error occurred while retrieving the certificate content from " +
                    "the database for the ID '%s'", certificateId);
            throw new CertificateRetrievingException(errorMessage, e);
        } catch (CertificateException e) {
            String errorMessage = String.format("An error occurred while build a certificate using the certificate " +
                    "content from the database for the ID '%s'", certificateId);
            throw new CertificateRetrievingException(errorMessage, e);
        } catch (IOException e) {
            String errorMessage = String.format("An error occurred while reading the certificate blob from the " +
                    "database for the ID '%s'", certificateId);
            throw new CertificateRetrievingException(errorMessage, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, queryResults, statementToGetApplicationCertificate);
        }

        return null;
    }

    /**
     * Get string from inputStream of a blob.
     *
     * @param is input stream.
     * @return String result.
     * @throws IOException
     */
    private String getBlobValue(InputStream is) throws IOException {

        if (is != null) {
            BufferedReader br = null;
            StringBuilder sb = new StringBuilder();
            String line;
            try {
                br = new BufferedReader(new InputStreamReader(is));
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        log.error("Error in retrieving the Blob value", e);
                    }
                }
            }

            return sb.toString();
        }
        return null;
    }
}
