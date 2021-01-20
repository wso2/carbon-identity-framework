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

public class JsWrapperBuilderUtil {

    private static JsWrapperBuilderUtil jsWrapperBuilderUtil = null;
    private static JsWrapperBuilder jsWrapperBuilder = null;


    private JsWrapperBuilderUtil(){
        if (FrameworkServiceDataHolder.getInstance().getJsGraphBuilderFactory() instanceof JsPolyglotGraphBuilderFactory){
            jsWrapperBuilder = new GraalJsWrapperBuilder();
        }
        else{
            jsWrapperBuilder = new NashornJsWrapperBuilder();
        }
    }

    public static JsWrapperBuilder getInstance(){
        if (jsWrapperBuilderUtil == null){
            jsWrapperBuilderUtil = new JsWrapperBuilderUtil();
        }
        return jsWrapperBuilder;
    }

    public interface JsWrapperBuilder {

        public JsAuthenticatedUser buildJsAuthenticatedUser(AuthenticatedUser authenticatedUser);

        public JsAuthenticationContext buildJsAuthenticationContext(AuthenticationContext authenticationContext);

        public JsCookie buildJsCookie (Cookie cookie);

        public JsParameters buildJsParameters(Map parameters);

        public JsServletRequest buildJsServletRequest (TransientObjectWrapper<HttpServletRequest> wrapped);

        public JsServletResponse buildJsServletResponse (TransientObjectWrapper<HttpServletResponse> wrapped);

        public SerializableJsFunction buildSerializableFunction(String source, boolean isFunction);

    }
}
