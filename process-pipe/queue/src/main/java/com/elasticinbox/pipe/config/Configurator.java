package com.elasticinbox.pipe.config;

import com.rabbitmq.client.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class Configurator {
    private static final Logger logger = LoggerFactory.getLogger(Configurator.class);

    private static final String DEFAULT_CONFIGURATION = "pipe.yaml";

    private static Config conf;

    /**
     * Inspect the classpath to find storage configuration file
     */
    static URI getStorageConfigURL() throws ConfigurationException {
        URI uri;
        String configUrl = System.getProperty("pipe.config");

        if (configUrl == null) {
            configUrl = DEFAULT_CONFIGURATION;
        }

        try {
            File file = new File(configUrl);
            if (!file.canRead()) {
                throw new ConfigurationException("Cannot read config file: " + configUrl);
            }
            uri = file.toURI();
        } catch (Exception e) {
            logger.error("Error opening logfile: ", e);
            throw new ConfigurationException("Cannot locate " + configUrl);
        }

        return uri;
    }

    static {
        try {
            URI uri = getStorageConfigURL();

            InputStream input;
            File configFile;
            try {
                configFile = new File(uri);
                input = new FileInputStream(configFile);
            } catch (IOException ex) {
                logger.error("cannot read config file {}", ex.getMessage());
                throw new AssertionError(ex);
            }

            Constructor constructor = new Constructor(Config.class);
            Yaml yaml = new Yaml(constructor);
            conf = (Config) yaml.load(input);
        } catch (YAMLException e) {
            logger.error("Fatal configuration error error", e);
            System.err.println(e.getMessage()
                    + "\nInvalid yaml; unable to start server. See log for stacktrace.");
            System.exit(1);
        } catch (ConfigurationException e) {
            logger.error("Fatal configuration error", e);
            System.err.println(e.getMessage()
                    + "\nFatal configuration error; unable to start server. See log for stacktrace.");
            System.exit(1);
        }
    }

    public static String getElasticSearchAddress() {
        return conf.elasticsearch_address;
    }

    public static int getElasticSearchPort() {
        return conf.elasticsearch_port;
    }

    public static String getElasticSearchCluster() {
        return conf.elasticsearch_cluster;
    }

    public static String getRabbitUser() {
        return conf.rabbitmq_user;
    }

    public static String getRabbitPassowrd() {
        return conf.rabbitmq_password;
    }

    public static QueueConfig getQueueConfig(String name) {
        return conf.queue_configs.get(name);
    }

    public static String getRabbitVHost() {
        return conf.rabbitmq_vhost;
    }

    public static Address[] getRabbitAddresses() {
        Address[] res = new Address[conf.rabbitmq_addresses.size()];
        int i = 0;
        for (String addr : conf.rabbitmq_addresses) {
            res[i] = Address.parseAddress(addr);
        }
        return res;
    }

    public static String getMailgunKey() {
        return conf.mailgun_key;
    }

    public static String getMailgunSendUrl() {
        return conf.mailgun_send_url;
    }
}
