
/**
 * EntitlementPolicyAdminServiceStub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */
        package org.wso2.carbon.identity.entitlement.stub;

        

        /*
        *  EntitlementPolicyAdminServiceStub java implementation
        */

        
        public class EntitlementPolicyAdminServiceStub extends org.apache.axis2.client.Stub
        implements EntitlementPolicyAdminService{
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
     _service = new org.apache.axis2.description.AxisService("EntitlementPolicyAdminService" + getUniqueSuffix());
     addAnonymousOperations();

        //creating the operations
        org.apache.axis2.description.AxisOperation __operation;

        _operations = new org.apache.axis2.description.AxisOperation[28];
        
                    __operation = new org.apache.axis2.description.RobustOutOnlyAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org", "addPolicies"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[0]=__operation;
            
        
                    __operation = new org.apache.axis2.description.RobustOutOnlyAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org", "addSubscriber"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[1]=__operation;
            
        
                    __operation = new org.apache.axis2.description.RobustOutOnlyAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org", "publish"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[2]=__operation;
            
        
                    __operation = new org.apache.axis2.description.RobustOutOnlyAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org", "orderPolicy"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[3]=__operation;
            
        
                   __operation = new org.apache.axis2.description.OutInAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org", "getStatusData"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[4]=__operation;
            
        
                    __operation = new org.apache.axis2.description.RobustOutOnlyAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org", "addPolicy"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[5]=__operation;
            
        
                    __operation = new org.apache.axis2.description.RobustOutOnlyAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org", "removePolicy"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[6]=__operation;
            
        
                   __operation = new org.apache.axis2.description.OutInAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org", "getSubscriber"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[7]=__operation;
            
        
                    __operation = new org.apache.axis2.description.RobustOutOnlyAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org", "updateSubscriber"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[8]=__operation;
            
        
                   __operation = new org.apache.axis2.description.OutInAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org", "getPolicy"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[9]=__operation;
            
        
                    __operation = new org.apache.axis2.description.RobustOutOnlyAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org", "publishPolicies"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[10]=__operation;
            
        
                   __operation = new org.apache.axis2.description.OutInAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org", "getAllPolicyIds"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[11]=__operation;
            
        
                   __operation = new org.apache.axis2.description.OutInAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org", "getEntitlementDataModules"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[12]=__operation;
            
        
                   __operation = new org.apache.axis2.description.OutInAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org", "getSubscriberIds"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[13]=__operation;
            
        
                    __operation = new org.apache.axis2.description.RobustOutOnlyAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org", "rollBackPolicy"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[14]=__operation;
            
        
                   __operation = new org.apache.axis2.description.OutInAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org", "getLightPolicy"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[15]=__operation;
            
        
                    __operation = new org.apache.axis2.description.RobustOutOnlyAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org", "importPolicyFromRegistry"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[16]=__operation;
            
        
                   __operation = new org.apache.axis2.description.OutInAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org", "getPublisherModuleData"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[17]=__operation;
            
        
                   __operation = new org.apache.axis2.description.OutInAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org", "getPolicyVersions"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[18]=__operation;
            
        
                    __operation = new org.apache.axis2.description.RobustOutOnlyAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org", "removePolicies"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[19]=__operation;
            
        
                    __operation = new org.apache.axis2.description.RobustOutOnlyAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org", "dePromotePolicy"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[20]=__operation;
            
        
                   __operation = new org.apache.axis2.description.OutInAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org", "getEntitlementData"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[21]=__operation;
            
        
                    __operation = new org.apache.axis2.description.RobustOutOnlyAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org", "publishToPDP"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[22]=__operation;
            
        
                   __operation = new org.apache.axis2.description.OutInAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org", "getPolicyByVersion"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[23]=__operation;
            
        
                    __operation = new org.apache.axis2.description.RobustOutOnlyAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org", "enableDisablePolicy"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[24]=__operation;
            
        
                   __operation = new org.apache.axis2.description.OutInAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org", "getAllPolicies"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[25]=__operation;
            
        
                    __operation = new org.apache.axis2.description.RobustOutOnlyAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org", "deleteSubscriber"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[26]=__operation;
            
        
                    __operation = new org.apache.axis2.description.RobustOutOnlyAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org", "updatePolicy"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[27]=__operation;
            
        
        }

    //populates the faults
    private void populateFaults(){
         
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "addPolicies"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "addPolicies"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "addPolicies"),"org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException");
           
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "addSubscriber"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "addSubscriber"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "addSubscriber"),"org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException");
           
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "publish"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "publish"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "publish"),"org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException");
           
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "orderPolicy"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "orderPolicy"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "orderPolicy"),"org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException");
           
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "getStatusData"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "getStatusData"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "getStatusData"),"org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException");
           
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "addPolicy"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "addPolicy"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "addPolicy"),"org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException");
           
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "removePolicy"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "removePolicy"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "removePolicy"),"org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException");
           
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "getSubscriber"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "getSubscriber"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "getSubscriber"),"org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException");
           
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "updateSubscriber"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "updateSubscriber"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "updateSubscriber"),"org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException");
           
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "getPolicy"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "getPolicy"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "getPolicy"),"org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException");
           
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "publishPolicies"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "publishPolicies"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "publishPolicies"),"org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException");
           
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "getAllPolicyIds"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "getAllPolicyIds"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "getAllPolicyIds"),"org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException");
           
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "getSubscriberIds"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "getSubscriberIds"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "getSubscriberIds"),"org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException");
           
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "rollBackPolicy"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "rollBackPolicy"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "rollBackPolicy"),"org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException");
           
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "getLightPolicy"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "getLightPolicy"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "getLightPolicy"),"org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException");
           
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "importPolicyFromRegistry"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "importPolicyFromRegistry"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "importPolicyFromRegistry"),"org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException");
           
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "getPolicyVersions"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "getPolicyVersions"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "getPolicyVersions"),"org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException");
           
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "removePolicies"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "removePolicies"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "removePolicies"),"org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException");
           
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "dePromotePolicy"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "dePromotePolicy"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "dePromotePolicy"),"org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException");
           
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "publishToPDP"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "publishToPDP"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "publishToPDP"),"org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException");
           
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "getPolicyByVersion"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "getPolicyByVersion"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "getPolicyByVersion"),"org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException");
           
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "enableDisablePolicy"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "enableDisablePolicy"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "enableDisablePolicy"),"org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException");
           
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "getAllPolicies"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "getAllPolicies"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "getAllPolicies"),"org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException");
           
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "deleteSubscriber"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "deleteSubscriber"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "deleteSubscriber"),"org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException");
           
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "updatePolicy"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "updatePolicy"),"org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("http://org.apache.axis2/xsd","EntitlementPolicyAdminServiceEntitlementException"), "updatePolicy"),"org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException");
           


    }

    /**
      *Constructor that takes in a configContext
      */

    public EntitlementPolicyAdminServiceStub(org.apache.axis2.context.ConfigurationContext configurationContext,
       java.lang.String targetEndpoint)
       throws org.apache.axis2.AxisFault {
         this(configurationContext,targetEndpoint,false);
   }


   /**
     * Constructor that takes in a configContext  and useseperate listner
     */
   public EntitlementPolicyAdminServiceStub(org.apache.axis2.context.ConfigurationContext configurationContext,
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
    public EntitlementPolicyAdminServiceStub(org.apache.axis2.context.ConfigurationContext configurationContext) throws org.apache.axis2.AxisFault {
        
                    this(configurationContext,"https://localhost:9443/services/EntitlementPolicyAdminService.EntitlementPolicyAdminServiceHttpsSoap12Endpoint/" );
                
    }

    /**
     * Default Constructor
     */
    public EntitlementPolicyAdminServiceStub() throws org.apache.axis2.AxisFault {
        
                    this("https://localhost:9443/services/EntitlementPolicyAdminService.EntitlementPolicyAdminServiceHttpsSoap12Endpoint/" );
                
    }

    /**
     * Constructor taking the target endpoint
     */
    public EntitlementPolicyAdminServiceStub(java.lang.String targetEndpoint) throws org.apache.axis2.AxisFault {
        this(null,targetEndpoint);
    }



         
                
                /**
                  * Auto generated method signature
                  * 
                     * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
                  */
                public void  addPolicies(
                 org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO[] policies140

                ) throws java.rmi.RemoteException
                
                
                        ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException
                {
                org.apache.axis2.context.MessageContext _messageContext = null;

                try {
                org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[0].getName());
                _operationClient.getOptions().setAction("urn:addPolicies");
                _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

                
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              
                org.apache.axiom.soap.SOAPEnvelope env = null;
                 _messageContext = new org.apache.axis2.context.MessageContext();

                
                                                    //Style is Doc.
                                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.AddPolicies dummyWrappedType = null;
                                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                                    policies140,
                                                                    dummyWrappedType,
                                                                    optimizeContent(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org",
                                                                    "addPolicies")));
                                                                

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
                      if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"addPolicies"))){
                          //make the fault by reflection
                          try{
                              java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"addPolicies"));
                              java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                              java.lang.Exception ex=
                                      (java.lang.Exception) exceptionClass.newInstance();
                              //message class
                              java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"addPolicies"));
                              java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                              java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                              java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                         new java.lang.Class[]{messageClass});
                              m.invoke(ex,new java.lang.Object[]{messageObject});
                              
                              if (ex instanceof org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException){
                                throw (org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException)ex;
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
                     * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
                  */
                public void  addSubscriber(
                 org.wso2.carbon.identity.entitlement.stub.dto.PublisherDataHolder holder142

                ) throws java.rmi.RemoteException
                
                
                        ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException
                {
                org.apache.axis2.context.MessageContext _messageContext = null;

                try {
                org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[1].getName());
                _operationClient.getOptions().setAction("urn:addSubscriber");
                _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

                
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              
                org.apache.axiom.soap.SOAPEnvelope env = null;
                 _messageContext = new org.apache.axis2.context.MessageContext();

                
                                                    //Style is Doc.
                                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.AddSubscriber dummyWrappedType = null;
                                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                                    holder142,
                                                                    dummyWrappedType,
                                                                    optimizeContent(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org",
                                                                    "addSubscriber")));
                                                                

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
                      if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"addSubscriber"))){
                          //make the fault by reflection
                          try{
                              java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"addSubscriber"));
                              java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                              java.lang.Exception ex=
                                      (java.lang.Exception) exceptionClass.newInstance();
                              //message class
                              java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"addSubscriber"));
                              java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                              java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                              java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                         new java.lang.Class[]{messageClass});
                              m.invoke(ex,new java.lang.Object[]{messageObject});
                              
                              if (ex instanceof org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException){
                                throw (org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException)ex;
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
                     * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
                  */
                public void  publish(
                 java.lang.String verificationCode144

                ) throws java.rmi.RemoteException
                
                
                        ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException
                {
                org.apache.axis2.context.MessageContext _messageContext = null;

                try {
                org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[2].getName());
                _operationClient.getOptions().setAction("urn:publish");
                _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

                
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              
                org.apache.axiom.soap.SOAPEnvelope env = null;
                 _messageContext = new org.apache.axis2.context.MessageContext();

                
                                                    //Style is Doc.
                                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.Publish dummyWrappedType = null;
                                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                                    verificationCode144,
                                                                    dummyWrappedType,
                                                                    optimizeContent(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org",
                                                                    "publish")));
                                                                

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
                      if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"publish"))){
                          //make the fault by reflection
                          try{
                              java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"publish"));
                              java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                              java.lang.Exception ex=
                                      (java.lang.Exception) exceptionClass.newInstance();
                              //message class
                              java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"publish"));
                              java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                              java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                              java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                         new java.lang.Class[]{messageClass});
                              m.invoke(ex,new java.lang.Object[]{messageObject});
                              
                              if (ex instanceof org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException){
                                throw (org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException)ex;
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
                     * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
                  */
                public void  orderPolicy(
                 java.lang.String policyId146,int newOrder147

                ) throws java.rmi.RemoteException
                
                
                        ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException
                {
                org.apache.axis2.context.MessageContext _messageContext = null;

                try {
                org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[3].getName());
                _operationClient.getOptions().setAction("urn:orderPolicy");
                _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

                
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              
                org.apache.axiom.soap.SOAPEnvelope env = null;
                 _messageContext = new org.apache.axis2.context.MessageContext();

                
                                                    //Style is Doc.
                                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.OrderPolicy dummyWrappedType = null;
                                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                                    policyId146,
                                                                    newOrder147,
                                                                    dummyWrappedType,
                                                                    optimizeContent(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org",
                                                                    "orderPolicy")));
                                                                

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
                      if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"orderPolicy"))){
                          //make the fault by reflection
                          try{
                              java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"orderPolicy"));
                              java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                              java.lang.Exception ex=
                                      (java.lang.Exception) exceptionClass.newInstance();
                              //message class
                              java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"orderPolicy"));
                              java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                              java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                              java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                         new java.lang.Class[]{messageClass});
                              m.invoke(ex,new java.lang.Object[]{messageObject});
                              
                              if (ex instanceof org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException){
                                throw (org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException)ex;
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
                     * @see org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminService#getStatusData
                     * @param getStatusData148
                    
                     * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
                     */

                    

                            public  org.wso2.carbon.identity.entitlement.stub.dto.PaginatedStatusHolder getStatusData(

                            java.lang.String about149,java.lang.String key150,java.lang.String type151,java.lang.String searchString152,int pageNumber153)
                        

                    throws java.rmi.RemoteException
                    
                    
                        ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException{
              org.apache.axis2.context.MessageContext _messageContext = null;
              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[4].getName());
              _operationClient.getOptions().setAction("urn:getStatusData");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              

              // create a message context
              _messageContext = new org.apache.axis2.context.MessageContext();

              

              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;
                    org.wso2.carbon.identity.entitlement.stub.types.axis2.GetStatusData dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    about149,
                                                    key150,
                                                    type151,
                                                    searchString152,
                                                    pageNumber153,
                                                    dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org",
                                                    "getStatusData")));
                                                
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
                                             org.wso2.carbon.identity.entitlement.stub.types.axis2.GetStatusDataResponse.class,
                                              getEnvelopeNamespaces(_returnEnv));

                               
                                        return getGetStatusDataResponse_return((org.wso2.carbon.identity.entitlement.stub.types.axis2.GetStatusDataResponse)object);
                                   
         }catch(org.apache.axis2.AxisFault f){

            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt!=null){
                if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getStatusData"))){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getStatusData"));
                        java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                        java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
                        //message class
                        java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getStatusData"));
                        java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                        java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                        java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                   new java.lang.Class[]{messageClass});
                        m.invoke(ex,new java.lang.Object[]{messageObject});
                        
                        if (ex instanceof org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException){
                          throw (org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException)ex;
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
                * @see org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminService#startgetStatusData
                    * @param getStatusData148
                
                */
                public  void startgetStatusData(

                 java.lang.String about149,java.lang.String key150,java.lang.String type151,java.lang.String searchString152,int pageNumber153,

                  final org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceCallbackHandler callback)

                throws java.rmi.RemoteException{

              org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[4].getName());
             _operationClient.getOptions().setAction("urn:getStatusData");
             _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              


              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env=null;
              final org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();

                    
                                    //Style is Doc.
                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.GetStatusData dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    about149,
                                                    key150,
                                                    type151,
                                                    searchString152,
                                                    pageNumber153,
                                                     dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org",
                                                    "getStatusData")));
                                                
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
                                                                         org.wso2.carbon.identity.entitlement.stub.types.axis2.GetStatusDataResponse.class,
                                                                         getEnvelopeNamespaces(resultEnv));
                                        callback.receiveResultgetStatusData(
                                            getGetStatusDataResponse_return((org.wso2.carbon.identity.entitlement.stub.types.axis2.GetStatusDataResponse)object));
                                        
                            } catch (org.apache.axis2.AxisFault e) {
                                callback.receiveErrorgetStatusData(e);
                            }
                            }

                            public void onError(java.lang.Exception error) {
								if (error instanceof org.apache.axis2.AxisFault) {
									org.apache.axis2.AxisFault f = (org.apache.axis2.AxisFault) error;
									org.apache.axiom.om.OMElement faultElt = f.getDetail();
									if (faultElt!=null){
										if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getStatusData"))){
											//make the fault by reflection
											try{
													java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getStatusData"));
													java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
													java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
													//message class
													java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getStatusData"));
														java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
													java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
													java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
															new java.lang.Class[]{messageClass});
													m.invoke(ex,new java.lang.Object[]{messageObject});
													
													if (ex instanceof org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException){
														callback.receiveErrorgetStatusData((org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException)ex);
											            return;
										            }
										            
					
										            callback.receiveErrorgetStatusData(new java.rmi.RemoteException(ex.getMessage(), ex));
                                            } catch(java.lang.ClassCastException e){
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetStatusData(f);
                                            } catch (java.lang.ClassNotFoundException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetStatusData(f);
                                            } catch (java.lang.NoSuchMethodException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetStatusData(f);
                                            } catch (java.lang.reflect.InvocationTargetException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetStatusData(f);
                                            } catch (java.lang.IllegalAccessException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetStatusData(f);
                                            } catch (java.lang.InstantiationException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetStatusData(f);
                                            } catch (org.apache.axis2.AxisFault e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetStatusData(f);
                                            }
									    } else {
										    callback.receiveErrorgetStatusData(f);
									    }
									} else {
									    callback.receiveErrorgetStatusData(f);
									}
								} else {
								    callback.receiveErrorgetStatusData(error);
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
                                    callback.receiveErrorgetStatusData(axisFault);
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
                     * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
                  */
                public void  addPolicy(
                 org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO policyDTO157

                ) throws java.rmi.RemoteException
                
                
                        ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException
                {
                org.apache.axis2.context.MessageContext _messageContext = null;

                try {
                org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[5].getName());
                _operationClient.getOptions().setAction("urn:addPolicy");
                _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

                
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              
                org.apache.axiom.soap.SOAPEnvelope env = null;
                 _messageContext = new org.apache.axis2.context.MessageContext();

                
                                                    //Style is Doc.
                                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.AddPolicy dummyWrappedType = null;
                                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                                    policyDTO157,
                                                                    dummyWrappedType,
                                                                    optimizeContent(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org",
                                                                    "addPolicy")));
                                                                

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
                      if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"addPolicy"))){
                          //make the fault by reflection
                          try{
                              java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"addPolicy"));
                              java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                              java.lang.Exception ex=
                                      (java.lang.Exception) exceptionClass.newInstance();
                              //message class
                              java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"addPolicy"));
                              java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                              java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                              java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                         new java.lang.Class[]{messageClass});
                              m.invoke(ex,new java.lang.Object[]{messageObject});
                              
                              if (ex instanceof org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException){
                                throw (org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException)ex;
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
                     * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
                  */
                public void  removePolicy(
                 java.lang.String policyId159,boolean dePromote160

                ) throws java.rmi.RemoteException
                
                
                        ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException
                {
                org.apache.axis2.context.MessageContext _messageContext = null;

                try {
                org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[6].getName());
                _operationClient.getOptions().setAction("urn:removePolicy");
                _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

                
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              
                org.apache.axiom.soap.SOAPEnvelope env = null;
                 _messageContext = new org.apache.axis2.context.MessageContext();

                
                                                    //Style is Doc.
                                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.RemovePolicy dummyWrappedType = null;
                                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                                    policyId159,
                                                                    dePromote160,
                                                                    dummyWrappedType,
                                                                    optimizeContent(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org",
                                                                    "removePolicy")));
                                                                

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
                      if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"removePolicy"))){
                          //make the fault by reflection
                          try{
                              java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"removePolicy"));
                              java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                              java.lang.Exception ex=
                                      (java.lang.Exception) exceptionClass.newInstance();
                              //message class
                              java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"removePolicy"));
                              java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                              java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                              java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                         new java.lang.Class[]{messageClass});
                              m.invoke(ex,new java.lang.Object[]{messageObject});
                              
                              if (ex instanceof org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException){
                                throw (org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException)ex;
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
                     * @see org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminService#getSubscriber
                     * @param getSubscriber161
                    
                     * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
                     */

                    

                            public  org.wso2.carbon.identity.entitlement.stub.dto.PublisherDataHolder getSubscriber(

                            java.lang.String subscribeId162)
                        

                    throws java.rmi.RemoteException
                    
                    
                        ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException{
              org.apache.axis2.context.MessageContext _messageContext = null;
              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[7].getName());
              _operationClient.getOptions().setAction("urn:getSubscriber");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              

              // create a message context
              _messageContext = new org.apache.axis2.context.MessageContext();

              

              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;
                    org.wso2.carbon.identity.entitlement.stub.types.axis2.GetSubscriber dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    subscribeId162,
                                                    dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org",
                                                    "getSubscriber")));
                                                
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
                                             org.wso2.carbon.identity.entitlement.stub.types.axis2.GetSubscriberResponse.class,
                                              getEnvelopeNamespaces(_returnEnv));

                               
                                        return getGetSubscriberResponse_return((org.wso2.carbon.identity.entitlement.stub.types.axis2.GetSubscriberResponse)object);
                                   
         }catch(org.apache.axis2.AxisFault f){

            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt!=null){
                if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getSubscriber"))){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getSubscriber"));
                        java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                        java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
                        //message class
                        java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getSubscriber"));
                        java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                        java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                        java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                   new java.lang.Class[]{messageClass});
                        m.invoke(ex,new java.lang.Object[]{messageObject});
                        
                        if (ex instanceof org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException){
                          throw (org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException)ex;
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
                * @see org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminService#startgetSubscriber
                    * @param getSubscriber161
                
                */
                public  void startgetSubscriber(

                 java.lang.String subscribeId162,

                  final org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceCallbackHandler callback)

                throws java.rmi.RemoteException{

              org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[7].getName());
             _operationClient.getOptions().setAction("urn:getSubscriber");
             _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              


              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env=null;
              final org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();

                    
                                    //Style is Doc.
                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.GetSubscriber dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    subscribeId162,
                                                     dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org",
                                                    "getSubscriber")));
                                                
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
                                                                         org.wso2.carbon.identity.entitlement.stub.types.axis2.GetSubscriberResponse.class,
                                                                         getEnvelopeNamespaces(resultEnv));
                                        callback.receiveResultgetSubscriber(
                                            getGetSubscriberResponse_return((org.wso2.carbon.identity.entitlement.stub.types.axis2.GetSubscriberResponse)object));
                                        
                            } catch (org.apache.axis2.AxisFault e) {
                                callback.receiveErrorgetSubscriber(e);
                            }
                            }

                            public void onError(java.lang.Exception error) {
								if (error instanceof org.apache.axis2.AxisFault) {
									org.apache.axis2.AxisFault f = (org.apache.axis2.AxisFault) error;
									org.apache.axiom.om.OMElement faultElt = f.getDetail();
									if (faultElt!=null){
										if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getSubscriber"))){
											//make the fault by reflection
											try{
													java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getSubscriber"));
													java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
													java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
													//message class
													java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getSubscriber"));
														java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
													java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
													java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
															new java.lang.Class[]{messageClass});
													m.invoke(ex,new java.lang.Object[]{messageObject});
													
													if (ex instanceof org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException){
														callback.receiveErrorgetSubscriber((org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException)ex);
											            return;
										            }
										            
					
										            callback.receiveErrorgetSubscriber(new java.rmi.RemoteException(ex.getMessage(), ex));
                                            } catch(java.lang.ClassCastException e){
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetSubscriber(f);
                                            } catch (java.lang.ClassNotFoundException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetSubscriber(f);
                                            } catch (java.lang.NoSuchMethodException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetSubscriber(f);
                                            } catch (java.lang.reflect.InvocationTargetException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetSubscriber(f);
                                            } catch (java.lang.IllegalAccessException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetSubscriber(f);
                                            } catch (java.lang.InstantiationException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetSubscriber(f);
                                            } catch (org.apache.axis2.AxisFault e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetSubscriber(f);
                                            }
									    } else {
										    callback.receiveErrorgetSubscriber(f);
									    }
									} else {
									    callback.receiveErrorgetSubscriber(f);
									}
								} else {
								    callback.receiveErrorgetSubscriber(error);
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
                                    callback.receiveErrorgetSubscriber(axisFault);
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
                     * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
                  */
                public void  updateSubscriber(
                 org.wso2.carbon.identity.entitlement.stub.dto.PublisherDataHolder holder166

                ) throws java.rmi.RemoteException
                
                
                        ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException
                {
                org.apache.axis2.context.MessageContext _messageContext = null;

                try {
                org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[8].getName());
                _operationClient.getOptions().setAction("urn:updateSubscriber");
                _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

                
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              
                org.apache.axiom.soap.SOAPEnvelope env = null;
                 _messageContext = new org.apache.axis2.context.MessageContext();

                
                                                    //Style is Doc.
                                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.UpdateSubscriber dummyWrappedType = null;
                                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                                    holder166,
                                                                    dummyWrappedType,
                                                                    optimizeContent(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org",
                                                                    "updateSubscriber")));
                                                                

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
                      if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"updateSubscriber"))){
                          //make the fault by reflection
                          try{
                              java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"updateSubscriber"));
                              java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                              java.lang.Exception ex=
                                      (java.lang.Exception) exceptionClass.newInstance();
                              //message class
                              java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"updateSubscriber"));
                              java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                              java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                              java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                         new java.lang.Class[]{messageClass});
                              m.invoke(ex,new java.lang.Object[]{messageObject});
                              
                              if (ex instanceof org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException){
                                throw (org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException)ex;
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
                     * @see org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminService#getPolicy
                     * @param getPolicy167
                    
                     * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
                     */

                    

                            public  org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO getPolicy(

                            java.lang.String policyId168,boolean isPDPPolicy169)
                        

                    throws java.rmi.RemoteException
                    
                    
                        ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException{
              org.apache.axis2.context.MessageContext _messageContext = null;
              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[9].getName());
              _operationClient.getOptions().setAction("urn:getPolicy");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              

              // create a message context
              _messageContext = new org.apache.axis2.context.MessageContext();

              

              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;
                    org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicy dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    policyId168,
                                                    isPDPPolicy169,
                                                    dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org",
                                                    "getPolicy")));
                                                
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
                                             org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicyResponse.class,
                                              getEnvelopeNamespaces(_returnEnv));

                               
                                        return getGetPolicyResponse_return((org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicyResponse)object);
                                   
         }catch(org.apache.axis2.AxisFault f){

            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt!=null){
                if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getPolicy"))){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getPolicy"));
                        java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                        java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
                        //message class
                        java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getPolicy"));
                        java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                        java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                        java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                   new java.lang.Class[]{messageClass});
                        m.invoke(ex,new java.lang.Object[]{messageObject});
                        
                        if (ex instanceof org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException){
                          throw (org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException)ex;
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
                * @see org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminService#startgetPolicy
                    * @param getPolicy167
                
                */
                public  void startgetPolicy(

                 java.lang.String policyId168,boolean isPDPPolicy169,

                  final org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceCallbackHandler callback)

                throws java.rmi.RemoteException{

              org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[9].getName());
             _operationClient.getOptions().setAction("urn:getPolicy");
             _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              


              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env=null;
              final org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();

                    
                                    //Style is Doc.
                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicy dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    policyId168,
                                                    isPDPPolicy169,
                                                     dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org",
                                                    "getPolicy")));
                                                
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
                                                                         org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicyResponse.class,
                                                                         getEnvelopeNamespaces(resultEnv));
                                        callback.receiveResultgetPolicy(
                                            getGetPolicyResponse_return((org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicyResponse)object));
                                        
                            } catch (org.apache.axis2.AxisFault e) {
                                callback.receiveErrorgetPolicy(e);
                            }
                            }

                            public void onError(java.lang.Exception error) {
								if (error instanceof org.apache.axis2.AxisFault) {
									org.apache.axis2.AxisFault f = (org.apache.axis2.AxisFault) error;
									org.apache.axiom.om.OMElement faultElt = f.getDetail();
									if (faultElt!=null){
										if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getPolicy"))){
											//make the fault by reflection
											try{
													java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getPolicy"));
													java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
													java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
													//message class
													java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getPolicy"));
														java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
													java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
													java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
															new java.lang.Class[]{messageClass});
													m.invoke(ex,new java.lang.Object[]{messageObject});
													
													if (ex instanceof org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException){
														callback.receiveErrorgetPolicy((org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException)ex);
											            return;
										            }
										            
					
										            callback.receiveErrorgetPolicy(new java.rmi.RemoteException(ex.getMessage(), ex));
                                            } catch(java.lang.ClassCastException e){
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetPolicy(f);
                                            } catch (java.lang.ClassNotFoundException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetPolicy(f);
                                            } catch (java.lang.NoSuchMethodException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetPolicy(f);
                                            } catch (java.lang.reflect.InvocationTargetException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetPolicy(f);
                                            } catch (java.lang.IllegalAccessException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetPolicy(f);
                                            } catch (java.lang.InstantiationException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetPolicy(f);
                                            } catch (org.apache.axis2.AxisFault e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetPolicy(f);
                                            }
									    } else {
										    callback.receiveErrorgetPolicy(f);
									    }
									} else {
									    callback.receiveErrorgetPolicy(f);
									}
								} else {
								    callback.receiveErrorgetPolicy(error);
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
                                    callback.receiveErrorgetPolicy(axisFault);
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
                     * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
                  */
                public void  publishPolicies(
                 java.lang.String[] policyIds173,java.lang.String[] subscriberIds174,java.lang.String action175,java.lang.String version176,boolean enabled177,int order178

                ) throws java.rmi.RemoteException
                
                
                        ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException
                {
                org.apache.axis2.context.MessageContext _messageContext = null;

                try {
                org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[10].getName());
                _operationClient.getOptions().setAction("urn:publishPolicies");
                _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

                
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              
                org.apache.axiom.soap.SOAPEnvelope env = null;
                 _messageContext = new org.apache.axis2.context.MessageContext();

                
                                                    //Style is Doc.
                                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.PublishPolicies dummyWrappedType = null;
                                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                                    policyIds173,
                                                                    subscriberIds174,
                                                                    action175,
                                                                    version176,
                                                                    enabled177,
                                                                    order178,
                                                                    dummyWrappedType,
                                                                    optimizeContent(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org",
                                                                    "publishPolicies")));
                                                                

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
                      if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"publishPolicies"))){
                          //make the fault by reflection
                          try{
                              java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"publishPolicies"));
                              java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                              java.lang.Exception ex=
                                      (java.lang.Exception) exceptionClass.newInstance();
                              //message class
                              java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"publishPolicies"));
                              java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                              java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                              java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                         new java.lang.Class[]{messageClass});
                              m.invoke(ex,new java.lang.Object[]{messageObject});
                              
                              if (ex instanceof org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException){
                                throw (org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException)ex;
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
                     * @see org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminService#getAllPolicyIds
                     * @param getAllPolicyIds179
                    
                     * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
                     */

                    

                            public  java.lang.String[] getAllPolicyIds(

                            java.lang.String searchString180)
                        

                    throws java.rmi.RemoteException
                    
                    
                        ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException{
              org.apache.axis2.context.MessageContext _messageContext = null;
              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[11].getName());
              _operationClient.getOptions().setAction("urn:getAllPolicyIds");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              

              // create a message context
              _messageContext = new org.apache.axis2.context.MessageContext();

              

              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;
                    org.wso2.carbon.identity.entitlement.stub.types.axis2.GetAllPolicyIds dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    searchString180,
                                                    dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org",
                                                    "getAllPolicyIds")));
                                                
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
                                             org.wso2.carbon.identity.entitlement.stub.types.axis2.GetAllPolicyIdsResponse.class,
                                              getEnvelopeNamespaces(_returnEnv));

                               
                                        return getGetAllPolicyIdsResponse_return((org.wso2.carbon.identity.entitlement.stub.types.axis2.GetAllPolicyIdsResponse)object);
                                   
         }catch(org.apache.axis2.AxisFault f){

            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt!=null){
                if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getAllPolicyIds"))){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getAllPolicyIds"));
                        java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                        java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
                        //message class
                        java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getAllPolicyIds"));
                        java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                        java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                        java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                   new java.lang.Class[]{messageClass});
                        m.invoke(ex,new java.lang.Object[]{messageObject});
                        
                        if (ex instanceof org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException){
                          throw (org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException)ex;
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
                * @see org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminService#startgetAllPolicyIds
                    * @param getAllPolicyIds179
                
                */
                public  void startgetAllPolicyIds(

                 java.lang.String searchString180,

                  final org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceCallbackHandler callback)

                throws java.rmi.RemoteException{

              org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[11].getName());
             _operationClient.getOptions().setAction("urn:getAllPolicyIds");
             _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              


              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env=null;
              final org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();

                    
                                    //Style is Doc.
                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.GetAllPolicyIds dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    searchString180,
                                                     dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org",
                                                    "getAllPolicyIds")));
                                                
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
                                                                         org.wso2.carbon.identity.entitlement.stub.types.axis2.GetAllPolicyIdsResponse.class,
                                                                         getEnvelopeNamespaces(resultEnv));
                                        callback.receiveResultgetAllPolicyIds(
                                            getGetAllPolicyIdsResponse_return((org.wso2.carbon.identity.entitlement.stub.types.axis2.GetAllPolicyIdsResponse)object));
                                        
                            } catch (org.apache.axis2.AxisFault e) {
                                callback.receiveErrorgetAllPolicyIds(e);
                            }
                            }

                            public void onError(java.lang.Exception error) {
								if (error instanceof org.apache.axis2.AxisFault) {
									org.apache.axis2.AxisFault f = (org.apache.axis2.AxisFault) error;
									org.apache.axiom.om.OMElement faultElt = f.getDetail();
									if (faultElt!=null){
										if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getAllPolicyIds"))){
											//make the fault by reflection
											try{
													java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getAllPolicyIds"));
													java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
													java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
													//message class
													java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getAllPolicyIds"));
														java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
													java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
													java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
															new java.lang.Class[]{messageClass});
													m.invoke(ex,new java.lang.Object[]{messageObject});
													
													if (ex instanceof org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException){
														callback.receiveErrorgetAllPolicyIds((org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException)ex);
											            return;
										            }
										            
					
										            callback.receiveErrorgetAllPolicyIds(new java.rmi.RemoteException(ex.getMessage(), ex));
                                            } catch(java.lang.ClassCastException e){
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetAllPolicyIds(f);
                                            } catch (java.lang.ClassNotFoundException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetAllPolicyIds(f);
                                            } catch (java.lang.NoSuchMethodException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetAllPolicyIds(f);
                                            } catch (java.lang.reflect.InvocationTargetException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetAllPolicyIds(f);
                                            } catch (java.lang.IllegalAccessException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetAllPolicyIds(f);
                                            } catch (java.lang.InstantiationException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetAllPolicyIds(f);
                                            } catch (org.apache.axis2.AxisFault e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetAllPolicyIds(f);
                                            }
									    } else {
										    callback.receiveErrorgetAllPolicyIds(f);
									    }
									} else {
									    callback.receiveErrorgetAllPolicyIds(f);
									}
								} else {
								    callback.receiveErrorgetAllPolicyIds(error);
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
                                    callback.receiveErrorgetAllPolicyIds(axisFault);
                                }
                            }
                });
                        

          org.apache.axis2.util.CallbackReceiver _callbackReceiver = null;
        if ( _operations[11].getMessageReceiver()==null &&  _operationClient.getOptions().isUseSeparateListener()) {
           _callbackReceiver = new org.apache.axis2.util.CallbackReceiver();
          _operations[11].setMessageReceiver(
                    _callbackReceiver);
        }

           //execute the operation client
           _operationClient.execute(false);

                    }
                
                    /**
                     * Auto generated method signature
                     * 
                     * @see org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminService#getEntitlementDataModules
                     * @param getEntitlementDataModules183
                    
                     */

                    

                            public  org.wso2.carbon.identity.entitlement.stub.dto.EntitlementFinderDataHolder[] getEntitlementDataModules(

                            )
                        

                    throws java.rmi.RemoteException
                    
                    {
              org.apache.axis2.context.MessageContext _messageContext = null;
              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[12].getName());
              _operationClient.getOptions().setAction("urn:getEntitlementDataModules");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              

              // create a message context
              _messageContext = new org.apache.axis2.context.MessageContext();

              

              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;
                    org.wso2.carbon.identity.entitlement.stub.types.axis2.GetEntitlementDataModules dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org",
                                                    "getEntitlementDataModules")));
                                                
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
                                             org.wso2.carbon.identity.entitlement.stub.types.axis2.GetEntitlementDataModulesResponse.class,
                                              getEnvelopeNamespaces(_returnEnv));

                               
                                        return getGetEntitlementDataModulesResponse_return((org.wso2.carbon.identity.entitlement.stub.types.axis2.GetEntitlementDataModulesResponse)object);
                                   
         }catch(org.apache.axis2.AxisFault f){

            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt!=null){
                if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getEntitlementDataModules"))){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getEntitlementDataModules"));
                        java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                        java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
                        //message class
                        java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getEntitlementDataModules"));
                        java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                        java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                        java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                   new java.lang.Class[]{messageClass});
                        m.invoke(ex,new java.lang.Object[]{messageObject});
                        

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
                * @see org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminService#startgetEntitlementDataModules
                    * @param getEntitlementDataModules183
                
                */
                public  void startgetEntitlementDataModules(

                 

                  final org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceCallbackHandler callback)

                throws java.rmi.RemoteException{

              org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[12].getName());
             _operationClient.getOptions().setAction("urn:getEntitlementDataModules");
             _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              


              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env=null;
              final org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();

                    
                                    //Style is Doc.
                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.GetEntitlementDataModules dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                     dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org",
                                                    "getEntitlementDataModules")));
                                                
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
                                                                         org.wso2.carbon.identity.entitlement.stub.types.axis2.GetEntitlementDataModulesResponse.class,
                                                                         getEnvelopeNamespaces(resultEnv));
                                        callback.receiveResultgetEntitlementDataModules(
                                            getGetEntitlementDataModulesResponse_return((org.wso2.carbon.identity.entitlement.stub.types.axis2.GetEntitlementDataModulesResponse)object));
                                        
                            } catch (org.apache.axis2.AxisFault e) {
                                callback.receiveErrorgetEntitlementDataModules(e);
                            }
                            }

                            public void onError(java.lang.Exception error) {
								if (error instanceof org.apache.axis2.AxisFault) {
									org.apache.axis2.AxisFault f = (org.apache.axis2.AxisFault) error;
									org.apache.axiom.om.OMElement faultElt = f.getDetail();
									if (faultElt!=null){
										if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getEntitlementDataModules"))){
											//make the fault by reflection
											try{
													java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getEntitlementDataModules"));
													java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
													java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
													//message class
													java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getEntitlementDataModules"));
														java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
													java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
													java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
															new java.lang.Class[]{messageClass});
													m.invoke(ex,new java.lang.Object[]{messageObject});
													
					
										            callback.receiveErrorgetEntitlementDataModules(new java.rmi.RemoteException(ex.getMessage(), ex));
                                            } catch(java.lang.ClassCastException e){
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetEntitlementDataModules(f);
                                            } catch (java.lang.ClassNotFoundException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetEntitlementDataModules(f);
                                            } catch (java.lang.NoSuchMethodException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetEntitlementDataModules(f);
                                            } catch (java.lang.reflect.InvocationTargetException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetEntitlementDataModules(f);
                                            } catch (java.lang.IllegalAccessException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetEntitlementDataModules(f);
                                            } catch (java.lang.InstantiationException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetEntitlementDataModules(f);
                                            } catch (org.apache.axis2.AxisFault e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetEntitlementDataModules(f);
                                            }
									    } else {
										    callback.receiveErrorgetEntitlementDataModules(f);
									    }
									} else {
									    callback.receiveErrorgetEntitlementDataModules(f);
									}
								} else {
								    callback.receiveErrorgetEntitlementDataModules(error);
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
                                    callback.receiveErrorgetEntitlementDataModules(axisFault);
                                }
                            }
                });
                        

          org.apache.axis2.util.CallbackReceiver _callbackReceiver = null;
        if ( _operations[12].getMessageReceiver()==null &&  _operationClient.getOptions().isUseSeparateListener()) {
           _callbackReceiver = new org.apache.axis2.util.CallbackReceiver();
          _operations[12].setMessageReceiver(
                    _callbackReceiver);
        }

           //execute the operation client
           _operationClient.execute(false);

                    }
                
                    /**
                     * Auto generated method signature
                     * 
                     * @see org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminService#getSubscriberIds
                     * @param getSubscriberIds186
                    
                     * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
                     */

                    

                            public  java.lang.String[] getSubscriberIds(

                            java.lang.String searchString187)
                        

                    throws java.rmi.RemoteException
                    
                    
                        ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException{
              org.apache.axis2.context.MessageContext _messageContext = null;
              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[13].getName());
              _operationClient.getOptions().setAction("urn:getSubscriberIds");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              

              // create a message context
              _messageContext = new org.apache.axis2.context.MessageContext();

              

              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;
                    org.wso2.carbon.identity.entitlement.stub.types.axis2.GetSubscriberIds dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    searchString187,
                                                    dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org",
                                                    "getSubscriberIds")));
                                                
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
                                             org.wso2.carbon.identity.entitlement.stub.types.axis2.GetSubscriberIdsResponse.class,
                                              getEnvelopeNamespaces(_returnEnv));

                               
                                        return getGetSubscriberIdsResponse_return((org.wso2.carbon.identity.entitlement.stub.types.axis2.GetSubscriberIdsResponse)object);
                                   
         }catch(org.apache.axis2.AxisFault f){

            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt!=null){
                if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getSubscriberIds"))){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getSubscriberIds"));
                        java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                        java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
                        //message class
                        java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getSubscriberIds"));
                        java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                        java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                        java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                   new java.lang.Class[]{messageClass});
                        m.invoke(ex,new java.lang.Object[]{messageObject});
                        
                        if (ex instanceof org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException){
                          throw (org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException)ex;
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
                * @see org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminService#startgetSubscriberIds
                    * @param getSubscriberIds186
                
                */
                public  void startgetSubscriberIds(

                 java.lang.String searchString187,

                  final org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceCallbackHandler callback)

                throws java.rmi.RemoteException{

              org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[13].getName());
             _operationClient.getOptions().setAction("urn:getSubscriberIds");
             _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              


              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env=null;
              final org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();

                    
                                    //Style is Doc.
                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.GetSubscriberIds dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    searchString187,
                                                     dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org",
                                                    "getSubscriberIds")));
                                                
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
                                                                         org.wso2.carbon.identity.entitlement.stub.types.axis2.GetSubscriberIdsResponse.class,
                                                                         getEnvelopeNamespaces(resultEnv));
                                        callback.receiveResultgetSubscriberIds(
                                            getGetSubscriberIdsResponse_return((org.wso2.carbon.identity.entitlement.stub.types.axis2.GetSubscriberIdsResponse)object));
                                        
                            } catch (org.apache.axis2.AxisFault e) {
                                callback.receiveErrorgetSubscriberIds(e);
                            }
                            }

                            public void onError(java.lang.Exception error) {
								if (error instanceof org.apache.axis2.AxisFault) {
									org.apache.axis2.AxisFault f = (org.apache.axis2.AxisFault) error;
									org.apache.axiom.om.OMElement faultElt = f.getDetail();
									if (faultElt!=null){
										if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getSubscriberIds"))){
											//make the fault by reflection
											try{
													java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getSubscriberIds"));
													java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
													java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
													//message class
													java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getSubscriberIds"));
														java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
													java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
													java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
															new java.lang.Class[]{messageClass});
													m.invoke(ex,new java.lang.Object[]{messageObject});
													
													if (ex instanceof org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException){
														callback.receiveErrorgetSubscriberIds((org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException)ex);
											            return;
										            }
										            
					
										            callback.receiveErrorgetSubscriberIds(new java.rmi.RemoteException(ex.getMessage(), ex));
                                            } catch(java.lang.ClassCastException e){
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetSubscriberIds(f);
                                            } catch (java.lang.ClassNotFoundException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetSubscriberIds(f);
                                            } catch (java.lang.NoSuchMethodException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetSubscriberIds(f);
                                            } catch (java.lang.reflect.InvocationTargetException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetSubscriberIds(f);
                                            } catch (java.lang.IllegalAccessException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetSubscriberIds(f);
                                            } catch (java.lang.InstantiationException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetSubscriberIds(f);
                                            } catch (org.apache.axis2.AxisFault e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetSubscriberIds(f);
                                            }
									    } else {
										    callback.receiveErrorgetSubscriberIds(f);
									    }
									} else {
									    callback.receiveErrorgetSubscriberIds(f);
									}
								} else {
								    callback.receiveErrorgetSubscriberIds(error);
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
                                    callback.receiveErrorgetSubscriberIds(axisFault);
                                }
                            }
                });
                        

          org.apache.axis2.util.CallbackReceiver _callbackReceiver = null;
        if ( _operations[13].getMessageReceiver()==null &&  _operationClient.getOptions().isUseSeparateListener()) {
           _callbackReceiver = new org.apache.axis2.util.CallbackReceiver();
          _operations[13].setMessageReceiver(
                    _callbackReceiver);
        }

           //execute the operation client
           _operationClient.execute(false);

                    }
                 
                
                /**
                  * Auto generated method signature
                  * 
                     * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
                  */
                public void  rollBackPolicy(
                 java.lang.String policyId191,java.lang.String version192

                ) throws java.rmi.RemoteException
                
                
                        ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException
                {
                org.apache.axis2.context.MessageContext _messageContext = null;

                try {
                org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[14].getName());
                _operationClient.getOptions().setAction("urn:rollBackPolicy");
                _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

                
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              
                org.apache.axiom.soap.SOAPEnvelope env = null;
                 _messageContext = new org.apache.axis2.context.MessageContext();

                
                                                    //Style is Doc.
                                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.RollBackPolicy dummyWrappedType = null;
                                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                                    policyId191,
                                                                    version192,
                                                                    dummyWrappedType,
                                                                    optimizeContent(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org",
                                                                    "rollBackPolicy")));
                                                                

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
                      if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"rollBackPolicy"))){
                          //make the fault by reflection
                          try{
                              java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"rollBackPolicy"));
                              java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                              java.lang.Exception ex=
                                      (java.lang.Exception) exceptionClass.newInstance();
                              //message class
                              java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"rollBackPolicy"));
                              java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                              java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                              java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                         new java.lang.Class[]{messageClass});
                              m.invoke(ex,new java.lang.Object[]{messageObject});
                              
                              if (ex instanceof org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException){
                                throw (org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException)ex;
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
                     * @see org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminService#getLightPolicy
                     * @param getLightPolicy193
                    
                     * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
                     */

                    

                            public  org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO getLightPolicy(

                            java.lang.String policyId194)
                        

                    throws java.rmi.RemoteException
                    
                    
                        ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException{
              org.apache.axis2.context.MessageContext _messageContext = null;
              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[15].getName());
              _operationClient.getOptions().setAction("urn:getLightPolicy");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              

              // create a message context
              _messageContext = new org.apache.axis2.context.MessageContext();

              

              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;
                    org.wso2.carbon.identity.entitlement.stub.types.axis2.GetLightPolicy dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    policyId194,
                                                    dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org",
                                                    "getLightPolicy")));
                                                
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
                                             org.wso2.carbon.identity.entitlement.stub.types.axis2.GetLightPolicyResponse.class,
                                              getEnvelopeNamespaces(_returnEnv));

                               
                                        return getGetLightPolicyResponse_return((org.wso2.carbon.identity.entitlement.stub.types.axis2.GetLightPolicyResponse)object);
                                   
         }catch(org.apache.axis2.AxisFault f){

            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt!=null){
                if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getLightPolicy"))){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getLightPolicy"));
                        java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                        java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
                        //message class
                        java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getLightPolicy"));
                        java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                        java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                        java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                   new java.lang.Class[]{messageClass});
                        m.invoke(ex,new java.lang.Object[]{messageObject});
                        
                        if (ex instanceof org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException){
                          throw (org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException)ex;
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
                * @see org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminService#startgetLightPolicy
                    * @param getLightPolicy193
                
                */
                public  void startgetLightPolicy(

                 java.lang.String policyId194,

                  final org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceCallbackHandler callback)

                throws java.rmi.RemoteException{

              org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[15].getName());
             _operationClient.getOptions().setAction("urn:getLightPolicy");
             _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              


              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env=null;
              final org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();

                    
                                    //Style is Doc.
                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.GetLightPolicy dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    policyId194,
                                                     dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org",
                                                    "getLightPolicy")));
                                                
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
                                                                         org.wso2.carbon.identity.entitlement.stub.types.axis2.GetLightPolicyResponse.class,
                                                                         getEnvelopeNamespaces(resultEnv));
                                        callback.receiveResultgetLightPolicy(
                                            getGetLightPolicyResponse_return((org.wso2.carbon.identity.entitlement.stub.types.axis2.GetLightPolicyResponse)object));
                                        
                            } catch (org.apache.axis2.AxisFault e) {
                                callback.receiveErrorgetLightPolicy(e);
                            }
                            }

                            public void onError(java.lang.Exception error) {
								if (error instanceof org.apache.axis2.AxisFault) {
									org.apache.axis2.AxisFault f = (org.apache.axis2.AxisFault) error;
									org.apache.axiom.om.OMElement faultElt = f.getDetail();
									if (faultElt!=null){
										if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getLightPolicy"))){
											//make the fault by reflection
											try{
													java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getLightPolicy"));
													java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
													java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
													//message class
													java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getLightPolicy"));
														java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
													java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
													java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
															new java.lang.Class[]{messageClass});
													m.invoke(ex,new java.lang.Object[]{messageObject});
													
													if (ex instanceof org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException){
														callback.receiveErrorgetLightPolicy((org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException)ex);
											            return;
										            }
										            
					
										            callback.receiveErrorgetLightPolicy(new java.rmi.RemoteException(ex.getMessage(), ex));
                                            } catch(java.lang.ClassCastException e){
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetLightPolicy(f);
                                            } catch (java.lang.ClassNotFoundException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetLightPolicy(f);
                                            } catch (java.lang.NoSuchMethodException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetLightPolicy(f);
                                            } catch (java.lang.reflect.InvocationTargetException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetLightPolicy(f);
                                            } catch (java.lang.IllegalAccessException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetLightPolicy(f);
                                            } catch (java.lang.InstantiationException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetLightPolicy(f);
                                            } catch (org.apache.axis2.AxisFault e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetLightPolicy(f);
                                            }
									    } else {
										    callback.receiveErrorgetLightPolicy(f);
									    }
									} else {
									    callback.receiveErrorgetLightPolicy(f);
									}
								} else {
								    callback.receiveErrorgetLightPolicy(error);
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
                                    callback.receiveErrorgetLightPolicy(axisFault);
                                }
                            }
                });
                        

          org.apache.axis2.util.CallbackReceiver _callbackReceiver = null;
        if ( _operations[15].getMessageReceiver()==null &&  _operationClient.getOptions().isUseSeparateListener()) {
           _callbackReceiver = new org.apache.axis2.util.CallbackReceiver();
          _operations[15].setMessageReceiver(
                    _callbackReceiver);
        }

           //execute the operation client
           _operationClient.execute(false);

                    }
                 
                
                /**
                  * Auto generated method signature
                  * 
                     * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
                  */
                public void  importPolicyFromRegistry(
                 java.lang.String policyRegistryPath198

                ) throws java.rmi.RemoteException
                
                
                        ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException
                {
                org.apache.axis2.context.MessageContext _messageContext = null;

                try {
                org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[16].getName());
                _operationClient.getOptions().setAction("urn:importPolicyFromRegistry");
                _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

                
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              
                org.apache.axiom.soap.SOAPEnvelope env = null;
                 _messageContext = new org.apache.axis2.context.MessageContext();

                
                                                    //Style is Doc.
                                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.ImportPolicyFromRegistry dummyWrappedType = null;
                                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                                    policyRegistryPath198,
                                                                    dummyWrappedType,
                                                                    optimizeContent(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org",
                                                                    "importPolicyFromRegistry")));
                                                                

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
                      if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"importPolicyFromRegistry"))){
                          //make the fault by reflection
                          try{
                              java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"importPolicyFromRegistry"));
                              java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                              java.lang.Exception ex=
                                      (java.lang.Exception) exceptionClass.newInstance();
                              //message class
                              java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"importPolicyFromRegistry"));
                              java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                              java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                              java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                         new java.lang.Class[]{messageClass});
                              m.invoke(ex,new java.lang.Object[]{messageObject});
                              
                              if (ex instanceof org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException){
                                throw (org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException)ex;
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
                     * @see org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminService#getPublisherModuleData
                     * @param getPublisherModuleData199
                    
                     */

                    

                            public  org.wso2.carbon.identity.entitlement.stub.dto.PublisherDataHolder[] getPublisherModuleData(

                            )
                        

                    throws java.rmi.RemoteException
                    
                    {
              org.apache.axis2.context.MessageContext _messageContext = null;
              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[17].getName());
              _operationClient.getOptions().setAction("urn:getPublisherModuleData");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              

              // create a message context
              _messageContext = new org.apache.axis2.context.MessageContext();

              

              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;
                    org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPublisherModuleData dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org",
                                                    "getPublisherModuleData")));
                                                
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
                                             org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPublisherModuleDataResponse.class,
                                              getEnvelopeNamespaces(_returnEnv));

                               
                                        return getGetPublisherModuleDataResponse_return((org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPublisherModuleDataResponse)object);
                                   
         }catch(org.apache.axis2.AxisFault f){

            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt!=null){
                if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getPublisherModuleData"))){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getPublisherModuleData"));
                        java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                        java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
                        //message class
                        java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getPublisherModuleData"));
                        java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                        java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                        java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                   new java.lang.Class[]{messageClass});
                        m.invoke(ex,new java.lang.Object[]{messageObject});
                        

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
                * @see org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminService#startgetPublisherModuleData
                    * @param getPublisherModuleData199
                
                */
                public  void startgetPublisherModuleData(

                 

                  final org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceCallbackHandler callback)

                throws java.rmi.RemoteException{

              org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[17].getName());
             _operationClient.getOptions().setAction("urn:getPublisherModuleData");
             _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              


              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env=null;
              final org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();

                    
                                    //Style is Doc.
                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPublisherModuleData dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                     dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org",
                                                    "getPublisherModuleData")));
                                                
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
                                                                         org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPublisherModuleDataResponse.class,
                                                                         getEnvelopeNamespaces(resultEnv));
                                        callback.receiveResultgetPublisherModuleData(
                                            getGetPublisherModuleDataResponse_return((org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPublisherModuleDataResponse)object));
                                        
                            } catch (org.apache.axis2.AxisFault e) {
                                callback.receiveErrorgetPublisherModuleData(e);
                            }
                            }

                            public void onError(java.lang.Exception error) {
								if (error instanceof org.apache.axis2.AxisFault) {
									org.apache.axis2.AxisFault f = (org.apache.axis2.AxisFault) error;
									org.apache.axiom.om.OMElement faultElt = f.getDetail();
									if (faultElt!=null){
										if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getPublisherModuleData"))){
											//make the fault by reflection
											try{
													java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getPublisherModuleData"));
													java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
													java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
													//message class
													java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getPublisherModuleData"));
														java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
													java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
													java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
															new java.lang.Class[]{messageClass});
													m.invoke(ex,new java.lang.Object[]{messageObject});
													
					
										            callback.receiveErrorgetPublisherModuleData(new java.rmi.RemoteException(ex.getMessage(), ex));
                                            } catch(java.lang.ClassCastException e){
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetPublisherModuleData(f);
                                            } catch (java.lang.ClassNotFoundException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetPublisherModuleData(f);
                                            } catch (java.lang.NoSuchMethodException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetPublisherModuleData(f);
                                            } catch (java.lang.reflect.InvocationTargetException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetPublisherModuleData(f);
                                            } catch (java.lang.IllegalAccessException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetPublisherModuleData(f);
                                            } catch (java.lang.InstantiationException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetPublisherModuleData(f);
                                            } catch (org.apache.axis2.AxisFault e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetPublisherModuleData(f);
                                            }
									    } else {
										    callback.receiveErrorgetPublisherModuleData(f);
									    }
									} else {
									    callback.receiveErrorgetPublisherModuleData(f);
									}
								} else {
								    callback.receiveErrorgetPublisherModuleData(error);
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
                                    callback.receiveErrorgetPublisherModuleData(axisFault);
                                }
                            }
                });
                        

          org.apache.axis2.util.CallbackReceiver _callbackReceiver = null;
        if ( _operations[17].getMessageReceiver()==null &&  _operationClient.getOptions().isUseSeparateListener()) {
           _callbackReceiver = new org.apache.axis2.util.CallbackReceiver();
          _operations[17].setMessageReceiver(
                    _callbackReceiver);
        }

           //execute the operation client
           _operationClient.execute(false);

                    }
                
                    /**
                     * Auto generated method signature
                     * 
                     * @see org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminService#getPolicyVersions
                     * @param getPolicyVersions202
                    
                     * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
                     */

                    

                            public  java.lang.String[] getPolicyVersions(

                            java.lang.String policyId203)
                        

                    throws java.rmi.RemoteException
                    
                    
                        ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException{
              org.apache.axis2.context.MessageContext _messageContext = null;
              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[18].getName());
              _operationClient.getOptions().setAction("urn:getPolicyVersions");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              

              // create a message context
              _messageContext = new org.apache.axis2.context.MessageContext();

              

              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;
                    org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicyVersions dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    policyId203,
                                                    dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org",
                                                    "getPolicyVersions")));
                                                
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
                                             org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicyVersionsResponse.class,
                                              getEnvelopeNamespaces(_returnEnv));

                               
                                        return getGetPolicyVersionsResponse_return((org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicyVersionsResponse)object);
                                   
         }catch(org.apache.axis2.AxisFault f){

            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt!=null){
                if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getPolicyVersions"))){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getPolicyVersions"));
                        java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                        java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
                        //message class
                        java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getPolicyVersions"));
                        java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                        java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                        java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                   new java.lang.Class[]{messageClass});
                        m.invoke(ex,new java.lang.Object[]{messageObject});
                        
                        if (ex instanceof org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException){
                          throw (org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException)ex;
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
                * @see org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminService#startgetPolicyVersions
                    * @param getPolicyVersions202
                
                */
                public  void startgetPolicyVersions(

                 java.lang.String policyId203,

                  final org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceCallbackHandler callback)

                throws java.rmi.RemoteException{

              org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[18].getName());
             _operationClient.getOptions().setAction("urn:getPolicyVersions");
             _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              


              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env=null;
              final org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();

                    
                                    //Style is Doc.
                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicyVersions dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    policyId203,
                                                     dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org",
                                                    "getPolicyVersions")));
                                                
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
                                                                         org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicyVersionsResponse.class,
                                                                         getEnvelopeNamespaces(resultEnv));
                                        callback.receiveResultgetPolicyVersions(
                                            getGetPolicyVersionsResponse_return((org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicyVersionsResponse)object));
                                        
                            } catch (org.apache.axis2.AxisFault e) {
                                callback.receiveErrorgetPolicyVersions(e);
                            }
                            }

                            public void onError(java.lang.Exception error) {
								if (error instanceof org.apache.axis2.AxisFault) {
									org.apache.axis2.AxisFault f = (org.apache.axis2.AxisFault) error;
									org.apache.axiom.om.OMElement faultElt = f.getDetail();
									if (faultElt!=null){
										if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getPolicyVersions"))){
											//make the fault by reflection
											try{
													java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getPolicyVersions"));
													java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
													java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
													//message class
													java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getPolicyVersions"));
														java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
													java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
													java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
															new java.lang.Class[]{messageClass});
													m.invoke(ex,new java.lang.Object[]{messageObject});
													
													if (ex instanceof org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException){
														callback.receiveErrorgetPolicyVersions((org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException)ex);
											            return;
										            }
										            
					
										            callback.receiveErrorgetPolicyVersions(new java.rmi.RemoteException(ex.getMessage(), ex));
                                            } catch(java.lang.ClassCastException e){
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetPolicyVersions(f);
                                            } catch (java.lang.ClassNotFoundException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetPolicyVersions(f);
                                            } catch (java.lang.NoSuchMethodException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetPolicyVersions(f);
                                            } catch (java.lang.reflect.InvocationTargetException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetPolicyVersions(f);
                                            } catch (java.lang.IllegalAccessException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetPolicyVersions(f);
                                            } catch (java.lang.InstantiationException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetPolicyVersions(f);
                                            } catch (org.apache.axis2.AxisFault e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetPolicyVersions(f);
                                            }
									    } else {
										    callback.receiveErrorgetPolicyVersions(f);
									    }
									} else {
									    callback.receiveErrorgetPolicyVersions(f);
									}
								} else {
								    callback.receiveErrorgetPolicyVersions(error);
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
                                    callback.receiveErrorgetPolicyVersions(axisFault);
                                }
                            }
                });
                        

          org.apache.axis2.util.CallbackReceiver _callbackReceiver = null;
        if ( _operations[18].getMessageReceiver()==null &&  _operationClient.getOptions().isUseSeparateListener()) {
           _callbackReceiver = new org.apache.axis2.util.CallbackReceiver();
          _operations[18].setMessageReceiver(
                    _callbackReceiver);
        }

           //execute the operation client
           _operationClient.execute(false);

                    }
                 
                
                /**
                  * Auto generated method signature
                  * 
                     * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
                  */
                public void  removePolicies(
                 java.lang.String[] policyIds207,boolean dePromote208

                ) throws java.rmi.RemoteException
                
                
                        ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException
                {
                org.apache.axis2.context.MessageContext _messageContext = null;

                try {
                org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[19].getName());
                _operationClient.getOptions().setAction("urn:removePolicies");
                _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

                
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              
                org.apache.axiom.soap.SOAPEnvelope env = null;
                 _messageContext = new org.apache.axis2.context.MessageContext();

                
                                                    //Style is Doc.
                                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.RemovePolicies dummyWrappedType = null;
                                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                                    policyIds207,
                                                                    dePromote208,
                                                                    dummyWrappedType,
                                                                    optimizeContent(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org",
                                                                    "removePolicies")));
                                                                

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
                      if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"removePolicies"))){
                          //make the fault by reflection
                          try{
                              java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"removePolicies"));
                              java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                              java.lang.Exception ex=
                                      (java.lang.Exception) exceptionClass.newInstance();
                              //message class
                              java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"removePolicies"));
                              java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                              java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                              java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                         new java.lang.Class[]{messageClass});
                              m.invoke(ex,new java.lang.Object[]{messageObject});
                              
                              if (ex instanceof org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException){
                                throw (org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException)ex;
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
                     * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
                  */
                public void  dePromotePolicy(
                 java.lang.String policyId210

                ) throws java.rmi.RemoteException
                
                
                        ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException
                {
                org.apache.axis2.context.MessageContext _messageContext = null;

                try {
                org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[20].getName());
                _operationClient.getOptions().setAction("urn:dePromotePolicy");
                _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

                
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              
                org.apache.axiom.soap.SOAPEnvelope env = null;
                 _messageContext = new org.apache.axis2.context.MessageContext();

                
                                                    //Style is Doc.
                                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.DePromotePolicy dummyWrappedType = null;
                                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                                    policyId210,
                                                                    dummyWrappedType,
                                                                    optimizeContent(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org",
                                                                    "dePromotePolicy")));
                                                                

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
                      if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"dePromotePolicy"))){
                          //make the fault by reflection
                          try{
                              java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"dePromotePolicy"));
                              java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                              java.lang.Exception ex=
                                      (java.lang.Exception) exceptionClass.newInstance();
                              //message class
                              java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"dePromotePolicy"));
                              java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                              java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                              java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                         new java.lang.Class[]{messageClass});
                              m.invoke(ex,new java.lang.Object[]{messageObject});
                              
                              if (ex instanceof org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException){
                                throw (org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException)ex;
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
                     * @see org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminService#getEntitlementData
                     * @param getEntitlementData211
                    
                     */

                    

                            public  org.wso2.carbon.identity.entitlement.stub.dto.EntitlementTreeNodeDTO getEntitlementData(

                            java.lang.String dataModule212,java.lang.String category213,java.lang.String regexp214,int dataLevel215,int limit216)
                        

                    throws java.rmi.RemoteException
                    
                    {
              org.apache.axis2.context.MessageContext _messageContext = null;
              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[21].getName());
              _operationClient.getOptions().setAction("urn:getEntitlementData");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              

              // create a message context
              _messageContext = new org.apache.axis2.context.MessageContext();

              

              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;
                    org.wso2.carbon.identity.entitlement.stub.types.axis2.GetEntitlementData dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    dataModule212,
                                                    category213,
                                                    regexp214,
                                                    dataLevel215,
                                                    limit216,
                                                    dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org",
                                                    "getEntitlementData")));
                                                
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
                                             org.wso2.carbon.identity.entitlement.stub.types.axis2.GetEntitlementDataResponse.class,
                                              getEnvelopeNamespaces(_returnEnv));

                               
                                        return getGetEntitlementDataResponse_return((org.wso2.carbon.identity.entitlement.stub.types.axis2.GetEntitlementDataResponse)object);
                                   
         }catch(org.apache.axis2.AxisFault f){

            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt!=null){
                if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getEntitlementData"))){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getEntitlementData"));
                        java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                        java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
                        //message class
                        java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getEntitlementData"));
                        java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                        java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                        java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                   new java.lang.Class[]{messageClass});
                        m.invoke(ex,new java.lang.Object[]{messageObject});
                        

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
                * @see org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminService#startgetEntitlementData
                    * @param getEntitlementData211
                
                */
                public  void startgetEntitlementData(

                 java.lang.String dataModule212,java.lang.String category213,java.lang.String regexp214,int dataLevel215,int limit216,

                  final org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceCallbackHandler callback)

                throws java.rmi.RemoteException{

              org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[21].getName());
             _operationClient.getOptions().setAction("urn:getEntitlementData");
             _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              


              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env=null;
              final org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();

                    
                                    //Style is Doc.
                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.GetEntitlementData dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    dataModule212,
                                                    category213,
                                                    regexp214,
                                                    dataLevel215,
                                                    limit216,
                                                     dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org",
                                                    "getEntitlementData")));
                                                
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
                                                                         org.wso2.carbon.identity.entitlement.stub.types.axis2.GetEntitlementDataResponse.class,
                                                                         getEnvelopeNamespaces(resultEnv));
                                        callback.receiveResultgetEntitlementData(
                                            getGetEntitlementDataResponse_return((org.wso2.carbon.identity.entitlement.stub.types.axis2.GetEntitlementDataResponse)object));
                                        
                            } catch (org.apache.axis2.AxisFault e) {
                                callback.receiveErrorgetEntitlementData(e);
                            }
                            }

                            public void onError(java.lang.Exception error) {
								if (error instanceof org.apache.axis2.AxisFault) {
									org.apache.axis2.AxisFault f = (org.apache.axis2.AxisFault) error;
									org.apache.axiom.om.OMElement faultElt = f.getDetail();
									if (faultElt!=null){
										if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getEntitlementData"))){
											//make the fault by reflection
											try{
													java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getEntitlementData"));
													java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
													java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
													//message class
													java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getEntitlementData"));
														java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
													java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
													java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
															new java.lang.Class[]{messageClass});
													m.invoke(ex,new java.lang.Object[]{messageObject});
													
					
										            callback.receiveErrorgetEntitlementData(new java.rmi.RemoteException(ex.getMessage(), ex));
                                            } catch(java.lang.ClassCastException e){
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetEntitlementData(f);
                                            } catch (java.lang.ClassNotFoundException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetEntitlementData(f);
                                            } catch (java.lang.NoSuchMethodException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetEntitlementData(f);
                                            } catch (java.lang.reflect.InvocationTargetException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetEntitlementData(f);
                                            } catch (java.lang.IllegalAccessException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetEntitlementData(f);
                                            } catch (java.lang.InstantiationException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetEntitlementData(f);
                                            } catch (org.apache.axis2.AxisFault e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetEntitlementData(f);
                                            }
									    } else {
										    callback.receiveErrorgetEntitlementData(f);
									    }
									} else {
									    callback.receiveErrorgetEntitlementData(f);
									}
								} else {
								    callback.receiveErrorgetEntitlementData(error);
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
                                    callback.receiveErrorgetEntitlementData(axisFault);
                                }
                            }
                });
                        

          org.apache.axis2.util.CallbackReceiver _callbackReceiver = null;
        if ( _operations[21].getMessageReceiver()==null &&  _operationClient.getOptions().isUseSeparateListener()) {
           _callbackReceiver = new org.apache.axis2.util.CallbackReceiver();
          _operations[21].setMessageReceiver(
                    _callbackReceiver);
        }

           //execute the operation client
           _operationClient.execute(false);

                    }
                 
                
                /**
                  * Auto generated method signature
                  * 
                     * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
                  */
                public void  publishToPDP(
                 java.lang.String[] policyIds220,java.lang.String action221,java.lang.String version222,boolean enabled223,int order224

                ) throws java.rmi.RemoteException
                
                
                        ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException
                {
                org.apache.axis2.context.MessageContext _messageContext = null;

                try {
                org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[22].getName());
                _operationClient.getOptions().setAction("urn:publishToPDP");
                _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

                
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              
                org.apache.axiom.soap.SOAPEnvelope env = null;
                 _messageContext = new org.apache.axis2.context.MessageContext();

                
                                                    //Style is Doc.
                                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.PublishToPDP dummyWrappedType = null;
                                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                                    policyIds220,
                                                                    action221,
                                                                    version222,
                                                                    enabled223,
                                                                    order224,
                                                                    dummyWrappedType,
                                                                    optimizeContent(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org",
                                                                    "publishToPDP")));
                                                                

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
                      if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"publishToPDP"))){
                          //make the fault by reflection
                          try{
                              java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"publishToPDP"));
                              java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                              java.lang.Exception ex=
                                      (java.lang.Exception) exceptionClass.newInstance();
                              //message class
                              java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"publishToPDP"));
                              java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                              java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                              java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                         new java.lang.Class[]{messageClass});
                              m.invoke(ex,new java.lang.Object[]{messageObject});
                              
                              if (ex instanceof org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException){
                                throw (org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException)ex;
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
                     * @see org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminService#getPolicyByVersion
                     * @param getPolicyByVersion225
                    
                     * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
                     */

                    

                            public  org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO getPolicyByVersion(

                            java.lang.String policyId226,java.lang.String version227)
                        

                    throws java.rmi.RemoteException
                    
                    
                        ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException{
              org.apache.axis2.context.MessageContext _messageContext = null;
              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[23].getName());
              _operationClient.getOptions().setAction("urn:getPolicyByVersion");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              

              // create a message context
              _messageContext = new org.apache.axis2.context.MessageContext();

              

              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;
                    org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicyByVersion dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    policyId226,
                                                    version227,
                                                    dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org",
                                                    "getPolicyByVersion")));
                                                
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
                                             org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicyByVersionResponse.class,
                                              getEnvelopeNamespaces(_returnEnv));

                               
                                        return getGetPolicyByVersionResponse_return((org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicyByVersionResponse)object);
                                   
         }catch(org.apache.axis2.AxisFault f){

            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt!=null){
                if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getPolicyByVersion"))){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getPolicyByVersion"));
                        java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                        java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
                        //message class
                        java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getPolicyByVersion"));
                        java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                        java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                        java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                   new java.lang.Class[]{messageClass});
                        m.invoke(ex,new java.lang.Object[]{messageObject});
                        
                        if (ex instanceof org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException){
                          throw (org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException)ex;
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
                * @see org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminService#startgetPolicyByVersion
                    * @param getPolicyByVersion225
                
                */
                public  void startgetPolicyByVersion(

                 java.lang.String policyId226,java.lang.String version227,

                  final org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceCallbackHandler callback)

                throws java.rmi.RemoteException{

              org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[23].getName());
             _operationClient.getOptions().setAction("urn:getPolicyByVersion");
             _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              


              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env=null;
              final org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();

                    
                                    //Style is Doc.
                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicyByVersion dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    policyId226,
                                                    version227,
                                                     dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org",
                                                    "getPolicyByVersion")));
                                                
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
                                                                         org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicyByVersionResponse.class,
                                                                         getEnvelopeNamespaces(resultEnv));
                                        callback.receiveResultgetPolicyByVersion(
                                            getGetPolicyByVersionResponse_return((org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicyByVersionResponse)object));
                                        
                            } catch (org.apache.axis2.AxisFault e) {
                                callback.receiveErrorgetPolicyByVersion(e);
                            }
                            }

                            public void onError(java.lang.Exception error) {
								if (error instanceof org.apache.axis2.AxisFault) {
									org.apache.axis2.AxisFault f = (org.apache.axis2.AxisFault) error;
									org.apache.axiom.om.OMElement faultElt = f.getDetail();
									if (faultElt!=null){
										if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getPolicyByVersion"))){
											//make the fault by reflection
											try{
													java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getPolicyByVersion"));
													java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
													java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
													//message class
													java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getPolicyByVersion"));
														java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
													java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
													java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
															new java.lang.Class[]{messageClass});
													m.invoke(ex,new java.lang.Object[]{messageObject});
													
													if (ex instanceof org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException){
														callback.receiveErrorgetPolicyByVersion((org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException)ex);
											            return;
										            }
										            
					
										            callback.receiveErrorgetPolicyByVersion(new java.rmi.RemoteException(ex.getMessage(), ex));
                                            } catch(java.lang.ClassCastException e){
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetPolicyByVersion(f);
                                            } catch (java.lang.ClassNotFoundException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetPolicyByVersion(f);
                                            } catch (java.lang.NoSuchMethodException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetPolicyByVersion(f);
                                            } catch (java.lang.reflect.InvocationTargetException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetPolicyByVersion(f);
                                            } catch (java.lang.IllegalAccessException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetPolicyByVersion(f);
                                            } catch (java.lang.InstantiationException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetPolicyByVersion(f);
                                            } catch (org.apache.axis2.AxisFault e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetPolicyByVersion(f);
                                            }
									    } else {
										    callback.receiveErrorgetPolicyByVersion(f);
									    }
									} else {
									    callback.receiveErrorgetPolicyByVersion(f);
									}
								} else {
								    callback.receiveErrorgetPolicyByVersion(error);
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
                                    callback.receiveErrorgetPolicyByVersion(axisFault);
                                }
                            }
                });
                        

          org.apache.axis2.util.CallbackReceiver _callbackReceiver = null;
        if ( _operations[23].getMessageReceiver()==null &&  _operationClient.getOptions().isUseSeparateListener()) {
           _callbackReceiver = new org.apache.axis2.util.CallbackReceiver();
          _operations[23].setMessageReceiver(
                    _callbackReceiver);
        }

           //execute the operation client
           _operationClient.execute(false);

                    }
                 
                
                /**
                  * Auto generated method signature
                  * 
                     * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
                  */
                public void  enableDisablePolicy(
                 java.lang.String policyId231,boolean enable232

                ) throws java.rmi.RemoteException
                
                
                        ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException
                {
                org.apache.axis2.context.MessageContext _messageContext = null;

                try {
                org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[24].getName());
                _operationClient.getOptions().setAction("urn:enableDisablePolicy");
                _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

                
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              
                org.apache.axiom.soap.SOAPEnvelope env = null;
                 _messageContext = new org.apache.axis2.context.MessageContext();

                
                                                    //Style is Doc.
                                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.EnableDisablePolicy dummyWrappedType = null;
                                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                                    policyId231,
                                                                    enable232,
                                                                    dummyWrappedType,
                                                                    optimizeContent(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org",
                                                                    "enableDisablePolicy")));
                                                                

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
                      if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"enableDisablePolicy"))){
                          //make the fault by reflection
                          try{
                              java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"enableDisablePolicy"));
                              java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                              java.lang.Exception ex=
                                      (java.lang.Exception) exceptionClass.newInstance();
                              //message class
                              java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"enableDisablePolicy"));
                              java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                              java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                              java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                         new java.lang.Class[]{messageClass});
                              m.invoke(ex,new java.lang.Object[]{messageObject});
                              
                              if (ex instanceof org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException){
                                throw (org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException)ex;
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
                     * @see org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminService#getAllPolicies
                     * @param getAllPolicies233
                    
                     * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
                     */

                    

                            public  org.wso2.carbon.identity.entitlement.stub.dto.PaginatedPolicySetDTO getAllPolicies(

                            java.lang.String policyTypeFilter234,java.lang.String policySearchString235,int pageNumber236,boolean isPDPPolicy237)
                        

                    throws java.rmi.RemoteException
                    
                    
                        ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException{
              org.apache.axis2.context.MessageContext _messageContext = null;
              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[25].getName());
              _operationClient.getOptions().setAction("urn:getAllPolicies");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              

              // create a message context
              _messageContext = new org.apache.axis2.context.MessageContext();

              

              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;
                    org.wso2.carbon.identity.entitlement.stub.types.axis2.GetAllPolicies dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    policyTypeFilter234,
                                                    policySearchString235,
                                                    pageNumber236,
                                                    isPDPPolicy237,
                                                    dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org",
                                                    "getAllPolicies")));
                                                
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
                                             org.wso2.carbon.identity.entitlement.stub.types.axis2.GetAllPoliciesResponse.class,
                                              getEnvelopeNamespaces(_returnEnv));

                               
                                        return getGetAllPoliciesResponse_return((org.wso2.carbon.identity.entitlement.stub.types.axis2.GetAllPoliciesResponse)object);
                                   
         }catch(org.apache.axis2.AxisFault f){

            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt!=null){
                if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getAllPolicies"))){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getAllPolicies"));
                        java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                        java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
                        //message class
                        java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getAllPolicies"));
                        java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                        java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                        java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                   new java.lang.Class[]{messageClass});
                        m.invoke(ex,new java.lang.Object[]{messageObject});
                        
                        if (ex instanceof org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException){
                          throw (org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException)ex;
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
                * @see org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminService#startgetAllPolicies
                    * @param getAllPolicies233
                
                */
                public  void startgetAllPolicies(

                 java.lang.String policyTypeFilter234,java.lang.String policySearchString235,int pageNumber236,boolean isPDPPolicy237,

                  final org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceCallbackHandler callback)

                throws java.rmi.RemoteException{

              org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[25].getName());
             _operationClient.getOptions().setAction("urn:getAllPolicies");
             _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              


              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env=null;
              final org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();

                    
                                    //Style is Doc.
                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.GetAllPolicies dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    policyTypeFilter234,
                                                    policySearchString235,
                                                    pageNumber236,
                                                    isPDPPolicy237,
                                                     dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org",
                                                    "getAllPolicies")));
                                                
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
                                                                         org.wso2.carbon.identity.entitlement.stub.types.axis2.GetAllPoliciesResponse.class,
                                                                         getEnvelopeNamespaces(resultEnv));
                                        callback.receiveResultgetAllPolicies(
                                            getGetAllPoliciesResponse_return((org.wso2.carbon.identity.entitlement.stub.types.axis2.GetAllPoliciesResponse)object));
                                        
                            } catch (org.apache.axis2.AxisFault e) {
                                callback.receiveErrorgetAllPolicies(e);
                            }
                            }

                            public void onError(java.lang.Exception error) {
								if (error instanceof org.apache.axis2.AxisFault) {
									org.apache.axis2.AxisFault f = (org.apache.axis2.AxisFault) error;
									org.apache.axiom.om.OMElement faultElt = f.getDetail();
									if (faultElt!=null){
										if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getAllPolicies"))){
											//make the fault by reflection
											try{
													java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getAllPolicies"));
													java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
													java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
													//message class
													java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getAllPolicies"));
														java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
													java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
													java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
															new java.lang.Class[]{messageClass});
													m.invoke(ex,new java.lang.Object[]{messageObject});
													
													if (ex instanceof org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException){
														callback.receiveErrorgetAllPolicies((org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException)ex);
											            return;
										            }
										            
					
										            callback.receiveErrorgetAllPolicies(new java.rmi.RemoteException(ex.getMessage(), ex));
                                            } catch(java.lang.ClassCastException e){
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetAllPolicies(f);
                                            } catch (java.lang.ClassNotFoundException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetAllPolicies(f);
                                            } catch (java.lang.NoSuchMethodException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetAllPolicies(f);
                                            } catch (java.lang.reflect.InvocationTargetException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetAllPolicies(f);
                                            } catch (java.lang.IllegalAccessException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetAllPolicies(f);
                                            } catch (java.lang.InstantiationException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetAllPolicies(f);
                                            } catch (org.apache.axis2.AxisFault e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveErrorgetAllPolicies(f);
                                            }
									    } else {
										    callback.receiveErrorgetAllPolicies(f);
									    }
									} else {
									    callback.receiveErrorgetAllPolicies(f);
									}
								} else {
								    callback.receiveErrorgetAllPolicies(error);
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
                                    callback.receiveErrorgetAllPolicies(axisFault);
                                }
                            }
                });
                        

          org.apache.axis2.util.CallbackReceiver _callbackReceiver = null;
        if ( _operations[25].getMessageReceiver()==null &&  _operationClient.getOptions().isUseSeparateListener()) {
           _callbackReceiver = new org.apache.axis2.util.CallbackReceiver();
          _operations[25].setMessageReceiver(
                    _callbackReceiver);
        }

           //execute the operation client
           _operationClient.execute(false);

                    }
                 
                
                /**
                  * Auto generated method signature
                  * 
                     * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
                  */
                public void  deleteSubscriber(
                 java.lang.String subscriberId241

                ) throws java.rmi.RemoteException
                
                
                        ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException
                {
                org.apache.axis2.context.MessageContext _messageContext = null;

                try {
                org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[26].getName());
                _operationClient.getOptions().setAction("urn:deleteSubscriber");
                _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

                
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              
                org.apache.axiom.soap.SOAPEnvelope env = null;
                 _messageContext = new org.apache.axis2.context.MessageContext();

                
                                                    //Style is Doc.
                                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.DeleteSubscriber dummyWrappedType = null;
                                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                                    subscriberId241,
                                                                    dummyWrappedType,
                                                                    optimizeContent(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org",
                                                                    "deleteSubscriber")));
                                                                

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
                      if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"deleteSubscriber"))){
                          //make the fault by reflection
                          try{
                              java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"deleteSubscriber"));
                              java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                              java.lang.Exception ex=
                                      (java.lang.Exception) exceptionClass.newInstance();
                              //message class
                              java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"deleteSubscriber"));
                              java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                              java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                              java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                         new java.lang.Class[]{messageClass});
                              m.invoke(ex,new java.lang.Object[]{messageObject});
                              
                              if (ex instanceof org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException){
                                throw (org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException)ex;
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
                     * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
                  */
                public void  updatePolicy(
                 org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO policyDTO243

                ) throws java.rmi.RemoteException
                
                
                        ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException
                {
                org.apache.axis2.context.MessageContext _messageContext = null;

                try {
                org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[27].getName());
                _operationClient.getOptions().setAction("urn:updatePolicy");
                _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

                
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              
                org.apache.axiom.soap.SOAPEnvelope env = null;
                 _messageContext = new org.apache.axis2.context.MessageContext();

                
                                                    //Style is Doc.
                                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.UpdatePolicy dummyWrappedType = null;
                                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                                    policyDTO243,
                                                                    dummyWrappedType,
                                                                    optimizeContent(new javax.xml.namespace.QName("http://entitlement.identity.carbon.wso2.org",
                                                                    "updatePolicy")));
                                                                

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
                      if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"updatePolicy"))){
                          //make the fault by reflection
                          try{
                              java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"updatePolicy"));
                              java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                              java.lang.Exception ex=
                                      (java.lang.Exception) exceptionClass.newInstance();
                              //message class
                              java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"updatePolicy"));
                              java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                              java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                              java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                         new java.lang.Class[]{messageClass});
                              m.invoke(ex,new java.lang.Object[]{messageObject});
                              
                              if (ex instanceof org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException){
                                throw (org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException)ex;
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
     //https://localhost:9443/services/EntitlementPolicyAdminService.EntitlementPolicyAdminServiceHttpsSoap12Endpoint/
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.entitlement.stub.types.axis2.AddPolicies param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.AddPolicies.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.entitlement.stub.types.axis2.AddSubscriber param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.AddSubscriber.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.entitlement.stub.types.axis2.Publish param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.Publish.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.entitlement.stub.types.axis2.OrderPolicy param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.OrderPolicy.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetStatusData param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetStatusData.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetStatusDataResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetStatusDataResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.entitlement.stub.types.axis2.AddPolicy param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.AddPolicy.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.entitlement.stub.types.axis2.RemovePolicy param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.RemovePolicy.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetSubscriber param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetSubscriber.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetSubscriberResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetSubscriberResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.entitlement.stub.types.axis2.UpdateSubscriber param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.UpdateSubscriber.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicy param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicy.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicyResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicyResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.entitlement.stub.types.axis2.PublishPolicies param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.PublishPolicies.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetAllPolicyIds param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetAllPolicyIds.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetAllPolicyIdsResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetAllPolicyIdsResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetEntitlementDataModules param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetEntitlementDataModules.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetEntitlementDataModulesResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetEntitlementDataModulesResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetSubscriberIds param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetSubscriberIds.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetSubscriberIdsResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetSubscriberIdsResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.entitlement.stub.types.axis2.RollBackPolicy param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.RollBackPolicy.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetLightPolicy param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetLightPolicy.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetLightPolicyResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetLightPolicyResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.entitlement.stub.types.axis2.ImportPolicyFromRegistry param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.ImportPolicyFromRegistry.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPublisherModuleData param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPublisherModuleData.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPublisherModuleDataResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPublisherModuleDataResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicyVersions param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicyVersions.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicyVersionsResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicyVersionsResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.entitlement.stub.types.axis2.RemovePolicies param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.RemovePolicies.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.entitlement.stub.types.axis2.DePromotePolicy param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.DePromotePolicy.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetEntitlementData param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetEntitlementData.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetEntitlementDataResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetEntitlementDataResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.entitlement.stub.types.axis2.PublishToPDP param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.PublishToPDP.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicyByVersion param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicyByVersion.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicyByVersionResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicyByVersionResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.entitlement.stub.types.axis2.EnableDisablePolicy param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.EnableDisablePolicy.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetAllPolicies param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetAllPolicies.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetAllPoliciesResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetAllPoliciesResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.entitlement.stub.types.axis2.DeleteSubscriber param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.DeleteSubscriber.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.wso2.carbon.identity.entitlement.stub.types.axis2.UpdatePolicy param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.UpdatePolicy.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO[] param1,
                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.AddPolicies dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.identity.entitlement.stub.types.axis2.AddPolicies wrappedType = new org.wso2.carbon.identity.entitlement.stub.types.axis2.AddPolicies();

                                 
                                              wrappedType.setPolicies(param1);
                                         

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.AddPolicies.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    org.wso2.carbon.identity.entitlement.stub.dto.PublisherDataHolder param1,
                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.AddSubscriber dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.identity.entitlement.stub.types.axis2.AddSubscriber wrappedType = new org.wso2.carbon.identity.entitlement.stub.types.axis2.AddSubscriber();

                                 
                                              wrappedType.setHolder(param1);
                                         

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.AddSubscriber.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    java.lang.String param1,
                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.Publish dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.identity.entitlement.stub.types.axis2.Publish wrappedType = new org.wso2.carbon.identity.entitlement.stub.types.axis2.Publish();

                                 
                                              wrappedType.setVerificationCode(param1);
                                         

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.Publish.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    java.lang.String param1,
                                    int param2,
                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.OrderPolicy dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.identity.entitlement.stub.types.axis2.OrderPolicy wrappedType = new org.wso2.carbon.identity.entitlement.stub.types.axis2.OrderPolicy();

                                 
                                              wrappedType.setPolicyId(param1);
                                         
                                              wrappedType.setNewOrder(param2);
                                         

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.OrderPolicy.MY_QNAME,factory));
                                    

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
                                    int param5,
                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.GetStatusData dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.identity.entitlement.stub.types.axis2.GetStatusData wrappedType = new org.wso2.carbon.identity.entitlement.stub.types.axis2.GetStatusData();

                                 
                                              wrappedType.setAbout(param1);
                                         
                                              wrappedType.setKey(param2);
                                         
                                              wrappedType.setType(param3);
                                         
                                              wrappedType.setSearchString(param4);
                                         
                                              wrappedType.setPageNumber(param5);
                                         

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetStatusData.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             

                                
                                private org.wso2.carbon.identity.entitlement.stub.dto.PaginatedStatusHolder getGetStatusDataResponse_return(
                                org.wso2.carbon.identity.entitlement.stub.types.axis2.GetStatusDataResponse wrappedType){
                                
                                        return wrappedType.get_return();
                                    
                                }
                             
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO param1,
                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.AddPolicy dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.identity.entitlement.stub.types.axis2.AddPolicy wrappedType = new org.wso2.carbon.identity.entitlement.stub.types.axis2.AddPolicy();

                                 
                                              wrappedType.setPolicyDTO(param1);
                                         

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.AddPolicy.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    java.lang.String param1,
                                    boolean param2,
                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.RemovePolicy dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.identity.entitlement.stub.types.axis2.RemovePolicy wrappedType = new org.wso2.carbon.identity.entitlement.stub.types.axis2.RemovePolicy();

                                 
                                              wrappedType.setPolicyId(param1);
                                         
                                              wrappedType.setDePromote(param2);
                                         

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.RemovePolicy.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    java.lang.String param1,
                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.GetSubscriber dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.identity.entitlement.stub.types.axis2.GetSubscriber wrappedType = new org.wso2.carbon.identity.entitlement.stub.types.axis2.GetSubscriber();

                                 
                                              wrappedType.setSubscribeId(param1);
                                         

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetSubscriber.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             

                                
                                private org.wso2.carbon.identity.entitlement.stub.dto.PublisherDataHolder getGetSubscriberResponse_return(
                                org.wso2.carbon.identity.entitlement.stub.types.axis2.GetSubscriberResponse wrappedType){
                                
                                        return wrappedType.get_return();
                                    
                                }
                             
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    org.wso2.carbon.identity.entitlement.stub.dto.PublisherDataHolder param1,
                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.UpdateSubscriber dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.identity.entitlement.stub.types.axis2.UpdateSubscriber wrappedType = new org.wso2.carbon.identity.entitlement.stub.types.axis2.UpdateSubscriber();

                                 
                                              wrappedType.setHolder(param1);
                                         

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.UpdateSubscriber.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    java.lang.String param1,
                                    boolean param2,
                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicy dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicy wrappedType = new org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicy();

                                 
                                              wrappedType.setPolicyId(param1);
                                         
                                              wrappedType.setIsPDPPolicy(param2);
                                         

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicy.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             

                                
                                private org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO getGetPolicyResponse_return(
                                org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicyResponse wrappedType){
                                
                                        return wrappedType.get_return();
                                    
                                }
                             
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    java.lang.String[] param1,
                                    java.lang.String[] param2,
                                    java.lang.String param3,
                                    java.lang.String param4,
                                    boolean param5,
                                    int param6,
                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.PublishPolicies dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.identity.entitlement.stub.types.axis2.PublishPolicies wrappedType = new org.wso2.carbon.identity.entitlement.stub.types.axis2.PublishPolicies();

                                 
                                              wrappedType.setPolicyIds(param1);
                                         
                                              wrappedType.setSubscriberIds(param2);
                                         
                                              wrappedType.setAction(param3);
                                         
                                              wrappedType.setVersion(param4);
                                         
                                              wrappedType.setEnabled(param5);
                                         
                                              wrappedType.setOrder(param6);
                                         

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.PublishPolicies.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    java.lang.String param1,
                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.GetAllPolicyIds dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.identity.entitlement.stub.types.axis2.GetAllPolicyIds wrappedType = new org.wso2.carbon.identity.entitlement.stub.types.axis2.GetAllPolicyIds();

                                 
                                              wrappedType.setSearchString(param1);
                                         

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetAllPolicyIds.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             

                                
                                private java.lang.String[] getGetAllPolicyIdsResponse_return(
                                org.wso2.carbon.identity.entitlement.stub.types.axis2.GetAllPolicyIdsResponse wrappedType){
                                
                                        return wrappedType.get_return();
                                    
                                }
                             
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.GetEntitlementDataModules dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.identity.entitlement.stub.types.axis2.GetEntitlementDataModules wrappedType = new org.wso2.carbon.identity.entitlement.stub.types.axis2.GetEntitlementDataModules();

                                 

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetEntitlementDataModules.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             

                                
                                private org.wso2.carbon.identity.entitlement.stub.dto.EntitlementFinderDataHolder[] getGetEntitlementDataModulesResponse_return(
                                org.wso2.carbon.identity.entitlement.stub.types.axis2.GetEntitlementDataModulesResponse wrappedType){
                                
                                        return wrappedType.get_return();
                                    
                                }
                             
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    java.lang.String param1,
                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.GetSubscriberIds dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.identity.entitlement.stub.types.axis2.GetSubscriberIds wrappedType = new org.wso2.carbon.identity.entitlement.stub.types.axis2.GetSubscriberIds();

                                 
                                              wrappedType.setSearchString(param1);
                                         

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetSubscriberIds.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             

                                
                                private java.lang.String[] getGetSubscriberIdsResponse_return(
                                org.wso2.carbon.identity.entitlement.stub.types.axis2.GetSubscriberIdsResponse wrappedType){
                                
                                        return wrappedType.get_return();
                                    
                                }
                             
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    java.lang.String param1,
                                    java.lang.String param2,
                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.RollBackPolicy dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.identity.entitlement.stub.types.axis2.RollBackPolicy wrappedType = new org.wso2.carbon.identity.entitlement.stub.types.axis2.RollBackPolicy();

                                 
                                              wrappedType.setPolicyId(param1);
                                         
                                              wrappedType.setVersion(param2);
                                         

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.RollBackPolicy.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    java.lang.String param1,
                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.GetLightPolicy dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.identity.entitlement.stub.types.axis2.GetLightPolicy wrappedType = new org.wso2.carbon.identity.entitlement.stub.types.axis2.GetLightPolicy();

                                 
                                              wrappedType.setPolicyId(param1);
                                         

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetLightPolicy.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             

                                
                                private org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO getGetLightPolicyResponse_return(
                                org.wso2.carbon.identity.entitlement.stub.types.axis2.GetLightPolicyResponse wrappedType){
                                
                                        return wrappedType.get_return();
                                    
                                }
                             
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    java.lang.String param1,
                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.ImportPolicyFromRegistry dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.identity.entitlement.stub.types.axis2.ImportPolicyFromRegistry wrappedType = new org.wso2.carbon.identity.entitlement.stub.types.axis2.ImportPolicyFromRegistry();

                                 
                                              wrappedType.setPolicyRegistryPath(param1);
                                         

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.ImportPolicyFromRegistry.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPublisherModuleData dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPublisherModuleData wrappedType = new org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPublisherModuleData();

                                 

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPublisherModuleData.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             

                                
                                private org.wso2.carbon.identity.entitlement.stub.dto.PublisherDataHolder[] getGetPublisherModuleDataResponse_return(
                                org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPublisherModuleDataResponse wrappedType){
                                
                                        return wrappedType.get_return();
                                    
                                }
                             
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    java.lang.String param1,
                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicyVersions dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicyVersions wrappedType = new org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicyVersions();

                                 
                                              wrappedType.setPolicyId(param1);
                                         

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicyVersions.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             

                                
                                private java.lang.String[] getGetPolicyVersionsResponse_return(
                                org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicyVersionsResponse wrappedType){
                                
                                        return wrappedType.get_return();
                                    
                                }
                             
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    java.lang.String[] param1,
                                    boolean param2,
                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.RemovePolicies dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.identity.entitlement.stub.types.axis2.RemovePolicies wrappedType = new org.wso2.carbon.identity.entitlement.stub.types.axis2.RemovePolicies();

                                 
                                              wrappedType.setPolicyIds(param1);
                                         
                                              wrappedType.setDePromote(param2);
                                         

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.RemovePolicies.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    java.lang.String param1,
                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.DePromotePolicy dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.identity.entitlement.stub.types.axis2.DePromotePolicy wrappedType = new org.wso2.carbon.identity.entitlement.stub.types.axis2.DePromotePolicy();

                                 
                                              wrappedType.setPolicyId(param1);
                                         

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.DePromotePolicy.MY_QNAME,factory));
                                    

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
                                    int param4,
                                    int param5,
                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.GetEntitlementData dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.identity.entitlement.stub.types.axis2.GetEntitlementData wrappedType = new org.wso2.carbon.identity.entitlement.stub.types.axis2.GetEntitlementData();

                                 
                                              wrappedType.setDataModule(param1);
                                         
                                              wrappedType.setCategory(param2);
                                         
                                              wrappedType.setRegexp(param3);
                                         
                                              wrappedType.setDataLevel(param4);
                                         
                                              wrappedType.setLimit(param5);
                                         

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetEntitlementData.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             

                                
                                private org.wso2.carbon.identity.entitlement.stub.dto.EntitlementTreeNodeDTO getGetEntitlementDataResponse_return(
                                org.wso2.carbon.identity.entitlement.stub.types.axis2.GetEntitlementDataResponse wrappedType){
                                
                                        return wrappedType.get_return();
                                    
                                }
                             
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    java.lang.String[] param1,
                                    java.lang.String param2,
                                    java.lang.String param3,
                                    boolean param4,
                                    int param5,
                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.PublishToPDP dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.identity.entitlement.stub.types.axis2.PublishToPDP wrappedType = new org.wso2.carbon.identity.entitlement.stub.types.axis2.PublishToPDP();

                                 
                                              wrappedType.setPolicyIds(param1);
                                         
                                              wrappedType.setAction(param2);
                                         
                                              wrappedType.setVersion(param3);
                                         
                                              wrappedType.setEnabled(param4);
                                         
                                              wrappedType.setOrder(param5);
                                         

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.PublishToPDP.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    java.lang.String param1,
                                    java.lang.String param2,
                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicyByVersion dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicyByVersion wrappedType = new org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicyByVersion();

                                 
                                              wrappedType.setPolicyId(param1);
                                         
                                              wrappedType.setVersion(param2);
                                         

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicyByVersion.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             

                                
                                private org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO getGetPolicyByVersionResponse_return(
                                org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicyByVersionResponse wrappedType){
                                
                                        return wrappedType.get_return();
                                    
                                }
                             
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    java.lang.String param1,
                                    boolean param2,
                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.EnableDisablePolicy dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.identity.entitlement.stub.types.axis2.EnableDisablePolicy wrappedType = new org.wso2.carbon.identity.entitlement.stub.types.axis2.EnableDisablePolicy();

                                 
                                              wrappedType.setPolicyId(param1);
                                         
                                              wrappedType.setEnable(param2);
                                         

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.EnableDisablePolicy.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    java.lang.String param1,
                                    java.lang.String param2,
                                    int param3,
                                    boolean param4,
                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.GetAllPolicies dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.identity.entitlement.stub.types.axis2.GetAllPolicies wrappedType = new org.wso2.carbon.identity.entitlement.stub.types.axis2.GetAllPolicies();

                                 
                                              wrappedType.setPolicyTypeFilter(param1);
                                         
                                              wrappedType.setPolicySearchString(param2);
                                         
                                              wrappedType.setPageNumber(param3);
                                         
                                              wrappedType.setIsPDPPolicy(param4);
                                         

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.GetAllPolicies.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             

                                
                                private org.wso2.carbon.identity.entitlement.stub.dto.PaginatedPolicySetDTO getGetAllPoliciesResponse_return(
                                org.wso2.carbon.identity.entitlement.stub.types.axis2.GetAllPoliciesResponse wrappedType){
                                
                                        return wrappedType.get_return();
                                    
                                }
                             
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    java.lang.String param1,
                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.DeleteSubscriber dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.identity.entitlement.stub.types.axis2.DeleteSubscriber wrappedType = new org.wso2.carbon.identity.entitlement.stub.types.axis2.DeleteSubscriber();

                                 
                                              wrappedType.setSubscriberId(param1);
                                         

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.DeleteSubscriber.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             
                                    
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO param1,
                                    org.wso2.carbon.identity.entitlement.stub.types.axis2.UpdatePolicy dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                org.wso2.carbon.identity.entitlement.stub.types.axis2.UpdatePolicy wrappedType = new org.wso2.carbon.identity.entitlement.stub.types.axis2.UpdatePolicy();

                                 
                                              wrappedType.setPolicyDTO(param1);
                                         

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(org.wso2.carbon.identity.entitlement.stub.types.axis2.UpdatePolicy.MY_QNAME,factory));
                                    

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                
                             
                             /* methods to provide back word compatibility */

                             


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
        
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.AddPolicies.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.AddPolicies.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.AddSubscriber.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.AddSubscriber.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.Publish.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.Publish.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.OrderPolicy.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.OrderPolicy.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.GetStatusData.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.GetStatusData.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.GetStatusDataResponse.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.GetStatusDataResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.AddPolicy.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.AddPolicy.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.RemovePolicy.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.RemovePolicy.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.GetSubscriber.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.GetSubscriber.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.GetSubscriberResponse.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.GetSubscriberResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.UpdateSubscriber.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.UpdateSubscriber.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicy.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicy.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicyResponse.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicyResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.PublishPolicies.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.PublishPolicies.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.GetAllPolicyIds.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.GetAllPolicyIds.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.GetAllPolicyIdsResponse.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.GetAllPolicyIdsResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.GetEntitlementDataModules.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.GetEntitlementDataModules.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.GetEntitlementDataModulesResponse.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.GetEntitlementDataModulesResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.GetSubscriberIds.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.GetSubscriberIds.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.GetSubscriberIdsResponse.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.GetSubscriberIdsResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.RollBackPolicy.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.RollBackPolicy.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.GetLightPolicy.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.GetLightPolicy.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.GetLightPolicyResponse.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.GetLightPolicyResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.ImportPolicyFromRegistry.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.ImportPolicyFromRegistry.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPublisherModuleData.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPublisherModuleData.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPublisherModuleDataResponse.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPublisherModuleDataResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicyVersions.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicyVersions.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicyVersionsResponse.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicyVersionsResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.RemovePolicies.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.RemovePolicies.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.DePromotePolicy.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.DePromotePolicy.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.GetEntitlementData.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.GetEntitlementData.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.GetEntitlementDataResponse.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.GetEntitlementDataResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.PublishToPDP.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.PublishToPDP.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicyByVersion.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicyByVersion.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicyByVersionResponse.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.GetPolicyByVersionResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.EnableDisablePolicy.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.EnableDisablePolicy.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.GetAllPolicies.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.GetAllPolicies.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.GetAllPoliciesResponse.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.GetAllPoliciesResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.DeleteSubscriber.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.DeleteSubscriber.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.UpdatePolicy.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.UpdatePolicy.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.class.equals(type)){
                
                           return org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementPolicyAdminServiceEntitlementException.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
        } catch (java.lang.Exception e) {
        throw org.apache.axis2.AxisFault.makeFault(e);
        }
           return null;
        }



    
   }
   