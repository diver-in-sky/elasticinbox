package com.elasticinbox.pipe.metaparse.server;

import java.io.IOException;

public class DeliveryException extends IOException {
    private static final long serialVersionUID = 5323215105334028562L;

    /**
     * {@inheritDoc}
     */
    public DeliveryException() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public DeliveryException(String message) {
        super(message);
    }

}
