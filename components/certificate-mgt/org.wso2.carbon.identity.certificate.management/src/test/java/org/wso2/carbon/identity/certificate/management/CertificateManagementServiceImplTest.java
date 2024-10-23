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

package org.wso2.carbon.identity.certificate.management;

import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.certificate.management.exception.CertificateMgtClientException;
import org.wso2.carbon.identity.certificate.management.exception.CertificateMgtException;
import org.wso2.carbon.identity.certificate.management.model.Certificate;
import org.wso2.carbon.identity.certificate.management.util.TestUtil;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.core.internal.IdentityCoreServiceDataHolder;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;

import java.sql.SQLException;

import javax.sql.DataSource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.CERTIFICATE;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.CERTIFICATE_NAME;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.CERTIFICATE_WITHOUT_BEGIN_END_MARKERS;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.CERTIFICATE_WITHOUT_BEGIN_MARKER;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.CERTIFICATE_WITHOUT_END_MARKER;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.INVALID_CERTIFICATE;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.TEST_OTHER_UUID;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.UPDATED_CERTIFICATE;

/**
 * This class is a test suite for the CertificateManagementServiceImpl class.
 * It contains unit tests to verify the functionality of the methods
 * in the CertificateManagementServiceImpl class.
 */
@WithCarbonHome
@WithRealmService(injectToSingletons = {IdentityCoreServiceDataHolder.class})
public class CertificateManagementServiceImplTest {

    private static final String DB_NAME = "certificate_mgt";

    private String certificateId;
    private String tenantDomain;
    private DataSource dataSource;
    private MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil;
    private CertificateManagementService certificateManagementService;

    @BeforeClass
    public void setUpClass() throws Exception {

        certificateManagementService = CertificateManagementServiceImpl.getInstance();
        tenantDomain = "carbon.super";
        TestUtil.initiateH2Database(DB_NAME);
    }

    @BeforeMethod
    public void setUp() throws SQLException {

        dataSource = mock(DataSource.class);
        identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
        identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSource);
        mockDBConnection();
    }

    @AfterMethod
    public void tearDown() {

        identityDatabaseUtil.close();
    }

    @AfterClass
    public void wrapUp() throws Exception {

        TestUtil.closeH2Database(DB_NAME);
    }

    @Test(priority = 1)
    public void testAddCertificate() throws CertificateMgtException {

        Certificate creatingCertificate = new Certificate.Builder()
                .name(CERTIFICATE_NAME)
                .certificate(CERTIFICATE)
                .build();
        certificateId = certificateManagementService.addCertificate(creatingCertificate, tenantDomain);
        Assert.assertNotNull(certificateId);
    }

    @Test(priority = 2)
    public void testGetCertificate() throws CertificateMgtException {

        Certificate certificate = certificateManagementService.getCertificate(certificateId, tenantDomain);
        Assert.assertNotNull(certificate);
        Assert.assertEquals(certificate.getId(), certificateId);
        Assert.assertEquals(certificate.getName(), CERTIFICATE_NAME);
        Assert.assertEquals(certificate.getCertificate(), CERTIFICATE);
    }

    @DataProvider
    public String[][] invalidDataProvider() {

        return new String[][]{
                {"", CERTIFICATE},
                {"   ", CERTIFICATE},
                {CERTIFICATE_NAME, ""},
                {CERTIFICATE_NAME, " "},
                {CERTIFICATE_NAME, CERTIFICATE_WITHOUT_BEGIN_END_MARKERS},
                {CERTIFICATE_NAME, INVALID_CERTIFICATE},
                {" ", INVALID_CERTIFICATE}
        };
    }

    @Test(priority = 3, dataProvider = "invalidDataProvider",
            expectedExceptions = CertificateMgtClientException.class)
    public void testAddInvalidCertificate(String certificateName, String certificateContent)
            throws CertificateMgtException {

        Certificate creatingCertificate = new Certificate.Builder()
                .name(certificateName)
                .certificate(certificateContent)
                .build();
        certificateManagementService.addCertificate(creatingCertificate, tenantDomain);
    }

    @Test(priority = 4, expectedExceptions = CertificateMgtException.class,
            expectedExceptionsMessageRegExp = "Unable to perform the operation.")
    public void testGetCertificateWithInvalidId() throws CertificateMgtException {

        certificateManagementService.getCertificate(TEST_OTHER_UUID, tenantDomain);
    }

    @Test(priority = 5)
    public void testUpdateCertificateContent() throws CertificateMgtException {

        certificateManagementService.updateCertificateContent(certificateId, UPDATED_CERTIFICATE, tenantDomain);

        Certificate updatedCertificate = certificateManagementService.getCertificate(certificateId, tenantDomain);
        Assert.assertEquals(updatedCertificate.getId(), certificateId);
        Assert.assertEquals(updatedCertificate.getName(), CERTIFICATE_NAME);
        Assert.assertEquals(updatedCertificate.getCertificate(), UPDATED_CERTIFICATE);
    }

    @Test(priority = 6, expectedExceptions = CertificateMgtException.class,
            expectedExceptionsMessageRegExp = "Unable to perform the operation.")
    public void testUpdateCertificateWithInvalidId() throws CertificateMgtException {

        certificateManagementService.updateCertificateContent(TEST_OTHER_UUID, CERTIFICATE, tenantDomain);
    }

    @DataProvider
    public String[][] invalidCertificateContentDataProvider() {

        return new String[][]{
                {CERTIFICATE_WITHOUT_BEGIN_END_MARKERS},
                {CERTIFICATE_WITHOUT_BEGIN_MARKER},
                {CERTIFICATE_WITHOUT_END_MARKER},
                {INVALID_CERTIFICATE}
        };
    }

    @Test(priority = 7, dataProvider = "invalidCertificateContentDataProvider",
            expectedExceptions = CertificateMgtClientException.class)
    public void testUpdateInvalidCertificate(String certificateContent) throws CertificateMgtException {

        certificateManagementService.updateCertificateContent(certificateId, certificateContent, tenantDomain);
    }

    @Test(priority = 8, expectedExceptions = CertificateMgtException.class,
            expectedExceptionsMessageRegExp = "Unable to perform the operation.")
    public void testDeleteCertificate() throws CertificateMgtException {

        certificateManagementService.deleteCertificate(certificateId, tenantDomain);
        certificateManagementService.getCertificate(certificateId, tenantDomain);
    }

    private void mockDBConnection() throws SQLException {

        when(dataSource.getConnection()).thenAnswer(invocation -> TestUtil.getConnection(DB_NAME));
    }
}
