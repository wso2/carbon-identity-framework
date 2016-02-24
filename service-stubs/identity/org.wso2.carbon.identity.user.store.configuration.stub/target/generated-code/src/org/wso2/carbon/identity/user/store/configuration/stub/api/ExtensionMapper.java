
/**
 * ExtensionMapper.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:46 UTC)
 */

        
            package org.wso2.carbon.identity.user.store.configuration.stub.api;
        
            /**
            *  ExtensionMapper class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class ExtensionMapper{

          public static java.lang.Object getTypeObject(java.lang.String namespaceURI,
                                                       java.lang.String typeName,
                                                       javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{

              
                  if (
                  "http://dto.configuration.store.user.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "UserStoreDTO".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.user.store.configuration.stub.dto.UserStoreDTO.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://utils.configuration.store.user.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "IdentityUserStoreMgtException".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.user.store.configuration.stub.utils.IdentityUserStoreMgtException.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://api.user.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "Properties".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.user.store.configuration.stub.api.Properties.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://api.user.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "Property".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.user.store.configuration.stub.api.Property.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://dto.configuration.store.user.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "PropertyDTO".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.user.store.configuration.stub.dto.PropertyDTO.Factory.parse(reader);
                        

                  }

              
             throw new org.apache.axis2.databinding.ADBException("Unsupported type " + namespaceURI + " " + typeName);
          }

        }
    