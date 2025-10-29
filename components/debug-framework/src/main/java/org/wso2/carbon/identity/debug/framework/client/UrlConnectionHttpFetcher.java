package org.wso2.carbon.identity.debug.framework.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Default HttpFetcher implementation using HttpURLConnection.
 */
public class UrlConnectionHttpFetcher implements HttpFetcher {

    private static final Log LOG = LogFactory.getLog(UrlConnectionHttpFetcher.class);

    @Override
    public Map<String, Object> getJson(String urlStr, Map<String, String> headers) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlStr);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Accept", "application/json");
            if (headers != null) {
                for (Map.Entry<String, String> e : headers.entrySet()) {
                    connection.setRequestProperty(e.getKey(), e.getValue());
                }
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                ObjectMapper mapper = new ObjectMapper();
                try (InputStream in = connection.getInputStream()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> claims = mapper.readValue(in, Map.class);
                    return claims != null ? claims : new HashMap<>();
                }
            } else {
                LOG.debug("Non-200 response from " + urlStr + ": " + responseCode);
            }
        } catch (Exception e) {
            LOG.debug("Error fetching JSON from " + urlStr, e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return new HashMap<>();
    }
}
