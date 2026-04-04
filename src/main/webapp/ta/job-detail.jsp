<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jspf/html-esc.jspf" %>
<%@ page import="bupt.ta.model.Job" %>
<%@ page import="bupt.ta.ai.AIMatchService" %>
<%
    Job job = (Job) request.getAttribute("job");
    AIMatchService.MatchResult match = (AIMatchService.MatchResult) request.getAttribute("match");
    if (job == null) {
        response.sendRedirect(request.getContextPath() + "/ta/jobs?error=job_not_found");
        return;
    }
    boolean isOpen = "OPEN".equals(job.getStatus());
    String moduleName = job.getModuleName() != null && !job.getModuleName().isEmpty() ? escHtml(job.getModuleName()) : "—";
    String wh = job.getWorkingHours() != null && !job.getWorkingHours().isEmpty() ? escHtml(job.getWorkingHours()) : "—";
    String wl = job.getWorkload() != null && !job.getWorkload().isEmpty() ? escHtml(job.getWorkload()) : "—";
    String pay = job.getPayment() != null && !job.getPayment().isEmpty() ? escHtml(job.getPayment()) : "—";
    String deadline = job.getDeadline() != null && !job.getDeadline().isEmpty() ? escHtml(job.getDeadline()) : "—";
    String respText = job.getResponsibilities() != null && !job.getResponsibilities().isEmpty() ? escHtml(job.getResponsibilities()) : "—";
    String desc = job.getDescription() != null && !job.getDescription().isEmpty() ? escHtml(job.getDescription()) : "—";
    String safeTitle = escHtml(job.getTitle() != null ? job.getTitle() : "");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title><%= safeTitle %> - Job Detail</title>
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
                <div class="icon-dot active">F</div>
                <div class="icon-dot">A</div>
                <div class="icon-dot">P</div>
            </div>
            <aside class="side-nav">
                <a class="active" href="${pageContext.request.contextPath}/ta/jobs">Find Jobs</a>
                <a href="${pageContext.request.contextPath}/ta/applications">My Applications</a>
                <a href="${pageContext.request.contextPath}/ta/profile">My Profile</a>
            </aside>
        </div>
        <main class="main-panel">
            <p class="breadcrumb-line"><a href="${pageContext.request.contextPath}/ta/jobs">&larr; Back to job list</a></p>
            <h1><%= safeTitle %></h1>
            <p class="job-detail-meta">
                <span class="status-pill <%= isOpen ? "status-pill-pending" : "status-pill-rejected" %>"><%= job.getStatus() %></span>
                <% if (job.getJobType() != null && !job.getJobType().isEmpty()) { %>
                <span class="muted-inline">Type: <%= "MODULE_TA".equals(job.getJobType()) ? "Module TA" : "INVIGILATION".equals(job.getJobType()) ? "Invigilation" : "Other" %></span>
                <% } %>
            </p>

            <% if (match != null) { %>
            <p class="ai-hint"><span class="match-badge" title="<%= escHtml(match.explanation) %>">Your match: <%= (int) match.score %>%</span>
                <% if (match.matched != null && !match.matched.isEmpty()) { %> · Matched: <%= escHtml(String.join(", ", match.matched)) %><% } %>
            </p>
            <% } %>

            <dl class="job-detail-dl">
                <dt>Module code</dt><dd><%= escHtml(job.getModuleCode() != null ? job.getModuleCode() : "—") %></dd>
                <dt>Module name</dt><dd><%= moduleName %></dd>
                <dt>Hours / schedule</dt><dd class="pre-wrap"><%= wh %></dd>
                <dt>Payment</dt><dd class="pre-wrap"><%= pay %></dd>
                <dt>Required skills</dt><dd><%= job.getRequiredSkills() != null && !job.getRequiredSkills().isEmpty() ? escHtml(String.join(", ", job.getRequiredSkills())) : "—" %></dd>
                <dt>Responsibilities</dt><dd class="pre-wrap"><%= respText %></dd>
                <dt>Workload</dt><dd class="pre-wrap"><%= wl %></dd>
                <dt>Application deadline</dt><dd><%= deadline %></dd>
                <dt>Open status</dt><dd><%= isOpen ? "Open for applications" : "Closed" %></dd>
            </dl>

            <div class="context-card job-detail-overview">
                <strong>Overview</strong>
                <p class="pre-wrap"><%= desc %></p>
            </div>

            <p><em>Posted by <%= escHtml(job.getPostedByName() != null ? job.getPostedByName() : "MO") %></em></p>

            <% if (isOpen) { %>
            <div class="job-detail-apply">
                <h2>Apply for this position</h2>
                <form action="${pageContext.request.contextPath}/ta/apply" method="post">
                    <input type="hidden" name="jobId" value="<%= job.getId() %>">
                    <button type="submit" class="btn btn-primary btn-lg">Submit application</button>
                </form>
            </div>
            <% } else { %>
            <p class="error">This job is closed; applications are not accepted.</p>
            <% } %>
        </main>
        <aside class="right-sidebar">
            <div class="widget-card">
                <div class="widget-title">Before you apply</div>
                <p class="widget-line">Check deadline and workload fit.</p>
                <p class="widget-line"><a href="${pageContext.request.contextPath}/ta/profile">Update your skills</a> to improve match.</p>
            </div>
        </aside>
    </div>
</div>
</body>
</html>
