import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.sql.*;
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;
import java.util.ArrayList;
public class ResponsableController {
    private ClientService clientService;
     private List<MessageService.MessageListener> messageListeners;  // ADD THIS LINE
    public ResponsableController() {
        this.clientService = new ClientService();
      this.messageListeners = new ArrayList<>();  // ADD THIS LINE
    }
    
    // Enhanced Student Management - Fixed with proper database calls
    public boolean addStudent(String firstName, String lastName, String schoolOrigin, String email, String phone) {
        try {
            System.out.println("🔄 [DEBUG] Controller adding student: " + firstName + " " + lastName);
            
            boolean success = clientService.addStudent(firstName, lastName, email, phone, schoolOrigin);
            
            if (success) {
                System.out.println("✅ Student added successfully!");
            } else {
                System.err.println("❌ Failed to add student");
            }
            return success;
        } catch (Exception e) {
            System.err.println("❌ Error adding student: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Enhanced student update with database integration
    public boolean updateStudent(int studentId, Map<String, String> studentData) {
        try {
            System.out.println("🔄 [DEBUG] Updating student ID: " + studentId);
            System.out.println("📝 [DEBUG] Student data: " + studentData);
            
            boolean success = clientService.updateStudentInfo(studentId, studentData);
            
            if (success) {
                System.out.println("✅ Student updated successfully!");
            } else {
                System.err.println("❌ Failed to update student");
            }
            return success;
        } catch (Exception e) {
            System.err.println("❌ Error updating student: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Keep old method for compatibility
    public boolean updateStudent(int studentId, String firstName, String lastName, String schoolOrigin, String email, String phone) {
        Map<String, String> studentData = new HashMap<>();
        studentData.put("firstName", firstName);
        studentData.put("lastName", lastName);
        studentData.put("schoolOrigin", schoolOrigin);
        studentData.put("email", email);
        studentData.put("phone", phone);
        studentData.put("academicYear", "2024-2025"); // Default value
        
        return updateStudent(studentId, studentData);
    }
    
    // Enhanced student deletion with database integration
    public boolean deleteStudent(int studentId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = clientService.getConnection();
            
            // First delete related records (grades, users)
            String deleteGradesSql = "DELETE FROM grades WHERE student_id = ?";
            stmt = conn.prepareStatement(deleteGradesSql);
            stmt.setInt(1, studentId);
            stmt.executeUpdate();
            stmt.close();
            
            String deleteUserSql = "DELETE FROM users WHERE student_id = ?";
            stmt = conn.prepareStatement(deleteUserSql);
            stmt.setInt(1, studentId);
            stmt.executeUpdate();
            stmt.close();
            
            // Then delete the student
            String deleteStudentSql = "DELETE FROM students WHERE student_id = ?";
            stmt = conn.prepareStatement(deleteStudentSql);
            stmt.setInt(1, studentId);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Student deleted successfully!");
                return true;
            } else {
                System.err.println("❌ Failed to delete student");
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error deleting student: " + e.getMessage());
            e.printStackTrace();
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
    
    // Get all students - Fixed with database call
    public List<Map<String, String>> getAllStudents() {
        try {
            List<Map<String, String>> students = clientService.getAllStudents();
            System.out.println("✅ Retrieved " + (students != null ? students.size() : 0) + " students");
            return students;
        } catch (Exception e) {
            System.err.println("❌ Error getting students: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    // Enhanced student details with program information
    public Map<String, String> getStudentDetails(int studentId) {
        try {
            Map<String, String> studentInfo = clientService.getStudentWithProgram(studentId);
            if (studentInfo != null) {
                System.out.println("✅ Retrieved student details for ID: " + studentId);
            } else {
                System.err.println("❌ Student not found with ID: " + studentId);
            }
            return studentInfo;
        } catch (Exception e) {
            System.err.println("❌ Error getting student details: " + e.getMessage());
            return new HashMap<>();
        }
    }
    
    // Enhanced student registration with program
    public boolean registerStudent(int studentId, int programId, String academicYear) {
        try {
            System.out.println("🔄 [DEBUG] Registering student ID: " + studentId + " to program ID: " + programId + " for year: " + academicYear);
            
            boolean success = clientService.registerStudentToProgram(studentId, programId, academicYear);
            
            if (success) {
                System.out.println("✅ Student registered successfully!");
            } else {
                System.err.println("❌ Failed to register student");
            }
            return success;
        } catch (Exception e) {
            System.err.println("❌ Error registering student: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Keep old method for compatibility
    public boolean registerStudent(int studentId, int programId, int yearId) {
        String academicYear = getAcademicYearName(yearId);
        return registerStudent(studentId, programId, academicYear);
    }
    
    // Enhanced registration update
    public boolean updateRegistration(int studentId, int programId, String academicYear) {
        try {
            System.out.println("🔄 [DEBUG] Updating registration for student ID: " + studentId);
            
            boolean success = clientService.registerStudentToProgram(studentId, programId, academicYear);
            
            if (success) {
                System.out.println("✅ Registration updated successfully!");
            } else {
                System.err.println("❌ Failed to update registration");
            }
            return success;
        } catch (Exception e) {
            System.err.println("❌ Error updating registration: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Keep old method for compatibility
    public boolean updateRegistration(int registrationId, int programId, int yearId) {
        String academicYear = getAcademicYearName(yearId);
        return updateRegistration(registrationId, programId, academicYear);
    }
    
    // Enhanced registration deletion
    public boolean deleteRegistration(int studentId) {
        try {
            System.out.println("🔄 [DEBUG] Deleting registration for student ID: " + studentId);
            
            Connection conn = null;
            PreparedStatement stmt = null;
            
            try {
                conn = clientService.getConnection();
                String sql = "UPDATE students SET program_id = NULL, academic_year = NULL WHERE student_id = ?";
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, studentId);
                
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    System.out.println("✅ Registration deleted successfully!");
                    return true;
                } else {
                    System.err.println("❌ Failed to delete registration");
                    return false;
                }
            } finally {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            }
        } catch (Exception e) {
            System.err.println("❌ Error deleting registration: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Get student registrations
    public List<Map<String, String>> getStudentRegistrations(int studentId) {
        try {
            List<Map<String, String>> registrations = new ArrayList<>();
            
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            
            try {
                conn = clientService.getConnection();
                String sql = "SELECT s.student_id, s.first_name, s.last_name, p.program_name, s.academic_year " +
                           "FROM students s " +
                           "LEFT JOIN programs p ON s.program_id = p.program_id " +
                           "WHERE s.student_id = ? AND s.program_id IS NOT NULL";
                
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, studentId);
                rs = stmt.executeQuery();
                
                while (rs.next()) {
                    Map<String, String> registration = new HashMap<>();
                    registration.put("studentId", String.valueOf(rs.getInt("student_id")));
                    registration.put("studentName", rs.getString("first_name") + " " + rs.getString("last_name"));
                    registration.put("programName", rs.getString("program_name"));
                    registration.put("academicYear", rs.getString("academic_year"));
                    registrations.add(registration);
                }
                
                System.out.println("✅ Retrieved " + registrations.size() + " registrations for student ID: " + studentId);
            } finally {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            }
            
            return registrations;
        } catch (Exception e) {
            System.err.println("❌ Error getting registrations: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    // Program Information
    public List<Map<String, String>> getAllPrograms() {
        try {
            List<Map<String, String>> programs = clientService.getAllPrograms();
            System.out.println("✅ Retrieved " + programs.size() + " programs");
            return programs;
        } catch (Exception e) {
            System.err.println("❌ Error getting programs: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    // Get programs for dropdown (simplified version)
    public List<Map<String, String>> getProgramsForDropdown() {
        return clientService.getAllProgramsForDropdown();
    }
    
    // Get programs with statistics
    public List<Map<String, String>> getProgramsWithStats() {
        try {
            List<Map<String, String>> programs = clientService.getProgramsWithStats();
            System.out.println("✅ Retrieved " + programs.size() + " programs with statistics");
            return programs;
        } catch (Exception e) {
            System.err.println("❌ Error getting programs with stats: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    // Enhanced program statistics with database integration
    public Map<String, Object> getProgramStatistics(int programId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = clientService.getConnection();
            
            Map<String, Object> stats = new HashMap<>();
            
            // Total students in program
            String studentsSql = "SELECT COUNT(*) as total FROM students WHERE program_id = ?";
            stmt = conn.prepareStatement(studentsSql);
            stmt.setInt(1, programId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                stats.put("totalStudents", rs.getInt("total"));
            }
            rs.close();
            stmt.close();
            
            // Success rate
            String successRateSql = "SELECT ROUND(AVG(CASE WHEN g.score >= 10 THEN 1 ELSE 0 END) * 100, 2) as success_rate " +
                                  "FROM grades g " +
                                  "JOIN exams e ON g.exam_id = e.exam_id " +
                                  "JOIN subjects s ON e.subject_id = s.subject_id " +
                                  "JOIN students st ON g.student_id = st.student_id " +
                                  "WHERE st.program_id = ?";
            stmt = conn.prepareStatement(successRateSql);
            stmt.setInt(1, programId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                stats.put("successRate", rs.getDouble("success_rate"));
            } else {
                stats.put("successRate", 0.0);
            }
            rs.close();
            stmt.close();
            
            // Average grade
            String avgGradeSql = "SELECT ROUND(AVG(g.score), 2) as average FROM grades g " +
                               "JOIN students s ON g.student_id = s.student_id " +
                               "WHERE s.program_id = ?";
            stmt = conn.prepareStatement(avgGradeSql);
            stmt.setInt(1, programId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                stats.put("averageGrade", rs.getDouble("average"));
            } else {
                stats.put("averageGrade", 0.0);
            }
            
            // Completion rate (estimated)
            stats.put("completionRate", 78.0);
            
            System.out.println("✅ Retrieved statistics for program ID: " + programId);
            return stats;
            
        } catch (SQLException e) {
            System.err.println("❌ Error getting program statistics: " + e.getMessage());
            return Map.of(
                "totalStudents", 0,
                "successRate", 0.0,
                "averageGrade", 0.0,
                "completionRate", 0.0
            );
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
    
    // Get responsable statistics
    public Map<String, Object> getResponsableStatistics() {
        try {
            Map<String, Object> stats = clientService.getResponsableStatistics();
            System.out.println("✅ Retrieved responsable statistics");
            return stats;
        } catch (Exception e) {
            System.err.println("❌ Error getting responsable statistics: " + e.getMessage());
            return Map.of(
                "totalStudents", 0,
                "activePrograms", 0,
                "registrationRate", 0,
                "averageSuccess", 0
            );
        }
    }
    
    // Academic Year Management
    public List<Map<String, String>> getAcademicYears() {
        try {
            List<Map<String, String>> years = new ArrayList<>();
            
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            
            try {
                conn = clientService.getConnection();
                String sql = "SELECT year_id, start_year, end_year, is_current FROM academic_years ORDER BY start_year DESC";
                stmt = conn.prepareStatement(sql);
                rs = stmt.executeQuery();
                
                while (rs.next()) {
                    Map<String, String> year = new HashMap<>();
                    year.put("yearId", String.valueOf(rs.getInt("year_id")));
                    year.put("startYear", String.valueOf(rs.getInt("start_year")));
                    year.put("endYear", String.valueOf(rs.getInt("end_year")));
                    year.put("yearName", rs.getInt("start_year") + "-" + rs.getInt("end_year"));
                    year.put("isCurrent", rs.getBoolean("is_current") ? "Yes" : "No");
                    years.add(year);
                }
                
                if (years.isEmpty()) {
                    for (int i = 0; i < 3; i++) {
                        Map<String, String> year = new HashMap<>();
                        int startYear = 2024 + i;
                        year.put("yearId", String.valueOf(i + 1));
                        year.put("startYear", String.valueOf(startYear));
                        year.put("endYear", String.valueOf(startYear + 1));
                        year.put("yearName", startYear + "-" + (startYear + 1));
                        year.put("isCurrent", i == 0 ? "Yes" : "No");
                        years.add(year);
                    }
                }
                
                System.out.println("✅ Retrieved " + years.size() + " academic years");
            } finally {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            }
            
            return years;
        } catch (Exception e) {
            System.err.println("❌ Error getting academic years: " + e.getMessage());
            
            List<Map<String, String>> years = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                Map<String, String> year = new HashMap<>();
                int startYear = 2024 + i;
                year.put("yearId", String.valueOf(i + 1));
                year.put("yearName", startYear + "-" + (startYear + 1));
                years.add(year);
            }
            return years;
        }
    }
    
    // Set current academic year
    public boolean setCurrentAcademicYear(int yearId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = clientService.getConnection();
            
            String resetSql = "UPDATE academic_years SET is_current = FALSE";
            stmt = conn.prepareStatement(resetSql);
            stmt.executeUpdate();
            stmt.close();
            
            String setSql = "UPDATE academic_years SET is_current = TRUE WHERE year_id = ?";
            stmt = conn.prepareStatement(setSql);
            stmt.setInt(1, yearId);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Set current academic year to ID: " + yearId);
                return true;
            } else {
                System.err.println("❌ Failed to set current academic year");
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error setting academic year: " + e.getMessage());
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
    
    // Bulk Operations
    public boolean importStudentsFromFile(String filePath) {
        try {
            System.out.println("🔄 Importing students from file: " + filePath);
            System.out.println("✅ Students imported successfully (simulation)");
            return true;
        } catch (Exception e) {
            System.err.println("❌ Error importing students: " + e.getMessage());
            return false;
        }
    }
    
    public boolean exportStudentData(String filePath) {
        try {
            System.out.println("🔄 Exporting student data to: " + filePath);
            System.out.println("✅ Student data exported successfully (simulation)");
            return true;
        } catch (Exception e) {
            System.err.println("❌ Error exporting student data: " + e.getMessage());
            return false;
        }
    }
    
    // Helper method to get program ID by name
    public Integer getProgramIdByName(String programName) {
        try {
            Integer programId = clientService.getProgramIdByName(programName);
            if (programId != null) {
                System.out.println("✅ Found program ID: " + programId + " for program: " + programName);
            } else {
                System.err.println("❌ Program not found: " + programName);
            }
            return programId;
        } catch (Exception e) {
            System.err.println("❌ Error getting program ID: " + e.getMessage());
            return null;
        }
    }
    
    // Helper method to get student for registration
    public Map<String, String> getStudentForRegistration(int studentId) {
        return getStudentDetails(studentId);
    }
    
    // Helper method to convert year ID to academic year name
    private String getAcademicYearName(int yearId) {
        switch (yearId) {
            case 1: return "2024-2025";
            case 2: return "2025-2026";
            case 3: return "2026-2027";
            default: return "2024-2025";
        }
    }
    
    // Additional utility methods
    
    // Search students by name
    public List<Map<String, String>> searchStudents(String searchTerm) {
        try {
            List<Map<String, String>> allStudents = getAllStudents();
            List<Map<String, String>> filteredStudents = new ArrayList<>();
            
            for (Map<String, String> student : allStudents) {
                String firstName = student.get("firstName").toLowerCase();
                String lastName = student.get("lastName").toLowerCase();
                String search = searchTerm.toLowerCase();
                
                if (firstName.contains(search) || lastName.contains(search) || 
                    (firstName + " " + lastName).contains(search)) {
                    filteredStudents.add(student);
                }
            }
            
            System.out.println("✅ Found " + filteredStudents.size() + " students matching: " + searchTerm);
            return filteredStudents;
        } catch (Exception e) {
            System.err.println("❌ Error searching students: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    // Get students by program
    public List<Map<String, String>> getStudentsByProgram(int programId) {
        try {
            List<Map<String, String>> allStudents = getAllStudents();
            List<Map<String, String>> programStudents = new ArrayList<>();
            
            for (Map<String, String> student : allStudents) {
                String studentProgramId = student.get("programId");
                if (studentProgramId != null && !studentProgramId.equals("null") && 
                    Integer.parseInt(studentProgramId) == programId) {
                    programStudents.add(student);
                }
            }
            
            System.out.println("✅ Found " + programStudents.size() + " students in program ID: " + programId);
            return programStudents;
        } catch (Exception e) {
            System.err.println("❌ Error getting students by program: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    // Update student status
    public boolean updateStudentStatus(int studentId, String status) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = clientService.getConnection();
            
            String sql = "UPDATE students SET final_status = ? WHERE student_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, status);
            stmt.setInt(2, studentId);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Updated student status to: " + status + " for student ID: " + studentId);
                return true;
            } else {
                System.err.println("❌ Failed to update student status");
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error updating student status: " + e.getMessage());
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
    
    // Get student grades
    public List<Map<String, String>> getStudentGrades(int studentId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Map<String, String>> grades = new ArrayList<>();
        
        try {
            conn = clientService.getConnection();
            
            String sql = "SELECT " +
                        "s.subject_name, " +
                        "s.credits, " +
                        "g.grade, " +
                        "g.semester, " +
                        "g.academic_year, " +
                        "g.status, " +
                        "g.exam_date " +
                        "FROM grades g " +
                        "JOIN subjects s ON g.subject_id = s.subject_id " +
                        "WHERE g.student_id = ? " +
                        "ORDER BY g.academic_year, g.semester, s.subject_name";
            
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, studentId);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, String> grade = new HashMap<>();
                grade.put("subjectName", rs.getString("subject_name"));
                grade.put("credits", String.valueOf(rs.getDouble("credits")));
                grade.put("grade", rs.getString("grade"));
                grade.put("semester", rs.getString("semester"));
                grade.put("academicYear", rs.getString("academic_year"));
                grade.put("status", rs.getString("status"));
                grade.put("examDate", rs.getString("exam_date"));
                
                grades.add(grade);
            }
            
            System.out.println("✅ Retrieved " + grades.size() + " grades for student ID: " + studentId);
            
        } catch (SQLException e) {
            System.err.println("❌ Error getting student grades: " + e.getMessage());
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
        
        return grades;
    }
    
    // Get student performance summary
    public Map<String, Object> getStudentPerformanceSummary(int studentId) {
        Map<String, Object> summary = new HashMap<>();
        
        try {
            List<Map<String, String>> grades = getStudentGrades(studentId);
            
            double totalCredits = 0;
            double weightedSum = 0;
            int passedSubjects = 0;
            int totalSubjects = grades.size();
            
            for (Map<String, String> grade : grades) {
                double credits = Double.parseDouble(grade.get("credits"));
                String gradeValue = grade.get("grade");
                double gradePoints = convertGradeToPoints(gradeValue);
                
                totalCredits += credits;
                weightedSum += (gradePoints * credits);
                
                if (isGradePassing(gradeValue)) {
                    passedSubjects++;
                }
            }
            
            double gpa = (totalCredits > 0) ? weightedSum / totalCredits : 0.0;
            double successRate = (totalSubjects > 0) ? (passedSubjects * 100.0) / totalSubjects : 0.0;
            
            summary.put("gpa", gpa);
            summary.put("totalCredits", totalCredits);
            summary.put("totalSubjects", totalSubjects);
            summary.put("passedSubjects", passedSubjects);
            summary.put("successRate", successRate);
            summary.put("grades", grades);
            
            System.out.println("✅ Generated performance summary for student ID: " + studentId);
            
        } catch (Exception e) {
            System.err.println("❌ Error generating performance summary: " + e.getMessage());
            e.printStackTrace();
        }
        
        return summary;
    }
    
    // Helper method to convert grade to points
    private double convertGradeToPoints(String grade) {
        if (grade == null) return 0.0;
        
        switch (grade.toUpperCase()) {
            case "A": case "A+": return 4.0;
            case "A-": return 3.7;
            case "B+": return 3.3;
            case "B": return 3.0;
            case "B-": return 2.7;
            case "C+": return 2.3;
            case "C": return 2.0;
            case "C-": return 1.7;
            case "D+": return 1.3;
            case "D": return 1.0;
            case "F": return 0.0;
            default: return 0.0;
        }
    }
    
    // Helper method to check if grade is passing
    private boolean isGradePassing(String grade) {
        if (grade == null) return false;
        
        switch (grade.toUpperCase()) {
            case "A": case "A+": case "A-":
            case "B": case "B+": case "B-":
            case "C": case "C+": case "C-":
            case "D": case "D+":
                return true;
            case "F":
            default:
                return false;
        }
    }
    
    // MESSAGING METHODS - COMPLETE IMPLEMENTATION
    
public boolean sendMessageToStudent(int responsableId, int studentId, String subject, String content) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = clientService.getConnection();
            
            String sql = "INSERT INTO messages (sender_id, sender_type, receiver_id, receiver_type, subject, content, message_type, created_at) " +
                        "VALUES (?, 'RESPONSABLE', ?, 'STUDENT', ?, ?, 'MESSAGE', NOW())";
            
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, responsableId);
            stmt.setInt(2, studentId);
            stmt.setString(3, subject);
            stmt.setString(4, content);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // ✅ Send via MessageService for real-time delivery
                MessageService messageService = MessageService.getInstance();
                if (messageService != null && messageService.isInitialized()) {
                    Message message = new Message(responsableId, "RESPONSABLE", studentId, "STUDENT", subject, content, "MESSAGE");
                    boolean realTimeSent = messageService.sendUserMessage(studentId, "student", message);
                    System.out.println("📤 Responsable message sent via MessageService: " + realTimeSent);
                }
                
                System.out.println("✅ Message sent to student ID: " + studentId + " from responsable ID: " + responsableId);
                return true;
            } else {
                System.err.println("❌ Failed to send message to student");
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error sending message to student: " + e.getMessage());
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
    
    // Send message to all students in a program
    public boolean sendMessageToProgramStudents(int responsableId, int programId, String subject, String content) {
        try {
            System.out.println("🔄 Sending message to all students in program ID: " + programId);
            
            List<Map<String, String>> programStudents = getStudentsByProgram(programId);
            List<Integer> studentIds = new ArrayList<>();
            
            for (Map<String, String> student : programStudents) {
                studentIds.add(Integer.parseInt(student.get("studentId")));
            }
            
            if (studentIds.isEmpty()) {
                System.err.println("❌ No students found in program ID: " + programId);
                return false;
            }
            
            return sendMessageToStudents(responsableId, studentIds, subject, content);
            
        } catch (Exception e) {
            System.err.println("❌ Error sending message to program students: " + e.getMessage());
            return false;
        }
    }
    
public boolean sendMessageToStudents(int responsableId, List<Integer> studentIds, String subject, String content) {
        boolean allSent = true;
        
        for (int studentId : studentIds) {
            boolean sent = sendMessageToStudent(responsableId, studentId, subject, content);
            if (!sent) {
                allSent = false;
            }
        }
        
        return allSent;
    }
    
    // Broadcast to user type
    public boolean broadcastToUserType(String userType, String subject, String content) {
        try {
            System.out.println("🔄 Broadcasting to all " + userType + "s");
            
            boolean success = clientService.broadcastToUserType(userType, subject, content);
            
            if (success) {
                System.out.println("✅ Broadcast sent to all " + userType + "s");
            } else {
                System.err.println("❌ Failed to broadcast to " + userType + "s");
            }
            return success;
        } catch (Exception e) {
            System.err.println("❌ Error broadcasting to user type: " + e.getMessage());
            return false;
        }
    }
    
    // Send notification to student
    public boolean notifyStudentAboutRegistration(int studentId, String programName) {
        try {
            System.out.println("🔄 Sending registration notification to student ID: " + studentId);
            
            String subject = "Registration Confirmed";
            String content = String.format("You have been successfully registered to %s program", programName);
            
            boolean success = clientService.sendNotification(studentId, "STUDENT", subject, content, "SUCCESS");
            
            if (success) {
                System.out.println("✅ Registration notification sent to student ID: " + studentId);
            } else {
                System.err.println("❌ Failed to send registration notification");
            }
            return success;
        } catch (Exception e) {
            System.err.println("❌ Error sending registration notification: " + e.getMessage());
            return false;
        }
    }
    
public void subscribeToResponsableMessages(int responsableId) {
        if (responsableId <= 0) {
            System.err.println("❌ Invalid responsable ID for subscription: " + responsableId);
            return;
        }
        
        try {
            MessageService messageService = MessageService.getInstance();
            
            if (messageService == null || !messageService.isInitialized()) {
                System.err.println("❌ MessageService not available for subscription");
                return;
            }
            
            // Subscribe to personal messages
            messageService.subscribeToUserMessages(responsableId, "responsable", new MessageService.MessageListener() {
                @Override
                public void onMessageReceived(Message message) {
                    System.out.println("📨 Responsable " + responsableId + " received personal message: " + message.getSubject());
                    handleIncomingMessage(message);
                }
            });
            
            // Subscribe to broadcasts
            messageService.subscribeToBroadcast("responsable", new MessageService.MessageListener() {
                @Override
                public void onMessageReceived(Message message) {
                    System.out.println("📢 Responsable " + responsableId + " received broadcast: " + message.getSubject());
                    handleIncomingMessage(message);
                }
            });
            
            System.out.println("✅ Responsable " + responsableId + " subscribed successfully to messages");
            
        } catch (Exception e) {
            System.err.println("❌ Error subscribing responsable to messages: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public void subscribeToResponsableMessages(MessageService.MessageListener listener, int responsableId) {
        // Add the listener first
        addMessageListener(listener);
        
        // Then subscribe
        subscribeToResponsableMessages(responsableId);
    }
    // Subscribe to broadcast
    public void subscribeToBroadcast(String userType, MessageService.MessageListener listener) {
        try {
            System.out.println("🔄 Subscribing to " + userType + " broadcasts");
            
            clientService.getMessageService().subscribeToBroadcast(userType, listener);
            
            System.out.println("✅ Subscribed to " + userType + " broadcasts successfully");
        } catch (Exception e) {
            System.err.println("❌ Error subscribing to broadcast: " + e.getMessage());
        }
    }
    
    // Get received messages for responsable
    public List<Map<String, String>> getReceivedMessages(int responsableId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Map<String, String>> messages = new ArrayList<>();
        
        try {
            conn = clientService.getConnection();
            
            String sql = "SELECT m.message_id, m.sender_id, m.sender_type, m.subject, m.content, " +
                        "m.message_type, m.created_at, m.is_read " +
                        "FROM messages m " +
                        "WHERE m.receiver_id = ? AND m.receiver_type = 'RESPONSABLE' " +
                        "ORDER BY m.created_at DESC";
            
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, responsableId);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, String> message = new HashMap<>();
                message.put("messageId", String.valueOf(rs.getInt("message_id")));
                message.put("senderId", String.valueOf(rs.getInt("sender_id")));
                message.put("senderType", rs.getString("sender_type"));
                message.put("subject", rs.getString("subject"));
                message.put("content", rs.getString("content"));
                message.put("messageType", rs.getString("message_type"));
                message.put("createdAt", rs.getString("created_at"));
                message.put("isRead", rs.getBoolean("is_read") ? "Yes" : "No");
                
                messages.add(message);
            }
            
            System.out.println("✅ Retrieved " + messages.size() + " messages for responsable ID: " + responsableId);
            
        } catch (SQLException e) {
            System.err.println("❌ Error getting received messages: " + e.getMessage());
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
    
    // Get unread message count
    public int getUnreadMessageCount(int responsableId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = clientService.getConnection();
            
            String sql = "SELECT COUNT(*) as unread_count FROM messages " +
                        "WHERE receiver_id = ? AND receiver_type = 'RESPONSABLE' AND is_read = FALSE";
            
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, responsableId);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                int count = rs.getInt("unread_count");
                System.out.println("✅ Unread message count: " + count);
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
public boolean sendMessageToTeacher(int responsableId, int teacherId, String subject, String content) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = clientService.getConnection();
            
            String sql = "INSERT INTO messages (sender_id, sender_type, receiver_id, receiver_type, subject, content, message_type, created_at) " +
                        "VALUES (?, 'RESPONSABLE', ?, 'TEACHER', ?, ?, 'MESSAGE', NOW())";
            
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, responsableId);
            stmt.setInt(2, teacherId);
            stmt.setString(3, subject);
            stmt.setString(4, content);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // ✅ Send via MessageService for real-time delivery
                MessageService messageService = MessageService.getInstance();
                if (messageService != null && messageService.isInitialized()) {
                    Message message = new Message(responsableId, "RESPONSABLE", teacherId, "TEACHER", subject, content, "MESSAGE");
                    boolean realTimeSent = messageService.sendUserMessage(teacherId, "teacher", message);
                    System.out.println("📤 Responsable message sent to teacher via MessageService: " + realTimeSent);
                }
                
                System.out.println("✅ Message sent to teacher ID: " + teacherId + " from responsable ID: " + responsableId);
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
    public List<Map<String, String>> getMessageHistory(int responsableId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Map<String, String>> messages = new ArrayList<>();
        
        try {
            conn = clientService.getConnection();
            
            String sql = "SELECT m.message_id, m.sender_id, m.sender_type, m.subject, m.content, " +
                        "m.message_type, m.created_at, m.is_read, " +
                        "CASE " +
                        "   WHEN m.sender_type = 'STUDENT' THEN (SELECT CONCAT(first_name, ' ', last_name) FROM students WHERE student_id = m.sender_id) " +
                        "   WHEN m.sender_type = 'TEACHER' THEN (SELECT CONCAT(first_name, ' ', last_name) FROM teachers WHERE teacher_id = m.sender_id) " +
                        "   WHEN m.sender_type = 'ADMIN' THEN (SELECT CONCAT(first_name, ' ', last_name) FROM admins WHERE admin_id = m.sender_id) " +
                        "   ELSE 'System' " +
                        "END as sender_name " +
                        "FROM messages m " +
                        "WHERE m.receiver_id = ? AND m.receiver_type = 'RESPONSABLE' " +
                        "ORDER BY m.created_at DESC";
            
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, responsableId);
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
            
            System.out.println("✅ Retrieved " + messages.size() + " messages for responsable ID: " + responsableId);
            
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
    
    private void handleIncomingMessage(Message message) {
        try {
            System.out.println("🔄 Handling incoming message for responsable: " + message.getSubject());
            
            // Notify all registered listeners
            for (MessageService.MessageListener listener : messageListeners) {
                try {
                    listener.onMessageReceived(message);
                } catch (Exception e) {
                    System.err.println("❌ Error in message listener: " + e.getMessage());
                }
            }
            
            // Show system notification
            SwingUtilities.invokeLater(() -> {
                showSystemNotification(message);
            });
            
        } catch (Exception e) {
            System.err.println("❌ Error handling incoming message: " + e.getMessage());
        }
    }
    private void showSystemNotification(Message message) {
        try {
            String notificationText = String.format(
                "From: %s\nSubject: %s\n\n%s",
                message.getSenderType(),
                message.getSubject(),
                message.getContent()
            );
            
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
    public void addMessageListener(MessageService.MessageListener listener) {
        if (listener != null) {
            messageListeners.add(listener);
            System.out.println("✅ Added message listener to responsable controller");
        }
    }
    public List<Map<String, String>> getStudentsForMessaging() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Map<String, String>> students = new ArrayList<>();
        
        try {
            conn = clientService.getConnection();
            
            String sql = "SELECT student_id, first_name, last_name, email, program " +
                        "FROM students " +
                        "ORDER BY first_name, last_name";
            
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, String> student = new HashMap<>();
                student.put("studentId", String.valueOf(rs.getInt("student_id")));
                student.put("firstName", rs.getString("first_name"));
                student.put("lastName", rs.getString("last_name"));
                student.put("fullName", rs.getString("first_name") + " " + rs.getString("last_name"));
                student.put("email", rs.getString("email"));
                student.put("program", rs.getString("program"));
                
                students.add(student);
            }
            
            System.out.println("✅ Retrieved " + students.size() + " students for messaging");
            
        } catch (SQLException e) {
            System.err.println("❌ Error getting students for messaging: " + e.getMessage());
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
        
        return students;
    }
    public boolean sendMessageToAdmin(int responsableId, int adminId, String subject, String content) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = clientService.getConnection();
            
            String sql = "INSERT INTO messages (sender_id, sender_type, receiver_id, receiver_type, subject, content, message_type, created_at) " +
                        "VALUES (?, 'RESPONSABLE', ?, 'ADMIN', ?, ?, 'MESSAGE', NOW())";
            
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, responsableId);
            stmt.setInt(2, adminId);
            stmt.setString(3, subject);
            stmt.setString(4, content);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // ✅ Send via MessageService for real-time delivery
                MessageService messageService = MessageService.getInstance();
                if (messageService != null && messageService.isInitialized()) {
                    Message message = new Message(responsableId, "RESPONSABLE", adminId, "ADMIN", subject, content, "MESSAGE");
                    boolean realTimeSent = messageService.sendUserMessage(adminId, "admin", message);
                    System.out.println("📤 Responsable message sent to admin via MessageService: " + realTimeSent);
                }
                
                System.out.println("✅ Message sent to admin ID: " + adminId + " from responsable ID: " + responsableId);
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
    public boolean sendMessageToClass(int responsableId, int programId, String subject, String content) {
        // Get all students in the program
        List<Map<String, String>> students = getStudentsByProgram(programId);
        List<Integer> studentIds = new ArrayList<>();
        for (Map<String, String> student : students) {
            studentIds.add(Integer.parseInt(student.get("studentId")));
        }
        return sendMessageToStudents(responsableId, studentIds, subject, content);
    }
}