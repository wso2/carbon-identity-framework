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
import org.wso2.carbon.identity.certificate.management.dao.impl.CacheBackedApplicationCertificateMgtDAO;
import org.wso2.carbon.identity.certificate.management.exception.CertificateMgtException;
import org.wso2.carbon.identity.certificate.management.model.Certificate;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.core.internal.component.IdentityCoreServiceDataHolder;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.CERTIFICATE_NAME;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.ENCODED_CERTIFICATE;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.TEST_ID;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.TEST_OTHER_ID;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.TEST_TENANT_ID;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.TEST_UUID;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.UPDATED_ENCODED_CERTIFICATE;

/**
 * Test class for CacheBackedCertificateMgtDAO.
 */
@WithCarbonHome
@WithRealmService(injectToSingletons = {IdentityCoreServiceDataHolder.class})
public class CacheBackedApplicationCertificateMgtDAOTest {

    private CacheBackedApplicationCertificateMgtDAO cacheBackedApplicationCertificateMgtDAO;
    private ApplicationCertificateManagementDAO applicationCertificateManagementDAO;
    private Certificate certificate;
    private int certificateId;

    @BeforeMethod
    public void setUpMethod() {

        applicationCertificateManagementDAO = mock(CacheBackedApplicationCertificateMgtDAO.class);
        cacheBackedApplicationCertificateMgtDAO =
                new CacheBackedApplicationCertificateMgtDAO(applicationCertificateManagementDAO);
    }

    @AfterMethod
    public void tearDownMethod() {

        applicationCertificateManagementDAO = null;
        cacheBackedApplicationCertificateMgtDAO = null;
    }

    @Test(priority = 1)
    public void testAddCertificate() throws CertificateMgtException {

        certificate = new Certificate.Builder()
                .id(TEST_UUID)
                .name(CERTIFICATE_NAME)
                .certificateContent(ENCODED_CERTIFICATE)
                .build();
        when(applicationCertificateManagementDAO.addCertificate(TEST_UUID, certificate, TEST_TENANT_ID))
                .thenReturn(TEST_ID);
        certificateId = cacheBackedApplicationCertificateMgtDAO.addCertificate(TEST_UUID, certificate, TEST_TENANT_ID);

        Assert.assertEquals(certificateId, TEST_ID);
        verify(applicationCertificateManagementDAO, times(1)).addCertificate(TEST_UUID,
                certificate, TEST_TENANT_ID);
    }

    @Test(priority = 2, dependsOnMethods = "testAddCertificate")
    public void testGetCertificate_FromDB() throws CertificateMgtException {

        when(applicationCertificateManagementDAO.getCertificate(certificateId, TEST_TENANT_ID)).thenReturn(certificate);
        Certificate resultFromDB = cacheBackedApplicationCertificateMgtDAO.getCertificate(certificateId,
                TEST_TENANT_ID);

        Assert.assertEquals(resultFromDB.getId(), certificate.getId());
        Assert.assertEquals(resultFromDB.getName(), certificate.getName());
        Assert.assertEquals(resultFromDB.getCertificateContent(), certificate.getCertificateContent());
        verify(applicationCertificateManagementDAO, times(1)).getCertificate(certificateId,
                TEST_TENANT_ID);
    }

    @Test(priority = 3, dependsOnMethods = "testGetCertificate_FromDB")
    public void testGetCertificate_FromCache() throws CertificateMgtException {

        // Fetch from cache.
        Certificate resultFromCache = cacheBackedApplicationCertificateMgtDAO.getCertificate(certificateId,
                TEST_TENANT_ID);

        Assert.assertEquals(resultFromCache.getId(), certificate.getId());
        Assert.assertEquals(resultFromCache.getName(), certificate.getName());
        Assert.assertEquals(resultFromCache.getCertificateContent(), certificate.getCertificateContent());
        verify(applicationCertificateManagementDAO, never()).getCertificate(certificateId, TEST_TENANT_ID);
    }

    @Test(priority = 4)
    public void testGetCertificate_NotFoundInCacheOrDB() throws CertificateMgtException {

        when(applicationCertificateManagementDAO.getCertificate(TEST_OTHER_ID, TEST_TENANT_ID)).thenReturn(null);
        Certificate result = cacheBackedApplicationCertificateMgtDAO.getCertificate(TEST_OTHER_ID, TEST_TENANT_ID);

        Assert.assertNull(result);
    }

    @Test(priority = 5, dependsOnMethods = "testGetCertificate_FromCache")
    public void testUpdateCertificateContent() throws CertificateMgtException {

        // Update the certificate and invalidate the cache.
        cacheBackedApplicationCertificateMgtDAO.updateCertificateContent(certificateId, UPDATED_ENCODED_CERTIFICATE,
                TEST_TENANT_ID);
        // Fetch again to verify the cache is invalidated.
        cacheBackedApplicationCertificateMgtDAO.getCertificate(certificateId, TEST_TENANT_ID);

        verify(applicationCertificateManagementDAO, times(1))
                .updateCertificateContent(certificateId, UPDATED_ENCODED_CERTIFICATE, TEST_TENANT_ID);
        verify(applicationCertificateManagementDAO, times(1)).getCertificate(certificateId,
                TEST_TENANT_ID);
    }

    @Test(priority = 6, dependsOnMethods = "testUpdateCertificateContent")
    public void testDeleteCertificate() throws CertificateMgtException {

        // Delete the certificate and invalidate the cache.
        cacheBackedApplicationCertificateMgtDAO.deleteCertificate(certificateId, TEST_TENANT_ID);
        // Fetch again to verify the cache is invalidated.
        when(applicationCertificateManagementDAO.getCertificate(certificateId, TEST_TENANT_ID)).thenReturn(null);
        cacheBackedApplicationCertificateMgtDAO.getCertificate(certificateId, TEST_TENANT_ID);

        verify(applicationCertificateManagementDAO, times(1)).deleteCertificate(certificateId,
                TEST_TENANT_ID);
        verify(applicationCertificateManagementDAO, times(1)).getCertificate(certificateId,
                TEST_TENANT_ID);
    }
}
