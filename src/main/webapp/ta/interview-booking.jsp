<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jspf/html-esc.jspf" %>
<%@ page import="bupt.ta.model.Application" %>
<%@ page import="bupt.ta.model.Job" %>
<%@ page import="bupt.ta.service.InterviewBookingService.SlotSummary" %>
<%@ page import="bupt.ta.util.InterviewCalendarSupport" %>
<%@ page import="java.time.LocalDateTime" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.util.List" %>
<%
    Application application = (Application) request.getAttribute("application");
    Job job = (Job) request.getAttribute("job");
    @SuppressWarnings("unchecked")
    List<SlotSummary> slotSummaries = (List<SlotSummary>) request.getAttribute("slotSummaries");
    if (slotSummaries == null) slotSummaries = java.util.Collections.emptyList();
    String ctx = request.getContextPath();
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <%@ include file="/WEB-INF/jspf/viewport.jspf" %>
    <title>Book interview - TA Recruitment</title>
    <link rel="stylesheet" href="<%= ctx %>/css/style.css">
</head>
<body>
<div class="container">
    <div class="nav top-nav">
        <span class="brand">BUPT Teaching Assistant Recruitment System</span>
        <span class="user"><%= session.getAttribute("realName") %> | <a href="<%= ctx %>/logout">Logout</a></span>
    </div>
    <div class="page-layout">
        <div class="left-nav-wrap">
            <div class="icon-rail">
                <div class="icon-dot">F</div>
                <div class="icon-dot active">A</div>
                <div class="icon-dot">P</div>
            </div>
            <%@ include file="/WEB-INF/jspf/ta-side-nav.jspf" %>
        </div>
        <main class="main-panel ta-main">
            <p class="breadcrumb-line"><a href="<%= ctx %>/ta/applications">&larr; Back to My Applications</a></p>
            <h1>Book interview slot</h1>
            <div class="context-card">
                <strong><%= escHtml(job != null ? job.getTitle() : application.getJobId()) %></strong>
                <p>Choose one available interview slot below. You can change or cancel your booking before the interview.</p>
            </div>
            <% if ("1".equals(request.getParameter("saved"))) { %><p class="success">Interview booking updated.</p><% } %>
            <% if (request.getParameter("error") != null) { %><p class="error"><%= escHtml(request.getParameter("error")) %></p><% } %>

            <% if (application.getInterviewSlotId() != null && !application.getInterviewSlotId().isEmpty()) { %>
            <section class="detail-card">
                <h2>Current booking</h2>
                <p><strong>Time:</strong> <%= escHtml(application.getInterviewTime() != null ? application.getInterviewTime() : "-") %></p>
                <p><strong>Location:</strong> <%= escHtml(application.getInterviewLocation() != null ? application.getInterviewLocation() : "-") %></p>
                <form action="<%= ctx %>/ta/interview-booking" method="post" class="inline-form">
                    <input type="hidden" name="applicationId" value="<%= escHtml(application.getId()) %>">
                    <input type="hidden" name="action" value="cancel">
                    <button type="submit" class="btn btn-danger btn-sm" onclick="return confirm('Cancel this interview booking?');">Cancel booking</button>
                </form>
                <% if (application.getInterviewTime() != null && !application.getInterviewTime().trim().isEmpty()) { %>
                <span class="muted-inline"> | </span><a href="<%= ctx %>/ta/interview-calendar?applicationId=<%= escHtml(application.getId()) %>">Download .ics</a>
                <% } %>
            </section>
            <% } %>

            <% if (slotSummaries.isEmpty()) { %>
            <p class="section-empty section-empty--card">No interview slots are available yet. Please check back later or contact the module organiser.</p>
            <% } else { %>
            <section class="detail-card">
                <h2>Available slots</h2>
                <div class="ta-duty-board">
                    <% for (SlotSummary summary : slotSummaries) {
                           LocalDateTime start = InterviewCalendarSupport.parseInterviewTime(summary.getSlot().getStartsAt());
                           LocalDateTime end = InterviewCalendarSupport.parseInterviewTime(summary.getSlot().getEndsAt());
                           boolean bookedHere = summary.getSlot().getId().equals(application.getInterviewSlotId());
                    %>
                    <article class="ta-duty-card">
                        <div class="ta-duty-head"><span class="arr-icon arr-icon-interview" aria-hidden="true">CAL</span>
                            <%= start != null ? escHtml(start.format(fmt)) : escHtml(summary.getSlot().getStartsAt()) %>
                            <% if (end != null) { %><span class="muted-inline"> to <%= escHtml(end.format(fmt)) %></span><% } %>
                        </div>
                        <p><strong>Location:</strong> <%= escHtml(summary.getSlot().getLocation()) %></p>
                        <p><strong>Capacity:</strong> <%= summary.getBookedCount() %> / <%= summary.getCapacity() %> booked</p>
                        <% if (summary.getSlot().getNotes() != null && !summary.getSlot().getNotes().trim().isEmpty()) { %>
                        <p class="pre-wrap"><%= escHtml(summary.getSlot().getNotes()) %></p>
                        <% } %>
                        <% if (bookedHere) { %>
                        <p class="success">This is your current booking.</p>
                        <% } else if (summary.isFull()) { %>
                        <p class="error">Slot full</p>
                        <% } else { %>
                        <form action="<%= ctx %>/ta/interview-booking" method="post">
                            <input type="hidden" name="applicationId" value="<%= escHtml(application.getId()) %>">
                            <input type="hidden" name="slotId" value="<%= escHtml(summary.getSlot().getId()) %>">
                            <input type="hidden" name="action" value="book">
                            <button type="submit" class="btn btn-primary btn-sm"><%= application.getInterviewSlotId() != null && !application.getInterviewSlotId().isEmpty() ? "Change to this slot" : "Book this slot" %></button>
                        </form>
                        <% } %>
                    </article>
                    <% } %>
                </div>
            </section>
            <% } %>
        </main>
        <aside class="right-sidebar">
            <div class="widget-card">
                <div class="widget-title">Tip</div>
                <p class="widget-line">Choose only one slot. The system will stop overlapping interview bookings.</p>
            </div>
        </aside>
    </div>
</div>
</body>
</html>
