package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js;

import org.wso2.carbon.identity.application.authentication.framework.context.TransientObjectWrapper;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * Interface for JavaScript Servlet Response Wrapper.
 */
public interface JsServletResponse {
    TransientObjectWrapper<HttpServletResponse> getWrapped();

    void addCookie(Cookie cookie);
}
