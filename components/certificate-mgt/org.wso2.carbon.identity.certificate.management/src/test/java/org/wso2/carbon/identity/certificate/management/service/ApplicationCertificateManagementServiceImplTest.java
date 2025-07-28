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
import org.wso2.carbon.identity.certificate.management.service.impl.ApplicationCertificateManagementServiceImpl;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.core.internal.component.IdentityCoreServiceDataHolder;

import static org.wso2.carbon.identity.certificate.management.util.TestUtil.CERTIFICATE_NAME;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.ENCODED_CERTIFICATE;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.INVALID_CERTIFICATE;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.INVALID_ENCODED_CERTIFICATE;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.TEST_OTHER_ID;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.TEST_TENANT_DOMAIN;
import static org.wso2.carbon.identity.certificate.management.util.TestUtil.UPDATED_ENCODED_CERTIFICATE;

/**
 * This class is a test suite for the ApplicationCertificateManagementServiceImpl class.
 * It contains unit tests to verify the functionality of the methods
 * in the ApplicationCertificateManagementServiceImpl class.
 */
@WithH2Database(files = {"dbscripts/h2.sql"})
@WithCarbonHome
@WithRealmService(injectToSingletons = {IdentityCoreServiceDataHolder.class})
public class ApplicationCertificateManagementServiceImplTest {

    private int certificateIntId;
    private ApplicationCertificateManagementService applicationCertificateManagementService;

    @BeforeClass
    public void setUpClass() {

        applicationCertificateManagementService = ApplicationCertificateManagementServiceImpl.getInstance();
        certificateIntId = 0;
    }

    @Test(priority = 1)
    public void testAddCertificate() throws CertificateMgtException {

        Certificate creatingCertificate = new Certificate.Builder()
                .name(CERTIFICATE_NAME)
                .certificateContent(ENCODED_CERTIFICATE)
                .build();
        certificateIntId = applicationCertificateManagementService.addCertificate(creatingCertificate, 
                TEST_TENANT_DOMAIN);
        Assert.assertNotEquals(certificateIntId, 0);
    }

    @Test(priority = 2)
    public void testGetCertificate() throws CertificateMgtException {

        Certificate certificate = applicationCertificateManagementService.getCertificate(certificateIntId,
                TEST_TENANT_DOMAIN);
        Assert.assertNotNull(certificate);
        Assert.assertEquals(certificate.getId(), String.valueOf(certificateIntId));
        Assert.assertEquals(certificate.getName(), CERTIFICATE_NAME);
        Assert.assertEquals(certificate.getCertificateContent(), ENCODED_CERTIFICATE);
    }

    @Test(priority = 3)
    public void testGetCertificateByName() throws CertificateMgtException {

        Certificate certificate = applicationCertificateManagementService.getCertificateByName(CERTIFICATE_NAME,
                TEST_TENANT_DOMAIN);
        Assert.assertNotNull(certificate);
        Assert.assertEquals(certificate.getId(), String.valueOf(certificateIntId));
        Assert.assertEquals(certificate.getName(), CERTIFICATE_NAME);
        Assert.assertEquals(certificate.getCertificateContent(), ENCODED_CERTIFICATE);
    }

    @DataProvider
    public Object[][] invalidCertificateDataProvider() {

        return new Object[][]{
                {"", ENCODED_CERTIFICATE},
                {"   ", ENCODED_CERTIFICATE},
                {CERTIFICATE_NAME, ""},
                {CERTIFICATE_NAME, " "},
                {CERTIFICATE_NAME, INVALID_ENCODED_CERTIFICATE},
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
                .certificateContent(certificateContent)
                .build();
        applicationCertificateManagementService.addCertificate(creatingCertificate, TEST_TENANT_DOMAIN);
    }

    @Test(priority = 5, expectedExceptions = CertificateMgtException.class,
            expectedExceptionsMessageRegExp = "Unable to perform the operation.")
    public void testGetCertificateWithInvalidId() throws CertificateMgtException {

        applicationCertificateManagementService.getCertificate(TEST_OTHER_ID, TEST_TENANT_DOMAIN);
    }

    @Test(priority = 6, expectedExceptions = CertificateMgtException.class,
            expectedExceptionsMessageRegExp = "Unable to perform the operation.")
    public void testGetCertificateWithInvalidName() throws CertificateMgtException {

        applicationCertificateManagementService.getCertificateByName("Invalid_name", TEST_TENANT_DOMAIN);
    }

    @Test(priority = 7)
    public void testUpdateCertificateWithId() throws CertificateMgtException {

        applicationCertificateManagementService.updateCertificateContent(certificateIntId, UPDATED_ENCODED_CERTIFICATE,
                TEST_TENANT_DOMAIN);

        Certificate updatedCertificate = applicationCertificateManagementService.getCertificate(certificateIntId,
                TEST_TENANT_DOMAIN);
        Assert.assertEquals(updatedCertificate.getId(), String.valueOf(certificateIntId));
        Assert.assertEquals(updatedCertificate.getName(), CERTIFICATE_NAME);
        Assert.assertEquals(updatedCertificate.getCertificateContent(), UPDATED_ENCODED_CERTIFICATE);
    }

    @Test(priority = 8, expectedExceptions = CertificateMgtException.class,
            expectedExceptionsMessageRegExp = "Unable to perform the operation.")
    public void testUpdateCertificateWithInvalidIntId() throws CertificateMgtException {

        applicationCertificateManagementService.updateCertificateContent(TEST_OTHER_ID, ENCODED_CERTIFICATE,
                TEST_TENANT_DOMAIN);
    }

    @DataProvider
    public Object[][] invalidCertificateContentDataProvider() {

        return new Object[][]{
                {""},
                {" "},
                {INVALID_ENCODED_CERTIFICATE},
                {INVALID_CERTIFICATE}
        };
    }

    @Test(priority = 9, dataProvider = "invalidCertificateContentDataProvider",
            expectedExceptions = CertificateMgtClientException.class)
    public void testUpdateInvalidCertificateWithIntId(String certificateContent)
            throws CertificateMgtException {

        applicationCertificateManagementService.updateCertificateContent(certificateIntId, certificateContent,
                TEST_TENANT_DOMAIN);
    }

    @Test(priority = 10, expectedExceptions = CertificateMgtException.class,
            expectedExceptionsMessageRegExp = "Unable to perform the operation.")
    public void testDeleteCertificateWithIntId() throws CertificateMgtException {

        applicationCertificateManagementService.deleteCertificate(certificateIntId, TEST_TENANT_DOMAIN);
        applicationCertificateManagementService.getCertificate(certificateIntId, TEST_TENANT_DOMAIN);
    }
}
