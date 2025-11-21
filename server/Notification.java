import java.io.Serializable;
import java.time.LocalDateTime;

public class Notification implements Serializable {
    private int notificationId;
    private int userId;
    private String title;
    private String message;
    private String type; // INFO, WARNING, ERROR, SUCCESS, GRADE, SYSTEM
    private boolean isRead;
    private boolean isActive;
    private LocalDateTime createdAt;
    private String actionUrl;
    private LocalDateTime expiresAt;

    // Constructors
    public Notification() {}

    public Notification(int userId, String title, String message, String type) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.isRead = false;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public int getNotificationId() { return notificationId; }
    public void setNotificationId(int notificationId) { this.notificationId = notificationId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getActionUrl() { return actionUrl; }
    public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    // Helper methods
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public String getIcon() {
        switch (type) {
            case "INFO": return "ℹ️";
            case "WARNING": return "⚠️";
            case "ERROR": return "❌";
            case "SUCCESS": return "✅";
            case "GRADE": return "📊";
            case "SYSTEM": return "⚙️";
            default: return "📢";
        }
    }

    @Override
    public String toString() {
        return String.format("Notification{id=%d, user=%d, title='%s', type=%s, read=%s}", 
            notificationId, userId, title, type, isRead);
    }
}