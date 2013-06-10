package com.elasticinbox.pipe.elasticsearch;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.EventAdmin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

    private final static Logger logger = LoggerFactory.getLogger(Activator.class);

    private volatile ElasticsearchQueueConsumer queueConsumer;
    private volatile Thread thread;

    private static EventAdmin ea;
    private BundleContext bc;
    private ServiceReference eaRef;

    synchronized static EventAdmin getEventAdmin() {
        return ea;
    }

    synchronized static void setEventAdmin(EventAdmin ea) {
        Activator.ea = ea;
    }

    @Override
    public void start(BundleContext context) throws Exception {
        logger.info("Starting ElasticSearch Indexer...");
        bc = context;
        eaRef = bc.getServiceReference(EventAdmin.class.getName());
        if (eaRef != null) {
            setEventAdmin((EventAdmin) bc.getService(eaRef));
        }

        queueConsumer = new ElasticsearchQueueConsumer();

        thread = new Thread(queueConsumer);
        thread.start();
        logger.info("ElasticSearch Indexer daemon started.");
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        logger.info("Stopping ElasticSearch Indexer...");
        if (!queueConsumer.isClosed()) {
            queueConsumer.close();
            thread.interrupt();
        }

        if (eaRef != null) {
            setEventAdmin(null);
            bc.ungetService(eaRef);
        }
        logger.info("ElasticSearch Indexer daemon stopped.");
    }
}
