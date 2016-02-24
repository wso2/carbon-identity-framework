
/**
 * ProcessInfoType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:46 UTC)
 */

            
                package org.wso2.carbon.bpel.stub.mgt.types;
            

            /**
            *  ProcessInfoType bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class ProcessInfoType
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = ProcessInfoType
                Namespace URI = http://wso2.org/bps/management/schema
                Namespace Prefix = ns1
                */
            

                        /**
                        * field for Pid
                        */

                        
                                    protected java.lang.String localPid ;
                                

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getPid(){
                               return localPid;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Pid
                               */
                               public void setPid(java.lang.String param){
                            
                                            this.localPid=param;
                                    

                               }
                            

                        /**
                        * field for Version
                        */

                        
                                    protected long localVersion ;
                                

                           /**
                           * Auto generated getter method
                           * @return long
                           */
                           public  long getVersion(){
                               return localVersion;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Version
                               */
                               public void setVersion(long param){
                            
                                            this.localVersion=param;
                                    

                               }
                            

                        /**
                        * field for Status
                        */

                        
                                    protected org.wso2.carbon.bpel.stub.mgt.types.ProcessStatus localStatus ;
                                

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.bpel.stub.mgt.types.ProcessStatus
                           */
                           public  org.wso2.carbon.bpel.stub.mgt.types.ProcessStatus getStatus(){
                               return localStatus;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Status
                               */
                               public void setStatus(org.wso2.carbon.bpel.stub.mgt.types.ProcessStatus param){
                            
                                            this.localStatus=param;
                                    

                               }
                            

                        /**
                        * field for OlderVersion
                        */

                        
                                    protected int localOlderVersion ;
                                

                           /**
                           * Auto generated getter method
                           * @return int
                           */
                           public  int getOlderVersion(){
                               return localOlderVersion;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param OlderVersion
                               */
                               public void setOlderVersion(int param){
                            
                                            this.localOlderVersion=param;
                                    

                               }
                            

                        /**
                        * field for DefinitionInfo
                        */

                        
                                    protected org.wso2.carbon.bpel.stub.mgt.types.DefinitionInfo localDefinitionInfo ;
                                

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.bpel.stub.mgt.types.DefinitionInfo
                           */
                           public  org.wso2.carbon.bpel.stub.mgt.types.DefinitionInfo getDefinitionInfo(){
                               return localDefinitionInfo;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param DefinitionInfo
                               */
                               public void setDefinitionInfo(org.wso2.carbon.bpel.stub.mgt.types.DefinitionInfo param){
                            
                                            this.localDefinitionInfo=param;
                                    

                               }
                            

                        /**
                        * field for DeploymentInfo
                        */

                        
                                    protected org.wso2.carbon.bpel.stub.mgt.types.DeploymentInfo localDeploymentInfo ;
                                

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.bpel.stub.mgt.types.DeploymentInfo
                           */
                           public  org.wso2.carbon.bpel.stub.mgt.types.DeploymentInfo getDeploymentInfo(){
                               return localDeploymentInfo;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param DeploymentInfo
                               */
                               public void setDeploymentInfo(org.wso2.carbon.bpel.stub.mgt.types.DeploymentInfo param){
                            
                                            this.localDeploymentInfo=param;
                                    

                               }
                            

                        /**
                        * field for InstanceSummary
                        */

                        
                                    protected org.wso2.carbon.bpel.stub.mgt.types.InstanceSummary localInstanceSummary ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localInstanceSummaryTracker = false ;

                           public boolean isInstanceSummarySpecified(){
                               return localInstanceSummaryTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.bpel.stub.mgt.types.InstanceSummary
                           */
                           public  org.wso2.carbon.bpel.stub.mgt.types.InstanceSummary getInstanceSummary(){
                               return localInstanceSummary;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param InstanceSummary
                               */
                               public void setInstanceSummary(org.wso2.carbon.bpel.stub.mgt.types.InstanceSummary param){
                            localInstanceSummaryTracker = param != null;
                                   
                                            this.localInstanceSummary=param;
                                    

                               }
                            

                        /**
                        * field for Properties
                        */

                        
                                    protected org.wso2.carbon.bpel.stub.mgt.types.ProcessProperties localProperties ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPropertiesTracker = false ;

                           public boolean isPropertiesSpecified(){
                               return localPropertiesTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.bpel.stub.mgt.types.ProcessProperties
                           */
                           public  org.wso2.carbon.bpel.stub.mgt.types.ProcessProperties getProperties(){
                               return localProperties;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Properties
                               */
                               public void setProperties(org.wso2.carbon.bpel.stub.mgt.types.ProcessProperties param){
                            localPropertiesTracker = param != null;
                                   
                                            this.localProperties=param;
                                    

                               }
                            

                        /**
                        * field for Endpoints
                        */

                        
                                    protected org.wso2.carbon.bpel.stub.mgt.types.EndpointReferencesType localEndpoints ;
                                

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.bpel.stub.mgt.types.EndpointReferencesType
                           */
                           public  org.wso2.carbon.bpel.stub.mgt.types.EndpointReferencesType getEndpoints(){
                               return localEndpoints;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Endpoints
                               */
                               public void setEndpoints(org.wso2.carbon.bpel.stub.mgt.types.EndpointReferencesType param){
                            
                                            this.localEndpoints=param;
                                    

                               }
                            

                        /**
                        * field for ExtraElement
                        * This was an Array!
                        */

                        
                                    protected org.apache.axiom.om.OMElement[] localExtraElement ;
                                
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
                           * @return org.apache.axiom.om.OMElement[]
                           */
                           public  org.apache.axiom.om.OMElement[] getExtraElement(){
                               return localExtraElement;
                           }

                           
                        


                               
                              /**
                               * validate the array for ExtraElement
                               */
                              protected void validateExtraElement(org.apache.axiom.om.OMElement[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param ExtraElement
                              */
                              public void setExtraElement(org.apache.axiom.om.OMElement[] param){
                              
                                   validateExtraElement(param);

                               localExtraElementTracker = param != null;
                                      
                                      this.localExtraElement=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param org.apache.axiom.om.OMElement
                             */
                             public void addExtraElement(org.apache.axiom.om.OMElement param){
                                   if (localExtraElement == null){
                                   localExtraElement = new org.apache.axiom.om.OMElement[]{};
                                   }

                            
                                 //update the setting tracker
                                localExtraElementTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localExtraElement);
                               list.add(param);
                               this.localExtraElement =
                             (org.apache.axiom.om.OMElement[])list.toArray(
                            new org.apache.axiom.om.OMElement[list.size()]);

                             }
                             

                        /**
                        * field for ExtraAttributes
                        * This was an Attribute!
                        * This was an Array!
                        */

                        
                                    protected org.apache.axiom.om.OMAttribute[] localExtraAttributes ;
                                

                           /**
                           * Auto generated getter method
                           * @return org.apache.axiom.om.OMAttribute[]
                           */
                           public  org.apache.axiom.om.OMAttribute[] getExtraAttributes(){
                               return localExtraAttributes;
                           }

                           
                        


                               
                              /**
                               * validate the array for ExtraAttributes
                               */
                              protected void validateExtraAttributes(org.apache.axiom.om.OMAttribute[] param){
                             
                              if ((param != null) && (param.length > 1)){
                                throw new java.lang.RuntimeException();
                              }
                              
                              if ((param != null) && (param.length < 1)){
                                throw new java.lang.RuntimeException();
                              }
                              
                              }


                             /**
                              * Auto generated setter method
                              * @param param ExtraAttributes
                              */
                              public void setExtraAttributes(org.apache.axiom.om.OMAttribute[] param){
                              
                                   validateExtraAttributes(param);

                               
                                      this.localExtraAttributes=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param org.apache.axiom.om.OMAttribute
                             */
                             public void addExtraAttributes(org.apache.axiom.om.OMAttribute param){
                                   if (localExtraAttributes == null){
                                   localExtraAttributes = new org.apache.axiom.om.OMAttribute[]{};
                                   }

                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localExtraAttributes);
                               list.add(param);
                               this.localExtraAttributes =
                             (org.apache.axiom.om.OMAttribute[])list.toArray(
                            new org.apache.axiom.om.OMAttribute[list.size()]);

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
                           namespacePrefix+":ProcessInfoType",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "ProcessInfoType",
                           xmlWriter);
                   }

               
                   }
               
                             if (localExtraAttributes != null) {
                                 for (int i=0;i <localExtraAttributes.length;i++){
                                     writeAttribute(localExtraAttributes[i].getNamespace().getName(),
                                                    localExtraAttributes[i].getLocalName(),
                                                    localExtraAttributes[i].getAttributeValue(),xmlWriter);
                                     }
                             }
                        
                                    namespace = "http://wso2.org/bps/management/schema";
                                    writeStartElement(null, namespace, "pid", xmlWriter);
                             

                                          if (localPid==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("pid cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localPid);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             
                                    namespace = "http://wso2.org/bps/management/schema";
                                    writeStartElement(null, namespace, "version", xmlWriter);
                             
                                               if (localVersion==java.lang.Long.MIN_VALUE) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("version cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localVersion));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             
                                            if (localStatus==null){
                                                 throw new org.apache.axis2.databinding.ADBException("status cannot be null!!");
                                            }
                                           localStatus.serialize(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","status"),
                                               xmlWriter);
                                        
                                    namespace = "http://wso2.org/bps/management/schema";
                                    writeStartElement(null, namespace, "olderVersion", xmlWriter);
                             
                                               if (localOlderVersion==java.lang.Integer.MIN_VALUE) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("olderVersion cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localOlderVersion));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             
                                            if (localDefinitionInfo==null){
                                                 throw new org.apache.axis2.databinding.ADBException("definitionInfo cannot be null!!");
                                            }
                                           localDefinitionInfo.serialize(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","definitionInfo"),
                                               xmlWriter);
                                        
                                            if (localDeploymentInfo==null){
                                                 throw new org.apache.axis2.databinding.ADBException("deploymentInfo cannot be null!!");
                                            }
                                           localDeploymentInfo.serialize(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","deploymentInfo"),
                                               xmlWriter);
                                         if (localInstanceSummaryTracker){
                                            if (localInstanceSummary==null){
                                                 throw new org.apache.axis2.databinding.ADBException("instanceSummary cannot be null!!");
                                            }
                                           localInstanceSummary.serialize(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","instanceSummary"),
                                               xmlWriter);
                                        } if (localPropertiesTracker){
                                            if (localProperties==null){
                                                 throw new org.apache.axis2.databinding.ADBException("properties cannot be null!!");
                                            }
                                           localProperties.serialize(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","properties"),
                                               xmlWriter);
                                        }
                                            if (localEndpoints==null){
                                                 throw new org.apache.axis2.databinding.ADBException("endpoints cannot be null!!");
                                            }
                                           localEndpoints.serialize(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","endpoints"),
                                               xmlWriter);
                                         if (localExtraElementTracker){
                            
                            if (localExtraElement != null){
                                for (int i = 0;i < localExtraElement.length;i++){
                                    if (localExtraElement[i] != null){
                                        localExtraElement[i].serialize(xmlWriter);
                                    } else {
                                        
                                                // we have to do nothing since minOccures zero
                                            
                                    }
                                }
                            } else {
                                throw new org.apache.axis2.databinding.ADBException("extraElement cannot be null!!");
                            }
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
                                                                      "pid"));
                                 
                                        if (localPid != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPid));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("pid cannot be null!!");
                                        }
                                    
                                      elementList.add(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema",
                                                                      "version"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localVersion));
                            
                            elementList.add(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema",
                                                                      "status"));
                            
                            
                                    if (localStatus==null){
                                         throw new org.apache.axis2.databinding.ADBException("status cannot be null!!");
                                    }
                                    elementList.add(localStatus);
                                
                                      elementList.add(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema",
                                                                      "olderVersion"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localOlderVersion));
                            
                            elementList.add(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema",
                                                                      "definitionInfo"));
                            
                            
                                    if (localDefinitionInfo==null){
                                         throw new org.apache.axis2.databinding.ADBException("definitionInfo cannot be null!!");
                                    }
                                    elementList.add(localDefinitionInfo);
                                
                            elementList.add(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema",
                                                                      "deploymentInfo"));
                            
                            
                                    if (localDeploymentInfo==null){
                                         throw new org.apache.axis2.databinding.ADBException("deploymentInfo cannot be null!!");
                                    }
                                    elementList.add(localDeploymentInfo);
                                 if (localInstanceSummaryTracker){
                            elementList.add(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema",
                                                                      "instanceSummary"));
                            
                            
                                    if (localInstanceSummary==null){
                                         throw new org.apache.axis2.databinding.ADBException("instanceSummary cannot be null!!");
                                    }
                                    elementList.add(localInstanceSummary);
                                } if (localPropertiesTracker){
                            elementList.add(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema",
                                                                      "properties"));
                            
                            
                                    if (localProperties==null){
                                         throw new org.apache.axis2.databinding.ADBException("properties cannot be null!!");
                                    }
                                    elementList.add(localProperties);
                                }
                            elementList.add(new javax.xml.namespace.QName("http://wso2.org/bps/management/schema",
                                                                      "endpoints"));
                            
                            
                                    if (localEndpoints==null){
                                         throw new org.apache.axis2.databinding.ADBException("endpoints cannot be null!!");
                                    }
                                    elementList.add(localEndpoints);
                                 if (localExtraElementTracker){
                            if (localExtraElement != null) {
                                for (int i = 0;i < localExtraElement.length;i++){
                                    if (localExtraElement[i] != null){
                                       elementList.add(new javax.xml.namespace.QName("",
                                                                          "extraElement"));
                                      elementList.add(
                                      org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localExtraElement[i]));
                                    } else {
                                        
                                                // have to do nothing
                                            
                                    }

                                }
                            } else {
                               throw new org.apache.axis2.databinding.ADBException("extraElement cannot be null!!");
                            }
                        }
                             for (int i=0;i <localExtraAttributes.length;i++){
                               attribList.add(org.apache.axis2.databinding.utils.Constants.OM_ATTRIBUTE_KEY);
                               attribList.add(localExtraAttributes[i]);
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
        public static ProcessInfoType parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            ProcessInfoType object =
                new ProcessInfoType();

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
                    
                            if (!"ProcessInfoType".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (ProcessInfoType)org.wso2.carbon.bpel.stub.mgt.ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }
                        

                  }
                

                }

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                
                        // now run through all any or extra attributes
                        // which were not reflected until now
                        for (int i=0; i < reader.getAttributeCount(); i++) {
                            if (!handledAttributes.contains(reader.getAttributeLocalName(i))) {
                                // this is an anyAttribute and we create
                                // an OMAttribute for this
                                org.apache.axiom.om.OMFactory factory = org.apache.axiom.om.OMAbstractFactory.getOMFactory();
                                org.apache.axiom.om.OMAttribute attr =
                                    factory.createOMAttribute(
                                            reader.getAttributeLocalName(i),
                                            factory.createOMNamespace(
                                                reader.getAttributeNamespace(i), reader.getAttributePrefix(i)),
                                            reader.getAttributeValue(i));

                                // and add it to the extra attributes
                                
                                         object.addExtraAttributes(attr);
                                    

                            }
                        }
                    
                    
                    reader.next();
                
                        java.util.ArrayList list10 = new java.util.ArrayList();
                    
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","pid").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"pid" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setPid(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                                }
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","version").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"version" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setVersion(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToLong(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                                }
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","status").equals(reader.getName())){
                                
                                                object.setStatus(org.wso2.carbon.bpel.stub.mgt.types.ProcessStatus.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                                }
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","olderVersion").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"olderVersion" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setOlderVersion(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                                }
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","definitionInfo").equals(reader.getName())){
                                
                                                object.setDefinitionInfo(org.wso2.carbon.bpel.stub.mgt.types.DefinitionInfo.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                                }
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","deploymentInfo").equals(reader.getName())){
                                
                                                object.setDeploymentInfo(org.wso2.carbon.bpel.stub.mgt.types.DeploymentInfo.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                                }
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","instanceSummary").equals(reader.getName())){
                                
                                                object.setInstanceSummary(org.wso2.carbon.bpel.stub.mgt.types.InstanceSummary.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","properties").equals(reader.getName())){
                                
                                                object.setProperties(org.wso2.carbon.bpel.stub.mgt.types.ProcessProperties.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://wso2.org/bps/management/schema","endpoints").equals(reader.getName())){
                                
                                                object.setEndpoints(org.wso2.carbon.bpel.stub.mgt.types.EndpointReferencesType.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                                }
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                   if (reader.isStartElement()){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                           boolean loopDone10=false;

                                             while (!loopDone10){
                                                 event = reader.getEventType();
                                                 if (javax.xml.stream.XMLStreamConstants.START_ELEMENT == event){

                                                      // We need to wrap the reader so that it produces a fake START_DOCUEMENT event
                                                      org.apache.axis2.databinding.utils.NamedStaxOMBuilder builder10
                                                         = new org.apache.axis2.databinding.utils.NamedStaxOMBuilder(
                                                              new org.apache.axis2.util.StreamWrapper(reader), reader.getName());

                                                       list10.add(builder10.getOMElement());
                                                        reader.next();
                                                        if (reader.isEndElement()) {
                                                            // we have two countinuos end elements
                                                           loopDone10 = true;
                                                        }

                                                 }else if (javax.xml.stream.XMLStreamConstants.END_ELEMENT == event){
                                                     loopDone10 = true;
                                                 }else{
                                                     reader.next();
                                                 }

                                             }

                                            
                                             object.setExtraElement((org.apache.axiom.om.OMElement[])
                                                 org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                     org.apache.axiom.om.OMElement.class,list10));
                                                
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
           
    