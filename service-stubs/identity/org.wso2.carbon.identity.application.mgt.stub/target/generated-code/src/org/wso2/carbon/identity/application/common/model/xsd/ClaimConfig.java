
/**
 * ClaimConfig.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:46 UTC)
 */

            
                package org.wso2.carbon.identity.application.common.model.xsd;
            

            /**
            *  ClaimConfig bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class ClaimConfig
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = ClaimConfig
                Namespace URI = http://model.common.application.identity.carbon.wso2.org/xsd
                Namespace Prefix = ns1
                */
            

                        /**
                        * field for AlwaysSendMappedLocalSubjectId
                        */

                        
                                    protected boolean localAlwaysSendMappedLocalSubjectId ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAlwaysSendMappedLocalSubjectIdTracker = false ;

                           public boolean isAlwaysSendMappedLocalSubjectIdSpecified(){
                               return localAlwaysSendMappedLocalSubjectIdTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getAlwaysSendMappedLocalSubjectId(){
                               return localAlwaysSendMappedLocalSubjectId;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param AlwaysSendMappedLocalSubjectId
                               */
                               public void setAlwaysSendMappedLocalSubjectId(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localAlwaysSendMappedLocalSubjectIdTracker =
                                       true;
                                   
                                            this.localAlwaysSendMappedLocalSubjectId=param;
                                    

                               }
                            

                        /**
                        * field for ClaimMappings
                        * This was an Array!
                        */

                        
                                    protected org.wso2.carbon.identity.application.common.model.xsd.ClaimMapping[] localClaimMappings ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localClaimMappingsTracker = false ;

                           public boolean isClaimMappingsSpecified(){
                               return localClaimMappingsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.identity.application.common.model.xsd.ClaimMapping[]
                           */
                           public  org.wso2.carbon.identity.application.common.model.xsd.ClaimMapping[] getClaimMappings(){
                               return localClaimMappings;
                           }

                           
                        


                               
                              /**
                               * validate the array for ClaimMappings
                               */
                              protected void validateClaimMappings(org.wso2.carbon.identity.application.common.model.xsd.ClaimMapping[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param ClaimMappings
                              */
                              public void setClaimMappings(org.wso2.carbon.identity.application.common.model.xsd.ClaimMapping[] param){
                              
                                   validateClaimMappings(param);

                               localClaimMappingsTracker = true;
                                      
                                      this.localClaimMappings=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param org.wso2.carbon.identity.application.common.model.xsd.ClaimMapping
                             */
                             public void addClaimMappings(org.wso2.carbon.identity.application.common.model.xsd.ClaimMapping param){
                                   if (localClaimMappings == null){
                                   localClaimMappings = new org.wso2.carbon.identity.application.common.model.xsd.ClaimMapping[]{};
                                   }

                            
                                 //update the setting tracker
                                localClaimMappingsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localClaimMappings);
                               list.add(param);
                               this.localClaimMappings =
                             (org.wso2.carbon.identity.application.common.model.xsd.ClaimMapping[])list.toArray(
                            new org.wso2.carbon.identity.application.common.model.xsd.ClaimMapping[list.size()]);

                             }
                             

                        /**
                        * field for IdpClaims
                        * This was an Array!
                        */

                        
                                    protected org.wso2.carbon.identity.application.common.model.xsd.Claim[] localIdpClaims ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localIdpClaimsTracker = false ;

                           public boolean isIdpClaimsSpecified(){
                               return localIdpClaimsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.identity.application.common.model.xsd.Claim[]
                           */
                           public  org.wso2.carbon.identity.application.common.model.xsd.Claim[] getIdpClaims(){
                               return localIdpClaims;
                           }

                           
                        


                               
                              /**
                               * validate the array for IdpClaims
                               */
                              protected void validateIdpClaims(org.wso2.carbon.identity.application.common.model.xsd.Claim[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param IdpClaims
                              */
                              public void setIdpClaims(org.wso2.carbon.identity.application.common.model.xsd.Claim[] param){
                              
                                   validateIdpClaims(param);

                               localIdpClaimsTracker = true;
                                      
                                      this.localIdpClaims=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param org.wso2.carbon.identity.application.common.model.xsd.Claim
                             */
                             public void addIdpClaims(org.wso2.carbon.identity.application.common.model.xsd.Claim param){
                                   if (localIdpClaims == null){
                                   localIdpClaims = new org.wso2.carbon.identity.application.common.model.xsd.Claim[]{};
                                   }

                            
                                 //update the setting tracker
                                localIdpClaimsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localIdpClaims);
                               list.add(param);
                               this.localIdpClaims =
                             (org.wso2.carbon.identity.application.common.model.xsd.Claim[])list.toArray(
                            new org.wso2.carbon.identity.application.common.model.xsd.Claim[list.size()]);

                             }
                             

                        /**
                        * field for LocalClaimDialect
                        */

                        
                                    protected boolean localLocalClaimDialect ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localLocalClaimDialectTracker = false ;

                           public boolean isLocalClaimDialectSpecified(){
                               return localLocalClaimDialectTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getLocalClaimDialect(){
                               return localLocalClaimDialect;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param LocalClaimDialect
                               */
                               public void setLocalClaimDialect(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localLocalClaimDialectTracker =
                                       true;
                                   
                                            this.localLocalClaimDialect=param;
                                    

                               }
                            

                        /**
                        * field for RoleClaimURI
                        */

                        
                                    protected java.lang.String localRoleClaimURI ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localRoleClaimURITracker = false ;

                           public boolean isRoleClaimURISpecified(){
                               return localRoleClaimURITracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getRoleClaimURI(){
                               return localRoleClaimURI;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param RoleClaimURI
                               */
                               public void setRoleClaimURI(java.lang.String param){
                            localRoleClaimURITracker = true;
                                   
                                            this.localRoleClaimURI=param;
                                    

                               }
                            

                        /**
                        * field for UserClaimURI
                        */

                        
                                    protected java.lang.String localUserClaimURI ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localUserClaimURITracker = false ;

                           public boolean isUserClaimURISpecified(){
                               return localUserClaimURITracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getUserClaimURI(){
                               return localUserClaimURI;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param UserClaimURI
                               */
                               public void setUserClaimURI(java.lang.String param){
                            localUserClaimURITracker = true;
                                   
                                            this.localUserClaimURI=param;
                                    

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
                           namespacePrefix+":ClaimConfig",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "ClaimConfig",
                           xmlWriter);
                   }

               
                   }
                if (localAlwaysSendMappedLocalSubjectIdTracker){
                                    namespace = "http://model.common.application.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "alwaysSendMappedLocalSubjectId", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("alwaysSendMappedLocalSubjectId cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAlwaysSendMappedLocalSubjectId));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localClaimMappingsTracker){
                                       if (localClaimMappings!=null){
                                            for (int i = 0;i < localClaimMappings.length;i++){
                                                if (localClaimMappings[i] != null){
                                                 localClaimMappings[i].serialize(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","claimMappings"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://model.common.application.identity.carbon.wso2.org/xsd", "claimMappings", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://model.common.application.identity.carbon.wso2.org/xsd", "claimMappings", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localIdpClaimsTracker){
                                       if (localIdpClaims!=null){
                                            for (int i = 0;i < localIdpClaims.length;i++){
                                                if (localIdpClaims[i] != null){
                                                 localIdpClaims[i].serialize(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","idpClaims"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://model.common.application.identity.carbon.wso2.org/xsd", "idpClaims", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://model.common.application.identity.carbon.wso2.org/xsd", "idpClaims", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localLocalClaimDialectTracker){
                                    namespace = "http://model.common.application.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "localClaimDialect", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("localClaimDialect cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localLocalClaimDialect));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localRoleClaimURITracker){
                                    namespace = "http://model.common.application.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "roleClaimURI", xmlWriter);
                             

                                          if (localRoleClaimURI==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localRoleClaimURI);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localUserClaimURITracker){
                                    namespace = "http://model.common.application.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "userClaimURI", xmlWriter);
                             

                                          if (localUserClaimURI==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localUserClaimURI);
                                            
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

                 if (localAlwaysSendMappedLocalSubjectIdTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                      "alwaysSendMappedLocalSubjectId"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAlwaysSendMappedLocalSubjectId));
                            } if (localClaimMappingsTracker){
                             if (localClaimMappings!=null) {
                                 for (int i = 0;i < localClaimMappings.length;i++){

                                    if (localClaimMappings[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                          "claimMappings"));
                                         elementList.add(localClaimMappings[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                          "claimMappings"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                          "claimMappings"));
                                        elementList.add(localClaimMappings);
                                    
                             }

                        } if (localIdpClaimsTracker){
                             if (localIdpClaims!=null) {
                                 for (int i = 0;i < localIdpClaims.length;i++){

                                    if (localIdpClaims[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                          "idpClaims"));
                                         elementList.add(localIdpClaims[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                          "idpClaims"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                          "idpClaims"));
                                        elementList.add(localIdpClaims);
                                    
                             }

                        } if (localLocalClaimDialectTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                      "localClaimDialect"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localLocalClaimDialect));
                            } if (localRoleClaimURITracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                      "roleClaimURI"));
                                 
                                         elementList.add(localRoleClaimURI==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localRoleClaimURI));
                                    } if (localUserClaimURITracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                      "userClaimURI"));
                                 
                                         elementList.add(localUserClaimURI==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localUserClaimURI));
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
        public static ClaimConfig parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            ClaimConfig object =
                new ClaimConfig();

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
                    
                            if (!"ClaimConfig".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (ClaimConfig)org.wso2.carbon.identity.application.common.xsd.ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }
                        

                  }
                

                }

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                
                    
                    reader.next();
                
                        java.util.ArrayList list2 = new java.util.ArrayList();
                    
                        java.util.ArrayList list3 = new java.util.ArrayList();
                    
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","alwaysSendMappedLocalSubjectId").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"alwaysSendMappedLocalSubjectId" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setAlwaysSendMappedLocalSubjectId(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","claimMappings").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list2.add(null);
                                                              reader.next();
                                                          } else {
                                                        list2.add(org.wso2.carbon.identity.application.common.model.xsd.ClaimMapping.Factory.parse(reader));
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
                                                                if (new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","claimMappings").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list2.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list2.add(org.wso2.carbon.identity.application.common.model.xsd.ClaimMapping.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone2 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setClaimMappings((org.wso2.carbon.identity.application.common.model.xsd.ClaimMapping[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                org.wso2.carbon.identity.application.common.model.xsd.ClaimMapping.class,
                                                                list2));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","idpClaims").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list3.add(null);
                                                              reader.next();
                                                          } else {
                                                        list3.add(org.wso2.carbon.identity.application.common.model.xsd.Claim.Factory.parse(reader));
                                                                }
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone3 = false;
                                                        while(!loopDone3){
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
                                                                loopDone3 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","idpClaims").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list3.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list3.add(org.wso2.carbon.identity.application.common.model.xsd.Claim.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone3 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setIdpClaims((org.wso2.carbon.identity.application.common.model.xsd.Claim[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                org.wso2.carbon.identity.application.common.model.xsd.Claim.class,
                                                                list3));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","localClaimDialect").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"localClaimDialect" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setLocalClaimDialect(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","roleClaimURI").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setRoleClaimURI(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","userClaimURI").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setUserClaimURI(
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
           
    