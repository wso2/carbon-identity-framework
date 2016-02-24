
/**
 * ExtensionMapper.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:46 UTC)
 */

        
            package org.wso2.carbon.identity.entitlement.stub.types.axis2;
        
            /**
            *  ExtensionMapper class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class ExtensionMapper{

          public static java.lang.Object getTypeObject(java.lang.String namespaceURI,
                                                       java.lang.String typeName,
                                                       javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{

              
                  if (
                  "http://dto.entitlement.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "PolicyFinderDataHolder".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.entitlement.stub.dto.PolicyFinderDataHolder.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://dto.entitlement.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "PIPFinderDataHolder".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.entitlement.stub.dto.PIPFinderDataHolder.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://dto.entitlement.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "PDPDataHolder".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.entitlement.stub.dto.PDPDataHolder.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://base.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "IdentityException".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.base.xsd.IdentityException.Factory.parse(reader);
                        

                  }

              
             throw new org.apache.axis2.databinding.ADBException("Unsupported type " + namespaceURI + " " + typeName);
          }

        }
    