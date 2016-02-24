
/**
 * UserRegistrationAdminServiceStub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */
        package org.wso2.carbon.identity.user.registration.stub;

        

        /*
        *  UserRegistrationAdminServiceStub java implementation
        */

        
        public class UserRegistrationAdminServiceStub extends org.apache.axis2.client.Stub
        implements UserRegistrationAdminService{
        protected org.apache.axis2.description.AxisOperation[] _operations;

        //hashmaps to keep the fault mapping
        private java.util.HashMap faultExceptionNameMap = new java.util.HashMap();
        private java.util.HashMap faultExceptionClassNameMap = new java.util.HashMap();
        private java.util.HashMap faultMessageMap = new java.util.HashMap();

        private static int counter = 0;

        private static synchronized java.lang.String getUniqueSuffix(){
            // reset the counter if it is greater than 99999
            if (counter > 99999){
                counter = 0;
            }
            counter = counter + 1; 
            return java.lang.Long.toString(java.lang.System.currentTimeMillis()) + "_" + counter;
        }

    
    private void populateAxisService() throws org.apache.axis2.AxisFault {

     //creating the Service with a unique name
     _service = new org.apache.axis2.description.AxisService("UserRegistrationAdminService" + getUniqueSuffix());
     addAnonymousOperations();

        //creating the operations
        org.apache.axis2.description.AxisOperation __operation;

        _operations = new org.apache.axis2.description.AxisOperation[7];
        
                    __operation = new org.apache.axis2.description.RobustOutOnlyAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://registration.user.identity.carbon.wso2.org", "addUser"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[0]=__operation;
            
        
                   __operation = new org.apache.axis2.description.OutInAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://registration.user.identity.carbon.wso2.org", "isAddUserWithInfoCardEnabled"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[1]=__operation;
            
        
                   __operation = new org.apache.axis2.description.OutInAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://registration.user.identity.carbon.wso2.org", "getPasswordRegularExpressions"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[2]=__operation;
            
        
                   __operation = new org.apache.axis2.description.OutInAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://registration.user.identity.carbon.wso2.org", "isAddUserWithOpenIDEnabled"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[3]=__operation;
            
        
                   __operation = new org.apache.axis2.description.OutInAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://registration.user.identity.carbon.wso2.org", "isAddUserEnabled"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[4]=__operation;
            
        
                   __operation = new org.apache.axis2.description.OutInAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://registration.user.identity.carbon.wso2.org", "isUserExist"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[5]=__operation;
            
        
                   __operation = new org.apache.axis2.description.OutInAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://registration.user.identity.carbon.wso2.org", "readUserFieldsForUserRegistration"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[6]=__operation;
            
        
        }

    //populates the faults
    private void populateFaults(){
         
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","UserRegistrationAdminServiceException"), "addUser"),"org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceException");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","UserRegistrationAdminServiceException"), "addUser"),"org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceException");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","UserRegistrationAdminServiceException"), "addUser"),"org.wso2.carbon.identity.user.registration.stub.types.axis2.UserRegistrationAdminServiceException");
           
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","UserRegistrationAdminServiceException"), "isAddUserWithInfoCardEnabled"),"org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceException");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","UserRegistrationAdminServiceException"), "isAddUserWithInfoCardEnabled"),"org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceException");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","UserRegistrationAdminServiceException"), "isAddUserWithInfoCardEnabled"),"org.wso2.carbon.identity.user.registration.stub.types.axis2.UserRegistrationAdminServiceException");
           
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","UserRegistrationAdminServiceIdentityException"), "getPasswordRegularExpressions"),"org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceIdentityException");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","UserRegistrationAdminServiceIdentityException"), "getPasswordRegularExpressions"),"org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceIdentityException");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","UserRegistrationAdminServiceIdentityException"), "getPasswordRegularExpressions"),"org.wso2.carbon.identity.user.registration.stub.types.axis2.UserRegistrationAdminServiceIdentityException");
           
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","UserRegistrationAdminServiceException"), "isAddUserWithOpenIDEnabled"),"org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceException");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","UserRegistrationAdminServiceException"), "isAddUserWithOpenIDEnabled"),"org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceException");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","UserRegistrationAdminServiceException"), "isAddUserWithOpenIDEnabled"),"org.wso2.carbon.identity.user.registration.stub.types.axis2.UserRegistrationAdminServiceException");
           
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","UserRegistrationAdminServiceException"), "isAddUserEnabled"),"org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceException");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","UserRegistrationAdminServiceException"), "isAddUserEnabled"),"org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceException");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","UserRegistrationAdminServiceException"), "isAddUserEnabled"),"org.wso2.carbon.identity.user.registration.stub.types.axis2.UserRegistrationAdminServiceException");
           
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","UserRegistrationAdminServiceUserRegistrationException"), "isUserExist"),"org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceUserRegistrationException");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","UserRegistrationAdminServiceUserRegistrationException"), "isUserExist"),"org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceUserRegistrationException");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","UserRegistrationAdminServiceUserRegistrationException"), "isUserExist"),"org.wso2.carbon.identity.user.registration.stub.types.axis2.UserRegistrationAdminServiceUserRegistrationException");
           
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","UserRegistrationAdminServiceIdentityException"), "readUserFieldsForUserRegistration"),"org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceIdentityException");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","UserRegistrationAdminServiceIdentityException"), "readUserFieldsForUserRegistration"),"org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceIdentityException");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","UserRegistrationAdminServiceIdentityException"), "readUserFieldsForUserRegistration"),"org.wso2.carbon.identity.user.registration.stub.types.axis2.UserRegistrationAdminServiceIdentityException");
           


    }

    /**
      *Constructor that takes in a configContext
      */

    public UserRegistrationAdminServiceStub(org.apache.axis2.context.ConfigurationContext configurationContext,
       java.lang.String targetEndpoint)
       throws org.apache.axis2.AxisFault {
         this(configurationContext,targetEndpoint,false);
   }


   /**
     * Constructor that takes in a configContext  and useseperate listner
     */
   public UserRegistrationAdminServiceStub(org.apache.axis2.context.ConfigurationContext configurationContext,
        java.lang.String targetEndpoint, boolean useSeparateListener)
        throws org.apache.axis2.AxisFault {
         //To populate AxisService
         populateAxisService();
         populateFaults();

        _serviceClient = new org.apache.axis2.client.ServiceClient(configurationContext,_service);
        
	
        _serviceClient.getOptions().setTo(new org.apache.axis2.addressing.EndpointReference(
                targetEndpoint));
        _serviceClient.getOptions().setUseSeparateListener(useSeparateListener);
        
            //Set the soap version
            _serviceClient.getOptions().setSoapVersionURI(org.apache.axiom.soap.SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        
    
    }

    /**
     * Default Constructor
     */
    public UserRegistrationAdminServiceStub(org.apache.axis2.context.ConfigurationContext configurationContext) throws org.apache.axis2.AxisFault {
        
                    this(configurationContext,"https://localhost:9443/services/UserRegistrationAdminService.UserRegistrationAdminServiceHttpsSoap12Endpoint/" );
                
    }

    /**
     * Default Constructor
     */
    public UserRegistrationAdminServiceStub() throws org.apache.axis2.AxisFault {
        
                    this("https://localhost:9443/services/UserRegistrationAdminService.UserRegistrationAdminServiceHttpsSoap12Endpoint/" );
                
    }

    /**
     * Constructor taking the target endpoint
     */
    public UserRegistrationAdminServiceStub(java.lang.String targetEndpoint) throws org.apache.axis2.AxisFault {
        this(null,targetEndpoint);
    }



         
                
                /**
                  * Auto generated method signature
                  * 
                     * @throws org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceException : 
                  */
                public void  addUser(
                 org.wso2.carbon.identity.user.registration.stub.dto.UserDTO user28

                ) throws java.rmi.RemoteException
                
                
                        ,org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceException
                {
                org.apache.axis2.context.MessageContext _messageContext = null;

                try {
                org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[0].getName());
                _operationClient.getOptions().setAction("urn:addUser");
                _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

                
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              
                org.apache.axiom.soap.SOAPEnvelope env = null;
                 _messageContext = new org.apache.axis2.context.MessageContext();

                
                                                    //Style is Doc.
                                                    org.wso2.carbon.identity.user.registration.stub.types.axis2.AddUser dummyWrappedType = null;
                                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                                    user28,
                                                                    dummyWrappedType,
                                                                    optimizeContent(new javax.xml.namespace.QName("http://registration.user.identity.carbon.wso2.org",
                                                                    "addUser")));
                                                                

              //adding SOAP soap_headers
         _serviceClient.addHeadersToEnvelope(env);
                // create message context with that soap envelope

            _messageContext.setEnvelope(env);

            // add the message contxt to the operation client
            _operationClient.addMessageContext(_messageContext);

             _operationClient.execute(true);

           
               }catch(org.apache.axis2.AxisFault f){
                  org.apache.axiom.om.OMElement faultElt = f.getDetail();
                  if (faultElt!=null){
                      if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"addUser"))){
                          //make the fault by reflection
                          try{
                              java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"addUser"));
                              java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                              java.lang.Exception ex=
                                      (java.lang.Exception) exceptionClass.newInstance();
                              //message class
                              java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"addUser"));
                              java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                              java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                              java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                         new java.lang.Class[]{messageClass});
                              m.invoke(ex,new java.lang.Object[]{messageObject});
                              
                              if (ex instanceof org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceException){
                                throw (org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceException)ex;
                              }
                              

                              throw new java.rmi.RemoteException(ex.getMessage(), ex);
                          }catch(java.lang.ClassCastException e){
                             // we cannot intantiate the class - throw the original Axis fault
                              throw f;
                          } catch (java.lang.ClassNotFoundException e) {
                              // we cannot intantiate the class - throw the original Axis fault
                              throw f;
                          }catch (java.lang.NoSuchMethodException e) {
                              // we cannot intantiate the class - throw the original Axis fault
                              throw f;
                          } catch (java.lang.reflect.InvocationTargetException e) {
                              // we cannot intantiate the class - throw the original Axis fault
                              throw f;
                          }  catch (java.lang.IllegalAccessException e) {
                              // we cannot intantiate the class - throw the original Axis fault
                              throw f;
                          }   catch (java.lang.InstantiationException e) {
                              // we cannot intantiate the class - throw the original Axis fault
                              throw f;
                          }
                      }else{
                          throw f;
                      }
                  }else{
                      throw f;
                  }
              } finally {
                if (_messageContext.getTransportOut() != null) {
                      _messageContext.getTransportOut().getSender().cleanup(_messageContext);
                }
              }
           
             return;
           }
            
                    /**
                     * Auto generated method signature
                     * 
                     * @see org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminService#isAddUserWithInfoCardEnabled
                     * @param isAddUserWithInfoCardEnabled29
                    
                     * @throws org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceException : 
                     */

                    

                            public  boolean isAddUserWithInfoCardEnabled(

                            )
                        

                    throws java.rmi.RemoteException
                    
                    
                        ,org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceException{
              org.apache.axis2.context.MessageContext _messageContext = null;
              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[1].getName());
              _operationClient.getOptions().setAction("urn:isAddUserWithInfoCardEnabled");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              

              // create a message context
              _messageContext = new org.apache.axis2.context.MessageContext();

              

              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;
                    org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserWithInfoCardEnabled dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("http://registration.user.identity.carbon.wso2.org",
                                                    "isAddUserWithInfoCardEnabled")));
                                                
        //adding SOAP soap_headers
         _serviceClient.addHeadersToEnvelope(env);
        // set the message context with that soap envelope
        _messageContext.setEnvelope(env);

        // add the message contxt to the operation client
        _operationClient.addMessageContext(_messageContext);

        //execute the operation client
        _operationClient.execute(true);

         
               org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient.getMessageContext(
                                           org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();
                
                
                                java.lang.Object object = fromOM(
                                             _returnEnv.getBody().getFirstElement() ,
                                             org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserWithInfoCardEnabledResponse.class,
                                              getEnvelopeNamespaces(_returnEnv));

                               
                                        return getIsAddUserWithInfoCardEnabledResponse_return((org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserWithInfoCardEnabledResponse)object);
                                   
         }catch(org.apache.axis2.AxisFault f){

            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt!=null){
                if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"isAddUserWithInfoCardEnabled"))){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"isAddUserWithInfoCardEnabled"));
                        java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                        java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
                        //message class
                        java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"isAddUserWithInfoCardEnabled"));
                        java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                        java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                        java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                   new java.lang.Class[]{messageClass});
                        m.invoke(ex,new java.lang.Object[]{messageObject});
                        
                        if (ex instanceof org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceException){
                          throw (org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceException)ex;
                        }
                        

                        throw new java.rmi.RemoteException(ex.getMessage(), ex);
                    }catch(java.lang.ClassCastException e){
                       // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.ClassNotFoundException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }catch (java.lang.NoSuchMethodException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }  catch (java.lang.IllegalAccessException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }   catch (java.lang.InstantiationException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }
                }else{
                    throw f;
                }
            }else{
                throw f;
            }
            } finally {
                if (_messageContext.getTransportOut() != null) {
                      _messageContext.getTransportOut().getSender().cleanup(_messageContext);
                }
            }
        }
            
                /**
                * Auto generated method signature for Asynchronous Invocations
                * 
                * @see org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminService#startisAddUserWithInfoCardEnabled
                    * @param isAddUserWithInfoCardEnabled29
                
                */
                public  void startisAddUserWithInfoCardEnabled(

                 

                  final org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceCallbackHandler callback)

                throws java.rmi.RemoteException{

              org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[1].getName());
             _operationClient.getOptions().setAction("urn:isAddUserWithInfoCardEnabled");
             _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              


              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env=null;
              final org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();

                    
                                    //Style is Doc.
                                    org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserWithInfoCardEnabled dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                     dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("http://registration.user.identity.carbon.wso2.org",
                                                    "isAddUserWithInfoCardEnabled")));
                                                
        // adding SOAP soap_headers
         _serviceClient.addHeadersToEnvelope(env);
        // create message context with that soap envelope
        _messageContext.setEnvelope(env);

        // add the message context to the operation client
        _operationClient.addMessageContext(_messageContext);


                    
                        _operationClient.setCallback(new org.apache.axis2.client.async.AxisCallback() {
                            public void onMessage(org.apache.axis2.context.MessageContext resultContext) {
                            try {
                                org.apache.axiom.soap.SOAPEnvelope resultEnv = resultContext.getEnvelope();
                                
                                        java.lang.Object object = fromOM(resultEnv.getBody().getFirstElement(),
                                                                         org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserWithInfoCardEnabledResponse.class,
                                                                         getEnvelopeNamespaces(resultEnv));
                                        callback.receiveResultisAddUserWithInfoCardEnabled(
                                            getIsAddUserWithInfoCardEnabledResponse_return((org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserWithInfoCardEnabledResponse)object));
                                        
                            } catch (org.apache.axis2.AxisFault e) {
                                callback.receiveErrorisAddUserWithInfoCardEnabled(e);
                            }
                            }

                            public void onError(java.lang.Exception error) {
								if (error instanceof org.apache.axis2.AxisFault) {
									org.apache.axis2.AxisFault f = (org.apache.axis2.AxisFault) error;
									org.apache.axiom.om.OMElement faultElt = f.getDetail();
									if (faultElt!=null){
										if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"isAddUserWithInfoCardEnabled"))){
											//make the fault by reflection
											try{
													java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"isAddUserWithInfoCardEnabled"));
													java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
													java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
													//message class
													java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"isAddUserWithInfoCardEnabled"));
														java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
													java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
													java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
															new java.lang.Class[]{messageClass});
													m.invoke(ex,new java.lang.Object[]{messageObject});
													
													if (ex instanceof org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceException){
														callback.receiveErrorisAddUserWithInfoCardEnabled((org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceException)ex);
											            return;
										            }
										            
					
										            callback.receiveErrorisAddUserWithInfoCardEnabled(new java.rmi.RemoteException(ex.getMessage(), ex));
                                            } catch(java.lang.ClassCastException e){
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorisAddUserWithInfoCardEnabled(f);
                                            } catch (java.lang.ClassNotFoundException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorisAddUserWithInfoCardEnabled(f);
                                            } catch (java.lang.NoSuchMethodException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorisAddUserWithInfoCardEnabled(f);
                                            } catch (java.lang.reflect.InvocationTargetException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorisAddUserWithInfoCardEnabled(f);
                                            } catch (java.lang.IllegalAccessException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorisAddUserWithInfoCardEnabled(f);
                                            } catch (java.lang.InstantiationException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorisAddUserWithInfoCardEnabled(f);
                                            } catch (org.apache.axis2.AxisFault e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorisAddUserWithInfoCardEnabled(f);
                                            }
									    } else {
										    callback.receiveErrorisAddUserWithInfoCardEnabled(f);
									    }
									} else {
									    callback.receiveErrorisAddUserWithInfoCardEnabled(f);
									}
								} else {
								    callback.receiveErrorisAddUserWithInfoCardEnabled(error);
								}
                            }

                            public void onFault(org.apache.axis2.context.MessageContext faultContext) {
                                org.apache.axis2.AxisFault fault = org.apache.axis2.util.Utils.getInboundFaultFromMessageContext(faultContext);
                                onError(fault);
                            }

                            public void onComplete() {
                                try {
                                    _messageContext.getTransportOut().getSender().cleanup(_messageContext);
                                } catch (org.apache.axis2.AxisFault axisFault) {
                                    callback.receiveErrorisAddUserWithInfoCardEnabled(axisFault);
                                }
                            }
                });
                        

          org.apache.axis2.util.CallbackReceiver _callbackReceiver = null;
        if ( _operations[1].getMessageReceiver()==null &&  _operationClient.getOptions().isUseSeparateListener()) {
           _callbackReceiver = new org.apache.axis2.util.CallbackReceiver();
          _operations[1].setMessageReceiver(
                    _callbackReceiver);
        }

           //execute the operation client
           _operationClient.execute(false);

                    }
                
                    /**
                     * Auto generated method signature
                     * 
                     * @see org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminService#getPasswordRegularExpressions
                     * @param getPasswordRegularExpressions32
                    
                     * @throws org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceIdentityException : 
                     */

                    

                            public  org.wso2.carbon.identity.user.registration.stub.dto.PasswordRegExDTO[] getPasswordRegularExpressions(

                            )
                        

                    throws java.rmi.RemoteException
                    
                    
                        ,org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceIdentityException{
              org.apache.axis2.context.MessageContext _messageContext = null;
              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[2].getName());
              _operationClient.getOptions().setAction("urn:getPasswordRegularExpressions");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              

              // create a message context
              _messageContext = new org.apache.axis2.context.MessageContext();

              

              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;
                    org.wso2.carbon.identity.user.registration.stub.types.axis2.GetPasswordRegularExpressions dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("http://registration.user.identity.carbon.wso2.org",
                                                    "getPasswordRegularExpressions")));
                                                
        //adding SOAP soap_headers
         _serviceClient.addHeadersToEnvelope(env);
        // set the message context with that soap envelope
        _messageContext.setEnvelope(env);

        // add the message contxt to the operation client
        _operationClient.addMessageContext(_messageContext);

        //execute the operation client
        _operationClient.execute(true);

         
               org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient.getMessageContext(
                                           org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();
                
                
                                java.lang.Object object = fromOM(
                                             _returnEnv.getBody().getFirstElement() ,
                                             org.wso2.carbon.identity.user.registration.stub.types.axis2.GetPasswordRegularExpressionsResponse.class,
                                              getEnvelopeNamespaces(_returnEnv));

                               
                                        return getGetPasswordRegularExpressionsResponse_return((org.wso2.carbon.identity.user.registration.stub.types.axis2.GetPasswordRegularExpressionsResponse)object);
                                   
         }catch(org.apache.axis2.AxisFault f){

            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt!=null){
                if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getPasswordRegularExpressions"))){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getPasswordRegularExpressions"));
                        java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                        java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
                        //message class
                        java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getPasswordRegularExpressions"));
                        java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                        java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                        java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                   new java.lang.Class[]{messageClass});
                        m.invoke(ex,new java.lang.Object[]{messageObject});
                        
                        if (ex instanceof org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceIdentityException){
                          throw (org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceIdentityException)ex;
                        }
                        

                        throw new java.rmi.RemoteException(ex.getMessage(), ex);
                    }catch(java.lang.ClassCastException e){
                       // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.ClassNotFoundException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }catch (java.lang.NoSuchMethodException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }  catch (java.lang.IllegalAccessException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }   catch (java.lang.InstantiationException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }
                }else{
                    throw f;
                }
            }else{
                throw f;
            }
            } finally {
                if (_messageContext.getTransportOut() != null) {
                      _messageContext.getTransportOut().getSender().cleanup(_messageContext);
                }
            }
        }
            
                /**
                * Auto generated method signature for Asynchronous Invocations
                * 
                * @see org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminService#startgetPasswordRegularExpressions
                    * @param getPasswordRegularExpressions32
                
                */
                public  void startgetPasswordRegularExpressions(

                 

                  final org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceCallbackHandler callback)

                throws java.rmi.RemoteException{

              org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[2].getName());
             _operationClient.getOptions().setAction("urn:getPasswordRegularExpressions");
             _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              


              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env=null;
              final org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();

                    
                                    //Style is Doc.
                                    org.wso2.carbon.identity.user.registration.stub.types.axis2.GetPasswordRegularExpressions dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                     dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("http://registration.user.identity.carbon.wso2.org",
                                                    "getPasswordRegularExpressions")));
                                                
        // adding SOAP soap_headers
         _serviceClient.addHeadersToEnvelope(env);
        // create message context with that soap envelope
        _messageContext.setEnvelope(env);

        // add the message context to the operation client
        _operationClient.addMessageContext(_messageContext);


                    
                        _operationClient.setCallback(new org.apache.axis2.client.async.AxisCallback() {
                            public void onMessage(org.apache.axis2.context.MessageContext resultContext) {
                            try {
                                org.apache.axiom.soap.SOAPEnvelope resultEnv = resultContext.getEnvelope();
                                
                                        java.lang.Object object = fromOM(resultEnv.getBody().getFirstElement(),
                                                                         org.wso2.carbon.identity.user.registration.stub.types.axis2.GetPasswordRegularExpressionsResponse.class,
                                                                         getEnvelopeNamespaces(resultEnv));
                                        callback.receiveResultgetPasswordRegularExpressions(
                                            getGetPasswordRegularExpressionsResponse_return((org.wso2.carbon.identity.user.registration.stub.types.axis2.GetPasswordRegularExpressionsResponse)object));
                                        
                            } catch (org.apache.axis2.AxisFault e) {
                                callback.receiveErrorgetPasswordRegularExpressions(e);
                            }
                            }

                            public void onError(java.lang.Exception error) {
								if (error instanceof org.apache.axis2.AxisFault) {
									org.apache.axis2.AxisFault f = (org.apache.axis2.AxisFault) error;
									org.apache.axiom.om.OMElement faultElt = f.getDetail();
									if (faultElt!=null){
										if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getPasswordRegularExpressions"))){
											//make the fault by reflection
											try{
													java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getPasswordRegularExpressions"));
													java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
													java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
													//message class
													java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getPasswordRegularExpressions"));
														java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
													java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
													java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
															new java.lang.Class[]{messageClass});
													m.invoke(ex,new java.lang.Object[]{messageObject});
													
													if (ex instanceof org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceIdentityException){
														callback.receiveErrorgetPasswordRegularExpressions((org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceIdentityException)ex);
											            return;
										            }
										            
					
										            callback.receiveErrorgetPasswordRegularExpressions(new java.rmi.RemoteException(ex.getMessage(), ex));
                                            } catch(java.lang.ClassCastException e){
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetPasswordRegularExpressions(f);
                                            } catch (java.lang.ClassNotFoundException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetPasswordRegularExpressions(f);
                                            } catch (java.lang.NoSuchMethodException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetPasswordRegularExpressions(f);
                                            } catch (java.lang.reflect.InvocationTargetException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetPasswordRegularExpressions(f);
                                            } catch (java.lang.IllegalAccessException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetPasswordRegularExpressions(f);
                                            } catch (java.lang.InstantiationException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetPasswordRegularExpressions(f);
                                            } catch (org.apache.axis2.AxisFault e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetPasswordRegularExpressions(f);
                                            }
									    } else {
										    callback.receiveErrorgetPasswordRegularExpressions(f);
									    }
									} else {
									    callback.receiveErrorgetPasswordRegularExpressions(f);
									}
								} else {
								    callback.receiveErrorgetPasswordRegularExpressions(error);
								}
                            }

                            public void onFault(org.apache.axis2.context.MessageContext faultContext) {
                                org.apache.axis2.AxisFault fault = org.apache.axis2.util.Utils.getInboundFaultFromMessageContext(faultContext);
                                onError(fault);
                            }

                            public void onComplete() {
                                try {
                                    _messageContext.getTransportOut().getSender().cleanup(_messageContext);
                                } catch (org.apache.axis2.AxisFault axisFault) {
                                    callback.receiveErrorgetPasswordRegularExpressions(axisFault);
                                }
                            }
                });
                        

          org.apache.axis2.util.CallbackReceiver _callbackReceiver = null;
        if ( _operations[2].getMessageReceiver()==null &&  _operationClient.getOptions().isUseSeparateListener()) {
           _callbackReceiver = new org.apache.axis2.util.CallbackReceiver();
          _operations[2].setMessageReceiver(
                    _callbackReceiver);
        }

           //execute the operation client
           _operationClient.execute(false);

                    }
                
                    /**
                     * Auto generated method signature
                     * 
                     * @see org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminService#isAddUserWithOpenIDEnabled
                     * @param isAddUserWithOpenIDEnabled35
                    
                     * @throws org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceException : 
                     */

                    

                            public  boolean isAddUserWithOpenIDEnabled(

                            )
                        

                    throws java.rmi.RemoteException
                    
                    
                        ,org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceException{
              org.apache.axis2.context.MessageContext _messageContext = null;
              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[3].getName());
              _operationClient.getOptions().setAction("urn:isAddUserWithOpenIDEnabled");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              

              // create a message context
              _messageContext = new org.apache.axis2.context.MessageContext();

              

              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;
                    org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserWithOpenIDEnabled dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("http://registration.user.identity.carbon.wso2.org",
                                                    "isAddUserWithOpenIDEnabled")));
                                                
        //adding SOAP soap_headers
         _serviceClient.addHeadersToEnvelope(env);
        // set the message context with that soap envelope
        _messageContext.setEnvelope(env);

        // add the message contxt to the operation client
        _operationClient.addMessageContext(_messageContext);

        //execute the operation client
        _operationClient.execute(true);

         
               org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient.getMessageContext(
                                           org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();
                
                
                                java.lang.Object object = fromOM(
                                             _returnEnv.getBody().getFirstElement() ,
                                             org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserWithOpenIDEnabledResponse.class,
                                              getEnvelopeNamespaces(_returnEnv));

                               
                                        return getIsAddUserWithOpenIDEnabledResponse_return((org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserWithOpenIDEnabledResponse)object);
                                   
         }catch(org.apache.axis2.AxisFault f){

            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt!=null){
                if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"isAddUserWithOpenIDEnabled"))){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"isAddUserWithOpenIDEnabled"));
                        java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                        java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
                        //message class
                        java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"isAddUserWithOpenIDEnabled"));
                        java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                        java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                        java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                   new java.lang.Class[]{messageClass});
                        m.invoke(ex,new java.lang.Object[]{messageObject});
                        
                        if (ex instanceof org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceException){
                          throw (org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceException)ex;
                        }
                        

                        throw new java.rmi.RemoteException(ex.getMessage(), ex);
                    }catch(java.lang.ClassCastException e){
                       // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.ClassNotFoundException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }catch (java.lang.NoSuchMethodException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }  catch (java.lang.IllegalAccessException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }   catch (java.lang.InstantiationException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }
                }else{
                    throw f;
                }
            }else{
                throw f;
            }
            } finally {
                if (_messageContext.getTransportOut() != null) {
                      _messageContext.getTransportOut().getSender().cleanup(_messageContext);
                }
            }
        }
            
                /**
                * Auto generated method signature for Asynchronous Invocations
                * 
                * @see org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminService#startisAddUserWithOpenIDEnabled
                    * @param isAddUserWithOpenIDEnabled35
                
                */
                public  void startisAddUserWithOpenIDEnabled(

                 

                  final org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceCallbackHandler callback)

                throws java.rmi.RemoteException{

              org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[3].getName());
             _operationClient.getOptions().setAction("urn:isAddUserWithOpenIDEnabled");
             _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              


              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env=null;
              final org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();

                    
                                    //Style is Doc.
                                    org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserWithOpenIDEnabled dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                     dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("http://registration.user.identity.carbon.wso2.org",
                                                    "isAddUserWithOpenIDEnabled")));
                                                
        // adding SOAP soap_headers
         _serviceClient.addHeadersToEnvelope(env);
        // create message context with that soap envelope
        _messageContext.setEnvelope(env);

        // add the message context to the operation client
        _operationClient.addMessageContext(_messageContext);


                    
                        _operationClient.setCallback(new org.apache.axis2.client.async.AxisCallback() {
                            public void onMessage(org.apache.axis2.context.MessageContext resultContext) {
                            try {
                                org.apache.axiom.soap.SOAPEnvelope resultEnv = resultContext.getEnvelope();
                                
                                        java.lang.Object object = fromOM(resultEnv.getBody().getFirstElement(),
                                                                         org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserWithOpenIDEnabledResponse.class,
                                                                         getEnvelopeNamespaces(resultEnv));
                                        callback.receiveResultisAddUserWithOpenIDEnabled(
                                            getIsAddUserWithOpenIDEnabledResponse_return((org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserWithOpenIDEnabledResponse)object));
                                        
                            } catch (org.apache.axis2.AxisFault e) {
                                callback.receiveErrorisAddUserWithOpenIDEnabled(e);
                            }
                            }

                            public void onError(java.lang.Exception error) {
								if (error instanceof org.apache.axis2.AxisFault) {
									org.apache.axis2.AxisFault f = (org.apache.axis2.AxisFault) error;
									org.apache.axiom.om.OMElement faultElt = f.getDetail();
									if (faultElt!=null){
										if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"isAddUserWithOpenIDEnabled"))){
											//make the fault by reflection
											try{
													java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"isAddUserWithOpenIDEnabled"));
													java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
													java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
													//message class
													java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"isAddUserWithOpenIDEnabled"));
														java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
													java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
													java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
															new java.lang.Class[]{messageClass});
													m.invoke(ex,new java.lang.Object[]{messageObject});
													
													if (ex instanceof org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceException){
														callback.receiveErrorisAddUserWithOpenIDEnabled((org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceException)ex);
											            return;
										            }
										            
					
										            callback.receiveErrorisAddUserWithOpenIDEnabled(new java.rmi.RemoteException(ex.getMessage(), ex));
                                            } catch(java.lang.ClassCastException e){
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorisAddUserWithOpenIDEnabled(f);
                                            } catch (java.lang.ClassNotFoundException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorisAddUserWithOpenIDEnabled(f);
                                            } catch (java.lang.NoSuchMethodException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorisAddUserWithOpenIDEnabled(f);
                                            } catch (java.lang.reflect.InvocationTargetException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorisAddUserWithOpenIDEnabled(f);
                                            } catch (java.lang.IllegalAccessException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorisAddUserWithOpenIDEnabled(f);
                                            } catch (java.lang.InstantiationException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorisAddUserWithOpenIDEnabled(f);
                                            } catch (org.apache.axis2.AxisFault e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorisAddUserWithOpenIDEnabled(f);
                                            }
									    } else {
										    callback.receiveErrorisAddUserWithOpenIDEnabled(f);
									    }
									} else {
									    callback.receiveErrorisAddUserWithOpenIDEnabled(f);
									}
								} else {
								    callback.receiveErrorisAddUserWithOpenIDEnabled(error);
								}
                            }

                            public void onFault(org.apache.axis2.context.MessageContext faultContext) {
                                org.apache.axis2.AxisFault fault = org.apache.axis2.util.Utils.getInboundFaultFromMessageContext(faultContext);
                                onError(fault);
                            }

                            public void onComplete() {
                                try {
                                    _messageContext.getTransportOut().getSender().cleanup(_messageContext);
                                } catch (org.apache.axis2.AxisFault axisFault) {
                                    callback.receiveErrorisAddUserWithOpenIDEnabled(axisFault);
                                }
                            }
                });
                        

          org.apache.axis2.util.CallbackReceiver _callbackReceiver = null;
        if ( _operations[3].getMessageReceiver()==null &&  _operationClient.getOptions().isUseSeparateListener()) {
           _callbackReceiver = new org.apache.axis2.util.CallbackReceiver();
          _operations[3].setMessageReceiver(
                    _callbackReceiver);
        }

           //execute the operation client
           _operationClient.execute(false);

                    }
                
                    /**
                     * Auto generated method signature
                     * 
                     * @see org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminService#isAddUserEnabled
                     * @param isAddUserEnabled38
                    
                     * @throws org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceException : 
                     */

                    

                            public  boolean isAddUserEnabled(

                            )
                        

                    throws java.rmi.RemoteException
                    
                    
                        ,org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceException{
              org.apache.axis2.context.MessageContext _messageContext = null;
              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[4].getName());
              _operationClient.getOptions().setAction("urn:isAddUserEnabled");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              

              // create a message context
              _messageContext = new org.apache.axis2.context.MessageContext();

              

              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;
                    org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserEnabled dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("http://registration.user.identity.carbon.wso2.org",
                                                    "isAddUserEnabled")));
                                                
        //adding SOAP soap_headers
         _serviceClient.addHeadersToEnvelope(env);
        // set the message context with that soap envelope
        _messageContext.setEnvelope(env);

        // add the message contxt to the operation client
        _operationClient.addMessageContext(_messageContext);

        //execute the operation client
        _operationClient.execute(true);

         
               org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient.getMessageContext(
                                           org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();
                
                
                                java.lang.Object object = fromOM(
                                             _returnEnv.getBody().getFirstElement() ,
                                             org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserEnabledResponse.class,
                                              getEnvelopeNamespaces(_returnEnv));

                               
                                        return getIsAddUserEnabledResponse_return((org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserEnabledResponse)object);
                                   
         }catch(org.apache.axis2.AxisFault f){

            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt!=null){
                if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"isAddUserEnabled"))){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"isAddUserEnabled"));
                        java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                        java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
                        //message class
                        java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"isAddUserEnabled"));
                        java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                        java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                        java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                   new java.lang.Class[]{messageClass});
                        m.invoke(ex,new java.lang.Object[]{messageObject});
                        
                        if (ex instanceof org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceException){
                          throw (org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceException)ex;
                        }
                        

                        throw new java.rmi.RemoteException(ex.getMessage(), ex);
                    }catch(java.lang.ClassCastException e){
                       // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.ClassNotFoundException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }catch (java.lang.NoSuchMethodException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }  catch (java.lang.IllegalAccessException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }   catch (java.lang.InstantiationException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }
                }else{
                    throw f;
                }
            }else{
                throw f;
            }
            } finally {
                if (_messageContext.getTransportOut() != null) {
                      _messageContext.getTransportOut().getSender().cleanup(_messageContext);
                }
            }
        }
            
                /**
                * Auto generated method signature for Asynchronous Invocations
                * 
                * @see org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminService#startisAddUserEnabled
                    * @param isAddUserEnabled38
                
                */
                public  void startisAddUserEnabled(

                 

                  final org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceCallbackHandler callback)

                throws java.rmi.RemoteException{

              org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[4].getName());
             _operationClient.getOptions().setAction("urn:isAddUserEnabled");
             _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              


              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env=null;
              final org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();

                    
                                    //Style is Doc.
                                    org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserEnabled dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                     dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("http://registration.user.identity.carbon.wso2.org",
                                                    "isAddUserEnabled")));
                                                
        // adding SOAP soap_headers
         _serviceClient.addHeadersToEnvelope(env);
        // create message context with that soap envelope
        _messageContext.setEnvelope(env);

        // add the message context to the operation client
        _operationClient.addMessageContext(_messageContext);


                    
                        _operationClient.setCallback(new org.apache.axis2.client.async.AxisCallback() {
                            public void onMessage(org.apache.axis2.context.MessageContext resultContext) {
                            try {
                                org.apache.axiom.soap.SOAPEnvelope resultEnv = resultContext.getEnvelope();
                                
                                        java.lang.Object object = fromOM(resultEnv.getBody().getFirstElement(),
                                                                         org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserEnabledResponse.class,
                                                                         getEnvelopeNamespaces(resultEnv));
                                        callback.receiveResultisAddUserEnabled(
                                            getIsAddUserEnabledResponse_return((org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserEnabledResponse)object));
                                        
                            } catch (org.apache.axis2.AxisFault e) {
                                callback.receiveErrorisAddUserEnabled(e);
                            }
                            }

                            public void onError(java.lang.Exception error) {
								if (error instanceof org.apache.axis2.AxisFault) {
									org.apache.axis2.AxisFault f = (org.apache.axis2.AxisFault) error;
									org.apache.axiom.om.OMElement faultElt = f.getDetail();
									if (faultElt!=null){
										if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"isAddUserEnabled"))){
											//make the fault by reflection
											try{
													java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"isAddUserEnabled"));
													java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
													java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
													//message class
													java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"isAddUserEnabled"));
														java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
													java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
													java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
															new java.lang.Class[]{messageClass});
													m.invoke(ex,new java.lang.Object[]{messageObject});
													
													if (ex instanceof org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceException){
														callback.receiveErrorisAddUserEnabled((org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceException)ex);
											            return;
										            }
										            
					
										            callback.receiveErrorisAddUserEnabled(new java.rmi.RemoteException(ex.getMessage(), ex));
                                            } catch(java.lang.ClassCastException e){
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorisAddUserEnabled(f);
                                            } catch (java.lang.ClassNotFoundException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorisAddUserEnabled(f);
                                            } catch (java.lang.NoSuchMethodException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorisAddUserEnabled(f);
                                            } catch (java.lang.reflect.InvocationTargetException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorisAddUserEnabled(f);
                                            } catch (java.lang.IllegalAccessException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorisAddUserEnabled(f);
                                            } catch (java.lang.InstantiationException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorisAddUserEnabled(f);
                                            } catch (org.apache.axis2.AxisFault e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorisAddUserEnabled(f);
                                            }
									    } else {
										    callback.receiveErrorisAddUserEnabled(f);
									    }
									} else {
									    callback.receiveErrorisAddUserEnabled(f);
									}
								} else {
								    callback.receiveErrorisAddUserEnabled(error);
								}
                            }

                            public void onFault(org.apache.axis2.context.MessageContext faultContext) {
                                org.apache.axis2.AxisFault fault = org.apache.axis2.util.Utils.getInboundFaultFromMessageContext(faultContext);
                                onError(fault);
                            }

                            public void onComplete() {
                                try {
                                    _messageContext.getTransportOut().getSender().cleanup(_messageContext);
                                } catch (org.apache.axis2.AxisFault axisFault) {
                                    callback.receiveErrorisAddUserEnabled(axisFault);
                                }
                            }
                });
                        

          org.apache.axis2.util.CallbackReceiver _callbackReceiver = null;
        if ( _operations[4].getMessageReceiver()==null &&  _operationClient.getOptions().isUseSeparateListener()) {
           _callbackReceiver = new org.apache.axis2.util.CallbackReceiver();
          _operations[4].setMessageReceiver(
                    _callbackReceiver);
        }

           //execute the operation client
           _operationClient.execute(false);

                    }
                
                    /**
                     * Auto generated method signature
                     * 
                     * @see org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminService#isUserExist
                     * @param isUserExist41
                    
                     * @throws org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceUserRegistrationException : 
                     */

                    

                            public  boolean isUserExist(

                            java.lang.String username42)
                        

                    throws java.rmi.RemoteException
                    
                    
                        ,org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceUserRegistrationException{
              org.apache.axis2.context.MessageContext _messageContext = null;
              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[5].getName());
              _operationClient.getOptions().setAction("urn:isUserExist");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              

              // create a message context
              _messageContext = new org.apache.axis2.context.MessageContext();

              

              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;
                    org.wso2.carbon.identity.user.registration.stub.types.axis2.IsUserExist dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    username42,
                                                    dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("http://registration.user.identity.carbon.wso2.org",
                                                    "isUserExist")));
                                                
        //adding SOAP soap_headers
         _serviceClient.addHeadersToEnvelope(env);
        // set the message context with that soap envelope
        _messageContext.setEnvelope(env);

        // add the message contxt to the operation client
        _operationClient.addMessageContext(_messageContext);

        //execute the operation client
        _operationClient.execute(true);

         
               org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient.getMessageContext(
                                           org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();
                
                
                                java.lang.Object object = fromOM(
                                             _returnEnv.getBody().getFirstElement() ,
                                             org.wso2.carbon.identity.user.registration.stub.types.axis2.IsUserExistResponse.class,
                                              getEnvelopeNamespaces(_returnEnv));

                               
                                        return getIsUserExistResponse_return((org.wso2.carbon.identity.user.registration.stub.types.axis2.IsUserExistResponse)object);
                                   
         }catch(org.apache.axis2.AxisFault f){

            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt!=null){
                if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"isUserExist"))){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"isUserExist"));
                        java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                        java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
                        //message class
                        java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"isUserExist"));
                        java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                        java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                        java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                   new java.lang.Class[]{messageClass});
                        m.invoke(ex,new java.lang.Object[]{messageObject});
                        
                        if (ex instanceof org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceUserRegistrationException){
                          throw (org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceUserRegistrationException)ex;
                        }
                        

                        throw new java.rmi.RemoteException(ex.getMessage(), ex);
                    }catch(java.lang.ClassCastException e){
                       // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.ClassNotFoundException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }catch (java.lang.NoSuchMethodException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }  catch (java.lang.IllegalAccessException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }   catch (java.lang.InstantiationException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }
                }else{
                    throw f;
                }
            }else{
                throw f;
            }
            } finally {
                if (_messageContext.getTransportOut() != null) {
                      _messageContext.getTransportOut().getSender().cleanup(_messageContext);
                }
            }
        }
            
                /**
                * Auto generated method signature for Asynchronous Invocations
                * 
                * @see org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminService#startisUserExist
                    * @param isUserExist41
                
                */
                public  void startisUserExist(

                 java.lang.String username42,

                  final org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceCallbackHandler callback)

                throws java.rmi.RemoteException{

              org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[5].getName());
             _operationClient.getOptions().setAction("urn:isUserExist");
             _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              


              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env=null;
              final org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();

                    
                                    //Style is Doc.
                                    org.wso2.carbon.identity.user.registration.stub.types.axis2.IsUserExist dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    username42,
                                                     dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("http://registration.user.identity.carbon.wso2.org",
                                                    "isUserExist")));
                                                
        // adding SOAP soap_headers
         _serviceClient.addHeadersToEnvelope(env);
        // create message context with that soap envelope
        _messageContext.setEnvelope(env);

        // add the message context to the operation client
        _operationClient.addMessageContext(_messageContext);


                    
                        _operationClient.setCallback(new org.apache.axis2.client.async.AxisCallback() {
                            public void onMessage(org.apache.axis2.context.MessageContext resultContext) {
                            try {
                                org.apache.axiom.soap.SOAPEnvelope resultEnv = resultContext.getEnvelope();
                                
                                        java.lang.Object object = fromOM(resultEnv.getBody().getFirstElement(),
                                                                         org.wso2.carbon.identity.user.registration.stub.types.axis2.IsUserExistResponse.class,
                                                                         getEnvelopeNamespaces(resultEnv));
                                        callback.receiveResultisUserExist(
                                            getIsUserExistResponse_return((org.wso2.carbon.identity.user.registration.stub.types.axis2.IsUserExistResponse)object));
                                        
                            } catch (org.apache.axis2.AxisFault e) {
                                callback.receiveErrorisUserExist(e);
                            }
                            }

                            public void onError(java.lang.Exception error) {
								if (error instanceof org.apache.axis2.AxisFault) {
									org.apache.axis2.AxisFault f = (org.apache.axis2.AxisFault) error;
									org.apache.axiom.om.OMElement faultElt = f.getDetail();
									if (faultElt!=null){
										if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"isUserExist"))){
											//make the fault by reflection
											try{
													java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"isUserExist"));
													java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
													java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
													//message class
													java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"isUserExist"));
														java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
													java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
													java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
															new java.lang.Class[]{messageClass});
													m.invoke(ex,new java.lang.Object[]{messageObject});
													
													if (ex instanceof org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceUserRegistrationException){
														callback.receiveErrorisUserExist((org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceUserRegistrationException)ex);
											            return;
										            }
										            
					
										            callback.receiveErrorisUserExist(new java.rmi.RemoteException(ex.getMessage(), ex));
                                            } catch(java.lang.ClassCastException e){
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorisUserExist(f);
                                            } catch (java.lang.ClassNotFoundException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorisUserExist(f);
                                            } catch (java.lang.NoSuchMethodException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorisUserExist(f);
                                            } catch (java.lang.reflect.InvocationTargetException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorisUserExist(f);
                                            } catch (java.lang.IllegalAccessException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorisUserExist(f);
                                            } catch (java.lang.InstantiationException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorisUserExist(f);
                                            } catch (org.apache.axis2.AxisFault e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorisUserExist(f);
                                            }
									    } else {
										    callback.receiveErrorisUserExist(f);
									    }
									} else {
									    callback.receiveErrorisUserExist(f);
									}
								} else {
								    callback.receiveErrorisUserExist(error);
								}
                            }

                            public void onFault(org.apache.axis2.context.MessageContext faultContext) {
                                org.apache.axis2.AxisFault fault = org.apache.axis2.util.Utils.getInboundFaultFromMessageContext(faultContext);
                                onError(fault);
                            }

                            public void onComplete() {
                                try {
                                    _messageContext.getTransportOut().getSender().cleanup(_messageContext);
                                } catch (org.apache.axis2.AxisFault axisFault) {
                                    callback.receiveErrorisUserExist(axisFault);
                                }
                            }
                });
                        

          org.apache.axis2.util.CallbackReceiver _callbackReceiver = null;
        if ( _operations[5].getMessageReceiver()==null &&  _operationClient.getOptions().isUseSeparateListener()) {
           _callbackReceiver = new org.apache.axis2.util.CallbackReceiver();
          _operations[5].setMessageReceiver(
                    _callbackReceiver);
        }

           //execute the operation client
           _operationClient.execute(false);

                    }
                
                    /**
                     * Auto generated method signature
                     * 
                     * @see org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminService#readUserFieldsForUserRegistration
                     * @param readUserFieldsForUserRegistration45
                    
                     * @throws org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceIdentityException : 
                     */

                    

                            public  org.wso2.carbon.identity.user.registration.stub.dto.UserFieldDTO[] readUserFieldsForUserRegistration(

                            java.lang.String dialect46)
                        

                    throws java.rmi.RemoteException
                    
                    
                        ,org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceIdentityException{
              org.apache.axis2.context.MessageContext _messageContext = null;
              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[6].getName());
              _operationClient.getOptions().setAction("urn:readUserFieldsForUserRegistration");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              

              // create a message context
              _messageContext = new org.apache.axis2.context.MessageContext();

              

              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;
                    org.wso2.carbon.identity.user.registration.stub.types.axis2.ReadUserFieldsForUserRegistration dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    dialect46,
                                                    dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("http://registration.user.identity.carbon.wso2.org",
                                                    "readUserFieldsForUserRegistration")));
                                                
        //adding SOAP soap_headers
         _serviceClient.addHeadersToEnvelope(env);
        // set the message context with that soap envelope
        _messageContext.setEnvelope(env);

        // add the message contxt to the operation client
        _operationClient.addMessageContext(_messageContext);

        //execute the operation client
        _operationClient.execute(true);

         
               org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient.getMessageContext(
                                           org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();
                
                
                                java.lang.Object object = fromOM(
                                             _returnEnv.getBody().getFirstElement() ,
                                             org.wso2.carbon.identity.user.registration.stub.types.axis2.ReadUserFieldsForUserRegistrationResponse.class,
                                              getEnvelopeNamespaces(_returnEnv));

                               
                                        return getReadUserFieldsForUserRegistrationResponse_return((org.wso2.carbon.identity.user.registration.stub.types.axis2.ReadUserFieldsForUserRegistrationResponse)object);
                                   
         }catch(org.apache.axis2.AxisFault f){

            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt!=null){
                if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"readUserFieldsForUserRegistration"))){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"readUserFieldsForUserRegistration"));
                        java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                        java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
                        //message class
                        java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"readUserFieldsForUserRegistration"));
                        java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                        java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                        java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                   new java.lang.Class[]{messageClass});
                        m.invoke(ex,new java.lang.Object[]{messageObject});
                        
                        if (ex instanceof org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceIdentityException){
                          throw (org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceIdentityException)ex;
                        }
                        

                        throw new java.rmi.RemoteException(ex.getMessage(), ex);
                    }catch(java.lang.ClassCastException e){
                       // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.ClassNotFoundException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }catch (java.lang.NoSuchMethodException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }  catch (java.lang.IllegalAccessException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }   catch (java.lang.InstantiationException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }
                }else{
                    throw f;
                }
            }else{
                throw f;
            }
            } finally {
                if (_messageContext.getTransportOut() != null) {
                      _messageContext.getTransportOut().getSender().cleanup(_messageContext);
                }
            }
        }
            
                /**
                * Auto generated method signature for Asynchronous Invocations
                * 
                * @see org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminService#startreadUserFieldsForUserRegistration
                    * @param readUserFieldsForUserRegistration45
                
                */
                public  void startreadUserFieldsForUserRegistration(

                 java.lang.String dialect46,

                  final org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceCallbackHandler callback)

                throws java.rmi.RemoteException{

              org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[6].getName());
             _operationClient.getOptions().setAction("urn:readUserFieldsForUserRegistration");
             _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              


              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env=null;
              final org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();

                    
                                    //Style is Doc.
                                    org.wso2.carbon.identity.user.registration.stub.types.axis2.ReadUserFieldsForUserRegistration dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    dialect46,
                                                     dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("http://registration.user.identity.carbon.wso2.org",
                                                    "readUserFieldsForUserRegistration")));
                                                
        // adding SOAP soap_headers
         _serviceClient.addHeadersToEnvelope(env);
        // create message context with that soap envelope
        _messageContext.setEnvelope(env);

        // add the message context to the operation client
        _operationClient.addMessageContext(_messageContext);


                    
                        _operationClient.setCallback(new org.apache.axis2.client.async.AxisCallback() {
                            public void onMessage(org.apache.axis2.context.MessageContext resultContext) {
                            try {
                                org.apache.axiom.soap.SOAPEnvelope resultEnv = resultContext.getEnvelope();
                                
                                        java.lang.Object object = fromOM(resultEnv.getBody().getFirstElement(),
                                                                         org.wso2.carbon.identity.user.registration.stub.types.axis2.ReadUserFieldsForUserRegistrationResponse.class,
                                                                         getEnvelopeNamespaces(resultEnv));
                                        callback.receiveResultreadUserFieldsForUserRegistration(
                                            getReadUserFieldsForUserRegistrationResponse_return((org.wso2.carbon.identity.user.registration.stub.types.axis2.ReadUserFieldsForUserRegistrationResponse)object));
                                        
                            } catch (org.apache.axis2.AxisFault e) {
                                callback.receiveErrorreadUserFieldsForUserRegistration(e);
                            }
                            }

                            public void onError(java.lang.Exception error) {
								if (error instanceof org.apache.axis2.AxisFault) {
									org.apache.axis2.AxisFault f = (org.apache.axis2.AxisFault) error;
									org.apache.axiom.om.OMElement faultElt = f.getDetail();
									if (faultElt!=null){
										if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"readUserFieldsForUserRegistration"))){
											//make the fault by reflection
											try{
													java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"readUserFieldsForUserRegistration"));
													java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
													java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
													//message class
													java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"readUserFieldsForUserRegistration"));
														java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
													java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
													java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
															new java.lang.Class[]{messageClass});
													m.invoke(ex,new java.lang.Object[]{messageObject});
													
													if (ex instanceof org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceIdentityException){
														callback.receiveErrorreadUserFieldsForUserRegistration((org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceIdentityException)ex);
											            return;
										            }
										            
					
										            callback.receiveErrorreadUserFieldsForUserRegistration(new java.rmi.RemoteException(ex.getMessage(), ex));
                                            } catch(java.lang.ClassCastException e){
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorreadUserFieldsForUserRegistration(f);
                                            } catch (java.lang.ClassNotFoundException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorreadUserFieldsForUserRegistration(f);
                                            } catch (java.lang.NoSuchMethodException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorreadUserFieldsForUserRegistration(f);
                                            } catch (java.lang.reflect.InvocationTargetException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorreadUserFieldsForUserRegistration(f);
                                            } catch (java.lang.IllegalAccessException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorreadUserFieldsForUserRegistration(f);
                                            } catch (java.lang.InstantiationException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorreadUserFieldsForUserRegistration(f);
                                            } catch (org.apache.axis2.AxisFault e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorreadUserFieldsForUserRegistration(f);
                                            }
									    } else {
										    callback.receiveErrorreadUserFieldsForUserRegistration(f);
									    }
									} else {
									    callback.receiveErrorreadUserFieldsForUserRegistration(f);
									}
								} else {
								    callback.receiveErrorreadUserFieldsForUserRegistration(error);
								}
                            }

                            public void onFault(org.apache.axis2.context.MessageContext faultContext) {
                                org.apache.axis2.AxisFault fault = org.apache.axis2.util.Utils.getInboundFaultFromMessageContext(faultContext);
                                onError(fault);
                            }

                            public void onComplete() {
                                try {
                                    _messageContext.getTransportOut().getSender().cleanup(_messageContext);
                                } catch (org.apache.axis2.AxisFault axisFault) {
                                    callback.receiveErrorreadUserFieldsForUserRegistration(axisFault);
                                }
                            }
                });
                        

          org.apache.axis2.util.CallbackReceiver _callbackReceiver = null;
        if ( _operations[6].getMessageReceiver()==null &&  _operationClient.getOptions().isUseSeparateListener()) {
           _callbackReceiver = new org.apache.axis2.util.CallbackReceiver();
          _operations[6].setMessageReceiver(
                    _callbackReceiver);
        }

           //execute the operation client
           _operationClient.execute(false);

                    }
                


       /**
        *  A utility method that copies the namepaces from the SOAPEnvelope
        */
       private java.util.Map getEnvelopeNamespaces(org.apache.axiom.soap.SOAPEnvelope env){
        java.util.Map returnMap = new java.util.HashMap();
        java.util.Iterator namespaceIterator = env.getAllDeclaredNamespaces();
        while (namespaceIterator.hasNext()) {
            org.apache.axiom.om.OMNamespace ns = (org.apache.axiom.om.OMNamespace) namespaceIterator.next();
            returnMap.put(ns.getPrefix(),ns.getNamespaceURI());
        }
       return returnMap;
    }

    
    
    private javax.xml.namespace.QName[] opNameArray = null;
    private boolean optimizeContent(javax.xml.namespace.QName opName) {
        

        if (opNameArray == null) {
            return false;
        }
        for (int i = 0; i < opNameArray.length; i++) {
            if (opName.equals(opNameArray[i])) {
                return true;   
            }
        }
        return false;
    }
     //https://localhost:9443/services/UserRegistrationAdminService.UserRegistrationAdminServiceHttpsSoap12Endpoint/
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.user.registration.stub.types.axis2.AddUser param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.user.registration.stub.types.axis2.AddUser.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.user.registration.stub.types.axis2.UserRegistrationAdminServiceException param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.user.registration.stub.types.axis2.UserRegistrationAdminServiceException.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserWithInfoCardEnabled param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserWithInfoCardEnabled.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserWithInfoCardEnabledResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserWithInfoCardEnabledResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.user.registration.stub.types.axis2.GetPasswordRegularExpressions param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.user.registration.stub.types.axis2.GetPasswordRegularExpressions.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.user.registration.stub.types.axis2.GetPasswordRegularExpressionsResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.user.registration.stub.types.axis2.GetPasswordRegularExpressionsResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.user.registration.stub.types.axis2.UserRegistrationAdminServiceIdentityException param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.user.registration.stub.types.axis2.UserRegistrationAdminServiceIdentityException.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserWithOpenIDEnabled param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserWithOpenIDEnabled.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserWithOpenIDEnabledResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserWithOpenIDEnabledResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserEnabled param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserEnabled.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserEnabledResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserEnabledResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.user.registration.stub.types.axis2.IsUserExist param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.user.registration.stub.types.axis2.IsUserExist.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.user.registration.stub.types.axis2.IsUserExistResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.user.registration.stub.types.axis2.IsUserExistResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.user.registration.stub.types.axis2.UserRegistrationAdminServiceUserRegistrationException param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.user.registration.stub.types.axis2.UserRegistrationAdminServiceUserRegistrationException.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.user.registration.stub.types.axis2.ReadUserFieldsForUserRegistration param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.user.registration.stub.types.axis2.ReadUserFieldsForUserRegistration.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.user.registration.stub.types.axis2.ReadUserFieldsForUserRegistrationResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.user.registration.stub.types.axis2.ReadUserFieldsForUserRegistrationResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    org.wso2.carbon.identity.user.registration.stub.dto.UserDTO param1,
                                    org.wso2.carbon.identity.user.registration.stub.types.axis2.AddUser dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.identity.user.registration.stub.types.axis2.AddUser wrappedType = new org.wso2.carbon.identity.user.registration.stub.types.axis2.AddUser();

                                 
                                              wrappedType.setUser(param1);
                                         

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.identity.user.registration.stub.types.axis2.AddUser.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserWithInfoCardEnabled dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserWithInfoCardEnabled wrappedType = new org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserWithInfoCardEnabled();

                                 

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserWithInfoCardEnabled.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             

                                
                                private boolean getIsAddUserWithInfoCardEnabledResponse_return(
                                org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserWithInfoCardEnabledResponse wrappedType){
                                
                                        return wrappedType.get_return();
                                    
                                }
                             
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    org.wso2.carbon.identity.user.registration.stub.types.axis2.GetPasswordRegularExpressions dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.identity.user.registration.stub.types.axis2.GetPasswordRegularExpressions wrappedType = new org.wso2.carbon.identity.user.registration.stub.types.axis2.GetPasswordRegularExpressions();

                                 

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.identity.user.registration.stub.types.axis2.GetPasswordRegularExpressions.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             

                                
                                private org.wso2.carbon.identity.user.registration.stub.dto.PasswordRegExDTO[] getGetPasswordRegularExpressionsResponse_return(
                                org.wso2.carbon.identity.user.registration.stub.types.axis2.GetPasswordRegularExpressionsResponse wrappedType){
                                
                                        return wrappedType.get_return();
                                    
                                }
                             
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserWithOpenIDEnabled dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserWithOpenIDEnabled wrappedType = new org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserWithOpenIDEnabled();

                                 

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserWithOpenIDEnabled.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             

                                
                                private boolean getIsAddUserWithOpenIDEnabledResponse_return(
                                org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserWithOpenIDEnabledResponse wrappedType){
                                
                                        return wrappedType.get_return();
                                    
                                }
                             
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserEnabled dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserEnabled wrappedType = new org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserEnabled();

                                 

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserEnabled.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             

                                
                                private boolean getIsAddUserEnabledResponse_return(
                                org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserEnabledResponse wrappedType){
                                
                                        return wrappedType.get_return();
                                    
                                }
                             
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    java.lang.String param1,
                                    org.wso2.carbon.identity.user.registration.stub.types.axis2.IsUserExist dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.identity.user.registration.stub.types.axis2.IsUserExist wrappedType = new org.wso2.carbon.identity.user.registration.stub.types.axis2.IsUserExist();

                                 
                                              wrappedType.setUsername(param1);
                                         

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.identity.user.registration.stub.types.axis2.IsUserExist.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             

                                
                                private boolean getIsUserExistResponse_return(
                                org.wso2.carbon.identity.user.registration.stub.types.axis2.IsUserExistResponse wrappedType){
                                
                                        return wrappedType.get_return();
                                    
                                }
                             
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    java.lang.String param1,
                                    org.wso2.carbon.identity.user.registration.stub.types.axis2.ReadUserFieldsForUserRegistration dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.identity.user.registration.stub.types.axis2.ReadUserFieldsForUserRegistration wrappedType = new org.wso2.carbon.identity.user.registration.stub.types.axis2.ReadUserFieldsForUserRegistration();

                                 
                                              wrappedType.setDialect(param1);
                                         

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.identity.user.registration.stub.types.axis2.ReadUserFieldsForUserRegistration.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             

                                
                                private org.wso2.carbon.identity.user.registration.stub.dto.UserFieldDTO[] getReadUserFieldsForUserRegistrationResponse_return(
                                org.wso2.carbon.identity.user.registration.stub.types.axis2.ReadUserFieldsForUserRegistrationResponse wrappedType){
                                
                                        return wrappedType.get_return();
                                    
                                }
                             


        /**
        *  get the default envelope
        */
        private org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory){
        return factory.getDefaultEnvelope();
        }


        private  java.lang.Object fromOM(
        org.apache.axiom.om.OMElement param,
        java.lang.Class type,
        java.util.Map extraNamespaces) throws org.apache.axis2.AxisFault{

        try {
        
                if (org.wso2.carbon.identity.user.registration.stub.types.axis2.AddUser.class.equals(type)){
                
                           return org.wso2.carbon.identity.user.registration.stub.types.axis2.AddUser.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.user.registration.stub.types.axis2.UserRegistrationAdminServiceException.class.equals(type)){
                
                           return org.wso2.carbon.identity.user.registration.stub.types.axis2.UserRegistrationAdminServiceException.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserWithInfoCardEnabled.class.equals(type)){
                
                           return org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserWithInfoCardEnabled.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserWithInfoCardEnabledResponse.class.equals(type)){
                
                           return org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserWithInfoCardEnabledResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.user.registration.stub.types.axis2.UserRegistrationAdminServiceException.class.equals(type)){
                
                           return org.wso2.carbon.identity.user.registration.stub.types.axis2.UserRegistrationAdminServiceException.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.user.registration.stub.types.axis2.GetPasswordRegularExpressions.class.equals(type)){
                
                           return org.wso2.carbon.identity.user.registration.stub.types.axis2.GetPasswordRegularExpressions.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.user.registration.stub.types.axis2.GetPasswordRegularExpressionsResponse.class.equals(type)){
                
                           return org.wso2.carbon.identity.user.registration.stub.types.axis2.GetPasswordRegularExpressionsResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.user.registration.stub.types.axis2.UserRegistrationAdminServiceIdentityException.class.equals(type)){
                
                           return org.wso2.carbon.identity.user.registration.stub.types.axis2.UserRegistrationAdminServiceIdentityException.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserWithOpenIDEnabled.class.equals(type)){
                
                           return org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserWithOpenIDEnabled.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserWithOpenIDEnabledResponse.class.equals(type)){
                
                           return org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserWithOpenIDEnabledResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.user.registration.stub.types.axis2.UserRegistrationAdminServiceException.class.equals(type)){
                
                           return org.wso2.carbon.identity.user.registration.stub.types.axis2.UserRegistrationAdminServiceException.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserEnabled.class.equals(type)){
                
                           return org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserEnabled.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserEnabledResponse.class.equals(type)){
                
                           return org.wso2.carbon.identity.user.registration.stub.types.axis2.IsAddUserEnabledResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.user.registration.stub.types.axis2.UserRegistrationAdminServiceException.class.equals(type)){
                
                           return org.wso2.carbon.identity.user.registration.stub.types.axis2.UserRegistrationAdminServiceException.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.user.registration.stub.types.axis2.IsUserExist.class.equals(type)){
                
                           return org.wso2.carbon.identity.user.registration.stub.types.axis2.IsUserExist.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.user.registration.stub.types.axis2.IsUserExistResponse.class.equals(type)){
                
                           return org.wso2.carbon.identity.user.registration.stub.types.axis2.IsUserExistResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.user.registration.stub.types.axis2.UserRegistrationAdminServiceUserRegistrationException.class.equals(type)){
                
                           return org.wso2.carbon.identity.user.registration.stub.types.axis2.UserRegistrationAdminServiceUserRegistrationException.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.user.registration.stub.types.axis2.ReadUserFieldsForUserRegistration.class.equals(type)){
                
                           return org.wso2.carbon.identity.user.registration.stub.types.axis2.ReadUserFieldsForUserRegistration.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.user.registration.stub.types.axis2.ReadUserFieldsForUserRegistrationResponse.class.equals(type)){
                
                           return org.wso2.carbon.identity.user.registration.stub.types.axis2.ReadUserFieldsForUserRegistrationResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.user.registration.stub.types.axis2.UserRegistrationAdminServiceIdentityException.class.equals(type)){
                
                           return org.wso2.carbon.identity.user.registration.stub.types.axis2.UserRegistrationAdminServiceIdentityException.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
        } catch (java.lang.Exception e) {
        throw org.apache.axis2.AxisFault.makeFault(e);
        }
           return null;
        }



    
   }
   