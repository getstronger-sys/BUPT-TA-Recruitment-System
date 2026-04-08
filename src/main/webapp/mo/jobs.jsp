<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jspf/html-esc.jspf" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.regex.Matcher" %>
<%@ page import="java.util.regex.Pattern" %>
<%@ page import="bupt.ta.model.Job" %>
<%@ page import="bupt.ta.model.Application" %>
<%@ page import="bupt.ta.ai.AIMatchService" %>
<% List<Object[]> jobsWithApps = (List<Object[]>) request.getAttribute("jobsWithApps"); if (jobsWithApps == null) jobsWithApps = java.util.Collections.emptyList();
   List<Object[]> moJobPickList = (List<Object[]>) request.getAttribute("moJobPickList");
   if (moJobPickList == null) moJobPickList = java.util.Collections.emptyList();
   boolean moJobListMode = Boolean.TRUE.equals(request.getAttribute("moJobListMode"));
   String moSelectedJobId = (String) request.getAttribute("moSelectedJobId");
   if (moSelectedJobId == null) moSelectedJobId = "";
   String moJobIdQ = moSelectedJobId.isEmpty() ? "" : "&jobId=" + java.net.URLEncoder.encode(moSelectedJobId, "UTF-8");
   String moView = (String) request.getAttribute("moJobsView");
   if (moView == null) moView = "pending";
   int moCntP = request.getAttribute("moJobsCountPending") != null ? (Integer) request.getAttribute("moJobsCountPending") : 0;
   int moCntI = request.getAttribute("moJobsCountInterview") != null ? (Integer) request.getAttribute("moJobsCountInterview") : 0;
   int moCntW = request.getAttribute("moJobsCountWithdrawn") != null ? (Integer) request.getAttribute("moJobsCountWithdrawn") : 0;
   int moCntO = request.getAttribute("moJobsCountOutcome") != null ? (Integer) request.getAttribute("moJobsCountOutcome") : 0;
   String moJobsBaseAttr = (String) request.getAttribute("moJobsBase");
   String moBase = moJobsBaseAttr != null ? moJobsBaseAttr : request.getContextPath() + "/mo/jobs";
   boolean moPastJobsPage = Boolean.TRUE.equals(request.getAttribute("moPastJobsPage"));
   String moCtx = request.getContextPath();
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title><%= moPastJobsPage ? "Past postings - MO" : "My Jobs - MO" %></title>
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
                <a class="<%= moPastJobsPage ? "" : "active" %>" href="<%= moCtx %>/mo/jobs">My Jobs</a>
                <a href="<%= moCtx %>/mo/post-job">Post Job</a>
                <a class="<%= moPastJobsPage ? "active" : "" %>" href="<%= moCtx %>/mo/past-jobs">Past postings</a>
            </aside>
        </div>
        <main class="main-panel">
            <% if ("1".equals(request.getParameter("success"))) { %><p class="success">Job posted successfully!</p><% } %>
            <% if ("1".equals(request.getParameter("updated"))) { %><p class="success">Applicant status updated.</p><% } %>
            <% if ("1".equals(request.getParameter("notice"))) { %><p class="success">Interview notice saved (in-app message).</p><% } %>
            <% String err = request.getParameter("error");
               if (err != null) {
                   String errMsg = err;
                   if ("not_pending".equals(err)) errMsg = "Only pending applications can be moved to interview.";
                   else if ("not_interview".equals(err)) errMsg = "Only interview-stage applications can be selected.";
                   else if ("not_applicant".equals(err)) errMsg = "This action is not allowed for the current status.";
                   else if ("batch_empty".equals(err)) errMsg = "Select at least one row.";
                   else if ("invalid_action".equals(err)) errMsg = "Invalid action.";
                   else if ("invalid_job".equals(err)) errMsg = "Invalid posting or access denied.";
            %><p class="error">Error: <%= errMsg %></p><% } %>

            <% if (moJobListMode) { %>
            <h1><%= moPastJobsPage ? "Past postings" : "Your postings" %></h1>
            <div class="context-card">
                <% if (moPastJobsPage) { %>
                <strong>Closed or past deadline</strong>
                <p>These jobs were closed manually or are past the application deadline. Use this list for history only. Active recruitment is under <strong>My Jobs</strong> in the sidebar.</p>
                <% } else { %>
                <strong>One posting at a time</strong>
                <p>Choose a job below to manage it. Applicants, interviewees, and progress are kept separate per posting and are not mixed in one list.</p>
                <% } %>
            </div>
            <% if (moPastJobsPage) { %>
            <p><a href="<%= moCtx %>/mo/jobs" class="btn btn-primary">Back to active postings</a></p>
            <% } else { %>
            <p><a href="<%= moCtx %>/mo/post-job" class="btn btn-primary">Post New Job</a></p>
            <% } %>
            <% if (moJobPickList.isEmpty()) { %>
            <p><%= moPastJobsPage ? "No closed or expired postings." : "You have not posted any jobs yet." %><% if (!moPastJobsPage) { %> <a href="<%= moCtx %>/mo/post-job">Post your first job</a>.<% } %></p>
            <% } else { %>
            <div class="mo-job-pick-grid">
            <% for (Object[] pick : moJobPickList) {
                Job pj = (Job) pick[0];
                List<AIMatchService.ApplicantRecommendation> pr = (List<AIMatchService.ApplicantRecommendation>) pick[1];
                List<AIMatchService.ApplicantRecommendation> ir = (List<AIMatchService.ApplicantRecommendation>) pick[2];
                List<AIMatchService.ApplicantRecommendation> wr = (List<AIMatchService.ApplicantRecommendation>) pick[3];
                List<AIMatchService.ApplicantRecommendation> or = (List<AIMatchService.ApplicantRecommendation>) pick[4];
                if (pr == null) pr = java.util.Collections.emptyList();
                if (ir == null) ir = java.util.Collections.emptyList();
                if (wr == null) wr = java.util.Collections.emptyList();
                if (or == null) or = java.util.Collections.emptyList();
                String pickHref = moBase + "?jobId=" + java.net.URLEncoder.encode(pj.getId(), "UTF-8") + "&view=pending";
            %>
                <a class="mo-job-pick-card context-card" href="<%= pickHref %>">
                    <h3><%= escHtml(pj.getTitle()) %></h3>
                    <p class="pick-meta"><%= escHtml(pj.getModuleCode()) %> · <%= escHtml(pj.getModuleName() != null ? pj.getModuleName() : "") %></p>
                    <p class="pick-meta"><span class="job-status-text">(<%= escHtml(pj.getStatus()) %>)</span>
                        <% if (moPastJobsPage && pj.getDeadline() != null && !pj.getDeadline().isEmpty()) { %>
                        <span class="muted-inline"> · Deadline <%= escHtml(pj.getDeadline()) %></span>
                        <% } %>
                    </p>
                    <p class="pick-stats">App <span class="tab-count"><%= pr.size() %></span> · Int <span class="tab-count"><%= ir.size() %></span> · Wdn <span class="tab-count"><%= wr.size() %></span> · Out <span class="tab-count"><%= or.size() %></span></p>
                    <span class="btn btn-primary mo-job-pick-cta">Manage this posting</span>
                </a>
            <% } %>
            </div>
            <% } %>
            <% } else { %>
            <p class="breadcrumb-row"><a href="<%= moBase %>" class="mini-link">&larr; Back to posting list</a></p>
            <% Job hdr = jobsWithApps.isEmpty() ? null : (Job) ((Object[]) jobsWithApps.get(0))[0]; %>
            <h1><%= hdr != null ? escHtml(hdr.getTitle()) : "Job management" %></h1>
            <% if (hdr != null) { %><p class="pick-meta"><%= escHtml(hdr.getModuleCode()) %> · <%= escHtml(hdr.getModuleName() != null ? hdr.getModuleName() : "") %></p><% } %>
            <% if (hdr != null) {
                   int taSlots = hdr.getTaSlots() > 0 ? hdr.getTaSlots() : 1;
                   List<String> allocationItems = new ArrayList<>();
                   String planRaw = hdr.getTaAllocationPlan();
                   if (planRaw != null) {
                       String[] parts = planRaw.split("[\\n;；]+");
                       for (String p : parts) {
                           String t = p != null ? p.trim() : "";
                           if (!t.isEmpty()) allocationItems.add(t);
                       }
                   }
                   List<String[]> weekMilestones = new ArrayList<>();
                   String timelineRaw = hdr.getExamTimeline() != null ? hdr.getExamTimeline() : "";
                   Matcher weekMatcher = Pattern.compile("(?:Week|W|第)\\s*(\\d{1,2})\\s*(?:周)?\\s*[:：-]?\\s*([^;；\\n]+)?", Pattern.CASE_INSENSITIVE).matcher(timelineRaw);
                   while (weekMatcher.find()) {
                       String weekNo = weekMatcher.group(1);
                       String detail = weekMatcher.group(2) != null ? weekMatcher.group(2).trim() : "";
                       weekMilestones.add(new String[]{weekNo, detail});
                   }
                   if (weekMilestones.isEmpty() && !timelineRaw.trim().isEmpty()) {
                       String[] fallback = timelineRaw.split("[;；\\n]+");
                       int wk = 1;
                       for (String f : fallback) {
                           String t = f != null ? f.trim() : "";
                           if (t.isEmpty()) continue;
                           weekMilestones.add(new String[]{String.valueOf(wk), t});
                           wk += 3;
                       }
                   }
            %>
            <div class="context-card">
                <strong>Recruitment arrangement</strong>
                <table class="arrangement-table">
                    <tbody>
                    <tr>
                        <th><span class="arr-icon arr-icon-slots" aria-hidden="true">TA</span>TA slots</th>
                        <td><%= taSlots %></td>
                    </tr>
                    <tr>
                        <th><span class="arr-icon arr-icon-timeline" aria-hidden="true">TL</span>Course timeline</th>
                        <td>
                            <% if (weekMilestones.isEmpty()) { %>
                            <p class="pre-wrap"><%= escHtml(hdr.getExamTimeline() != null ? hdr.getExamTimeline() : "Not set.") %></p>
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
                                    <div class="week-desc"><%= escHtml(item[1] != null && !item[1].isEmpty() ? item[1] : "Milestone") %></div>
                                </div>
                                <% } %>
                            </div>
                            <% } %>
                        </td>
                    </tr>
                    <tr>
                        <th><span class="arr-icon arr-icon-interview" aria-hidden="true">IV</span>Interview timetable</th>
                        <td class="pre-wrap"><%= escHtml(hdr.getInterviewSchedule() != null ? hdr.getInterviewSchedule() : "Not set.") %></td>
                    </tr>
                    <tr>
                        <th><span class="arr-icon arr-icon-location" aria-hidden="true">LOC</span>Interview location</th>
                        <td class="pre-wrap"><%= escHtml(hdr.getInterviewLocation() != null ? hdr.getInterviewLocation() : "Not set.") %></td>
                    </tr>
                    </tbody>
                </table>
                <div class="ta-duty-board">
                    <% for (int idx = 1; idx <= taSlots; idx++) {
                           String item = idx <= allocationItems.size()
                                   ? allocationItems.get(idx - 1)
                                   : "General support: lab/tutorial assistance, Q&A, and exam-day backup.";
                    %>
                    <article class="ta-duty-card">
                        <div class="ta-duty-head"><span class="arr-icon arr-icon-slots" aria-hidden="true">TA</span>TA-<%= idx %></div>
                        <p class="pre-wrap"><%= escHtml(item) %></p>
                    </article>
                    <% } %>
                </div>
            </div>
            <% } %>
            <div class="context-card">
                <strong>Workflow</strong>
                <p>Use the four tabs: Applicants &rarr; Interview &rarr; Withdrawn &rarr; Outcomes. This page shows only <strong>this posting</strong>.
                <% if (moPastJobsPage) { %><span class="muted-inline"> (Closed or past deadline; you can still review and adjust records.)</span><% } %>
                </p>
            </div>
            <nav class="mo-jobs-tabs" aria-label="Application views">
                <a href="<%= moBase %>?view=pending<%= moJobIdQ %>" class="mo-jobs-tab <%= "pending".equals(moView) ? "active" : "" %>">Applicants<span class="tab-count"><%= moCntP %></span></a>
                <a href="<%= moBase %>?view=interview<%= moJobIdQ %>" class="mo-jobs-tab <%= "interview".equals(moView) ? "active" : "" %>">Interview<span class="tab-count"><%= moCntI %></span></a>
                <a href="<%= moBase %>?view=withdrawn<%= moJobIdQ %>" class="mo-jobs-tab <%= "withdrawn".equals(moView) ? "active" : "" %>">Withdrawn<span class="tab-count"><%= moCntW %></span></a>
                <a href="<%= moBase %>?view=outcome<%= moJobIdQ %>" class="mo-jobs-tab <%= "outcome".equals(moView) ? "active" : "" %>">Outcomes<span class="tab-count"><%= moCntO %></span></a>
            </nav>
            <p class="ai-hint"><strong>AI hint</strong>: Within each group, applicants are sorted by match score and workload.</p>
            <p><a href="${pageContext.request.contextPath}/mo/post-job" class="btn btn-primary">Post New Job</a></p>

            <% for (Object[] row : jobsWithApps) {
                Job j = (Job) row[0];
                List<AIMatchService.ApplicantRecommendation> pendingRecs = (List<AIMatchService.ApplicantRecommendation>) row[1];
                List<AIMatchService.ApplicantRecommendation> interviewRecs = (List<AIMatchService.ApplicantRecommendation>) row[2];
                List<AIMatchService.ApplicantRecommendation> withdrawnRecs = (List<AIMatchService.ApplicantRecommendation>) row[3];
                List<AIMatchService.ApplicantRecommendation> outcomeRecs = (List<AIMatchService.ApplicantRecommendation>) row[4];
                if (pendingRecs == null) pendingRecs = java.util.Collections.emptyList();
                if (interviewRecs == null) interviewRecs = java.util.Collections.emptyList();
                if (withdrawnRecs == null) withdrawnRecs = java.util.Collections.emptyList();
                if (outcomeRecs == null) outcomeRecs = java.util.Collections.emptyList();
                int totalApps = pendingRecs.size() + interviewRecs.size() + withdrawnRecs.size() + outcomeRecs.size();
                String batchPendingFormId = "batch_pending_" + j.getId().replaceAll("[^A-Za-z0-9]", "_");
                String batchNoticeFormId = "batch_notice_" + j.getId().replaceAll("[^A-Za-z0-9]", "_");
            %>
            <div class="job-card">
                <div class="job-card-head">
                    <div class="job-card-title">
                        <h3><%= j.getTitle() %> - <%= j.getModuleCode() %> <span class="job-status-text">(<%= j.getStatus() %>)</span></h3>
                        <% if (j.getJobType() != null && !j.getJobType().isEmpty()) { %>
                        <p class="skills">Type: <%= "MODULE_TA".equals(j.getJobType()) ? "Module TA" : "INVIGILATION".equals(j.getJobType()) ? "Invigilation" : "Other" %></p>
                        <% } %>
                    </div>
                    <div class="job-card-actions">
                        <% if ("OPEN".equals(j.getStatus())) { %>
                        <form action="${pageContext.request.contextPath}/mo/close-job" method="post">
                            <input type="hidden" name="jobId" value="<%= j.getId() %>">
                            <input type="hidden" name="action" value="close">
                            <button type="submit" class="btn btn-danger">Close Job</button>
                        </form>
                        <% } else { %>
                        <form action="${pageContext.request.contextPath}/mo/close-job" method="post">
                            <input type="hidden" name="jobId" value="<%= j.getId() %>">
                            <input type="hidden" name="action" value="reopen">
                            <button type="submit" class="btn btn-primary">Reopen</button>
                        </form>
                        <% } %>
                    </div>
                </div>

                <p class="job-description"><%= j.getDescription() != null ? j.getDescription() : "" %></p>
                <% if (j.getRequiredSkills() != null && !j.getRequiredSkills().isEmpty()) { %>
                <p class="skills">Required: <%= String.join(", ", j.getRequiredSkills()) %></p>
                <% } %>

                <div class="applicants-panel">
                    <div class="applicants-head">
                        <h4>
                            <% if ("pending".equals(moView)) { %>Applicants<% } else if ("interview".equals(moView)) { %>Interview<% } else if ("withdrawn".equals(moView)) { %>Withdrawn<% } else { %>Outcomes<% } %>
                            <span class="job-apps-count">(this posting: <%= "pending".equals(moView) ? pendingRecs.size() : "interview".equals(moView) ? interviewRecs.size() : "withdrawn".equals(moView) ? withdrawnRecs.size() : outcomeRecs.size() %>)</span>
                        </h4>
                    </div>

                    <% if (totalApps == 0) { %>
                    <div class="empty-applicants-card">No applications yet.</div>
                    <% } else if ("pending".equals(moView)) { %>
                    <form id="<%= batchPendingFormId %>" action="${pageContext.request.contextPath}/mo/batch-applicants" method="post" class="batch-form-hidden">
                        <input type="hidden" name="action" value="toInterview">
                        <input type="hidden" name="returnJobId" value="<%= j.getId() %>">
                    </form>
                    <% if (!pendingRecs.isEmpty()) { %>
                    <p class="batch-toolbar">
                        <button type="submit" class="btn btn-primary" form="<%= batchPendingFormId %>">Set selected to interview (batch)</button>
                    </p>
                    <% for (AIMatchService.ApplicantRecommendation rec : pendingRecs) {
                        Application a = rec.application;
                        String appliedText = a.getAppliedAt() != null ? a.getAppliedAt().replace("T", " ").replaceFirst("\\..*$", "") : "-";
                        String applicantName = a.getApplicantName() != null ? a.getApplicantName() : a.getApplicantId();
                        boolean hasProfile = rec.profile != null;
                        String skillsText = hasProfile && rec.profile.getSkills() != null && !rec.profile.getSkills().isEmpty() ? String.join(", ", rec.profile.getSkills()) : "Not provided";
                        String missingText = rec.matchResult.missing != null && !rec.matchResult.missing.isEmpty() ? String.join(", ", rec.matchResult.missing) : "No major gaps";
                        boolean hasCv = hasProfile && rec.profile.getCvFilePath() != null && !rec.profile.getCvFilePath().isEmpty();
                        String degreeText = hasProfile && rec.profile.getDegree() != null && !rec.profile.getDegree().isEmpty() ? escHtml(rec.profile.getDegree()) : "-";
                        String programmeText = hasProfile && rec.profile.getProgramme() != null && !rec.profile.getProgramme().isEmpty() ? escHtml(rec.profile.getProgramme()) : "-";
                        String taExpText = hasProfile && rec.profile.getTaExperience() != null && !rec.profile.getTaExperience().isEmpty() ? escHtml(rec.profile.getTaExperience()) : "Not provided.";
                        String templateId = "applicant-tpl-" + j.getId() + "-" + a.getId();
                        String noteText = a.getNotes() != null && !a.getNotes().isEmpty() ? a.getNotes() : "No notes saved for this application.";
                        String profileStateText;
                        if (!hasProfile) {
                            profileStateText = "No profile submitted yet.";
                        } else if (hasCv) {
                            profileStateText = "Profile available, CV uploaded.";
                        } else {
                            profileStateText = "Profile available, CV missing.";
                        }
                    %>
                    <article class="applicant-card">
                        <label class="batch-check-label">
                            <input type="checkbox" name="applicationId" value="<%= a.getId() %>" class="batch-checkbox" form="<%= batchPendingFormId %>">
                            <span class="batch-check-hint">Include in batch</span>
                        </label>
                        <div class="applicant-topline">
                            <div class="applicant-title-group">
                                <div class="applicant-name-row">
                                    <h5><%= applicantName %></h5>
                                    <% if (rec.workloadBalanced) { %>
                                    <span class="load-badge" title="Lower workload">Low load</span>
                                    <% } %>
                                </div>
                                <div class="applicant-links">
                                    <button type="button" class="btn btn-primary applicant-quick-btn" data-template="<%= templateId %>">Quick view</button>
                                    <a href="${pageContext.request.contextPath}/mo/applicant-detail?applicantId=<%= a.getApplicantId() %>" class="mini-link">Full profile</a>
                                    <% if (hasCv) { %>
                                    <a href="${pageContext.request.contextPath}/view-cv?userId=<%= a.getApplicantId() %>" target="_blank" rel="noopener" class="mini-link">View CV</a>
                                    <a href="${pageContext.request.contextPath}/view-cv?userId=<%= a.getApplicantId() %>&amp;download=1" class="mini-link">Download CV</a>
                                    <% } else { %>
                                    <span class="muted-inline">CV not uploaded</span>
                                    <% } %>
                                </div>
                            </div>
                            <div class="applicant-score-area">
                                <span class="match-badge" title="<%= rec.matchResult.explanation %>"><%= (int)rec.matchResult.score %>% match</span>
                                <span class="status-pill status-pill-pending">PENDING</span>
                            </div>
                        </div>
                        <div class="applicant-sections applicant-sections-compact">
                            <section class="applicant-section">
                                <div class="section-label">Profile</div>
                                <p class="section-copy"><strong>Skills:</strong> <%= skillsText %></p>
                                <p class="section-copy muted-inline"><%= profileStateText %></p>
                            </section>
                            <section class="applicant-section">
                                <div class="section-label">AI Review</div>
                                <p class="section-copy"><strong>Missing skills:</strong> <%= missingText %></p>
                                <p class="section-copy"><strong>Workload:</strong> <%= rec.currentWorkload %> jobs</p>
                            </section>
                            <section class="applicant-section">
                                <div class="section-label">Application</div>
                                <p class="section-copy"><strong>Applied:</strong> <%= appliedText %></p>
                                <p class="section-copy"><strong>Preferred role:</strong> <%= escHtml(a.getPreferredRole() != null && !a.getPreferredRole().isEmpty() ? a.getPreferredRole() : "Not selected") %></p>
                            </section>
                        </div>
                        <div class="applicant-actions">
                            <div class="decision-bar">
                                <form action="${pageContext.request.contextPath}/mo/select-applicant" method="post" class="decision-form decision-form-inline">
                                    <input type="hidden" name="applicationId" value="<%= a.getId() %>">
                                    <input type="text" name="notes" placeholder="Optional notes" class="note-input">
                                    <div class="decision-buttons decision-buttons-inline">
                                        <button type="submit" name="action" value="interview" class="btn btn-primary decision-btn">Move to interview</button>
                                        <button type="submit" name="action" value="reject" class="btn btn-danger decision-btn">Reject</button>
                                    </div>
                                </form>
                            </div>
                        </div>
                        <template id="<%= templateId %>">
                            <div class="quick-detail-sheet">
                                <p class="quick-detail-name"><%= escHtml(applicantName) %></p>
                                <p><strong>Degree:</strong> <%= degreeText %></p>
                                <p><strong>Programme:</strong> <%= programmeText %></p>
                                <p><strong>Skills:</strong> <%= escHtml(skillsText) %></p>
                                <div class="detail-block-text">
                                    <strong>TA experience</strong>
                                    <p class="pre-wrap"><%= taExpText %></p>
                                </div>
                                <p><strong>CV:</strong>
                                    <% if (hasCv) { %>
                                    <a href="${pageContext.request.contextPath}/view-cv?userId=<%= a.getApplicantId() %>" target="_blank" rel="noopener">View</a>
                                    <span class="muted-inline"> | </span>
                                    <a href="${pageContext.request.contextPath}/view-cv?userId=<%= a.getApplicantId() %>&amp;download=1">Download</a>
                                    <% } else { %>
                                    <span class="muted-inline">Not uploaded</span>
                                    <% } %>
                                </p>
                            </div>
                        </template>
                    </article>
                    <% } %>
                    <p class="batch-toolbar">
                        <button type="submit" class="btn btn-primary" form="<%= batchPendingFormId %>">Set selected to interview (batch)</button>
                    </p>
                    <% } else { %><p class="muted-inline section-empty">No applicants for this posting.</p><% } %>
                    <% } else if ("interview".equals(moView)) { %>
                    <% if (!interviewRecs.isEmpty()) { %>
                    <form id="<%= batchNoticeFormId %>" action="${pageContext.request.contextPath}/mo/batch-applicants" method="post" class="notice-form-fields">
                        <input type="hidden" name="action" value="sendNotice">
                        <input type="hidden" name="returnJobId" value="<%= j.getId() %>">
                        <div class="notice-fields">
                            <label>Interview time <input type="text" name="interviewTime" placeholder="e.g. 2026-04-10 14:00" class="note-input notice-input-wide"></label>
                            <label>Location <input type="text" name="interviewLocation" placeholder="Room or online link" class="note-input notice-input-wide"></label>
                            <label class="notice-label-block">Assessment <textarea name="interviewAssessment" rows="2" placeholder="Scope, format, etc." class="note-input notice-textarea"></textarea></label>
                        </div>
                    </form>
                    <p class="batch-toolbar">
                        <button type="submit" class="btn btn-success" form="<%= batchNoticeFormId %>">Send/update in-app interview notice</button>
                    </p>
                    <% for (AIMatchService.ApplicantRecommendation rec : interviewRecs) {
                        Application a = rec.application;
                        String appliedText = a.getAppliedAt() != null ? a.getAppliedAt().replace("T", " ").replaceFirst("\\..*$", "") : "-";
                        String applicantName = a.getApplicantName() != null ? a.getApplicantName() : a.getApplicantId();
                        boolean hasProfile = rec.profile != null;
                        String skillsText = hasProfile && rec.profile.getSkills() != null && !rec.profile.getSkills().isEmpty() ? String.join(", ", rec.profile.getSkills()) : "Not provided";
                        String missingText = rec.matchResult.missing != null && !rec.matchResult.missing.isEmpty() ? String.join(", ", rec.matchResult.missing) : "No major gaps";
                        boolean hasCv = hasProfile && rec.profile.getCvFilePath() != null && !rec.profile.getCvFilePath().isEmpty();
                        String profileStateText = !hasProfile ? "No profile submitted yet." : (hasCv ? "Profile available, CV uploaded." : "Profile available, CV missing.");
                        boolean hasNotice = (a.getInterviewTime() != null && !a.getInterviewTime().isEmpty())
                                || (a.getInterviewLocation() != null && !a.getInterviewLocation().isEmpty())
                                || (a.getInterviewAssessment() != null && !a.getInterviewAssessment().isEmpty());
                    %>
                    <article class="applicant-card">
                        <label class="batch-check-label">
                            <input type="checkbox" name="applicationId" value="<%= a.getId() %>" class="batch-checkbox" form="<%= batchNoticeFormId %>">
                            <span class="batch-check-hint">Select for notice</span>
                        </label>
                        <div class="applicant-topline">
                            <div class="applicant-title-group">
                                <div class="applicant-name-row">
                                    <h5><%= applicantName %></h5>
                                </div>
                                <div class="applicant-links">
                                    <a href="${pageContext.request.contextPath}/mo/applicant-detail?applicantId=<%= a.getApplicantId() %>" class="mini-link">Full profile</a>
                                    <% if (hasCv) { %>
                                    <a href="${pageContext.request.contextPath}/view-cv?userId=<%= a.getApplicantId() %>" target="_blank" rel="noopener" class="mini-link">View CV</a>
                                    <a href="${pageContext.request.contextPath}/view-cv?userId=<%= a.getApplicantId() %>&amp;download=1" class="mini-link">Download CV</a>
                                    <% } else { %><span class="muted-inline">CV not uploaded</span><% } %>
                                </div>
                            </div>
                            <div class="applicant-score-area">
                                <span class="match-badge" title="<%= rec.matchResult.explanation %>"><%= (int)rec.matchResult.score %>% match</span>
                                <span class="status-pill status-pill-interview">INTERVIEW</span>
                            </div>
                        </div>
                        <% if (hasNotice) { %>
                        <div class="notice-preview">
                            <p><strong>Notice:</strong> Time <%= a.getInterviewTime() != null ? a.getInterviewTime() : "&mdash;" %> |
                                Location <%= a.getInterviewLocation() != null ? a.getInterviewLocation() : "&mdash;" %></p>
                            <% if (a.getInterviewAssessment() != null && !a.getInterviewAssessment().isEmpty()) { %>
                            <p class="muted-inline">Assessment: <%= a.getInterviewAssessment() %></p>
                            <% } %>
                        </div>
                        <% } %>
                        <div class="applicant-sections applicant-sections-compact">
                            <section class="applicant-section">
                                <div class="section-label">Profile</div>
                                <p class="section-copy"><strong>Skills:</strong> <%= skillsText %></p>
                                <p class="section-copy muted-inline"><%= profileStateText %></p>
                            </section>
                            <section class="applicant-section">
                                <div class="section-label">AI Review</div>
                                <p class="section-copy"><strong>Missing skills:</strong> <%= missingText %></p>
                            </section>
                            <section class="applicant-section">
                                <div class="section-label">Application</div>
                                <p class="section-copy"><strong>Applied:</strong> <%= appliedText %></p>
                                <p class="section-copy"><strong>Preferred role:</strong> <%= escHtml(a.getPreferredRole() != null && !a.getPreferredRole().isEmpty() ? a.getPreferredRole() : "Not selected") %></p>
                            </section>
                        </div>
                        <div class="applicant-actions">
                            <div class="decision-bar">
                                <form action="${pageContext.request.contextPath}/mo/select-applicant" method="post" class="decision-form decision-form-inline">
                                    <input type="hidden" name="applicationId" value="<%= a.getId() %>">
                                    <input type="text" name="notes" placeholder="Optional notes" class="note-input">
                                    <div class="decision-buttons decision-buttons-inline">
                                        <button type="submit" name="action" value="select" class="btn btn-success decision-btn">Select</button>
                                        <button type="submit" name="action" value="reject" class="btn btn-danger decision-btn">Reject</button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </article>
                    <% } %>
                    <p class="batch-toolbar">
                        <button type="submit" class="btn btn-success" form="<%= batchNoticeFormId %>">Send/update in-app interview notice</button>
                    </p>
                    <% } else { %><p class="muted-inline section-empty">No interviewees for this posting.</p><% } %>
                    <% } else if ("withdrawn".equals(moView)) { %>
                    <% for (AIMatchService.ApplicantRecommendation rec : withdrawnRecs) {
                        Application a = rec.application;
                        String appliedText = a.getAppliedAt() != null ? a.getAppliedAt().replace("T", " ").replaceFirst("\\..*$", "") : "-";
                        String applicantName = a.getApplicantName() != null ? a.getApplicantName() : a.getApplicantId();
                    %>
                    <article class="applicant-card applicant-card-withdrawn">
                        <div class="applicant-topline">
                            <div class="applicant-title-group">
                                <h5><%= applicantName %></h5>
                                <p class="muted-inline">Application withdrawn</p>
                            </div>
                            <span class="status-pill status-pill-rejected">WITHDRAWN</span>
                        </div>
                        <p class="section-copy"><strong>Applied:</strong> <%= appliedText %></p>
                        <p class="section-copy"><strong>Preferred role:</strong> <%= escHtml(a.getPreferredRole() != null && !a.getPreferredRole().isEmpty() ? a.getPreferredRole() : "Not selected") %></p>
                    </article>
                    <% } %>
                    <% if (withdrawnRecs.isEmpty()) { %><p class="muted-inline section-empty">No withdrawn applications for this posting.</p><% } %>
                    <% } else { %>
                    <% for (AIMatchService.ApplicantRecommendation rec : outcomeRecs) {
                        Application a = rec.application;
                        String appliedText = a.getAppliedAt() != null ? a.getAppliedAt().replace("T", " ").replaceFirst("\\..*$", "") : "-";
                        String applicantName = a.getApplicantName() != null ? a.getApplicantName() : a.getApplicantId();
                        boolean hasProfile = rec.profile != null;
                        String skillsText = hasProfile && rec.profile.getSkills() != null && !rec.profile.getSkills().isEmpty() ? String.join(", ", rec.profile.getSkills()) : "Not provided";
                        String missingText = rec.matchResult.missing != null && !rec.matchResult.missing.isEmpty() ? String.join(", ", rec.matchResult.missing) : "No major gaps";
                        boolean hasCv = hasProfile && rec.profile.getCvFilePath() != null && !rec.profile.getCvFilePath().isEmpty();
                        String profileStateText = !hasProfile ? "No profile submitted yet." : (hasCv ? "Profile available, CV uploaded." : "Profile available, CV missing.");
                        String noteText = a.getNotes() != null && !a.getNotes().isEmpty() ? a.getNotes() : "No notes saved for this application.";
                        String statusPillClass = "SELECTED".equals(a.getStatus()) ? "status-pill status-pill-selected" : "status-pill status-pill-rejected";
                        String degreeText = hasProfile && rec.profile.getDegree() != null && !rec.profile.getDegree().isEmpty() ? escHtml(rec.profile.getDegree()) : "-";
                        String programmeText = hasProfile && rec.profile.getProgramme() != null && !rec.profile.getProgramme().isEmpty() ? escHtml(rec.profile.getProgramme()) : "-";
                        String taExpText = hasProfile && rec.profile.getTaExperience() != null && !rec.profile.getTaExperience().isEmpty() ? escHtml(rec.profile.getTaExperience()) : "Not provided.";
                        String templateId = "applicant-outcome-" + j.getId() + "-" + a.getId();
                    %>
                    <article class="applicant-card">
                        <div class="applicant-topline">
                            <div class="applicant-title-group">
                                <div class="applicant-name-row">
                                    <h5><%= applicantName %></h5>
                                </div>
                                <div class="applicant-links">
                                    <button type="button" class="btn btn-primary applicant-quick-btn" data-template="<%= templateId %>">Quick view</button>
                                    <a href="${pageContext.request.contextPath}/mo/applicant-detail?applicantId=<%= a.getApplicantId() %>" class="mini-link">Full profile</a>
                                    <% if (hasCv) { %>
                                    <a href="${pageContext.request.contextPath}/view-cv?userId=<%= a.getApplicantId() %>" target="_blank" rel="noopener" class="mini-link">View CV</a>
                                    <a href="${pageContext.request.contextPath}/view-cv?userId=<%= a.getApplicantId() %>&amp;download=1" class="mini-link">Download CV</a>
                                    <% } else { %>
                                    <span class="muted-inline">CV not uploaded</span>
                                    <% } %>
                                </div>
                            </div>
                            <div class="applicant-score-area">
                                <span class="match-badge" title="<%= rec.matchResult.explanation %>"><%= (int)rec.matchResult.score %>% match</span>
                                <span class="<%= statusPillClass %>"><%= a.getStatus() %></span>
                            </div>
                        </div>
                        <div class="applicant-sections applicant-sections-compact">
                            <section class="applicant-section">
                                <div class="section-label">Profile</div>
                                <p class="section-copy"><strong>Skills:</strong> <%= skillsText %></p>
                                <p class="section-copy muted-inline"><%= profileStateText %></p>
                            </section>
                            <section class="applicant-section">
                                <div class="section-label">AI Review</div>
                                <p class="section-copy"><strong>Missing skills:</strong> <%= missingText %></p>
                            </section>
                            <section class="applicant-section">
                                <div class="section-label">Application</div>
                                <p class="section-copy"><strong>Applied:</strong> <%= appliedText %></p>
                                <p class="section-copy"><strong>Preferred role:</strong> <%= escHtml(a.getPreferredRole() != null && !a.getPreferredRole().isEmpty() ? a.getPreferredRole() : "Not selected") %></p>
                            </section>
                        </div>
                        <div class="applicant-actions">
                            <div class="decision-bar decision-bar-recorded">
                                <div class="decision-bar-copy">
                                    <div class="section-label">Decision recorded</div>
                                    <p>Status: <strong><%= a.getStatus() %></strong></p>
                                </div>
                                <div class="decision-summary">
                                    <p><strong>Notes:</strong> <%= noteText %></p>
                                </div>
                            </div>
                        </div>
                        <template id="<%= templateId %>">
                            <div class="quick-detail-sheet">
                                <p class="quick-detail-name"><%= escHtml(applicantName) %></p>
                                <p><strong>Degree:</strong> <%= degreeText %></p>
                                <p><strong>Programme:</strong> <%= programmeText %></p>
                                <p><strong>Skills:</strong> <%= escHtml(skillsText) %></p>
                                <div class="detail-block-text">
                                    <strong>TA experience</strong>
                                    <p class="pre-wrap"><%= taExpText %></p>
                                </div>
                                <p><strong>CV:</strong>
                                    <% if (hasCv) { %>
                                    <a href="${pageContext.request.contextPath}/view-cv?userId=<%= a.getApplicantId() %>" target="_blank" rel="noopener">View</a>
                                    <span class="muted-inline"> | </span>
                                    <a href="${pageContext.request.contextPath}/view-cv?userId=<%= a.getApplicantId() %>&amp;download=1">Download</a>
                                    <% } else { %>
                                    <span class="muted-inline">Not uploaded</span>
                                    <% } %>
                                </p>
                            </div>
                        </template>
                    </article>
                    <% } %>
                    <% if (outcomeRecs.isEmpty()) { %><p class="muted-inline section-empty">No outcomes recorded for this posting yet.</p><% } %>
                    <% } %>
                </div>
            </div>
            <% }
               if (jobsWithApps.isEmpty()) { %>
            <p class="error">Could not load this posting. <a href="<%= moBase %>">Back to list</a></p>
            <% } %>
            <% } %>
        </main>
        <aside class="right-sidebar">
            <div class="widget-card">
                <div class="widget-title">MO Dashboard</div>
                <p class="widget-line">Applicants &rarr; Interview &rarr; In-app notice &rarr; Select / reject</p>
            </div>
        </aside>
    </div>
</div>

<dialog id="applicantQuickDialog" class="applicant-quick-dialog">
    <div class="applicant-quick-dialog-inner">
        <div class="applicant-quick-dialog-head">
            <h3>Applicant detail</h3>
            <button type="button" class="dialog-close-btn" aria-label="Close">&times;</button>
        </div>
        <div class="applicant-quick-dialog-body"></div>
    </div>
</dialog>
<script>
(function () {
    var dialog = document.getElementById('applicantQuickDialog');
    if (!dialog) return;
    var body = dialog.querySelector('.applicant-quick-dialog-body');
    var closeBtn = dialog.querySelector('.dialog-close-btn');
    if (closeBtn) closeBtn.addEventListener('click', function () { dialog.close(); });
    dialog.addEventListener('click', function (e) { if (e.target === dialog) dialog.close(); });
    document.querySelectorAll('.applicant-quick-btn').forEach(function (btn) {
        btn.addEventListener('click', function () {
            var id = btn.getAttribute('data-template');
            var tpl = id ? document.getElementById(id) : null;
            if (body) body.innerHTML = '';
            if (tpl && tpl.content && body) body.appendChild(tpl.content.cloneNode(true));
            dialog.showModal();
        });
    });
})();
</script>
</body>
</html>
