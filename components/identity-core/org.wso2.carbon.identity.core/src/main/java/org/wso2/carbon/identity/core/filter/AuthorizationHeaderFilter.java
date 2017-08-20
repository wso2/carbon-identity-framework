/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.core.filter;

import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.lang.StringUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

/**
 * Servlet Filter implementation class for authorization header validation. This handles the NPE thrown by CXF
 * transport exposing the stack-trace and server information to the end user for authorization headers with invalid
 * encoded json data by sending a proper error response back to the user. If there is more than single space between
 * auth_type and json data this will modify the header to be in correct format and pass the request along the filter
 * chain.
 */
@WebFilter("/AuthorizationHeaderFilter")
public class AuthorizationHeaderFilter implements Filter {

    private static final String AUTH_TYPE_BASIC = "Basic";
    private static final int AUTH_TYPE_BASIC_LENGTH = 5; //length of "Basic" String
    private static final int MINIMUM_CREDENTIAL_SIZE = 4;

    private ServletContext context;

    public void init(FilterConfig fConfig) throws ServletException {

        this.context = fConfig.getServletContext();
        this.context.log("AuthorizationHeaderFilter initialized");
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        final HttpServletRequest req = (HttpServletRequest) request;
        final String authHeader = req.getHeader(HTTPConstants.HEADER_AUTHORIZATION);
        if (StringUtils.isEmpty(authHeader)) {
            chain.doFilter(request, response);
            return;
        }

        String authType = null;
        if (authHeader.length() >= AUTH_TYPE_BASIC_LENGTH) {
            authType = authHeader.trim().substring(0, AUTH_TYPE_BASIC_LENGTH);
        }
        if (AUTH_TYPE_BASIC.equals(authType)) {
            final String authCredentials = (authHeader.trim().substring(AUTH_TYPE_BASIC_LENGTH)).trim();
            //if auth header comes in invalid format send error in response
            if (StringUtils.isBlank(authCredentials) || authCredentials.indexOf(' ') >= 0
                    || authCredentials.length() < MINIMUM_CREDENTIAL_SIZE) {
                String errorMsg = "Internal Server Error";
                handleErrorResponse((HttpServletResponse) response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        errorMsg);
            } else if (authHeader.substring((AUTH_TYPE_BASIC + " ").length()).startsWith(" ")) {
                //if there is more than single space between auth_type and credentials modify the request header
                HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(req) {

                    @Override
                    public Enumeration<String> getHeaders(String name) {

                        if (StringUtils.equalsIgnoreCase(name, HTTPConstants.HEADER_AUTHORIZATION)) {

                            Enumeration<String> headerValues = req.getHeaders(name);
                            ArrayList<String> newHeaderValues = new ArrayList<>();
                            while (headerValues.hasMoreElements()) {
                                String value = headerValues.nextElement();
                                if (StringUtils.equals(value, authHeader)) {
                                    value = AUTH_TYPE_BASIC + " " + authCredentials;
                                    newHeaderValues.add(value);
                                } else {
                                    newHeaderValues.add(value);
                                }
                            }
                            return Collections.enumeration(newHeaderValues);
                        }
                        return super.getHeaders(name);
                    }
                };
                // pass the request along the filter chain
                chain.doFilter(wrapper, response);
            }
            //if auth header comes in correct format, forward
            else {
                chain.doFilter(request, response);
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    public void destroy() {
        //we can close resources here
    }

    private void handleErrorResponse(HttpServletResponse response, int error, String errorMsg) throws IOException {

        response.sendError(error, errorMsg);
    }
}
