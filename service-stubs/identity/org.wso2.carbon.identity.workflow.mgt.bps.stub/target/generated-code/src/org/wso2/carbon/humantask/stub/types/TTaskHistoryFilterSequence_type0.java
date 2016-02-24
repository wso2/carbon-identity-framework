
/**
 * TTaskHistoryFilterSequence_type0.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:46 UTC)
 */

            
                package org.wso2.carbon.humantask.stub.types;
            

            /**
            *  TTaskHistoryFilterSequence_type0 bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class TTaskHistoryFilterSequence_type0
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = tTaskHistoryFilterSequence_type0
                Namespace URI = http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803
                Namespace Prefix = ns1
                */
            

                        /**
                        * field for Status
                        * This was an Array!
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TStatus[] localStatus ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localStatusTracker = false ;

                           public boolean isStatusSpecified(){
                               return localStatusTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TStatus[]
                           */
                           public  org.wso2.carbon.humantask.stub.types.TStatus[] getStatus(){
                               return localStatus;
                           }

                           
                        


                               
                              /**
                               * validate the array for Status
                               */
                              protected void validateStatus(org.wso2.carbon.humantask.stub.types.TStatus[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param Status
                              */
                              public void setStatus(org.wso2.carbon.humantask.stub.types.TStatus[] param){
                              
                                   validateStatus(param);

                               localStatusTracker = param != null;
                                      
                                      this.localStatus=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param org.wso2.carbon.humantask.stub.types.TStatus
                             */
                             public void addStatus(org.wso2.carbon.humantask.stub.types.TStatus param){
                                   if (localStatus == null){
                                   localStatus = new org.wso2.carbon.humantask.stub.types.TStatus[]{};
                                   }

                            
                                 //update the setting tracker
                                localStatusTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localStatus);
                               list.add(param);
                               this.localStatus =
                             (org.wso2.carbon.humantask.stub.types.TStatus[])list.toArray(
                            new org.wso2.carbon.humantask.stub.types.TStatus[list.size()]);

                             }
                             

                        /**
                        * field for EventType
                        * This was an Array!
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskEventType[] localEventType ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localEventTypeTracker = false ;

                           public boolean isEventTypeSpecified(){
                               return localEventTypeTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskEventType[]
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskEventType[] getEventType(){
                               return localEventType;
                           }

                           
                        


                               
                              /**
                               * validate the array for EventType
                               */
                              protected void validateEventType(org.wso2.carbon.humantask.stub.types.TTaskEventType[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param EventType
                              */
                              public void setEventType(org.wso2.carbon.humantask.stub.types.TTaskEventType[] param){
                              
                                   validateEventType(param);

                               localEventTypeTracker = param != null;
                                      
                                      this.localEventType=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param org.wso2.carbon.humantask.stub.types.TTaskEventType
                             */
                             public void addEventType(org.wso2.carbon.humantask.stub.types.TTaskEventType param){
                                   if (localEventType == null){
                                   localEventType = new org.wso2.carbon.humantask.stub.types.TTaskEventType[]{};
                                   }

                            
                                 //update the setting tracker
                                localEventTypeTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localEventType);
                               list.add(param);
                               this.localEventType =
                             (org.wso2.carbon.humantask.stub.types.TTaskEventType[])list.toArray(
                            new org.wso2.carbon.humantask.stub.types.TTaskEventType[list.size()]);

                             }
                             

                        /**
                        * field for Principal
                        */

                        
                                    protected java.lang.String localPrincipal ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPrincipalTracker = false ;

                           public boolean isPrincipalSpecified(){
                               return localPrincipalTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getPrincipal(){
                               return localPrincipal;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Principal
                               */
                               public void setPrincipal(java.lang.String param){
                            localPrincipalTracker = param != null;
                                   
                                            this.localPrincipal=param;
                                    

                               }
                            

                        /**
                        * field for AfterEventTime
                        */

                        
                                    protected java.util.Calendar localAfterEventTime ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAfterEventTimeTracker = false ;

                           public boolean isAfterEventTimeSpecified(){
                               return localAfterEventTimeTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.util.Calendar
                           */
                           public  java.util.Calendar getAfterEventTime(){
                               return localAfterEventTime;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param AfterEventTime
                               */
                               public void setAfterEventTime(java.util.Calendar param){
                            localAfterEventTimeTracker = param != null;
                                   
                                            this.localAfterEventTime=param;
                                    

                               }
                            

                        /**
                        * field for BeforeEventTime
                        */

                        
                                    protected java.util.Calendar localBeforeEventTime ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localBeforeEventTimeTracker = false ;

                           public boolean isBeforeEventTimeSpecified(){
                               return localBeforeEventTimeTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.util.Calendar
                           */
                           public  java.util.Calendar getBeforeEventTime(){
                               return localBeforeEventTime;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param BeforeEventTime
                               */
                               public void setBeforeEventTime(java.util.Calendar param){
                            localBeforeEventTimeTracker = param != null;
                                   
                                            this.localBeforeEventTime=param;
                                    

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
               

                   java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803");
                   if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           namespacePrefix+":tTaskHistoryFilterSequence_type0",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "tTaskHistoryFilterSequence_type0",
                           xmlWriter);
                   }

               
                   }
                if (localStatusTracker){
                                       if (localStatus!=null){
                                            for (int i = 0;i < localStatus.length;i++){
                                                if (localStatus[i] != null){
                                                 localStatus[i].serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","status"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                        // we don't have to do any thing since minOccures is zero
                                                    
                                                }

                                            }
                                     } else {
                                        
                                               throw new org.apache.axis2.databinding.ADBException("status cannot be null!!");
                                        
                                    }
                                 } if (localEventTypeTracker){
                                       if (localEventType!=null){
                                            for (int i = 0;i < localEventType.length;i++){
                                                if (localEventType[i] != null){
                                                 localEventType[i].serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","eventType"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                        // we don't have to do any thing since minOccures is zero
                                                    
                                                }

                                            }
                                     } else {
                                        
                                               throw new org.apache.axis2.databinding.ADBException("eventType cannot be null!!");
                                        
                                    }
                                 } if (localPrincipalTracker){
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803";
                                    writeStartElement(null, namespace, "principal", xmlWriter);
                             

                                          if (localPrincipal==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("principal cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localPrincipal);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localAfterEventTimeTracker){
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803";
                                    writeStartElement(null, namespace, "afterEventTime", xmlWriter);
                             

                                          if (localAfterEventTime==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("afterEventTime cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAfterEventTime));
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localBeforeEventTimeTracker){
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803";
                                    writeStartElement(null, namespace, "beforeEventTime", xmlWriter);
                             

                                          if (localBeforeEventTime==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("beforeEventTime cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localBeforeEventTime));
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             }

        }

        private static java.lang.String generatePrefix(java.lang.String namespace) {
            if(namespace.equals("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803")){
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

                 if (localStatusTracker){
                             if (localStatus!=null) {
                                 for (int i = 0;i < localStatus.length;i++){

                                    if (localStatus[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                          "status"));
                                         elementList.add(localStatus[i]);
                                    } else {
                                        
                                                // nothing to do
                                            
                                    }

                                 }
                             } else {
                                 
                                        throw new org.apache.axis2.databinding.ADBException("status cannot be null!!");
                                    
                             }

                        } if (localEventTypeTracker){
                             if (localEventType!=null) {
                                 for (int i = 0;i < localEventType.length;i++){

                                    if (localEventType[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                          "eventType"));
                                         elementList.add(localEventType[i]);
                                    } else {
                                        
                                                // nothing to do
                                            
                                    }

                                 }
                             } else {
                                 
                                        throw new org.apache.axis2.databinding.ADBException("eventType cannot be null!!");
                                    
                             }

                        } if (localPrincipalTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "principal"));
                                 
                                        if (localPrincipal != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPrincipal));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("principal cannot be null!!");
                                        }
                                    } if (localAfterEventTimeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "afterEventTime"));
                                 
                                        if (localAfterEventTime != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAfterEventTime));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("afterEventTime cannot be null!!");
                                        }
                                    } if (localBeforeEventTimeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "beforeEventTime"));
                                 
                                        if (localBeforeEventTime != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localBeforeEventTime));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("beforeEventTime cannot be null!!");
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
        public static TTaskHistoryFilterSequence_type0 parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            TTaskHistoryFilterSequence_type0 object =
                new TTaskHistoryFilterSequence_type0();

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
                

                
                        java.util.ArrayList list1 = new java.util.ArrayList();
                    
                        java.util.ArrayList list2 = new java.util.ArrayList();
                    
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","status").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    list1.add(org.wso2.carbon.humantask.stub.types.TStatus.Factory.parse(reader));
                                                                
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone1 = false;
                                                        while(!loopDone1){
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
                                                                loopDone1 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","status").equals(reader.getName())){
                                                                    list1.add(org.wso2.carbon.humantask.stub.types.TStatus.Factory.parse(reader));
                                                                        
                                                                }else{
                                                                    loopDone1 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setStatus((org.wso2.carbon.humantask.stub.types.TStatus[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                org.wso2.carbon.humantask.stub.types.TStatus.class,
                                                                list1));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","eventType").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    list2.add(org.wso2.carbon.humantask.stub.types.TTaskEventType.Factory.parse(reader));
                                                                
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
                                                                if (new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","eventType").equals(reader.getName())){
                                                                    list2.add(org.wso2.carbon.humantask.stub.types.TTaskEventType.Factory.parse(reader));
                                                                        
                                                                }else{
                                                                    loopDone2 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setEventType((org.wso2.carbon.humantask.stub.types.TTaskEventType[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                org.wso2.carbon.humantask.stub.types.TTaskEventType.class,
                                                                list2));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","principal").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"principal" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setPrincipal(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","afterEventTime").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"afterEventTime" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setAfterEventTime(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToDateTime(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","beforeEventTime").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"beforeEventTime" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setBeforeEventTime(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToDateTime(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                  
                            while (!reader.isStartElement() && !reader.isEndElement())
                                reader.next();
                            



            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class

        

        }
           
    