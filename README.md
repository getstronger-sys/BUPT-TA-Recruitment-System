# BUPT International School - Teaching Assistant Recruitment System

EBU6304 Software Engineering Group Project - A lightweight Java Servlet/JSP web application for TA recruitment.

## Team Members

| Student ID | Name          |
| ---------- | ------------- |
| 231223771  | Weiyi Li      |
| 231223520  | Qingwei Zhang |
| 231223324  | Jialin Ma     |
| 231223531  | Tongxin Liu   |
| 231223553  | Yue Hu        |
| 231223298  | Erwei Hou     |

<!-- Other members: add a row in your own pull request. -->

## Requirements Met

- **Technology**: Java Servlet + JSP (no Spring Boot, no database)
- **Data Storage**: JSON files (users.json, profiles.json, jobs.json, applications.json)
- **AI Features** (rule-based, explainable):
  - **Skill matching**: Job–applicant match score (0–100%), matched/missing skills listed
  - **Missing skills**: Identifies skills an applicant lacks for each job
  - **Workload balancing**: Recommends TAs with lower workload; Admin view flags overloaded TAs
- **Core Features**:
  - **TA**: Create profile, upload CV, find jobs, apply for jobs, check application status
  - **MO**: Post jobs, select applicants for jobs
  - **Admin**: Check TA's overall workload

## Default Accounts

| Role  | Username | Password |
| ----- | -------- | -------- |
| TA    | ta1      | ta123    |
| TA    | ta2      | ta123    |
| MO    | mo1      | mo123    |
| Admin | admin    | admin123 |

## Build & Run

### Prerequisites

- Java 11+
- Maven 3.6+
- Apache Tomcat 9+ (or use Maven Tomcat plugin)

### Build

```bash
cd ta-recruitment-system
mvn clean package
```

This produces `target/ta-recruitment.war`.

### Deploy to Tomcat

1. Copy `target/ta-recruitment.war` to Tomcat's `webapps/` directory.
2. Start Tomcat.
3. Open: `http://localhost:8080/ta-recruitment/`

### Run with Cargo (Embedded Tomcat)

`cargo:run` 需要先有打好的 WAR，请**在同一命令里先执行 `package`**（否则会报找不到 `target/ta-recruitment.war`）：

```bash
mvn package cargo:run
# Or with Maven wrapper (no Maven install needed):
# Windows: mvnw.cmd package cargo:run
# Unix: ./mvnw package cargo:run
```

Then open: **http://localhost:8080/ta-recruitment/**

## Project Structure

```
ta-recruitment-system/
├── pom.xml
├── README.md
├── src/main/
│   ├── java/bupt/ta/
│   │   ├── model/         # User, TAProfile, Job, Application
│   │   ├── storage/      # DataStorage (JSON file I/O)
│   │   ├── servlet/      # Login, Register, TA, MO, Admin servlets
│   │   └── filter/       # AuthFilter (role-based access)
│   └── webapp/
│       ├── WEB-INF/web.xml
│       ├── css/style.css
│       ├── index.jsp, register.jsp, dashboard.jsp
│       ├── ta/            # TA pages (profile, jobs, applications)
│       ├── mo/            # MO pages (post-job, jobs)
│       └── admin/         # Admin pages (workload)
└── data/                  # Created at runtime (JSON files, uploads)
```

## Data Files

For local development, data is stored in the project root `data/` directory so accounts, jobs, applications, and uploads survive restarts.
If needed, you can override the storage location with `-Dta.data.dir=<path>` or environment variable `TA_DATA_DIR`.

- `users.json` - User accounts
- `profiles.json` - TA profiles (skills, CV path, etc.)
- `jobs.json` - Job postings
- `applications.json` - Job applications
- `uploads/` - Uploaded CV files

## User Manual

### TA Workflow

1. Login as TA (e.g. ta1/ta123)
2. **My Profile**: Create profile, add skills, upload CV
3. **Find Jobs**: Search and apply for open jobs
4. **My Applications**: View application status (PENDING/SELECTED/REJECTED)

### MO Workflow

1. Login as MO (e.g. mo1/mo123)
2. **Post Job**: Create new job with module code, skills, description
3. **My Jobs**: View posted jobs and applicants
4. Select or Reject applicants

### Admin Workflow

1. Login as Admin (admin/admin123)
2. **TA Workload**: View number of selected jobs per TA

## License

Educational project - BUPT International School.
