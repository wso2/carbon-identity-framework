package org.wso2.carbon.identity.core.model;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;

public class FilterData {

    private String filterString;
    private List<String> filterValues = new ArrayList<>();

    public String getFilterString() { return filterString; }

    public void setFilterString(String filterString) { this.filterString = filterString; }

    public List<String> getFilterValues() { return filterValues; }

    public void setFilterValues(List<String> filterValues) { this.filterValues = filterValues; }

    public void addFilterValue(String filterValue) { filterValues.add(filterValue); }
}
