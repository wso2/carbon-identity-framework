package org.wso2.carbon.idp.mgt;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.SAML2SSOFederatedAuthenticatorConfig;

import java.security.cert.X509Certificate;

/**
 * Created by pasindutennage on 9/27/16.
 */
public class SAMLMetadataConverter implements MetadataConverter {

    public boolean canHandle(Property property) {
        //checks whether this type of federatedAuthenticatorConfig contains a property with the name "meta_data_saml"

        if(property!=null){
            String meta = property.getName();
            if(meta!=null && meta.contains("saml")){
                return  true;
            }else{
                return false;
            }

        }else{
            return  false;
        }


    }

    private FederatedAuthenticatorConfig validate(FederatedAuthenticatorConfig original, FederatedAuthenticatorConfig metaPassed) {

        //original gets modified and it's returned
        Property propertiesOriginal[] = original.getProperties();//size 25
        Property propertiesMetadata[] = metaPassed.getProperties();//size 24

        for (int i = 0; i < 24; i++) {
            Property propertyMetadata = propertiesMetadata[i];
            if (propertyMetadata != null) {
                String propertyMetaName = propertyMetadata.getName();
                String propertyMetaValue = propertyMetadata.getValue();

                if (propertyMetaName != null && propertyMetaName.length() > 0) {

                    for (int j = 0; j < 25; j++) {
                        Property propertyOrigin = propertiesOriginal[j];
                        if (propertyOrigin != null) {
                            String propertyOriginName = propertyOrigin.getName();
                            String propertyOriginValue = propertyOrigin.getValue();
                                if(propertyOriginName !=null && propertyOriginName.equals(propertyMetaName) ){
                                    if(propertyMetaValue==null || propertyMetaValue.equals("")){
                                        propertiesMetadata[i].setValue(propertyOriginValue);
                                    }else if(propertyOriginValue!=null && propertyOriginValue.length()>0){
                                        propertiesMetadata[i].setValue(propertyOriginValue);
                                    }
                                    break;
                                }
                        }
                    }
                }
            }
        }
        original.setProperties(propertiesMetadata);
        return original;
    }

    public FederatedAuthenticatorConfig getFederatedAuthenticatorConfigByParsingStringToXML(String metadata,StringBuilder builder) throws javax.xml.stream.XMLStreamException {
        //invoke build method in FederatedAuthenticationConfig
        //Compare original federatedAuthenticationConfig vs the one returned by "parse" method
//        Property properties[] = federatedAuthenticatorConfig.getProperties();
//        if (properties != null && properties.length != 0) {
//            for (int i = 0; i < properties.length; i++) {
//                if (properties != null) {
//                    if (properties[i].getName() != null && properties[i].getName().equals("meta_data_saml")) {
//                        String metadata = properties[i].getValue();
                        //parse metadata into OMElement object and build, returns a FederatedAuthenticationConfig object
                        OMElement element = AXIOMUtil.stringToOM(metadata);
                        FederatedAuthenticatorConfig federatedAuthenticatorConfigMetadata = SAML2SSOFederatedAuthenticatorConfig.build(AXIOMUtil.stringToOM(metadata),builder);
//                        //compare two files and add missing parametrs  validate
//                        //FederatedAuthenticatorConfig federatedAuthenticatorConfigFinal = validate(federatedAuthenticatorConfig, federatedAuthenticatorConfigMetadata);
//                        federatedAuthenticatorConfig.setProperties(federatedAuthenticatorConfigMetadata.getProperties());
//                        return federatedAuthenticatorConfig;
//                    }
//                }
//            }
//        } else {
//
//        }
//
//
//        return null;
        return federatedAuthenticatorConfigMetadata;
    }


    public FederatedAuthenticatorConfig getFederatedAuthenticatorConfigByParsingXMLToString(FederatedAuthenticatorConfig federatedAuthenticatorConfig) {
        //get SAML parameters from parameter and create the xml string and set it as a property
        return null;
    }


}
