# Student Management System

A comprehensive Java-based Student Management System featuring role-based access control, real-time messaging, and advanced academic management.

---

## 🚀 Features

**Role-Based Access Control**
- **Admin:** Full system management, user management, program management
- **Teacher:** Exam creation, grade management, student performance tracking
- **Student:** View grades, transcripts, personal information
- **Responsable:** Student registration, program management, academic oversight

**Core Modules**
- User Authentication & Authorization
- Student Registration & Management
- Academic Program Management
- Exam & Grade Management
- Real-time Messaging System
- Transcript Generation
- Statistics & Reporting

---

## 🛠️ Technology Stack

- **Backend:** Java, JDBC, MySQL
- **Messaging:** ActiveMQ (JMS)
- **Database:** MySQL
- **Architecture:** MVC Pattern, Client-Server

---

## 📋 Prerequisites

- Java 8 or higher
- MySQL Server 5.7+
- ActiveMQ 5.16+
- MySQL Connector/J

---

## 🗄️ Database Setup

Create MySQL database:
```sql
CREATE DATABASE gestion_scolarite;
```
Import the database schema from `database/schema.sql`.

---

## 📂 Project Structure

```
StudentManagement/
├── client/
│   ├── controllers/
│   │   ├── AdminController.java
│   │   ├── TeacherController.java
│   │   ├── StudentController.java
│   │   └── ResponsableController.java
│   ├── views/
│   │   ├── AdminView.java
│   │   ├── TeacherView.java
│   │   ├── StudentView.java
│   │   ├── ResponsableView.java
│   │   └── LoginView.java
│   ├── models/
│   │   ├── Message.java
│   │   ├── Notification.java
│   │   ├── User.java
│   │   ├── Student.java
│   │   ├── Teacher.java
│   │   ├── Admin.java
│   │   └── Responsable.java
│   ├── services/
│   │   ├── ClientService.java
│   │   ├── MessageService.java
│   │   └── ServerMessageService.java
│   ├── lib/
│   │   ├── activemq-client.jar
│   │   ├── mysql-connector-java.jar
│   │   
│   ├── database/
│   │   ├── schema.sql
│   │   └── sample_data.sql
│   ├── Main.java
│   └── Config.java
├── server/
│   ├── services/
│   │   └── ServerMessageService.java
│   ├── models/
│   │   ├── Message.java
│   │   └── Notification.java
│   └── ServerMain.java
├── docs/
│   ├── README.md
│   ├── SETUP.md
│   └── API.md
└── scripts/
    ├── start-client.bat
    ├── start-server.bat
    ├── compile.bat
    └── database-setup.sql
```

---

Update database credentials in `ClientService.java`:

```java
String url = "jdbc:mysql://localhost:3306/gestion_scolarite";
String user = "your_username";
String pass = "your_password";
```

---

## 🔧 Installation

1. **Clone the repository**

   ```bash
   git clone https://github.com/yourusername/student-management-system.git
   cd student-management-system
   ```

2. **Setup ActiveMQ**

   - Download and install ActiveMQ
   - Start ActiveMQ server:
     ```bash
     # On Windows
     activemq start

     # On Linux/Mac
     ./activemq start
     ```

3. **Configure Database**

   - Update connection settings in `ClientService.java`
   - Run the database initialization script

4. **Add Dependencies**
   - MySQL Connector/J
   - ActiveMQ Client JARs

---

## 🚀 Running the Application

1. **Start the Server**

2. **Compile and Run (Windows example):**
   ```bat
   // Clean
   del *.class /s

   # Compile Message class first
   javac -cp ".;../lib/*" models/Message.java

   # Then compile MessageService
   javac -cp ".;../lib/*;models" MessageService.java

   # Then compile everything else
   javac -cp ".;../lib/*;models" ClientService.java views/*.java controllers/*.java models/*.java Main.java

   java -cp ".;../lib/*;services;views;controllers;models" Main
   ```

---

## 👥 Default Login Credentials

| Role        | Username | Password | Description             |
|-------------|----------|----------|-------------------------|
| Admin       | admin    | password | Full system access      |
| Teacher     | prof1    | password | Teaching staff          |
| Student     | ahmed    | password | Student access          |
| Responsable | resp     | password | Academic management     |


---

## 🔄 Messaging System

Real-time communication using ActiveMQ:
- **Direct Messaging:** User-to-user communication
- **Broadcasts:** System-wide announcements
- **Notifications:** Grade updates, exam alerts
- **Teacher-Student Communication:** Class announcements

**Message Types**
- `ANNOUNCEMENT` - System-wide broadcasts
- `ALERT` - Important notifications
- `NOTIFICATION` - General updates
- `MESSAGE` - Direct user communication
- `GRADE` - Grade publication notifications

---

## 📊 Key Features by Role

**Admin**
- User account management
- Program creation and management
- System statistics and reporting
- Teacher assignment to subjects
- System configuration

**Teacher**
- Exam creation and management
- Grade entry and modification
- Student performance tracking
- Class management
- Communication with students

**Student**
- View personal information
- Check grades and transcripts
- View academic progress
- Access course materials
- Receive notifications

**Responsable**
- Student registration
- Program enrollment
- Academic oversight
- Student status management
- Registration statistics

---

## 🎯 Usage Examples

**Adding a New Student**
- Login as Responsable
- Navigate to "Student Registration"
- Fill student details
- Assign to academic program
- Complete registration

**Creating an Exam**
- Login as Teacher
- Go to "Exam Management"
- Create new exam with details
- Set coefficients and types
- Publish exam

**Generating Transcripts**
- Login as Student
- Navigate to "Relevé de Notes"
- Click "Generate Transcript"
- View or print academic record

---

## 🔒 Security Features

- Role-based access control
- Secure password handling
- Session management
- Input validation
- SQL injection prevention

---

## 📈 Reporting & Analytics

- Student performance statistics
- Program success rates
- Teacher effectiveness metrics
- System usage analytics
- Academic progress tracking

---

## 🐛 Troubleshooting

**Common Issues**

- **Database Connection Failed**
  - Check MySQL service is running
  - Verify database credentials
  - Ensure MySQL Connector/J is in classpath

- **ActiveMQ Connection Issues**
  - Confirm ActiveMQ server is running
  - Check broker URL in MessageService
  - Verify ActiveMQ JARs are included

- **Login Failures**
  - Verify user exists in database
  - Check user role assignments
  - Confirm password hashes

- **Missing Dependencies**
  - Ensure all required JARs are in classpath
  - Check Java version compatibility
  - Verify library versions

---

## 🤝 Contributing

- Fork the project
- Create a feature branch
- Commit your changes
- Push to the branch
- Open a Pull Request

---

## 👨‍💻 Development Team

[Fantastic Guys]

---

## 🔄 Version History

- v1.0.0 - Initial release with core functionality
- v1.1.0 - Added messaging system
- v1.2.0 - Enhanced reporting features
