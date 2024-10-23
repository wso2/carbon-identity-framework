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

package org.wso2.carbon.identity.certificate.management.dao;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.certificate.management.core.dao.CertificateManagementDAO;
import org.wso2.carbon.identity.certificate.management.core.dao.impl.CacheBackedCertificateMgtDAO;
import org.wso2.carbon.identity.certificate.management.core.exception.CertificateMgtException;
import org.wso2.carbon.identity.certificate.management.core.model.Certificate;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.core.internal.IdentityCoreServiceDataHolder;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.CERTIFICATE;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.CERTIFICATE_NAME;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.TEST_OTHER_UUID;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.TEST_TENANT_ID;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.TEST_UUID;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.UPDATED_CERTIFICATE;

/**
 * Test class for CacheBackedCertificateMgtDAO.
 */
@WithCarbonHome
@WithRealmService(injectToSingletons = {IdentityCoreServiceDataHolder.class})
public class CacheBackedCertificateMgtDAOTest {

    private CacheBackedCertificateMgtDAO cacheBackedCertificateMgtDAO;
    private CertificateManagementDAO certificateManagementDAO;
    private Certificate certificate;

    @BeforeMethod
    public void setUpMethod() {

        certificateManagementDAO = mock(CertificateManagementDAO.class);
        cacheBackedCertificateMgtDAO = new CacheBackedCertificateMgtDAO(certificateManagementDAO);
    }

    @AfterMethod
    public void tearDownMethod() {

        certificateManagementDAO = null;
        cacheBackedCertificateMgtDAO = null;
    }

    @Test(priority = 1)
    public void testAddCertificate() throws CertificateMgtException {

        certificate = new Certificate.Builder()
                .id(TEST_UUID)
                .name(CERTIFICATE_NAME)
                .certificate(CERTIFICATE)
                .build();
        cacheBackedCertificateMgtDAO.addCertificate(TEST_UUID, certificate, TEST_TENANT_ID);
        verify(certificateManagementDAO, times(1)).addCertificate(TEST_UUID,
                certificate, TEST_TENANT_ID);
    }

    @Test(priority = 2, dependsOnMethods = "testAddCertificate")
    public void testGetCertificate_FromDB() throws CertificateMgtException {

        when(certificateManagementDAO.getCertificate(TEST_UUID, TEST_TENANT_ID)).thenReturn(certificate);
        Certificate resultFromDB = cacheBackedCertificateMgtDAO.getCertificate(TEST_UUID, TEST_TENANT_ID);

        Assert.assertEquals(certificate.getId(), resultFromDB.getId());
        Assert.assertEquals(certificate.getName(), resultFromDB.getName());
        Assert.assertEquals(certificate.getCertificate(), resultFromDB.getCertificate());
        verify(certificateManagementDAO, times(1)).getCertificate(TEST_UUID,
                TEST_TENANT_ID);
    }

    @Test(priority = 3, dependsOnMethods = "testGetCertificate_FromDB")
    public void testGetCertificate_FromCache() throws CertificateMgtException {

        // Fetch from cache.
        Certificate resultFromCache = cacheBackedCertificateMgtDAO.getCertificate(TEST_UUID, TEST_TENANT_ID);

        Assert.assertEquals(certificate.getId(), resultFromCache.getId());
        Assert.assertEquals(certificate.getName(), resultFromCache.getName());
        Assert.assertEquals(certificate.getCertificate(), resultFromCache.getCertificate());
        verify(certificateManagementDAO, never()).getCertificate(TEST_UUID, TEST_TENANT_ID);
    }

    @Test(priority = 4)
    public void testGetCertificate_NotFoundInCacheOrDB() throws CertificateMgtException {

        when(certificateManagementDAO.getCertificate(TEST_OTHER_UUID, TEST_TENANT_ID)).thenReturn(null);
        Certificate result = cacheBackedCertificateMgtDAO.getCertificate(TEST_OTHER_UUID, TEST_TENANT_ID);

        Assert.assertNull(result);
    }

    @Test(priority = 5, dependsOnMethods = "testGetCertificate_FromCache")
    public void testUpdateCertificateContent() throws CertificateMgtException {

        // Update the certificate and invalidate the cache.
        cacheBackedCertificateMgtDAO.updateCertificateContent(TEST_UUID, UPDATED_CERTIFICATE,
                TEST_TENANT_ID);
        // Fetch again to verify the cache is invalidated.
        cacheBackedCertificateMgtDAO.getCertificate(TEST_UUID, TEST_TENANT_ID);

        verify(certificateManagementDAO, times(1)).updateCertificateContent(TEST_UUID,
                UPDATED_CERTIFICATE, TEST_TENANT_ID);
        verify(certificateManagementDAO, times(1)).getCertificate(TEST_UUID,
                TEST_TENANT_ID);
    }

    @Test(priority = 6, dependsOnMethods = "testUpdateCertificateContent")
    public void testDeleteCertificate() throws CertificateMgtException {

        // Delete the certificate and invalidate the cache.
        cacheBackedCertificateMgtDAO.deleteCertificate(TEST_UUID, TEST_TENANT_ID);
        // Fetch again to verify the cache is invalidated.
        when(certificateManagementDAO.getCertificate(TEST_UUID, TEST_TENANT_ID)).thenReturn(null);
        cacheBackedCertificateMgtDAO.getCertificate(TEST_UUID, TEST_TENANT_ID);

        verify(certificateManagementDAO, times(1)).deleteCertificate(TEST_UUID,
                TEST_TENANT_ID);
        verify(certificateManagementDAO, times(1)).getCertificate(TEST_UUID,
                TEST_TENANT_ID);
    }
}
