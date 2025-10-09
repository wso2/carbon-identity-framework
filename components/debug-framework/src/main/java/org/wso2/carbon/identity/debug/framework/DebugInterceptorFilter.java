package org.wso2.carbon.identity.debug.framework;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet filter that intercepts requests to /commonauth and handles debug flows.
 * This filter runs before the WSO2 authentication framework and can handle debug callbacks
 * even when authentication contexts have expired.
 */
public class DebugInterceptorFilter implements Filter {

    private static final Log LOG = LogFactory.getLog(DebugInterceptorFilter.class);
    private RequestCoordinator debugRequestCoordinator;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        try {
            this.debugRequestCoordinator = new RequestCoordinator();
            LOG.info("Debug Interceptor Filter initialized successfully");
        } catch (Exception e) {
            LOG.error("Failed to initialize Debug Interceptor Filter: " + e.getMessage(), e);
            throw new ServletException("Debug filter initialization failed", e);
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, 
                        FilterChain filterChain) throws IOException, ServletException {
        
        if (!(servletRequest instanceof HttpServletRequest) || 
            !(servletResponse instanceof HttpServletResponse)) {
            // Not HTTP request, pass through
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        try {
            // Check if this is a debug flow and handle it
            if (debugRequestCoordinator != null && 
                debugRequestCoordinator.handleCommonAuthRequest(request, response)) {
                
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Debug flow handled by interceptor filter for URI: " + request.getRequestURI());
                }
                // Debug flow handled, don't proceed to WSO2 authentication framework
                return;
            }

            // Not a debug flow, proceed to normal WSO2 authentication
            filterChain.doFilter(servletRequest, servletResponse);

        } catch (Exception e) {
            LOG.error("Error in debug interceptor filter: " + e.getMessage(), e);
            // In case of error, proceed to normal flow
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    @Override
    public void destroy() {
        debugRequestCoordinator = null;
        LOG.info("Debug Interceptor Filter destroyed");
    }
}
