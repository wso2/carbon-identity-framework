/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
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
package org.wso2.carbon.identity.sso.saml.servlet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.owasp.encoder.Encode;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.CommonAuthenticationHandler;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationRequestCacheEntry;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationResultCacheEntry;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticationRequest;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticationResult;
import org.wso2.carbon.identity.application.authentication.framework.model.CommonAuthRequestWrapper;
import org.wso2.carbon.identity.application.authentication.framework.model.CommonAuthResponseWrapper;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.base.IdentityConstants;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.sso.saml.SAMLSSOConstants;
import org.wso2.carbon.identity.sso.saml.SAMLSSOService;
import org.wso2.carbon.identity.sso.saml.cache.SessionDataCache;
import org.wso2.carbon.identity.sso.saml.cache.SessionDataCacheEntry;
import org.wso2.carbon.identity.sso.saml.cache.SessionDataCacheKey;
import org.wso2.carbon.identity.sso.saml.dto.QueryParamDTO;
import org.wso2.carbon.identity.sso.saml.dto.SAMLSSOAuthnReqDTO;
import org.wso2.carbon.identity.sso.saml.dto.SAMLSSOReqValidationResponseDTO;
import org.wso2.carbon.identity.sso.saml.dto.SAMLSSORespDTO;
import org.wso2.carbon.identity.sso.saml.dto.SAMLSSOSessionDTO;
import org.wso2.carbon.identity.sso.saml.internal.IdentitySAMLSSOServiceComponent;
import org.wso2.carbon.identity.sso.saml.logout.LogoutRequestSender;
import org.wso2.carbon.identity.sso.saml.util.SAMLSSOUtil;
import org.wso2.carbon.idp.mgt.util.IdPManagementUtil;
import org.wso2.carbon.registry.core.utils.UUIDGenerator;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * This is the entry point for authentication process in an SSO scenario. This servlet is registered
 * with the URL pattern /samlsso and act as the control servlet. The message flow of an SSO scenario
 * is as follows.
 * <ol>
 * <li>SP sends a SAML Request via HTTP POST to the https://<ip>:<port>/samlsso endpoint.</li>
 * <li>IdP validates the SAML Request and checks whether this user is already authenticated.</li>
 * <li>If the user is authenticated, it will generate a SAML Response and send it back the SP via
 * the samlsso_redirect_ajaxprocessor.jsp.</li>
 * <li>If the user is not authenticated, it will send him to the login page and prompts user to
 * enter his credentials.</li>
 * <li>If these credentials are valid, then the user will be redirected back the SP with a valid
 * SAML Assertion. If not, he will be prompted again for credentials.</li>
 * </ol>
 */
public class SAMLSSOProviderServlet extends HttpServlet {

    private static final long serialVersionUID = -5182312441482721905L;
    private static Log log = LogFactory.getLog(SAMLSSOProviderServlet.class);

    private SAMLSSOService samlSsoService = new SAMLSSOService();
    private boolean isCacheAvailable = false;

    @Override
    protected void doGet(HttpServletRequest httpServletRequest,
                         HttpServletResponse httpServletResponse) throws ServletException, IOException {
        try {
            handleRequest(httpServletRequest, httpServletResponse, false);
        } finally {
            SAMLSSOUtil.removeSaaSApplicationThreaLocal();
            SAMLSSOUtil.removeUserTenantDomainThreaLocal();
            SAMLSSOUtil.removeTenantDomainFromThreadLocal();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            handleRequest(req, resp, true);
        } finally {
            SAMLSSOUtil.removeSaaSApplicationThreaLocal();
            SAMLSSOUtil.removeUserTenantDomainThreaLocal();
            SAMLSSOUtil.removeTenantDomainFromThreadLocal();
        }
    }

    /**
     * All requests are handled by this handleRequest method. In case of SAMLRequest the user
     * will be redirected to commonAuth servlet for authentication. Based on successful
     * authentication of the user a SAMLResponse is sent back to service provider.
     * In case of logout requests, the IDP will send logout requests
     * to the other session participants and then send the logout response back to the initiator.
     *
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    private void handleRequest(HttpServletRequest req, HttpServletResponse resp, boolean isPost)
            throws ServletException, IOException {

        String sessionId = null;
        Cookie ssoTokenIdCookie = getTokenIdCookie(req);

        if (ssoTokenIdCookie != null) {
            sessionId = ssoTokenIdCookie.getValue();
        }

        String queryString = req.getQueryString();
        if (log.isDebugEnabled()) {
            log.debug("Query string : " + queryString);
        }
        // if an openid authentication or password authentication
        String authMode = req.getParameter(SAMLSSOConstants.AUTH_MODE);
        if (!SAMLSSOConstants.AuthnModes.OPENID.equals(authMode)) {
            authMode = SAMLSSOConstants.AuthnModes.USERNAME_PASSWORD;
        }
        String relayState = req.getParameter(SAMLSSOConstants.RELAY_STATE);
        String spEntityID = req.getParameter(SAMLSSOConstants.QueryParameter
                .SP_ENTITY_ID.toString());
        String samlRequest = req.getParameter(SAMLSSOConstants.SAML_REQUEST);
        String sessionDataKey = getSessionDataKey(req);
        String slo = req.getParameter(SAMLSSOConstants.QueryParameter.SLO.toString());
        Object flowStatus = req.getAttribute(FrameworkConstants.RequestParams.FLOW_STATUS);

        try {

            //TODO add debug log istocommonth and flowstatus
            String isToCommonOauth = req.getParameter(FrameworkConstants.RequestParams.TO_COMMONAUTH);
            if ("true".equals(isToCommonOauth) && flowStatus == null) {
                sendRequestToFramework(req, resp);
                return;
            }

            String tenantDomain = req.getParameter(MultitenantConstants.TENANT_DOMAIN);
            SAMLSSOUtil.setTenantDomainInThreadLocal(tenantDomain);

            if (sessionDataKey != null) { //Response from common authentication framework.
                SAMLSSOSessionDTO sessionDTO = getSessionDataFromCache(sessionDataKey);

                if (sessionDTO != null) {
                    SAMLSSOUtil.setTenantDomainInThreadLocal(sessionDTO.getTenantDomain());
                    if (sessionDTO.isInvalidLogout()) {
                        log.warn("Redirecting to default logout page due to an invalid logout request");
                        String defaultLogoutLocation = SAMLSSOUtil.getDefaultLogoutEndpoint();
                        resp.sendRedirect(defaultLogoutLocation);
                    } else if (sessionDTO.isLogoutReq()) {
                        handleLogoutResponseFromFramework(req, resp, sessionDTO);
                    } else {
                        handleAuthenticationReponseFromFramework(req, resp, sessionId, sessionDTO);
                    }

                    removeAuthenticationResult(req, sessionDataKey);

                } else {
                    log.error("Failed to retrieve sessionDTO from the cache for key " + sessionDataKey);
                    String errorResp = SAMLSSOUtil.buildErrorResponse(
                            SAMLSSOConstants.StatusCodes.IDENTITY_PROVIDER_ERROR,
                            SAMLSSOConstants.Notification.EXCEPTION_STATUS, null);
                    sendNotification(errorResp, SAMLSSOConstants.Notification.EXCEPTION_STATUS,
                            SAMLSSOConstants.Notification.EXCEPTION_MESSAGE, null, req, resp);
                    return;
                }
            } else if (spEntityID != null || slo != null) { // idp initiated SSO/SLO
                handleIdPInitSSO(req, resp, relayState, queryString, authMode, sessionId, isPost, (slo != null));
            } else if (samlRequest != null) {// SAMLRequest received. SP initiated SSO
                handleSPInitSSO(req, resp, queryString, relayState, authMode, samlRequest, sessionId, isPost);
            } else {
                log.debug("Invalid request message or single logout message ");

                if (sessionId == null) {
                    String errorResp = SAMLSSOUtil.buildErrorResponse(
                            SAMLSSOConstants.StatusCodes.REQUESTOR_ERROR,
                            "Invalid request message", null);
                    sendNotification(errorResp, SAMLSSOConstants.Notification.INVALID_MESSAGE_STATUS,
                            SAMLSSOConstants.Notification.INVALID_MESSAGE_MESSAGE, null, req, resp);
                } else {
                    // Non-SAML request are assumed to be logout requests
                    sendToFrameworkForLogout(req, resp, null, null, sessionId, true, false);
                }
            }
        } catch (UserStoreException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error occurred while handling SAML2 SSO request", e);
            }
            String errorResp = null;
            try {
                errorResp = SAMLSSOUtil.buildErrorResponse(
                        SAMLSSOConstants.StatusCodes.IDENTITY_PROVIDER_ERROR,
                        "Error occurred while handling SAML2 SSO request", null);
            } catch (IdentityException e1) {
                log.error("Error while building SAML response", e1);
            }
            sendNotification(errorResp, SAMLSSOConstants.Notification.EXCEPTION_STATUS,
                    SAMLSSOConstants.Notification.EXCEPTION_MESSAGE, null, req, resp);
        } catch (IdentityException e) {
            log.error("Error when processing the authentication request!", e);
            String errorResp = null;
            try {
                errorResp = SAMLSSOUtil.buildErrorResponse(
                        SAMLSSOConstants.StatusCodes.IDENTITY_PROVIDER_ERROR,
                        "Error when processing the authentication request", null);
            } catch (IdentityException e1) {
                log.error("Error while building SAML response", e1);
            }
            sendNotification(errorResp, SAMLSSOConstants.Notification.EXCEPTION_STATUS,
                    SAMLSSOConstants.Notification.EXCEPTION_MESSAGE, null, req, resp);
        }
    }

    /**
     * In federated and multi steps scenario there is a redirection from commonauth to samlsso so have to get
     * session data key from query parameter
     *
     * @param req Http servlet request
     * @return Session data key
     */
    private String getSessionDataKey(HttpServletRequest req) {
        String sessionDataKey = (String) req.getAttribute(SAMLSSOConstants.SESSION_DATA_KEY);
        if (sessionDataKey == null) {
            sessionDataKey = req.getParameter(SAMLSSOConstants.SESSION_DATA_KEY);
        }
        return sessionDataKey;
    }

    /**
     * Prompts user a notification with the status and message
     *
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    private void sendNotification(String errorResp, String status, String message,
                                  String acUrl, HttpServletRequest req,
                                  HttpServletResponse resp) throws ServletException, IOException {

        String redirectURL = SAMLSSOUtil.getNotificationEndpoint();

        //TODO Send status codes rather than full messages in the GET request
        String queryParams = "?" + SAMLSSOConstants.STATUS + "=" + URLEncoder.encode(status, "UTF-8") +
                "&" + SAMLSSOConstants.STATUS_MSG + "=" + URLEncoder.encode(message, "UTF-8");

        if (errorResp != null) {
            queryParams += "&" + SAMLSSOConstants.SAML_RESP + "=" + URLEncoder.encode(errorResp, "UTF-8");
        }

        if (acUrl != null) {
            queryParams += "&" + SAMLSSOConstants.ASSRTN_CONSUMER_URL + "=" + URLEncoder.encode(acUrl, "UTF-8");
        }

        resp.sendRedirect(redirectURL + queryParams);
    }

    private void handleIdPInitSSO(HttpServletRequest req, HttpServletResponse resp, String relayState,
                                  String queryString, String authMode, String sessionId,
                                  boolean isPost, boolean isLogout) throws UserStoreException, IdentityException,
                                                                      IOException, ServletException {

        String rpSessionId = req.getParameter(MultitenantConstants.SSO_AUTH_SESSION_ID);
        SAMLSSOService samlSSOService = new SAMLSSOService();

        String defaultLogoutLocation = SAMLSSOUtil.getDefaultLogoutEndpoint();
        SAMLSSOReqValidationResponseDTO signInRespDTO = samlSSOService.validateIdPInitSSORequest(
                relayState, queryString, getQueryParams(req), defaultLogoutLocation, sessionId, rpSessionId,
                authMode, isLogout);

        if (!signInRespDTO.isLogOutReq()) {
            if (signInRespDTO.isValid()) {
                sendToFrameworkForAuthentication(req, resp, signInRespDTO, relayState, false);
            } else {
                if(log.isDebugEnabled()) {
                    log.debug("Invalid IdP initiated SAML SSO Request");
                }

                String errorResp = signInRespDTO.getResponse();
                sendNotification(errorResp, SAMLSSOConstants.Notification.EXCEPTION_STATUS,
                                 SAMLSSOConstants.Notification.EXCEPTION_MESSAGE,
                                 signInRespDTO.getAssertionConsumerURL(), req, resp);
            }
        } else {
            if(signInRespDTO.isValid()) {
                sendToFrameworkForLogout(req, resp, signInRespDTO, relayState, sessionId, false, isPost);
            } else {
                if(log.isDebugEnabled()) {
                    log.debug("Invalid IdP initiated SAML Single Logout Request");
                }

                if (signInRespDTO.isLogoutFromAuthFramework()) {
                    sendToFrameworkForLogout(req, resp, null, null, sessionId, true, isPost);
                } else {
                    String errorResp = signInRespDTO.getResponse();
                    sendNotification(errorResp, SAMLSSOConstants.Notification.INVALID_MESSAGE_STATUS,
                                     SAMLSSOConstants.Notification.EXCEPTION_MESSAGE,
                                     signInRespDTO.getAssertionConsumerURL(), req, resp);
                }
            }
        }
    }

    /**
     * If the SAMLRequest is a Logout request then IDP will send logout requests to other session
     * participants and then sends the logout Response back to the initiator. In case of
     * authentication request, check if there is a valid session for the user, if there is, the user
     * will be redirected directly to the Service Provider, if not the user will be redirected to
     * the login page.
     *
     * @param req
     * @param resp
     * @param sessionId
     * @param samlRequest
     * @param relayState
     * @param authMode
     * @throws IdentityException
     * @throws IOException
     * @throws ServletException
     * @throws org.wso2.carbon.identity.base.IdentityException
     */
    private void handleSPInitSSO(HttpServletRequest req, HttpServletResponse resp,
                                 String queryString, String relayState, String authMode,
                                 String samlRequest, String sessionId, boolean isPost)
            throws UserStoreException, IdentityException, IOException, ServletException {

        String rpSessionId = req.getParameter(MultitenantConstants.SSO_AUTH_SESSION_ID);
        SAMLSSOService samlSSOService = new SAMLSSOService();

        SAMLSSOReqValidationResponseDTO signInRespDTO = samlSSOService.validateSPInitSSORequest(
                samlRequest, queryString, sessionId, rpSessionId, authMode, isPost);

        if (!signInRespDTO.isLogOutReq()) { // an <AuthnRequest> received
            if (signInRespDTO.isValid()) {
                sendToFrameworkForAuthentication(req, resp, signInRespDTO, relayState, isPost);
            } else {
                //TODO send invalid response to SP
                if (log.isDebugEnabled() &&
                        IdentityUtil.isTokenLoggable(IdentityConstants.IdentityTokens.SAML_REQUEST)) {
                    log.debug("Invalid SAML SSO Request : " + samlRequest);
                }
                String errorResp = signInRespDTO.getResponse();
                sendNotification(errorResp, SAMLSSOConstants.Notification.EXCEPTION_STATUS,
                        SAMLSSOConstants.Notification.EXCEPTION_MESSAGE,
                        signInRespDTO.getAssertionConsumerURL(), req, resp);
            }
        } else { // a <LogoutRequest> received
            if (signInRespDTO.isValid()) {
                sendToFrameworkForLogout(req, resp, signInRespDTO, relayState, sessionId, false, isPost);
            } else {
                if (log.isDebugEnabled() &&
                        IdentityUtil.isTokenLoggable(IdentityConstants.IdentityTokens.SAML_REQUEST)) {
                    log.debug("Invalid SAML SSO Logout Request : " + samlRequest);
                }
                if (signInRespDTO.isLogoutFromAuthFramework()) {
                    sendToFrameworkForLogout(req, resp, null, null, sessionId, true, isPost);
                } else {
                    //TODO send invalid response to SP
                    String errorResp = signInRespDTO.getResponse();
                    sendNotification(errorResp, SAMLSSOConstants.Notification.EXCEPTION_STATUS,
                                     SAMLSSOConstants.Notification.EXCEPTION_MESSAGE,
                                     signInRespDTO.getAssertionConsumerURL(), req, resp);
                }
            }
        }
    }

    /**
     * Sends the user for authentication to the login page
     *
     * @param req
     * @param resp
     * @param signInRespDTO
     * @param relayState
     * @throws ServletException
     * @throws IOException
     */
    private void sendToFrameworkForAuthentication(HttpServletRequest req, HttpServletResponse resp,
                                                  SAMLSSOReqValidationResponseDTO signInRespDTO, String relayState, boolean isPost)
            throws ServletException, IOException, UserStoreException, IdentityException {

        SAMLSSOSessionDTO sessionDTO = new SAMLSSOSessionDTO();
        sessionDTO.setHttpQueryString(req.getQueryString());
        sessionDTO.setDestination(signInRespDTO.getDestination());
        sessionDTO.setRelayState(relayState);
        sessionDTO.setRequestMessageString(signInRespDTO.getRequestMessageString());
        sessionDTO.setIssuer(signInRespDTO.getIssuer());
        sessionDTO.setRequestID(signInRespDTO.getId());
        sessionDTO.setSubject(signInRespDTO.getSubject());
        sessionDTO.setRelyingPartySessionId(signInRespDTO.getRpSessionId());
        sessionDTO.setAssertionConsumerURL(signInRespDTO.getAssertionConsumerURL());
        sessionDTO.setTenantDomain(SAMLSSOUtil.getTenantDomainFromThreadLocal());
        sessionDTO.setAttributeConsumingServiceIndex(signInRespDTO.getAttributeConsumingServiceIndex());
        sessionDTO.setForceAuth(signInRespDTO.isForceAuthn());
        sessionDTO.setPassiveAuth(signInRespDTO.isPassive());
        sessionDTO.setValidationRespDTO(signInRespDTO);
        sessionDTO.setIdPInitSSO(signInRespDTO.isIdPInitSSO());

        String sessionDataKey = UUIDGenerator.generateUUID();
        addSessionDataToCache(sessionDataKey, sessionDTO);

        String commonAuthURL = IdentityUtil.getServerURL(FrameworkConstants.COMMONAUTH, false, true);
        String selfPath = req.getContextPath();
        // Setting authentication request context
        AuthenticationRequest authenticationRequest = new AuthenticationRequest();

        // Adding query parameters
        authenticationRequest.appendRequestQueryParams(req.getParameterMap());
        for (Enumeration headerNames = req.getHeaderNames(); headerNames.hasMoreElements(); ) {
            String headerName = headerNames.nextElement().toString();
            authenticationRequest.addHeader(headerName, req.getHeader(headerName));
        }

        authenticationRequest.setRelyingParty(signInRespDTO.getIssuer());
        authenticationRequest.setCommonAuthCallerPath(selfPath);
        authenticationRequest.setForceAuth(signInRespDTO.isForceAuthn());
        if (!authenticationRequest.getForceAuth() && authenticationRequest.getRequestQueryParam("forceAuth") != null) {
            String[] forceAuth = authenticationRequest.getRequestQueryParam("forceAuth");
            if (!forceAuth[0].trim().isEmpty() && Boolean.parseBoolean(forceAuth[0].trim())) {
                authenticationRequest.setForceAuth(Boolean.parseBoolean(forceAuth[0].trim()));
            }
        }
        authenticationRequest.setPassiveAuth(signInRespDTO.isPassive());
        authenticationRequest.setTenantDomain(sessionDTO.getTenantDomain());
        authenticationRequest.setPost(isPost);

        // Creating cache entry and adding entry to the cache before calling to commonauth
        AuthenticationRequestCacheEntry authRequest = new AuthenticationRequestCacheEntry
                (authenticationRequest);
        addAuthenticationRequestToRequest(req, authRequest);
        FrameworkUtils.setRequestPathCredentials(req);
        sendRequestToFramework(req, resp, sessionDataKey, FrameworkConstants.RequestType.CLAIM_TYPE_SAML_SSO);
    }

    private void addAuthenticationRequestToRequest(HttpServletRequest request,AuthenticationRequestCacheEntry authRequest){
        request.setAttribute(FrameworkConstants.RequestAttribute.AUTH_REQUEST, authRequest);
    }

    private void sendToFrameworkForLogout(HttpServletRequest request, HttpServletResponse response,
                                          SAMLSSOReqValidationResponseDTO signInRespDTO, String relayState,
                                          String sessionId,
                                          boolean invalid, boolean isPost) throws ServletException, IOException {

        SAMLSSOSessionDTO sessionDTO = new SAMLSSOSessionDTO();
        sessionDTO.setHttpQueryString(request.getQueryString());
        sessionDTO.setRelayState(relayState);
        sessionDTO.setSessionId(sessionId);
        sessionDTO.setLogoutReq(true);
        sessionDTO.setInvalidLogout(invalid);

        if (signInRespDTO != null) {
            sessionDTO.setDestination(signInRespDTO.getDestination());
            sessionDTO.setRequestMessageString(signInRespDTO.getRequestMessageString());
            sessionDTO.setIssuer(signInRespDTO.getIssuer());
            sessionDTO.setRequestID(signInRespDTO.getId());
            sessionDTO.setSubject(signInRespDTO.getSubject());
            sessionDTO.setRelyingPartySessionId(signInRespDTO.getRpSessionId());
            sessionDTO.setAssertionConsumerURL(signInRespDTO.getAssertionConsumerURL());
            sessionDTO.setValidationRespDTO(signInRespDTO);
        }

        String sessionDataKey = UUIDGenerator.generateUUID();
        addSessionDataToCache(sessionDataKey, sessionDTO);


        String commonAuthURL = IdentityUtil.getServerURL(FrameworkConstants.COMMONAUTH, false, true);

        String selfPath = request.getContextPath();

        //Add all parameters to authentication context before sending to authentication
        // framework
        AuthenticationRequest authenticationRequest = new
                AuthenticationRequest();
        authenticationRequest.addRequestQueryParam(FrameworkConstants.RequestParams.LOGOUT,
                                                   new String[]{"true"});
        authenticationRequest.setRequestQueryParams(request.getParameterMap());
        authenticationRequest.setCommonAuthCallerPath(selfPath);
        authenticationRequest.setPost(isPost);

        if (signInRespDTO != null) {
            authenticationRequest.setRelyingParty(signInRespDTO.getIssuer());
        }
        authenticationRequest.appendRequestQueryParams(request.getParameterMap());
        //Add headers to AuthenticationRequestContext
        for (Enumeration e = request.getHeaderNames(); e.hasMoreElements(); ) {
            String headerName = e.nextElement().toString();
            authenticationRequest.addHeader(headerName, request.getHeader(headerName));
        }

        AuthenticationRequestCacheEntry authRequest = new AuthenticationRequestCacheEntry
                (authenticationRequest);
        addAuthenticationRequestToRequest(request, authRequest);
        sendRequestToFramework(request, response, sessionDataKey, FrameworkConstants.RequestType.CLAIM_TYPE_SAML_SSO);
    }

    /**
     * Sends the Response message back to the Service Provider.
     *
     * @param req
     * @param resp
     * @param relayState
     * @param response
     * @param acUrl
     * @param subject
     * @throws ServletException
     * @throws IOException
     */
    private void sendResponse(HttpServletRequest req, HttpServletResponse resp, String relayState,
                              String response, String acUrl, String subject, String authenticatedIdPs,
                              String tenantDomain)
            throws ServletException, IOException, IdentityException {

        acUrl = getACSUrlWithTenantPartitioning(acUrl, tenantDomain);

        if (acUrl == null || acUrl.trim().length() == 0) {
            // if ACS is null. Send to error page
            log.error("ACS Url is Null");
            throw IdentityException.error("Unexpected error in sending message out");
        }

        if (response == null || response.trim().length() == 0) {
            // if response is null
            log.error("Response message is Null");
            throw IdentityException.error("Unexpected error in sending message out");
        }

        if (IdentitySAMLSSOServiceComponent.getSsoRedirectHtml() != null) {

            String finalPage = null;
            String htmlPage = IdentitySAMLSSOServiceComponent.getSsoRedirectHtml();
            String pageWithAcs = htmlPage.replace("$acUrl", acUrl);
            String pageWithAcsResponse = pageWithAcs.replace("<!--$params-->", "<!--$params-->\n" + "<input type='hidden' name='SAMLResponse' value='" + Encode.forHtmlAttribute(response) + "'>");
            String pageWithAcsResponseRelay = pageWithAcsResponse;

            if(relayState != null) {
                pageWithAcsResponseRelay = pageWithAcsResponse.replace("<!--$params-->", "<!--$params-->\n" + "<input type='hidden' name='RelayState' value='" + Encode.forHtmlAttribute(relayState)+ "'>");
            }

            if (authenticatedIdPs == null || authenticatedIdPs.isEmpty()) {
                finalPage = pageWithAcsResponseRelay;
            } else {
                finalPage = pageWithAcsResponseRelay.replace(
                        "<!--$additionalParams-->",
                        "<input type='hidden' name='AuthenticatedIdPs' value='"
                                + Encode.forHtmlAttribute(authenticatedIdPs) + "'>");
            }

            PrintWriter out = resp.getWriter();
            out.print(finalPage);

            if (log.isDebugEnabled()) {
                log.debug("samlsso_response.html " + finalPage);
            }


        } else {
            PrintWriter out = resp.getWriter();
            out.println("<html>");
            out.println("<body>");
            out.println("<p>You are now redirected back to " + Encode.forHtmlContent(acUrl));
            out.println(" If the redirection fails, please click the post button.</p>");
            out.println("<form method='post' action='" + Encode.forHtmlAttribute(acUrl) + "'>");
            out.println("<p>");
            out.println("<input type='hidden' name='SAMLResponse' value='" + Encode.forHtmlAttribute(response) + "'>");

            if(relayState != null) {
                out.println("<input type='hidden' name='RelayState' value='" + Encode.forHtmlAttribute(relayState) + "'>");
            }

            if (authenticatedIdPs != null && !authenticatedIdPs.isEmpty()) {
                out.println("<input type='hidden' name='AuthenticatedIdPs' value='" +
                        Encode.forHtmlAttribute(authenticatedIdPs) + "'>");
            }

            out.println("<button type='submit'>POST</button>");
            out.println("</p>");
            out.println("</form>");
            out.println("<script type='text/javascript'>");
            out.println("document.forms[0].submit();");
            out.println("</script>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    /**
     * This method handles authentication and sends authentication Response message back to the
     * Service Provider after successful authentication. In case of authentication failure the user
     * is prompted back for authentication.
     *
     * @param req
     * @param resp
     * @param sessionId
     * @throws IdentityException
     * @throws IOException
     * @throws ServletException
     */
    private void handleAuthenticationReponseFromFramework(HttpServletRequest req, HttpServletResponse resp,
                                                          String sessionId, SAMLSSOSessionDTO sessionDTO)
            throws UserStoreException, IdentityException, IOException, ServletException {

        String sessionDataKey = getSessionDataKey(req);
        AuthenticationResult authResult = getAuthenticationResult(req, sessionDataKey);

        if (log.isDebugEnabled() && authResult == null) {
            log.debug("Session data is not found for key : " + sessionDataKey);
        }
        SAMLSSOReqValidationResponseDTO reqValidationDTO = sessionDTO.getValidationRespDTO();
        SAMLSSOAuthnReqDTO authnReqDTO = new SAMLSSOAuthnReqDTO();

        if (authResult == null || !authResult.isAuthenticated()) {

            if (log.isDebugEnabled() && authResult != null) {
                log.debug("Unauthenticated User");
            }

            if (reqValidationDTO.isPassive()) { //if passive

                String destination = reqValidationDTO.getAssertionConsumerURL();

                if (SAMLSSOUtil.validateACS(sessionDTO.getTenantDomain(), sessionDTO.getIssuer(), reqValidationDTO
                        .getAssertionConsumerURL())) {
                    List<String> statusCodes = new ArrayList<String>();
                    statusCodes.add(SAMLSSOConstants.StatusCodes.NO_PASSIVE);
                    statusCodes.add(SAMLSSOConstants.StatusCodes.IDENTITY_PROVIDER_ERROR);
                    reqValidationDTO.setResponse(SAMLSSOUtil.buildErrorResponse(
                            reqValidationDTO.getId(), statusCodes,
                            "Cannot authenticate Subject in Passive Mode",
                            destination));

                    sendResponse(req, resp, sessionDTO.getRelayState(), reqValidationDTO.getResponse(),
                            reqValidationDTO.getAssertionConsumerURL(), reqValidationDTO.getSubject(),
                            null, sessionDTO.getTenantDomain());
                    return;
                } else {
                    String errorResp = SAMLSSOUtil.buildErrorResponse(
                            SAMLSSOConstants.StatusCodes.AUTHN_FAILURE,
                            "User authentication failed", destination);
                    sendNotification(errorResp, SAMLSSOConstants.Notification.EXCEPTION_STATUS,
                            SAMLSSOConstants.Notification.EXCEPTION_MESSAGE,
                            reqValidationDTO.getAssertionConsumerURL(), req, resp);
                    return;
                }

            } else { // if forceAuthn or normal flow
                //TODO send a saml response with a status message.
                if (!authResult.isAuthenticated()) {
                    String destination = reqValidationDTO.getDestination();
                    String errorResp = SAMLSSOUtil.buildErrorResponse(
                            SAMLSSOConstants.StatusCodes.AUTHN_FAILURE,
                            "User authentication failed", destination);
                    sendNotification(errorResp, SAMLSSOConstants.Notification.EXCEPTION_STATUS,
                            SAMLSSOConstants.Notification.EXCEPTION_MESSAGE,
                            reqValidationDTO.getAssertionConsumerURL(), req, resp);
                    return;
                } else {
                    throw IdentityException.error("Session data is not found for authenticated user");
                }
            }
        } else {
            populateAuthnReqDTO(req, authnReqDTO, sessionDTO, authResult);
            req.setAttribute(SAMLSSOConstants.AUTHENTICATION_RESULT, authResult);
            String relayState = null;

            if (req.getParameter(SAMLSSOConstants.RELAY_STATE) != null) {
                relayState = req.getParameter(SAMLSSOConstants.RELAY_STATE);
            } else {
                relayState = sessionDTO.getRelayState();
            }

            startTenantFlow(authnReqDTO.getTenantDomain());

            if (sessionId == null) {
                sessionId = UUIDGenerator.generateUUID();
            }

            SAMLSSOService samlSSOService = new SAMLSSOService();
            SAMLSSORespDTO authRespDTO = samlSSOService.authenticate(authnReqDTO, sessionId, authResult.isAuthenticated(),
                    authResult.getAuthenticatedAuthenticators(), SAMLSSOConstants.AuthnModes.USERNAME_PASSWORD);

            if (authRespDTO.isSessionEstablished()) { // authenticated

                storeTokenIdCookie(sessionId, req, resp, authnReqDTO.getTenantDomain());
                removeSessionDataFromCache(req.getParameter(SAMLSSOConstants.SESSION_DATA_KEY));

                sendResponse(req, resp, relayState, authRespDTO.getRespString(),
                        authRespDTO.getAssertionConsumerURL(), authRespDTO.getSubject().getAuthenticatedSubjectIdentifier(),
                        authResult.getAuthenticatedIdPs(), sessionDTO.getTenantDomain());
            } else { // authentication FAILURE
                String errorResp = authRespDTO.getRespString();
                sendNotification(errorResp, SAMLSSOConstants.Notification.EXCEPTION_STATUS,
                        SAMLSSOConstants.Notification.EXCEPTION_MESSAGE,
                        authRespDTO.getAssertionConsumerURL(), req, resp);
            }
        }
    }

    private void handleLogoutResponseFromFramework(HttpServletRequest request,
                                                   HttpServletResponse response, SAMLSSOSessionDTO sessionDTO)
            throws ServletException, IOException, IdentityException {

        SAMLSSOReqValidationResponseDTO validationResponseDTO = sessionDTO.getValidationRespDTO();

        if (validationResponseDTO != null) {
            // sending LogoutRequests to other session participants
            LogoutRequestSender.getInstance().sendLogoutRequests(validationResponseDTO.getLogoutRespDTO());
            SAMLSSOUtil.removeSession(sessionDTO.getSessionId(), validationResponseDTO.getIssuer());
            removeSessionDataFromCache(request.getParameter(SAMLSSOConstants.SESSION_DATA_KEY));

            if (validationResponseDTO.isIdPInitSLO()) {
                // redirecting to the return URL or IS logout page
                response.sendRedirect(validationResponseDTO.getReturnToURL());
            } else {
                // sending LogoutResponse back to the initiator
                sendResponse(request, response, sessionDTO.getRelayState(), validationResponseDTO.getLogoutResponse(),
                             validationResponseDTO.getAssertionConsumerURL(), validationResponseDTO.getSubject(), null,
                             sessionDTO.getTenantDomain());
            }
        } else {
            try {
                samlSsoService.doSingleLogout(request.getSession().getId());
            } catch (IdentityException e) {
                log.error("Error when processing the logout request!", e);
            }

            String errorResp = SAMLSSOUtil.buildErrorResponse(
                    SAMLSSOConstants.StatusCodes.REQUESTOR_ERROR,
                    "Invalid request",
                    sessionDTO.getAssertionConsumerURL());
            sendNotification(errorResp, SAMLSSOConstants.Notification.INVALID_MESSAGE_STATUS,
                    SAMLSSOConstants.Notification.INVALID_MESSAGE_MESSAGE,
                    sessionDTO.getAssertionConsumerURL(), request, response);
        }
    }

    /**
     * @param req
     * @param authnReqDTO
     */
    private void populateAuthnReqDTO(HttpServletRequest req, SAMLSSOAuthnReqDTO authnReqDTO,
                                     SAMLSSOSessionDTO sessionDTO, AuthenticationResult authResult)
            throws UserStoreException, IdentityException {

        authnReqDTO.setAssertionConsumerURL(sessionDTO.getAssertionConsumerURL());
        authnReqDTO.setId(sessionDTO.getRequestID());
        authnReqDTO.setIssuer(sessionDTO.getIssuer());
        authnReqDTO.setSubject(sessionDTO.getSubject());
        authnReqDTO.setRpSessionId(sessionDTO.getRelyingPartySessionId());
        authnReqDTO.setRequestMessageString(sessionDTO.getRequestMessageString());
        authnReqDTO.setQueryString(sessionDTO.getHttpQueryString());
        authnReqDTO.setDestination(sessionDTO.getDestination());
        authnReqDTO.setUser(authResult.getSubject());
        authnReqDTO.setIdPInitSSOEnabled(sessionDTO.isIdPInitSSO());
        authnReqDTO.setClaimMapping(authResult.getClaimMapping());
        authnReqDTO.setTenantDomain(sessionDTO.getTenantDomain());
        authnReqDTO.setIdPInitSLOEnabled(sessionDTO.isIdPInitSLO());
        if (!(sessionDTO.getAttributeConsumingServiceIndex() < 1)) {
            authnReqDTO.setAttributeConsumingServiceIndex(sessionDTO.getAttributeConsumingServiceIndex());
        }

        SAMLSSOUtil.setIsSaaSApplication(authResult.isSaaSApp());
        SAMLSSOUtil.setUserTenantDomain(authResult.getSubject().getTenantDomain());
    }

    private Cookie getTokenIdCookie(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (StringUtils.equals(cookie.getName(), "samlssoTokenId")) {
                    return cookie;
                }
            }
        }
        return null;
    }

    /**
     * @param sessionId
     * @param req
     * @param resp
     */
    private void storeTokenIdCookie(String sessionId, HttpServletRequest req, HttpServletResponse resp,
                                    String tenantDomain) {
        Cookie samlssoTokenIdCookie = new Cookie("samlssoTokenId", sessionId);
        samlssoTokenIdCookie.setMaxAge(IdPManagementUtil.getIdleSessionTimeOut(tenantDomain)*60);
        samlssoTokenIdCookie.setSecure(true);
        samlssoTokenIdCookie.setHttpOnly(true);
        resp.addCookie(samlssoTokenIdCookie);
    }

    public void removeTokenIdCookie(HttpServletRequest req, HttpServletResponse resp) {

        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (StringUtils.equals(cookie.getName(), "samlssoTokenId")) {
                    cookie.setMaxAge(0);
                    resp.addCookie(cookie);
                    break;
                }
            }
        }
    }

    private String getACSUrlWithTenantPartitioning(String acsUrl, String tenantDomain) {
        String acsUrlWithTenantDomain = acsUrl;
        if (tenantDomain != null && "true".equals(IdentityUtil.getProperty(
                IdentityConstants.ServerConfig.SSO_TENANT_PARTITIONING_ENABLED))) {
            acsUrlWithTenantDomain =
                    acsUrlWithTenantDomain + "?" +
                            MultitenantConstants.TENANT_DOMAIN + "=" + tenantDomain;
        }
        return acsUrlWithTenantDomain;
    }

    private void addSessionDataToCache(String sessionDataKey, SAMLSSOSessionDTO sessionDTO) {
        SessionDataCacheKey cacheKey = new SessionDataCacheKey(sessionDataKey);
        SessionDataCacheEntry cacheEntry = new SessionDataCacheEntry();
        cacheEntry.setSessionDTO(sessionDTO);
        SessionDataCache.getInstance().addToCache(cacheKey, cacheEntry);
    }

    private SAMLSSOSessionDTO getSessionDataFromCache(String sessionDataKey) {
        SAMLSSOSessionDTO sessionDTO = null;
        SessionDataCacheKey cacheKey = new SessionDataCacheKey(sessionDataKey);
        SessionDataCacheEntry cacheEntry = SessionDataCache.getInstance().getValueFromCache(cacheKey);

        if (cacheEntry != null) {
            sessionDTO = cacheEntry.getSessionDTO();
        }

        return sessionDTO;
    }

    private void removeSessionDataFromCache(String sessionDataKey) {
        if (sessionDataKey != null) {
            SessionDataCacheKey cacheKey = new SessionDataCacheKey(sessionDataKey);
            SessionDataCache.getInstance().clearCacheEntry(cacheKey);
        }
    }

    /**
     * Get authentication result
     * When using federated or multiple steps authenticators, there is a redirection from commonauth to samlsso,
     * So in that case we cannot use request attribute and have to get the result from cache
     *
     * @param req Http servlet request
     * @param sessionDataKey Session data key
     * @return
     */
    private AuthenticationResult getAuthenticationResult(HttpServletRequest req, String sessionDataKey) {

        AuthenticationResult result = getAuthenticationResultFromRequest(req);
        if (result == null) {
            isCacheAvailable = true;
            result = getAuthenticationResultFromCache(sessionDataKey);
        }
        return result;
    }

    private AuthenticationResult getAuthenticationResultFromCache(String sessionDataKey) {
        AuthenticationResult authResult = null;
        AuthenticationResultCacheEntry authResultCacheEntry = FrameworkUtils
                .getAuthenticationResultFromCache(sessionDataKey);
        if (authResultCacheEntry != null) {
            authResult = authResultCacheEntry.getResult();
        } else {
            log.error("Cannot find AuthenticationResult from the cache");
        }
        return authResult;
    }

    /**
     * Get authentication result attribute from request
     * @param req Http servlet request
     * @return Authentication result
     */
    private AuthenticationResult getAuthenticationResultFromRequest(HttpServletRequest req) {

        return (AuthenticationResult) req.getAttribute(FrameworkConstants.RequestAttribute.AUTH_RESULT);
    }

    /**
     * Remove authentication result from request
     * @param req
     */
    private void removeAuthenticationResult(HttpServletRequest req, String sessionDataKey) {

            FrameworkUtils.removeAuthenticationResultFromCache(sessionDataKey);
            req.removeAttribute(FrameworkConstants.RequestAttribute.AUTH_RESULT);
    }

    /**
     * Remove authentication result from request and cache
     * @param req
     */
    private void removeAuthenticationResultFromRequest(HttpServletRequest req) {

        req.removeAttribute(FrameworkConstants.RequestAttribute.AUTH_RESULT);
    }

    private void startTenantFlow(String tenantDomain) throws IdentityException {

        int tenantId = MultitenantConstants.SUPER_TENANT_ID;

        if (tenantDomain != null && !tenantDomain.trim().isEmpty() && !"null".equalsIgnoreCase(tenantDomain.trim())) {
            try {
                tenantId = SAMLSSOUtil.getRealmService().getTenantManager().getTenantId(tenantDomain);
                if (tenantId == -1) {
                    // invalid tenantId, hence throw exception to avoid setting invalid tenant info
                    // to CC
                    String message = "Invalid Tenant Domain : " + tenantDomain;
                    if (log.isDebugEnabled()) {
                        log.debug(message);
                    }
                    throw IdentityException.error(message);
                }
            } catch (UserStoreException e) {
                String message = "Error occurred while getting tenant ID from tenantDomain " + tenantDomain;
                log.error(message, e);
                throw IdentityException.error(message, e);
            }
        } else {
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext
                .getThreadLocalCarbonContext();
        carbonContext.setTenantId(tenantId);
        carbonContext.setTenantDomain(tenantDomain);
    }

    private QueryParamDTO[] getQueryParams(HttpServletRequest request) {

        List<QueryParamDTO> queryParamDTOs =  new ArrayList<>();
        for(SAMLSSOConstants.QueryParameter queryParameter : SAMLSSOConstants.QueryParameter.values()) {
            queryParamDTOs.add(new QueryParamDTO(queryParameter.toString(),
                    request.getParameter(queryParameter.toString())));
        }

        return queryParamDTOs.toArray(new QueryParamDTO[queryParamDTOs.size()]);
    }

    /**
     * In SAML there is no redirection from authentication endpoint to  commonauth and it send a post request to samlsso
     * servlet and sending the request to authentication framework from here, this overload method not sending
     * sessionDataKey and type to commonauth that's why overloaded the method here
     *
     * @param request Http servlet request
     * @param response Http servlet response
     * @throws ServletException
     * @throws IOException
     */
    private void sendRequestToFramework(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        CommonAuthenticationHandler commonAuthenticationHandler = new CommonAuthenticationHandler();

        CommonAuthResponseWrapper responseWrapper = new CommonAuthResponseWrapper(response);
        commonAuthenticationHandler.doGet(request, responseWrapper);

        Object object = request.getAttribute(FrameworkConstants.RequestParams.FLOW_STATUS);
        if (object != null) {
            AuthenticatorFlowStatus status = (AuthenticatorFlowStatus) object;
            if (status == AuthenticatorFlowStatus.INCOMPLETE) {
                response.sendRedirect(responseWrapper.getRedirectURL());
            } else {
                doGet(request, response);
            }
        } else {
            request.setAttribute(FrameworkConstants.RequestParams.FLOW_STATUS, AuthenticatorFlowStatus.UNKNOWN);
            doGet(request, response);
        }
    }

    /**
     * This method use to call authentication framework directly via API other than using HTTP redirects.
     * Sending wrapper request object to doGet method since other original request doesn't exist required parameters
     * Doesn't check SUCCESS_COMPLETED since taking decision with INCOMPLETE status
     *
     * @param request  Http Request
     * @param response Http Response
     * @param sessionDataKey Session data key
     * @param type authenticator type
     * @throws ServletException
     * @throws IOException
     */
    private void sendRequestToFramework(HttpServletRequest request, HttpServletResponse response, String sessionDataKey,
            String type)
            throws ServletException, IOException {

        CommonAuthenticationHandler commonAuthenticationHandler = new CommonAuthenticationHandler();

        CommonAuthRequestWrapper requestWrapper = new CommonAuthRequestWrapper(request);
        requestWrapper.setParameter(FrameworkConstants.SESSION_DATA_KEY, sessionDataKey);
        requestWrapper.setParameter(FrameworkConstants.RequestParams.TYPE, type);

        CommonAuthResponseWrapper responseWrapper = new CommonAuthResponseWrapper(response);
        commonAuthenticationHandler.doGet(requestWrapper, responseWrapper);

        Object object = request.getAttribute(FrameworkConstants.RequestParams.FLOW_STATUS);
        if (object != null) {
            AuthenticatorFlowStatus status = (AuthenticatorFlowStatus) object;
            if (status == AuthenticatorFlowStatus.INCOMPLETE) {
                if (responseWrapper.isRedirect()) {
                    response.sendRedirect(responseWrapper.getRedirectURL());
                } else {
                    return;
                }
            } else {
                doGet(requestWrapper, response);
            }
        } else {
            requestWrapper.setAttribute(FrameworkConstants.RequestParams.FLOW_STATUS, AuthenticatorFlowStatus.UNKNOWN);
            doGet(requestWrapper, response);
        }
    }
}
