
/**
 * ExtensionMapper.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:46 UTC)
 */

        
            package org.wso2.carbon.identity.workflow.mgt.stub;
        
            /**
            *  ExtensionMapper class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class ExtensionMapper{

          public static java.lang.Object getTypeObject(java.lang.String namespaceURI,
                                                       java.lang.String typeName,
                                                       javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{

              
                  if (
                  "http://metadata.bean.mgt.workflow.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "InputData".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.workflow.mgt.stub.metadata.bean.InputData.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://bean.mgt.workflow.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "WorkflowRequest".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.workflow.mgt.stub.bean.WorkflowRequest.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://dto.mgt.workflow.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "WorkflowImpl".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.workflow.mgt.stub.metadata.WorkflowImpl.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://dto.mgt.workflow.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "WorkflowEvent".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.workflow.mgt.stub.metadata.WorkflowEvent.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://exception.mgt.workflow.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "WorkflowException".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.workflow.mgt.stub.WorkflowException.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://metadata.bean.mgt.workflow.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "ParametersMetaData".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.workflow.mgt.stub.metadata.bean.ParametersMetaData.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://bean.mgt.workflow.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "Parameter".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.workflow.mgt.stub.bean.Parameter.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://dto.mgt.workflow.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "WorkflowWizard".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.workflow.mgt.stub.metadata.WorkflowWizard.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://metadata.bean.mgt.workflow.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "ParameterMetaData".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.workflow.mgt.stub.metadata.bean.ParameterMetaData.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://dto.mgt.workflow.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "Association".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.workflow.mgt.stub.metadata.Association.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://bean.mgt.workflow.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "WorkflowRequestAssociation".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.workflow.mgt.stub.bean.WorkflowRequestAssociation.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://metadata.bean.mgt.workflow.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "Item".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.workflow.mgt.stub.metadata.bean.Item.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://metadata.bean.mgt.workflow.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "MapType".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.workflow.mgt.stub.metadata.bean.MapType.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://dto.mgt.workflow.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "Template".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.workflow.mgt.stub.metadata.Template.Factory.parse(reader);
                        

                  }

              
             throw new org.apache.axis2.databinding.ADBException("Unsupported type " + namespaceURI + " " + typeName);
          }

        }
    