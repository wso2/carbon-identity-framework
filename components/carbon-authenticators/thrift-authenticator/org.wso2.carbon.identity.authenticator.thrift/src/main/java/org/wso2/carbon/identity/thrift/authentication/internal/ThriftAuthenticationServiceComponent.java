/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.thrift.authentication.internal;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.protocol.TCompactProtocol;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.identity.thrift.authentication.AuthenticatorServlet;
import org.wso2.carbon.identity.thrift.authentication.TCPThriftAuthenticationService;
import org.wso2.carbon.identity.thrift.authentication.ThriftAuthenticatorService;
import org.wso2.carbon.identity.thrift.authentication.dao.DBThriftSessionDAO;
import org.wso2.carbon.identity.thrift.authentication.dao.ThriftSessionDAO;
import org.wso2.carbon.identity.thrift.authentication.internal.generatedCode.AuthenticatorService;
import org.wso2.carbon.identity.thrift.authentication.internal.util.HostAddressFinder;
import org.wso2.carbon.identity.thrift.authentication.internal.util.ThriftAuthenticationConfigParser;
import org.wso2.carbon.identity.thrift.authentication.internal.util.ThriftAuthenticationConstants;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;

import javax.servlet.ServletException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;

/**
 * @scr.component name="org.wso2.carbon.identity.thrift.authentication.internal.ThriftAuthenticationServiceComponent" immediate="true"
 * @scr.reference name="http.service" interface="org.osgi.service.http.HttpService"
 * cardinality="1..1" policy="dynamic" bind="setHttpService" unbind="unsetHttpService"
 * @scr.reference name="org.wso2.carbon.user.core"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService" unbind="unsetRealmService"
 * @scr.reference name="configuration.context"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic"  bind="setConfigurationContext" unbind="unsetConfigurationContext"
 */

public class ThriftAuthenticationServiceComponent {

    private static Log log = LogFactory.getLog(ThriftAuthenticationServiceComponent.class);


    private static HttpService httpServiceInstance;
    private static RealmService realmServiceInstance;
    private ServiceRegistration thriftAuthenticationService;
    private ConfigurationContextService configurationContext;
    private TCPThriftAuthenticationService TCPThriftAuthenticationService;

    public static int readPortOffset() {
        return CarbonUtils.
                getPortFromServerConfig(ThriftAuthenticationConstants.CARBON_CONFIG_PORT_OFFSET_NODE) + 1;
    }

    protected void activate(ComponentContext compCtx) {
        try {
            //configure ThriftSessionDAO
            ThriftSessionDAO thriftSessionDAO;
            try {
                OMElement thriftSessionDAOElement = ThriftAuthenticationConfigParser.getInstance()
                        .getConfigElement("ThriftSessionDAO");
                thriftSessionDAO = ((ThriftSessionDAO) Class.forName(thriftSessionDAOElement.getText()).newInstance()).getInstance();
            } catch (Throwable throwable) {
                log.error("Error in loading ThriftSessionDAO hence using default org.wso2.carbon.identity.thrift.authentication.dao.DBThriftSessionDAO, ", throwable);
                thriftSessionDAO = new DBThriftSessionDAO();
            }
            //configure thriftSessionTimeout in ms
            long thriftSessionTimeout;
            try {
                OMElement thriftSessionTimeoutElement = ThriftAuthenticationConfigParser.getInstance()

                        .getConfigElement("ThriftSessionTimeout");

                thriftSessionTimeout = Long.parseLong(thriftSessionTimeoutElement.getText());

            } catch (Throwable throwable) {

                log.error("Error in loading ThriftSessionTimeout hence using the default: 30min, ", throwable);

                thriftSessionTimeout = 60000L * 30;

            }
            //get an instance of this to register as an osgi service

            ThriftAuthenticatorServiceImpl thriftAuthenticatorServiceImpl =

                    new ThriftAuthenticatorServiceImpl(getRealmServiceInstance(), thriftSessionDAO, thriftSessionTimeout);
            //register as an osgi service

            thriftAuthenticationService = compCtx.getBundleContext().registerService(

                    ThriftAuthenticatorService.class.getName(), thriftAuthenticatorServiceImpl, null);
            //register AuthenticatorServiceImpl as a thrift service.

            startThriftServices(thriftAuthenticatorServiceImpl);
        } catch (RuntimeException e) {
            log.error("Error in starting Thrift Authentication Service ", e);
        } catch (Throwable e) {
            log.error("Error in starting Thrift Authentication Service ", e);
        }
        //populate thrift sessions from db, if there is any in the db
    }

    protected void deactivate(ComponentContext compCtx) {

        if (TCPThriftAuthenticationService != null) {
            TCPThriftAuthenticationService.stop();
        }
        compCtx.getBundleContext().ungetService(thriftAuthenticationService.getReference());

    }
    public static HttpService getHttpServiceInstance() {
        return httpServiceInstance;
    }

    public static void setHttpServiceInstance(HttpService httpServiceInstance) {
        ThriftAuthenticationServiceComponent.httpServiceInstance = httpServiceInstance;
    }

    public static RealmService getRealmServiceInstance() {
        return realmServiceInstance;
    }

    public static void setRealmServiceInstance(RealmService realmServiceInstance) {
        ThriftAuthenticationServiceComponent.realmServiceInstance = realmServiceInstance;
    }
    protected void setHttpService(HttpService httpService) {
        setHttpServiceInstance(httpService);
    }

    protected void unsetHttpService(HttpService httpService) {

        setHttpServiceInstance(null);
    }

    protected void setRealmService(RealmService realmService) {
        setRealmServiceInstance(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        setRealmServiceInstance(null);
    }

    protected void setConfigurationContext(ConfigurationContextService configurationContext) {
        this.configurationContext = configurationContext;
    }

    protected void unsetConfigurationContext(ConfigurationContextService configurationContext) {
        this.configurationContext = null;
    }

    private void startThriftServices(ThriftAuthenticatorService thriftAuthenticatorService) throws Exception {
        startThriftHttpAuthenticatorService(thriftAuthenticatorService);
        startThriftTcpAuthenticatorService(thriftAuthenticatorService);
    }

    private void startThriftHttpAuthenticatorService(ThriftAuthenticatorService thriftAuthenticatorService) {
        // Authenticator service should be exposed over SSL. Since Thrift 0.5 doesn't have support
        // for SSL transport this is commented out for now until later Thrift version is used. Using
        // servlet based authenticator service for authentication for now.
        try {
            AuthenticatorService.Processor authServiceProcessor = new AuthenticatorService.Processor(
                    new AuthenticatorServiceImpl(thriftAuthenticatorService));

            TCompactProtocol.Factory inProtFactory = new TCompactProtocol.Factory();
            TCompactProtocol.Factory outProtFactory = new TCompactProtocol.Factory();

            getHttpServiceInstance().registerServlet("/thriftAuthenticator",
                    new AuthenticatorServlet(authServiceProcessor,
                            inProtFactory,
                            outProtFactory),
                    new Hashtable(),
                    getHttpServiceInstance().createDefaultHttpContext());
        } catch (ServletException e) {
            log.error("Unable to start Thrift Authenticator Service." + e);
        } catch (NamespaceException e) {
            log.error("Unable to start Thrift Authenticator Service" + e);
        }
    }

    private void startThriftTcpAuthenticatorService(ThriftAuthenticatorService thriftAuthenticatorService) throws Exception {

        int portOffset = readPortOffset();

        ServerConfiguration serverConfig = ServerConfiguration.getInstance();

        String serverUrl = CarbonUtils.getServerURL(serverConfig, configurationContext.getServerConfigContext());


        OMElement hostnameElement = ThriftAuthenticationConfigParser.getInstance().getConfigElement("Hostname");
        String hostName;
        if (hostnameElement == null) {
            try {
                hostName = new URL(serverUrl).getHost();
            } catch (MalformedURLException e) {
                hostName = HostAddressFinder.findAddress("localhost");
                if (!serverUrl.matches("local:/.*/services/")) {
                    log.info("Thrift Authentication Service url :" + serverUrl + " is using local, hence hostname is assigned as '" + hostName + "'");
                }
            }
        } else {
            hostName = hostnameElement.getText();
        }

        OMElement portElement = ThriftAuthenticationConfigParser.getInstance().getConfigElement("Port");
        int port;
        if (portElement != null) {
            port = Integer.parseInt(portElement.getText());
        } else {
            throw new Exception("Error, Thrift Authentication Service config does not have a port defined!");
        }
        port = port + portOffset;

        String keyStore = serverConfig.getFirstProperty("Security.KeyStore.Location");
        if (keyStore == null) {
            keyStore = System.getProperty("Security.KeyStore.Location");
            if (keyStore == null) {
                throw new Exception("Cannot initialize Thrift Authentication Service, Security.KeyStore.Location is null");
            }
        }
        String keyStorePassword = serverConfig.getFirstProperty("Security.KeyStore.Password");
        if (keyStorePassword == null) {
            keyStorePassword = System.getProperty("Security.KeyStore.Password");
            if (keyStorePassword == null) {
                throw new Exception("Cannot initialize Thrift Authentication Service, Security.KeyStore.Password is null ");
            }
        }

        OMElement clientTimeoutElement = ThriftAuthenticationConfigParser.getInstance().getConfigElement(ThriftAuthenticationConstants.CLIENT_TIMEOUT);
        int clientTimeout;
        if (clientTimeoutElement != null) {
            try {
                clientTimeout = Integer.parseInt(clientTimeoutElement.getText());
            } catch (Throwable e) {
                String msg = "Error, in Thrift Auth Client Timeout, hence using the default timeout: " + ThriftAuthenticationConstants.DEFAULT_CLIENT_TIMEOUT + "ms";
                log.error(msg + e);
                clientTimeout = ThriftAuthenticationConstants.DEFAULT_CLIENT_TIMEOUT;
            }
        } else {
            String msg = "Thrift Authentication Service Client Timeout is not set, hence using the default timeout: " + ThriftAuthenticationConstants.DEFAULT_CLIENT_TIMEOUT + "ms";
            log.info(msg);
            clientTimeout = ThriftAuthenticationConstants.DEFAULT_CLIENT_TIMEOUT;
        }
        TCPThriftAuthenticationService = new TCPThriftAuthenticationService(hostName, port, keyStore, keyStorePassword, clientTimeout, thriftAuthenticatorService);
        TCPThriftAuthenticationService.start();

    }

}
