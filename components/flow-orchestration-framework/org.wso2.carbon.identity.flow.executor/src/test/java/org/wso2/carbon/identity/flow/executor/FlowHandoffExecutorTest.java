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

package org.wso2.carbon.identity.flow.executor;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.flow.execution.engine.model.ExecutorResponse;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.executor.FlowExecutorConstants.ErrorMessages;
import org.wso2.carbon.identity.flow.mgt.model.ExecutorDTO;
import org.wso2.carbon.identity.flow.mgt.model.NodeConfig;

import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorStatus.STATUS_COMPLETE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorStatus.STATUS_EXTERNAL_REDIRECTION;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorStatus.STATUS_USER_ERROR;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.OTFI;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.REDIRECT_URL;
import static org.wso2.carbon.identity.flow.executor.FlowExecutorConstants.ErrorMessages.ERROR_CODE_HANDOFF_EXPIRED;
import static org.wso2.carbon.identity.flow.executor.FlowExecutorConstants.ErrorMessages.ERROR_CODE_INVALID_HANDOFF;
import static org.wso2.carbon.identity.flow.executor.FlowExecutorConstants.FlowHandoff.EXPIRY_TIME_METADATA_KEY;
import static org.wso2.carbon.identity.flow.executor.FlowExecutorConstants.FlowHandoff.FLOW_HANDOFF_CREATED_TIMESTAMP;
import static org.wso2.carbon.identity.flow.executor.FlowExecutorConstants.FlowHandoff.FLOW_HANDOFF_EXECUTOR;
import static org.wso2.carbon.identity.flow.executor.FlowExecutorConstants.FlowHandoff.HANDOFF;
import static org.wso2.carbon.identity.flow.executor.FlowExecutorConstants.FlowHandoff.TARGET_URL_METADATA_KEY;

/**
 * Unit tests for {@link FlowHandoffExecutor}.
 */
public class FlowHandoffExecutorTest {

    private static final String PORTAL_URL = "https://localhost:9443/accounts/execution-flow.jsp";
    private static final String STEP_TARGET_URL = "https://app.example.com/continue";
    private static final String FLOW_ID = "test-flow-id";

    private FlowHandoffExecutor executor;
    private FlowExecutionContext context;

    @BeforeMethod
    public void setUp() {

        executor = new FlowHandoffExecutor();
        context = new FlowExecutionContext();
        context.setContextIdentifier(FLOW_ID);
        context.setPortalUrl(PORTAL_URL);
        context.setCurrentNode(buildNode(null));
    }

    @Test
    public void testGetName() {

        assertEquals(executor.getName(), FLOW_HANDOFF_EXECUTOR);
    }

    @Test
    public void testGetInitiationData() {

        assertTrue(executor.getInitiationData().isEmpty());
    }

    @Test
    public void testRollback() {

        assertNull(executor.rollback(context));
    }

    @Test
    public void testIssueHandoff() {

        long before = System.currentTimeMillis();
        ExecutorResponse response = executor.execute(context);
        long after = System.currentTimeMillis();

        assertEquals(response.getResult(), STATUS_EXTERNAL_REDIRECTION);
        assertNull(response.getRequiredData());
        assertEquals(response.getAdditionalInfo().get(HANDOFF), "true");

        String otfi = (String) response.getContextProperties().get(OTFI);
        assertNotNull(otfi);
        assertNotEquals(otfi, FLOW_ID);
        assertEquals(response.getAdditionalInfo().get(REDIRECT_URL),
                PORTAL_URL + "?flowId=" + otfi + "&handoff=true");

        long createdTimestamp =
                Long.parseLong((String) response.getContextProperties().get(FLOW_HANDOFF_CREATED_TIMESTAMP));
        assertTrue(createdTimestamp >= before && createdTimestamp <= after);
    }

    @Test
    public void testIssueHandoffWithQueryInPortalUrl() {

        context.setPortalUrl(PORTAL_URL + "?spId=abc");

        ExecutorResponse response = executor.execute(context);

        String url = response.getAdditionalInfo().get(REDIRECT_URL);
        assertTrue(url.startsWith(PORTAL_URL + "?spId=abc&flowId="));
    }

    @Test
    public void testIssueHandoffWithStepTargetUrl() {

        context.setCurrentNode(buildNode(null, STEP_TARGET_URL));

        ExecutorResponse response = executor.execute(context);

        String otfi = (String) response.getContextProperties().get(OTFI);
        assertEquals(response.getAdditionalInfo().get(REDIRECT_URL),
                STEP_TARGET_URL + "?flowId=" + otfi + "&handoff=true");
    }

    @Test
    public void testIssueHandoffWithStepTargetUrlWithQuery() {

        context.setCurrentNode(buildNode(null, STEP_TARGET_URL + "?lang=en"));

        ExecutorResponse response = executor.execute(context);

        String url = response.getAdditionalInfo().get(REDIRECT_URL);
        assertTrue(url.startsWith(STEP_TARGET_URL + "?lang=en&flowId="));
    }

    @Test
    public void testIssueHandoffWithBlankStepTargetUrl() {

        context.setCurrentNode(buildNode(null, " "));

        ExecutorResponse response = executor.execute(context);

        String url = response.getAdditionalInfo().get(REDIRECT_URL);
        assertTrue(url.startsWith(PORTAL_URL + "?flowId="));
    }

    @Test
    public void testResumeBeforeExpiry() {

        issueAndStore();

        ExecutorResponse response = executor.execute(context);

        assertEquals(response.getResult(), STATUS_COMPLETE);
        assertFalse(context.getProperties().containsKey(FLOW_HANDOFF_CREATED_TIMESTAMP));
    }

    @DataProvider(name = "expiryTimeData")
    public Object[][] expiryTimeData() {

        return new Object[][]{
                {"120", 120L},
                {null, 60L},
                {"invalid", 60L},
                {"-5", 60L},
                {"0", 60L}
        };
    }

    @Test(dataProvider = "expiryTimeData")
    public void testResumeHonoursExpiryTime(String expiryTime, long expectedSeconds) {

        context.setCurrentNode(buildNode(expiryTime));

        setCreatedTimestamp(System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(expectedSeconds) + 5000);
        assertEquals(executor.execute(context).getResult(), STATUS_COMPLETE);

        setCreatedTimestamp(System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(expectedSeconds) - 1000);
        assertUserErrorAndCreatedTimestampRemoved(executor.execute(context), ERROR_CODE_HANDOFF_EXPIRED);
    }

    @Test
    public void testResumeWithInvalidCreatedTimestamp() {

        context.setProperty(FLOW_HANDOFF_CREATED_TIMESTAMP, "invalid");

        assertUserErrorAndCreatedTimestampRemoved(executor.execute(context), ERROR_CODE_INVALID_HANDOFF);
    }

    @Test
    public void testResumeIsSingleUse() {

        setCreatedTimestamp(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5));
        assertUserErrorAndCreatedTimestampRemoved(executor.execute(context), ERROR_CODE_HANDOFF_EXPIRED);

        // With the created timestamp removed, the next call issues a new link instead of resuming.
        assertEquals(executor.execute(context).getResult(), STATUS_EXTERNAL_REDIRECTION);
    }

    private NodeConfig buildNode(String expiryTime) {

        return buildNode(expiryTime, null);
    }

    private NodeConfig buildNode(String expiryTime, String targetUrl) {

        ExecutorDTO executorDTO = new ExecutorDTO(FLOW_HANDOFF_EXECUTOR);
        if (expiryTime != null) {
            executorDTO.addMetadata(EXPIRY_TIME_METADATA_KEY, expiryTime);
        }
        if (targetUrl != null) {
            executorDTO.addMetadata(TARGET_URL_METADATA_KEY, targetUrl);
        }
        return new NodeConfig.Builder().id("handoff").executorConfig(executorDTO).build();
    }

    private void issueAndStore() {

        ExecutorResponse response = executor.execute(context);
        context.addProperties(response.getContextProperties());
    }

    private void setCreatedTimestamp(long createdTimestamp) {

        context.setProperty(FLOW_HANDOFF_CREATED_TIMESTAMP, String.valueOf(createdTimestamp));
    }

    private void assertUserErrorAndCreatedTimestampRemoved(ExecutorResponse response, ErrorMessages error) {

        assertEquals(response.getResult(), STATUS_USER_ERROR);
        assertEquals(response.getErrorCode(), error.getCode());
        assertEquals(response.getErrorMessage(), error.getMessage());
        assertEquals(response.getErrorDescription(), error.getDescription());
        assertFalse(context.getProperties().containsKey(FLOW_HANDOFF_CREATED_TIMESTAMP));
    }
}
