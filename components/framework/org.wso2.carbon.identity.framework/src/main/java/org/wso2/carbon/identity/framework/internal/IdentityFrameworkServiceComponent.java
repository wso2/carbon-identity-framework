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
import org.wso2.carbon.identity.framework.IdentityProcessor;
import org.wso2.carbon.identity.framework.servlet.IdentityGateway;
import org.wso2.carbon.identity.framework.request.factory.HttpIdentityRequestFactory;
import org.wso2.carbon.identity.framework.util.IdentityGatewayUtil;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.Collections;
import java.util.Comparator;
import javax.servlet.Servlet;

/**
 * @scr.component name="identity.gateway.framework.component"
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
 * @scr.reference name="identity.processor.2"
 * interface="org.wso2.carbon.identity.framework.IdentityProcessor" cardinality="0..n"
 * policy="dynamic" bind="addIdentityProcessor" unbind="unSetIdentityProcessor"
 */
public class IdentityFrameworkServiceComponent {

    public static final String COMMON_SERVLET_URL = "/commonauth";
    private static final String IDENTITY_GATEWAY_URL = "/identity-gateway";
    private static final Log log = LogFactory.getLog(IdentityFrameworkServiceComponent.class);

    private IdentityFrameworkDataHolder dataHolder = IdentityFrameworkDataHolder.getInstance();
    private HttpService httpService;


    protected void setRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("RealmService is set in the Application Authentication Framework bundle");
        }
        dataHolder.setRealmService(realmService);
    }

    protected void setRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService is set in the Application Authentication Framework bundle");
        }
        dataHolder.setRegistryService(registryService);
    }

    protected void activate(ComponentContext ctxt) {
        BundleContext bundleContext = ctxt.getBundleContext();

        Servlet identityServlet = new ContextPathServletAdaptor(new IdentityGateway(), IDENTITY_GATEWAY_URL);
        try {
            httpService.registerServlet(IDENTITY_GATEWAY_URL, identityServlet, null, null);
            if (log.isDebugEnabled()) {
                log.debug("Identity Gateway Servlet registered under : " + IDENTITY_GATEWAY_URL);
            }
        } catch (Exception e) {
            String errMsg = "Error when registering Identity Gateway Servlet via the HttpService.";
            log.error(errMsg, e);
            throw new RuntimeException(errMsg, e);
        }

        //Default HttpIdentityRequestFactory is registered.
        HttpIdentityRequestFactory httpIdentityRequestFactory = new HttpIdentityRequestFactory();
    //    bundleContext.registerService(HttpIdentityRequestFactory.class, httpIdentityRequestFactory, null);

        // TODO : fix osgi issue
     //   bundleContext.registerService(IdentityProcessor.class, new InitRequestProcessor(), null);
     //   bundleContext.registerService(HttpIdentityResponseFactory.class, new FrameworkLoginResponseFactory(), null);


        if (log.isDebugEnabled()) {
            log.debug("Identity Gateway Framework bundle is activated");
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.debug("Identity Gateway Framework bundle is deactivated");
        }
    }

    protected void setHttpService(HttpService httpService) {
        if (log.isDebugEnabled()) {
            log.debug("HTTP Service is set in the Identity Gateway Framework bundle");
        }

        this.httpService = httpService;
    }

    protected void unSetHttpService(HttpService httpService) {
        if (log.isDebugEnabled()) {
            log.debug("HTTP Service is unSet in the Identity Gateway Framework bundle");
        }

        this.httpService = null;
    }

    protected void unSetRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("RealmService is unSet in the Identity Gateway Framework bundle");
        }
        dataHolder.setRealmService(null);
    }

    protected void unSetRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService is unSet in the Identity Gateway Framework bundle");
        }
        dataHolder.setRegistryService(null);
    }


    protected void addIdentityProcessor(IdentityProcessor requestProcessor) {

        dataHolder.getIdentityProcessors().add(requestProcessor);
        Collections.sort(dataHolder.getIdentityProcessors(), identityProcessorComparator);
        if (log.isDebugEnabled()) {
            log.debug("Added IdentityProcessor : " + requestProcessor.getName());
        }
    }

    protected void unSetIdentityProcessor(IdentityProcessor requestProcessor) {

        dataHolder.getIdentityProcessors().remove(requestProcessor);
        if (log.isDebugEnabled()) {
            log.debug("Removed IdentityProcessor : " + requestProcessor.getName());
        }
    }

//    protected void addHttpIdentityRequestFactory(HttpIdentityRequestFactory factory) {
//
//        dataHolder.getHttpIdentityRequestFactories().add(factory);
//        Collections.sort(dataHolder.getHttpIdentityRequestFactories(), httpIdentityRequestFactoryComparator);
//        if (log.isDebugEnabled()) {
//            log.debug("Added HttpIdentityRequestFactory : " + factory.getName());
//        }
//    }
//
//    protected void unSetHttpIdentityRequestFactory(HttpIdentityRequestFactory factory) {
//
//        dataHolder.getHttpIdentityRequestFactories().remove(factory);
//        if (log.isDebugEnabled()) {
//            log.debug("Removed HttpIdentityRequestFactory : " + factory.getName());
//        }
//    }
//
//    protected void addHttpIdentityResponseFactory(HttpIdentityResponseFactory factory) {
//
//        dataHolder.getHttpIdentityResponseFactories().add(factory);
//        Collections.sort(dataHolder.getHttpIdentityResponseFactories(), httpIdentityResponseFactoryComparator);
//        if (log.isDebugEnabled()) {
//            log.debug("Added HttpIdentityResponseFactory : " + factory.getName());
//        }
//    }
//
//    protected void unSetHttpIdentityResponseFactory(HttpIdentityResponseFactory factory) {
//
//        dataHolder.getHttpIdentityResponseFactories().remove(factory);
//        if (log.isDebugEnabled()) {
//            log.debug("Removed HttpIdentityResponseFactory : " + factory.getName());
//        }
//    }

    private static Comparator<IdentityProcessor> identityProcessorComparator = new Comparator<IdentityProcessor>() {
        @Override
        public int compare(IdentityProcessor identityProcessor1, IdentityProcessor identityProcessor2) {
            return IdentityGatewayUtil.comparePriority(identityProcessor1.getPriority(), identityProcessor2.getPriority());
        }
    };

//    private static Comparator<HttpIdentityRequestFactory> httpIdentityRequestFactoryComparator =
//            new Comparator<HttpIdentityRequestFactory>() {
//                @Override
//                public int compare(HttpIdentityRequestFactory factory1, HttpIdentityRequestFactory factory2) {
//                    return IdentityGatewayUtil.comparePriority(factory1.getPriority(), factory2.getPriority());
//                }
//            };
//
//    private static Comparator<HttpIdentityResponseFactory> httpIdentityResponseFactoryComparator =
//            new Comparator<HttpIdentityResponseFactory>() {
//                @Override
//                public int compare(HttpIdentityResponseFactory factory1, HttpIdentityResponseFactory factory2) {
//                    return IdentityGatewayUtil.comparePriority(factory1.getPriority(), factory2.getPriority());
//                }
//            };


}
