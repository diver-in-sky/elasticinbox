package com.elasticinbox.pipe.config;

public class QueueConfig {
    private String queueName = "";
    private Boolean queueAutodelete = false;
    private Boolean queueDurable = true;
    private Boolean queueDeclare = false;

    private String exchangeName = "";
    private Boolean exchangeDurable = true;
    private Boolean exchangeDeclare = true;
    private String exchangeType = "direct";

    private String routingKey = "";

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public Boolean getQueueAutodelete() {
        return queueAutodelete;
    }

    public void setQueueAutodelete(Boolean queueAutodelete) {
        this.queueAutodelete = queueAutodelete;
    }

    public Boolean getQueueDurable() {
        return queueDurable;
    }

    public void setQueueDurable(Boolean queueDurable) {
        this.queueDurable = queueDurable;
    }

    public Boolean getQueueDeclare() {
        return queueDeclare;
    }

    public void setQueueDeclare(Boolean queueDeclare) {
        this.queueDeclare = queueDeclare;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public Boolean getExchangeDeclare() {
        return exchangeDeclare;
    }

    public void setExchangeDeclare(Boolean exchangeDeclare) {
        this.exchangeDeclare = exchangeDeclare;
    }

    public String getExchangeType() {
        return exchangeType;
    }

    public void setExchangeType(String exchangeType) {
        this.exchangeType = exchangeType;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    public Boolean getExchangeDurable() {
        return exchangeDurable;
    }

    public void setExchangeDurable(Boolean exchangeDurable) {
        this.exchangeDurable = exchangeDurable;
    }

    public String toString() {
        return queueName + " [autodelete=" + queueAutodelete + " durable=" +
                queueDurable + " declare=" + queueDeclare + "] " + exchangeName +
                " [type=" + exchangeType + " durable=" + exchangeDurable + " declare" +
                exchangeDeclare + "] routing: " + routingKey;
    }
}
