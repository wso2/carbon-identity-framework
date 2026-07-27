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

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

public class TenantKeyUtilTest {

    @Test
    public void testBuildAndExtractTenantServiceKeyWithUnderscores() {

        String tenantDomain = "foo_bar.example.com";
        String service = "oauth2_token_service";

        String tenantServiceKey = TenantKeyUtil.buildTenantServiceKey(tenantDomain, service);
        TenantKeyUtil.TenantKeyParts parts = TenantKeyUtil.parse(tenantServiceKey);

        assertEquals(parts.tenantDomain(), tenantDomain);
        assertEquals(parts.serviceName(), service);
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
        TenantKeyUtil.TenantKeyParts parts = TenantKeyUtil.parse(tenantServiceKey);

        assertEquals(parts.tenantDomain(), "foo_bar.example.com");
        assertEquals(parts.serviceName(), "oauth2");
    }

    @Test
    public void testParseServiceSupportsAdditionalColons() {

        String tenantServiceKey = TenantKeyUtil.buildTenantServiceKey("foo_bar.example.com", "oauth2:token");
        TenantKeyUtil.TenantKeyParts parts = TenantKeyUtil.parse(tenantServiceKey);

        assertEquals(parts.tenantDomain(), "foo_bar.example.com");
        assertEquals(parts.serviceName(), "oauth2:token");
    }

}
