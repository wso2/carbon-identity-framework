
/**
 * ScopeInfoWithEventsType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:46 UTC)
 */

            
                package org.wso2.carbon.bpel.stub.mgt.types;
            

            /**
            *  ScopeInfoWithEventsType bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class ScopeInfoWithEventsType
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = ScopeInfoWithEventsType
                Namespace URI = http://wso2.org/bps/management/schema
                Namespace Prefix = ns1
                */
            

                        /**
                        * field for Siid
                        */

                        
                                    protected java.lang.String localSiid ;
                                

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getSiid(){
                               return localSiid;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Siid
                               */
                               public void setSiid(java.lang.String param){
                            
                                            this.localSiid=param;
                                    

                               }
                            

                        /**
                        * field for Name
                        */

                        
                                    protected java.lang.String localName ;
                                

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
                            
                                            this.localName=param;
                                    

                               }
                            

                        /**
                        * field for Status
                        */

                        
                                    protected org.wso2.carbon.bpel.stub.mgt.types.ScopeStatusType localStatus ;
                                

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.bpel.stub.mgt.types.ScopeStatusType
                           */
                           public  org.wso2.carbon.bpel.stub.mgt.types.ScopeStatusType getStatus(){
                               return localStatus;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Status
                               */
                               public void setStatus(org.wso2.carbon.bpel.stub.mgt.types.ScopeStatusType param){
                            
                                            this.localStatus=param;
                                    

                               }
                            

                        /**
                        * field for ChildrenWithEvents
                        */

                        
                                    protected org.wso2.carbon.bpel.stub.mgt.types.ChildrenWithEvents_type0 localChildrenWithEvents ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localChildrenWithEventsTracker = false ;

                           public boolean isChildrenWithEventsSpecified(){
                               return localChildrenWithEventsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.bpel.stub.mgt.types.ChildrenWithEvents_type0
                           */
                           public  org.wso2.carbon.bpel.stub.mgt.types.ChildrenWithEvents_type0 getChildrenWithEvents(){
                               return localChildrenWithEvents;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ChildrenWithEvents
                               */
                               public void setChildrenWithEvents(org.wso2.carbon.bpel.stub.mgt.types.ChildrenWithEvents_type0 param){
                            localChildrenWithEventsTracker = param != null;
                                   
                                            this.localChildrenWithEvents=param;
                                    

                               }
                            

                        /**
                        * field for ActivitiesWithEvents
                        */

                        
                                    protected org.wso2.carbon.bpel.stub.mgt.types.ActivitiesWithEvents_type0 localActivitiesWithEvents ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localActivitiesWithEventsTracker = false ;

                           public boolean isActivitiesWithEventsSpecified(){
                               return localActivitiesWithEventsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.bpel.stub.mgt.types.ActivitiesWithEvents_type0
                           */
                           public  org.wso2.carbon.bpel.stub.mgt.types.ActivitiesWithEvents_type0 getActivitiesWithEvents(){
                               return localActivitiesWithEvents;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ActivitiesWithEvents
                               */
                               public void setActivitiesWithEvents(org.wso2.carbon.bpel.stub.mgt.types.ActivitiesWithEvents_type0 param){
                            localActivitiesWithEventsTracker = param != null;
                                   
                                            this.localActivitiesWithEvents=param;
                                    

                               }
                            

                        /**
                        * field for VariablesWithEvents
                        */

                        
                                    protected org.wso2.carbon.bpel.stub.mgt.types.VariablesWithEvents_type0 localVariablesWithEvents ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localVariablesWithEventsTracker = false ;

                           public boolean isVariablesWithEventsSpecified(){
                               return localVariablesWithEventsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.bpel.stub.mgt.types.VariablesWithEvents_type0
                           */
                           public  org.wso2.carbon.bpel.stub.mgt.types.VariablesWithEvents_type0 getVariablesWithEvents(){
                               return localVariablesWithEvents;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param VariablesWithEvents
                               */
                               public void setVariablesWithEvents(org.wso2.carbon.bpel.stub.mgt.types.VariablesWithEvents_type0 param){
                            localVariablesWithEventsTracker = param != null;
                                   
                                            this.localVariablesWithEvents=param;
                                    

                               }
                            

                        /**
                        * field for CorrelationSets
                        */

                        
                                    protected org.wso2.carbon.bpel.stub.mgt.types.CorrelationSets_type1 localCorrelationSets ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localCorrelationSetsTracker = false ;

                           public boolean isCorrelationSetsSpecified(){
                               return localCorrelationSetsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.bpel.stub.mgt.types.CorrelationSets_type1
                           */
                           public  org.wso2.carbon.bpel.stub.mgt.types.CorrelationSets_type1 getCorrelationSets(){
                               return localCorrelationSets;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param CorrelationSets
                               */
                               public void setCorrelationSets(org.wso2.carbon.bpel.stub.mgt.types.CorrelationSets_type1 param){
                            localCorrelationSetsTracker = param != null;
                                   
                                            this.localCorrelationSets=param;
                                    

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
                           namespacePrefix+":ScopeInfoWithEventsType",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "ScopeInfoWithEventsType",
                           xmlWriter);
                   }

               
                   }
               
                                    namespace = "http://wso2.org/bps/management/schema";
                                    writeStartElement(null, namespace, "siid", xmlWriter);
                             

                                          if (localSiid==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("siid cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localSiid);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             
                                    namespace = "http://wso2.org/bps/management/schema";
                                    writeStartElement(null, namespace, "name", xmlWriter);
                             

                                          if (localName==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("name cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localName);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             
                                            if (localStatus==null){
                                                 throw new org.apache.axis2.databinding.ADBException("status cannot be null!!");
                                            }
                                           localStatus.serialize(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","status"),
                                               xmlWriter);
                                         if (localChildrenWithEventsTracker){
                                            if (localChildrenWithEvents==null){
                                                 throw new org.apache.axis2.databinding.ADBException("childrenWithEvents cannot be null!!");
                                            }
                                           localChildrenWithEvents.serialize(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","childrenWithEvents"),
                                               xmlWriter);
                                        } if (localActivitiesWithEventsTracker){
                                            if (localActivitiesWithEvents==null){
                                                 throw new org.apache.axis2.databinding.ADBException("activitiesWithEvents cannot be null!!");
                                            }
                                           localActivitiesWithEvents.serialize(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","activitiesWithEvents"),
                                               xmlWriter);
                                        } if (localVariablesWithEventsTracker){
                                            if (localVariablesWithEvents==null){
                                                 throw new org.apache.axis2.databinding.ADBException("variablesWithEvents cannot be null!!");
                                            }
                                           localVariablesWithEvents.serialize(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","variablesWithEvents"),
                                               xmlWriter);
                                        } if (localCorrelationSetsTracker){
                                            if (localCorrelationSets==null){
                                                 throw new org.apache.axis2.databinding.ADBException("correlationSets cannot be null!!");
                                            }
                                           localCorrelationSets.serialize(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","correlationSets"),
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

                
                                      elementList.add(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema",
                                                                      "siid"));
                                 
                                        if (localSiid != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSiid));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("siid cannot be null!!");
                                        }
                                    
                                      elementList.add(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema",
                                                                      "name"));
                                 
                                        if (localName != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localName));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("name cannot be null!!");
                                        }
                                    
                            elementList.add(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema",
                                                                      "status"));
                            
                            
                                    if (localStatus==null){
                                         throw new org.apache.axis2.databinding.ADBException("status cannot be null!!");
                                    }
                                    elementList.add(localStatus);
                                 if (localChildrenWithEventsTracker){
                            elementList.add(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema",
                                                                      "childrenWithEvents"));
                            
                            
                                    if (localChildrenWithEvents==null){
                                         throw new org.apache.axis2.databinding.ADBException("childrenWithEvents cannot be null!!");
                                    }
                                    elementList.add(localChildrenWithEvents);
                                } if (localActivitiesWithEventsTracker){
                            elementList.add(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema",
                                                                      "activitiesWithEvents"));
                            
                            
                                    if (localActivitiesWithEvents==null){
                                         throw new org.apache.axis2.databinding.ADBException("activitiesWithEvents cannot be null!!");
                                    }
                                    elementList.add(localActivitiesWithEvents);
                                } if (localVariablesWithEventsTracker){
                            elementList.add(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema",
                                                                      "variablesWithEvents"));
                            
                            
                                    if (localVariablesWithEvents==null){
                                         throw new org.apache.axis2.databinding.ADBException("variablesWithEvents cannot be null!!");
                                    }
                                    elementList.add(localVariablesWithEvents);
                                } if (localCorrelationSetsTracker){
                            elementList.add(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema",
                                                                      "correlationSets"));
                            
                            
                                    if (localCorrelationSets==null){
                                         throw new org.apache.axis2.databinding.ADBException("correlationSets cannot be null!!");
                                    }
                                    elementList.add(localCorrelationSets);
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
        public static ScopeInfoWithEventsType parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            ScopeInfoWithEventsType object =
                new ScopeInfoWithEventsType();

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
                    
                            if (!"ScopeInfoWithEventsType".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (ScopeInfoWithEventsType)org.wso2.carbon.bpel.stub.mgt.ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }
                        

                  }
                

                }

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                
                    
                    reader.next();
                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","siid").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"siid" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setSiid(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                                }
                            
                                    
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
                                
                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                                }
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","status").equals(reader.getName())){
                                
                                                object.setStatus(org.wso2.carbon.bpel.stub.mgt.types.ScopeStatusType.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                                }
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","childrenWithEvents").equals(reader.getName())){
                                
                                                object.setChildrenWithEvents(org.wso2.carbon.bpel.stub.mgt.types.ChildrenWithEvents_type0.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","activitiesWithEvents").equals(reader.getName())){
                                
                                                object.setActivitiesWithEvents(org.wso2.carbon.bpel.stub.mgt.types.ActivitiesWithEvents_type0.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","variablesWithEvents").equals(reader.getName())){
                                
                                                object.setVariablesWithEvents(org.wso2.carbon.bpel.stub.mgt.types.VariablesWithEvents_type0.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","correlationSets").equals(reader.getName())){
                                
                                                object.setCorrelationSets(org.wso2.carbon.bpel.stub.mgt.types.CorrelationSets_type1.Factory.parse(reader));
                                              
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
           
    