package org.wso2.carbon.identity.application.authentication.framework.config.model.graph;

import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;

/**
 * Singleton to get the appropriate Javascript Proxy Object Wrapper Factory.
 */
public class JsWrapperFactorySingleton {

    private static JsWrapperFactorySingleton jsWrapperFactorySingleton = null;
    private static JsWrapperFactory jsWrapperFactory = null;


    private JsWrapperFactorySingleton() {
        if (FrameworkServiceDataHolder.getInstance().
                getJsGraphBuilderFactory() instanceof JsPolyglotGraphBuilderFactory) {
            jsWrapperFactory = new GraalJsWrapperFactory();
        } else {
            jsWrapperFactory = new NashornJsWrapperFactory();
        }
    }

    public static JsWrapperFactory getInstance() {
        if (jsWrapperFactorySingleton == null) {
            jsWrapperFactorySingleton = new JsWrapperFactorySingleton();
        }
        return jsWrapperFactory;
    }

    public static void clearInstance() {
        jsWrapperFactory = null;
        jsWrapperFactorySingleton = null;
    }

}
