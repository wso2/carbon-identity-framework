
/**
 * ProcessDeployDetailsList_type0.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:46 UTC)
 */

            
                package org.wso2.carbon.bpel.stub.mgt.types;
            

            /**
            *  ProcessDeployDetailsList_type0 bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class ProcessDeployDetailsList_type0
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = ProcessDeployDetailsList_type0
                Namespace URI = http://wso2.org/bps/management/schema
                Namespace Prefix = ns1
                */
            

                        /**
                        * field for ProcessName
                        */

                        
                                    protected javax.xml.namespace.QName localProcessName ;
                                

                           /**
                           * Auto generated getter method
                           * @return javax.xml.namespace.QName
                           */
                           public  javax.xml.namespace.QName getProcessName(){
                               return localProcessName;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ProcessName
                               */
                               public void setProcessName(javax.xml.namespace.QName param){
                            
                                            this.localProcessName=param;
                                    

                               }
                            

                        /**
                        * field for ProcessState
                        */

                        
                                    protected org.wso2.carbon.bpel.stub.mgt.types.ProcessStatus localProcessState ;
                                

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.bpel.stub.mgt.types.ProcessStatus
                           */
                           public  org.wso2.carbon.bpel.stub.mgt.types.ProcessStatus getProcessState(){
                               return localProcessState;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ProcessState
                               */
                               public void setProcessState(org.wso2.carbon.bpel.stub.mgt.types.ProcessStatus param){
                            
                                            this.localProcessState=param;
                                    

                               }
                            

                        /**
                        * field for IsInMemory
                        */

                        
                                    protected boolean localIsInMemory ;
                                

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getIsInMemory(){
                               return localIsInMemory;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param IsInMemory
                               */
                               public void setIsInMemory(boolean param){
                            
                                            this.localIsInMemory=param;
                                    

                               }
                            

                        /**
                        * field for ProcessType
                        */

                        
                                    protected java.lang.String localProcessType ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localProcessTypeTracker = false ;

                           public boolean isProcessTypeSpecified(){
                               return localProcessTypeTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getProcessType(){
                               return localProcessType;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ProcessType
                               */
                               public void setProcessType(java.lang.String param){
                            localProcessTypeTracker = param != null;
                                   
                                            this.localProcessType=param;
                                    

                               }
                            

                        /**
                        * field for ProvideServiceList
                        */

                        
                                    protected org.wso2.carbon.bpel.stub.mgt.types.ProvideServiceListType localProvideServiceList ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localProvideServiceListTracker = false ;

                           public boolean isProvideServiceListSpecified(){
                               return localProvideServiceListTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.bpel.stub.mgt.types.ProvideServiceListType
                           */
                           public  org.wso2.carbon.bpel.stub.mgt.types.ProvideServiceListType getProvideServiceList(){
                               return localProvideServiceList;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ProvideServiceList
                               */
                               public void setProvideServiceList(org.wso2.carbon.bpel.stub.mgt.types.ProvideServiceListType param){
                            localProvideServiceListTracker = param != null;
                                   
                                            this.localProvideServiceList=param;
                                    

                               }
                            

                        /**
                        * field for InvokeServiceList
                        */

                        
                                    protected org.wso2.carbon.bpel.stub.mgt.types.InvokeServiceListType localInvokeServiceList ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localInvokeServiceListTracker = false ;

                           public boolean isInvokeServiceListSpecified(){
                               return localInvokeServiceListTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.bpel.stub.mgt.types.InvokeServiceListType
                           */
                           public  org.wso2.carbon.bpel.stub.mgt.types.InvokeServiceListType getInvokeServiceList(){
                               return localInvokeServiceList;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param InvokeServiceList
                               */
                               public void setInvokeServiceList(org.wso2.carbon.bpel.stub.mgt.types.InvokeServiceListType param){
                            localInvokeServiceListTracker = param != null;
                                   
                                            this.localInvokeServiceList=param;
                                    

                               }
                            

                        /**
                        * field for MexInterperterList
                        */

                        
                                    protected org.wso2.carbon.bpel.stub.mgt.types.MexInterpreterListType localMexInterperterList ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localMexInterperterListTracker = false ;

                           public boolean isMexInterperterListSpecified(){
                               return localMexInterperterListTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.bpel.stub.mgt.types.MexInterpreterListType
                           */
                           public  org.wso2.carbon.bpel.stub.mgt.types.MexInterpreterListType getMexInterperterList(){
                               return localMexInterperterList;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param MexInterperterList
                               */
                               public void setMexInterperterList(org.wso2.carbon.bpel.stub.mgt.types.MexInterpreterListType param){
                            localMexInterperterListTracker = param != null;
                                   
                                            this.localMexInterperterList=param;
                                    

                               }
                            

                        /**
                        * field for PropertyList
                        */

                        
                                    protected org.wso2.carbon.bpel.stub.mgt.types.PropertyListType localPropertyList ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPropertyListTracker = false ;

                           public boolean isPropertyListSpecified(){
                               return localPropertyListTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.bpel.stub.mgt.types.PropertyListType
                           */
                           public  org.wso2.carbon.bpel.stub.mgt.types.PropertyListType getPropertyList(){
                               return localPropertyList;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PropertyList
                               */
                               public void setPropertyList(org.wso2.carbon.bpel.stub.mgt.types.PropertyListType param){
                            localPropertyListTracker = param != null;
                                   
                                            this.localPropertyList=param;
                                    

                               }
                            

                        /**
                        * field for ProcessEventsList
                        */

                        
                                    protected org.wso2.carbon.bpel.stub.mgt.types.ProcessEventsListType localProcessEventsList ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localProcessEventsListTracker = false ;

                           public boolean isProcessEventsListSpecified(){
                               return localProcessEventsListTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.bpel.stub.mgt.types.ProcessEventsListType
                           */
                           public  org.wso2.carbon.bpel.stub.mgt.types.ProcessEventsListType getProcessEventsList(){
                               return localProcessEventsList;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ProcessEventsList
                               */
                               public void setProcessEventsList(org.wso2.carbon.bpel.stub.mgt.types.ProcessEventsListType param){
                            localProcessEventsListTracker = param != null;
                                   
                                            this.localProcessEventsList=param;
                                    

                               }
                            

                        /**
                        * field for CleanUpList
                        */

                        
                                    protected org.wso2.carbon.bpel.stub.mgt.types.CleanUpListType localCleanUpList ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localCleanUpListTracker = false ;

                           public boolean isCleanUpListSpecified(){
                               return localCleanUpListTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.bpel.stub.mgt.types.CleanUpListType
                           */
                           public  org.wso2.carbon.bpel.stub.mgt.types.CleanUpListType getCleanUpList(){
                               return localCleanUpList;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param CleanUpList
                               */
                               public void setCleanUpList(org.wso2.carbon.bpel.stub.mgt.types.CleanUpListType param){
                            localCleanUpListTracker = param != null;
                                   
                                            this.localCleanUpList=param;
                                    

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
                           namespacePrefix+":ProcessDeployDetailsList_type0",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "ProcessDeployDetailsList_type0",
                           xmlWriter);
                   }

               
                   }
               
                                    namespace = "http://wso2.org/bps/management/schema";
                                    writeStartElement(null, namespace, "processName", xmlWriter);
                             

                                          if (localProcessName==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("processName cannot be null!!");
                                                  
                                          }else{

                                        
                                                writeQName(localProcessName,xmlWriter);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             
                                            if (localProcessState==null){
                                                 throw new org.apache.axis2.databinding.ADBException("processState cannot be null!!");
                                            }
                                           localProcessState.serialize(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","processState"),
                                               xmlWriter);
                                        
                                    namespace = "http://wso2.org/bps/management/schema";
                                    writeStartElement(null, namespace, "isInMemory", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("isInMemory cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIsInMemory));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                              if (localProcessTypeTracker){
                                    namespace = "http://wso2.org/bps/management/schema";
                                    writeStartElement(null, namespace, "processType", xmlWriter);
                             

                                          if (localProcessType==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("processType cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localProcessType);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localProvideServiceListTracker){
                                            if (localProvideServiceList==null){
                                                 throw new org.apache.axis2.databinding.ADBException("provideServiceList cannot be null!!");
                                            }
                                           localProvideServiceList.serialize(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","provideServiceList"),
                                               xmlWriter);
                                        } if (localInvokeServiceListTracker){
                                            if (localInvokeServiceList==null){
                                                 throw new org.apache.axis2.databinding.ADBException("invokeServiceList cannot be null!!");
                                            }
                                           localInvokeServiceList.serialize(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","invokeServiceList"),
                                               xmlWriter);
                                        } if (localMexInterperterListTracker){
                                            if (localMexInterperterList==null){
                                                 throw new org.apache.axis2.databinding.ADBException("mexInterperterList cannot be null!!");
                                            }
                                           localMexInterperterList.serialize(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","mexInterperterList"),
                                               xmlWriter);
                                        } if (localPropertyListTracker){
                                            if (localPropertyList==null){
                                                 throw new org.apache.axis2.databinding.ADBException("propertyList cannot be null!!");
                                            }
                                           localPropertyList.serialize(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","propertyList"),
                                               xmlWriter);
                                        } if (localProcessEventsListTracker){
                                            if (localProcessEventsList==null){
                                                 throw new org.apache.axis2.databinding.ADBException("processEventsList cannot be null!!");
                                            }
                                           localProcessEventsList.serialize(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","processEventsList"),
                                               xmlWriter);
                                        } if (localCleanUpListTracker){
                                            if (localCleanUpList==null){
                                                 throw new org.apache.axis2.databinding.ADBException("cleanUpList cannot be null!!");
                                            }
                                           localCleanUpList.serialize(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","cleanUpList"),
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
                                                                      "processName"));
                                 
                                        if (localProcessName != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localProcessName));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("processName cannot be null!!");
                                        }
                                    
                            elementList.add(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema",
                                                                      "processState"));
                            
                            
                                    if (localProcessState==null){
                                         throw new org.apache.axis2.databinding.ADBException("processState cannot be null!!");
                                    }
                                    elementList.add(localProcessState);
                                
                                      elementList.add(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema",
                                                                      "isInMemory"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIsInMemory));
                             if (localProcessTypeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema",
                                                                      "processType"));
                                 
                                        if (localProcessType != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localProcessType));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("processType cannot be null!!");
                                        }
                                    } if (localProvideServiceListTracker){
                            elementList.add(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema",
                                                                      "provideServiceList"));
                            
                            
                                    if (localProvideServiceList==null){
                                         throw new org.apache.axis2.databinding.ADBException("provideServiceList cannot be null!!");
                                    }
                                    elementList.add(localProvideServiceList);
                                } if (localInvokeServiceListTracker){
                            elementList.add(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema",
                                                                      "invokeServiceList"));
                            
                            
                                    if (localInvokeServiceList==null){
                                         throw new org.apache.axis2.databinding.ADBException("invokeServiceList cannot be null!!");
                                    }
                                    elementList.add(localInvokeServiceList);
                                } if (localMexInterperterListTracker){
                            elementList.add(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema",
                                                                      "mexInterperterList"));
                            
                            
                                    if (localMexInterperterList==null){
                                         throw new org.apache.axis2.databinding.ADBException("mexInterperterList cannot be null!!");
                                    }
                                    elementList.add(localMexInterperterList);
                                } if (localPropertyListTracker){
                            elementList.add(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema",
                                                                      "propertyList"));
                            
                            
                                    if (localPropertyList==null){
                                         throw new org.apache.axis2.databinding.ADBException("propertyList cannot be null!!");
                                    }
                                    elementList.add(localPropertyList);
                                } if (localProcessEventsListTracker){
                            elementList.add(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema",
                                                                      "processEventsList"));
                            
                            
                                    if (localProcessEventsList==null){
                                         throw new org.apache.axis2.databinding.ADBException("processEventsList cannot be null!!");
                                    }
                                    elementList.add(localProcessEventsList);
                                } if (localCleanUpListTracker){
                            elementList.add(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema",
                                                                      "cleanUpList"));
                            
                            
                                    if (localCleanUpList==null){
                                         throw new org.apache.axis2.databinding.ADBException("cleanUpList cannot be null!!");
                                    }
                                    elementList.add(localCleanUpList);
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
        public static ProcessDeployDetailsList_type0 parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            ProcessDeployDetailsList_type0 object =
                new ProcessDeployDetailsList_type0();

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
                    
                            if (!"ProcessDeployDetailsList_type0".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (ProcessDeployDetailsList_type0)org.wso2.carbon.bpel.stub.mgt.ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }
                        

                  }
                

                }

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                
                    
                    reader.next();
                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","processName").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"processName" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                            int index = content.indexOf(":");
                                            if(index > 0){
                                                prefix = content.substring(0,index);
                                             } else {
                                                prefix = "";
                                             }
                                             namespaceuri = reader.getNamespaceURI(prefix);
                                             object.setProcessName(
                                                  org.apache.axis2.databinding.utils.ConverterUtil.convertToQName(content,namespaceuri));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                                }
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","processState").equals(reader.getName())){
                                
                                                object.setProcessState(org.wso2.carbon.bpel.stub.mgt.types.ProcessStatus.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                                }
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","isInMemory").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"isInMemory" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setIsInMemory(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                                }
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","processType").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"processType" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setProcessType(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","provideServiceList").equals(reader.getName())){
                                
                                                object.setProvideServiceList(org.wso2.carbon.bpel.stub.mgt.types.ProvideServiceListType.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","invokeServiceList").equals(reader.getName())){
                                
                                                object.setInvokeServiceList(org.wso2.carbon.bpel.stub.mgt.types.InvokeServiceListType.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","mexInterperterList").equals(reader.getName())){
                                
                                                object.setMexInterperterList(org.wso2.carbon.bpel.stub.mgt.types.MexInterpreterListType.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","propertyList").equals(reader.getName())){
                                
                                                object.setPropertyList(org.wso2.carbon.bpel.stub.mgt.types.PropertyListType.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","processEventsList").equals(reader.getName())){
                                
                                                object.setProcessEventsList(org.wso2.carbon.bpel.stub.mgt.types.ProcessEventsListType.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","cleanUpList").equals(reader.getName())){
                                
                                                object.setCleanUpList(org.wso2.carbon.bpel.stub.mgt.types.CleanUpListType.Factory.parse(reader));
                                              
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
           
    