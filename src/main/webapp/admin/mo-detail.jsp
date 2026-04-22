<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jspf/html-esc.jspf" %>
<%@ page import="java.util.List" %>
<%@ page import="bupt.ta.model.Application" %>
<%@ page import="bupt.ta.model.Job" %>
<%@ page import="bupt.ta.model.User" %>
<%@ page import="bupt.ta.model.WorkArrangementItem" %>
<%@ page import="bupt.ta.model.AssignedModule" %>
<%@ page import="bupt.ta.service.AdminService" %>
<%
    AdminService.MODetailReport report = (AdminService.MODetailReport) request.getAttribute("report");
    if (report == null) {
        response.sendRedirect(request.getContextPath() + "/admin/users?error=invalid_mo");
        return;
    }
    User user = report.getUser();
    String displayName = user.getRealName() != null && !user.getRealName().trim().isEmpty() ? user.getRealName().trim()
            : (user.getUsername() != null && !user.getUsername().trim().isEmpty() ? user.getUsername().trim() : user.getId());
    List<AssignedModule> assignedModules = (List<AssignedModule>) request.getAttribute("assignedModules");
    if (assignedModules == null) assignedModules = java.util.Collections.emptyList();
    StringBuilder assignedModulesText = new StringBuilder();
    for (AssignedModule m : assignedModules) {
        if (m == null || m.getModuleCode() == null || m.getModuleCode().trim().isEmpty()) continue;
        if (assignedModulesText.length() > 0) assignedModulesText.append("\n");
        assignedModulesText.append(m.getModuleCode().trim().toUpperCase());
        if (m.getModuleName() != null && !m.getModuleName().trim().isEmpty()) {
            assignedModulesText.append(" | ").append(m.getModuleName().trim());
        }
    }
    String assignError = (String) request.getAttribute("assignError");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <%@ include file="/WEB-INF/jspf/viewport.jspf" %>
    <title><%= escHtml(displayName) %> - Admin MO Detail</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="container">
    <div class="nav top-nav">
        <span class="brand">BUPT Teaching Assistant Recruitment System</span>
        <div class="user user-inline-actions"><span><%= session.getAttribute("realName") %> |</span><form action="${pageContext.request.contextPath}/logout" method="post" class="inline-form logout-form"><%@ include file="/WEB-INF/jspf/csrf-hidden.jspf" %><button type="submit" class="logout-button">Logout</button></form></div>
    </div>
    <div class="page-layout">
        <div class="left-nav-wrap">
            <div class="icon-rail">
                <div class="icon-dot">D</div>
                <div class="icon-dot">W</div>
                <div class="icon-dot">M</div>
                <div class="icon-dot active">U</div>
            </div>
            <aside class="side-nav">
                <a href="${pageContext.request.contextPath}/admin/dashboard">Summary</a>
                <a href="${pageContext.request.contextPath}/admin/workload">Workload</a>
                <a href="${pageContext.request.contextPath}/admin/monitoring">Monitoring</a>
                <a class="active" href="${pageContext.request.contextPath}/admin/users">Users</a>
            </aside>
        </div>
        <main class="main-panel admin-main">
            <p class="breadcrumb-line"><a href="${pageContext.request.contextPath}/admin/users">&larr; Back to user directory</a></p>
            <h1>MO Detail: <%= escHtml(displayName) %></h1>
            <p class="ta-page-lead">Read-only history for one module organiser, including account record, posting content, workload risks, and all applications received by this MO.</p>
            <% if ("1".equals(request.getParameter("assignedUpdated"))) { %>
            <p class="success">Assigned modules updated successfully.</p>
            <% } %>
            <% if (assignError != null && !assignError.trim().isEmpty()) { %>
            <p class="error"><%= escHtml(assignError) %></p>
            <% } %>
            <div class="context-card">
                <strong>Admin governance view</strong>
                <p>This page is mainly read-only for traceability (posting history, application counts, risk flags, and statuses), while <strong>Assigned modules for this term</strong> can be updated below.</p>
            </div>

            <div class="stats-row admin-stat-grid">
                <div class="stat-card">
                    <div class="stat-icon">J</div>
                    <div>
                        <div class="stat-title">Posted jobs</div>
                        <div class="stat-value"><%= report.getTotalJobs() %></div>
                        <div class="stat-meta">Active <%= report.getActiveJobs() %> | Inactive <%= report.getInactiveJobs() %></div>
                    </div>
                </div>
                <div class="stat-card">
                    <div class="stat-icon">A</div>
                    <div>
                        <div class="stat-title">Applications received</div>
                        <div class="stat-value"><%= report.getTotalApplications() %></div>
                        <div class="stat-meta">Distinct applicants <%= report.getDistinctApplicants() %></div>
                    </div>
                </div>
                <div class="stat-card">
                    <div class="stat-icon">S</div>
                    <div>
                        <div class="stat-title">Decision distribution</div>
                        <div class="stat-value"><%= report.getSelectedCount() %></div>
                        <div class="stat-meta">Interview <%= report.getInterviewCount() %> | Waitlist <%= report.getWaitlistCount() %> | Rejected <%= report.getRejectedCount() %></div>
                    </div>
                </div>
                <div class="stat-card">
                    <div class="stat-icon">R</div>
                    <div>
                        <div class="stat-title">Risk flags</div>
                        <div class="stat-value"><%= report.getCapacityRiskCount() + report.getInactiveActiveRiskCount() %></div>
                        <div class="stat-meta">Capacity <%= report.getCapacityRiskCount() %> | Inactive-active <%= report.getInactiveActiveRiskCount() %></div>
                    </div>
                </div>
            </div>

            <div class="detail-grid admin-detail-grid">
                <section class="detail-card">
                    <h3>Account record</h3>
                    <dl class="admin-kv-list">
                        <dt>User ID</dt><dd><code><%= escHtml(user.getId()) %></code></dd>
                        <dt>Role</dt><dd>MO</dd>
                        <dt>Username</dt><dd><%= escHtml(user.getUsername() != null ? user.getUsername() : "-") %></dd>
                        <dt>Real name</dt><dd><%= escHtml(user.getRealName() != null && !user.getRealName().isEmpty() ? user.getRealName() : "-") %></dd>
                        <dt>Email</dt><dd><%= escHtml(user.getEmail() != null && !user.getEmail().isEmpty() ? user.getEmail() : "-") %></dd>
                    </dl>
                </section>

                <section class="detail-card">
                    <h3>Recruitment summary</h3>
                    <div class="admin-chip-grid">
                        <span class="admin-chip">Pending: <strong><%= report.getPendingCount() %></strong></span>
                        <span class="admin-chip">Interview: <strong><%= report.getInterviewCount() %></strong></span>
                        <span class="admin-chip">Waitlist: <strong><%= report.getWaitlistCount() %></strong></span>
                        <span class="admin-chip">Selected: <strong><%= report.getSelectedCount() %></strong></span>
                        <span class="admin-chip">Rejected: <strong><%= report.getRejectedCount() %></strong></span>
                        <span class="admin-chip">Withdrawn: <strong><%= report.getWithdrawnCount() %></strong></span>
                        <span class="admin-chip">Auto-closed: <strong><%= report.getAutoClosedCount() %></strong></span>
                    </div>
                </section>
            </div>

            <section class="detail-card">
                <h3>Assigned modules for this term</h3>
                <p class="muted-inline">MO can only post jobs for these module codes. One line per module, format: <code>MODULE_CODE | Module Name</code>.</p>
                <form action="${pageContext.request.contextPath}/admin/mo-detail" method="post" class="form">
                    <%@ include file="/WEB-INF/jspf/csrf-hidden.jspf" %>
                    <input type="hidden" name="userId" value="<%= escHtml(user.getId()) %>">
                    <label>Assigned modules list</label>
                    <textarea name="assignedModulesText" rows="6" placeholder="EBU6304 | Software Engineering&#10;EBU6202 | Data Structures and Algorithms"><%= escHtml(assignedModulesText.toString()) %></textarea>
                    <button type="submit" class="btn btn-primary">Save assigned modules</button>
                </form>
            </section>

            <section class="detail-card">
                <h3>Posted jobs summary</h3>
                <% if (report.getJobRows().isEmpty()) { %>
                <p class="section-empty section-empty--card">This MO has not posted any jobs.</p>
                <% } else { %>
                <div class="table-scroll-wrap">
                    <table class="admin-table">
                        <thead>
                        <tr>
                            <th>Job</th>
                            <th>Status</th>
                            <th>Deadline</th>
                            <th>Applications</th>
                            <th>Selection</th>
                            <th>Capacity</th>
                            <th>Risk flags</th>
                        </tr>
                        </thead>
                        <tbody>
                        <% for (AdminService.MOJobDetailRow row : report.getJobRows()) {
                               Job job = row.getJob();
                        %>
                        <tr>
                            <td>
                                <strong><%= escHtml(job.getTitle() != null ? job.getTitle() : job.getId()) %></strong>
                                <div class="admin-row-subtext"><%= escHtml(job.getModuleCode() != null ? job.getModuleCode() : "-") %> | Job ID: <code><%= escHtml(job.getId()) %></code></div>
                            </td>
                            <td><span class="status-pill <%= "OPEN".equalsIgnoreCase(job.getStatus()) ? "status-pill-pending" : "status-pill-rejected" %>"><%= escHtml(job.getStatus() != null ? job.getStatus() : "-") %></span></td>
                            <td><%= escHtml(job.getDeadline() != null && !job.getDeadline().isEmpty() ? job.getDeadline() : "-") %></td>
                            <td><strong><%= row.getTotalApplications() %></strong><div class="admin-row-subtext">Pending <%= row.getPendingCount() %> | Interview <%= row.getInterviewCount() %> | Waitlist <%= row.getWaitlistCount() %></div></td>
                            <td><strong><%= row.getSelectedCount() %></strong><div class="admin-row-subtext">Rejected <%= row.getRejectedCount() %> | Withdrawn <%= row.getWithdrawnCount() %> | Auto-closed <%= row.getAutoClosedCount() %></div></td>
                            <td>
                                <% if (job.getMaxApplicants() > 0) { %>
                                <%= row.getSelectedCount() %> / <%= job.getMaxApplicants() %>
                                <% } else { %>
                                No limit
                                <% } %>
                            </td>
                            <td>
                                <div class="admin-summary-stack">
                                    <span class="<%= row.isAtOrOverCapacity() ? "balance-warn" : "balance-ok" %>"><%= row.isAtOrOverCapacity() ? "At or over capacity" : "Within capacity" %></span>
                                    <% if (row.isInactiveWithActiveApplications()) { %>
                                    <span class="balance-warn">Inactive job still has active applications</span>
                                    <% } %>
                                    <% if (row.isOverCapacity()) { %>
                                    <span class="balance-warn">Selected count exceeds max applicants</span>
                                    <% } %>
                                </div>
                            </td>
                        </tr>
                        <% } %>
                        </tbody>
                    </table>
                </div>
                <% } %>
            </section>

            <section class="detail-card">
                <h3>Posting details</h3>
                <% if (report.getJobRows().isEmpty()) { %>
                <p class="section-empty section-empty--card">No posting detail is available because this MO has no jobs.</p>
                <% } else { %>
                <div class="admin-posting-stack">
                    <% for (AdminService.MOJobDetailRow row : report.getJobRows()) {
                           Job job = row.getJob();
                    %>
                    <article class="detail-card admin-posting-card">
                        <div class="admin-posting-head">
                            <div>
                                <h3><%= escHtml(job.getTitle() != null ? job.getTitle() : job.getId()) %></h3>
                                <p class="muted-inline"><%= escHtml(job.getModuleCode() != null ? job.getModuleCode() : "-") %> | Created <%= escHtml(job.getCreatedAt() != null && !job.getCreatedAt().isEmpty() ? job.getCreatedAt() : "-") %></p>
                            </div>
                            <span class="status-pill <%= "OPEN".equalsIgnoreCase(job.getStatus()) ? "status-pill-pending" : "status-pill-rejected" %>"><%= escHtml(job.getStatus() != null ? job.getStatus() : "-") %></span>
                        </div>
                        <div class="detail-grid">
                            <div class="detail-card">
                                <h3>Core fields</h3>
                                <dl class="admin-kv-list">
                                    <dt>Job ID</dt><dd><code><%= escHtml(job.getId()) %></code></dd>
                                    <dt>Job type</dt><dd><%= escHtml(job.getJobType() != null && !job.getJobType().isEmpty() ? job.getJobType() : "-") %></dd>
                                    <dt>Working hours</dt><dd class="pre-wrap"><%= escHtml(job.getWorkingHours() != null && !job.getWorkingHours().isEmpty() ? job.getWorkingHours() : "-") %></dd>
                                    <dt>Workload</dt><dd class="pre-wrap"><%= escHtml(job.getWorkload() != null && !job.getWorkload().isEmpty() ? job.getWorkload() : "-") %></dd>
                                    <dt>Payment</dt><dd class="pre-wrap"><%= escHtml(job.getPayment() != null && !job.getPayment().isEmpty() ? job.getPayment() : "-") %></dd>
                                    <dt>Deadline</dt><dd><%= escHtml(job.getDeadline() != null && !job.getDeadline().isEmpty() ? job.getDeadline() : "-") %></dd>
                                    <dt>Max applicants</dt><dd><%= job.getMaxApplicants() > 0 ? job.getMaxApplicants() : 0 %></dd>
                                    <dt>Planned recruits</dt><dd><%= job.getTaSlots() > 0 ? job.getTaSlots() : 0 %></dd>
                                </dl>
                            </div>
                            <div class="detail-card">
                                <h3>Recruitment planning</h3>
                                <dl class="admin-kv-list">
                                    <dt>Required skills</dt><dd><%= job.getRequiredSkills() != null && !job.getRequiredSkills().isEmpty() ? escHtml(String.join(", ", job.getRequiredSkills())) : "-" %></dd>
                                    <dt>Estimated interview time</dt><dd class="pre-wrap"><%= escHtml(job.getInterviewSchedule() != null && !job.getInterviewSchedule().isEmpty() ? job.getInterviewSchedule() : "-") %></dd>
                                    <dt>Estimated interview location</dt><dd class="pre-wrap"><%= escHtml(job.getInterviewLocation() != null && !job.getInterviewLocation().isEmpty() ? job.getInterviewLocation() : "-") %></dd>
                                    <dt>Auto-fill from waitlist</dt><dd><%= job.isAutoFillFromWaitlist() ? "Yes" : "No" %></dd>
                                    <dt>Application summary</dt><dd>Applications <strong><%= row.getTotalApplications() %></strong> | Selected <strong><%= row.getSelectedCount() %></strong> | Waitlist <strong><%= row.getWaitlistCount() %></strong></dd>
                                </dl>
                            </div>
                        </div>
                        <div class="admin-text-block">
                            <strong>Description</strong>
                            <p class="pre-wrap"><%= escHtml(job.getDescription() != null && !job.getDescription().isEmpty() ? job.getDescription() : "Not provided.") %></p>
                        </div>
                        <div class="admin-text-block">
                            <strong>Responsibilities</strong>
                            <p class="pre-wrap"><%= escHtml(job.getResponsibilities() != null && !job.getResponsibilities().isEmpty() ? job.getResponsibilities() : "Not provided.") %></p>
                        </div>
                        <div class="admin-text-block">
                            <strong>Course timeline</strong>
                            <p class="pre-wrap"><%= escHtml(job.getExamTimeline() != null && !job.getExamTimeline().isEmpty() ? job.getExamTimeline() : "Not provided.") %></p>
                        </div>
                        <div class="admin-text-block">
                            <strong>Multi-TA allocation plan</strong>
                            <p class="pre-wrap"><%= escHtml(job.getTaAllocationPlan() != null && !job.getTaAllocationPlan().isEmpty() ? job.getTaAllocationPlan() : "Not provided.") %></p>
                        </div>
                        <div class="admin-text-block">
                            <strong>Work arrangements</strong>
                            <% if (job.getWorkArrangements() == null || job.getWorkArrangements().isEmpty()) { %>
                            <p>No structured work arrangements were provided.</p>
                            <% } else { %>
                            <div class="table-scroll-wrap">
                                <table class="admin-table admin-table--compact">
                                    <thead>
                                    <tr>
                                        <th>Work item</th>
                                        <th>Per-session duration</th>
                                        <th>Occurrences</th>
                                        <th>TA count</th>
                                        <th>Specific time</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <% for (WorkArrangementItem item : job.getWorkArrangements()) { %>
                                    <tr>
                                        <td><%= escHtml(item.getWorkName() != null && !item.getWorkName().isEmpty() ? item.getWorkName() : "-") %></td>
                                        <td class="pre-wrap"><%= escHtml(item.getResolvedSessionDuration() != null && !item.getResolvedSessionDuration().isEmpty() ? item.getResolvedSessionDuration() : "-") %></td>
                                        <td><%= item.getResolvedOccurrenceCount() %></td>
                                        <td><%= item.getTaCount() > 0 ? item.getTaCount() : 0 %></td>
                                        <td class="pre-wrap"><%= escHtml(item.getSpecificTime() != null && !item.getSpecificTime().isEmpty() ? item.getSpecificTime() : "-") %></td>
                                    </tr>
                                    <% } %>
                                    </tbody>
                                </table>
                            </div>
                            <% } %>
                        </div>
                    </article>
                    <% } %>
                </div>
                <% } %>
            </section>

            <section class="detail-card">
                <h3>Application history across MO postings</h3>
                <% if (report.getApplicationRows().isEmpty()) { %>
                <p class="section-empty section-empty--card">No applications have been submitted to this MO's jobs.</p>
                <% } else { %>
                <div class="table-scroll-wrap">
                    <table class="admin-table">
                        <thead>
                        <tr>
                            <th>Application</th>
                            <th>Applicant</th>
                            <th>Job</th>
                            <th>Applied</th>
                            <th>Preferred role</th>
                            <th>Status</th>
                            <th>Interview record</th>
                            <th>Notes</th>
                        </tr>
                        </thead>
                        <tbody>
                        <% for (AdminService.AdminApplicationRow row : report.getApplicationRows()) {
                               Application app = row.getApplication();
                               Job job = row.getJob();
                               String status = app.getStatus() != null ? app.getStatus().toUpperCase() : "UNKNOWN";
                               String statusClass = "status-pill-pending";
                               if ("SELECTED".equals(status)) statusClass = "status-pill-selected";
                               else if ("REJECTED".equals(status) || "WITHDRAWN".equals(status) || AdminService.STATUS_AUTO_CLOSED.equals(status)) statusClass = "status-pill-rejected";
                               else if ("INTERVIEW".equals(status)) statusClass = "status-pill-interview";
                        %>
                        <tr>
                            <td><code><%= escHtml(app.getId()) %></code></td>
                            <td>
                                <a href="${pageContext.request.contextPath}/admin/ta-detail?userId=<%= app.getApplicantId() %>" class="admin-inline-link"><%= escHtml(app.getApplicantName() != null && !app.getApplicantName().isEmpty() ? app.getApplicantName() : app.getApplicantId()) %></a>
                                <div class="admin-row-subtext">Applicant ID: <code><%= escHtml(app.getApplicantId()) %></code></div>
                            </td>
                            <td>
                                <strong><%= escHtml(job != null && job.getTitle() != null ? job.getTitle() : app.getJobId()) %></strong>
                                <div class="admin-row-subtext"><%= escHtml(job != null && job.getModuleCode() != null ? job.getModuleCode() : "-") %></div>
                            </td>
                            <td><%= escHtml(app.getAppliedAt() != null && !app.getAppliedAt().isEmpty() ? app.getAppliedAt() : "-") %></td>
                            <td><%= escHtml(app.getPreferredRole() != null && !app.getPreferredRole().isEmpty() ? app.getPreferredRole() : "-") %></td>
                            <td><span class="status-pill <%= statusClass %>"><%= escHtml(status) %></span></td>
                            <td>
                                <div class="admin-summary-stack">
                                    <span>Time: <%= escHtml(app.getInterviewTime() != null && !app.getInterviewTime().isEmpty() ? app.getInterviewTime() : "-") %></span>
                                    <span>Location: <%= escHtml(app.getInterviewLocation() != null && !app.getInterviewLocation().isEmpty() ? app.getInterviewLocation() : "-") %></span>
                                    <span>Assessment: <%= escHtml(app.getInterviewAssessment() != null && !app.getInterviewAssessment().isEmpty() ? app.getInterviewAssessment() : "-") %></span>
                                </div>
                            </td>
                            <td class="pre-wrap"><%= escHtml(app.getNotes() != null && !app.getNotes().isEmpty() ? app.getNotes() : "-") %></td>
                        </tr>
                        <% } %>
                        </tbody>
                    </table>
                </div>
                <% } %>
            </section>
        </main>
        <aside class="right-sidebar">
            <div class="widget-card">
                <div class="widget-title">MO Snapshot</div>
                <p class="widget-line">Jobs: <%= report.getTotalJobs() %> | Active: <%= report.getActiveJobs() %></p>
                <p class="widget-line">Pending: <%= report.getPendingCount() %> | Interview: <%= report.getInterviewCount() %></p>
                <p class="widget-line">Selected: <%= report.getSelectedCount() %> | Waitlist: <%= report.getWaitlistCount() %></p>
            </div>
            <div class="widget-card">
                <div class="widget-title">Quick Links</div>
                <p class="widget-line"><a href="${pageContext.request.contextPath}/admin/users">Back to user directory</a></p>
                <p class="widget-line"><a href="${pageContext.request.contextPath}/admin/monitoring">Open monitoring</a></p>
                <p class="widget-line"><a href="${pageContext.request.contextPath}/admin/workload">Review TA workload</a></p>
            </div>
        </aside>
    </div>
</div>
</body>
</html>
