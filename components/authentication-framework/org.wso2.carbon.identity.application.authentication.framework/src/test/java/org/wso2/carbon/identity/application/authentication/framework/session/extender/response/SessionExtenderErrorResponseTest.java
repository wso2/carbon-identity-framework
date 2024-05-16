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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.session.extender.response;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.wso2.carbon.identity.application.authentication.framework.session.extender.SessionExtenderTestConstants.ERROR_RESPONSE_BODY;
import static org.wso2.carbon.identity.application.authentication.framework.session.extender.SessionExtenderTestConstants.EXCEPTION_DESCRIPTION;
import static org.wso2.carbon.identity.application.authentication.framework.session.extender.SessionExtenderTestConstants.EXCEPTION_ERROR_CODE;
import static org.wso2.carbon.identity.application.authentication.framework.session.extender.SessionExtenderTestConstants.EXCEPTION_MESSAGE;
import static org.wso2.carbon.identity.application.authentication.framework.session.extender.SessionExtenderTestConstants.TRACE_ID;

/**
 * Unit test cases for SessionExtenderResponse.
 */
public class SessionExtenderErrorResponseTest {

    @Test
    public void buildTestErrorResponse() {

        SessionExtenderErrorResponse.SessionExtenderErrorResponseBuilder builder =
                new SessionExtenderErrorResponse.SessionExtenderErrorResponseBuilder();
        builder.setErrorCode(EXCEPTION_ERROR_CODE);
        builder.setErrorMessage(EXCEPTION_MESSAGE);
        builder.setErrorDescription(EXCEPTION_DESCRIPTION);
        builder.setTraceId(TRACE_ID);
        SessionExtenderErrorResponse response = builder.build();
        assertNotNull(response.getResponse(), "Error creating failure response.");
        assertEquals(response.getResponse(), ERROR_RESPONSE_BODY, "Incorrect error response.");
    }
}
