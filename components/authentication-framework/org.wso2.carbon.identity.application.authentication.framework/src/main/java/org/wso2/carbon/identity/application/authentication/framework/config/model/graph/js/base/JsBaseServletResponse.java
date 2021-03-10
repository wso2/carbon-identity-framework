package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.base;

import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.AbstractJSObjectWrapper;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsServletResponse;
import org.wso2.carbon.identity.application.authentication.framework.context.TransientObjectWrapper;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * Abstract Javascript wrapper for Java level HttpServletResponse.
 * This provides controlled access to HttpServletResponse object via provided javascript native syntax.
 * e.g
 * response.headers.["Set-Cookie"] = ['crsftoken=xxxxxssometokenxxxxx']
 * <p>
 * instead of
 * context.getResponse().addCookie(cookie);
 * <p>
 * Also it prevents writing an arbitrary values to the respective fields,
 * keeping consistency on runtime HttpServletResponse.
 */
public abstract class JsBaseServletResponse extends AbstractJSObjectWrapper<TransientObjectWrapper<HttpServletResponse>>
        implements JsServletResponse {

    public JsBaseServletResponse(TransientObjectWrapper<HttpServletResponse> wrapped) {

        super(wrapped);
    }

    /**
     * Gets the HttpSevletResponse from wrapped Object Wrapper.
     */
    protected HttpServletResponse getResponse() {

        TransientObjectWrapper<HttpServletResponse> transientObjectWrapper = getWrapped();
        return transientObjectWrapper.getWrapped();
    }

    /**
     * Adds the Cookie to Servlet Response.
     */
    public void addCookie(Cookie cookie) {
        getResponse().addCookie(cookie);
    }
}
