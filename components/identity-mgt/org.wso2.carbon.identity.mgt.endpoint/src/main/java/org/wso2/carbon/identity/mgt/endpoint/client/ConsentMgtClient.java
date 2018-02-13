package org.wso2.carbon.identity.mgt.endpoint.client;

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
import org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointConstants;
import org.wso2.carbon.identity.mgt.endpoint.IdentityManagementServiceUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class ConsentMgtClient {

    private final String CLIENT = "Client ";
    private final Log log = LogFactory.getLog(ConsentMgtClient.class);
    private String BASE_PATH = IdentityManagementServiceUtil.getInstance().getServiceContextURL()
            .replace(IdentityManagementEndpointConstants.UserInfoRecovery.SERVICE_CONTEXT_URL_DOMAIN,
                    "api/identity/consent-mgt/v1.0");
    private final String PURPOSE_ID = "purposeId";
    private final String PURPOSES_ENDPOINT = BASE_PATH + "/consents/purposes";
    private final String PURPOSE_ENDPOINT = BASE_PATH + "/consents/purposes";
    private final String PURPOSES = "purposes";

    public String getPurposes(String tenantDomain) throws ConsentMgtClientException {

        String purposesResponse = null;
        try {
            purposesResponse = executeGet(PURPOSES_ENDPOINT);
            JSONArray purposes = new JSONArray(purposesResponse);

            for (int i = 0; i < purposes.length(); i++) {
                JSONObject purpose = (JSONObject) purposes.get(i);
                purpose = retrievePurpose(purpose.getInt(PURPOSE_ID));
                purposes.put(i, purpose);

            }
            JSONObject purposesJson = new JSONObject();
            purposesJson.put(PURPOSES, purposes);
            return purposesJson.toString();
        } catch (IOException e) {
            throw new ConsentMgtClientException("Error while retrieving purposes", e);
        }
    }

    private String executeGet(String url) throws ConsentMgtClientException, IOException {

        boolean isDebugEnabled = log.isDebugEnabled();
        try (CloseableHttpClient httpclient = HttpClientBuilder.create().useSystemProperties().build()) {

            HttpGet httpGet = new HttpGet(url);
            setAuthorizationHeader(httpGet);

            try (CloseableHttpResponse response = httpclient.execute(httpGet)) {

                if (isDebugEnabled) {
                    log.debug("HTTP status " + response.getStatusLine().getStatusCode());
                }

                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                    String inputLine;
                    StringBuilder responseString = new StringBuilder();

                    while ((inputLine = reader.readLine()) != null) {
                        responseString.append(inputLine);
                    }

                    return responseString.toString();

                } else {
                    throw new ConsentMgtClientException("Error while retriving data from " + url + ". Found http " +
                            "status " + response.getStatusLine());
                }
            } finally {
                httpGet.releaseConnection();
            }
        }
    }

    /**
     * adding OAuth authorization headers to a httpMethod
     *
     * @param httpMethod method which wants to add Authorization header
     */
    private void setAuthorizationHeader(HttpRequestBase httpMethod) {

        String toEncode = IdentityManagementServiceUtil.getInstance().getAppName() + ":"
                + String.valueOf(IdentityManagementServiceUtil.getInstance().getAppPassword());
        byte[] encoding = Base64.encodeBase64(toEncode.getBytes());
        String authHeader = new String(encoding, Charset.defaultCharset());
        httpMethod.addHeader(HTTPConstants.HEADER_AUTHORIZATION,
                CLIENT + authHeader);

    }

    private JSONObject retrievePurpose(int purposeId) throws ConsentMgtClientException, IOException {

        String purposeResponse = executeGet(PURPOSE_ENDPOINT + "/" + purposeId);
        JSONObject purpose = new JSONObject(purposeResponse);
        return purpose;
    }
}
