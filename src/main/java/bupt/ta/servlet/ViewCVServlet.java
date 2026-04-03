package bupt.ta.servlet;

import bupt.ta.model.TAProfile;
import bupt.ta.storage.DataStorage;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ViewCVServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String role = (String) req.getSession().getAttribute("role");
        String currentUserId = (String) req.getSession().getAttribute("userId");
        String targetUserId = req.getParameter("userId");

        if (targetUserId == null || targetUserId.trim().isEmpty()) {
            targetUserId = currentUserId;
        }

        boolean allowed = "ADMIN".equals(role) || "MO".equals(role) || ("TA".equals(role) && targetUserId.equals(currentUserId));
        if (!allowed) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "No permission to view this CV");
            return;
        }

        DataStorage storage = new DataStorage(getServletContext());
        TAProfile profile = storage.getProfileByUserId(targetUserId);
        if (profile == null || profile.getCvFilePath() == null || profile.getCvFilePath().trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "CV not uploaded");
            return;
        }

        String storedPath = profile.getCvFilePath().replace("\\", "/").trim();
        Path file;
        Path storageBase = storage.getBasePath();
        if (Path.of(storedPath).isAbsolute()) {
            file = Path.of(storedPath).normalize();
        } else if (storedPath.startsWith("data/")) {
            // Backward compatibility for old values like data/uploads/xxx.pdf
            file = storageBase.getParent().resolve(storedPath).normalize();
        } else {
            // Current values are relative to data dir, e.g. uploads/xxx.pdf
            file = storageBase.resolve(storedPath).normalize();
        }
        if (!Files.exists(file) || !Files.isRegularFile(file)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "CV file not found");
            return;
        }

        String filename = file.getFileName().toString();
        String contentType = getServletContext().getMimeType(filename);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        resp.setContentType(contentType);
        boolean asDownload = "1".equals(req.getParameter("download")) || "true".equalsIgnoreCase(req.getParameter("download"));
        String disp = asDownload ? "attachment" : "inline";
        resp.setHeader("Content-Disposition", disp + "; filename=\"" + filename.replace("\"", "") + "\"");
        resp.setContentLengthLong(Files.size(file));
        Files.copy(file, resp.getOutputStream());
    }
}
