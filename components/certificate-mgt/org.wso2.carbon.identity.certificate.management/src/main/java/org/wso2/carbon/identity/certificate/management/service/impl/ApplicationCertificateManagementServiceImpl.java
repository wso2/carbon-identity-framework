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

package org.wso2.carbon.identity.certificate.management.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.annotation.bundle.Capability;
import org.wso2.carbon.identity.certificate.management.constant.CertificateMgtErrors;
import org.wso2.carbon.identity.certificate.management.dao.impl.ApplicationCertificateManagementDAOImpl;
import org.wso2.carbon.identity.certificate.management.dao.impl.CacheBackedApplicationCertificateMgtDAO;
import org.wso2.carbon.identity.certificate.management.exception.CertificateMgtClientException;
import org.wso2.carbon.identity.certificate.management.exception.CertificateMgtException;
import org.wso2.carbon.identity.certificate.management.model.Certificate;
import org.wso2.carbon.identity.certificate.management.service.ApplicationCertificateManagementService;
import org.wso2.carbon.identity.certificate.management.util.CertificateMgtExceptionHandler;
import org.wso2.carbon.identity.certificate.management.util.CertificateValidator;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import java.util.UUID;

/**
 * This class is responsible for managing trusted certificates of applications of a tenant.
 * This service supports using auto-incremented IDs for certificate management operations in application-mgt.
 *
 * @deprecated It is recommended to use {@link CertificateManagementServiceImpl}, which supports operations with UUID.
 */
@Deprecated
@Capability(
        namespace = "osgi.service",
        attribute = {
                "objectClass=org.wso2.carbon.identity.certificate.management.service" +
                        ".ApplicationCertificateManagementService",
                "service.scope=singleton"
        }
)
public class ApplicationCertificateManagementServiceImpl implements ApplicationCertificateManagementService {

    private static final Log LOG = LogFactory.getLog(ApplicationCertificateManagementServiceImpl.class);
    private static final ApplicationCertificateManagementService INSTANCE =
            new ApplicationCertificateManagementServiceImpl();
    private static final CacheBackedApplicationCertificateMgtDAO CACHE_BACKED_DAO =
            new CacheBackedApplicationCertificateMgtDAO(new ApplicationCertificateManagementDAOImpl());

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
        doPreAddValidations(certificate);
        String generatedCertificateId = UUID.randomUUID().toString();
        return CACHE_BACKED_DAO.addCertificate(generatedCertificateId, certificate,
                IdentityTenantUtil.getTenantId(tenantDomain));
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
        Certificate certificate = CACHE_BACKED_DAO.getCertificate(certificateId,
                IdentityTenantUtil.getTenantId(tenantDomain));
        if (certificate == null) {
            LOG.debug("No certificate found for the id: " + certificateId);
            CertificateMgtExceptionHandler.throwClientException(CertificateMgtErrors.ERROR_CERTIFICATE_DOES_NOT_EXIST,
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
        Certificate certificate = CACHE_BACKED_DAO.getCertificateByName(certificateName,
                IdentityTenantUtil.getTenantId(tenantDomain));
        if (certificate == null) {
            LOG.debug("No certificate found for the name: " + certificateName);
            CertificateMgtExceptionHandler.throwClientException(
                    CertificateMgtErrors.ERROR_CERTIFICATE_DOES_NOT_EXIST_WITH_GIVEN_NAME, certificateName);
        }

        return certificate;
    }

    /**
     * Update a certificate with given id.
     * Only the non-null and non-empty fields in the provided certificate will be updated.
     * Null or empty fields will be ignored.
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
        doPreUpdateValidations(certificateId, certificateContent, tenantDomain);
        CACHE_BACKED_DAO.updateCertificateContent(certificateId, certificateContent,
                IdentityTenantUtil.getTenantId(tenantDomain));
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
        CACHE_BACKED_DAO.deleteCertificate(certificateId, IdentityTenantUtil.getTenantId(tenantDomain));
    }

    /**
     * Validate the certificate.
     *
     * @param certificate Certificate information.
     * @throws CertificateMgtClientException If the certificate is invalid.
     */
    private void doPreAddValidations(Certificate certificate) throws CertificateMgtClientException {

        CertificateValidator.validateForBlank(CertificateValidator.NAME_FIELD, certificate.getName());
        CertificateValidator.validateForBlank(CertificateValidator.CERTIFICATE_FIELD,
                certificate.getCertificateContent());
        CertificateValidator.validatePemFormat(certificate.getCertificateContent());
    }

    /**
     * Validate the certificate content and id.
     *
     * @param certificateContent Certificate content.
     * @param tenantDomain Tenant domain.
     * @throws CertificateMgtException If the certificate content or certificate id is invalid.
     */
    private void doPreUpdateValidations(int certificateId, String certificateContent, String tenantDomain)
            throws CertificateMgtException {

        Certificate certificate = CACHE_BACKED_DAO.getCertificate(certificateId,
                IdentityTenantUtil.getTenantId(tenantDomain));
        if (certificate == null) {
            CertificateMgtExceptionHandler.throwClientException(CertificateMgtErrors.ERROR_CERTIFICATE_DOES_NOT_EXIST,
                    String.valueOf(certificateId));
        }
        CertificateValidator.validateCertificateContent(certificateContent);
    }
}
