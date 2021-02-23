package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsParameters;

import java.util.Map;

/**
 * Javascript wrapper for Java level HashMap of HTTP headers/cookies for GraalJs Execution.
 * This provides controlled access to HTTPServletRequest object's headers and cookies via provided javascript native
 * syntax.
 * Also it prevents writing an arbitrary values to the respective fields, keeping consistency on runtime.
 */
public class GraalJsParameters extends AbstractJSObjectWrapper<Map> implements ProxyObject, JsParameters {

    private static final Log LOG = LogFactory.getLog(GraalJsParameters.class);

    public GraalJsParameters(Map wrapped) {

        super(wrapped);
    }

    @Override
    public Object getMember(String name) {

        Object member = getWrapped().get(name);
        if (member instanceof Map) {
            return new GraalJsParameters((Map) member);
        }
        return member;
    }

    @Override
    public Object getMemberKeys() {

        return null;
    }

    @Override
    public boolean hasMember(String name) {

        return getWrapped().get(name) != null;
    }

    public void putMember(String key, Value value) {
        LOG.warn("Unsupported operation. Parameters are read only. Can't set parameter " + key + " to value: " + value);
    }

    @Override
    public boolean removeMember(String name) {

        LOG.warn("Unsupported operation. Parameters are read only. Can't remove parameter " + name);
        return false;
    }
}
