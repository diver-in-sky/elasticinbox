package com.elasticinbox.pipe.mailgun;

import java.util.Dictionary;
import java.util.Hashtable;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator, ServletContextListener {

    private final static Logger logger = LoggerFactory.getLogger(Activator.class);

    private volatile MailgunSenderQueueConsumer queueConsumer;
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
    public void contextInitialized(final ServletContextEvent sce) {
        if (getEventAdmin() != null) {
            Dictionary<String, String> properties = new Hashtable<String, String>();
            properties.put("context-path", sce.getServletContext().getContextPath());

            getEventAdmin().sendEvent(new Event("com/elasticinbox/pipe/mailgun/DEPLOYED", properties));
            logger.info("Mailgun REST API Deployed");
        }
    }

    @Override
    public void contextDestroyed(final ServletContextEvent sce) {
        if (getEventAdmin() != null) {
            Dictionary<String, String> properties = new Hashtable<String, String>();
            properties.put("context-path", sce.getServletContext().getContextPath());
            getEventAdmin().sendEvent(new Event("com/elasticinbox/pipe/mailgun/UNDEPLOYED", properties));
            logger.info("Mailgun REST API Undeployed");
        }
    }

    @Override
    public void start(BundleContext context) throws Exception {
        bc = context;
        eaRef = bc.getServiceReference(EventAdmin.class.getName());
        if (eaRef != null) {
            setEventAdmin((EventAdmin) bc.getService(eaRef));
        }

        queueConsumer = new MailgunSenderQueueConsumer();

        thread = new Thread(queueConsumer);
        thread.start();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if (!queueConsumer.isClosed()) {
            queueConsumer.close();
            thread.interrupt();
        }

        if (eaRef != null) {
            setEventAdmin(null);
            bc.ungetService(eaRef);
        }
    }


}
