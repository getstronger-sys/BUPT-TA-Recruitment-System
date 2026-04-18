<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jspf/html-esc.jspf" %>
<%@ page import="bupt.ta.model.Job" %>
<%@ page import="bupt.ta.model.WorkArrangementItem" %>
<%@ page import="bupt.ta.service.InterviewBookingService.SlotSummary" %>
<%@ page import="bupt.ta.util.WorkQuotaPlanner" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.regex.Matcher" %>
<%@ page import="java.util.regex.Pattern" %>
<%
    Job job = (Job) request.getAttribute("job");
    if (job == null) {
        response.sendRedirect(request.getContextPath() + "/mo/jobs?error=invalid_job");
        return;
    }
    String moListPath = (String) request.getAttribute("moListPath");
    if (moListPath == null) moListPath = "/mo/jobs";
    boolean moPastJobsPage = Boolean.TRUE.equals(request.getAttribute("moPastJobsPage"));
    String moCtx = request.getContextPath();
    String manageHref = moCtx + moListPath + "?jobId=" + java.net.URLEncoder.encode(job.getId(), "UTF-8") + "&view=pending";

    boolean isOpen = "OPEN".equals(job.getStatus());
    String moduleName = job.getModuleName() != null && !job.getModuleName().isEmpty() ? escHtml(job.getModuleName()) : "&mdash;";
    String wh = job.getWorkingHours() != null && !job.getWorkingHours().isEmpty() ? escHtml(job.getWorkingHours()) : "&mdash;";
    String wl = job.getWorkload() != null && !job.getWorkload().isEmpty() ? escHtml(job.getWorkload()) : "&mdash;";
    String pay = job.getPayment() != null && !job.getPayment().isEmpty() ? escHtml(job.getPayment()) : "&mdash;";
    String deadline = job.getDeadline() != null && !job.getDeadline().isEmpty() ? escHtml(job.getDeadline()) : "&mdash;";
    String examTimeline = job.getExamTimeline() != null && !job.getExamTimeline().isEmpty() ? escHtml(job.getExamTimeline()) : "&mdash;";
    String interviewSchedule = job.getInterviewSchedule() != null && !job.getInterviewSchedule().isEmpty() ? escHtml(job.getInterviewSchedule()) : "&mdash;";
    String interviewLocation = job.getInterviewLocation() != null && !job.getInterviewLocation().isEmpty() ? escHtml(job.getInterviewLocation()) : "&mdash;";
    int plannedRecruits = job.getTaSlots() > 0 ? job.getTaSlots() : 1;
    WorkQuotaPlanner.Recommendation quotaRec = WorkQuotaPlanner.recommend(job.getWorkArrangements(), plannedRecruits);
    List<SlotSummary> slotSummaries = (List<SlotSummary>) request.getAttribute("slotSummaries");
    if (slotSummaries == null) slotSummaries = new ArrayList<>();
    List<String[]> weekMilestones = new ArrayList<>();
    String timelineRaw = job.getExamTimeline() != null ? job.getExamTimeline() : "";
    Matcher weekMatcher = Pattern.compile("(?:Week|W)\\s*(\\d{1,2})\\s*[:\\-]?\\s*([^;\\n]+)?", Pattern.CASE_INSENSITIVE).matcher(timelineRaw);
    while (weekMatcher.find()) {
        String weekNo = weekMatcher.group(1);
        String detail = weekMatcher.group(2) != null ? weekMatcher.group(2).trim() : "";
        weekMilestones.add(new String[]{weekNo, escHtml(detail)});
    }
    if (weekMilestones.isEmpty() && !timelineRaw.trim().isEmpty()) {
        String[] fallback = timelineRaw.split("[;\\n]+");
        int wk = 1;
        for (String f : fallback) {
            String t = f != null ? f.trim() : "";
            if (t.isEmpty()) continue;
            weekMilestones.add(new String[]{String.valueOf(wk), escHtml(t)});
            wk += 3;
        }
    }
%>
<%@ include file="/WEB-INF/jspf/job-ta-plan-chunks.jspf" %>
<%
    String respText = job.getResponsibilities() != null && !job.getResponsibilities().isEmpty() ? escHtml(job.getResponsibilities()) : "&mdash;";
    String desc = job.getDescription() != null && !job.getDescription().isEmpty() ? escHtml(job.getDescription()) : "&mdash;";
    String safeTitle = escHtml(job.getTitle() != null ? job.getTitle() : "");
    String createdAt = job.getCreatedAt() != null && !job.getCreatedAt().isEmpty() ? escHtml(job.getCreatedAt()) : "&mdash;";
    String maxAppText = job.getMaxApplicants() <= 0 ? "No limit" : String.valueOf(job.getMaxApplicants());
    request.setAttribute("moNavActive", moPastJobsPage ? "past" : "jobs");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <%@ include file="/WEB-INF/jspf/viewport.jspf" %>
    <title><%= safeTitle %> - Posting detail</title>
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
                <div class="icon-dot active">J</div>
                <div class="icon-dot">P</div>
                <div class="icon-dot">D</div>
            </div>
            <%@ include file="/WEB-INF/jspf/mo-side-nav.jspf" %>
        </div>
        <main class="main-panel">
            <p class="breadcrumb-line">
                <a href="<%= moCtx %><%= moListPath %>">&larr; Back to posting list</a>
                &nbsp;|&nbsp;
                <a href="<%= manageHref %>">Manage applicants</a>
            </p>
            <% if ("1".equals(request.getParameter("posted"))) { %>
            <p class="success">Posting saved. Everything below is the full text you entered (same fields TAs see, plus MO-only limits). When you are ready, open <a href="<%= manageHref %>">Manage applicants</a>.</p>
            <% } %>
            <% if ("1".equals(request.getParameter("taCountsUpdated"))) { %>
            <p class="success">TA counts per work item were updated. Summary text was refreshed while planned recruits stayed unchanged.</p>
            <% } %>
            <% if ("1".equals(request.getParameter("slotSaved"))) { %>
            <p class="success">Interview slot updated successfully.</p>
            <% } %>
            <% String waErr = request.getParameter("error");
               if ("wa_count_mismatch".equals(waErr)) { %><p class="error">Could not update TA counts (form mismatch). Please try again.</p><% }
               else if ("wa_ta_invalid".equals(waErr)) { %><p class="error">Each row needs at least 1 TA.</p><% }
               else if ("planned_ta_invalid".equals(waErr)) { %><p class="error">Planned recruits must be at least 1.</p><% } %>
            <% if (request.getParameter("slotError") != null) { %><p class="error"><%= escHtml(request.getParameter("slotError")) %></p><% } %>
            <h1><%= safeTitle %></h1>
            <p class="job-detail-meta">
                <span class="status-pill <%= isOpen ? "status-pill-pending" : "status-pill-rejected" %>"><%= escHtml(job.getStatus()) %></span>
                <% if (job.getJobType() != null && !job.getJobType().isEmpty()) { %>
                <span class="muted-inline">Type: <%= "MODULE_TA".equals(job.getJobType()) ? "Module TA" : "INVIGILATION".equals(job.getJobType()) ? "Invigilation" : "Other" %></span>
                <% } %>
            </p>

            <% if (job.getWorkArrangements() != null && !job.getWorkArrangements().isEmpty()) { %>
            <h2 class="job-wa-heading">Work arrangements</h2>
            <p class="muted-inline job-wa-edit-hint">You can change <strong>TA count</strong> per row anytime; other columns reflect what was set at posting time.</p>
            <form action="<%= moCtx %>/mo/update-work-ta-counts" method="post" class="form job-wa-ta-form">
                <input type="hidden" name="jobId" value="<%= escHtml(job.getId()) %>">
                <table class="job-wa-table">
                    <thead>
                    <tr>
                        <th scope="col">Work name</th>
                        <th scope="col">Per-session duration</th>
                        <th scope="col">Occurrences</th>
                        <th scope="col">TA count</th>
                        <th scope="col">Specific time</th>
                    </tr>
                    </thead>
                    <tbody>
                    <% for (WorkArrangementItem wa : job.getWorkArrangements()) {
                           String wn = wa.getWorkName() != null && !wa.getWorkName().isEmpty() ? escHtml(wa.getWorkName()) : "&mdash;";
                           String sdRaw = wa.getResolvedSessionDuration();
                           String sd = sdRaw != null && !sdRaw.isEmpty() ? escHtml(sdRaw) : "&mdash;";
                           int occ = wa.getResolvedOccurrenceCount();
                           int wc = wa.getTaCount() > 0 ? wa.getTaCount() : 1;
                    %>
                    <tr>
                        <td><%= wn %></td>
                        <td class="pre-wrap"><%= sd %></td>
                        <td><%= occ %></td>
                        <td class="job-wa-ta-cell">
                            <label class="sr-only">TA count</label>
                            <input type="number" name="waTaCount" class="job-wa-ta-input" min="1" required value="<%= wc %>">
                        </td>
                        <td class="pre-wrap"><% if (wa.getSpecificTime() != null && !wa.getSpecificTime().isEmpty()) { %><%= escHtml(wa.getSpecificTime()) %><% } else { %><span class="muted-inline">TBD &mdash; to be arranged as needed</span><% } %></td>
                    </tr>
                    <% } %>
                    </tbody>
                </table>
                <label>Planned recruits *</label>
                <input type="number" name="plannedTaCount" min="1" required value="<%= plannedRecruits %>">
                <p class="job-wa-save-row"><button type="submit" class="btn btn-primary">Save TA counts</button></p>
            </form>
            <% } %>

            <dl class="job-detail-dl">
                <dt>Posting ID</dt><dd><code><%= escHtml(job.getId()) %></code></dd>
                <dt>Created</dt><dd><%= createdAt %></dd>
                <dt>Module code</dt><dd><%= escHtml(job.getModuleCode() != null ? job.getModuleCode() : "&mdash;") %></dd>
                <dt>Module name</dt><dd><%= moduleName %></dd>
                <dt>Hours / schedule</dt><dd class="pre-wrap"><%= wh %></dd>
                <dt>Payment</dt><dd class="pre-wrap"><%= pay %></dd>
                <dt>Required skills</dt><dd><%= job.getRequiredSkills() != null && !job.getRequiredSkills().isEmpty() ? escHtml(String.join(", ", job.getRequiredSkills())) : "&mdash;" %></dd>
                <dt>Responsibilities</dt><dd class="pre-wrap"><%= respText %></dd>
                <dt>Workload</dt><dd class="pre-wrap"><%= wl %></dd>
                <dt>Max applicants</dt><dd><%= maxAppText %></dd>
                <dt>Planned recruits</dt><dd><%= plannedRecruits %></dd>
                <dt>Auto-fill from waitlist</dt><dd><%= job.isAutoFillFromWaitlist() ? "Yes" : "No" %></dd>
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
                <dt class="job-detail-dt job-detail-dt--rich job-detail-dt--plan"><span class="job-detail-dt-inner"><span class="job-detail-dt-ico" aria-hidden="true">&#128203;</span>Multi-TA allocation plan</span></dt>
                <dd class="job-detail-dd job-detail-dd--rich">
                    <% if (taPlanChunks.isEmpty()) { %>
                    <span class="job-detail-empty">&mdash;</span>
                    <% } else if (taPlanChunks.size() == 1 && taPlanChunks.get(0)[0] == null) { %>
                    <div class="job-rich-text pre-wrap"><%= taPlanChunks.get(0)[1] %></div>
                    <% } else { %>
                    <div class="ta-plan-grid">
                        <% for (String[] row : taPlanChunks) { %>
                        <article class="ta-plan-card">
                            <% if (row[0] != null) { %><span class="ta-plan-badge"><%= row[0] %></span><% } %>
                            <p class="ta-plan-text"><%= row[1] %></p>
                        </article>
                        <% } %>
                    </div>
                    <% } %>
                </dd>
                <dt class="job-detail-dt job-detail-dt--rich job-detail-dt--schedule"><span class="job-detail-dt-inner"><span class="job-detail-dt-ico" aria-hidden="true">&#128197;</span>Interview schedule (published)</span></dt>
                <dd class="job-detail-dd job-detail-dd--rich">
                    <% if (job.getInterviewSchedule() == null || job.getInterviewSchedule().trim().isEmpty()) { %>
                    <span class="job-detail-empty">&mdash;</span>
                    <% } else { %>
                    <div class="interview-info-card interview-info-card--schedule">
                        <div class="interview-info-card-head">
                            <span class="arr-icon arr-icon-interview" aria-hidden="true">CAL</span>
                            <div class="interview-info-card-body">
                                <span class="interview-info-label">When</span>
                                <p class="interview-info-value"><%= interviewSchedule %></p>
                            </div>
                        </div>
                        <p class="interview-info-note">Applicants see this on the TA job page. Ask them to arrive early and bring ID if needed.</p>
                    </div>
                    <% } %>
                </dd>
                <dt class="job-detail-dt job-detail-dt--rich job-detail-dt--location"><span class="job-detail-dt-inner"><span class="job-detail-dt-ico" aria-hidden="true">&#128205;</span>Interview location (published)</span></dt>
                <dd class="job-detail-dd job-detail-dd--rich">
                    <% if (job.getInterviewLocation() == null || job.getInterviewLocation().trim().isEmpty()) { %>
                    <span class="job-detail-empty">&mdash;</span>
                    <% } else { %>
                    <div class="interview-info-card interview-info-card--location">
                        <div class="interview-info-card-head">
                            <span class="arr-icon arr-icon-location" aria-hidden="true">LOC</span>
                            <div class="interview-info-card-body">
                                <span class="interview-info-label">Where</span>
                                <p class="interview-info-value pre-wrap"><%= interviewLocation %></p>
                            </div>
                        </div>
                    </div>
                    <% } %>
                </dd>
                <dt>Application deadline</dt><dd><%= deadline %></dd>
                <dt>Open status</dt><dd><%= isOpen ? "Open for applications" : "Closed" %></dd>
            </dl>
            <h2>Per-TA responsibilities</h2>
            <p class="muted-inline job-wa-edit-hint">
                Recommendation uses the same balancing logic as posting page:
                planned recruits = <strong><%= plannedRecruits %></strong>,
                total estimated workload = <strong><%= String.format(Locale.US, "%.2f", quotaRec.getTotalHours()) %> h</strong>,
                average per TA = <strong><%= String.format(Locale.US, "%.2f", quotaRec.getAverageHours()) %> h</strong>,
                imbalance (max-min) = <strong><%= String.format(Locale.US, "%.2f", quotaRec.getImbalanceHours()) %> h</strong>.
                <% if (quotaRec.getUnknownDurationRows() > 0) { %>
                    <span> <%= quotaRec.getUnknownDurationRows() %> row(s) used default 1h because duration text could not be parsed.</span>
                <% } %>
            </p>
            <div class="ta-duty-board">
                <% for (WorkQuotaPlanner.TAQuota q : quotaRec.getQuotas()) {
                       StringBuilder duty = new StringBuilder();
                       for (Map.Entry<String, Integer> e : q.getWorkCounts().entrySet()) {
                           if (duty.length() > 0) duty.append("; ");
                           duty.append(escHtml(e.getKey())).append(" x ").append(e.getValue());
                       }
                       if (duty.length() == 0) {
                           duty.append("No assigned work units.");
                       }
                %>
                <article class="ta-duty-card">
                    <div class="ta-duty-head"><span class="arr-icon arr-icon-slots" aria-hidden="true">TA</span><%= escHtml(q.getName()) %></div>
                    <p><strong>Estimated load:</strong> <%= String.format(Locale.US, "%.2f", q.getTotalHours()) %> h</p>
                    <p class="pre-wrap"><%= duty.toString() %></p>
                </article>
                <% } %>
            </div>

            <section class="detail-card">
                <h2>Bookable interview slots</h2>
                <p class="muted-inline job-wa-edit-hint">Create reusable interview times here. Applicants in Interview or Waitlist can book, change, or cancel their own slot.</p>
                <% if (!moPastJobsPage) { %>
                <form action="<%= moCtx %>/mo/interview-slots" method="post" class="form">
                    <input type="hidden" name="jobId" value="<%= escHtml(job.getId()) %>">
                    <input type="hidden" name="action" value="create">
                    <label>Start time</label>
                    <input type="datetime-local" name="startsAt" required>
                    <label>Duration (minutes)</label>
                    <input type="number" name="durationMinutes" min="15" value="45" required>
                    <label>Capacity</label>
                    <input type="number" name="capacity" min="1" value="1" required>
                    <label>Location</label>
                    <input type="text" name="location" required placeholder="Room or Teams link">
                    <label>Notes</label>
                    <textarea name="notes" placeholder="Optional instructions for applicants"></textarea>
                    <button type="submit" class="btn btn-primary">Add interview slot</button>
                </form>
                <% } %>

                <% if (slotSummaries.isEmpty()) { %>
                <p class="section-empty">No bookable interview slots yet.</p>
                <% } else { %>
                <div class="table-scroll-wrap">
                    <table class="admin-table mo-cal-table">
                        <thead>
                        <tr>
                            <th>Time</th>
                            <th>Location</th>
                            <th>Capacity</th>
                            <th>Booked applicants</th>
                            <th>Notes</th>
                            <% if (!moPastJobsPage) { %><th></th><% } %>
                        </tr>
                        </thead>
                        <tbody>
                        <% for (SlotSummary summary : slotSummaries) { %>
                        <tr>
                            <td class="pre-wrap"><%= escHtml(summary.getSlot().getStartsAt()) %><br><span class="muted-inline">to <%= escHtml(summary.getSlot().getEndsAt()) %></span></td>
                            <td class="pre-wrap"><%= escHtml(summary.getSlot().getLocation()) %></td>
                            <td><%= summary.getBookedCount() %> / <%= summary.getCapacity() %></td>
                            <td>
                                <% if (summary.getBookedApplications().isEmpty()) { %>
                                <span class="muted-inline">No bookings yet</span>
                                <% } else { %>
                                <% for (bupt.ta.model.Application booked : summary.getBookedApplications()) { %>
                                <div><%= escHtml(booked.getApplicantName() != null ? booked.getApplicantName() : booked.getApplicantId()) %> (<%= escHtml(booked.getStatus()) %>)</div>
                                <% } %>
                                <% } %>
                            </td>
                            <td class="pre-wrap"><%= escHtml(summary.getSlot().getNotes() != null && !summary.getSlot().getNotes().isEmpty() ? summary.getSlot().getNotes() : "-") %></td>
                            <% if (!moPastJobsPage) { %>
                            <td>
                                <form action="<%= moCtx %>/mo/interview-slots" method="post" class="inline-form">
                                    <input type="hidden" name="jobId" value="<%= escHtml(job.getId()) %>">
                                    <input type="hidden" name="slotId" value="<%= escHtml(summary.getSlot().getId()) %>">
                                    <input type="hidden" name="action" value="delete">
                                    <button type="submit" class="btn btn-danger btn-sm" onclick="return confirm('Delete this interview slot?');">Delete</button>
                                </form>
                            </td>
                            <% } %>
                        </tr>
                        <% } %>
                        </tbody>
                    </table>
                </div>
                <% } %>
            </section>

            <div class="context-card job-detail-overview">
                <strong>Overview</strong>
                <p class="pre-wrap"><%= desc %></p>
            </div>

            <p><em>Posted as <%= escHtml(job.getPostedByName() != null ? job.getPostedByName() : "MO") %></em></p>

            <p>
                <a href="<%= manageHref %>" class="btn btn-primary">Go to applicant management</a>
                <a href="<%= moCtx %><%= moListPath %>" class="btn btn-primary">Back to list</a>
            </p>
        </main>
        <aside class="right-sidebar">
            <div class="widget-card">
                <div class="widget-title">Posting detail</div>
                <p class="widget-line">This is what applicants see on the TA job page, plus MO-only fields (limits, waitlist).</p>
                <p class="widget-line"><a href="<%= manageHref %>">Review applications</a> for this posting.</p>
            </div>
        </aside>
    </div>
</div>
</body>
</html>
