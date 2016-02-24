
/**
 * UserProfileAdmin.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:46 UTC)
 */

            
                package org.wso2.carbon.identity.user.profile.stub.types;
            

            /**
            *  UserProfileAdmin bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class UserProfileAdmin extends org.wso2.carbon.core.xsd.AbstractAdmin
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = UserProfileAdmin
                Namespace URI = http://mgt.profile.user.identity.carbon.wso2.org/xsd
                Namespace Prefix = ns1
                */
            

                        /**
                        * field for AddProfileEnabled
                        */

                        
                                    protected boolean localAddProfileEnabled ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAddProfileEnabledTracker = false ;

                           public boolean isAddProfileEnabledSpecified(){
                               return localAddProfileEnabledTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getAddProfileEnabled(){
                               return localAddProfileEnabled;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param AddProfileEnabled
                               */
                               public void setAddProfileEnabled(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localAddProfileEnabledTracker =
                                       true;
                                   
                                            this.localAddProfileEnabled=param;
                                    

                               }
                            

                        /**
                        * field for AssociatedIDs
                        * This was an Array!
                        */

                        
                                    protected org.wso2.carbon.identity.user.profile.stub.types.AssociatedAccountDTO[] localAssociatedIDs ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAssociatedIDsTracker = false ;

                           public boolean isAssociatedIDsSpecified(){
                               return localAssociatedIDsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.identity.user.profile.stub.types.AssociatedAccountDTO[]
                           */
                           public  org.wso2.carbon.identity.user.profile.stub.types.AssociatedAccountDTO[] getAssociatedIDs(){
                               return localAssociatedIDs;
                           }

                           
                        


                               
                              /**
                               * validate the array for AssociatedIDs
                               */
                              protected void validateAssociatedIDs(org.wso2.carbon.identity.user.profile.stub.types.AssociatedAccountDTO[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param AssociatedIDs
                              */
                              public void setAssociatedIDs(org.wso2.carbon.identity.user.profile.stub.types.AssociatedAccountDTO[] param){
                              
                                   validateAssociatedIDs(param);

                               localAssociatedIDsTracker = true;
                                      
                                      this.localAssociatedIDs=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param org.wso2.carbon.identity.user.profile.stub.types.AssociatedAccountDTO
                             */
                             public void addAssociatedIDs(org.wso2.carbon.identity.user.profile.stub.types.AssociatedAccountDTO param){
                                   if (localAssociatedIDs == null){
                                   localAssociatedIDs = new org.wso2.carbon.identity.user.profile.stub.types.AssociatedAccountDTO[]{};
                                   }

                            
                                 //update the setting tracker
                                localAssociatedIDsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localAssociatedIDs);
                               list.add(param);
                               this.localAssociatedIDs =
                             (org.wso2.carbon.identity.user.profile.stub.types.AssociatedAccountDTO[])list.toArray(
                            new org.wso2.carbon.identity.user.profile.stub.types.AssociatedAccountDTO[list.size()]);

                             }
                             

                        /**
                        * field for ProfileFieldsForInternalStore
                        */

                        
                                    protected org.wso2.carbon.identity.user.profile.stub.types.UserProfileDTO localProfileFieldsForInternalStore ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localProfileFieldsForInternalStoreTracker = false ;

                           public boolean isProfileFieldsForInternalStoreSpecified(){
                               return localProfileFieldsForInternalStoreTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.identity.user.profile.stub.types.UserProfileDTO
                           */
                           public  org.wso2.carbon.identity.user.profile.stub.types.UserProfileDTO getProfileFieldsForInternalStore(){
                               return localProfileFieldsForInternalStore;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ProfileFieldsForInternalStore
                               */
                               public void setProfileFieldsForInternalStore(org.wso2.carbon.identity.user.profile.stub.types.UserProfileDTO param){
                            localProfileFieldsForInternalStoreTracker = true;
                                   
                                            this.localProfileFieldsForInternalStore=param;
                                    

                               }
                            

                        /**
                        * field for ReadOnlyUserStore
                        */

                        
                                    protected boolean localReadOnlyUserStore ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localReadOnlyUserStoreTracker = false ;

                           public boolean isReadOnlyUserStoreSpecified(){
                               return localReadOnlyUserStoreTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getReadOnlyUserStore(){
                               return localReadOnlyUserStore;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ReadOnlyUserStore
                               */
                               public void setReadOnlyUserStore(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localReadOnlyUserStoreTracker =
                                       true;
                                   
                                            this.localReadOnlyUserStore=param;
                                    

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
                

                   java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://mgt.profile.user.identity.carbon.wso2.org/xsd");
                   if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           namespacePrefix+":UserProfileAdmin",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "UserProfileAdmin",
                           xmlWriter);
                   }

                if (localAddProfileEnabledTracker){
                                    namespace = "http://mgt.profile.user.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "addProfileEnabled", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("addProfileEnabled cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAddProfileEnabled));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localAssociatedIDsTracker){
                                       if (localAssociatedIDs!=null){
                                            for (int i = 0;i < localAssociatedIDs.length;i++){
                                                if (localAssociatedIDs[i] != null){
                                                 localAssociatedIDs[i].serialize(new javax.xml.namespace.QName("http://mgt.profile.user.identity.carbon.wso2.org/xsd","associatedIDs"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://mgt.profile.user.identity.carbon.wso2.org/xsd", "associatedIDs", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://mgt.profile.user.identity.carbon.wso2.org/xsd", "associatedIDs", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localProfileFieldsForInternalStoreTracker){
                                    if (localProfileFieldsForInternalStore==null){

                                        writeStartElement(null, "http://mgt.profile.user.identity.carbon.wso2.org/xsd", "profileFieldsForInternalStore", xmlWriter);

                                       // write the nil attribute
                                      writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                      xmlWriter.writeEndElement();
                                    }else{
                                     localProfileFieldsForInternalStore.serialize(new javax.xml.namespace.QName("http://mgt.profile.user.identity.carbon.wso2.org/xsd","profileFieldsForInternalStore"),
                                        xmlWriter);
                                    }
                                } if (localReadOnlyUserStoreTracker){
                                    namespace = "http://mgt.profile.user.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "readOnlyUserStore", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("readOnlyUserStore cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localReadOnlyUserStore));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             }
                    xmlWriter.writeEndElement();
               

        }

        private static java.lang.String generatePrefix(java.lang.String namespace) {
            if(namespace.equals("http://mgt.profile.user.identity.carbon.wso2.org/xsd")){
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

                
                    attribList.add(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema-instance","type"));
                    attribList.add(new javax.xml.namespace.QName("http://mgt.profile.user.identity.carbon.wso2.org/xsd","UserProfileAdmin"));
                 if (localAddProfileEnabledTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://mgt.profile.user.identity.carbon.wso2.org/xsd",
                                                                      "addProfileEnabled"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAddProfileEnabled));
                            } if (localAssociatedIDsTracker){
                             if (localAssociatedIDs!=null) {
                                 for (int i = 0;i < localAssociatedIDs.length;i++){

                                    if (localAssociatedIDs[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://mgt.profile.user.identity.carbon.wso2.org/xsd",
                                                                          "associatedIDs"));
                                         elementList.add(localAssociatedIDs[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://mgt.profile.user.identity.carbon.wso2.org/xsd",
                                                                          "associatedIDs"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://mgt.profile.user.identity.carbon.wso2.org/xsd",
                                                                          "associatedIDs"));
                                        elementList.add(localAssociatedIDs);
                                    
                             }

                        } if (localProfileFieldsForInternalStoreTracker){
                            elementList.add(new javax.xml.namespace.QName("http://mgt.profile.user.identity.carbon.wso2.org/xsd",
                                                                      "profileFieldsForInternalStore"));
                            
                            
                                    elementList.add(localProfileFieldsForInternalStore==null?null:
                                    localProfileFieldsForInternalStore);
                                } if (localReadOnlyUserStoreTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://mgt.profile.user.identity.carbon.wso2.org/xsd",
                                                                      "readOnlyUserStore"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localReadOnlyUserStore));
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
        public static UserProfileAdmin parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            UserProfileAdmin object =
                new UserProfileAdmin();

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
                    
                            if (!"UserProfileAdmin".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (UserProfileAdmin)org.wso2.carbon.identity.user.profile.stub.ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }
                        

                  }
                

                }

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                
                    
                    reader.next();
                
                        java.util.ArrayList list2 = new java.util.ArrayList();
                    
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://mgt.profile.user.identity.carbon.wso2.org/xsd","addProfileEnabled").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"addProfileEnabled" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setAddProfileEnabled(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://mgt.profile.user.identity.carbon.wso2.org/xsd","associatedIDs").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list2.add(null);
                                                              reader.next();
                                                          } else {
                                                        list2.add(org.wso2.carbon.identity.user.profile.stub.types.AssociatedAccountDTO.Factory.parse(reader));
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
                                                                if (new javax.xml.namespace.QName("http://mgt.profile.user.identity.carbon.wso2.org/xsd","associatedIDs").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list2.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list2.add(org.wso2.carbon.identity.user.profile.stub.types.AssociatedAccountDTO.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone2 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setAssociatedIDs((org.wso2.carbon.identity.user.profile.stub.types.AssociatedAccountDTO[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                org.wso2.carbon.identity.user.profile.stub.types.AssociatedAccountDTO.class,
                                                                list2));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://mgt.profile.user.identity.carbon.wso2.org/xsd","profileFieldsForInternalStore").equals(reader.getName())){
                                
                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                          object.setProfileFieldsForInternalStore(null);
                                          reader.next();
                                            
                                            reader.next();
                                          
                                      }else{
                                    
                                                object.setProfileFieldsForInternalStore(org.wso2.carbon.identity.user.profile.stub.types.UserProfileDTO.Factory.parse(reader));
                                              
                                        reader.next();
                                    }
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://mgt.profile.user.identity.carbon.wso2.org/xsd","readOnlyUserStore").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"readOnlyUserStore" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setReadOnlyUserStore(
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
           
    