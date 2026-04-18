<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jspf/html-esc.jspf" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <%@ include file="/WEB-INF/jspf/viewport.jspf" %>
    <title>Forgot Password - BUPT TA Recruitment</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="container container--auth">
    <header class="auth-topbar">
        <span class="brand">BUPT Teaching Assistant Recruitment System</span>
    </header>
    <div class="auth-shell">
        <div class="auth-card auth-card--wide">
            <a class="auth-back" href="${pageContext.request.contextPath}/index.jsp">&larr; Back to sign in</a>
            <div class="auth-card-head">
                <h1 class="auth-title">Reset password</h1>
                <p class="auth-subtitle">Verify your email, then set a new password</p>
            </div>

            <% String err = (String) request.getAttribute("error"); if (err != null) { %>
            <p class="error"><%= escHtml(err) %></p>
            <% } %>
            <% String ok = (String) request.getAttribute("success"); if (ok != null) { %>
            <p class="success"><%= escHtml(ok) %></p>
            <% } %>

            <%
                boolean otpRequired = request.getAttribute("otpRequired") != null
                        && Boolean.TRUE.equals(request.getAttribute("otpRequired"));
            %>

            <% if (!otpRequired) { %>
                <p class="muted-inline">Password reset is currently disabled because email delivery is not enabled/configured by the admin.</p>
            <% } %>

            <form action="${pageContext.request.contextPath}/forgot-password" method="post" class="form form--auth">
                <%@ include file="/WEB-INF/jspf/csrf-hidden.jspf" %>
                <label for="fp-username">Username *</label>
                <input id="fp-username" type="text" name="username" required autocomplete="username" value="<%= escHtml((String) request.getAttribute("username")) %>">

                <label for="fp-email">Email *</label>
                <input id="fp-email" type="email" name="email" required autocomplete="email" value="<%= escHtml((String) request.getAttribute("email")) %>">

                <% if (otpRequired) { %>
                    <label for="fp-otp">Email verification code *</label>
                    <div style="display:flex; gap:10px; align-items:flex-end;">
                        <div style="flex:1 1 auto;">
                            <input id="fp-otp" type="text" name="otp" inputmode="numeric" autocomplete="one-time-code" placeholder="6-digit code" value="<%= escHtml((String) request.getAttribute("otp")) %>">
                        </div>
                        <button type="submit" name="action" value="sendOtp" style="width:auto; white-space:nowrap;">Send code</button>
                    </div>

                    <label for="fp-new">New password * (min 4 characters)</label>
                    <input id="fp-new" type="password" name="newPassword" minlength="4" autocomplete="new-password">
                    <label for="fp-confirm">Confirm new password *</label>
                    <input id="fp-confirm" type="password" name="confirmPassword" autocomplete="new-password">

                    <button type="submit" name="action" value="resetPassword">Update password</button>
                <% } else { %>
                    <button type="submit" disabled>Update password</button>
                <% } %>
            </form>
        </div>
    </div>
</div>
<script>
document.addEventListener("DOMContentLoaded", function () {
    var form = document.querySelector("form");
    if (!form) return;
    var sendBtn = form.querySelector("button[name='action'][value='sendOtp']");
    var emailInput = form.querySelector("#fp-email");
    if (!sendBtn) return;

    function otpCooldownKey() {
        var emailKey = "";
        try { emailKey = (emailInput && emailInput.value) ? emailInput.value.trim().toLowerCase() : ""; } catch (e) {}
        return "otp_cd_forgot_" + emailKey;
    }

    function startOtpCooldown(seconds) {
        var totalMs = Math.max(1, seconds || 60) * 1000;
        var until = Date.now() + totalMs;
        try { sessionStorage.setItem(otpCooldownKey(), String(until)); } catch (e) {}
        // Do NOT disable immediately here; disabling the clicked submit button
        // before form submission can prevent the POST from being sent in some browsers.
    }

    function applyOtpCooldown() {
        var untilRaw = null;
        try { untilRaw = sessionStorage.getItem(otpCooldownKey()); } catch (e) {}
        var until = untilRaw ? parseInt(untilRaw, 10) : 0;
        if (!until || isNaN(until)) {
            sendBtn.disabled = false;
            sendBtn.textContent = "Send code";
            return;
        }
        var left = Math.ceil((until - Date.now()) / 1000);
        if (left <= 0) {
            try { sessionStorage.removeItem(otpCooldownKey()); } catch (e) {}
            sendBtn.disabled = false;
            sendBtn.textContent = "Send code";
            return;
        }
        sendBtn.disabled = true;
        sendBtn.textContent = "Send again (" + left + "s)";
        window.setTimeout(applyOtpCooldown, 250);
    }

    sendBtn.addEventListener("click", function () { startOtpCooldown(60); });
    if (emailInput) {
        emailInput.addEventListener("input", function () { applyOtpCooldown(); });
    }
    applyOtpCooldown();
});
</script>
</body>
</html>

