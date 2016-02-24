
/**
 * ExtensionMapper.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:46 UTC)
 */

        
            package org.wso2.carbon.identity.mgt.stub;
        
            /**
            *  ExtensionMapper class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class ExtensionMapper{

          public static java.lang.Object getTypeObject(java.lang.String namespaceURI,
                                                       java.lang.String typeName,
                                                       javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{

              
                  if (
                  "http://mgt.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "IdentityMgtServiceException".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.mgt.xsd.IdentityMgtServiceException.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://dto.mgt.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "EmailTemplateDTO".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.mgt.stub.dto.EmailTemplateDTO.Factory.parse(reader);
                        

                  }

              
             throw new org.apache.axis2.databinding.ADBException("Unsupported type " + namespaceURI + " " + typeName);
          }

        }
    