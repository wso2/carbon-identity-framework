package org.wso2.carbon.identity.application.authentication.framework.services;

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
import org.wso2.carbon.identity.application.authentication.framework.util.LoginContextManagementUtil;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PostAuthenticationMgtService {

    private static final Log log = LogFactory.getLog(PostAuthenticationMgtService.class);

    public void handlePostAuthentication(HttpServletRequest request, HttpServletResponse response,
                                         AuthenticationContext authenticationContext) throws FrameworkException {

        logDebug("Executing Post Authentication Management Service");

        List<PostAuthenticationHandler> postAuthenticationHandlers =
                FrameworkServiceDataHolder.getInstance().getPostAuthenticationHandlers();

        int currentPostHandlerIndex = authenticationContext.getCurrentPostAuthHandlerIndex();
        logDebug("Starting from current post handler index " + currentPostHandlerIndex);

        if (isPostAuthenticationInProgress(authenticationContext, postAuthenticationHandlers,
                currentPostHandlerIndex)) {

            for (; currentPostHandlerIndex < postAuthenticationHandlers.size(); currentPostHandlerIndex++) {
                PostAuthenticationHandler currentHandler = postAuthenticationHandlers.get(currentPostHandlerIndex);
                if (executePostAuthnHandler(request, response, authenticationContext, currentHandler)) {
                    request.setAttribute(FrameworkConstants.RequestParams.FLOW_STATUS, AuthenticatorFlowStatus
                            .INCOMPLETE);
                    return;
                }
            }

            markPostAuthenticationCompleted(authenticationContext);
        } else {
            markPostAuthenticationCompleted(authenticationContext);
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
                                            AuthenticationContext authenticationContext,
                                            PostAuthenticationHandler currentHandler) throws PostAuthenticationFailedException {

        if (currentHandler.isEnabled()) {

            logDebug(currentHandler.getName() + " is enabled. Hence executing.");
            PostAuthnHandlerFlowStatus flowStatus = currentHandler.handle(request, response, authenticationContext);
            logDebug("Post authentication handler " + currentHandler.getName() + " returned with status : " +
                    flowStatus);

            if (isExecutionFinished(flowStatus)) {
                logDebug("Post authentication handler " + currentHandler.getName() + " completed execution");
                authenticationContext.setExecutedPostAuthHandler(currentHandler.getName());

            } else {
                logDebug("Post authentication handler " + currentHandler.getName() + " is not completed yet. Hence " +
                        "returning");
                return true;
            }
        } else {
            logDebug("Post authentication handler " + currentHandler.getName() + " is disabled. Hence returning" +
                    " without executing");
        }
        return false;
    }

    private boolean isExecutionFinished(PostAuthnHandlerFlowStatus flowStatus) {

        return PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED.equals(flowStatus) || PostAuthnHandlerFlowStatus
                .UNSUCCESS_COMPLETED.equals(flowStatus);
    }

    private void logDebug(String message) {

        if (log.isDebugEnabled()) {
            log.debug(message);
        }
    }

    private boolean isPostAuthenticationInProgress(AuthenticationContext authenticationContext,
                                                   List<PostAuthenticationHandler>
                                                           postAuthenticationHandlers, int currentPostHandlerIndex) {

        return !LoginContextManagementUtil.isPostAuthenticationExtensionCompleted(authenticationContext) &&
                postAuthenticationHandlers.size() > currentPostHandlerIndex;
    }

    private void markPostAuthenticationCompleted(AuthenticationContext authenticationContext) {

        logDebug("Post authentication evaluation has completed for the flow with session data key" +
                authenticationContext.getContextIdentifier());
        LoginContextManagementUtil.markPostAuthenticaionCompleted(authenticationContext);
    }
}
