package org.wso2.carbon.identity.debug.framework.client;

import java.util.Map;

/**
 * Abstraction for fetching JSON over HTTP. Implementations may use URLConnection, HttpClient, or be mocked in tests.
 */
public interface HttpFetcher {

    /**
     * Perform an HTTP GET and parse the response body as a JSON object into a Map.
     * Returns an empty map on non-200 responses or errors.
     */
    Map<String, Object> getJson(String url, Map<String, String> headers);
}
