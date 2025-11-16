/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.debug.framework;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet filter that intercepts /commonauth/* requests to handle debug flow callbacks.
 * This filter runs BEFORE the normal CommonAuthenticationHandler servlet.
 * It checks if a request is a debug flow callback and if so, ensures it's properly
 * routed to the debug request coordinator.
 *
 * The filter does not modify the request/response; instead, it relies on the
 * downstream CommonAuthenticationHandler and DebugService to handle debug flows.
 * The main purpose is to add logging and ensure debug requests are handled first.
 */
public class DebugInterceptorFilter implements Filter {

    private static final Log LOG = LogFactory.getLog(DebugInterceptorFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOG.info("DebugInterceptorFilter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
                HttpServletRequest httpRequest = (HttpServletRequest) request;

                // Log incoming request details for debug visibility
                String state = httpRequest.getParameter("state");
                String code = httpRequest.getParameter("code");
                String error = httpRequest.getParameter("error");

                // Check if this looks like a debug callback (state parameter starts with "debug-")
                if (state != null && state.startsWith("debug-")) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("DebugInterceptorFilter: Potential debug callback detected - State: " + state +
                                " | Code: " + (code != null ? "present" : "absent") +
                                " | Error: " + (error != null ? error : "none"));
                    }
                } else if (LOG.isDebugEnabled()) {
                    if (state != null || code != null || error != null) {
                        LOG.debug("DebugInterceptorFilter: Non-debug callback - State: " + state +
                                " | Code: " + code + " | Error: " + error);
                    }
                }

                // Check if the debug flow has already handled this request
                // (This flag would be set by the debug flow to prevent fallthrough)
                Boolean debugHandled = (Boolean) httpRequest.getAttribute(
                        "debugRequestHandled");
                if (debugHandled != null && debugHandled) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("DebugInterceptorFilter: Request already handled by debug flow");
                    }
                }
            }

            // Continue to next filter/servlet in the chain
            chain.doFilter(request, response);

        } catch (IOException e) {
            LOG.error("IOException in DebugInterceptorFilter: " + e.getMessage(), e);
            // Re-throw IOException as it's part of normal filter operation
            throw e;
        } catch (ServletException e) {
            LOG.error("ServletException in DebugInterceptorFilter: " + e.getMessage(), e);
            // Re-throw ServletException as it's part of normal filter operation
            throw e;
        } catch (Exception e) {
            LOG.error("Unexpected error in DebugInterceptorFilter: " + e.getMessage(), e);
            // Log but attempt to continue to next filter/servlet
            // This prevents a single filter error from breaking the entire request pipeline
            try {
                chain.doFilter(request, response);
            } catch (Exception chainException) {
                LOG.error("Error continuing filter chain after DebugInterceptorFilter exception: " + 
                        chainException.getMessage(), chainException);
                throw new ServletException("DebugInterceptorFilter encountered an error", chainException);
            }
        }
    }

    @Override
    public void destroy() {
        LOG.info("DebugInterceptorFilter destroyed");
    }
}
