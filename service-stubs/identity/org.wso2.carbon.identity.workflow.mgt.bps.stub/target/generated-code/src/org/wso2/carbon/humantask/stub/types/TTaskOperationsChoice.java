
/**
 * TTaskOperationsChoice.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:46 UTC)
 */

            
                package org.wso2.carbon.humantask.stub.types;
            

            /**
            *  TTaskOperationsChoice bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class TTaskOperationsChoice
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = tTaskOperationsChoice
                Namespace URI = http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803
                Namespace Prefix = ns1
                */
            
            /** Whenever a new property is set ensure all others are unset
             *  There can be only one choice and the last one wins
             */
            private void clearAllSettingTrackers() {
            
                   localActivateTracker = false;
                
                   localAddAttachmentTracker = false;
                
                   localAddCommentTracker = false;
                
                   localClaimTracker = false;
                
                   localCompleteTracker = false;
                
                   localDelegateTracker = false;
                
                   localDeleteAttachmentTracker = false;
                
                   localDeleteCommentTracker = false;
                
                   localDeleteFaultTracker = false;
                
                   localDeleteOutputTracker = false;
                
                   localFailTracker = false;
                
                   localForwardTracker = false;
                
                   localGetAttachmentTracker = false;
                
                   localGetAttachmentInfosTracker = false;
                
                   localGetCommentsTracker = false;
                
                   localGetFaultTracker = false;
                
                   localGetInputTracker = false;
                
                   localGetOutcomeTracker = false;
                
                   localGetOutputTracker = false;
                
                   localGetParentTaskTracker = false;
                
                   localGetParentTaskIdentifierTracker = false;
                
                   localGetRenderingTracker = false;
                
                   localGetRenderingTypesTracker = false;
                
                   localGetSubtaskIdentifiersTracker = false;
                
                   localGetSubtasksTracker = false;
                
                   localGetTaskDescriptionTracker = false;
                
                   localGetTaskDetailsTracker = false;
                
                   localGetTaskHistoryTracker = false;
                
                   localGetTaskInstanceDataTracker = false;
                
                   localHasSubtasksTracker = false;
                
                   localInstantiateSubtaskTracker = false;
                
                   localIsSubtaskTracker = false;
                
                   localNominateTracker = false;
                
                   localReleaseTracker = false;
                
                   localRemoveTracker = false;
                
                   localResumeTracker = false;
                
                   localSetFaultTracker = false;
                
                   localSetGenericHumanRoleTracker = false;
                
                   localSetOutputTracker = false;
                
                   localSetPriorityTracker = false;
                
                   localSetTaskCompletionDeadlineExpressionTracker = false;
                
                   localSetTaskCompletionDurationExpressionTracker = false;
                
                   localSetTaskStartDeadlineExpressionTracker = false;
                
                   localSetTaskStartDurationExpressionTracker = false;
                
                   localSkipTracker = false;
                
                   localStartTracker = false;
                
                   localStopTracker = false;
                
                   localSuspendTracker = false;
                
                   localSuspendUntilTracker = false;
                
                   localUpdateCommentTracker = false;
                
                   localExtraElementTracker = false;
                
            }
        

                        /**
                        * field for Activate
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localActivate ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localActivateTracker = false ;

                           public boolean isActivateSpecified(){
                               return localActivateTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getActivate(){
                               return localActivate;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Activate
                               */
                               public void setActivate(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localActivateTracker = param != null;
                                   
                                            this.localActivate=param;
                                    

                               }
                            

                        /**
                        * field for AddAttachment
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localAddAttachment ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAddAttachmentTracker = false ;

                           public boolean isAddAttachmentSpecified(){
                               return localAddAttachmentTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getAddAttachment(){
                               return localAddAttachment;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param AddAttachment
                               */
                               public void setAddAttachment(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localAddAttachmentTracker = param != null;
                                   
                                            this.localAddAttachment=param;
                                    

                               }
                            

                        /**
                        * field for AddComment
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localAddComment ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAddCommentTracker = false ;

                           public boolean isAddCommentSpecified(){
                               return localAddCommentTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getAddComment(){
                               return localAddComment;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param AddComment
                               */
                               public void setAddComment(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localAddCommentTracker = param != null;
                                   
                                            this.localAddComment=param;
                                    

                               }
                            

                        /**
                        * field for Claim
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localClaim ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localClaimTracker = false ;

                           public boolean isClaimSpecified(){
                               return localClaimTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getClaim(){
                               return localClaim;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Claim
                               */
                               public void setClaim(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localClaimTracker = param != null;
                                   
                                            this.localClaim=param;
                                    

                               }
                            

                        /**
                        * field for Complete
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localComplete ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localCompleteTracker = false ;

                           public boolean isCompleteSpecified(){
                               return localCompleteTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getComplete(){
                               return localComplete;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Complete
                               */
                               public void setComplete(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localCompleteTracker = param != null;
                                   
                                            this.localComplete=param;
                                    

                               }
                            

                        /**
                        * field for Delegate
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localDelegate ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localDelegateTracker = false ;

                           public boolean isDelegateSpecified(){
                               return localDelegateTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getDelegate(){
                               return localDelegate;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Delegate
                               */
                               public void setDelegate(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localDelegateTracker = param != null;
                                   
                                            this.localDelegate=param;
                                    

                               }
                            

                        /**
                        * field for DeleteAttachment
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localDeleteAttachment ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localDeleteAttachmentTracker = false ;

                           public boolean isDeleteAttachmentSpecified(){
                               return localDeleteAttachmentTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getDeleteAttachment(){
                               return localDeleteAttachment;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param DeleteAttachment
                               */
                               public void setDeleteAttachment(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localDeleteAttachmentTracker = param != null;
                                   
                                            this.localDeleteAttachment=param;
                                    

                               }
                            

                        /**
                        * field for DeleteComment
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localDeleteComment ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localDeleteCommentTracker = false ;

                           public boolean isDeleteCommentSpecified(){
                               return localDeleteCommentTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getDeleteComment(){
                               return localDeleteComment;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param DeleteComment
                               */
                               public void setDeleteComment(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localDeleteCommentTracker = param != null;
                                   
                                            this.localDeleteComment=param;
                                    

                               }
                            

                        /**
                        * field for DeleteFault
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localDeleteFault ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localDeleteFaultTracker = false ;

                           public boolean isDeleteFaultSpecified(){
                               return localDeleteFaultTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getDeleteFault(){
                               return localDeleteFault;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param DeleteFault
                               */
                               public void setDeleteFault(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localDeleteFaultTracker = param != null;
                                   
                                            this.localDeleteFault=param;
                                    

                               }
                            

                        /**
                        * field for DeleteOutput
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localDeleteOutput ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localDeleteOutputTracker = false ;

                           public boolean isDeleteOutputSpecified(){
                               return localDeleteOutputTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getDeleteOutput(){
                               return localDeleteOutput;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param DeleteOutput
                               */
                               public void setDeleteOutput(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localDeleteOutputTracker = param != null;
                                   
                                            this.localDeleteOutput=param;
                                    

                               }
                            

                        /**
                        * field for Fail
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localFail ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localFailTracker = false ;

                           public boolean isFailSpecified(){
                               return localFailTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getFail(){
                               return localFail;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Fail
                               */
                               public void setFail(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localFailTracker = param != null;
                                   
                                            this.localFail=param;
                                    

                               }
                            

                        /**
                        * field for Forward
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localForward ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localForwardTracker = false ;

                           public boolean isForwardSpecified(){
                               return localForwardTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getForward(){
                               return localForward;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Forward
                               */
                               public void setForward(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localForwardTracker = param != null;
                                   
                                            this.localForward=param;
                                    

                               }
                            

                        /**
                        * field for GetAttachment
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localGetAttachment ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localGetAttachmentTracker = false ;

                           public boolean isGetAttachmentSpecified(){
                               return localGetAttachmentTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getGetAttachment(){
                               return localGetAttachment;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param GetAttachment
                               */
                               public void setGetAttachment(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localGetAttachmentTracker = param != null;
                                   
                                            this.localGetAttachment=param;
                                    

                               }
                            

                        /**
                        * field for GetAttachmentInfos
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localGetAttachmentInfos ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localGetAttachmentInfosTracker = false ;

                           public boolean isGetAttachmentInfosSpecified(){
                               return localGetAttachmentInfosTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getGetAttachmentInfos(){
                               return localGetAttachmentInfos;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param GetAttachmentInfos
                               */
                               public void setGetAttachmentInfos(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localGetAttachmentInfosTracker = param != null;
                                   
                                            this.localGetAttachmentInfos=param;
                                    

                               }
                            

                        /**
                        * field for GetComments
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localGetComments ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localGetCommentsTracker = false ;

                           public boolean isGetCommentsSpecified(){
                               return localGetCommentsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getGetComments(){
                               return localGetComments;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param GetComments
                               */
                               public void setGetComments(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localGetCommentsTracker = param != null;
                                   
                                            this.localGetComments=param;
                                    

                               }
                            

                        /**
                        * field for GetFault
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localGetFault ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localGetFaultTracker = false ;

                           public boolean isGetFaultSpecified(){
                               return localGetFaultTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getGetFault(){
                               return localGetFault;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param GetFault
                               */
                               public void setGetFault(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localGetFaultTracker = param != null;
                                   
                                            this.localGetFault=param;
                                    

                               }
                            

                        /**
                        * field for GetInput
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localGetInput ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localGetInputTracker = false ;

                           public boolean isGetInputSpecified(){
                               return localGetInputTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getGetInput(){
                               return localGetInput;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param GetInput
                               */
                               public void setGetInput(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localGetInputTracker = param != null;
                                   
                                            this.localGetInput=param;
                                    

                               }
                            

                        /**
                        * field for GetOutcome
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localGetOutcome ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localGetOutcomeTracker = false ;

                           public boolean isGetOutcomeSpecified(){
                               return localGetOutcomeTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getGetOutcome(){
                               return localGetOutcome;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param GetOutcome
                               */
                               public void setGetOutcome(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localGetOutcomeTracker = param != null;
                                   
                                            this.localGetOutcome=param;
                                    

                               }
                            

                        /**
                        * field for GetOutput
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localGetOutput ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localGetOutputTracker = false ;

                           public boolean isGetOutputSpecified(){
                               return localGetOutputTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getGetOutput(){
                               return localGetOutput;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param GetOutput
                               */
                               public void setGetOutput(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localGetOutputTracker = param != null;
                                   
                                            this.localGetOutput=param;
                                    

                               }
                            

                        /**
                        * field for GetParentTask
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localGetParentTask ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localGetParentTaskTracker = false ;

                           public boolean isGetParentTaskSpecified(){
                               return localGetParentTaskTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getGetParentTask(){
                               return localGetParentTask;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param GetParentTask
                               */
                               public void setGetParentTask(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localGetParentTaskTracker = param != null;
                                   
                                            this.localGetParentTask=param;
                                    

                               }
                            

                        /**
                        * field for GetParentTaskIdentifier
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localGetParentTaskIdentifier ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localGetParentTaskIdentifierTracker = false ;

                           public boolean isGetParentTaskIdentifierSpecified(){
                               return localGetParentTaskIdentifierTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getGetParentTaskIdentifier(){
                               return localGetParentTaskIdentifier;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param GetParentTaskIdentifier
                               */
                               public void setGetParentTaskIdentifier(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localGetParentTaskIdentifierTracker = param != null;
                                   
                                            this.localGetParentTaskIdentifier=param;
                                    

                               }
                            

                        /**
                        * field for GetRendering
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localGetRendering ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localGetRenderingTracker = false ;

                           public boolean isGetRenderingSpecified(){
                               return localGetRenderingTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getGetRendering(){
                               return localGetRendering;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param GetRendering
                               */
                               public void setGetRendering(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localGetRenderingTracker = param != null;
                                   
                                            this.localGetRendering=param;
                                    

                               }
                            

                        /**
                        * field for GetRenderingTypes
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localGetRenderingTypes ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localGetRenderingTypesTracker = false ;

                           public boolean isGetRenderingTypesSpecified(){
                               return localGetRenderingTypesTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getGetRenderingTypes(){
                               return localGetRenderingTypes;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param GetRenderingTypes
                               */
                               public void setGetRenderingTypes(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localGetRenderingTypesTracker = param != null;
                                   
                                            this.localGetRenderingTypes=param;
                                    

                               }
                            

                        /**
                        * field for GetSubtaskIdentifiers
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localGetSubtaskIdentifiers ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localGetSubtaskIdentifiersTracker = false ;

                           public boolean isGetSubtaskIdentifiersSpecified(){
                               return localGetSubtaskIdentifiersTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getGetSubtaskIdentifiers(){
                               return localGetSubtaskIdentifiers;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param GetSubtaskIdentifiers
                               */
                               public void setGetSubtaskIdentifiers(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localGetSubtaskIdentifiersTracker = param != null;
                                   
                                            this.localGetSubtaskIdentifiers=param;
                                    

                               }
                            

                        /**
                        * field for GetSubtasks
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localGetSubtasks ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localGetSubtasksTracker = false ;

                           public boolean isGetSubtasksSpecified(){
                               return localGetSubtasksTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getGetSubtasks(){
                               return localGetSubtasks;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param GetSubtasks
                               */
                               public void setGetSubtasks(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localGetSubtasksTracker = param != null;
                                   
                                            this.localGetSubtasks=param;
                                    

                               }
                            

                        /**
                        * field for GetTaskDescription
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localGetTaskDescription ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localGetTaskDescriptionTracker = false ;

                           public boolean isGetTaskDescriptionSpecified(){
                               return localGetTaskDescriptionTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getGetTaskDescription(){
                               return localGetTaskDescription;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param GetTaskDescription
                               */
                               public void setGetTaskDescription(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localGetTaskDescriptionTracker = param != null;
                                   
                                            this.localGetTaskDescription=param;
                                    

                               }
                            

                        /**
                        * field for GetTaskDetails
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localGetTaskDetails ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localGetTaskDetailsTracker = false ;

                           public boolean isGetTaskDetailsSpecified(){
                               return localGetTaskDetailsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getGetTaskDetails(){
                               return localGetTaskDetails;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param GetTaskDetails
                               */
                               public void setGetTaskDetails(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localGetTaskDetailsTracker = param != null;
                                   
                                            this.localGetTaskDetails=param;
                                    

                               }
                            

                        /**
                        * field for GetTaskHistory
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localGetTaskHistory ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localGetTaskHistoryTracker = false ;

                           public boolean isGetTaskHistorySpecified(){
                               return localGetTaskHistoryTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getGetTaskHistory(){
                               return localGetTaskHistory;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param GetTaskHistory
                               */
                               public void setGetTaskHistory(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localGetTaskHistoryTracker = param != null;
                                   
                                            this.localGetTaskHistory=param;
                                    

                               }
                            

                        /**
                        * field for GetTaskInstanceData
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localGetTaskInstanceData ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localGetTaskInstanceDataTracker = false ;

                           public boolean isGetTaskInstanceDataSpecified(){
                               return localGetTaskInstanceDataTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getGetTaskInstanceData(){
                               return localGetTaskInstanceData;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param GetTaskInstanceData
                               */
                               public void setGetTaskInstanceData(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localGetTaskInstanceDataTracker = param != null;
                                   
                                            this.localGetTaskInstanceData=param;
                                    

                               }
                            

                        /**
                        * field for HasSubtasks
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localHasSubtasks ;
                                
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
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getHasSubtasks(){
                               return localHasSubtasks;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param HasSubtasks
                               */
                               public void setHasSubtasks(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localHasSubtasksTracker = param != null;
                                   
                                            this.localHasSubtasks=param;
                                    

                               }
                            

                        /**
                        * field for InstantiateSubtask
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localInstantiateSubtask ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localInstantiateSubtaskTracker = false ;

                           public boolean isInstantiateSubtaskSpecified(){
                               return localInstantiateSubtaskTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getInstantiateSubtask(){
                               return localInstantiateSubtask;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param InstantiateSubtask
                               */
                               public void setInstantiateSubtask(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localInstantiateSubtaskTracker = param != null;
                                   
                                            this.localInstantiateSubtask=param;
                                    

                               }
                            

                        /**
                        * field for IsSubtask
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localIsSubtask ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localIsSubtaskTracker = false ;

                           public boolean isIsSubtaskSpecified(){
                               return localIsSubtaskTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getIsSubtask(){
                               return localIsSubtask;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param IsSubtask
                               */
                               public void setIsSubtask(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localIsSubtaskTracker = param != null;
                                   
                                            this.localIsSubtask=param;
                                    

                               }
                            

                        /**
                        * field for Nominate
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localNominate ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localNominateTracker = false ;

                           public boolean isNominateSpecified(){
                               return localNominateTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getNominate(){
                               return localNominate;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Nominate
                               */
                               public void setNominate(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localNominateTracker = param != null;
                                   
                                            this.localNominate=param;
                                    

                               }
                            

                        /**
                        * field for Release
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localRelease ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localReleaseTracker = false ;

                           public boolean isReleaseSpecified(){
                               return localReleaseTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getRelease(){
                               return localRelease;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Release
                               */
                               public void setRelease(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localReleaseTracker = param != null;
                                   
                                            this.localRelease=param;
                                    

                               }
                            

                        /**
                        * field for Remove
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localRemove ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localRemoveTracker = false ;

                           public boolean isRemoveSpecified(){
                               return localRemoveTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getRemove(){
                               return localRemove;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Remove
                               */
                               public void setRemove(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localRemoveTracker = param != null;
                                   
                                            this.localRemove=param;
                                    

                               }
                            

                        /**
                        * field for Resume
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localResume ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localResumeTracker = false ;

                           public boolean isResumeSpecified(){
                               return localResumeTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getResume(){
                               return localResume;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Resume
                               */
                               public void setResume(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localResumeTracker = param != null;
                                   
                                            this.localResume=param;
                                    

                               }
                            

                        /**
                        * field for SetFault
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localSetFault ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localSetFaultTracker = false ;

                           public boolean isSetFaultSpecified(){
                               return localSetFaultTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getSetFault(){
                               return localSetFault;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param SetFault
                               */
                               public void setSetFault(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localSetFaultTracker = param != null;
                                   
                                            this.localSetFault=param;
                                    

                               }
                            

                        /**
                        * field for SetGenericHumanRole
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localSetGenericHumanRole ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localSetGenericHumanRoleTracker = false ;

                           public boolean isSetGenericHumanRoleSpecified(){
                               return localSetGenericHumanRoleTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getSetGenericHumanRole(){
                               return localSetGenericHumanRole;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param SetGenericHumanRole
                               */
                               public void setSetGenericHumanRole(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localSetGenericHumanRoleTracker = param != null;
                                   
                                            this.localSetGenericHumanRole=param;
                                    

                               }
                            

                        /**
                        * field for SetOutput
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localSetOutput ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localSetOutputTracker = false ;

                           public boolean isSetOutputSpecified(){
                               return localSetOutputTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getSetOutput(){
                               return localSetOutput;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param SetOutput
                               */
                               public void setSetOutput(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localSetOutputTracker = param != null;
                                   
                                            this.localSetOutput=param;
                                    

                               }
                            

                        /**
                        * field for SetPriority
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localSetPriority ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localSetPriorityTracker = false ;

                           public boolean isSetPrioritySpecified(){
                               return localSetPriorityTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getSetPriority(){
                               return localSetPriority;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param SetPriority
                               */
                               public void setSetPriority(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localSetPriorityTracker = param != null;
                                   
                                            this.localSetPriority=param;
                                    

                               }
                            

                        /**
                        * field for SetTaskCompletionDeadlineExpression
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localSetTaskCompletionDeadlineExpression ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localSetTaskCompletionDeadlineExpressionTracker = false ;

                           public boolean isSetTaskCompletionDeadlineExpressionSpecified(){
                               return localSetTaskCompletionDeadlineExpressionTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getSetTaskCompletionDeadlineExpression(){
                               return localSetTaskCompletionDeadlineExpression;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param SetTaskCompletionDeadlineExpression
                               */
                               public void setSetTaskCompletionDeadlineExpression(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localSetTaskCompletionDeadlineExpressionTracker = param != null;
                                   
                                            this.localSetTaskCompletionDeadlineExpression=param;
                                    

                               }
                            

                        /**
                        * field for SetTaskCompletionDurationExpression
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localSetTaskCompletionDurationExpression ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localSetTaskCompletionDurationExpressionTracker = false ;

                           public boolean isSetTaskCompletionDurationExpressionSpecified(){
                               return localSetTaskCompletionDurationExpressionTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getSetTaskCompletionDurationExpression(){
                               return localSetTaskCompletionDurationExpression;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param SetTaskCompletionDurationExpression
                               */
                               public void setSetTaskCompletionDurationExpression(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localSetTaskCompletionDurationExpressionTracker = param != null;
                                   
                                            this.localSetTaskCompletionDurationExpression=param;
                                    

                               }
                            

                        /**
                        * field for SetTaskStartDeadlineExpression
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localSetTaskStartDeadlineExpression ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localSetTaskStartDeadlineExpressionTracker = false ;

                           public boolean isSetTaskStartDeadlineExpressionSpecified(){
                               return localSetTaskStartDeadlineExpressionTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getSetTaskStartDeadlineExpression(){
                               return localSetTaskStartDeadlineExpression;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param SetTaskStartDeadlineExpression
                               */
                               public void setSetTaskStartDeadlineExpression(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localSetTaskStartDeadlineExpressionTracker = param != null;
                                   
                                            this.localSetTaskStartDeadlineExpression=param;
                                    

                               }
                            

                        /**
                        * field for SetTaskStartDurationExpression
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localSetTaskStartDurationExpression ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localSetTaskStartDurationExpressionTracker = false ;

                           public boolean isSetTaskStartDurationExpressionSpecified(){
                               return localSetTaskStartDurationExpressionTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getSetTaskStartDurationExpression(){
                               return localSetTaskStartDurationExpression;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param SetTaskStartDurationExpression
                               */
                               public void setSetTaskStartDurationExpression(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localSetTaskStartDurationExpressionTracker = param != null;
                                   
                                            this.localSetTaskStartDurationExpression=param;
                                    

                               }
                            

                        /**
                        * field for Skip
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localSkip ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localSkipTracker = false ;

                           public boolean isSkipSpecified(){
                               return localSkipTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getSkip(){
                               return localSkip;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Skip
                               */
                               public void setSkip(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localSkipTracker = param != null;
                                   
                                            this.localSkip=param;
                                    

                               }
                            

                        /**
                        * field for Start
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localStart ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localStartTracker = false ;

                           public boolean isStartSpecified(){
                               return localStartTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getStart(){
                               return localStart;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Start
                               */
                               public void setStart(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localStartTracker = param != null;
                                   
                                            this.localStart=param;
                                    

                               }
                            

                        /**
                        * field for Stop
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localStop ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localStopTracker = false ;

                           public boolean isStopSpecified(){
                               return localStopTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getStop(){
                               return localStop;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Stop
                               */
                               public void setStop(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localStopTracker = param != null;
                                   
                                            this.localStop=param;
                                    

                               }
                            

                        /**
                        * field for Suspend
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localSuspend ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localSuspendTracker = false ;

                           public boolean isSuspendSpecified(){
                               return localSuspendTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getSuspend(){
                               return localSuspend;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Suspend
                               */
                               public void setSuspend(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localSuspendTracker = param != null;
                                   
                                            this.localSuspend=param;
                                    

                               }
                            

                        /**
                        * field for SuspendUntil
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localSuspendUntil ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localSuspendUntilTracker = false ;

                           public boolean isSuspendUntilSpecified(){
                               return localSuspendUntilTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getSuspendUntil(){
                               return localSuspendUntil;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param SuspendUntil
                               */
                               public void setSuspendUntil(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localSuspendUntilTracker = param != null;
                                   
                                            this.localSuspendUntil=param;
                                    

                               }
                            

                        /**
                        * field for UpdateComment
                        */

                        
                                    protected org.wso2.carbon.humantask.stub.types.TTaskOperation localUpdateComment ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localUpdateCommentTracker = false ;

                           public boolean isUpdateCommentSpecified(){
                               return localUpdateCommentTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.humantask.stub.types.TTaskOperation
                           */
                           public  org.wso2.carbon.humantask.stub.types.TTaskOperation getUpdateComment(){
                               return localUpdateComment;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param UpdateComment
                               */
                               public void setUpdateComment(org.wso2.carbon.humantask.stub.types.TTaskOperation param){
                            
                                clearAllSettingTrackers();
                            localUpdateCommentTracker = param != null;
                                   
                                            this.localUpdateComment=param;
                                    

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
                           namespacePrefix+":tTaskOperationsChoice",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "tTaskOperationsChoice",
                           xmlWriter);
                   }

               
                   }
                if (localActivateTracker){
                                            if (localActivate==null){
                                                 throw new org.apache.axis2.databinding.ADBException("activate cannot be null!!");
                                            }
                                           localActivate.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","activate"),
                                               xmlWriter);
                                        } if (localAddAttachmentTracker){
                                            if (localAddAttachment==null){
                                                 throw new org.apache.axis2.databinding.ADBException("addAttachment cannot be null!!");
                                            }
                                           localAddAttachment.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","addAttachment"),
                                               xmlWriter);
                                        } if (localAddCommentTracker){
                                            if (localAddComment==null){
                                                 throw new org.apache.axis2.databinding.ADBException("addComment cannot be null!!");
                                            }
                                           localAddComment.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","addComment"),
                                               xmlWriter);
                                        } if (localClaimTracker){
                                            if (localClaim==null){
                                                 throw new org.apache.axis2.databinding.ADBException("claim cannot be null!!");
                                            }
                                           localClaim.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","claim"),
                                               xmlWriter);
                                        } if (localCompleteTracker){
                                            if (localComplete==null){
                                                 throw new org.apache.axis2.databinding.ADBException("complete cannot be null!!");
                                            }
                                           localComplete.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","complete"),
                                               xmlWriter);
                                        } if (localDelegateTracker){
                                            if (localDelegate==null){
                                                 throw new org.apache.axis2.databinding.ADBException("delegate cannot be null!!");
                                            }
                                           localDelegate.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","delegate"),
                                               xmlWriter);
                                        } if (localDeleteAttachmentTracker){
                                            if (localDeleteAttachment==null){
                                                 throw new org.apache.axis2.databinding.ADBException("deleteAttachment cannot be null!!");
                                            }
                                           localDeleteAttachment.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","deleteAttachment"),
                                               xmlWriter);
                                        } if (localDeleteCommentTracker){
                                            if (localDeleteComment==null){
                                                 throw new org.apache.axis2.databinding.ADBException("deleteComment cannot be null!!");
                                            }
                                           localDeleteComment.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","deleteComment"),
                                               xmlWriter);
                                        } if (localDeleteFaultTracker){
                                            if (localDeleteFault==null){
                                                 throw new org.apache.axis2.databinding.ADBException("deleteFault cannot be null!!");
                                            }
                                           localDeleteFault.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","deleteFault"),
                                               xmlWriter);
                                        } if (localDeleteOutputTracker){
                                            if (localDeleteOutput==null){
                                                 throw new org.apache.axis2.databinding.ADBException("deleteOutput cannot be null!!");
                                            }
                                           localDeleteOutput.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","deleteOutput"),
                                               xmlWriter);
                                        } if (localFailTracker){
                                            if (localFail==null){
                                                 throw new org.apache.axis2.databinding.ADBException("fail cannot be null!!");
                                            }
                                           localFail.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","fail"),
                                               xmlWriter);
                                        } if (localForwardTracker){
                                            if (localForward==null){
                                                 throw new org.apache.axis2.databinding.ADBException("forward cannot be null!!");
                                            }
                                           localForward.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","forward"),
                                               xmlWriter);
                                        } if (localGetAttachmentTracker){
                                            if (localGetAttachment==null){
                                                 throw new org.apache.axis2.databinding.ADBException("getAttachment cannot be null!!");
                                            }
                                           localGetAttachment.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","getAttachment"),
                                               xmlWriter);
                                        } if (localGetAttachmentInfosTracker){
                                            if (localGetAttachmentInfos==null){
                                                 throw new org.apache.axis2.databinding.ADBException("getAttachmentInfos cannot be null!!");
                                            }
                                           localGetAttachmentInfos.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","getAttachmentInfos"),
                                               xmlWriter);
                                        } if (localGetCommentsTracker){
                                            if (localGetComments==null){
                                                 throw new org.apache.axis2.databinding.ADBException("getComments cannot be null!!");
                                            }
                                           localGetComments.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","getComments"),
                                               xmlWriter);
                                        } if (localGetFaultTracker){
                                            if (localGetFault==null){
                                                 throw new org.apache.axis2.databinding.ADBException("getFault cannot be null!!");
                                            }
                                           localGetFault.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","getFault"),
                                               xmlWriter);
                                        } if (localGetInputTracker){
                                            if (localGetInput==null){
                                                 throw new org.apache.axis2.databinding.ADBException("getInput cannot be null!!");
                                            }
                                           localGetInput.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","getInput"),
                                               xmlWriter);
                                        } if (localGetOutcomeTracker){
                                            if (localGetOutcome==null){
                                                 throw new org.apache.axis2.databinding.ADBException("getOutcome cannot be null!!");
                                            }
                                           localGetOutcome.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","getOutcome"),
                                               xmlWriter);
                                        } if (localGetOutputTracker){
                                            if (localGetOutput==null){
                                                 throw new org.apache.axis2.databinding.ADBException("getOutput cannot be null!!");
                                            }
                                           localGetOutput.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","getOutput"),
                                               xmlWriter);
                                        } if (localGetParentTaskTracker){
                                            if (localGetParentTask==null){
                                                 throw new org.apache.axis2.databinding.ADBException("getParentTask cannot be null!!");
                                            }
                                           localGetParentTask.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","getParentTask"),
                                               xmlWriter);
                                        } if (localGetParentTaskIdentifierTracker){
                                            if (localGetParentTaskIdentifier==null){
                                                 throw new org.apache.axis2.databinding.ADBException("getParentTaskIdentifier cannot be null!!");
                                            }
                                           localGetParentTaskIdentifier.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","getParentTaskIdentifier"),
                                               xmlWriter);
                                        } if (localGetRenderingTracker){
                                            if (localGetRendering==null){
                                                 throw new org.apache.axis2.databinding.ADBException("getRendering cannot be null!!");
                                            }
                                           localGetRendering.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","getRendering"),
                                               xmlWriter);
                                        } if (localGetRenderingTypesTracker){
                                            if (localGetRenderingTypes==null){
                                                 throw new org.apache.axis2.databinding.ADBException("getRenderingTypes cannot be null!!");
                                            }
                                           localGetRenderingTypes.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","getRenderingTypes"),
                                               xmlWriter);
                                        } if (localGetSubtaskIdentifiersTracker){
                                            if (localGetSubtaskIdentifiers==null){
                                                 throw new org.apache.axis2.databinding.ADBException("getSubtaskIdentifiers cannot be null!!");
                                            }
                                           localGetSubtaskIdentifiers.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","getSubtaskIdentifiers"),
                                               xmlWriter);
                                        } if (localGetSubtasksTracker){
                                            if (localGetSubtasks==null){
                                                 throw new org.apache.axis2.databinding.ADBException("getSubtasks cannot be null!!");
                                            }
                                           localGetSubtasks.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","getSubtasks"),
                                               xmlWriter);
                                        } if (localGetTaskDescriptionTracker){
                                            if (localGetTaskDescription==null){
                                                 throw new org.apache.axis2.databinding.ADBException("getTaskDescription cannot be null!!");
                                            }
                                           localGetTaskDescription.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","getTaskDescription"),
                                               xmlWriter);
                                        } if (localGetTaskDetailsTracker){
                                            if (localGetTaskDetails==null){
                                                 throw new org.apache.axis2.databinding.ADBException("getTaskDetails cannot be null!!");
                                            }
                                           localGetTaskDetails.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","getTaskDetails"),
                                               xmlWriter);
                                        } if (localGetTaskHistoryTracker){
                                            if (localGetTaskHistory==null){
                                                 throw new org.apache.axis2.databinding.ADBException("getTaskHistory cannot be null!!");
                                            }
                                           localGetTaskHistory.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","getTaskHistory"),
                                               xmlWriter);
                                        } if (localGetTaskInstanceDataTracker){
                                            if (localGetTaskInstanceData==null){
                                                 throw new org.apache.axis2.databinding.ADBException("getTaskInstanceData cannot be null!!");
                                            }
                                           localGetTaskInstanceData.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","getTaskInstanceData"),
                                               xmlWriter);
                                        } if (localHasSubtasksTracker){
                                            if (localHasSubtasks==null){
                                                 throw new org.apache.axis2.databinding.ADBException("hasSubtasks cannot be null!!");
                                            }
                                           localHasSubtasks.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","hasSubtasks"),
                                               xmlWriter);
                                        } if (localInstantiateSubtaskTracker){
                                            if (localInstantiateSubtask==null){
                                                 throw new org.apache.axis2.databinding.ADBException("instantiateSubtask cannot be null!!");
                                            }
                                           localInstantiateSubtask.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","instantiateSubtask"),
                                               xmlWriter);
                                        } if (localIsSubtaskTracker){
                                            if (localIsSubtask==null){
                                                 throw new org.apache.axis2.databinding.ADBException("isSubtask cannot be null!!");
                                            }
                                           localIsSubtask.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","isSubtask"),
                                               xmlWriter);
                                        } if (localNominateTracker){
                                            if (localNominate==null){
                                                 throw new org.apache.axis2.databinding.ADBException("nominate cannot be null!!");
                                            }
                                           localNominate.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","nominate"),
                                               xmlWriter);
                                        } if (localReleaseTracker){
                                            if (localRelease==null){
                                                 throw new org.apache.axis2.databinding.ADBException("release cannot be null!!");
                                            }
                                           localRelease.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","release"),
                                               xmlWriter);
                                        } if (localRemoveTracker){
                                            if (localRemove==null){
                                                 throw new org.apache.axis2.databinding.ADBException("remove cannot be null!!");
                                            }
                                           localRemove.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","remove"),
                                               xmlWriter);
                                        } if (localResumeTracker){
                                            if (localResume==null){
                                                 throw new org.apache.axis2.databinding.ADBException("resume cannot be null!!");
                                            }
                                           localResume.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","resume"),
                                               xmlWriter);
                                        } if (localSetFaultTracker){
                                            if (localSetFault==null){
                                                 throw new org.apache.axis2.databinding.ADBException("setFault cannot be null!!");
                                            }
                                           localSetFault.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","setFault"),
                                               xmlWriter);
                                        } if (localSetGenericHumanRoleTracker){
                                            if (localSetGenericHumanRole==null){
                                                 throw new org.apache.axis2.databinding.ADBException("setGenericHumanRole cannot be null!!");
                                            }
                                           localSetGenericHumanRole.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","setGenericHumanRole"),
                                               xmlWriter);
                                        } if (localSetOutputTracker){
                                            if (localSetOutput==null){
                                                 throw new org.apache.axis2.databinding.ADBException("setOutput cannot be null!!");
                                            }
                                           localSetOutput.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","setOutput"),
                                               xmlWriter);
                                        } if (localSetPriorityTracker){
                                            if (localSetPriority==null){
                                                 throw new org.apache.axis2.databinding.ADBException("setPriority cannot be null!!");
                                            }
                                           localSetPriority.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","setPriority"),
                                               xmlWriter);
                                        } if (localSetTaskCompletionDeadlineExpressionTracker){
                                            if (localSetTaskCompletionDeadlineExpression==null){
                                                 throw new org.apache.axis2.databinding.ADBException("setTaskCompletionDeadlineExpression cannot be null!!");
                                            }
                                           localSetTaskCompletionDeadlineExpression.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","setTaskCompletionDeadlineExpression"),
                                               xmlWriter);
                                        } if (localSetTaskCompletionDurationExpressionTracker){
                                            if (localSetTaskCompletionDurationExpression==null){
                                                 throw new org.apache.axis2.databinding.ADBException("setTaskCompletionDurationExpression cannot be null!!");
                                            }
                                           localSetTaskCompletionDurationExpression.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","setTaskCompletionDurationExpression"),
                                               xmlWriter);
                                        } if (localSetTaskStartDeadlineExpressionTracker){
                                            if (localSetTaskStartDeadlineExpression==null){
                                                 throw new org.apache.axis2.databinding.ADBException("setTaskStartDeadlineExpression cannot be null!!");
                                            }
                                           localSetTaskStartDeadlineExpression.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","setTaskStartDeadlineExpression"),
                                               xmlWriter);
                                        } if (localSetTaskStartDurationExpressionTracker){
                                            if (localSetTaskStartDurationExpression==null){
                                                 throw new org.apache.axis2.databinding.ADBException("setTaskStartDurationExpression cannot be null!!");
                                            }
                                           localSetTaskStartDurationExpression.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","setTaskStartDurationExpression"),
                                               xmlWriter);
                                        } if (localSkipTracker){
                                            if (localSkip==null){
                                                 throw new org.apache.axis2.databinding.ADBException("skip cannot be null!!");
                                            }
                                           localSkip.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","skip"),
                                               xmlWriter);
                                        } if (localStartTracker){
                                            if (localStart==null){
                                                 throw new org.apache.axis2.databinding.ADBException("start cannot be null!!");
                                            }
                                           localStart.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","start"),
                                               xmlWriter);
                                        } if (localStopTracker){
                                            if (localStop==null){
                                                 throw new org.apache.axis2.databinding.ADBException("stop cannot be null!!");
                                            }
                                           localStop.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","stop"),
                                               xmlWriter);
                                        } if (localSuspendTracker){
                                            if (localSuspend==null){
                                                 throw new org.apache.axis2.databinding.ADBException("suspend cannot be null!!");
                                            }
                                           localSuspend.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","suspend"),
                                               xmlWriter);
                                        } if (localSuspendUntilTracker){
                                            if (localSuspendUntil==null){
                                                 throw new org.apache.axis2.databinding.ADBException("suspendUntil cannot be null!!");
                                            }
                                           localSuspendUntil.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","suspendUntil"),
                                               xmlWriter);
                                        } if (localUpdateCommentTracker){
                                            if (localUpdateComment==null){
                                                 throw new org.apache.axis2.databinding.ADBException("updateComment cannot be null!!");
                                            }
                                           localUpdateComment.serialize(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","updateComment"),
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

                 if (localActivateTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "activate"));
                            
                            
                                    if (localActivate==null){
                                         throw new org.apache.axis2.databinding.ADBException("activate cannot be null!!");
                                    }
                                    elementList.add(localActivate);
                                } if (localAddAttachmentTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "addAttachment"));
                            
                            
                                    if (localAddAttachment==null){
                                         throw new org.apache.axis2.databinding.ADBException("addAttachment cannot be null!!");
                                    }
                                    elementList.add(localAddAttachment);
                                } if (localAddCommentTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "addComment"));
                            
                            
                                    if (localAddComment==null){
                                         throw new org.apache.axis2.databinding.ADBException("addComment cannot be null!!");
                                    }
                                    elementList.add(localAddComment);
                                } if (localClaimTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "claim"));
                            
                            
                                    if (localClaim==null){
                                         throw new org.apache.axis2.databinding.ADBException("claim cannot be null!!");
                                    }
                                    elementList.add(localClaim);
                                } if (localCompleteTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "complete"));
                            
                            
                                    if (localComplete==null){
                                         throw new org.apache.axis2.databinding.ADBException("complete cannot be null!!");
                                    }
                                    elementList.add(localComplete);
                                } if (localDelegateTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "delegate"));
                            
                            
                                    if (localDelegate==null){
                                         throw new org.apache.axis2.databinding.ADBException("delegate cannot be null!!");
                                    }
                                    elementList.add(localDelegate);
                                } if (localDeleteAttachmentTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "deleteAttachment"));
                            
                            
                                    if (localDeleteAttachment==null){
                                         throw new org.apache.axis2.databinding.ADBException("deleteAttachment cannot be null!!");
                                    }
                                    elementList.add(localDeleteAttachment);
                                } if (localDeleteCommentTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "deleteComment"));
                            
                            
                                    if (localDeleteComment==null){
                                         throw new org.apache.axis2.databinding.ADBException("deleteComment cannot be null!!");
                                    }
                                    elementList.add(localDeleteComment);
                                } if (localDeleteFaultTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "deleteFault"));
                            
                            
                                    if (localDeleteFault==null){
                                         throw new org.apache.axis2.databinding.ADBException("deleteFault cannot be null!!");
                                    }
                                    elementList.add(localDeleteFault);
                                } if (localDeleteOutputTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "deleteOutput"));
                            
                            
                                    if (localDeleteOutput==null){
                                         throw new org.apache.axis2.databinding.ADBException("deleteOutput cannot be null!!");
                                    }
                                    elementList.add(localDeleteOutput);
                                } if (localFailTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "fail"));
                            
                            
                                    if (localFail==null){
                                         throw new org.apache.axis2.databinding.ADBException("fail cannot be null!!");
                                    }
                                    elementList.add(localFail);
                                } if (localForwardTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "forward"));
                            
                            
                                    if (localForward==null){
                                         throw new org.apache.axis2.databinding.ADBException("forward cannot be null!!");
                                    }
                                    elementList.add(localForward);
                                } if (localGetAttachmentTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "getAttachment"));
                            
                            
                                    if (localGetAttachment==null){
                                         throw new org.apache.axis2.databinding.ADBException("getAttachment cannot be null!!");
                                    }
                                    elementList.add(localGetAttachment);
                                } if (localGetAttachmentInfosTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "getAttachmentInfos"));
                            
                            
                                    if (localGetAttachmentInfos==null){
                                         throw new org.apache.axis2.databinding.ADBException("getAttachmentInfos cannot be null!!");
                                    }
                                    elementList.add(localGetAttachmentInfos);
                                } if (localGetCommentsTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "getComments"));
                            
                            
                                    if (localGetComments==null){
                                         throw new org.apache.axis2.databinding.ADBException("getComments cannot be null!!");
                                    }
                                    elementList.add(localGetComments);
                                } if (localGetFaultTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "getFault"));
                            
                            
                                    if (localGetFault==null){
                                         throw new org.apache.axis2.databinding.ADBException("getFault cannot be null!!");
                                    }
                                    elementList.add(localGetFault);
                                } if (localGetInputTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "getInput"));
                            
                            
                                    if (localGetInput==null){
                                         throw new org.apache.axis2.databinding.ADBException("getInput cannot be null!!");
                                    }
                                    elementList.add(localGetInput);
                                } if (localGetOutcomeTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "getOutcome"));
                            
                            
                                    if (localGetOutcome==null){
                                         throw new org.apache.axis2.databinding.ADBException("getOutcome cannot be null!!");
                                    }
                                    elementList.add(localGetOutcome);
                                } if (localGetOutputTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "getOutput"));
                            
                            
                                    if (localGetOutput==null){
                                         throw new org.apache.axis2.databinding.ADBException("getOutput cannot be null!!");
                                    }
                                    elementList.add(localGetOutput);
                                } if (localGetParentTaskTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "getParentTask"));
                            
                            
                                    if (localGetParentTask==null){
                                         throw new org.apache.axis2.databinding.ADBException("getParentTask cannot be null!!");
                                    }
                                    elementList.add(localGetParentTask);
                                } if (localGetParentTaskIdentifierTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "getParentTaskIdentifier"));
                            
                            
                                    if (localGetParentTaskIdentifier==null){
                                         throw new org.apache.axis2.databinding.ADBException("getParentTaskIdentifier cannot be null!!");
                                    }
                                    elementList.add(localGetParentTaskIdentifier);
                                } if (localGetRenderingTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "getRendering"));
                            
                            
                                    if (localGetRendering==null){
                                         throw new org.apache.axis2.databinding.ADBException("getRendering cannot be null!!");
                                    }
                                    elementList.add(localGetRendering);
                                } if (localGetRenderingTypesTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "getRenderingTypes"));
                            
                            
                                    if (localGetRenderingTypes==null){
                                         throw new org.apache.axis2.databinding.ADBException("getRenderingTypes cannot be null!!");
                                    }
                                    elementList.add(localGetRenderingTypes);
                                } if (localGetSubtaskIdentifiersTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "getSubtaskIdentifiers"));
                            
                            
                                    if (localGetSubtaskIdentifiers==null){
                                         throw new org.apache.axis2.databinding.ADBException("getSubtaskIdentifiers cannot be null!!");
                                    }
                                    elementList.add(localGetSubtaskIdentifiers);
                                } if (localGetSubtasksTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "getSubtasks"));
                            
                            
                                    if (localGetSubtasks==null){
                                         throw new org.apache.axis2.databinding.ADBException("getSubtasks cannot be null!!");
                                    }
                                    elementList.add(localGetSubtasks);
                                } if (localGetTaskDescriptionTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "getTaskDescription"));
                            
                            
                                    if (localGetTaskDescription==null){
                                         throw new org.apache.axis2.databinding.ADBException("getTaskDescription cannot be null!!");
                                    }
                                    elementList.add(localGetTaskDescription);
                                } if (localGetTaskDetailsTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "getTaskDetails"));
                            
                            
                                    if (localGetTaskDetails==null){
                                         throw new org.apache.axis2.databinding.ADBException("getTaskDetails cannot be null!!");
                                    }
                                    elementList.add(localGetTaskDetails);
                                } if (localGetTaskHistoryTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "getTaskHistory"));
                            
                            
                                    if (localGetTaskHistory==null){
                                         throw new org.apache.axis2.databinding.ADBException("getTaskHistory cannot be null!!");
                                    }
                                    elementList.add(localGetTaskHistory);
                                } if (localGetTaskInstanceDataTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "getTaskInstanceData"));
                            
                            
                                    if (localGetTaskInstanceData==null){
                                         throw new org.apache.axis2.databinding.ADBException("getTaskInstanceData cannot be null!!");
                                    }
                                    elementList.add(localGetTaskInstanceData);
                                } if (localHasSubtasksTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "hasSubtasks"));
                            
                            
                                    if (localHasSubtasks==null){
                                         throw new org.apache.axis2.databinding.ADBException("hasSubtasks cannot be null!!");
                                    }
                                    elementList.add(localHasSubtasks);
                                } if (localInstantiateSubtaskTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "instantiateSubtask"));
                            
                            
                                    if (localInstantiateSubtask==null){
                                         throw new org.apache.axis2.databinding.ADBException("instantiateSubtask cannot be null!!");
                                    }
                                    elementList.add(localInstantiateSubtask);
                                } if (localIsSubtaskTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "isSubtask"));
                            
                            
                                    if (localIsSubtask==null){
                                         throw new org.apache.axis2.databinding.ADBException("isSubtask cannot be null!!");
                                    }
                                    elementList.add(localIsSubtask);
                                } if (localNominateTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "nominate"));
                            
                            
                                    if (localNominate==null){
                                         throw new org.apache.axis2.databinding.ADBException("nominate cannot be null!!");
                                    }
                                    elementList.add(localNominate);
                                } if (localReleaseTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "release"));
                            
                            
                                    if (localRelease==null){
                                         throw new org.apache.axis2.databinding.ADBException("release cannot be null!!");
                                    }
                                    elementList.add(localRelease);
                                } if (localRemoveTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "remove"));
                            
                            
                                    if (localRemove==null){
                                         throw new org.apache.axis2.databinding.ADBException("remove cannot be null!!");
                                    }
                                    elementList.add(localRemove);
                                } if (localResumeTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "resume"));
                            
                            
                                    if (localResume==null){
                                         throw new org.apache.axis2.databinding.ADBException("resume cannot be null!!");
                                    }
                                    elementList.add(localResume);
                                } if (localSetFaultTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "setFault"));
                            
                            
                                    if (localSetFault==null){
                                         throw new org.apache.axis2.databinding.ADBException("setFault cannot be null!!");
                                    }
                                    elementList.add(localSetFault);
                                } if (localSetGenericHumanRoleTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "setGenericHumanRole"));
                            
                            
                                    if (localSetGenericHumanRole==null){
                                         throw new org.apache.axis2.databinding.ADBException("setGenericHumanRole cannot be null!!");
                                    }
                                    elementList.add(localSetGenericHumanRole);
                                } if (localSetOutputTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "setOutput"));
                            
                            
                                    if (localSetOutput==null){
                                         throw new org.apache.axis2.databinding.ADBException("setOutput cannot be null!!");
                                    }
                                    elementList.add(localSetOutput);
                                } if (localSetPriorityTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "setPriority"));
                            
                            
                                    if (localSetPriority==null){
                                         throw new org.apache.axis2.databinding.ADBException("setPriority cannot be null!!");
                                    }
                                    elementList.add(localSetPriority);
                                } if (localSetTaskCompletionDeadlineExpressionTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "setTaskCompletionDeadlineExpression"));
                            
                            
                                    if (localSetTaskCompletionDeadlineExpression==null){
                                         throw new org.apache.axis2.databinding.ADBException("setTaskCompletionDeadlineExpression cannot be null!!");
                                    }
                                    elementList.add(localSetTaskCompletionDeadlineExpression);
                                } if (localSetTaskCompletionDurationExpressionTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "setTaskCompletionDurationExpression"));
                            
                            
                                    if (localSetTaskCompletionDurationExpression==null){
                                         throw new org.apache.axis2.databinding.ADBException("setTaskCompletionDurationExpression cannot be null!!");
                                    }
                                    elementList.add(localSetTaskCompletionDurationExpression);
                                } if (localSetTaskStartDeadlineExpressionTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "setTaskStartDeadlineExpression"));
                            
                            
                                    if (localSetTaskStartDeadlineExpression==null){
                                         throw new org.apache.axis2.databinding.ADBException("setTaskStartDeadlineExpression cannot be null!!");
                                    }
                                    elementList.add(localSetTaskStartDeadlineExpression);
                                } if (localSetTaskStartDurationExpressionTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "setTaskStartDurationExpression"));
                            
                            
                                    if (localSetTaskStartDurationExpression==null){
                                         throw new org.apache.axis2.databinding.ADBException("setTaskStartDurationExpression cannot be null!!");
                                    }
                                    elementList.add(localSetTaskStartDurationExpression);
                                } if (localSkipTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "skip"));
                            
                            
                                    if (localSkip==null){
                                         throw new org.apache.axis2.databinding.ADBException("skip cannot be null!!");
                                    }
                                    elementList.add(localSkip);
                                } if (localStartTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "start"));
                            
                            
                                    if (localStart==null){
                                         throw new org.apache.axis2.databinding.ADBException("start cannot be null!!");
                                    }
                                    elementList.add(localStart);
                                } if (localStopTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "stop"));
                            
                            
                                    if (localStop==null){
                                         throw new org.apache.axis2.databinding.ADBException("stop cannot be null!!");
                                    }
                                    elementList.add(localStop);
                                } if (localSuspendTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "suspend"));
                            
                            
                                    if (localSuspend==null){
                                         throw new org.apache.axis2.databinding.ADBException("suspend cannot be null!!");
                                    }
                                    elementList.add(localSuspend);
                                } if (localSuspendUntilTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "suspendUntil"));
                            
                            
                                    if (localSuspendUntil==null){
                                         throw new org.apache.axis2.databinding.ADBException("suspendUntil cannot be null!!");
                                    }
                                    elementList.add(localSuspendUntil);
                                } if (localUpdateCommentTracker){
                            elementList.add(new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
                                                                      "updateComment"));
                            
                            
                                    if (localUpdateComment==null){
                                         throw new org.apache.axis2.databinding.ADBException("updateComment cannot be null!!");
                                    }
                                    elementList.add(localUpdateComment);
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
        public static TTaskOperationsChoice parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            TTaskOperationsChoice object =
                new TTaskOperationsChoice();

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
                

                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","activate").equals(reader.getName())){
                                
                                                object.setActivate(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","addAttachment").equals(reader.getName())){
                                
                                                object.setAddAttachment(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","addComment").equals(reader.getName())){
                                
                                                object.setAddComment(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","claim").equals(reader.getName())){
                                
                                                object.setClaim(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","complete").equals(reader.getName())){
                                
                                                object.setComplete(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","delegate").equals(reader.getName())){
                                
                                                object.setDelegate(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","deleteAttachment").equals(reader.getName())){
                                
                                                object.setDeleteAttachment(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","deleteComment").equals(reader.getName())){
                                
                                                object.setDeleteComment(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","deleteFault").equals(reader.getName())){
                                
                                                object.setDeleteFault(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","deleteOutput").equals(reader.getName())){
                                
                                                object.setDeleteOutput(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","fail").equals(reader.getName())){
                                
                                                object.setFail(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","forward").equals(reader.getName())){
                                
                                                object.setForward(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","getAttachment").equals(reader.getName())){
                                
                                                object.setGetAttachment(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","getAttachmentInfos").equals(reader.getName())){
                                
                                                object.setGetAttachmentInfos(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","getComments").equals(reader.getName())){
                                
                                                object.setGetComments(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","getFault").equals(reader.getName())){
                                
                                                object.setGetFault(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","getInput").equals(reader.getName())){
                                
                                                object.setGetInput(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","getOutcome").equals(reader.getName())){
                                
                                                object.setGetOutcome(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","getOutput").equals(reader.getName())){
                                
                                                object.setGetOutput(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","getParentTask").equals(reader.getName())){
                                
                                                object.setGetParentTask(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","getParentTaskIdentifier").equals(reader.getName())){
                                
                                                object.setGetParentTaskIdentifier(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","getRendering").equals(reader.getName())){
                                
                                                object.setGetRendering(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","getRenderingTypes").equals(reader.getName())){
                                
                                                object.setGetRenderingTypes(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","getSubtaskIdentifiers").equals(reader.getName())){
                                
                                                object.setGetSubtaskIdentifiers(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","getSubtasks").equals(reader.getName())){
                                
                                                object.setGetSubtasks(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","getTaskDescription").equals(reader.getName())){
                                
                                                object.setGetTaskDescription(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","getTaskDetails").equals(reader.getName())){
                                
                                                object.setGetTaskDetails(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","getTaskHistory").equals(reader.getName())){
                                
                                                object.setGetTaskHistory(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","getTaskInstanceData").equals(reader.getName())){
                                
                                                object.setGetTaskInstanceData(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","hasSubtasks").equals(reader.getName())){
                                
                                                object.setHasSubtasks(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","instantiateSubtask").equals(reader.getName())){
                                
                                                object.setInstantiateSubtask(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","isSubtask").equals(reader.getName())){
                                
                                                object.setIsSubtask(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","nominate").equals(reader.getName())){
                                
                                                object.setNominate(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","release").equals(reader.getName())){
                                
                                                object.setRelease(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","remove").equals(reader.getName())){
                                
                                                object.setRemove(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","resume").equals(reader.getName())){
                                
                                                object.setResume(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","setFault").equals(reader.getName())){
                                
                                                object.setSetFault(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","setGenericHumanRole").equals(reader.getName())){
                                
                                                object.setSetGenericHumanRole(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","setOutput").equals(reader.getName())){
                                
                                                object.setSetOutput(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","setPriority").equals(reader.getName())){
                                
                                                object.setSetPriority(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","setTaskCompletionDeadlineExpression").equals(reader.getName())){
                                
                                                object.setSetTaskCompletionDeadlineExpression(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","setTaskCompletionDurationExpression").equals(reader.getName())){
                                
                                                object.setSetTaskCompletionDurationExpression(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","setTaskStartDeadlineExpression").equals(reader.getName())){
                                
                                                object.setSetTaskStartDeadlineExpression(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","setTaskStartDurationExpression").equals(reader.getName())){
                                
                                                object.setSetTaskStartDurationExpression(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","skip").equals(reader.getName())){
                                
                                                object.setSkip(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","start").equals(reader.getName())){
                                
                                                object.setStart(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","stop").equals(reader.getName())){
                                
                                                object.setStop(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","suspend").equals(reader.getName())){
                                
                                                object.setSuspend(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","suspendUntil").equals(reader.getName())){
                                
                                                object.setSuspendUntil(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803","updateComment").equals(reader.getName())){
                                
                                                object.setUpdateComment(org.wso2.carbon.humantask.stub.types.TTaskOperation.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                        else
                                    
                                   if (reader.isStartElement()){
                                
                                    
                                     
                                     //use the QName from the parser as the name for the builder
                                     javax.xml.namespace.QName startQname51 = reader.getName();

                                     // We need to wrap the reader so that it produces a fake START_DOCUMENT event
                                     // this is needed by the builder classes
                                     org.apache.axis2.databinding.utils.NamedStaxOMBuilder builder51 =
                                         new org.apache.axis2.databinding.utils.NamedStaxOMBuilder(
                                             new org.apache.axis2.util.StreamWrapper(reader),startQname51);
                                     object.setExtraElement(builder51.getOMElement());
                                       
                                         reader.next();
                                     
                              }  // End of if for expected property start element
                                



            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class

        

        }
           
    