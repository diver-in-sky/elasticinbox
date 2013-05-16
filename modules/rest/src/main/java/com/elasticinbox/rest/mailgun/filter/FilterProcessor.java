package com.elasticinbox.rest.mailgun.filter;

import java.util.LinkedList;
import java.util.List;

public class FilterProcessor<T> {
    private List<Filter<T>> filters;

    public FilterProcessor() {
        filters = new LinkedList<Filter<T>>();
    }

    public void add(Filter<T> filter) {
        filters.add(filter);
    }

    public T doFilter(final T input) {
        T output = input;
        for (Filter<T> filter : filters) {
            output = filter.filter(output);
        }

        return output;
    }
}
