
/**
 * TTaskAbstract.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:46 UTC)
 */

            
                package org.wso2.carbon.humantask.stub.types;
            

            /**
            *  TTaskAbstract bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class TTaskAbstract
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = tTaskAbstract
                Namespace URI = http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803
                Namespace Prefix = ns1
                */
            

                        /**
                        * field for Id
                        */

                        
                                    protected org.apache.axis2.databinding.types.URI localId ;
                                

                           /**
                           * Auto generated getter method
                           * @return org.apache.axis2.databinding.types.URI
                           */
                           public  org.apache.axis2.databinding.types.URI getId(){
                               return localId;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Id
                               */
                               public void setId(org.apache.axis2.databinding.types.URI param){
                            
                                            this.localId=param;
                                    

                               }
                            

                        /**
                        * field for TaskType
                        */

                        
                                    protected java.lang.String localTaskType ;
                                

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getTaskType(){
                               return localTaskType;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param TaskType
                               */
                               public void setTaskType(java.lang.String param){
                            
                                            this.localTaskType=param;
                                    

                               }
                            

                        /**
                        * field for Name
                        */

                        
                                    protected javax.xml.namespace.QName localName ;
                                

                           /**
                           * Auto generated getter method
                           * @return javax.xml.namespace.QName
                           */
                           public  javax.xml.namespace.QName getName(){
                               return localName;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Name
                               */
                               public void setName(javax.xml.namespace.QName param){
                            
                                            this.localName=param;
                                    

                               }
                            

                        /**
                        * field for Status
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TStatus localStatus ;
                                

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TStatus
                           */
                           public  org.wso2.carbon.humantask.stub.types.TStatus getStatus(){
                               return localStatus;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Status
                               */
                               public void setStatus(org.wso2.carbon.humantask.stub.types.TStatus param){
                            
                                            this.localStatus=param;
                                    

                               }
                            

                        /**
                        * field for Priority
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TPriority localPriority ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPriorityTracker = false ;

                           public boolean isPrioritySpecified(){
                               return localPriorityTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TPriority
                           */
                           public  org.wso2.carbon.humantask.stub.types.TPriority getPriority(){
                               return localPriority;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Priority
                               */
                               public void setPriority(org.wso2.carbon.humantask.stub.types.TPriority param){
                            localPriorityTracker = param != null;
                                   
                                            this.localPriority=param;
                                    

                               }
                            

                        /**
                        * field for TaskInitiator
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TUser localTaskInitiator ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localTaskInitiatorTracker = false ;

                           public boolean isTaskInitiatorSpecified(){
                               return localTaskInitiatorTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TUser
                           */
                           public  org.wso2.carbon.humantask.stub.types.TUser getTaskInitiator(){
                               return localTaskInitiator;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param TaskInitiator
                               */
                               public void setTaskInitiator(org.wso2.carbon.humantask.stub.types.TUser param){
                            localTaskInitiatorTracker = param != null;
                                   
                                            this.localTaskInitiator=param;
                                    

                               }
                            

                        /**
                        * field for TaskStakeholders
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TOrganizationalEntity localTaskStakeholders ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localTaskStakeholdersTracker = false ;

                           public boolean isTaskStakeholdersSpecified(){
                               return localTaskStakeholdersTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TOrganizationalEntity
                           */
                           public  org.wso2.carbon.humantask.stub.types.TOrganizationalEntity getTaskStakeholders(){
                               return localTaskStakeholders;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param TaskStakeholders
                               */
                               public void setTaskStakeholders(org.wso2.carbon.humantask.stub.types.TOrganizationalEntity param){
                            localTaskStakeholdersTracker = param != null;
                                   
                                            this.localTaskStakeholders=param;
                                    

                               }
                            

                        /**
                        * field for PotentialOwners
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TOrganizationalEntity localPotentialOwners ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPotentialOwnersTracker = false ;

                           public boolean isPotentialOwnersSpecified(){
                               return localPotentialOwnersTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TOrganizationalEntity
                           */
                           public  org.wso2.carbon.humantask.stub.types.TOrganizationalEntity getPotentialOwners(){
                               return localPotentialOwners;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PotentialOwners
                               */
                               public void setPotentialOwners(org.wso2.carbon.humantask.stub.types.TOrganizationalEntity param){
                            localPotentialOwnersTracker = param != null;
                                   
                                            this.localPotentialOwners=param;
                                    

                               }
                            

                        /**
                        * field for BusinessAdministrators
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TOrganizationalEntity localBusinessAdministrators ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localBusinessAdministratorsTracker = false ;

                           public boolean isBusinessAdministratorsSpecified(){
                               return localBusinessAdministratorsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TOrganizationalEntity
                           */
                           public  org.wso2.carbon.humantask.stub.types.TOrganizationalEntity getBusinessAdministrators(){
                               return localBusinessAdministrators;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param BusinessAdministrators
                               */
                               public void setBusinessAdministrators(org.wso2.carbon.humantask.stub.types.TOrganizationalEntity param){
                            localBusinessAdministratorsTracker = param != null;
                                   
                                            this.localBusinessAdministrators=param;
                                    

                               }
                            

                        /**
                        * field for ActualOwner
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TUser localActualOwner ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localActualOwnerTracker = false ;

                           public boolean isActualOwnerSpecified(){
                               return localActualOwnerTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TUser
                           */
                           public  org.wso2.carbon.humantask.stub.types.TUser getActualOwner(){
                               return localActualOwner;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ActualOwner
                               */
                               public void setActualOwner(org.wso2.carbon.humantask.stub.types.TUser param){
                            localActualOwnerTracker = param != null;
                                   
                                            this.localActualOwner=param;
                                    

                               }
                            

                        /**
                        * field for NotificationRecipients
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TOrganizationalEntity localNotificationRecipients ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localNotificationRecipientsTracker = false ;

                           public boolean isNotificationRecipientsSpecified(){
                               return localNotificationRecipientsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TOrganizationalEntity
                           */
                           public  org.wso2.carbon.humantask.stub.types.TOrganizationalEntity getNotificationRecipients(){
                               return localNotificationRecipients;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param NotificationRecipients
                               */
                               public void setNotificationRecipients(org.wso2.carbon.humantask.stub.types.TOrganizationalEntity param){
                            localNotificationRecipientsTracker = param != null;
                                   
                                            this.localNotificationRecipients=param;
                                    

                               }
                            

                        /**
                        * field for CreatedTime
                        */

                        
                                    protected java.util.Calendar localCreatedTime ;
                                

                           /**
                           * Auto generated getter method
                           * @return java.util.Calendar
                           */
                           public  java.util.Calendar getCreatedTime(){
                               return localCreatedTime;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param CreatedTime
                               */
                               public void setCreatedTime(java.util.Calendar param){
                            
                                            this.localCreatedTime=param;
                                    

                               }
                            

                        /**
                        * field for UpdatedTime
                        */

                        
                                    protected java.util.Calendar localUpdatedTime ;
                                

                           /**
                           * Auto generated getter method
                           * @return java.util.Calendar
                           */
                           public  java.util.Calendar getUpdatedTime(){
                               return localUpdatedTime;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param UpdatedTime
                               */
                               public void setUpdatedTime(java.util.Calendar param){
                            
                                            this.localUpdatedTime=param;
                                    

                               }
                            

                        /**
                        * field for ActivationTime
                        */

                        
                                    protected java.util.Calendar localActivationTime ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localActivationTimeTracker = false ;

                           public boolean isActivationTimeSpecified(){
                               return localActivationTimeTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.util.Calendar
                           */
                           public  java.util.Calendar getActivationTime(){
                               return localActivationTime;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ActivationTime
                               */
                               public void setActivationTime(java.util.Calendar param){
                            localActivationTimeTracker = param != null;
                                   
                                            this.localActivationTime=param;
                                    

                               }
                            

                        /**
                        * field for ExpirationTime
                        */

                        
                                    protected java.util.Calendar localExpirationTime ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localExpirationTimeTracker = false ;

                           public boolean isExpirationTimeSpecified(){
                               return localExpirationTimeTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.util.Calendar
                           */
                           public  java.util.Calendar getExpirationTime(){
                               return localExpirationTime;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ExpirationTime
                               */
                               public void setExpirationTime(java.util.Calendar param){
                            localExpirationTimeTracker = param != null;
                                   
                                            this.localExpirationTime=param;
                                    

                               }
                            

                        /**
                        * field for IsSkipable
                        */

                        
                                    protected boolean localIsSkipable ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localIsSkipableTracker = false ;

                           public boolean isIsSkipableSpecified(){
                               return localIsSkipableTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getIsSkipable(){
                               return localIsSkipable;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param IsSkipable
                               */
                               public void setIsSkipable(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localIsSkipableTracker =
                                       true;
                                   
                                            this.localIsSkipable=param;
                                    

                               }
                            

                        /**
                        * field for HasPotentialOwners
                        */

                        
                                    protected boolean localHasPotentialOwners ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localHasPotentialOwnersTracker = false ;

                           public boolean isHasPotentialOwnersSpecified(){
                               return localHasPotentialOwnersTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getHasPotentialOwners(){
                               return localHasPotentialOwners;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param HasPotentialOwners
                               */
                               public void setHasPotentialOwners(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localHasPotentialOwnersTracker =
                                       true;
                                   
                                            this.localHasPotentialOwners=param;
                                    

                               }
                            

                        /**
                        * field for StartByTimeExists
                        */

                        
                                    protected boolean localStartByTimeExists ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localStartByTimeExistsTracker = false ;

                           public boolean isStartByTimeExistsSpecified(){
                               return localStartByTimeExistsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getStartByTimeExists(){
                               return localStartByTimeExists;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param StartByTimeExists
                               */
                               public void setStartByTimeExists(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localStartByTimeExistsTracker =
                                       true;
                                   
                                            this.localStartByTimeExists=param;
                                    

                               }
                            

                        /**
                        * field for CompleteByTimeExists
                        */

                        
                                    protected boolean localCompleteByTimeExists ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localCompleteByTimeExistsTracker = false ;

                           public boolean isCompleteByTimeExistsSpecified(){
                               return localCompleteByTimeExistsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getCompleteByTimeExists(){
                               return localCompleteByTimeExists;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param CompleteByTimeExists
                               */
                               public void setCompleteByTimeExists(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localCompleteByTimeExistsTracker =
                                       true;
                                   
                                            this.localCompleteByTimeExists=param;
                                    

                               }
                            

                        /**
                        * field for PresentationName
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TPresentationName localPresentationName ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPresentationNameTracker = false ;

                           public boolean isPresentationNameSpecified(){
                               return localPresentationNameTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TPresentationName
                           */
                           public  org.wso2.carbon.humantask.stub.types.TPresentationName getPresentationName(){
                               return localPresentationName;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PresentationName
                               */
                               public void setPresentationName(org.wso2.carbon.humantask.stub.types.TPresentationName param){
                            localPresentationNameTracker = param != null;
                                   
                                            this.localPresentationName=param;
                                    

                               }
                            

                        /**
                        * field for PresentationSubject
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TPresentationSubject localPresentationSubject ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPresentationSubjectTracker = false ;

                           public boolean isPresentationSubjectSpecified(){
                               return localPresentationSubjectTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TPresentationSubject
                           */
                           public  org.wso2.carbon.humantask.stub.types.TPresentationSubject getPresentationSubject(){
                               return localPresentationSubject;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PresentationSubject
                               */
                               public void setPresentationSubject(org.wso2.carbon.humantask.stub.types.TPresentationSubject param){
                            localPresentationSubjectTracker = param != null;
                                   
                                            this.localPresentationSubject=param;
                                    

                               }
                            

                        /**
                        * field for PresentationDescription
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TPresentationDescription localPresentationDescription ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPresentationDescriptionTracker = false ;

                           public boolean isPresentationDescriptionSpecified(){
                               return localPresentationDescriptionTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TPresentationDescription
                           */
                           public  org.wso2.carbon.humantask.stub.types.TPresentationDescription getPresentationDescription(){
                               return localPresentationDescription;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PresentationDescription
                               */
                               public void setPresentationDescription(org.wso2.carbon.humantask.stub.types.TPresentationDescription param){
                            localPresentationDescriptionTracker = param != null;
                                   
                                            this.localPresentationDescription=param;
                                    

                               }
                            

                        /**
                        * field for RenderingMethodExists
                        */

                        
                                    protected boolean localRenderingMethodExists ;
                                

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getRenderingMethodExists(){
                               return localRenderingMethodExists;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param RenderingMethodExists
                               */
                               public void setRenderingMethodExists(boolean param){
                            
                                            this.localRenderingMethodExists=param;
                                    

                               }
                            

                        /**
                        * field for HasOutput
                        */

                        
                                    protected boolean localHasOutput ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localHasOutputTracker = false ;

                           public boolean isHasOutputSpecified(){
                               return localHasOutputTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getHasOutput(){
                               return localHasOutput;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param HasOutput
                               */
                               public void setHasOutput(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localHasOutputTracker =
                                       true;
                                   
                                            this.localHasOutput=param;
                                    

                               }
                            

                        /**
                        * field for HasFault
                        */

                        
                                    protected boolean localHasFault ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localHasFaultTracker = false ;

                           public boolean isHasFaultSpecified(){
                               return localHasFaultTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getHasFault(){
                               return localHasFault;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param HasFault
                               */
                               public void setHasFault(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localHasFaultTracker =
                                       true;
                                   
                                            this.localHasFault=param;
                                    

                               }
                            

                        /**
                        * field for HasAttachments
                        */

                        
                                    protected boolean localHasAttachments ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localHasAttachmentsTracker = false ;

                           public boolean isHasAttachmentsSpecified(){
                               return localHasAttachmentsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getHasAttachments(){
                               return localHasAttachments;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param HasAttachments
                               */
                               public void setHasAttachments(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localHasAttachmentsTracker =
                                       true;
                                   
                                            this.localHasAttachments=param;
                                    

                               }
                            

                        /**
                        * field for HasComments
                        */

                        
                                    protected boolean localHasComments ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localHasCommentsTracker = false ;

                           public boolean isHasCommentsSpecified(){
                               return localHasCommentsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getHasComments(){
                               return localHasComments;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param HasComments
                               */
                               public void setHasComments(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localHasCommentsTracker =
                                       true;
                                   
                                            this.localHasComments=param;
                                    

                               }
                            

                        /**
                        * field for Escalated
                        */

                        
                                    protected boolean localEscalated ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localEscalatedTracker = false ;

                           public boolean isEscalatedSpecified(){
                               return localEscalatedTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getEscalated(){
                               return localEscalated;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Escalated
                               */
                               public void setEscalated(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localEscalatedTracker =
                                       true;
                                   
                                            this.localEscalated=param;
                                    

                               }
                            

                        /**
                        * field for Outcome
                        */

                        
                                    protected java.lang.String localOutcome ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localOutcomeTracker = false ;

                           public boolean isOutcomeSpecified(){
                               return localOutcomeTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getOutcome(){
                               return localOutcome;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Outcome
                               */
                               public void setOutcome(java.lang.String param){
                            localOutcomeTracker = param != null;
                                   
                                            this.localOutcome=param;
                                    

                               }
                            

                        /**
                        * field for ParentTaskId
                        */

                        
                                    protected org.apache.axis2.databinding.types.URI localParentTaskId ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localParentTaskIdTracker = false ;

                           public boolean isParentTaskIdSpecified(){
                               return localParentTaskIdTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.apache.axis2.databinding.types.URI
                           */
                           public  org.apache.axis2.databinding.types.URI getParentTaskId(){
                               return localParentTaskId;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ParentTaskId
                               */
                               public void setParentTaskId(org.apache.axis2.databinding.types.URI param){
                            localParentTaskIdTracker = param != null;
                                   
                                            this.localParentTaskId=param;
                                    

                               }
                            

                        /**
                        * field for HasSubTasks
                        */

                        
                                    protected boolean localHasSubTasks ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localHasSubTasksTracker = false ;

                           public boolean isHasSubTasksSpecified(){
                               return localHasSubTasksTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getHasSubTasks(){
                               return localHasSubTasks;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param HasSubTasks
                               */
                               public void setHasSubTasks(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localHasSubTasksTracker =
                                       true;
                                   
                                            this.localHasSubTasks=param;
                                    

                               }
                            

                        /**
                        * field for NumberOfComments
                        */

                        
                                    protected int localNumberOfComments ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localNumberOfCommentsTracker = false ;

                           public boolean isNumberOfCommentsSpecified(){
                               return localNumberOfCommentsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return int
                           */
                           public  int getNumberOfComments(){
                               return localNumberOfComments;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param NumberOfComments
                               */
                               public void setNumberOfComments(int param){
                            
                                       // setting primitive attribute tracker to true
                                       localNumberOfCommentsTracker =
                                       param != java.lang.Integer.MIN_VALUE;
                                   
                                            this.localNumberOfComments=param;
                                    

                               }
                            

                        /**
                        * field for NumberOfAttachments
                        */

                        
                                    protected int localNumberOfAttachments ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localNumberOfAttachmentsTracker = false ;

                           public boolean isNumberOfAttachmentsSpecified(){
                               return localNumberOfAttachmentsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return int
                           */
                           public  int getNumberOfAttachments(){
                               return localNumberOfAttachments;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param NumberOfAttachments
                               */
                               public void setNumberOfAttachments(int param){
                            
                                       // setting primitive attribute tracker to true
                                       localNumberOfAttachmentsTracker =
                                       param != java.lang.Integer.MIN_VALUE;
                                   
                                            this.localNumberOfAttachments=param;
                                    

                               }
                            

                        /**
                        * field for PreviousStatus
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TStatus localPreviousStatus ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPreviousStatusTracker = false ;

                           public boolean isPreviousStatusSpecified(){
                               return localPreviousStatusTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TStatus
                           */
                           public  org.wso2.carbon.humantask.stub.types.TStatus getPreviousStatus(){
                               return localPreviousStatus;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PreviousStatus
                               */
                               public void setPreviousStatus(org.wso2.carbon.humantask.stub.types.TStatus param){
                            localPreviousStatusTracker = param != null;
                                   
                                            this.localPreviousStatus=param;
                                    

                               }
                            

                        /**
                        * field for ResponseServiceName
                        */

                        
                                    protected java.lang.String localResponseServiceName ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localResponseServiceNameTracker = false ;

                           public boolean isResponseServiceNameSpecified(){
                               return localResponseServiceNameTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getResponseServiceName(){
                               return localResponseServiceName;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ResponseServiceName
                               */
                               public void setResponseServiceName(java.lang.String param){
                            localResponseServiceNameTracker = param != null;
                                   
                                            this.localResponseServiceName=param;
                                    

                               }
                            

                        /**
                        * field for ResponseOperationName
                        */

                        
                                    protected java.lang.String localResponseOperationName ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localResponseOperationNameTracker = false ;

                           public boolean isResponseOperationNameSpecified(){
                               return localResponseOperationNameTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getResponseOperationName(){
                               return localResponseOperationName;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ResponseOperationName
                               */
                               public void setResponseOperationName(java.lang.String param){
                            localResponseOperationNameTracker = param != null;
                                   
                                            this.localResponseOperationName=param;
                                    

                               }
                            

                        /**
                        * field for IsClaimable
                        */

                        
                                    protected boolean localIsClaimable ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localIsClaimableTracker = false ;

                           public boolean isIsClaimableSpecified(){
                               return localIsClaimableTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getIsClaimable(){
                               return localIsClaimable;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param IsClaimable
                               */
                               public void setIsClaimable(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localIsClaimableTracker =
                                       true;
                                   
                                            this.localIsClaimable=param;
                                    

                               }
                            

                        /**
                        * field for IsStartable
                        */

                        
                                    protected boolean localIsStartable ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localIsStartableTracker = false ;

                           public boolean isIsStartableSpecified(){
                               return localIsStartableTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getIsStartable(){
                               return localIsStartable;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param IsStartable
                               */
                               public void setIsStartable(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localIsStartableTracker =
                                       true;
                                   
                                            this.localIsStartable=param;
                                    

                               }
                            

                        /**
                        * field for IsStopable
                        */

                        
                                    protected boolean localIsStopable ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localIsStopableTracker = false ;

                           public boolean isIsStopableSpecified(){
                               return localIsStopableTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getIsStopable(){
                               return localIsStopable;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param IsStopable
                               */
                               public void setIsStopable(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localIsStopableTracker =
                                       true;
                                   
                                            this.localIsStopable=param;
                                    

                               }
                            

                        /**
                        * field for IsReleasable
                        */

                        
                                    protected boolean localIsReleasable ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localIsReleasableTracker = false ;

                           public boolean isIsReleasableSpecified(){
                               return localIsReleasableTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getIsReleasable(){
                               return localIsReleasable;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param IsReleasable
                               */
                               public void setIsReleasable(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localIsReleasableTracker =
                                       true;
                                   
                                            this.localIsReleasable=param;
                                    

                               }
                            

                        /**
                        * field for IsSuspendable
                        */

                        
                                    protected boolean localIsSuspendable ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localIsSuspendableTracker = false ;

                           public boolean isIsSuspendableSpecified(){
                               return localIsSuspendableTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getIsSuspendable(){
                               return localIsSuspendable;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param IsSuspendable
                               */
                               public void setIsSuspendable(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localIsSuspendableTracker =
                                       true;
                                   
                                            this.localIsSuspendable=param;
                                    

                               }
                            

                        /**
                        * field for IsResumable
                        */

                        
                                    protected boolean localIsResumable ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localIsResumableTracker = false ;

                           public boolean isIsResumableSpecified(){
                               return localIsResumableTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getIsResumable(){
                               return localIsResumable;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param IsResumable
                               */
                               public void setIsResumable(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localIsResumableTracker =
                                       true;
                                   
                                            this.localIsResumable=param;
                                    

                               }
                            

                        /**
                        * field for IsCompletable
                        */

                        
                                    protected boolean localIsCompletable ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localIsCompletableTracker = false ;

                           public boolean isIsCompletableSpecified(){
                               return localIsCompletableTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getIsCompletable(){
                               return localIsCompletable;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param IsCompletable
                               */
                               public void setIsCompletable(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localIsCompletableTracker =
                                       true;
                                   
                                            this.localIsCompletable=param;
                                    

                               }
                            

                        /**
                        * field for IsRemovable
                        */

                        
                                    protected boolean localIsRemovable ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localIsRemovableTracker = false ;

                           public boolean isIsRemovableSpecified(){
                               return localIsRemovableTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getIsRemovable(){
                               return localIsRemovable;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param IsRemovable
                               */
                               public void setIsRemovable(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localIsRemovableTracker =
                                       true;
                                   
                                            this.localIsRemovable=param;
                                    

                               }
                            

                        /**
                        * field for IsForwardable
                        */

                        
                                    protected boolean localIsForwardable ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localIsForwardableTracker = false ;

                           public boolean isIsForwardableSpecified(){
                               return localIsForwardableTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getIsForwardable(){
                               return localIsForwardable;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param IsForwardable
                               */
                               public void setIsForwardable(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localIsForwardableTracker =
                                       true;
                                   
                                            this.localIsForwardable=param;
                                    

                               }
                            

                        /**
                        * field for IsDelegatable
                        */

                        
                                    protected boolean localIsDelegatable ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localIsDelegatableTracker = false ;

                           public boolean isIsDelegatableSpecified(){
                               return localIsDelegatableTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getIsDelegatable(){
                               return localIsDelegatable;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param IsDelegatable
                               */
                               public void setIsDelegatable(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localIsDelegatableTracker =
                                       true;
                                   
                                            this.localIsDelegatable=param;
                                    

                               }
                            

                        /**
                        * field for PackageName
                        */

                        
                                    protected java.lang.String localPackageName ;
                                

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getPackageName(){
                               return localPackageName;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PackageName
                               */
                               public void setPackageName(java.lang.String param){
                            
                                            this.localPackageName=param;
                                    

                               }
                            

                        /**
                        * field for TenantId
                        */

                        
                                    protected int localTenantId ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localTenantIdTracker = false ;

                           public boolean isTenantIdSpecified(){
                               return localTenantIdTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return int
                           */
                           public  int getTenantId(){
                               return localTenantId;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param TenantId
                               */
                               public void setTenantId(int param){
                            
                                       // setting primitive attribute tracker to true
                                       localTenantIdTracker =
                                       param != java.lang.Integer.MIN_VALUE;
                                   
                                            this.localTenantId=param;
                                    

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
               

                   java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803");
                   if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           namespacePrefix+":tTaskAbstract",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "tTaskAbstract",
                           xmlWriter);
                   }

               
                   }
               
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803";
                                    writeStartElement(null, namespace, "id", xmlWriter);
                             

                                          if (localId==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("id cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localId));
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803";
                                    writeStartElement(null, namespace, "taskType", xmlWriter);
                             

                                          if (localTaskType==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("taskType cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localTaskType);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803";
                                    writeStartElement(null, namespace, "name", xmlWriter);
                             

                                          if (localName==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("name cannot be null!!");
                                                  
                                          }else{

                                        
                                                writeQName(localName,xmlWriter);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             
                                            if (localStatus==null){
                                                 throw new org.apache.axis2.databinding.ADBException("status cannot be null!!");
                                            }
                                           localStatus.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","status"),
                                               xmlWriter);
                                         if (localPriorityTracker){
                                            if (localPriority==null){
                                                 throw new org.apache.axis2.databinding.ADBException("priority cannot be null!!");
                                            }
                                           localPriority.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","priority"),
                                               xmlWriter);
                                        } if (localTaskInitiatorTracker){
                                            if (localTaskInitiator==null){
                                                 throw new org.apache.axis2.databinding.ADBException("taskInitiator cannot be null!!");
                                            }
                                           localTaskInitiator.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","taskInitiator"),
                                               xmlWriter);
                                        } if (localTaskStakeholdersTracker){
                                            if (localTaskStakeholders==null){
                                                 throw new org.apache.axis2.databinding.ADBException("taskStakeholders cannot be null!!");
                                            }
                                           localTaskStakeholders.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","taskStakeholders"),
                                               xmlWriter);
                                        } if (localPotentialOwnersTracker){
                                            if (localPotentialOwners==null){
                                                 throw new org.apache.axis2.databinding.ADBException("potentialOwners cannot be null!!");
                                            }
                                           localPotentialOwners.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","potentialOwners"),
                                               xmlWriter);
                                        } if (localBusinessAdministratorsTracker){
                                            if (localBusinessAdministrators==null){
                                                 throw new org.apache.axis2.databinding.ADBException("businessAdministrators cannot be null!!");
                                            }
                                           localBusinessAdministrators.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","businessAdministrators"),
                                               xmlWriter);
                                        } if (localActualOwnerTracker){
                                            if (localActualOwner==null){
                                                 throw new org.apache.axis2.databinding.ADBException("actualOwner cannot be null!!");
                                            }
                                           localActualOwner.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","actualOwner"),
                                               xmlWriter);
                                        } if (localNotificationRecipientsTracker){
                                            if (localNotificationRecipients==null){
                                                 throw new org.apache.axis2.databinding.ADBException("notificationRecipients cannot be null!!");
                                            }
                                           localNotificationRecipients.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","notificationRecipients"),
                                               xmlWriter);
                                        }
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803";
                                    writeStartElement(null, namespace, "createdTime", xmlWriter);
                             

                                          if (localCreatedTime==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("createdTime cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCreatedTime));
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803";
                                    writeStartElement(null, namespace, "updatedTime", xmlWriter);
                             

                                          if (localUpdatedTime==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("updatedTime cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localUpdatedTime));
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                              if (localActivationTimeTracker){
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803";
                                    writeStartElement(null, namespace, "activationTime", xmlWriter);
                             

                                          if (localActivationTime==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("activationTime cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localActivationTime));
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localExpirationTimeTracker){
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803";
                                    writeStartElement(null, namespace, "expirationTime", xmlWriter);
                             

                                          if (localExpirationTime==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("expirationTime cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localExpirationTime));
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localIsSkipableTracker){
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803";
                                    writeStartElement(null, namespace, "isSkipable", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("isSkipable cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIsSkipable));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localHasPotentialOwnersTracker){
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803";
                                    writeStartElement(null, namespace, "hasPotentialOwners", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("hasPotentialOwners cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localHasPotentialOwners));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localStartByTimeExistsTracker){
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803";
                                    writeStartElement(null, namespace, "startByTimeExists", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("startByTimeExists cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localStartByTimeExists));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localCompleteByTimeExistsTracker){
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803";
                                    writeStartElement(null, namespace, "completeByTimeExists", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("completeByTimeExists cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCompleteByTimeExists));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localPresentationNameTracker){
                                            if (localPresentationName==null){
                                                 throw new org.apache.axis2.databinding.ADBException("presentationName cannot be null!!");
                                            }
                                           localPresentationName.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","presentationName"),
                                               xmlWriter);
                                        } if (localPresentationSubjectTracker){
                                            if (localPresentationSubject==null){
                                                 throw new org.apache.axis2.databinding.ADBException("presentationSubject cannot be null!!");
                                            }
                                           localPresentationSubject.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","presentationSubject"),
                                               xmlWriter);
                                        } if (localPresentationDescriptionTracker){
                                            if (localPresentationDescription==null){
                                                 throw new org.apache.axis2.databinding.ADBException("presentationDescription cannot be null!!");
                                            }
                                           localPresentationDescription.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","presentationDescription"),
                                               xmlWriter);
                                        }
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803";
                                    writeStartElement(null, namespace, "renderingMethodExists", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("renderingMethodExists cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localRenderingMethodExists));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                              if (localHasOutputTracker){
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803";
                                    writeStartElement(null, namespace, "hasOutput", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("hasOutput cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localHasOutput));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localHasFaultTracker){
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803";
                                    writeStartElement(null, namespace, "hasFault", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("hasFault cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localHasFault));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localHasAttachmentsTracker){
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803";
                                    writeStartElement(null, namespace, "hasAttachments", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("hasAttachments cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localHasAttachments));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localHasCommentsTracker){
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803";
                                    writeStartElement(null, namespace, "hasComments", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("hasComments cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localHasComments));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localEscalatedTracker){
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803";
                                    writeStartElement(null, namespace, "escalated", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("escalated cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localEscalated));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localOutcomeTracker){
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803";
                                    writeStartElement(null, namespace, "outcome", xmlWriter);
                             

                                          if (localOutcome==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("outcome cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localOutcome);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localParentTaskIdTracker){
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803";
                                    writeStartElement(null, namespace, "parentTaskId", xmlWriter);
                             

                                          if (localParentTaskId==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("parentTaskId cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localParentTaskId));
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localHasSubTasksTracker){
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803";
                                    writeStartElement(null, namespace, "hasSubTasks", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("hasSubTasks cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localHasSubTasks));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localNumberOfCommentsTracker){
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803";
                                    writeStartElement(null, namespace, "numberOfComments", xmlWriter);
                             
                                               if (localNumberOfComments==java.lang.Integer.MIN_VALUE) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("numberOfComments cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localNumberOfComments));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localNumberOfAttachmentsTracker){
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803";
                                    writeStartElement(null, namespace, "numberOfAttachments", xmlWriter);
                             
                                               if (localNumberOfAttachments==java.lang.Integer.MIN_VALUE) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("numberOfAttachments cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localNumberOfAttachments));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localPreviousStatusTracker){
                                            if (localPreviousStatus==null){
                                                 throw new org.apache.axis2.databinding.ADBException("previousStatus cannot be null!!");
                                            }
                                           localPreviousStatus.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","previousStatus"),
                                               xmlWriter);
                                        } if (localResponseServiceNameTracker){
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803";
                                    writeStartElement(null, namespace, "responseServiceName", xmlWriter);
                             

                                          if (localResponseServiceName==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("responseServiceName cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localResponseServiceName);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localResponseOperationNameTracker){
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803";
                                    writeStartElement(null, namespace, "responseOperationName", xmlWriter);
                             

                                          if (localResponseOperationName==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("responseOperationName cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localResponseOperationName);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localIsClaimableTracker){
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803";
                                    writeStartElement(null, namespace, "isClaimable", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("isClaimable cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIsClaimable));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localIsStartableTracker){
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803";
                                    writeStartElement(null, namespace, "isStartable", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("isStartable cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIsStartable));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localIsStopableTracker){
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803";
                                    writeStartElement(null, namespace, "isStopable", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("isStopable cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIsStopable));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localIsReleasableTracker){
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803";
                                    writeStartElement(null, namespace, "isReleasable", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("isReleasable cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIsReleasable));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localIsSuspendableTracker){
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803";
                                    writeStartElement(null, namespace, "isSuspendable", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("isSuspendable cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIsSuspendable));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localIsResumableTracker){
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803";
                                    writeStartElement(null, namespace, "isResumable", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("isResumable cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIsResumable));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localIsCompletableTracker){
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803";
                                    writeStartElement(null, namespace, "isCompletable", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("isCompletable cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIsCompletable));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localIsRemovableTracker){
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803";
                                    writeStartElement(null, namespace, "isRemovable", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("isRemovable cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIsRemovable));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localIsForwardableTracker){
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803";
                                    writeStartElement(null, namespace, "isForwardable", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("isForwardable cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIsForwardable));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localIsDelegatableTracker){
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803";
                                    writeStartElement(null, namespace, "isDelegatable", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("isDelegatable cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIsDelegatable));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             }
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803";
                                    writeStartElement(null, namespace, "packageName", xmlWriter);
                             

                                          if (localPackageName==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("packageName cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localPackageName);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                              if (localTenantIdTracker){
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803";
                                    writeStartElement(null, namespace, "tenantId", xmlWriter);
                             
                                               if (localTenantId==java.lang.Integer.MIN_VALUE) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("tenantId cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localTenantId));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localExtraElementTracker){
                            
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


        
                 java.util.ArrayList elementList = new java.util.ArrayList();
                 java.util.ArrayList attribList = new java.util.ArrayList();

                
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "id"));
                                 
                                        if (localId != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localId));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("id cannot be null!!");
                                        }
                                    
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "taskType"));
                                 
                                        if (localTaskType != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localTaskType));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("taskType cannot be null!!");
                                        }
                                    
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "name"));
                                 
                                        if (localName != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localName));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("name cannot be null!!");
                                        }
                                    
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "status"));
                            
                            
                                    if (localStatus==null){
                                         throw new org.apache.axis2.databinding.ADBException("status cannot be null!!");
                                    }
                                    elementList.add(localStatus);
                                 if (localPriorityTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "priority"));
                            
                            
                                    if (localPriority==null){
                                         throw new org.apache.axis2.databinding.ADBException("priority cannot be null!!");
                                    }
                                    elementList.add(localPriority);
                                } if (localTaskInitiatorTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "taskInitiator"));
                            
                            
                                    if (localTaskInitiator==null){
                                         throw new org.apache.axis2.databinding.ADBException("taskInitiator cannot be null!!");
                                    }
                                    elementList.add(localTaskInitiator);
                                } if (localTaskStakeholdersTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "taskStakeholders"));
                            
                            
                                    if (localTaskStakeholders==null){
                                         throw new org.apache.axis2.databinding.ADBException("taskStakeholders cannot be null!!");
                                    }
                                    elementList.add(localTaskStakeholders);
                                } if (localPotentialOwnersTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "potentialOwners"));
                            
                            
                                    if (localPotentialOwners==null){
                                         throw new org.apache.axis2.databinding.ADBException("potentialOwners cannot be null!!");
                                    }
                                    elementList.add(localPotentialOwners);
                                } if (localBusinessAdministratorsTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "businessAdministrators"));
                            
                            
                                    if (localBusinessAdministrators==null){
                                         throw new org.apache.axis2.databinding.ADBException("businessAdministrators cannot be null!!");
                                    }
                                    elementList.add(localBusinessAdministrators);
                                } if (localActualOwnerTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "actualOwner"));
                            
                            
                                    if (localActualOwner==null){
                                         throw new org.apache.axis2.databinding.ADBException("actualOwner cannot be null!!");
                                    }
                                    elementList.add(localActualOwner);
                                } if (localNotificationRecipientsTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "notificationRecipients"));
                            
                            
                                    if (localNotificationRecipients==null){
                                         throw new org.apache.axis2.databinding.ADBException("notificationRecipients cannot be null!!");
                                    }
                                    elementList.add(localNotificationRecipients);
                                }
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "createdTime"));
                                 
                                        if (localCreatedTime != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCreatedTime));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("createdTime cannot be null!!");
                                        }
                                    
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "updatedTime"));
                                 
                                        if (localUpdatedTime != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localUpdatedTime));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("updatedTime cannot be null!!");
                                        }
                                     if (localActivationTimeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "activationTime"));
                                 
                                        if (localActivationTime != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localActivationTime));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("activationTime cannot be null!!");
                                        }
                                    } if (localExpirationTimeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "expirationTime"));
                                 
                                        if (localExpirationTime != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localExpirationTime));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("expirationTime cannot be null!!");
                                        }
                                    } if (localIsSkipableTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "isSkipable"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIsSkipable));
                            } if (localHasPotentialOwnersTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "hasPotentialOwners"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localHasPotentialOwners));
                            } if (localStartByTimeExistsTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "startByTimeExists"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localStartByTimeExists));
                            } if (localCompleteByTimeExistsTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "completeByTimeExists"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCompleteByTimeExists));
                            } if (localPresentationNameTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "presentationName"));
                            
                            
                                    if (localPresentationName==null){
                                         throw new org.apache.axis2.databinding.ADBException("presentationName cannot be null!!");
                                    }
                                    elementList.add(localPresentationName);
                                } if (localPresentationSubjectTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "presentationSubject"));
                            
                            
                                    if (localPresentationSubject==null){
                                         throw new org.apache.axis2.databinding.ADBException("presentationSubject cannot be null!!");
                                    }
                                    elementList.add(localPresentationSubject);
                                } if (localPresentationDescriptionTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "presentationDescription"));
                            
                            
                                    if (localPresentationDescription==null){
                                         throw new org.apache.axis2.databinding.ADBException("presentationDescription cannot be null!!");
                                    }
                                    elementList.add(localPresentationDescription);
                                }
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "renderingMethodExists"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localRenderingMethodExists));
                             if (localHasOutputTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "hasOutput"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localHasOutput));
                            } if (localHasFaultTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "hasFault"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localHasFault));
                            } if (localHasAttachmentsTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "hasAttachments"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localHasAttachments));
                            } if (localHasCommentsTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "hasComments"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localHasComments));
                            } if (localEscalatedTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "escalated"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localEscalated));
                            } if (localOutcomeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "outcome"));
                                 
                                        if (localOutcome != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localOutcome));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("outcome cannot be null!!");
                                        }
                                    } if (localParentTaskIdTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "parentTaskId"));
                                 
                                        if (localParentTaskId != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localParentTaskId));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("parentTaskId cannot be null!!");
                                        }
                                    } if (localHasSubTasksTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "hasSubTasks"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localHasSubTasks));
                            } if (localNumberOfCommentsTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "numberOfComments"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localNumberOfComments));
                            } if (localNumberOfAttachmentsTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "numberOfAttachments"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localNumberOfAttachments));
                            } if (localPreviousStatusTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "previousStatus"));
                            
                            
                                    if (localPreviousStatus==null){
                                         throw new org.apache.axis2.databinding.ADBException("previousStatus cannot be null!!");
                                    }
                                    elementList.add(localPreviousStatus);
                                } if (localResponseServiceNameTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "responseServiceName"));
                                 
                                        if (localResponseServiceName != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localResponseServiceName));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("responseServiceName cannot be null!!");
                                        }
                                    } if (localResponseOperationNameTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "responseOperationName"));
                                 
                                        if (localResponseOperationName != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localResponseOperationName));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("responseOperationName cannot be null!!");
                                        }
                                    } if (localIsClaimableTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "isClaimable"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIsClaimable));
                            } if (localIsStartableTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "isStartable"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIsStartable));
                            } if (localIsStopableTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "isStopable"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIsStopable));
                            } if (localIsReleasableTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "isReleasable"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIsReleasable));
                            } if (localIsSuspendableTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "isSuspendable"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIsSuspendable));
                            } if (localIsResumableTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "isResumable"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIsResumable));
                            } if (localIsCompletableTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "isCompletable"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIsCompletable));
                            } if (localIsRemovableTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "isRemovable"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIsRemovable));
                            } if (localIsForwardableTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "isForwardable"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIsForwardable));
                            } if (localIsDelegatableTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "isDelegatable"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIsDelegatable));
                            }
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "packageName"));
                                 
                                        if (localPackageName != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPackageName));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("packageName cannot be null!!");
                                        }
                                     if (localTenantIdTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "tenantId"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localTenantId));
                            } if (localExtraElementTracker){
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
        public static TTaskAbstract parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            TTaskAbstract object =
                new TTaskAbstract();

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
                    
                            if (!"tTaskAbstract".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (TTaskAbstract)org.wso2.carbon.humantask.stub.api.ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }
                        

                  }
                

                }

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                
                    
                    reader.next();
                
                        java.util.ArrayList list49 = new java.util.ArrayList();
                    
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","id").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"id" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setId(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToAnyURI(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                                }
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","taskType").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"taskType" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setTaskType(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                                }
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","name").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"name" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                            int index = content.indexOf(":");
                                            if(index > 0){
                                                prefix = content.substring(0,index);
                                             } else {
                                                prefix = "";
                                             }
                                             namespaceuri = reader.getNamespaceURI(prefix);
                                             object.setName(
                                                  org.apache.axis2.databinding.utils.ConverterUtil.convertToQName(content,namespaceuri));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                                }
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","status").equals(reader.getName())){
                                
                                                object.setStatus(org.wso2.carbon.humantask.stub.types.TStatus.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                                }
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","priority").equals(reader.getName())){
                                
                                                object.setPriority(org.wso2.carbon.humantask.stub.types.TPriority.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","taskInitiator").equals(reader.getName())){
                                
                                                object.setTaskInitiator(org.wso2.carbon.humantask.stub.types.TUser.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","taskStakeholders").equals(reader.getName())){
                                
                                                object.setTaskStakeholders(org.wso2.carbon.humantask.stub.types.TOrganizationalEntity.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","potentialOwners").equals(reader.getName())){
                                
                                                object.setPotentialOwners(org.wso2.carbon.humantask.stub.types.TOrganizationalEntity.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","businessAdministrators").equals(reader.getName())){
                                
                                                object.setBusinessAdministrators(org.wso2.carbon.humantask.stub.types.TOrganizationalEntity.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","actualOwner").equals(reader.getName())){
                                
                                                object.setActualOwner(org.wso2.carbon.humantask.stub.types.TUser.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","notificationRecipients").equals(reader.getName())){
                                
                                                object.setNotificationRecipients(org.wso2.carbon.humantask.stub.types.TOrganizationalEntity.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","createdTime").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"createdTime" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setCreatedTime(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToDateTime(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                                }
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","updatedTime").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"updatedTime" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setUpdatedTime(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToDateTime(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                                }
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","activationTime").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"activationTime" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setActivationTime(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToDateTime(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","expirationTime").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"expirationTime" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setExpirationTime(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToDateTime(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","isSkipable").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"isSkipable" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setIsSkipable(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","hasPotentialOwners").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"hasPotentialOwners" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setHasPotentialOwners(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","startByTimeExists").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"startByTimeExists" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setStartByTimeExists(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","completeByTimeExists").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"completeByTimeExists" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setCompleteByTimeExists(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","presentationName").equals(reader.getName())){
                                
                                                object.setPresentationName(org.wso2.carbon.humantask.stub.types.TPresentationName.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","presentationSubject").equals(reader.getName())){
                                
                                                object.setPresentationSubject(org.wso2.carbon.humantask.stub.types.TPresentationSubject.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","presentationDescription").equals(reader.getName())){
                                
                                                object.setPresentationDescription(org.wso2.carbon.humantask.stub.types.TPresentationDescription.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","renderingMethodExists").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"renderingMethodExists" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setRenderingMethodExists(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                                }
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","hasOutput").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"hasOutput" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setHasOutput(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","hasFault").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"hasFault" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setHasFault(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","hasAttachments").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"hasAttachments" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setHasAttachments(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","hasComments").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"hasComments" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setHasComments(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","escalated").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"escalated" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setEscalated(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","outcome").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"outcome" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setOutcome(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","parentTaskId").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"parentTaskId" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setParentTaskId(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToAnyURI(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","hasSubTasks").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"hasSubTasks" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setHasSubTasks(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","numberOfComments").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"numberOfComments" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setNumberOfComments(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                               object.setNumberOfComments(java.lang.Integer.MIN_VALUE);
                                           
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","numberOfAttachments").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"numberOfAttachments" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setNumberOfAttachments(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                               object.setNumberOfAttachments(java.lang.Integer.MIN_VALUE);
                                           
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","previousStatus").equals(reader.getName())){
                                
                                                object.setPreviousStatus(org.wso2.carbon.humantask.stub.types.TStatus.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","responseServiceName").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"responseServiceName" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setResponseServiceName(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","responseOperationName").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"responseOperationName" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setResponseOperationName(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","isClaimable").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"isClaimable" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setIsClaimable(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","isStartable").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"isStartable" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setIsStartable(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","isStopable").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"isStopable" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setIsStopable(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","isReleasable").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"isReleasable" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setIsReleasable(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","isSuspendable").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"isSuspendable" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setIsSuspendable(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","isResumable").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"isResumable" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setIsResumable(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","isCompletable").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"isCompletable" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setIsCompletable(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","isRemovable").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"isRemovable" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setIsRemovable(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","isForwardable").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"isForwardable" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setIsForwardable(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","isDelegatable").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"isDelegatable" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setIsDelegatable(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","packageName").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"packageName" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setPackageName(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                                }
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","tenantId").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"tenantId" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setTenantId(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                               object.setTenantId(java.lang.Integer.MIN_VALUE);
                                           
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                   if (reader.isStartElement()){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                           boolean loopDone49=false;

                                             while (!loopDone49){
                                                 event = reader.getEventType();
                                                 if (javax.xml.stream.XMLStreamConstants.START_ELEMENT == event){

                                                      // We need to wrap the reader so that it produces a fake START_DOCUEMENT event
                                                      org.apache.axis2.databinding.utils.NamedStaxOMBuilder builder49
                                                         = new org.apache.axis2.databinding.utils.NamedStaxOMBuilder(
                                                              new org.apache.axis2.util.StreamWrapper(reader), reader.getName());

                                                       list49.add(builder49.getOMElement());
                                                        reader.next();
                                                        if (reader.isEndElement()) {
                                                            // we have two countinuos end elements
                                                           loopDone49 = true;
                                                        }

                                                 }else if (javax.xml.stream.XMLStreamConstants.END_ELEMENT == event){
                                                     loopDone49 = true;
                                                 }else{
                                                     reader.next();
                                                 }

                                             }

                                            
                                             object.setExtraElement((org.apache.axiom.om.OMElement[])
                                                 org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                     org.apache.axiom.om.OMElement.class,list49));
                                                
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
           
    