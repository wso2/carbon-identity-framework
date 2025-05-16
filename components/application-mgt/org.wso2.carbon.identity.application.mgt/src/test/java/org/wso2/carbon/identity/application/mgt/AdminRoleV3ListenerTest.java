/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org).
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

package org.wso2.carbon.identity.application.mgt;

import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.mgt.listener.AdminRoleV3Listener;

import java.lang.reflect.Method;

import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class AdminRoleV3ListenerTest {

    @Test
    void testIsValidSubOrgAdminPermission() throws Exception {

        AdminRoleV3Listener adminRoleListener = spy(new AdminRoleV3Listener());
        // Get the private method using reflection
        Method isValidSubOrgPermissionMethod = AdminRoleV3Listener.class.getDeclaredMethod("isValidSubOrgPermission",
                String.class);
        isValidSubOrgPermissionMethod.setAccessible(true);

        // Test case: permission starts with INTERNAL_ORG_SCOPE_PREFIX
        assertTrue((Boolean) isValidSubOrgPermissionMethod.invoke(adminRoleListener,
                "internal_org_application_mgt_view"));

        // Test case: permission starts with CONSOLE_ORG_SCOPE_PREFIX
        assertTrue((Boolean) isValidSubOrgPermissionMethod.invoke(adminRoleListener, "console:org:applications"));

        // Test case: permission does not start with INTERNAL_SCOPE_PREFIX or CONSOLE_SCOPE_PREFIX
        assertFalse((Boolean) isValidSubOrgPermissionMethod.invoke(adminRoleListener, "read"));

        // Test case: permission starts with INTERNAL_SCOPE_PREFIX
        assertFalse((Boolean) isValidSubOrgPermissionMethod.invoke(adminRoleListener, "internal_application_mgt_view"));

        // Test case: permission starts with CONSOLE_SCOPE_PREFIX
        assertFalse((Boolean) isValidSubOrgPermissionMethod.invoke(adminRoleListener, "console:applications"));
    }
}
