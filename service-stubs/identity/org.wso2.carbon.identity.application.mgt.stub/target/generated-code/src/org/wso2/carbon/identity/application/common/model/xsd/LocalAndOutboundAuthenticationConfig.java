
/**
 * LocalAndOutboundAuthenticationConfig.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:46 UTC)
 */

            
                package org.wso2.carbon.identity.application.common.model.xsd;
            

            /**
            *  LocalAndOutboundAuthenticationConfig bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class LocalAndOutboundAuthenticationConfig
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = LocalAndOutboundAuthenticationConfig
                Namespace URI = http://model.common.application.identity.carbon.wso2.org/xsd
                Namespace Prefix = ns1
                */
            

                        /**
                        * field for AlwaysSendBackAuthenticatedListOfIdPs
                        */

                        
                                    protected boolean localAlwaysSendBackAuthenticatedListOfIdPs ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAlwaysSendBackAuthenticatedListOfIdPsTracker = false ;

                           public boolean isAlwaysSendBackAuthenticatedListOfIdPsSpecified(){
                               return localAlwaysSendBackAuthenticatedListOfIdPsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getAlwaysSendBackAuthenticatedListOfIdPs(){
                               return localAlwaysSendBackAuthenticatedListOfIdPs;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param AlwaysSendBackAuthenticatedListOfIdPs
                               */
                               public void setAlwaysSendBackAuthenticatedListOfIdPs(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localAlwaysSendBackAuthenticatedListOfIdPsTracker =
                                       true;
                                   
                                            this.localAlwaysSendBackAuthenticatedListOfIdPs=param;
                                    

                               }
                            

                        /**
                        * field for AuthenticationStepForAttributes
                        */

                        
                                    protected org.wso2.carbon.identity.application.common.model.xsd.AuthenticationStep localAuthenticationStepForAttributes ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAuthenticationStepForAttributesTracker = false ;

                           public boolean isAuthenticationStepForAttributesSpecified(){
                               return localAuthenticationStepForAttributesTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.identity.application.common.model.xsd.AuthenticationStep
                           */
                           public  org.wso2.carbon.identity.application.common.model.xsd.AuthenticationStep getAuthenticationStepForAttributes(){
                               return localAuthenticationStepForAttributes;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param AuthenticationStepForAttributes
                               */
                               public void setAuthenticationStepForAttributes(org.wso2.carbon.identity.application.common.model.xsd.AuthenticationStep param){
                            localAuthenticationStepForAttributesTracker = true;
                                   
                                            this.localAuthenticationStepForAttributes=param;
                                    

                               }
                            

                        /**
                        * field for AuthenticationStepForSubject
                        */

                        
                                    protected org.wso2.carbon.identity.application.common.model.xsd.AuthenticationStep localAuthenticationStepForSubject ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAuthenticationStepForSubjectTracker = false ;

                           public boolean isAuthenticationStepForSubjectSpecified(){
                               return localAuthenticationStepForSubjectTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.identity.application.common.model.xsd.AuthenticationStep
                           */
                           public  org.wso2.carbon.identity.application.common.model.xsd.AuthenticationStep getAuthenticationStepForSubject(){
                               return localAuthenticationStepForSubject;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param AuthenticationStepForSubject
                               */
                               public void setAuthenticationStepForSubject(org.wso2.carbon.identity.application.common.model.xsd.AuthenticationStep param){
                            localAuthenticationStepForSubjectTracker = true;
                                   
                                            this.localAuthenticationStepForSubject=param;
                                    

                               }
                            

                        /**
                        * field for AuthenticationSteps
                        * This was an Array!
                        */

                        
                                    protected org.wso2.carbon.identity.application.common.model.xsd.AuthenticationStep[] localAuthenticationSteps ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAuthenticationStepsTracker = false ;

                           public boolean isAuthenticationStepsSpecified(){
                               return localAuthenticationStepsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.identity.application.common.model.xsd.AuthenticationStep[]
                           */
                           public  org.wso2.carbon.identity.application.common.model.xsd.AuthenticationStep[] getAuthenticationSteps(){
                               return localAuthenticationSteps;
                           }

                           
                        


                               
                              /**
                               * validate the array for AuthenticationSteps
                               */
                              protected void validateAuthenticationSteps(org.wso2.carbon.identity.application.common.model.xsd.AuthenticationStep[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param AuthenticationSteps
                              */
                              public void setAuthenticationSteps(org.wso2.carbon.identity.application.common.model.xsd.AuthenticationStep[] param){
                              
                                   validateAuthenticationSteps(param);

                               localAuthenticationStepsTracker = true;
                                      
                                      this.localAuthenticationSteps=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param org.wso2.carbon.identity.application.common.model.xsd.AuthenticationStep
                             */
                             public void addAuthenticationSteps(org.wso2.carbon.identity.application.common.model.xsd.AuthenticationStep param){
                                   if (localAuthenticationSteps == null){
                                   localAuthenticationSteps = new org.wso2.carbon.identity.application.common.model.xsd.AuthenticationStep[]{};
                                   }

                            
                                 //update the setting tracker
                                localAuthenticationStepsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localAuthenticationSteps);
                               list.add(param);
                               this.localAuthenticationSteps =
                             (org.wso2.carbon.identity.application.common.model.xsd.AuthenticationStep[])list.toArray(
                            new org.wso2.carbon.identity.application.common.model.xsd.AuthenticationStep[list.size()]);

                             }
                             

                        /**
                        * field for AuthenticationType
                        */

                        
                                    protected java.lang.String localAuthenticationType ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAuthenticationTypeTracker = false ;

                           public boolean isAuthenticationTypeSpecified(){
                               return localAuthenticationTypeTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getAuthenticationType(){
                               return localAuthenticationType;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param AuthenticationType
                               */
                               public void setAuthenticationType(java.lang.String param){
                            localAuthenticationTypeTracker = true;
                                   
                                            this.localAuthenticationType=param;
                                    

                               }
                            

                        /**
                        * field for SubjectClaimUri
                        */

                        
                                    protected java.lang.String localSubjectClaimUri ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localSubjectClaimUriTracker = false ;

                           public boolean isSubjectClaimUriSpecified(){
                               return localSubjectClaimUriTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getSubjectClaimUri(){
                               return localSubjectClaimUri;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param SubjectClaimUri
                               */
                               public void setSubjectClaimUri(java.lang.String param){
                            localSubjectClaimUriTracker = true;
                                   
                                            this.localSubjectClaimUri=param;
                                    

                               }
                            

                        /**
                        * field for UseTenantDomainInLocalSubjectIdentifier
                        */

                        
                                    protected boolean localUseTenantDomainInLocalSubjectIdentifier ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localUseTenantDomainInLocalSubjectIdentifierTracker = false ;

                           public boolean isUseTenantDomainInLocalSubjectIdentifierSpecified(){
                               return localUseTenantDomainInLocalSubjectIdentifierTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getUseTenantDomainInLocalSubjectIdentifier(){
                               return localUseTenantDomainInLocalSubjectIdentifier;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param UseTenantDomainInLocalSubjectIdentifier
                               */
                               public void setUseTenantDomainInLocalSubjectIdentifier(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localUseTenantDomainInLocalSubjectIdentifierTracker =
                                       true;
                                   
                                            this.localUseTenantDomainInLocalSubjectIdentifier=param;
                                    

                               }
                            

                        /**
                        * field for UseUserstoreDomainInLocalSubjectIdentifier
                        */

                        
                                    protected boolean localUseUserstoreDomainInLocalSubjectIdentifier ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localUseUserstoreDomainInLocalSubjectIdentifierTracker = false ;

                           public boolean isUseUserstoreDomainInLocalSubjectIdentifierSpecified(){
                               return localUseUserstoreDomainInLocalSubjectIdentifierTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getUseUserstoreDomainInLocalSubjectIdentifier(){
                               return localUseUserstoreDomainInLocalSubjectIdentifier;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param UseUserstoreDomainInLocalSubjectIdentifier
                               */
                               public void setUseUserstoreDomainInLocalSubjectIdentifier(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localUseUserstoreDomainInLocalSubjectIdentifierTracker =
                                       true;
                                   
                                            this.localUseUserstoreDomainInLocalSubjectIdentifier=param;
                                    

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
                           namespacePrefix+":LocalAndOutboundAuthenticationConfig",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "LocalAndOutboundAuthenticationConfig",
                           xmlWriter);
                   }

               
                   }
                if (localAlwaysSendBackAuthenticatedListOfIdPsTracker){
                                    namespace = "http://model.common.application.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "alwaysSendBackAuthenticatedListOfIdPs", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("alwaysSendBackAuthenticatedListOfIdPs cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAlwaysSendBackAuthenticatedListOfIdPs));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localAuthenticationStepForAttributesTracker){
                                    if (localAuthenticationStepForAttributes==null){

                                        writeStartElement(null, "http://model.common.application.identity.carbon.wso2.org/xsd", "authenticationStepForAttributes", xmlWriter);

                                       // write the nil attribute
                                      writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                      xmlWriter.writeEndElement();
                                    }else{
                                     localAuthenticationStepForAttributes.serialize(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","authenticationStepForAttributes"),
                                        xmlWriter);
                                    }
                                } if (localAuthenticationStepForSubjectTracker){
                                    if (localAuthenticationStepForSubject==null){

                                        writeStartElement(null, "http://model.common.application.identity.carbon.wso2.org/xsd", "authenticationStepForSubject", xmlWriter);

                                       // write the nil attribute
                                      writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                      xmlWriter.writeEndElement();
                                    }else{
                                     localAuthenticationStepForSubject.serialize(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","authenticationStepForSubject"),
                                        xmlWriter);
                                    }
                                } if (localAuthenticationStepsTracker){
                                       if (localAuthenticationSteps!=null){
                                            for (int i = 0;i < localAuthenticationSteps.length;i++){
                                                if (localAuthenticationSteps[i] != null){
                                                 localAuthenticationSteps[i].serialize(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","authenticationSteps"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://model.common.application.identity.carbon.wso2.org/xsd", "authenticationSteps", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://model.common.application.identity.carbon.wso2.org/xsd", "authenticationSteps", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localAuthenticationTypeTracker){
                                    namespace = "http://model.common.application.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "authenticationType", xmlWriter);
                             

                                          if (localAuthenticationType==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAuthenticationType);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localSubjectClaimUriTracker){
                                    namespace = "http://model.common.application.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "subjectClaimUri", xmlWriter);
                             

                                          if (localSubjectClaimUri==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localSubjectClaimUri);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localUseTenantDomainInLocalSubjectIdentifierTracker){
                                    namespace = "http://model.common.application.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "useTenantDomainInLocalSubjectIdentifier", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("useTenantDomainInLocalSubjectIdentifier cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localUseTenantDomainInLocalSubjectIdentifier));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localUseUserstoreDomainInLocalSubjectIdentifierTracker){
                                    namespace = "http://model.common.application.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "useUserstoreDomainInLocalSubjectIdentifier", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("useUserstoreDomainInLocalSubjectIdentifier cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localUseUserstoreDomainInLocalSubjectIdentifier));
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

                 if (localAlwaysSendBackAuthenticatedListOfIdPsTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                      "alwaysSendBackAuthenticatedListOfIdPs"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAlwaysSendBackAuthenticatedListOfIdPs));
                            } if (localAuthenticationStepForAttributesTracker){
                            elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                      "authenticationStepForAttributes"));
                            
                            
                                    elementList.add(localAuthenticationStepForAttributes==null?null:
                                    localAuthenticationStepForAttributes);
                                } if (localAuthenticationStepForSubjectTracker){
                            elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                      "authenticationStepForSubject"));
                            
                            
                                    elementList.add(localAuthenticationStepForSubject==null?null:
                                    localAuthenticationStepForSubject);
                                } if (localAuthenticationStepsTracker){
                             if (localAuthenticationSteps!=null) {
                                 for (int i = 0;i < localAuthenticationSteps.length;i++){

                                    if (localAuthenticationSteps[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                          "authenticationSteps"));
                                         elementList.add(localAuthenticationSteps[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                          "authenticationSteps"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                          "authenticationSteps"));
                                        elementList.add(localAuthenticationSteps);
                                    
                             }

                        } if (localAuthenticationTypeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                      "authenticationType"));
                                 
                                         elementList.add(localAuthenticationType==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAuthenticationType));
                                    } if (localSubjectClaimUriTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                      "subjectClaimUri"));
                                 
                                         elementList.add(localSubjectClaimUri==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSubjectClaimUri));
                                    } if (localUseTenantDomainInLocalSubjectIdentifierTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                      "useTenantDomainInLocalSubjectIdentifier"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localUseTenantDomainInLocalSubjectIdentifier));
                            } if (localUseUserstoreDomainInLocalSubjectIdentifierTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd",
                                                                      "useUserstoreDomainInLocalSubjectIdentifier"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localUseUserstoreDomainInLocalSubjectIdentifier));
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
        public static LocalAndOutboundAuthenticationConfig parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            LocalAndOutboundAuthenticationConfig object =
                new LocalAndOutboundAuthenticationConfig();

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
                    
                            if (!"LocalAndOutboundAuthenticationConfig".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (LocalAndOutboundAuthenticationConfig)org.wso2.carbon.identity.application.common.xsd.ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }
                        

                  }
                

                }

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                
                    
                    reader.next();
                
                        java.util.ArrayList list4 = new java.util.ArrayList();
                    
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","alwaysSendBackAuthenticatedListOfIdPs").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"alwaysSendBackAuthenticatedListOfIdPs" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setAlwaysSendBackAuthenticatedListOfIdPs(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","authenticationStepForAttributes").equals(reader.getName())){
                                
                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                          object.setAuthenticationStepForAttributes(null);
                                          reader.next();
                                            
                                            reader.next();
                                          
                                      }else{
                                    
                                                object.setAuthenticationStepForAttributes(org.wso2.carbon.identity.application.common.model.xsd.AuthenticationStep.Factory.parse(reader));
                                              
                                        reader.next();
                                    }
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","authenticationStepForSubject").equals(reader.getName())){
                                
                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                          object.setAuthenticationStepForSubject(null);
                                          reader.next();
                                            
                                            reader.next();
                                          
                                      }else{
                                    
                                                object.setAuthenticationStepForSubject(org.wso2.carbon.identity.application.common.model.xsd.AuthenticationStep.Factory.parse(reader));
                                              
                                        reader.next();
                                    }
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","authenticationSteps").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list4.add(null);
                                                              reader.next();
                                                          } else {
                                                        list4.add(org.wso2.carbon.identity.application.common.model.xsd.AuthenticationStep.Factory.parse(reader));
                                                                }
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone4 = false;
                                                        while(!loopDone4){
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
                                                                loopDone4 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","authenticationSteps").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list4.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list4.add(org.wso2.carbon.identity.application.common.model.xsd.AuthenticationStep.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone4 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setAuthenticationSteps((org.wso2.carbon.identity.application.common.model.xsd.AuthenticationStep[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                org.wso2.carbon.identity.application.common.model.xsd.AuthenticationStep.class,
                                                                list4));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","authenticationType").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setAuthenticationType(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","subjectClaimUri").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setSubjectClaimUri(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","useTenantDomainInLocalSubjectIdentifier").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"useTenantDomainInLocalSubjectIdentifier" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setUseTenantDomainInLocalSubjectIdentifier(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.common.application.identity.carbon.wso2.org/xsd","useUserstoreDomainInLocalSubjectIdentifier").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"useUserstoreDomainInLocalSubjectIdentifier" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setUseUserstoreDomainInLocalSubjectIdentifier(
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
           
    