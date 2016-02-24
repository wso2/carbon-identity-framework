
/**
 * ParameterMetaData.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:46 UTC)
 */

            
                package org.wso2.carbon.identity.workflow.mgt.stub.metadata.bean;
            

            /**
            *  ParameterMetaData bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class ParameterMetaData
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = ParameterMetaData
                Namespace URI = http://metadata.bean.mgt.workflow.identity.carbon.wso2.org/xsd
                Namespace Prefix = ns3
                */
            

                        /**
                        * field for CustomInputType
                        */

                        
                                    protected java.lang.String localCustomInputType ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localCustomInputTypeTracker = false ;

                           public boolean isCustomInputTypeSpecified(){
                               return localCustomInputTypeTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getCustomInputType(){
                               return localCustomInputType;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param CustomInputType
                               */
                               public void setCustomInputType(java.lang.String param){
                            localCustomInputTypeTracker = true;
                                   
                                            this.localCustomInputType=param;
                                    

                               }
                            

                        /**
                        * field for DataType
                        */

                        
                                    protected java.lang.String localDataType ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localDataTypeTracker = false ;

                           public boolean isDataTypeSpecified(){
                               return localDataTypeTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getDataType(){
                               return localDataType;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param DataType
                               */
                               public void setDataType(java.lang.String param){
                            localDataTypeTracker = true;
                                   
                                            this.localDataType=param;
                                    

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
                        * field for InputData
                        */

                        
                                    protected org.wso2.carbon.identity.workflow.mgt.stub.metadata.bean.InputData localInputData ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localInputDataTracker = false ;

                           public boolean isInputDataSpecified(){
                               return localInputDataTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.identity.workflow.mgt.stub.metadata.bean.InputData
                           */
                           public  org.wso2.carbon.identity.workflow.mgt.stub.metadata.bean.InputData getInputData(){
                               return localInputData;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param InputData
                               */
                               public void setInputData(org.wso2.carbon.identity.workflow.mgt.stub.metadata.bean.InputData param){
                            localInputDataTracker = true;
                                   
                                            this.localInputData=param;
                                    

                               }
                            

                        /**
                        * field for InputType
                        */

                        
                                    protected java.lang.String localInputType ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localInputTypeTracker = false ;

                           public boolean isInputTypeSpecified(){
                               return localInputTypeTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getInputType(){
                               return localInputType;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param InputType
                               */
                               public void setInputType(java.lang.String param){
                            localInputTypeTracker = true;
                                   
                                            this.localInputType=param;
                                    

                               }
                            

                        /**
                        * field for IsInputDataRequired
                        */

                        
                                    protected boolean localIsInputDataRequired ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localIsInputDataRequiredTracker = false ;

                           public boolean isIsInputDataRequiredSpecified(){
                               return localIsInputDataRequiredTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getIsInputDataRequired(){
                               return localIsInputDataRequired;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param IsInputDataRequired
                               */
                               public void setIsInputDataRequired(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localIsInputDataRequiredTracker =
                                       true;
                                   
                                            this.localIsInputDataRequired=param;
                                    

                               }
                            

                        /**
                        * field for IsRequired
                        */

                        
                                    protected boolean localIsRequired ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localIsRequiredTracker = false ;

                           public boolean isIsRequiredSpecified(){
                               return localIsRequiredTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getIsRequired(){
                               return localIsRequired;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param IsRequired
                               */
                               public void setIsRequired(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localIsRequiredTracker =
                                       true;
                                   
                                            this.localIsRequired=param;
                                    

                               }
                            

                        /**
                        * field for Name
                        */

                        
                                    protected java.lang.String localName ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localNameTracker = false ;

                           public boolean isNameSpecified(){
                               return localNameTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getName(){
                               return localName;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Name
                               */
                               public void setName(java.lang.String param){
                            localNameTracker = true;
                                   
                                            this.localName=param;
                                    

                               }
                            

                        /**
                        * field for RegExForValidate
                        */

                        
                                    protected java.lang.String localRegExForValidate ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localRegExForValidateTracker = false ;

                           public boolean isRegExForValidateSpecified(){
                               return localRegExForValidateTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getRegExForValidate(){
                               return localRegExForValidate;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param RegExForValidate
                               */
                               public void setRegExForValidate(java.lang.String param){
                            localRegExForValidateTracker = true;
                                   
                                            this.localRegExForValidate=param;
                                    

                               }
                            

                        /**
                        * field for Validate
                        */

                        
                                    protected boolean localValidate ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localValidateTracker = false ;

                           public boolean isValidateSpecified(){
                               return localValidateTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getValidate(){
                               return localValidate;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Validate
                               */
                               public void setValidate(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localValidateTracker =
                                       true;
                                   
                                            this.localValidate=param;
                                    

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
               

                   java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://metadata.bean.mgt.workflow.identity.carbon.wso2.org/xsd");
                   if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           namespacePrefix+":ParameterMetaData",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "ParameterMetaData",
                           xmlWriter);
                   }

               
                   }
                if (localCustomInputTypeTracker){
                                    namespace = "http://metadata.bean.mgt.workflow.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "customInputType", xmlWriter);
                             

                                          if (localCustomInputType==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localCustomInputType);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localDataTypeTracker){
                                    namespace = "http://metadata.bean.mgt.workflow.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "dataType", xmlWriter);
                             

                                          if (localDataType==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localDataType);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localDefaultValueTracker){
                                    namespace = "http://metadata.bean.mgt.workflow.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "defaultValue", xmlWriter);
                             

                                          if (localDefaultValue==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localDefaultValue);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localDisplayNameTracker){
                                    namespace = "http://metadata.bean.mgt.workflow.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "displayName", xmlWriter);
                             

                                          if (localDisplayName==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localDisplayName);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localInputDataTracker){
                                    if (localInputData==null){

                                        writeStartElement(null, "http://metadata.bean.mgt.workflow.identity.carbon.wso2.org/xsd", "inputData", xmlWriter);

                                       // write the nil attribute
                                      writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                      xmlWriter.writeEndElement();
                                    }else{
                                     localInputData.serialize(new javax.xml.namespace.QName("http://metadata.bean.mgt.workflow.identity.carbon.wso2.org/xsd","inputData"),
                                        xmlWriter);
                                    }
                                } if (localInputTypeTracker){
                                    namespace = "http://metadata.bean.mgt.workflow.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "inputType", xmlWriter);
                             

                                          if (localInputType==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localInputType);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localIsInputDataRequiredTracker){
                                    namespace = "http://metadata.bean.mgt.workflow.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "isInputDataRequired", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("isInputDataRequired cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIsInputDataRequired));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localIsRequiredTracker){
                                    namespace = "http://metadata.bean.mgt.workflow.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "isRequired", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("isRequired cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIsRequired));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localNameTracker){
                                    namespace = "http://metadata.bean.mgt.workflow.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "name", xmlWriter);
                             

                                          if (localName==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localName);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localRegExForValidateTracker){
                                    namespace = "http://metadata.bean.mgt.workflow.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "regExForValidate", xmlWriter);
                             

                                          if (localRegExForValidate==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localRegExForValidate);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localValidateTracker){
                                    namespace = "http://metadata.bean.mgt.workflow.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "validate", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("validate cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localValidate));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             }
                    xmlWriter.writeEndElement();
               

        }

        private static java.lang.String generatePrefix(java.lang.String namespace) {
            if(namespace.equals("http://metadata.bean.mgt.workflow.identity.carbon.wso2.org/xsd")){
                return "ns3";
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

                 if (localCustomInputTypeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://metadata.bean.mgt.workflow.identity.carbon.wso2.org/xsd",
                                                                      "customInputType"));
                                 
                                         elementList.add(localCustomInputType==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCustomInputType));
                                    } if (localDataTypeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://metadata.bean.mgt.workflow.identity.carbon.wso2.org/xsd",
                                                                      "dataType"));
                                 
                                         elementList.add(localDataType==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDataType));
                                    } if (localDefaultValueTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://metadata.bean.mgt.workflow.identity.carbon.wso2.org/xsd",
                                                                      "defaultValue"));
                                 
                                         elementList.add(localDefaultValue==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDefaultValue));
                                    } if (localDisplayNameTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://metadata.bean.mgt.workflow.identity.carbon.wso2.org/xsd",
                                                                      "displayName"));
                                 
                                         elementList.add(localDisplayName==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDisplayName));
                                    } if (localInputDataTracker){
                            elementList.add(new javax.xml.namespace.QName("http://metadata.bean.mgt.workflow.identity.carbon.wso2.org/xsd",
                                                                      "inputData"));
                            
                            
                                    elementList.add(localInputData==null?null:
                                    localInputData);
                                } if (localInputTypeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://metadata.bean.mgt.workflow.identity.carbon.wso2.org/xsd",
                                                                      "inputType"));
                                 
                                         elementList.add(localInputType==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localInputType));
                                    } if (localIsInputDataRequiredTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://metadata.bean.mgt.workflow.identity.carbon.wso2.org/xsd",
                                                                      "isInputDataRequired"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIsInputDataRequired));
                            } if (localIsRequiredTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://metadata.bean.mgt.workflow.identity.carbon.wso2.org/xsd",
                                                                      "isRequired"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIsRequired));
                            } if (localNameTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://metadata.bean.mgt.workflow.identity.carbon.wso2.org/xsd",
                                                                      "name"));
                                 
                                         elementList.add(localName==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localName));
                                    } if (localRegExForValidateTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://metadata.bean.mgt.workflow.identity.carbon.wso2.org/xsd",
                                                                      "regExForValidate"));
                                 
                                         elementList.add(localRegExForValidate==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localRegExForValidate));
                                    } if (localValidateTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://metadata.bean.mgt.workflow.identity.carbon.wso2.org/xsd",
                                                                      "validate"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localValidate));
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
        public static ParameterMetaData parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            ParameterMetaData object =
                new ParameterMetaData();

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
                    
                            if (!"ParameterMetaData".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (ParameterMetaData)org.wso2.carbon.identity.workflow.mgt.stub.ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }
                        

                  }
                

                }

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                
                    
                    reader.next();
                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://metadata.bean.mgt.workflow.identity.carbon.wso2.org/xsd","customInputType").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setCustomInputType(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://metadata.bean.mgt.workflow.identity.carbon.wso2.org/xsd","dataType").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setDataType(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://metadata.bean.mgt.workflow.identity.carbon.wso2.org/xsd","defaultValue").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://metadata.bean.mgt.workflow.identity.carbon.wso2.org/xsd","displayName").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://metadata.bean.mgt.workflow.identity.carbon.wso2.org/xsd","inputData").equals(reader.getName())){
                                
                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                          object.setInputData(null);
                                          reader.next();
                                            
                                            reader.next();
                                          
                                      }else{
                                    
                                                object.setInputData(org.wso2.carbon.identity.workflow.mgt.stub.metadata.bean.InputData.Factory.parse(reader));
                                              
                                        reader.next();
                                    }
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://metadata.bean.mgt.workflow.identity.carbon.wso2.org/xsd","inputType").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setInputType(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://metadata.bean.mgt.workflow.identity.carbon.wso2.org/xsd","isInputDataRequired").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"isInputDataRequired" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setIsInputDataRequired(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://metadata.bean.mgt.workflow.identity.carbon.wso2.org/xsd","isRequired").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"isRequired" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setIsRequired(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://metadata.bean.mgt.workflow.identity.carbon.wso2.org/xsd","name").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setName(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://metadata.bean.mgt.workflow.identity.carbon.wso2.org/xsd","regExForValidate").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setRegExForValidate(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://metadata.bean.mgt.workflow.identity.carbon.wso2.org/xsd","validate").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"validate" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setValidate(
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
           
    