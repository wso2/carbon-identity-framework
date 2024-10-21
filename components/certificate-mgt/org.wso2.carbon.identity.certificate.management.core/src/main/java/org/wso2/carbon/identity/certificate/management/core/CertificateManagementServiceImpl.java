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

package org.wso2.carbon.identity.certificate.management.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.certificate.management.core.constant.CertificateMgtErrors;
import org.wso2.carbon.identity.certificate.management.core.dao.impl.CertificateManagementDAOImpl;
import org.wso2.carbon.identity.certificate.management.core.exception.CertificateMgtClientException;
import org.wso2.carbon.identity.certificate.management.core.exception.CertificateMgtException;
import org.wso2.carbon.identity.certificate.management.core.model.Certificate;
import org.wso2.carbon.identity.certificate.management.core.util.CertificateMgtExceptionHandler;
import org.wso2.carbon.identity.certificate.management.core.util.CertificateValidator;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import java.util.UUID;

/**
 * This class is responsible for managing trusted certificates of a tenant.
 */
public class CertificateManagementServiceImpl implements CertificateManagementService {

    private static final Log LOG = LogFactory.getLog(CertificateManagementServiceImpl.class);
    private static final CertificateManagementService INSTANCE = new CertificateManagementServiceImpl();
    private static final CertificateManagementDAOImpl DAO = new CertificateManagementDAOImpl();
    private static final CertificateValidator CERTIFICATE_VALIDATOR = new CertificateValidator();

    private CertificateManagementServiceImpl() {
    }

    public static CertificateManagementService getInstance() {

        return INSTANCE;
    }

    /**
     * Add a certificate.
     *
     * @param certificate  Certificate information.
     * @param tenantDomain Tenant domain.
     * @return Certificate ID.
     * @throws CertificateMgtException If an error occurs while adding the certificate.
     */
    @Override
    public String addCertificate(Certificate certificate, String tenantDomain) throws CertificateMgtException {

        LOG.debug("Adding certificate with name: " + certificate.getName());
        doPreAddCertificateValidations(certificate);
        String generatedCertificateId = UUID.randomUUID().toString();
        DAO.addCertificate(generatedCertificateId, certificate, IdentityTenantUtil.getTenantId(tenantDomain));

        return generatedCertificateId;
    }

    /**
     * Get certificate information with given id.
     *
     * @param certificateId Certificate ID.
     * @param tenantDomain  Tenant domain.
     * @return Certificate information.
     * @throws CertificateMgtException If an error occurs while getting the certificate.
     */
    @Override
    public Certificate getCertificate(String certificateId, String tenantDomain) throws CertificateMgtException {

        LOG.debug("Retrieving certificate with id: " + certificateId);
        Certificate certificate = DAO.getCertificate(certificateId, IdentityTenantUtil.getTenantId(tenantDomain));
        if (certificate == null) {
            LOG.debug("No certificate found for the id: " + certificateId);
            CertificateMgtExceptionHandler.throwClientException(CertificateMgtErrors.ERROR_CERTIFICATE_DOES_NOT_EXIST,
                    certificateId);
        }

        return certificate;
    }

    /**
     * Update a certificate with given id.
     * Only the non-null and non-empty fields in the provided certificate will be updated.
     * Null or empty fields will be ignored.
     *
     * @param certificateId Certificate ID.
     * @param certificate   Certificate content.
     * @param tenantDomain  Tenant domain.
     * @throws CertificateMgtException If an error occurs while updating the certificate.
     */
    @Override
    public void updateCertificate(String certificateId, Certificate certificate, String tenantDomain)
            throws CertificateMgtException {

        LOG.debug("Updating certificate with id: " + certificateId);
        checkIfCertificateExists(certificateId, tenantDomain);
        if (certificate.getName() != null && certificate.getCertificate() != null) {
            CERTIFICATE_VALIDATOR.validateCertificateName(certificate.getName());
            CERTIFICATE_VALIDATOR.validateCertificateContent(certificate.getCertificate());

            DAO.updateCertificate(certificateId, certificate, IdentityTenantUtil.getTenantId(tenantDomain));
            return;
        }
        if (certificate.getCertificate() != null) {
            CERTIFICATE_VALIDATOR.validateCertificateContent(certificate.getCertificate());
            DAO.patchCertificateContent(certificateId, certificate.getCertificate(),
                    IdentityTenantUtil.getTenantId(tenantDomain));
            return;
        }
        if (certificate.getName() != null) {
            CERTIFICATE_VALIDATOR.validateCertificateName(certificate.getName());
            DAO.patchCertificateName(certificateId, certificate.getName(),
                    IdentityTenantUtil.getTenantId(tenantDomain));
        }
    }

    /**
     * Delete a certificate with given id.
     *
     * @param certificateId Certificate ID.
     * @param tenantDomain  Tenant domain.
     * @throws CertificateMgtException If an error occurs while deleting the certificate.
     */
    @Override
    public void deleteCertificate(String certificateId, String tenantDomain) throws CertificateMgtException {

        LOG.debug("Deleting certificate with id: " + certificateId);
        DAO.deleteCertificate(certificateId, IdentityTenantUtil.getTenantId(tenantDomain));
    }

    /**
     * Validate the certificate.
     *
     * @param certificate Certificate information.
     */
    private void doPreAddCertificateValidations(Certificate certificate) throws CertificateMgtClientException {

        CERTIFICATE_VALIDATOR.validateForBlank(CertificateValidator.NAME_FIELD, certificate.getName());
        CERTIFICATE_VALIDATOR.validateForBlank(CertificateValidator.CERTIFICATE_FIELD, certificate.getCertificate());
        CERTIFICATE_VALIDATOR.validatePemFormat(certificate.getCertificate());
    }

    /**
     * Check if the certificate existence.
     *
     * @param certificateId     Action ID.
     * @param tenantDomain Tenant Domain.
     * @throws CertificateMgtException If the certificate does not exist.
     */
    private void checkIfCertificateExists(String certificateId, String tenantDomain) throws CertificateMgtException {

        Certificate certificate = DAO.getCertificate(certificateId, IdentityTenantUtil.getTenantId(tenantDomain));
        if (certificate == null) {
            CertificateMgtExceptionHandler.throwClientException(CertificateMgtErrors.ERROR_CERTIFICATE_DOES_NOT_EXIST,
                    certificateId);
        }
    }
}
