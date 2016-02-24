
/**
 * NotificationDataDTO.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:46 UTC)
 */

            
                package org.wso2.carbon.identity.mgt.stub.dto;
            

            /**
            *  NotificationDataDTO bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class NotificationDataDTO
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = NotificationDataDTO
                Namespace URI = http://dto.mgt.identity.carbon.wso2.org/xsd
                Namespace Prefix = ns1
                */
            

                        /**
                        * field for DomainName
                        */

                        
                                    protected java.lang.String localDomainName ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localDomainNameTracker = false ;

                           public boolean isDomainNameSpecified(){
                               return localDomainNameTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getDomainName(){
                               return localDomainName;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param DomainName
                               */
                               public void setDomainName(java.lang.String param){
                            localDomainNameTracker = true;
                                   
                                            this.localDomainName=param;
                                    

                               }
                            

                        /**
                        * field for FirstName
                        */

                        
                                    protected java.lang.String localFirstName ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localFirstNameTracker = false ;

                           public boolean isFirstNameSpecified(){
                               return localFirstNameTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getFirstName(){
                               return localFirstName;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param FirstName
                               */
                               public void setFirstName(java.lang.String param){
                            localFirstNameTracker = true;
                                   
                                            this.localFirstName=param;
                                    

                               }
                            

                        /**
                        * field for Notification
                        */

                        
                                    protected java.lang.String localNotification ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localNotificationTracker = false ;

                           public boolean isNotificationSpecified(){
                               return localNotificationTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getNotification(){
                               return localNotification;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Notification
                               */
                               public void setNotification(java.lang.String param){
                            localNotificationTracker = true;
                                   
                                            this.localNotification=param;
                                    

                               }
                            

                        /**
                        * field for NotificationAddress
                        */

                        
                                    protected java.lang.String localNotificationAddress ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localNotificationAddressTracker = false ;

                           public boolean isNotificationAddressSpecified(){
                               return localNotificationAddressTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getNotificationAddress(){
                               return localNotificationAddress;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param NotificationAddress
                               */
                               public void setNotificationAddress(java.lang.String param){
                            localNotificationAddressTracker = true;
                                   
                                            this.localNotificationAddress=param;
                                    

                               }
                            

                        /**
                        * field for NotificationCode
                        */

                        
                                    protected java.lang.String localNotificationCode ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localNotificationCodeTracker = false ;

                           public boolean isNotificationCodeSpecified(){
                               return localNotificationCodeTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getNotificationCode(){
                               return localNotificationCode;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param NotificationCode
                               */
                               public void setNotificationCode(java.lang.String param){
                            localNotificationCodeTracker = true;
                                   
                                            this.localNotificationCode=param;
                                    

                               }
                            

                        /**
                        * field for NotificationSent
                        */

                        
                                    protected boolean localNotificationSent ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localNotificationSentTracker = false ;

                           public boolean isNotificationSentSpecified(){
                               return localNotificationSentTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getNotificationSent(){
                               return localNotificationSent;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param NotificationSent
                               */
                               public void setNotificationSent(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localNotificationSentTracker =
                                       true;
                                   
                                            this.localNotificationSent=param;
                                    

                               }
                            

                        /**
                        * field for NotificationSubject
                        */

                        
                                    protected java.lang.String localNotificationSubject ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localNotificationSubjectTracker = false ;

                           public boolean isNotificationSubjectSpecified(){
                               return localNotificationSubjectTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getNotificationSubject(){
                               return localNotificationSubject;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param NotificationSubject
                               */
                               public void setNotificationSubject(java.lang.String param){
                            localNotificationSubjectTracker = true;
                                   
                                            this.localNotificationSubject=param;
                                    

                               }
                            

                        /**
                        * field for NotificationType
                        */

                        
                                    protected java.lang.String localNotificationType ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localNotificationTypeTracker = false ;

                           public boolean isNotificationTypeSpecified(){
                               return localNotificationTypeTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getNotificationType(){
                               return localNotificationType;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param NotificationType
                               */
                               public void setNotificationType(java.lang.String param){
                            localNotificationTypeTracker = true;
                                   
                                            this.localNotificationType=param;
                                    

                               }
                            

                        /**
                        * field for UserId
                        */

                        
                                    protected java.lang.String localUserId ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localUserIdTracker = false ;

                           public boolean isUserIdSpecified(){
                               return localUserIdTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getUserId(){
                               return localUserId;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param UserId
                               */
                               public void setUserId(java.lang.String param){
                            localUserIdTracker = true;
                                   
                                            this.localUserId=param;
                                    

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
               

                   java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://dto.mgt.identity.carbon.wso2.org/xsd");
                   if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           namespacePrefix+":NotificationDataDTO",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "NotificationDataDTO",
                           xmlWriter);
                   }

               
                   }
                if (localDomainNameTracker){
                                    namespace = "http://dto.mgt.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "domainName", xmlWriter);
                             

                                          if (localDomainName==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localDomainName);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localFirstNameTracker){
                                    namespace = "http://dto.mgt.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "firstName", xmlWriter);
                             

                                          if (localFirstName==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localFirstName);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localNotificationTracker){
                                    namespace = "http://dto.mgt.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "notification", xmlWriter);
                             

                                          if (localNotification==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localNotification);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localNotificationAddressTracker){
                                    namespace = "http://dto.mgt.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "notificationAddress", xmlWriter);
                             

                                          if (localNotificationAddress==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localNotificationAddress);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localNotificationCodeTracker){
                                    namespace = "http://dto.mgt.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "notificationCode", xmlWriter);
                             

                                          if (localNotificationCode==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localNotificationCode);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localNotificationSentTracker){
                                    namespace = "http://dto.mgt.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "notificationSent", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("notificationSent cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localNotificationSent));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localNotificationSubjectTracker){
                                    namespace = "http://dto.mgt.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "notificationSubject", xmlWriter);
                             

                                          if (localNotificationSubject==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localNotificationSubject);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localNotificationTypeTracker){
                                    namespace = "http://dto.mgt.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "notificationType", xmlWriter);
                             

                                          if (localNotificationType==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localNotificationType);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localUserIdTracker){
                                    namespace = "http://dto.mgt.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "userId", xmlWriter);
                             

                                          if (localUserId==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localUserId);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             }
                    xmlWriter.writeEndElement();
               

        }

        private static java.lang.String generatePrefix(java.lang.String namespace) {
            if(namespace.equals("http://dto.mgt.identity.carbon.wso2.org/xsd")){
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

                 if (localDomainNameTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://dto.mgt.identity.carbon.wso2.org/xsd",
                                                                      "domainName"));
                                 
                                         elementList.add(localDomainName==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDomainName));
                                    } if (localFirstNameTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://dto.mgt.identity.carbon.wso2.org/xsd",
                                                                      "firstName"));
                                 
                                         elementList.add(localFirstName==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localFirstName));
                                    } if (localNotificationTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://dto.mgt.identity.carbon.wso2.org/xsd",
                                                                      "notification"));
                                 
                                         elementList.add(localNotification==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localNotification));
                                    } if (localNotificationAddressTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://dto.mgt.identity.carbon.wso2.org/xsd",
                                                                      "notificationAddress"));
                                 
                                         elementList.add(localNotificationAddress==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localNotificationAddress));
                                    } if (localNotificationCodeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://dto.mgt.identity.carbon.wso2.org/xsd",
                                                                      "notificationCode"));
                                 
                                         elementList.add(localNotificationCode==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localNotificationCode));
                                    } if (localNotificationSentTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://dto.mgt.identity.carbon.wso2.org/xsd",
                                                                      "notificationSent"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localNotificationSent));
                            } if (localNotificationSubjectTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://dto.mgt.identity.carbon.wso2.org/xsd",
                                                                      "notificationSubject"));
                                 
                                         elementList.add(localNotificationSubject==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localNotificationSubject));
                                    } if (localNotificationTypeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://dto.mgt.identity.carbon.wso2.org/xsd",
                                                                      "notificationType"));
                                 
                                         elementList.add(localNotificationType==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localNotificationType));
                                    } if (localUserIdTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://dto.mgt.identity.carbon.wso2.org/xsd",
                                                                      "userId"));
                                 
                                         elementList.add(localUserId==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localUserId));
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
        public static NotificationDataDTO parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            NotificationDataDTO object =
                new NotificationDataDTO();

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
                    
                            if (!"NotificationDataDTO".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (NotificationDataDTO)org.wso2.carbon.captcha.mgt.beans.xsd.ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }
                        

                  }
                

                }

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                
                    
                    reader.next();
                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://dto.mgt.identity.carbon.wso2.org/xsd","domainName").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setDomainName(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://dto.mgt.identity.carbon.wso2.org/xsd","firstName").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setFirstName(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://dto.mgt.identity.carbon.wso2.org/xsd","notification").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setNotification(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://dto.mgt.identity.carbon.wso2.org/xsd","notificationAddress").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setNotificationAddress(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://dto.mgt.identity.carbon.wso2.org/xsd","notificationCode").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setNotificationCode(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://dto.mgt.identity.carbon.wso2.org/xsd","notificationSent").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"notificationSent" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setNotificationSent(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://dto.mgt.identity.carbon.wso2.org/xsd","notificationSubject").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setNotificationSubject(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://dto.mgt.identity.carbon.wso2.org/xsd","notificationType").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setNotificationType(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://dto.mgt.identity.carbon.wso2.org/xsd","userId").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setUserId(
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
           
    