<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="bupt.ta.model.Job" %>
<%@ page import="bupt.ta.model.Application" %>
<%@ page import="bupt.ta.ai.AIMatchService" %>
<% List<Object[]> jobsWithApps = (List<Object[]>) request.getAttribute("jobsWithApps"); if (jobsWithApps == null) jobsWithApps = java.util.Collections.emptyList(); %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>My Jobs - MO</title>
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
                <div class="icon-dot active">J</div>
                <div class="icon-dot">P</div>
                <div class="icon-dot">D</div>
            </div>
            <aside class="side-nav">
                <a class="active" href="${pageContext.request.contextPath}/mo/jobs">My Jobs</a>
                <a href="${pageContext.request.contextPath}/mo/post-job">Post Job</a>
            </aside>
        </div>
        <main class="main-panel">
    <h1>My Posted Jobs</h1>
    <div class="context-card">
        <strong>MO Tip</strong>
        <p>Use AI match score as a reference, then check applicant profile and CV before final decision.</p>
    </div>
    <p class="ai-hint"><strong>AI Recommendations</strong>: Applicants sorted by skill match score. Workload balance hint shown for fair distribution.</p>
    <% if ("1".equals(request.getParameter("success"))) { %><p class="success">Job posted successfully!</p><% } %>
    <% if ("1".equals(request.getParameter("updated"))) { %><p class="success">Applicant status updated.</p><% } %>
    <% String err = request.getParameter("error"); if (err != null) { %><p class="error">Error: <%= err %></p><% } %>

    <p><a href="${pageContext.request.contextPath}/mo/post-job" class="btn btn-primary">Post New Job</a></p>

    <% for (Object[] row : jobsWithApps) {
        Job j = (Job) row[0];
        List<AIMatchService.ApplicantRecommendation> recs = (List<AIMatchService.ApplicantRecommendation>) row[1];
        if (recs == null) recs = java.util.Collections.emptyList();
    %>
    <div class="job-card">
        <h3><%= j.getTitle() %> - <%= j.getModuleCode() %> <span style="color:#666;">(<%= j.getStatus() %>)</span>
            <% if ("OPEN".equals(j.getStatus())) { %>
            <form action="${pageContext.request.contextPath}/mo/close-job" method="post" style="display:inline;margin-left:8px;">
                <input type="hidden" name="jobId" value="<%= j.getId() %>">
                <input type="hidden" name="action" value="close">
                <button type="submit" class="btn btn-danger" style="padding:2px 8px;font-size:0.8em;">Close Job</button>
            </form>
            <% } else { %>
            <form action="${pageContext.request.contextPath}/mo/close-job" method="post" style="display:inline;margin-left:8px;">
                <input type="hidden" name="jobId" value="<%= j.getId() %>">
                <input type="hidden" name="action" value="reopen">
                <button type="submit" class="btn btn-primary" style="padding:2px 8px;font-size:0.8em;">Reopen</button>
            </form>
            <% } %>
        </h3>
        <% if (j.getJobType() != null && !j.getJobType().isEmpty()) { %>
        <p class="skills">Type: <%= "MODULE_TA".equals(j.getJobType()) ? "Module TA" : "INVIGILATION".equals(j.getJobType()) ? "Invigilation" : "Other" %></p>
        <% } %>
        <p><%= j.getDescription() != null ? j.getDescription() : "" %></p>
        <% if (j.getRequiredSkills() != null && !j.getRequiredSkills().isEmpty()) { %>
        <p class="skills">Required: <%= String.join(", ", j.getRequiredSkills()) %></p>
        <% } %>
        <h4>Applicants (<%= recs.size() %>) - AI sorted by match & workload balance</h4>
        <table>
            <tr><th>Applicant</th><th>Profile</th><th>CV</th><th>Match</th><th>Missing Skills</th><th>Workload</th><th>Applied</th><th>Status</th><th>Action</th></tr>
            <% for (AIMatchService.ApplicantRecommendation rec : recs) {
                Application a = rec.application;
                String statusClass = "status-pending";
                if ("SELECTED".equals(a.getStatus())) statusClass = "status-selected";
                else if ("REJECTED".equals(a.getStatus()) || "WITHDRAWN".equals(a.getStatus())) statusClass = "status-rejected";
            %>
            <tr>
                <td><%= a.getApplicantName() != null ? a.getApplicantName() : a.getApplicantId() %>
                    <% if (rec.workloadBalanced && "PENDING".equals(a.getStatus())) { %>
                    <span class="balance-badge" title="Lower workload - good for balance">âš?/span>
                    <% } %>
                    <br>
                    <a href="${pageContext.request.contextPath}/mo/applicant-detail?applicantId=<%= a.getApplicantId() %>" class="mini-link">View full profile</a>
                </td>
                <td class="ai-missing-cell">
                    <% if (rec.profile != null) { %>
                    ID: <%= rec.profile.getStudentId() != null && !rec.profile.getStudentId().isEmpty() ? rec.profile.getStudentId() : "-" %><br>
                    Phone: <%= rec.profile.getPhone() != null && !rec.profile.getPhone().isEmpty() ? rec.profile.getPhone() : "-" %><br>
                    Skills: <%= rec.profile.getSkills() != null && !rec.profile.getSkills().isEmpty() ? String.join(", ", rec.profile.getSkills()) : "-" %><br>
                    Intro: <%= rec.profile.getIntroduction() != null && !rec.profile.getIntroduction().isEmpty() ? rec.profile.getIntroduction() : "-" %>
                    <% } else { %>
                    No profile submitted.
                    <% } %>
                </td>
                <td>
                    <% if (rec.profile != null && rec.profile.getCvFilePath() != null && !rec.profile.getCvFilePath().isEmpty()) { %>
                    <a href="${pageContext.request.contextPath}/view-cv?userId=<%= a.getApplicantId() %>" target="_blank">View CV</a>
                    <% } else { %>
                    -
                    <% } %>
                </td>
                <td><span class="match-badge" title="<%= rec.matchResult.explanation %>"><%= (int)rec.matchResult.score %>%</span></td>
                <td class="ai-missing-cell"><%= (rec.matchResult.missing != null && !rec.matchResult.missing.isEmpty()) ? String.join(", ", rec.matchResult.missing) : "-" %></td>
                <td><%= rec.currentWorkload %> jobs</td>
                <td><%= a.getAppliedAt() != null ? a.getAppliedAt() : "-" %></td>
                <td class="<%= statusClass %>"><%= a.getStatus() %></td>
                <td>
                    <% if ("PENDING".equals(a.getStatus())) { %>
                    <form action="${pageContext.request.contextPath}/mo/select-applicant" method="post" style="display:inline;">
                        <input type="hidden" name="applicationId" value="<%= a.getId() %>">
                        <input type="hidden" name="action" value="select">
                        <input type="text" name="notes" placeholder="Notes" style="width:80px;">
                        <button type="submit" class="btn btn-success">Select</button>
                    </form>
                    <form action="${pageContext.request.contextPath}/mo/select-applicant" method="post" style="display:inline;">
                        <input type="hidden" name="applicationId" value="<%= a.getId() %>">
                        <input type="hidden" name="action" value="reject">
                        <button type="submit" class="btn btn-danger">Reject</button>
                    </form>
                    <% } %>
                </td>
            </tr>
            <% } %>
            <% if (recs.isEmpty()) { %>
            <tr><td colspan="9">No applications yet.</td></tr>
            <% } %>
        </table>
    </div>
    <% }
       if (jobsWithApps.isEmpty()) { %>
    <p>No jobs posted yet. <a href="${pageContext.request.contextPath}/mo/post-job">Post your first job</a>.</p>
    <% } %>
        </main>
        <aside class="right-sidebar">
            <div class="widget-card">
                <div class="widget-title">MO Dashboard</div>
                <p class="widget-line">Track applicant quality and workload balance.</p>
                <p class="widget-line">Use profile + CV before selecting.</p>
            </div>
            <div class="widget-card">
                <div class="widget-title">Reminder</div>
                <p class="widget-line">Close jobs after quota is met.</p>
            </div>
        </aside>
    </div>
</div>
</body>
</html>
