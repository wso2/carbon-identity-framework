
/**
 * ExtensionMapper.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:46 UTC)
 */

        
            package org.wso2.carbon.captcha.mgt.beans.xsd;
        
            /**
            *  ExtensionMapper class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class ExtensionMapper{

          public static java.lang.Object getTypeObject(java.lang.String namespaceURI,
                                                       java.lang.String typeName,
                                                       javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{

              
                  if (
                  "http://beans.mgt.captcha.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "CaptchaInfoBean".equals(typeName)){
                   
                            return  org.wso2.carbon.captcha.mgt.beans.xsd.CaptchaInfoBean.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://dto.mgt.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "ChallengeQuestionIdsDTO".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.mgt.stub.dto.ChallengeQuestionIdsDTO.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://base.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "IdentityException".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.base.xsd.IdentityException.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://dto.mgt.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "UserIdentityClaimDTO".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.mgt.stub.dto.UserIdentityClaimDTO.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://beans.mgt.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "VerificationBean".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.mgt.stub.beans.VerificationBean.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://dto.mgt.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "UserChallengesDTO".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.mgt.stub.dto.UserChallengesDTO.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://mgt.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "IdentityMgtServiceException".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.mgt.xsd.IdentityMgtServiceException.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://dto.mgt.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "ChallengeQuestionDTO".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.mgt.stub.dto.ChallengeQuestionDTO.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://dto.mgt.identity.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "NotificationDataDTO".equals(typeName)){
                   
                            return  org.wso2.carbon.identity.mgt.stub.dto.NotificationDataDTO.Factory.parse(reader);
                        

                  }

              
             throw new org.apache.axis2.databinding.ADBException("Unsupported type " + namespaceURI + " " + typeName);
          }

        }
    