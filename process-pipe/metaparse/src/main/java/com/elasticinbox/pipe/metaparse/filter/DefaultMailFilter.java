package com.elasticinbox.pipe.metaparse.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticinbox.config.Configurator;
import com.elasticinbox.core.model.Message;
import com.elasticinbox.core.model.ReservedLabels;

public final class DefaultMailFilter implements Filter<Message> {
    private static final Logger logger = LoggerFactory.getLogger(DefaultMailFilter.class);

    @Override
    public Message filter(Message message) {
        // by default store in Inbox
        if (message.getLabels().isEmpty()) {
            message.addLabel(ReservedLabels.INBOX.getId());

            // add to POP3 if enabled
            if (Configurator.isLmtpPop3Enabled()) {
                logger.debug("Adding message received via LMTP to POP3");
                message.addLabel(ReservedLabels.POP3.getId());
            }
        }

        return message;
    }
}
