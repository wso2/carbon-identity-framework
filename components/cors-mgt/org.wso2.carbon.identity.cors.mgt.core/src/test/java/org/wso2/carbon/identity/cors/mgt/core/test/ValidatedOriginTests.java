/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.cors.mgt.core.test;

import org.testng.annotations.Test;
import org.wso2.carbon.identity.cors.mgt.core.exception.CORSManagementServiceClientException;
import org.wso2.carbon.identity.cors.mgt.core.model.Origin;
import org.wso2.carbon.identity.cors.mgt.core.model.ValidatedOrigin;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

/**
 * Tests the validated origin class.
 */
public class ValidatedOriginTests {

    @Test
    public void testValidation() {

        String uri = "http://example.com";

        ValidatedOrigin validatedOrigin = null;

        try {
            validatedOrigin = new ValidatedOrigin(uri);
        } catch (CORSManagementServiceClientException e) {
            fail(e.getMessage());
        }

        assertNotNull(validatedOrigin);
        assertEquals(validatedOrigin.toString(), uri);
    }

    @Test
    public void testValidationAppScheme() {

        String uri = "app://example.com";
        ValidatedOrigin validatedOrigin = null;
        try {
            validatedOrigin = new ValidatedOrigin(uri);
        } catch (CORSManagementServiceClientException e) {
            fail(e.getMessage());
        }

        assertNotNull(validatedOrigin);
        assertEquals(validatedOrigin.toString(), uri);
    }

    @Test
    public void testHTTPOrigin() {

        String uri = "http://example.com";
        ValidatedOrigin o = null;
        try {
            o = new ValidatedOrigin(new Origin(uri).toString());
        } catch (CORSManagementServiceClientException e) {
            fail(e.getMessage());
        }

        assertEquals(o.toString(), uri);
        assertEquals(o.getScheme(), "http");
        assertEquals(o.getHost(), "example.com");
        assertEquals(o.getPort(), -1);
        assertEquals(o.getSuffix(), "example.com");
    }

    @Test
    public void testHTTPSOrigin() {

        String uri = "https://example.com";
        ValidatedOrigin o = null;
        try {
            o = new ValidatedOrigin(new Origin(uri).toString());
        } catch (CORSManagementServiceClientException e) {
            fail(e.getMessage());
        }

        assertEquals(o.toString(), uri);
        assertEquals(o.getScheme(), "https");
        assertEquals(o.getHost(), "example.com");
        assertEquals(o.getPort(), -1);
        assertEquals(o.getSuffix(), "example.com");
    }

    @Test
    public void testAPPOrigin() {

        String uri = "app://example.com";
        ValidatedOrigin o = null;
        try {
            o = new ValidatedOrigin(new Origin(uri).toString());
        } catch (CORSManagementServiceClientException e) {
            fail(e.getMessage());
        }

        assertEquals(o.toString(), uri);
        assertEquals(o.getScheme(), "app");
        assertEquals(o.getHost(), "example.com");
        assertEquals(o.getPort(), -1);
        assertEquals(o.getSuffix(), "example.com");
    }

    @Test
    public void testIPAddressHost() {

        String uri = "http://192.168.0.1:8080";
        ValidatedOrigin o = null;
        try {
            o = new ValidatedOrigin(new Origin(uri).toString());
        } catch (CORSManagementServiceClientException e) {
            fail(e.getMessage());
        }

        assertEquals(o.toString(), uri);
        assertEquals(o.getScheme(), "http");
        assertEquals(o.getHost(), "192.168.0.1");
        assertEquals(o.getPort(), 8080);
        assertEquals(o.getSuffix(), "192.168.0.1:8080");
    }

    @Test
    public void testNullHost() {

        String uri = "http:///path/";
        ValidatedOrigin o;
        try {
            o = new ValidatedOrigin(new Origin(uri).toString());
            fail();
        } catch (CORSManagementServiceClientException e) {
            assertEquals(e.getMessage(), "Bad origin URI: Missing authority (host)");
        }
    }
}
