
/**
 * UserStoreInfo.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:46 UTC)
 */

            
                package org.wso2.carbon.user.mgt.stub.types.carbon;
            

            /**
            *  UserStoreInfo bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class UserStoreInfo
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = UserStoreInfo
                Namespace URI = http://common.mgt.user.carbon.wso2.org/xsd
                Namespace Prefix = ns1
                */
            

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
                        * field for CaseSensitiveUsername
                        */

                        
                                    protected boolean localCaseSensitiveUsername ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localCaseSensitiveUsernameTracker = false ;

                           public boolean isCaseSensitiveUsernameSpecified(){
                               return localCaseSensitiveUsernameTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getCaseSensitiveUsername(){
                               return localCaseSensitiveUsername;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param CaseSensitiveUsername
                               */
                               public void setCaseSensitiveUsername(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localCaseSensitiveUsernameTracker =
                                       true;
                                   
                                            this.localCaseSensitiveUsername=param;
                                    

                               }
                            

                        /**
                        * field for DomainName
                        */

                        
                                    protected java.lang.String localDomainName ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localDomainNameTracker = false ;

                           public boolean isDomainNameSpecified(){
                               return localDomainNameTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getDomainName(){
                               return localDomainName;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param DomainName
                               */
                               public void setDomainName(java.lang.String param){
                            localDomainNameTracker = true;
                                   
                                            this.localDomainName=param;
                                    

                               }
                            

                        /**
                        * field for ExternalIdP
                        */

                        
                                    protected java.lang.String localExternalIdP ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localExternalIdPTracker = false ;

                           public boolean isExternalIdPSpecified(){
                               return localExternalIdPTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getExternalIdP(){
                               return localExternalIdP;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ExternalIdP
                               */
                               public void setExternalIdP(java.lang.String param){
                            localExternalIdPTracker = true;
                                   
                                            this.localExternalIdP=param;
                                    

                               }
                            

                        /**
                        * field for MaxRoleLimit
                        */

                        
                                    protected int localMaxRoleLimit ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localMaxRoleLimitTracker = false ;

                           public boolean isMaxRoleLimitSpecified(){
                               return localMaxRoleLimitTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return int
                           */
                           public  int getMaxRoleLimit(){
                               return localMaxRoleLimit;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param MaxRoleLimit
                               */
                               public void setMaxRoleLimit(int param){
                            
                                       // setting primitive attribute tracker to true
                                       localMaxRoleLimitTracker =
                                       param != java.lang.Integer.MIN_VALUE;
                                   
                                            this.localMaxRoleLimit=param;
                                    

                               }
                            

                        /**
                        * field for MaxUserLimit
                        */

                        
                                    protected int localMaxUserLimit ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localMaxUserLimitTracker = false ;

                           public boolean isMaxUserLimitSpecified(){
                               return localMaxUserLimitTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return int
                           */
                           public  int getMaxUserLimit(){
                               return localMaxUserLimit;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param MaxUserLimit
                               */
                               public void setMaxUserLimit(int param){
                            
                                       // setting primitive attribute tracker to true
                                       localMaxUserLimitTracker =
                                       param != java.lang.Integer.MIN_VALUE;
                                   
                                            this.localMaxUserLimit=param;
                                    

                               }
                            

                        /**
                        * field for PasswordRegEx
                        */

                        
                                    protected java.lang.String localPasswordRegEx ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPasswordRegExTracker = false ;

                           public boolean isPasswordRegExSpecified(){
                               return localPasswordRegExTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getPasswordRegEx(){
                               return localPasswordRegEx;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PasswordRegEx
                               */
                               public void setPasswordRegEx(java.lang.String param){
                            localPasswordRegExTracker = true;
                                   
                                            this.localPasswordRegEx=param;
                                    

                               }
                            

                        /**
                        * field for PasswordRegExViolationErrorMsg
                        */

                        
                                    protected java.lang.String localPasswordRegExViolationErrorMsg ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPasswordRegExViolationErrorMsgTracker = false ;

                           public boolean isPasswordRegExViolationErrorMsgSpecified(){
                               return localPasswordRegExViolationErrorMsgTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getPasswordRegExViolationErrorMsg(){
                               return localPasswordRegExViolationErrorMsg;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PasswordRegExViolationErrorMsg
                               */
                               public void setPasswordRegExViolationErrorMsg(java.lang.String param){
                            localPasswordRegExViolationErrorMsgTracker = true;
                                   
                                            this.localPasswordRegExViolationErrorMsg=param;
                                    

                               }
                            

                        /**
                        * field for PasswordsExternallyManaged
                        */

                        
                                    protected boolean localPasswordsExternallyManaged ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPasswordsExternallyManagedTracker = false ;

                           public boolean isPasswordsExternallyManagedSpecified(){
                               return localPasswordsExternallyManagedTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getPasswordsExternallyManaged(){
                               return localPasswordsExternallyManaged;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PasswordsExternallyManaged
                               */
                               public void setPasswordsExternallyManaged(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localPasswordsExternallyManagedTracker =
                                       true;
                                   
                                            this.localPasswordsExternallyManaged=param;
                                    

                               }
                            

                        /**
                        * field for ReadGroupsEnabled
                        */

                        
                                    protected boolean localReadGroupsEnabled ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localReadGroupsEnabledTracker = false ;

                           public boolean isReadGroupsEnabledSpecified(){
                               return localReadGroupsEnabledTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getReadGroupsEnabled(){
                               return localReadGroupsEnabled;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ReadGroupsEnabled
                               */
                               public void setReadGroupsEnabled(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localReadGroupsEnabledTracker =
                                       true;
                                   
                                            this.localReadGroupsEnabled=param;
                                    

                               }
                            

                        /**
                        * field for ReadOnly
                        */

                        
                                    protected boolean localReadOnly ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localReadOnlyTracker = false ;

                           public boolean isReadOnlySpecified(){
                               return localReadOnlyTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getReadOnly(){
                               return localReadOnly;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ReadOnly
                               */
                               public void setReadOnly(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localReadOnlyTracker =
                                       true;
                                   
                                            this.localReadOnly=param;
                                    

                               }
                            

                        /**
                        * field for RoleNameRegEx
                        */

                        
                                    protected java.lang.String localRoleNameRegEx ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localRoleNameRegExTracker = false ;

                           public boolean isRoleNameRegExSpecified(){
                               return localRoleNameRegExTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getRoleNameRegEx(){
                               return localRoleNameRegEx;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param RoleNameRegEx
                               */
                               public void setRoleNameRegEx(java.lang.String param){
                            localRoleNameRegExTracker = true;
                                   
                                            this.localRoleNameRegEx=param;
                                    

                               }
                            

                        /**
                        * field for UserNameRegEx
                        */

                        
                                    protected java.lang.String localUserNameRegEx ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localUserNameRegExTracker = false ;

                           public boolean isUserNameRegExSpecified(){
                               return localUserNameRegExTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getUserNameRegEx(){
                               return localUserNameRegEx;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param UserNameRegEx
                               */
                               public void setUserNameRegEx(java.lang.String param){
                            localUserNameRegExTracker = true;
                                   
                                            this.localUserNameRegEx=param;
                                    

                               }
                            

                        /**
                        * field for UsernameRegExViolationErrorMsg
                        */

                        
                                    protected java.lang.String localUsernameRegExViolationErrorMsg ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localUsernameRegExViolationErrorMsgTracker = false ;

                           public boolean isUsernameRegExViolationErrorMsgSpecified(){
                               return localUsernameRegExViolationErrorMsgTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getUsernameRegExViolationErrorMsg(){
                               return localUsernameRegExViolationErrorMsg;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param UsernameRegExViolationErrorMsg
                               */
                               public void setUsernameRegExViolationErrorMsg(java.lang.String param){
                            localUsernameRegExViolationErrorMsgTracker = true;
                                   
                                            this.localUsernameRegExViolationErrorMsg=param;
                                    

                               }
                            

                        /**
                        * field for WriteGroupsEnabled
                        */

                        
                                    protected boolean localWriteGroupsEnabled ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localWriteGroupsEnabledTracker = false ;

                           public boolean isWriteGroupsEnabledSpecified(){
                               return localWriteGroupsEnabledTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getWriteGroupsEnabled(){
                               return localWriteGroupsEnabled;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param WriteGroupsEnabled
                               */
                               public void setWriteGroupsEnabled(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localWriteGroupsEnabledTracker =
                                       true;
                                   
                                            this.localWriteGroupsEnabled=param;
                                    

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
                           namespacePrefix+":UserStoreInfo",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "UserStoreInfo",
                           xmlWriter);
                   }

               
                   }
                if (localBulkImportSupportedTracker){
                                    namespace = "http://common.mgt.user.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "bulkImportSupported", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("bulkImportSupported cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localBulkImportSupported));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localCaseSensitiveUsernameTracker){
                                    namespace = "http://common.mgt.user.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "caseSensitiveUsername", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("caseSensitiveUsername cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCaseSensitiveUsername));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localDomainNameTracker){
                                    namespace = "http://common.mgt.user.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "domainName", xmlWriter);
                             

                                          if (localDomainName==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localDomainName);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localExternalIdPTracker){
                                    namespace = "http://common.mgt.user.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "externalIdP", xmlWriter);
                             

                                          if (localExternalIdP==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localExternalIdP);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localMaxRoleLimitTracker){
                                    namespace = "http://common.mgt.user.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "maxRoleLimit", xmlWriter);
                             
                                               if (localMaxRoleLimit==java.lang.Integer.MIN_VALUE) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("maxRoleLimit cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localMaxRoleLimit));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localMaxUserLimitTracker){
                                    namespace = "http://common.mgt.user.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "maxUserLimit", xmlWriter);
                             
                                               if (localMaxUserLimit==java.lang.Integer.MIN_VALUE) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("maxUserLimit cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localMaxUserLimit));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localPasswordRegExTracker){
                                    namespace = "http://common.mgt.user.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "passwordRegEx", xmlWriter);
                             

                                          if (localPasswordRegEx==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localPasswordRegEx);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localPasswordRegExViolationErrorMsgTracker){
                                    namespace = "http://common.mgt.user.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "passwordRegExViolationErrorMsg", xmlWriter);
                             

                                          if (localPasswordRegExViolationErrorMsg==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localPasswordRegExViolationErrorMsg);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localPasswordsExternallyManagedTracker){
                                    namespace = "http://common.mgt.user.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "passwordsExternallyManaged", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("passwordsExternallyManaged cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPasswordsExternallyManaged));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localReadGroupsEnabledTracker){
                                    namespace = "http://common.mgt.user.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "readGroupsEnabled", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("readGroupsEnabled cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localReadGroupsEnabled));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localReadOnlyTracker){
                                    namespace = "http://common.mgt.user.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "readOnly", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("readOnly cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localReadOnly));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localRoleNameRegExTracker){
                                    namespace = "http://common.mgt.user.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "roleNameRegEx", xmlWriter);
                             

                                          if (localRoleNameRegEx==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localRoleNameRegEx);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localUserNameRegExTracker){
                                    namespace = "http://common.mgt.user.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "userNameRegEx", xmlWriter);
                             

                                          if (localUserNameRegEx==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localUserNameRegEx);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localUsernameRegExViolationErrorMsgTracker){
                                    namespace = "http://common.mgt.user.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "usernameRegExViolationErrorMsg", xmlWriter);
                             

                                          if (localUsernameRegExViolationErrorMsg==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localUsernameRegExViolationErrorMsg);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localWriteGroupsEnabledTracker){
                                    namespace = "http://common.mgt.user.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "writeGroupsEnabled", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("writeGroupsEnabled cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localWriteGroupsEnabled));
                                               }
                                    
                                   xmlWriter.writeEndElement();
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

                 if (localBulkImportSupportedTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd",
                                                                      "bulkImportSupported"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localBulkImportSupported));
                            } if (localCaseSensitiveUsernameTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd",
                                                                      "caseSensitiveUsername"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCaseSensitiveUsername));
                            } if (localDomainNameTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd",
                                                                      "domainName"));
                                 
                                         elementList.add(localDomainName==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDomainName));
                                    } if (localExternalIdPTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd",
                                                                      "externalIdP"));
                                 
                                         elementList.add(localExternalIdP==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localExternalIdP));
                                    } if (localMaxRoleLimitTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd",
                                                                      "maxRoleLimit"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localMaxRoleLimit));
                            } if (localMaxUserLimitTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd",
                                                                      "maxUserLimit"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localMaxUserLimit));
                            } if (localPasswordRegExTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd",
                                                                      "passwordRegEx"));
                                 
                                         elementList.add(localPasswordRegEx==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPasswordRegEx));
                                    } if (localPasswordRegExViolationErrorMsgTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd",
                                                                      "passwordRegExViolationErrorMsg"));
                                 
                                         elementList.add(localPasswordRegExViolationErrorMsg==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPasswordRegExViolationErrorMsg));
                                    } if (localPasswordsExternallyManagedTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd",
                                                                      "passwordsExternallyManaged"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPasswordsExternallyManaged));
                            } if (localReadGroupsEnabledTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd",
                                                                      "readGroupsEnabled"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localReadGroupsEnabled));
                            } if (localReadOnlyTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd",
                                                                      "readOnly"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localReadOnly));
                            } if (localRoleNameRegExTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd",
                                                                      "roleNameRegEx"));
                                 
                                         elementList.add(localRoleNameRegEx==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localRoleNameRegEx));
                                    } if (localUserNameRegExTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd",
                                                                      "userNameRegEx"));
                                 
                                         elementList.add(localUserNameRegEx==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localUserNameRegEx));
                                    } if (localUsernameRegExViolationErrorMsgTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd",
                                                                      "usernameRegExViolationErrorMsg"));
                                 
                                         elementList.add(localUsernameRegExViolationErrorMsg==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localUsernameRegExViolationErrorMsg));
                                    } if (localWriteGroupsEnabledTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd",
                                                                      "writeGroupsEnabled"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localWriteGroupsEnabled));
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
        public static UserStoreInfo parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            UserStoreInfo object =
                new UserStoreInfo();

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
                    
                            if (!"UserStoreInfo".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (UserStoreInfo)org.wso2.carbon.user.api.xsd.ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }
                        

                  }
                

                }

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                
                    
                    reader.next();
                
                                    
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd","caseSensitiveUsername").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"caseSensitiveUsername" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setCaseSensitiveUsername(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd","domainName").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setDomainName(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd","externalIdP").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setExternalIdP(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd","maxRoleLimit").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"maxRoleLimit" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setMaxRoleLimit(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                               object.setMaxRoleLimit(java.lang.Integer.MIN_VALUE);
                                           
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd","maxUserLimit").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"maxUserLimit" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setMaxUserLimit(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                               object.setMaxUserLimit(java.lang.Integer.MIN_VALUE);
                                           
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd","passwordRegEx").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setPasswordRegEx(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd","passwordRegExViolationErrorMsg").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setPasswordRegExViolationErrorMsg(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd","passwordsExternallyManaged").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"passwordsExternallyManaged" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setPasswordsExternallyManaged(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd","readGroupsEnabled").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"readGroupsEnabled" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setReadGroupsEnabled(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd","readOnly").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"readOnly" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setReadOnly(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd","roleNameRegEx").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setRoleNameRegEx(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd","userNameRegEx").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setUserNameRegEx(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd","usernameRegExViolationErrorMsg").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setUsernameRegExViolationErrorMsg(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://common.mgt.user.carbon.wso2.org/xsd","writeGroupsEnabled").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"writeGroupsEnabled" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setWriteGroupsEnabled(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
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
           
    