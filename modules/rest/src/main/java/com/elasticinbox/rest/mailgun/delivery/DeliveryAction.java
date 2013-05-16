package com.elasticinbox.rest.mailgun.delivery;

public enum DeliveryAction {
    /**
     * Accept and deliver
     */
    DELIVER,

    /**
     * Accept and drop silently
     */
    DISCARD,

    /**
     * Reject message permanently (permanently unavailable)
     */
    REJECT,

    /**
     * Defer delivery (temporary unavailable)
     */
    DEFER
}
