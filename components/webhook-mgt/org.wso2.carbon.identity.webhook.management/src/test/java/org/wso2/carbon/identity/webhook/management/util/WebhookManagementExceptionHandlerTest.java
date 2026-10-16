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

package org.wso2.carbon.identity.webhook.management.util;

import org.testng.annotations.Test;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtClientException;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtServerException;
import org.wso2.carbon.identity.webhook.management.internal.constant.ErrorMessage;
import org.wso2.carbon.identity.webhook.management.internal.util.WebhookManagementExceptionHandler;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link WebhookManagementExceptionHandler}.
 */
public class WebhookManagementExceptionHandlerTest {

    private static final ErrorMessage ERROR_WITH_PLACEHOLDER = ErrorMessage.ERROR_CODE_WEBHOOK_NOT_FOUND;
    private static final String WEBHOOK_ID = "d4f1a2b3-c5e6-7890-1234-abcdef567890";

    @Test
    public void testClientExceptionCarriesCodeMessageAndDescription() {

        WebhookMgtClientException exception =
                WebhookManagementExceptionHandler.handleClientException(ERROR_WITH_PLACEHOLDER, WEBHOOK_ID);

        assertNotNull(exception);
        assertEquals(exception.getErrorCode(), ERROR_WITH_PLACEHOLDER.getCode());
        assertEquals(exception.getMessage(), ERROR_WITH_PLACEHOLDER.getMessage());
        assertTrue(exception.getDescription().contains(WEBHOOK_ID));
        assertFalse(exception.getDescription().contains("%s"));
    }

    /**
     * With no data the description is passed through unformatted. Calling String.format on it would either throw
     * or consume the placeholder, so the raw description is what a caller with nothing to substitute gets.
     */
    @Test
    public void testClientExceptionWithoutDataLeavesTheDescriptionUnformatted() {

        WebhookMgtClientException noVarargs =
                WebhookManagementExceptionHandler.handleClientException(ERROR_WITH_PLACEHOLDER);
        WebhookMgtClientException emptyVarargs =
                WebhookManagementExceptionHandler.handleClientException(ERROR_WITH_PLACEHOLDER, new String[0]);

        assertEquals(noVarargs.getDescription(), ERROR_WITH_PLACEHOLDER.getDescription());
        assertEquals(emptyVarargs.getDescription(), ERROR_WITH_PLACEHOLDER.getDescription());
    }

    @Test
    public void testServerExceptionCarriesCodeMessageAndDescription() {

        WebhookMgtServerException exception =
                WebhookManagementExceptionHandler.handleServerException(ERROR_WITH_PLACEHOLDER, WEBHOOK_ID);

        assertNotNull(exception);
        assertEquals(exception.getErrorCode(), ERROR_WITH_PLACEHOLDER.getCode());
        assertEquals(exception.getMessage(), ERROR_WITH_PLACEHOLDER.getMessage());
        assertTrue(exception.getDescription().contains(WEBHOOK_ID));
        assertNull(exception.getCause());
    }

    /**
     * The overload taking a throwable keeps it as the cause, so the stack trace of whatever failed underneath
     * survives into the server exception rather than being replaced by it.
     */
    @Test
    public void testServerExceptionKeepsTheGivenCause() {

        Throwable cause = new IllegalStateException("datasource unavailable");

        WebhookMgtServerException exception = WebhookManagementExceptionHandler.handleServerException(
                ERROR_WITH_PLACEHOLDER, cause, WEBHOOK_ID);

        assertSame(exception.getCause(), cause);
        assertEquals(exception.getErrorCode(), ERROR_WITH_PLACEHOLDER.getCode());
        assertTrue(exception.getDescription().contains(WEBHOOK_ID));
    }

    @Test
    public void testServerExceptionWithCauseAndNoDataLeavesTheDescriptionUnformatted() {

        Throwable cause = new IllegalStateException("datasource unavailable");

        WebhookMgtServerException exception =
                WebhookManagementExceptionHandler.handleServerException(ERROR_WITH_PLACEHOLDER, cause);

        assertEquals(exception.getDescription(), ERROR_WITH_PLACEHOLDER.getDescription());
        assertSame(exception.getCause(), cause);
    }

    /**
     * Every error message is usable by every overload: the code, message and description are all non-blank, so
     * no error reaches a caller as a partially populated exception.
     * <p>
     * Two arguments are supplied because the widest description takes two, and String.format ignores arguments a
     * description does not use. That makes this a guard on the descriptions as well: an error message added with
     * more placeholders than arguments supplied here fails with MissingFormatArgumentException rather than
     * reaching a caller and throwing at the moment it is raised.
     */
    @Test
    public void testEveryErrorMessageProducesAPopulatedException() {

        for (ErrorMessage error : ErrorMessage.values()) {
            WebhookMgtServerException exception =
                    WebhookManagementExceptionHandler.handleServerException(error, WEBHOOK_ID, WEBHOOK_ID);

            assertNotNull(exception.getErrorCode(), "No code for " + error.name());
            assertFalse(exception.getErrorCode().isEmpty(), "Empty code for " + error.name());
            assertNotNull(exception.getMessage(), "No message for " + error.name());
            assertNotNull(exception.getDescription(), "No description for " + error.name());
        }
    }
}
