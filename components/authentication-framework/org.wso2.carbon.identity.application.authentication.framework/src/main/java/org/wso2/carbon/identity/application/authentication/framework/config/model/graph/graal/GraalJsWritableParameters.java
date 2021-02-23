package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graal;


import org.graalvm.polyglot.Value;

import java.util.List;
import java.util.Map;

/**
 * Javascript wrapper for Java level HashMap of HTTP headers/cookies for Nashorn Execution.
 */
public class GraalJsWritableParameters extends GraalJsParameters {


    public GraalJsWritableParameters(Map wrapped) {
        super(wrapped);
    }

    @Override
    public Object getMember(String name) {

        Object member = getWrapped().get(name);
        if (member instanceof Map) {
            return new GraalJsWritableParameters((Map) member);
        }
        return member;
    }

    @Override
    public boolean removeMember(String name) {

        getWrapped().remove(name);
        return false;
    }

    @Override
    public void putMember(String key, Value value) {
        getWrapped().put(key, value.as(List.class));
    }
}
