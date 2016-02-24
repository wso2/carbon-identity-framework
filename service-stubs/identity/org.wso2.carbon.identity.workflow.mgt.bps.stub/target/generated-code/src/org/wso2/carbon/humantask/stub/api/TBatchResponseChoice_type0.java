
/**
 * TBatchResponseChoice_type0.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:46 UTC)
 */

            
                package org.wso2.carbon.humantask.stub.api;
            

            /**
            *  TBatchResponseChoice_type0 bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class TBatchResponseChoice_type0
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = tBatchResponseChoice_type0
                Namespace URI = http://docs.oasis-open.org/ns/bpel4people/ws-humantask/api/200803
                Namespace Prefix = ns2
                */
            
            /** Whenever a new property is set ensure all others are unset
             *  There can be only one choice and the last one wins
             */
            private void clearAllSettingTrackers() {
            
                   localIllegalStateTracker = false;
                
                   localIllegalArgumentTracker = false;
                
                   localIllegalAccessTracker = false;
                
                   localIllegalOperationTracker = false;
                
                   localRecipientNotAllowedTracker = false;
                
                   localExtraElementTracker = false;
                
            }
        

                        /**
                        * field for IllegalState
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.api.IllegalState_type0 localIllegalState ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localIllegalStateTracker = false ;

                           public boolean isIllegalStateSpecified(){
                               return localIllegalStateTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.api.IllegalState_type0
                           */
                           public  org.wso2.carbon.humantask.stub.api.IllegalState_type0 getIllegalState(){
                               return localIllegalState;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param IllegalState
                               */
                               public void setIllegalState(org.wso2.carbon.humantask.stub.api.IllegalState_type0 param){
                            
                                clearAllSettingTrackers();
                            localIllegalStateTracker = param != null;
                                   
                                            this.localIllegalState=param;
                                    

                               }
                            

                        /**
                        * field for IllegalArgument
                        */

                        
                                    protected java.lang.String localIllegalArgument ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localIllegalArgumentTracker = false ;

                           public boolean isIllegalArgumentSpecified(){
                               return localIllegalArgumentTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getIllegalArgument(){
                               return localIllegalArgument;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param IllegalArgument
                               */
                               public void setIllegalArgument(java.lang.String param){
                            
                                clearAllSettingTrackers();
                            localIllegalArgumentTracker = param != null;
                                   
                                            this.localIllegalArgument=param;
                                    

                               }
                            

                        /**
                        * field for IllegalAccess
                        */

                        
                                    protected java.lang.String localIllegalAccess ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localIllegalAccessTracker = false ;

                           public boolean isIllegalAccessSpecified(){
                               return localIllegalAccessTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getIllegalAccess(){
                               return localIllegalAccess;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param IllegalAccess
                               */
                               public void setIllegalAccess(java.lang.String param){
                            
                                clearAllSettingTrackers();
                            localIllegalAccessTracker = param != null;
                                   
                                            this.localIllegalAccess=param;
                                    

                               }
                            

                        /**
                        * field for IllegalOperation
                        */

                        
                                    protected java.lang.String localIllegalOperation ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localIllegalOperationTracker = false ;

                           public boolean isIllegalOperationSpecified(){
                               return localIllegalOperationTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getIllegalOperation(){
                               return localIllegalOperation;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param IllegalOperation
                               */
                               public void setIllegalOperation(java.lang.String param){
                            
                                clearAllSettingTrackers();
                            localIllegalOperationTracker = param != null;
                                   
                                            this.localIllegalOperation=param;
                                    

                               }
                            

                        /**
                        * field for RecipientNotAllowed
                        */

                        
                                    protected java.lang.String localRecipientNotAllowed ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localRecipientNotAllowedTracker = false ;

                           public boolean isRecipientNotAllowedSpecified(){
                               return localRecipientNotAllowedTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getRecipientNotAllowed(){
                               return localRecipientNotAllowed;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param RecipientNotAllowed
                               */
                               public void setRecipientNotAllowed(java.lang.String param){
                            
                                clearAllSettingTrackers();
                            localRecipientNotAllowedTracker = param != null;
                                   
                                            this.localRecipientNotAllowed=param;
                                    

                               }
                            

                        /**
                        * field for ExtraElement
                        */

                        
                                    protected org.apache.axiom.om.OMElement localExtraElement ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localExtraElementTracker = false ;

                           public boolean isExtraElementSpecified(){
                               return localExtraElementTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.apache.axiom.om.OMElement
                           */
                           public  org.apache.axiom.om.OMElement getExtraElement(){
                               return localExtraElement;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ExtraElement
                               */
                               public void setExtraElement(org.apache.axiom.om.OMElement param){
                            
                                clearAllSettingTrackers();
                            localExtraElementTracker = param != null;
                                   
                                            this.localExtraElement=param;
                                    

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
                
                  if (serializeType){
               

                   java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://docs.oasis-open.org/ns/bpel4people/ws-humantask/api/200803");
                   if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           namespacePrefix+":tBatchResponseChoice_type0",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "tBatchResponseChoice_type0",
                           xmlWriter);
                   }

               
                   }
                if (localIllegalStateTracker){
                                            if (localIllegalState==null){
                                                 throw new org.apache.axis2.databinding.ADBException("illegalState cannot be null!!");
                                            }
                                           localIllegalState.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/api/200803","illegalState"),
                                               xmlWriter);
                                        } if (localIllegalArgumentTracker){
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/api/200803";
                                    writeStartElement(null, namespace, "illegalArgument", xmlWriter);
                             

                                          if (localIllegalArgument==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("illegalArgument cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localIllegalArgument);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localIllegalAccessTracker){
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/api/200803";
                                    writeStartElement(null, namespace, "illegalAccess", xmlWriter);
                             

                                          if (localIllegalAccess==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("illegalAccess cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localIllegalAccess);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localIllegalOperationTracker){
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/api/200803";
                                    writeStartElement(null, namespace, "illegalOperation", xmlWriter);
                             

                                          if (localIllegalOperation==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("illegalOperation cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localIllegalOperation);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localRecipientNotAllowedTracker){
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/api/200803";
                                    writeStartElement(null, namespace, "recipientNotAllowed", xmlWriter);
                             

                                          if (localRecipientNotAllowed==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("recipientNotAllowed cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localRecipientNotAllowed);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localExtraElementTracker){
                            
                            if (localExtraElement != null) {
                                localExtraElement.serialize(xmlWriter);
                            } else {
                               throw new org.apache.axis2.databinding.ADBException("extraElement cannot be null!!");
                            }
                        }

        }

        private static java.lang.String generatePrefix(java.lang.String namespace) {
            if(namespace.equals("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/api/200803")){
                return "ns2";
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

                 if (localIllegalStateTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/api/200803",
                                                                      "illegalState"));
                            
                            
                                    if (localIllegalState==null){
                                         throw new org.apache.axis2.databinding.ADBException("illegalState cannot be null!!");
                                    }
                                    elementList.add(localIllegalState);
                                } if (localIllegalArgumentTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/api/200803",
                                                                      "illegalArgument"));
                                 
                                        if (localIllegalArgument != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIllegalArgument));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("illegalArgument cannot be null!!");
                                        }
                                    } if (localIllegalAccessTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/api/200803",
                                                                      "illegalAccess"));
                                 
                                        if (localIllegalAccess != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIllegalAccess));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("illegalAccess cannot be null!!");
                                        }
                                    } if (localIllegalOperationTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/api/200803",
                                                                      "illegalOperation"));
                                 
                                        if (localIllegalOperation != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIllegalOperation));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("illegalOperation cannot be null!!");
                                        }
                                    } if (localRecipientNotAllowedTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/api/200803",
                                                                      "recipientNotAllowed"));
                                 
                                        if (localRecipientNotAllowed != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localRecipientNotAllowed));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("recipientNotAllowed cannot be null!!");
                                        }
                                    } if (localExtraElementTracker){
                            if (localExtraElement != null){
                                elementList.add(org.apache.axis2.databinding.utils.Constants.OM_ELEMENT_KEY);
                                elementList.add(localExtraElement);
                            } else {
                               throw new org.apache.axis2.databinding.ADBException("extraElement cannot be null!!");
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
        public static TBatchResponseChoice_type0 parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            TBatchResponseChoice_type0 object =
                new TBatchResponseChoice_type0();

            int event;
            java.lang.String nillableValue = null;
            java.lang.String prefix ="";
            java.lang.String namespaceuri ="";
            try {
                
                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/api/200803","illegalState").equals(reader.getName())){
                                
                                                object.setIllegalState(org.wso2.carbon.humantask.stub.api.IllegalState_type0.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/api/200803","illegalArgument").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"illegalArgument" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setIllegalArgument(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/api/200803","illegalAccess").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"illegalAccess" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setIllegalAccess(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/api/200803","illegalOperation").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"illegalOperation" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setIllegalOperation(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/api/200803","recipientNotAllowed").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"recipientNotAllowed" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setRecipientNotAllowed(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                   if (reader.isStartElement()){
                                
                                    
                                     
                                     //use the QName from the parser as the name for the builder
                                     javax.xml.namespace.QName startQname6 = reader.getName();

                                     // We need to wrap the reader so that it produces a fake START_DOCUMENT event
                                     // this is needed by the builder classes
                                     org.apache.axis2.databinding.utils.NamedStaxOMBuilder builder6 =
                                         new org.apache.axis2.databinding.utils.NamedStaxOMBuilder(
                                             new org.apache.axis2.util.StreamWrapper(reader),startQname6);
                                     object.setExtraElement(builder6.getOMElement());
                                       
                                         reader.next();
                                     
                              }  // End of if for expected property start element
                                



            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class

        

        }
           
    