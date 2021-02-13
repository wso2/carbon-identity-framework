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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.application.authentication.framework.util;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.core.SameSiteCookie;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.registry.core.utils.UUIDGenerator;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handles session nonce cookie.
 * Session nonce cookie helps to mitigate the session hijacking.
 */
public class SessionNonceCookieUtil {

    public static final String NONCE_COOKIE = "sessionNonceCookie";
    public static final String NONCE_COOKIE_CONFIG = "EnableSessionNonceCookie";
    public static final String NONCE_ERROR_CODE = "sessionNonceErrorCode";

    private static Boolean nonceCookieConfig;

    /**
     * Get dynamic name for the nonce cookie
     *
     * @param context Authentication Context.
     *
     * @return name of the nonce cookie for that context.
     */
    public static String getNonceCookieName(AuthenticationContext context) {

        return NONCE_COOKIE + "-" + context.getContextIdentifier();
    }

    /**
     * Initiate or update nonce value in both cookie and context.
     *
     * @param request HttpServletRequest.
     * @param response HttpServletResponse.
     * @param context Authentication Context.
     */
    public static void addNonceCookie(HttpServletRequest request, HttpServletResponse response,
                                      AuthenticationContext context) {

        if (isNonceCookieEnabled()) {
            removeExistingNonceCookies(request, response);
            String nonceId = UUIDGenerator.generateUUID();
            String cookieName = getNonceCookieName(context);
            FrameworkUtils.setCookie(request, response, cookieName, nonceId, null, SameSiteCookie.NONE);
            context.setProperty(cookieName, nonceId);
        }
    }

    /**
     * Validate whether nonce value matches in cookie and context.
     *
     * @param request HttpServletRequest.
     * @param context Authentication Context.
     *
     * @return boolean whether nonce cookie value valid or not.
     */
    public static boolean validateNonceCookie(HttpServletRequest request,
                                              AuthenticationContext context) {

        if (!isNonceCookieEnabled()) {
            return true;
        }

        boolean validNonceValue = false;
        String cookieName = getNonceCookieName(context);
        String nonceFromContext = (String) context.getProperty(cookieName);
        Cookie nonceCookie = FrameworkUtils.getCookie(request, cookieName);
        String nonceFromSession = null;
        if (nonceCookie != null) {
            nonceFromSession = nonceCookie.getValue();
        }
        if (!StringUtils.isEmpty(nonceFromContext) && !StringUtils.isEmpty(nonceFromSession)
                && nonceFromContext.equals(nonceFromSession)) {
            validNonceValue = true;
        }
        return validNonceValue;

    }

    /**
     * Removes nonce value from context and cookie.
     *
     * @param request HttpServletRequest.
     * @param response HttpServletResponse.
     * @param context Authentication Context.
     */
    public static void removeNonceCookie(HttpServletRequest request, HttpServletResponse response,
                                         AuthenticationContext context) {

        if (isNonceCookieEnabled()) {
            String cookieName = getNonceCookieName(context);
            FrameworkUtils.removeCookie(request, response, cookieName);
            context.removeProperty(cookieName);
        }
    }

    /**
     * Clear all existing session cookies in the browser.
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     */
    private static void removeExistingNonceCookies(HttpServletRequest request, HttpServletResponse response) {

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return;
        }
        for (Cookie cookie : cookies) {
            String cookieName = cookie.getName();
            if (StringUtils.isNotEmpty(cookieName) && cookieName.startsWith(NONCE_COOKIE)) {
                FrameworkUtils.removeCookie(request, response, cookieName);
            }
        }
    }

    /**
     * Check whether nonce cookie config is enabled or not.
     *
     * @return nonce cookie enabled or not.
     */
    public static boolean isNonceCookieEnabled() {

        if (nonceCookieConfig == null) {
            nonceCookieConfig = Boolean.parseBoolean(IdentityUtil.getProperty(NONCE_COOKIE_CONFIG));
        }
        return nonceCookieConfig;
    }

}
