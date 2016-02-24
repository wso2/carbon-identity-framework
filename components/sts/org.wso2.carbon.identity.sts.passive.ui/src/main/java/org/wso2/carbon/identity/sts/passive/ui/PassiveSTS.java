/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.sts.passive.ui;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.owasp.encoder.Encode;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationRequestCacheEntry;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationResultCache;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationResultCacheEntry;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationResultCacheKey;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticationRequest;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticationResult;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.cache.CacheEntry;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.sts.passive.stub.types.RequestToken;
import org.wso2.carbon.identity.sts.passive.stub.types.ResponseToken;
import org.wso2.carbon.identity.sts.passive.ui.cache.SessionDataCache;
import org.wso2.carbon.identity.sts.passive.ui.cache.SessionDataCacheEntry;
import org.wso2.carbon.identity.sts.passive.ui.cache.SessionDataCacheKey;
import org.wso2.carbon.identity.sts.passive.ui.client.IdentityPassiveSTSClient;
import org.wso2.carbon.identity.sts.passive.ui.dto.SessionDTO;
import org.wso2.carbon.identity.sts.passive.ui.util.PassiveSTSUtil;
import org.wso2.carbon.idp.mgt.util.IdPManagementUtil;
import org.wso2.carbon.registry.core.utils.UUIDGenerator;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

public class PassiveSTS extends HttpServlet {

    private static final Log log = LogFactory.getLog(PassiveSTS.class);

    /**
     *
     */
    private static final long serialVersionUID = 1927253892844132565L;

    private String stsRedirectPage = null;
    private String redirectHtmlFilePath = CarbonUtils.getCarbonHome() + File.separator + "repository"
            + File.separator + "resources" + File.separator + "identity" + File.separator + "pages" + File.separator +
            "sts_response.html";

    /**
     * This method reads Passive STS Html Redirect file content.
     * This should have been implemented in the backend but done in the front end to avoid API changes
     *
     * @return Passive STS Html Redirect Page File Content
     */
    private String readPassiveSTSHtmlRedirectPage() {
        FileInputStream fileInputStream = null;
        String fileContent = null;
        try {
            fileInputStream = new FileInputStream(new File(redirectHtmlFilePath));
            fileContent = new Scanner(fileInputStream, "UTF-8").useDelimiter("\\A").next();

            if (log.isDebugEnabled()) {
                log.debug("sts_response.html : " + fileContent);
            }

        } catch (FileNotFoundException e) {
            // The Passive STS Redirect HTML file is optional. When the file is not found, use the default page content.
            if (log.isDebugEnabled()) {
                log.debug("Passive STS Redirect HTML file not found in : " + redirectHtmlFilePath +
                        ". Default Redirect is used.");
            }
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    log.error("Error occurred when closing file input stream for sts_response.html", e);
                }
            }
        }

        if (StringUtils.isBlank(fileContent)) {
            fileContent = "<html>" +
                    "    <body>" +
                    "        <p>You are now redirected to $url ." +
                    "           If the redirection fails, please click the post button." +
                    "        </p>" +
                    "        <form method='post' action='$url'>" +
                    "        <p>" +
                    "            <!--$params-->" +
                    "            <!--$additionalParams-->" +
                    "            <button type='submit'>POST</button>" +
                    "       </p>" +
                    "       </form>" +
                    "        <script type='text/javascript'>" +
                    "            document.forms[0].submit();" +
                    "        </script>" +
                    "    </body>" +
                    "</html>";
        }

        // Adding parameters to the Passive STS HTML redirect page
        String parameters = "<input type=\"hidden\" name=\"wa\" value=\"$action\">" +
                "<input type=\"hidden\" name=\"wresult\" value=\"$result\">";

        if (StringUtils.isNotBlank(parameters)) {
            fileContent = fileContent.replace("<!--$params-->", parameters);
        }

        return fileContent;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        if (req.getParameter("sessionDataKey") != null) {
            handleResponseFromAuthenticationFramework(req, resp);
        } else if ("wsignout1.0".equals(getAttribute(req.getParameterMap(), PassiveRequestorConstants.ACTION))) {
            handleLogoutRequest(req, resp);
        } else {
            handleAuthenticationRequest(req, resp);
        }
    }

    private void sendData(HttpServletResponse httpResp, ResponseToken respToken, String action,
                          String authenticatedIdPs)
            throws ServletException, IOException {

        if (StringUtils.isBlank(stsRedirectPage)) {
            // Read the Passive STS Html Redirect Page File Content
            stsRedirectPage = readPassiveSTSHtmlRedirectPage();
        }
        String finalPage = null;
        String htmlPage = stsRedirectPage;
        String pageWithReply = htmlPage.replace("$url", String.valueOf(respToken.getReplyTo()));

        String pageWithReplyAction = pageWithReply.replace("$action", Encode.forHtmlAttribute(String.valueOf(action)));
        String pageWithReplyActionResult = pageWithReplyAction.replace("$result",
                Encode.forHtmlAttribute(String.valueOf(respToken.getResults())));
        String pageWithReplyActionResultContext;
        if (respToken.getContext() != null) {
            pageWithReplyActionResultContext = pageWithReplyActionResult.replace(
                    PassiveRequestorConstants.PASSIVE_ADDITIONAL_PARAMETER,
                    PassiveRequestorConstants.PASSIVE_ADDITIONAL_PARAMETER + "<input type='hidden' name='wctx' value='"
                            + Encode.forHtmlAttribute(respToken.getContext()) + "'>");
        } else {
            pageWithReplyActionResultContext = pageWithReplyActionResult;
        }

        if (authenticatedIdPs == null || authenticatedIdPs.isEmpty()) {
            finalPage = pageWithReplyActionResultContext;
        } else {
            finalPage = pageWithReplyActionResultContext.replace(PassiveRequestorConstants.PASSIVE_ADDITIONAL_PARAMETER,
                    "<input type='hidden' name='AuthenticatedIdPs' value='" +
                            Encode.forHtmlAttribute(authenticatedIdPs) + "'>");
        }

        PrintWriter out = httpResp.getWriter();
        out.print(finalPage);

        if (log.isDebugEnabled()) {
            log.debug("sts_response.html : " + finalPage);
        }
        return;
    }

    private String getAttribute(Map paramMap, String name) {
        if (paramMap.get(name) != null && paramMap.get(name) instanceof String[]) {
            return ((String[]) paramMap.get(name))[0];
        }
        return null;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    private void openURLWithNoTrust(String realm) throws IOException {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        // Nothing to implement
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        // Nothing to implement
                    }
                } };

        // Ignore differences between given hostname and certificate hostname
        HostnameVerifier hv = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            SSLSocketFactory defaultSSLSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
            HostnameVerifier defaultHostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
            String renegotiation = System.getProperty("sun.security.ssl.allowUnsafeRenegotiation");
            try {
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                HttpsURLConnection.setDefaultHostnameVerifier(hv);
                System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
                new URL(realm).getContent();
            } finally {
                HttpsURLConnection.setDefaultSSLSocketFactory(defaultSSLSocketFactory);
                HttpsURLConnection.setDefaultHostnameVerifier(defaultHostnameVerifier);
                System.getProperty("sun.security.ssl.allowUnsafeRenegotiation", renegotiation);
            }
        } catch (Exception ignore) {
            if (log.isDebugEnabled()) {
                log.debug("Error while installing trust manager", ignore);
            }
        }
    }

    private void persistRealms(RequestToken reqToken, HttpSession session) {
        Set<String> realms = (Set<String>) session.getAttribute("realms");
        if (realms == null) {
            realms = new HashSet<>();
            session.setAttribute("realms", realms);
        }
        realms.add(reqToken.getRealm());
    }

    private void sendToAuthenticationFramework(HttpServletRequest request, HttpServletResponse response,
                                               String sessionDataKey, SessionDTO sessionDTO) throws IOException {

        String commonAuthURL = IdentityUtil.getServerURL(FrameworkConstants.COMMONAUTH, false, true);

        String selfPath = request.getRequestURI();
        //Authentication context keeps data which should be sent to commonAuth endpoint
        AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        authenticationRequest.setRelyingParty(sessionDTO.getRealm());
        authenticationRequest.setCommonAuthCallerPath(selfPath);
        authenticationRequest.setForceAuth(false);
        authenticationRequest.setRequestQueryParams(request.getParameterMap());

        //adding headers in out going request to authentication request context
        for (Enumeration e = request.getHeaderNames(); e.hasMoreElements(); ) {
            String headerName = e.nextElement().toString();
            authenticationRequest.addHeader(headerName, request.getHeader(headerName));
        }

        //Add authenticationRequest cache entry to cache
        AuthenticationRequestCacheEntry authRequest = new AuthenticationRequestCacheEntry(authenticationRequest);
        FrameworkUtils.addAuthenticationRequestToCache(sessionDataKey, authRequest);
        StringBuilder queryStringBuilder = new StringBuilder();
        queryStringBuilder.append("?").
                append(FrameworkConstants.SESSION_DATA_KEY).
                                  append("=").
                                  append(sessionDataKey).
                                  append("&").
                                  append(FrameworkConstants.RequestParams.TYPE).
                                  append("=").
                                  append(FrameworkConstants.PASSIVE_STS);
        response.sendRedirect(commonAuthURL + queryStringBuilder.toString());
    }

    private void handleResponseFromAuthenticationFramework(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String sessionDataKey = request.getParameter(FrameworkConstants.SESSION_DATA_KEY);
        SessionDTO sessionDTO = getSessionDataFromCache(sessionDataKey);
        AuthenticationResult authnResult = getAuthenticationResultFromCache(sessionDataKey);

        if (sessionDTO != null && authnResult != null) {

            if (authnResult.isAuthenticated()) {
                process(request, response, sessionDTO, authnResult);
            } else {
                // TODO how to send back the authentication failure to client.
                //for now user will be redirected back to the framework
                // According to ws-federation-1.2-spec; 'wtrealm' will not be sent in the Passive STS Logout Request.
                if (sessionDTO.getRealm() == null || sessionDTO.getRealm().trim().length() == 0) {
                    sessionDTO.setRealm(new String());
                }
                sendToAuthenticationFramework(request, response, sessionDataKey, sessionDTO);
            }
        } else {
            sendToRetryPage(request, response);
        }
    }

    private void process(HttpServletRequest request, HttpServletResponse response,
                         SessionDTO sessionDTO, AuthenticationResult authnResult) throws ServletException, IOException {

        HttpSession session = request.getSession();

        session.removeAttribute(PassiveRequestorConstants.PASSIVE_REQ_ATTR_MAP);

        RequestToken reqToken = new RequestToken();

        Map<ClaimMapping, String> attrMap = authnResult.getSubject().getUserAttributes();
        StringBuilder buffer = null;

        if (MapUtils.isNotEmpty(attrMap)) {
            buffer = new StringBuilder();
            for (Iterator<Entry<ClaimMapping, String>> iterator = attrMap.entrySet().iterator(); iterator
                    .hasNext(); ) {
                Entry<ClaimMapping, String> entry = iterator.next();
                buffer.append("{" + entry.getKey().getRemoteClaim().getClaimUri() + "|" + entry.getValue() + "}#CODE#");
            }
        }

        reqToken.setAction(sessionDTO.getAction());
        if (buffer != null) {
            reqToken.setAttributes(buffer.toString());
        } else {
            reqToken.setAttributes(sessionDTO.getAttributes());
        }
        reqToken.setContext(sessionDTO.getContext());
        reqToken.setReplyTo(sessionDTO.getReplyTo());
        reqToken.setPseudo(sessionDTO.getPseudo());
        reqToken.setRealm(sessionDTO.getRealm());
        reqToken.setRequest(sessionDTO.getRequest());
        reqToken.setRequestPointer(sessionDTO.getRequestPointer());
        reqToken.setPolicy(sessionDTO.getPolicy());
        reqToken.setPseudo(session.getId());
        reqToken.setUserName(authnResult.getSubject().getAuthenticatedSubjectIdentifier());
        reqToken.setTenantDomain(sessionDTO.getTenantDomain());

        String serverURL = CarbonUIUtil.getServerURL(session.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) session.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

        IdentityPassiveSTSClient passiveSTSClient = null;
        passiveSTSClient = new IdentityPassiveSTSClient(serverURL, configContext);

        ResponseToken respToken = passiveSTSClient.getResponse(reqToken);

        if (respToken != null && respToken.getResults() != null) {
            persistRealms(reqToken, request.getSession());
            sendData(response, respToken, reqToken.getAction(),
                     authnResult.getAuthenticatedIdPs());
        }
    }

    private void handleLogoutRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {

        Set<String> realms = (Set<String>) request.getSession().getAttribute("realms");

        if (CollectionUtils.isNotEmpty(realms)) {
            for (String realm : realms) {
                openURLWithNoTrust(realm + "?wa=wsignoutcleanup1.0");
            }
        }

        try {
            sendFrameworkForLogout(request, response);
        } catch (ServletException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while sending the logout request", e);
            }
        }
    }

    private void sendFrameworkForLogout(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map paramMap = request.getParameterMap();
        SessionDTO sessionDTO = new SessionDTO();
        sessionDTO.setAction(getAttribute(paramMap, PassiveRequestorConstants.ACTION));
        sessionDTO.setAttributes(getAttribute(paramMap, PassiveRequestorConstants.ATTRIBUTE));
        sessionDTO.setContext(getAttribute(paramMap, PassiveRequestorConstants.CONTEXT));
        sessionDTO.setReplyTo(getAttribute(paramMap, PassiveRequestorConstants.REPLY_TO));
        sessionDTO.setPseudo(getAttribute(paramMap, PassiveRequestorConstants.PSEUDO));
        sessionDTO.setRealm(getAttribute(paramMap, PassiveRequestorConstants.REALM));
        sessionDTO.setRequest(getAttribute(paramMap, PassiveRequestorConstants.REQUEST));
        sessionDTO.setRequestPointer(getAttribute(paramMap, PassiveRequestorConstants.REQUEST_POINTER));
        sessionDTO.setPolicy(getAttribute(paramMap, PassiveRequestorConstants.POLCY));
        sessionDTO.setReqQueryString(request.getQueryString());

        String sessionDataKey = UUIDGenerator.generateUUID();
        addSessionDataToCache(sessionDataKey, sessionDTO);
        String commonAuthURL = IdentityUtil.getServerURL(FrameworkConstants.COMMONAUTH, false, true);

        String selfPath = request.getRequestURI();
        AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        authenticationRequest.addRequestQueryParam(FrameworkConstants.RequestParams.LOGOUT,
                new String[]{Boolean.TRUE.toString()});
        authenticationRequest.setRequestQueryParams(request.getParameterMap());
        authenticationRequest.setCommonAuthCallerPath(selfPath);
        authenticationRequest.appendRequestQueryParams(request.getParameterMap());
        // According to ws-federation-1.2-spec; 'wtrealm' will not be sent in the Passive STS Logout Request.
        if (sessionDTO.getRealm() == null || sessionDTO.getRealm().trim().length() == 0) {
            authenticationRequest.setRelyingParty(new String());
        }
        for (Enumeration e = request.getHeaderNames(); e.hasMoreElements(); ) {
            String headerName = e.nextElement().toString();
            authenticationRequest.addHeader(headerName, request.getHeader(headerName));
        }

        AuthenticationRequestCacheEntry authRequest = new AuthenticationRequestCacheEntry
                (authenticationRequest);
        FrameworkUtils.addAuthenticationRequestToCache(sessionDataKey, authRequest);
        String queryParams = "?" + FrameworkConstants.SESSION_DATA_KEY + "=" + sessionDataKey
                + "&" + FrameworkConstants.RequestParams.TYPE + "=" + FrameworkConstants.PASSIVE_STS;

        response.sendRedirect(commonAuthURL + queryParams);

    }

    private void handleAuthenticationRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        Map paramMap = request.getParameterMap();

        SessionDTO sessionDTO = new SessionDTO();
        sessionDTO.setAction(getAttribute(paramMap, PassiveRequestorConstants.ACTION));
        sessionDTO.setAttributes(getAttribute(paramMap, PassiveRequestorConstants.ATTRIBUTE));
        sessionDTO.setContext(getAttribute(paramMap, PassiveRequestorConstants.CONTEXT));
        sessionDTO.setReplyTo(getAttribute(paramMap, PassiveRequestorConstants.REPLY_TO));
        sessionDTO.setPseudo(getAttribute(paramMap, PassiveRequestorConstants.PSEUDO));
        sessionDTO.setRealm(getAttribute(paramMap, PassiveRequestorConstants.REALM));
        sessionDTO.setRequest(getAttribute(paramMap, PassiveRequestorConstants.REQUEST));
        sessionDTO.setRequestPointer(getAttribute(paramMap, PassiveRequestorConstants.REQUEST_POINTER));
        sessionDTO.setPolicy(getAttribute(paramMap, PassiveRequestorConstants.POLCY));
        sessionDTO.setTenantDomain(getAttribute(paramMap, MultitenantConstants.TENANT_DOMAIN));
        sessionDTO.setReqQueryString(request.getQueryString());

        String sessionDataKey = UUIDGenerator.generateUUID();
        addSessionDataToCache(sessionDataKey, sessionDTO);

        sendToAuthenticationFramework(request, response, sessionDataKey, sessionDTO);
    }

    private void addSessionDataToCache(String sessionDataKey, SessionDTO sessionDTO) {
        SessionDataCacheKey cacheKey = new SessionDataCacheKey(sessionDataKey);
        SessionDataCacheEntry cacheEntry = new SessionDataCacheEntry();
        cacheEntry.setSessionDTO(sessionDTO);
        SessionDataCache.getInstance().addToCache(cacheKey, cacheEntry);
    }

    private SessionDTO getSessionDataFromCache(String sessionDataKey) {
        SessionDTO sessionDTO = null;
        SessionDataCacheKey cacheKey = new SessionDataCacheKey(sessionDataKey);
        SessionDataCacheEntry cacheEntry = SessionDataCache.getInstance().getValueFromCache(cacheKey);

        if (cacheEntry != null) {
            sessionDTO = cacheEntry.getSessionDTO();
        } else {
            log.error("SessionDTO does not exist. Probably due to cache timeout");
        }

        return sessionDTO;
    }

    private AuthenticationResult getAuthenticationResultFromCache(String sessionDataKey) {
        AuthenticationResult authResult = null;
        AuthenticationResultCacheEntry authResultCacheEntry = FrameworkUtils.getAuthenticationResultFromCache(sessionDataKey);
        if (authResultCacheEntry != null) {
            authResult = authResultCacheEntry.getResult();
        } else {
            log.error("AuthenticationResult does not exist. Probably due to cache timeout");
        }

        return authResult;
    }

    private void sendToRetryPage(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.sendRedirect(PassiveSTSUtil.getRetryUrl());
    }
}
