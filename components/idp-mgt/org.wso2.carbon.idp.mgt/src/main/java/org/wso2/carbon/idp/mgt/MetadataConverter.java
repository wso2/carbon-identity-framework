package org.wso2.carbon.idp.mgt;

import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.Property;

import java.security.cert.X509Certificate;

/**
 * Created by pasindutennage on 9/26/16.
 */
public interface MetadataConverter {

    //Convertfrom, to xml
    //can handle (property
    //
    // )


    public boolean canHandle(Property property);
    //using property name

    public FederatedAuthenticatorConfig getFederatedAuthenticatorConfigByParsingStringToXML(String name,StringBuilder  builder) throws IdentityProviderManagementException, javax.xml.stream.XMLStreamException ;
    //invoke parse method in FederatedAuthenticationConfig
    //Compare original federatedAuthenticationConfig vs the one returned by "parse" method


    public FederatedAuthenticatorConfig getFederatedAuthenticatorConfigByParsingXMLToString (FederatedAuthenticatorConfig federatedAuthenticatorConfig);
    //get SAML parameters from parameter and create the xml string and set it as a property

}
