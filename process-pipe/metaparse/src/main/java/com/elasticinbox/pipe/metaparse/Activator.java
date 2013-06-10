package com.elasticinbox.pipe.metaparse;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.EventAdmin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

    private final static Logger logger = LoggerFactory.getLogger(Activator.class);

    private volatile MetaparseQueueConsumer queueConsumer;
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
        logger.info("Starting Metaparse...");

        bc = context;
        eaRef = bc.getServiceReference(EventAdmin.class.getName());
        if (eaRef != null) {
            setEventAdmin((EventAdmin) bc.getService(eaRef));
        }

        queueConsumer = new MetaparseQueueConsumer();

        thread = new Thread(queueConsumer);
        thread.start();

        logger.info("Metaparse daemon started.");
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        logger.info("Stopping Metaparse...");

        if (!queueConsumer.isClosed()) {
            queueConsumer.close();
            thread.interrupt();
        }

        if (eaRef != null) {
            setEventAdmin(null);
            bc.ungetService(eaRef);
        }

        logger.info("Metaparse daemon stopped.");
    }


}
