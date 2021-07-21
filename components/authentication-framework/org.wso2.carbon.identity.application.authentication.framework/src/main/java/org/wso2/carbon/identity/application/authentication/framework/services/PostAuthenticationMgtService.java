/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.services;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.exception.PostAuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.PostAuthenticationHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.PostAuthnHandlerFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.authentication.framework.util.LoginContextManagementUtil;
import org.wso2.carbon.registry.core.utils.UUIDGenerator;

import java.util.List;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handles post authentication processors. Post authentication processors are registered as OSGI bundles.
 */
public class PostAuthenticationMgtService {

    private static final Log log = LogFactory.getLog(PostAuthenticationMgtService.class);

    /**
     * Handles post authentication upon an overall authentication event.
     *
     * @param request               HttpServletRequest.
     * @param response              HttpServletResponse.
     * @param authenticationContext Authentication context.
     * @throws FrameworkException FrameworkException.
     */
    public void handlePostAuthentication(HttpServletRequest request, HttpServletResponse response,
            AuthenticationContext authenticationContext) throws FrameworkException {

        if (log.isDebugEnabled()) {
            log.debug("Executing Post Authentication Management Service for context " + authenticationContext
                    .getContextIdentifier());
        }

        List<PostAuthenticationHandler> postAuthenticationHandlers = FrameworkServiceDataHolder.getInstance()
                .getPostAuthenticationHandlers();

        int currentPostHandlerIndex = authenticationContext.getCurrentPostAuthHandlerIndex();
        if (log.isDebugEnabled()) {
            log.debug("Starting from current post handler index " + currentPostHandlerIndex + " for context : "
                    + authenticationContext.getContextIdentifier());
        }

        if (isPostAuthenticationInProgress(authenticationContext, postAuthenticationHandlers,
                currentPostHandlerIndex)) {

            validatePASTRCookie(authenticationContext, request);
            // Need to set this before a handler does redirect. If a handler redirects there is no point in setting
            // cookie afterwards because the response is committed.
            setPASTRCookie(authenticationContext, request, response);
            for (; currentPostHandlerIndex < postAuthenticationHandlers.size(); currentPostHandlerIndex++) {
                PostAuthenticationHandler currentHandler = postAuthenticationHandlers.get(currentPostHandlerIndex);
                if (executePostAuthnHandler(request, response, authenticationContext, currentHandler)) {
                    request.setAttribute(FrameworkConstants.RequestParams.FLOW_STATUS,
                            AuthenticatorFlowStatus.INCOMPLETE);
                    return;
                }
            }

            endPostAuthenticationHandlerFlow(authenticationContext, request, response);
        } else {
            endPostAuthenticationHandlerFlow(authenticationContext, request, response);
        }

    }

    /**
     * @param request               Incoming HttpServletRequest.
     * @param response              HttpServletResponse.
     * @param authenticationContext Authentication context.
     * @param currentHandler        Current post authentication handler.
     * @return Whether this handler needs to be continued or not. True if the same handler needs to be continued,
     * else false.
     * @throws PostAuthenticationFailedException Post Authentication Failed Exception.
     */
    private boolean executePostAuthnHandler(HttpServletRequest request, HttpServletResponse response,
            AuthenticationContext authenticationContext, PostAuthenticationHandler currentHandler)
            throws PostAuthenticationFailedException {

        if (currentHandler.isEnabled()) {

            if (log.isDebugEnabled()) {
                log.debug(
                        currentHandler.getName() + " is enabled. Hence executing for context : " + authenticationContext
                                .getContextIdentifier());
            }
            PostAuthnHandlerFlowStatus flowStatus = currentHandler.handle(request, response, authenticationContext);
            if (log.isDebugEnabled()) {
                log.debug("Post authentication handler " + currentHandler.getName() + " returned with status : "
                        + flowStatus + " for context identifier : " + authenticationContext.getContextIdentifier());
            }

            if (isExecutionFinished(flowStatus)) {
                if (log.isDebugEnabled()) {
                    log.debug("Post authentication handler " + currentHandler.getName()
                            + " completed execution for session context : " + authenticationContext
                            .getContextIdentifier());
                }
                authenticationContext.setExecutedPostAuthHandler(currentHandler.getName());

            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Post authentication handler " + currentHandler.getName() + " is not completed yet. Hence"
                            + " returning for context : " + authenticationContext.getContextIdentifier());
                }
                return true;
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Post authentication handler " + currentHandler.getName() + " is disabled. Hence returning"
                        + " without executing for context : " + authenticationContext.getContextIdentifier());
            }
        }
        return false;
    }

    private boolean isExecutionFinished(PostAuthnHandlerFlowStatus flowStatus) {

        return PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED.equals(flowStatus)
                || PostAuthnHandlerFlowStatus.UNSUCCESS_COMPLETED.equals(flowStatus);
    }

    private boolean isPostAuthenticationInProgress(AuthenticationContext authenticationContext,
            List<PostAuthenticationHandler> postAuthenticationHandlers, int currentPostHandlerIndex) {

        return !LoginContextManagementUtil.isPostAuthenticationExtensionCompleted(authenticationContext)
                && postAuthenticationHandlers.size() > currentPostHandlerIndex;
    }

    private void markPostAuthenticationCompleted(AuthenticationContext authenticationContext) {

        if (log.isDebugEnabled()) {
            log.debug("Post authentication evaluation has completed for the flow with session data key : "
                    + authenticationContext.getContextIdentifier());
        }
        LoginContextManagementUtil.markPostAuthenticationCompleted(authenticationContext);
    }

    private void setPASTRCookie(AuthenticationContext context, HttpServletRequest request,
            HttpServletResponse response) {

        if (context.getParameter(FrameworkConstants.PASTR_COOKIE) != null) {
            if (log.isDebugEnabled()) {
                log.debug("PASTR cookie is already set to context : " + context.getContextIdentifier());
            }
            return;
        } else {
            if (log.isDebugEnabled()) {
                log.debug(
                        "PASTR cookie is not set to context : " + context.getContextIdentifier() + ". Hence setting the"
                                + " " + "cookie");
            }
            String pastrCookieValue = UUIDGenerator.generateUUID();
            FrameworkUtils
                    .setCookie(request, response, FrameworkUtils.getPASTRCookieName(context.getContextIdentifier()),
                            pastrCookieValue, -1);
            context.addParameter(FrameworkConstants.PASTR_COOKIE, pastrCookieValue);
        }
    }

    private void validatePASTRCookie(AuthenticationContext context, HttpServletRequest request)
            throws PostAuthenticationFailedException {

        Object pstrCookieObj = context.getParameter(FrameworkConstants.PASTR_COOKIE);
        if (pstrCookieObj != null) {
            String storedPastrCookieValue = (String) pstrCookieObj;
            Cookie pastrCookie = FrameworkUtils
                    .getCookie(request, FrameworkUtils.getPASTRCookieName(context.getContextIdentifier()));
            if (pastrCookie != null && StringUtils.equals(storedPastrCookieValue, pastrCookie.getValue())) {
                if (log.isDebugEnabled()) {
                    log.debug("pastr cookie validated successfully for sequence : " + context.getContextIdentifier());
                }
                return;
            } else {
                throw new PostAuthenticationFailedException(
                        "Invalid Request: Your authentication flow is ended or " + "invalid. Please initiate again.",
                        "Post authentication sequence tracking" + " cookie not found in request with context id : "
                                + context.getContextIdentifier());
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug(
                        "No stored pastr cookie found in authentication context for : " + context.getContextIdentifier()
                                + " . Hence returning without validating");
            }
        }
    }

    private void removePASTRCookie(HttpServletRequest request, HttpServletResponse response,
            AuthenticationContext context) {

        Object pstrCookieObj = context.getParameter(FrameworkConstants.PASTR_COOKIE);
        if (pstrCookieObj != null) {
            if (log.isDebugEnabled()) {
                log.debug("Removing post authentication sequnce tracker cookie for context : " + context
                        .getContextIdentifier());
            }
            FrameworkUtils
                    .setCookie(request, response, FrameworkUtils.getPASTRCookieName(context.getContextIdentifier()),
                            pstrCookieObj.toString(), 0);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("PASTR cookie is not set to context : " + context.getContextIdentifier());
            }
        }
    }

    private void endPostAuthenticationHandlerFlow(AuthenticationContext authenticationContext,
            HttpServletRequest request, HttpServletResponse response) {

        markPostAuthenticationCompleted(authenticationContext);
        // Since the post authn sequences is ended here, remove the cookie
        removePASTRCookie(request, response, authenticationContext);
    }
}
