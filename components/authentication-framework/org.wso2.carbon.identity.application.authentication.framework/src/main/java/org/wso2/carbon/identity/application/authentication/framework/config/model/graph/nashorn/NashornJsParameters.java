package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.nashorn;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsParameters;

import java.util.Map;

/**
 * Javascript wrapper for Java level HashMap of HTTP headers/cookies for Nashorn Execution.
 * This provides controlled access to HTTPServletRequest object's headers and cookies via provided javascript native
 * syntax.
 * Also it prevents writing an arbitrary values to the respective fields, keeping consistency on runtime.
 */
public class NashornJsParameters extends AbstractJSObjectWrapper<Map> implements JsParameters {

    private static final Log LOG = LogFactory.getLog(JsParameters.class);

    public NashornJsParameters(Map wrapped) {
        super(wrapped);
    }

    @Override
    public Object getMember(String name) {
        return getWrapped().get(name);
    }

    @Override
    public boolean hasMember(String name) {
        return getWrapped().get(name) != null;
    }

    @Override
    public void removeMember(String name) {

        LOG.warn("Unsupported operation. Parameters are read only. Can't remove parameter " + name);
    }

    @Override
    public void setMember(String name, Object value) {

        LOG.warn("Unsupported operation. Parameters are read only. Can't set parameter " +
                name + " to value: " + value);
    }
}
