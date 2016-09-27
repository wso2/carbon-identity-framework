package org.wso2.carbon.identity.application.authentication.framework;

import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;

/**
 * Created by pasindutennage on 9/26/16.
 */
public interface MetadataConverter {

    //Convertfrom, to xml
    //can handle (property
    //
    // )


    public boolean canHandle(FederatedAuthenticatorConfig federatedAuthenticatorConfig);
    //using property name

    public FederatedAuthenticatorConfig getFederatedAuthenticatorConfigByParsingStringToXML(FederatedAuthenticatorConfig federatedAuthenticatorConfig) throws  javax.xml.stream.XMLStreamException ;
    //invoke parse method in FederatedAuthenticationConfig
    //Compare original federatedAuthenticationConfig vs the one returned by "parse" method


    public FederatedAuthenticatorConfig getFederatedAuthenticatorConfigByParsingXMLToString (FederatedAuthenticatorConfig federatedAuthenticatorConfig);
    //get SAML parameters from parameter and create the xml string and set it as a property

}
