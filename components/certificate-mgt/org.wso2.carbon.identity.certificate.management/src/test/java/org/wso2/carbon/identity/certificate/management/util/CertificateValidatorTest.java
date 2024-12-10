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

package org.wso2.carbon.identity.certificate.management.util;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.certificate.management.constant.CertificateMgtErrors;
import org.wso2.carbon.identity.certificate.management.exception.CertificateMgtClientException;

/**
 * This class is a test suite for the CertificateValidator class.
 * It contains unit tests to verify the functionality of the methods
 * in the CertificateValidator class.
 */
public class CertificateValidatorTest {

    private static final String ERROR_INVALID_REQUEST = "Invalid request.";

    @DataProvider
    public Object[][] isBlankDataProvider() {

        return new String[][]{
                {TestUtil.NAME_FIELD, null},
                {TestUtil.NAME_FIELD, ""},
                {TestUtil.CERTIFICATE_FIELD, "  "}
        };
    }

    @Test(dataProvider = "isBlankDataProvider")
    public void testIsBlank(String fieldName, String fieldValue) {

        try {
            CertificateValidator.validateForBlank(fieldName, fieldValue);
        } catch (CertificateMgtClientException e) {
            Assert.assertEquals(e.getMessage(), ERROR_INVALID_REQUEST);
            Assert.assertEquals(e.getDescription(), fieldName + " cannot be empty.");
        }
    }

    @DataProvider
    public Object[][] isNotBlankDataProvider() {

        return new String[][]{
                {TestUtil.NAME_FIELD, TestUtil.CERTIFICATE_NAME},
                {TestUtil.CERTIFICATE_FIELD, TestUtil.CERTIFICATE}
        };
    }

    @Test(dataProvider = "isNotBlankDataProvider")
    public void testIsNotBlank(String fieldName, String fieldValue) {

        try {
            CertificateValidator.validateForBlank(fieldName, fieldValue);
        } catch (CertificateMgtClientException e) {
            Assert.fail();
        }
    }

    @DataProvider
    public Object[][] invalidCertificateNameDataProvider() {

        return new String[][]{
                {""},
                {"   "}
        };
    }

    @Test(dataProvider = "invalidCertificateNameDataProvider")
    public void testIsInvalidCertificateName(String certificateName) {

        try {
            CertificateValidator.validateCertificateName(certificateName);
        } catch (CertificateMgtClientException e) {
            Assert.assertEquals(e.getMessage(), ERROR_INVALID_REQUEST);
            Assert.assertEquals(e.getDescription(), "Name is invalid.");
        }
    }

    @DataProvider
    public Object[][] validCertificateNameDataProvider() {

        return new String[][]{
                {"test"},
                {"test certificate"}
        };
    }

    @Test(dataProvider = "validCertificateNameDataProvider")
    public void testIsValidCertificateName(String certificateName) {

        try {
            CertificateValidator.validateCertificateName(certificateName);
        } catch (CertificateMgtClientException e) {
            Assert.fail();
        }
    }

    @DataProvider
    public Object[][] invalidPEMFormatDataProvider() {

        return new Object[][]{
                {TestUtil.INVALID_CERTIFICATE, CertificateMgtErrors.ERROR_INVALID_CERTIFICATE_CONTENT},
                {TestUtil.INVALID_ENCODED_CERTIFICATE, CertificateMgtErrors.ERROR_INVALID_CERTIFICATE_CONTENT}
        };
    }

    @Test(dataProvider = "invalidPEMFormatDataProvider")
    public void testIsInvalidPEMFormat(String certificateName, CertificateMgtErrors error) {

        try {
            CertificateValidator.validatePemFormat(certificateName);
        } catch (CertificateMgtClientException e) {
            Assert.assertEquals(e.getErrorCode(), error.getCode());
            Assert.assertEquals(e.getMessage(), error.getMessage());
            Assert.assertEquals(e.getDescription(), error.getDescription());
        }
    }

    @DataProvider
    public Object[][] validPEMFormatDataProvider() {

        return new String[][]{
                {TestUtil.CERTIFICATE},
                {TestUtil.UPDATED_CERTIFICATE},
                {TestUtil.ENCODED_CERTIFICATE},
                {TestUtil.UPDATED_ENCODED_CERTIFICATE},
        };
    }

    @Test(dataProvider = "validPEMFormatDataProvider")
    public void testIsValidPEMFormat(String certificateName) {

        try {
            CertificateValidator.validatePemFormat(certificateName);
        } catch (CertificateMgtClientException e) {
            Assert.fail();
        }
    }

    @DataProvider
    public Object[][] invalidCertificateContentDataProvider() {

        return new Object[][]{
                {"", CertificateMgtErrors.ERROR_INVALID_FIELD},
                {"   ", CertificateMgtErrors.ERROR_INVALID_FIELD},
                {TestUtil.INVALID_ENCODED_CERTIFICATE,
                        CertificateMgtErrors.ERROR_INVALID_CERTIFICATE_CONTENT},
                {TestUtil.INVALID_CERTIFICATE, CertificateMgtErrors.ERROR_INVALID_CERTIFICATE_CONTENT}
        };
    }

    @Test(dataProvider = "invalidCertificateContentDataProvider")
    public void testIsInvalidCertificateContent(String certificateName, CertificateMgtErrors error) {

        try {
            CertificateValidator.validateCertificateContent(certificateName);
        } catch (CertificateMgtClientException e) {
            Assert.assertEquals(e.getErrorCode(), error.getCode());
            Assert.assertEquals(e.getMessage(), error.getMessage());
            if (error == CertificateMgtErrors.ERROR_INVALID_FIELD) {
                Assert.assertEquals(e.getDescription(), String.format(error.getDescription(),
                        TestUtil.CERTIFICATE_FIELD));
            } else {
                Assert.assertEquals(e.getDescription(), error.getDescription());
            }

        }
    }

    @DataProvider
    public Object[][] validCertificateContentDataProvider() {

        return new String[][]{
                {TestUtil.CERTIFICATE},
                {TestUtil.UPDATED_CERTIFICATE},
                {TestUtil.ENCODED_CERTIFICATE},
                {TestUtil.UPDATED_ENCODED_CERTIFICATE},
        };
    }

    @Test(dataProvider = "validCertificateContentDataProvider")
    public void testIsValidCertificateContent(String certificateName) {

        try {
            CertificateValidator.validateCertificateContent(certificateName);
        } catch (CertificateMgtClientException e) {
            Assert.fail();
        }
    }
}
