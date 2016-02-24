
/**
 * ExtensionMapper.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:46 UTC)
 */

        
            package org.wso2.carbon.user.api.xsd;
        
            /**
            *  ExtensionMapper class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class ExtensionMapper{

          public static java.lang.Object getTypeObject(java.lang.String namespaceURI,
                                                       java.lang.String typeName,
                                                       javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{

              
                  if (
                  "http://core.user.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "UserStoreException".equals(typeName)){
                   
                            return  org.wso2.carbon.user.core.xsd.UserStoreException.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://common.mgt.user.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "UserAdminException".equals(typeName)){
                   
                            return  org.wso2.carbon.user.mgt.stub.types.carbon.UserAdminException.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://common.mgt.user.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "UserStoreInfo".equals(typeName)){
                   
                            return  org.wso2.carbon.user.mgt.stub.types.carbon.UserStoreInfo.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://common.mgt.user.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "FlaggedName".equals(typeName)){
                   
                            return  org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://common.mgt.user.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "UserRealmInfo".equals(typeName)){
                   
                            return  org.wso2.carbon.user.mgt.stub.types.carbon.UserRealmInfo.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://common.mgt.user.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "ClaimValue".equals(typeName)){
                   
                            return  org.wso2.carbon.user.mgt.stub.types.carbon.ClaimValue.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://common.mgt.user.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "UIPermissionNode".equals(typeName)){
                   
                            return  org.wso2.carbon.user.mgt.stub.types.carbon.UIPermissionNode.Factory.parse(reader);
                        

                  }

              
             throw new org.apache.axis2.databinding.ADBException("Unsupported type " + namespaceURI + " " + typeName);
          }

        }
    