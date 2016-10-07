REST API implementation for WSO2 IS 
===================================

This is a REST implementation of the WSO2 IS Entitlement Service, done as a part of GSoC 2016

The code is still in early stages, and I would highly appreciate if you could carry out tests and provide feedback / issues / comments on it.

Design and implementation
-------------------------

Design and implementation details of the endpoint is available at [http://manzzup.blogspot.com/2016/08/gsoc-2016-rest-implementation-for-wso2.html](http://manzzup.blogspot.com/2016/08/gsoc-2016-rest-implementation-for-wso2.html)


Procedure
--------

1. Download the target/entitlement.war file
2. Place it in your **{IS ROOT}/repository/deployement/server/webapps** (Tested for IS 5.2.0)
3. You can hot deploy the war file as well
4. Once deployed the WADL definitions for the service can be seen at, **https://localhost:9443/entitlement/entitlement/Decision?_wadl**
5. The service curently support both JSON and XML
6. TO test various service methods, use the curl requests and json/xml request definitions available under resources/curlTests

Thank you!!
