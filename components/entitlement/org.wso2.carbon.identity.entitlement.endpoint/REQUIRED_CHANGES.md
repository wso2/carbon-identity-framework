Changes need to be done for different wso2 components for completion of the endpoint
====================================================================================

Balana
------

1) Public constructor for [MultiRequests](https://github.com/wso2/balana/blob/master/modules/balana-core/src/main/java/org/wso2/balana/xacml3/MultiRequests.java)

Needs the public constructor for manual creation of `RequestCtx` object in JSONParser

2) Public getter for obligationId in [Obligation](https://github.com/wso2/balana/blob/master/modules/balana-core/src/main/java/org/wso2/balana/xacml3/Obligation.java)

Refer the following [PR](https://github.com/wso2/balana/pull/41)

3) Public method in [PDP](https://github.com/wso2/balana/blob/master/modules/balana-core/src/main/java/org/wso2/balana/PDP.java) that
can convert a given XACML String to `ResponseCtx` object. 

This process only done internally in `evaluate` method bodies. But in the REST endpoint, someone can send the request in XACML 
but needs the response in JSON, for which the `evaluate` method should either return a ResponseCtx object or a JSON String. Since
JSON is not already supported in Balana, if there's a converter method to produce `RequestCtx` from XACML String, the exsting 
evaluate method can be used.

4) Integrating the JSON support

JSON support is give using 2 supporter classes in the REST source code. But since the functionality of the code is better related 
to balana, it's better to implement them inside Balana.
