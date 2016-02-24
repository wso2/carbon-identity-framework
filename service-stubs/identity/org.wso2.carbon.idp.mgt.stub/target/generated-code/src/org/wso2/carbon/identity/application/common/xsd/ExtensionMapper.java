
/**
 * ExtensionMapper.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:46 UTC)
 */

        
            package org.wso2.carbon.identity.application.common.xsd;
        
            /**
            *  ExtensionMapper class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class ExtensionMapper{

          public static java.lang.Object getTypeObject(java.lang.String namespaceURI,
                                                       java.lang.String typeName,
                                                       javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{

              
                  if (
                  "http://model.common.application.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "Claim".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.application.common.model.idp.xsd.Claim.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://model.common.application.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "LocalRole".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.application.common.model.idp.xsd.LocalRole.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://model.common.application.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "IdentityProviderProperty".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProviderProperty.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://model.common.application.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "InboundProvisioningConfig".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.application.common.model.idp.xsd.InboundProvisioningConfig.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://model.common.application.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "FederatedAuthenticatorConfig".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://model.common.application.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "PermissionsAndRoleConfig".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.application.common.model.idp.xsd.PermissionsAndRoleConfig.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://model.common.application.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "ClaimConfig".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.application.common.model.idp.xsd.ClaimConfig.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://model.common.application.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "Property".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.application.common.model.idp.xsd.Property.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://model.common.application.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "RoleMapping".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.application.common.model.idp.xsd.RoleMapping.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://model.common.application.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "ProvisioningConnectorConfig".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.application.common.model.idp.xsd.ProvisioningConnectorConfig.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://model.common.application.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "ClaimMapping".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.application.common.model.idp.xsd.ClaimMapping.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://model.common.application.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "ApplicationPermission".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.application.common.model.idp.xsd.ApplicationPermission.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://model.common.application.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "JustInTimeProvisioningConfig".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.application.common.model.idp.xsd.JustInTimeProvisioningConfig.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://model.common.application.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "IdentityProvider".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider.Factory.parse(reader);
                        

                  }

              
             throw new org.apache.axis2.databinding.ADBException("Unsupported type " + namespaceURI + " " + typeName);
          }

        }
    