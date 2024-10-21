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
import org.wso2.carbon.identity.certificate.management.core.constant.CertificateMgtConstants;
import org.wso2.carbon.identity.certificate.management.core.dao.impl.ApplicationCertificateManagementDAOImpl;
import org.wso2.carbon.identity.certificate.management.core.dao.impl.CertificateManagementDAOImpl;
import org.wso2.carbon.identity.certificate.management.core.exception.CertificateMgtClientException;
import org.wso2.carbon.identity.certificate.management.core.exception.CertificateMgtException;
import org.wso2.carbon.identity.certificate.management.core.model.Certificate;
import org.wso2.carbon.identity.certificate.management.core.util.CertificateMgtUtil;
import org.wso2.carbon.identity.certificate.management.core.util.CertificateValidator;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import java.util.UUID;

/**
 * This class is responsible for managing certificates of a tenant.
 */
public class CertificateManagementServiceImpl implements CertificateManagementService {

    private static final Log LOG = LogFactory.getLog(CertificateManagementServiceImpl.class);
    private static final CertificateManagementService INSTANCE = new CertificateManagementServiceImpl();
    private static final CertificateManagementDAOImpl DAO = new CertificateManagementDAOImpl();
    private static final ApplicationCertificateManagementDAOImpl APPLICATION_CERT_DAO =
            new ApplicationCertificateManagementDAOImpl();
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

        if (LOG.isDebugEnabled()) {
            LOG.debug("Adding certificate with name: " + certificate.getName());
        }
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

        if (LOG.isDebugEnabled()) {
            LOG.debug("Retrieving certificate with id: " + certificateId);
        }
        Certificate certificate = DAO.getCertificate(certificateId, IdentityTenantUtil.getTenantId(tenantDomain));
        if (certificate == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No certificate found for the id: " + certificateId);
            }
            throw CertificateMgtUtil.raiseClientException(
                    CertificateMgtConstants.ErrorMessages.ERROR_CERTIFICATE_DOES_NOT_EXIST, certificateId);
        }

        return certificate;
    }

    /**
     * Update a certificate with given id.
     *
     * @param certificateId Certificate ID.
     * @param certificate   Certificate content.
     * @param tenantDomain  Tenant domain.
     * @throws CertificateMgtException If an error occurs while updating the certificate.
     */
    @Override
    public void updateCertificate(String certificateId, Certificate certificate, String tenantDomain)
            throws CertificateMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Updating certificate with id: " + certificateId);
        }
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

        if (LOG.isDebugEnabled()) {
            LOG.debug("Deleting certificate with id: " + certificateId);
        }
        DAO.deleteCertificate(certificateId, IdentityTenantUtil.getTenantId(tenantDomain));
    }

    /**
     * Add a certificate.
     *
     * @param certificate  Certificate information.
     * @param tenantDomain Tenant domain.
     * @return Auto incremented integer id of the Certificate.
     * @throws CertificateMgtException If an error occurs while adding the certificate.
     * @deprecated Use {@link #addCertificate(Certificate, String)} instead.
     */
    @Override
    public int addCert(Certificate certificate, String tenantDomain) throws CertificateMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Adding certificate with name: " + certificate.getName());
        }
        doPreAddCertificateValidations(certificate);
        String generatedCertificateId = UUID.randomUUID().toString();
        return APPLICATION_CERT_DAO.addCertificate(generatedCertificateId, certificate,
                IdentityTenantUtil.getTenantId(tenantDomain));
    }

    /**
     * Get certificate information with given id.
     *
     * @param certificateId Certificate ID.
     * @param tenantDomain  Tenant domain.
     * @return Certificate information.
     * @throws CertificateMgtException If an error occurs while getting the certificate.
     * @deprecated Use {@link #getCertificate(String, String)} instead.
     */
    @Override
    public Certificate getCertificate(int certificateId, String tenantDomain) throws CertificateMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Retrieving certificate with id: " + certificateId);
        }
        Certificate certificate = APPLICATION_CERT_DAO.getCertificate(certificateId,
                IdentityTenantUtil.getTenantId(tenantDomain));
        if (certificate == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No certificate found for the id: " + certificateId);
            }
            throw CertificateMgtUtil.raiseClientException(
                    CertificateMgtConstants.ErrorMessages.ERROR_CERTIFICATE_DOES_NOT_EXIST,
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
     * @deprecated Use {@link #getCertificate(String, String)} instead.
     */
    @Override
    public Certificate getCertificateByName(String certificateName, String tenantDomain)
            throws CertificateMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Retrieving certificate with name: " + certificateName);
        }
        Certificate certificate = APPLICATION_CERT_DAO.getCertificateByName(certificateName,
                IdentityTenantUtil.getTenantId(tenantDomain));
        if (certificate == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No certificate found for the name: " + certificateName);
            }
            throw CertificateMgtUtil.raiseClientException(
                    CertificateMgtConstants.ErrorMessages.ERROR_CERTIFICATE_DOES_NOT_EXIST_WITH_GIVEN_NAME,
                    certificateName);
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
     * @deprecated Use {@link #updateCertificate(String, Certificate, String)} instead.
     */
    @Override
    public void updateCertificateContent(int certificateId, String certificateContent, String tenantDomain)
            throws CertificateMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Updating certificate with id: " + certificateId);
        }
        checkIfCertificateExists(certificateId, tenantDomain);
        CERTIFICATE_VALIDATOR.validateCertificateContent(certificateContent);
        APPLICATION_CERT_DAO.updateCertificateContent(certificateId, certificateContent,
                IdentityTenantUtil.getTenantId(tenantDomain));
    }

    /**
     * Delete a certificate with given id.
     *
     * @param certificateId Certificate ID.
     * @param tenantDomain  Tenant domain.
     * @throws CertificateMgtException If an error occurs while deleting the certificate.
     * @deprecated Use {@link #deleteCertificate(String, String)} instead.
     */
    @Override
    public void deleteCertificate(int certificateId, String tenantDomain) throws CertificateMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Deleting certificate with id: " + certificateId);
        }
        APPLICATION_CERT_DAO.deleteCertificate(certificateId, IdentityTenantUtil.getTenantId(tenantDomain));
    }

    /**
     * Validate the certificate.
     *
     * @param certificate Certificate information.
     */
    private void doPreAddCertificateValidations(Certificate certificate) throws CertificateMgtClientException {

        CERTIFICATE_VALIDATOR.validateForBlank(CertificateMgtConstants.NAME_FIELD, certificate.getName());
        CERTIFICATE_VALIDATOR.validateForBlank(CertificateMgtConstants.CERTIFICATE_FIELD, certificate.getCertificate());
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
            throw CertificateMgtUtil.raiseClientException(
                    CertificateMgtConstants.ErrorMessages.ERROR_CERTIFICATE_DOES_NOT_EXIST, certificateId);
        }
    }

    /**
     * Check if the certificate existence.
     *
     * @param certificateId     Action ID.
     * @param tenantDomain Tenant Domain.
     * @throws CertificateMgtException If the certificate does not exist.
     * @deprecated Use {@link #checkIfCertificateExists(String, String)} instead.
     */
    private void checkIfCertificateExists(int certificateId, String tenantDomain) throws CertificateMgtException {

        Certificate certificate = APPLICATION_CERT_DAO.getCertificate(certificateId,
                IdentityTenantUtil.getTenantId(tenantDomain));
        if (certificate == null) {
            throw CertificateMgtUtil.raiseClientException(
                    CertificateMgtConstants.ErrorMessages.ERROR_CERTIFICATE_DOES_NOT_EXIST,
                    String.valueOf(certificateId));
        }
    }
}
