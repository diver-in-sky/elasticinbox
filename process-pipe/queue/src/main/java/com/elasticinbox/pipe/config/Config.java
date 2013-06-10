package com.elasticinbox.pipe.config;


import java.util.List;
import java.util.Map;

public class Config {
    public String mailgun_key;

    // elasticsearch
    public String elasticsearch_address;
    public int elasticsearch_port;
    public String elasticsearch_cluster;

    // rabbitmq
    public List<String> rabbitmq_addresses;
    public String rabbitmq_user;
    public String rabbitmq_password;
    public String rabbitmq_vhost;

    public Map<String, QueueConfig> queue_configs;

    public String mailgun_send_url;
}
