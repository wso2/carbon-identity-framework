
/**
 * WorkflowWizard.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:46 UTC)
 */

            
                package org.wso2.carbon.identity.workflow.mgt.stub.metadata;
            

            /**
            *  WorkflowWizard bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class WorkflowWizard
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = WorkflowWizard
                Namespace URI = http://dto.mgt.workflow.identity.carbon.wso2.org/xsd
                Namespace Prefix = ns2
                */
            

                        /**
                        * field for Template
                        */

                        
                                    protected org.wso2.carbon.identity.workflow.mgt.stub.metadata.Template localTemplate ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localTemplateTracker = false ;

                           public boolean isTemplateSpecified(){
                               return localTemplateTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.identity.workflow.mgt.stub.metadata.Template
                           */
                           public  org.wso2.carbon.identity.workflow.mgt.stub.metadata.Template getTemplate(){
                               return localTemplate;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Template
                               */
                               public void setTemplate(org.wso2.carbon.identity.workflow.mgt.stub.metadata.Template param){
                            localTemplateTracker = true;
                                   
                                            this.localTemplate=param;
                                    

                               }
                            

                        /**
                        * field for TemplateId
                        */

                        
                                    protected java.lang.String localTemplateId ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localTemplateIdTracker = false ;

                           public boolean isTemplateIdSpecified(){
                               return localTemplateIdTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getTemplateId(){
                               return localTemplateId;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param TemplateId
                               */
                               public void setTemplateId(java.lang.String param){
                            localTemplateIdTracker = true;
                                   
                                            this.localTemplateId=param;
                                    

                               }
                            

                        /**
                        * field for TemplateParameters
                        * This was an Array!
                        */

                        
                                    protected org.wso2.carbon.identity.workflow.mgt.stub.bean.Parameter[] localTemplateParameters ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localTemplateParametersTracker = false ;

                           public boolean isTemplateParametersSpecified(){
                               return localTemplateParametersTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.identity.workflow.mgt.stub.bean.Parameter[]
                           */
                           public  org.wso2.carbon.identity.workflow.mgt.stub.bean.Parameter[] getTemplateParameters(){
                               return localTemplateParameters;
                           }

                           
                        


                               
                              /**
                               * validate the array for TemplateParameters
                               */
                              protected void validateTemplateParameters(org.wso2.carbon.identity.workflow.mgt.stub.bean.Parameter[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param TemplateParameters
                              */
                              public void setTemplateParameters(org.wso2.carbon.identity.workflow.mgt.stub.bean.Parameter[] param){
                              
                                   validateTemplateParameters(param);

                               localTemplateParametersTracker = true;
                                      
                                      this.localTemplateParameters=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param org.wso2.carbon.identity.workflow.mgt.stub.bean.Parameter
                             */
                             public void addTemplateParameters(org.wso2.carbon.identity.workflow.mgt.stub.bean.Parameter param){
                                   if (localTemplateParameters == null){
                                   localTemplateParameters = new org.wso2.carbon.identity.workflow.mgt.stub.bean.Parameter[]{};
                                   }

                            
                                 //update the setting tracker
                                localTemplateParametersTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localTemplateParameters);
                               list.add(param);
                               this.localTemplateParameters =
                             (org.wso2.carbon.identity.workflow.mgt.stub.bean.Parameter[])list.toArray(
                            new org.wso2.carbon.identity.workflow.mgt.stub.bean.Parameter[list.size()]);

                             }
                             

                        /**
                        * field for WorkflowDescription
                        */

                        
                                    protected java.lang.String localWorkflowDescription ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localWorkflowDescriptionTracker = false ;

                           public boolean isWorkflowDescriptionSpecified(){
                               return localWorkflowDescriptionTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getWorkflowDescription(){
                               return localWorkflowDescription;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param WorkflowDescription
                               */
                               public void setWorkflowDescription(java.lang.String param){
                            localWorkflowDescriptionTracker = true;
                                   
                                            this.localWorkflowDescription=param;
                                    

                               }
                            

                        /**
                        * field for WorkflowId
                        */

                        
                                    protected java.lang.String localWorkflowId ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localWorkflowIdTracker = false ;

                           public boolean isWorkflowIdSpecified(){
                               return localWorkflowIdTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getWorkflowId(){
                               return localWorkflowId;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param WorkflowId
                               */
                               public void setWorkflowId(java.lang.String param){
                            localWorkflowIdTracker = true;
                                   
                                            this.localWorkflowId=param;
                                    

                               }
                            

                        /**
                        * field for WorkflowImpl
                        */

                        
                                    protected org.wso2.carbon.identity.workflow.mgt.stub.metadata.WorkflowImpl localWorkflowImpl ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localWorkflowImplTracker = false ;

                           public boolean isWorkflowImplSpecified(){
                               return localWorkflowImplTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.identity.workflow.mgt.stub.metadata.WorkflowImpl
                           */
                           public  org.wso2.carbon.identity.workflow.mgt.stub.metadata.WorkflowImpl getWorkflowImpl(){
                               return localWorkflowImpl;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param WorkflowImpl
                               */
                               public void setWorkflowImpl(org.wso2.carbon.identity.workflow.mgt.stub.metadata.WorkflowImpl param){
                            localWorkflowImplTracker = true;
                                   
                                            this.localWorkflowImpl=param;
                                    

                               }
                            

                        /**
                        * field for WorkflowImplId
                        */

                        
                                    protected java.lang.String localWorkflowImplId ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localWorkflowImplIdTracker = false ;

                           public boolean isWorkflowImplIdSpecified(){
                               return localWorkflowImplIdTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getWorkflowImplId(){
                               return localWorkflowImplId;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param WorkflowImplId
                               */
                               public void setWorkflowImplId(java.lang.String param){
                            localWorkflowImplIdTracker = true;
                                   
                                            this.localWorkflowImplId=param;
                                    

                               }
                            

                        /**
                        * field for WorkflowImplParameters
                        * This was an Array!
                        */

                        
                                    protected org.wso2.carbon.identity.workflow.mgt.stub.bean.Parameter[] localWorkflowImplParameters ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localWorkflowImplParametersTracker = false ;

                           public boolean isWorkflowImplParametersSpecified(){
                               return localWorkflowImplParametersTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.identity.workflow.mgt.stub.bean.Parameter[]
                           */
                           public  org.wso2.carbon.identity.workflow.mgt.stub.bean.Parameter[] getWorkflowImplParameters(){
                               return localWorkflowImplParameters;
                           }

                           
                        


                               
                              /**
                               * validate the array for WorkflowImplParameters
                               */
                              protected void validateWorkflowImplParameters(org.wso2.carbon.identity.workflow.mgt.stub.bean.Parameter[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param WorkflowImplParameters
                              */
                              public void setWorkflowImplParameters(org.wso2.carbon.identity.workflow.mgt.stub.bean.Parameter[] param){
                              
                                   validateWorkflowImplParameters(param);

                               localWorkflowImplParametersTracker = true;
                                      
                                      this.localWorkflowImplParameters=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param org.wso2.carbon.identity.workflow.mgt.stub.bean.Parameter
                             */
                             public void addWorkflowImplParameters(org.wso2.carbon.identity.workflow.mgt.stub.bean.Parameter param){
                                   if (localWorkflowImplParameters == null){
                                   localWorkflowImplParameters = new org.wso2.carbon.identity.workflow.mgt.stub.bean.Parameter[]{};
                                   }

                            
                                 //update the setting tracker
                                localWorkflowImplParametersTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localWorkflowImplParameters);
                               list.add(param);
                               this.localWorkflowImplParameters =
                             (org.wso2.carbon.identity.workflow.mgt.stub.bean.Parameter[])list.toArray(
                            new org.wso2.carbon.identity.workflow.mgt.stub.bean.Parameter[list.size()]);

                             }
                             

                        /**
                        * field for WorkflowName
                        */

                        
                                    protected java.lang.String localWorkflowName ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localWorkflowNameTracker = false ;

                           public boolean isWorkflowNameSpecified(){
                               return localWorkflowNameTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getWorkflowName(){
                               return localWorkflowName;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param WorkflowName
                               */
                               public void setWorkflowName(java.lang.String param){
                            localWorkflowNameTracker = true;
                                   
                                            this.localWorkflowName=param;
                                    

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
               

                   java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://dto.mgt.workflow.identity.carbon.wso2.org/xsd");
                   if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           namespacePrefix+":WorkflowWizard",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "WorkflowWizard",
                           xmlWriter);
                   }

               
                   }
                if (localTemplateTracker){
                                    if (localTemplate==null){

                                        writeStartElement(null, "http://dto.mgt.workflow.identity.carbon.wso2.org/xsd", "template", xmlWriter);

                                       // write the nil attribute
                                      writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                      xmlWriter.writeEndElement();
                                    }else{
                                     localTemplate.serialize(new javax.xml.namespace.QName("http://dto.mgt.workflow.identity.carbon.wso2.org/xsd","template"),
                                        xmlWriter);
                                    }
                                } if (localTemplateIdTracker){
                                    namespace = "http://dto.mgt.workflow.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "templateId", xmlWriter);
                             

                                          if (localTemplateId==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localTemplateId);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localTemplateParametersTracker){
                                       if (localTemplateParameters!=null){
                                            for (int i = 0;i < localTemplateParameters.length;i++){
                                                if (localTemplateParameters[i] != null){
                                                 localTemplateParameters[i].serialize(new javax.xml.namespace.QName("http://dto.mgt.workflow.identity.carbon.wso2.org/xsd","templateParameters"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://dto.mgt.workflow.identity.carbon.wso2.org/xsd", "templateParameters", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://dto.mgt.workflow.identity.carbon.wso2.org/xsd", "templateParameters", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localWorkflowDescriptionTracker){
                                    namespace = "http://dto.mgt.workflow.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "workflowDescription", xmlWriter);
                             

                                          if (localWorkflowDescription==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localWorkflowDescription);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localWorkflowIdTracker){
                                    namespace = "http://dto.mgt.workflow.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "workflowId", xmlWriter);
                             

                                          if (localWorkflowId==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localWorkflowId);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localWorkflowImplTracker){
                                    if (localWorkflowImpl==null){

                                        writeStartElement(null, "http://dto.mgt.workflow.identity.carbon.wso2.org/xsd", "workflowImpl", xmlWriter);

                                       // write the nil attribute
                                      writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                      xmlWriter.writeEndElement();
                                    }else{
                                     localWorkflowImpl.serialize(new javax.xml.namespace.QName("http://dto.mgt.workflow.identity.carbon.wso2.org/xsd","workflowImpl"),
                                        xmlWriter);
                                    }
                                } if (localWorkflowImplIdTracker){
                                    namespace = "http://dto.mgt.workflow.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "workflowImplId", xmlWriter);
                             

                                          if (localWorkflowImplId==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localWorkflowImplId);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localWorkflowImplParametersTracker){
                                       if (localWorkflowImplParameters!=null){
                                            for (int i = 0;i < localWorkflowImplParameters.length;i++){
                                                if (localWorkflowImplParameters[i] != null){
                                                 localWorkflowImplParameters[i].serialize(new javax.xml.namespace.QName("http://dto.mgt.workflow.identity.carbon.wso2.org/xsd","workflowImplParameters"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://dto.mgt.workflow.identity.carbon.wso2.org/xsd", "workflowImplParameters", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://dto.mgt.workflow.identity.carbon.wso2.org/xsd", "workflowImplParameters", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localWorkflowNameTracker){
                                    namespace = "http://dto.mgt.workflow.identity.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "workflowName", xmlWriter);
                             

                                          if (localWorkflowName==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localWorkflowName);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             }
                    xmlWriter.writeEndElement();
               

        }

        private static java.lang.String generatePrefix(java.lang.String namespace) {
            if(namespace.equals("http://dto.mgt.workflow.identity.carbon.wso2.org/xsd")){
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

                 if (localTemplateTracker){
                            elementList.add(new javax.xml.namespace.QName("http://dto.mgt.workflow.identity.carbon.wso2.org/xsd",
                                                                      "template"));
                            
                            
                                    elementList.add(localTemplate==null?null:
                                    localTemplate);
                                } if (localTemplateIdTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://dto.mgt.workflow.identity.carbon.wso2.org/xsd",
                                                                      "templateId"));
                                 
                                         elementList.add(localTemplateId==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localTemplateId));
                                    } if (localTemplateParametersTracker){
                             if (localTemplateParameters!=null) {
                                 for (int i = 0;i < localTemplateParameters.length;i++){

                                    if (localTemplateParameters[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://dto.mgt.workflow.identity.carbon.wso2.org/xsd",
                                                                          "templateParameters"));
                                         elementList.add(localTemplateParameters[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://dto.mgt.workflow.identity.carbon.wso2.org/xsd",
                                                                          "templateParameters"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://dto.mgt.workflow.identity.carbon.wso2.org/xsd",
                                                                          "templateParameters"));
                                        elementList.add(localTemplateParameters);
                                    
                             }

                        } if (localWorkflowDescriptionTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://dto.mgt.workflow.identity.carbon.wso2.org/xsd",
                                                                      "workflowDescription"));
                                 
                                         elementList.add(localWorkflowDescription==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localWorkflowDescription));
                                    } if (localWorkflowIdTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://dto.mgt.workflow.identity.carbon.wso2.org/xsd",
                                                                      "workflowId"));
                                 
                                         elementList.add(localWorkflowId==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localWorkflowId));
                                    } if (localWorkflowImplTracker){
                            elementList.add(new javax.xml.namespace.QName("http://dto.mgt.workflow.identity.carbon.wso2.org/xsd",
                                                                      "workflowImpl"));
                            
                            
                                    elementList.add(localWorkflowImpl==null?null:
                                    localWorkflowImpl);
                                } if (localWorkflowImplIdTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://dto.mgt.workflow.identity.carbon.wso2.org/xsd",
                                                                      "workflowImplId"));
                                 
                                         elementList.add(localWorkflowImplId==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localWorkflowImplId));
                                    } if (localWorkflowImplParametersTracker){
                             if (localWorkflowImplParameters!=null) {
                                 for (int i = 0;i < localWorkflowImplParameters.length;i++){

                                    if (localWorkflowImplParameters[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://dto.mgt.workflow.identity.carbon.wso2.org/xsd",
                                                                          "workflowImplParameters"));
                                         elementList.add(localWorkflowImplParameters[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://dto.mgt.workflow.identity.carbon.wso2.org/xsd",
                                                                          "workflowImplParameters"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://dto.mgt.workflow.identity.carbon.wso2.org/xsd",
                                                                          "workflowImplParameters"));
                                        elementList.add(localWorkflowImplParameters);
                                    
                             }

                        } if (localWorkflowNameTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://dto.mgt.workflow.identity.carbon.wso2.org/xsd",
                                                                      "workflowName"));
                                 
                                         elementList.add(localWorkflowName==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localWorkflowName));
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
        public static WorkflowWizard parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            WorkflowWizard object =
                new WorkflowWizard();

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
                    
                            if (!"WorkflowWizard".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (WorkflowWizard)org.wso2.carbon.identity.workflow.mgt.stub.ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }
                        

                  }
                

                }

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                
                    
                    reader.next();
                
                        java.util.ArrayList list3 = new java.util.ArrayList();
                    
                        java.util.ArrayList list8 = new java.util.ArrayList();
                    
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://dto.mgt.workflow.identity.carbon.wso2.org/xsd","template").equals(reader.getName())){
                                
                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                          object.setTemplate(null);
                                          reader.next();
                                            
                                            reader.next();
                                          
                                      }else{
                                    
                                                object.setTemplate(org.wso2.carbon.identity.workflow.mgt.stub.metadata.Template.Factory.parse(reader));
                                              
                                        reader.next();
                                    }
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://dto.mgt.workflow.identity.carbon.wso2.org/xsd","templateId").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setTemplateId(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://dto.mgt.workflow.identity.carbon.wso2.org/xsd","templateParameters").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list3.add(null);
                                                              reader.next();
                                                          } else {
                                                        list3.add(org.wso2.carbon.identity.workflow.mgt.stub.bean.Parameter.Factory.parse(reader));
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
                                                                if (new javax.xml.namespace.QName("http://dto.mgt.workflow.identity.carbon.wso2.org/xsd","templateParameters").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list3.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list3.add(org.wso2.carbon.identity.workflow.mgt.stub.bean.Parameter.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone3 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setTemplateParameters((org.wso2.carbon.identity.workflow.mgt.stub.bean.Parameter[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                org.wso2.carbon.identity.workflow.mgt.stub.bean.Parameter.class,
                                                                list3));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://dto.mgt.workflow.identity.carbon.wso2.org/xsd","workflowDescription").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setWorkflowDescription(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://dto.mgt.workflow.identity.carbon.wso2.org/xsd","workflowId").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setWorkflowId(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://dto.mgt.workflow.identity.carbon.wso2.org/xsd","workflowImpl").equals(reader.getName())){
                                
                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                          object.setWorkflowImpl(null);
                                          reader.next();
                                            
                                            reader.next();
                                          
                                      }else{
                                    
                                                object.setWorkflowImpl(org.wso2.carbon.identity.workflow.mgt.stub.metadata.WorkflowImpl.Factory.parse(reader));
                                              
                                        reader.next();
                                    }
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://dto.mgt.workflow.identity.carbon.wso2.org/xsd","workflowImplId").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setWorkflowImplId(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://dto.mgt.workflow.identity.carbon.wso2.org/xsd","workflowImplParameters").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list8.add(null);
                                                              reader.next();
                                                          } else {
                                                        list8.add(org.wso2.carbon.identity.workflow.mgt.stub.bean.Parameter.Factory.parse(reader));
                                                                }
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone8 = false;
                                                        while(!loopDone8){
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
                                                                loopDone8 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("http://dto.mgt.workflow.identity.carbon.wso2.org/xsd","workflowImplParameters").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list8.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list8.add(org.wso2.carbon.identity.workflow.mgt.stub.bean.Parameter.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone8 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setWorkflowImplParameters((org.wso2.carbon.identity.workflow.mgt.stub.bean.Parameter[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                org.wso2.carbon.identity.workflow.mgt.stub.bean.Parameter.class,
                                                                list8));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://dto.mgt.workflow.identity.carbon.wso2.org/xsd","workflowName").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setWorkflowName(
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
           
    