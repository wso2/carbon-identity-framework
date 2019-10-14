package org.wso2.carbon.identity.core.util;

import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;

public class SAMLInitializer {

    public static void doBootstrap() {
            Thread thread = Thread.currentThread();
            ClassLoader loader = thread.getContextClassLoader();
            thread.setContextClassLoader(InitializationService.class.getClassLoader());

            try {
                InitializationService.initialize();

                org.opensaml.saml.config.SAMLConfigurationInitializer initializer_1 = new org.opensaml.saml.config.SAMLConfigurationInitializer();
                initializer_1.init();

                org.opensaml.saml.config.XMLObjectProviderInitializer initializer_2 = new org.opensaml.saml.config.XMLObjectProviderInitializer();
                initializer_2.init();

                org.opensaml.core.xml.config.XMLObjectProviderInitializer initializer_3 = new org.opensaml.core.xml.config.XMLObjectProviderInitializer();
                initializer_3.init();

                org.opensaml.core.xml.config.GlobalParserPoolInitializer initializer_4 = new org.opensaml.core.xml.config.GlobalParserPoolInitializer();
                initializer_4.init();

                org.opensaml.xmlsec.config.JavaCryptoValidationInitializer initializer_5 = new org.opensaml.xmlsec.config.JavaCryptoValidationInitializer();
                initializer_5.init();

                org.opensaml.xmlsec.config.XMLObjectProviderInitializer initializer_6 = new org.opensaml.xmlsec.config.XMLObjectProviderInitializer();
                initializer_6.init();

                org.opensaml.xmlsec.config.ApacheXMLSecurityInitializer initializer_7 = new org.opensaml.xmlsec.config.ApacheXMLSecurityInitializer();
                initializer_7.init();

                org.opensaml.xmlsec.config.GlobalSecurityConfigurationInitializer initializer_8 = new org.opensaml.xmlsec.config.GlobalSecurityConfigurationInitializer();
                initializer_8.init();

                org.opensaml.xmlsec.config.GlobalAlgorithmRegistryInitializer initializer_9 = new org.opensaml.xmlsec.config.GlobalAlgorithmRegistryInitializer();
                initializer_9.init();
            } catch (InitializationException e) {
                System.out.println("Error in bootstrapping the OpenSAML3 library");
                e.printStackTrace();
            } finally {
                thread.setContextClassLoader(loader);
            }
    }
}
