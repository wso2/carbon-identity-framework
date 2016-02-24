
/**
 * XMPPConfigurationServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.identity.provider.stub;

    /**
     *  XMPPConfigurationServiceCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class XMPPConfigurationServiceCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public XMPPConfigurationServiceCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public XMPPConfigurationServiceCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for addUserXmppSettings method
            * override this method for handling normal response from addUserXmppSettings operation
            */
           public void receiveResultaddUserXmppSettings(
                    boolean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from addUserXmppSettings operation
           */
            public void receiveErroraddUserXmppSettings(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getUserIM method
            * override this method for handling normal response from getUserIM operation
            */
           public void receiveResultgetUserIM(
                    java.lang.String result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getUserIM operation
           */
            public void receiveErrorgetUserIM(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getXmppSettings method
            * override this method for handling normal response from getXmppSettings operation
            */
           public void receiveResultgetXmppSettings(
                    org.wso2.carbon.identity.provider.stub.dto.XMPPSettingsDTO result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getXmppSettings operation
           */
            public void receiveErrorgetXmppSettings(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for isXMPPSettingsEnabled method
            * override this method for handling normal response from isXMPPSettingsEnabled operation
            */
           public void receiveResultisXMPPSettingsEnabled(
                    boolean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from isXMPPSettingsEnabled operation
           */
            public void receiveErrorisXMPPSettingsEnabled(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for hasXMPPSettings method
            * override this method for handling normal response from hasXMPPSettings operation
            */
           public void receiveResulthasXMPPSettings(
                    boolean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from hasXMPPSettings operation
           */
            public void receiveErrorhasXMPPSettings(java.lang.Exception e) {
            }
                


    }
    