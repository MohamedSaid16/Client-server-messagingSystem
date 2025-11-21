import javax.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;
import java.util.concurrent.ConcurrentHashMap;

public class ServerMessageService {
    private static final String BROKER_URL = "tcp://localhost:61616";
    private static ServerMessageService instance;
    
    private Connection connection;
    private Session session;
    private ConcurrentHashMap<String, MessageProducer> producers;
    
    public static ServerMessageService getInstance() {
        if (instance == null) {
            instance = new ServerMessageService();
        }
        return instance;
    }
    
    private ServerMessageService() {
        this.producers = new ConcurrentHashMap<>();
        initialize();
    }
    
    private void initialize() {
        try {
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(BROKER_URL);
            connectionFactory.setTrustAllPackages(true); // This allows all serializable objects
            
            connection = connectionFactory.createConnection();
            connection.start();
            
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
            System.out.println("✅ Server Message Service connected to ActiveMQ");
            
        } catch (JMSException e) {
            System.err.println("❌ Failed to initialize Server Message Service: " + e.getMessage());
        }
    }
    
    public void sendToUser(int userId, String userType, java.io.Serializable message) {
        try {
            String queueName = "USER." + userType.toUpperCase() + "." + userId;
            MessageProducer producer = getProducer(queueName);
            
            ObjectMessage jmsMessage = session.createObjectMessage(message);
            producer.send(jmsMessage);
            
            System.out.println("📤 Message sent to " + queueName);
            
        } catch (JMSException e) {
            System.err.println("❌ Error sending message: " + e.getMessage());
        }
    }
    
    public void broadcastToUserType(String userType, java.io.Serializable message) {
        try {
            String topicName = "BROADCAST." + userType.toUpperCase();
            Topic topic = session.createTopic(topicName);
            MessageProducer producer = session.createProducer(topic);
            
            ObjectMessage jmsMessage = session.createObjectMessage(message);
            producer.send(jmsMessage);
            producer.close();
            
            System.out.println("📢 Broadcast sent to " + topicName);
            
        } catch (JMSException e) {
            System.err.println("❌ Error broadcasting: " + e.getMessage());
        }
    }
    
    public void sendNotification(int userId, java.io.Serializable notification) {
        try {
            String queueName = "NOTIFICATION." + userId;
            MessageProducer producer = getProducer(queueName);
            
            ObjectMessage jmsMessage = session.createObjectMessage(notification);
            producer.send(jmsMessage);
            
            System.out.println("🔔 Notification sent to user " + userId);
            
        } catch (JMSException e) {
            System.err.println("❌ Error sending notification: " + e.getMessage());
        }
    }
    
    private MessageProducer getProducer(String queueName) throws JMSException {
        MessageProducer producer = producers.get(queueName);
        if (producer == null) {
            Queue queue = session.createQueue(queueName);
            producer = session.createProducer(queue);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producers.put(queueName, producer);
        }
        return producer;
    }
    
    public void close() {
        try {
            for (MessageProducer producer : producers.values()) {
                producer.close();
            }
            if (session != null) session.close();
            if (connection != null) connection.close();
            System.out.println("✅ Server Message Service closed");
        } catch (JMSException e) {
            System.err.println("❌ Error closing: " + e.getMessage());
        }
    }
}