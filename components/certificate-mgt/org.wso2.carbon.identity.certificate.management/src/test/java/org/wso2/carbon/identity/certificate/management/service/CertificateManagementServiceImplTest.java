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

package org.wso2.carbon.identity.certificate.management.service;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.certificate.management.exception.CertificateMgtClientException;
import org.wso2.carbon.identity.certificate.management.exception.CertificateMgtException;
import org.wso2.carbon.identity.certificate.management.model.Certificate;
import org.wso2.carbon.identity.certificate.management.service.impl.CertificateManagementServiceImpl;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.core.internal.component.IdentityCoreServiceDataHolder;

import static org.wso2.carbon.identity.certificate.management.util.TestUtil.CERTIFICATE_NAME;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.ENCODED_CERTIFICATE;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.INVALID_CERTIFICATE;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.INVALID_ENCODED_CERTIFICATE;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.TEST_OTHER_UUID;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.TEST_TENANT_DOMAIN;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.UPDATED_ENCODED_CERTIFICATE;

/**
 * This class is a test suite for the CertificateManagementServiceImpl class.
 * It contains unit tests to verify the functionality of the methods
 * in the CertificateManagementServiceImpl class.
 */
@WithH2Database(files = {"dbscripts/h2.sql"})
@WithCarbonHome
@WithRealmService(injectToSingletons = {IdentityCoreServiceDataHolder.class})
public class CertificateManagementServiceImplTest {

    private String certificateId;
    private CertificateManagementService certificateManagementService;

    @BeforeClass
    public void setUpClass() {

        certificateManagementService = CertificateManagementServiceImpl.getInstance();
    }

    @Test(priority = 1)
    public void testAddCertificate() throws CertificateMgtException {

        Certificate creatingCertificate = new Certificate.Builder()
                .name(CERTIFICATE_NAME)
                .certificateContent(ENCODED_CERTIFICATE)
                .build();
        certificateId = certificateManagementService.addCertificate(creatingCertificate, TEST_TENANT_DOMAIN);
        Assert.assertNotNull(certificateId);
    }

    @Test(priority = 2)
    public void testGetCertificate() throws CertificateMgtException {

        Certificate certificate = certificateManagementService.getCertificate(certificateId, TEST_TENANT_DOMAIN);
        Assert.assertNotNull(certificate);
        Assert.assertEquals(certificate.getId(), certificateId);
        Assert.assertEquals(certificate.getName(), CERTIFICATE_NAME);
        Assert.assertEquals(certificate.getCertificateContent(), ENCODED_CERTIFICATE);
    }

    @DataProvider
    public String[][] invalidDataProvider() {

        return new String[][]{
                {"", ENCODED_CERTIFICATE},
                {"   ", ENCODED_CERTIFICATE},
                {CERTIFICATE_NAME, ""},
                {CERTIFICATE_NAME, " "},
                {CERTIFICATE_NAME, INVALID_ENCODED_CERTIFICATE},
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
                .certificateContent(certificateContent)
                .build();
        certificateManagementService.addCertificate(creatingCertificate, TEST_TENANT_DOMAIN);
    }

    @Test(priority = 4, expectedExceptions = CertificateMgtException.class,
            expectedExceptionsMessageRegExp = "Unable to perform the operation.")
    public void testGetCertificateWithInvalidId() throws CertificateMgtException {

        certificateManagementService.getCertificate(TEST_OTHER_UUID, TEST_TENANT_DOMAIN);
    }

    @Test(priority = 5)
    public void testUpdateCertificateContent() throws CertificateMgtException {

        certificateManagementService.updateCertificateContent(certificateId, UPDATED_ENCODED_CERTIFICATE,
                TEST_TENANT_DOMAIN);

        Certificate updatedCertificate = certificateManagementService.getCertificate(certificateId, TEST_TENANT_DOMAIN);
        Assert.assertEquals(updatedCertificate.getId(), certificateId);
        Assert.assertEquals(updatedCertificate.getName(), CERTIFICATE_NAME);
        Assert.assertEquals(updatedCertificate.getCertificateContent(), UPDATED_ENCODED_CERTIFICATE);
    }

    @Test(priority = 6, expectedExceptions = CertificateMgtException.class,
            expectedExceptionsMessageRegExp = "Unable to perform the operation.")
    public void testUpdateCertificateWithInvalidId() throws CertificateMgtException {

        certificateManagementService.updateCertificateContent(TEST_OTHER_UUID, ENCODED_CERTIFICATE, TEST_TENANT_DOMAIN);
    }

    @DataProvider
    public String[][] invalidCertificateContentDataProvider() {

        return new String[][]{
                {INVALID_ENCODED_CERTIFICATE},
                {INVALID_CERTIFICATE}
        };
    }

    @Test(priority = 7, dataProvider = "invalidCertificateContentDataProvider",
            expectedExceptions = CertificateMgtClientException.class)
    public void testUpdateInvalidCertificate(String certificateContent) throws CertificateMgtException {

        certificateManagementService.updateCertificateContent(certificateId, certificateContent, TEST_TENANT_DOMAIN);
    }

    @Test(priority = 8, expectedExceptions = CertificateMgtException.class,
            expectedExceptionsMessageRegExp = "Unable to perform the operation.")
    public void testDeleteCertificate() throws CertificateMgtException {

        certificateManagementService.deleteCertificate(certificateId, TEST_TENANT_DOMAIN);
        certificateManagementService.getCertificate(certificateId, TEST_TENANT_DOMAIN);
    }
}
