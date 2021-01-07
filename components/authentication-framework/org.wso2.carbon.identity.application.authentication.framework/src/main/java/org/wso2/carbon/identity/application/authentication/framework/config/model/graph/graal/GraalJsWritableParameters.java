package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graal;


import org.graalvm.polyglot.Value;

import java.util.List;
import java.util.Map;
import java.lang.String;

public class GraalJsWritableParameters extends GraalJsParameters {


    public GraalJsWritableParameters(Map wrapped) {
        super(wrapped);
    }

    @Override
    public boolean removeMember(String name) {

        if (getWrapped().containsKey(name)) {
            getWrapped().remove(name);
        }
        return false;
    }

    @Override
    public void putMember(String key, Value value) {
        getWrapped().put(key, value.as(List.class));
    }
}
