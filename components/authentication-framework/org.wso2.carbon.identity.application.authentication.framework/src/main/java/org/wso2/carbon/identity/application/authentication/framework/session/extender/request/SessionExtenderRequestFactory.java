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

package org.wso2.carbon.identity.application.authentication.framework.session.extender.request;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.inbound.FrameworkClientException;
import org.wso2.carbon.identity.application.authentication.framework.inbound.HttpIdentityRequestFactory;
import org.wso2.carbon.identity.application.authentication.framework.inbound.HttpIdentityResponse;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityRequest;
import org.wso2.carbon.identity.application.authentication.framework.session.extender.SessionExtenderConstants;
import org.wso2.carbon.identity.application.authentication.framework.session.extender.exception.SessionExtenderClientException;
import org.wso2.carbon.identity.application.authentication.framework.session.extender.response.SessionExtenderErrorResponse;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;

import java.util.regex.Matcher;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.wso2.carbon.identity.application.authentication.framework.session.extender.SessionExtenderConstants.APPLICATION_JSON;
import static org.wso2.carbon.identity.application.authentication.framework.session.extender.SessionExtenderConstants.ERROR_LOG_TEMPLATE;
import static org.wso2.carbon.identity.application.authentication.framework.session.extender.SessionExtenderConstants.SESSION_EXTENDER_ENDPOINT;
import static org.wso2.carbon.identity.application.authentication.framework.session.extender.SessionExtenderConstants.SESSION_ID_PARAM_NAME;

/**
 * Factory for Session Extender requests.
 */
public class SessionExtenderRequestFactory extends HttpIdentityRequestFactory {

    private static final Log log = LogFactory.getLog(SessionExtenderRequestFactory.class);

    public static final String CORRELATION_ID_MDC = "Correlation-ID";
    public static final String TRACE_ID = "faaabef8-df76-408a-aa54-808858c250be";


    public SessionExtenderRequestFactory() {

        super();
    }

    @Override
    public String getName() {

        return "SessionExtenderRequestFactory";
    }

    @Override
    public boolean canHandle(HttpServletRequest request, HttpServletResponse response) {

        boolean canHandle = false;
        if (request != null) {
            Matcher matcher = SESSION_EXTENDER_ENDPOINT.matcher(request.getRequestURI());
            canHandle = matcher.matches();
            if (canHandle && log.isDebugEnabled()) {
                log.debug("canHandle evaluated as true for SessionExtenderRequestFactory.");
            }
        }
        return canHandle;
    }

    @Override
    public IdentityRequest.IdentityRequestBuilder create(HttpServletRequest request, HttpServletResponse response)
            throws FrameworkClientException {

        if (log.isDebugEnabled()) {
            log.debug("SessionExtenderRequest creation initiated by the factory.");
        }

        SessionExtenderRequest.SessionExtenderRequestBuilder builder = new SessionExtenderRequest.
                SessionExtenderRequestBuilder(request, response);
        super.create(builder, request, response);

        String sessionKeyValue = request.getParameter(SESSION_ID_PARAM_NAME);
        if (sessionKeyValue != null) {
            builder.setSessionKey(sessionKeyValue);
        }

        Cookie commonAuthCookie = FrameworkUtils.getAuthCookie(request);
        if (commonAuthCookie != null) {
            builder.setSessionCookie(commonAuthCookie);
        }

        if (sessionKeyValue == null && commonAuthCookie == null) {
            throw new SessionExtenderClientException(SessionExtenderConstants.Error.INVALID_REQUEST.getCode(),
                    SessionExtenderConstants.Error.INVALID_REQUEST.getMessage(),
                    "No session identifier parameter or cookie present in request.");
        }

        return builder;
    }

    @Override
    public HttpIdentityResponse.HttpIdentityResponseBuilder handleException(FrameworkClientException exception,
                                                                            HttpServletRequest request,
                                                                            HttpServletResponse response) {

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

        if (log.isDebugEnabled()) {
            String clientErrorLog = String.format(ERROR_LOG_TEMPLATE, traceId,
                    SessionExtenderRequestFactory.class.getName(), errorLogDescription);
            log.debug(clientErrorLog);
        }

        SessionExtenderErrorResponse sessionExtenderErrorResponse = sessionErrorResponseBuilder.build();
        errorResponseBuilder.setStatusCode(SC_BAD_REQUEST);
        errorResponseBuilder.setContentType(APPLICATION_JSON);
        errorResponseBuilder.setBody(sessionExtenderErrorResponse.getResponse());
        return errorResponseBuilder;
    }
}
