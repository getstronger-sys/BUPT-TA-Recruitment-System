<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jspf/html-esc.jspf" %>
<%@ page import="java.util.List" %>
<%@ page import="bupt.ta.model.Job" %>
<%@ page import="bupt.ta.ai.AIMatchService" %>
<% List<Object[]> jobsWithMatch = (List<Object[]>) request.getAttribute("jobsWithMatch"); if (jobsWithMatch == null) jobsWithMatch = java.util.Collections.emptyList();
   request.setAttribute("taNavActive", "jobs"); %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <%@ include file="/WEB-INF/jspf/viewport.jspf" %>
    <title>Find Jobs - TA Recruitment</title>
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
                <div class="icon-dot">S</div>
                <div class="icon-dot">A</div>
                <div class="icon-dot">P</div>
            </div>
            <%@ include file="/WEB-INF/jspf/ta-side-nav.jspf" %>
        </div>
        <main class="main-panel ta-main">
            <h1>Find Available Jobs</h1>
            <p class="ta-page-lead">Browse open positions matched to your profile. Use search filters to narrow by module or skill.</p>
            <div class="context-card">
                <strong>Quick Tip</strong>
                <p>Open <strong>View details</strong> for full module info, hours, payment, workload, deadline, and responsibilities, then use <strong>Review and apply</strong> to confirm your profile before submitting.</p>
            </div>
            <p class="ai-hint"><strong>AI Skill Matching</strong>: Jobs are ordered by your match score. Complete your <a href="${pageContext.request.contextPath}/ta/profile">profile skills</a> for better matching.</p>

            <form action="${pageContext.request.contextPath}/ta/jobs" method="get" class="search-form search-form--ta">
                <input type="text" name="keyword" placeholder="Keyword (title, module)" value="<%= request.getParameter("keyword") != null ? request.getParameter("keyword") : "" %>">
                <input type="text" name="moduleCode" placeholder="Module code" value="<%= request.getParameter("moduleCode") != null ? request.getParameter("moduleCode") : "" %>">
                <input type="text" name="skill" placeholder="Required skill" value="<%= request.getParameter("skill") != null ? request.getParameter("skill") : "" %>">
                <select name="jobType">
                    <option value="">All types</option>
                    <option value="MODULE_TA" <%= "MODULE_TA".equals(request.getParameter("jobType")) ? "selected" : "" %>>Module TA</option>
                    <option value="INVIGILATION" <%= "INVIGILATION".equals(request.getParameter("jobType")) ? "selected" : "" %>>Invigilation</option>
                    <option value="OTHER" <%= "OTHER".equals(request.getParameter("jobType")) ? "selected" : "" %>>Other</option>
                </select>
                <button type="submit" class="btn btn-primary">Search</button>
            </form>

            <% String err = request.getParameter("error");
               if ("already_applied".equals(err)) { %><p class="error">You have already applied for this job.</p>
            <% } else if ("job_closed".equals(err)) { %><p class="error">This job is no longer open.</p>
            <% } else if ("job_not_found".equals(err)) { %><p class="error">Job not found.</p>
            <% } else if ("invalid_job".equals(err)) { %><p class="error">Invalid job.</p><% } %>

            <% for (Object[] row : jobsWithMatch) {
                Job j = (Job) row[0];
                AIMatchService.MatchResult match = (AIMatchService.MatchResult) row[1];
                boolean saved = row.length > 2 && Boolean.TRUE.equals(row[2]);
                String title = escHtml(j.getTitle() != null ? j.getTitle() : "");
                String moduleCode = escHtml(j.getModuleCode() != null ? j.getModuleCode() : "");
                String moduleName = escHtml(j.getModuleName() != null ? j.getModuleName() : "");
                String desc = escHtml(j.getDescription() != null ? j.getDescription() : "");
                String deadline = escHtml(j.getDeadline() != null ? j.getDeadline() : "");
                String workingHours = escHtml(j.getWorkingHours() != null ? j.getWorkingHours() : "");
                String workload = escHtml(j.getWorkload() != null ? j.getWorkload() : "");
                String postedByName = escHtml(j.getPostedByName() != null ? j.getPostedByName() : "MO");
                String safeJobId = escHtml(j.getId() != null ? j.getId() : "");
            %>
            <div class="job-card ta-job-card">
                <h3><%= title %> - <%= moduleCode %>
                    <span class="match-badge" title="<%= escHtml(match.explanation) %>">Match: <%= (int)match.score %>%</span>
                </h3>
                <p><strong><%= moduleName %></strong>
                    <% if (j.getJobType() != null && !j.getJobType().isEmpty()) { %>
                    | Type: <%= "MODULE_TA".equals(j.getJobType()) ? "Module TA" : "INVIGILATION".equals(j.getJobType()) ? "Invigilation" : "Other" %>
                    <% } %>
                </p>
                <p><%= desc %></p>
                <% if (j.getRequiredSkills() != null && !j.getRequiredSkills().isEmpty()) { %>
                <p class="skills">Required: <%= escHtml(String.join(", ", j.getRequiredSkills())) %></p>
                <% } %>
                <% if (!workingHours.isEmpty()) { %>
                <p><strong>Hours / schedule:</strong> <%= workingHours %></p>
                <% } %>
                <% if (!workload.isEmpty()) { %>
                <p><strong>Workload:</strong> <%= workload %></p>
                <% } %>
                <% if (!deadline.isEmpty()) { %>
                <p class="job-list-deadline"><strong>Apply by:</strong> <%= deadline %></p>
                <% } %>
                <p class="job-list-deadline"><strong>TA slots:</strong> <%= j.getTaSlots() > 0 ? j.getTaSlots() : 1 %></p>
                <% if (j.getExamTimeline() != null && !j.getExamTimeline().isEmpty()) { %>
                <p><strong>Timeline:</strong> <%= j.getExamTimeline() %></p>
                <% } %>
                <% if (j.getInterviewSchedule() != null && !j.getInterviewSchedule().isEmpty()) { %>
                <p><strong>Interview:</strong> <%= j.getInterviewSchedule() %>
                    <% if (j.getInterviewLocation() != null && !j.getInterviewLocation().isEmpty()) { %>
                    @ <%= j.getInterviewLocation() %>
                    <% } %>
                </p>
                <% } %>
                <% if (match.matched != null && !match.matched.isEmpty()) { %>
                <p class="ai-matched">Your matched skills: <%= escHtml(String.join(", ", match.matched)) %></p>
                <% } %>
                <% if (match.missing != null && !match.missing.isEmpty()) { %>
                <p class="ai-missing">Missing skills for this job: <strong><%= escHtml(String.join(", ", match.missing)) %></strong>. Consider adding them to your profile.</p>
                <% } %>
                <p><em>Posted by <%= postedByName %></em></p>
                <div class="ta-job-actions">
                    <a href="${pageContext.request.contextPath}/ta/job?jobId=<%= safeJobId %>" class="btn btn-primary">Open vacancy details</a>
                    <form action="${pageContext.request.contextPath}/ta/save-job" method="post" class="inline-form">
                        <input type="hidden" name="jobId" value="<%= safeJobId %>">
                        <input type="hidden" name="action" value="<%= saved ? "unsave" : "save" %>">
                        <input type="hidden" name="returnTo" value="/ta/jobs">
                        <button type="submit" class="btn <%= saved ? "btn-danger" : "btn-success" %>"><%= saved ? "Remove saved" : "Save job" %></button>
                    </form>
                </div>
            </div>
            <% }
               if (jobsWithMatch.isEmpty()) { %>
            <p class="section-empty section-empty--card ta-empty">No jobs match your search. Try different keywords or clear filters.</p>
            <% } %>
        </main>
        <aside class="right-sidebar">
            <div class="widget-card">
                <div class="widget-title">TA Status</div>
                <p class="widget-line">Role: Teaching Assistant</p>
                <p class="widget-line">Tip: complete profile for better match.</p>
            </div>
            <div class="widget-card">
                <div class="widget-title">Reminders</div>
                <p class="widget-line">Check deadlines before applying.</p>
                <p class="widget-line">Upload CV to unlock full review.</p>
            </div>
        </aside>
    </div>
</div>
</body>
</html>
