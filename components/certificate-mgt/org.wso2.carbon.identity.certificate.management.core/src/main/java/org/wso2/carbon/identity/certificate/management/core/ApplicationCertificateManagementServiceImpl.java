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
import org.wso2.carbon.identity.certificate.management.core.dao.impl.ApplicationCertificateManagementDAOImpl;
import org.wso2.carbon.identity.certificate.management.core.exception.CertificateMgtClientException;
import org.wso2.carbon.identity.certificate.management.core.exception.CertificateMgtException;
import org.wso2.carbon.identity.certificate.management.core.model.Certificate;
import org.wso2.carbon.identity.certificate.management.core.util.CertificateMgtUtil;
import org.wso2.carbon.identity.certificate.management.core.util.CertificateValidator;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import java.util.UUID;

/**
 * This class is responsible for managing certificates of applications of a tenant.
 * @deprecated Use {@link CertificateManagementServiceImpl} instead.
 */
public class ApplicationCertificateManagementServiceImpl implements ApplicationCertificateManagementService {

    private static final Log LOG = LogFactory.getLog(ApplicationCertificateManagementServiceImpl.class);
    private static final ApplicationCertificateManagementService INSTANCE =
            new ApplicationCertificateManagementServiceImpl();
    private static final ApplicationCertificateManagementDAOImpl DAO = new ApplicationCertificateManagementDAOImpl();
    private static final CertificateValidator CERTIFICATE_VALIDATOR = new CertificateValidator();

    private ApplicationCertificateManagementServiceImpl() {
    }

    public static ApplicationCertificateManagementService getInstance() {

        return INSTANCE;
    }

    /**
     * Add a certificate.
     *
     * @param certificate  Certificate information.
     * @param tenantDomain Tenant domain.
     * @return Auto incremented integer id of the Certificate.
     * @throws CertificateMgtException If an error occurs while adding the certificate.
     */
    @Override
    @Deprecated
    public int addCertificate(Certificate certificate, String tenantDomain) throws CertificateMgtException {

        LOG.debug("Adding certificate with name: " + certificate.getName());
        doPreAddCertificateValidations(certificate);
        String generatedCertificateId = UUID.randomUUID().toString();
        return DAO.addCertificate(generatedCertificateId, certificate, IdentityTenantUtil.getTenantId(tenantDomain));
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
    @Deprecated
    public Certificate getCertificate(int certificateId, String tenantDomain) throws CertificateMgtException {

        LOG.debug("Retrieving certificate with id: " + certificateId);
        Certificate certificate = DAO.getCertificate(certificateId, IdentityTenantUtil.getTenantId(tenantDomain));
        if (certificate == null) {
            LOG.debug("No certificate found for the id: " + certificateId);
            throw CertificateMgtUtil.raiseClientException(
                    CertificateMgtErrors.ERROR_CERTIFICATE_DOES_NOT_EXIST,
                    String.valueOf(certificateId));
        }

        return certificate;
    }

    /**
     * Get certificate information with given name.
     *
     * @param certificateName Certificate name.
     * @param tenantDomain    Tenant domain.
     * @return Certificate information.
     * @throws CertificateMgtException If an error occurs while getting the certificate by name.
     */
    @Override
    @Deprecated
    public Certificate getCertificateByName(String certificateName, String tenantDomain)
            throws CertificateMgtException {

        LOG.debug("Retrieving certificate with name: " + certificateName);
        Certificate certificate = DAO.getCertificateByName(certificateName,
                IdentityTenantUtil.getTenantId(tenantDomain));
        if (certificate == null) {
            LOG.debug("No certificate found for the name: " + certificateName);
            throw CertificateMgtUtil.raiseClientException(
                    CertificateMgtErrors.ERROR_CERTIFICATE_DOES_NOT_EXIST_WITH_GIVEN_NAME, certificateName);
        }

        return certificate;
    }

    /**
     * Update a certificate with given id.
     *
     * @param certificateId      Certificate ID.
     * @param certificateContent Certificate content.
     * @param tenantDomain       Tenant domain.
     * @throws CertificateMgtException If an error occurs while updating the certificate.
     */
    @Override
    @Deprecated
    public void updateCertificateContent(int certificateId, String certificateContent, String tenantDomain)
            throws CertificateMgtException {

        LOG.debug("Updating certificate with id: " + certificateId);
        checkIfCertificateExists(certificateId, tenantDomain);
        CERTIFICATE_VALIDATOR.validateCertificateContent(certificateContent);
        DAO.updateCertificateContent(certificateId, certificateContent, IdentityTenantUtil.getTenantId(tenantDomain));
    }

    /**
     * Delete a certificate with given id.
     *
     * @param certificateId Certificate ID.
     * @param tenantDomain  Tenant domain.
     * @throws CertificateMgtException If an error occurs while deleting the certificate.
     */
    @Override
    @Deprecated
    public void deleteCertificate(int certificateId, String tenantDomain) throws CertificateMgtException {

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
    private void checkIfCertificateExists(int certificateId, String tenantDomain) throws CertificateMgtException {

        Certificate certificate = DAO.getCertificate(certificateId, IdentityTenantUtil.getTenantId(tenantDomain));
        if (certificate == null) {
            throw CertificateMgtUtil.raiseClientException(CertificateMgtErrors.ERROR_CERTIFICATE_DOES_NOT_EXIST,
                    String.valueOf(certificateId));
        }
    }
}
