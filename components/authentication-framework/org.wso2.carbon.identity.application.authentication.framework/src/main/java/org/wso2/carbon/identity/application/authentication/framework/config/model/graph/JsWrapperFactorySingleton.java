package org.wso2.carbon.identity.application.authentication.framework.config.model.graph;

import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsAuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsAuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsCookie;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsParameters;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsServletRequest;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsServletResponse;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.TransientObjectWrapper;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;

import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

    public interface JsWrapperFactory {

        JsAuthenticatedUser createJsAuthenticatedUser(AuthenticatedUser authenticatedUser);

        JsAuthenticationContext createJsAuthenticationContext(AuthenticationContext authenticationContext);

        JsCookie createJsCookie(Cookie cookie);

        JsParameters createJsParameters(Map parameters);

        JsServletRequest createJsServletRequest(TransientObjectWrapper<HttpServletRequest> wrapped);

        JsServletResponse createJsServletResponse(TransientObjectWrapper<HttpServletResponse> wrapped);

        SerializableJsFunction createSerializableFunction(String source, boolean isFunction);

    }
}
