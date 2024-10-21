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
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.certificate.management.core.constant.CertificateMgtErrors;
import org.wso2.carbon.identity.certificate.management.core.exception.CertificateMgtClientException;
import org.wso2.carbon.identity.certificate.management.core.exception.CertificateMgtServerException;
import org.wso2.carbon.identity.certificate.management.core.util.CertificateMgtExceptionHandler;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * This class is a test suite for the CertificateMgtExceptionHandler class.
 * It contains unit tests to verify the functionality of the methods
 * in the CertificateMgtExceptionHandler class.
 */
public class CertificateMgtExceptionHandlerTest {

    @DataProvider
    public Object[][] certificateMgtClientExceptionDataProvider() {

        return new Object[][]{
                {CertificateMgtErrors.ERROR_EMPTY_FIELD, TestUtil.NAME_FIELD},
                {CertificateMgtErrors.ERROR_INVALID_FIELD, TestUtil.CERTIFICATE_FIELD}
        };
    }

    @Test(dataProvider = "certificateMgtClientExceptionDataProvider")
    public void testThrowClientException(CertificateMgtErrors error, String... data) {

        try {
            CertificateMgtExceptionHandler.throwClientException(error, data);
        } catch (CertificateMgtClientException e) {
            Assert.assertEquals(error.getMessage(), e.getMessage());
            Assert.assertEquals(String.format(error.getDescription(), data), e.getDescription());
            Assert.assertEquals(error.getCode(), e.getErrorCode());
            return;
        }
        Assert.fail();
    }

    @DataProvider
    public Object[][] certificateMgtServerExceptionDataProvider() {

        return new Object[][]{
                {CertificateMgtErrors.ERROR_WHILE_ADDING_CERTIFICATE, new SQLIntegrityConstraintViolationException(),
                        TestUtil.CERTIFICATE_NAME},
                {CertificateMgtErrors.ERROR_WHILE_UPDATING_CERTIFICATE, new DataAccessException(), TestUtil.TEST_UUID}
        };
    }

    @Test(dataProvider = "certificateMgtServerExceptionDataProvider")
    public void testThrowServerException(CertificateMgtErrors error, Throwable throwable, String... data) {

        try {
            CertificateMgtExceptionHandler.throwServerException(error, throwable, data);
        } catch (CertificateMgtServerException e) {
            Assert.assertEquals(throwable, e.getCause());
            Assert.assertEquals(error.getMessage(), e.getMessage());
            Assert.assertEquals(String.format(error.getDescription(), data), e.getDescription());
            Assert.assertEquals(error.getCode(), e.getErrorCode());
            return;
        }
        Assert.fail();
    }
}
