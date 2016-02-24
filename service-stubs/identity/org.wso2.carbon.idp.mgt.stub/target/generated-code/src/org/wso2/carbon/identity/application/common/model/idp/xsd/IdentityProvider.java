
/**
 * IdentityProvider.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:46 UTC)
 */

            
                package org.wso2.carbon.identity.application.common.model.idp.xsd;
            

            /**
            *  IdentityProvider bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class IdentityProvider
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = IdentityProvider
                Namespace URI = http://model.common.application.identity.carbon.wso2.org/xsd
                Namespace Prefix = ns1
                */
            

                        /**
                        * field for Alias
                        */

                        
                                    protected java.lang.String localAlias ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAliasTracker = false ;

                           public boolean isAliasSpecified(){
                               return localAliasTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getAlias(){
                               return localAlias;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Alias
                               */
                               public void setAlias(java.lang.String param){
                            localAliasTracker = true;
                                   
                                            this.localAlias=param;
                                    

                               }
                            

                        /**
                        * field for Certificate
                        */

                        
                                    protected java.lang.String localCertificate ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localCertificateTracker = false ;

                           public boolean isCertificateSpecified(){
                               return localCertificateTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getCertificate(){
                               return localCertificate;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Certificate
                               */
                               public void setCertificate(java.lang.String param){
                            localCertificateTracker = true;
                                   
                                            this.localCertificate=param;
                                    

                               }
                            

                        /**
                        * field for ClaimConfig
                        */

                        
                                    protected org.wso2.carbon.identity.application.common.model.idp.xsd.ClaimConfig localClaimConfig ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localClaimConfigTracker = false ;

                           public boolean isClaimConfigSpecified(){
                               return localClaimConfigTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.identity.application.common.model.idp.xsd.ClaimConfig
                           */
                           public  org.wso2.carbon.identity.application.common.model.idp.xsd.ClaimConfig getClaimConfig(){
                               return localClaimConfig;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ClaimConfig
                               */
                               public void setClaimConfig(org.wso2.carbon.identity.application.common.model.idp.xsd.ClaimConfig param){
                            localClaimConfigTracker = true;
                                   
                                            this.localClaimConfig=param;
                                    

                               }
                            

                        /**
                        * field for DefaultAuthenticatorConfig
                        */

                        
                                    protected org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig localDefaultAuthenticatorConfig ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localDefaultAuthenticatorConfigTracker = false ;

                           public boolean isDefaultAuthenticatorConfigSpecified(){
                               return localDefaultAuthenticatorConfigTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig
                           */
                           public  org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig getDefaultAuthenticatorConfig(){
                               return localDefaultAuthenticatorConfig;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param DefaultAuthenticatorConfig
                               */
                               public void setDefaultAuthenticatorConfig(org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig param){
                            localDefaultAuthenticatorConfigTracker = true;
                                   
                                            this.localDefaultAuthenticatorConfig=param;
                                    

                               }
                            

                        /**
                        * field for DefaultProvisioningConnectorConfig
                        */

                        
                                    protected org.wso2.carbon.identity.application.common.model.idp.xsd.ProvisioningConnectorConfig localDefaultProvisioningConnectorConfig ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localDefaultProvisioningConnectorConfigTracker = false ;

                           public boolean isDefaultProvisioningConnectorConfigSpecified(){
                               return localDefaultProvisioningConnectorConfigTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.identity.application.common.model.idp.xsd.ProvisioningConnectorConfig
                           */
                           public  org.wso2.carbon.identity.application.common.model.idp.xsd.ProvisioningConnectorConfig getDefaultProvisioningConnectorConfig(){
                               return localDefaultProvisioningConnectorConfig;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param DefaultProvisioningConnectorConfig
                               */
                               public void setDefaultProvisioningConnectorConfig(org.wso2.carbon.identity.application.common.model.idp.xsd.ProvisioningConnectorConfig param){
                            localDefaultProvisioningConnectorConfigTracker = true;
                                   
                                            this.localDefaultProvisioningConnectorConfig=param;
                                    

                               }
                            

                        /**
                        * field for DisplayName
                        */

                        
                                    protected java.lang.String localDisplayName ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localDisplayNameTracker = false ;

                           public boolean isDisplayNameSpecified(){
                               return localDisplayNameTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getDisplayName(){
                               return localDisplayName;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param DisplayName
                               */
                               public void setDisplayName(java.lang.String param){
                            localDisplayNameTracker = true;
                                   
                                            this.localDisplayName=param;
                                    

                               }
                            

                        /**
                        * field for Enable
                        */

                        
                                    protected boolean localEnable ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localEnableTracker = false ;

                           public boolean isEnableSpecified(){
                               return localEnableTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getEnable(){
                               return localEnable;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Enable
                               */
                               public void setEnable(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localEnableTracker =
                                       true;
                                   
                                            this.localEnable=param;
                                    

                               }
                            

                        /**
                        * field for FederatedAuthenticatorConfigs
                        * This was an Array!
                        */

                        
                                    protected org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig[] localFederatedAuthenticatorConfigs ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localFederatedAuthenticatorConfigsTracker = false ;

                           public boolean isFederatedAuthenticatorConfigsSpecified(){
                               return localFederatedAuthenticatorConfigsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig[]
                           */
                           public  org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig[] getFederatedAuthenticatorConfigs(){
                               return localFederatedAuthenticatorConfigs;
                           }

                           
                        


                               
                              /**
                               * validate the array for FederatedAuthenticatorConfigs
                               */
                              protected void validateFederatedAuthenticatorConfigs(org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param FederatedAuthenticatorConfigs
                              */
                              public void setFederatedAuthenticatorConfigs(org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig[] param){
                              
                                   validateFederatedAuthenticatorConfigs(param);

                               localFederatedAuthenticatorConfigsTracker = true;
                                      
                                      this.localFederatedAuthenticatorConfigs=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig
                             */
                             public void addFederatedAuthenticatorConfigs(org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig param){
                                   if (localFederatedAuthenticatorConfigs == null){
                                   localFederatedAuthenticatorConfigs = new org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig[]{};
                                   }

                            
                                 //update the setting tracker
                                localFederatedAuthenticatorConfigsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localFederatedAuthenticatorConfigs);
                               list.add(param);
                               this.localFederatedAuthenticatorConfigs =
                             (org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig[])list.toArray(
                            new org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig[list.size()]);

                             }
                             

                        /**
                        * field for FederationHub
                        */

                        
                                    protected boolean localFederationHub ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localFederationHubTracker = false ;

                           public boolean isFederationHubSpecified(){
                               return localFederationHubTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getFederationHub(){
                               return localFederationHub;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param FederationHub
                               */
                               public void setFederationHub(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localFederationHubTracker =
                                       true;
                                   
                                            this.localFederationHub=param;
                                    

                               }
                            

                        /**
                        * field for HomeRealmId
                        */

                        
                                    protected java.lang.String localHomeRealmId ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localHomeRealmIdTracker = false ;

                           public boolean isHomeRealmIdSpecified(){
                               return localHomeRealmIdTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getHomeRealmId(){
                               return localHomeRealmId;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param HomeRealmId
                               */
                               public void setHomeRealmId(java.lang.String param){
                            localHomeRealmIdTracker = true;
                                   
                                            this.localHomeRealmId=param;
                                    

                               }
                            

                        /**
                        * field for IdentityProviderDescription
                        */

                        
                                    protected java.lang.String localIdentityProviderDescription ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localIdentityProviderDescriptionTracker = false ;

                           public boolean isIdentityProviderDescriptionSpecified(){
                               return localIdentityProviderDescriptionTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getIdentityProviderDescription(){
                               return localIdentityProviderDescription;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param IdentityProviderDescription
                               */
                               public void setIdentityProviderDescription(java.lang.String param){
                            localIdentityProviderDescriptionTracker = true;
                                   
                                            this.localIdentityProviderDescription=param;
                                    

                               }
                            

                        /**
                        * field for IdentityProviderName
                        */

                        
                                    protected java.lang.String localIdentityProviderName ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localIdentityProviderNameTracker = false ;

                           public boolean isIdentityProviderNameSpecified(){
                               return localIdentityProviderNameTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getIdentityProviderName(){
                               return localIdentityProviderName;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param IdentityProviderName
                               */
                               public void setIdentityProviderName(java.lang.String param){
                            localIdentityProviderNameTracker = true;
                                   
                                            this.localIdentityProviderName=param;
                                    

                               }
                            

                        /**
                        * field for IdpProperties
                        * This was an Array!
                        */

                        
                                    protected org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProviderProperty[] localIdpProperties ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localIdpPropertiesTracker = false ;

                           public boolean isIdpPropertiesSpecified(){
                               return localIdpPropertiesTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProviderProperty[]
                           */
                           public  org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProviderProperty[] getIdpProperties(){
                               return localIdpProperties;
                           }

                           
                        


                               
                              /**
                               * validate the array for IdpProperties
                               */
                              protected void validateIdpProperties(org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProviderProperty[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param IdpProperties
                              */
                              public void setIdpProperties(org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProviderProperty[] param){
                              
                                   validateIdpProperties(param);

                               localIdpPropertiesTracker = true;
                                      
                                      this.localIdpProperties=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProviderProperty
                             */
                             public void addIdpProperties(org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProviderProperty param){
                                   if (localIdpProperties == null){
                                   localIdpProperties = new org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProviderProperty[]{};
                                   }

                            
                                 //update the setting tracker
                                localIdpPropertiesTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localIdpProperties);
                               list.add(param);
                               this.localIdpProperties =
                             (org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProviderProperty[])list.toArray(
                            new org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProviderProperty[list.size()]);

                             }
                             

                        /**
                        * field for JustInTimeProvisioningConfig
                        */

                        
                                    protected org.wso2.carbon.identity.application.common.model.idp.xsd.JustInTimeProvisioningConfig localJustInTimeProvisioningConfig ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localJustInTimeProvisioningConfigTracker = false ;

                           public boolean isJustInTimeProvisioningConfigSpecified(){
                               return localJustInTimeProvisioningConfigTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.identity.application.common.model.idp.xsd.JustInTimeProvisioningConfig
                           */
                           public  org.wso2.carbon.identity.application.common.model.idp.xsd.JustInTimeProvisioningConfig getJustInTimeProvisioningConfig(){
                               return localJustInTimeProvisioningConfig;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param JustInTimeProvisioningConfig
                               */
                               public void setJustInTimeProvisioningConfig(org.wso2.carbon.identity.application.common.model.idp.xsd.JustInTimeProvisioningConfig param){
                            localJustInTimeProvisioningConfigTracker = true;
                                   
                                            this.localJustInTimeProvisioningConfig=param;
                                    

                               }
                            

                        /**
                        * field for PermissionAndRoleConfig
                        */

                        
                                    protected org.wso2.carbon.identity.application.common.model.idp.xsd.PermissionsAndRoleConfig localPermissionAndRoleConfig ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPermissionAndRoleConfigTracker = false ;

                           public boolean isPermissionAndRoleConfigSpecified(){
                               return localPermissionAndRoleConfigTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.identity.application.common.model.idp.xsd.PermissionsAndRoleConfig
                           */
                           public  org.wso2.carbon.identity.application.common.model.idp.xsd.PermissionsAndRoleConfig getPermissionAndRoleConfig(){
                               return localPermissionAndRoleConfig;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PermissionAndRoleConfig
                               */
                               public void setPermissionAndRoleConfig(org.wso2.carbon.identity.application.common.model.idp.xsd.PermissionsAndRoleConfig param){
                            localPermissionAndRoleConfigTracker = true;
                                   
                                            this.localPermissionAndRoleConfig=param;
                                    

                               }
                            

                        /**
                        * field for Primary
                        */

                        
                                    protected boolean localPrimary ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPrimaryTracker = false ;

                           public boolean isPrimarySpecified(){
                               return localPrimaryTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getPrimary(){
                               return localPrimary;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Primary
                               */
                               public void setPrimary(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localPrimaryTracker =
                                       true;
                                   
                                            this.localPrimary=param;
                                    

                               }
                            

                        /**
                        * field for ProvisioningConnectorConfigs
                        * This was an Array!
                        */

                        
                                    protected org.wso2.carbon.identity.application.common.model.idp.xsd.ProvisioningConnectorConfig[] localProvisioningConnectorConfigs ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localProvisioningConnectorConfigsTracker = false ;

                           public boolean isProvisioningConnectorConfigsSpecified(){
                               return localProvisioningConnectorConfigsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.identity.application.common.model.idp.xsd.ProvisioningConnectorConfig[]
                           */
                           public  org.wso2.carbon.identity.application.common.model.idp.xsd.ProvisioningConnectorConfig[] getProvisioningConnectorConfigs(){
                               return localProvisioningConnectorConfigs;
                           }

                           
                        


                               
                              /**
                               * validate the array for ProvisioningConnectorConfigs
                               */
                              protected void validateProvisioningConnectorConfigs(org.wso2.carbon.identity.application.common.model.idp.xsd.ProvisioningConnectorConfig[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param ProvisioningConnectorConfigs
                              */
                              public void setProvisioningConnectorConfigs(org.wso2.carbon.identity.application.common.model.idp.xsd.ProvisioningConnectorConfig[] param){
                              
                                   validateProvisioningConnectorConfigs(param);

                               localProvisioningConnectorConfigsTracker = true;
                                      
                                      this.localProvisioningConnectorConfigs=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param org.wso2.carbon.identity.application.common.model.idp.xsd.ProvisioningConnectorConfig
                             */
                             public void addProvisioningConnectorConfigs(org.wso2.carbon.identity.application.common.model.idp.xsd.ProvisioningConnectorConfig param){
                                   if (localProvisioningConnectorConfigs == null){
                                   localProvisioningConnectorConfigs = new org.wso2.carbon.identity.application.common.model.idp.xsd.ProvisioningConnectorConfig[]{};
                                   }

                            
                                 //update the setting tracker
                                localProvisioningConnectorConfigsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localProvisioningConnectorConfigs);
                               list.add(param);
                               this.localProvisioningConnectorConfigs =
                             (org.wso2.carbon.identity.application.common.model.idp.xsd.ProvisioningConnectorConfig[])list.toArray(
                            new org.wso2.carbon.identity.application.common.model.idp.xsd.ProvisioningConnectorConfig[list.size()]);

                             }
                             

                        /**
                        * field for ProvisioningRole
                        */

                        
                                    protected java.lang.String localProvisioningRole ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localProvisioningRoleTracker = false ;

                           public boolean isProvisioningRoleSpecified(){
                               return localProvisioningRoleTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getProvisioningRole(){
                               return localProvisioningRole;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ProvisioningRole
                               */
                               public void setProvisioningRole(java.lang.String param){
                            localProvisioningRoleTracker = true;
                                   
                                            this.localProvisioningRole=param;
                                    

                               }
                            

     
     
        /**
        *
        * @param parentQName
        * @param factory
        * @return org.apache.axiom.om.OMElement
        */
       public org.apache.axiom.om.OMElement getOMElement (
               final javax.xml.namespace.QName parentQName,
               final org.apache.axiom.om.OMFactory factory) throws org.apache.axis2.databinding.ADBException{


        
               org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this,parentQName);
               return factory.createOMElement(dataSource,parentQName);
            
        }

         public void serialize(final javax.xml.namespace.QName parentQName,
                                       javax.xml.stream.XMLStreamWriter xmlWriter)
                                throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
                           serialize(parentQName,xmlWriter,false);
         }

         public void serialize(final javax.xml.namespace.QName parentQName,
                               javax.xml.stream.XMLStreamWriter xmlWriter,
                               boolean serializeType)
            throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
            
                


                java.lang.String prefix = null;
                java.lang.String namespace = null;
                

                    prefix = parentQName.getPrefix();
                    namespace = parentQName.getNamespaceURI();
                    writeStartElement(prefix, namespace, parentQName.getLocalPart(), xmlWriter);
                
                  if (serializeType){
               

                   java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://model.common.application.identity.carbon.wso2.org/xsd");
                   if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           namespacePrefix+":IdentityProvider",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "IdentityProvider",
                           xmlWriter);
                   }

               
                   }
                if (localAliasTracker){
                                    namespace = "http://model.common.application.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "alias", xmlWriter);
                             

                                          if (localAlias==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAlias);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localCertificateTracker){
                                    namespace = "http://model.common.application.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "certificate", xmlWriter);
                             

                                          if (localCertificate==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localCertificate);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localClaimConfigTracker){
                                    if (localClaimConfig==null){

                                        writeStartElement(null, "http://model.common.application.identity.carbon.wso2.org/xsd", "claimConfig", xmlWriter);

                                       // write the nil attribute
                                      writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                      xmlWriter.writeEndElement();
                                    }else{
                                     localClaimConfig.serialize(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","claimConfig"),
                                        xmlWriter);
                                    }
                                } if (localDefaultAuthenticatorConfigTracker){
                                    if (localDefaultAuthenticatorConfig==null){

                                        writeStartElement(null, "http://model.common.application.identity.carbon.wso2.org/xsd", "defaultAuthenticatorConfig", xmlWriter);

                                       // write the nil attribute
                                      writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                      xmlWriter.writeEndElement();
                                    }else{
                                     localDefaultAuthenticatorConfig.serialize(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","defaultAuthenticatorConfig"),
                                        xmlWriter);
                                    }
                                } if (localDefaultProvisioningConnectorConfigTracker){
                                    if (localDefaultProvisioningConnectorConfig==null){

                                        writeStartElement(null, "http://model.common.application.identity.carbon.wso2.org/xsd", "defaultProvisioningConnectorConfig", xmlWriter);

                                       // write the nil attribute
                                      writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                      xmlWriter.writeEndElement();
                                    }else{
                                     localDefaultProvisioningConnectorConfig.serialize(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","defaultProvisioningConnectorConfig"),
                                        xmlWriter);
                                    }
                                } if (localDisplayNameTracker){
                                    namespace = "http://model.common.application.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "displayName", xmlWriter);
                             

                                          if (localDisplayName==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localDisplayName);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localEnableTracker){
                                    namespace = "http://model.common.application.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "enable", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("enable cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localEnable));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localFederatedAuthenticatorConfigsTracker){
                                       if (localFederatedAuthenticatorConfigs!=null){
                                            for (int i = 0;i < localFederatedAuthenticatorConfigs.length;i++){
                                                if (localFederatedAuthenticatorConfigs[i] != null){
                                                 localFederatedAuthenticatorConfigs[i].serialize(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","federatedAuthenticatorConfigs"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://model.common.application.identity.carbon.wso2.org/xsd", "federatedAuthenticatorConfigs", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://model.common.application.identity.carbon.wso2.org/xsd", "federatedAuthenticatorConfigs", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localFederationHubTracker){
                                    namespace = "http://model.common.application.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "federationHub", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("federationHub cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localFederationHub));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localHomeRealmIdTracker){
                                    namespace = "http://model.common.application.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "homeRealmId", xmlWriter);
                             

                                          if (localHomeRealmId==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localHomeRealmId);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localIdentityProviderDescriptionTracker){
                                    namespace = "http://model.common.application.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "identityProviderDescription", xmlWriter);
                             

                                          if (localIdentityProviderDescription==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localIdentityProviderDescription);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localIdentityProviderNameTracker){
                                    namespace = "http://model.common.application.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "identityProviderName", xmlWriter);
                             

                                          if (localIdentityProviderName==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localIdentityProviderName);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localIdpPropertiesTracker){
                                       if (localIdpProperties!=null){
                                            for (int i = 0;i < localIdpProperties.length;i++){
                                                if (localIdpProperties[i] != null){
                                                 localIdpProperties[i].serialize(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","idpProperties"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://model.common.application.identity.carbon.wso2.org/xsd", "idpProperties", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://model.common.application.identity.carbon.wso2.org/xsd", "idpProperties", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localJustInTimeProvisioningConfigTracker){
                                    if (localJustInTimeProvisioningConfig==null){

                                        writeStartElement(null, "http://model.common.application.identity.carbon.wso2.org/xsd", "justInTimeProvisioningConfig", xmlWriter);

                                       // write the nil attribute
                                      writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                      xmlWriter.writeEndElement();
                                    }else{
                                     localJustInTimeProvisioningConfig.serialize(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","justInTimeProvisioningConfig"),
                                        xmlWriter);
                                    }
                                } if (localPermissionAndRoleConfigTracker){
                                    if (localPermissionAndRoleConfig==null){

                                        writeStartElement(null, "http://model.common.application.identity.carbon.wso2.org/xsd", "permissionAndRoleConfig", xmlWriter);

                                       // write the nil attribute
                                      writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                      xmlWriter.writeEndElement();
                                    }else{
                                     localPermissionAndRoleConfig.serialize(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","permissionAndRoleConfig"),
                                        xmlWriter);
                                    }
                                } if (localPrimaryTracker){
                                    namespace = "http://model.common.application.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "primary", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("primary cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPrimary));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localProvisioningConnectorConfigsTracker){
                                       if (localProvisioningConnectorConfigs!=null){
                                            for (int i = 0;i < localProvisioningConnectorConfigs.length;i++){
                                                if (localProvisioningConnectorConfigs[i] != null){
                                                 localProvisioningConnectorConfigs[i].serialize(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","provisioningConnectorConfigs"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://model.common.application.identity.carbon.wso2.org/xsd", "provisioningConnectorConfigs", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://model.common.application.identity.carbon.wso2.org/xsd", "provisioningConnectorConfigs", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localProvisioningRoleTracker){
                                    namespace = "http://model.common.application.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "provisioningRole", xmlWriter);
                             

                                          if (localProvisioningRole==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localProvisioningRole);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             }
                    xmlWriter.writeEndElement();
               

        }

        private static java.lang.String generatePrefix(java.lang.String namespace) {
            if(namespace.equals("http://model.common.application.identity.carbon.wso2.org/xsd")){
                return "ns1";
            }
            return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }

        /**
         * Utility method to write an element start tag.
         */
        private void writeStartElement(java.lang.String prefix, java.lang.String namespace, java.lang.String localPart,
                                       javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
            if (writerPrefix != null) {
                xmlWriter.writeStartElement(namespace, localPart);
            } else {
                if (namespace.length() == 0) {
                    prefix = "";
                } else if (prefix == null) {
                    prefix = generatePrefix(namespace);
                }

                xmlWriter.writeStartElement(prefix, localPart, namespace);
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }
        }
        
        /**
         * Util method to write an attribute with the ns prefix
         */
        private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                    java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
            if (xmlWriter.getPrefix(namespace) == null) {
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }
            xmlWriter.writeAttribute(namespace,attName,attValue);
        }

        /**
         * Util method to write an attribute without the ns prefix
         */
        private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                    java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
            if (namespace.equals("")) {
                xmlWriter.writeAttribute(attName,attValue);
            } else {
                registerPrefix(xmlWriter, namespace);
                xmlWriter.writeAttribute(namespace,attName,attValue);
            }
        }


           /**
             * Util method to write an attribute without the ns prefix
             */
            private void writeQNameAttribute(java.lang.String namespace, java.lang.String attName,
                                             javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

                java.lang.String attributeNamespace = qname.getNamespaceURI();
                java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
                if (attributePrefix == null) {
                    attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
                }
                java.lang.String attributeValue;
                if (attributePrefix.trim().length() > 0) {
                    attributeValue = attributePrefix + ":" + qname.getLocalPart();
                } else {
                    attributeValue = qname.getLocalPart();
                }

                if (namespace.equals("")) {
                    xmlWriter.writeAttribute(attName, attributeValue);
                } else {
                    registerPrefix(xmlWriter, namespace);
                    xmlWriter.writeAttribute(namespace, attName, attributeValue);
                }
            }
        /**
         *  method to handle Qnames
         */

        private void writeQName(javax.xml.namespace.QName qname,
                                javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            java.lang.String namespaceURI = qname.getNamespaceURI();
            if (namespaceURI != null) {
                java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
                if (prefix == null) {
                    prefix = generatePrefix(namespaceURI);
                    xmlWriter.writeNamespace(prefix, namespaceURI);
                    xmlWriter.setPrefix(prefix,namespaceURI);
                }

                if (prefix.trim().length() > 0){
                    xmlWriter.writeCharacters(prefix + ":" + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                } else {
                    // i.e this is the default namespace
                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                }

            } else {
                xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
            }
        }

        private void writeQNames(javax.xml.namespace.QName[] qnames,
                                 javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

            if (qnames != null) {
                // we have to store this data until last moment since it is not possible to write any
                // namespace data after writing the charactor data
                java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
                java.lang.String namespaceURI = null;
                java.lang.String prefix = null;

                for (int i = 0; i < qnames.length; i++) {
                    if (i > 0) {
                        stringToWrite.append(" ");
                    }
                    namespaceURI = qnames[i].getNamespaceURI();
                    if (namespaceURI != null) {
                        prefix = xmlWriter.getPrefix(namespaceURI);
                        if ((prefix == null) || (prefix.length() == 0)) {
                            prefix = generatePrefix(namespaceURI);
                            xmlWriter.writeNamespace(prefix, namespaceURI);
                            xmlWriter.setPrefix(prefix,namespaceURI);
                        }

                        if (prefix.trim().length() > 0){
                            stringToWrite.append(prefix).append(":").append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                        } else {
                            stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                        }
                    } else {
                        stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                    }
                }
                xmlWriter.writeCharacters(stringToWrite.toString());
            }

        }


        /**
         * Register a namespace prefix
         */
        private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
            java.lang.String prefix = xmlWriter.getPrefix(namespace);
            if (prefix == null) {
                prefix = generatePrefix(namespace);
                while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                    prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                }
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }
            return prefix;
        }


  
        /**
        * databinding method to get an XML representation of this object
        *
        */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName)
                    throws org.apache.axis2.databinding.ADBException{


        
                 java.util.ArrayList elementList = new java.util.ArrayList();
                 java.util.ArrayList attribList = new java.util.ArrayList();

                 if (localAliasTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                      "alias"));
                                 
                                         elementList.add(localAlias==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAlias));
                                    } if (localCertificateTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                      "certificate"));
                                 
                                         elementList.add(localCertificate==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCertificate));
                                    } if (localClaimConfigTracker){
                            elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                      "claimConfig"));
                            
                            
                                    elementList.add(localClaimConfig==null?null:
                                    localClaimConfig);
                                } if (localDefaultAuthenticatorConfigTracker){
                            elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                      "defaultAuthenticatorConfig"));
                            
                            
                                    elementList.add(localDefaultAuthenticatorConfig==null?null:
                                    localDefaultAuthenticatorConfig);
                                } if (localDefaultProvisioningConnectorConfigTracker){
                            elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                      "defaultProvisioningConnectorConfig"));
                            
                            
                                    elementList.add(localDefaultProvisioningConnectorConfig==null?null:
                                    localDefaultProvisioningConnectorConfig);
                                } if (localDisplayNameTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                      "displayName"));
                                 
                                         elementList.add(localDisplayName==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDisplayName));
                                    } if (localEnableTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                      "enable"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localEnable));
                            } if (localFederatedAuthenticatorConfigsTracker){
                             if (localFederatedAuthenticatorConfigs!=null) {
                                 for (int i = 0;i < localFederatedAuthenticatorConfigs.length;i++){

                                    if (localFederatedAuthenticatorConfigs[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                          "federatedAuthenticatorConfigs"));
                                         elementList.add(localFederatedAuthenticatorConfigs[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                          "federatedAuthenticatorConfigs"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                          "federatedAuthenticatorConfigs"));
                                        elementList.add(localFederatedAuthenticatorConfigs);
                                    
                             }

                        } if (localFederationHubTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                      "federationHub"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localFederationHub));
                            } if (localHomeRealmIdTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                      "homeRealmId"));
                                 
                                         elementList.add(localHomeRealmId==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localHomeRealmId));
                                    } if (localIdentityProviderDescriptionTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                      "identityProviderDescription"));
                                 
                                         elementList.add(localIdentityProviderDescription==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIdentityProviderDescription));
                                    } if (localIdentityProviderNameTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                      "identityProviderName"));
                                 
                                         elementList.add(localIdentityProviderName==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIdentityProviderName));
                                    } if (localIdpPropertiesTracker){
                             if (localIdpProperties!=null) {
                                 for (int i = 0;i < localIdpProperties.length;i++){

                                    if (localIdpProperties[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                          "idpProperties"));
                                         elementList.add(localIdpProperties[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                          "idpProperties"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                          "idpProperties"));
                                        elementList.add(localIdpProperties);
                                    
                             }

                        } if (localJustInTimeProvisioningConfigTracker){
                            elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                      "justInTimeProvisioningConfig"));
                            
                            
                                    elementList.add(localJustInTimeProvisioningConfig==null?null:
                                    localJustInTimeProvisioningConfig);
                                } if (localPermissionAndRoleConfigTracker){
                            elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                      "permissionAndRoleConfig"));
                            
                            
                                    elementList.add(localPermissionAndRoleConfig==null?null:
                                    localPermissionAndRoleConfig);
                                } if (localPrimaryTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                      "primary"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPrimary));
                            } if (localProvisioningConnectorConfigsTracker){
                             if (localProvisioningConnectorConfigs!=null) {
                                 for (int i = 0;i < localProvisioningConnectorConfigs.length;i++){

                                    if (localProvisioningConnectorConfigs[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                          "provisioningConnectorConfigs"));
                                         elementList.add(localProvisioningConnectorConfigs[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                          "provisioningConnectorConfigs"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                          "provisioningConnectorConfigs"));
                                        elementList.add(localProvisioningConnectorConfigs);
                                    
                             }

                        } if (localProvisioningRoleTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                      "provisioningRole"));
                                 
                                         elementList.add(localProvisioningRole==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localProvisioningRole));
                                    }

                return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
            
            

        }

  

     /**
      *  Factory class that keeps the parse method
      */
    public static class Factory{

        
        

        /**
        * static method to create the object
        * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
        *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
        * Postcondition: If this object is an element, the reader is positioned at its end element
        *                If this object is a complex type, the reader is positioned at the end element of its outer element
        */
        public static IdentityProvider parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            IdentityProvider object =
                new IdentityProvider();

            int event;
            java.lang.String nillableValue = null;
            java.lang.String prefix ="";
            java.lang.String namespaceuri ="";
            try {
                
                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();

                
                if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                  java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                        "type");
                  if (fullTypeName!=null){
                    java.lang.String nsPrefix = null;
                    if (fullTypeName.indexOf(":") > -1){
                        nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                    }
                    nsPrefix = nsPrefix==null?"":nsPrefix;

                    java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                    
                            if (!"IdentityProvider".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (IdentityProvider)org.wso2.carbon.identity.application.common.xsd.ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }
                        

                  }
                

                }

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                
                    
                    reader.next();
                
                        java.util.ArrayList list8 = new java.util.ArrayList();
                    
                        java.util.ArrayList list13 = new java.util.ArrayList();
                    
                        java.util.ArrayList list17 = new java.util.ArrayList();
                    
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","alias").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setAlias(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","certificate").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setCertificate(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","claimConfig").equals(reader.getName())){
                                
                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                          object.setClaimConfig(null);
                                          reader.next();
                                            
                                            reader.next();
                                          
                                      }else{
                                    
                                                object.setClaimConfig(org.wso2.carbon.identity.application.common.model.idp.xsd.ClaimConfig.Factory.parse(reader));
                                              
                                        reader.next();
                                    }
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","defaultAuthenticatorConfig").equals(reader.getName())){
                                
                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                          object.setDefaultAuthenticatorConfig(null);
                                          reader.next();
                                            
                                            reader.next();
                                          
                                      }else{
                                    
                                                object.setDefaultAuthenticatorConfig(org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig.Factory.parse(reader));
                                              
                                        reader.next();
                                    }
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","defaultProvisioningConnectorConfig").equals(reader.getName())){
                                
                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                          object.setDefaultProvisioningConnectorConfig(null);
                                          reader.next();
                                            
                                            reader.next();
                                          
                                      }else{
                                    
                                                object.setDefaultProvisioningConnectorConfig(org.wso2.carbon.identity.application.common.model.idp.xsd.ProvisioningConnectorConfig.Factory.parse(reader));
                                              
                                        reader.next();
                                    }
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","displayName").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setDisplayName(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","enable").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"enable" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setEnable(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","federatedAuthenticatorConfigs").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list8.add(null);
                                                              reader.next();
                                                          } else {
                                                        list8.add(org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig.Factory.parse(reader));
                                                                }
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone8 = false;
                                                        while(!loopDone8){
                                                            // We should be at the end element, but make sure
                                                            while (!reader.isEndElement())
                                                                reader.next();
                                                            // Step out of this element
                                                            reader.next();
                                                            // Step to next element event.
                                                            while (!reader.isStartElement() && !reader.isEndElement())
                                                                reader.next();
                                                            if (reader.isEndElement()){
                                                                //two continuous end elements means we are exiting the xml structure
                                                                loopDone8 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","federatedAuthenticatorConfigs").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list8.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list8.add(org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone8 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setFederatedAuthenticatorConfigs((org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig.class,
                                                                list8));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","federationHub").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"federationHub" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setFederationHub(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","homeRealmId").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setHomeRealmId(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","identityProviderDescription").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setIdentityProviderDescription(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","identityProviderName").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setIdentityProviderName(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","idpProperties").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list13.add(null);
                                                              reader.next();
                                                          } else {
                                                        list13.add(org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProviderProperty.Factory.parse(reader));
                                                                }
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone13 = false;
                                                        while(!loopDone13){
                                                            // We should be at the end element, but make sure
                                                            while (!reader.isEndElement())
                                                                reader.next();
                                                            // Step out of this element
                                                            reader.next();
                                                            // Step to next element event.
                                                            while (!reader.isStartElement() && !reader.isEndElement())
                                                                reader.next();
                                                            if (reader.isEndElement()){
                                                                //two continuous end elements means we are exiting the xml structure
                                                                loopDone13 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","idpProperties").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list13.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list13.add(org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProviderProperty.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone13 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setIdpProperties((org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProviderProperty[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProviderProperty.class,
                                                                list13));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","justInTimeProvisioningConfig").equals(reader.getName())){
                                
                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                          object.setJustInTimeProvisioningConfig(null);
                                          reader.next();
                                            
                                            reader.next();
                                          
                                      }else{
                                    
                                                object.setJustInTimeProvisioningConfig(org.wso2.carbon.identity.application.common.model.idp.xsd.JustInTimeProvisioningConfig.Factory.parse(reader));
                                              
                                        reader.next();
                                    }
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","permissionAndRoleConfig").equals(reader.getName())){
                                
                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                          object.setPermissionAndRoleConfig(null);
                                          reader.next();
                                            
                                            reader.next();
                                          
                                      }else{
                                    
                                                object.setPermissionAndRoleConfig(org.wso2.carbon.identity.application.common.model.idp.xsd.PermissionsAndRoleConfig.Factory.parse(reader));
                                              
                                        reader.next();
                                    }
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","primary").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"primary" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setPrimary(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","provisioningConnectorConfigs").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list17.add(null);
                                                              reader.next();
                                                          } else {
                                                        list17.add(org.wso2.carbon.identity.application.common.model.idp.xsd.ProvisioningConnectorConfig.Factory.parse(reader));
                                                                }
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone17 = false;
                                                        while(!loopDone17){
                                                            // We should be at the end element, but make sure
                                                            while (!reader.isEndElement())
                                                                reader.next();
                                                            // Step out of this element
                                                            reader.next();
                                                            // Step to next element event.
                                                            while (!reader.isStartElement() && !reader.isEndElement())
                                                                reader.next();
                                                            if (reader.isEndElement()){
                                                                //two continuous end elements means we are exiting the xml structure
                                                                loopDone17 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","provisioningConnectorConfigs").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list17.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list17.add(org.wso2.carbon.identity.application.common.model.idp.xsd.ProvisioningConnectorConfig.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone17 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setProvisioningConnectorConfigs((org.wso2.carbon.identity.application.common.model.idp.xsd.ProvisioningConnectorConfig[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                org.wso2.carbon.identity.application.common.model.idp.xsd.ProvisioningConnectorConfig.class,
                                                                list17));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","provisioningRole").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setProvisioningRole(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                  
                            while (!reader.isStartElement() && !reader.isEndElement())
                                reader.next();
                            
                                if (reader.isStartElement())
                                // A start element we are not expecting indicates a trailing invalid property
                                throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                            



            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class

        

        }
           
    