package com.elasticinbox.rest.mailgun.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticinbox.core.message.MimeParser;
import com.elasticinbox.core.model.Message;
import com.elasticinbox.core.model.ReservedLabels;

public final class SpamMailFilter implements Filter<Message> {
    private static final Logger logger = LoggerFactory
            .getLogger(SpamMailFilter.class);

    private final static String MIME_HEADER_SPAM_VALUE = "YES";

    @Override
    public Message filter(Message message) {
        if (message.getMinorHeader(MimeParser.MIME_HEADER_SPAM) != null
                && message.getMinorHeader(MimeParser.MIME_HEADER_SPAM).equalsIgnoreCase(MIME_HEADER_SPAM_VALUE)) {
            logger.debug("Applying filter for SPAM");
            message.addLabel(ReservedLabels.SPAM.getId());
        }

        return message;
    }
}
