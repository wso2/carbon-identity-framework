
/**
 * PolicyDTO.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:46 UTC)
 */

            
                package org.wso2.carbon.identity.entitlement.stub.dto;
            

            /**
            *  PolicyDTO bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class PolicyDTO
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = PolicyDTO
                Namespace URI = http://dto.entitlement.identity.carbon.wso2.org/xsd
                Namespace Prefix = ns1
                */
            

                        /**
                        * field for Active
                        */

                        
                                    protected boolean localActive ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localActiveTracker = false ;

                           public boolean isActiveSpecified(){
                               return localActiveTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getActive(){
                               return localActive;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Active
                               */
                               public void setActive(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localActiveTracker =
                                       true;
                                   
                                            this.localActive=param;
                                    

                               }
                            

                        /**
                        * field for AttributeDTOs
                        * This was an Array!
                        */

                        
                                    protected org.wso2.carbon.identity.entitlement.stub.dto.AttributeDTO[] localAttributeDTOs ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAttributeDTOsTracker = false ;

                           public boolean isAttributeDTOsSpecified(){
                               return localAttributeDTOsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.identity.entitlement.stub.dto.AttributeDTO[]
                           */
                           public  org.wso2.carbon.identity.entitlement.stub.dto.AttributeDTO[] getAttributeDTOs(){
                               return localAttributeDTOs;
                           }

                           
                        


                               
                              /**
                               * validate the array for AttributeDTOs
                               */
                              protected void validateAttributeDTOs(org.wso2.carbon.identity.entitlement.stub.dto.AttributeDTO[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param AttributeDTOs
                              */
                              public void setAttributeDTOs(org.wso2.carbon.identity.entitlement.stub.dto.AttributeDTO[] param){
                              
                                   validateAttributeDTOs(param);

                               localAttributeDTOsTracker = true;
                                      
                                      this.localAttributeDTOs=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param org.wso2.carbon.identity.entitlement.stub.dto.AttributeDTO
                             */
                             public void addAttributeDTOs(org.wso2.carbon.identity.entitlement.stub.dto.AttributeDTO param){
                                   if (localAttributeDTOs == null){
                                   localAttributeDTOs = new org.wso2.carbon.identity.entitlement.stub.dto.AttributeDTO[]{};
                                   }

                            
                                 //update the setting tracker
                                localAttributeDTOsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localAttributeDTOs);
                               list.add(param);
                               this.localAttributeDTOs =
                             (org.wso2.carbon.identity.entitlement.stub.dto.AttributeDTO[])list.toArray(
                            new org.wso2.carbon.identity.entitlement.stub.dto.AttributeDTO[list.size()]);

                             }
                             

                        /**
                        * field for LastModifiedTime
                        */

                        
                                    protected java.lang.String localLastModifiedTime ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localLastModifiedTimeTracker = false ;

                           public boolean isLastModifiedTimeSpecified(){
                               return localLastModifiedTimeTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getLastModifiedTime(){
                               return localLastModifiedTime;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param LastModifiedTime
                               */
                               public void setLastModifiedTime(java.lang.String param){
                            localLastModifiedTimeTracker = true;
                                   
                                            this.localLastModifiedTime=param;
                                    

                               }
                            

                        /**
                        * field for LastModifiedUser
                        */

                        
                                    protected java.lang.String localLastModifiedUser ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localLastModifiedUserTracker = false ;

                           public boolean isLastModifiedUserSpecified(){
                               return localLastModifiedUserTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getLastModifiedUser(){
                               return localLastModifiedUser;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param LastModifiedUser
                               */
                               public void setLastModifiedUser(java.lang.String param){
                            localLastModifiedUserTracker = true;
                                   
                                            this.localLastModifiedUser=param;
                                    

                               }
                            

                        /**
                        * field for Policy
                        */

                        
                                    protected java.lang.String localPolicy ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPolicyTracker = false ;

                           public boolean isPolicySpecified(){
                               return localPolicyTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getPolicy(){
                               return localPolicy;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Policy
                               */
                               public void setPolicy(java.lang.String param){
                            localPolicyTracker = true;
                                   
                                            this.localPolicy=param;
                                    

                               }
                            

                        /**
                        * field for PolicyEditor
                        */

                        
                                    protected java.lang.String localPolicyEditor ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPolicyEditorTracker = false ;

                           public boolean isPolicyEditorSpecified(){
                               return localPolicyEditorTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getPolicyEditor(){
                               return localPolicyEditor;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PolicyEditor
                               */
                               public void setPolicyEditor(java.lang.String param){
                            localPolicyEditorTracker = true;
                                   
                                            this.localPolicyEditor=param;
                                    

                               }
                            

                        /**
                        * field for PolicyEditorData
                        * This was an Array!
                        */

                        
                                    protected java.lang.String[] localPolicyEditorData ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPolicyEditorDataTracker = false ;

                           public boolean isPolicyEditorDataSpecified(){
                               return localPolicyEditorDataTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String[]
                           */
                           public  java.lang.String[] getPolicyEditorData(){
                               return localPolicyEditorData;
                           }

                           
                        


                               
                              /**
                               * validate the array for PolicyEditorData
                               */
                              protected void validatePolicyEditorData(java.lang.String[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param PolicyEditorData
                              */
                              public void setPolicyEditorData(java.lang.String[] param){
                              
                                   validatePolicyEditorData(param);

                               localPolicyEditorDataTracker = true;
                                      
                                      this.localPolicyEditorData=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param java.lang.String
                             */
                             public void addPolicyEditorData(java.lang.String param){
                                   if (localPolicyEditorData == null){
                                   localPolicyEditorData = new java.lang.String[]{};
                                   }

                            
                                 //update the setting tracker
                                localPolicyEditorDataTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localPolicyEditorData);
                               list.add(param);
                               this.localPolicyEditorData =
                             (java.lang.String[])list.toArray(
                            new java.lang.String[list.size()]);

                             }
                             

                        /**
                        * field for PolicyId
                        */

                        
                                    protected java.lang.String localPolicyId ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPolicyIdTracker = false ;

                           public boolean isPolicyIdSpecified(){
                               return localPolicyIdTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getPolicyId(){
                               return localPolicyId;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PolicyId
                               */
                               public void setPolicyId(java.lang.String param){
                            localPolicyIdTracker = true;
                                   
                                            this.localPolicyId=param;
                                    

                               }
                            

                        /**
                        * field for PolicyIdReferences
                        * This was an Array!
                        */

                        
                                    protected java.lang.String[] localPolicyIdReferences ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPolicyIdReferencesTracker = false ;

                           public boolean isPolicyIdReferencesSpecified(){
                               return localPolicyIdReferencesTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String[]
                           */
                           public  java.lang.String[] getPolicyIdReferences(){
                               return localPolicyIdReferences;
                           }

                           
                        


                               
                              /**
                               * validate the array for PolicyIdReferences
                               */
                              protected void validatePolicyIdReferences(java.lang.String[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param PolicyIdReferences
                              */
                              public void setPolicyIdReferences(java.lang.String[] param){
                              
                                   validatePolicyIdReferences(param);

                               localPolicyIdReferencesTracker = true;
                                      
                                      this.localPolicyIdReferences=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param java.lang.String
                             */
                             public void addPolicyIdReferences(java.lang.String param){
                                   if (localPolicyIdReferences == null){
                                   localPolicyIdReferences = new java.lang.String[]{};
                                   }

                            
                                 //update the setting tracker
                                localPolicyIdReferencesTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localPolicyIdReferences);
                               list.add(param);
                               this.localPolicyIdReferences =
                             (java.lang.String[])list.toArray(
                            new java.lang.String[list.size()]);

                             }
                             

                        /**
                        * field for PolicyOrder
                        */

                        
                                    protected int localPolicyOrder ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPolicyOrderTracker = false ;

                           public boolean isPolicyOrderSpecified(){
                               return localPolicyOrderTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return int
                           */
                           public  int getPolicyOrder(){
                               return localPolicyOrder;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PolicyOrder
                               */
                               public void setPolicyOrder(int param){
                            
                                       // setting primitive attribute tracker to true
                                       localPolicyOrderTracker =
                                       param != java.lang.Integer.MIN_VALUE;
                                   
                                            this.localPolicyOrder=param;
                                    

                               }
                            

                        /**
                        * field for PolicySetIdReferences
                        * This was an Array!
                        */

                        
                                    protected java.lang.String[] localPolicySetIdReferences ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPolicySetIdReferencesTracker = false ;

                           public boolean isPolicySetIdReferencesSpecified(){
                               return localPolicySetIdReferencesTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String[]
                           */
                           public  java.lang.String[] getPolicySetIdReferences(){
                               return localPolicySetIdReferences;
                           }

                           
                        


                               
                              /**
                               * validate the array for PolicySetIdReferences
                               */
                              protected void validatePolicySetIdReferences(java.lang.String[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param PolicySetIdReferences
                              */
                              public void setPolicySetIdReferences(java.lang.String[] param){
                              
                                   validatePolicySetIdReferences(param);

                               localPolicySetIdReferencesTracker = true;
                                      
                                      this.localPolicySetIdReferences=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param java.lang.String
                             */
                             public void addPolicySetIdReferences(java.lang.String param){
                                   if (localPolicySetIdReferences == null){
                                   localPolicySetIdReferences = new java.lang.String[]{};
                                   }

                            
                                 //update the setting tracker
                                localPolicySetIdReferencesTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localPolicySetIdReferences);
                               list.add(param);
                               this.localPolicySetIdReferences =
                             (java.lang.String[])list.toArray(
                            new java.lang.String[list.size()]);

                             }
                             

                        /**
                        * field for PolicyStatusHolders
                        * This was an Array!
                        */

                        
                                    protected org.wso2.carbon.identity.entitlement.stub.dto.StatusHolder[] localPolicyStatusHolders ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPolicyStatusHoldersTracker = false ;

                           public boolean isPolicyStatusHoldersSpecified(){
                               return localPolicyStatusHoldersTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.identity.entitlement.stub.dto.StatusHolder[]
                           */
                           public  org.wso2.carbon.identity.entitlement.stub.dto.StatusHolder[] getPolicyStatusHolders(){
                               return localPolicyStatusHolders;
                           }

                           
                        


                               
                              /**
                               * validate the array for PolicyStatusHolders
                               */
                              protected void validatePolicyStatusHolders(org.wso2.carbon.identity.entitlement.stub.dto.StatusHolder[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param PolicyStatusHolders
                              */
                              public void setPolicyStatusHolders(org.wso2.carbon.identity.entitlement.stub.dto.StatusHolder[] param){
                              
                                   validatePolicyStatusHolders(param);

                               localPolicyStatusHoldersTracker = true;
                                      
                                      this.localPolicyStatusHolders=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param org.wso2.carbon.identity.entitlement.stub.dto.StatusHolder
                             */
                             public void addPolicyStatusHolders(org.wso2.carbon.identity.entitlement.stub.dto.StatusHolder param){
                                   if (localPolicyStatusHolders == null){
                                   localPolicyStatusHolders = new org.wso2.carbon.identity.entitlement.stub.dto.StatusHolder[]{};
                                   }

                            
                                 //update the setting tracker
                                localPolicyStatusHoldersTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localPolicyStatusHolders);
                               list.add(param);
                               this.localPolicyStatusHolders =
                             (org.wso2.carbon.identity.entitlement.stub.dto.StatusHolder[])list.toArray(
                            new org.wso2.carbon.identity.entitlement.stub.dto.StatusHolder[list.size()]);

                             }
                             

                        /**
                        * field for PolicyType
                        */

                        
                                    protected java.lang.String localPolicyType ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPolicyTypeTracker = false ;

                           public boolean isPolicyTypeSpecified(){
                               return localPolicyTypeTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getPolicyType(){
                               return localPolicyType;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PolicyType
                               */
                               public void setPolicyType(java.lang.String param){
                            localPolicyTypeTracker = true;
                                   
                                            this.localPolicyType=param;
                                    

                               }
                            

                        /**
                        * field for Promote
                        */

                        
                                    protected boolean localPromote ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPromoteTracker = false ;

                           public boolean isPromoteSpecified(){
                               return localPromoteTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getPromote(){
                               return localPromote;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Promote
                               */
                               public void setPromote(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localPromoteTracker =
                                       true;
                                   
                                            this.localPromote=param;
                                    

                               }
                            

                        /**
                        * field for Version
                        */

                        
                                    protected java.lang.String localVersion ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localVersionTracker = false ;

                           public boolean isVersionSpecified(){
                               return localVersionTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getVersion(){
                               return localVersion;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Version
                               */
                               public void setVersion(java.lang.String param){
                            localVersionTracker = true;
                                   
                                            this.localVersion=param;
                                    

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
               

                   java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://dto.entitlement.identity.carbon.wso2.org/xsd");
                   if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           namespacePrefix+":PolicyDTO",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "PolicyDTO",
                           xmlWriter);
                   }

               
                   }
                if (localActiveTracker){
                                    namespace = "http://dto.entitlement.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "active", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("active cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localActive));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localAttributeDTOsTracker){
                                       if (localAttributeDTOs!=null){
                                            for (int i = 0;i < localAttributeDTOs.length;i++){
                                                if (localAttributeDTOs[i] != null){
                                                 localAttributeDTOs[i].serialize(new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd","attributeDTOs"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://dto.entitlement.identity.carbon.wso2.org/xsd", "attributeDTOs", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://dto.entitlement.identity.carbon.wso2.org/xsd", "attributeDTOs", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localLastModifiedTimeTracker){
                                    namespace = "http://dto.entitlement.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "lastModifiedTime", xmlWriter);
                             

                                          if (localLastModifiedTime==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localLastModifiedTime);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localLastModifiedUserTracker){
                                    namespace = "http://dto.entitlement.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "lastModifiedUser", xmlWriter);
                             

                                          if (localLastModifiedUser==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localLastModifiedUser);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localPolicyTracker){
                                    namespace = "http://dto.entitlement.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "policy", xmlWriter);
                             

                                          if (localPolicy==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localPolicy);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localPolicyEditorTracker){
                                    namespace = "http://dto.entitlement.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "policyEditor", xmlWriter);
                             

                                          if (localPolicyEditor==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localPolicyEditor);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localPolicyEditorDataTracker){
                             if (localPolicyEditorData!=null) {
                                   namespace = "http://dto.entitlement.identity.carbon.wso2.org/xsd";
                                   for (int i = 0;i < localPolicyEditorData.length;i++){
                                        
                                            if (localPolicyEditorData[i] != null){
                                        
                                                writeStartElement(null, namespace, "policyEditorData", xmlWriter);

                                            
                                                        xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPolicyEditorData[i]));
                                                    
                                                xmlWriter.writeEndElement();
                                              
                                                } else {
                                                   
                                                           // write null attribute
                                                            namespace = "http://dto.entitlement.identity.carbon.wso2.org/xsd";
                                                            writeStartElement(null, namespace, "policyEditorData", xmlWriter);
                                                            writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                            xmlWriter.writeEndElement();
                                                       
                                                }

                                   }
                             } else {
                                 
                                         // write the null attribute
                                        // write null attribute
                                           writeStartElement(null, "http://dto.entitlement.identity.carbon.wso2.org/xsd", "policyEditorData", xmlWriter);

                                           // write the nil attribute
                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                           xmlWriter.writeEndElement();
                                    
                             }

                        } if (localPolicyIdTracker){
                                    namespace = "http://dto.entitlement.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "policyId", xmlWriter);
                             

                                          if (localPolicyId==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localPolicyId);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localPolicyIdReferencesTracker){
                             if (localPolicyIdReferences!=null) {
                                   namespace = "http://dto.entitlement.identity.carbon.wso2.org/xsd";
                                   for (int i = 0;i < localPolicyIdReferences.length;i++){
                                        
                                            if (localPolicyIdReferences[i] != null){
                                        
                                                writeStartElement(null, namespace, "policyIdReferences", xmlWriter);

                                            
                                                        xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPolicyIdReferences[i]));
                                                    
                                                xmlWriter.writeEndElement();
                                              
                                                } else {
                                                   
                                                           // write null attribute
                                                            namespace = "http://dto.entitlement.identity.carbon.wso2.org/xsd";
                                                            writeStartElement(null, namespace, "policyIdReferences", xmlWriter);
                                                            writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                            xmlWriter.writeEndElement();
                                                       
                                                }

                                   }
                             } else {
                                 
                                         // write the null attribute
                                        // write null attribute
                                           writeStartElement(null, "http://dto.entitlement.identity.carbon.wso2.org/xsd", "policyIdReferences", xmlWriter);

                                           // write the nil attribute
                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                           xmlWriter.writeEndElement();
                                    
                             }

                        } if (localPolicyOrderTracker){
                                    namespace = "http://dto.entitlement.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "policyOrder", xmlWriter);
                             
                                               if (localPolicyOrder==java.lang.Integer.MIN_VALUE) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("policyOrder cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPolicyOrder));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localPolicySetIdReferencesTracker){
                             if (localPolicySetIdReferences!=null) {
                                   namespace = "http://dto.entitlement.identity.carbon.wso2.org/xsd";
                                   for (int i = 0;i < localPolicySetIdReferences.length;i++){
                                        
                                            if (localPolicySetIdReferences[i] != null){
                                        
                                                writeStartElement(null, namespace, "policySetIdReferences", xmlWriter);

                                            
                                                        xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPolicySetIdReferences[i]));
                                                    
                                                xmlWriter.writeEndElement();
                                              
                                                } else {
                                                   
                                                           // write null attribute
                                                            namespace = "http://dto.entitlement.identity.carbon.wso2.org/xsd";
                                                            writeStartElement(null, namespace, "policySetIdReferences", xmlWriter);
                                                            writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                            xmlWriter.writeEndElement();
                                                       
                                                }

                                   }
                             } else {
                                 
                                         // write the null attribute
                                        // write null attribute
                                           writeStartElement(null, "http://dto.entitlement.identity.carbon.wso2.org/xsd", "policySetIdReferences", xmlWriter);

                                           // write the nil attribute
                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                           xmlWriter.writeEndElement();
                                    
                             }

                        } if (localPolicyStatusHoldersTracker){
                                       if (localPolicyStatusHolders!=null){
                                            for (int i = 0;i < localPolicyStatusHolders.length;i++){
                                                if (localPolicyStatusHolders[i] != null){
                                                 localPolicyStatusHolders[i].serialize(new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd","policyStatusHolders"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://dto.entitlement.identity.carbon.wso2.org/xsd", "policyStatusHolders", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://dto.entitlement.identity.carbon.wso2.org/xsd", "policyStatusHolders", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localPolicyTypeTracker){
                                    namespace = "http://dto.entitlement.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "policyType", xmlWriter);
                             

                                          if (localPolicyType==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localPolicyType);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localPromoteTracker){
                                    namespace = "http://dto.entitlement.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "promote", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("promote cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPromote));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localVersionTracker){
                                    namespace = "http://dto.entitlement.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "version", xmlWriter);
                             

                                          if (localVersion==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localVersion);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             }
                    xmlWriter.writeEndElement();
               

        }

        private static java.lang.String generatePrefix(java.lang.String namespace) {
            if(namespace.equals("http://dto.entitlement.identity.carbon.wso2.org/xsd")){
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

                 if (localActiveTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd",
                                                                      "active"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localActive));
                            } if (localAttributeDTOsTracker){
                             if (localAttributeDTOs!=null) {
                                 for (int i = 0;i < localAttributeDTOs.length;i++){

                                    if (localAttributeDTOs[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd",
                                                                          "attributeDTOs"));
                                         elementList.add(localAttributeDTOs[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd",
                                                                          "attributeDTOs"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd",
                                                                          "attributeDTOs"));
                                        elementList.add(localAttributeDTOs);
                                    
                             }

                        } if (localLastModifiedTimeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd",
                                                                      "lastModifiedTime"));
                                 
                                         elementList.add(localLastModifiedTime==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localLastModifiedTime));
                                    } if (localLastModifiedUserTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd",
                                                                      "lastModifiedUser"));
                                 
                                         elementList.add(localLastModifiedUser==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localLastModifiedUser));
                                    } if (localPolicyTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd",
                                                                      "policy"));
                                 
                                         elementList.add(localPolicy==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPolicy));
                                    } if (localPolicyEditorTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd",
                                                                      "policyEditor"));
                                 
                                         elementList.add(localPolicyEditor==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPolicyEditor));
                                    } if (localPolicyEditorDataTracker){
                            if (localPolicyEditorData!=null){
                                  for (int i = 0;i < localPolicyEditorData.length;i++){
                                      
                                         if (localPolicyEditorData[i] != null){
                                          elementList.add(new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd",
                                                                              "policyEditorData"));
                                          elementList.add(
                                          org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPolicyEditorData[i]));
                                          } else {
                                             
                                                    elementList.add(new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd",
                                                                              "policyEditorData"));
                                                    elementList.add(null);
                                                
                                          }
                                      

                                  }
                            } else {
                              
                                    elementList.add(new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd",
                                                                              "policyEditorData"));
                                    elementList.add(null);
                                
                            }

                        } if (localPolicyIdTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd",
                                                                      "policyId"));
                                 
                                         elementList.add(localPolicyId==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPolicyId));
                                    } if (localPolicyIdReferencesTracker){
                            if (localPolicyIdReferences!=null){
                                  for (int i = 0;i < localPolicyIdReferences.length;i++){
                                      
                                         if (localPolicyIdReferences[i] != null){
                                          elementList.add(new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd",
                                                                              "policyIdReferences"));
                                          elementList.add(
                                          org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPolicyIdReferences[i]));
                                          } else {
                                             
                                                    elementList.add(new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd",
                                                                              "policyIdReferences"));
                                                    elementList.add(null);
                                                
                                          }
                                      

                                  }
                            } else {
                              
                                    elementList.add(new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd",
                                                                              "policyIdReferences"));
                                    elementList.add(null);
                                
                            }

                        } if (localPolicyOrderTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd",
                                                                      "policyOrder"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPolicyOrder));
                            } if (localPolicySetIdReferencesTracker){
                            if (localPolicySetIdReferences!=null){
                                  for (int i = 0;i < localPolicySetIdReferences.length;i++){
                                      
                                         if (localPolicySetIdReferences[i] != null){
                                          elementList.add(new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd",
                                                                              "policySetIdReferences"));
                                          elementList.add(
                                          org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPolicySetIdReferences[i]));
                                          } else {
                                             
                                                    elementList.add(new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd",
                                                                              "policySetIdReferences"));
                                                    elementList.add(null);
                                                
                                          }
                                      

                                  }
                            } else {
                              
                                    elementList.add(new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd",
                                                                              "policySetIdReferences"));
                                    elementList.add(null);
                                
                            }

                        } if (localPolicyStatusHoldersTracker){
                             if (localPolicyStatusHolders!=null) {
                                 for (int i = 0;i < localPolicyStatusHolders.length;i++){

                                    if (localPolicyStatusHolders[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd",
                                                                          "policyStatusHolders"));
                                         elementList.add(localPolicyStatusHolders[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd",
                                                                          "policyStatusHolders"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd",
                                                                          "policyStatusHolders"));
                                        elementList.add(localPolicyStatusHolders);
                                    
                             }

                        } if (localPolicyTypeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd",
                                                                      "policyType"));
                                 
                                         elementList.add(localPolicyType==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPolicyType));
                                    } if (localPromoteTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd",
                                                                      "promote"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPromote));
                            } if (localVersionTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd",
                                                                      "version"));
                                 
                                         elementList.add(localVersion==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localVersion));
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
        public static PolicyDTO parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            PolicyDTO object =
                new PolicyDTO();

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
                    
                            if (!"PolicyDTO".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (PolicyDTO)org.wso2.carbon.identity.entitlement.stub.types.axis2.ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }
                        

                  }
                

                }

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                
                    
                    reader.next();
                
                        java.util.ArrayList list2 = new java.util.ArrayList();
                    
                        java.util.ArrayList list7 = new java.util.ArrayList();
                    
                        java.util.ArrayList list9 = new java.util.ArrayList();
                    
                        java.util.ArrayList list11 = new java.util.ArrayList();
                    
                        java.util.ArrayList list12 = new java.util.ArrayList();
                    
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd","active").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"active" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setActive(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd","attributeDTOs").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list2.add(null);
                                                              reader.next();
                                                          } else {
                                                        list2.add(org.wso2.carbon.identity.entitlement.stub.dto.AttributeDTO.Factory.parse(reader));
                                                                }
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone2 = false;
                                                        while(!loopDone2){
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
                                                                loopDone2 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd","attributeDTOs").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list2.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list2.add(org.wso2.carbon.identity.entitlement.stub.dto.AttributeDTO.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone2 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setAttributeDTOs((org.wso2.carbon.identity.entitlement.stub.dto.AttributeDTO[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                org.wso2.carbon.identity.entitlement.stub.dto.AttributeDTO.class,
                                                                list2));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd","lastModifiedTime").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setLastModifiedTime(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd","lastModifiedUser").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setLastModifiedUser(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd","policy").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setPolicy(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd","policyEditor").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setPolicyEditor(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd","policyEditorData").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                              nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                              if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                  list7.add(null);
                                                       
                                                  reader.next();
                                              } else {
                                            list7.add(reader.getElementText());
                                            }
                                            //loop until we find a start element that is not part of this array
                                            boolean loopDone7 = false;
                                            while(!loopDone7){
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
                                                    loopDone7 = true;
                                                } else {
                                                    if (new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd","policyEditorData").equals(reader.getName())){
                                                         
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list7.add(null);
                                                                   
                                                              reader.next();
                                                          } else {
                                                        list7.add(reader.getElementText());
                                                        }
                                                    }else{
                                                        loopDone7 = true;
                                                    }
                                                }
                                            }
                                            // call the converter utility  to convert and set the array
                                            
                                                    object.setPolicyEditorData((java.lang.String[])
                                                        list7.toArray(new java.lang.String[list7.size()]));
                                                
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd","policyId").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setPolicyId(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd","policyIdReferences").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                              nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                              if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                  list9.add(null);
                                                       
                                                  reader.next();
                                              } else {
                                            list9.add(reader.getElementText());
                                            }
                                            //loop until we find a start element that is not part of this array
                                            boolean loopDone9 = false;
                                            while(!loopDone9){
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
                                                    loopDone9 = true;
                                                } else {
                                                    if (new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd","policyIdReferences").equals(reader.getName())){
                                                         
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list9.add(null);
                                                                   
                                                              reader.next();
                                                          } else {
                                                        list9.add(reader.getElementText());
                                                        }
                                                    }else{
                                                        loopDone9 = true;
                                                    }
                                                }
                                            }
                                            // call the converter utility  to convert and set the array
                                            
                                                    object.setPolicyIdReferences((java.lang.String[])
                                                        list9.toArray(new java.lang.String[list9.size()]));
                                                
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd","policyOrder").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"policyOrder" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setPolicyOrder(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                               object.setPolicyOrder(java.lang.Integer.MIN_VALUE);
                                           
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd","policySetIdReferences").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                              nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                              if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                  list11.add(null);
                                                       
                                                  reader.next();
                                              } else {
                                            list11.add(reader.getElementText());
                                            }
                                            //loop until we find a start element that is not part of this array
                                            boolean loopDone11 = false;
                                            while(!loopDone11){
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
                                                    loopDone11 = true;
                                                } else {
                                                    if (new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd","policySetIdReferences").equals(reader.getName())){
                                                         
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list11.add(null);
                                                                   
                                                              reader.next();
                                                          } else {
                                                        list11.add(reader.getElementText());
                                                        }
                                                    }else{
                                                        loopDone11 = true;
                                                    }
                                                }
                                            }
                                            // call the converter utility  to convert and set the array
                                            
                                                    object.setPolicySetIdReferences((java.lang.String[])
                                                        list11.toArray(new java.lang.String[list11.size()]));
                                                
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd","policyStatusHolders").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list12.add(null);
                                                              reader.next();
                                                          } else {
                                                        list12.add(org.wso2.carbon.identity.entitlement.stub.dto.StatusHolder.Factory.parse(reader));
                                                                }
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone12 = false;
                                                        while(!loopDone12){
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
                                                                loopDone12 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd","policyStatusHolders").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list12.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list12.add(org.wso2.carbon.identity.entitlement.stub.dto.StatusHolder.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone12 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setPolicyStatusHolders((org.wso2.carbon.identity.entitlement.stub.dto.StatusHolder[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                org.wso2.carbon.identity.entitlement.stub.dto.StatusHolder.class,
                                                                list12));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd","policyType").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setPolicyType(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd","promote").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"promote" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setPromote(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd","version").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setVersion(
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
           
    