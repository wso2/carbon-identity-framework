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
import org.wso2.carbon.identity.certificate.management.core.dao.impl.CertificateManagementDAOImpl;
import org.wso2.carbon.identity.certificate.management.core.exception.CertificateMgtException;
import org.wso2.carbon.identity.certificate.management.core.model.Certificate;
import org.wso2.carbon.identity.certificate.management.util.TestUtil;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import javax.sql.DataSource;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.CERTIFICATE;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.CERTIFICATE_NAME;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.UPDATED_CERTIFICATE;

/**
 * This class is a test suite for the CertificateManagementDAOImpl class.
 * It contains unit tests to verify the functionality of the methods
 * in the CertificateManagementDAOImpl class.
 */
public class CertificateManagementDAOImplTest {

    private static final String DB_NAME = "certificate_mgt_dao";
    private static final int TENANT_ID = 2;

    private String certificateId;
    private String otherCertificateId;
    private Connection connection;
    private DataSource dataSource;
    private MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil;
    private MockedStatic<IdentityTenantUtil> identityTenantUtil;
    private CertificateManagementDAOImpl certificateMgtDAOImpl;

    @BeforeClass
    public void setUpClass() throws SQLException {

        certificateMgtDAOImpl = new CertificateManagementDAOImpl();
        certificateId = String.valueOf(UUID.randomUUID());
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

        connection.close();
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
        certificateMgtDAOImpl.addCertificate(certificateId, creatingCertificate, TENANT_ID);
    }

    @Test(priority = 2)
    public void testGetCertificate() throws CertificateMgtException {

        Certificate certificate = certificateMgtDAOImpl.getCertificate(certificateId, TENANT_ID);
        Assert.assertEquals(certificate.getId(), certificateId);
        Assert.assertEquals(certificate.getName(), CERTIFICATE_NAME);
        Assert.assertEquals(certificate.getCertificate(), CERTIFICATE);
    }

    @Test(priority = 3, expectedExceptions = CertificateMgtException.class,
            expectedExceptionsMessageRegExp = "Error while adding Certificate.")
    public void testInvalidCertificateAddition() throws CertificateMgtException {

        // Adding a certificate with null uuid to generate unique key constraint violation.
        Certificate creatingCertificate = new Certificate.Builder()
                .name(CERTIFICATE_NAME)
                .certificate(CERTIFICATE)
                .build();

        certificateMgtDAOImpl.addCertificate(null, creatingCertificate, TENANT_ID);
    }

    @Test(priority = 4)
    public void testUpdateCertificateContent() throws CertificateMgtException, SQLException {

        certificateMgtDAOImpl.updateCertificateContent(certificateId, UPDATED_CERTIFICATE, TENANT_ID);
        mockDBConnection();
        Certificate certificate = certificateMgtDAOImpl.getCertificate(certificateId, TENANT_ID);
        Assert.assertEquals(certificate.getId(), certificateId);
        Assert.assertEquals(certificate.getName(), CERTIFICATE_NAME);
        Assert.assertEquals(certificate.getCertificate(), UPDATED_CERTIFICATE);
    }

    @Test(priority = 6)
    public void testDeleteCertificate() throws CertificateMgtException, SQLException {

        certificateMgtDAOImpl.deleteCertificate(certificateId, TENANT_ID);
        mockDBConnection();
        Certificate certificate = certificateMgtDAOImpl.getCertificate(certificateId, TENANT_ID);
        Assert.assertNull(certificate);
    }

    private void mockDBConnection() throws SQLException {

        connection = spy(TestUtil.getConnection(DB_NAME));
        when(dataSource.getConnection()).thenReturn(connection);
    }
}
