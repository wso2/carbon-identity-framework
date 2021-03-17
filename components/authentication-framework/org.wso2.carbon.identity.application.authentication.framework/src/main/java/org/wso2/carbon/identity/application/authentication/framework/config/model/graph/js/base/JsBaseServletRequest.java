package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.base;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.AbstractJSObjectWrapper;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsServletRequest;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.graal.GraalJsServletRequest;
import org.wso2.carbon.identity.application.authentication.framework.context.TransientObjectWrapper;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;

import javax.servlet.http.HttpServletRequest;

/**
 * Abstract Javascript wrapper for Java level HTTPServletRequest.
 * This provides controlled access to HTTPServletRequest object via provided javascript native syntax.
 * e.g
 * var redirect_uri = context.request.params.redirect_uri
 * <p>
 * instead of
 * var userName = context.getRequest().getParameter("redirect_uri)
 * <p>
 * Also it prevents writing an arbitrary values to the respective fields,
 * keeping consistency on runtime HTTPServletRequest.
 */
public abstract class JsBaseServletRequest extends AbstractJSObjectWrapper<TransientObjectWrapper<HttpServletRequest>>
implements JsServletRequest {

    protected static final Log LOG = LogFactory.getLog(GraalJsServletRequest.class);


    public JsBaseServletRequest(TransientObjectWrapper<HttpServletRequest> wrapped) {

        super(wrapped);
    }

    @Override
    public boolean hasMember(String name) {

        if (getRequest() == null) {
            //Transient Object is null, hence no member access is possible.
            return false;
        }
        switch (name) {
            case FrameworkConstants.JSAttributes.JS_HEADERS:
                return getRequest().getHeaderNames() != null;
            case FrameworkConstants.JSAttributes.JS_PARAMS:
                return getRequest().getParameterMap() != null;
            case FrameworkConstants.JSAttributes.JS_COOKIES:
                return getRequest().getCookies() != null;
            default:
                return super.hasMember(name);
        }
    }

    protected HttpServletRequest getRequest() {

        TransientObjectWrapper<HttpServletRequest> transientObjectWrapper = getWrapped();
        return transientObjectWrapper.getWrapped();
    }
}
