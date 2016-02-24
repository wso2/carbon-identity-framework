
/**
 * UserRealmInfo.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:46 UTC)
 */

            
                package org.wso2.carbon.user.mgt.stub.types.carbon;
            

            /**
            *  UserRealmInfo bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class UserRealmInfo
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = UserRealmInfo
                Namespace URI = http://common.mgt.user.carbon.wso2.org/xsd
                Namespace Prefix = ns1
                */
            

                        /**
                        * field for AdminRole
                        */

                        
                                    protected java.lang.String localAdminRole ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAdminRoleTracker = false ;

                           public boolean isAdminRoleSpecified(){
                               return localAdminRoleTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getAdminRole(){
                               return localAdminRole;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param AdminRole
                               */
                               public void setAdminRole(java.lang.String param){
                            localAdminRoleTracker = true;
                                   
                                            this.localAdminRole=param;
                                    

                               }
                            

                        /**
                        * field for AdminUser
                        */

                        
                                    protected java.lang.String localAdminUser ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAdminUserTracker = false ;

                           public boolean isAdminUserSpecified(){
                               return localAdminUserTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getAdminUser(){
                               return localAdminUser;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param AdminUser
                               */
                               public void setAdminUser(java.lang.String param){
                            localAdminUserTracker = true;
                                   
                                            this.localAdminUser=param;
                                    

                               }
                            

                        /**
                        * field for BulkImportSupported
                        */

                        
                                    protected boolean localBulkImportSupported ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localBulkImportSupportedTracker = false ;

                           public boolean isBulkImportSupportedSpecified(){
                               return localBulkImportSupportedTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getBulkImportSupported(){
                               return localBulkImportSupported;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param BulkImportSupported
                               */
                               public void setBulkImportSupported(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localBulkImportSupportedTracker =
                                       true;
                                   
                                            this.localBulkImportSupported=param;
                                    

                               }
                            

                        /**
                        * field for DefaultUserClaims
                        * This was an Array!
                        */

                        
                                    protected java.lang.String[] localDefaultUserClaims ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localDefaultUserClaimsTracker = false ;

                           public boolean isDefaultUserClaimsSpecified(){
                               return localDefaultUserClaimsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String[]
                           */
                           public  java.lang.String[] getDefaultUserClaims(){
                               return localDefaultUserClaims;
                           }

                           
                        


                               
                              /**
                               * validate the array for DefaultUserClaims
                               */
                              protected void validateDefaultUserClaims(java.lang.String[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param DefaultUserClaims
                              */
                              public void setDefaultUserClaims(java.lang.String[] param){
                              
                                   validateDefaultUserClaims(param);

                               localDefaultUserClaimsTracker = true;
                                      
                                      this.localDefaultUserClaims=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param java.lang.String
                             */
                             public void addDefaultUserClaims(java.lang.String param){
                                   if (localDefaultUserClaims == null){
                                   localDefaultUserClaims = new java.lang.String[]{};
                                   }

                            
                                 //update the setting tracker
                                localDefaultUserClaimsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localDefaultUserClaims);
                               list.add(param);
                               this.localDefaultUserClaims =
                             (java.lang.String[])list.toArray(
                            new java.lang.String[list.size()]);

                             }
                             

                        /**
                        * field for DomainNames
                        * This was an Array!
                        */

                        
                                    protected java.lang.String[] localDomainNames ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localDomainNamesTracker = false ;

                           public boolean isDomainNamesSpecified(){
                               return localDomainNamesTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String[]
                           */
                           public  java.lang.String[] getDomainNames(){
                               return localDomainNames;
                           }

                           
                        


                               
                              /**
                               * validate the array for DomainNames
                               */
                              protected void validateDomainNames(java.lang.String[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param DomainNames
                              */
                              public void setDomainNames(java.lang.String[] param){
                              
                                   validateDomainNames(param);

                               localDomainNamesTracker = true;
                                      
                                      this.localDomainNames=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param java.lang.String
                             */
                             public void addDomainNames(java.lang.String param){
                                   if (localDomainNames == null){
                                   localDomainNames = new java.lang.String[]{};
                                   }

                            
                                 //update the setting tracker
                                localDomainNamesTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localDomainNames);
                               list.add(param);
                               this.localDomainNames =
                             (java.lang.String[])list.toArray(
                            new java.lang.String[list.size()]);

                             }
                             

                        /**
                        * field for EnableUIPageCache
                        */

                        
                                    protected boolean localEnableUIPageCache ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localEnableUIPageCacheTracker = false ;

                           public boolean isEnableUIPageCacheSpecified(){
                               return localEnableUIPageCacheTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getEnableUIPageCache(){
                               return localEnableUIPageCache;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param EnableUIPageCache
                               */
                               public void setEnableUIPageCache(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localEnableUIPageCacheTracker =
                                       true;
                                   
                                            this.localEnableUIPageCache=param;
                                    

                               }
                            

                        /**
                        * field for EveryOneRole
                        */

                        
                                    protected java.lang.String localEveryOneRole ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localEveryOneRoleTracker = false ;

                           public boolean isEveryOneRoleSpecified(){
                               return localEveryOneRoleTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getEveryOneRole(){
                               return localEveryOneRole;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param EveryOneRole
                               */
                               public void setEveryOneRole(java.lang.String param){
                            localEveryOneRoleTracker = true;
                                   
                                            this.localEveryOneRole=param;
                                    

                               }
                            

                        /**
                        * field for MaxItemsPerUIPage
                        */

                        
                                    protected int localMaxItemsPerUIPage ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localMaxItemsPerUIPageTracker = false ;

                           public boolean isMaxItemsPerUIPageSpecified(){
                               return localMaxItemsPerUIPageTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return int
                           */
                           public  int getMaxItemsPerUIPage(){
                               return localMaxItemsPerUIPage;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param MaxItemsPerUIPage
                               */
                               public void setMaxItemsPerUIPage(int param){
                            
                                       // setting primitive attribute tracker to true
                                       localMaxItemsPerUIPageTracker =
                                       param != java.lang.Integer.MIN_VALUE;
                                   
                                            this.localMaxItemsPerUIPage=param;
                                    

                               }
                            

                        /**
                        * field for MaxUIPagesInCache
                        */

                        
                                    protected int localMaxUIPagesInCache ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localMaxUIPagesInCacheTracker = false ;

                           public boolean isMaxUIPagesInCacheSpecified(){
                               return localMaxUIPagesInCacheTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return int
                           */
                           public  int getMaxUIPagesInCache(){
                               return localMaxUIPagesInCache;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param MaxUIPagesInCache
                               */
                               public void setMaxUIPagesInCache(int param){
                            
                                       // setting primitive attribute tracker to true
                                       localMaxUIPagesInCacheTracker =
                                       param != java.lang.Integer.MIN_VALUE;
                                   
                                            this.localMaxUIPagesInCache=param;
                                    

                               }
                            

                        /**
                        * field for MultipleUserStore
                        */

                        
                                    protected boolean localMultipleUserStore ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localMultipleUserStoreTracker = false ;

                           public boolean isMultipleUserStoreSpecified(){
                               return localMultipleUserStoreTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getMultipleUserStore(){
                               return localMultipleUserStore;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param MultipleUserStore
                               */
                               public void setMultipleUserStore(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localMultipleUserStoreTracker =
                                       true;
                                   
                                            this.localMultipleUserStore=param;
                                    

                               }
                            

                        /**
                        * field for PrimaryUserStoreInfo
                        */

                        
                                    protected org.wso2.carbon.user.mgt.stub.types.carbon.UserStoreInfo localPrimaryUserStoreInfo ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPrimaryUserStoreInfoTracker = false ;

                           public boolean isPrimaryUserStoreInfoSpecified(){
                               return localPrimaryUserStoreInfoTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.user.mgt.stub.types.carbon.UserStoreInfo
                           */
                           public  org.wso2.carbon.user.mgt.stub.types.carbon.UserStoreInfo getPrimaryUserStoreInfo(){
                               return localPrimaryUserStoreInfo;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PrimaryUserStoreInfo
                               */
                               public void setPrimaryUserStoreInfo(org.wso2.carbon.user.mgt.stub.types.carbon.UserStoreInfo param){
                            localPrimaryUserStoreInfoTracker = true;
                                   
                                            this.localPrimaryUserStoreInfo=param;
                                    

                               }
                            

                        /**
                        * field for RequiredUserClaims
                        * This was an Array!
                        */

                        
                                    protected java.lang.String[] localRequiredUserClaims ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localRequiredUserClaimsTracker = false ;

                           public boolean isRequiredUserClaimsSpecified(){
                               return localRequiredUserClaimsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String[]
                           */
                           public  java.lang.String[] getRequiredUserClaims(){
                               return localRequiredUserClaims;
                           }

                           
                        


                               
                              /**
                               * validate the array for RequiredUserClaims
                               */
                              protected void validateRequiredUserClaims(java.lang.String[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param RequiredUserClaims
                              */
                              public void setRequiredUserClaims(java.lang.String[] param){
                              
                                   validateRequiredUserClaims(param);

                               localRequiredUserClaimsTracker = true;
                                      
                                      this.localRequiredUserClaims=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param java.lang.String
                             */
                             public void addRequiredUserClaims(java.lang.String param){
                                   if (localRequiredUserClaims == null){
                                   localRequiredUserClaims = new java.lang.String[]{};
                                   }

                            
                                 //update the setting tracker
                                localRequiredUserClaimsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localRequiredUserClaims);
                               list.add(param);
                               this.localRequiredUserClaims =
                             (java.lang.String[])list.toArray(
                            new java.lang.String[list.size()]);

                             }
                             

                        /**
                        * field for UserClaims
                        * This was an Array!
                        */

                        
                                    protected java.lang.String[] localUserClaims ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localUserClaimsTracker = false ;

                           public boolean isUserClaimsSpecified(){
                               return localUserClaimsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String[]
                           */
                           public  java.lang.String[] getUserClaims(){
                               return localUserClaims;
                           }

                           
                        


                               
                              /**
                               * validate the array for UserClaims
                               */
                              protected void validateUserClaims(java.lang.String[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param UserClaims
                              */
                              public void setUserClaims(java.lang.String[] param){
                              
                                   validateUserClaims(param);

                               localUserClaimsTracker = true;
                                      
                                      this.localUserClaims=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param java.lang.String
                             */
                             public void addUserClaims(java.lang.String param){
                                   if (localUserClaims == null){
                                   localUserClaims = new java.lang.String[]{};
                                   }

                            
                                 //update the setting tracker
                                localUserClaimsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localUserClaims);
                               list.add(param);
                               this.localUserClaims =
                             (java.lang.String[])list.toArray(
                            new java.lang.String[list.size()]);

                             }
                             

                        /**
                        * field for UserStoresInfo
                        * This was an Array!
                        */

                        
                                    protected org.wso2.carbon.user.mgt.stub.types.carbon.UserStoreInfo[] localUserStoresInfo ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localUserStoresInfoTracker = false ;

                           public boolean isUserStoresInfoSpecified(){
                               return localUserStoresInfoTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.user.mgt.stub.types.carbon.UserStoreInfo[]
                           */
                           public  org.wso2.carbon.user.mgt.stub.types.carbon.UserStoreInfo[] getUserStoresInfo(){
                               return localUserStoresInfo;
                           }

                           
                        


                               
                              /**
                               * validate the array for UserStoresInfo
                               */
                              protected void validateUserStoresInfo(org.wso2.carbon.user.mgt.stub.types.carbon.UserStoreInfo[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param UserStoresInfo
                              */
                              public void setUserStoresInfo(org.wso2.carbon.user.mgt.stub.types.carbon.UserStoreInfo[] param){
                              
                                   validateUserStoresInfo(param);

                               localUserStoresInfoTracker = true;
                                      
                                      this.localUserStoresInfo=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param org.wso2.carbon.user.mgt.stub.types.carbon.UserStoreInfo
                             */
                             public void addUserStoresInfo(org.wso2.carbon.user.mgt.stub.types.carbon.UserStoreInfo param){
                                   if (localUserStoresInfo == null){
                                   localUserStoresInfo = new org.wso2.carbon.user.mgt.stub.types.carbon.UserStoreInfo[]{};
                                   }

                            
                                 //update the setting tracker
                                localUserStoresInfoTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localUserStoresInfo);
                               list.add(param);
                               this.localUserStoresInfo =
                             (org.wso2.carbon.user.mgt.stub.types.carbon.UserStoreInfo[])list.toArray(
                            new org.wso2.carbon.user.mgt.stub.types.carbon.UserStoreInfo[list.size()]);

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
               

                   java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://common.mgt.user.carbon.wso2.org/xsd");
                   if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           namespacePrefix+":UserRealmInfo",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "UserRealmInfo",
                           xmlWriter);
                   }

               
                   }
                if (localAdminRoleTracker){
                                    namespace = "http://common.mgt.user.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "adminRole", xmlWriter);
                             

                                          if (localAdminRole==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAdminRole);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localAdminUserTracker){
                                    namespace = "http://common.mgt.user.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "adminUser", xmlWriter);
                             

                                          if (localAdminUser==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAdminUser);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localBulkImportSupportedTracker){
                                    namespace = "http://common.mgt.user.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "bulkImportSupported", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("bulkImportSupported cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localBulkImportSupported));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localDefaultUserClaimsTracker){
                             if (localDefaultUserClaims!=null) {
                                   namespace = "http://common.mgt.user.carbon.wso2.org/xsd";
                                   for (int i = 0;i < localDefaultUserClaims.length;i++){
                                        
                                            if (localDefaultUserClaims[i] != null){
                                        
                                                writeStartElement(null, namespace, "defaultUserClaims", xmlWriter);

                                            
                                                        xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDefaultUserClaims[i]));
                                                    
                                                xmlWriter.writeEndElement();
                                              
                                                } else {
                                                   
                                                           // write null attribute
                                                            namespace = "http://common.mgt.user.carbon.wso2.org/xsd";
                                                            writeStartElement(null, namespace, "defaultUserClaims", xmlWriter);
                                                            writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                            xmlWriter.writeEndElement();
                                                       
                                                }

                                   }
                             } else {
                                 
                                         // write the null attribute
                                        // write null attribute
                                           writeStartElement(null, "http://common.mgt.user.carbon.wso2.org/xsd", "defaultUserClaims", xmlWriter);

                                           // write the nil attribute
                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                           xmlWriter.writeEndElement();
                                    
                             }

                        } if (localDomainNamesTracker){
                             if (localDomainNames!=null) {
                                   namespace = "http://common.mgt.user.carbon.wso2.org/xsd";
                                   for (int i = 0;i < localDomainNames.length;i++){
                                        
                                            if (localDomainNames[i] != null){
                                        
                                                writeStartElement(null, namespace, "domainNames", xmlWriter);

                                            
                                                        xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDomainNames[i]));
                                                    
                                                xmlWriter.writeEndElement();
                                              
                                                } else {
                                                   
                                                           // write null attribute
                                                            namespace = "http://common.mgt.user.carbon.wso2.org/xsd";
                                                            writeStartElement(null, namespace, "domainNames", xmlWriter);
                                                            writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                            xmlWriter.writeEndElement();
                                                       
                                                }

                                   }
                             } else {
                                 
                                         // write the null attribute
                                        // write null attribute
                                           writeStartElement(null, "http://common.mgt.user.carbon.wso2.org/xsd", "domainNames", xmlWriter);

                                           // write the nil attribute
                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                           xmlWriter.writeEndElement();
                                    
                             }

                        } if (localEnableUIPageCacheTracker){
                                    namespace = "http://common.mgt.user.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "enableUIPageCache", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("enableUIPageCache cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localEnableUIPageCache));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localEveryOneRoleTracker){
                                    namespace = "http://common.mgt.user.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "everyOneRole", xmlWriter);
                             

                                          if (localEveryOneRole==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localEveryOneRole);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localMaxItemsPerUIPageTracker){
                                    namespace = "http://common.mgt.user.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "maxItemsPerUIPage", xmlWriter);
                             
                                               if (localMaxItemsPerUIPage==java.lang.Integer.MIN_VALUE) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("maxItemsPerUIPage cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localMaxItemsPerUIPage));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localMaxUIPagesInCacheTracker){
                                    namespace = "http://common.mgt.user.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "maxUIPagesInCache", xmlWriter);
                             
                                               if (localMaxUIPagesInCache==java.lang.Integer.MIN_VALUE) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("maxUIPagesInCache cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localMaxUIPagesInCache));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localMultipleUserStoreTracker){
                                    namespace = "http://common.mgt.user.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "multipleUserStore", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("multipleUserStore cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localMultipleUserStore));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localPrimaryUserStoreInfoTracker){
                                    if (localPrimaryUserStoreInfo==null){

                                        writeStartElement(null, "http://common.mgt.user.carbon.wso2.org/xsd", "primaryUserStoreInfo", xmlWriter);

                                       // write the nil attribute
                                      writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                      xmlWriter.writeEndElement();
                                    }else{
                                     localPrimaryUserStoreInfo.serialize(new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd","primaryUserStoreInfo"),
                                        xmlWriter);
                                    }
                                } if (localRequiredUserClaimsTracker){
                             if (localRequiredUserClaims!=null) {
                                   namespace = "http://common.mgt.user.carbon.wso2.org/xsd";
                                   for (int i = 0;i < localRequiredUserClaims.length;i++){
                                        
                                            if (localRequiredUserClaims[i] != null){
                                        
                                                writeStartElement(null, namespace, "requiredUserClaims", xmlWriter);

                                            
                                                        xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localRequiredUserClaims[i]));
                                                    
                                                xmlWriter.writeEndElement();
                                              
                                                } else {
                                                   
                                                           // write null attribute
                                                            namespace = "http://common.mgt.user.carbon.wso2.org/xsd";
                                                            writeStartElement(null, namespace, "requiredUserClaims", xmlWriter);
                                                            writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                            xmlWriter.writeEndElement();
                                                       
                                                }

                                   }
                             } else {
                                 
                                         // write the null attribute
                                        // write null attribute
                                           writeStartElement(null, "http://common.mgt.user.carbon.wso2.org/xsd", "requiredUserClaims", xmlWriter);

                                           // write the nil attribute
                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                           xmlWriter.writeEndElement();
                                    
                             }

                        } if (localUserClaimsTracker){
                             if (localUserClaims!=null) {
                                   namespace = "http://common.mgt.user.carbon.wso2.org/xsd";
                                   for (int i = 0;i < localUserClaims.length;i++){
                                        
                                            if (localUserClaims[i] != null){
                                        
                                                writeStartElement(null, namespace, "userClaims", xmlWriter);

                                            
                                                        xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localUserClaims[i]));
                                                    
                                                xmlWriter.writeEndElement();
                                              
                                                } else {
                                                   
                                                           // write null attribute
                                                            namespace = "http://common.mgt.user.carbon.wso2.org/xsd";
                                                            writeStartElement(null, namespace, "userClaims", xmlWriter);
                                                            writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                            xmlWriter.writeEndElement();
                                                       
                                                }

                                   }
                             } else {
                                 
                                         // write the null attribute
                                        // write null attribute
                                           writeStartElement(null, "http://common.mgt.user.carbon.wso2.org/xsd", "userClaims", xmlWriter);

                                           // write the nil attribute
                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                           xmlWriter.writeEndElement();
                                    
                             }

                        } if (localUserStoresInfoTracker){
                                       if (localUserStoresInfo!=null){
                                            for (int i = 0;i < localUserStoresInfo.length;i++){
                                                if (localUserStoresInfo[i] != null){
                                                 localUserStoresInfo[i].serialize(new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd","userStoresInfo"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://common.mgt.user.carbon.wso2.org/xsd", "userStoresInfo", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://common.mgt.user.carbon.wso2.org/xsd", "userStoresInfo", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 }
                    xmlWriter.writeEndElement();
               

        }

        private static java.lang.String generatePrefix(java.lang.String namespace) {
            if(namespace.equals("http://common.mgt.user.carbon.wso2.org/xsd")){
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

                 if (localAdminRoleTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd",
                                                                      "adminRole"));
                                 
                                         elementList.add(localAdminRole==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAdminRole));
                                    } if (localAdminUserTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd",
                                                                      "adminUser"));
                                 
                                         elementList.add(localAdminUser==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAdminUser));
                                    } if (localBulkImportSupportedTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd",
                                                                      "bulkImportSupported"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localBulkImportSupported));
                            } if (localDefaultUserClaimsTracker){
                            if (localDefaultUserClaims!=null){
                                  for (int i = 0;i < localDefaultUserClaims.length;i++){
                                      
                                         if (localDefaultUserClaims[i] != null){
                                          elementList.add(new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd",
                                                                              "defaultUserClaims"));
                                          elementList.add(
                                          org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDefaultUserClaims[i]));
                                          } else {
                                             
                                                    elementList.add(new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd",
                                                                              "defaultUserClaims"));
                                                    elementList.add(null);
                                                
                                          }
                                      

                                  }
                            } else {
                              
                                    elementList.add(new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd",
                                                                              "defaultUserClaims"));
                                    elementList.add(null);
                                
                            }

                        } if (localDomainNamesTracker){
                            if (localDomainNames!=null){
                                  for (int i = 0;i < localDomainNames.length;i++){
                                      
                                         if (localDomainNames[i] != null){
                                          elementList.add(new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd",
                                                                              "domainNames"));
                                          elementList.add(
                                          org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDomainNames[i]));
                                          } else {
                                             
                                                    elementList.add(new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd",
                                                                              "domainNames"));
                                                    elementList.add(null);
                                                
                                          }
                                      

                                  }
                            } else {
                              
                                    elementList.add(new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd",
                                                                              "domainNames"));
                                    elementList.add(null);
                                
                            }

                        } if (localEnableUIPageCacheTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd",
                                                                      "enableUIPageCache"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localEnableUIPageCache));
                            } if (localEveryOneRoleTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd",
                                                                      "everyOneRole"));
                                 
                                         elementList.add(localEveryOneRole==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localEveryOneRole));
                                    } if (localMaxItemsPerUIPageTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd",
                                                                      "maxItemsPerUIPage"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localMaxItemsPerUIPage));
                            } if (localMaxUIPagesInCacheTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd",
                                                                      "maxUIPagesInCache"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localMaxUIPagesInCache));
                            } if (localMultipleUserStoreTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd",
                                                                      "multipleUserStore"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localMultipleUserStore));
                            } if (localPrimaryUserStoreInfoTracker){
                            elementList.add(new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd",
                                                                      "primaryUserStoreInfo"));
                            
                            
                                    elementList.add(localPrimaryUserStoreInfo==null?null:
                                    localPrimaryUserStoreInfo);
                                } if (localRequiredUserClaimsTracker){
                            if (localRequiredUserClaims!=null){
                                  for (int i = 0;i < localRequiredUserClaims.length;i++){
                                      
                                         if (localRequiredUserClaims[i] != null){
                                          elementList.add(new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd",
                                                                              "requiredUserClaims"));
                                          elementList.add(
                                          org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localRequiredUserClaims[i]));
                                          } else {
                                             
                                                    elementList.add(new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd",
                                                                              "requiredUserClaims"));
                                                    elementList.add(null);
                                                
                                          }
                                      

                                  }
                            } else {
                              
                                    elementList.add(new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd",
                                                                              "requiredUserClaims"));
                                    elementList.add(null);
                                
                            }

                        } if (localUserClaimsTracker){
                            if (localUserClaims!=null){
                                  for (int i = 0;i < localUserClaims.length;i++){
                                      
                                         if (localUserClaims[i] != null){
                                          elementList.add(new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd",
                                                                              "userClaims"));
                                          elementList.add(
                                          org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localUserClaims[i]));
                                          } else {
                                             
                                                    elementList.add(new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd",
                                                                              "userClaims"));
                                                    elementList.add(null);
                                                
                                          }
                                      

                                  }
                            } else {
                              
                                    elementList.add(new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd",
                                                                              "userClaims"));
                                    elementList.add(null);
                                
                            }

                        } if (localUserStoresInfoTracker){
                             if (localUserStoresInfo!=null) {
                                 for (int i = 0;i < localUserStoresInfo.length;i++){

                                    if (localUserStoresInfo[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd",
                                                                          "userStoresInfo"));
                                         elementList.add(localUserStoresInfo[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd",
                                                                          "userStoresInfo"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd",
                                                                          "userStoresInfo"));
                                        elementList.add(localUserStoresInfo);
                                    
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
        public static UserRealmInfo parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            UserRealmInfo object =
                new UserRealmInfo();

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
                    
                            if (!"UserRealmInfo".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (UserRealmInfo)org.wso2.carbon.user.api.xsd.ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }
                        

                  }
                

                }

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                
                    
                    reader.next();
                
                        java.util.ArrayList list4 = new java.util.ArrayList();
                    
                        java.util.ArrayList list5 = new java.util.ArrayList();
                    
                        java.util.ArrayList list12 = new java.util.ArrayList();
                    
                        java.util.ArrayList list13 = new java.util.ArrayList();
                    
                        java.util.ArrayList list14 = new java.util.ArrayList();
                    
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd","adminRole").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setAdminRole(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd","adminUser").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setAdminUser(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd","bulkImportSupported").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"bulkImportSupported" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setBulkImportSupported(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd","defaultUserClaims").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                              nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                              if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                  list4.add(null);
                                                       
                                                  reader.next();
                                              } else {
                                            list4.add(reader.getElementText());
                                            }
                                            //loop until we find a start element that is not part of this array
                                            boolean loopDone4 = false;
                                            while(!loopDone4){
                                                // Ensure we are at the EndElement
                                                while (!reader.isEndElement()){
                                                    reader.next();
                                                }
                                                // Step out of this element
                                                reader.next();
                                                // Step to next element event.
                                                while (!reader.isStartElement() && !reader.isEndElement())
                                                    reader.next();
                                                if (reader.isEndElement()){
                                                    //two continuous end elements means we are exiting the xml structure
                                                    loopDone4 = true;
                                                } else {
                                                    if (new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd","defaultUserClaims").equals(reader.getName())){
                                                         
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list4.add(null);
                                                                   
                                                              reader.next();
                                                          } else {
                                                        list4.add(reader.getElementText());
                                                        }
                                                    }else{
                                                        loopDone4 = true;
                                                    }
                                                }
                                            }
                                            // call the converter utility  to convert and set the array
                                            
                                                    object.setDefaultUserClaims((java.lang.String[])
                                                        list4.toArray(new java.lang.String[list4.size()]));
                                                
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd","domainNames").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                              nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                              if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                  list5.add(null);
                                                       
                                                  reader.next();
                                              } else {
                                            list5.add(reader.getElementText());
                                            }
                                            //loop until we find a start element that is not part of this array
                                            boolean loopDone5 = false;
                                            while(!loopDone5){
                                                // Ensure we are at the EndElement
                                                while (!reader.isEndElement()){
                                                    reader.next();
                                                }
                                                // Step out of this element
                                                reader.next();
                                                // Step to next element event.
                                                while (!reader.isStartElement() && !reader.isEndElement())
                                                    reader.next();
                                                if (reader.isEndElement()){
                                                    //two continuous end elements means we are exiting the xml structure
                                                    loopDone5 = true;
                                                } else {
                                                    if (new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd","domainNames").equals(reader.getName())){
                                                         
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list5.add(null);
                                                                   
                                                              reader.next();
                                                          } else {
                                                        list5.add(reader.getElementText());
                                                        }
                                                    }else{
                                                        loopDone5 = true;
                                                    }
                                                }
                                            }
                                            // call the converter utility  to convert and set the array
                                            
                                                    object.setDomainNames((java.lang.String[])
                                                        list5.toArray(new java.lang.String[list5.size()]));
                                                
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd","enableUIPageCache").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"enableUIPageCache" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setEnableUIPageCache(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd","everyOneRole").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setEveryOneRole(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd","maxItemsPerUIPage").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"maxItemsPerUIPage" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setMaxItemsPerUIPage(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                               object.setMaxItemsPerUIPage(java.lang.Integer.MIN_VALUE);
                                           
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd","maxUIPagesInCache").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"maxUIPagesInCache" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setMaxUIPagesInCache(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                               object.setMaxUIPagesInCache(java.lang.Integer.MIN_VALUE);
                                           
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd","multipleUserStore").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"multipleUserStore" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setMultipleUserStore(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd","primaryUserStoreInfo").equals(reader.getName())){
                                
                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                          object.setPrimaryUserStoreInfo(null);
                                          reader.next();
                                            
                                            reader.next();
                                          
                                      }else{
                                    
                                                object.setPrimaryUserStoreInfo(org.wso2.carbon.user.mgt.stub.types.carbon.UserStoreInfo.Factory.parse(reader));
                                              
                                        reader.next();
                                    }
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd","requiredUserClaims").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                              nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                              if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                  list12.add(null);
                                                       
                                                  reader.next();
                                              } else {
                                            list12.add(reader.getElementText());
                                            }
                                            //loop until we find a start element that is not part of this array
                                            boolean loopDone12 = false;
                                            while(!loopDone12){
                                                // Ensure we are at the EndElement
                                                while (!reader.isEndElement()){
                                                    reader.next();
                                                }
                                                // Step out of this element
                                                reader.next();
                                                // Step to next element event.
                                                while (!reader.isStartElement() && !reader.isEndElement())
                                                    reader.next();
                                                if (reader.isEndElement()){
                                                    //two continuous end elements means we are exiting the xml structure
                                                    loopDone12 = true;
                                                } else {
                                                    if (new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd","requiredUserClaims").equals(reader.getName())){
                                                         
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list12.add(null);
                                                                   
                                                              reader.next();
                                                          } else {
                                                        list12.add(reader.getElementText());
                                                        }
                                                    }else{
                                                        loopDone12 = true;
                                                    }
                                                }
                                            }
                                            // call the converter utility  to convert and set the array
                                            
                                                    object.setRequiredUserClaims((java.lang.String[])
                                                        list12.toArray(new java.lang.String[list12.size()]));
                                                
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd","userClaims").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                              nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                              if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                  list13.add(null);
                                                       
                                                  reader.next();
                                              } else {
                                            list13.add(reader.getElementText());
                                            }
                                            //loop until we find a start element that is not part of this array
                                            boolean loopDone13 = false;
                                            while(!loopDone13){
                                                // Ensure we are at the EndElement
                                                while (!reader.isEndElement()){
                                                    reader.next();
                                                }
                                                // Step out of this element
                                                reader.next();
                                                // Step to next element event.
                                                while (!reader.isStartElement() && !reader.isEndElement())
                                                    reader.next();
                                                if (reader.isEndElement()){
                                                    //two continuous end elements means we are exiting the xml structure
                                                    loopDone13 = true;
                                                } else {
                                                    if (new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd","userClaims").equals(reader.getName())){
                                                         
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list13.add(null);
                                                                   
                                                              reader.next();
                                                          } else {
                                                        list13.add(reader.getElementText());
                                                        }
                                                    }else{
                                                        loopDone13 = true;
                                                    }
                                                }
                                            }
                                            // call the converter utility  to convert and set the array
                                            
                                                    object.setUserClaims((java.lang.String[])
                                                        list13.toArray(new java.lang.String[list13.size()]));
                                                
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd","userStoresInfo").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list14.add(null);
                                                              reader.next();
                                                          } else {
                                                        list14.add(org.wso2.carbon.user.mgt.stub.types.carbon.UserStoreInfo.Factory.parse(reader));
                                                                }
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone14 = false;
                                                        while(!loopDone14){
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
                                                                loopDone14 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd","userStoresInfo").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list14.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list14.add(org.wso2.carbon.user.mgt.stub.types.carbon.UserStoreInfo.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone14 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setUserStoresInfo((org.wso2.carbon.user.mgt.stub.types.carbon.UserStoreInfo[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                org.wso2.carbon.user.mgt.stub.types.carbon.UserStoreInfo.class,
                                                                list14));
                                                            
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
           
    