import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.sql.*;
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;
public class StudentController {
    private ClientService clientService;
    private List<MessageService.MessageListener> messageListeners;
    public StudentController() {
       this.clientService = new ClientService();
        this.messageListeners = new ArrayList<>();
    }
    
    // Student Information
    public Map<String, String> getStudentInfo(int studentId) {
        Map<String, String> info = clientService.getStudentInfo(studentId);
        if (info != null) {
            return info;
        }
        return new HashMap<>();
    }
    
    // Grades Management
    public List<Map<String, String>> getStudentGrades(int studentId) {
        try {
            return clientService.getStudentGrades(studentId);
        } catch (Exception e) {
            System.err.println("Error getting student grades: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public List<Map<String, String>> getGradesBySubject(int studentId, String subject) {
        try {
            List<Map<String, String>> allGrades = getStudentGrades(studentId);
            if (allGrades != null) {
                List<Map<String, String>> filtered = new ArrayList<>();
                for (Map<String, String> grade : allGrades) {
                    if (subject.equals(grade.get("subject"))) {
                        filtered.add(grade);
                    }
                }
                return filtered;
            }
        } catch (Exception e) {
            System.err.println("Error getting grades by subject: " + e.getMessage());
        }
        return new ArrayList<>();
    }
    
    public Map<String, Double> getSubjectAverages(int studentId) {
        try {
            List<Map<String, String>> grades = getStudentGrades(studentId);
            Map<String, Double> subjectAverages = new HashMap<>();
            Map<String, Double> subjectTotals = new HashMap<>();
            Map<String, Double> subjectWeights = new HashMap<>();
            
            if (grades != null && !grades.isEmpty()) {
                for (Map<String, String> grade : grades) {
                    String subject = grade.get("subject");
                    double score = Double.parseDouble(grade.get("score"));
                    double coefficient = Double.parseDouble(grade.get("coefficient"));
                    
                    // Update totals and weights
                    double currentTotal = subjectTotals.getOrDefault(subject, 0.0);
                    double currentWeight = subjectWeights.getOrDefault(subject, 0.0);
                    
                    subjectTotals.put(subject, currentTotal + (score * coefficient));
                    subjectWeights.put(subject, currentWeight + coefficient);
                }
                
                // Calculate averages
                for (String subject : subjectTotals.keySet()) {
                    double total = subjectTotals.get(subject);
                    double weight = subjectWeights.get(subject);
                    double average = total / weight;
                    subjectAverages.put(subject, Math.round(average * 100.0) / 100.0);
                }
            }
            
            return subjectAverages;
        } catch (Exception e) {
            System.err.println("Error calculating subject averages: " + e.getMessage());
            return new HashMap<>();
        }
    }
    
    // Academic Performance
    public double getOverallAverage(int studentId) {
        try {
            Double average = clientService.getOverallAverage(studentId);
            return average != null ? average : 0.0;
        } catch (Exception e) {
            System.err.println("Error getting overall average: " + e.getMessage());
            return 0.0;
        }
    }
    
    // FIXED: Changed return type to String to match ClientService
    public String getFinalStatus(int studentId) {
        try {
            return clientService.getFinalStatus(studentId);
        } catch (Exception e) {
            System.err.println("Error getting final status: " + e.getMessage());
            return "UNKNOWN";
        }
    }
    
    // New method that returns detailed status info
    public Map<String, Object> getFinalStatusDetailed(int studentId) {
        try {
            String status = getFinalStatus(studentId);
            double average = getOverallAverage(studentId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", status);
            result.put("average", average);
            result.put("statusArabic", convertStatusToArabic(status));
            result.put("statusEnglish", convertStatusToEnglish(status));
            result.put("canProceed", !"EXCLU".equals(status) && average >= 10.0);
            
            return result;
        } catch (Exception e) {
            System.err.println("Error getting final status: " + e.getMessage());
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("status", "UNKNOWN");
            errorResult.put("average", 0.0);
            errorResult.put("statusArabic", "غير معروف");
            errorResult.put("statusEnglish", "Unknown");
            errorResult.put("canProceed", false);
            return errorResult;
        }
    }
    
    public Map<String, Object> getAcademicProgress(int studentId) {
        try {
            double average = getOverallAverage(studentId);
            List<Map<String, String>> grades = getStudentGrades(studentId);
            int totalExams = grades != null ? grades.size() : 0;
            int passedExams = 0;
            
            if (grades != null && !grades.isEmpty()) {
                for (Map<String, String> grade : grades) {
                    double score = Double.parseDouble(grade.get("score"));
                    if (score >= 10.0) {
                        passedExams++;
                    }
                }
            }
            
            double progressPercentage = totalExams > 0 ? (passedExams * 100.0 / totalExams) : 0.0;
            
            Map<String, Object> progress = new HashMap<>();
            progress.put("overallAverage", average);
            progress.put("totalExams", totalExams);
            progress.put("passedExams", passedExams);
            progress.put("progressPercentage", Math.round(progressPercentage * 100.0) / 100.0);
            progress.put("academicLevel", getAcademicLevel(average));
            
            return progress;
        } catch (Exception e) {
            System.err.println("Error getting academic progress: " + e.getMessage());
            Map<String, Object> errorProgress = new HashMap<>();
            errorProgress.put("overallAverage", 0.0);
            errorProgress.put("totalExams", 0);
            errorProgress.put("passedExams", 0);
            errorProgress.put("progressPercentage", 0.0);
            errorProgress.put("academicLevel", " Unknown");
            return errorProgress;
        }
    }
    
    // Utility Methods
    public String convertStatusToArabic(String status) {
        if (status == null) return "Unknown ";
        
        switch (status.toUpperCase()) {
            case "ADMIS": return "ADMIS";
            case "REDOUBLANT": return "Redoublant";
            case "EXCLU": return "Exclu";
            case "IN_PROGRESS": return "In Progress";
            default: return "Unknown";
        }
    }
    
    public String convertStatusToEnglish(String status) {
        if (status == null) return "Unknown";
        
        switch (status.toUpperCase()) {
            case "ADMIS": return "Admitted";
            case "REDOUBLANT": return "Repeating";
            case "EXCLU": return "Excluded";
            case "IN_PROGRESS": return "In Progress";
            default: return "Unknown";
        }
    }
    
    private String getAcademicLevel(double average) {
        if (average >= 16) return "Excellent";
        else if (average >= 14) return "Very Good";
        else if (average >= 12) return "Good";
        else if (average >= 10) return "Acceptable";
        else return "Weak";
    }
    
    // Personal Information Management
    public boolean updateContactInfo(int studentId, String email, String phone) {
        try {
            // Implementation for updating contact info
            System.out.println("Updating contact info for student ID: " + studentId);
            return true;
        } catch (Exception e) {
            System.err.println("Error updating contact info: " + e.getMessage());
            return false;
        }
    }
    
    // Add this method to StudentController class
    public List<Map<String, String>> getStudentTranscript(int studentId) {
        return clientService.getStudentTranscript(studentId);
    }
    
    // Document Generation
    public String generateGradeReport(int studentId) {
        try {
            Map<String, String> studentInfo = getStudentInfo(studentId);
            List<Map<String, String>> grades = getStudentGrades(studentId);
            double average = getOverallAverage(studentId);
            Map<String, Object> status = getFinalStatusDetailed(studentId);
            
            StringBuilder report = new StringBuilder();
            report.append("Student Grade Report\n");
            report.append("====================\n");
            report.append("Name: ").append(studentInfo.get("firstName")).append(" ").append(studentInfo.get("lastName")).append("\n");
            report.append("Program: ").append(studentInfo.get("program")).append("\n");
            report.append("Overall Average: ").append(String.format("%.2f", average)).append("\n");
            report.append("Final Status: ").append(status.get("statusEnglish")).append("\n\n");
            report.append("Grades:\n");
            
            if (grades != null && !grades.isEmpty()) {
                for (Map<String, String> grade : grades) {
                    report.append("- ").append(grade.get("subject"))
                          .append(" | ").append(grade.get("exam"))
                          .append(" | ").append(grade.get("score"))
                          .append("/20\n");
                }
            } else {
                report.append("No grades available.\n");
            }
            
            return report.toString();
        } catch (Exception e) {
            System.err.println("Error generating grade report: " + e.getMessage());
            return "Error generating report";
        }
    }
    
    // Add these missing methods that might be called from views
    public Double getOverallAverageAsDouble(int studentId) {
        return getOverallAverage(studentId);
    }
    
    // Sample data for testing
    public List<Map<String, String>> getSampleGrades() {
        ArrayList<Map<String, String>> sampleGrades = new ArrayList<>();
        
        Map<String, String> grade1 = new HashMap<>();
        grade1.put("subject", "Mathematics");
        grade1.put("exam", "Midterm Exam");
        grade1.put("score", "16.5");
        grade1.put("coefficient", "2.0");
        sampleGrades.add(grade1);
        
        Map<String, String> grade2 = new HashMap<>();
        grade2.put("subject", "Mathematics");
        grade2.put("exam", "Final Exam");
        grade2.put("score", "15.0");
        grade2.put("coefficient", "3.0");
        sampleGrades.add(grade2);
        
        Map<String, String> grade3 = new HashMap<>();
        grade3.put("subject", "Computer Science");
        grade3.put("exam", "Project");
        grade3.put("score", "18.0");
        grade3.put("coefficient", "2.5");
        sampleGrades.add(grade3);
        
        return sampleGrades;
    }
    
    // ADD this method to your StudentController class:
    public List<Map<String, String>> getStudentSubjects(int studentId) {
        try {
            return clientService.getSubjectsByStudent(studentId);
        } catch (Exception e) {
            System.err.println("Error getting student subjects: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    // MESSAGING METHODS - COMPLETE REAL IMPLEMENTATIONS
    
    // Send message to teacher
public boolean sendMessageToTeacher(int studentId, int teacherId, String subject, String content) {
    Connection conn = null;
    PreparedStatement stmt = null;
    
    try {
        conn = clientService.getConnection();
        
        String sql = "INSERT INTO messages (sender_id, sender_type, receiver_id, receiver_type, subject, content, message_type, created_at) " +
                    "VALUES (?, 'STUDENT', ?, 'TEACHER', ?, ?, 'MESSAGE', NOW())";
        
        stmt = conn.prepareStatement(sql);
        stmt.setInt(1, studentId);
        stmt.setInt(2, teacherId);
        stmt.setString(3, subject);
        stmt.setString(4, content);
        
        int rowsAffected = stmt.executeUpdate();
        
        if (rowsAffected > 0) {
            // ✅ ADD THIS: Send via MessageService for real-time delivery
            MessageService messageService = MessageService.getInstance();
            if (messageService != null && messageService.isInitialized()) {
                Message message = new Message(studentId, "STUDENT", teacherId, "TEACHER", subject, content, "MESSAGE");
                boolean realTimeSent = messageService.sendUserMessage(teacherId, "teacher", message);
                System.out.println("📤 Student message sent via MessageService: " + realTimeSent);
            }
            
            System.out.println("✅ Message sent to teacher ID: " + teacherId + " from student ID: " + studentId);
            return true;
        } else {
            System.err.println("❌ Failed to send message to teacher");
            return false;
        }
        
    } catch (SQLException e) {
        System.err.println("❌ Error sending message to teacher: " + e.getMessage());
        return false;
    } finally {
        try {
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
}
    
    // Send message to responsable
public boolean sendMessageToResponsable(int studentId, int responsableId, String subject, String content) {
    Connection conn = null;
    PreparedStatement stmt = null;
    
    try {
        conn = clientService.getConnection();
        
        String sql = "INSERT INTO messages (sender_id, sender_type, receiver_id, receiver_type, subject, content, message_type, created_at) " +
                    "VALUES (?, 'STUDENT', ?, 'RESPONSABLE', ?, ?, 'MESSAGE', NOW())";
        
        stmt = conn.prepareStatement(sql);
        stmt.setInt(1, studentId);
        stmt.setInt(2, responsableId);
        stmt.setString(3, subject);
        stmt.setString(4, content);
        
        int rowsAffected = stmt.executeUpdate();
        
        if (rowsAffected > 0) {
            // ✅ ADD THIS: Send via MessageService for real-time delivery
            MessageService messageService = MessageService.getInstance();
            if (messageService != null && messageService.isInitialized()) {
                Message message = new Message(studentId, "STUDENT", responsableId, "RESPONSABLE", subject, content, "MESSAGE");
                boolean realTimeSent = messageService.sendUserMessage(responsableId, "responsable", message);
                System.out.println("📤 Student message sent to responsable via MessageService: " + realTimeSent);
            }
            
            System.out.println("✅ Message sent to responsable ID: " + responsableId + " from student ID: " + studentId);
            return true;
        } else {
            System.err.println("❌ Failed to send message to responsable");
            return false;
        }
        
    } catch (SQLException e) {
        System.err.println("❌ Error sending message to responsable: " + e.getMessage());
        return false;
    } finally {
        try {
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
}
    
    // Send message to admin
public boolean sendMessageToAdmin(int studentId, int adminId, String subject, String content) {
    Connection conn = null;
    PreparedStatement stmt = null;
    
    try {
        conn = clientService.getConnection();
        
        String sql = "INSERT INTO messages (sender_id, sender_type, receiver_id, receiver_type, subject, content, message_type, created_at) " +
                    "VALUES (?, 'STUDENT', ?, 'ADMIN', ?, ?, 'MESSAGE', NOW())";
        
        stmt = conn.prepareStatement(sql);
        stmt.setInt(1, studentId);
        stmt.setInt(2, adminId);
        stmt.setString(3, subject);
        stmt.setString(4, content);
        
        int rowsAffected = stmt.executeUpdate();
        
        if (rowsAffected > 0) {
            // ✅ ADD THIS: Send via MessageService for real-time delivery
            MessageService messageService = MessageService.getInstance();
            if (messageService != null && messageService.isInitialized()) {
                Message message = new Message(studentId, "STUDENT", adminId, "ADMIN", subject, content, "MESSAGE");
                boolean realTimeSent = messageService.sendUserMessage(adminId, "admin", message);
                System.out.println("📤 Student message sent to admin via MessageService: " + realTimeSent);
            }
            
            System.out.println("✅ Message sent to admin ID: " + adminId + " from student ID: " + studentId);
            return true;
        } else {
            System.err.println("❌ Failed to send message to admin");
            return false;
        }
        
    } catch (SQLException e) {
        System.err.println("❌ Error sending message to admin: " + e.getMessage());
        return false;
    } finally {
        try {
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
}
    
    // COMPLETELY FIXED subscription method - only takes studentId
    public void subscribeToStudentMessages(int studentId) {
        if (studentId <= 0) {
            System.err.println("❌ Invalid student ID for subscription: " + studentId);
            return;
        }
        
        try {
            MessageService messageService = MessageService.getInstance();
            
            if (messageService == null || !messageService.isInitialized()) {
                System.err.println("❌ MessageService not available for subscription");
                return;
            }
            
            // Subscribe to personal messages
            messageService.subscribeToUserMessages(studentId, "student", new MessageService.MessageListener() {
                @Override
                public void onMessageReceived(Message message) {
                    System.out.println("📨 Student " + studentId + " received personal message: " + message.getSubject());
                    handleIncomingMessage(message); // This method exists now
                }
            });
            
            // Subscribe to broadcasts
            messageService.subscribeToBroadcast("student", new MessageService.MessageListener() {
                @Override
                public void onMessageReceived(Message message) {
                    System.out.println("📢 Student " + studentId + " received broadcast: " + message.getSubject());
                    handleIncomingMessage(message); // This method exists now
                }
            });
            
            System.out.println("✅ Student " + studentId + " subscribed successfully to messages");
            System.out.println("   📡 Subscribed to: user.student." + studentId);
            System.out.println("   📡 Subscribed to: broadcast.student");
            
        } catch (Exception e) {
            System.err.println("❌ Error subscribing student to messages: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Get message history for student
    public List<Map<String, String>> getMessageHistory(int studentId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Map<String, String>> messages = new ArrayList<>();
        
        try {
            conn = clientService.getConnection();
            
            String sql = "SELECT m.message_id, m.sender_id, m.sender_type, m.subject, m.content, " +
                        "m.message_type, m.created_at, m.is_read, " +
                        "CASE " +
                        "   WHEN m.sender_type = 'TEACHER' THEN (SELECT CONCAT(first_name, ' ', last_name) FROM teachers WHERE teacher_id = m.sender_id) " +
                        "   WHEN m.sender_type = 'RESPONSABLE' THEN (SELECT CONCAT(first_name, ' ', last_name) FROM responsables WHERE responsable_id = m.sender_id) " +
                        "   WHEN m.sender_type = 'ADMIN' THEN (SELECT CONCAT(first_name, ' ', last_name) FROM admins WHERE admin_id = m.sender_id) " +
                        "   ELSE 'System' " +
                        "END as sender_name " +
                        "FROM messages m " +
                        "WHERE m.receiver_id = ? AND m.receiver_type = 'STUDENT' " +
                        "ORDER BY m.created_at DESC";
            
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, studentId);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, String> message = new HashMap<>();
                message.put("messageId", String.valueOf(rs.getInt("message_id")));
                message.put("senderId", String.valueOf(rs.getInt("sender_id")));
                message.put("senderType", rs.getString("sender_type"));
                message.put("senderName", rs.getString("sender_name"));
                message.put("subject", rs.getString("subject"));
                message.put("content", rs.getString("content"));
                message.put("messageType", rs.getString("message_type"));
                message.put("createdAt", rs.getString("created_at"));
                message.put("isRead", rs.getBoolean("is_read") ? "Yes" : "No");
                
                messages.add(message);
            }
            
            System.out.println("✅ Retrieved " + messages.size() + " messages for student ID: " + studentId);
            
        } catch (SQLException e) {
            System.err.println("❌ Error getting message history: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
        
        return messages;
    }
    
    // Get unread messages count
    public int getUnreadMessageCount(int studentId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = clientService.getConnection();
            
            String sql = "SELECT COUNT(*) as unread_count FROM messages " +
                        "WHERE receiver_id = ? AND receiver_type = 'STUDENT' AND is_read = FALSE";
            
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, studentId);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                int count = rs.getInt("unread_count");
                System.out.println("✅ Unread message count for student: " + count);
                return count;
            }
            
            return 0;
            
        } catch (SQLException e) {
            System.err.println("❌ Error getting unread message count: " + e.getMessage());
            return 0;
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }
    
    // Mark message as read
    public boolean markMessageAsRead(int messageId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = clientService.getConnection();
            
            String sql = "UPDATE messages SET is_read = TRUE WHERE message_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, messageId);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Marked message ID: " + messageId + " as read");
                return true;
            } else {
                System.err.println("❌ Failed to mark message as read");
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error marking message as read: " + e.getMessage());
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }
    
    // Delete message
    public boolean deleteMessage(int messageId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = clientService.getConnection();
            
            String sql = "DELETE FROM messages WHERE message_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, messageId);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Deleted message ID: " + messageId);
                return true;
            } else {
                System.err.println("❌ Failed to delete message");
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error deleting message: " + e.getMessage());
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }
    
    // Get teachers for messaging (to populate dropdown)
    public List<Map<String, String>> getTeachersForMessaging(int studentId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Map<String, String>> teachers = new ArrayList<>();
        
        try {
            conn = clientService.getConnection();
            
            String sql = "SELECT DISTINCT t.teacher_id, t.first_name, t.last_name, t.email, s.subject_name " +
                        "FROM teachers t " +
                        "JOIN teacher_subjects ts ON t.teacher_id = ts.teacher_id " +
                        "JOIN subjects s ON ts.subject_id = s.subject_id " +
                        "JOIN student_subjects ss ON s.subject_id = ss.subject_id " +
                        "WHERE ss.student_id = ? " +
                        "ORDER BY t.first_name, t.last_name";
            
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, studentId);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, String> teacher = new HashMap<>();
                teacher.put("teacherId", String.valueOf(rs.getInt("teacher_id")));
                teacher.put("firstName", rs.getString("first_name"));
                teacher.put("lastName", rs.getString("last_name"));
                teacher.put("fullName", rs.getString("first_name") + " " + rs.getString("last_name"));
                teacher.put("email", rs.getString("email"));
                teacher.put("subject", rs.getString("subject_name"));
                
                teachers.add(teacher);
            }
            
            System.out.println("✅ Retrieved " + teachers.size() + " teachers for student ID: " + studentId);
            
        } catch (SQLException e) {
            System.err.println("❌ Error getting teachers for messaging: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
        
        return teachers;
    }
    
    // Get responsables for messaging
    public List<Map<String, String>> getResponsablesForMessaging() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Map<String, String>> responsables = new ArrayList<>();
        
        try {
            conn = clientService.getConnection();
            
            String sql = "SELECT responsable_id, first_name, last_name, email, phone " +
                        "FROM responsables " +
                        "ORDER BY first_name, last_name";
            
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, String> responsable = new HashMap<>();
                responsable.put("responsableId", String.valueOf(rs.getInt("responsable_id")));
                responsable.put("firstName", rs.getString("first_name"));
                responsable.put("lastName", rs.getString("last_name"));
                responsable.put("fullName", rs.getString("first_name") + " " + rs.getString("last_name"));
                responsable.put("email", rs.getString("email"));
                responsable.put("phone", rs.getString("phone"));
                
                responsables.add(responsable);
            }
            
            System.out.println("✅ Retrieved " + responsables.size() + " responsables for messaging");
            
        } catch (SQLException e) {
            System.err.println("❌ Error getting responsables for messaging: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
        
        return responsables;
    }
    
    // Send grade inquiry to teacher
    public boolean sendGradeInquiry(int studentId, int teacherId, String subject, String exam, String message) {
        String subjectLine = "Grade Inquiry: " + subject + " - " + exam;
        String content = "Dear Teacher,\n\n" +
                        "I would like to inquire about my grade for " + subject + " - " + exam + ".\n\n" +
                        "Additional details:\n" + message + "\n\n" +
                        "Thank you,\nStudent ID: " + studentId;
        
        return sendMessageToTeacher(studentId, teacherId, subjectLine, content);
    }
    
    // Send academic advice request
    public boolean sendAcademicAdviceRequest(int studentId, int responsableId, String message) {
        String subject = "Academic Advice Request";
        String content = "Dear Academic Advisor,\n\n" +
                        "I would like to request academic advice regarding my studies.\n\n" +
                        "My concerns:\n" + message + "\n\n" +
                        "Thank you,\nStudent ID: " + studentId;
        
        return sendMessageToResponsable(studentId, responsableId, subject, content);
    }
    public void addMessageListener(MessageService.MessageListener listener) {
        if (listener != null) {
            messageListeners.add(listener);
            System.out.println("✅ Added message listener to student controller");
        }
    }

private void handleIncomingMessage(Message message) {
    try {
        System.out.println("🔄 Handling incoming message: " + message.getSubject());
        
        // Notify all registered listeners
        for (MessageService.MessageListener listener : messageListeners) {
            try {
                listener.onMessageReceived(message);
            } catch (Exception e) {
                System.err.println("❌ Error in message listener: " + e.getMessage());
            }
        }
        
        // Also show system notification
        SwingUtilities.invokeLater(() -> {
            showSystemNotification(message);
        });
        
    } catch (Exception e) {
        System.err.println("❌ Error handling incoming message: " + e.getMessage());
    }
}
private void showSystemNotification(Message message) {
    try {
        // Create a formatted notification message
        String notificationText = String.format(
            "From: %s\nSubject: %s\n\n%s",
            message.getSenderType(),
            message.getSubject(),
            message.getContent()
        );
        
        // Show notification dialog
        JOptionPane.showMessageDialog(
            null,
            notificationText,
            "📨 New Message Received",
            JOptionPane.INFORMATION_MESSAGE
        );
        
        System.out.println("🔔 System notification shown for message: " + message.getSubject());
        
    } catch (Exception e) {
        System.err.println("❌ Error showing system notification: " + e.getMessage());
    }
}
}