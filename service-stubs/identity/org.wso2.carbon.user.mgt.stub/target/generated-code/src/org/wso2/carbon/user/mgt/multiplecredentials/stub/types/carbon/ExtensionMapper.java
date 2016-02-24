
/**
 * ExtensionMapper.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:46 UTC)
 */

        
            package org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon;
        
            /**
            *  ExtensionMapper class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class ExtensionMapper{

          public static java.lang.Object getTypeObject(java.lang.String namespaceURI,
                                                       java.lang.String typeName,
                                                       javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{

              
                  if (
                  "http://multiplecredentials.core.user.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "CredentialProperty".equals(typeName)){
                   
                            return  org.wso2.carbon.user.mgt.multiplecredentials.stub.types.CredentialProperty.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://multiplecredentials.core.user.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "Credential".equals(typeName)){
                   
                            return  org.wso2.carbon.user.mgt.multiplecredentials.stub.types.Credential.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://common.mgt.user.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "MultipleCredentialsUserAdminException".equals(typeName)){
                   
                            return  org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminException.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://common.mgt.user.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "ClaimValue".equals(typeName)){
                   
                            return  org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.ClaimValue.Factory.parse(reader);
                        

                  }

              
             throw new org.apache.axis2.databinding.ADBException("Unsupported type " + namespaceURI + " " + typeName);
          }

        }
    