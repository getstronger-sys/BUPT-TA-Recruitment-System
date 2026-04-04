<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jspf/html-esc.jspf" %>
<%@ page import="java.util.List" %>
<%@ page import="bupt.ta.model.Application" %>
<%@ page import="bupt.ta.model.Job" %>
<%!
    /** Format stored appliedAt (ISO-like) to yyyy-MM-dd HH:mm */
    static String formatAppliedAt(String raw) {
        if (raw == null || raw.trim().isEmpty()) return "-";
        String t = raw.trim();
        int tPos = t.indexOf('T');
        if (tPos < 0) {
            return t.length() >= 16 ? t.substring(0, 16) : t;
        }
        String datePart = t.substring(0, tPos);
        String timePart = t.substring(tPos + 1);
        int dot = timePart.indexOf('.');
        if (dot >= 0) timePart = timePart.substring(0, dot);
        int plus = timePart.indexOf('+');
        if (plus >= 0) timePart = timePart.substring(0, plus);
        int z = timePart.toUpperCase().indexOf('Z');
        if (z >= 0) timePart = timePart.substring(0, z);
        String[] segs = timePart.split(":");
        if (segs.length >= 2) {
            String h = segs[0];
            String m = segs[1];
            if (h.length() == 1) h = "0" + h;
            if (m.length() >= 2) m = m.substring(0, 2);
            else if (m.length() == 1) m = "0" + m;
            return datePart + " " + h + ":" + m;
        }
        return datePart + " " + timePart;
    }
%>
<%
    List<Object[]> applications = (List<Object[]>) request.getAttribute("applications");
    if (applications == null) applications = java.util.Collections.emptyList();
    Integer pointsObj = (Integer) request.getAttribute("points");
    int points = pointsObj != null ? pointsObj : 0;
    Integer selectedObj = (Integer) request.getAttribute("selectedCount");
    int selectedCount = selectedObj != null ? selectedObj : 0;
    Integer pendingObj = (Integer) request.getAttribute("pendingCount");
    int pendingCount = pendingObj != null ? pendingObj : 0;
    Integer rejectedObj = (Integer) request.getAttribute("rejectedCount");
    int rejectedCount = rejectedObj != null ? rejectedObj : 0;
    Integer interviewObj = (Integer) request.getAttribute("interviewCount");
    int interviewCount = interviewObj != null ? interviewObj : 0;
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <%@ include file="/WEB-INF/jspf/viewport.jspf" %>
    <title>My Applications - TA Recruitment</title>
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
                <div class="icon-dot">F</div>
                <div class="icon-dot active">A</div>
                <div class="icon-dot">P</div>
            </div>
            <aside class="side-nav">
                <a href="${pageContext.request.contextPath}/ta/dashboard">Home</a>
                <a href="${pageContext.request.contextPath}/ta/jobs">Find Jobs</a>
                <a class="active" href="${pageContext.request.contextPath}/ta/applications">My Applications</a>
                <a href="${pageContext.request.contextPath}/ta/profile">My Profile</a>
            </aside>
        </div>
        <main class="main-panel">
            <h1>My Applications</h1>
            <div class="context-card">
                <strong>How it works</strong>
                <p>Pending &rarr; Interview (see time and location posted by the module organiser) &rarr; Selected or rejected. Notices are in-app only (no email).</p>
            </div>
            <% if ("1".equals(request.getParameter("success"))) { %><p class="success">Application submitted successfully!</p><% } %>
            <% if ("1".equals(request.getParameter("withdrawn"))) { %><p class="success">Application withdrawn.</p><% } %>
            <% if ("already_processed".equals(request.getParameter("error"))) { %><p class="error">Cannot withdraw - already processed.</p><% } %>
            <% if ("not_found".equals(request.getParameter("error"))) { %><p class="error">Application not found.</p><% } %>

            <div class="stats-row">
                <div class="stat-card">
                    <div class="stat-icon">T</div>
                    <div>
                        <div class="stat-title">TA Points</div>
                        <div class="stat-value"><%= points %></div>
                    </div>
                </div>
                <div class="stat-card">
                    <div>
                        <div class="stat-title">Status Overview</div>
                        <div class="stat-meta">Selected <%= selectedCount %> | Pending <%= pendingCount %> | Interview <%= interviewCount %> | Closed <%= rejectedCount %></div>
                    </div>
                </div>
            </div>

            <p class="applications-table-hint muted-inline">Swipe or drag the bar below to see all columns if the table is wide.</p>
            <div class="applications-table-scroll">
            <table class="applications-table">
                <tr>
                    <th class="col-job">Job</th>
                    <th class="col-module">Module</th>
                    <th class="col-applied">Applied at</th>
                    <th class="col-progress">Progress</th>
                    <th class="col-status">Status</th>
                    <th class="col-notice">Interview</th>
                    <th class="col-action">Action</th>
                </tr>
                <% for (Object[] row : applications) {
                    Application a = (Application) row[0];
                    Job j = (Job) row[1];
                    String statusClass = "status-pending";
                    int progress = 40;
                    if ("SELECTED".equals(a.getStatus())) { statusClass = "status-selected"; progress = 100; }
                    else if ("REJECTED".equals(a.getStatus())) { statusClass = "status-rejected"; progress = 100; }
                    else if ("WITHDRAWN".equals(a.getStatus())) { statusClass = "status-rejected"; progress = 100; }
                    else if ("INTERVIEW".equals(a.getStatus())) { statusClass = "status-pending"; progress = 75; }
                    boolean hasNotice = (a.getInterviewTime() != null && !a.getInterviewTime().isEmpty())
                            || (a.getInterviewLocation() != null && !a.getInterviewLocation().isEmpty())
                            || (a.getInterviewAssessment() != null && !a.getInterviewAssessment().isEmpty());
                    String noticeTplId = "ta-notice-" + a.getId().replaceAll("[^A-Za-z0-9]", "_");
                    String jobTitle = j != null ? j.getTitle() : a.getJobId();
                    String timeN = a.getInterviewTime() != null ? a.getInterviewTime() : "";
                    String locN = a.getInterviewLocation() != null ? a.getInterviewLocation() : "";
                    String assessN = a.getInterviewAssessment() != null ? a.getInterviewAssessment() : "";
                %>
                <tr>
                    <td class="col-job"><%= escHtml(jobTitle) %></td>
                    <td class="col-module"><%= j != null ? escHtml(j.getModuleCode()) : "-" %></td>
                    <td class="col-applied"><%= formatAppliedAt(a.getAppliedAt()) %></td>
                    <td class="col-progress">
                        <div class="progress-wrap">
                            <div class="progress-bar" style="width:<%= progress %>%"></div>
                        </div>
                        <div class="progress-text"><%= progress %>%</div>
                    </td>
                    <td class="col-status <%= statusClass %>"><%= a.getStatus() %></td>
                    <td class="col-notice interview-notice-cell">
                        <% if (hasNotice) { %>
                        <button type="button" class="btn btn-primary btn-sm ta-notice-btn" data-template="<%= noticeTplId %>">View notice</button>
                        <template id="<%= noticeTplId %>">
                            <div class="quick-detail-sheet ta-notice-sheet">
                                <p class="quick-detail-name"><%= escHtml(jobTitle) %></p>
                                <p><strong>Time:</strong> <%= escHtml(timeN.isEmpty() ? "—" : timeN) %></p>
                                <p><strong>Location:</strong> <%= escHtml(locN.isEmpty() ? "—" : locN) %></p>
                                <% if (!assessN.isEmpty()) { %>
                                <div class="detail-block-text">
                                    <strong>Assessment</strong>
                                    <p class="pre-wrap"><%= escHtml(assessN) %></p>
                                </div>
                                <% } %>
                            </div>
                        </template>
                        <% } else if ("INTERVIEW".equals(a.getStatus())) { %>
                        <span class="muted-inline">Awaiting organiser</span>
                        <% } else { %>
                        <span class="muted-inline">&mdash;</span>
                        <% } %>
                    </td>
                    <td class="col-action">
                        <% if ("PENDING".equals(a.getStatus()) || "INTERVIEW".equals(a.getStatus())) { %>
                        <form action="${pageContext.request.contextPath}/ta/withdraw" method="post" style="display:inline;">
                            <input type="hidden" name="applicationId" value="<%= a.getId() %>">
                            <button type="submit" class="btn btn-danger btn-sm" onclick="return confirm('Withdraw this application?')">Withdraw</button>
                        </form>
                        <% } %>
                    </td>
                </tr>
                <% }
                   if (applications.isEmpty()) { %>
                <tr><td colspan="7">No applications yet. <a href="${pageContext.request.contextPath}/ta/jobs">Find jobs</a> to apply.</td></tr>
                <% } %>
            </table>
            </div>
        </main>
        <aside class="right-sidebar">
            <div class="widget-card">
                <div class="widget-title">TA Points</div>
                <p class="widget-line">Current: <%= points %></p>
                <p class="widget-line">Selected: <%= selectedCount %> | Pending: <%= pendingCount %> | Interview: <%= interviewCount %></p>
            </div>
            <div class="widget-card">
                <div class="widget-title">Reminders</div>
                <p class="widget-line">Pending applications can be withdrawn.</p>
                <p class="widget-line">Processed records are archived.</p>
            </div>
        </aside>
    </div>
</div>

<dialog id="taNoticeDialog" class="applicant-quick-dialog">
    <div class="applicant-quick-dialog-inner">
        <div class="applicant-quick-dialog-head">
            <h3>Interview notice</h3>
            <button type="button" class="dialog-close-btn" aria-label="Close">&times;</button>
        </div>
        <div class="applicant-quick-dialog-body ta-notice-dialog-body"></div>
    </div>
</dialog>
<script>
(function () {
    var dialog = document.getElementById('taNoticeDialog');
    if (!dialog) return;
    var body = dialog.querySelector('.ta-notice-dialog-body');
    var closeBtn = dialog.querySelector('.dialog-close-btn');
    if (closeBtn) closeBtn.addEventListener('click', function () { dialog.close(); });
    dialog.addEventListener('click', function (e) { if (e.target === dialog) dialog.close(); });
    document.querySelectorAll('.ta-notice-btn').forEach(function (btn) {
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
