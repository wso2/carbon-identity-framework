package org.wso2.carbon.identity.mgt.endpoint.util.client;

import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.wso2.carbon.identity.mgt.endpoint.util.IdentityManagementEndpointUtil;
import org.wso2.carbon.identity.mgt.endpoint.util.IdentityManagementServiceUtil;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class ValidationConfigurationRetrievalClient {

    private static final Log log = LogFactory.getLog(ConfiguredAuthenticatorsRetrievalClient.class);
    private static final String VALIDATION_MGT_API_PATH = "/api/server/v1/validation-rules";
    private static final String CLIENT = "Client ";

    /**
     * Get validation configurations.
     *
     * @param tenantDomain  tenant domain.
     * @return  configurations.
     * @throws ValidationConfigurationRetrievalClientException If an error occurred in retrieving the configurations.
     */
    public JSONArray getConfigurations(String tenantDomain)
            throws ValidationConfigurationRetrievalClientException {

        try (CloseableHttpClient httpclient = HttpClientBuilder.create().useSystemProperties().build()) {
            HttpGet request =
                    new HttpGet(getValidationMgtEndpoint(tenantDomain));
            setAuthorizationHeader(request);

            try (CloseableHttpResponse response = httpclient.execute(request)) {

                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    JSONArray jsonResponse = new JSONArray(
                            new JSONTokener(new InputStreamReader(response.getEntity().getContent())));
                    return jsonResponse;
                }
            } finally {
                request.releaseConnection();
            }
        } catch (IOException | JSONException e) {
            //JSONException may occur if the application don't have an access URL configured
            String msg = "Error while getting validation configurations for tenant: " + tenantDomain;
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new ValidationConfigurationRetrievalClientException(msg, e);
        }
        return null;
    }

    /**
     * Method to get password configuration.
     *
     * @param tenantDomain  tenant domain.
     * @return  json object of password configuration.
     * @throws ValidationConfigurationRetrievalClientException If an error occurred in getting password configuration.
     */
    public JSONObject getPasswordConfiguration(String tenantDomain)
            throws ValidationConfigurationRetrievalClientException {

        JSONObject passwordConfig = new JSONObject();
        JSONArray configurations = getConfigurations(tenantDomain);

        if (configurations != null && configurations.length() > 0) {
            for (int i = 0; i < configurations.length(); i++) {
                JSONObject config = (JSONObject) configurations.get(i);
                if (((String)config.get("field")).equalsIgnoreCase("password")) {
                    JSONArray rules = (JSONArray) config.get("rules");
                    for (int j = 0; j < rules.length(); j++) {
                        JSONObject rule = rules.getJSONObject(j);
                        String name = (String)rule.get("validator");
                        if (name.equalsIgnoreCase("LengthValidator")) {
                            addValue("min.length", (JSONArray) rule.get("properties"), passwordConfig,
                                    "minLength");
                            addValue("max.length", (JSONArray) rule.get("properties"), passwordConfig,
                                    "maxLength");
                        } else if (name.equalsIgnoreCase("NumeralValidator")) {
                            addValue("min.length", (JSONArray) rule.get("properties"), passwordConfig,
                                    "minNumber");
                        } else if (name.equalsIgnoreCase("LowerCaseValidator")) {
                            addValue("min.length", (JSONArray) rule.get("properties"), passwordConfig,
                                    "minLowerCase");
                        } else if (name.equalsIgnoreCase("UpperCaseValidator")) {
                            addValue("min.length", (JSONArray) rule.get("properties"), passwordConfig,
                                    "minUpperCase");
                        } else if (name.equalsIgnoreCase("SpecialCharacterValidator")) {
                            addValue("min.length", (JSONArray) rule.get("properties"), passwordConfig,
                                    "minSpecialChr");
                        } else if (name.equalsIgnoreCase("UniqueCharacterValidator")) {
                            addValue("min.unique.character", (JSONArray) rule.get("properties"),
                                    passwordConfig, "minUniqueChr");
                        } else if (name.equalsIgnoreCase("RepeatedCharacterValidator")) {
                            addValue("max.consecutive.character", (JSONArray) rule.get("properties"),
                                    passwordConfig, "maxConsecutiveChr");
                        }
                    }
                }
            }
        }
        if (passwordConfig.length() == 0) {
            passwordConfig.put("minLength", 8);
            passwordConfig.put("maxLength", 30);
            passwordConfig.put("minNumber", 1);
            passwordConfig.put("minUpperCase", 1);
            passwordConfig.put("minLowerCase", 1);
            passwordConfig.put("minSpecialChr", 1);
        }
        return passwordConfig;
    }

    private void addValue(String propertyName, JSONArray properties, JSONObject response, String key) {

        for(int i = 0; i < properties.length(); i++) {
            JSONObject property = properties.getJSONObject(i);
            if (((String)property.get("key")).equalsIgnoreCase(propertyName)) {
                response.put(key, Integer.parseInt((String) property.get("value")));
            }
        }
        return;
    }

    private String getValidationMgtEndpoint(String tenantDomain)
            throws ValidationConfigurationRetrievalClientException {

        return getEndpoint(tenantDomain, VALIDATION_MGT_API_PATH);
    }

    private String getEndpoint(String tenantDomain, String context)
            throws ValidationConfigurationRetrievalClientException {

        try {
            return IdentityManagementEndpointUtil.getBasePath(tenantDomain, context);
        } catch (ApiException e) {
            throw new ValidationConfigurationRetrievalClientException("Error while building url for context: " +
                    context);
        }
    }

    private void setAuthorizationHeader(HttpRequestBase httpMethod) {

        String toEncode = IdentityManagementServiceUtil.getInstance().getAppName() + ":"
                + String.valueOf(IdentityManagementServiceUtil.getInstance().getAppPassword());
        byte[] encoding = Base64.encodeBase64(toEncode.getBytes());
        String authHeader = new String(encoding, Charset.defaultCharset());
        httpMethod.addHeader(HTTPConstants.HEADER_AUTHORIZATION, CLIENT + authHeader);
    }
}
