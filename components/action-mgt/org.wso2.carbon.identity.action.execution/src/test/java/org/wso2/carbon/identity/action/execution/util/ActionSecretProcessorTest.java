/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.action.execution.util;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.execution.internal.component.ActionExecutionServiceComponentHolder;
import org.wso2.carbon.identity.action.execution.internal.util.ActionSecretProcessor;
import org.wso2.carbon.identity.action.management.api.model.AuthProperty;
import org.wso2.carbon.identity.secret.mgt.core.SecretManager;
import org.wso2.carbon.identity.secret.mgt.core.SecretResolveManager;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;
import org.wso2.carbon.identity.secret.mgt.core.model.ResolvedSecret;
import org.wso2.carbon.identity.secret.mgt.core.model.Secret;
import org.wso2.carbon.identity.secret.mgt.core.model.SecretType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class ActionSecretProcessorTest {

    private static final String SECRET_TYPE = "ACTION_API_ENDPOINT_AUTH_SECRETS";
    private static final String SECRET_TYPE_ID = "secret-type-id";
    private static final String ACTION_ID = "actionId";
    private static final String AUTH_TYPE = "CLIENT_CREDENTIAL";
    private static final String PROPERTY_NAME = "internalAccessToken";
    private static final String EXPECTED_SECRET_NAME = ACTION_ID + ":" + AUTH_TYPE + ":" + PROPERTY_NAME;

    private SecretManager secretManager;
    private SecretResolveManager secretResolveManager;
    private ActionSecretProcessor actionSecretProcessor;

    @BeforeMethod
    public void setUp() throws SecretManagementException {

        secretManager = mock(SecretManager.class);
        secretResolveManager = mock(SecretResolveManager.class);

        SecretType secretType = mock(SecretType.class);
        when(secretType.getId()).thenReturn(SECRET_TYPE_ID);
        when(secretManager.getSecretType(SECRET_TYPE)).thenReturn(secretType);

        ActionExecutionServiceComponentHolder.getInstance().setSecretManager(secretManager);
        ActionExecutionServiceComponentHolder.getInstance().setSecretResolveManager(secretResolveManager);

        actionSecretProcessor = new ActionSecretProcessor();
    }

    @Test
    public void testEncryptPropertyAddsNewSecretWhenSecretDoesNotExist() throws SecretManagementException {

        when(secretManager.isSecretExist(SECRET_TYPE, EXPECTED_SECRET_NAME)).thenReturn(false);

        AuthProperty input = new AuthProperty.AuthPropertyBuilder()
                .name(PROPERTY_NAME)
                .value("plain-token")
                .isConfidential(true)
                .build();

        AuthProperty encrypted = actionSecretProcessor.encryptProperty(input, AUTH_TYPE, ACTION_ID);

        verify(secretManager, times(1)).addSecret(eq(SECRET_TYPE), any(Secret.class));
        verify(secretManager, never()).updateSecretValue(any(), any(), any());

        assertNotNull(encrypted);
        assertEquals(encrypted.getName(), PROPERTY_NAME);
        assertTrue(encrypted.getIsConfidential());
        // Returned value is a secret reference, not the raw value.
        assertEquals(encrypted.getValue(), SECRET_TYPE_ID + ":" + EXPECTED_SECRET_NAME);
    }

    @Test
    public void testEncryptPropertyUpdatesSecretWhenValueChanged() throws SecretManagementException {

        when(secretManager.isSecretExist(SECRET_TYPE, EXPECTED_SECRET_NAME)).thenReturn(true);
        ResolvedSecret resolvedSecret = mock(ResolvedSecret.class);
        when(resolvedSecret.getResolvedSecretValue()).thenReturn("old-token");
        when(secretResolveManager.getResolvedSecret(SECRET_TYPE, EXPECTED_SECRET_NAME)).thenReturn(resolvedSecret);

        AuthProperty input = new AuthProperty.AuthPropertyBuilder()
                .name(PROPERTY_NAME)
                .value("new-token")
                .isConfidential(true)
                .build();

        AuthProperty encrypted = actionSecretProcessor.encryptProperty(input, AUTH_TYPE, ACTION_ID);

        verify(secretManager, times(1)).updateSecretValue(SECRET_TYPE, EXPECTED_SECRET_NAME, "new-token");
        verify(secretManager, never()).addSecret(any(), any());
        assertEquals(encrypted.getValue(), SECRET_TYPE_ID + ":" + EXPECTED_SECRET_NAME);
    }

    @Test
    public void testEncryptPropertySkipsUpdateWhenValueUnchanged() throws SecretManagementException {

        when(secretManager.isSecretExist(SECRET_TYPE, EXPECTED_SECRET_NAME)).thenReturn(true);
        ResolvedSecret resolvedSecret = mock(ResolvedSecret.class);
        when(resolvedSecret.getResolvedSecretValue()).thenReturn("same-token");
        when(secretResolveManager.getResolvedSecret(SECRET_TYPE, EXPECTED_SECRET_NAME)).thenReturn(resolvedSecret);

        AuthProperty input = new AuthProperty.AuthPropertyBuilder()
                .name(PROPERTY_NAME)
                .value("same-token")
                .isConfidential(true)
                .build();

        actionSecretProcessor.encryptProperty(input, AUTH_TYPE, ACTION_ID);

        verify(secretManager, never()).updateSecretValue(any(), any(), any());
        verify(secretManager, never()).addSecret(any(), any());
    }

    @Test(expectedExceptions = SecretManagementException.class)
    public void testEncryptPropertyPropagatesSecretManagementException() throws SecretManagementException {

        when(secretManager.isSecretExist(SECRET_TYPE, EXPECTED_SECRET_NAME))
                .thenThrow(new SecretManagementException("error", "fail"));

        AuthProperty input = new AuthProperty.AuthPropertyBuilder()
                .name(PROPERTY_NAME)
                .value("any")
                .isConfidential(true)
                .build();

        actionSecretProcessor.encryptProperty(input, AUTH_TYPE, ACTION_ID);
    }
}
