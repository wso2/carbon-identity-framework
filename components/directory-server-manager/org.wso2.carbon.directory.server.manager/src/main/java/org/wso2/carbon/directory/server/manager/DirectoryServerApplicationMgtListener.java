/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.directory.server.manager;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.directory.server.manager.common.ServerPrinciple;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementValidationException;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationMgtSystemConfig;
import org.wso2.carbon.identity.application.mgt.dao.ApplicationDAO;
import org.wso2.carbon.identity.application.mgt.listener.AbstractApplicationMgtListener;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class DirectoryServerApplicationMgtListener extends AbstractApplicationMgtListener {

    private static Log log = LogFactory.getLog(DirectoryServerApplicationMgtListener.class);

    @Override
    public int getDefaultOrderId() {

        return 999;
    }

    @Override
    public boolean doPreDeleteApplication(String applicationName, String tenantDomain, String userName)
            throws IdentityApplicationManagementException {

        ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
        ServiceProvider serviceProvider = appDAO.getApplication(applicationName, tenantDomain);
        if (serviceProvider != null &&
                serviceProvider.getInboundAuthenticationConfig() != null &&
                serviceProvider.getInboundAuthenticationConfig().getInboundAuthenticationRequestConfigs() != null) {
            InboundAuthenticationRequestConfig[] configs = serviceProvider.getInboundAuthenticationConfig()
                    .getInboundAuthenticationRequestConfigs();
            for (InboundAuthenticationRequestConfig config : configs) {
                if ("kerberos".equalsIgnoreCase(config.getInboundAuthType()) && config.getInboundAuthKey() != null) {
                    DirectoryServerManager directoryServerManager = new DirectoryServerManager();
                    try {
                        directoryServerManager.removeServer(config.getInboundAuthKey());
                    } catch (DirectoryServerManagerException e) {
                        String error = "Error while removing a kerberos: " + config.getInboundAuthKey();
                        throw new IdentityApplicationManagementException(error, e);
                    }
                    break;
                }
            }
        }
        return true;
    }

    @Override
    public boolean doPreUpdateApplication(ServiceProvider serviceProvider, String tenantDomain, String userName)
            throws IdentityApplicationManagementException {

        List<String> validationMsg = new ArrayList<>();

        InboundAuthenticationConfig inboundAuthenticationConfig = serviceProvider.getInboundAuthenticationConfig();
        if (inboundAuthenticationConfig != null &&
                inboundAuthenticationConfig.getInboundAuthenticationRequestConfigs() != null) {
            for (InboundAuthenticationRequestConfig authConfig
                    : inboundAuthenticationConfig.getInboundAuthenticationRequestConfigs()) {

                if (StringUtils.equals(authConfig.getInboundAuthType(), "kerberos")) {
                    if (authConfig.getInboundConfiguration() != null) {
                        String inboundAuthKey = authConfig.getInboundAuthKey();
                        if (authConfig.getInboundConfiguration() == null) {
                            return true;
                        }
                        ServerPrinciple serverPrinciple;
                        try {
                            serverPrinciple = unmarshalServerPrinciple(authConfig.getInboundConfiguration(),
                                    serviceProvider.getApplicationName(), tenantDomain);
                        } catch (IdentityApplicationManagementException e) {
                            validationMsg.add("Kerberos inbound configuration in the file is not valid.");
                            throw new IdentityApplicationManagementValidationException(
                                    validationMsg.toArray(new String[0]));
                        }
                        if (!inboundAuthKey.equals(serverPrinciple.getServerName())) {
                            validationMsg.add(String.format("The Inbound Auth Key of the  application name %s " +
                                            "is not match with Service Principal Name %s.", authConfig
                                            .getInboundAuthKey(),
                                    serverPrinciple.getServerName()));
                        }
                    }
                    break;
                }
            }
        }
        if (!validationMsg.isEmpty()) {
            throw new IdentityApplicationManagementValidationException(validationMsg.toArray(new String[0]));
        }
        return true;
    }

    @Override
    public void doImportServiceProvider(ServiceProvider serviceProvider) throws IdentityApplicationManagementException {

        InboundAuthenticationConfig inboundAuthenticationConfig = serviceProvider.getInboundAuthenticationConfig();
        if (inboundAuthenticationConfig != null &&
                inboundAuthenticationConfig.getInboundAuthenticationRequestConfigs() != null) {

            for (InboundAuthenticationRequestConfig authConfig
                    : inboundAuthenticationConfig.getInboundAuthenticationRequestConfigs()) {
                if (StringUtils.equals(authConfig.getInboundAuthType(), "kerberos")) {

                    String inboundConfiguration = authConfig.getInboundConfiguration();
                    if (inboundConfiguration == null || "".equals(inboundConfiguration)) {
                        String errorMsg = String.format("No inbound configurations found for kerberos in the imported" +
                                "%s", serviceProvider.getApplicationName());
                        throw new IdentityApplicationManagementException(errorMsg);
                    }
                    ServerPrinciple serverPrinciple = unmarshalServerPrinciple(authConfig.getInboundConfiguration(),
                            serviceProvider.getApplicationName(), serviceProvider.getOwner().getTenantDomain());
                    try {
                        DirectoryServerManager directoryServerManager = new DirectoryServerManager();
                        if (directoryServerManager.isExistingServicePrinciple(serverPrinciple.getServerName())) {
                            directoryServerManager.removeServer(serverPrinciple.getServerName());
                        }
                        directoryServerManager.addServer(serverPrinciple.getServerName(),
                                serverPrinciple.getServerDescription(), serverPrinciple.getServerPassword());
                    } catch (DirectoryServerManagerException e) {
                        throw new IdentityApplicationManagementException(String.format("Error in adding kerberos " +
                                "server for %s", serviceProvider.getApplicationName()), e);
                    }
                    return;
                }
            }
        }
    }

    @Override
    public void doExportServiceProvider(ServiceProvider serviceProvider, Boolean exportSecrets)
            throws IdentityApplicationManagementException {

        InboundAuthenticationConfig inboundAuthenticationConfig = serviceProvider.getInboundAuthenticationConfig();
        if (inboundAuthenticationConfig != null &&
                inboundAuthenticationConfig.getInboundAuthenticationRequestConfigs() != null) {
            for (InboundAuthenticationRequestConfig authConfig
                    : inboundAuthenticationConfig.getInboundAuthenticationRequestConfigs()) {
                if (StringUtils.equals(authConfig.getInboundAuthType(), "kerberos")) {
                    String inboundAuthKey = authConfig.getInboundAuthKey();
                    if (exportSecrets) {
                        try {
                            DirectoryServerManager directoryServerManager = new DirectoryServerManager();
                            ServerPrinciple serverPrinciple =
                                    directoryServerManager.getServicePrinciple(inboundAuthKey);
                            if (serverPrinciple != null) {
                                authConfig.setInboundConfiguration(marshalServerPrinciple(serverPrinciple));
                            } else {
                                throw new IdentityApplicationManagementException(String.format(
                                        "There is no kerberos server with  %s", inboundAuthKey));
                            }
                        } catch (DirectoryServerManagerException e) {
                            throw new IdentityApplicationManagementException(String.format("Error in adding kerberos " +
                                    "server for %s", serviceProvider.getApplicationName()), e);
                        }
                    } else {
                        inboundAuthenticationConfig.setInboundAuthenticationRequestConfigs(
                                (InboundAuthenticationRequestConfig[]) ArrayUtils.removeElement
                                        (inboundAuthenticationConfig.getInboundAuthenticationRequestConfigs(),
                                                authConfig));
                    }
                    return;
                }
            }
        }
    }

    /**
     * Unmarshal server principle.
     *
     * @param inboundConfiguration server principle
     * @param serviceProviderName  service Provider Name
     * @param tenantDomain         tenant Domain
     * @return ServerPrinciple
     * @throws IdentityApplicationManagementException Identity Application Management Exception
     */
    private ServerPrinciple unmarshalServerPrinciple(String inboundConfiguration, String serviceProviderName,
                                                     String tenantDomain) throws
            IdentityApplicationManagementException {

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(ServerPrinciple.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return (ServerPrinciple) unmarshaller.unmarshal(new ByteArrayInputStream(
                    inboundConfiguration.getBytes(StandardCharsets.UTF_8)));
        } catch (JAXBException e) {
            throw new IdentityApplicationManagementException(String.format("Error in unmarshelling Trusted Service Data"
                    + " %s@%s", serviceProviderName, tenantDomain), e);
        }

    }

    /**
     * Marshal server principle.
     *
     * @param serverPrinciple server principle
     * @return marshaled server principle
     * @throws IdentityApplicationManagementException Identity Application Management Exception
     */
    private String marshalServerPrinciple(ServerPrinciple serverPrinciple)
            throws IdentityApplicationManagementException {

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(ServerPrinciple.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            StringWriter sw = new StringWriter();
            jaxbMarshaller.marshal(serverPrinciple, sw);
            return sw.toString();
        } catch (JAXBException e) {
            throw new IdentityApplicationManagementException(String.format("Error in exporting server principle" +
                    "%s", serverPrinciple.getServerName()), e);
        }

    }
}
