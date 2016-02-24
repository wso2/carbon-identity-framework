/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authenticator.iwa.servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authenticator.iwa.IWAAuthenticator;
import org.wso2.carbon.identity.application.authenticator.iwa.IWAConstants;
import org.wso2.carbon.identity.application.authenticator.iwa.IWAServiceDataHolder;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import waffle.servlet.AutoDisposableWindowsPrincipal;
import waffle.servlet.NegotiateSecurityFilter;
import waffle.servlet.WindowsPrincipal;
import waffle.servlet.spi.SecurityFilterProvider;
import waffle.servlet.spi.SecurityFilterProviderCollection;
import waffle.util.AuthorizationHeader;
import waffle.windows.auth.IWindowsAuthProvider;
import waffle.windows.auth.IWindowsIdentity;
import waffle.windows.auth.IWindowsImpersonationContext;
import waffle.windows.auth.PrincipalFormat;
import waffle.windows.auth.impl.WindowsAuthProviderImpl;

import javax.security.auth.Subject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.security.Principal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * This class handles the IWA login requests. The implementation is based on the NegotiateSecurityFilter class.
 */
public class IWAServelet extends HttpServlet {

    public static final String PRINCIPAL_SESSION_KEY = NegotiateSecurityFilter.class
            .getName() + ".PRINCIPAL";
    private static Log log = LogFactory.getLog(IWAServelet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String commonAuthURL = IdentityUtil.getServerURL(IWAConstants.COMMON_AUTH_EP, false, true);
        String param = request.getParameter(IWAConstants.IWA_PARAM_STATE);
        if (param == null) {
            throw new IllegalArgumentException(IWAConstants.IWA_PARAM_STATE + " parameter is null.");
        }
        commonAuthURL += "?" + IWAConstants.IWA_PARAM_STATE + "=" + URLEncoder.encode(param, IWAConstants.UTF_8) +
                         "&" + IWAAuthenticator.IWA_PROCESSED + "=1";

        if (doFilterPrincipal(request)) {
            // previously authenticated user
            response.sendRedirect(commonAuthURL);
            return;
        }
        AuthorizationHeader authorizationHeader = new AuthorizationHeader(request);
        // authenticate user
        if (!authorizationHeader.isNull()) {
            // log the user in using the token
            IWindowsIdentity windowsIdentity;
            try {
                windowsIdentity = IWAServiceDataHolder.getInstance().getProviders().doFilter(request, response);
                if (windowsIdentity == null) {
                    return;
                }
            } catch (IOException e) {
                log.warn("error logging in user.", e);
                sendUnauthorized(response, true);
                return;
            }
            IWindowsImpersonationContext ctx = null;
            try {
                if (!IWAServiceDataHolder.getInstance().isAllowGuestLogin() && windowsIdentity.isGuest()) {
                    log.warn("guest login disabled: " + windowsIdentity.getFqn());
                    sendUnauthorized(response, true);
                    return;
                }
                if (log.isDebugEnabled()) {
                    log.debug("logged in user: " + windowsIdentity.getFqn() + " (" + windowsIdentity.getSidString() +
                              ")");
                }
                HttpSession session = request.getSession(true);
                if (session == null) {
                    throw new ServletException("Expected HttpSession");
                }

                Subject subject = (Subject) session.getAttribute(IWAConstants.SUBJECT_ATTRIBUTE);
                if (subject == null) {
                    subject = new Subject();
                }

                WindowsPrincipal windowsPrincipal;
                if (IWAServiceDataHolder.getInstance().isImpersonate()) {
                    windowsPrincipal = new AutoDisposableWindowsPrincipal(windowsIdentity, IWAServiceDataHolder.getInstance().getPrincipalFormat(), IWAServiceDataHolder.getInstance().getRoleFormat());
                } else {
                    windowsPrincipal = new WindowsPrincipal(windowsIdentity, IWAServiceDataHolder.getInstance().getPrincipalFormat(), IWAServiceDataHolder.getInstance().getRoleFormat());
                }
                if (log.isDebugEnabled()) {
                    log.debug("roles: " + windowsPrincipal.getRolesString());
                }
                subject.getPrincipals().add(windowsPrincipal);
                session.setAttribute(IWAConstants.SUBJECT_ATTRIBUTE, subject);

                log.info("Successfully logged in user: " + windowsIdentity.getFqn());

                request.getSession().setAttribute(PRINCIPAL_SESSION_KEY, windowsPrincipal);
                if (IWAServiceDataHolder.getInstance().isImpersonate()) {
                    if (log.isDebugEnabled()) {
                        log.debug("impersonating user");
                    }
                    ctx = windowsIdentity.impersonate();
                }
            } finally {
                if (IWAServiceDataHolder.getInstance().isImpersonate() && ctx != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("terminating impersonation");
                    }
                    ctx.revertToSelf();
                } else {
                    windowsIdentity.dispose();
                }
            }
            response.sendRedirect(commonAuthURL);
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("authorization required");
        }
        sendUnauthorized(response, false);
    }

    /**
     * Check whether the request is already authenticated using IWA
     *
     * @param request The HttpServletRequest
     * @return
     * @throws IOException
     * @throws ServletException
     */
    private boolean doFilterPrincipal(HttpServletRequest request) throws IOException, ServletException {
        Principal principal = request.getUserPrincipal();
        if (principal == null) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                principal = (Principal) session.getAttribute(PRINCIPAL_SESSION_KEY);
            }
        }

        if (principal == null) {
            // no principal in this request
            return false;
        }

        if (IWAServiceDataHolder.getInstance().getProviders().isPrincipalException(request)) {
            // the providers signal to authenticate despite an existing principal, eg. NTLM post
            return false;
        }

        // user already authenticated

        if (principal instanceof WindowsPrincipal) {
            if (log.isDebugEnabled()) {
                log.debug("previously authenticated Windows user: " + principal.getName());
            }
            WindowsPrincipal windowsPrincipal = (WindowsPrincipal) principal;

            if (IWAServiceDataHolder.getInstance().isImpersonate() && windowsPrincipal.getIdentity() == null) {
                // This can happen when the session has been serialized then de-serialized
                // and because the IWindowsIdentity field is transient. In this case re-ask an
                // authentication to get a new identity.
                return false;
            }


            IWindowsImpersonationContext ctx = null;
            if (IWAServiceDataHolder.getInstance().isImpersonate()) {
                if (log.isDebugEnabled()) {
                    log.debug("re-impersonating user");
                }
                ctx = windowsPrincipal.getIdentity().impersonate();
            }
            if (IWAServiceDataHolder.getInstance().isImpersonate() && ctx != null) {
                if (log.isDebugEnabled()) {
                    log.debug("terminating impersonation");
                }
                ctx.revertToSelf();
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("previously authenticated user: " + principal.getName());
            }
        }
        return true;
    }

    /**
     * Send response as unauthorized
     *
     * @param response
     * @param close    whether to close the connection or to keep it alive
     */
    private void sendUnauthorized(HttpServletResponse response, boolean close) {
        try {
            IWAServiceDataHolder.getInstance().getProviders().sendUnauthorized(response);
            if (close) {
                response.setHeader(IWAConstants.HTTP_CONNECTION_HEADER, IWAConstants.CONNECTION_CLOSE);
            } else {
                response.setHeader(IWAConstants.HTTP_CONNECTION_HEADER, IWAConstants.CONNECTION_KEEP_ALIVE);
            }
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            response.flushBuffer();
        } catch (IOException e) {
            log.error("Error when sending unauthorized response.", e);
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        Map<String, String> implParameters = new HashMap<String, String>();
        String authProvider = null;
        String[] providerNames = null;
        if (config != null) {
            Enumeration parameterNames = config.getInitParameterNames();
            while (parameterNames.hasMoreElements()) {
                String parameterName = (String) parameterNames.nextElement();
                String parameterValue = config

                        .getInitParameter(parameterName);
                if (parameterName.equals(IWAConstants.PRINCIPAL_FORMAT)) {
                    IWAServiceDataHolder.getInstance().setPrincipalFormat(PrincipalFormat.valueOf(parameterValue));
                } else if (parameterName.equals(IWAConstants.ROLE_FORMAT)) {
                    IWAServiceDataHolder.getInstance().setRoleFormat(PrincipalFormat.valueOf(parameterValue));
                } else if (parameterName.equals(IWAConstants.ALLOW_GUEST_LOGIN)) {
                    IWAServiceDataHolder.getInstance().setAllowGuestLogin(Boolean.parseBoolean(parameterValue));
                } else if (parameterName.equals(IWAConstants.IMPERSONATE)) {
                    IWAServiceDataHolder.getInstance().setImpersonate(Boolean.parseBoolean(parameterValue));
                } else if (parameterName.equals(IWAConstants.SECURITY_FILTER_PROVIDERS)) {
                    providerNames = parameterValue.split("\\s+");
                } else if (parameterName.equals(IWAConstants.AUTH_PROVIDER)) {
                    authProvider = parameterValue;
                } else {
                    implParameters.put(parameterName, parameterValue);
                }
            }
        }

        if (authProvider != null) {
            try {
                IWAServiceDataHolder.getInstance().setAuth(
                        (IWindowsAuthProvider) Class.forName(authProvider).getConstructor().newInstance());
            } catch (Exception e) {
                throw new ServletException("Error loading '" + authProvider, e);
            }
        }

        if (IWAServiceDataHolder.getInstance().getAuth() == null) {
            IWAServiceDataHolder.getInstance().setAuth(new WindowsAuthProviderImpl());
        }

        if (providerNames != null) {
            IWAServiceDataHolder.getInstance().setProviders(new SecurityFilterProviderCollection(
                    providerNames, IWAServiceDataHolder.getInstance().getAuth()));
        }

        // create default providers if none specified
        if (IWAServiceDataHolder.getInstance().getProviders() == null) {
            if (log.isDebugEnabled()) {
                log.debug("initializing default security filter providers");
            }
            IWAServiceDataHolder.getInstance().setProviders(new SecurityFilterProviderCollection(
                    IWAServiceDataHolder.getInstance().getAuth()));
        }

        // apply provider implementation parameters
        for (Map.Entry<String, String> implParameter : implParameters.entrySet()) {
            String[] classAndParameter = implParameter.getKey().split("/", 2);
            if (classAndParameter.length == 2) {
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("Setting " + classAndParameter[0] + ", " + classAndParameter[1] + "=" +
                                  implParameter.getValue());
                    }
                    SecurityFilterProvider provider = IWAServiceDataHolder.getInstance().getProviders().getByClassName(
                            classAndParameter[0]);
                    provider.initParameter(classAndParameter[1], implParameter.getValue());
                } catch (ClassNotFoundException e) {
                    throw new ServletException("Invalid class: " + classAndParameter[0] + " in " + implParameter
                            .getKey(), e);
                }
            } else {
                throw new ServletException("Invalid parameter: " + implParameter.getKey());
            }
        }
    }
}
