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

package org.wso2.carbon.identity.certificate.management.core.util;

import org.apache.axiom.om.util.Base64;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.certificate.management.core.constant.CertificateMgtConstants;
import org.wso2.carbon.identity.certificate.management.core.exception.CertificateMgtClientException;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

/**
 * This class is responsible for validating certificates.
 */
public class CertificateValidator {

    private static final String BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----";
    private static final String END_CERTIFICATE = "-----END CERTIFICATE-----";

    /**
     * Validate whether required fields exist.
     *
     * @param fieldValue Field value.
     * @throws CertificateMgtClientException if the provided field is empty.
     */
    public void validateForBlank(String fieldName, String fieldValue) throws CertificateMgtClientException {

        if (StringUtils.isBlank(fieldValue)) {
            throw CertificateMgtUtil.raiseClientException(CertificateMgtConstants.ErrorMessages.ERROR_EMPTY_FIELD,
                    fieldName);
        }
    }

    /**
     * Validate the certificate name.
     *
     * @param name Certificate name.
     * @throws CertificateMgtClientException if the name is not valid.
     */
    public void validateCertificateName(String name) throws CertificateMgtClientException {

        if (StringUtils.isBlank(name)) {
            throw CertificateMgtUtil.raiseClientException(CertificateMgtConstants.ErrorMessages.ERROR_INVALID_FIELD,
                    CertificateMgtConstants.NAME_FIELD);
        }
    }

    /**
     * Validate the certificate content.
     *
     * @param certificate Certificate in PEM format.
     * @throws CertificateMgtClientException if the certificate content is not valid.
     */
    public void validateCertificateContent(String certificate) throws CertificateMgtClientException {

        if (StringUtils.isBlank(certificate)) {
            throw CertificateMgtUtil.raiseClientException(CertificateMgtConstants.ErrorMessages.ERROR_INVALID_FIELD,
                    CertificateMgtConstants.CERTIFICATE_FIELD);
        }
        validatePemFormat(certificate);
    }

    /**
     * Validate the PEM format of the certificate.
     *
     * @param certificate Certificate in PEM format.
     * @throws CertificateMgtClientException if the certificate is not in PEM format.
     */
    public void validatePemFormat(String certificate) throws CertificateMgtClientException {

        if (!certificate.startsWith(BEGIN_CERTIFICATE) || !certificate.endsWith(END_CERTIFICATE)) {
            throw CertificateMgtUtil.raiseClientException(
                    CertificateMgtConstants.ErrorMessages.ERROR_MISSING_CERTIFICATE_BEGIN_END_MARKERS);
        }

        try {
            String certificateContentString = certificate.substring(
                certificate.indexOf(BEGIN_CERTIFICATE) + BEGIN_CERTIFICATE.length(),
                certificate.indexOf(END_CERTIFICATE));
            byte[] bytes = Base64.decode(certificateContentString);

            CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(bytes));
        } catch (CertificateException e) {
            throw CertificateMgtUtil.raiseClientException(
                    CertificateMgtConstants.ErrorMessages.ERROR_INVALID_CERTIFICATE_CONTENT);
        }
    }
}
