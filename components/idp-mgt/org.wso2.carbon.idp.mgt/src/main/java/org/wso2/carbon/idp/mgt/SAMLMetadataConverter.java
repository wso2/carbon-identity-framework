package org.wso2.carbon.idp.mgt;

import org.apache.axiom.om.OMElement;
import  org.apache.axiom.om.util.AXIOMUtil;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.SAML2SSOFederatedAuthenticatorConfig;

/**
 * Created by pasindutennage on 9/27/16.
 */
public class SAMLMetadataConverter implements MetadataConverter {

    public boolean canHandle(FederatedAuthenticatorConfig federatedAuthenticatorConfig) {
        //checks whether this type of federatedAuthenticatorConfig contains a property with the name "metadataFromFileSystem"

        Property properties[] = federatedAuthenticatorConfig.getProperties();
        if (properties != null && properties.length != 0) {
            for (int i = 0; i < properties.length; i++) {
                if (properties != null) {
                    if (properties[i].getName() !=null && properties[i].getName().equals("metadataFromFileSystem")) {
                        if(properties[i].getValue()==null){
                            return false;
                        }else{
                            return true;
                        }
                    }
                }
            }return false;
        } else {
            return false;
        }
    }

    private FederatedAuthenticatorConfig validate(FederatedAuthenticatorConfig original, FederatedAuthenticatorConfig metaPassed){

        //original gets modified and it's returned
        Property propertiesOriginal[] = original.getProperties();
        Property propertiesMetadata[] = metaPassed.getProperties();

        for(int i = 0 ; i<25 ; i++){
            //compare 2 objects and return the correct one

        }
        return null;
    }

    public FederatedAuthenticatorConfig getFederatedAuthenticatorConfigByParsingStringToXML(FederatedAuthenticatorConfig federatedAuthenticatorConfig)throws  javax.xml.stream.XMLStreamException {
        //invoke build method in FederatedAuthenticationConfig
        //Compare original federatedAuthenticationConfig vs the one returned by "parse" method
        Property properties[] = federatedAuthenticatorConfig.getProperties();
        if (properties != null && properties.length != 0) {
            for (int i = 0; i < properties.length; i++) {
                if (properties != null) {
                    if (properties[i].getName() !=null && properties[i].getName().equals("metadataFromFileSystem")) {
                        String metadata = properties[i].getValue();
                        //parse metadata into OMElement object and build, returns a FederatedAuthenticationConfig object
                        OMElement element =  AXIOMUtil.stringToOM(metadata);
                        FederatedAuthenticatorConfig federatedAuthenticatorConfigMetadata = SAML2SSOFederatedAuthenticatorConfig.build( AXIOMUtil.stringToOM(metadata));
                        //compare two files and add missing parametrs  validate
                        //FederatedAuthenticatorConfig federatedAuthenticatorConfigFinal  = validate(federatedAuthenticatorConfig,federatedAuthenticatorConfigMetadata);

                        federatedAuthenticatorConfig.setProperties(federatedAuthenticatorConfigMetadata.getProperties());

                        return federatedAuthenticatorConfig ;
                    }
                }
            }
        } else {

        }




        return null;
    }


    public FederatedAuthenticatorConfig getFederatedAuthenticatorConfigByParsingXMLToString(FederatedAuthenticatorConfig federatedAuthenticatorConfig) {
        //get SAML parameters from parameter and create the xml string and set it as a property
        return null;
    }



}
