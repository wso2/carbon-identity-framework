package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graal;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsHeaders;
import org.wso2.carbon.identity.application.authentication.framework.context.TransientObjectWrapper;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class GraalJsServletResponse extends AbstractJSObjectWrapper<TransientObjectWrapper<HttpServletResponse>> implements ProxyObject {

    public GraalJsServletResponse(TransientObjectWrapper<HttpServletResponse> wrapped) {

        super(wrapped);
    }

    @Override
    public Object getMember(String name) {

        switch (name) {
            case FrameworkConstants.JSAttributes.JS_HEADERS:
                Map headers = new HashMap();
                Collection<String> headerNames = getResponse().getHeaderNames();
                if (headerNames != null) {
                    for (String element : headerNames) {
                        headers.put(element, getResponse().getHeader(element));
                    }
                }
                return new JsHeaders(headers, getResponse());
            default:
                return super.getMember(name);
        }
    }

    @Override
    public Object getMemberKeys() {

        return null;
    }

    @Override
    public boolean hasMember(String name) {

        if (getResponse() == null) {
            //Transient Object is null, hence no member access is possible.
            return false;
        }

        switch (name) {
            case FrameworkConstants.JSAttributes.JS_HEADERS:
                return getResponse().getHeaderNames() != null;
            default:
                return super.hasMember(name);
        }
    }

    @Override
    public void putMember(String key, Value value) {

    }

    private HttpServletResponse getResponse() {

        TransientObjectWrapper<HttpServletResponse> transientObjectWrapper = getWrapped();
        return transientObjectWrapper.getWrapped();
    }

    public void addCookie(Cookie cookie) {
        getResponse().addCookie(cookie);
    }
}
