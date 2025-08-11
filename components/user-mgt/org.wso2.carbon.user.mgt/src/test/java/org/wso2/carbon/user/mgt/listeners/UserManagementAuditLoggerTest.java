/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.user.mgt.listeners;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.context.IdentityContext;
import org.wso2.carbon.identity.core.context.model.Flow;
import org.wso2.carbon.identity.event.IdentityEventConfigBuilder;
import org.wso2.carbon.user.mgt.listeners.utils.ListenerUtils;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class UserManagementAuditLoggerTest {

    private UserManagementAuditLogger auditLogger;

    @BeforeMethod
    public void setUp() {
        // Destroy the current identity context to avoid Flow being already set
        auditLogger = new UserManagementAuditLogger();
    }

    @AfterMethod
    public void tearDown() {
        // Destroy the current identity context after each test
        IdentityContext.destroyCurrentContext();
    }

    @DataProvider(name = "passwordUpdateFlowData")
    public Object[][] passwordUpdateFlowData() {
        return new Object[][]{
                {Flow.InitiatingPersona.USER, ListenerUtils.CHANGE_PASSWORD_BY_USER_ACTION},
                {Flow.InitiatingPersona.ADMIN, ListenerUtils.CHANGE_PASSWORD_BY_ADMIN_ACTION},
                {Flow.InitiatingPersona.APPLICATION, ListenerUtils.CHANGE_PASSWORD_BY_ADMIN_ACTION}
        };
    }

    @Test(dataProvider = "passwordUpdateFlowData")
    public void testGetPasswordUpdateAuditMessageAction(Flow.InitiatingPersona persona, String expectedAction) {

        configureCarbonHome();
        IdentityContext.getThreadLocalIdentityContext().enterFlow(new Flow.CredentialFlowBuilder()
                .name(Flow.Name.CREDENTIAL_RESET)
                .initiatingPersona(persona)
                .credentialType(Flow.CredentialType.PASSWORD)
                .build());
        String action = auditLogger.getPasswordUpdateAuditMessageAction();
        assertEquals(action, expectedAction);
    }

    @Test()
    public void testGetPasswordUpdateAuditMessageActionNull() {

        configureCarbonHome();
        String action = auditLogger.getPasswordUpdateAuditMessageAction();
        assertNull(action);
    }

    /**
     * Configures the carbon home system property for the test environment.
     */
    private void configureCarbonHome() {
        String carbonHome = IdentityEventConfigBuilder.class.getResource("/").getFile();
        System.setProperty("carbon.home", carbonHome);
    }
}
