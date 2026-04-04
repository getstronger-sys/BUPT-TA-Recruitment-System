<%@ page contentType="text/html;charset=UTF-8" language="java" %>
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
    <title>Applicant Detail - MO</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="container">
    <div class="nav top-nav">
        <span class="brand">QM TA Portal</span>
        <span class="user"><%= session.getAttribute("realName") %> | <a href="${pageContext.request.contextPath}/logout">Logout</a></span>
    </div>
    <div class="page-layout">
        <div class="left-nav-wrap">
            <div class="icon-rail">
                <div class="icon-dot">J</div>
                <div class="icon-dot">P</div>
                <div class="icon-dot active">D</div>
            </div>
            <aside class="side-nav">
                <a href="${pageContext.request.contextPath}/mo/jobs">My Jobs</a>
                <a href="${pageContext.request.contextPath}/mo/post-job">Post Job</a>
                <a class="active" href="${pageContext.request.contextPath}/mo/applicant-detail?applicantId=<%= user != null ? user.getId() : "" %>">Applicant Detail</a>
            </aside>
        </div>
        <main class="main-panel">
    <h1><%= hidePersonal ? "Withdrawn application record" : "Applicant Detail" %></h1>
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
    <% } %>
    <% if (!hidePersonal) { %>
    <div class="detail-grid">
        <div class="detail-card">
            <h3>Basic Information</h3>
            <p><strong>Name:</strong> <%= user != null && user.getRealName() != null ? user.getRealName() : "-" %></p>
            <p><strong>Email:</strong> <%= user != null && user.getEmail() != null ? user.getEmail() : "-" %></p>
            <p><strong>Student ID:</strong> <%= profile != null && profile.getStudentId() != null && !profile.getStudentId().isEmpty() ? profile.getStudentId() : "-" %></p>
            <p><strong>Phone:</strong> <%= profile != null && profile.getPhone() != null && !profile.getPhone().isEmpty() ? profile.getPhone() : "-" %></p>
            <p><strong>Availability:</strong> <%= profile != null && profile.getAvailability() != null && !profile.getAvailability().isEmpty() ? profile.getAvailability() : "-" %></p>
            <p><strong>CV:</strong>
                <% if (profile != null && profile.getCvFilePath() != null && !profile.getCvFilePath().isEmpty()) { %>
                <a href="${pageContext.request.contextPath}/view-cv?userId=<%= user != null ? user.getId() : "" %>" target="_blank">View CV</a>
                <% } else { %>
                Not uploaded
                <% } %>
            </p>
        </div>
        <div class="detail-card">
            <h3>Application Summary</h3>
            <p><strong>Total (for your jobs):</strong> <%= appRows.size() %></p>
            <p><strong>Selected:</strong> <span class="status-selected"><%= selected %></span></p>
            <p><strong>Pending:</strong> <span class="status-pending"><%= pending %></span></p>
            <p><strong>Interview:</strong> <span class="status-pending"><%= interview %></span></p>
            <p><strong>Other:</strong> <span class="status-rejected"><%= other %></span></p>
            <p><strong>Skills:</strong>
                <%= profile != null && profile.getSkills() != null && !profile.getSkills().isEmpty() ? String.join(", ", profile.getSkills()) : "-" %>
            </p>
        </div>
    </div>

    <div class="detail-card">
        <h3>Self Introduction</h3>
        <p><%= profile != null && profile.getIntroduction() != null && !profile.getIntroduction().isEmpty() ? profile.getIntroduction() : "No introduction provided." %></p>
    </div>
    <% } %>

    <h2>Applications to Your Jobs</h2>
    <table>
        <tr>
            <th>Job Title</th>
            <th>Module</th>
            <th>Applied At</th>
            <th>Status</th>
            <th>Notes</th>
        </tr>
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
    </table>
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
