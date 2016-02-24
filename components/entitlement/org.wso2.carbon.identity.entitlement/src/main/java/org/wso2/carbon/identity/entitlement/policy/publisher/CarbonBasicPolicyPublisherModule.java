/*
*  Copyright (c)  WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.identity.entitlement.policy.publisher;


import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.dto.PublisherDataHolder;
import org.wso2.carbon.identity.entitlement.dto.PublisherPropertyDTO;

import javax.xml.stream.XMLStreamException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Carbon implementation of PolicyPublisherModule
 */
public class CarbonBasicPolicyPublisherModule extends AbstractPolicyPublisherModule {

    private static final String MODULE_NAME = "Carbon Basic Auth Policy Publisher Module";
    private static Log log = LogFactory.getLog(CarbonBasicPolicyPublisherModule.class);
    private ConfigurationContext configCtx;
    private String serverUrl;

    private String serverUserName;

    private String serverPassword;

    @Override
    public void init(PublisherDataHolder propertyHolder) throws EntitlementException {

        PublisherPropertyDTO[] propertyDTOs = propertyHolder.getPropertyDTOs();
        for (PublisherPropertyDTO dto : propertyDTOs) {
            if ("subscriberURL".equals(dto.getId())) {
                serverUrl = dto.getValue();
            } else if ("subscriberUserName".equals(dto.getId())) {
                serverUserName = dto.getValue();
            } else if ("subscriberPassword".equals(dto.getId())) {
                serverPassword = dto.getValue();
            }
        }

        try {
            configCtx = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        } catch (AxisFault axisFault) {
            log.error("Error while initializing module", axisFault);
            throw new EntitlementException("Error while initializing module", axisFault);
        }
    }

    public String getModuleName() {
        return MODULE_NAME;
    }

    @Override
    public Properties loadProperties() {

        Properties properties = new Properties();

        Map<String, String> dataMap1 = new HashMap<String, String>();
        dataMap1.put(AbstractPolicyPublisherModule.REQUIRED, "true");
        dataMap1.put(AbstractPolicyPublisherModule.DISPLAY_NAME, "Subscriber URL");
        dataMap1.put(AbstractPolicyPublisherModule.ORDER, "1");

        Map<String, String> dataMap2 = new HashMap<String, String>();
        dataMap2.put(AbstractPolicyPublisherModule.REQUIRED, "true");
        dataMap2.put(AbstractPolicyPublisherModule.DISPLAY_NAME, "Subscriber User Name");
        dataMap2.put(AbstractPolicyPublisherModule.ORDER, "2");

        Map<String, String> dataMap3 = new HashMap<String, String>();
        dataMap3.put(AbstractPolicyPublisherModule.REQUIRED, "true");
        dataMap3.put(AbstractPolicyPublisherModule.DISPLAY_NAME, "Subscriber Password");
        dataMap3.put(AbstractPolicyPublisherModule.ORDER, "3");
        dataMap3.put(AbstractPolicyPublisherModule.SECRET, "true");

        properties.put("subscriberURL", dataMap1);
        properties.put("subscriberUserName", dataMap2);
        properties.put("subscriberPassword", dataMap3);

        return properties;
    }

    public void publishNew(PolicyDTO policyDTO) throws EntitlementException {

        String body = "<xsd:addPolicy xmlns:xsd=\"http://org.apache.axis2/xsd\" xmlns:xsd1=\"http://dto.entitlement.identity.carbon.wso2.org/xsd\">" +
                "  <xsd:policyDTO>" +
                "  <xsd1:active>" + Boolean.toString(policyDTO.isActive()) + "</xsd1:active>" +
                "  <xsd1:policy><![CDATA[" + policyDTO.getPolicy() + "]]>  </xsd1:policy>" +
                "  <xsd1:policyId>" + policyDTO.getPolicyId() + "</xsd1:policyId>" +
                "  <xsd1:policyOrder>" + policyDTO.getPolicyOrder() + "</xsd1:policyOrder>" +
                "  <xsd1:promote>true</xsd1:promote>" +
                "  </xsd:policyDTO>" +
                "  </xsd:addPolicy>";
        doSend(body);
    }

    @Override
    public void order(PolicyDTO policyDTO) throws EntitlementException {

        String body = "<xsd:orderPolicy xmlns:xsd=\"http://org.apache.axis2/xsd\">" +
                "<xsd:policyId>" + policyDTO.getPolicyId() + "</xsd:policyId>" +
                "<xsd:newOrder>" + policyDTO.getPolicyOrder() + "</xsd:newOrder>" +
                "</xsd:orderPolicy>";
        doSend(body);
    }

    @Override
    public void disable(PolicyDTO policyDTO) throws EntitlementException {

        String body = "<xsd:enableDisablePolicy  xmlns:xsd=\"http://org.apache.axis2/xsd\">" +
                "<xsd:policyId>" + policyDTO.getPolicyId() + "</xsd:policyId>" +
                "<xsd:enable>false</xsd:enable>" +
                "</xsd:enableDisablePolicy>";
        doSend(body);
    }

    @Override
    public void enable(PolicyDTO policyDTO) throws EntitlementException {

        String body = "<xsd:enableDisablePolicy  xmlns:xsd=\"http://org.apache.axis2/xsd\">" +
                "<xsd:policyId>" + policyDTO.getPolicyId() + "</xsd:policyId>" +
                "<xsd:enable>true</xsd:enable>" +
                "</xsd:enableDisablePolicy>";
        doSend(body);
    }

    @Override
    public void update(PolicyDTO policyDTO) throws EntitlementException {

        String body = "<xsd:updatePolicy xmlns:xsd=\"http://org.apache.axis2/xsd\" xmlns:xsd1=\"http://dto.entitlement.identity.carbon.wso2.org/xsd\">" +
                "  <xsd:policyDTO>" +
                "  <xsd1:policy><![CDATA[" + policyDTO.getPolicy() + "]]>  </xsd1:policy>" +
                "  <xsd:policyId>" + policyDTO.getPolicyId() + "</xsd:policyId>" +
                "  <xsd1:promote>true</xsd1:promote>" +
                "  </xsd:policyDTO>" +
                "  </xsd:updatePolicy>";
        doSend(body);
    }

    @Override
    public void delete(PolicyDTO policyDTO) throws EntitlementException {

        String body = "<xsd:dePromotePolicy xmlns:xsd=\"http://org.apache.axis2/xsd\">" +
                "<xsd:policyId>" + policyDTO.getPolicyId() + "</xsd:policyId>" +
                "</xsd:dePromotePolicy>";
        doSend(body);
    }


    private void doSend(String body) throws EntitlementException {

        if (serverUrl != null) {
            serverUrl = serverUrl.trim();
            if (!serverUrl.endsWith("/")) {
                serverUrl += "/";
            }
        }

        String serverEndPoint = serverUrl + "EntitlementPolicyAdminService";
        ServiceClient client = null;
        try {
            MultiThreadedHttpConnectionManager httpConnectionManager = new MultiThreadedHttpConnectionManager();
            HttpClient httpClient = new HttpClient(httpConnectionManager);
            client = new ServiceClient(configCtx, null);
            Options option = client.getOptions();
            option.setManageSession(true);
            HttpTransportProperties.Authenticator authenticator =
                    new HttpTransportProperties.Authenticator();
            authenticator.setUsername(serverUserName);
            authenticator.setPassword(serverPassword);
            authenticator.setPreemptiveAuthentication(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE, authenticator);
            option.setProperty(Constants.Configuration.TRANSPORT_URL, serverEndPoint);
            option.setProperty(HTTPConstants.REUSE_HTTP_CLIENT, Constants.VALUE_TRUE);
            option.setProperty(HTTPConstants.CACHED_HTTP_CLIENT, httpClient);
            client.sendRobust(AXIOMUtil.stringToOM(body));
        } catch (AxisFault axisFault) {
            log.error("Policy publish fails due : " + axisFault.getMessage(), axisFault);
            throw new EntitlementException("Policy publish fails due : " + axisFault.getMessage());
        } catch (XMLStreamException e) {
            log.error("Policy publish fails due : " + e.getMessage(), e);
            throw new EntitlementException("Policy publish fails due : " + e.getMessage());
        } finally {
            if (client != null) {
                try {
                    client.cleanupTransport();
                    client.cleanup();
                } catch (AxisFault axisFault) {
                    log.error("Error while cleaning HTTP client", axisFault);
                }
            }
        }
    }
}
