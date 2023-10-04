package org.wso2.carbon.identity.role.v2.mgt.core;

import java.util.HashMap;
import java.util.Map;

/**
 * Filter query builder class.
 */
public class FilterQueryBuilder {

    private Map<String, String> stringParameters;
    private String filter;

    /**
     * Get filter query builder attributes.
     *
     * @return Map of filter query builder attributes.
     */
    public Map<String, String> getFilterAttributeValue() {

        return stringParameters;
    }

    /**
     * Add a filter query builder attribute.
     *
     * @param value attribute value.
     */
    public void setFilterAttributeValue(String name, String value) {

        if (stringParameters == null) {
            stringParameters = new HashMap<>();
        }
        stringParameters.put(name, value);
    }

    /**
     * Set filter query.
     *
     * @param filter filter query.
     */
    public void setFilterQuery(String filter) {

        this.filter = filter;
    }

    /**
     * Get filter query.
     *
     * @return Filter query string.
     */
    public String getFilterQuery() {

        return filter;
    }
}
