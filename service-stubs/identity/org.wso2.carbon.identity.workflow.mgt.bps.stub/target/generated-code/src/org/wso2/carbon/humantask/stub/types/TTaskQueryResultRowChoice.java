
/**
 * TTaskQueryResultRowChoice.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:46 UTC)
 */

            
                package org.wso2.carbon.humantask.stub.types;
            

            /**
            *  TTaskQueryResultRowChoice bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class TTaskQueryResultRowChoice
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = tTaskQueryResultRowChoice
                Namespace URI = http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803
                Namespace Prefix = ns1
                */
            
            /** Whenever a new property is set ensure all others are unset
             *  There can be only one choice and the last one wins
             */
            private void clearAllSettingTrackers() {
            
                   localIdTracker = false;
                
                   localTaskTypeTracker = false;
                
                   localNameTracker = false;
                
                   localStatusTracker = false;
                
                   localPriorityTracker = false;
                
                   localTaskInitiatorTracker = false;
                
                   localTaskStakeholdersTracker = false;
                
                   localPotentialOwnersTracker = false;
                
                   localBusinessAdministratorsTracker = false;
                
                   localActualOwnerTracker = false;
                
                   localNotificationRecipientsTracker = false;
                
                   localCreatedTimeTracker = false;
                
                   localCreatedByTracker = false;
                
                   localLastModifiedTimeTracker = false;
                
                   localLastModifiedByTracker = false;
                
                   localActivationTimeTracker = false;
                
                   localExpirationTimeTracker = false;
                
                   localIsSkipableTracker = false;
                
                   localHasPotentialOwnersTracker = false;
                
                   localStartByTimeTracker = false;
                
                   localCompleteByTimeTracker = false;
                
                   localPresentationNameTracker = false;
                
                   localPresentationSubjectTracker = false;
                
                   localRenderingMethodNameTracker = false;
                
                   localHasOutputTracker = false;
                
                   localHasFaultTracker = false;
                
                   localHasAttachmentsTracker = false;
                
                   localHasCommentsTracker = false;
                
                   localEscalatedTracker = false;
                
                   localParentTaskIdTracker = false;
                
                   localHasSubtasksTracker = false;
                
                   localSearchByTracker = false;
                
                   localOutcomeTracker = false;
                
                   localTaskOperationsTracker = false;
                
                   localExtraElementTracker = false;
                
            }
        

                        /**
                        * field for Id
                        */

                        
                                    protected org.apache.axis2.databinding.types.URI localId ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localIdTracker = false ;

                           public boolean isIdSpecified(){
                               return localIdTracker;
                           }

                           

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
                            
                                clearAllSettingTrackers();
                            localIdTracker = param != null;
                                   
                                            this.localId=param;
                                    

                               }
                            

                        /**
                        * field for TaskType
                        */

                        
                                    protected java.lang.String localTaskType ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localTaskTypeTracker = false ;

                           public boolean isTaskTypeSpecified(){
                               return localTaskTypeTracker;
                           }

                           

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
                            
                                clearAllSettingTrackers();
                            localTaskTypeTracker = param != null;
                                   
                                            this.localTaskType=param;
                                    

                               }
                            

                        /**
                        * field for Name
                        */

                        
                                    protected javax.xml.namespace.QName localName ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localNameTracker = false ;

                           public boolean isNameSpecified(){
                               return localNameTracker;
                           }

                           

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
                            
                                clearAllSettingTrackers();
                            localNameTracker = param != null;
                                   
                                            this.localName=param;
                                    

                               }
                            

                        /**
                        * field for Status
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TStatus localStatus ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localStatusTracker = false ;

                           public boolean isStatusSpecified(){
                               return localStatusTracker;
                           }

                           

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
                            
                                clearAllSettingTrackers();
                            localStatusTracker = param != null;
                                   
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
                            
                                clearAllSettingTrackers();
                            localPriorityTracker = param != null;
                                   
                                            this.localPriority=param;
                                    

                               }
                            

                        /**
                        * field for TaskInitiator
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TOrganizationalEntity localTaskInitiator ;
                                
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
                           * @return org.wso2.carbon.humantask.stub.types.TOrganizationalEntity
                           */
                           public  org.wso2.carbon.humantask.stub.types.TOrganizationalEntity getTaskInitiator(){
                               return localTaskInitiator;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param TaskInitiator
                               */
                               public void setTaskInitiator(org.wso2.carbon.humantask.stub.types.TOrganizationalEntity param){
                            
                                clearAllSettingTrackers();
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
                            
                                clearAllSettingTrackers();
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
                            
                                clearAllSettingTrackers();
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
                            
                                clearAllSettingTrackers();
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
                            
                                clearAllSettingTrackers();
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
                            
                                clearAllSettingTrackers();
                            localNotificationRecipientsTracker = param != null;
                                   
                                            this.localNotificationRecipients=param;
                                    

                               }
                            

                        /**
                        * field for CreatedTime
                        */

                        
                                    protected java.util.Calendar localCreatedTime ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localCreatedTimeTracker = false ;

                           public boolean isCreatedTimeSpecified(){
                               return localCreatedTimeTracker;
                           }

                           

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
                            
                                clearAllSettingTrackers();
                            localCreatedTimeTracker = param != null;
                                   
                                            this.localCreatedTime=param;
                                    

                               }
                            

                        /**
                        * field for CreatedBy
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TUser localCreatedBy ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localCreatedByTracker = false ;

                           public boolean isCreatedBySpecified(){
                               return localCreatedByTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TUser
                           */
                           public  org.wso2.carbon.humantask.stub.types.TUser getCreatedBy(){
                               return localCreatedBy;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param CreatedBy
                               */
                               public void setCreatedBy(org.wso2.carbon.humantask.stub.types.TUser param){
                            
                                clearAllSettingTrackers();
                            localCreatedByTracker = param != null;
                                   
                                            this.localCreatedBy=param;
                                    

                               }
                            

                        /**
                        * field for LastModifiedTime
                        */

                        
                                    protected java.util.Calendar localLastModifiedTime ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localLastModifiedTimeTracker = false ;

                           public boolean isLastModifiedTimeSpecified(){
                               return localLastModifiedTimeTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.util.Calendar
                           */
                           public  java.util.Calendar getLastModifiedTime(){
                               return localLastModifiedTime;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param LastModifiedTime
                               */
                               public void setLastModifiedTime(java.util.Calendar param){
                            
                                clearAllSettingTrackers();
                            localLastModifiedTimeTracker = param != null;
                                   
                                            this.localLastModifiedTime=param;
                                    

                               }
                            

                        /**
                        * field for LastModifiedBy
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TUser localLastModifiedBy ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localLastModifiedByTracker = false ;

                           public boolean isLastModifiedBySpecified(){
                               return localLastModifiedByTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TUser
                           */
                           public  org.wso2.carbon.humantask.stub.types.TUser getLastModifiedBy(){
                               return localLastModifiedBy;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param LastModifiedBy
                               */
                               public void setLastModifiedBy(org.wso2.carbon.humantask.stub.types.TUser param){
                            
                                clearAllSettingTrackers();
                            localLastModifiedByTracker = param != null;
                                   
                                            this.localLastModifiedBy=param;
                                    

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
                            
                                clearAllSettingTrackers();
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
                            
                                clearAllSettingTrackers();
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
                            
                                clearAllSettingTrackers();
                            
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
                            
                                clearAllSettingTrackers();
                            
                                       // setting primitive attribute tracker to true
                                       localHasPotentialOwnersTracker =
                                       true;
                                   
                                            this.localHasPotentialOwners=param;
                                    

                               }
                            

                        /**
                        * field for StartByTime
                        */

                        
                                    protected java.util.Calendar localStartByTime ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localStartByTimeTracker = false ;

                           public boolean isStartByTimeSpecified(){
                               return localStartByTimeTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.util.Calendar
                           */
                           public  java.util.Calendar getStartByTime(){
                               return localStartByTime;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param StartByTime
                               */
                               public void setStartByTime(java.util.Calendar param){
                            
                                clearAllSettingTrackers();
                            localStartByTimeTracker = param != null;
                                   
                                            this.localStartByTime=param;
                                    

                               }
                            

                        /**
                        * field for CompleteByTime
                        */

                        
                                    protected java.util.Calendar localCompleteByTime ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localCompleteByTimeTracker = false ;

                           public boolean isCompleteByTimeSpecified(){
                               return localCompleteByTimeTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.util.Calendar
                           */
                           public  java.util.Calendar getCompleteByTime(){
                               return localCompleteByTime;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param CompleteByTime
                               */
                               public void setCompleteByTime(java.util.Calendar param){
                            
                                clearAllSettingTrackers();
                            localCompleteByTimeTracker = param != null;
                                   
                                            this.localCompleteByTime=param;
                                    

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
                            
                                clearAllSettingTrackers();
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
                            
                                clearAllSettingTrackers();
                            localPresentationSubjectTracker = param != null;
                                   
                                            this.localPresentationSubject=param;
                                    

                               }
                            

                        /**
                        * field for RenderingMethodName
                        */

                        
                                    protected javax.xml.namespace.QName localRenderingMethodName ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localRenderingMethodNameTracker = false ;

                           public boolean isRenderingMethodNameSpecified(){
                               return localRenderingMethodNameTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return javax.xml.namespace.QName
                           */
                           public  javax.xml.namespace.QName getRenderingMethodName(){
                               return localRenderingMethodName;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param RenderingMethodName
                               */
                               public void setRenderingMethodName(javax.xml.namespace.QName param){
                            
                                clearAllSettingTrackers();
                            localRenderingMethodNameTracker = param != null;
                                   
                                            this.localRenderingMethodName=param;
                                    

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
                            
                                clearAllSettingTrackers();
                            
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
                            
                                clearAllSettingTrackers();
                            
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
                            
                                clearAllSettingTrackers();
                            
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
                            
                                clearAllSettingTrackers();
                            
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
                            
                                clearAllSettingTrackers();
                            
                                       // setting primitive attribute tracker to true
                                       localEscalatedTracker =
                                       true;
                                   
                                            this.localEscalated=param;
                                    

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
                            
                                clearAllSettingTrackers();
                            localParentTaskIdTracker = param != null;
                                   
                                            this.localParentTaskId=param;
                                    

                               }
                            

                        /**
                        * field for HasSubtasks
                        */

                        
                                    protected boolean localHasSubtasks ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localHasSubtasksTracker = false ;

                           public boolean isHasSubtasksSpecified(){
                               return localHasSubtasksTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getHasSubtasks(){
                               return localHasSubtasks;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param HasSubtasks
                               */
                               public void setHasSubtasks(boolean param){
                            
                                clearAllSettingTrackers();
                            
                                       // setting primitive attribute tracker to true
                                       localHasSubtasksTracker =
                                       true;
                                   
                                            this.localHasSubtasks=param;
                                    

                               }
                            

                        /**
                        * field for SearchBy
                        */

                        
                                    protected java.lang.String localSearchBy ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localSearchByTracker = false ;

                           public boolean isSearchBySpecified(){
                               return localSearchByTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getSearchBy(){
                               return localSearchBy;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param SearchBy
                               */
                               public void setSearchBy(java.lang.String param){
                            
                                clearAllSettingTrackers();
                            localSearchByTracker = param != null;
                                   
                                            this.localSearchBy=param;
                                    

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
                            
                                clearAllSettingTrackers();
                            localOutcomeTracker = param != null;
                                   
                                            this.localOutcome=param;
                                    

                               }
                            

                        /**
                        * field for TaskOperations
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperations localTaskOperations ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localTaskOperationsTracker = false ;

                           public boolean isTaskOperationsSpecified(){
                               return localTaskOperationsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperations
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperations getTaskOperations(){
                               return localTaskOperations;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param TaskOperations
                               */
                               public void setTaskOperations(org.wso2.carbon.humantask.stub.types.TTaskOperations param){
                            
                                clearAllSettingTrackers();
                            localTaskOperationsTracker = param != null;
                                   
                                            this.localTaskOperations=param;
                                    

                               }
                            

                        /**
                        * field for ExtraElement
                        */

                        
                                    protected org.apache.axiom.om.OMElement localExtraElement ;
                                
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
                           * @return org.apache.axiom.om.OMElement
                           */
                           public  org.apache.axiom.om.OMElement getExtraElement(){
                               return localExtraElement;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ExtraElement
                               */
                               public void setExtraElement(org.apache.axiom.om.OMElement param){
                            
                                clearAllSettingTrackers();
                            localExtraElementTracker = param != null;
                                   
                                            this.localExtraElement=param;
                                    

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
                
                  if (serializeType){
               

                   java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803");
                   if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           namespacePrefix+":tTaskQueryResultRowChoice",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "tTaskQueryResultRowChoice",
                           xmlWriter);
                   }

               
                   }
                if (localIdTracker){
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803";
                                    writeStartElement(null, namespace, "id", xmlWriter);
                             

                                          if (localId==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("id cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localId));
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localTaskTypeTracker){
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803";
                                    writeStartElement(null, namespace, "taskType", xmlWriter);
                             

                                          if (localTaskType==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("taskType cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localTaskType);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localNameTracker){
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803";
                                    writeStartElement(null, namespace, "name", xmlWriter);
                             

                                          if (localName==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("name cannot be null!!");
                                                  
                                          }else{

                                        
                                                writeQName(localName,xmlWriter);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localStatusTracker){
                                            if (localStatus==null){
                                                 throw new org.apache.axis2.databinding.ADBException("status cannot be null!!");
                                            }
                                           localStatus.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","status"),
                                               xmlWriter);
                                        } if (localPriorityTracker){
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
                                        } if (localCreatedTimeTracker){
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803";
                                    writeStartElement(null, namespace, "createdTime", xmlWriter);
                             

                                          if (localCreatedTime==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("createdTime cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCreatedTime));
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localCreatedByTracker){
                                            if (localCreatedBy==null){
                                                 throw new org.apache.axis2.databinding.ADBException("createdBy cannot be null!!");
                                            }
                                           localCreatedBy.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","createdBy"),
                                               xmlWriter);
                                        } if (localLastModifiedTimeTracker){
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803";
                                    writeStartElement(null, namespace, "lastModifiedTime", xmlWriter);
                             

                                          if (localLastModifiedTime==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("lastModifiedTime cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localLastModifiedTime));
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localLastModifiedByTracker){
                                            if (localLastModifiedBy==null){
                                                 throw new org.apache.axis2.databinding.ADBException("lastModifiedBy cannot be null!!");
                                            }
                                           localLastModifiedBy.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","lastModifiedBy"),
                                               xmlWriter);
                                        } if (localActivationTimeTracker){
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
                             } if (localStartByTimeTracker){
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803";
                                    writeStartElement(null, namespace, "startByTime", xmlWriter);
                             

                                          if (localStartByTime==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("startByTime cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localStartByTime));
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localCompleteByTimeTracker){
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803";
                                    writeStartElement(null, namespace, "completeByTime", xmlWriter);
                             

                                          if (localCompleteByTime==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("completeByTime cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCompleteByTime));
                                            
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
                                        } if (localRenderingMethodNameTracker){
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803";
                                    writeStartElement(null, namespace, "renderingMethodName", xmlWriter);
                             

                                          if (localRenderingMethodName==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("renderingMethodName cannot be null!!");
                                                  
                                          }else{

                                        
                                                writeQName(localRenderingMethodName,xmlWriter);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localHasOutputTracker){
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
                             } if (localHasSubtasksTracker){
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803";
                                    writeStartElement(null, namespace, "hasSubtasks", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("hasSubtasks cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localHasSubtasks));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localSearchByTracker){
                                    namespace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803";
                                    writeStartElement(null, namespace, "searchBy", xmlWriter);
                             

                                          if (localSearchBy==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("searchBy cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localSearchBy);
                                            
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
                             } if (localTaskOperationsTracker){
                                            if (localTaskOperations==null){
                                                 throw new org.apache.axis2.databinding.ADBException("taskOperations cannot be null!!");
                                            }
                                           localTaskOperations.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","taskOperations"),
                                               xmlWriter);
                                        } if (localExtraElementTracker){
                            
                            if (localExtraElement != null) {
                                localExtraElement.serialize(xmlWriter);
                            } else {
                               throw new org.apache.axis2.databinding.ADBException("extraElement cannot be null!!");
                            }
                        }

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

                 if (localIdTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "id"));
                                 
                                        if (localId != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localId));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("id cannot be null!!");
                                        }
                                    } if (localTaskTypeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "taskType"));
                                 
                                        if (localTaskType != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localTaskType));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("taskType cannot be null!!");
                                        }
                                    } if (localNameTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "name"));
                                 
                                        if (localName != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localName));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("name cannot be null!!");
                                        }
                                    } if (localStatusTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "status"));
                            
                            
                                    if (localStatus==null){
                                         throw new org.apache.axis2.databinding.ADBException("status cannot be null!!");
                                    }
                                    elementList.add(localStatus);
                                } if (localPriorityTracker){
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
                                } if (localCreatedTimeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "createdTime"));
                                 
                                        if (localCreatedTime != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCreatedTime));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("createdTime cannot be null!!");
                                        }
                                    } if (localCreatedByTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "createdBy"));
                            
                            
                                    if (localCreatedBy==null){
                                         throw new org.apache.axis2.databinding.ADBException("createdBy cannot be null!!");
                                    }
                                    elementList.add(localCreatedBy);
                                } if (localLastModifiedTimeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "lastModifiedTime"));
                                 
                                        if (localLastModifiedTime != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localLastModifiedTime));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("lastModifiedTime cannot be null!!");
                                        }
                                    } if (localLastModifiedByTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "lastModifiedBy"));
                            
                            
                                    if (localLastModifiedBy==null){
                                         throw new org.apache.axis2.databinding.ADBException("lastModifiedBy cannot be null!!");
                                    }
                                    elementList.add(localLastModifiedBy);
                                } if (localActivationTimeTracker){
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
                            } if (localStartByTimeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "startByTime"));
                                 
                                        if (localStartByTime != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localStartByTime));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("startByTime cannot be null!!");
                                        }
                                    } if (localCompleteByTimeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "completeByTime"));
                                 
                                        if (localCompleteByTime != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCompleteByTime));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("completeByTime cannot be null!!");
                                        }
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
                                } if (localRenderingMethodNameTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "renderingMethodName"));
                                 
                                        if (localRenderingMethodName != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localRenderingMethodName));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("renderingMethodName cannot be null!!");
                                        }
                                    } if (localHasOutputTracker){
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
                            } if (localParentTaskIdTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "parentTaskId"));
                                 
                                        if (localParentTaskId != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localParentTaskId));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("parentTaskId cannot be null!!");
                                        }
                                    } if (localHasSubtasksTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "hasSubtasks"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localHasSubtasks));
                            } if (localSearchByTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "searchBy"));
                                 
                                        if (localSearchBy != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSearchBy));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("searchBy cannot be null!!");
                                        }
                                    } if (localOutcomeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "outcome"));
                                 
                                        if (localOutcome != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localOutcome));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("outcome cannot be null!!");
                                        }
                                    } if (localTaskOperationsTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "taskOperations"));
                            
                            
                                    if (localTaskOperations==null){
                                         throw new org.apache.axis2.databinding.ADBException("taskOperations cannot be null!!");
                                    }
                                    elementList.add(localTaskOperations);
                                } if (localExtraElementTracker){
                            if (localExtraElement != null){
                                elementList.add(org.apache.axis2.databinding.utils.Constants.OM_ELEMENT_KEY);
                                elementList.add(localExtraElement);
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
        public static TTaskQueryResultRowChoice parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            TTaskQueryResultRowChoice object =
                new TTaskQueryResultRowChoice();

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
                                
                                        else
                                    
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
                                
                                        else
                                    
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
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","status").equals(reader.getName())){
                                
                                                object.setStatus(org.wso2.carbon.humantask.stub.types.TStatus.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","priority").equals(reader.getName())){
                                
                                                object.setPriority(org.wso2.carbon.humantask.stub.types.TPriority.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","taskInitiator").equals(reader.getName())){
                                
                                                object.setTaskInitiator(org.wso2.carbon.humantask.stub.types.TOrganizationalEntity.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","taskStakeholders").equals(reader.getName())){
                                
                                                object.setTaskStakeholders(org.wso2.carbon.humantask.stub.types.TOrganizationalEntity.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","potentialOwners").equals(reader.getName())){
                                
                                                object.setPotentialOwners(org.wso2.carbon.humantask.stub.types.TOrganizationalEntity.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","businessAdministrators").equals(reader.getName())){
                                
                                                object.setBusinessAdministrators(org.wso2.carbon.humantask.stub.types.TOrganizationalEntity.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","actualOwner").equals(reader.getName())){
                                
                                                object.setActualOwner(org.wso2.carbon.humantask.stub.types.TUser.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","notificationRecipients").equals(reader.getName())){
                                
                                                object.setNotificationRecipients(org.wso2.carbon.humantask.stub.types.TOrganizationalEntity.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
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
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","createdBy").equals(reader.getName())){
                                
                                                object.setCreatedBy(org.wso2.carbon.humantask.stub.types.TUser.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","lastModifiedTime").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"lastModifiedTime" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setLastModifiedTime(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToDateTime(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","lastModifiedBy").equals(reader.getName())){
                                
                                                object.setLastModifiedBy(org.wso2.carbon.humantask.stub.types.TUser.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
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
                                
                                        else
                                    
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
                                
                                        else
                                    
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
                                
                                        else
                                    
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
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","startByTime").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"startByTime" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setStartByTime(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToDateTime(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","completeByTime").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"completeByTime" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setCompleteByTime(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToDateTime(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","presentationName").equals(reader.getName())){
                                
                                                object.setPresentationName(org.wso2.carbon.humantask.stub.types.TPresentationName.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","presentationSubject").equals(reader.getName())){
                                
                                                object.setPresentationSubject(org.wso2.carbon.humantask.stub.types.TPresentationSubject.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","renderingMethodName").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"renderingMethodName" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                            int index = content.indexOf(":");
                                            if(index > 0){
                                                prefix = content.substring(0,index);
                                             } else {
                                                prefix = "";
                                             }
                                             namespaceuri = reader.getNamespaceURI(prefix);
                                             object.setRenderingMethodName(
                                                  org.apache.axis2.databinding.utils.ConverterUtil.convertToQName(content,namespaceuri));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
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
                                
                                        else
                                    
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
                                
                                        else
                                    
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
                                
                                        else
                                    
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
                                
                                        else
                                    
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
                                
                                        else
                                    
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
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","hasSubtasks").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"hasSubtasks" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setHasSubtasks(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","searchBy").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"searchBy" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setSearchBy(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
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
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","taskOperations").equals(reader.getName())){
                                
                                                object.setTaskOperations(org.wso2.carbon.humantask.stub.types.TTaskOperations.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                   if (reader.isStartElement()){
                                
                                    
                                     
                                     //use the QName from the parser as the name for the builder
                                     javax.xml.namespace.QName startQname35 = reader.getName();

                                     // We need to wrap the reader so that it produces a fake START_DOCUMENT event
                                     // this is needed by the builder classes
                                     org.apache.axis2.databinding.utils.NamedStaxOMBuilder builder35 =
                                         new org.apache.axis2.databinding.utils.NamedStaxOMBuilder(
                                             new org.apache.axis2.util.StreamWrapper(reader),startQname35);
                                     object.setExtraElement(builder35.getOMElement());
                                       
                                         reader.next();
                                     
                              }  // End of if for expected property start element
                                



            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class

        

        }
           
    