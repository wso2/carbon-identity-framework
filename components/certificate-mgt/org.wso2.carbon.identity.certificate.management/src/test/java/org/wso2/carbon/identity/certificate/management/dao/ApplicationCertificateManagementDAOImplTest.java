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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.certificate.management.dao.impl.ApplicationCertificateManagementDAOImpl;
import org.wso2.carbon.identity.certificate.management.exception.CertificateMgtException;
import org.wso2.carbon.identity.certificate.management.model.Certificate;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;

import java.util.UUID;

import static org.wso2.carbon.identity.certificate.management.util.TestUtil.CERTIFICATE_NAME;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.ENCODED_CERTIFICATE;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.TEST_TENANT_ID;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.TEST_UUID;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.UPDATED_ENCODED_CERTIFICATE;

/**
 * This class is a test suite for the ApplicationCertificateManagementDAOImpl class.
 * It contains unit tests to verify the functionality of the methods
 * in the ApplicationCertificateManagementDAOImpl class.
 */
@WithH2Database(files = {"dbscripts/h2.sql"})
@WithCarbonHome
public class ApplicationCertificateManagementDAOImplTest {

    private int certificateID;
    private ApplicationCertificateManagementDAOImpl applicationCertificateManagementDAO;

    @BeforeClass
    public void setUpClass() {

        applicationCertificateManagementDAO = new ApplicationCertificateManagementDAOImpl();
    }

    @Test(priority = 1)
    public void testAddCertificate() throws CertificateMgtException {

        Certificate creatingCertificate = new Certificate.Builder()
                .name(CERTIFICATE_NAME)
                .certificateContent(ENCODED_CERTIFICATE)
                .build();
        certificateID = applicationCertificateManagementDAO.addCertificate(TEST_UUID, creatingCertificate,
                TEST_TENANT_ID);
        Assert.assertNotEquals(certificateID, 0);
    }

    @Test(priority = 2)
    public void testGetCertificate() throws CertificateMgtException {

        Certificate certificate = applicationCertificateManagementDAO.getCertificate(certificateID, TEST_TENANT_ID);
        Assert.assertNotNull(certificate);
        Assert.assertEquals(certificate.getId(), String.valueOf(certificateID));
        Assert.assertEquals(certificate.getName(), CERTIFICATE_NAME);
        Assert.assertEquals(certificate.getCertificateContent(), ENCODED_CERTIFICATE);
    }

    @Test(priority = 3)
    public void testGetCertificateByName() throws CertificateMgtException {

        Certificate certificate = applicationCertificateManagementDAO.getCertificateByName(CERTIFICATE_NAME,
                TEST_TENANT_ID);
        Assert.assertNotNull(certificate);
        Assert.assertEquals(certificate.getId(), String.valueOf(certificateID));
        Assert.assertEquals(certificate.getName(), CERTIFICATE_NAME);
        Assert.assertEquals(certificate.getCertificateContent(), ENCODED_CERTIFICATE);
    }

    @Test(priority = 4, expectedExceptions = CertificateMgtException.class,
            expectedExceptionsMessageRegExp = "Error while adding Certificate.")
    public void testAddInvalidCertificate() throws CertificateMgtException {

        // Adding a certificate to the same tenant with the same name to generate unique key constraint violation.
        String otherCertificateUUID = String.valueOf(UUID.randomUUID());
        Certificate creatingCertificate = new Certificate.Builder()
                .name(CERTIFICATE_NAME)
                .certificateContent(ENCODED_CERTIFICATE)
                .build();

        applicationCertificateManagementDAO.addCertificate(otherCertificateUUID, creatingCertificate, TEST_TENANT_ID);
    }

    @Test(priority = 5)
    public void testUpdateCertificate() throws CertificateMgtException {

        applicationCertificateManagementDAO.updateCertificateContent(certificateID, UPDATED_ENCODED_CERTIFICATE,
                TEST_TENANT_ID);
        Certificate certificate = applicationCertificateManagementDAO.getCertificate(certificateID, TEST_TENANT_ID);
        Assert.assertEquals(certificate.getId(), String.valueOf(certificateID));
        Assert.assertEquals(certificate.getName(), CERTIFICATE_NAME);
        Assert.assertEquals(certificate.getCertificateContent(), UPDATED_ENCODED_CERTIFICATE);
    }

    @Test(priority = 6)
    public void testDeleteCertificate() throws CertificateMgtException {

        applicationCertificateManagementDAO.deleteCertificate(certificateID, TEST_TENANT_ID);
        Certificate certificate = applicationCertificateManagementDAO.getCertificate(certificateID, TEST_TENANT_ID);
        Assert.assertNull(certificate);
    }
}
