
/**
 * ExtensionMapper.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:46 UTC)
 */

        
            package org.wso2.carbon.identity.user.profile.stub;
        
            /**
            *  ExtensionMapper class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class ExtensionMapper{

          public static java.lang.Object getTypeObject(java.lang.String namespaceURI,
                                                       java.lang.String typeName,
                                                       javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{

              
                  if (
                  "http://mgt.profile.user.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "AssociatedAccountDTO".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.user.profile.stub.types.AssociatedAccountDTO.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://core.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "AbstractAdmin".equals(typeName)){
                   
                            return  org.wso2.carbon.core.xsd.AbstractAdmin.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://mgt.profile.user.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "UserProfileException".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.user.profile.stub.types.UserProfileException.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://mgt.profile.user.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "UserProfileAdmin".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.user.profile.stub.types.UserProfileAdmin.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://mgt.profile.user.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "UserProfileDTO".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.user.profile.stub.types.UserProfileDTO.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://mgt.profile.user.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "UserFieldDTO".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.user.profile.stub.types.UserFieldDTO.Factory.parse(reader);
                        

                  }

              
             throw new org.apache.axis2.databinding.ADBException("Unsupported type " + namespaceURI + " " + typeName);
          }

        }
    