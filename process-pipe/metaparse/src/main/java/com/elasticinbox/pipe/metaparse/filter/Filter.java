package com.elasticinbox.pipe.metaparse.filter;

public interface Filter<T> {
    /**
     * Processes input and returns modified version
     *
     * @param input
     * @return
     */
    T filter(final T input);
}
