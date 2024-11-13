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
import org.wso2.carbon.identity.certificate.management.dao.impl.CertificateManagementDAOImpl;
import org.wso2.carbon.identity.certificate.management.exception.CertificateMgtException;
import org.wso2.carbon.identity.certificate.management.model.Certificate;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;

import static org.wso2.carbon.identity.certificate.management.util.TestUtil.CERTIFICATE_NAME;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.ENCODED_CERTIFICATE;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.TEST_OTHER_UUID;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.TEST_TENANT_ID;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.TEST_UUID;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.UPDATED_ENCODED_CERTIFICATE;

/**
 * This class is a test suite for the CertificateManagementDAOImpl class.
 * It contains unit tests to verify the functionality of the methods
 * in the CertificateManagementDAOImpl class.
 */
@WithH2Database(files = {"dbscripts/h2.sql"})
@WithCarbonHome
public class CertificateManagementDAOImplTest {

    private CertificateManagementDAOImpl certificateMgtDAOImpl;

    @BeforeClass
    public void setUpClass() {

        certificateMgtDAOImpl = new CertificateManagementDAOImpl();
    }

    @Test(priority = 1)
    public void testAddCertificate() throws CertificateMgtException {

        Certificate creatingCertificate = new Certificate.Builder()
                .name(CERTIFICATE_NAME)
                .certificateContent(ENCODED_CERTIFICATE)
                .build();
        certificateMgtDAOImpl.addCertificate(TEST_UUID, creatingCertificate, TEST_TENANT_ID);
    }

    @Test(priority = 2)
    public void testGetCertificate() throws CertificateMgtException {

        Certificate certificate = certificateMgtDAOImpl.getCertificate(TEST_UUID, TEST_TENANT_ID);
        Assert.assertEquals(certificate.getId(), TEST_UUID);
        Assert.assertEquals(certificate.getName(), CERTIFICATE_NAME);
        Assert.assertEquals(certificate.getCertificateContent(), ENCODED_CERTIFICATE);
    }

    @Test(priority = 3, expectedExceptions = CertificateMgtException.class,
            expectedExceptionsMessageRegExp = "Error while adding Certificate.")
    public void testAddInvalidCertificate() throws CertificateMgtException {

        // Adding a certificate to the same tenant with the same name to generate unique key constraint violation.
        Certificate creatingCertificate = new Certificate.Builder()
                .name(CERTIFICATE_NAME)
                .certificateContent(ENCODED_CERTIFICATE)
                .build();

        certificateMgtDAOImpl.addCertificate(TEST_OTHER_UUID, creatingCertificate, TEST_TENANT_ID);
    }

    @Test(priority = 4)
    public void testUpdateCertificateContent() throws CertificateMgtException {

        certificateMgtDAOImpl.updateCertificateContent(TEST_UUID, UPDATED_ENCODED_CERTIFICATE, TEST_TENANT_ID);
        Certificate certificate = certificateMgtDAOImpl.getCertificate(TEST_UUID, TEST_TENANT_ID);
        Assert.assertEquals(certificate.getId(), TEST_UUID);
        Assert.assertEquals(certificate.getName(), CERTIFICATE_NAME);
        Assert.assertEquals(certificate.getCertificateContent(), UPDATED_ENCODED_CERTIFICATE);
    }

    @Test(priority = 6)
    public void testDeleteCertificate() throws CertificateMgtException {

        certificateMgtDAOImpl.deleteCertificate(TEST_UUID, TEST_TENANT_ID);
        Certificate certificate = certificateMgtDAOImpl.getCertificate(TEST_UUID, TEST_TENANT_ID);
        Assert.assertNull(certificate);
    }
}
