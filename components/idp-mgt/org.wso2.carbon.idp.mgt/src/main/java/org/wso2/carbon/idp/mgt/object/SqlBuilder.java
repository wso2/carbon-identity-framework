package org.wso2.carbon.idp.mgt.object;

        import java.util.HashMap;
        import java.util.Map;

/**
 * Create SQL builder.
 */
public class SqlBuilder {

    private Map<Integer, String> stringParameters = new HashMap<>();
    private int count = 1;
    private String filter;

    public Map<Integer, String> getPrepareStatement() {
        return stringParameters;
    }

    public void setPreparedStatement(String value) {
        stringParameters.put(count, value);
        count++;
    }

    public void setFilterQuery(String filter) {
        this.filter = filter;
    }

    public String getFilterQuery() {
        return filter;
    }
}

