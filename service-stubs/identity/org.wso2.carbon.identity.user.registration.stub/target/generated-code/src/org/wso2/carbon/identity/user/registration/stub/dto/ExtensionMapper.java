
/**
 * ExtensionMapper.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:46 UTC)
 */

        
            package org.wso2.carbon.identity.user.registration.stub.dto;
        
            /**
            *  ExtensionMapper class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class ExtensionMapper{

          public static java.lang.Object getTypeObject(java.lang.String namespaceURI,
                                                       java.lang.String typeName,
                                                       javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{

              
                  if (
                  "http://registration.user.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "UserRegistrationException".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.user.registration.stub.types.UserRegistrationException.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://dto.registration.user.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "PasswordRegExDTO".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.user.registration.stub.dto.PasswordRegExDTO.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://dto.registration.user.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "UserDTO".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.user.registration.stub.dto.UserDTO.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://org.apache.axis2/xsd".equals(namespaceURI) &&
                  "Exception".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.user.registration.stub.types.axis2.Exception.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://base.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "IdentityException".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.base.xsd.IdentityException.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://dto.registration.user.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "UserFieldDTO".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.user.registration.stub.dto.UserFieldDTO.Factory.parse(reader);
                        

                  }

              
             throw new org.apache.axis2.databinding.ADBException("Unsupported type " + namespaceURI + " " + typeName);
          }

        }
    