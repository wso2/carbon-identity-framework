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

package org.wso2.carbon.identity.application.authentication.framework.listener;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.exception.ConsentAppMappingException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.services.ConsentAppMappingService;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.testng.Assert.assertEquals;

/**
 * Unit tests for {@link ConsentAppMappingPurposeDeleteListener}.
 */
public class ConsentAppMappingPurposeDeleteListenerTest {

    private static final String PURPOSE_UUID = "purpose-uuid-1";
    private static final String TENANT_DOMAIN = "carbon.super";

    @Mock
    private ConsentAppMappingService consentAppMappingService;

    private ConsentAppMappingPurposeDeleteListener listener;
    private MockedStatic<FrameworkServiceDataHolder> dataHolderMock;

    @BeforeMethod
    public void setUp() {

        openMocks(this);
        listener = new ConsentAppMappingPurposeDeleteListener();

        FrameworkServiceDataHolder dataHolder = mock(FrameworkServiceDataHolder.class);
        dataHolderMock = mockStatic(FrameworkServiceDataHolder.class);
        dataHolderMock.when(FrameworkServiceDataHolder::getInstance).thenReturn(dataHolder);
        when(dataHolder.getConsentAppMappingService()).thenReturn(consentAppMappingService);
    }

    @AfterMethod
    public void tearDown() {

        dataHolderMock.close();
    }

    @Test
    public void testGetDefaultOrderId() {

        assertEquals(listener.getDefaultOrderId(), 100);
    }

    @Test
    public void testPostDeletePurpose_success() throws Exception {

        listener.postDeletePurpose(PURPOSE_UUID, TENANT_DOMAIN);

        verify(consentAppMappingService).removeAllApplicationMappingsForPurpose(PURPOSE_UUID);
    }

    @Test
    public void testPostDeletePurpose_exceptionIsWarned() throws Exception {

        doThrow(new ConsentAppMappingException("Test error"))
                .when(consentAppMappingService).removeAllApplicationMappingsForPurpose(PURPOSE_UUID);

        listener.postDeletePurpose(PURPOSE_UUID, TENANT_DOMAIN);
    }
}
