/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl;

import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthHistory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;

import static org.mockito.Mockito.mock;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Test
public class GraphBasedSequenceHandlerAmrTest extends GraphBasedSequenceHandlerAbstractTest {

    public void testHandle_Static1_Javascript_Amr() throws Exception {
        AuthenticationContext context = executeWithAmrArray(new String[] { "pwd", });

        List<AuthHistory> authHistories = context.getAuthenticationStepHistory();
        assertNotNull(authHistories);
        assertEquals(1, authHistories.size());

        context = executeWithAmrArray(new String[] { "pwd", "hwk", });

        authHistories = context.getAuthenticationStepHistory();
        assertNotNull(authHistories);
        assertEquals(2, authHistories.size());

        context = executeWithAmrArray(new String[] { "pwd", "fpt", });

        authHistories = context.getAuthenticationStepHistory();
        assertNotNull(authHistories);
        assertEquals(2, authHistories.size());

        context = executeWithAmrArray(new String[] { "pwd", "fpt", "hwk" });

        authHistories = context.getAuthenticationStepHistory();
        assertNotNull(authHistories);
        assertEquals(3, authHistories.size());

        assertEquals( "FptMockAuthenticator",
                authHistories.get(1).getAuthenticatorName(), "Second Authenticator should be Fingerprint");

        assertEquals( "HwkMockAuthenticator",
                authHistories.get(2).getAuthenticatorName(), "Third Authenticator should be Hardware Key");


        context = executeWithAmrArray(new String[] { "pwd", "hwk", "fpt" });

        authHistories = context.getAuthenticationStepHistory();
        assertNotNull(authHistories);
        assertEquals(3, authHistories.size());

        assertEquals( "HwkMockAuthenticator",
                authHistories.get(1).getAuthenticatorName(), "Second Authenticator should be Hardware Key");

        assertEquals( "FptMockAuthenticator",
                authHistories.get(2).getAuthenticatorName(), "Third Authenticator should be Fingerprint");

    }

    private AuthenticationContext executeWithAmrArray(String[] amrArray)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, FrameworkException,
            XMLStreamException {
        ServiceProvider sp1 = getTestServiceProvider("js-sp-amr-static-1.xml");

        AuthenticationContext context = getAuthenticationContext("", APPLICATION_AUTHENTICATION_FILE_NAME, sp1);
        if (amrArray != null) {
            for (String amr : amrArray) {
                context.getAmrRequested().add(amr);
            }
        }

        SequenceConfig sequenceConfig = configurationLoader
                .getSequenceConfig(context, Collections.<String, String[]>emptyMap(), sp1);
        context.setSequenceConfig(sequenceConfig);

        HttpServletRequest req = mock(HttpServletRequest.class);

        HttpServletResponse resp = mock(HttpServletResponse.class);

        UserCoreUtil.setDomainInThreadLocal("test_domain");

        graphBasedSequenceHandler.handle(req, resp, context);
        return context;
    }

}