
/**
 * ServiceProvider.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:46 UTC)
 */

            
                package org.wso2.carbon.identity.application.common.model.xsd;
            

            /**
            *  ServiceProvider bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class ServiceProvider
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = ServiceProvider
                Namespace URI = http://model.common.application.identity.carbon.wso2.org/xsd
                Namespace Prefix = ns1
                */
            

                        /**
                        * field for ApplicationID
                        */

                        
                                    protected int localApplicationID ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localApplicationIDTracker = false ;

                           public boolean isApplicationIDSpecified(){
                               return localApplicationIDTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return int
                           */
                           public  int getApplicationID(){
                               return localApplicationID;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ApplicationID
                               */
                               public void setApplicationID(int param){
                            
                                       // setting primitive attribute tracker to true
                                       localApplicationIDTracker =
                                       param != java.lang.Integer.MIN_VALUE;
                                   
                                            this.localApplicationID=param;
                                    

                               }
                            

                        /**
                        * field for ApplicationName
                        */

                        
                                    protected java.lang.String localApplicationName ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localApplicationNameTracker = false ;

                           public boolean isApplicationNameSpecified(){
                               return localApplicationNameTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getApplicationName(){
                               return localApplicationName;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ApplicationName
                               */
                               public void setApplicationName(java.lang.String param){
                            localApplicationNameTracker = true;
                                   
                                            this.localApplicationName=param;
                                    

                               }
                            

                        /**
                        * field for ClaimConfig
                        */

                        
                                    protected org.wso2.carbon.identity.application.common.model.xsd.ClaimConfig localClaimConfig ;
                                
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
                           * @return org.wso2.carbon.identity.application.common.model.xsd.ClaimConfig
                           */
                           public  org.wso2.carbon.identity.application.common.model.xsd.ClaimConfig getClaimConfig(){
                               return localClaimConfig;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ClaimConfig
                               */
                               public void setClaimConfig(org.wso2.carbon.identity.application.common.model.xsd.ClaimConfig param){
                            localClaimConfigTracker = true;
                                   
                                            this.localClaimConfig=param;
                                    

                               }
                            

                        /**
                        * field for Description
                        */

                        
                                    protected java.lang.String localDescription ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localDescriptionTracker = false ;

                           public boolean isDescriptionSpecified(){
                               return localDescriptionTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getDescription(){
                               return localDescription;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Description
                               */
                               public void setDescription(java.lang.String param){
                            localDescriptionTracker = true;
                                   
                                            this.localDescription=param;
                                    

                               }
                            

                        /**
                        * field for InboundAuthenticationConfig
                        */

                        
                                    protected org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationConfig localInboundAuthenticationConfig ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localInboundAuthenticationConfigTracker = false ;

                           public boolean isInboundAuthenticationConfigSpecified(){
                               return localInboundAuthenticationConfigTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationConfig
                           */
                           public  org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationConfig getInboundAuthenticationConfig(){
                               return localInboundAuthenticationConfig;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param InboundAuthenticationConfig
                               */
                               public void setInboundAuthenticationConfig(org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationConfig param){
                            localInboundAuthenticationConfigTracker = true;
                                   
                                            this.localInboundAuthenticationConfig=param;
                                    

                               }
                            

                        /**
                        * field for InboundProvisioningConfig
                        */

                        
                                    protected org.wso2.carbon.identity.application.common.model.xsd.InboundProvisioningConfig localInboundProvisioningConfig ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localInboundProvisioningConfigTracker = false ;

                           public boolean isInboundProvisioningConfigSpecified(){
                               return localInboundProvisioningConfigTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.identity.application.common.model.xsd.InboundProvisioningConfig
                           */
                           public  org.wso2.carbon.identity.application.common.model.xsd.InboundProvisioningConfig getInboundProvisioningConfig(){
                               return localInboundProvisioningConfig;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param InboundProvisioningConfig
                               */
                               public void setInboundProvisioningConfig(org.wso2.carbon.identity.application.common.model.xsd.InboundProvisioningConfig param){
                            localInboundProvisioningConfigTracker = true;
                                   
                                            this.localInboundProvisioningConfig=param;
                                    

                               }
                            

                        /**
                        * field for LocalAndOutBoundAuthenticationConfig
                        */

                        
                                    protected org.wso2.carbon.identity.application.common.model.xsd.LocalAndOutboundAuthenticationConfig localLocalAndOutBoundAuthenticationConfig ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localLocalAndOutBoundAuthenticationConfigTracker = false ;

                           public boolean isLocalAndOutBoundAuthenticationConfigSpecified(){
                               return localLocalAndOutBoundAuthenticationConfigTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.identity.application.common.model.xsd.LocalAndOutboundAuthenticationConfig
                           */
                           public  org.wso2.carbon.identity.application.common.model.xsd.LocalAndOutboundAuthenticationConfig getLocalAndOutBoundAuthenticationConfig(){
                               return localLocalAndOutBoundAuthenticationConfig;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param LocalAndOutBoundAuthenticationConfig
                               */
                               public void setLocalAndOutBoundAuthenticationConfig(org.wso2.carbon.identity.application.common.model.xsd.LocalAndOutboundAuthenticationConfig param){
                            localLocalAndOutBoundAuthenticationConfigTracker = true;
                                   
                                            this.localLocalAndOutBoundAuthenticationConfig=param;
                                    

                               }
                            

                        /**
                        * field for OutboundProvisioningConfig
                        */

                        
                                    protected org.wso2.carbon.identity.application.common.model.xsd.OutboundProvisioningConfig localOutboundProvisioningConfig ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localOutboundProvisioningConfigTracker = false ;

                           public boolean isOutboundProvisioningConfigSpecified(){
                               return localOutboundProvisioningConfigTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.identity.application.common.model.xsd.OutboundProvisioningConfig
                           */
                           public  org.wso2.carbon.identity.application.common.model.xsd.OutboundProvisioningConfig getOutboundProvisioningConfig(){
                               return localOutboundProvisioningConfig;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param OutboundProvisioningConfig
                               */
                               public void setOutboundProvisioningConfig(org.wso2.carbon.identity.application.common.model.xsd.OutboundProvisioningConfig param){
                            localOutboundProvisioningConfigTracker = true;
                                   
                                            this.localOutboundProvisioningConfig=param;
                                    

                               }
                            

                        /**
                        * field for Owner
                        */

                        
                                    protected org.wso2.carbon.identity.application.common.model.xsd.User localOwner ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localOwnerTracker = false ;

                           public boolean isOwnerSpecified(){
                               return localOwnerTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.identity.application.common.model.xsd.User
                           */
                           public  org.wso2.carbon.identity.application.common.model.xsd.User getOwner(){
                               return localOwner;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Owner
                               */
                               public void setOwner(org.wso2.carbon.identity.application.common.model.xsd.User param){
                            localOwnerTracker = true;
                                   
                                            this.localOwner=param;
                                    

                               }
                            

                        /**
                        * field for PermissionAndRoleConfig
                        */

                        
                                    protected org.wso2.carbon.identity.application.common.model.xsd.PermissionsAndRoleConfig localPermissionAndRoleConfig ;
                                
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
                           * @return org.wso2.carbon.identity.application.common.model.xsd.PermissionsAndRoleConfig
                           */
                           public  org.wso2.carbon.identity.application.common.model.xsd.PermissionsAndRoleConfig getPermissionAndRoleConfig(){
                               return localPermissionAndRoleConfig;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PermissionAndRoleConfig
                               */
                               public void setPermissionAndRoleConfig(org.wso2.carbon.identity.application.common.model.xsd.PermissionsAndRoleConfig param){
                            localPermissionAndRoleConfigTracker = true;
                                   
                                            this.localPermissionAndRoleConfig=param;
                                    

                               }
                            

                        /**
                        * field for RequestPathAuthenticatorConfigs
                        * This was an Array!
                        */

                        
                                    protected org.wso2.carbon.identity.application.common.model.xsd.RequestPathAuthenticatorConfig[] localRequestPathAuthenticatorConfigs ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localRequestPathAuthenticatorConfigsTracker = false ;

                           public boolean isRequestPathAuthenticatorConfigsSpecified(){
                               return localRequestPathAuthenticatorConfigsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.identity.application.common.model.xsd.RequestPathAuthenticatorConfig[]
                           */
                           public  org.wso2.carbon.identity.application.common.model.xsd.RequestPathAuthenticatorConfig[] getRequestPathAuthenticatorConfigs(){
                               return localRequestPathAuthenticatorConfigs;
                           }

                           
                        


                               
                              /**
                               * validate the array for RequestPathAuthenticatorConfigs
                               */
                              protected void validateRequestPathAuthenticatorConfigs(org.wso2.carbon.identity.application.common.model.xsd.RequestPathAuthenticatorConfig[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param RequestPathAuthenticatorConfigs
                              */
                              public void setRequestPathAuthenticatorConfigs(org.wso2.carbon.identity.application.common.model.xsd.RequestPathAuthenticatorConfig[] param){
                              
                                   validateRequestPathAuthenticatorConfigs(param);

                               localRequestPathAuthenticatorConfigsTracker = true;
                                      
                                      this.localRequestPathAuthenticatorConfigs=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param org.wso2.carbon.identity.application.common.model.xsd.RequestPathAuthenticatorConfig
                             */
                             public void addRequestPathAuthenticatorConfigs(org.wso2.carbon.identity.application.common.model.xsd.RequestPathAuthenticatorConfig param){
                                   if (localRequestPathAuthenticatorConfigs == null){
                                   localRequestPathAuthenticatorConfigs = new org.wso2.carbon.identity.application.common.model.xsd.RequestPathAuthenticatorConfig[]{};
                                   }

                            
                                 //update the setting tracker
                                localRequestPathAuthenticatorConfigsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localRequestPathAuthenticatorConfigs);
                               list.add(param);
                               this.localRequestPathAuthenticatorConfigs =
                             (org.wso2.carbon.identity.application.common.model.xsd.RequestPathAuthenticatorConfig[])list.toArray(
                            new org.wso2.carbon.identity.application.common.model.xsd.RequestPathAuthenticatorConfig[list.size()]);

                             }
                             

                        /**
                        * field for SaasApp
                        */

                        
                                    protected boolean localSaasApp ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localSaasAppTracker = false ;

                           public boolean isSaasAppSpecified(){
                               return localSaasAppTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getSaasApp(){
                               return localSaasApp;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param SaasApp
                               */
                               public void setSaasApp(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localSaasAppTracker =
                                       true;
                                   
                                            this.localSaasApp=param;
                                    

                               }
                            

                        /**
                        * field for SpProperties
                        * This was an Array!
                        */

                        
                                    protected org.wso2.carbon.identity.application.common.model.xsd.ServiceProviderProperty[] localSpProperties ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localSpPropertiesTracker = false ;

                           public boolean isSpPropertiesSpecified(){
                               return localSpPropertiesTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.identity.application.common.model.xsd.ServiceProviderProperty[]
                           */
                           public  org.wso2.carbon.identity.application.common.model.xsd.ServiceProviderProperty[] getSpProperties(){
                               return localSpProperties;
                           }

                           
                        


                               
                              /**
                               * validate the array for SpProperties
                               */
                              protected void validateSpProperties(org.wso2.carbon.identity.application.common.model.xsd.ServiceProviderProperty[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param SpProperties
                              */
                              public void setSpProperties(org.wso2.carbon.identity.application.common.model.xsd.ServiceProviderProperty[] param){
                              
                                   validateSpProperties(param);

                               localSpPropertiesTracker = true;
                                      
                                      this.localSpProperties=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param org.wso2.carbon.identity.application.common.model.xsd.ServiceProviderProperty
                             */
                             public void addSpProperties(org.wso2.carbon.identity.application.common.model.xsd.ServiceProviderProperty param){
                                   if (localSpProperties == null){
                                   localSpProperties = new org.wso2.carbon.identity.application.common.model.xsd.ServiceProviderProperty[]{};
                                   }

                            
                                 //update the setting tracker
                                localSpPropertiesTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localSpProperties);
                               list.add(param);
                               this.localSpProperties =
                             (org.wso2.carbon.identity.application.common.model.xsd.ServiceProviderProperty[])list.toArray(
                            new org.wso2.carbon.identity.application.common.model.xsd.ServiceProviderProperty[list.size()]);

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
                           namespacePrefix+":ServiceProvider",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "ServiceProvider",
                           xmlWriter);
                   }

               
                   }
                if (localApplicationIDTracker){
                                    namespace = "http://model.common.application.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "applicationID", xmlWriter);
                             
                                               if (localApplicationID==java.lang.Integer.MIN_VALUE) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("applicationID cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localApplicationID));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localApplicationNameTracker){
                                    namespace = "http://model.common.application.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "applicationName", xmlWriter);
                             

                                          if (localApplicationName==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localApplicationName);
                                            
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
                                } if (localDescriptionTracker){
                                    namespace = "http://model.common.application.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "description", xmlWriter);
                             

                                          if (localDescription==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localDescription);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localInboundAuthenticationConfigTracker){
                                    if (localInboundAuthenticationConfig==null){

                                        writeStartElement(null, "http://model.common.application.identity.carbon.wso2.org/xsd", "inboundAuthenticationConfig", xmlWriter);

                                       // write the nil attribute
                                      writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                      xmlWriter.writeEndElement();
                                    }else{
                                     localInboundAuthenticationConfig.serialize(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","inboundAuthenticationConfig"),
                                        xmlWriter);
                                    }
                                } if (localInboundProvisioningConfigTracker){
                                    if (localInboundProvisioningConfig==null){

                                        writeStartElement(null, "http://model.common.application.identity.carbon.wso2.org/xsd", "inboundProvisioningConfig", xmlWriter);

                                       // write the nil attribute
                                      writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                      xmlWriter.writeEndElement();
                                    }else{
                                     localInboundProvisioningConfig.serialize(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","inboundProvisioningConfig"),
                                        xmlWriter);
                                    }
                                } if (localLocalAndOutBoundAuthenticationConfigTracker){
                                    if (localLocalAndOutBoundAuthenticationConfig==null){

                                        writeStartElement(null, "http://model.common.application.identity.carbon.wso2.org/xsd", "localAndOutBoundAuthenticationConfig", xmlWriter);

                                       // write the nil attribute
                                      writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                      xmlWriter.writeEndElement();
                                    }else{
                                     localLocalAndOutBoundAuthenticationConfig.serialize(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","localAndOutBoundAuthenticationConfig"),
                                        xmlWriter);
                                    }
                                } if (localOutboundProvisioningConfigTracker){
                                    if (localOutboundProvisioningConfig==null){

                                        writeStartElement(null, "http://model.common.application.identity.carbon.wso2.org/xsd", "outboundProvisioningConfig", xmlWriter);

                                       // write the nil attribute
                                      writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                      xmlWriter.writeEndElement();
                                    }else{
                                     localOutboundProvisioningConfig.serialize(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","outboundProvisioningConfig"),
                                        xmlWriter);
                                    }
                                } if (localOwnerTracker){
                                    if (localOwner==null){

                                        writeStartElement(null, "http://model.common.application.identity.carbon.wso2.org/xsd", "owner", xmlWriter);

                                       // write the nil attribute
                                      writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                      xmlWriter.writeEndElement();
                                    }else{
                                     localOwner.serialize(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","owner"),
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
                                } if (localRequestPathAuthenticatorConfigsTracker){
                                       if (localRequestPathAuthenticatorConfigs!=null){
                                            for (int i = 0;i < localRequestPathAuthenticatorConfigs.length;i++){
                                                if (localRequestPathAuthenticatorConfigs[i] != null){
                                                 localRequestPathAuthenticatorConfigs[i].serialize(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","requestPathAuthenticatorConfigs"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://model.common.application.identity.carbon.wso2.org/xsd", "requestPathAuthenticatorConfigs", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://model.common.application.identity.carbon.wso2.org/xsd", "requestPathAuthenticatorConfigs", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localSaasAppTracker){
                                    namespace = "http://model.common.application.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "saasApp", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("saasApp cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSaasApp));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localSpPropertiesTracker){
                                       if (localSpProperties!=null){
                                            for (int i = 0;i < localSpProperties.length;i++){
                                                if (localSpProperties[i] != null){
                                                 localSpProperties[i].serialize(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","spProperties"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://model.common.application.identity.carbon.wso2.org/xsd", "spProperties", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://model.common.application.identity.carbon.wso2.org/xsd", "spProperties", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
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

                 if (localApplicationIDTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                      "applicationID"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localApplicationID));
                            } if (localApplicationNameTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                      "applicationName"));
                                 
                                         elementList.add(localApplicationName==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localApplicationName));
                                    } if (localClaimConfigTracker){
                            elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                      "claimConfig"));
                            
                            
                                    elementList.add(localClaimConfig==null?null:
                                    localClaimConfig);
                                } if (localDescriptionTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                      "description"));
                                 
                                         elementList.add(localDescription==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDescription));
                                    } if (localInboundAuthenticationConfigTracker){
                            elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                      "inboundAuthenticationConfig"));
                            
                            
                                    elementList.add(localInboundAuthenticationConfig==null?null:
                                    localInboundAuthenticationConfig);
                                } if (localInboundProvisioningConfigTracker){
                            elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                      "inboundProvisioningConfig"));
                            
                            
                                    elementList.add(localInboundProvisioningConfig==null?null:
                                    localInboundProvisioningConfig);
                                } if (localLocalAndOutBoundAuthenticationConfigTracker){
                            elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                      "localAndOutBoundAuthenticationConfig"));
                            
                            
                                    elementList.add(localLocalAndOutBoundAuthenticationConfig==null?null:
                                    localLocalAndOutBoundAuthenticationConfig);
                                } if (localOutboundProvisioningConfigTracker){
                            elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                      "outboundProvisioningConfig"));
                            
                            
                                    elementList.add(localOutboundProvisioningConfig==null?null:
                                    localOutboundProvisioningConfig);
                                } if (localOwnerTracker){
                            elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                      "owner"));
                            
                            
                                    elementList.add(localOwner==null?null:
                                    localOwner);
                                } if (localPermissionAndRoleConfigTracker){
                            elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                      "permissionAndRoleConfig"));
                            
                            
                                    elementList.add(localPermissionAndRoleConfig==null?null:
                                    localPermissionAndRoleConfig);
                                } if (localRequestPathAuthenticatorConfigsTracker){
                             if (localRequestPathAuthenticatorConfigs!=null) {
                                 for (int i = 0;i < localRequestPathAuthenticatorConfigs.length;i++){

                                    if (localRequestPathAuthenticatorConfigs[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                          "requestPathAuthenticatorConfigs"));
                                         elementList.add(localRequestPathAuthenticatorConfigs[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                          "requestPathAuthenticatorConfigs"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                          "requestPathAuthenticatorConfigs"));
                                        elementList.add(localRequestPathAuthenticatorConfigs);
                                    
                             }

                        } if (localSaasAppTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                      "saasApp"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSaasApp));
                            } if (localSpPropertiesTracker){
                             if (localSpProperties!=null) {
                                 for (int i = 0;i < localSpProperties.length;i++){

                                    if (localSpProperties[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                          "spProperties"));
                                         elementList.add(localSpProperties[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                          "spProperties"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                          "spProperties"));
                                        elementList.add(localSpProperties);
                                    
                             }

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
        public static ServiceProvider parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            ServiceProvider object =
                new ServiceProvider();

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
                    
                            if (!"ServiceProvider".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (ServiceProvider)org.wso2.carbon.identity.application.common.xsd.ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }
                        

                  }
                

                }

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                
                    
                    reader.next();
                
                        java.util.ArrayList list11 = new java.util.ArrayList();
                    
                        java.util.ArrayList list13 = new java.util.ArrayList();
                    
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","applicationID").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"applicationID" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setApplicationID(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                               object.setApplicationID(java.lang.Integer.MIN_VALUE);
                                           
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","applicationName").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setApplicationName(
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
                                    
                                                object.setClaimConfig(org.wso2.carbon.identity.application.common.model.xsd.ClaimConfig.Factory.parse(reader));
                                              
                                        reader.next();
                                    }
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","description").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setDescription(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","inboundAuthenticationConfig").equals(reader.getName())){
                                
                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                          object.setInboundAuthenticationConfig(null);
                                          reader.next();
                                            
                                            reader.next();
                                          
                                      }else{
                                    
                                                object.setInboundAuthenticationConfig(org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationConfig.Factory.parse(reader));
                                              
                                        reader.next();
                                    }
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","inboundProvisioningConfig").equals(reader.getName())){
                                
                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                          object.setInboundProvisioningConfig(null);
                                          reader.next();
                                            
                                            reader.next();
                                          
                                      }else{
                                    
                                                object.setInboundProvisioningConfig(org.wso2.carbon.identity.application.common.model.xsd.InboundProvisioningConfig.Factory.parse(reader));
                                              
                                        reader.next();
                                    }
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","localAndOutBoundAuthenticationConfig").equals(reader.getName())){
                                
                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                          object.setLocalAndOutBoundAuthenticationConfig(null);
                                          reader.next();
                                            
                                            reader.next();
                                          
                                      }else{
                                    
                                                object.setLocalAndOutBoundAuthenticationConfig(org.wso2.carbon.identity.application.common.model.xsd.LocalAndOutboundAuthenticationConfig.Factory.parse(reader));
                                              
                                        reader.next();
                                    }
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","outboundProvisioningConfig").equals(reader.getName())){
                                
                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                          object.setOutboundProvisioningConfig(null);
                                          reader.next();
                                            
                                            reader.next();
                                          
                                      }else{
                                    
                                                object.setOutboundProvisioningConfig(org.wso2.carbon.identity.application.common.model.xsd.OutboundProvisioningConfig.Factory.parse(reader));
                                              
                                        reader.next();
                                    }
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","owner").equals(reader.getName())){
                                
                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                          object.setOwner(null);
                                          reader.next();
                                            
                                            reader.next();
                                          
                                      }else{
                                    
                                                object.setOwner(org.wso2.carbon.identity.application.common.model.xsd.User.Factory.parse(reader));
                                              
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
                                    
                                                object.setPermissionAndRoleConfig(org.wso2.carbon.identity.application.common.model.xsd.PermissionsAndRoleConfig.Factory.parse(reader));
                                              
                                        reader.next();
                                    }
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","requestPathAuthenticatorConfigs").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list11.add(null);
                                                              reader.next();
                                                          } else {
                                                        list11.add(org.wso2.carbon.identity.application.common.model.xsd.RequestPathAuthenticatorConfig.Factory.parse(reader));
                                                                }
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone11 = false;
                                                        while(!loopDone11){
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
                                                                loopDone11 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","requestPathAuthenticatorConfigs").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list11.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list11.add(org.wso2.carbon.identity.application.common.model.xsd.RequestPathAuthenticatorConfig.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone11 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setRequestPathAuthenticatorConfigs((org.wso2.carbon.identity.application.common.model.xsd.RequestPathAuthenticatorConfig[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                org.wso2.carbon.identity.application.common.model.xsd.RequestPathAuthenticatorConfig.class,
                                                                list11));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","saasApp").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"saasApp" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setSaasApp(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","spProperties").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list13.add(null);
                                                              reader.next();
                                                          } else {
                                                        list13.add(org.wso2.carbon.identity.application.common.model.xsd.ServiceProviderProperty.Factory.parse(reader));
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
                                                                if (new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","spProperties").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list13.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list13.add(org.wso2.carbon.identity.application.common.model.xsd.ServiceProviderProperty.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone13 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setSpProperties((org.wso2.carbon.identity.application.common.model.xsd.ServiceProviderProperty[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                org.wso2.carbon.identity.application.common.model.xsd.ServiceProviderProperty.class,
                                                                list13));
                                                            
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
           
    