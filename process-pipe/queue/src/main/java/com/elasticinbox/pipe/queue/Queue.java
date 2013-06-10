package com.elasticinbox.pipe.queue;


import com.elasticinbox.pipe.config.Configurator;
import com.elasticinbox.pipe.config.QueueConfig;
import com.rabbitmq.client.*;
import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;

public class Queue {
    private final static Logger logger = LoggerFactory.getLogger(Queue.class);

    private Channel channel;

    private Connection connection;

    private QueueConfig queueConfig;

    private QueueingConsumer consumer;

    public Queue(QueueConfig config, String user, String password, String vHost) {
        queueConfig = config;

        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setUsername(user);
        connectionFactory.setPassword(password);
        connectionFactory.setVirtualHost(vHost);

        try {
            connection = connectionFactory.newConnection(Configurator.getRabbitAddresses());
            channel = connection.createChannel();

            if (queueConfig.getQueueDeclare()) {
                channel.queueDeclare(queueConfig.getQueueName(), queueConfig.getQueueDurable(), false,
                        queueConfig.getQueueAutodelete(), new HashMap<String, Object>());
            }
            if (queueConfig.getExchangeDeclare()) {
                channel.exchangeDeclare(queueConfig.getExchangeName(),
                        queueConfig.getExchangeType(),
                        queueConfig.getExchangeDurable());
                if (queueConfig.getQueueDeclare()) {
                    channel.queueBind(queueConfig.getQueueName(),
                            queueConfig.getExchangeName(),
                            queueConfig.getRoutingKey());
                }
            }
        } catch (IOException e) {
            logger.error("Exception during queue create", e);
        }
    }

    public void publish(byte[] message) throws IOException {
        channel.basicPublish(queueConfig.getExchangeName(),
                queueConfig.getRoutingKey(),
                MessageProperties.PERSISTENT_BASIC, message);
    }

    public QueueingConsumer createConsumer() throws IOException {
        consumer = new QueueingConsumer(channel);
        channel.basicConsume(queueConfig.getQueueName(), false, consumer);
        return consumer;
    }

    public void ack(long deliveryTag) throws IOException {
        channel.basicAck(deliveryTag, false);
    }

    public void nack(long deliveryTag, boolean requeue) throws IOException {
        channel.basicNack(deliveryTag, false, requeue);
    }

    public void close(int code, String message) {
        try {
            channel.close(code, message);
        } catch (Exception e) {
            logger.debug("failed to close channel on [{}]", e);
        }
        try {
            connection.close(code, message);
        } catch (Exception e) {
            logger.debug("failed to close connection on [{}]", e);
        }
    }

    @Override
    public String toString() {
        return queueConfig.toString();
    }

}