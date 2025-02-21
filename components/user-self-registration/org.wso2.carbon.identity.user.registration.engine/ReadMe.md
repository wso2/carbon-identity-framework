### APIs

#### Deployment.toml config

```toml
[[resource.access_control]]
context="(.*)/reg-orchestration(.*)"
secure=false
http_method="POST"
```

#### Config API
POST https://localhost:9443/reg-orchestration/config
Use the json payload in https://github.com/brionmario/iam-product-registration-orchestration/blob/main/schemas/composer/payload.json as it is.
##### Retrieve the json payload from the API
GET https://localhost:9443/reg-orchestration/config

#### Portal API
initiate: https://localhost:9443/reg-orchestration/portal/initiate
No json payload required for initiate API

continue: https://localhost:9443/reg-orchestration/portal/continue
use the json payload in current portal as it is. https://github.com/brionmario/iam-product-registration-orchestration/tree/main/schemas/portal


#### Testing the config through /registration/initiate api

use the following curl command to test the config. New flow is engaged with the applicationId set to "newflow"

```cURL
curl --location 'https://localhost:9443/api/users/v2/registration/initiate' \
--header 'Content-Type: application/json' \
--data '{
  "applicationId": "newflow"
}'
```



