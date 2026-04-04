<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="bupt.ta.model.Job" %>
<%@ page import="bupt.ta.model.Application" %>
<%@ page import="bupt.ta.ai.AIMatchService" %>
<% List<Object[]> jobsWithApps = (List<Object[]>) request.getAttribute("jobsWithApps"); if (jobsWithApps == null) jobsWithApps = java.util.Collections.emptyList(); %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>My Jobs - MO</title>
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
                <a class="active" href="${pageContext.request.contextPath}/mo/jobs">My Jobs</a>
                <a href="${pageContext.request.contextPath}/mo/post-job">Post Job</a>
            </aside>
        </div>
        <main class="main-panel">
            <h1>My Posted Jobs</h1>
            <div class="context-card">
                <strong>流程</strong>
                <p>申请者 → 设为面试者 → 填写面试通知（站内）→ 录用或拒绝。可批量勾选后统一操作。</p>
            </div>
            <p class="ai-hint"><strong>AI 参考</strong>：同组内按匹配分与工作量排序。</p>
            <% if ("1".equals(request.getParameter("success"))) { %><p class="success">Job posted successfully!</p><% } %>
            <% if ("1".equals(request.getParameter("updated"))) { %><p class="success">Applicant status updated.</p><% } %>
            <% if ("1".equals(request.getParameter("notice"))) { %><p class="success">面试通知已保存（站内消息）。</p><% } %>
            <% String err = request.getParameter("error");
               if (err != null) {
                   String errMsg = err;
                   if ("not_pending".equals(err)) errMsg = "仅申请中状态可设为面试者。";
                   else if ("not_interview".equals(err)) errMsg = "仅面试中状态可录用。";
                   else if ("not_applicant".equals(err)) errMsg = "当前状态不可执行该操作。";
                   else if ("batch_empty".equals(err)) errMsg = "请至少勾选一项。";
                   else if ("invalid_action".equals(err)) errMsg = "无效操作。";
            %><p class="error">Error: <%= errMsg %></p><% } %>

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
                        <h4>Applications (<%= totalApps %>)</h4>
                    </div>

                    <% if (totalApps == 0) { %>
                    <div class="empty-applicants-card">No applications yet.</div>
                    <% } %>

                    <%-- 1. 申请者 --%>
                    <h4 class="applicant-section-title">1. 申请者 (<%= pendingRecs.size() %>)</h4>
                    <form id="<%= batchPendingFormId %>" action="${pageContext.request.contextPath}/mo/batch-applicants" method="post" class="batch-form-hidden">
                        <input type="hidden" name="action" value="toInterview">
                    </form>
                    <% if (!pendingRecs.isEmpty()) { %>
                    <p class="batch-toolbar">
                        <button type="submit" class="btn btn-primary" form="<%= batchPendingFormId %>">将勾选者设为面试者（批量）</button>
                    </p>
                    <% for (AIMatchService.ApplicantRecommendation rec : pendingRecs) {
                        Application a = rec.application;
                        String appliedText = a.getAppliedAt() != null ? a.getAppliedAt().replace("T", " ").replaceFirst("\\..*$", "") : "-";
                        String applicantName = a.getApplicantName() != null ? a.getApplicantName() : a.getApplicantId();
                        boolean hasProfile = rec.profile != null;
                        String skillsText = hasProfile && rec.profile.getSkills() != null && !rec.profile.getSkills().isEmpty() ? String.join(", ", rec.profile.getSkills()) : "Not provided";
                        String missingText = rec.matchResult.missing != null && !rec.matchResult.missing.isEmpty() ? String.join(", ", rec.matchResult.missing) : "No major gaps";
                        boolean hasCv = hasProfile && rec.profile.getCvFilePath() != null && !rec.profile.getCvFilePath().isEmpty();
                        String profileStateText = !hasProfile ? "No profile submitted yet." : (hasCv ? "Profile available, CV uploaded." : "Profile available, CV missing.");
                    %>
                    <article class="applicant-card">
                        <label class="batch-check-label">
                            <input type="checkbox" name="applicationId" value="<%= a.getId() %>" class="batch-checkbox" form="<%= batchPendingFormId %>">
                            <span class="batch-check-hint">勾选参与批量</span>
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
                                    <a href="${pageContext.request.contextPath}/mo/applicant-detail?applicantId=<%= a.getApplicantId() %>" class="mini-link">Full profile</a>
                                    <% if (hasCv) { %>
                                    <a href="${pageContext.request.contextPath}/view-cv?userId=<%= a.getApplicantId() %>" target="_blank" class="mini-link">View CV</a>
                                    <% } else { %><span class="muted-inline">CV not uploaded</span><% } %>
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
                            </section>
                        </div>
                        <div class="applicant-actions">
                            <div class="decision-bar">
                                <form action="${pageContext.request.contextPath}/mo/select-applicant" method="post" class="decision-form decision-form-inline">
                                    <input type="hidden" name="applicationId" value="<%= a.getId() %>">
                                    <input type="text" name="notes" placeholder="Optional notes" class="note-input">
                                    <div class="decision-buttons decision-buttons-inline">
                                        <button type="submit" name="action" value="interview" class="btn btn-primary decision-btn">设为面试者</button>
                                        <button type="submit" name="action" value="reject" class="btn btn-danger decision-btn">拒绝</button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </article>
                    <% } %>
                    <p class="batch-toolbar">
                        <button type="submit" class="btn btn-primary" form="<%= batchPendingFormId %>">将勾选者设为面试者（批量）</button>
                    </p>
                    <% } else { %><p class="muted-inline section-empty">暂无申请者。</p><% } %>

                    <%-- 2. 面试者 --%>
                    <h4 class="applicant-section-title">2. 面试者 (<%= interviewRecs.size() %>)</h4>
                    <% if (!interviewRecs.isEmpty()) { %>
                    <form id="<%= batchNoticeFormId %>" action="${pageContext.request.contextPath}/mo/batch-applicants" method="post" class="notice-form-fields">
                        <input type="hidden" name="action" value="sendNotice">
                        <div class="notice-fields">
                            <label>面试时间 <input type="text" name="interviewTime" placeholder="例如 2026-04-10 14:00" class="note-input notice-input-wide"></label>
                            <label>地点 <input type="text" name="interviewLocation" placeholder="教室或线上链接" class="note-input notice-input-wide"></label>
                            <label class="notice-label-block">考核内容 <textarea name="interviewAssessment" rows="2" placeholder="范围、形式等" class="note-input notice-textarea"></textarea></label>
                        </div>
                    </form>
                    <p class="batch-toolbar">
                        <button type="submit" class="btn btn-success" form="<%= batchNoticeFormId %>">向勾选者发送/更新站内面试通知</button>
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
                            <span class="batch-check-hint">勾选接收通知</span>
                        </label>
                        <div class="applicant-topline">
                            <div class="applicant-title-group">
                                <div class="applicant-name-row">
                                    <h5><%= applicantName %></h5>
                                </div>
                                <div class="applicant-links">
                                    <a href="${pageContext.request.contextPath}/mo/applicant-detail?applicantId=<%= a.getApplicantId() %>" class="mini-link">Full profile</a>
                                    <% if (hasCv) { %>
                                    <a href="${pageContext.request.contextPath}/view-cv?userId=<%= a.getApplicantId() %>" target="_blank" class="mini-link">View CV</a>
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
                            <p><strong>已发通知：</strong> 时间 <%= a.getInterviewTime() != null ? a.getInterviewTime() : "—" %> |
                                地点 <%= a.getInterviewLocation() != null ? a.getInterviewLocation() : "—" %></p>
                            <% if (a.getInterviewAssessment() != null && !a.getInterviewAssessment().isEmpty()) { %>
                            <p class="muted-inline">考核：<%= a.getInterviewAssessment() %></p>
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
                            </section>
                        </div>
                        <div class="applicant-actions">
                            <div class="decision-bar">
                                <form action="${pageContext.request.contextPath}/mo/select-applicant" method="post" class="decision-form decision-form-inline">
                                    <input type="hidden" name="applicationId" value="<%= a.getId() %>">
                                    <input type="text" name="notes" placeholder="Optional notes" class="note-input">
                                    <div class="decision-buttons decision-buttons-inline">
                                        <button type="submit" name="action" value="select" class="btn btn-success decision-btn">录用</button>
                                        <button type="submit" name="action" value="reject" class="btn btn-danger decision-btn">拒绝</button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </article>
                    <% } %>
                    <p class="batch-toolbar">
                        <button type="submit" class="btn btn-success" form="<%= batchNoticeFormId %>">向勾选者发送/更新站内面试通知</button>
                    </p>
                    <% } else { %><p class="muted-inline section-empty">暂无面试者。</p><% } %>

                    <%-- 3. 撤回者 --%>
                    <h4 class="applicant-section-title">3. 撤回者 (<%= withdrawnRecs.size() %>)</h4>
                    <% for (AIMatchService.ApplicantRecommendation rec : withdrawnRecs) {
                        Application a = rec.application;
                        String appliedText = a.getAppliedAt() != null ? a.getAppliedAt().replace("T", " ").replaceFirst("\\..*$", "") : "-";
                        String applicantName = a.getApplicantName() != null ? a.getApplicantName() : a.getApplicantId();
                    %>
                    <article class="applicant-card applicant-card-withdrawn">
                        <div class="applicant-topline">
                            <div class="applicant-title-group">
                                <h5><%= applicantName %></h5>
                                <p class="muted-inline">已撤回申请</p>
                            </div>
                            <span class="status-pill status-pill-rejected">WITHDRAWN</span>
                        </div>
                        <p class="section-copy"><strong>Applied:</strong> <%= appliedText %></p>
                    </article>
                    <% } %>
                    <% if (withdrawnRecs.isEmpty() && totalApps > 0) { %><p class="muted-inline section-empty">无撤回记录。</p><% } %>

                    <%-- 4. 已处理 --%>
                    <h4 class="applicant-section-title">4. 已处理 (<%= outcomeRecs.size() %>)</h4>
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
                    %>
                    <article class="applicant-card">
                        <div class="applicant-topline">
                            <div class="applicant-title-group">
                                <div class="applicant-name-row">
                                    <h5><%= applicantName %></h5>
                                </div>
                                <div class="applicant-links">
                                    <a href="${pageContext.request.contextPath}/mo/applicant-detail?applicantId=<%= a.getApplicantId() %>" class="mini-link">Full profile</a>
                                    <% if (hasCv) { %>
                                    <a href="${pageContext.request.contextPath}/view-cv?userId=<%= a.getApplicantId() %>" target="_blank" class="mini-link">View CV</a>
                                    <% } else { %><span class="muted-inline">CV not uploaded</span><% } %>
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
                    </article>
                    <% } %>
                    <% if (outcomeRecs.isEmpty() && totalApps > 0) { %><p class="muted-inline section-empty">暂无已处理记录。</p><% } %>
                </div>
            </div>
            <% }
               if (jobsWithApps.isEmpty()) { %>
            <p>No jobs posted yet. <a href="${pageContext.request.contextPath}/mo/post-job">Post your first job</a>.</p>
            <% } %>
        </main>
        <aside class="right-sidebar">
            <div class="widget-card">
                <div class="widget-title">MO Dashboard</div>
                <p class="widget-line">申请者 → 面试者 → 站内通知 → 录用/拒绝</p>
            </div>
        </aside>
    </div>
</div>
</body>
</html>
