<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="bupt.ta.model.Application" %>
<%@ page import="bupt.ta.model.Job" %>
<% List<Object[]> applications = (List<Object[]>) request.getAttribute("applications"); if (applications == null) applications = java.util.Collections.emptyList(); %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>My Applications - TA Recruitment</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="container">
    <div class="nav">
        <a href="${pageContext.request.contextPath}/ta/jobs">Find Jobs</a>
        <a href="${pageContext.request.contextPath}/ta/applications">My Applications</a>
        <a href="${pageContext.request.contextPath}/ta/profile">My Profile</a>
        <span class="user"><%= session.getAttribute("realName") %> | <a href="${pageContext.request.contextPath}/logout">Logout</a></span>
    </div>
    <h1>My Applications</h1>
    <% if ("1".equals(request.getParameter("success"))) { %><p class="success">Application submitted successfully!</p><% } %>
    <% if ("1".equals(request.getParameter("withdrawn"))) { %><p class="success">Application withdrawn.</p><% } %>
    <% if ("already_processed".equals(request.getParameter("error"))) { %><p class="error">Cannot withdraw - already processed.</p><% } %>
    <% if ("not_found".equals(request.getParameter("error"))) { %><p class="error">Application not found.</p><% } %>

    <table>
        <tr>
            <th>Job</th>
            <th>Module</th>
            <th>Applied At</th>
            <th>Status</th>
            <th>Action</th>
        </tr>
        <% for (Object[] row : applications) {
            Application a = (Application) row[0];
            Job j = (Job) row[1];
            String statusClass = "status-pending";
            if ("SELECTED".equals(a.getStatus())) statusClass = "status-selected";
            else if ("REJECTED".equals(a.getStatus())) statusClass = "status-rejected";
            else if ("WITHDRAWN".equals(a.getStatus())) statusClass = "status-rejected";
        %>
        <tr>
            <td><%= j != null ? j.getTitle() : a.getJobId() %></td>
            <td><%= j != null ? j.getModuleCode() : "-" %></td>
            <td><%= a.getAppliedAt() != null ? a.getAppliedAt() : "-" %></td>
            <td class="<%= statusClass %>"><%= a.getStatus() %></td>
            <td>
                <% if ("PENDING".equals(a.getStatus())) { %>
                <form action="${pageContext.request.contextPath}/ta/withdraw" method="post" style="display:inline;">
                    <input type="hidden" name="applicationId" value="<%= a.getId() %>">
                    <button type="submit" class="btn btn-danger" onclick="return confirm('Withdraw this application?')">Withdraw</button>
                </form>
                <% } %>
            </td>
        </tr>
        <% }
           if (applications.isEmpty()) { %>
        <tr><td colspan="5">No applications yet. <a href="${pageContext.request.contextPath}/ta/jobs">Find jobs</a> to apply.</td></tr>
        <% } %>
    </table>
</div>
</body>
</html>
