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

import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.certificate.management.dao.impl.ApplicationCertificateManagementDAOImpl;
import org.wso2.carbon.identity.certificate.management.exception.CertificateMgtException;
import org.wso2.carbon.identity.certificate.management.model.Certificate;
import org.wso2.carbon.identity.certificate.management.util.TestUtil;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import java.sql.SQLException;
import java.util.UUID;

import javax.sql.DataSource;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.CERTIFICATE;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.CERTIFICATE_NAME;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.UPDATED_CERTIFICATE;

/**
 * This class is a test suite for the ApplicationCertificateManagementDAOImpl class.
 * It contains unit tests to verify the functionality of the methods
 * in the ApplicationCertificateManagementDAOImpl class.
 */
public class ApplicationCertificateManagementDAOImplTest {

    private static final String DB_NAME = "application_certificate_mgt_dao";
    private static final int TENANT_ID = 2;

    private String certificateUUID;
    private int certificateID;
    private DataSource dataSource;
    private MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil;
    private MockedStatic<IdentityTenantUtil> identityTenantUtil;
    private ApplicationCertificateManagementDAOImpl applicationCertificateManagementDAO;

    @BeforeClass
    public void setUpClass() throws SQLException {

        applicationCertificateManagementDAO = new ApplicationCertificateManagementDAOImpl();
        certificateUUID = String.valueOf(UUID.randomUUID());
        TestUtil.initiateH2Database(DB_NAME);
    }

    @BeforeMethod
    public void setUp() throws SQLException {

        identityTenantUtil = mockStatic(IdentityTenantUtil.class);
        dataSource = mock(DataSource.class);
        identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
        identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSource);
        mockDBConnection();
        identityTenantUtil.when(()-> IdentityTenantUtil.getTenantId(anyString())).thenReturn(TENANT_ID);
    }

    @AfterMethod
    public void tearDown() throws Exception {

        identityDatabaseUtil.close();
        identityTenantUtil.close();
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
        certificateID = applicationCertificateManagementDAO.addCertificate(certificateUUID, creatingCertificate,
                TENANT_ID);
        Assert.assertEquals(certificateID, 1);
    }

    @Test(priority = 2)
    public void testGetCertificate() throws CertificateMgtException {

        Certificate certificate = applicationCertificateManagementDAO.getCertificate(certificateID, TENANT_ID);
        Assert.assertNotNull(certificate);
        Assert.assertEquals(certificate.getId(), String.valueOf(certificateID));
        Assert.assertEquals(certificate.getName(), CERTIFICATE_NAME);
        Assert.assertEquals(certificate.getCertificate(), CERTIFICATE);
    }

    @Test(priority = 3)
    public void testGetCertificateByName() throws CertificateMgtException {

        Certificate certificate = applicationCertificateManagementDAO.getCertificateByName(CERTIFICATE_NAME, TENANT_ID);
        Assert.assertNotNull(certificate);
        Assert.assertEquals(certificate.getId(), String.valueOf(certificateID));
        Assert.assertEquals(certificate.getName(), CERTIFICATE_NAME);
        Assert.assertEquals(certificate.getCertificate(), CERTIFICATE);
    }

    @Test(priority = 4, expectedExceptions = CertificateMgtException.class,
            expectedExceptionsMessageRegExp = "Error while adding Certificate.")
    public void testAddInvalidCertificate() throws CertificateMgtException {

        // Adding a certificate to the same tenant with the same name to generate unique key constraint violation.
        String otherCertificateUUID = String.valueOf(UUID.randomUUID());
        Certificate creatingCertificate = new Certificate.Builder()
                .name(CERTIFICATE_NAME)
                .certificate(CERTIFICATE)
                .build();

        applicationCertificateManagementDAO.addCertificate(otherCertificateUUID, creatingCertificate, TENANT_ID);
    }

    @Test(priority = 5)
    public void testUpdateCertificate() throws CertificateMgtException, SQLException {

        applicationCertificateManagementDAO.updateCertificateContent(certificateID, UPDATED_CERTIFICATE, TENANT_ID);
        mockDBConnection();
        Certificate certificate = applicationCertificateManagementDAO.getCertificate(certificateID, TENANT_ID);
        Assert.assertEquals(certificate.getId(), String.valueOf(certificateID));
        Assert.assertEquals(certificate.getName(), CERTIFICATE_NAME);
        Assert.assertEquals(certificate.getCertificate(), UPDATED_CERTIFICATE);
    }

    @Test(priority = 6)
    public void testDeleteCertificate() throws CertificateMgtException, SQLException {

        applicationCertificateManagementDAO.deleteCertificate(certificateID, TENANT_ID);
        mockDBConnection();
        Certificate certificate = applicationCertificateManagementDAO.getCertificate(certificateID, TENANT_ID);
        Assert.assertNull(certificate);
    }

    private void mockDBConnection() throws SQLException {

        when(dataSource.getConnection()).thenReturn(TestUtil.getConnection(DB_NAME));
    }
}
