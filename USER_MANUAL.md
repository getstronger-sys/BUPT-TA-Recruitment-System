# User Manual - BUPT TA Recruitment System

## Setup

### Method 1: Build & Deploy to Tomcat

1. **Build** the project: run `mvn clean package` (or `.\mvnw.cmd clean package` if using Maven Wrapper).
2. **Deploy** the generated `target/ta-recruitment.war` to your Apache Tomcat `webapps/` directory.
3. **Start** the Tomcat server.
4. **Access** the application at: `http://localhost:8080/ta-recruitment/`

### Method 2: Run with Embedded Tomcat (Recommended)

1. Open the project directory and ensure `JAVA_HOME` is set to your JDK path.
2. Run: `.\mvnw.cmd cargo:run` (Windows) or `./mvnw cargo:run` (Unix).
3. After startup, access: `http://localhost:8080/ta-recruitment/`

---

## Login Page

- **Screenshot**: [Login screen - index.jsp]
- Enter username and password. Use default accounts: ta1/ta123 (TA), mo1/mo123 (MO), admin/admin123 (Admin).
- Click "Register" to create a new TA or MO account.

## TA - My Profile

- **Screenshot**: [TA Profile screen - ta/profile.jsp]
- Fill student ID, phone, degree, programme, year of study, previous experience, skills (comma-separated), availability, and introduction.
- Upload CV (PDF, DOC, DOCX, TXT, max 5MB).
- Click "Save Profile" and "Upload CV".

## TA - Find Jobs

- **Screenshot**: [Job search screen - ta/jobs.jsp]
- Search by keyword, module code, or required skill.
- Click "Apply" on a job to submit application.

## TA - My Applications

- **Screenshot**: [Applications list - ta/applications.jsp]
- View all applications with status: PENDING, SELECTED, REJECTED, WITHDRAWN.
- **Withdraw** a pending application if needed.

## MO - Post Job

- **Screenshot**: [Post job form - mo/post-job.jsp]
- Enter job title, module code, module name, **job type** (Module TA / Invigilation / Other), description, required skills.
- Set max applicants (0 = unlimited). Click "Post Job".

## MO - My Jobs

- **Screenshot**: [MO jobs with applicants - mo/jobs.jsp]
- View posted jobs and applicants. **Close** or **Reopen** a job.
- For each PENDING applicant: click "Select" or "Reject". Optionally add notes.

## Admin - TA Workload

- **Screenshot**: [Workload table - admin/workload.jsp]
- View number of selected jobs per TA.
- Sorted by workload (highest first).
- **Export** workload data to CSV for reporting.

---

## Deployment Flowchart

![Deployment Flowchart](images/Deployment%20flowchart.png)

The diagram illustrates two deployment paths: the traditional build-and-deploy workflow, and the optional Maven Wrapper embedded-server approach.
