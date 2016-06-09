REST API implementation for WSO2 IS 
===================================

This is a REST implementation of the WSO2 IS Entitlement Service, done as a part of GSoC 2016

The code is still in early stages, and I would highly appreciate if you could carry out tests and provide feedback / issues / comments on it.

Procedure
--------

1. Download the target/wso2-entitlement.war file
2. Place it in your **{IS ROOT}/repository/deployement/server/webapps** (Tested for IS 5.2.0)
3. You can hot deploy the war file as well
4. Once deployed the WADL definitions for the service can be seen at, **https://localhost:9443/wso2-entitlement/entitlement/Decision?_wadl**
5. The service curently support both JSON and XML
6. TO test various service methods, use the curl requests and json/xml request definitions available under resources/curlTests

Thank you!!

*You are awesome :)*
