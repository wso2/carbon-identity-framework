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
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.certificate.management.core.CertificateManagementService;
import org.wso2.carbon.identity.certificate.management.core.CertificateManagementServiceImpl;
import org.wso2.carbon.identity.certificate.management.core.exception.CertificateMgtClientException;
import org.wso2.carbon.identity.certificate.management.core.exception.CertificateMgtException;
import org.wso2.carbon.identity.certificate.management.core.model.Certificate;
import org.wso2.carbon.identity.certificate.management.util.TestUtil;
import org.wso2.carbon.identity.common.testng.WithAxisConfiguration;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.core.internal.IdentityCoreServiceDataHolder;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;

import java.sql.SQLException;

import javax.sql.DataSource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.CERTIFICATE;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.CERTIFICATE_NAME;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.CERTIFICATE_WITHOUT_BEGIN_END_MARKERS;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.INVALID_CERTIFICATE;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.UPDATED_CERTIFICATE;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.UPDATED_CERTIFICATE_NAME;

/**
 * This class is a test suite for the CertificateManagementServiceImpl class.
 * It contains unit tests to verify the functionality of the methods
 * in the CertificateManagementServiceImpl class.
 */
@WithAxisConfiguration
@WithCarbonHome
@WithRealmService(injectToSingletons = {IdentityCoreServiceDataHolder.class})
public class CertificateManagementServiceImplTest {

    private static final String DB_NAME = "certificate_mgt";

    private String certificateId;
    private int certificateIntId;
    private String tenantDomain;
    private DataSource dataSource;
    private MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil;
    private CertificateManagementService certificateManagementService;

    @BeforeClass
    public void setUpClass() throws Exception {

        certificateManagementService = CertificateManagementServiceImpl.getInstance();
        tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        certificateIntId = 0;
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
    public void tearDown() throws SQLException {

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
    public Object[][] invalidCertificateDataProvider() {

        return new Object[][]{
                {"", CERTIFICATE},
                {"   ", CERTIFICATE},
                {CERTIFICATE_NAME, ""},
                {CERTIFICATE_NAME, " "},
                {CERTIFICATE_NAME, CERTIFICATE_WITHOUT_BEGIN_END_MARKERS},
                {CERTIFICATE_NAME, INVALID_CERTIFICATE},
                {" ", INVALID_CERTIFICATE}
        };
    }

    @Test(priority = 3, dataProvider = "invalidCertificateDataProvider",
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

        Certificate certificate = certificateManagementService.getCertificate("invalid_id", tenantDomain);
    }

    @Test(priority = 5)
    public void testUpdateCertificate() throws CertificateMgtException, SQLException {

        Certificate updatingCertificate = new Certificate.Builder()
                .name(UPDATED_CERTIFICATE_NAME)
                .certificate(UPDATED_CERTIFICATE)
                .build();
        certificateManagementService.updateCertificate(certificateId, updatingCertificate, tenantDomain);

        Certificate updatedCertificate = certificateManagementService.getCertificate(certificateId, tenantDomain);
        Assert.assertEquals(updatedCertificate.getId(), certificateId);
        Assert.assertEquals(updatedCertificate.getName(), UPDATED_CERTIFICATE_NAME);
        Assert.assertEquals(updatedCertificate.getCertificate(), UPDATED_CERTIFICATE);
    }

    @Test(priority = 6)
    public void testUpdateCertificateNameOnly() throws CertificateMgtException {

        Certificate updatingCertificate = new Certificate.Builder()
                .name(CERTIFICATE_NAME)
                .build();
        certificateManagementService.updateCertificate(certificateId, updatingCertificate, tenantDomain);

        Certificate updatedCertificate = certificateManagementService.getCertificate(certificateId, tenantDomain);
        Assert.assertEquals(updatedCertificate.getId(), certificateId);
        Assert.assertEquals(updatedCertificate.getName(), CERTIFICATE_NAME);
        Assert.assertEquals(updatedCertificate.getCertificate(), UPDATED_CERTIFICATE);
    }

    @Test(priority = 7)
    public void testUpdateCertificateContentOnly() throws CertificateMgtException {

        Certificate updatingCertificate = new Certificate.Builder()
                .certificate(CERTIFICATE)
                .build();
        certificateManagementService.updateCertificate(certificateId, updatingCertificate, tenantDomain);

        Certificate updatedCertificate = certificateManagementService.getCertificate(certificateId, tenantDomain);
        Assert.assertEquals(updatedCertificate.getId(), certificateId);
        Assert.assertEquals(updatedCertificate.getName(), CERTIFICATE_NAME);
        Assert.assertEquals(updatedCertificate.getCertificate(), CERTIFICATE);
    }

    @Test(priority = 8, expectedExceptions = CertificateMgtException.class,
            expectedExceptionsMessageRegExp = "Unable to perform the operation.")
    public void testUpdateCertificateWithInvalidId() throws CertificateMgtException {

        Certificate updatingCertificate = new Certificate.Builder()
                .name(UPDATED_CERTIFICATE_NAME)
                .certificate(UPDATED_CERTIFICATE)
                .build();
        certificateManagementService.updateCertificate("invalid_id", updatingCertificate, tenantDomain);
    }

    @Test(priority = 9, dataProvider = "invalidCertificateDataProvider",
            expectedExceptions = CertificateMgtClientException.class)
    public void testUpdateInvalidCertificate(String certificateName, String certificateContent)
            throws CertificateMgtException {

        Certificate updatingCertificate = new Certificate.Builder()
                .name(certificateName)
                .certificate(certificateContent)
                .build();
        certificateManagementService.updateCertificate(certificateId, updatingCertificate, tenantDomain);
    }

    @Test(priority = 10, expectedExceptions = CertificateMgtException.class,
            expectedExceptionsMessageRegExp = "Unable to perform the operation.")
    public void testDeleteCertificate() throws CertificateMgtException, SQLException {

        certificateManagementService.deleteCertificate(certificateId, tenantDomain);
        certificateManagementService.getCertificate(certificateId, tenantDomain);
    }

    @Test(priority = 11)
    public void testAddCertificateWithIntId() throws CertificateMgtException {

        Certificate creatingCertificate = new Certificate.Builder()
                .name(CERTIFICATE_NAME)
                .certificate(CERTIFICATE)
                .build();
        certificateIntId = certificateManagementService.addCert(creatingCertificate, tenantDomain);
        Assert.assertNotEquals(certificateIntId, 0);
    }

    @Test(priority = 12)
    public void testGetCertificateWithIntId() throws CertificateMgtException {

        Certificate certificate = certificateManagementService.getCertificate(certificateIntId, tenantDomain);
        Assert.assertNotNull(certificate);
        Assert.assertEquals(certificate.getId(), String.valueOf(certificateIntId));
        Assert.assertEquals(certificate.getName(), CERTIFICATE_NAME);
        Assert.assertEquals(certificate.getCertificate(), CERTIFICATE);
    }

    @Test(priority = 13)
    public void testGetCertificateByNameWithIntId() throws CertificateMgtException {

        Certificate certificate = certificateManagementService.getCertificateByName(CERTIFICATE_NAME, tenantDomain);
        Assert.assertNotNull(certificate);
        Assert.assertEquals(certificate.getId(), String.valueOf(certificateIntId));
        Assert.assertEquals(certificate.getName(), CERTIFICATE_NAME);
        Assert.assertEquals(certificate.getCertificate(), CERTIFICATE);
    }

    @Test(priority = 14, dataProvider = "invalidCertificateDataProvider",
            expectedExceptions = CertificateMgtClientException.class)
    public void testAddInvalidCertificateWithIntId(String certificateName, String certificateContent)
            throws CertificateMgtException {

        Certificate creatingCertificate = new Certificate.Builder()
                .name(certificateName)
                .certificate(certificateContent)
                .build();
        certificateManagementService.addCert(creatingCertificate, tenantDomain);
    }

    @Test(priority = 15, expectedExceptions = CertificateMgtException.class,
            expectedExceptionsMessageRegExp = "Unable to perform the operation.")
    public void testGetCertificateWithInvalidIntId() throws CertificateMgtException {

        certificateManagementService.getCertificate(100, tenantDomain);
    }

    @Test(priority = 16, expectedExceptions = CertificateMgtException.class,
            expectedExceptionsMessageRegExp = "Unable to perform the operation.")
    public void testGetCertificateWithInvalidName() throws CertificateMgtException {

        certificateManagementService.getCertificateByName("Invalid_name", tenantDomain);
    }

    @Test(priority = 17)
    public void testUpdateCertificateWithIntId() throws CertificateMgtException, SQLException {

        certificateManagementService.updateCertificateContent(certificateIntId, UPDATED_CERTIFICATE, tenantDomain);

        Certificate updatedCertificate = certificateManagementService.getCertificate(certificateIntId, tenantDomain);
        Assert.assertEquals(updatedCertificate.getId(), String.valueOf(certificateIntId));
        Assert.assertEquals(updatedCertificate.getName(), CERTIFICATE_NAME);
        Assert.assertEquals(updatedCertificate.getCertificate(), UPDATED_CERTIFICATE);
    }

    @Test(priority = 18, expectedExceptions = CertificateMgtException.class,
            expectedExceptionsMessageRegExp = "Unable to perform the operation.")
    public void testUpdateCertificateWithInvalidIntId() throws CertificateMgtException {

        certificateManagementService.updateCertificateContent(100, CERTIFICATE, tenantDomain);
    }

    @DataProvider
    public Object[][] invalidCertificateContentDataProvider() {

        return new Object[][]{
                {""},
                {" "},
                {CERTIFICATE_WITHOUT_BEGIN_END_MARKERS},
                {INVALID_CERTIFICATE}
        };
    }

    @Test(priority = 19, dataProvider = "invalidCertificateContentDataProvider",
            expectedExceptions = CertificateMgtClientException.class)
    public void testUpdateInvalidCertificateWithIntId(String certificateContent)
            throws CertificateMgtException {

        certificateManagementService.updateCertificateContent(certificateIntId, certificateContent, tenantDomain);
    }

    @Test(priority = 20, expectedExceptions = CertificateMgtException.class,
            expectedExceptionsMessageRegExp = "Unable to perform the operation.")
    public void testDeleteCertificateWithIntId() throws CertificateMgtException, SQLException {

        certificateManagementService.deleteCertificate(certificateIntId, tenantDomain);
        certificateManagementService.getCertificate(certificateIntId, tenantDomain);
    }

    private void mockDBConnection() throws SQLException {

        when(dataSource.getConnection()).thenAnswer(invocation -> spy(TestUtil.getConnection(DB_NAME)));
    }
}
