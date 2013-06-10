package com.elasticinbox.pipe.queue;

import com.elasticinbox.pipe.config.Configurator;
import com.elasticinbox.pipe.config.QueueConfig;
import com.rabbitmq.client.QueueingConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public abstract class AbstractQueueConsumer implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(AbstractQueueConsumer.class);

    protected volatile boolean closed = false;

    protected QueueConfig queueConfig;
    protected QueueingConsumer consumer;

    protected Queue queue;

    public AbstractQueueConsumer(QueueConfig conf) {
        queueConfig = conf;
    }

    public boolean isClosed() {
        return closed;
    }

    public void close() {
        closed = true;
    }

    @Override
    public void run() {
        while (true) {
            if (closed) break;
            queue = new Queue(queueConfig,
                    Configurator.getRabbitUser(),
                    Configurator.getRabbitPassowrd(),
                    Configurator.getRabbitVHost());
            try {
                consumer = queue.createConsumer();
            } catch (Exception e) {
                if (!closed) {
                    logger.warn("failed to create queue [{}]", e);
                }
                queue.close(0, "failed to create queue");
                continue;
            }

            while (true) {
                if (closed) break;
                QueueingConsumer.Delivery task;

                try {
                    task = consumer.nextDelivery();
                } catch (Exception e) {
                    if (!closed) {
                        logger.error("failed to get next message, reconnecting...", e);
                    }
                    queue.close(0, "failed to get message");
                    break;
                }

                if (task != null && task.getBody() != null) {
                    try {
                        processTask(task);
                        queue.ack(task.getEnvelope().getDeliveryTag());
                    } catch (Exception e) {
                        logger.warn("failed to parse request for delivery tag [{}], ack'ing...", e);
                        try {
                            queue.nack(task.getEnvelope().getDeliveryTag(), true);
                        } catch (IOException e1) {
                            logger.warn("failed to ack [{}]", e1, task.getEnvelope().getDeliveryTag());
                        }
                    }
                }
            }
        }
        queue.close(0, "closing consumer");
    }

    protected abstract void processTask(QueueingConsumer.Delivery task) throws IOException;
}
