
import java.io.Serializable;
import java.time.LocalDateTime;

public class Message implements Serializable {
    private int messageId;
    private int senderId;
    private String senderType; // STUDENT, TEACHER, ADMIN, RESPONSABLE, SYSTEM
    private int receiverId;
    private String receiverType; // STUDENT, TEACHER, ADMIN, RESPONSABLE, ALL
    private String subject;
    private String content;
    private String messageType; // ANNOUNCEMENT, ALERT, NOTIFICATION, MESSAGE, BROADCAST
    private String priority; // LOW, MEDIUM, HIGH, URGENT
    private boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private String relatedEntityType; // GRADE, EXAM, PROGRAM, REGISTRATION, NONE
    private Integer relatedEntityId;

    // Constructors
    public Message() {}

    public Message(int senderId, String senderType, int receiverId, String receiverType, 
                   String subject, String content, String messageType) {
        this.senderId = senderId;
        this.senderType = senderType;
        this.receiverId = receiverId;
        this.receiverType = receiverType;
        this.subject = subject;
        this.content = content;
        this.messageType = messageType;
        this.priority = "MEDIUM";
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
        this.relatedEntityType = "NONE";
    }

    // Getters and Setters
    public int getMessageId() { return messageId; }
    public void setMessageId(int messageId) { this.messageId = messageId; }

    public int getSenderId() { return senderId; }
    public void setSenderId(int senderId) { this.senderId = senderId; }

    public String getSenderType() { return senderType; }
    public void setSenderType(String senderType) { this.senderType = senderType; }

    public int getReceiverId() { return receiverId; }
    public void setReceiverId(int receiverId) { this.receiverId = receiverId; }

    public String getReceiverType() { return receiverType; }
    public void setReceiverType(String receiverType) { this.receiverType = receiverType; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public String getRelatedEntityType() { return relatedEntityType; }
    public void setRelatedEntityType(String relatedEntityType) { this.relatedEntityType = relatedEntityType; }

    public Integer getRelatedEntityId() { return relatedEntityId; }
    public void setRelatedEntityId(Integer relatedEntityId) { this.relatedEntityId = relatedEntityId; }

    @Override
    public String toString() {
        return String.format("Message{id=%d, from=%s %d, to=%s %d, subject='%s', type=%s}", 
            messageId, senderType, senderId, receiverType, receiverId, subject, messageType);
    }
}