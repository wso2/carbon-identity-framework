package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graal;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;

import java.util.Map;
import javax.servlet.http.HttpServletResponse;

public class GraalJsHeaders implements ProxyObject {

    private Map wrapped;
    private HttpServletResponse response;

    public GraalJsHeaders(Map wrapped, HttpServletResponse response) {

        this.wrapped = wrapped;
        this.response = response;
    }

    @Override
    public Object getMember(String name) {

        if (wrapped == null) {
            return false;
        } else {
            return wrapped.get(name);
        }
    }

    @Override
    public Object getMemberKeys() {

        return null;
    }

    @Override
    public boolean hasMember(String name) {

        if (wrapped == null) {
            return false;
        } else {
            return wrapped.get(name) != null;
        }
    }


    @Override
    public boolean removeMember(String name) {

        if (wrapped == null) {
            return false;
        } else {
            if (wrapped.containsKey(name)) {
                wrapped.remove(name);
            }
        }
        return false;
    }

    @Override
    public void putMember(String name, Value value) {

        if (wrapped != null) {
            wrapped.put(name, value);
            //adds a new header to the response.
            response.addHeader(name, String.valueOf(value));
        }
    }
}
