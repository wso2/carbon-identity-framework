/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.framework.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.equinox.http.helper.ContextPathServletAdaptor;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpService;
import org.wso2.carbon.identity.framework.HttpIdentityRequestFactory;
import org.wso2.carbon.identity.framework.HttpIdentityResponseFactory;
import org.wso2.carbon.identity.framework.IdentityProcessor;
import org.wso2.carbon.identity.framework.IdentityServlet;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

import javax.servlet.Servlet;
import java.util.Collections;
import java.util.Comparator;

/**
 * @scr.component name="identity.framework.component"
 * immediate="true"
 * @scr.reference name="osgi.httpservice"
 * interface="org.osgi.service.http.HttpService"
 * cardinality="1..1" policy="dynamic" bind="setHttpService"
 * unbind="unSetHttpService"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService"
 * unbind="unSetRealmService"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService"
 * unbind="unSetRegistryService"
 * @scr.reference name="identity.processor"
 * interface="org.wso2.carbon.identity.framework.IdentityProcessor"
 * cardinality="0..n" policy="dynamic" bind="addIdentityProcessor"
 * unbind="unSetIdentityProcessor"
 * @scr.reference name="identity.request.factory"
 * interface="org.wso2.carbon.identity.framework.HttpIdentityRequestFactory"
 * cardinality="0..n" policy="dynamic" bind="addHttpIdentityRequestFactory"
 * unbind="unSetHttpIdentityRequestFactory"
 * @scr.reference name="identity.response.factory"
 * interface="org.wso2.carbon.identity.framework.HttpIdentityResponseFactory"
 * cardinality="0..n" policy="dynamic" bind="addHttpIdentityResponseFactory"
 * unbind="unSetHttpIdentityResponseFactory"
 */


public class FrameworkServiceComponent {

    public static final String COMMON_SERVLET_URL = "/commonauth";
    private static final String IDENTITY_SERVLET_URL = "/identitynew";
    private static final Log log = LogFactory.getLog(FrameworkServiceComponent.class);




    private HttpService httpService;

    public static RealmService getRealmService() {
        return FrameworkServiceDataHolder.getInstance().getRealmService();
    }

    protected void setRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("RealmService is set in the Application Authentication Framework bundle");
        }
        FrameworkServiceDataHolder.getInstance().setRealmService(realmService);
    }

    public static RegistryService getRegistryService() {
        return FrameworkServiceDataHolder.getInstance().getRegistryService();
    }

    protected void setRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService is set in the Application Authentication Framework bundle");
        }
        FrameworkServiceDataHolder.getInstance().setRegistryService(registryService);
    }

    public static BundleContext getBundleContext() {
        BundleContext bundleContext = FrameworkServiceDataHolder.getInstance().getBundleContext();
        if (bundleContext == null) {
            String msg = "System has not been started properly. Bundle Context is null.";
            log.error(msg);
        }

        return bundleContext;
    }

    @SuppressWarnings("unchecked")
    protected void activate(ComponentContext ctxt) {
        BundleContext bundleContext = ctxt.getBundleContext();

        Servlet identityServlet = new ContextPathServletAdaptor(new IdentityServlet(),
                                                                IDENTITY_SERVLET_URL);
        try {
            httpService.registerServlet(IDENTITY_SERVLET_URL, identityServlet, null, null);
        } catch (Exception e) {
            String errMsg = "Error when registering servlets via the HttpService.";
            log.error(errMsg, e);
            throw new RuntimeException(errMsg, e);
        }

        //Default HttpIdentityRequestFactory is registered.
        HttpIdentityRequestFactory httpIdentityRequestFactory = new HttpIdentityRequestFactory();
        bundleContext.registerService(HttpIdentityRequestFactory.class, httpIdentityRequestFactory, null);


        FrameworkServiceDataHolder.getInstance().setBundleContext(bundleContext);


        if (log.isDebugEnabled()) {
            log.debug("Application Authentication Framework bundle is activated");
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.debug("Application Authentication Framework bundle is deactivated");
        }

        FrameworkServiceDataHolder.getInstance().setBundleContext(null);
    }

    protected void setHttpService(HttpService httpService) {
        if (log.isDebugEnabled()) {
            log.debug("HTTP Service is set in the Application Authentication Framework bundle");
        }

        this.httpService = httpService;
    }

    protected void unSetHttpService(HttpService httpService) {
        if (log.isDebugEnabled()) {
            log.debug("HTTP Service is unSet in the Application Authentication Framework bundle");
        }

        this.httpService = null;
    }

    protected void unSetRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("RealmService is unSet in the Application Authentication Framework bundle");
        }
        FrameworkServiceDataHolder.getInstance().setRealmService(null);
    }

    protected void unSetRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService is unSet in the Application Authentication Framework bundle");
        }
        FrameworkServiceDataHolder.getInstance().setRegistryService(null);
    }



    protected void addIdentityProcessor(IdentityProcessor requestProcessor) {

        FrameworkServiceDataHolder.getInstance().getIdentityProcessors().add(requestProcessor);
        Collections.sort(FrameworkServiceDataHolder.getInstance().getIdentityProcessors(),
                         identityProcessor);

        if (log.isDebugEnabled()) {
            log.debug("Added IdentityProcessor : " + requestProcessor.getName());
        }
    }

    protected void unSetIdentityProcessor(IdentityProcessor requestProcessor) {

        FrameworkServiceDataHolder.getInstance().getIdentityProcessors().remove(requestProcessor);

        if (log.isDebugEnabled()) {
            log.debug("Removed IdentityProcessor : " + requestProcessor.getName());
        }
    }

    protected void addHttpIdentityRequestFactory(HttpIdentityRequestFactory factory) {

        FrameworkServiceDataHolder.getInstance().getHttpIdentityRequestFactories().add(factory);
        Collections.sort(FrameworkServiceDataHolder.getInstance().getHttpIdentityRequestFactories(),
                         httpIdentityRequestFactory);
        if (log.isDebugEnabled()) {
            log.debug("Added HttpIdentityRequestFactory : " + factory.getName());
        }
    }

    protected void unSetHttpIdentityRequestFactory(HttpIdentityRequestFactory factory) {

        FrameworkServiceDataHolder.getInstance().getHttpIdentityRequestFactories().remove(factory);
        if (log.isDebugEnabled()) {
            log.debug("Removed HttpIdentityRequestFactory : " + factory.getName());
        }
    }

    protected void addHttpIdentityResponseFactory(HttpIdentityResponseFactory factory) {

        FrameworkServiceDataHolder.getInstance().getHttpIdentityResponseFactories().add(factory);
        Collections.sort(FrameworkServiceDataHolder.getInstance().getHttpIdentityResponseFactories(),
                         httpIdentityResponseFactory);
        if (log.isDebugEnabled()) {
            log.debug("Added HttpIdentityResponseFactory : " + factory.getName());
        }
    }

    protected void unSetHttpIdentityResponseFactory(HttpIdentityResponseFactory factory) {

        FrameworkServiceDataHolder.getInstance().getHttpIdentityResponseFactories().remove(factory);
        if (log.isDebugEnabled()) {
            log.debug("Removed HttpIdentityResponseFactory : " + factory.getName());
        }
    }


    private static Comparator<IdentityProcessor> identityProcessor =
            new Comparator<IdentityProcessor>() {

                @Override
                public int compare(IdentityProcessor identityProcessor1,
                                   IdentityProcessor identityProcessor2) {

                    if (identityProcessor1.getPriority() > identityProcessor2.getPriority()) {
                        return 1;
                    } else if (identityProcessor1.getPriority() < identityProcessor2.getPriority()) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            };
    private static Comparator<HttpIdentityRequestFactory> httpIdentityRequestFactory =
            new Comparator<HttpIdentityRequestFactory>() {

                @Override
                public int compare(HttpIdentityRequestFactory factory1,
                                   HttpIdentityRequestFactory factory2) {

                    if (factory1.getPriority() > factory2.getPriority()) {
                        return 1;
                    } else if (factory1.getPriority() < factory2.getPriority()) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            };
    private static Comparator<HttpIdentityResponseFactory> httpIdentityResponseFactory =
            new Comparator<HttpIdentityResponseFactory>() {

                @Override
                public int compare(HttpIdentityResponseFactory factory1,
                                   HttpIdentityResponseFactory factory2) {

                    if (factory1.getPriority() > factory2.getPriority()) {
                        return 1;
                    } else if (factory1.getPriority() < factory2.getPriority()) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            };
}
