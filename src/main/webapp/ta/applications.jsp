<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="bupt.ta.model.Application" %>
<%@ page import="bupt.ta.model.Job" %>
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
                <a href="${pageContext.request.contextPath}/ta/jobs">Find Jobs</a>
                <a class="active" href="${pageContext.request.contextPath}/ta/applications">My Applications</a>
                <a href="${pageContext.request.contextPath}/ta/profile">My Profile</a>
            </aside>
        </div>
        <main class="main-panel">
            <h1>My Applications</h1>
            <div class="context-card">
                <strong>流程说明</strong>
                <p>申请中 → 面试中（可查看 MO 发布的面试时间与地点）→ 录用或拒绝。站内通知，不发邮件。</p>
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
                        <div class="stat-meta">录用 <%= selectedCount %> | 申请中 <%= pendingCount %> | 面试中 <%= interviewCount %> | 已结束 <%= rejectedCount %></div>
                    </div>
                </div>
            </div>

            <table>
                <tr>
                    <th>Job</th>
                    <th>Module</th>
                    <th>Applied At</th>
                    <th>Progress</th>
                    <th>Status</th>
                    <th>面试通知（站内）</th>
                    <th>Action</th>
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
                %>
                <tr>
                    <td><%= j != null ? j.getTitle() : a.getJobId() %></td>
                    <td><%= j != null ? j.getModuleCode() : "-" %></td>
                    <td><%= a.getAppliedAt() != null ? a.getAppliedAt() : "-" %></td>
                    <td>
                        <div class="progress-wrap">
                            <div class="progress-bar" style="width:<%= progress %>%"></div>
                        </div>
                        <div class="progress-text"><%= progress %>%</div>
                    </td>
                    <td class="<%= statusClass %>"><%= a.getStatus() %></td>
                    <td class="interview-notice-cell">
                        <% if (hasNotice) { %>
                        <div class="notice-inline">
                            <span>时间 <%= a.getInterviewTime() != null ? a.getInterviewTime() : "—" %></span><br>
                            <span>地点 <%= a.getInterviewLocation() != null ? a.getInterviewLocation() : "—" %></span>
                            <% if (a.getInterviewAssessment() != null && !a.getInterviewAssessment().isEmpty()) { %>
                            <br><span>考核 <%= a.getInterviewAssessment() %></span>
                            <% } %>
                        </div>
                        <% } else if ("INTERVIEW".equals(a.getStatus())) { %>
                        <span class="muted-inline">待 MO 发布</span>
                        <% } else { %>
                        —
                        <% } %>
                    </td>
                    <td>
                        <% if ("PENDING".equals(a.getStatus()) || "INTERVIEW".equals(a.getStatus())) { %>
                        <form action="${pageContext.request.contextPath}/ta/withdraw" method="post" style="display:inline;">
                            <input type="hidden" name="applicationId" value="<%= a.getId() %>">
                            <button type="submit" class="btn btn-danger" onclick="return confirm('Withdraw this application?')">Withdraw</button>
                        </form>
                        <% } %>
                    </td>
                </tr>
                <% }
                   if (applications.isEmpty()) { %>
                <tr><td colspan="7">No applications yet. <a href="${pageContext.request.contextPath}/ta/jobs">Find jobs</a> to apply.</td></tr>
                <% } %>
            </table>
        </main>
        <aside class="right-sidebar">
            <div class="widget-card">
                <div class="widget-title">TA Points</div>
                <p class="widget-line">Current: <%= points %></p>
                <p class="widget-line">录用: <%= selectedCount %> | 申请中: <%= pendingCount %> | 面试: <%= interviewCount %></p>
            </div>
            <div class="widget-card">
                <div class="widget-title">Reminders</div>
                <p class="widget-line">Pending applications can be withdrawn.</p>
                <p class="widget-line">Processed records are archived.</p>
            </div>
        </aside>
    </div>
</div>
</body>
</html>
