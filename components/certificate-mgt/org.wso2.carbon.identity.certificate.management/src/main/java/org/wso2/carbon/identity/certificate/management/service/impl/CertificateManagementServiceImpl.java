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
import org.wso2.carbon.identity.certificate.management.dao.impl.CacheBackedCertificateMgtDAO;
import org.wso2.carbon.identity.certificate.management.dao.impl.CertificateManagementDAOImpl;
import org.wso2.carbon.identity.certificate.management.exception.CertificateMgtClientException;
import org.wso2.carbon.identity.certificate.management.exception.CertificateMgtException;
import org.wso2.carbon.identity.certificate.management.model.Certificate;
import org.wso2.carbon.identity.certificate.management.service.CertificateManagementService;
import org.wso2.carbon.identity.certificate.management.util.CertificateMgtExceptionHandler;
import org.wso2.carbon.identity.certificate.management.util.CertificateValidator;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import java.util.UUID;

/**
 * This class is responsible for managing trusted certificates of a tenant.
 */
@Capability(
        namespace = "osgi.service",
        attribute = {
                "objectClass=org.wso2.carbon.identity.certificate.management.service.CertificateManagementService",
                "service.scope=singleton"
        }
)
public class CertificateManagementServiceImpl implements CertificateManagementService {

    private static final Log LOG = LogFactory.getLog(CertificateManagementServiceImpl.class);
    private static final CertificateManagementService INSTANCE = new CertificateManagementServiceImpl();
    private static final CacheBackedCertificateMgtDAO CACHE_BACKED_DAO =
            new CacheBackedCertificateMgtDAO(new CertificateManagementDAOImpl());

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
        doPreAddValidations(certificate);
        String generatedCertificateId = UUID.randomUUID().toString();
        CACHE_BACKED_DAO.addCertificate(generatedCertificateId, certificate,
                IdentityTenantUtil.getTenantId(tenantDomain));

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
        Certificate certificate = CACHE_BACKED_DAO.getCertificate(certificateId,
                IdentityTenantUtil.getTenantId(tenantDomain));
        if (certificate == null) {
            LOG.debug("No certificate found for the id: " + certificateId);
            CertificateMgtExceptionHandler.throwClientException(CertificateMgtErrors.ERROR_CERTIFICATE_DOES_NOT_EXIST,
                    certificateId);
        }

        return certificate;
    }

    /**
     * Update a certificate content with given id.
     *
     * @param certificateId      Certificate ID.
     * @param certificateContent Certificate content.
     * @param tenantDomain       Tenant domain.
     * @throws CertificateMgtException If an error occurs while updating the certificate.
     */
    @Override
    public void updateCertificateContent(String certificateId, String certificateContent, String tenantDomain)
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
    public void deleteCertificate(String certificateId, String tenantDomain) throws CertificateMgtException {

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
    private void doPreUpdateValidations(String certificateId, String certificateContent, String tenantDomain)
            throws CertificateMgtException {

        Certificate certificate = CACHE_BACKED_DAO.getCertificate(certificateId,
                IdentityTenantUtil.getTenantId(tenantDomain));
        if (certificate == null) {
            CertificateMgtExceptionHandler.throwClientException(CertificateMgtErrors.ERROR_CERTIFICATE_DOES_NOT_EXIST,
                    certificateId);
        }
        CertificateValidator.validateCertificateContent(certificateContent);
    }
}
