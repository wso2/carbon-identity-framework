package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.base;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.AbstractJSObjectWrapper;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsParameters;

import java.util.Map;

/**
 * Abstract Javascript wrapper for Java level HashMap of HTTP headers/cookies.
 * This provides controlled access to HTTPServletRequest object's headers and cookies via provided javascript native
 * syntax.
 * Also it prevents writing an arbitrary values to the respective fields, keeping consistency on runtime.
 */
public abstract class JsBaseParameters extends AbstractJSObjectWrapper<Map> implements JsParameters {

    protected static final Log LOG = LogFactory.getLog(JsBaseParameters.class);

    public JsBaseParameters(Map wrapped) {
        super(wrapped);
    }

    @Override
    public boolean hasMember(String name) {
        return getWrapped().get(name) != null;
    }
}
