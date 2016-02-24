
/**
 * DirectoryServerManagerCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.directory.server.manager.stub;

    /**
     *  DirectoryServerManagerCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class DirectoryServerManagerCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public DirectoryServerManagerCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public DirectoryServerManagerCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for removeServer method
            * override this method for handling normal response from removeServer operation
            */
           public void receiveResultremoveServer(
                    ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from removeServer operation
           */
            public void receiveErrorremoveServer(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for changePassword method
            * override this method for handling normal response from changePassword operation
            */
           public void receiveResultchangePassword(
                    ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from changePassword operation
           */
            public void receiveErrorchangePassword(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getPasswordConformanceRegularExpression method
            * override this method for handling normal response from getPasswordConformanceRegularExpression operation
            */
           public void receiveResultgetPasswordConformanceRegularExpression(
                    java.lang.String result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getPasswordConformanceRegularExpression operation
           */
            public void receiveErrorgetPasswordConformanceRegularExpression(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for isKDCEnabled method
            * override this method for handling normal response from isKDCEnabled operation
            */
           public void receiveResultisKDCEnabled(
                    boolean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from isKDCEnabled operation
           */
            public void receiveErrorisKDCEnabled(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for listServicePrinciples method
            * override this method for handling normal response from listServicePrinciples operation
            */
           public void receiveResultlistServicePrinciples(
                    org.wso2.carbon.directory.common.stub.types.ServerPrinciple[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from listServicePrinciples operation
           */
            public void receiveErrorlistServicePrinciples(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for isExistingServicePrinciple method
            * override this method for handling normal response from isExistingServicePrinciple operation
            */
           public void receiveResultisExistingServicePrinciple(
                    boolean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from isExistingServicePrinciple operation
           */
            public void receiveErrorisExistingServicePrinciple(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for addServer method
            * override this method for handling normal response from addServer operation
            */
           public void receiveResultaddServer(
                    ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from addServer operation
           */
            public void receiveErroraddServer(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getServiceNameConformanceRegularExpression method
            * override this method for handling normal response from getServiceNameConformanceRegularExpression operation
            */
           public void receiveResultgetServiceNameConformanceRegularExpression(
                    java.lang.String result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getServiceNameConformanceRegularExpression operation
           */
            public void receiveErrorgetServiceNameConformanceRegularExpression(java.lang.Exception e) {
            }
                


    }
    