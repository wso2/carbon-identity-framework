/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.servlet.mgt;

import org.apache.cxf.Bus;
import org.apache.cxf.common.classloader.ClassLoaderUtils;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.common.util.PrimitiveUtils;
import org.apache.cxf.common.util.PropertyUtils;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.feature.Feature;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.ext.search.SearchBean;
import org.apache.cxf.jaxrs.ext.search.odata.ODataParser;
import org.apache.cxf.jaxrs.lifecycle.PerRequestResourceProvider;
import org.apache.cxf.jaxrs.lifecycle.ResourceProvider;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.apache.cxf.jaxrs.model.ApplicationInfo;
import org.apache.cxf.jaxrs.model.ProviderInfo;
import org.apache.cxf.jaxrs.provider.ProviderFactory;
import org.apache.cxf.jaxrs.utils.InjectionUtils;
import org.apache.cxf.jaxrs.utils.ResourceUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.invoker.Invoker;
import org.apache.cxf.transport.http.DestinationRegistry;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.ws.rs.core.Application;

/**
 * Custom CXFNonSpringJaxrsServlet to override the default behavior of CXFNonSpringJaxrsServlet.
 */
public class CustomCXFNonSpringJaxrsServlet extends CXFNonSpringServlet {

    private static final long serialVersionUID = -8916352798780577499L;
    private static final Logger LOG = LogUtils.getL7dLogger(CustomCXFNonSpringJaxrsServlet.class);
    private static final String USER_MODEL_PARAM = "user.model";
    private static final String SERVICE_ADDRESS_PARAM = "jaxrs.address";
    private static final String IGNORE_APP_PATH_PARAM = "jaxrs.application.address.ignore";
    private static final String SERVICE_CLASSES_PARAM = "jaxrs.serviceClasses";
    private static final String PROVIDERS_PARAM = "jaxrs.providers";
    private static final String FEATURES_PARAM = "jaxrs.features";
    private static final String OUT_INTERCEPTORS_PARAM = "jaxrs.outInterceptors";
    private static final String OUT_FAULT_INTERCEPTORS_PARAM = "jaxrs.outFaultInterceptors";
    private static final String IN_INTERCEPTORS_PARAM = "jaxrs.inInterceptors";
    private static final String INVOKER_PARAM = "jaxrs.invoker";
    private static final String SERVICE_SCOPE_PARAM = "jaxrs.scope";
    private static final String EXTENSIONS_PARAM = "jaxrs.extensions";
    private static final String LANGUAGES_PARAM = "jaxrs.languages";
    private static final String PROPERTIES_PARAM = "jaxrs.properties";
    private static final String SCHEMAS_PARAM = "jaxrs.schemaLocations";
    private static final String DOC_LOCATION_PARAM = "jaxrs.documentLocation";
    private static final String STATIC_SUB_RESOLUTION_PARAM = "jaxrs.static.subresources";
    private static final String SERVICE_SCOPE_SINGLETON = "singleton";
    private static final String SERVICE_SCOPE_REQUEST = "prototype";
    private static final String PARAMETER_SPLIT_CHAR = "class.parameter.split.char";
    private static final String DEFAULT_PARAMETER_SPLIT_CHAR = ",";
    private static final String SPACE_PARAMETER_SPLIT_CHAR = "space";
    private static final String JAXRS_APPLICATION_PARAM = "javax.ws.rs.Application";
    private static final String SEARCH_QUERY_PARAMETER_NAME = "search.query.parameter.name";
    private static final String FILTER = "filter";
    private static final String SEARCH_PARSER = "search.parser";

    private ClassLoader classLoader;
    private Application application;

    public CustomCXFNonSpringJaxrsServlet() {
    }

    public CustomCXFNonSpringJaxrsServlet(Application app) {

        this.application = app;
    }

    public CustomCXFNonSpringJaxrsServlet(Application app, DestinationRegistry destinationRegistry, Bus bus) {

        super(destinationRegistry, bus);
        this.application = app;
    }

    public void init(ServletConfig servletConfig) throws ServletException {

        super.init(servletConfig);
        configureSearchProperties();
        if (this.getApplication() != null) {
            this.createServerFromApplication(servletConfig);
        } else {
            String applicationClass = servletConfig.getInitParameter(JAXRS_APPLICATION_PARAM);
            if (applicationClass != null) {
                this.createServerFromApplication(applicationClass, servletConfig);
            } else {
                String splitChar = this.getParameterSplitChar(servletConfig);
                JAXRSServerFactoryBean bean = new JAXRSServerFactoryBean();
                bean.setBus(this.getBus());
                String address = servletConfig.getInitParameter(SERVICE_ADDRESS_PARAM);
                if (address == null) {
                    address = "/";
                }

                bean.setAddress(address);
                bean.setStaticSubresourceResolution(this.getStaticSubResolutionValue(servletConfig));
                String modelRef = servletConfig.getInitParameter(USER_MODEL_PARAM);
                if (modelRef != null) {
                    bean.setModelRef(modelRef.trim());
                }

                this.setDocLocation(bean, servletConfig);
                this.setSchemasLocations(bean, servletConfig);
                this.setAllInterceptors(bean, servletConfig, splitChar);
                this.setInvoker(bean, servletConfig);
                Map<Class<?>, Map<String, List<String>>> resourceClasses = this.getServiceClasses(servletConfig,
                        modelRef != null, splitChar);
                Map<Class<?>, ResourceProvider> resourceProviders = this.getResourceProviders(servletConfig,
                        resourceClasses);
                List<?> providers = this.getProviders(servletConfig, splitChar);
                bean.setResourceClasses(new ArrayList<>(resourceClasses.keySet()));
                bean.setProviders(providers);

                for (Map.Entry<Class<?>, ResourceProvider> entry : resourceProviders.entrySet()) {
                    bean.setResourceProvider(entry.getKey(), entry.getValue());
                }

                this.setExtensions(bean, servletConfig);
                List<? extends Feature> features = this.getFeatures(servletConfig, splitChar);
                bean.getFeatures().addAll(features);
                bean.create();
            }
        }
    }

    private void configureSearchProperties() {

        this.getBus().setProperty(SEARCH_QUERY_PARAMETER_NAME, FILTER);
        this.getBus().setProperty(SEARCH_PARSER, new ODataParser<>(SearchBean.class));
    }

    protected String getParameterSplitChar(ServletConfig servletConfig) {

        String param = servletConfig.getInitParameter(PARAMETER_SPLIT_CHAR);
        return !StringUtils.isEmpty(param) && SPACE_PARAMETER_SPLIT_CHAR.equals(param.trim()) ? " "
                : DEFAULT_PARAMETER_SPLIT_CHAR;
    }

    protected boolean getStaticSubResolutionValue(ServletConfig servletConfig) {

        String param = servletConfig.getInitParameter(STATIC_SUB_RESOLUTION_PARAM);
        return param != null && Boolean.parseBoolean(param.trim());
    }

    protected void setExtensions(JAXRSServerFactoryBean bean, ServletConfig servletConfig) {

        bean.setExtensionMappings(CastUtils.cast(parseMapSequence(servletConfig.getInitParameter(EXTENSIONS_PARAM))));
        bean.setLanguageMappings(CastUtils.cast(parseMapSequence(servletConfig.getInitParameter(LANGUAGES_PARAM))));
        Map<String, Object> properties = CastUtils.cast(parseMapSequence(
                servletConfig.getInitParameter(PROPERTIES_PARAM)), String.class, Object.class);

        //Custom impl to allow property values to be defined as system properties
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue().toString();

            if (value.startsWith("{systemProperties")) {
                int begin = value.indexOf("'");
                int end = value.lastIndexOf("'");
                String propertyKey = value.substring(begin + 1, end);
                String systemPropValue = System.getProperty(propertyKey);
                if (systemPropValue != null && !systemPropValue.isEmpty()) {
                    properties.put(key, systemPropValue);
                }
            }
        }

        if (!properties.isEmpty()) {
            bean.getProperties(true).putAll(properties);
        }
    }

    protected void setAllInterceptors(JAXRSServerFactoryBean bean, ServletConfig servletConfig,
                                      String splitChar) throws ServletException {

        this.setInterceptors(bean, servletConfig, OUT_INTERCEPTORS_PARAM, splitChar);
        this.setInterceptors(bean, servletConfig, OUT_FAULT_INTERCEPTORS_PARAM, splitChar);
        this.setInterceptors(bean, servletConfig, IN_INTERCEPTORS_PARAM, splitChar);
    }

    protected void setSchemasLocations(JAXRSServerFactoryBean bean, ServletConfig servletConfig) {

        String schemas = servletConfig.getInitParameter(SCHEMAS_PARAM);
        if (schemas != null) {
            String[] locations = schemas.split(" ");
            List<String> list = new ArrayList<>();

            for (String loc : locations) {
                String theLoc = loc.trim();
                if (!theLoc.isEmpty()) {
                    list.add(theLoc);
                }
            }

            if (!list.isEmpty()) {
                bean.setSchemaLocations(list);
            }
        }
    }

    protected void setDocLocation(JAXRSServerFactoryBean bean, ServletConfig servletConfig) {

        String docLocation = servletConfig.getInitParameter(DOC_LOCATION_PARAM);
        if (docLocation != null) {
            bean.setDocLocation(docLocation);
        }
    }

    protected void setInterceptors(JAXRSServerFactoryBean bean, ServletConfig servletConfig, String paramName,
                                   String splitChar) throws ServletException {

        String initParameter = servletConfig.getInitParameter(paramName);
        if (initParameter != null) {
            String[] values = initParameter.split(splitChar);
            List<Interceptor<? extends Message>> interceptors = new ArrayList<>();

            for (String interceptorVal : values) {
                Map<String, List<String>> props = new HashMap<>();
                String theValue = this.getClassNameAndProperties(interceptorVal, props);
                if (!theValue.isEmpty()) {
                    try {
                        Class<?> intClass = this.loadClass(theValue, "Interceptor");
                        Object object = intClass.getDeclaredConstructor().newInstance();
                        this.injectProperties(object, props);
                        interceptors.add((Interceptor<? extends Message>) object);
                    } catch (ServletException exception) {
                        throw exception;
                    } catch (Exception exception) {
                        LOG.warning("Interceptor class " + theValue + " can not be created");
                        throw new ServletException(exception);
                    }
                }
            }

            if (!interceptors.isEmpty()) {
                if (OUT_INTERCEPTORS_PARAM.equals(paramName)) {
                    bean.setOutInterceptors(interceptors);
                } else if (OUT_FAULT_INTERCEPTORS_PARAM.equals(paramName)) {
                    bean.setOutFaultInterceptors(interceptors);
                } else {
                    bean.setInInterceptors(interceptors);
                }
            }
        }
    }

    protected void setInvoker(JAXRSServerFactoryBean bean, ServletConfig servletConfig) throws ServletException {

        String initParameter = servletConfig.getInitParameter(INVOKER_PARAM);
        if (initParameter != null) {
            Map<String, List<String>> props = new HashMap<>();
            String classNameAndProperties = this.getClassNameAndProperties(initParameter, props);
            if (!classNameAndProperties.isEmpty()) {
                try {
                    Class<?> intClass = this.loadClass(classNameAndProperties, "Invoker");
                    Object object = intClass.getDeclaredConstructor().newInstance();
                    this.injectProperties(object, props);
                    bean.setInvoker((Invoker) object);
                } catch (ServletException exception) {
                    throw exception;
                } catch (Exception exception) {
                    LOG.warning("Invoker class " + classNameAndProperties + " can not be created");
                    throw new ServletException(exception);
                }
            }
        }
    }

    protected Map<Class<?>, Map<String, List<String>>> getServiceClasses(ServletConfig servletConfig,
                                                                         boolean modelAvailable, String splitChar)
            throws ServletException {

        String serviceBeans = servletConfig.getInitParameter(SERVICE_CLASSES_PARAM);
        if (serviceBeans == null) {
            if (modelAvailable) {
                return Collections.emptyMap();
            } else {
                throw new ServletException("At least one resource class should be specified");
            }
        } else {
            String[] classNames = serviceBeans.split(splitChar);
            Map<Class<?>, Map<String, List<String>>> classMapHashMap = new HashMap<>();

            for (String cName : classNames) {
                Map<String, List<String>> props = new HashMap<>();
                String theName = this.getClassNameAndProperties(cName, props);
                if (!theName.isEmpty()) {
                    Class<?> cls = this.loadClass(theName);
                    classMapHashMap.put(cls, props);
                }
            }

            if (classMapHashMap.isEmpty()) {
                throw new ServletException("At least one resource class should be specified");
            } else {
                return classMapHashMap;
            }
        }
    }

    protected List<Feature> getFeatures(ServletConfig servletConfig, String splitChar)
            throws ServletException {

        String featuresList = servletConfig.getInitParameter(FEATURES_PARAM);
        if (featuresList == null) {
            return Collections.emptyList();
        } else {
            String[] classNames = featuresList.split(splitChar);
            List<Feature> features = new ArrayList<>();

            for (String cName : classNames) {
                Map<String, List<String>> props = new HashMap<>();
                String classNameAndProperties = this.getClassNameAndProperties(cName, props);
                if (!classNameAndProperties.isEmpty()) {
                    Class<?> cls = this.loadClass(classNameAndProperties);
                    if (Feature.class.isAssignableFrom(cls)) {
                        features.add((Feature) this.createSingletonInstance(cls, props, servletConfig));
                    }
                }
            }
            return features;
        }
    }

    protected List<Object> getProviders(ServletConfig servletConfig, String splitChar) throws ServletException {

        String providersList = servletConfig.getInitParameter(PROVIDERS_PARAM);
        if (providersList == null) {
            return Collections.emptyList();
        } else {
            String[] classNames = providersList.split(splitChar);
            List<Object> providers = new ArrayList<>();

            for (String cName : classNames) {
                Map<String, List<String>> props = new HashMap<>();
                String theName = this.getClassNameAndProperties(cName, props);
                if (!theName.isEmpty()) {
                    Class<?> cls = this.loadClass(theName);
                    providers.add(this.createSingletonInstance(cls, props, servletConfig));
                }
            }
            return providers;
        }
    }

    private String getClassNameAndProperties(String cName, Map<String, List<String>> props) {

        String className = cName.trim();
        int ind = className.indexOf(40);
        if (ind != -1 && className.endsWith(")")) {
            props.putAll(parseMapListSequence(className.substring(ind + 1, className.length() - 1)));
            className = className.substring(0, ind).trim();
        }

        return className;
    }

    protected static Map<String, List<String>> parseMapListSequence(String sequence) {

        if (sequence != null) {
            sequence = sequence.trim();
            Map<String, List<String>> sequenceList = new HashMap<>();
            String[] pairs = sequence.split(" ");

            for (String pair : pairs) {
                String thePair = pair.trim();
                if (!thePair.isEmpty()) {
                    String[] values = thePair.split("=");
                    String key;
                    String value;
                    if (values.length == 2) {
                        key = values[0].trim();
                        value = values[1].trim();
                    } else {
                        key = thePair;
                        value = "";
                    }

                    List<String> keysList = sequenceList.computeIfAbsent(key, k -> new LinkedList<>());
                    (keysList).add(value);
                }
            }
            return sequenceList;
        } else {
            return Collections.emptyMap();
        }
    }

    protected Map<Class<?>, ResourceProvider> getResourceProviders(ServletConfig servletConfig, Map<Class<?>,
            Map<String, List<String>>> resourceClasses) throws ServletException {

        String scope = servletConfig.getInitParameter(SERVICE_SCOPE_PARAM);
        if (scope != null && !SERVICE_SCOPE_SINGLETON.equals(scope) && !SERVICE_SCOPE_REQUEST.equals(scope)) {
            throw new ServletException("Only singleton and prototype scopes are supported");
        } else {
            boolean isPrototype = SERVICE_SCOPE_REQUEST.equals(scope);
            Map<Class<?>, ResourceProvider> providerHashMap = new HashMap<>();

            for (Map.Entry<Class<?>, Map<String, List<String>>> entry : resourceClasses.entrySet()) {
                Class<?> entryKey = entry.getKey();
                providerHashMap.put(entryKey, isPrototype ? new PerRequestResourceProvider(entryKey) :
                        new SingletonResourceProvider(this.createSingletonInstance(entryKey, entry.getValue(),
                                servletConfig), true));
            }
            return providerHashMap;
        }
    }

    protected boolean isAppResourceLifecycleASingleton(Application app, ServletConfig servletConfig) {

        String scope = servletConfig.getInitParameter(SERVICE_SCOPE_PARAM);
        if (scope == null) {
            scope = (String) app.getProperties().get(SERVICE_SCOPE_PARAM);
        }
        return SERVICE_SCOPE_SINGLETON.equals(scope);
    }

    protected Object createSingletonInstance(Class<?> cls, Map<String, List<String>> props, ServletConfig sc)
            throws ServletException {

        Constructor<?> resourceConstructor = ResourceUtils.findResourceConstructor(cls, false);
        if (resourceConstructor == null) {
            throw new ServletException("No valid constructor found for " + cls.getName());
        } else {
            boolean isApplication = Application.class.isAssignableFrom(resourceConstructor.getDeclaringClass());

            try {
                ProviderInfo<?> provider;
                if (resourceConstructor.getParameterTypes().length == 0) {
                    if (isApplication) {
                        provider = new ApplicationInfo((Application) resourceConstructor.newInstance(), this.getBus());
                    } else {
                        provider = new ProviderInfo<>(resourceConstructor.newInstance(), this.getBus(),
                                false, true);
                    }
                } else {
                    Map<Class<?>, Object> values = new HashMap<>();
                    values.put(ServletContext.class, sc.getServletContext());
                    values.put(ServletConfig.class, sc);
                    provider = ProviderFactory.createProviderFromConstructor(resourceConstructor, values,
                            this.getBus(), isApplication, true);
                }

                Object instance = provider.getProvider();
                this.injectProperties(instance, props);
                return isApplication ? provider : instance;
            } catch (InstantiationException exception) {
                throw new ServletException("Resource class " + cls.getName() + " can not be instantiated");
            } catch (IllegalAccessException exception) {
                throw new ServletException("Resource class " + cls.getName() + " " +
                        "can not be instantiated due to IllegalAccessException");
            } catch (InvocationTargetException exception) {
                throw new ServletException("Resource class " + cls.getName() + " " +
                        "can not be instantiated due to InvocationTargetException");
            }
        }
    }

    private void injectProperties(Object instance, Map<String, List<String>> props) {

        if (props != null && !props.isEmpty()) {
            Method[] methods = instance.getClass().getMethods();
            Map<String, Method> methodsMap = new HashMap<>();

            for (Method method : methods) {
                methodsMap.put(method.getName(), method);
            }

            for (Map.Entry<String, List<String>> entry : props.entrySet()) {
                Method method = methodsMap.get("set" + StringUtils.capitalize(entry.getKey()));
                if (method != null) {
                    Class<?> type = method.getParameterTypes()[0];
                    Object value;
                    if (InjectionUtils.isPrimitive(type)) {
                        value = PrimitiveUtils.read((String) ((List<?>) entry.getValue()).get(0), type);
                    } else {
                        value = entry.getValue();
                    }
                    InjectionUtils.injectThroughMethod(instance, method, value);
                }
            }
        }
    }

    protected void createServerFromApplication(String applicationNames, ServletConfig servletConfig)
            throws ServletException {

        boolean ignoreApplicationPath = this.isIgnoreApplicationPath(servletConfig);
        String[] classNames = applicationNames.split(this.getParameterSplitChar(servletConfig));
        if (classNames.length > 1 && ignoreApplicationPath) {
            throw new ServletException("\"jaxrs.application.address.ignore\" parameter must be set to false for " +
                    "multiple Applications be supported");
        } else {
            for (String cName : classNames) {
                ApplicationInfo providerApp = this.createApplicationInfo(cName, servletConfig);
                Application app = providerApp.getProvider();
                JAXRSServerFactoryBean bean = ResourceUtils.createApplication(app, ignoreApplicationPath,
                        this.getStaticSubResolutionValue(servletConfig), this.isAppResourceLifecycleASingleton(app,
                                servletConfig), this.getBus());
                String splitChar = this.getParameterSplitChar(servletConfig);
                this.setAllInterceptors(bean, servletConfig, splitChar);
                this.setInvoker(bean, servletConfig);
                this.setExtensions(bean, servletConfig);
                this.setDocLocation(bean, servletConfig);
                this.setSchemasLocations(bean, servletConfig);
                List<?> providers = this.getProviders(servletConfig, splitChar);
                bean.setProviders(providers);
                List<Feature> features = this.getFeatures(servletConfig, splitChar);
                bean.getFeatures().addAll(features);
                bean.setBus(this.getBus());
                bean.setApplicationInfo(providerApp);
                bean.create();
            }
        }
    }

    protected boolean isIgnoreApplicationPath(ServletConfig servletConfig) {

        String ignoreParam = servletConfig.getInitParameter(IGNORE_APP_PATH_PARAM);
        return ignoreParam == null || PropertyUtils.isTrue(ignoreParam);
    }

    protected void createServerFromApplication(ServletConfig servletConfig) throws ServletException {

        Application app = this.getApplication();
        JAXRSServerFactoryBean bean = ResourceUtils.createApplication(app, this.isIgnoreApplicationPath(servletConfig),
                this.getStaticSubResolutionValue(servletConfig),
                this.isAppResourceLifecycleASingleton(app, servletConfig), this.getBus());
        String splitChar = this.getParameterSplitChar(servletConfig);
        this.setAllInterceptors(bean, servletConfig, splitChar);
        this.setInvoker(bean, servletConfig);
        this.setExtensions(bean, servletConfig);
        this.setDocLocation(bean, servletConfig);
        this.setSchemasLocations(bean, servletConfig);
        List<?> providers = this.getProviders(servletConfig, splitChar);
        bean.setProviders(providers);
        List<Feature> features = this.getFeatures(servletConfig, splitChar);
        bean.getFeatures().addAll(features);
        bean.setBus(this.getBus());
        bean.setApplication(this.getApplication());
        bean.create();
    }

    protected ApplicationInfo createApplicationInfo(String appClassName, ServletConfig servletConfig)
            throws ServletException {

        Map<String, List<String>> props = new HashMap<>();
        appClassName = this.getClassNameAndProperties(appClassName, props);
        Class<?> appClass = this.loadApplicationClass(appClassName);
        ApplicationInfo appInfo = (ApplicationInfo) this.createSingletonInstance(appClass, props, servletConfig);
        Map<String, Object> servletProps = new HashMap<>();
        ServletContext servletContext = servletConfig.getServletContext();
        Enumeration<String> names = servletContext.getInitParameterNames();

        String name;
        while (names.hasMoreElements()) {
            name = names.nextElement();
            servletProps.put(name, servletContext.getInitParameter(name));
        }

        names = servletConfig.getInitParameterNames();

        while (names.hasMoreElements()) {
            name = names.nextElement();
            servletProps.put(name, servletConfig.getInitParameter(name));
        }

        appInfo.setOverridingProps(servletProps);
        return appInfo;
    }

    protected Class<?> loadApplicationClass(String appClassName) throws ServletException {

        return this.loadClass(appClassName, "Application");
    }

    protected Class<?> loadClass(String cName) throws ServletException {

        return this.loadClass(cName, "Resource");
    }

    protected Class<?> loadClass(String cName, String classType) throws ServletException {

        try {
            Class<?> aClass;
            if (this.classLoader == null) {
                aClass = ClassLoaderUtils.loadClass(cName, org.apache.cxf.jaxrs.servlet.CXFNonSpringJaxrsServlet.class);
            } else {
                aClass = this.classLoader.loadClass(cName);
            }

            return aClass;
        } catch (ClassNotFoundException exception) {
            throw new ServletException("No " + classType + " class " + cName.trim() + " can be found", exception);
        }
    }

    public void setClassLoader(ClassLoader loader) {

        this.classLoader = loader;
    }

    protected Application getApplication() {

        return this.application;
    }

    private static class ApplicationImpl extends Application {

        private final Set<Object> applicationSingletons;

        ApplicationImpl(Set<Object> applicationSingletons) {

            this.applicationSingletons = applicationSingletons;
        }

        @Override
        public Set<Object> getSingletons() {

            return this.applicationSingletons;
        }
    }
}
