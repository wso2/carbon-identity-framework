namespace java org.wso2.carbon.identity.entitlement.thrift

exception EntitlementException {
    1: required string message
}

service EntitlementService {
   string getDecision (
 	1: required string request
	2: required string sessionId) throws (1:EntitlementException ee)
   string getDecisionByAttributes (
 	1: required string subject
	2: required string resource
	3: required string action
	4: required list<string> environment
	5: required string sessionId) throws (1:EntitlementException ee)
}