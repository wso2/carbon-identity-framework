
/**
 * ActivityInfoType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:46 UTC)
 */

            
                package org.wso2.carbon.bpel.stub.mgt.types;
            

            /**
            *  ActivityInfoType bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class ActivityInfoType
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = ActivityInfoType
                Namespace URI = http://wso2.org/bps/management/schema
                Namespace Prefix = ns1
                */
            

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
                            localNameTracker = param != null;
                                   
                                            this.localName=param;
                                    

                               }
                            

                        /**
                        * field for Type
                        */

                        
                                    protected java.lang.String localType ;
                                

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getType(){
                               return localType;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Type
                               */
                               public void setType(java.lang.String param){
                            
                                            this.localType=param;
                                    

                               }
                            

                        /**
                        * field for Aiid
                        */

                        
                                    protected java.lang.String localAiid ;
                                

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getAiid(){
                               return localAiid;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Aiid
                               */
                               public void setAiid(java.lang.String param){
                            
                                            this.localAiid=param;
                                    

                               }
                            

                        /**
                        * field for Status
                        */

                        
                                    protected org.wso2.carbon.bpel.stub.mgt.types.ActivityStatusType localStatus ;
                                

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.bpel.stub.mgt.types.ActivityStatusType
                           */
                           public  org.wso2.carbon.bpel.stub.mgt.types.ActivityStatusType getStatus(){
                               return localStatus;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Status
                               */
                               public void setStatus(org.wso2.carbon.bpel.stub.mgt.types.ActivityStatusType param){
                            
                                            this.localStatus=param;
                                    

                               }
                            

                        /**
                        * field for DateEnabled
                        */

                        
                                    protected java.util.Calendar localDateEnabled ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localDateEnabledTracker = false ;

                           public boolean isDateEnabledSpecified(){
                               return localDateEnabledTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.util.Calendar
                           */
                           public  java.util.Calendar getDateEnabled(){
                               return localDateEnabled;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param DateEnabled
                               */
                               public void setDateEnabled(java.util.Calendar param){
                            localDateEnabledTracker = param != null;
                                   
                                            this.localDateEnabled=param;
                                    

                               }
                            

                        /**
                        * field for DateStarted
                        */

                        
                                    protected java.util.Calendar localDateStarted ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localDateStartedTracker = false ;

                           public boolean isDateStartedSpecified(){
                               return localDateStartedTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.util.Calendar
                           */
                           public  java.util.Calendar getDateStarted(){
                               return localDateStarted;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param DateStarted
                               */
                               public void setDateStarted(java.util.Calendar param){
                            localDateStartedTracker = param != null;
                                   
                                            this.localDateStarted=param;
                                    

                               }
                            

                        /**
                        * field for DateCompleted
                        */

                        
                                    protected java.util.Calendar localDateCompleted ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localDateCompletedTracker = false ;

                           public boolean isDateCompletedSpecified(){
                               return localDateCompletedTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.util.Calendar
                           */
                           public  java.util.Calendar getDateCompleted(){
                               return localDateCompleted;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param DateCompleted
                               */
                               public void setDateCompleted(java.util.Calendar param){
                            localDateCompletedTracker = param != null;
                                   
                                            this.localDateCompleted=param;
                                    

                               }
                            

                        /**
                        * field for Datedied
                        */

                        
                                    protected java.util.Calendar localDatedied ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localDatediedTracker = false ;

                           public boolean isDatediedSpecified(){
                               return localDatediedTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.util.Calendar
                           */
                           public  java.util.Calendar getDatedied(){
                               return localDatedied;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Datedied
                               */
                               public void setDatedied(java.util.Calendar param){
                            localDatediedTracker = param != null;
                                   
                                            this.localDatedied=param;
                                    

                               }
                            

                        /**
                        * field for Failure
                        */

                        
                                    protected org.wso2.carbon.bpel.stub.mgt.types.FailureInfoType localFailure ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localFailureTracker = false ;

                           public boolean isFailureSpecified(){
                               return localFailureTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.bpel.stub.mgt.types.FailureInfoType
                           */
                           public  org.wso2.carbon.bpel.stub.mgt.types.FailureInfoType getFailure(){
                               return localFailure;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Failure
                               */
                               public void setFailure(org.wso2.carbon.bpel.stub.mgt.types.FailureInfoType param){
                            localFailureTracker = param != null;
                                   
                                            this.localFailure=param;
                                    

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
               

                   java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://wso2.org/bps/management/schema");
                   if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           namespacePrefix+":ActivityInfoType",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "ActivityInfoType",
                           xmlWriter);
                   }

               
                   }
                if (localNameTracker){
                                    namespace = "http://wso2.org/bps/management/schema";
                                    writeStartElement(null, namespace, "name", xmlWriter);
                             

                                          if (localName==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("name cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localName);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             }
                                    namespace = "http://wso2.org/bps/management/schema";
                                    writeStartElement(null, namespace, "type", xmlWriter);
                             

                                          if (localType==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("type cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localType);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             
                                    namespace = "http://wso2.org/bps/management/schema";
                                    writeStartElement(null, namespace, "aiid", xmlWriter);
                             

                                          if (localAiid==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("aiid cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAiid);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             
                                            if (localStatus==null){
                                                 throw new org.apache.axis2.databinding.ADBException("status cannot be null!!");
                                            }
                                           localStatus.serialize(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","status"),
                                               xmlWriter);
                                         if (localDateEnabledTracker){
                                    namespace = "http://wso2.org/bps/management/schema";
                                    writeStartElement(null, namespace, "dateEnabled", xmlWriter);
                             

                                          if (localDateEnabled==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("dateEnabled cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDateEnabled));
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localDateStartedTracker){
                                    namespace = "http://wso2.org/bps/management/schema";
                                    writeStartElement(null, namespace, "dateStarted", xmlWriter);
                             

                                          if (localDateStarted==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("dateStarted cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDateStarted));
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localDateCompletedTracker){
                                    namespace = "http://wso2.org/bps/management/schema";
                                    writeStartElement(null, namespace, "dateCompleted", xmlWriter);
                             

                                          if (localDateCompleted==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("dateCompleted cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDateCompleted));
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localDatediedTracker){
                                    namespace = "http://wso2.org/bps/management/schema";
                                    writeStartElement(null, namespace, "datedied", xmlWriter);
                             

                                          if (localDatedied==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("datedied cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDatedied));
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localFailureTracker){
                                            if (localFailure==null){
                                                 throw new org.apache.axis2.databinding.ADBException("failure cannot be null!!");
                                            }
                                           localFailure.serialize(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","failure"),
                                               xmlWriter);
                                        }
                    xmlWriter.writeEndElement();
               

        }

        private static java.lang.String generatePrefix(java.lang.String namespace) {
            if(namespace.equals("http://wso2.org/bps/management/schema")){
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

                 if (localNameTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema",
                                                                      "name"));
                                 
                                        if (localName != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localName));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("name cannot be null!!");
                                        }
                                    }
                                      elementList.add(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema",
                                                                      "type"));
                                 
                                        if (localType != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localType));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("type cannot be null!!");
                                        }
                                    
                                      elementList.add(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema",
                                                                      "aiid"));
                                 
                                        if (localAiid != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAiid));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("aiid cannot be null!!");
                                        }
                                    
                            elementList.add(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema",
                                                                      "status"));
                            
                            
                                    if (localStatus==null){
                                         throw new org.apache.axis2.databinding.ADBException("status cannot be null!!");
                                    }
                                    elementList.add(localStatus);
                                 if (localDateEnabledTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema",
                                                                      "dateEnabled"));
                                 
                                        if (localDateEnabled != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDateEnabled));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("dateEnabled cannot be null!!");
                                        }
                                    } if (localDateStartedTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema",
                                                                      "dateStarted"));
                                 
                                        if (localDateStarted != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDateStarted));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("dateStarted cannot be null!!");
                                        }
                                    } if (localDateCompletedTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema",
                                                                      "dateCompleted"));
                                 
                                        if (localDateCompleted != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDateCompleted));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("dateCompleted cannot be null!!");
                                        }
                                    } if (localDatediedTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema",
                                                                      "datedied"));
                                 
                                        if (localDatedied != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDatedied));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("datedied cannot be null!!");
                                        }
                                    } if (localFailureTracker){
                            elementList.add(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema",
                                                                      "failure"));
                            
                            
                                    if (localFailure==null){
                                         throw new org.apache.axis2.databinding.ADBException("failure cannot be null!!");
                                    }
                                    elementList.add(localFailure);
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
        public static ActivityInfoType parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            ActivityInfoType object =
                new ActivityInfoType();

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
                    
                            if (!"ActivityInfoType".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (ActivityInfoType)org.wso2.carbon.bpel.stub.mgt.ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }
                        

                  }
                

                }

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                
                    
                    reader.next();
                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","name").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"name" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setName(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","type").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"type" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setType(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                                }
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","aiid").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"aiid" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setAiid(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                                }
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","status").equals(reader.getName())){
                                
                                                object.setStatus(org.wso2.carbon.bpel.stub.mgt.types.ActivityStatusType.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                                }
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","dateEnabled").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"dateEnabled" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setDateEnabled(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToDateTime(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","dateStarted").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"dateStarted" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setDateStarted(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToDateTime(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","dateCompleted").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"dateCompleted" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setDateCompleted(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToDateTime(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","datedied").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"datedied" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setDatedied(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToDateTime(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","failure").equals(reader.getName())){
                                
                                                object.setFailure(org.wso2.carbon.bpel.stub.mgt.types.FailureInfoType.Factory.parse(reader));
                                              
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
           
    