/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.identity.core.util;

import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.config.SAMLConfigurationInitializer;
import org.opensaml.core.xml.config.GlobalParserPoolInitializer;
import org.opensaml.xmlsec.config.JavaCryptoValidationInitializer;
import org.opensaml.xmlsec.config.ApacheXMLSecurityInitializer;
import org.opensaml.xmlsec.config.GlobalSecurityConfigurationInitializer;
import org.opensaml.xmlsec.config.GlobalAlgorithmRegistryInitializer;

/**
 * Initializes the OpenSAML 3 library at a central location to ensure that it is
 * accessible from any component
 */
public class SAMLInitializer {

    /**
     * Initializes the required initializers
     * @throws InitializationException
     */
    public static void doBootstrap() throws InitializationException {

        Thread thread = Thread.currentThread();
        ClassLoader originalClassLoader = thread.getContextClassLoader();
        thread.setContextClassLoader(InitializationService.class.getClassLoader());

        try {

            InitializationService.initialize();

            SAMLConfigurationInitializer samlConfigurationInitializer = new SAMLConfigurationInitializer();
            samlConfigurationInitializer.init();

            org.opensaml.saml.config.XMLObjectProviderInitializer samlXMLObjectProviderInitializer = new org.opensaml.saml.config.XMLObjectProviderInitializer();
            samlXMLObjectProviderInitializer.init();

            org.opensaml.core.xml.config.XMLObjectProviderInitializer coreXMLObjectProviderInitializer = new org.opensaml.core.xml.config.XMLObjectProviderInitializer();
            coreXMLObjectProviderInitializer.init();

            GlobalParserPoolInitializer globalParserPoolInitializer = new GlobalParserPoolInitializer();
            globalParserPoolInitializer.init();

            JavaCryptoValidationInitializer javaCryptoValidationInitializer = new JavaCryptoValidationInitializer();
            javaCryptoValidationInitializer.init();

            org.opensaml.xmlsec.config.XMLObjectProviderInitializer xmlsecXMLObjectProviderInitializer = new org.opensaml.xmlsec.config.XMLObjectProviderInitializer();
            xmlsecXMLObjectProviderInitializer.init();

            ApacheXMLSecurityInitializer apacheXMLSecurityInitializer = new ApacheXMLSecurityInitializer();
            apacheXMLSecurityInitializer.init();

            GlobalSecurityConfigurationInitializer globalSecurityConfigurationInitializer = new GlobalSecurityConfigurationInitializer();
            globalSecurityConfigurationInitializer.init();

            GlobalAlgorithmRegistryInitializer globalAlgorithmRegistryInitializer = new GlobalAlgorithmRegistryInitializer();
            globalAlgorithmRegistryInitializer.init();

        } finally {
            thread.setContextClassLoader(originalClassLoader);
        }
    }
}
