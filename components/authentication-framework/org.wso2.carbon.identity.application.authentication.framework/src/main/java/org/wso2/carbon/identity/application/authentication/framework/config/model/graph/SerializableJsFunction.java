package org.wso2.carbon.identity.application.authentication.framework.config.model.graph;

import java.io.Serializable;

/**
 *  Javascript function wrapper. This allows serialization of a javascript defined function.
 *
 */
public class SerializableJsFunction implements Serializable {

    private String name;
    private String source;
    private boolean isFunction;

    public SerializableJsFunction(String name, String source, boolean isFunction) {

        this.name = name;
        this.source = source;
        this.isFunction = isFunction;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getSource() {

        return source;
    }

    public void setSource(String source) {

        this.source = source;
    }

    public boolean isFunction() {

        return isFunction;
    }

    public void setFunction(boolean function) {

        isFunction = function;
    }
}
