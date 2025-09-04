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

package org.wso2.carbon.identity.flow.execution.engine.util;

import com.nimbusds.jwt.JWTClaimsSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.UserAssertionUtils;
import org.wso2.carbon.identity.core.ServiceURLBuilder;
import org.wso2.carbon.identity.core.URLBuilderException;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineServerException;
import org.wso2.carbon.identity.flow.execution.engine.graph.AuthenticationExecutor;
import org.wso2.carbon.identity.flow.execution.engine.internal.FlowExecutionEngineDataHolder;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.USER_ID_CLAIM;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_AUTHENTICATION_ASSERTION_GENERATION_FAILURE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_FLOW_USER_NOT_FOUND;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.USER_ASSERTION_EXPIRY_PROPERTY;

/**
 * Utility class for generating authentication assertions.
 */
public class AuthenticationAssertionUtils {

    private static final Log LOG = LogFactory.getLog(AuthenticationAssertionUtils.class);
    private static final long DEFAULT_ASSERTION_LIFETIME_MS = 2000L;

    private AuthenticationAssertionUtils() {

    }

    /**
     * Generates a signed user assertion for the given flow execution context.
     *
     * @param context Flow execution context.
     * @return Signed user assertion as a string.
     * @throws FlowEngineServerException If an error occurs while generating the assertion.
     */
    public static String getSignedUserAssertion(FlowExecutionContext context)
            throws FlowEngineServerException {

        try {
            JWTClaimsSet claims = buildUserAssertionClaimSet(context);
            return UserAssertionUtils.generateSignedUserAssertion(claims, context.getTenantDomain());
        } catch (FrameworkException e) {
            throw FlowExecutionEngineUtils.handleServerException(context.getFlowType(),
                    ERROR_CODE_AUTHENTICATION_ASSERTION_GENERATION_FAILURE, e, context.getContextIdentifier());
        }
    }

    private static JWTClaimsSet buildUserAssertionClaimSet(FlowExecutionContext context)
            throws FlowEngineServerException {

        try {
            if (context.getFlowUser() == null) {
                throw FlowExecutionEngineUtils.handleServerException(context.getFlowType(),
                        ERROR_CODE_FLOW_USER_NOT_FOUND, context.getContextIdentifier());
            }
            long now = System.currentTimeMillis();
            Date issueTime = new Date(now);
            Date expirationTime = calculateUserAssertionExpiryTime(now);
            String serverURL = ServiceURLBuilder.create().build(IdentityUtil.getHostName()).getAbsolutePublicURL();
            String username = context.getFlowUser().getUsername();
            String userId = context.getFlowUser().getUserId();

            List<String> amrValues = context.getCompletedNodes().stream()
                    .map(node -> node.getExecutorConfig().getName())
                    .map(FlowExecutionEngineDataHolder.getInstance().getExecutors()::get)
                    .filter(AuthenticationExecutor.class::isInstance)
                    .map(executor -> ((AuthenticationExecutor) executor).getAMRValue())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            String fullQualifiedUsername = getFullQualifiedUsername(context, username);
            return new JWTClaimsSet.Builder()
                    .issuer(serverURL)
                    .audience(serverURL)
                    .subject(fullQualifiedUsername)
                    .issueTime(issueTime)
                    .notBeforeTime(issueTime)
                    .expirationTime(expirationTime)
                    .jwtID(UUID.randomUUID().toString())
                    .claim(FrameworkConstants.AMR, amrValues)
                    .claim(USER_ID_CLAIM, userId)
                    .claim(FrameworkConstants.USERNAME_CLAIM, username)
                    .build();
        } catch (URLBuilderException e) {
            throw FlowExecutionEngineUtils.handleServerException(ERROR_CODE_AUTHENTICATION_ASSERTION_GENERATION_FAILURE,
                    e, context.getContextIdentifier());
        }
    }

    private static String getFullQualifiedUsername(FlowExecutionContext context, String username) {

        String fullQualifiedUsername = UserCoreUtil.addTenantDomainToEntry(username, context.getTenantDomain());
        fullQualifiedUsername = UserCoreUtil.addDomainToName(fullQualifiedUsername,
                context.getFlowUser().getUserStoreDomain());
        return fullQualifiedUsername;
    }

    private static Date calculateUserAssertionExpiryTime(long currentTimeMillis) {

        long configuredLifetime = DEFAULT_ASSERTION_LIFETIME_MS;
        try {
            configuredLifetime = Long.parseLong(IdentityUtil.getProperty(USER_ASSERTION_EXPIRY_PROPERTY));
            if (configuredLifetime <= 0) {
                configuredLifetime = DEFAULT_ASSERTION_LIFETIME_MS;
            }
        } catch (NumberFormatException e) {
            LOG.warn(String.format("Invalid value for authentication assertion lifetime. Falling back to default %d ms",
                    DEFAULT_ASSERTION_LIFETIME_MS));
        }
        long expiryTime = currentTimeMillis + configuredLifetime;
        return new Date(expiryTime);
    }
}
