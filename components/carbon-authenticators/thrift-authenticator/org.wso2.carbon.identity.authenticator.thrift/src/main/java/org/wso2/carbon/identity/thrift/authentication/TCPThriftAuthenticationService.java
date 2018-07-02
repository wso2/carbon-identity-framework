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
package org.wso2.carbon.identity.thrift.authentication;

import org.apache.axiom.om.OMElement;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.wso2.carbon.identity.thrift.authentication.dao.InMemoryThriftSessionDAO;
import org.wso2.carbon.identity.thrift.authentication.exception.ThriftAuthenticationException;
import org.wso2.carbon.identity.thrift.authentication.internal.AuthenticatorServiceImpl;
import org.wso2.carbon.identity.thrift.authentication.internal.ThriftAuthenticatorServiceImpl;
import org.wso2.carbon.identity.thrift.authentication.internal.generatedCode.AuthenticatorService;
import org.wso2.carbon.identity.thrift.authentication.internal.util.ThriftAuthenticationConfigParser;
import org.wso2.carbon.identity.thrift.authentication.internal.util.ThriftAuthenticationConstants;
import org.wso2.carbon.utils.ThriftSession;

import javax.net.ssl.SSLServerSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * The tcp Thrift Authentication service
 */
public class TCPThriftAuthenticationService {
    private final String hostName;
    private final int port;
    private final String keyStore;
    private final String keyStorePassword;
    private final int clientTimeout;
    private ThriftAuthenticatorService thriftAuthenticatorService;
    private Log log = LogFactory.getLog(TCPThriftAuthenticationService.class);
    private TServer authenticationServer;

    public TCPThriftAuthenticationService(String hostName, int port, String keyStore, String keyStorePassword, int clientTimeout, ThriftAuthenticatorService thriftAuthenticatorService) {
        this.hostName = hostName;
        this.port = port;
        this.keyStore = keyStore;
        this.keyStorePassword = keyStorePassword;
        this.clientTimeout = clientTimeout;
        this.thriftAuthenticatorService = thriftAuthenticatorService;
    }

    public TCPThriftAuthenticationService(String hostName, int port, long thriftSessionTimeOut) throws ThriftAuthenticationException {
        this.hostName = hostName;
        this.port = port;
        String SecurityKeyStore = System.getProperty(ThriftAuthenticationConstants.SECURITY_KEY_STORE_LOCATION);
        if (SecurityKeyStore == null) {
            throw new ThriftAuthenticationException("Cannot start agent server, not valid Security.KeyStore.Location is null");
        }

        String SecurityKeyStorePassword
                = System.getProperty(ThriftAuthenticationConstants.SECURITY_KEY_STORE_PASSWORD);
        if (SecurityKeyStorePassword == null) {
            throw new ThriftAuthenticationException("Cannot start agent server, not valid Security.KeyStore.Password is null ");
        }

        this.keyStore = SecurityKeyStore;
        this.keyStorePassword = SecurityKeyStorePassword;
        this.clientTimeout = 30000;

        this.thriftAuthenticatorService = new ThriftAuthenticatorServiceImpl(null, new InMemoryThriftSessionDAO(), thriftSessionTimeOut);
    }

    public void start() throws TTransportException, UnknownHostException {
        InetAddress inetAddress = InetAddress.getByName(hostName);

        TSSLTransportFactory.TSSLTransportParameters params =
                new TSSLTransportFactory.TSSLTransportParameters();
        params.setKeyStore(keyStore, keyStorePassword);

        TServerSocket serverTransport;

        serverTransport = TSSLTransportFactory.getServerSocket(port, clientTimeout, inetAddress, params);
        SSLServerSocket sslServerSocket = (javax.net.ssl.SSLServerSocket) serverTransport.getServerSocket();

        OMElement sslEnabledProtocolsElement = ThriftAuthenticationConfigParser.getInstance()
                .getConfigElement(ThriftAuthenticationConstants.CONFIG_SSL_ENABLED_PROTOCOLS);
        if (sslEnabledProtocolsElement != null) {
            String sslEnabledProtocols = sslEnabledProtocolsElement.getText();
            if (StringUtils.isNotBlank(sslEnabledProtocols)) {
                String[] sslProtocolsArray = sslEnabledProtocols.split(",");
                sslServerSocket.setEnabledProtocols(sslProtocolsArray);
            }
        }

        OMElement ciphersElement = ThriftAuthenticationConfigParser.getInstance()
                .getConfigElement(ThriftAuthenticationConstants.CONFIG_CIPHERS);
        if (ciphersElement != null) {
            String ciphers = ciphersElement.getText();
            if (StringUtils.isNotBlank(ciphers)) {
                String[] ciphersArray = ciphers.split(",");
                sslServerSocket.setEnabledCipherSuites(ciphersArray);
            }
        }

        AuthenticatorService.Processor<AuthenticatorServiceImpl> processor =
                new AuthenticatorService.Processor<AuthenticatorServiceImpl>(
                        new AuthenticatorServiceImpl(thriftAuthenticatorService));
        authenticationServer = new TThreadPoolServer(
                new TThreadPoolServer.Args(serverTransport).processor(processor));
        Thread thread = new Thread(new ServerRunnable(authenticationServer));
        if (log.isDebugEnabled()) {
            log.debug("Thrift Authentication Service started at ssl://" + hostName + ":" + port);
        }
        thread.start();
    }

    public void stop() {
        authenticationServer.stop();
    }

    public boolean isAuthenticated(String sessionId) {
        return thriftAuthenticatorService.isAuthenticated(sessionId);
    }

    public ThriftSession getSessionInfo(String sessionId) {
        return thriftAuthenticatorService.getSessionInfo(sessionId);
    }

    /**
     * Thread that starts thrift server
     */
    private static class ServerRunnable implements Runnable {
        TServer server;

        public ServerRunnable(TServer server) {
            this.server = server;
        }

        @Override
        public void run() {
            server.serve();
        }
    }

}
