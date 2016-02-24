
/**
 * HumanTaskClientAPIAdminCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.humantask.stub.ui.task.client.api;

    /**
     *  HumanTaskClientAPIAdminCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class HumanTaskClientAPIAdminCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public HumanTaskClientAPIAdminCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public HumanTaskClientAPIAdminCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for batchStop method
            * override this method for handling normal response from batchStop operation
            */
           public void receiveResultbatchStop(
                    org.wso2.carbon.humantask.stub.api.TBatchResponse[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from batchStop operation
           */
            public void receiveErrorbatchStop(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getMyTaskAbstracts method
            * override this method for handling normal response from getMyTaskAbstracts operation
            */
           public void receiveResultgetMyTaskAbstracts(
                    org.wso2.carbon.humantask.stub.types.TTaskAbstract[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getMyTaskAbstracts operation
           */
            public void receiveErrorgetMyTaskAbstracts(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for stop method
            * override this method for handling normal response from stop operation
            */
           public void receiveResultstop(
                    ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from stop operation
           */
            public void receiveErrorstop(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for batchComplete method
            * override this method for handling normal response from batchComplete operation
            */
           public void receiveResultbatchComplete(
                    org.wso2.carbon.humantask.stub.api.TBatchResponse[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from batchComplete operation
           */
            public void receiveErrorbatchComplete(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for resume method
            * override this method for handling normal response from resume operation
            */
           public void receiveResultresume(
                    ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from resume operation
           */
            public void receiveErrorresume(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getRenderingTypes method
            * override this method for handling normal response from getRenderingTypes operation
            */
           public void receiveResultgetRenderingTypes(
                    javax.xml.namespace.QName[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getRenderingTypes operation
           */
            public void receiveErrorgetRenderingTypes(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for setTaskCompletionDeadlineExpression method
            * override this method for handling normal response from setTaskCompletionDeadlineExpression operation
            */
           public void receiveResultsetTaskCompletionDeadlineExpression(
                    ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from setTaskCompletionDeadlineExpression operation
           */
            public void receiveErrorsetTaskCompletionDeadlineExpression(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for setOutput method
            * override this method for handling normal response from setOutput operation
            */
           public void receiveResultsetOutput(
                    ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from setOutput operation
           */
            public void receiveErrorsetOutput(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getTaskOperations method
            * override this method for handling normal response from getTaskOperations operation
            */
           public void receiveResultgetTaskOperations(
                    org.wso2.carbon.humantask.stub.types.TTaskOperations result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getTaskOperations operation
           */
            public void receiveErrorgetTaskOperations(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for batchRelease method
            * override this method for handling normal response from batchRelease operation
            */
           public void receiveResultbatchRelease(
                    org.wso2.carbon.humantask.stub.api.TBatchResponse[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from batchRelease operation
           */
            public void receiveErrorbatchRelease(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getTaskDetails method
            * override this method for handling normal response from getTaskDetails operation
            */
           public void receiveResultgetTaskDetails(
                    org.wso2.carbon.humantask.stub.types.TTaskDetails result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getTaskDetails operation
           */
            public void receiveErrorgetTaskDetails(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for forward method
            * override this method for handling normal response from forward operation
            */
           public void receiveResultforward(
                    ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from forward operation
           */
            public void receiveErrorforward(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for suspend method
            * override this method for handling normal response from suspend operation
            */
           public void receiveResultsuspend(
                    ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from suspend operation
           */
            public void receiveErrorsuspend(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getAssignableUserList method
            * override this method for handling normal response from getAssignableUserList operation
            */
           public void receiveResultgetAssignableUserList(
                    org.wso2.carbon.humantask.stub.types.TUser[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getAssignableUserList operation
           */
            public void receiveErrorgetAssignableUserList(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for isSubtask method
            * override this method for handling normal response from isSubtask operation
            */
           public void receiveResultisSubtask(
                    boolean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from isSubtask operation
           */
            public void receiveErrorisSubtask(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for updateComment method
            * override this method for handling normal response from updateComment operation
            */
           public void receiveResultupdateComment(
                    ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from updateComment operation
           */
            public void receiveErrorupdateComment(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getMyTaskDetails method
            * override this method for handling normal response from getMyTaskDetails operation
            */
           public void receiveResultgetMyTaskDetails(
                    org.wso2.carbon.humantask.stub.types.TTaskDetails[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getMyTaskDetails operation
           */
            public void receiveErrorgetMyTaskDetails(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for batchNominate method
            * override this method for handling normal response from batchNominate operation
            */
           public void receiveResultbatchNominate(
                    org.wso2.carbon.humantask.stub.api.TBatchResponse[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from batchNominate operation
           */
            public void receiveErrorbatchNominate(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for loadTask method
            * override this method for handling normal response from loadTask operation
            */
           public void receiveResultloadTask(
                    org.wso2.carbon.humantask.stub.types.TTaskAbstract result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from loadTask operation
           */
            public void receiveErrorloadTask(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getSubtaskIdentifiers method
            * override this method for handling normal response from getSubtaskIdentifiers operation
            */
           public void receiveResultgetSubtaskIdentifiers(
                    org.apache.axis2.databinding.types.URI[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getSubtaskIdentifiers operation
           */
            public void receiveErrorgetSubtaskIdentifiers(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getOutcome method
            * override this method for handling normal response from getOutcome operation
            */
           public void receiveResultgetOutcome(
                    java.lang.String result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getOutcome operation
           */
            public void receiveErrorgetOutcome(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getRendering method
            * override this method for handling normal response from getRendering operation
            */
           public void receiveResultgetRendering(
                    java.lang.Object result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getRendering operation
           */
            public void receiveErrorgetRendering(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for simpleQuery method
            * override this method for handling normal response from simpleQuery operation
            */
           public void receiveResultsimpleQuery(
                    org.wso2.carbon.humantask.stub.types.TTaskSimpleQueryResultSet result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from simpleQuery operation
           */
            public void receiveErrorsimpleQuery(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for skip method
            * override this method for handling normal response from skip operation
            */
           public void receiveResultskip(
                    ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from skip operation
           */
            public void receiveErrorskip(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for batchFail method
            * override this method for handling normal response from batchFail operation
            */
           public void receiveResultbatchFail(
                    org.wso2.carbon.humantask.stub.api.TBatchResponse[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from batchFail operation
           */
            public void receiveErrorbatchFail(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for setTaskCompletionDurationExpression method
            * override this method for handling normal response from setTaskCompletionDurationExpression operation
            */
           public void receiveResultsetTaskCompletionDurationExpression(
                    ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from setTaskCompletionDurationExpression operation
           */
            public void receiveErrorsetTaskCompletionDurationExpression(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for start method
            * override this method for handling normal response from start operation
            */
           public void receiveResultstart(
                    ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from start operation
           */
            public void receiveErrorstart(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for fail method
            * override this method for handling normal response from fail operation
            */
           public void receiveResultfail(
                    ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from fail operation
           */
            public void receiveErrorfail(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for activate method
            * override this method for handling normal response from activate operation
            */
           public void receiveResultactivate(
                    ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from activate operation
           */
            public void receiveErroractivate(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for loadAuthorisationParams method
            * override this method for handling normal response from loadAuthorisationParams operation
            */
           public void receiveResultloadAuthorisationParams(
                    org.wso2.carbon.humantask.stub.types.TTaskAuthorisationParams result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from loadAuthorisationParams operation
           */
            public void receiveErrorloadAuthorisationParams(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for addComment method
            * override this method for handling normal response from addComment operation
            */
           public void receiveResultaddComment(
                    org.apache.axis2.databinding.types.URI result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from addComment operation
           */
            public void receiveErroraddComment(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for deleteComment method
            * override this method for handling normal response from deleteComment operation
            */
           public void receiveResultdeleteComment(
                    ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from deleteComment operation
           */
            public void receiveErrordeleteComment(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getTaskInstanceData method
            * override this method for handling normal response from getTaskInstanceData operation
            */
           public void receiveResultgetTaskInstanceData(
                    org.wso2.carbon.humantask.stub.types.TTaskInstanceData result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getTaskInstanceData operation
           */
            public void receiveErrorgetTaskInstanceData(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for delegate method
            * override this method for handling normal response from delegate operation
            */
           public void receiveResultdelegate(
                    ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from delegate operation
           */
            public void receiveErrordelegate(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getComments method
            * override this method for handling normal response from getComments operation
            */
           public void receiveResultgetComments(
                    org.wso2.carbon.humantask.stub.types.TComment[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getComments operation
           */
            public void receiveErrorgetComments(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getParentTask method
            * override this method for handling normal response from getParentTask operation
            */
           public void receiveResultgetParentTask(
                    org.wso2.carbon.humantask.stub.types.TTaskDetails result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getParentTask operation
           */
            public void receiveErrorgetParentTask(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for addAttachment method
            * override this method for handling normal response from addAttachment operation
            */
           public void receiveResultaddAttachment(
                    boolean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from addAttachment operation
           */
            public void receiveErroraddAttachment(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for batchResume method
            * override this method for handling normal response from batchResume operation
            */
           public void receiveResultbatchResume(
                    org.wso2.carbon.humantask.stub.api.TBatchResponse[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from batchResume operation
           */
            public void receiveErrorbatchResume(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for batchRemove method
            * override this method for handling normal response from batchRemove operation
            */
           public void receiveResultbatchRemove(
                    org.wso2.carbon.humantask.stub.api.TBatchResponse[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from batchRemove operation
           */
            public void receiveErrorbatchRemove(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getAttachment method
            * override this method for handling normal response from getAttachment operation
            */
           public void receiveResultgetAttachment(
                    org.wso2.carbon.humantask.stub.types.TAttachment[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getAttachment operation
           */
            public void receiveErrorgetAttachment(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getAttachmentInfos method
            * override this method for handling normal response from getAttachmentInfos operation
            */
           public void receiveResultgetAttachmentInfos(
                    org.wso2.carbon.humantask.stub.types.TAttachmentInfo[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getAttachmentInfos operation
           */
            public void receiveErrorgetAttachmentInfos(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for remove method
            * override this method for handling normal response from remove operation
            */
           public void receiveResultremove(
                    ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from remove operation
           */
            public void receiveErrorremove(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for batchStart method
            * override this method for handling normal response from batchStart operation
            */
           public void receiveResultbatchStart(
                    org.wso2.carbon.humantask.stub.api.TBatchResponse[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from batchStart operation
           */
            public void receiveErrorbatchStart(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for instantiateSubtask method
            * override this method for handling normal response from instantiateSubtask operation
            */
           public void receiveResultinstantiateSubtask(
                    org.apache.axis2.databinding.types.URI result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from instantiateSubtask operation
           */
            public void receiveErrorinstantiateSubtask(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getTaskHistory method
            * override this method for handling normal response from getTaskHistory operation
            */
           public void receiveResultgetTaskHistory(
                    org.wso2.carbon.humantask.stub.types.TTaskEventType[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getTaskHistory operation
           */
            public void receiveErrorgetTaskHistory(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for setTaskStartDeadlineExpression method
            * override this method for handling normal response from setTaskStartDeadlineExpression operation
            */
           public void receiveResultsetTaskStartDeadlineExpression(
                    ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from setTaskStartDeadlineExpression operation
           */
            public void receiveErrorsetTaskStartDeadlineExpression(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for loadTaskEvents method
            * override this method for handling normal response from loadTaskEvents operation
            */
           public void receiveResultloadTaskEvents(
                    org.wso2.carbon.humantask.stub.types.TTaskEvents result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from loadTaskEvents operation
           */
            public void receiveErrorloadTaskEvents(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for batchSkip method
            * override this method for handling normal response from batchSkip operation
            */
           public void receiveResultbatchSkip(
                    org.wso2.carbon.humantask.stub.api.TBatchResponse[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from batchSkip operation
           */
            public void receiveErrorbatchSkip(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for complete method
            * override this method for handling normal response from complete operation
            */
           public void receiveResultcomplete(
                    ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from complete operation
           */
            public void receiveErrorcomplete(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for batchDelegate method
            * override this method for handling normal response from batchDelegate operation
            */
           public void receiveResultbatchDelegate(
                    org.wso2.carbon.humantask.stub.api.TBatchResponse[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from batchDelegate operation
           */
            public void receiveErrorbatchDelegate(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for batchSetGenericHumanRole method
            * override this method for handling normal response from batchSetGenericHumanRole operation
            */
           public void receiveResultbatchSetGenericHumanRole(
                    org.wso2.carbon.humantask.stub.api.TBatchResponse[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from batchSetGenericHumanRole operation
           */
            public void receiveErrorbatchSetGenericHumanRole(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for setGenericHumanRole method
            * override this method for handling normal response from setGenericHumanRole operation
            */
           public void receiveResultsetGenericHumanRole(
                    ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from setGenericHumanRole operation
           */
            public void receiveErrorsetGenericHumanRole(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getInput method
            * override this method for handling normal response from getInput operation
            */
           public void receiveResultgetInput(
                    java.lang.Object result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getInput operation
           */
            public void receiveErrorgetInput(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for hasSubtasks method
            * override this method for handling normal response from hasSubtasks operation
            */
           public void receiveResulthasSubtasks(
                    boolean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from hasSubtasks operation
           */
            public void receiveErrorhasSubtasks(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for batchActivate method
            * override this method for handling normal response from batchActivate operation
            */
           public void receiveResultbatchActivate(
                    org.wso2.carbon.humantask.stub.api.TBatchResponse[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from batchActivate operation
           */
            public void receiveErrorbatchActivate(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for claim method
            * override this method for handling normal response from claim operation
            */
           public void receiveResultclaim(
                    ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from claim operation
           */
            public void receiveErrorclaim(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for query method
            * override this method for handling normal response from query operation
            */
           public void receiveResultquery(
                    org.wso2.carbon.humantask.stub.types.TTaskQueryResultSet result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from query operation
           */
            public void receiveErrorquery(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for batchClaim method
            * override this method for handling normal response from batchClaim operation
            */
           public void receiveResultbatchClaim(
                    org.wso2.carbon.humantask.stub.api.TBatchResponse[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from batchClaim operation
           */
            public void receiveErrorbatchClaim(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for batchSetPriority method
            * override this method for handling normal response from batchSetPriority operation
            */
           public void receiveResultbatchSetPriority(
                    org.wso2.carbon.humantask.stub.api.TBatchResponse[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from batchSetPriority operation
           */
            public void receiveErrorbatchSetPriority(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for setFault method
            * override this method for handling normal response from setFault operation
            */
           public void receiveResultsetFault(
                    ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from setFault operation
           */
            public void receiveErrorsetFault(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for suspendUntil method
            * override this method for handling normal response from suspendUntil operation
            */
           public void receiveResultsuspendUntil(
                    ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from suspendUntil operation
           */
            public void receiveErrorsuspendUntil(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for setTaskStartDurationExpression method
            * override this method for handling normal response from setTaskStartDurationExpression operation
            */
           public void receiveResultsetTaskStartDurationExpression(
                    ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from setTaskStartDurationExpression operation
           */
            public void receiveErrorsetTaskStartDurationExpression(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getTaskDescription method
            * override this method for handling normal response from getTaskDescription operation
            */
           public void receiveResultgetTaskDescription(
                    java.lang.String result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getTaskDescription operation
           */
            public void receiveErrorgetTaskDescription(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for deleteAttachment method
            * override this method for handling normal response from deleteAttachment operation
            */
           public void receiveResultdeleteAttachment(
                    ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from deleteAttachment operation
           */
            public void receiveErrordeleteAttachment(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for nominate method
            * override this method for handling normal response from nominate operation
            */
           public void receiveResultnominate(
                    ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from nominate operation
           */
            public void receiveErrornominate(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for deleteOutput method
            * override this method for handling normal response from deleteOutput operation
            */
           public void receiveResultdeleteOutput(
                    ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from deleteOutput operation
           */
            public void receiveErrordeleteOutput(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for batchForward method
            * override this method for handling normal response from batchForward operation
            */
           public void receiveResultbatchForward(
                    org.wso2.carbon.humantask.stub.api.TBatchResponse[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from batchForward operation
           */
            public void receiveErrorbatchForward(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for batchSuspend method
            * override this method for handling normal response from batchSuspend operation
            */
           public void receiveResultbatchSuspend(
                    org.wso2.carbon.humantask.stub.api.TBatchResponse[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from batchSuspend operation
           */
            public void receiveErrorbatchSuspend(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getSubtasks method
            * override this method for handling normal response from getSubtasks operation
            */
           public void receiveResultgetSubtasks(
                    org.wso2.carbon.humantask.stub.types.TTaskDetails[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getSubtasks operation
           */
            public void receiveErrorgetSubtasks(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for deleteFault method
            * override this method for handling normal response from deleteFault operation
            */
           public void receiveResultdeleteFault(
                    ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from deleteFault operation
           */
            public void receiveErrordeleteFault(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getOutput method
            * override this method for handling normal response from getOutput operation
            */
           public void receiveResultgetOutput(
                    java.lang.Object result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getOutput operation
           */
            public void receiveErrorgetOutput(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for release method
            * override this method for handling normal response from release operation
            */
           public void receiveResultrelease(
                    ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from release operation
           */
            public void receiveErrorrelease(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getFault method
            * override this method for handling normal response from getFault operation
            */
           public void receiveResultgetFault(
                    org.wso2.carbon.humantask.stub.types.TFault result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getFault operation
           */
            public void receiveErrorgetFault(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for setPriority method
            * override this method for handling normal response from setPriority operation
            */
           public void receiveResultsetPriority(
                    ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from setPriority operation
           */
            public void receiveErrorsetPriority(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for batchSuspendUntil method
            * override this method for handling normal response from batchSuspendUntil operation
            */
           public void receiveResultbatchSuspendUntil(
                    org.wso2.carbon.humantask.stub.api.TBatchResponse[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from batchSuspendUntil operation
           */
            public void receiveErrorbatchSuspendUntil(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getParentTaskIdentifier method
            * override this method for handling normal response from getParentTaskIdentifier operation
            */
           public void receiveResultgetParentTaskIdentifier(
                    org.apache.axis2.databinding.types.URI result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getParentTaskIdentifier operation
           */
            public void receiveErrorgetParentTaskIdentifier(java.lang.Exception e) {
            }
                


    }
    