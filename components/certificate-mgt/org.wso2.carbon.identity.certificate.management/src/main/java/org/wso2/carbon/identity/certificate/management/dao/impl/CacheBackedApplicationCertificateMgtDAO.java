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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.certificate.management.cache.CertificateCacheById;
import org.wso2.carbon.identity.certificate.management.cache.CertificateCacheEntry;
import org.wso2.carbon.identity.certificate.management.cache.CertificateIdCacheKey;
import org.wso2.carbon.identity.certificate.management.dao.ApplicationCertificateManagementDAO;
import org.wso2.carbon.identity.certificate.management.exception.CertificateMgtException;
import org.wso2.carbon.identity.certificate.management.model.Certificate;

/**
 * This class represents the caching layer of Application Certificate Management.
 * This supports using auto-incremented IDs for certificate management operations in application-mgt.
 *
 * @deprecated It is recommended to use {@link CacheBackedCertificateMgtDAO}, which supports operations with UUID.
 */
@Deprecated
public class CacheBackedApplicationCertificateMgtDAO implements ApplicationCertificateManagementDAO {

    private static final Log LOG = LogFactory.getLog(CacheBackedApplicationCertificateMgtDAO.class);
    private final CertificateCacheById certificateCacheById;
    private final ApplicationCertificateManagementDAO applicationCertificateMgtDAO;

    public CacheBackedApplicationCertificateMgtDAO(ApplicationCertificateManagementDAO applicationCertificateMgtDAO) {

        this.applicationCertificateMgtDAO = applicationCertificateMgtDAO;
        certificateCacheById = CertificateCacheById.getInstance();
    }

    @Override
    @Deprecated
    public int addCertificate(String certificateId, Certificate certificate, int tenantId)
            throws CertificateMgtException {

        return applicationCertificateMgtDAO.addCertificate(certificateId, certificate, tenantId);
    }

    @Override
    @Deprecated
    public Certificate getCertificate(int certificateId, int tenantId) throws CertificateMgtException {

        CertificateIdCacheKey cacheKey = new CertificateIdCacheKey(String.valueOf(certificateId));
        CertificateCacheEntry entry = certificateCacheById.getValueFromCache(cacheKey, tenantId);

        if (entry != null) {
            LOG.debug("Cache entry found for Certificate integer id: " + certificateId);
            return entry.getCertificate();
        }

        LOG.debug("Cache entry not found for Certificate integer id: " + certificateId +
                ". Fetching entry from DB.");

        Certificate certificate = applicationCertificateMgtDAO.getCertificate(certificateId, tenantId);

        if (certificate != null) {
            LOG.debug("Entry fetched from DB for Certificate integer id: " + certificateId + ". Updating cache.");
            certificateCacheById.addToCache(cacheKey, new CertificateCacheEntry(certificate), tenantId);
        } else {
            LOG.debug("Entry for Certificate integer id: " + certificateId + " not found in cache or DB.");
        }
        return certificate;
    }

    @Override
    @Deprecated
    public Certificate getCertificateByName(String certificateName, int tenantId) throws CertificateMgtException {

        Certificate certificate = applicationCertificateMgtDAO.getCertificateByName(certificateName, tenantId);

        if (certificate != null) {
            LOG.debug("Entry fetched from DB for Certificate name: " + certificateName + ". Updating cache.");
            certificateCacheById.addToCache(new CertificateIdCacheKey(certificate.getId()),
                    new CertificateCacheEntry(certificate), tenantId);
        }
        return certificate;
    }

    @Override
    @Deprecated
    public void updateCertificateContent(int certificateId, String certificateContent, int tenantId)
            throws CertificateMgtException {

        certificateCacheById.clearCacheEntry(new CertificateIdCacheKey(String.valueOf(certificateId)), tenantId);
        applicationCertificateMgtDAO.updateCertificateContent(certificateId, certificateContent, tenantId);
    }

    @Override
    @Deprecated
    public void deleteCertificate(int certificateId, int tenantId) throws CertificateMgtException {

        certificateCacheById.clearCacheEntry(new CertificateIdCacheKey(String.valueOf(certificateId)), tenantId);
        applicationCertificateMgtDAO.deleteCertificate(certificateId, tenantId);
    }
}
