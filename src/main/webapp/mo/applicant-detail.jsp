<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jspf/html-esc.jspf" %>
<%@ page import="java.util.List" %>
<%@ page import="bupt.ta.model.User" %>
<%@ page import="bupt.ta.model.TAProfile" %>
<%@ page import="bupt.ta.model.Application" %>
<%@ page import="bupt.ta.model.Job" %>
<%
    User user = (User) request.getAttribute("applicantUser");
    TAProfile profile = (TAProfile) request.getAttribute("applicantProfile");
    List<Object[]> appRows = (List<Object[]>) request.getAttribute("appRows");
    if (appRows == null) appRows = java.util.Collections.emptyList();
    Integer selectedObj = (Integer) request.getAttribute("selectedCount");
    Integer pendingObj = (Integer) request.getAttribute("pendingCount");
    Integer otherObj = (Integer) request.getAttribute("otherCount");
    Integer interviewObj = (Integer) request.getAttribute("interviewCount");
    String llmApplicantInsight = (String) request.getAttribute("llmApplicantInsight");
    int selected = selectedObj != null ? selectedObj : 0;
    int pending = pendingObj != null ? pendingObj : 0;
    int interview = interviewObj != null ? interviewObj : 0;
    int other = otherObj != null ? otherObj : 0;
    Boolean hidePi = (Boolean) request.getAttribute("hideApplicantPersonalInfo");
    boolean hidePersonal = hidePi != null && hidePi.booleanValue();
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <%@ include file="/WEB-INF/jspf/viewport.jspf" %>
    <title>Applicant Detail - MO</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="container">
    <div class="nav top-nav">
        <span class="brand">BUPT Teaching Assistant Recruitment System</span>
        <span class="user"><%= session.getAttribute("realName") %> | <a href="${pageContext.request.contextPath}/logout">Logout</a></span>
    </div>
    <div class="page-layout">
        <div class="left-nav-wrap">
            <div class="icon-rail">
                <div class="icon-dot">J</div>
                <div class="icon-dot">P</div>
                <div class="icon-dot active">D</div>
            </div>
            <%@ include file="/WEB-INF/jspf/mo-side-nav.jspf" %>
        </div>
        <main class="main-panel mo-main">
    <div class="mo-applicant-head">
    <h1><%= hidePersonal ? "Withdrawn application record" : "Applicant Detail" %></h1>
    <% if (hidePersonal) { %>
    <p class="mo-manage-hero-lead">Personal data is hidden for withdrawn applicants.</p>
    <% } else { %>
    <p class="mo-manage-hero-lead">Review profile, academic background, and applications to your postings.</p>
    <% } %>
    </div>
    <% if (hidePersonal) { %>
    <div class="context-card">
        <strong>Privacy</strong>
        <p>This applicant withdrew all applications to your jobs. Name, contact details, profile, and CV are not shown. Only status and job reference are listed below.</p>
    </div>
    <% } else { %>
    <div class="context-card">
        <strong>Review Tip</strong>
        <p>Check skills fit, availability and previous application status together for a balanced selection.</p>
    </div>
    <% if (llmApplicantInsight != null && !llmApplicantInsight.trim().isEmpty()) { %>
    <div class="llm-insight-card context-card">
        <strong>AI applicant insight (DeepSeek)</strong>
        <p class="pre-wrap llm-insight-body"><%= escHtml(llmApplicantInsight) %></p>
        <p class="muted-inline llm-insight-disclaimer">Narrative only; verify against profile and interview.</p>
    </div>
    <% } %>
    <% } %>
    <% if (!hidePersonal) { %>
    <div class="detail-grid">
        <div class="detail-card">
            <h3>Basic Information</h3>
            <p><strong>Name:</strong> <%= user != null && user.getRealName() != null && !user.getRealName().isEmpty() ? escHtml(user.getRealName()) : "-" %></p>
            <p><strong>Email:</strong> <%= (profile != null && profile.getEmail() != null && !profile.getEmail().isEmpty()) ? escHtml(profile.getEmail()) : (user != null && user.getEmail() != null && !user.getEmail().isEmpty() ? escHtml(user.getEmail()) : "-") %></p>
            <p><strong>Student ID:</strong> <%= profile != null && profile.getStudentId() != null && !profile.getStudentId().isEmpty() ? escHtml(profile.getStudentId()) : "-" %></p>
            <p><strong>Phone:</strong> <%= profile != null && profile.getPhone() != null && !profile.getPhone().isEmpty() ? escHtml(profile.getPhone()) : "-" %></p>
            <p><strong>Availability:</strong> <%= profile != null && profile.getAvailability() != null && !profile.getAvailability().isEmpty() ? escHtml(profile.getAvailability()) : "-" %></p>
        </div>
        <div class="detail-card">
            <h3>Application Summary</h3>
            <p><strong>Total (for your jobs):</strong> <%= appRows.size() %></p>
            <p><strong>Selected:</strong> <span class="status-selected"><%= selected %></span></p>
            <p><strong>Pending:</strong> <span class="status-pending"><%= pending %></span></p>
            <p><strong>Interview:</strong> <span class="status-pending"><%= interview %></span></p>
            <p><strong>Other:</strong> <span class="status-rejected"><%= other %></span></p>
        </div>
    </div>

    <div class="detail-card applicant-academic-card">
        <h3>Academic &amp; experience</h3>
        <p><strong>Degree:</strong> <%= profile != null && profile.getDegree() != null && !profile.getDegree().isEmpty() ? escHtml(profile.getDegree()) : "-" %></p>
        <p><strong>Programme:</strong> <%= profile != null && profile.getProgramme() != null && !profile.getProgramme().isEmpty() ? escHtml(profile.getProgramme()) : "-" %></p>
        <p><strong>Year of study:</strong> <%= profile != null && profile.getYearOfStudy() != null && !profile.getYearOfStudy().isEmpty() ? escHtml(profile.getYearOfStudy()) : "-" %></p>
        <p><strong>Skills:</strong> <%= profile != null && profile.getSkills() != null && !profile.getSkills().isEmpty() ? escHtml(String.join(", ", profile.getSkills())) : "-" %></p>
        <div class="detail-block-text">
            <strong>TA experience</strong>
            <p class="pre-wrap"><%= profile != null && profile.getTaExperience() != null && !profile.getTaExperience().isEmpty() ? escHtml(profile.getTaExperience()) : "Not provided." %></p>
        </div>
        <p><strong>CV:</strong>
            <% if (profile != null && profile.getCvFilePath() != null && !profile.getCvFilePath().isEmpty() && user != null) { %>
            <a href="${pageContext.request.contextPath}/view-cv?userId=<%= user.getId() %>" target="_blank" rel="noopener">View</a>
            <span class="muted-inline"> | </span>
            <a href="${pageContext.request.contextPath}/view-cv?userId=<%= user.getId() %>&amp;download=1">Download</a>
            <% } else { %>
            Not uploaded
            <% } %>
        </p>
    </div>

    <div class="detail-card">
        <h3>Self introduction</h3>
        <p class="pre-wrap"><%= profile != null && profile.getIntroduction() != null && !profile.getIntroduction().isEmpty() ? escHtml(profile.getIntroduction()) : "No introduction provided." %></p>
    </div>
    <% } %>

    <h2>Applications to Your Jobs</h2>
    <div class="mo-detail-table-wrap">
    <table>
        <thead>
        <tr>
            <th>Job Title</th>
            <th>Module</th>
            <th>Applied At</th>
            <th>Status</th>
            <th>Notes</th>
        </tr>
        </thead>
        <tbody>
        <% for (Object[] row : appRows) {
            Application a = (Application) row[0];
            Job j = (Job) row[1];
            String statusClass = "status-pending";
            if ("SELECTED".equals(a.getStatus())) statusClass = "status-selected";
            else if ("INTERVIEW".equals(a.getStatus())) statusClass = "status-pending";
            else if ("REJECTED".equals(a.getStatus()) || "WITHDRAWN".equals(a.getStatus())) statusClass = "status-rejected";
        %>
        <tr>
            <td><%= j != null ? j.getTitle() : a.getJobId() %></td>
            <td><%= j != null ? j.getModuleCode() : "-" %></td>
            <td><%= a.getAppliedAt() != null ? a.getAppliedAt() : "-" %></td>
            <td class="<%= statusClass %>"><%= a.getStatus() %></td>
            <td><%= a.getNotes() != null && !a.getNotes().isEmpty() ? a.getNotes() : "-" %></td>
        </tr>
        <% } %>
        </tbody>
    </table>
    </div>
        </main>
        <aside class="right-sidebar">
            <% if (!hidePersonal) { %>
            <div class="widget-card">
                <div class="widget-title">Applicant Snapshot</div>
                <p class="widget-line">Selected: <%= selected %></p>
                <p class="widget-line">Pending: <%= pending %> | Interview: <%= interview %></p>
                <p class="widget-line">Other: <%= other %></p>
            </div>
            <% } %>
        </aside>
    </div>
</div>
</body>
</html>
