/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;

import java.io.InputStream;
import java.util.UUID;
import javax.xml.stream.XMLStreamException;

public class AbstractFrameworkTest {

    protected ServiceProvider getTestServiceProvider(String spFileName) throws XMLStreamException {
        InputStream inputStream = this.getClass().getResourceAsStream(spFileName);
        OMElement documentElement = new StAXOMBuilder(inputStream).getDocumentElement();
        return ServiceProvider.build(documentElement);
    }

    protected AuthenticationContext getAuthenticationContext(ServiceProvider serviceProvider) {
        AuthenticationContext authenticationContext = new AuthenticationContext();
        authenticationContext.setServiceProviderName(serviceProvider.getApplicationName());
        authenticationContext.setTenantDomain("test_domain");
        authenticationContext.setCurrentStep(1);
        authenticationContext.setContextIdentifier(UUID.randomUUID().toString());
        return authenticationContext;
    }

    /**
     * To get the identity provider based on the file configurations.
     * @param idpFileName Relevant file of IDP configuration.
     * @return Related Identity Provider.
     * @throws XMLStreamException XML Stream Exception.
     */
    protected IdentityProvider getTestIdentityProvider(String idpFileName) throws XMLStreamException {

        InputStream inputStream = this.getClass().getResourceAsStream(idpFileName);
        OMElement documentElement = new StAXOMBuilder(inputStream).getDocumentElement();
        return IdentityProvider.build(documentElement);
    }

}
