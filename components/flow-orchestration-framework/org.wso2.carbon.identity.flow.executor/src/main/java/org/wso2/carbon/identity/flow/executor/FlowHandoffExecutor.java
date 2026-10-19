/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.flow.executor;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.flow.execution.engine.graph.Executor;
import org.wso2.carbon.identity.flow.execution.engine.model.ExecutorResponse;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.executor.FlowExecutorConstants.ErrorMessages;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorStatus.STATUS_COMPLETE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorStatus.STATUS_EXTERNAL_REDIRECTION;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.OTFI;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.REDIRECT_URL;
import static org.wso2.carbon.identity.flow.executor.FlowExecutorConstants.FLOW_ID;
import static org.wso2.carbon.identity.flow.executor.FlowExecutorConstants.FlowHandoff.DEFAULT_HANDOFF_EXPIRY_SECONDS;
import static org.wso2.carbon.identity.flow.executor.FlowExecutorConstants.FlowHandoff.EXPIRY_TIME_METADATA_KEY;
import static org.wso2.carbon.identity.flow.executor.FlowExecutorConstants.FlowHandoff.FLOW_HANDOFF_CREATED_TIMESTAMP;
import static org.wso2.carbon.identity.flow.executor.FlowExecutorConstants.FlowHandoff.FLOW_HANDOFF_EXECUTOR;
import static org.wso2.carbon.identity.flow.executor.FlowExecutorConstants.FlowHandoff.HANDOFF;
import static org.wso2.carbon.identity.flow.executor.FlowExecutorConstants.FlowHandoff.TARGET_URL_METADATA_KEY;
import static org.wso2.carbon.identity.flow.executor.util.FlowExecutorUtils.getMetadataValue;
import static org.wso2.carbon.identity.flow.executor.util.FlowExecutorUtils.userErrorResponse;

/**
 * Executor that pauses the flow and returns a short-lived, single-use link to continue it in a browser.
 */
public class FlowHandoffExecutor implements Executor {

    private static final Log LOG = LogFactory.getLog(FlowHandoffExecutor.class);

    @Override
    public String getName() {

        return FLOW_HANDOFF_EXECUTOR;
    }

    @Override
    public ExecutorResponse execute(FlowExecutionContext context) {

        ExecutorResponse response = new ExecutorResponse();
        Object createdTimestamp = context.getProperty(FLOW_HANDOFF_CREATED_TIMESTAMP);
        if (createdTimestamp == null) {
            return issueHandoff(context, response);
        }
        return verifyHandoff(context, response, createdTimestamp);
    }

    @Override
    public List<String> getInitiationData() {

        return Collections.emptyList();
    }

    @Override
    public ExecutorResponse rollback(FlowExecutionContext context) {

        return null;
    }

    // Enable with the new flow type.
    // @Override
    // public Set<FlowTypes> getSupportedFlowTypes() {
    //
    //     return EnumSet.of(FlowTypes.<NEW_FLOW_TYPE>);
    // }
    //
    // @Override
    // public FlowExecutorMetadata getExecutorMetadata() {
    //
    //     return FlowExecutorMetadata.builder()
    //             .displayName("Flow Handoff")
    //             .icon("assets/images/icons/arrow-up-right.svg")
    //             .build();
    // }

    private ExecutorResponse issueHandoff(FlowExecutionContext context, ExecutorResponse response) {

        String targetUrl = getTargetUrl(context);
        String otfi = UUID.randomUUID().toString();
        while (otfi.equals(context.getContextIdentifier())) {
            otfi = UUID.randomUUID().toString();
        }
        Map<String, Object> contextProperties = new HashMap<>();
        contextProperties.put(OTFI, otfi);
        contextProperties.put(FLOW_HANDOFF_CREATED_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
        response.setContextProperty(contextProperties);

        String separator = targetUrl.contains("?") ? "&" : "?";
        String url = targetUrl + separator + FLOW_ID + "=" + otfi + "&" + HANDOFF + "=true";

        Map<String, String> additionalInfo = new HashMap<>();
        additionalInfo.put(REDIRECT_URL, url);
        additionalInfo.put(HANDOFF, "true");
        response.setAdditionalInfo(additionalInfo);
        response.setResult(STATUS_EXTERNAL_REDIRECTION);
        return response;
    }

    private ExecutorResponse verifyHandoff(FlowExecutionContext context, ExecutorResponse response,
                                           Object createdTimestampValue) {

        context.getProperties().remove(FLOW_HANDOFF_CREATED_TIMESTAMP);
        long createdTimestamp;
        try {
            createdTimestamp = Long.parseLong(String.valueOf(createdTimestampValue));
        } catch (NumberFormatException e) {
            LOG.debug("Invalid flow handoff created timestamp.");
            return userErrorResponse(response, ErrorMessages.ERROR_CODE_INVALID_HANDOFF);
        }
        if (System.currentTimeMillis() - createdTimestamp >= TimeUnit.SECONDS.toMillis(getExpiryTime(context))) {
            LOG.debug("Flow handoff link has expired.");
            return userErrorResponse(response, ErrorMessages.ERROR_CODE_HANDOFF_EXPIRED);
        }

        response.setResult(STATUS_COMPLETE);
        return response;
    }

    private String getTargetUrl(FlowExecutionContext context) {

        String targetUrl = getMetadataValue(context, TARGET_URL_METADATA_KEY);
        if (StringUtils.isNotBlank(targetUrl)) {
            return targetUrl.trim();
        }
        return context.getPortalUrl() != null ? context.getPortalUrl() : "";
    }

    private long getExpiryTime(FlowExecutionContext context) {

        String expiryTime = getMetadataValue(context, EXPIRY_TIME_METADATA_KEY);
        if (StringUtils.isNotBlank(expiryTime)) {
            try {
                long expiry = Long.parseLong(expiryTime.trim());
                if (expiry > 0) {
                    return expiry;
                }
            } catch (NumberFormatException e) {
                // Fall back to the default expiry time.
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Invalid flow handoff expiry time: " + expiryTime + ". Using the default value: "
                        + DEFAULT_HANDOFF_EXPIRY_SECONDS);
            }
        }
        return DEFAULT_HANDOFF_EXPIRY_SECONDS;
    }
}
