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

package org.wso2.carbon.identity.ai.service.mgt.constants;

import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Test class for AIConstants.
 */
public class AIConstantsTest {

    @Test
    public void testErrorMessages() {
        AIConstants.ErrorMessages errorMessage = AIConstants.ErrorMessages.MAXIMUM_RETRIES_EXCEEDED;
        assertEquals(errorMessage.getCode(), "AI-10000");
        assertEquals(errorMessage.getMessage(), "Maximum retries exceeded to retrieve the access token.");
        assertEquals(errorMessage.toString(), "AI-10000:Maximum retries exceeded to retrieve the access token.");

        errorMessage = AIConstants.ErrorMessages.UNABLE_TO_ACCESS_AI_SERVICE_WITH_RENEW_ACCESS_TOKEN;
        assertEquals(errorMessage.getCode(), "AI-10003");
        assertEquals(errorMessage.getMessage(), "Unable to access the AI service with the renewed access token.");
        assertEquals(errorMessage.toString(), "AI-10003:Unable to access the AI service with " +
                "the renewed access token.");

        errorMessage = AIConstants.ErrorMessages.REQUEST_TIMEOUT;
        assertEquals(errorMessage.getCode(), "AI-10004");
        assertEquals(errorMessage.getMessage(), "Request to the AI service timed out.");
        assertEquals(errorMessage.toString(), "AI-10004:Request to the AI service timed out.");

        errorMessage = AIConstants.ErrorMessages.ERROR_RETRIEVING_ACCESS_TOKEN;
        assertEquals(errorMessage.getCode(), "AI-10007");
        assertEquals(errorMessage.getMessage(), "Error occurred while retrieving the access token.");
        assertEquals(errorMessage.toString(), "AI-10007:Error occurred while retrieving the access token.");

        errorMessage = AIConstants.ErrorMessages.CLIENT_ERROR_WHILE_CONNECTING_TO_AI_SERVICE;
        assertEquals(errorMessage.getCode(), "AI-10008");
        assertEquals(errorMessage.getMessage(), "Client error occurred for %s tenant while connecting to AI service.");
        assertEquals(errorMessage.toString(), "AI-10008:Client error occurred for %s tenant while" +
                " connecting to AI service.");

        errorMessage = AIConstants.ErrorMessages.SERVER_ERROR_WHILE_CONNECTING_TO_AI_SERVICE;
        assertEquals(errorMessage.getCode(), "AI-10009");
        assertEquals(errorMessage.getMessage(), "Server error occurred for %s tenant while connecting to AI service.");
        assertEquals(errorMessage.toString(), "AI-10009:Server error occurred for %s tenant while " +
                "connecting to AI service.");
    }
}
