/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.core.circuitbreaker;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

public class TenantKeyUtilTest {

    @Test
    public void testBuildAndExtractTenantServiceKeyWithUnderscores() {

        String tenantDomain = "foo_bar.example.com";
        String service = "oauth2_token_service";

        String tenantServiceKey = TenantKeyUtil.buildTenantServiceKey(tenantDomain, service);

        assertEquals(TenantKeyUtil.extractTenantDomain(tenantServiceKey), tenantDomain);
        assertEquals(TenantKeyUtil.extractService(tenantServiceKey), service);
    }

    @Test
    public void testBuildTenantServiceKeyAvoidsUnderscoreDrivenCollisions() {

        String tenantServiceKeyOne = TenantKeyUtil.buildTenantServiceKey("foo_bar", "oauth2");
        String tenantServiceKeyTwo = TenantKeyUtil.buildTenantServiceKey("foo", "bar_oauth2");

        assertNotEquals(tenantServiceKeyOne, tenantServiceKeyTwo);
    }

    @Test
    public void testBuildTenantServiceKeyTrimsInputs() {

        String tenantServiceKey = TenantKeyUtil.buildTenantServiceKey(" foo_bar.example.com ", " oauth2 ");

        assertEquals(TenantKeyUtil.extractTenantDomain(tenantServiceKey), "foo_bar.example.com");
        assertEquals(TenantKeyUtil.extractService(tenantServiceKey), "oauth2");
    }

    @Test
    public void testExtractServiceSupportsAdditionalColons() {

        String tenantServiceKey = TenantKeyUtil.buildTenantServiceKey("foo_bar.example.com", "oauth2:token");

        assertEquals(TenantKeyUtil.extractTenantDomain(tenantServiceKey), "foo_bar.example.com");
        assertEquals(TenantKeyUtil.extractService(tenantServiceKey), "oauth2:token");
    }

    @Test(dataProvider = "invalidBuildInputs", expectedExceptions = IllegalArgumentException.class)
    public void testBuildTenantServiceKeyRejectsInvalidInputs(String tenantDomain, String service) {

        TenantKeyUtil.buildTenantServiceKey(tenantDomain, service);
    }

    @DataProvider(name = "invalidBuildInputs")
    public Object[][] invalidBuildInputs() {

        return new Object[][]{
                {null, "oauth2"},
                {"", "oauth2"},
                {" ", "oauth2"},
                {"foo.com", null},
                {"foo.com", ""},
                {"foo.com", " "}
        };
    }

    @Test(dataProvider = "invalidExtractInputs", expectedExceptions = IllegalArgumentException.class)
    public void testExtractTenantDomainRejectsInvalidKeys(String tenantServiceKey) {

        TenantKeyUtil.extractTenantDomain(tenantServiceKey);
    }

    @Test(dataProvider = "invalidExtractInputs", expectedExceptions = IllegalArgumentException.class)
    public void testExtractServiceRejectsInvalidKeys(String tenantServiceKey) {

        TenantKeyUtil.extractService(tenantServiceKey);
    }

    @DataProvider(name = "invalidExtractInputs")
    public Object[][] invalidExtractInputs() {

        return new Object[][]{
                {null},
                {""},
                {" "},
                {"foo"},
                {":oauth2"},
                {"foo.com:"},
                {"foo.com: "}
        };
    }
}
