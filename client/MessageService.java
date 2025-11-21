import javax.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQConnection;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class MessageService {
    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String BROKER_USERNAME = "admin";
    private static final String BROKER_PASSWORD = "admin";
    
    private Connection connection;
    private Session session;
    private ConcurrentHashMap<String, MessageConsumer> consumers;
    private ConcurrentHashMap<String, List<MessageListener>> listeners;
    private ConcurrentHashMap<String, MessageProducer> producers;
    
    private static MessageService instance;
    private boolean initialized = false;
    
    public static MessageService getInstance() {
        if (instance == null) {
            instance = new MessageService();
        }
        return instance;
    }
    
    private MessageService() {
        this.consumers = new ConcurrentHashMap<>();
        this.listeners = new ConcurrentHashMap<>();
        this.producers = new ConcurrentHashMap<>();
        initialize();
    }
    
    private void initialize() {
        try {
            // Configure connection factory with better settings
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(BROKER_URL);
            connectionFactory.setUserName(BROKER_USERNAME);
            connectionFactory.setPassword(BROKER_PASSWORD);
            connectionFactory.setTrustAllPackages(true);
            // Performance optimizations
            connectionFactory.setMaxThreadPoolSize(50);
            connectionFactory.setAlwaysSessionAsync(false);
            connectionFactory.setUseAsyncSend(true);
            connectionFactory.setOptimizeAcknowledge(true);
            connectionFactory.setAlwaysSyncSend(false);
            
            // Create connection with client ID for durable subscribers
            connection = connectionFactory.createConnection();
            connection.start();
            
            // Create session with transaction support
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
            initialized = true;
            System.out.println("✅ ActiveMQ connection established successfully");
            System.out.println("📡 Broker URL: " + BROKER_URL);
            System.out.println("👤 Connected as: " + BROKER_USERNAME);
            
        } catch (JMSException e) {
            System.err.println("❌ Failed to initialize ActiveMQ: " + e.getMessage());
            e.printStackTrace();
            initialized = false;
        }
    }
    
    public boolean isInitialized() {
        return initialized;
    }
    
    // Enhanced message sending with retry and error handling
    public boolean sendUserMessage(int userId, String userType, Message message) {
        if (!initialized) {
            System.err.println("❌ MessageService not initialized");
            return false;
        }
        
        MessageProducer producer = null;
        try {
            String queueName = "user." + userType.toLowerCase() + "." + userId;
            
            // Reuse producer if exists, create new one if not
            producer = producers.get(queueName);
            if (producer == null) {
                Queue queue = session.createQueue(queueName);
                producer = session.createProducer(queue);
                
                // Configure producer for better reliability
                producer.setDeliveryMode(DeliveryMode.PERSISTENT);
                producer.setTimeToLive(7 * 24 * 60 * 60 * 1000); // 7 days TTL
                producer.setPriority(4); // Medium priority
                
                producers.put(queueName, producer);
            }
            
            ObjectMessage objectMessage = session.createObjectMessage(message);
            
            // Set message properties for better filtering
            objectMessage.setStringProperty("MessageType", message.getMessageType());
            objectMessage.setStringProperty("SenderType", message.getSenderType());
            objectMessage.setIntProperty("SenderId", message.getSenderId());
            objectMessage.setStringProperty("ReceiverType", message.getReceiverType());
            objectMessage.setIntProperty("ReceiverId", message.getReceiverId());
            objectMessage.setStringProperty("Subject", message.getSubject());
            
            producer.send(objectMessage);
            
            System.out.println("✅ Message sent to " + queueName + 
                             " | Type: " + message.getMessageType() + 
                             " | Subject: " + message.getSubject());
            return true;
            
        } catch (JMSException e) {
            System.err.println("❌ Error sending message to user " + userId + ": " + e.getMessage());
            
            // Remove faulty producer from cache
            if (producer != null) {
                String queueName = "user." + userType.toLowerCase() + "." + userId;
                producers.remove(queueName);
                try {
                    producer.close();
                } catch (JMSException ex) {
                    // Ignore close error
                }
            }
            return false;
        }
    }
    
    // Enhanced broadcast with durable topics
    public boolean broadcastMessage(String userType, Message message) {
        if (!initialized) {
            System.err.println("❌ MessageService not initialized");
            return false;
        }
        
        MessageProducer producer = null;
        try {
            String topicName = "broadcast." + userType.toLowerCase();
            Topic topic = session.createTopic(topicName);
            producer = session.createProducer(topic);
            
            // Configure broadcast producer
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.setTimeToLive(24 * 60 * 60 * 1000); // 24 hours TTL
            
            ObjectMessage objectMessage = session.createObjectMessage(message);
            
            // Set broadcast properties
            objectMessage.setStringProperty("MessageType", "BROADCAST");
            objectMessage.setStringProperty("BroadcastTarget", userType);
            objectMessage.setBooleanProperty("IsBroadcast", true);
            
            producer.send(objectMessage);
            producer.close();
            
            System.out.println("✅ Broadcast sent to " + topicName + 
                             " | Target: " + userType + 
                             " | Subject: " + message.getSubject());
            return true;
            
        } catch (JMSException e) {
            System.err.println("❌ Error broadcasting to " + userType + ": " + e.getMessage());
            if (producer != null) {
                try {
                    producer.close();
                } catch (JMSException ex) {
                    // Ignore close error
                }
            }
            return false;
        }
    }
    
    // Enhanced subscription with durable consumers
    public void subscribeToUserMessages(int userId, String userType, MessageListener listener) {
        if (!initialized) {
            System.err.println("❌ MessageService not initialized");
            return;
        }
        
        try {
            String queueName = "user." + userType.toLowerCase() + "." + userId;
            String consumerKey = queueName + "_" + userId;
            
            if (!consumers.containsKey(consumerKey)) {
                Queue queue = session.createQueue(queueName);
                MessageConsumer consumer = session.createConsumer(queue);
                consumers.put(consumerKey, consumer);
                
                // Use thread-safe list for listeners
                listeners.putIfAbsent(consumerKey, new CopyOnWriteArrayList<>());
                
                // Set message listener
                consumer.setMessageListener(new javax.jms.MessageListener() {
                    @Override
                    public void onMessage(javax.jms.Message jmsMessage) {
                        handleIncomingMessage(jmsMessage, consumerKey);
                    }
                });
                
                System.out.println("✅ Subscribed to messages for " + queueName);
            }
            
            // Add the listener
            listeners.get(consumerKey).add(listener);
            
        } catch (JMSException e) {
            System.err.println("❌ Error subscribing to user messages: " + e.getMessage());
        }
    }
    
public void subscribeToBroadcast(String userType, MessageListener listener) {
    if (!initialized) {
        System.err.println("❌ MessageService not initialized");
        return;
    }
    
    try {
        String topicName = "broadcast." + userType.toLowerCase();
        String consumerKey = "broadcast_" + userType;
        
        if (!consumers.containsKey(consumerKey)) {
            Topic topic = session.createTopic(topicName);
            
            // 🚨 CHANGE THIS LINE: Use regular consumer instead of durable subscriber
            MessageConsumer consumer = session.createConsumer(topic);
            
            consumers.put(consumerKey, consumer);
            listeners.putIfAbsent(consumerKey, new CopyOnWriteArrayList<>());
            
            consumer.setMessageListener(new javax.jms.MessageListener() {
                @Override
                public void onMessage(javax.jms.Message jmsMessage) {
                    handleIncomingMessage(jmsMessage, consumerKey);
                }
            });
            
            System.out.println("✅ Subscribed to broadcasts for " + topicName);
        }
        
        listeners.get(consumerKey).add(listener);
        
    } catch (JMSException e) {
        System.err.println("❌ Error subscribing to broadcast: " + e.getMessage());
    }
}
    
    // Centralized message handling
    private void handleIncomingMessage(javax.jms.Message jmsMessage, String consumerKey) {
        if (jmsMessage instanceof ObjectMessage) {
            try {
                Message message = (Message) ((ObjectMessage) jmsMessage).getObject();
                List<MessageListener> consumerListeners = listeners.get(consumerKey);
                
                if (consumerListeners != null && !consumerListeners.isEmpty()) {
                    for (MessageListener listener : consumerListeners) {
                        try {
                            listener.onMessageReceived(message);
                        } catch (Exception e) {
                            System.err.println("❌ Error in message listener: " + e.getMessage());
                        }
                    }
                }
                
                System.out.println("📨 Message processed: " + message.getSubject() + 
                                 " | Type: " + message.getMessageType());
                
            } catch (JMSException e) {
                System.err.println("❌ Error processing incoming message: " + e.getMessage());
            }
        }
    }
    
    // Unsubscribe from messages
    public void unsubscribeFromUserMessages(int userId, String userType, MessageListener listener) {
        String consumerKey = "user." + userType.toLowerCase() + "." + userId + "_" + userId;
        List<MessageListener> consumerListeners = listeners.get(consumerKey);
        
        if (consumerListeners != null) {
            consumerListeners.remove(listener);
            System.out.println("✅ Unsubscribed listener from " + consumerKey);
            
            // Remove consumer if no listeners left
            if (consumerListeners.isEmpty()) {
                MessageConsumer consumer = consumers.remove(consumerKey);
                if (consumer != null) {
                    try {
                        consumer.close();
                    } catch (JMSException e) {
                        System.err.println("Error closing consumer: " + e.getMessage());
                    }
                }
                listeners.remove(consumerKey);
            }
        }
    }
    
    // Get message statistics
    public void printStatistics() {
        System.out.println("📊 MessageService Statistics:");
        System.out.println("   Active Consumers: " + consumers.size());
        System.out.println("   Active Producers: " + producers.size());
        System.out.println("   Total Listeners: " + listeners.values().stream()
            .mapToInt(List::size)
            .sum());
        System.out.println("   Initialized: " + initialized);
    }
    
    // Close all resources properly
    public void close() {
        try {
            // Close all producers
            for (MessageProducer producer : producers.values()) {
                try {
                    producer.close();
                } catch (JMSException e) {
                    System.err.println("Error closing producer: " + e.getMessage());
                }
            }
            producers.clear();
            
            // Close all consumers
            for (MessageConsumer consumer : consumers.values()) {
                try {
                    consumer.close();
                } catch (JMSException e) {
                    System.err.println("Error closing consumer: " + e.getMessage());
                }
            }
            consumers.clear();
            
            // Clear listeners
            listeners.clear();
            
            // Close session and connection
            if (session != null) {
                session.close();
            }
            if (connection != null) {
                connection.close();
            }
            
            initialized = false;
            System.out.println("✅ ActiveMQ connection closed successfully");
            
        } catch (JMSException e) {
            System.err.println("❌ Error closing MessageService: " + e.getMessage());
        }
    }
    
    // Message listener interface
    public interface MessageListener {
        void onMessageReceived(Message message);
    }
    // Add this method to your MessageService class
public void debugQueuesAndTopics() {
    try {
        System.out.println("=== ActiveMQ Debug Information ===");
        System.out.println("Broker URL: " + BROKER_URL);
        System.out.println("Connection Status: " + (connection != null ? "Connected" : "Disconnected"));
        System.out.println("Session Status: " + (session != null ? "Active" : "Inactive"));
        System.out.println("Active Consumers: " + consumers.size());
        System.out.println("Active Producers: " + producers.size());
        
        // List all active queues/consumers
        consumers.forEach((key, consumer) -> {
            System.out.println("Consumer: " + key);
        });
        
        producers.forEach((key, producer) -> {
            System.out.println("Producer: " + key);
        });
        
    } catch (Exception e) {
        System.err.println("Debug error: " + e.getMessage());
    }
}
}