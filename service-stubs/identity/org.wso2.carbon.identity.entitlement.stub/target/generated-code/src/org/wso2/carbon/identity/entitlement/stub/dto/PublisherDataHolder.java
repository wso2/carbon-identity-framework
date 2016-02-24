
/**
 * PublisherDataHolder.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:46 UTC)
 */

            
                package org.wso2.carbon.identity.entitlement.stub.dto;
            

            /**
            *  PublisherDataHolder bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class PublisherDataHolder
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = PublisherDataHolder
                Namespace URI = http://dto.entitlement.identity.carbon.wso2.org/xsd
                Namespace Prefix = ns1
                */
            

                        /**
                        * field for LatestStatus
                        */

                        
                                    protected org.wso2.carbon.identity.entitlement.stub.dto.StatusHolder localLatestStatus ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localLatestStatusTracker = false ;

                           public boolean isLatestStatusSpecified(){
                               return localLatestStatusTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.identity.entitlement.stub.dto.StatusHolder
                           */
                           public  org.wso2.carbon.identity.entitlement.stub.dto.StatusHolder getLatestStatus(){
                               return localLatestStatus;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param LatestStatus
                               */
                               public void setLatestStatus(org.wso2.carbon.identity.entitlement.stub.dto.StatusHolder param){
                            localLatestStatusTracker = true;
                                   
                                            this.localLatestStatus=param;
                                    

                               }
                            

                        /**
                        * field for ModuleName
                        */

                        
                                    protected java.lang.String localModuleName ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localModuleNameTracker = false ;

                           public boolean isModuleNameSpecified(){
                               return localModuleNameTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getModuleName(){
                               return localModuleName;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ModuleName
                               */
                               public void setModuleName(java.lang.String param){
                            localModuleNameTracker = true;
                                   
                                            this.localModuleName=param;
                                    

                               }
                            

                        /**
                        * field for PropertyDTOs
                        * This was an Array!
                        */

                        
                                    protected org.wso2.carbon.identity.entitlement.stub.dto.PublisherPropertyDTO[] localPropertyDTOs ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPropertyDTOsTracker = false ;

                           public boolean isPropertyDTOsSpecified(){
                               return localPropertyDTOsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.identity.entitlement.stub.dto.PublisherPropertyDTO[]
                           */
                           public  org.wso2.carbon.identity.entitlement.stub.dto.PublisherPropertyDTO[] getPropertyDTOs(){
                               return localPropertyDTOs;
                           }

                           
                        


                               
                              /**
                               * validate the array for PropertyDTOs
                               */
                              protected void validatePropertyDTOs(org.wso2.carbon.identity.entitlement.stub.dto.PublisherPropertyDTO[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param PropertyDTOs
                              */
                              public void setPropertyDTOs(org.wso2.carbon.identity.entitlement.stub.dto.PublisherPropertyDTO[] param){
                              
                                   validatePropertyDTOs(param);

                               localPropertyDTOsTracker = true;
                                      
                                      this.localPropertyDTOs=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param org.wso2.carbon.identity.entitlement.stub.dto.PublisherPropertyDTO
                             */
                             public void addPropertyDTOs(org.wso2.carbon.identity.entitlement.stub.dto.PublisherPropertyDTO param){
                                   if (localPropertyDTOs == null){
                                   localPropertyDTOs = new org.wso2.carbon.identity.entitlement.stub.dto.PublisherPropertyDTO[]{};
                                   }

                            
                                 //update the setting tracker
                                localPropertyDTOsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localPropertyDTOs);
                               list.add(param);
                               this.localPropertyDTOs =
                             (org.wso2.carbon.identity.entitlement.stub.dto.PublisherPropertyDTO[])list.toArray(
                            new org.wso2.carbon.identity.entitlement.stub.dto.PublisherPropertyDTO[list.size()]);

                             }
                             

                        /**
                        * field for StatusHolders
                        * This was an Array!
                        */

                        
                                    protected org.wso2.carbon.identity.entitlement.stub.dto.StatusHolder[] localStatusHolders ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localStatusHoldersTracker = false ;

                           public boolean isStatusHoldersSpecified(){
                               return localStatusHoldersTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.identity.entitlement.stub.dto.StatusHolder[]
                           */
                           public  org.wso2.carbon.identity.entitlement.stub.dto.StatusHolder[] getStatusHolders(){
                               return localStatusHolders;
                           }

                           
                        


                               
                              /**
                               * validate the array for StatusHolders
                               */
                              protected void validateStatusHolders(org.wso2.carbon.identity.entitlement.stub.dto.StatusHolder[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param StatusHolders
                              */
                              public void setStatusHolders(org.wso2.carbon.identity.entitlement.stub.dto.StatusHolder[] param){
                              
                                   validateStatusHolders(param);

                               localStatusHoldersTracker = true;
                                      
                                      this.localStatusHolders=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param org.wso2.carbon.identity.entitlement.stub.dto.StatusHolder
                             */
                             public void addStatusHolders(org.wso2.carbon.identity.entitlement.stub.dto.StatusHolder param){
                                   if (localStatusHolders == null){
                                   localStatusHolders = new org.wso2.carbon.identity.entitlement.stub.dto.StatusHolder[]{};
                                   }

                            
                                 //update the setting tracker
                                localStatusHoldersTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localStatusHolders);
                               list.add(param);
                               this.localStatusHolders =
                             (org.wso2.carbon.identity.entitlement.stub.dto.StatusHolder[])list.toArray(
                            new org.wso2.carbon.identity.entitlement.stub.dto.StatusHolder[list.size()]);

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
                           namespacePrefix+":PublisherDataHolder",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "PublisherDataHolder",
                           xmlWriter);
                   }

               
                   }
                if (localLatestStatusTracker){
                                    if (localLatestStatus==null){

                                        writeStartElement(null, "http://dto.entitlement.identity.carbon.wso2.org/xsd", "latestStatus", xmlWriter);

                                       // write the nil attribute
                                      writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                      xmlWriter.writeEndElement();
                                    }else{
                                     localLatestStatus.serialize(new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd","latestStatus"),
                                        xmlWriter);
                                    }
                                } if (localModuleNameTracker){
                                    namespace = "http://dto.entitlement.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "moduleName", xmlWriter);
                             

                                          if (localModuleName==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localModuleName);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localPropertyDTOsTracker){
                                       if (localPropertyDTOs!=null){
                                            for (int i = 0;i < localPropertyDTOs.length;i++){
                                                if (localPropertyDTOs[i] != null){
                                                 localPropertyDTOs[i].serialize(new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd","propertyDTOs"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://dto.entitlement.identity.carbon.wso2.org/xsd", "propertyDTOs", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://dto.entitlement.identity.carbon.wso2.org/xsd", "propertyDTOs", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localStatusHoldersTracker){
                                       if (localStatusHolders!=null){
                                            for (int i = 0;i < localStatusHolders.length;i++){
                                                if (localStatusHolders[i] != null){
                                                 localStatusHolders[i].serialize(new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd","statusHolders"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://dto.entitlement.identity.carbon.wso2.org/xsd", "statusHolders", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://dto.entitlement.identity.carbon.wso2.org/xsd", "statusHolders", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
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

                 if (localLatestStatusTracker){
                            elementList.add(new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd",
                                                                      "latestStatus"));
                            
                            
                                    elementList.add(localLatestStatus==null?null:
                                    localLatestStatus);
                                } if (localModuleNameTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd",
                                                                      "moduleName"));
                                 
                                         elementList.add(localModuleName==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localModuleName));
                                    } if (localPropertyDTOsTracker){
                             if (localPropertyDTOs!=null) {
                                 for (int i = 0;i < localPropertyDTOs.length;i++){

                                    if (localPropertyDTOs[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd",
                                                                          "propertyDTOs"));
                                         elementList.add(localPropertyDTOs[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd",
                                                                          "propertyDTOs"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd",
                                                                          "propertyDTOs"));
                                        elementList.add(localPropertyDTOs);
                                    
                             }

                        } if (localStatusHoldersTracker){
                             if (localStatusHolders!=null) {
                                 for (int i = 0;i < localStatusHolders.length;i++){

                                    if (localStatusHolders[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd",
                                                                          "statusHolders"));
                                         elementList.add(localStatusHolders[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd",
                                                                          "statusHolders"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd",
                                                                          "statusHolders"));
                                        elementList.add(localStatusHolders);
                                    
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
        public static PublisherDataHolder parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            PublisherDataHolder object =
                new PublisherDataHolder();

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
                    
                            if (!"PublisherDataHolder".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (PublisherDataHolder)org.wso2.carbon.identity.entitlement.stub.types.axis2.ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }
                        

                  }
                

                }

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                
                    
                    reader.next();
                
                        java.util.ArrayList list3 = new java.util.ArrayList();
                    
                        java.util.ArrayList list4 = new java.util.ArrayList();
                    
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd","latestStatus").equals(reader.getName())){
                                
                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                          object.setLatestStatus(null);
                                          reader.next();
                                            
                                            reader.next();
                                          
                                      }else{
                                    
                                                object.setLatestStatus(org.wso2.carbon.identity.entitlement.stub.dto.StatusHolder.Factory.parse(reader));
                                              
                                        reader.next();
                                    }
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd","moduleName").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setModuleName(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd","propertyDTOs").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list3.add(null);
                                                              reader.next();
                                                          } else {
                                                        list3.add(org.wso2.carbon.identity.entitlement.stub.dto.PublisherPropertyDTO.Factory.parse(reader));
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
                                                                if (new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd","propertyDTOs").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list3.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list3.add(org.wso2.carbon.identity.entitlement.stub.dto.PublisherPropertyDTO.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone3 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setPropertyDTOs((org.wso2.carbon.identity.entitlement.stub.dto.PublisherPropertyDTO[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                org.wso2.carbon.identity.entitlement.stub.dto.PublisherPropertyDTO.class,
                                                                list3));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd","statusHolders").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list4.add(null);
                                                              reader.next();
                                                          } else {
                                                        list4.add(org.wso2.carbon.identity.entitlement.stub.dto.StatusHolder.Factory.parse(reader));
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
                                                                if (new javax.xml.namespace.QName("http://dto.entitlement.identity.carbon.wso2.org/xsd","statusHolders").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list4.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list4.add(org.wso2.carbon.identity.entitlement.stub.dto.StatusHolder.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone4 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setStatusHolders((org.wso2.carbon.identity.entitlement.stub.dto.StatusHolder[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                org.wso2.carbon.identity.entitlement.stub.dto.StatusHolder.class,
                                                                list4));
                                                            
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
           
    