
/**
 * UserFieldDTO.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:46 UTC)
 */

            
                package org.wso2.carbon.identity.user.registration.stub.dto;
            

            /**
            *  UserFieldDTO bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class UserFieldDTO
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = UserFieldDTO
                Namespace URI = http://dto.registration.user.identity.carbon.wso2.org/xsd
                Namespace Prefix = ns1
                */
            

                        /**
                        * field for ClaimUri
                        */

                        
                                    protected java.lang.String localClaimUri ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localClaimUriTracker = false ;

                           public boolean isClaimUriSpecified(){
                               return localClaimUriTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getClaimUri(){
                               return localClaimUri;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ClaimUri
                               */
                               public void setClaimUri(java.lang.String param){
                            localClaimUriTracker = true;
                                   
                                            this.localClaimUri=param;
                                    

                               }
                            

                        /**
                        * field for DefaultValue
                        */

                        
                                    protected java.lang.String localDefaultValue ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localDefaultValueTracker = false ;

                           public boolean isDefaultValueSpecified(){
                               return localDefaultValueTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getDefaultValue(){
                               return localDefaultValue;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param DefaultValue
                               */
                               public void setDefaultValue(java.lang.String param){
                            localDefaultValueTracker = true;
                                   
                                            this.localDefaultValue=param;
                                    

                               }
                            

                        /**
                        * field for DisplayOrder
                        */

                        
                                    protected int localDisplayOrder ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localDisplayOrderTracker = false ;

                           public boolean isDisplayOrderSpecified(){
                               return localDisplayOrderTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return int
                           */
                           public  int getDisplayOrder(){
                               return localDisplayOrder;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param DisplayOrder
                               */
                               public void setDisplayOrder(int param){
                            
                                       // setting primitive attribute tracker to true
                                       localDisplayOrderTracker =
                                       param != java.lang.Integer.MIN_VALUE;
                                   
                                            this.localDisplayOrder=param;
                                    

                               }
                            

                        /**
                        * field for FieldName
                        */

                        
                                    protected java.lang.String localFieldName ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localFieldNameTracker = false ;

                           public boolean isFieldNameSpecified(){
                               return localFieldNameTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getFieldName(){
                               return localFieldName;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param FieldName
                               */
                               public void setFieldName(java.lang.String param){
                            localFieldNameTracker = true;
                                   
                                            this.localFieldName=param;
                                    

                               }
                            

                        /**
                        * field for FieldValue
                        */

                        
                                    protected java.lang.String localFieldValue ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localFieldValueTracker = false ;

                           public boolean isFieldValueSpecified(){
                               return localFieldValueTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getFieldValue(){
                               return localFieldValue;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param FieldValue
                               */
                               public void setFieldValue(java.lang.String param){
                            localFieldValueTracker = true;
                                   
                                            this.localFieldValue=param;
                                    

                               }
                            

                        /**
                        * field for InputValues
                        * This was an Array!
                        */

                        
                                    protected java.lang.String[] localInputValues ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localInputValuesTracker = false ;

                           public boolean isInputValuesSpecified(){
                               return localInputValuesTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String[]
                           */
                           public  java.lang.String[] getInputValues(){
                               return localInputValues;
                           }

                           
                        


                               
                              /**
                               * validate the array for InputValues
                               */
                              protected void validateInputValues(java.lang.String[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param InputValues
                              */
                              public void setInputValues(java.lang.String[] param){
                              
                                   validateInputValues(param);

                               localInputValuesTracker = true;
                                      
                                      this.localInputValues=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param java.lang.String
                             */
                             public void addInputValues(java.lang.String param){
                                   if (localInputValues == null){
                                   localInputValues = new java.lang.String[]{};
                                   }

                            
                                 //update the setting tracker
                                localInputValuesTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localInputValues);
                               list.add(param);
                               this.localInputValues =
                             (java.lang.String[])list.toArray(
                            new java.lang.String[list.size()]);

                             }
                             

                        /**
                        * field for MaxLength
                        */

                        
                                    protected int localMaxLength ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localMaxLengthTracker = false ;

                           public boolean isMaxLengthSpecified(){
                               return localMaxLengthTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return int
                           */
                           public  int getMaxLength(){
                               return localMaxLength;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param MaxLength
                               */
                               public void setMaxLength(int param){
                            
                                       // setting primitive attribute tracker to true
                                       localMaxLengthTracker =
                                       param != java.lang.Integer.MIN_VALUE;
                                   
                                            this.localMaxLength=param;
                                    

                               }
                            

                        /**
                        * field for MinLength
                        */

                        
                                    protected int localMinLength ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localMinLengthTracker = false ;

                           public boolean isMinLengthSpecified(){
                               return localMinLengthTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return int
                           */
                           public  int getMinLength(){
                               return localMinLength;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param MinLength
                               */
                               public void setMinLength(int param){
                            
                                       // setting primitive attribute tracker to true
                                       localMinLengthTracker =
                                       param != java.lang.Integer.MIN_VALUE;
                                   
                                            this.localMinLength=param;
                                    

                               }
                            

                        /**
                        * field for RegEx
                        */

                        
                                    protected java.lang.String localRegEx ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localRegExTracker = false ;

                           public boolean isRegExSpecified(){
                               return localRegExTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getRegEx(){
                               return localRegEx;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param RegEx
                               */
                               public void setRegEx(java.lang.String param){
                            localRegExTracker = true;
                                   
                                            this.localRegEx=param;
                                    

                               }
                            

                        /**
                        * field for Required
                        */

                        
                                    protected boolean localRequired ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localRequiredTracker = false ;

                           public boolean isRequiredSpecified(){
                               return localRequiredTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getRequired(){
                               return localRequired;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Required
                               */
                               public void setRequired(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localRequiredTracker =
                                       true;
                                   
                                            this.localRequired=param;
                                    

                               }
                            

                        /**
                        * field for SupportedByDefault
                        */

                        
                                    protected boolean localSupportedByDefault ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localSupportedByDefaultTracker = false ;

                           public boolean isSupportedByDefaultSpecified(){
                               return localSupportedByDefaultTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getSupportedByDefault(){
                               return localSupportedByDefault;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param SupportedByDefault
                               */
                               public void setSupportedByDefault(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localSupportedByDefaultTracker =
                                       true;
                                   
                                            this.localSupportedByDefault=param;
                                    

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
               

                   java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://dto.registration.user.identity.carbon.wso2.org/xsd");
                   if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           namespacePrefix+":UserFieldDTO",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "UserFieldDTO",
                           xmlWriter);
                   }

               
                   }
                if (localClaimUriTracker){
                                    namespace = "http://dto.registration.user.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "claimUri", xmlWriter);
                             

                                          if (localClaimUri==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localClaimUri);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localDefaultValueTracker){
                                    namespace = "http://dto.registration.user.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "defaultValue", xmlWriter);
                             

                                          if (localDefaultValue==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localDefaultValue);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localDisplayOrderTracker){
                                    namespace = "http://dto.registration.user.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "displayOrder", xmlWriter);
                             
                                               if (localDisplayOrder==java.lang.Integer.MIN_VALUE) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("displayOrder cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDisplayOrder));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localFieldNameTracker){
                                    namespace = "http://dto.registration.user.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "fieldName", xmlWriter);
                             

                                          if (localFieldName==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localFieldName);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localFieldValueTracker){
                                    namespace = "http://dto.registration.user.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "fieldValue", xmlWriter);
                             

                                          if (localFieldValue==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localFieldValue);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localInputValuesTracker){
                             if (localInputValues!=null) {
                                   namespace = "http://dto.registration.user.identity.carbon.wso2.org/xsd";
                                   for (int i = 0;i < localInputValues.length;i++){
                                        
                                            if (localInputValues[i] != null){
                                        
                                                writeStartElement(null, namespace, "inputValues", xmlWriter);

                                            
                                                        xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localInputValues[i]));
                                                    
                                                xmlWriter.writeEndElement();
                                              
                                                } else {
                                                   
                                                           // write null attribute
                                                            namespace = "http://dto.registration.user.identity.carbon.wso2.org/xsd";
                                                            writeStartElement(null, namespace, "inputValues", xmlWriter);
                                                            writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                            xmlWriter.writeEndElement();
                                                       
                                                }

                                   }
                             } else {
                                 
                                         // write the null attribute
                                        // write null attribute
                                           writeStartElement(null, "http://dto.registration.user.identity.carbon.wso2.org/xsd", "inputValues", xmlWriter);

                                           // write the nil attribute
                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                           xmlWriter.writeEndElement();
                                    
                             }

                        } if (localMaxLengthTracker){
                                    namespace = "http://dto.registration.user.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "maxLength", xmlWriter);
                             
                                               if (localMaxLength==java.lang.Integer.MIN_VALUE) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("maxLength cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localMaxLength));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localMinLengthTracker){
                                    namespace = "http://dto.registration.user.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "minLength", xmlWriter);
                             
                                               if (localMinLength==java.lang.Integer.MIN_VALUE) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("minLength cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localMinLength));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localRegExTracker){
                                    namespace = "http://dto.registration.user.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "regEx", xmlWriter);
                             

                                          if (localRegEx==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localRegEx);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localRequiredTracker){
                                    namespace = "http://dto.registration.user.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "required", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("required cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localRequired));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localSupportedByDefaultTracker){
                                    namespace = "http://dto.registration.user.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "supportedByDefault", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("supportedByDefault cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSupportedByDefault));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             }
                    xmlWriter.writeEndElement();
               

        }

        private static java.lang.String generatePrefix(java.lang.String namespace) {
            if(namespace.equals("http://dto.registration.user.identity.carbon.wso2.org/xsd")){
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

                 if (localClaimUriTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://dto.registration.user.identity.carbon.wso2.org/xsd",
                                                                      "claimUri"));
                                 
                                         elementList.add(localClaimUri==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localClaimUri));
                                    } if (localDefaultValueTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://dto.registration.user.identity.carbon.wso2.org/xsd",
                                                                      "defaultValue"));
                                 
                                         elementList.add(localDefaultValue==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDefaultValue));
                                    } if (localDisplayOrderTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://dto.registration.user.identity.carbon.wso2.org/xsd",
                                                                      "displayOrder"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDisplayOrder));
                            } if (localFieldNameTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://dto.registration.user.identity.carbon.wso2.org/xsd",
                                                                      "fieldName"));
                                 
                                         elementList.add(localFieldName==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localFieldName));
                                    } if (localFieldValueTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://dto.registration.user.identity.carbon.wso2.org/xsd",
                                                                      "fieldValue"));
                                 
                                         elementList.add(localFieldValue==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localFieldValue));
                                    } if (localInputValuesTracker){
                            if (localInputValues!=null){
                                  for (int i = 0;i < localInputValues.length;i++){
                                      
                                         if (localInputValues[i] != null){
                                          elementList.add(new javax.xml.namespace.QName("http://dto.registration.user.identity.carbon.wso2.org/xsd",
                                                                              "inputValues"));
                                          elementList.add(
                                          org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localInputValues[i]));
                                          } else {
                                             
                                                    elementList.add(new javax.xml.namespace.QName("http://dto.registration.user.identity.carbon.wso2.org/xsd",
                                                                              "inputValues"));
                                                    elementList.add(null);
                                                
                                          }
                                      

                                  }
                            } else {
                              
                                    elementList.add(new javax.xml.namespace.QName("http://dto.registration.user.identity.carbon.wso2.org/xsd",
                                                                              "inputValues"));
                                    elementList.add(null);
                                
                            }

                        } if (localMaxLengthTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://dto.registration.user.identity.carbon.wso2.org/xsd",
                                                                      "maxLength"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localMaxLength));
                            } if (localMinLengthTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://dto.registration.user.identity.carbon.wso2.org/xsd",
                                                                      "minLength"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localMinLength));
                            } if (localRegExTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://dto.registration.user.identity.carbon.wso2.org/xsd",
                                                                      "regEx"));
                                 
                                         elementList.add(localRegEx==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localRegEx));
                                    } if (localRequiredTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://dto.registration.user.identity.carbon.wso2.org/xsd",
                                                                      "required"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localRequired));
                            } if (localSupportedByDefaultTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://dto.registration.user.identity.carbon.wso2.org/xsd",
                                                                      "supportedByDefault"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSupportedByDefault));
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
        public static UserFieldDTO parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            UserFieldDTO object =
                new UserFieldDTO();

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
                    
                            if (!"UserFieldDTO".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (UserFieldDTO)org.wso2.carbon.identity.user.registration.stub.dto.ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }
                        

                  }
                

                }

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                
                    
                    reader.next();
                
                        java.util.ArrayList list6 = new java.util.ArrayList();
                    
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://dto.registration.user.identity.carbon.wso2.org/xsd","claimUri").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setClaimUri(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://dto.registration.user.identity.carbon.wso2.org/xsd","defaultValue").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setDefaultValue(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://dto.registration.user.identity.carbon.wso2.org/xsd","displayOrder").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"displayOrder" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setDisplayOrder(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                               object.setDisplayOrder(java.lang.Integer.MIN_VALUE);
                                           
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://dto.registration.user.identity.carbon.wso2.org/xsd","fieldName").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setFieldName(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://dto.registration.user.identity.carbon.wso2.org/xsd","fieldValue").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setFieldValue(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://dto.registration.user.identity.carbon.wso2.org/xsd","inputValues").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                              nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                              if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                  list6.add(null);
                                                       
                                                  reader.next();
                                              } else {
                                            list6.add(reader.getElementText());
                                            }
                                            //loop until we find a start element that is not part of this array
                                            boolean loopDone6 = false;
                                            while(!loopDone6){
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
                                                    loopDone6 = true;
                                                } else {
                                                    if (new javax.xml.namespace.QName("http://dto.registration.user.identity.carbon.wso2.org/xsd","inputValues").equals(reader.getName())){
                                                         
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list6.add(null);
                                                                   
                                                              reader.next();
                                                          } else {
                                                        list6.add(reader.getElementText());
                                                        }
                                                    }else{
                                                        loopDone6 = true;
                                                    }
                                                }
                                            }
                                            // call the converter utility  to convert and set the array
                                            
                                                    object.setInputValues((java.lang.String[])
                                                        list6.toArray(new java.lang.String[list6.size()]));
                                                
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://dto.registration.user.identity.carbon.wso2.org/xsd","maxLength").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"maxLength" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setMaxLength(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                               object.setMaxLength(java.lang.Integer.MIN_VALUE);
                                           
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://dto.registration.user.identity.carbon.wso2.org/xsd","minLength").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"minLength" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setMinLength(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                               object.setMinLength(java.lang.Integer.MIN_VALUE);
                                           
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://dto.registration.user.identity.carbon.wso2.org/xsd","regEx").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setRegEx(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://dto.registration.user.identity.carbon.wso2.org/xsd","required").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"required" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setRequired(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://dto.registration.user.identity.carbon.wso2.org/xsd","supportedByDefault").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"supportedByDefault" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setSupportedByDefault(
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
           
    