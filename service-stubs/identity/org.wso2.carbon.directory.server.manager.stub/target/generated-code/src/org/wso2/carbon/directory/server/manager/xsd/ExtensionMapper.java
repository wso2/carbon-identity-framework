
/**
 * ExtensionMapper.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:46 UTC)
 */

        
            package org.wso2.carbon.directory.server.manager.xsd;
        
            /**
            *  ExtensionMapper class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class ExtensionMapper{

          public static java.lang.Object getTypeObject(java.lang.String namespaceURI,
                                                       java.lang.String typeName,
                                                       javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{

              
                  if (
                  "http://common.manager.server.directory.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "ServerPrinciple".equals(typeName)){
                   
                            return  org.wso2.carbon.directory.common.stub.types.ServerPrinciple.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://manager.server.directory.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "DirectoryServerManagerException".equals(typeName)){
                   
                            return  org.wso2.carbon.directory.server.manager.xsd.DirectoryServerManagerException.Factory.parse(reader);
                        

                  }

              
             throw new org.apache.axis2.databinding.ADBException("Unsupported type " + namespaceURI + " " + typeName);
          }

        }
    