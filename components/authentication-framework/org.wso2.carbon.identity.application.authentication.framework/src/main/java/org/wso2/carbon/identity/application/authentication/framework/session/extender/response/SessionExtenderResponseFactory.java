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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.annotation.bundle.Capability;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.inbound.HttpIdentityResponse;
import org.wso2.carbon.identity.application.authentication.framework.inbound.HttpIdentityResponseFactory;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityResponse;
import org.wso2.carbon.identity.application.authentication.framework.session.extender.SessionExtenderConstants;
import org.wso2.carbon.identity.application.authentication.framework.session.extender.exception.SessionExtenderClientException;
import org.wso2.carbon.identity.application.authentication.framework.session.extender.exception.SessionExtenderServerException;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_CONFLICT;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.wso2.carbon.identity.application.authentication.framework.session.extender.SessionExtenderConstants.APPLICATION_JSON;
import static org.wso2.carbon.identity.application.authentication.framework.session.extender.SessionExtenderConstants.ERROR_LOG_TEMPLATE;
import static org.wso2.carbon.identity.application.authentication.framework.session.extender.SessionExtenderConstants.TRACE_ID_HEADER_NAME;

/**
 * Factory for Session Extender responses.
 */
@Capability(
        namespace = "osgi.service",
        attribute = {
                "objectClass=org.wso2.carbon.identity.application.authentication.framework.inbound." +
                        "HttpIdentityResponseFactory",
                "service.scope=singleton"
        }
)
public class SessionExtenderResponseFactory extends HttpIdentityResponseFactory {

    private static final Log log = LogFactory.getLog(SessionExtenderResponseFactory.class);

    @Override
    public boolean canHandle(IdentityResponse identityResponse) {

        boolean canHandle = identityResponse instanceof SessionExtenderResponse;
        if (canHandle && log.isDebugEnabled()) {
            log.debug("canHandle evaluated as true for SessionExtenderResponseFactory.");
        }
        return canHandle;
    }

    @Override
    public boolean canHandle(FrameworkException exception) {

        boolean canHandle = exception instanceof SessionExtenderClientException ||
                exception instanceof SessionExtenderServerException;
        if (canHandle && log.isDebugEnabled()) {
            log.debug("canHandle evaluated as true for SessionExtenderResponseFactory.");
        }
        return canHandle;
    }

    @Override
    public HttpIdentityResponse.HttpIdentityResponseBuilder create(IdentityResponse identityResponse) {

        HttpIdentityResponse.HttpIdentityResponseBuilder responseBuilder =
                new HttpIdentityResponse.HttpIdentityResponseBuilder();
        create(responseBuilder, identityResponse);
        return responseBuilder;
    }

    @Override
    public void create(HttpIdentityResponse.HttpIdentityResponseBuilder builder, IdentityResponse identityResponse) {

        builder.setStatusCode(SC_OK);
        if (identityResponse instanceof SessionExtenderResponse) {
            SessionExtenderResponse sessionExtenderResponse = (SessionExtenderResponse) identityResponse;
            if (sessionExtenderResponse.getTraceId() != null) {
                builder.addHeader(TRACE_ID_HEADER_NAME, sessionExtenderResponse.getTraceId());
            }
        }
    }

    @Override
    public HttpIdentityResponse.HttpIdentityResponseBuilder handleException(FrameworkException exception) {

        HttpIdentityResponse.HttpIdentityResponseBuilder errorResponseBuilder =
                new HttpIdentityResponse.HttpIdentityResponseBuilder();

        SessionExtenderErrorResponse.SessionExtenderErrorResponseBuilder sessionErrorResponseBuilder =
                new SessionExtenderErrorResponse.SessionExtenderErrorResponseBuilder();
        sessionErrorResponseBuilder.setErrorCode(exception.getErrorCode());

        String traceId = FrameworkUtils.getCorrelation();
        String errorLogDescription;
        if (exception instanceof SessionExtenderClientException) {
            sessionErrorResponseBuilder.setErrorMessage(((SessionExtenderClientException) exception).getErrorMessage());
            sessionErrorResponseBuilder.setErrorDescription(
                    ((SessionExtenderClientException) exception).getDescription());
            sessionErrorResponseBuilder.setTraceId(traceId);
            errorLogDescription = ((SessionExtenderClientException) exception).getDescription();
        } else {
            sessionErrorResponseBuilder.setErrorMessage(exception.getMessage());
            errorLogDescription = exception.getMessage();
        }

        SessionExtenderErrorResponse sessionExtenderErrorResponse = sessionErrorResponseBuilder.build();

        if (exception instanceof SessionExtenderClientException) {
            if (exception.getErrorCode().equals(SessionExtenderConstants.Error.CONFLICT.getCode())) {
                errorResponseBuilder.setStatusCode(SC_CONFLICT);
            } else {
                errorResponseBuilder.setStatusCode(SC_BAD_REQUEST);
            }
        } else {
            errorResponseBuilder.setStatusCode(SC_INTERNAL_SERVER_ERROR);
        }

        if (log.isDebugEnabled()) {
            String clientErrorLog = String.format(ERROR_LOG_TEMPLATE, traceId,
                    SessionExtenderResponseFactory.class.getName(), errorLogDescription);
            log.debug(clientErrorLog);
        }

        errorResponseBuilder.setContentType(APPLICATION_JSON);
        errorResponseBuilder.setBody(sessionExtenderErrorResponse.getResponse());
        return errorResponseBuilder;
    }
}
