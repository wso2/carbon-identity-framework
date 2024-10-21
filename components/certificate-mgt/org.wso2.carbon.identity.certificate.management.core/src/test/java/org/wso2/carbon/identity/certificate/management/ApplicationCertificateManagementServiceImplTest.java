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
import org.wso2.carbon.identity.certificate.management.core.ApplicationCertificateManagementService;
import org.wso2.carbon.identity.certificate.management.core.ApplicationCertificateManagementServiceImpl;
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

/**
 * This class is a test suite for the ApplicationCertificateManagementServiceImpl class.
 * It contains unit tests to verify the functionality of the methods
 * in the ApplicationCertificateManagementServiceImpl class.
 */
@WithAxisConfiguration
@WithCarbonHome
@WithRealmService(injectToSingletons = {IdentityCoreServiceDataHolder.class})
public class ApplicationCertificateManagementServiceImplTest {

    private static final String DB_NAME = "application_certificate_mgt";

    private int certificateIntId;
    private String tenantDomain;
    private DataSource dataSource;
    private MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil;
    private ApplicationCertificateManagementService applicationCertificateManagementService;

    @BeforeClass
    public void setUpClass() throws Exception {

        applicationCertificateManagementService = ApplicationCertificateManagementServiceImpl.getInstance();
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
        certificateIntId = applicationCertificateManagementService.addCertificate(creatingCertificate, tenantDomain);
        Assert.assertNotEquals(certificateIntId, 0);
    }

    @Test(priority = 2)
    public void testGetCertificate() throws CertificateMgtException {

        Certificate certificate = applicationCertificateManagementService.getCertificate(certificateIntId,
                tenantDomain);
        Assert.assertNotNull(certificate);
        Assert.assertEquals(certificate.getId(), String.valueOf(certificateIntId));
        Assert.assertEquals(certificate.getName(), CERTIFICATE_NAME);
        Assert.assertEquals(certificate.getCertificate(), CERTIFICATE);
    }

    @Test(priority = 3)
    public void testGetCertificateByName() throws CertificateMgtException {

        Certificate certificate = applicationCertificateManagementService.getCertificateByName(CERTIFICATE_NAME,
                tenantDomain);
        Assert.assertNotNull(certificate);
        Assert.assertEquals(certificate.getId(), String.valueOf(certificateIntId));
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

    @Test(priority = 4, dataProvider = "invalidCertificateDataProvider",
            expectedExceptions = CertificateMgtClientException.class)
    public void testAddInvalidCertificate(String certificateName, String certificateContent)
            throws CertificateMgtException {

        Certificate creatingCertificate = new Certificate.Builder()
                .name(certificateName)
                .certificate(certificateContent)
                .build();
        applicationCertificateManagementService.addCertificate(creatingCertificate, tenantDomain);
    }

    @Test(priority = 5, expectedExceptions = CertificateMgtException.class,
            expectedExceptionsMessageRegExp = "Unable to perform the operation.")
    public void testGetCertificateWithInvalidId() throws CertificateMgtException {

        applicationCertificateManagementService.getCertificate(100, tenantDomain);
    }

    @Test(priority = 6, expectedExceptions = CertificateMgtException.class,
            expectedExceptionsMessageRegExp = "Unable to perform the operation.")
    public void testGetCertificateWithInvalidName() throws CertificateMgtException {

        applicationCertificateManagementService.getCertificateByName("Invalid_name", tenantDomain);
    }

    @Test(priority = 7)
    public void testUpdateCertificateWithId() throws CertificateMgtException {

        applicationCertificateManagementService.updateCertificateContent(certificateIntId, UPDATED_CERTIFICATE,
                tenantDomain);

        Certificate updatedCertificate = applicationCertificateManagementService.getCertificate(certificateIntId,
                tenantDomain);
        Assert.assertEquals(updatedCertificate.getId(), String.valueOf(certificateIntId));
        Assert.assertEquals(updatedCertificate.getName(), CERTIFICATE_NAME);
        Assert.assertEquals(updatedCertificate.getCertificate(), UPDATED_CERTIFICATE);
    }

    @Test(priority = 8, expectedExceptions = CertificateMgtException.class,
            expectedExceptionsMessageRegExp = "Unable to perform the operation.")
    public void testUpdateCertificateWithInvalidIntId() throws CertificateMgtException {

        applicationCertificateManagementService.updateCertificateContent(100, CERTIFICATE, tenantDomain);
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

    @Test(priority = 9, dataProvider = "invalidCertificateContentDataProvider",
            expectedExceptions = CertificateMgtClientException.class)
    public void testUpdateInvalidCertificateWithIntId(String certificateContent)
            throws CertificateMgtException {

        applicationCertificateManagementService.updateCertificateContent(certificateIntId, certificateContent,
                tenantDomain);
    }

    @Test(priority = 10, expectedExceptions = CertificateMgtException.class,
            expectedExceptionsMessageRegExp = "Unable to perform the operation.")
    public void testDeleteCertificateWithIntId() throws CertificateMgtException {

        applicationCertificateManagementService.deleteCertificate(certificateIntId, tenantDomain);
        applicationCertificateManagementService.getCertificate(certificateIntId, tenantDomain);
    }

    private void mockDBConnection() throws SQLException {

        when(dataSource.getConnection()).thenAnswer(invocation -> spy(TestUtil.getConnection(DB_NAME)));
    }
}
