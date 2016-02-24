
/**
 * MultipleCredentialsUserAdminStub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */
        package org.wso2.carbon.user.mgt.stub;

        

        /*
        *  MultipleCredentialsUserAdminStub java implementation
        */

        
        public class MultipleCredentialsUserAdminStub extends org.apache.axis2.client.Stub
        implements MultipleCredentialsUserAdmin{
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
     _service = new org.apache.axis2.description.AxisService("MultipleCredentialsUserAdmin" + getUniqueSuffix());
     addAnonymousOperations();

        //creating the operations
        org.apache.axis2.description.AxisOperation __operation;

        _operations = new org.apache.axis2.description.AxisOperation[17];
        
                    __operation = new org.apache.axis2.description.RobustOutOnlyAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org", "deleteUserClaimValues"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[0]=__operation;
            
        
                   __operation = new org.apache.axis2.description.OutInAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org", "getUserClaimValue"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[1]=__operation;
            
        
                   __operation = new org.apache.axis2.description.OutInAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org", "getUserClaimValues"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[2]=__operation;
            
        
                    __operation = new org.apache.axis2.description.RobustOutOnlyAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org", "addUser"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[3]=__operation;
            
        
                    __operation = new org.apache.axis2.description.RobustOutOnlyAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org", "addCredential"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[4]=__operation;
            
        
                    __operation = new org.apache.axis2.description.RobustOutOnlyAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org", "addUserWithUserId"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[5]=__operation;
            
        
                   __operation = new org.apache.axis2.description.OutInAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org", "getCredentials"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[6]=__operation;
            
        
                   __operation = new org.apache.axis2.description.OutInAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org", "getAllUserClaimValues"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[7]=__operation;
            
        
                    __operation = new org.apache.axis2.description.RobustOutOnlyAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org", "addUsers"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[8]=__operation;
            
        
                   __operation = new org.apache.axis2.description.OutInAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org", "authenticate"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[9]=__operation;
            
        
                    __operation = new org.apache.axis2.description.RobustOutOnlyAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org", "setUserClaimValues"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[10]=__operation;
            
        
                    __operation = new org.apache.axis2.description.RobustOutOnlyAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org", "updateCredential"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[11]=__operation;
            
        
                    __operation = new org.apache.axis2.description.RobustOutOnlyAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org", "deleteUserClaimValue"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[12]=__operation;
            
        
                    __operation = new org.apache.axis2.description.RobustOutOnlyAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org", "deleteCredential"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[13]=__operation;
            
        
                    __operation = new org.apache.axis2.description.RobustOutOnlyAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org", "setUserClaimValue"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[14]=__operation;
            
        
                    __operation = new org.apache.axis2.description.RobustOutOnlyAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org", "deleteUser"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[15]=__operation;
            
        
                   __operation = new org.apache.axis2.description.OutInAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org", "getUserId"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[16]=__operation;
            
        
        }

    //populates the faults
    private void populateFaults(){
         
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "deleteUserClaimValues"),"org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "deleteUserClaimValues"),"org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "deleteUserClaimValues"),"org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException");
           
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "getUserClaimValue"),"org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "getUserClaimValue"),"org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "getUserClaimValue"),"org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException");
           
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "getUserClaimValues"),"org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "getUserClaimValues"),"org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "getUserClaimValues"),"org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException");
           
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "addUser"),"org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "addUser"),"org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "addUser"),"org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException");
           
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "addCredential"),"org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "addCredential"),"org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "addCredential"),"org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException");
           
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "addUserWithUserId"),"org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "addUserWithUserId"),"org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "addUserWithUserId"),"org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException");
           
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "getCredentials"),"org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "getCredentials"),"org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "getCredentials"),"org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException");
           
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "getAllUserClaimValues"),"org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "getAllUserClaimValues"),"org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "getAllUserClaimValues"),"org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException");
           
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "addUsers"),"org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "addUsers"),"org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "addUsers"),"org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException");
           
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "authenticate"),"org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "authenticate"),"org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "authenticate"),"org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException");
           
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "setUserClaimValues"),"org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "setUserClaimValues"),"org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "setUserClaimValues"),"org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException");
           
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "updateCredential"),"org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "updateCredential"),"org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "updateCredential"),"org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException");
           
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "deleteUserClaimValue"),"org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "deleteUserClaimValue"),"org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "deleteUserClaimValue"),"org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException");
           
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "deleteCredential"),"org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "deleteCredential"),"org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "deleteCredential"),"org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException");
           
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "setUserClaimValue"),"org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "setUserClaimValue"),"org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "setUserClaimValue"),"org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException");
           
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "deleteUser"),"org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "deleteUser"),"org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "deleteUser"),"org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException");
           
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "getUserId"),"org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "getUserId"),"org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org","MultipleCredentialsUserAdminMultipleCredentialsUserAdminException"), "getUserId"),"org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException");
           


    }

    /**
      *Constructor that takes in a configContext
      */

    public MultipleCredentialsUserAdminStub(org.apache.axis2.context.ConfigurationContext configurationContext,
       java.lang.String targetEndpoint)
       throws org.apache.axis2.AxisFault {
         this(configurationContext,targetEndpoint,false);
   }


   /**
     * Constructor that takes in a configContext  and useseperate listner
     */
   public MultipleCredentialsUserAdminStub(org.apache.axis2.context.ConfigurationContext configurationContext,
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
    public MultipleCredentialsUserAdminStub(org.apache.axis2.context.ConfigurationContext configurationContext) throws org.apache.axis2.AxisFault {
        
                    this(configurationContext,"https://localhost:9443/services/MultipleCredentialsUserAdmin.MultipleCredentialsUserAdminHttpsSoap12Endpoint/" );
                
    }

    /**
     * Default Constructor
     */
    public MultipleCredentialsUserAdminStub() throws org.apache.axis2.AxisFault {
        
                    this("https://localhost:9443/services/MultipleCredentialsUserAdmin.MultipleCredentialsUserAdminHttpsSoap12Endpoint/" );
                
    }

    /**
     * Constructor taking the target endpoint
     */
    public MultipleCredentialsUserAdminStub(java.lang.String targetEndpoint) throws org.apache.axis2.AxisFault {
        this(null,targetEndpoint);
    }



         
                
                /**
                  * Auto generated method signature
                  * 
                     * @throws org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException : 
                  */
                public void  deleteUserClaimValues(
                 java.lang.String identifer133,java.lang.String credentialType134,java.lang.String[] claims135,java.lang.String profileName136

                ) throws java.rmi.RemoteException
                
                
                        ,org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException
                {
                org.apache.axis2.context.MessageContext _messageContext = null;

                try {
                org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[0].getName());
                _operationClient.getOptions().setAction("urn:deleteUserClaimValues");
                _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

                
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              
                org.apache.axiom.soap.SOAPEnvelope env = null;
                 _messageContext = new org.apache.axis2.context.MessageContext();

                
                                                    //Style is Doc.
                                                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.DeleteUserClaimValues dummyWrappedType = null;
                                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                                    identifer133,
                                                                    credentialType134,
                                                                    claims135,
                                                                    profileName136,
                                                                    dummyWrappedType,
                                                                    optimizeContent(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org",
                                                                    "deleteUserClaimValues")));
                                                                

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
                      if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"deleteUserClaimValues"))){
                          //make the fault by reflection
                          try{
                              java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"deleteUserClaimValues"));
                              java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                              java.lang.Exception ex=
                                      (java.lang.Exception) exceptionClass.newInstance();
                              //message class
                              java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"deleteUserClaimValues"));
                              java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                              java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                              java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                         new java.lang.Class[]{messageClass});
                              m.invoke(ex,new java.lang.Object[]{messageObject});
                              
                              if (ex instanceof org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException){
                                throw (org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException)ex;
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
                     * @see org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdmin#getUserClaimValue
                     * @param getUserClaimValue137
                    
                     * @throws org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException : 
                     */

                    

                            public  java.lang.String getUserClaimValue(

                            java.lang.String identifer138,java.lang.String credentialType139,java.lang.String claimUri140,java.lang.String profileName141)
                        

                    throws java.rmi.RemoteException
                    
                    
                        ,org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException{
              org.apache.axis2.context.MessageContext _messageContext = null;
              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[1].getName());
              _operationClient.getOptions().setAction("urn:getUserClaimValue");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              

              // create a message context
              _messageContext = new org.apache.axis2.context.MessageContext();

              

              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;
                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserClaimValue dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    identifer138,
                                                    credentialType139,
                                                    claimUri140,
                                                    profileName141,
                                                    dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org",
                                                    "getUserClaimValue")));
                                                
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
                                             org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserClaimValueResponse.class,
                                              getEnvelopeNamespaces(_returnEnv));

                               
                                        return getGetUserClaimValueResponse_return((org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserClaimValueResponse)object);
                                   
         }catch(org.apache.axis2.AxisFault f){

            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt!=null){
                if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getUserClaimValue"))){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getUserClaimValue"));
                        java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                        java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
                        //message class
                        java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getUserClaimValue"));
                        java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                        java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                        java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                   new java.lang.Class[]{messageClass});
                        m.invoke(ex,new java.lang.Object[]{messageObject});
                        
                        if (ex instanceof org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException){
                          throw (org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException)ex;
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
                * @see org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdmin#startgetUserClaimValue
                    * @param getUserClaimValue137
                
                */
                public  void startgetUserClaimValue(

                 java.lang.String identifer138,java.lang.String credentialType139,java.lang.String claimUri140,java.lang.String profileName141,

                  final org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminCallbackHandler callback)

                throws java.rmi.RemoteException{

              org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[1].getName());
             _operationClient.getOptions().setAction("urn:getUserClaimValue");
             _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              


              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env=null;
              final org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();

                    
                                    //Style is Doc.
                                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserClaimValue dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    identifer138,
                                                    credentialType139,
                                                    claimUri140,
                                                    profileName141,
                                                     dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org",
                                                    "getUserClaimValue")));
                                                
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
                                                                         org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserClaimValueResponse.class,
                                                                         getEnvelopeNamespaces(resultEnv));
                                        callback.receiveResultgetUserClaimValue(
                                            getGetUserClaimValueResponse_return((org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserClaimValueResponse)object));
                                        
                            } catch (org.apache.axis2.AxisFault e) {
                                callback.receiveErrorgetUserClaimValue(e);
                            }
                            }

                            public void onError(java.lang.Exception error) {
								if (error instanceof org.apache.axis2.AxisFault) {
									org.apache.axis2.AxisFault f = (org.apache.axis2.AxisFault) error;
									org.apache.axiom.om.OMElement faultElt = f.getDetail();
									if (faultElt!=null){
										if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getUserClaimValue"))){
											//make the fault by reflection
											try{
													java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getUserClaimValue"));
													java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
													java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
													//message class
													java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getUserClaimValue"));
														java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
													java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
													java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
															new java.lang.Class[]{messageClass});
													m.invoke(ex,new java.lang.Object[]{messageObject});
													
													if (ex instanceof org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException){
														callback.receiveErrorgetUserClaimValue((org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException)ex);
											            return;
										            }
										            
					
										            callback.receiveErrorgetUserClaimValue(new java.rmi.RemoteException(ex.getMessage(), ex));
                                            } catch(java.lang.ClassCastException e){
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetUserClaimValue(f);
                                            } catch (java.lang.ClassNotFoundException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetUserClaimValue(f);
                                            } catch (java.lang.NoSuchMethodException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetUserClaimValue(f);
                                            } catch (java.lang.reflect.InvocationTargetException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetUserClaimValue(f);
                                            } catch (java.lang.IllegalAccessException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetUserClaimValue(f);
                                            } catch (java.lang.InstantiationException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetUserClaimValue(f);
                                            } catch (org.apache.axis2.AxisFault e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetUserClaimValue(f);
                                            }
									    } else {
										    callback.receiveErrorgetUserClaimValue(f);
									    }
									} else {
									    callback.receiveErrorgetUserClaimValue(f);
									}
								} else {
								    callback.receiveErrorgetUserClaimValue(error);
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
                                    callback.receiveErrorgetUserClaimValue(axisFault);
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
                     * @see org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdmin#getUserClaimValues
                     * @param getUserClaimValues144
                    
                     * @throws org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException : 
                     */

                    

                            public  org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.ClaimValue[] getUserClaimValues(

                            java.lang.String identifer145,java.lang.String credentialType146,java.lang.String[] claims147,java.lang.String profileName148)
                        

                    throws java.rmi.RemoteException
                    
                    
                        ,org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException{
              org.apache.axis2.context.MessageContext _messageContext = null;
              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[2].getName());
              _operationClient.getOptions().setAction("urn:getUserClaimValues");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              

              // create a message context
              _messageContext = new org.apache.axis2.context.MessageContext();

              

              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;
                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserClaimValues dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    identifer145,
                                                    credentialType146,
                                                    claims147,
                                                    profileName148,
                                                    dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org",
                                                    "getUserClaimValues")));
                                                
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
                                             org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserClaimValuesResponse.class,
                                              getEnvelopeNamespaces(_returnEnv));

                               
                                        return getGetUserClaimValuesResponse_return((org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserClaimValuesResponse)object);
                                   
         }catch(org.apache.axis2.AxisFault f){

            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt!=null){
                if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getUserClaimValues"))){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getUserClaimValues"));
                        java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                        java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
                        //message class
                        java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getUserClaimValues"));
                        java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                        java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                        java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                   new java.lang.Class[]{messageClass});
                        m.invoke(ex,new java.lang.Object[]{messageObject});
                        
                        if (ex instanceof org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException){
                          throw (org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException)ex;
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
                * @see org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdmin#startgetUserClaimValues
                    * @param getUserClaimValues144
                
                */
                public  void startgetUserClaimValues(

                 java.lang.String identifer145,java.lang.String credentialType146,java.lang.String[] claims147,java.lang.String profileName148,

                  final org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminCallbackHandler callback)

                throws java.rmi.RemoteException{

              org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[2].getName());
             _operationClient.getOptions().setAction("urn:getUserClaimValues");
             _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              


              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env=null;
              final org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();

                    
                                    //Style is Doc.
                                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserClaimValues dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    identifer145,
                                                    credentialType146,
                                                    claims147,
                                                    profileName148,
                                                     dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org",
                                                    "getUserClaimValues")));
                                                
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
                                                                         org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserClaimValuesResponse.class,
                                                                         getEnvelopeNamespaces(resultEnv));
                                        callback.receiveResultgetUserClaimValues(
                                            getGetUserClaimValuesResponse_return((org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserClaimValuesResponse)object));
                                        
                            } catch (org.apache.axis2.AxisFault e) {
                                callback.receiveErrorgetUserClaimValues(e);
                            }
                            }

                            public void onError(java.lang.Exception error) {
								if (error instanceof org.apache.axis2.AxisFault) {
									org.apache.axis2.AxisFault f = (org.apache.axis2.AxisFault) error;
									org.apache.axiom.om.OMElement faultElt = f.getDetail();
									if (faultElt!=null){
										if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getUserClaimValues"))){
											//make the fault by reflection
											try{
													java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getUserClaimValues"));
													java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
													java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
													//message class
													java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getUserClaimValues"));
														java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
													java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
													java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
															new java.lang.Class[]{messageClass});
													m.invoke(ex,new java.lang.Object[]{messageObject});
													
													if (ex instanceof org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException){
														callback.receiveErrorgetUserClaimValues((org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException)ex);
											            return;
										            }
										            
					
										            callback.receiveErrorgetUserClaimValues(new java.rmi.RemoteException(ex.getMessage(), ex));
                                            } catch(java.lang.ClassCastException e){
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetUserClaimValues(f);
                                            } catch (java.lang.ClassNotFoundException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetUserClaimValues(f);
                                            } catch (java.lang.NoSuchMethodException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetUserClaimValues(f);
                                            } catch (java.lang.reflect.InvocationTargetException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetUserClaimValues(f);
                                            } catch (java.lang.IllegalAccessException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetUserClaimValues(f);
                                            } catch (java.lang.InstantiationException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetUserClaimValues(f);
                                            } catch (org.apache.axis2.AxisFault e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetUserClaimValues(f);
                                            }
									    } else {
										    callback.receiveErrorgetUserClaimValues(f);
									    }
									} else {
									    callback.receiveErrorgetUserClaimValues(f);
									}
								} else {
								    callback.receiveErrorgetUserClaimValues(error);
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
                                    callback.receiveErrorgetUserClaimValues(axisFault);
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
                     * @throws org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException : 
                  */
                public void  addUser(
                 org.wso2.carbon.user.mgt.multiplecredentials.stub.types.Credential credential152,java.lang.String[] roleList153,org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.ClaimValue[] claims154,java.lang.String profileName155

                ) throws java.rmi.RemoteException
                
                
                        ,org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException
                {
                org.apache.axis2.context.MessageContext _messageContext = null;

                try {
                org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[3].getName());
                _operationClient.getOptions().setAction("urn:addUser");
                _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

                
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              
                org.apache.axiom.soap.SOAPEnvelope env = null;
                 _messageContext = new org.apache.axis2.context.MessageContext();

                
                                                    //Style is Doc.
                                                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.AddUser dummyWrappedType = null;
                                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                                    credential152,
                                                                    roleList153,
                                                                    claims154,
                                                                    profileName155,
                                                                    dummyWrappedType,
                                                                    optimizeContent(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org",
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
                              
                              if (ex instanceof org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException){
                                throw (org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException)ex;
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
                     * @throws org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException : 
                  */
                public void  addCredential(
                 java.lang.String anIdentifier157,java.lang.String credentialType158,org.wso2.carbon.user.mgt.multiplecredentials.stub.types.Credential credential159

                ) throws java.rmi.RemoteException
                
                
                        ,org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException
                {
                org.apache.axis2.context.MessageContext _messageContext = null;

                try {
                org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[4].getName());
                _operationClient.getOptions().setAction("urn:addCredential");
                _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

                
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              
                org.apache.axiom.soap.SOAPEnvelope env = null;
                 _messageContext = new org.apache.axis2.context.MessageContext();

                
                                                    //Style is Doc.
                                                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.AddCredential dummyWrappedType = null;
                                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                                    anIdentifier157,
                                                                    credentialType158,
                                                                    credential159,
                                                                    dummyWrappedType,
                                                                    optimizeContent(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org",
                                                                    "addCredential")));
                                                                

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
                      if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"addCredential"))){
                          //make the fault by reflection
                          try{
                              java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"addCredential"));
                              java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                              java.lang.Exception ex=
                                      (java.lang.Exception) exceptionClass.newInstance();
                              //message class
                              java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"addCredential"));
                              java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                              java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                              java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                         new java.lang.Class[]{messageClass});
                              m.invoke(ex,new java.lang.Object[]{messageObject});
                              
                              if (ex instanceof org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException){
                                throw (org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException)ex;
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
                     * @throws org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException : 
                  */
                public void  addUserWithUserId(
                 java.lang.String userId161,org.wso2.carbon.user.mgt.multiplecredentials.stub.types.Credential credential162,java.lang.String[] roleList163,org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.ClaimValue[] claims164,java.lang.String profileName165

                ) throws java.rmi.RemoteException
                
                
                        ,org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException
                {
                org.apache.axis2.context.MessageContext _messageContext = null;

                try {
                org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[5].getName());
                _operationClient.getOptions().setAction("urn:addUserWithUserId");
                _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

                
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              
                org.apache.axiom.soap.SOAPEnvelope env = null;
                 _messageContext = new org.apache.axis2.context.MessageContext();

                
                                                    //Style is Doc.
                                                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.AddUserWithUserId dummyWrappedType = null;
                                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                                    userId161,
                                                                    credential162,
                                                                    roleList163,
                                                                    claims164,
                                                                    profileName165,
                                                                    dummyWrappedType,
                                                                    optimizeContent(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org",
                                                                    "addUserWithUserId")));
                                                                

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
                      if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"addUserWithUserId"))){
                          //make the fault by reflection
                          try{
                              java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"addUserWithUserId"));
                              java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                              java.lang.Exception ex=
                                      (java.lang.Exception) exceptionClass.newInstance();
                              //message class
                              java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"addUserWithUserId"));
                              java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                              java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                              java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                         new java.lang.Class[]{messageClass});
                              m.invoke(ex,new java.lang.Object[]{messageObject});
                              
                              if (ex instanceof org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException){
                                throw (org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException)ex;
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
                     * @see org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdmin#getCredentials
                     * @param getCredentials166
                    
                     * @throws org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException : 
                     */

                    

                            public  org.wso2.carbon.user.mgt.multiplecredentials.stub.types.Credential[] getCredentials(

                            java.lang.String anIdentifier167,java.lang.String credentialType168)
                        

                    throws java.rmi.RemoteException
                    
                    
                        ,org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException{
              org.apache.axis2.context.MessageContext _messageContext = null;
              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[6].getName());
              _operationClient.getOptions().setAction("urn:getCredentials");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              

              // create a message context
              _messageContext = new org.apache.axis2.context.MessageContext();

              

              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;
                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetCredentials dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    anIdentifier167,
                                                    credentialType168,
                                                    dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org",
                                                    "getCredentials")));
                                                
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
                                             org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetCredentialsResponse.class,
                                              getEnvelopeNamespaces(_returnEnv));

                               
                                        return getGetCredentialsResponse_return((org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetCredentialsResponse)object);
                                   
         }catch(org.apache.axis2.AxisFault f){

            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt!=null){
                if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getCredentials"))){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getCredentials"));
                        java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                        java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
                        //message class
                        java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getCredentials"));
                        java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                        java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                        java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                   new java.lang.Class[]{messageClass});
                        m.invoke(ex,new java.lang.Object[]{messageObject});
                        
                        if (ex instanceof org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException){
                          throw (org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException)ex;
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
                * @see org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdmin#startgetCredentials
                    * @param getCredentials166
                
                */
                public  void startgetCredentials(

                 java.lang.String anIdentifier167,java.lang.String credentialType168,

                  final org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminCallbackHandler callback)

                throws java.rmi.RemoteException{

              org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[6].getName());
             _operationClient.getOptions().setAction("urn:getCredentials");
             _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              


              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env=null;
              final org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();

                    
                                    //Style is Doc.
                                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetCredentials dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    anIdentifier167,
                                                    credentialType168,
                                                     dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org",
                                                    "getCredentials")));
                                                
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
                                                                         org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetCredentialsResponse.class,
                                                                         getEnvelopeNamespaces(resultEnv));
                                        callback.receiveResultgetCredentials(
                                            getGetCredentialsResponse_return((org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetCredentialsResponse)object));
                                        
                            } catch (org.apache.axis2.AxisFault e) {
                                callback.receiveErrorgetCredentials(e);
                            }
                            }

                            public void onError(java.lang.Exception error) {
								if (error instanceof org.apache.axis2.AxisFault) {
									org.apache.axis2.AxisFault f = (org.apache.axis2.AxisFault) error;
									org.apache.axiom.om.OMElement faultElt = f.getDetail();
									if (faultElt!=null){
										if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getCredentials"))){
											//make the fault by reflection
											try{
													java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getCredentials"));
													java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
													java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
													//message class
													java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getCredentials"));
														java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
													java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
													java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
															new java.lang.Class[]{messageClass});
													m.invoke(ex,new java.lang.Object[]{messageObject});
													
													if (ex instanceof org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException){
														callback.receiveErrorgetCredentials((org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException)ex);
											            return;
										            }
										            
					
										            callback.receiveErrorgetCredentials(new java.rmi.RemoteException(ex.getMessage(), ex));
                                            } catch(java.lang.ClassCastException e){
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetCredentials(f);
                                            } catch (java.lang.ClassNotFoundException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetCredentials(f);
                                            } catch (java.lang.NoSuchMethodException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetCredentials(f);
                                            } catch (java.lang.reflect.InvocationTargetException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetCredentials(f);
                                            } catch (java.lang.IllegalAccessException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetCredentials(f);
                                            } catch (java.lang.InstantiationException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetCredentials(f);
                                            } catch (org.apache.axis2.AxisFault e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetCredentials(f);
                                            }
									    } else {
										    callback.receiveErrorgetCredentials(f);
									    }
									} else {
									    callback.receiveErrorgetCredentials(f);
									}
								} else {
								    callback.receiveErrorgetCredentials(error);
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
                                    callback.receiveErrorgetCredentials(axisFault);
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
                     * Auto generated method signature
                     * 
                     * @see org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdmin#getAllUserClaimValues
                     * @param getAllUserClaimValues171
                    
                     * @throws org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException : 
                     */

                    

                            public  org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.ClaimValue[] getAllUserClaimValues(

                            java.lang.String identifer172,java.lang.String credentialType173,java.lang.String profileName174)
                        

                    throws java.rmi.RemoteException
                    
                    
                        ,org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException{
              org.apache.axis2.context.MessageContext _messageContext = null;
              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[7].getName());
              _operationClient.getOptions().setAction("urn:getAllUserClaimValues");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              

              // create a message context
              _messageContext = new org.apache.axis2.context.MessageContext();

              

              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;
                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetAllUserClaimValues dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    identifer172,
                                                    credentialType173,
                                                    profileName174,
                                                    dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org",
                                                    "getAllUserClaimValues")));
                                                
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
                                             org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetAllUserClaimValuesResponse.class,
                                              getEnvelopeNamespaces(_returnEnv));

                               
                                        return getGetAllUserClaimValuesResponse_return((org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetAllUserClaimValuesResponse)object);
                                   
         }catch(org.apache.axis2.AxisFault f){

            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt!=null){
                if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getAllUserClaimValues"))){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getAllUserClaimValues"));
                        java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                        java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
                        //message class
                        java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getAllUserClaimValues"));
                        java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                        java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                        java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                   new java.lang.Class[]{messageClass});
                        m.invoke(ex,new java.lang.Object[]{messageObject});
                        
                        if (ex instanceof org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException){
                          throw (org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException)ex;
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
                * @see org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdmin#startgetAllUserClaimValues
                    * @param getAllUserClaimValues171
                
                */
                public  void startgetAllUserClaimValues(

                 java.lang.String identifer172,java.lang.String credentialType173,java.lang.String profileName174,

                  final org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminCallbackHandler callback)

                throws java.rmi.RemoteException{

              org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[7].getName());
             _operationClient.getOptions().setAction("urn:getAllUserClaimValues");
             _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              


              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env=null;
              final org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();

                    
                                    //Style is Doc.
                                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetAllUserClaimValues dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    identifer172,
                                                    credentialType173,
                                                    profileName174,
                                                     dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org",
                                                    "getAllUserClaimValues")));
                                                
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
                                                                         org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetAllUserClaimValuesResponse.class,
                                                                         getEnvelopeNamespaces(resultEnv));
                                        callback.receiveResultgetAllUserClaimValues(
                                            getGetAllUserClaimValuesResponse_return((org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetAllUserClaimValuesResponse)object));
                                        
                            } catch (org.apache.axis2.AxisFault e) {
                                callback.receiveErrorgetAllUserClaimValues(e);
                            }
                            }

                            public void onError(java.lang.Exception error) {
								if (error instanceof org.apache.axis2.AxisFault) {
									org.apache.axis2.AxisFault f = (org.apache.axis2.AxisFault) error;
									org.apache.axiom.om.OMElement faultElt = f.getDetail();
									if (faultElt!=null){
										if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getAllUserClaimValues"))){
											//make the fault by reflection
											try{
													java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getAllUserClaimValues"));
													java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
													java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
													//message class
													java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getAllUserClaimValues"));
														java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
													java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
													java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
															new java.lang.Class[]{messageClass});
													m.invoke(ex,new java.lang.Object[]{messageObject});
													
													if (ex instanceof org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException){
														callback.receiveErrorgetAllUserClaimValues((org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException)ex);
											            return;
										            }
										            
					
										            callback.receiveErrorgetAllUserClaimValues(new java.rmi.RemoteException(ex.getMessage(), ex));
                                            } catch(java.lang.ClassCastException e){
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetAllUserClaimValues(f);
                                            } catch (java.lang.ClassNotFoundException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetAllUserClaimValues(f);
                                            } catch (java.lang.NoSuchMethodException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetAllUserClaimValues(f);
                                            } catch (java.lang.reflect.InvocationTargetException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetAllUserClaimValues(f);
                                            } catch (java.lang.IllegalAccessException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetAllUserClaimValues(f);
                                            } catch (java.lang.InstantiationException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetAllUserClaimValues(f);
                                            } catch (org.apache.axis2.AxisFault e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetAllUserClaimValues(f);
                                            }
									    } else {
										    callback.receiveErrorgetAllUserClaimValues(f);
									    }
									} else {
									    callback.receiveErrorgetAllUserClaimValues(f);
									}
								} else {
								    callback.receiveErrorgetAllUserClaimValues(error);
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
                                    callback.receiveErrorgetAllUserClaimValues(axisFault);
                                }
                            }
                });
                        

          org.apache.axis2.util.CallbackReceiver _callbackReceiver = null;
        if ( _operations[7].getMessageReceiver()==null &&  _operationClient.getOptions().isUseSeparateListener()) {
           _callbackReceiver = new org.apache.axis2.util.CallbackReceiver();
          _operations[7].setMessageReceiver(
                    _callbackReceiver);
        }

           //execute the operation client
           _operationClient.execute(false);

                    }
                 
                
                /**
                  * Auto generated method signature
                  * 
                     * @throws org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException : 
                  */
                public void  addUsers(
                 org.wso2.carbon.user.mgt.multiplecredentials.stub.types.Credential[] credential178,java.lang.String[] roleList179,org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.ClaimValue[] claims180,java.lang.String profileName181

                ) throws java.rmi.RemoteException
                
                
                        ,org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException
                {
                org.apache.axis2.context.MessageContext _messageContext = null;

                try {
                org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[8].getName());
                _operationClient.getOptions().setAction("urn:addUsers");
                _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

                
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              
                org.apache.axiom.soap.SOAPEnvelope env = null;
                 _messageContext = new org.apache.axis2.context.MessageContext();

                
                                                    //Style is Doc.
                                                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.AddUsers dummyWrappedType = null;
                                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                                    credential178,
                                                                    roleList179,
                                                                    claims180,
                                                                    profileName181,
                                                                    dummyWrappedType,
                                                                    optimizeContent(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org",
                                                                    "addUsers")));
                                                                

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
                      if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"addUsers"))){
                          //make the fault by reflection
                          try{
                              java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"addUsers"));
                              java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                              java.lang.Exception ex=
                                      (java.lang.Exception) exceptionClass.newInstance();
                              //message class
                              java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"addUsers"));
                              java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                              java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                              java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                         new java.lang.Class[]{messageClass});
                              m.invoke(ex,new java.lang.Object[]{messageObject});
                              
                              if (ex instanceof org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException){
                                throw (org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException)ex;
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
                     * @see org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdmin#authenticate
                     * @param authenticate182
                    
                     * @throws org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException : 
                     */

                    

                            public  boolean authenticate(

                            org.wso2.carbon.user.mgt.multiplecredentials.stub.types.Credential credential183)
                        

                    throws java.rmi.RemoteException
                    
                    
                        ,org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException{
              org.apache.axis2.context.MessageContext _messageContext = null;
              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[9].getName());
              _operationClient.getOptions().setAction("urn:authenticate");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              

              // create a message context
              _messageContext = new org.apache.axis2.context.MessageContext();

              

              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;
                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.Authenticate dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    credential183,
                                                    dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org",
                                                    "authenticate")));
                                                
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
                                             org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.AuthenticateResponse.class,
                                              getEnvelopeNamespaces(_returnEnv));

                               
                                        return getAuthenticateResponse_return((org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.AuthenticateResponse)object);
                                   
         }catch(org.apache.axis2.AxisFault f){

            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt!=null){
                if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"authenticate"))){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"authenticate"));
                        java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                        java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
                        //message class
                        java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"authenticate"));
                        java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                        java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                        java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                   new java.lang.Class[]{messageClass});
                        m.invoke(ex,new java.lang.Object[]{messageObject});
                        
                        if (ex instanceof org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException){
                          throw (org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException)ex;
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
                * @see org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdmin#startauthenticate
                    * @param authenticate182
                
                */
                public  void startauthenticate(

                 org.wso2.carbon.user.mgt.multiplecredentials.stub.types.Credential credential183,

                  final org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminCallbackHandler callback)

                throws java.rmi.RemoteException{

              org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[9].getName());
             _operationClient.getOptions().setAction("urn:authenticate");
             _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              


              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env=null;
              final org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();

                    
                                    //Style is Doc.
                                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.Authenticate dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    credential183,
                                                     dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org",
                                                    "authenticate")));
                                                
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
                                                                         org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.AuthenticateResponse.class,
                                                                         getEnvelopeNamespaces(resultEnv));
                                        callback.receiveResultauthenticate(
                                            getAuthenticateResponse_return((org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.AuthenticateResponse)object));
                                        
                            } catch (org.apache.axis2.AxisFault e) {
                                callback.receiveErrorauthenticate(e);
                            }
                            }

                            public void onError(java.lang.Exception error) {
								if (error instanceof org.apache.axis2.AxisFault) {
									org.apache.axis2.AxisFault f = (org.apache.axis2.AxisFault) error;
									org.apache.axiom.om.OMElement faultElt = f.getDetail();
									if (faultElt!=null){
										if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"authenticate"))){
											//make the fault by reflection
											try{
													java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"authenticate"));
													java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
													java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
													//message class
													java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"authenticate"));
														java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
													java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
													java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
															new java.lang.Class[]{messageClass});
													m.invoke(ex,new java.lang.Object[]{messageObject});
													
													if (ex instanceof org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException){
														callback.receiveErrorauthenticate((org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException)ex);
											            return;
										            }
										            
					
										            callback.receiveErrorauthenticate(new java.rmi.RemoteException(ex.getMessage(), ex));
                                            } catch(java.lang.ClassCastException e){
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorauthenticate(f);
                                            } catch (java.lang.ClassNotFoundException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorauthenticate(f);
                                            } catch (java.lang.NoSuchMethodException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorauthenticate(f);
                                            } catch (java.lang.reflect.InvocationTargetException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorauthenticate(f);
                                            } catch (java.lang.IllegalAccessException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorauthenticate(f);
                                            } catch (java.lang.InstantiationException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorauthenticate(f);
                                            } catch (org.apache.axis2.AxisFault e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorauthenticate(f);
                                            }
									    } else {
										    callback.receiveErrorauthenticate(f);
									    }
									} else {
									    callback.receiveErrorauthenticate(f);
									}
								} else {
								    callback.receiveErrorauthenticate(error);
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
                                    callback.receiveErrorauthenticate(axisFault);
                                }
                            }
                });
                        

          org.apache.axis2.util.CallbackReceiver _callbackReceiver = null;
        if ( _operations[9].getMessageReceiver()==null &&  _operationClient.getOptions().isUseSeparateListener()) {
           _callbackReceiver = new org.apache.axis2.util.CallbackReceiver();
          _operations[9].setMessageReceiver(
                    _callbackReceiver);
        }

           //execute the operation client
           _operationClient.execute(false);

                    }
                 
                
                /**
                  * Auto generated method signature
                  * 
                     * @throws org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException : 
                  */
                public void  setUserClaimValues(
                 java.lang.String identifer187,java.lang.String credentialType188,org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.ClaimValue[] claims189,java.lang.String profileName190

                ) throws java.rmi.RemoteException
                
                
                        ,org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException
                {
                org.apache.axis2.context.MessageContext _messageContext = null;

                try {
                org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[10].getName());
                _operationClient.getOptions().setAction("urn:setUserClaimValues");
                _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

                
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              
                org.apache.axiom.soap.SOAPEnvelope env = null;
                 _messageContext = new org.apache.axis2.context.MessageContext();

                
                                                    //Style is Doc.
                                                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.SetUserClaimValues dummyWrappedType = null;
                                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                                    identifer187,
                                                                    credentialType188,
                                                                    claims189,
                                                                    profileName190,
                                                                    dummyWrappedType,
                                                                    optimizeContent(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org",
                                                                    "setUserClaimValues")));
                                                                

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
                      if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"setUserClaimValues"))){
                          //make the fault by reflection
                          try{
                              java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"setUserClaimValues"));
                              java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                              java.lang.Exception ex=
                                      (java.lang.Exception) exceptionClass.newInstance();
                              //message class
                              java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"setUserClaimValues"));
                              java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                              java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                              java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                         new java.lang.Class[]{messageClass});
                              m.invoke(ex,new java.lang.Object[]{messageObject});
                              
                              if (ex instanceof org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException){
                                throw (org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException)ex;
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
                     * @throws org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException : 
                  */
                public void  updateCredential(
                 java.lang.String identifier192,java.lang.String credentialType193,org.wso2.carbon.user.mgt.multiplecredentials.stub.types.Credential credential194

                ) throws java.rmi.RemoteException
                
                
                        ,org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException
                {
                org.apache.axis2.context.MessageContext _messageContext = null;

                try {
                org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[11].getName());
                _operationClient.getOptions().setAction("urn:updateCredential");
                _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

                
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              
                org.apache.axiom.soap.SOAPEnvelope env = null;
                 _messageContext = new org.apache.axis2.context.MessageContext();

                
                                                    //Style is Doc.
                                                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.UpdateCredential dummyWrappedType = null;
                                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                                    identifier192,
                                                                    credentialType193,
                                                                    credential194,
                                                                    dummyWrappedType,
                                                                    optimizeContent(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org",
                                                                    "updateCredential")));
                                                                

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
                      if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"updateCredential"))){
                          //make the fault by reflection
                          try{
                              java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"updateCredential"));
                              java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                              java.lang.Exception ex=
                                      (java.lang.Exception) exceptionClass.newInstance();
                              //message class
                              java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"updateCredential"));
                              java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                              java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                              java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                         new java.lang.Class[]{messageClass});
                              m.invoke(ex,new java.lang.Object[]{messageObject});
                              
                              if (ex instanceof org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException){
                                throw (org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException)ex;
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
                     * @throws org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException : 
                  */
                public void  deleteUserClaimValue(
                 java.lang.String identifer196,java.lang.String credentialType197,java.lang.String claimURI198,java.lang.String profileName199

                ) throws java.rmi.RemoteException
                
                
                        ,org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException
                {
                org.apache.axis2.context.MessageContext _messageContext = null;

                try {
                org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[12].getName());
                _operationClient.getOptions().setAction("urn:deleteUserClaimValue");
                _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

                
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              
                org.apache.axiom.soap.SOAPEnvelope env = null;
                 _messageContext = new org.apache.axis2.context.MessageContext();

                
                                                    //Style is Doc.
                                                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.DeleteUserClaimValue dummyWrappedType = null;
                                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                                    identifer196,
                                                                    credentialType197,
                                                                    claimURI198,
                                                                    profileName199,
                                                                    dummyWrappedType,
                                                                    optimizeContent(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org",
                                                                    "deleteUserClaimValue")));
                                                                

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
                      if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"deleteUserClaimValue"))){
                          //make the fault by reflection
                          try{
                              java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"deleteUserClaimValue"));
                              java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                              java.lang.Exception ex=
                                      (java.lang.Exception) exceptionClass.newInstance();
                              //message class
                              java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"deleteUserClaimValue"));
                              java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                              java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                              java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                         new java.lang.Class[]{messageClass});
                              m.invoke(ex,new java.lang.Object[]{messageObject});
                              
                              if (ex instanceof org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException){
                                throw (org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException)ex;
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
                     * @throws org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException : 
                  */
                public void  deleteCredential(
                 java.lang.String identifier201,java.lang.String credentialType202

                ) throws java.rmi.RemoteException
                
                
                        ,org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException
                {
                org.apache.axis2.context.MessageContext _messageContext = null;

                try {
                org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[13].getName());
                _operationClient.getOptions().setAction("urn:deleteCredential");
                _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

                
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              
                org.apache.axiom.soap.SOAPEnvelope env = null;
                 _messageContext = new org.apache.axis2.context.MessageContext();

                
                                                    //Style is Doc.
                                                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.DeleteCredential dummyWrappedType = null;
                                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                                    identifier201,
                                                                    credentialType202,
                                                                    dummyWrappedType,
                                                                    optimizeContent(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org",
                                                                    "deleteCredential")));
                                                                

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
                      if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"deleteCredential"))){
                          //make the fault by reflection
                          try{
                              java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"deleteCredential"));
                              java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                              java.lang.Exception ex=
                                      (java.lang.Exception) exceptionClass.newInstance();
                              //message class
                              java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"deleteCredential"));
                              java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                              java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                              java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                         new java.lang.Class[]{messageClass});
                              m.invoke(ex,new java.lang.Object[]{messageObject});
                              
                              if (ex instanceof org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException){
                                throw (org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException)ex;
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
                     * @throws org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException : 
                  */
                public void  setUserClaimValue(
                 java.lang.String identifer204,java.lang.String credentialType205,java.lang.String claimURI206,java.lang.String claimValue207,java.lang.String profileName208

                ) throws java.rmi.RemoteException
                
                
                        ,org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException
                {
                org.apache.axis2.context.MessageContext _messageContext = null;

                try {
                org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[14].getName());
                _operationClient.getOptions().setAction("urn:setUserClaimValue");
                _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

                
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              
                org.apache.axiom.soap.SOAPEnvelope env = null;
                 _messageContext = new org.apache.axis2.context.MessageContext();

                
                                                    //Style is Doc.
                                                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.SetUserClaimValue dummyWrappedType = null;
                                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                                    identifer204,
                                                                    credentialType205,
                                                                    claimURI206,
                                                                    claimValue207,
                                                                    profileName208,
                                                                    dummyWrappedType,
                                                                    optimizeContent(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org",
                                                                    "setUserClaimValue")));
                                                                

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
                      if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"setUserClaimValue"))){
                          //make the fault by reflection
                          try{
                              java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"setUserClaimValue"));
                              java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                              java.lang.Exception ex=
                                      (java.lang.Exception) exceptionClass.newInstance();
                              //message class
                              java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"setUserClaimValue"));
                              java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                              java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                              java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                         new java.lang.Class[]{messageClass});
                              m.invoke(ex,new java.lang.Object[]{messageObject});
                              
                              if (ex instanceof org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException){
                                throw (org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException)ex;
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
                     * @throws org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException : 
                  */
                public void  deleteUser(
                 java.lang.String identifier210,java.lang.String credentialType211

                ) throws java.rmi.RemoteException
                
                
                        ,org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException
                {
                org.apache.axis2.context.MessageContext _messageContext = null;

                try {
                org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[15].getName());
                _operationClient.getOptions().setAction("urn:deleteUser");
                _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

                
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              
                org.apache.axiom.soap.SOAPEnvelope env = null;
                 _messageContext = new org.apache.axis2.context.MessageContext();

                
                                                    //Style is Doc.
                                                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.DeleteUser dummyWrappedType = null;
                                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                                    identifier210,
                                                                    credentialType211,
                                                                    dummyWrappedType,
                                                                    optimizeContent(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org",
                                                                    "deleteUser")));
                                                                

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
                      if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"deleteUser"))){
                          //make the fault by reflection
                          try{
                              java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"deleteUser"));
                              java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                              java.lang.Exception ex=
                                      (java.lang.Exception) exceptionClass.newInstance();
                              //message class
                              java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"deleteUser"));
                              java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                              java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                              java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                         new java.lang.Class[]{messageClass});
                              m.invoke(ex,new java.lang.Object[]{messageObject});
                              
                              if (ex instanceof org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException){
                                throw (org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException)ex;
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
                     * @see org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdmin#getUserId
                     * @param getUserId212
                    
                     * @throws org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException : 
                     */

                    

                            public  java.lang.String getUserId(

                            org.wso2.carbon.user.mgt.multiplecredentials.stub.types.Credential credential213)
                        

                    throws java.rmi.RemoteException
                    
                    
                        ,org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException{
              org.apache.axis2.context.MessageContext _messageContext = null;
              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[16].getName());
              _operationClient.getOptions().setAction("urn:getUserId");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              

              // create a message context
              _messageContext = new org.apache.axis2.context.MessageContext();

              

              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;
                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserId dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    credential213,
                                                    dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org",
                                                    "getUserId")));
                                                
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
                                             org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserIdResponse.class,
                                              getEnvelopeNamespaces(_returnEnv));

                               
                                        return getGetUserIdResponse_return((org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserIdResponse)object);
                                   
         }catch(org.apache.axis2.AxisFault f){

            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt!=null){
                if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getUserId"))){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getUserId"));
                        java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                        java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
                        //message class
                        java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getUserId"));
                        java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                        java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                        java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                   new java.lang.Class[]{messageClass});
                        m.invoke(ex,new java.lang.Object[]{messageObject});
                        
                        if (ex instanceof org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException){
                          throw (org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException)ex;
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
                * @see org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdmin#startgetUserId
                    * @param getUserId212
                
                */
                public  void startgetUserId(

                 org.wso2.carbon.user.mgt.multiplecredentials.stub.types.Credential credential213,

                  final org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminCallbackHandler callback)

                throws java.rmi.RemoteException{

              org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[16].getName());
             _operationClient.getOptions().setAction("urn:getUserId");
             _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              


              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env=null;
              final org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();

                    
                                    //Style is Doc.
                                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserId dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    credential213,
                                                     dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("http://multiplecredentials.mgt.user.carbon.wso2.org",
                                                    "getUserId")));
                                                
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
                                                                         org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserIdResponse.class,
                                                                         getEnvelopeNamespaces(resultEnv));
                                        callback.receiveResultgetUserId(
                                            getGetUserIdResponse_return((org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserIdResponse)object));
                                        
                            } catch (org.apache.axis2.AxisFault e) {
                                callback.receiveErrorgetUserId(e);
                            }
                            }

                            public void onError(java.lang.Exception error) {
								if (error instanceof org.apache.axis2.AxisFault) {
									org.apache.axis2.AxisFault f = (org.apache.axis2.AxisFault) error;
									org.apache.axiom.om.OMElement faultElt = f.getDetail();
									if (faultElt!=null){
										if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getUserId"))){
											//make the fault by reflection
											try{
													java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getUserId"));
													java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
													java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
													//message class
													java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getUserId"));
														java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
													java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
													java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
															new java.lang.Class[]{messageClass});
													m.invoke(ex,new java.lang.Object[]{messageObject});
													
													if (ex instanceof org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException){
														callback.receiveErrorgetUserId((org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException)ex);
											            return;
										            }
										            
					
										            callback.receiveErrorgetUserId(new java.rmi.RemoteException(ex.getMessage(), ex));
                                            } catch(java.lang.ClassCastException e){
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetUserId(f);
                                            } catch (java.lang.ClassNotFoundException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetUserId(f);
                                            } catch (java.lang.NoSuchMethodException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetUserId(f);
                                            } catch (java.lang.reflect.InvocationTargetException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetUserId(f);
                                            } catch (java.lang.IllegalAccessException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetUserId(f);
                                            } catch (java.lang.InstantiationException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetUserId(f);
                                            } catch (org.apache.axis2.AxisFault e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetUserId(f);
                                            }
									    } else {
										    callback.receiveErrorgetUserId(f);
									    }
									} else {
									    callback.receiveErrorgetUserId(f);
									}
								} else {
								    callback.receiveErrorgetUserId(error);
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
                                    callback.receiveErrorgetUserId(axisFault);
                                }
                            }
                });
                        

          org.apache.axis2.util.CallbackReceiver _callbackReceiver = null;
        if ( _operations[16].getMessageReceiver()==null &&  _operationClient.getOptions().isUseSeparateListener()) {
           _callbackReceiver = new org.apache.axis2.util.CallbackReceiver();
          _operations[16].setMessageReceiver(
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
     //https://localhost:9443/services/MultipleCredentialsUserAdmin.MultipleCredentialsUserAdminHttpsSoap12Endpoint/
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.DeleteUserClaimValues param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.DeleteUserClaimValues.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserClaimValue param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserClaimValue.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserClaimValueResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserClaimValueResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserClaimValues param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserClaimValues.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserClaimValuesResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserClaimValuesResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.AddUser param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.AddUser.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.AddCredential param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.AddCredential.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.AddUserWithUserId param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.AddUserWithUserId.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetCredentials param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetCredentials.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetCredentialsResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetCredentialsResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetAllUserClaimValues param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetAllUserClaimValues.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetAllUserClaimValuesResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetAllUserClaimValuesResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.AddUsers param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.AddUsers.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.Authenticate param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.Authenticate.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.AuthenticateResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.AuthenticateResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.SetUserClaimValues param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.SetUserClaimValues.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.UpdateCredential param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.UpdateCredential.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.DeleteUserClaimValue param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.DeleteUserClaimValue.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.DeleteCredential param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.DeleteCredential.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.SetUserClaimValue param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.SetUserClaimValue.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.DeleteUser param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.DeleteUser.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserId param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserId.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserIdResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserIdResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    java.lang.String param1,
                                    java.lang.String param2,
                                    java.lang.String[] param3,
                                    java.lang.String param4,
                                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.DeleteUserClaimValues dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.DeleteUserClaimValues wrappedType = new org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.DeleteUserClaimValues();

                                 
                                              wrappedType.setIdentifer(param1);
                                         
                                              wrappedType.setCredentialType(param2);
                                         
                                              wrappedType.setClaims(param3);
                                         
                                              wrappedType.setProfileName(param4);
                                         

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.DeleteUserClaimValues.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    java.lang.String param1,
                                    java.lang.String param2,
                                    java.lang.String param3,
                                    java.lang.String param4,
                                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserClaimValue dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserClaimValue wrappedType = new org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserClaimValue();

                                 
                                              wrappedType.setIdentifer(param1);
                                         
                                              wrappedType.setCredentialType(param2);
                                         
                                              wrappedType.setClaimUri(param3);
                                         
                                              wrappedType.setProfileName(param4);
                                         

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserClaimValue.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             

                                
                                private java.lang.String getGetUserClaimValueResponse_return(
                                org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserClaimValueResponse wrappedType){
                                
                                        return wrappedType.get_return();
                                    
                                }
                             
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    java.lang.String param1,
                                    java.lang.String param2,
                                    java.lang.String[] param3,
                                    java.lang.String param4,
                                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserClaimValues dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserClaimValues wrappedType = new org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserClaimValues();

                                 
                                              wrappedType.setIdentifer(param1);
                                         
                                              wrappedType.setCredentialType(param2);
                                         
                                              wrappedType.setClaims(param3);
                                         
                                              wrappedType.setProfileName(param4);
                                         

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserClaimValues.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             

                                
                                private org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.ClaimValue[] getGetUserClaimValuesResponse_return(
                                org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserClaimValuesResponse wrappedType){
                                
                                        return wrappedType.get_return();
                                    
                                }
                             
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.Credential param1,
                                    java.lang.String[] param2,
                                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.ClaimValue[] param3,
                                    java.lang.String param4,
                                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.AddUser dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.AddUser wrappedType = new org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.AddUser();

                                 
                                              wrappedType.setCredential(param1);
                                         
                                              wrappedType.setRoleList(param2);
                                         
                                              wrappedType.setClaims(param3);
                                         
                                              wrappedType.setProfileName(param4);
                                         

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.AddUser.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    java.lang.String param1,
                                    java.lang.String param2,
                                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.Credential param3,
                                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.AddCredential dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.AddCredential wrappedType = new org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.AddCredential();

                                 
                                              wrappedType.setAnIdentifier(param1);
                                         
                                              wrappedType.setCredentialType(param2);
                                         
                                              wrappedType.setCredential(param3);
                                         

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.AddCredential.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    java.lang.String param1,
                                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.Credential param2,
                                    java.lang.String[] param3,
                                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.ClaimValue[] param4,
                                    java.lang.String param5,
                                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.AddUserWithUserId dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.AddUserWithUserId wrappedType = new org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.AddUserWithUserId();

                                 
                                              wrappedType.setUserId(param1);
                                         
                                              wrappedType.setCredential(param2);
                                         
                                              wrappedType.setRoleList(param3);
                                         
                                              wrappedType.setClaims(param4);
                                         
                                              wrappedType.setProfileName(param5);
                                         

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.AddUserWithUserId.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    java.lang.String param1,
                                    java.lang.String param2,
                                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetCredentials dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetCredentials wrappedType = new org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetCredentials();

                                 
                                              wrappedType.setAnIdentifier(param1);
                                         
                                              wrappedType.setCredentialType(param2);
                                         

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetCredentials.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             

                                
                                private org.wso2.carbon.user.mgt.multiplecredentials.stub.types.Credential[] getGetCredentialsResponse_return(
                                org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetCredentialsResponse wrappedType){
                                
                                        return wrappedType.get_return();
                                    
                                }
                             
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    java.lang.String param1,
                                    java.lang.String param2,
                                    java.lang.String param3,
                                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetAllUserClaimValues dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetAllUserClaimValues wrappedType = new org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetAllUserClaimValues();

                                 
                                              wrappedType.setIdentifer(param1);
                                         
                                              wrappedType.setCredentialType(param2);
                                         
                                              wrappedType.setProfileName(param3);
                                         

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetAllUserClaimValues.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             

                                
                                private org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.ClaimValue[] getGetAllUserClaimValuesResponse_return(
                                org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetAllUserClaimValuesResponse wrappedType){
                                
                                        return wrappedType.get_return();
                                    
                                }
                             
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.Credential[] param1,
                                    java.lang.String[] param2,
                                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.ClaimValue[] param3,
                                    java.lang.String param4,
                                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.AddUsers dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.AddUsers wrappedType = new org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.AddUsers();

                                 
                                              wrappedType.setCredential(param1);
                                         
                                              wrappedType.setRoleList(param2);
                                         
                                              wrappedType.setClaims(param3);
                                         
                                              wrappedType.setProfileName(param4);
                                         

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.AddUsers.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.Credential param1,
                                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.Authenticate dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.Authenticate wrappedType = new org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.Authenticate();

                                 
                                              wrappedType.setCredential(param1);
                                         

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.Authenticate.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             

                                
                                private boolean getAuthenticateResponse_return(
                                org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.AuthenticateResponse wrappedType){
                                
                                        return wrappedType.get_return();
                                    
                                }
                             
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    java.lang.String param1,
                                    java.lang.String param2,
                                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.ClaimValue[] param3,
                                    java.lang.String param4,
                                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.SetUserClaimValues dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.SetUserClaimValues wrappedType = new org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.SetUserClaimValues();

                                 
                                              wrappedType.setIdentifer(param1);
                                         
                                              wrappedType.setCredentialType(param2);
                                         
                                              wrappedType.setClaims(param3);
                                         
                                              wrappedType.setProfileName(param4);
                                         

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.SetUserClaimValues.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    java.lang.String param1,
                                    java.lang.String param2,
                                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.Credential param3,
                                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.UpdateCredential dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.UpdateCredential wrappedType = new org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.UpdateCredential();

                                 
                                              wrappedType.setIdentifier(param1);
                                         
                                              wrappedType.setCredentialType(param2);
                                         
                                              wrappedType.setCredential(param3);
                                         

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.UpdateCredential.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    java.lang.String param1,
                                    java.lang.String param2,
                                    java.lang.String param3,
                                    java.lang.String param4,
                                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.DeleteUserClaimValue dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.DeleteUserClaimValue wrappedType = new org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.DeleteUserClaimValue();

                                 
                                              wrappedType.setIdentifer(param1);
                                         
                                              wrappedType.setCredentialType(param2);
                                         
                                              wrappedType.setClaimURI(param3);
                                         
                                              wrappedType.setProfileName(param4);
                                         

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.DeleteUserClaimValue.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    java.lang.String param1,
                                    java.lang.String param2,
                                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.DeleteCredential dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.DeleteCredential wrappedType = new org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.DeleteCredential();

                                 
                                              wrappedType.setIdentifier(param1);
                                         
                                              wrappedType.setCredentialType(param2);
                                         

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.DeleteCredential.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    java.lang.String param1,
                                    java.lang.String param2,
                                    java.lang.String param3,
                                    java.lang.String param4,
                                    java.lang.String param5,
                                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.SetUserClaimValue dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.SetUserClaimValue wrappedType = new org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.SetUserClaimValue();

                                 
                                              wrappedType.setIdentifer(param1);
                                         
                                              wrappedType.setCredentialType(param2);
                                         
                                              wrappedType.setClaimURI(param3);
                                         
                                              wrappedType.setClaimValue(param4);
                                         
                                              wrappedType.setProfileName(param5);
                                         

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.SetUserClaimValue.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    java.lang.String param1,
                                    java.lang.String param2,
                                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.DeleteUser dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.DeleteUser wrappedType = new org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.DeleteUser();

                                 
                                              wrappedType.setIdentifier(param1);
                                         
                                              wrappedType.setCredentialType(param2);
                                         

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.DeleteUser.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.Credential param1,
                                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserId dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserId wrappedType = new org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserId();

                                 
                                              wrappedType.setCredential(param1);
                                         

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserId.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             

                                
                                private java.lang.String getGetUserIdResponse_return(
                                org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserIdResponse wrappedType){
                                
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
        
                if (org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.DeleteUserClaimValues.class.equals(type)){
                
                           return org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.DeleteUserClaimValues.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException.class.equals(type)){
                
                           return org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserClaimValue.class.equals(type)){
                
                           return org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserClaimValue.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserClaimValueResponse.class.equals(type)){
                
                           return org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserClaimValueResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException.class.equals(type)){
                
                           return org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserClaimValues.class.equals(type)){
                
                           return org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserClaimValues.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserClaimValuesResponse.class.equals(type)){
                
                           return org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserClaimValuesResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException.class.equals(type)){
                
                           return org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.AddUser.class.equals(type)){
                
                           return org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.AddUser.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException.class.equals(type)){
                
                           return org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.AddCredential.class.equals(type)){
                
                           return org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.AddCredential.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException.class.equals(type)){
                
                           return org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.AddUserWithUserId.class.equals(type)){
                
                           return org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.AddUserWithUserId.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException.class.equals(type)){
                
                           return org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetCredentials.class.equals(type)){
                
                           return org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetCredentials.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetCredentialsResponse.class.equals(type)){
                
                           return org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetCredentialsResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException.class.equals(type)){
                
                           return org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetAllUserClaimValues.class.equals(type)){
                
                           return org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetAllUserClaimValues.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetAllUserClaimValuesResponse.class.equals(type)){
                
                           return org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetAllUserClaimValuesResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException.class.equals(type)){
                
                           return org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.AddUsers.class.equals(type)){
                
                           return org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.AddUsers.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException.class.equals(type)){
                
                           return org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.Authenticate.class.equals(type)){
                
                           return org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.Authenticate.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.AuthenticateResponse.class.equals(type)){
                
                           return org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.AuthenticateResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException.class.equals(type)){
                
                           return org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.SetUserClaimValues.class.equals(type)){
                
                           return org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.SetUserClaimValues.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException.class.equals(type)){
                
                           return org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.UpdateCredential.class.equals(type)){
                
                           return org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.UpdateCredential.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException.class.equals(type)){
                
                           return org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.DeleteUserClaimValue.class.equals(type)){
                
                           return org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.DeleteUserClaimValue.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException.class.equals(type)){
                
                           return org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.DeleteCredential.class.equals(type)){
                
                           return org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.DeleteCredential.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException.class.equals(type)){
                
                           return org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.SetUserClaimValue.class.equals(type)){
                
                           return org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.SetUserClaimValue.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException.class.equals(type)){
                
                           return org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.DeleteUser.class.equals(type)){
                
                           return org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.DeleteUser.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException.class.equals(type)){
                
                           return org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserId.class.equals(type)){
                
                           return org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserId.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserIdResponse.class.equals(type)){
                
                           return org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.GetUserIdResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException.class.equals(type)){
                
                           return org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
        } catch (java.lang.Exception e) {
        throw org.apache.axis2.AxisFault.makeFault(e);
        }
           return null;
        }



    
   }
   