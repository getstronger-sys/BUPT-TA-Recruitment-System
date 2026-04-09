<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jspf/html-esc.jspf" %>
<%@ page import="bupt.ta.model.Job" %>
<%@ page import="bupt.ta.ai.AIMatchService" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.regex.Matcher" %>
<%@ page import="java.util.regex.Pattern" %>
<%
    request.setAttribute("taNavActive", "jobs");
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
    String examTimeline = job.getExamTimeline() != null && !job.getExamTimeline().isEmpty() ? escHtml(job.getExamTimeline()) : "—";
    String taPlan = job.getTaAllocationPlan() != null && !job.getTaAllocationPlan().isEmpty() ? escHtml(job.getTaAllocationPlan()) : "—";
    String interviewSchedule = job.getInterviewSchedule() != null && !job.getInterviewSchedule().isEmpty() ? escHtml(job.getInterviewSchedule()) : "—";
    String interviewLocation = job.getInterviewLocation() != null && !job.getInterviewLocation().isEmpty() ? escHtml(job.getInterviewLocation()) : "—";
    int taSlots = job.getTaSlots() > 0 ? job.getTaSlots() : 1;
    List<String> allocationItems = new ArrayList<>();
    String taPlanRaw = job.getTaAllocationPlan();
    if (taPlanRaw != null) {
        String[] planParts = taPlanRaw.split("[\\n;；]+");
        for (String p : planParts) {
            String t = p != null ? p.trim() : "";
            if (!t.isEmpty()) allocationItems.add(escHtml(t));
        }
    }
    List<String[]> weekMilestones = new ArrayList<>();
    String timelineRaw = job.getExamTimeline() != null ? job.getExamTimeline() : "";
    Matcher weekMatcher = Pattern.compile("(?:Week|W|第)\\s*(\\d{1,2})\\s*(?:周)?\\s*[:：-]?\\s*([^;；\\n]+)?", Pattern.CASE_INSENSITIVE).matcher(timelineRaw);
    while (weekMatcher.find()) {
        String weekNo = weekMatcher.group(1);
        String detail = weekMatcher.group(2) != null ? weekMatcher.group(2).trim() : "";
        weekMilestones.add(new String[]{weekNo, escHtml(detail)});
    }
    if (weekMilestones.isEmpty() && !timelineRaw.trim().isEmpty()) {
        String[] fallback = timelineRaw.split("[;；\\n]+");
        int wk = 1;
        for (String f : fallback) {
            String t = f != null ? f.trim() : "";
            if (t.isEmpty()) continue;
            weekMilestones.add(new String[]{String.valueOf(wk), escHtml(t)});
            wk += 3;
        }
    }
    String respText = job.getResponsibilities() != null && !job.getResponsibilities().isEmpty() ? escHtml(job.getResponsibilities()) : "—";
    String desc = job.getDescription() != null && !job.getDescription().isEmpty() ? escHtml(job.getDescription()) : "—";
    String safeTitle = escHtml(job.getTitle() != null ? job.getTitle() : "");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <%@ include file="/WEB-INF/jspf/viewport.jspf" %>
    <title><%= safeTitle %> - Job Detail</title>
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
                <div class="icon-dot active">F</div>
                <div class="icon-dot">A</div>
                <div class="icon-dot">P</div>
            </div>
            <%@ include file="/WEB-INF/jspf/ta-side-nav.jspf" %>
        </div>
        <main class="main-panel ta-main">
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
                <dt>TA slots</dt><dd><%= job.getTaSlots() > 0 ? job.getTaSlots() : 1 %></dd>
                <dt>Course timeline</dt>
                <dd>
                    <% if (weekMilestones.isEmpty()) { %>
                    <span class="pre-wrap"><%= examTimeline %></span>
                    <% } else { %>
                    <div class="week-timeline-list">
                        <% for (String[] item : weekMilestones) {
                               int weekNum = 1;
                               try { weekNum = Integer.parseInt(item[0]); } catch (Exception ignored) {}
                               int progress = Math.max(0, Math.min(100, (int) Math.round((weekNum / 14.0) * 100)));
                        %>
                        <div class="week-timeline-row">
                            <div class="week-line">
                                <span class="week-label">W<%= weekNum %></span>
                                <span class="week-progress"><span class="week-progress-fill" style="width:<%= progress %>%"></span></span>
                            </div>
                            <div class="week-desc"><%= item[1] != null && !item[1].isEmpty() ? item[1] : "Milestone" %></div>
                        </div>
                        <% } %>
                    </div>
                    <% } %>
                </dd>
                <dt>Multi-TA allocation plan</dt><dd class="pre-wrap"><%= taPlan %></dd>
                <dt>Interview schedule</dt><dd class="pre-wrap"><%= interviewSchedule %></dd>
                <dt>Interview location</dt><dd class="pre-wrap"><%= interviewLocation %></dd>
                <dt>Application deadline</dt><dd><%= deadline %></dd>
                <dt>Open status</dt><dd><%= isOpen ? "Open for applications" : "Closed" %></dd>
            </dl>
            <h2>Per-TA responsibilities</h2>
            <div class="ta-duty-board">
                <% for (int idx = 1; idx <= taSlots; idx++) {
                       String allocation = idx <= allocationItems.size()
                               ? allocationItems.get(idx - 1)
                               : "General support: lab/tutorial assistance, Q&A, and exam-day backup.";
                %>
                <article class="ta-duty-card">
                    <div class="ta-duty-head"><span class="arr-icon arr-icon-slots" aria-hidden="true">TA</span>TA-<%= idx %></div>
                    <p class="pre-wrap"><%= allocation %></p>
                </article>
                <% } %>
            </div>

            <div class="context-card job-detail-overview">
                <strong>Overview</strong>
                <p class="pre-wrap"><%= desc %></p>
            </div>

            <p><em>Posted by <%= escHtml(job.getPostedByName() != null ? job.getPostedByName() : "MO") %></em></p>

            <% if (isOpen) { %>
            <div class="job-detail-apply">
                <h2>Apply for this position</h2>
                <p class="muted-inline">Review the job details above, then continue to confirm what will be shared from your profile and CV.</p>
                <p><a href="${pageContext.request.contextPath}/ta/apply-confirm?jobId=<%= escHtml(job.getId()) %>" class="btn btn-primary btn-lg">Review and apply</a></p>
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
