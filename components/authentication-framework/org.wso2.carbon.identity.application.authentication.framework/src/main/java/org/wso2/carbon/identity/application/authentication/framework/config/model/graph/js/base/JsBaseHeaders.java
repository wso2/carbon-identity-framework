package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.base;

import java.util.Map;
import javax.servlet.http.HttpServletResponse;

/**
 * Abstract Javascript wrapper for Java level HashMap of HTTP headers.
 * This provides controlled access to HTTPServletResponse object's headers via provided javascript native syntax.
 * Also it prevents writing an arbitrary values to the respective fields, keeping consistency on runtime.
 */
public abstract class JsBaseHeaders {

    protected Map wrapped;
    protected HttpServletResponse response;

    public JsBaseHeaders(Map wrapped, HttpServletResponse response) {

        this.wrapped = wrapped;
        this.response = response;
    }

    public boolean hasMember(String name) {

        if (wrapped == null) {
            return false;
        } else {
            return wrapped.get(name) != null;
        }
    }
}
