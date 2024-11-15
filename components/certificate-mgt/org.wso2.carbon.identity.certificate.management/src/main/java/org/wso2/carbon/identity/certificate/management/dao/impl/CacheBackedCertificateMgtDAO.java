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
import org.wso2.carbon.identity.certificate.management.dao.CertificateManagementDAO;
import org.wso2.carbon.identity.certificate.management.exception.CertificateMgtException;
import org.wso2.carbon.identity.certificate.management.model.Certificate;

/**
 * Cache backed Certificate Management DAO.
 */
public class CacheBackedCertificateMgtDAO implements CertificateManagementDAO {

    private static final Log LOG = LogFactory.getLog(CacheBackedCertificateMgtDAO.class);
    private final CertificateCacheById certificateCacheById;
    private final CertificateManagementDAO certificateManagementDAO;

    public CacheBackedCertificateMgtDAO(CertificateManagementDAO certificateManagementDAO) {

        this.certificateManagementDAO = certificateManagementDAO;
        certificateCacheById = CertificateCacheById.getInstance();
    }

    @Override
    public void addCertificate(String certificateId, Certificate certificate, int tenantId)
            throws CertificateMgtException {

        certificateManagementDAO.addCertificate(certificateId, certificate, tenantId);
    }

    @Override
    public Certificate getCertificate(String certificateId, int tenantId) throws CertificateMgtException {

        CertificateIdCacheKey cacheKey = new CertificateIdCacheKey(certificateId);
        CertificateCacheEntry entry = certificateCacheById.getValueFromCache(cacheKey, tenantId);

        if (entry != null) {
            LOG.debug("Cache entry found for Certificate id: " + certificateId);
            return entry.getCertificate();
        }

        LOG.debug("Cache entry not found for Certificate id: " + certificateId + ". Fetching entry from DB.");

        Certificate certificate = certificateManagementDAO.getCertificate(certificateId, tenantId);

        if (certificate != null) {
            LOG.debug("Entry fetched from DB for Certificate id: " + certificateId + ". Updating cache.");
            certificateCacheById.addToCache(cacheKey, new CertificateCacheEntry(certificate), tenantId);
        } else {
            LOG.debug("Entry for Certificate id: " + certificateId + " not found in cache or DB.");
        }
        return certificate;
    }

    @Override
    public void updateCertificateContent(String certificateId, String certificateContent, int tenantId)
            throws CertificateMgtException {

        certificateCacheById.clearCacheEntry(new CertificateIdCacheKey(certificateId), tenantId);
        certificateManagementDAO.updateCertificateContent(certificateId, certificateContent, tenantId);
    }

    @Override
    public void deleteCertificate(String certificateId, int tenantId) throws CertificateMgtException {

        certificateCacheById.clearCacheEntry(new CertificateIdCacheKey(certificateId), tenantId);
        certificateManagementDAO.deleteCertificate(certificateId, tenantId);
    }
}
