
/**
 * TTaskEventType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:46 UTC)
 */

            
                package org.wso2.carbon.humantask.stub.types;
            

            /**
            *  TTaskEventType bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class TTaskEventType
        implements org.apache.axis2.databinding.ADBBean{
        
                public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                "tTaskEventType",
                "ns1");

            

                        /**
                        * field for TTaskEventType
                        */

                        
                                    protected java.lang.String localTTaskEventType ;
                                
                            private static java.util.HashMap _table_ = new java.util.HashMap();

                            // Constructor
                            
                                protected TTaskEventType(java.lang.String value, boolean isRegisterValue) {
                                    localTTaskEventType = value;
                                    if (isRegisterValue){
                                        
                                               _table_.put(localTTaskEventType, this);
                                           
                                    }

                                }
                            
                                    public static final java.lang.String _create =
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString("create");
                                
                                    public static final java.lang.String _claim =
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString("claim");
                                
                                    public static final java.lang.String _start =
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString("start");
                                
                                    public static final java.lang.String _stop =
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString("stop");
                                
                                    public static final java.lang.String _release =
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString("release");
                                
                                    public static final java.lang.String _suspend =
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString("suspend");
                                
                                    public static final java.lang.String _suspendUntil =
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString("suspendUntil");
                                
                                    public static final java.lang.String _resume =
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString("resume");
                                
                                    public static final java.lang.String _complete =
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString("complete");
                                
                                    public static final java.lang.String _remove =
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString("remove");
                                
                                    public static final java.lang.String _fail =
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString("fail");
                                
                                    public static final java.lang.String _setPriority =
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString("setPriority");
                                
                                    public static final java.lang.String _addAttachment =
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString("addAttachment");
                                
                                    public static final java.lang.String _deleteattachment =
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString("deleteattachment");
                                
                                    public static final java.lang.String _addComment =
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString("addComment");
                                
                                    public static final java.lang.String _skip =
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString("skip");
                                
                                    public static final java.lang.String _forward =
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString("forward");
                                
                                    public static final java.lang.String _delegate =
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString("delegate");
                                
                                    public static final java.lang.String _setOutput =
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString("setOutput");
                                
                                    public static final java.lang.String _deleteOutput =
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString("deleteOutput");
                                
                                    public static final java.lang.String _setFault =
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString("setFault");
                                
                                    public static final java.lang.String _deleteFault =
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString("deleteFault");
                                
                                    public static final java.lang.String _activate =
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString("activate");
                                
                                    public static final java.lang.String _nominate =
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString("nominate");
                                
                                    public static final java.lang.String _setGenericHumanRole =
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString("setGenericHumanRole");
                                
                                    public static final java.lang.String _expire =
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString("expire");
                                
                                    public static final java.lang.String _escalated =
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString("escalated");
                                
                                public static final TTaskEventType create =
                                    new TTaskEventType(_create,true);
                            
                                public static final TTaskEventType claim =
                                    new TTaskEventType(_claim,true);
                            
                                public static final TTaskEventType start =
                                    new TTaskEventType(_start,true);
                            
                                public static final TTaskEventType stop =
                                    new TTaskEventType(_stop,true);
                            
                                public static final TTaskEventType release =
                                    new TTaskEventType(_release,true);
                            
                                public static final TTaskEventType suspend =
                                    new TTaskEventType(_suspend,true);
                            
                                public static final TTaskEventType suspendUntil =
                                    new TTaskEventType(_suspendUntil,true);
                            
                                public static final TTaskEventType resume =
                                    new TTaskEventType(_resume,true);
                            
                                public static final TTaskEventType complete =
                                    new TTaskEventType(_complete,true);
                            
                                public static final TTaskEventType remove =
                                    new TTaskEventType(_remove,true);
                            
                                public static final TTaskEventType fail =
                                    new TTaskEventType(_fail,true);
                            
                                public static final TTaskEventType setPriority =
                                    new TTaskEventType(_setPriority,true);
                            
                                public static final TTaskEventType addAttachment =
                                    new TTaskEventType(_addAttachment,true);
                            
                                public static final TTaskEventType deleteattachment =
                                    new TTaskEventType(_deleteattachment,true);
                            
                                public static final TTaskEventType addComment =
                                    new TTaskEventType(_addComment,true);
                            
                                public static final TTaskEventType skip =
                                    new TTaskEventType(_skip,true);
                            
                                public static final TTaskEventType forward =
                                    new TTaskEventType(_forward,true);
                            
                                public static final TTaskEventType delegate =
                                    new TTaskEventType(_delegate,true);
                            
                                public static final TTaskEventType setOutput =
                                    new TTaskEventType(_setOutput,true);
                            
                                public static final TTaskEventType deleteOutput =
                                    new TTaskEventType(_deleteOutput,true);
                            
                                public static final TTaskEventType setFault =
                                    new TTaskEventType(_setFault,true);
                            
                                public static final TTaskEventType deleteFault =
                                    new TTaskEventType(_deleteFault,true);
                            
                                public static final TTaskEventType activate =
                                    new TTaskEventType(_activate,true);
                            
                                public static final TTaskEventType nominate =
                                    new TTaskEventType(_nominate,true);
                            
                                public static final TTaskEventType setGenericHumanRole =
                                    new TTaskEventType(_setGenericHumanRole,true);
                            
                                public static final TTaskEventType expire =
                                    new TTaskEventType(_expire,true);
                            
                                public static final TTaskEventType escalated =
                                    new TTaskEventType(_escalated,true);
                            

                                public java.lang.String getValue() { return localTTaskEventType;}

                                public boolean equals(java.lang.Object obj) {return (obj == this);}
                                public int hashCode() { return toString().hashCode();}
                                public java.lang.String toString() {
                                
                                        return localTTaskEventType.toString();
                                    

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
                       new org.apache.axis2.databinding.ADBDataSource(this,MY_QNAME);
               return factory.createOMElement(dataSource,MY_QNAME);
            
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
            
                
                //We can safely assume an element has only one type associated with it
                
                            java.lang.String namespace = parentQName.getNamespaceURI();
                            java.lang.String _localName = parentQName.getLocalPart();
                        
                            writeStartElement(null, namespace, _localName, xmlWriter);

                            // add the type details if this is used in a simple type
                               if (serializeType){
                                   java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803");
                                   if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                                           namespacePrefix+":tTaskEventType",
                                           xmlWriter);
                                   } else {
                                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                                           "tTaskEventType",
                                           xmlWriter);
                                   }
                               }
                            
                                          if (localTTaskEventType==null){
                                            
                                                     throw new org.apache.axis2.databinding.ADBException("tTaskEventType cannot be null !!");
                                                
                                         }else{
                                        
                                                       xmlWriter.writeCharacters(localTTaskEventType);
                                            
                                         }
                                    
                            xmlWriter.writeEndElement();
                    

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


        
                
                //We can safely assume an element has only one type associated with it
                 return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(MY_QNAME,
                            new java.lang.Object[]{
                            org.apache.axis2.databinding.utils.reader.ADBXMLStreamReader.ELEMENT_TEXT,
                            org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localTTaskEventType)
                            },
                            null);

        }

  

     /**
      *  Factory class that keeps the parse method
      */
    public static class Factory{

        
        
                public static TTaskEventType fromValue(java.lang.String value)
                      throws java.lang.IllegalArgumentException {
                    TTaskEventType enumeration = (TTaskEventType)
                       
                               _table_.get(value);
                           

                    if ((enumeration == null) && !((value == null) || (value.equals("")))) {
                        throw new java.lang.IllegalArgumentException();
                    }
                    return enumeration;
                }
                public static TTaskEventType fromString(java.lang.String value,java.lang.String namespaceURI)
                      throws java.lang.IllegalArgumentException {
                    try {
                       
                                       return fromValue(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(value));
                                   

                    } catch (java.lang.Exception e) {
                        throw new java.lang.IllegalArgumentException();
                    }
                }

                public static TTaskEventType fromString(javax.xml.stream.XMLStreamReader xmlStreamReader,
                                                                    java.lang.String content) {
                    if (content.indexOf(":") > -1){
                        java.lang.String prefix = content.substring(0,content.indexOf(":"));
                        java.lang.String namespaceUri = xmlStreamReader.getNamespaceContext().getNamespaceURI(prefix);
                        return TTaskEventType.Factory.fromString(content,namespaceUri);
                    } else {
                       return TTaskEventType.Factory.fromString(content,"");
                    }
                }
            

        /**
        * static method to create the object
        * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
        *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
        * Postcondition: If this object is an element, the reader is positioned at its end element
        *                If this object is a complex type, the reader is positioned at the end element of its outer element
        */
        public static TTaskEventType parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            TTaskEventType object = null;
                // initialize a hash map to keep values
                java.util.Map attributeMap = new java.util.HashMap();
                java.util.List extraAttributeList = new java.util.ArrayList<org.apache.axiom.om.OMAttribute>();
            

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
                

                   
                while(!reader.isEndElement()) {
                    if (reader.isStartElement()  || reader.hasText()){
                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"tTaskEventType" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                        if (content.indexOf(":") > 0) {
                                            // this seems to be a Qname so find the namespace and send
                                            prefix = content.substring(0, content.indexOf(":"));
                                            namespaceuri = reader.getNamespaceURI(prefix);
                                            object = TTaskEventType.Factory.fromString(content,namespaceuri);
                                        } else {
                                            // this seems to be not a qname send and empty namespace incase of it is
                                            // check is done in fromString method
                                            object = TTaskEventType.Factory.fromString(content,"");
                                        }
                                        
                                        
                             } else {
                                reader.next();
                             }  
                           }  // end of while loop
                        



            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class

        

        }
           
    