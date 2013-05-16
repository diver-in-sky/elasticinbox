package com.elasticinbox.rest.mailgun.server;

/**
 * Standard and extended LMTP status codes
 *
 * @see <a href="http://tools.ietf.org/html/rfc3463">RFC3463</a>
 */
public enum DeliveryReturnCode {
    OK,
    TEMPORARY_FAILURE,
    PERMANENT_FAILURE,
    NO_SUCH_USER,
    OVER_QUOTA
}
