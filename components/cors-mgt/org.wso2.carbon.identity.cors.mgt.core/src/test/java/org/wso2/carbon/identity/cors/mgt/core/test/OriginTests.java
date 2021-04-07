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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

/**
 * Tests the base origin class.
 */
public class OriginTests {

    @Test
    public void testOrigin() throws CORSManagementServiceClientException {

        String uri = "http://example.com";
        Origin o = new Origin(uri);

        assertEquals(o.toString(), uri);
        assertEquals(o.hashCode(), uri.hashCode());
    }

    @Test
    public void testOriginEquality() throws CORSManagementServiceClientException {

        String uri = "http://example.com";
        Origin o1 = new Origin(uri);
        Origin o2 = new Origin(uri);

        assertEquals(o2, o1);
    }

    @Test
    public void testOriginInequality() throws CORSManagementServiceClientException {

        String uri1 = "http://example.com";
        String uri2 = "HTTP://EXAMPLE.COM";
        Origin o1 = new Origin(uri1);
        Origin o2 = new Origin(uri2);

        assertNotEquals(o2, o1);
    }

    @Test
    public void testOriginInequalityNull() throws CORSManagementServiceClientException {

        assertNotEquals(new Origin("http://example.com"), null);
    }

    @Test
    public void testValidation() {

        String uri = "http://example.com";

        Origin origin = null;

        try {
            origin = new Origin(uri);
        } catch (CORSManagementServiceClientException e) {
            fail(e.getMessage());
        }

        assertNotNull(origin);
        assertEquals(origin.toString(), uri);
    }

    @Test
    public void testValidationAppScheme() {

        String uri = "app://example.com";
        Origin origin = null;
        try {
            origin = new Origin(uri);
        } catch (CORSManagementServiceClientException e) {
            fail(e.getMessage());
        }

        assertNotNull(origin);
        assertEquals(origin.toString(), uri);
    }

    @Test
    public void testHTTPOrigin() {

        String uri = "http://example.com";
        Origin origin = null;
        try {
            origin = new Origin(new Origin(uri).toString());
        } catch (CORSManagementServiceClientException e) {
            fail(e.getMessage());
        }

        assertEquals(origin.toString(), uri);
        assertEquals(origin.getScheme(), "http");
        assertEquals(origin.getHost(), "example.com");
        assertEquals(origin.getPort(), -1);
        assertEquals(origin.getSuffix(), "example.com");
    }

    @Test
    public void testHTTPSOrigin() {

        String uri = "https://example.com";
        Origin origin = null;
        try {
            origin = new Origin(new Origin(uri).toString());
        } catch (CORSManagementServiceClientException e) {
            fail(e.getMessage());
        }

        assertEquals(origin.toString(), uri);
        assertEquals(origin.getScheme(), "https");
        assertEquals(origin.getHost(), "example.com");
        assertEquals(origin.getPort(), -1);
        assertEquals(origin.getSuffix(), "example.com");
    }

    @Test
    public void testAPPOrigin() {

        String uri = "app://example.com";
        Origin origin = null;
        try {
            origin = new Origin(new Origin(uri).toString());
        } catch (CORSManagementServiceClientException e) {
            fail(e.getMessage());
        }

        assertEquals(origin.toString(), uri);
        assertEquals(origin.getScheme(), "app");
        assertEquals(origin.getHost(), "example.com");
        assertEquals(origin.getPort(), -1);
        assertEquals(origin.getSuffix(), "example.com");
    }

    @Test
    public void testIPAddressHost() {

        String uri = "http://192.168.0.1:8080";
        Origin origin = null;
        try {
            origin = new Origin(new Origin(uri).toString());
        } catch (CORSManagementServiceClientException e) {
            fail(e.getMessage());
        }

        assertEquals(origin.toString(), uri);
        assertEquals(origin.getScheme(), "http");
        assertEquals(origin.getHost(), "192.168.0.1");
        assertEquals(origin.getPort(), 8080);
        assertEquals(origin.getSuffix(), "192.168.0.1:8080");
    }

    @Test
    public void testNullHost() {

        String uri = "http:///path/";
        Origin origin;
        try {
            origin = new Origin(new Origin(uri).toString());
            fail();
        } catch (CORSManagementServiceClientException e) {
            assertEquals(e.getMessage(), "Bad origin URI: Missing authority (host)");
        }
    }
}
