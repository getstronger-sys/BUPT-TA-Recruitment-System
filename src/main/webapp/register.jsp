<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jspf/html-esc.jspf" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <%@ include file="/WEB-INF/jspf/viewport.jspf" %>
    <title>Register - BUPT TA Recruitment</title>
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
                <h1 class="auth-title">Create account</h1>
                <p class="auth-subtitle">Register as a Teaching Assistant or Module Organiser</p>
            </div>

            <% String err = (String) request.getAttribute("error"); if (err != null) { %>
            <p class="error"><%= err %></p>
            <% } %>
            <% String ok = (String) request.getAttribute("success"); if (ok != null) { %>
            <p class="success"><%= ok %></p>
            <% } %>
            <%
                boolean otpRequired = request.getAttribute("otpRequired") != null
                        && Boolean.TRUE.equals(request.getAttribute("otpRequired"));
            %>

            <form action="${pageContext.request.contextPath}/register" method="post" class="form form--auth">
                <label for="reg-username">Username *</label>
                <input id="reg-username" type="text" name="username" required autocomplete="username" value="<%= escHtml((String) request.getAttribute("username")) %>">
                <label for="reg-password">Password * (min 4 characters)</label>
                <input id="reg-password" type="password" name="password" required minlength="4" autocomplete="new-password">
                <label for="reg-confirm">Confirm password *</label>
                <input id="reg-confirm" type="password" name="confirmPassword" required autocomplete="new-password">
                <label for="reg-role">Role *</label>
                <select id="reg-role" name="role" required>
                    <option value="TA" <%= !"MO".equals(request.getAttribute("role")) ? "selected" : "" %>>Teaching Assistant (TA)</option>
                    <option value="MO" <%= "MO".equals(request.getAttribute("role")) ? "selected" : "" %>>Module Organiser (MO)</option>
                </select>
                <label for="reg-student-id">Student ID <span class="muted-inline">(required for TA applicants)</span></label>
                <input id="reg-student-id" type="text" name="studentId" value="<%= escHtml((String) request.getAttribute("studentId")) %>" placeholder="e.g. 2023001234" autocomplete="off">
                <label for="reg-email">Email *</label>
                <input id="reg-email" type="email" name="email" required autocomplete="email" value="<%= escHtml((String) request.getAttribute("email")) %>">
                <% if (otpRequired) { %>
                    <label for="reg-otp">Email verification code *</label>
                    <div class="auth-inline-row" style="display:flex; gap:10px; align-items:flex-end;">
                        <div style="flex:1 1 auto;">
                            <input id="reg-otp" type="text" name="emailOtp" inputmode="numeric" autocomplete="one-time-code" placeholder="6-digit code" value="<%= escHtml((String) request.getAttribute("emailOtp")) %>">
                        </div>
                        <button type="submit" name="action" value="sendOtp" style="width:auto; white-space:nowrap;">Send code</button>
                    </div>
                <% } else { %>
                    <input type="hidden" name="emailOtp" value="">
                    <p class="muted-inline" style="margin-top:10px;">Email verification is currently disabled by admin settings.</p>
                <% } %>
                <label for="reg-name">Real name</label>
                <input id="reg-name" type="text" name="realName" placeholder="Your name" autocomplete="name" value="<%= escHtml((String) request.getAttribute("realName")) %>">
                <button type="submit" name="action" value="register">Register</button>
            </form>
            <div class="auth-card-footer">
                <a href="${pageContext.request.contextPath}/index.jsp">Already have an account? Sign in</a>
            </div>
        </div>
    </div>
</div>
<script>
document.addEventListener("DOMContentLoaded", function () {
    // Preserve typed passwords when clicking "Send code" (page reload after POST).
    try {
        var pwd = document.getElementById("reg-password");
        var cpwd = document.getElementById("reg-confirm");
        if (pwd && !pwd.value && sessionStorage.getItem("reg_pwd")) {
            pwd.value = sessionStorage.getItem("reg_pwd");
        }
        if (cpwd && !cpwd.value && sessionStorage.getItem("reg_cpwd")) {
            cpwd.value = sessionStorage.getItem("reg_cpwd");
        }
    } catch (e) { /* ignore */ }

    var messages = {
        valueMissing: "Please fill out this field.",
        typeMismatch: "Please enter a valid value.",
        patternMismatch: "Please match the requested format.",
        tooShort: "Please lengthen this text.",
        tooLong: "Please shorten this text.",
        rangeUnderflow: "Value is too small.",
        rangeOverflow: "Value is too large.",
        stepMismatch: "Please enter a valid value.",
        badInput: "Please enter a valid value."
    };

    document.querySelectorAll("form").forEach(function (form) {
        var sendBtn = form.querySelector("button[name='action'][value='sendOtp']");
        var regBtn = form.querySelector("button[name='action'][value='register']");
        var pwdInput = form.querySelector("#reg-password");
        var cpwdInput = form.querySelector("#reg-confirm");
        var emailInput = form.querySelector("#reg-email");

        function otpCooldownKey() {
            var emailKey = "";
            try { emailKey = (emailInput && emailInput.value) ? emailInput.value.trim().toLowerCase() : ""; } catch (e) {}
            return "otp_cd_register_" + emailKey;
        }

        function startOtpCooldown(seconds) {
            if (!sendBtn) return;
            var totalMs = Math.max(1, seconds || 60) * 1000;
            var until = Date.now() + totalMs;
            try { sessionStorage.setItem(otpCooldownKey(), String(until)); } catch (e) {}
            // Do NOT disable immediately here; disabling the clicked submit button
            // before form submission can prevent the POST from being sent in some browsers.
        }

        function applyOtpCooldown() {
            if (!sendBtn) return;
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

        if (sendBtn && pwdInput && cpwdInput) {
            sendBtn.addEventListener("click", function () {
                try {
                    sessionStorage.setItem("reg_pwd", pwdInput.value || "");
                    sessionStorage.setItem("reg_cpwd", cpwdInput.value || "");
                } catch (e) { /* ignore */ }
                startOtpCooldown(60);
            });
        }
        // Do not clear stored passwords on Register click; registration may fail (e.g. wrong OTP)
        // and the page re-renders. Keep them to avoid losing typed input.
        if (emailInput) {
            emailInput.addEventListener("input", function () {
                // Email changed -> re-evaluate cooldown key.
                applyOtpCooldown();
            });
        }

        applyOtpCooldown();

        var controls = form.querySelectorAll("input, textarea, select");
        controls.forEach(function (control) {
            control.addEventListener("invalid", function () {
                control.setCustomValidity("");
                for (var key in messages) {
                    if (control.validity[key]) {
                        control.setCustomValidity(messages[key]);
                        break;
                    }
                }
            });
            control.addEventListener("input", function () { control.setCustomValidity(""); });
            control.addEventListener("change", function () { control.setCustomValidity(""); });
        });
    });
});
</script>
</body>
</html>
