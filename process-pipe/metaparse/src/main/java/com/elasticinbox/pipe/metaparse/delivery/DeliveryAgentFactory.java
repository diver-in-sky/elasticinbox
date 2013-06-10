package com.elasticinbox.pipe.metaparse.delivery;

import com.elasticinbox.core.DAOFactory;
import com.elasticinbox.core.MessageDAO;

public class DeliveryAgentFactory {
    private final MessageDAO messageDAO;

    public DeliveryAgentFactory() {
        DAOFactory dao = DAOFactory.getDAOFactory();
        messageDAO = dao.getMessageDAO();
    }

    public IDeliveryAgent getDeliveryAgent() {
        return new ElasticInboxDeliveryAgent(messageDAO);
    }

}
